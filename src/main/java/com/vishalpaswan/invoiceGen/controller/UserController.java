package com.vishalpaswan.invoiceGen.controller;

import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.service.UsersService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/public/invoice-gen/api")
@RequiredArgsConstructor
@CrossOrigin("*")
public class UserController {
    private final UsersService usersService;

    // save new user ... signup
    @PostMapping("/signup")
    public ResponseEntity<?> saveNewUser(@Valid @RequestBody Map<String, String> newUser) {
        String password = newUser.get("password");
        String confirmPassword = newUser.get("confirmPassword");
        if (!password.equals(confirmPassword)) {
            return new ResponseEntity<>("Passwords do not match", HttpStatus.BAD_REQUEST);
        }
        Users newUserData = new Users();
        newUserData.setUsername(newUser.get("username"));
        newUserData.setEmail(newUser.get("email"));
        newUserData.setPassword(newUser.get("password"));
        return usersService.saveNewUser(newUserData);
    }

    // login user
    @PostMapping("/login")
    public ResponseEntity<?> loginUser(@RequestBody Map<String, String> loginData) {
        String username = loginData.get("username");
        String password = loginData.get("password");
        System.out.println(username + " And " + password);
        return usersService.loginUser(username, password);
    }

//    @DeleteMapping("/delete-user")
//    public ResponseEntity<?> deleteUser(@RequestBody Map<String, String> deleteData) {
//        String username = deleteData.get("username");
//        String email = deleteData.get("email");
//        return usersService.deleteUser(username, email);
//    }

}
