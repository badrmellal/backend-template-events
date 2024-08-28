package backend.event_management_system.service;

import backend.event_management_system.exceptions.*;
import backend.event_management_system.jwt.JwtTokenProvider;
import backend.event_management_system.models.Roles;
import backend.event_management_system.models.Users;
import backend.event_management_system.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UsersService implements UserServiceInterface, UserDetailsService {

    private LoginAttemptService loginAttemptService;
    private UsersRepository usersRepository;
    private JwtTokenProvider jwtTokenProvider;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private AuthenticationManager authenticationManager;
    private S3Service s3Service;

    @Autowired
    public UsersService(LoginAttemptService loginAttemptService, UsersRepository usersRepository, JwtTokenProvider jwtTokenProvider, BCryptPasswordEncoder bCryptPasswordEncoder, @Lazy AuthenticationManager authenticationManager, S3Service s3Service) {
        this.loginAttemptService = loginAttemptService;
        this.usersRepository = usersRepository;
        this.jwtTokenProvider = jwtTokenProvider;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.authenticationManager = authenticationManager;
        this.s3Service = s3Service;
    }

    @Override
    public Users register(String username, String email, String password, String roleName) throws UsernameExistException, EmailExistException {

        validateNewUsernameAndEmail(null, username, email );
        Roles role = getRoleByName(roleName);

            Users user = new Users();
            user.setUsername(username);
            user.setEmail(email);
            user.setJoinDate(new Date());
            user.setUserPassword(bCryptPasswordEncoder.encode(password));
            user.setRole(role.name());
            user.setAuthorities(role.getAuthorities());
            user.setProfileImageUrl(getTemporaryProfileImageUrl());

            usersRepository.save(user);
        return user;
    }

    @Override
    public String login(String email, String password) throws UserNotFoundException, EmailNotFoundException {
        Users user = findUserByEmail(email);

        if (loginAttemptService.hasExceededMaxNumberOfAttempts(email)){
            throw new RuntimeException("Sorry you have exceeded the maximum login attempts. Try again later!");
        }
        if (!bCryptPasswordEncoder.matches(password, user.getPassword())){
            loginAttemptService.addUserToLoginAttemptCache(email);
            throw new RuntimeException("Invalid Password. Please try again.");
        }

        Authentication auth = authenticationManager
                .authenticate(new UsernamePasswordAuthenticationToken(email, password));
        SecurityContextHolder.getContext().setAuthentication(auth);
        loginAttemptService.removeUserFromLoginAttemptCache(email);
        return jwtTokenProvider.generateToken(auth);
    }


    @Override
    public List<Users> getUsers() {
        return usersRepository.findAll();
    }

    @Override
    public Users findUserByUsername(String username) throws UsernameNotFoundException {
        return usersRepository.findUserByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("This username cannot be found. Please try again!"));
    }

    @Override
    public Users findUserByEmail(String email) throws EmailNotFoundException {
        return usersRepository.findUserByEmail(email)
                .orElseThrow(() -> new EmailNotFoundException("This email cannot be found. Please try again!"));
    }

    @Override
    public Users updateProfileImage(String username, MultipartFile profileImage) throws NotValidImageException, UserNotFoundException {

        Users user = usersRepository.findUserByUsername(username)
                .orElseThrow(() -> new UserNotFoundException(username + " username doesn't exist. Please try again."));
       if (!profileImage.isEmpty() && profileImage != null){
           if (!isAnImage(profileImage)){
               throw new NotValidImageException("This image format can't be saved. Please try another one.");
           }
           String profileImageUrl = s3Service.uploadProfileImage(username, profileImage);
           user.setProfileImageUrl(profileImageUrl);
           usersRepository.save(user);
       }
        return user;
    }

    @Override
    public UserDetails loadUserByUsername(String email) {
        try {
            return usersRepository.findUserByEmail(email)
                    .orElseThrow(()-> new EmailNotFoundException("This email doesn't exist. Please try again!"));
        } catch (EmailNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public Users updateUserRole(Long id, String assignedRole) throws EmailNotFoundException {
         Users user = usersRepository.getReferenceById(id);
         user.setRole(assignedRole);
        usersRepository.save(user);
        return user;
    }

    @Override
    public void deleteUser(Long id) throws UserNotFoundException{
        try{
            usersRepository.deleteById(id);
        } catch (EmptyResultDataAccessException exception){
            throw new UserNotFoundException("Error with" + id + "not found." + exception);
        }
    }

    private Roles getRoleByName(String roleName){
        return Optional.of(Roles.valueOf(roleName))
                .orElseThrow(()-> new IllegalArgumentException("Invalid Role" + roleName));
    }

    private String getTemporaryProfileImageUrl() {
        return "https://i.postimg.cc/L534NY7n/profile-avatar.png";
    }

    private boolean isAnImage(MultipartFile file){
        String contentType = file.getContentType();
        return contentType.equals("image/png") || contentType.equals("image/jpeg");
    }

    private void validateNewUsernameAndEmail(String currentUsername, String newUsername, String newEmail) throws UsernameExistException, EmailExistException {
        Users userByUsername = usersRepository.findUserByUsername(newUsername).orElse(null);
        Users userByEmail = usersRepository.findUserByEmail(newEmail).orElse(null);

        if (userByUsername != null && !userByUsername.getUsername().equals(currentUsername)) {
            throw new UsernameExistException("Username already exists");
        }
        if (userByEmail != null && !userByEmail.getEmail().equals(currentUsername)) {
            throw new EmailExistException("Email already exists");
        }
    }
}
