package com.example.healthcheckapi.controller;

import com.example.healthcheckapi.model.User;
import com.example.healthcheckapi.repository.UserRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class UserController {
    @Autowired
    UserRepository userRepository;

    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;

    public UserController() {
        System.out.println("in user controller constructor");
    }

    @PostMapping("/user")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {

            System.out.println("In post");

            if(user==null || user.getPassword() == null || user.getFirst_name() == null ||
                    user.getUsername() == null || user.getLast_name() == null)
            {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }

            // check if already exists

            System.out.println("calling get user");

            System.out.println("Setting for post request");
            Optional<User> u = userRepository.findByUsername(user.getUsername());
            System.out.println(u);

            System.out.println("checking if user is present");
            if (u.isPresent()) {
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }

            // encrypt password
            String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());

            user.setPassword(encodedPassword);
            System.out.println("encoded: " + encodedPassword);

            User _user = userRepository
                    .save(new User(user.getFirst_name(), user.getLast_name(), user.getPassword(), user.getUsername()));


            System.out.println("user saved in db");

            return new ResponseEntity<>(_user, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("exception: " +e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

//    @GetMapping("/user/self")
    @GetMapping("/user/{userid}")
    public ResponseEntity<User> getUserByEmail(HttpServletRequest request, @PathVariable int userid) {

        try{
            String upd = request.getHeader("authorization");
            if (upd == null || upd.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String pair = new String(Base64.decodeBase64(upd.substring(6)));
            String userName = pair.split(":")[0];
            String password = pair.split(":")[1];

            System.out.println("username: " + userName);
            System.out.println("password: " + password);


            System.out.println("In get");
            Optional<User> inputUser = userRepository.findByUsername(userName);
            if (inputUser.get().getId() != userid)
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);

            if (inputUser.isPresent()) {
                if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {
                    return new ResponseEntity<>(inputUser.get(), HttpStatus.OK);
                }else {
                    System.out.println("Password does not match");
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } else {
                System.out.println("User Not Found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch(Exception e)
        {
            System.out.println("Exception:"+e);
        }
        System.out.println("End - User Not Found");
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }

    @PutMapping("/user/{userid}")
    public ResponseEntity<String> updateUser(@PathVariable int userid, @RequestBody User user, HttpServletRequest request) {

        System.out.println("In put");

        if (user == null) {
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        if ((user.getFirst_name() == null || user.getFirst_name().isEmpty())
                && (user.getLast_name() == null || user.getLast_name().isEmpty())
                && (user.getPassword() == null || user.getPassword().isEmpty())) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        if (user.getUsername() != null || user.getId() != 0) { //null
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        String upd = request.getHeader("authorization");
        if (upd == null || upd.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String pair = new String(Base64.decodeBase64(upd.substring(6)));
        String userName = pair.split(":")[0];
        String password = pair.split(":")[1];

        System.out.println("username: " + userName);
        System.out.println("password: " + password);
        
        Optional<User> inputUser = userRepository.findByUsername(userName);
        if (inputUser.get().getId() != userid)
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        

        // validate password
        if (inputUser.isPresent()) {
            if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {// update

                User updatedUser = inputUser.get();
                if(user.getFirst_name() != null)
                    updatedUser.setFirst_name(user.getFirst_name());
                if(user.getLast_name() != null)
                    updatedUser.setLast_name(user.getLast_name());
                if(user.getPassword() != null)
                    updatedUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

                updatedUser.setAccount_updated(OffsetDateTime.now().toString());
                
                userRepository.save(updatedUser);
                
                return new ResponseEntity<>("Update success", HttpStatus.NO_CONTENT);

            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } else {

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}
