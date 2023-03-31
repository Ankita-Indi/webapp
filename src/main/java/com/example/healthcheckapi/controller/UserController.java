package com.example.healthcheckapi.controller;

import com.example.healthcheckapi.model.User;
import com.example.healthcheckapi.repository.UserRepository;
import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.timgroup.statsd.StatsDClient;

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

    @Autowired
    private StatsDClient statsDClient;

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController() {
        System.out.println("in user controller constructor");
    }

    @PostMapping("/user")
    public ResponseEntity<User> createUser(@RequestBody User user) {
        try {

            System.out.println("In post");

            statsDClient.incrementCounter("endpoint.user.api.post");
            logger.info("endpoint.user.api.post hit successfully");

            if(user==null || user.getPassword() == null || user.getFirst_name() == null ||
                    user.getUsername() == null || user.getLast_name() == null || user.getAccount_created() != null ||
                    user.getAccount_updated() != null)
            {
                logger.error("endpoint.user.api.post - Incorrect input");
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }


            // check if already exists

            System.out.println("calling get user");

            System.out.println("Setting for post request");
            Optional<User> u = userRepository.findByUsername(user.getUsername());
            System.out.println(u);

            System.out.println("checking if user is present");
            if (u.isPresent()) {
                logger.error("endpoint.user.api.post User is already present");
                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
            }

            // encrypt password
            String encodedPassword = bCryptPasswordEncoder.encode(user.getPassword());


            user.setPassword(encodedPassword);
            System.out.println("encoded: " + encodedPassword);

            User _user = userRepository
                    .save(new User(user.getFirst_name(), user.getLast_name(), user.getPassword(), user.getUsername()));


            System.out.println("user saved in db");
            logger.info("endpoint.user.api.post - User saved");
            return new ResponseEntity<>(_user, HttpStatus.CREATED);
        } catch (Exception e) {
            System.out.println("exception: " +e);
            logger.error("endpoint.user.api.post - exception: " +e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

//    @GetMapping("/user/self")
    @GetMapping("/user/{userid}")
    public ResponseEntity<User> getUserByEmail(HttpServletRequest request, @PathVariable int userid) {
        statsDClient.incrementCounter("endpoint.user.self.api.get");
        logger.info("endpoint.user.self.api.get hit successfully");

        try{
            String upd = request.getHeader("authorization");
            if (upd == null || upd.isEmpty()) {
                logger.error("endpoint.user.self.api.get - User Details not provided");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String pair = new String(Base64.decodeBase64(upd.substring(6)));
            String[] splitpair = pair.split(":");
            if(splitpair.length != 2){
                logger.error("endpoint.user.self.api.get - User Details not provided");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            String userName = pair.split(":")[0];
            String password = pair.split(":")[1];



            System.out.println("username: " + userName);
            System.out.println("password: " + password);


            System.out.println("In get");
            Optional<User> inputUser = userRepository.findByUsername(userName);


            if (inputUser.isPresent()) {
                logger.info("endpoint.user.self.api.get - User Found");
                if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {
                    if (inputUser.get().getId() != userid){
                        logger.error("endpoint.user.self.api.get - Incorrect User");
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                    logger.info("endpoint.user.self.api.put - User updated successfully");
                    return new ResponseEntity<>(inputUser.get(), HttpStatus.OK);
                }else {
                    System.out.println("Password does not match");
                    logger.error("endpoint.user.self.api.get - Incorrect Password");
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } else {
                System.out.println("User Not Found");
                logger.error("endpoint.user.self.api.put - User Not Found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch(Exception e) {
            System.out.println("Exception:" + e);
            logger.error("endpoint.user.self.api.get - exception: " +e);
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @PutMapping("/user/{userid}")
    public ResponseEntity<String> updateUser(@PathVariable int userid, @RequestBody User user, HttpServletRequest request) {
        try {
            System.out.println("In put");

            statsDClient.incrementCounter("endpoint.user.self.api.put");
            logger.info("endpoint.user.self.api.put hit successfully");

            if (user == null) {
                logger.error("endpoint.user.self.api.put - No user given");
                return new ResponseEntity<>(HttpStatus.NO_CONTENT);
            }
            if ((user.getFirst_name() == null || user.getFirst_name().isEmpty())
                    || (user.getLast_name() == null || user.getLast_name().isEmpty())
                    || (user.getPassword() == null || user.getPassword().isEmpty())
                    || (user.getAccount_created() != null)
                    || user.getAccount_updated() != null
                    || user.getUsername() == null || user.getUsername().isEmpty()) {
                System.out.println("Something is null or absent");
                logger.error("endpoint.user.self.api.put - Missing data");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

//            if (user.getUsername() != null || user.getId() != 0) { //null
//                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
//            }
            String upd = request.getHeader("authorization");
            if (upd == null || upd.isEmpty()) {
                logger.error("endpoint.user.self.api.put - Missing Credentials");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String pair = new String(Base64.decodeBase64(upd.substring(6)));
            String[] splitpair = pair.split(":");
            if(splitpair.length != 2){
                logger.error("endpoint.user.self.api.put - Incorrect Credentials");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            String userName = pair.split(":")[0];
            String password = pair.split(":")[1];

            System.out.println("username: " + userName);
            System.out.println("password: " + password);

            Optional<User> inputUser = userRepository.findByUsername(userName);



            // validate password
            if (inputUser.isPresent()) {
                if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {
                    if (inputUser.get().getId() != userid) {
                        logger.error("endpoint.user.self.api.put - Forbidden Access");
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                    if(!userName.matches(user.getUsername())){
                        System.out.println("Username mismatch");
                        logger.error("endpoint.user.self.api.put - Username mismatch");
                        return new ResponseEntity<>( HttpStatus.BAD_REQUEST);
                    }

                    User updatedUser = inputUser.get();
                    if (user.getFirst_name() != null)
                        updatedUser.setFirst_name(user.getFirst_name());
                    if (user.getLast_name() != null)
                        updatedUser.setLast_name(user.getLast_name());
                    if (user.getPassword() != null)
                        updatedUser.setPassword(bCryptPasswordEncoder.encode(user.getPassword()));

                    updatedUser.setAccount_updated(OffsetDateTime.now().toString());

                    userRepository.save(updatedUser);
                    logger.info("endpoint.user.self.api.put - User updated successfully");

                    return new ResponseEntity<>(HttpStatus.NO_CONTENT);

                } else {
                    logger.error("endpoint.user.self.api.put - Incorrect Password");
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } else {
                logger.error("endpoint.user.self.api.put - User Not Found");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        }
        catch(Exception e) {
            System.out.println("Exception:" + e);
            logger.error("endpoint.user.self.api.put - User Not Found");
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }
}
