package backend.event_management_system.service;

import backend.event_management_system.exceptions.*;
import backend.event_management_system.models.Roles;
import backend.event_management_system.models.Users;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserServiceInterface {
    Users register(String username, String email, String password, Roles role) throws UsernameExistException, EmailExistException;
    List<Users> getUsers();
    Users findUserByUsername(String username) throws UsernameNotFoundException;
    Users findUserByEmail(String email) throws EmailNotFoundException;
    Users updateProfileImage(String username, MultipartFile profileImage) throws NotValidImageException, UserNotFoundException;

}
