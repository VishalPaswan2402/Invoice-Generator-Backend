package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UsersService {
    @Autowired
    private final UserRepository userRepository;

    // validate email
    public boolean isValidEmail(String email) {
        String regex = "^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$";
        return email != null && email.matches(regex);
    }

    // save new user
    public ResponseEntity<?> saveNewUser(Users newUser) {
        if (userRepository.existsByUsername(newUser.getUsername())) {
            return new ResponseEntity<>("Username already exist!", HttpStatus.BAD_REQUEST);
        }
        if (!isValidEmail(newUser.getEmail())) {
            return new ResponseEntity<>("Invalid email.", HttpStatus.BAD_REQUEST);
        }
        Users savedNewUser = userRepository.save(newUser);
        return new ResponseEntity<>(savedNewUser, HttpStatus.CREATED);
    }

    // login user
    public ResponseEntity<?> loginUser(String username, String password) {
        if (username.isEmpty() || password.isEmpty()) {
            return new ResponseEntity<>("Enter both username and password!", HttpStatus.BAD_REQUEST);
        }
        Optional<Users> userFound = userRepository.findByUsername(username);

        if (userFound.isEmpty()) {
            return new ResponseEntity<>("User not found.", HttpStatus.NOT_FOUND);
        } else {
            if (!userFound.get().getPassword().equals(password)) {
                return new ResponseEntity<>("Invalid password", HttpStatus.UNAUTHORIZED);
            } else {
                return new ResponseEntity<>(userFound, HttpStatus.OK);
            }
        }
    }

    // delete user
    public ResponseEntity<?> deleteUser(String username, String email) {
        Optional<Users> currentUser = userRepository.findByUsername(username);
        if (currentUser.isEmpty()) {
            return new ResponseEntity<>("No user found!", HttpStatus.BAD_REQUEST);
        }
        if (!currentUser.get().getEmail().equals(email)) {
            return new ResponseEntity<>("No user found!", HttpStatus.BAD_REQUEST);
        }
        userRepository.deleteById(currentUser.get().getId());
        return new ResponseEntity<>("User deleted successfully!", HttpStatus.OK);
    }

}
