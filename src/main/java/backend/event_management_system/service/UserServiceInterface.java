package backend.event_management_system.service;

import backend.event_management_system.exceptions.EmailExistException;
import backend.event_management_system.exceptions.NotValidImageException;
import backend.event_management_system.exceptions.UserNotFoundException;
import backend.event_management_system.exceptions.UsernameExistException;
import backend.event_management_system.models.Users;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

public interface UserServiceInterface {
    Users register(String username, String email) throws UsernameExistException, EmailExistException;
    List<Users> getUsers();
    Users findUserByUsername(String username);
    Users findUserByEmail(String email);
    Users updateProfileImage(String username, MultipartFile profileImage) throws NotValidImageException, UserNotFoundException;

}
