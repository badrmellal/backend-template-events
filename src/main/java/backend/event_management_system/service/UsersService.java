package backend.event_management_system.service;

import backend.event_management_system.exceptions.EmailExistException;
import backend.event_management_system.exceptions.NotValidImageException;
import backend.event_management_system.exceptions.UserNotFoundException;
import backend.event_management_system.exceptions.UsernameExistException;
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

import java.util.List;

@Service
@Transactional
public class UsersService implements UserServiceInterface, UserDetailsService {

    private LoginAttemptService loginAttemptService;
    private UsersRepository usersRepository;
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    @Autowired
    public UsersService(LoginAttemptService loginAttemptService, BCryptPasswordEncoder bCryptPasswordEncoder, UsersRepository usersRepository) {
        this.loginAttemptService = loginAttemptService;
        this.bCryptPasswordEncoder = bCryptPasswordEncoder;
        this.usersRepository = usersRepository;
    }

    @Override
    public Users register(String username, String email) throws UsernameExistException, EmailExistException {
        return null;
    }

    @Override
    public List<Users> getUsers() {
        return List.of();
    }

    @Override
    public Users findUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }

    @Override
    public Users findUserByEmail(String email) {
        return null;
    }

    @Override
    public Users updateProfileImage(String username, MultipartFile profileImage) throws NotValidImageException, UserNotFoundException {
        return null;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return null;
    }
}
