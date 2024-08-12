package backend.event_management_system.service;

import backend.event_management_system.exceptions.*;
import backend.event_management_system.models.Roles;
import backend.event_management_system.models.Users;
import backend.event_management_system.repository.UsersRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.Date;
import java.util.List;

@Service
@Transactional
public class UsersService implements UserServiceInterface, UserDetailsService {

    private LoginAttemptService loginAttemptService;
    private UsersRepository usersRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;
    private S3Service s3Service;

    @Autowired
    public UsersService(LoginAttemptService loginAttemptService, BCryptPasswordEncoder bCryptPasswordEncoder, UsersRepository usersRepository) {
        this.loginAttemptService = loginAttemptService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.usersRepository = usersRepository;
    }

    @Override
    public Users register(String username, String email, String password, Roles role) throws UsernameExistException, EmailExistException {
            validateNewUsernameAndEmail(null, username, email );
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
                .orElseThrow(() -> new UsernameNotFoundException(username + "username doesn't exist. Please try again."));
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
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return usersRepository.findUserByUsername(username)
                .orElseThrow(()-> new UsernameNotFoundException("This username doesn't exist. Please try again!"));
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
