package com.vishalpaswan.invoiceGen.service;

import com.vishalpaswan.invoiceGen.entity.Users;
import com.vishalpaswan.invoiceGen.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
@RequiredArgsConstructor
public class UsersService {
    @Autowired
    private final UserRepository userRepository;

    // save new user
    public ResponseEntity<?> saveNewUser(Users newUser){
        if(userRepository.existsByUsername(newUser.getUsername())){
            return new ResponseEntity<>("Username already exist!",HttpStatus.BAD_REQUEST);
        }
        Users savedNewUser=userRepository.save(newUser);
        return new ResponseEntity<>(newUser, HttpStatus.CREATED);
    }

    // login user
    public ResponseEntity<?> loginUser(String username,String password){
        if(username.isEmpty() || password.isEmpty()){
            return new ResponseEntity<>("Enter both username and password!",HttpStatus.BAD_REQUEST);
        }
        Users userFound=userRepository.findByUsername(username);
        if(userFound==null){
            return new ResponseEntity<>("User not found.",HttpStatus.NOT_FOUND);
        }
        else{
            if(!userFound.getPassword().equals(password)){
                return new ResponseEntity<>("Invalid password",HttpStatus.UNAUTHORIZED);
            }
            else{
                return new ResponseEntity<>(userFound,HttpStatus.OK);
            }
        }
    }

}
