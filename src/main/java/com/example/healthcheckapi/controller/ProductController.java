package com.example.healthcheckapi.controller;

import com.example.healthcheckapi.model.Product;
import com.example.healthcheckapi.model.Image;
import com.example.healthcheckapi.model.User;
import com.example.healthcheckapi.repository.ImageRepository;
import com.example.healthcheckapi.repository.ProductRepository;
import com.example.healthcheckapi.repository.UserRepository;
import com.example.healthcheckapi.service.ImageStorageService;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import com.timgroup.statsd.StatsDClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.multipart.MultipartFile;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

//import com.alibaba.fastjson.JSON;
//import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonAlias;
import org.springframework.boot.jackson.JsonObjectDeserializer;







import javax.servlet.http.HttpServletRequest;
import java.io.File;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/v1")
public class ProductController {
    @Autowired
    ProductRepository productRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    BCryptPasswordEncoder bCryptPasswordEncoder;
     @Autowired
    ImageRepository imageRepository;

    @Autowired
    private StatsDClient statsDClient;

    @Autowired
    ImageStorageService service;

    private final static Logger logger = LoggerFactory.getLogger(UserController.class);

    public ProductController() {
        System.out.println("in product controller constructor");
    }

    @PostMapping("/product")
    public ResponseEntity<Product> createProduct(@RequestBody Product product, HttpServletRequest request) {
        System.out.println("In post");
        try {
            statsDClient.incrementCounter("endpoint.product.self.api.post");
            logger.info("endpoint.product.self.api.post hit successfully");
            String upd = request.getHeader("authorization");
            System.out.println(upd);
            if (upd == null || upd.isEmpty()) {
                logger.error("endpoint.product.self.api.post - Missing Credentials");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String pair = new String(Base64.decodeBase64(upd.substring(6)));
            String userName = pair.split(":")[0];
            String password = pair.split(":")[1];
            System.out.println("username: " + userName);
            System.out.println("password: " + password);
            Optional<User> inputUser = userRepository.findByUsername(userName);

            if (inputUser.isPresent()) {
                if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {
                    System.out.println("Password matched");
                    if (product == null || product.getName() == null || product.getDescription() == null ||
                            product.getSku() == null || product.getManufacturer() == null || product.getQuantity() < 0 ||
                            product.getQuantity() > 100 || product.getQuantity() == null ||
                            product.getDate_last_updated() != null || product.getDate_added() != null ||
                            product.getOwner_user_id() != null)
                    {
                        logger.error("endpoint.product.self.api.post - Missing data");
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                    }

                    // check if already exists

                    System.out.println("Setting for post request");
                    Optional<Product> u = productRepository.findBySku(product.getSku());
                    System.out.println(u);


                    System.out.println("checking if product sku is present");

                    if (u.isPresent()) {
                        System.out.println("In sku present check");
                        logger.error("endpoint.product.self.api.post - User Absent");
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                    }

                    Product p = new Product(product.getName(), product.getDescription(), product.getSku(),
                            product.getManufacturer(), product.getQuantity(), inputUser.get().getId());


                    Product _product = productRepository.save(p);


                    System.out.println("product saved in db");
                    logger.info("endpoint.product.self.api.post Changes saved");
                    return new ResponseEntity<>(_product, HttpStatus.CREATED);
                } else {
                    logger.error("endpoint.product.self.api.post - Incorrect Password");
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } else {
                System.out.println("User does not exist");
                logger.error("endpoint.product.self.api.post - User Absent");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("exception: " + e);
            logger.error("endpoint.product.self.api.post - exception: " +e);
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);

        }
    }


    @PutMapping("/product/{productId}")
//    @RequestMapping(value = "/product/{productId}", method = {RequestMethod.DELETE})
    public ResponseEntity<Product> updateProduct(@PathVariable int productId, @RequestBody Product product, HttpServletRequest request) {

        System.out.println("In put");
//
        try {
            statsDClient.incrementCounter("endpoint.product.self.api.put");
            logger.info("endpoint.product.self.api.put hit successfully");
            String upd = request.getHeader("authorization");
            System.out.println(upd);
            if (upd == null || upd.isEmpty()) {
                logger.error("endpoint.product.self.api.put - Missing Credentials");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            if ((product.getName() == null || product.getName().isEmpty())
                    || (product.getManufacturer() == null || product.getManufacturer().isEmpty())
                    || (product.getSku() == null || product.getSku().isEmpty())
                    || product.getDate_added() != null
                    || product.getDate_last_updated() != null
                    || product.getOwner_user_id() != null) {
                System.out.println("Something is null or absent");
                logger.error("endpoint.product.self.api.put - Missing data");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            String pair = new String(Base64.decodeBase64(upd.substring(6)));
            String userName = pair.split(":")[0];
            String password = pair.split(":")[1];
            System.out.println("username: " + userName);
            System.out.println("password: " + password);
            Optional<User> inputUser = userRepository.findByUsername(userName);
            Optional<Product> inputProduct = productRepository.findById(productId);

            if (inputUser.isPresent()) {
                if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {
                    System.out.println("Password matched");
                    if (inputProduct.isPresent()) {
                        if (inputUser.get().getId() != inputProduct.get().getOwner_user_id()){
                            logger.error("endpoint.product.self.api.put - Forbidden Access");
                            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
                        }

                        if (!inputProduct.get().getSku().matches(product.getSku())) {
                            System.out.println("Cannot change Sku");
                            logger.error("endpoint.product.self.api.put - Sku update not possible");
                            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                        }

                        if (product.getQuantity() != null && product.getQuantity() >= 0 && product.getQuantity() <= 100) {
                            Product updatedProduct = inputProduct.get();
                            if (product.getName() != null)
                                updatedProduct.setName(product.getName());
                            if (product.getDescription() != null)
                                updatedProduct.setDescription(product.getDescription());
                            if (product.getManufacturer() != null)
                                updatedProduct.setManufacturer(product.getManufacturer());
                            updatedProduct.setQuantity(product.getQuantity());
                            updatedProduct.setDate_last_updated(OffsetDateTime.now().toString());

                            productRepository.save(updatedProduct);
                            logger.info("endpoint.product.self.api.put Changes saved");
                            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
                        } else {
                            logger.error("endpoint.product.self.api.put - Incorrect Input");
                            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                        }
                    } else {
                        logger.error("endpoint.product.self.api.put - Product Absent");
                        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                    }
                } else {
                    logger.error("endpoint.product.self.api.put - Incorrect Password");
                    return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
                }
            } else {
                System.out.println("User not there");
                logger.error("endpoint.product.self.api.put - User Absent");
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            System.out.println("Exception:" + e);
            logger.error("endpoint.product.self.api.put - exception: " +e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

    }

    @PatchMapping("/product/{productId}")
    public ResponseEntity<Product> updateProductPatch(@PathVariable int productId, HttpServletRequest request, @RequestBody Product product) {

        System.out.println("In patch");
        try {
            statsDClient.incrementCounter("endpoint.product.self.api.patch");
            logger.info("endpoint.product.self.api.patch hit successfully");
            String upd = request.getHeader("authorization");
            System.out.println(upd);
            if (upd == null || upd.isEmpty()) {
                logger.error("endpoint.product.self.api.put - Missing Credentials");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String pair = new String(Base64.decodeBase64(upd.substring(6)));
            String userName = pair.split(":")[0];
            String password = pair.split(":")[1];
            System.out.println("username: " + userName);
            System.out.println("password: " + password);
            Optional<User> inputUser = userRepository.findByUsername(userName);
            Optional<Product> inputProduct = productRepository.findById(productId);


            if (inputUser.isPresent()) {
                if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {
                    System.out.println("Password matched");
                    if (inputProduct.isPresent()) {
                        Product updatedProduct = inputProduct.get();
                        if (product.getDate_added() != null
                                || product.getDate_last_updated() != null
                                || product.getOwner_user_id() != null){
                            System.out.println("Cannot update dates or owner");
                            logger.error("endpoint.product.self.api.patch - Incorrect Updates");
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

                        }
                        if (inputUser.get().getId() != inputProduct.get().getOwner_user_id()) {
                            logger.error("endpoint.product.self.api.patch - Incorrect Act");
                            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
                        }
                        if(product.getName() != null){
                            System.out.println("Test: " + product.getName());
                            updatedProduct.setName(product.getName());}
                        else //(product.getName() == null)
                            updatedProduct.setName(inputProduct.get().getName());
                        if(product.getManufacturer() != null)
                            updatedProduct.setManufacturer(product.getManufacturer());
                        else //if(product.getManufacturer() == null)
                            updatedProduct.setManufacturer(inputProduct.get().getManufacturer());
                        if(product.getManufacturer() != null)
                            updatedProduct.setManufacturer(product.getManufacturer());
                        else //if(product.getManufacturer() == null)
                            updatedProduct.setManufacturer(inputProduct.get().getManufacturer());
                        if(product.getDescription() != null)
                            updatedProduct.setDescription(product.getDescription());
                        else //if(product.getManufacturer() == null)
                            updatedProduct.setDescription(inputProduct.get().getDescription());
                        if(product.getSku() != null) {
                                if (!inputProduct.get().getSku().matches(product.getSku())) {
                                    System.out.println("Cannot change Sku");
                                    logger.error("endpoint.product.self.api.patch - Sku Update Impossible");
                                    return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                                }
                                else{
                                    updatedProduct.setSku(product.getSku());
                                }
                        }
                        else
                            updatedProduct.setSku(inputProduct.get().getSku());
                        if(product.getQuantity() == null)
                            updatedProduct.setQuantity(inputProduct.get().getQuantity());
                        else {
                            if (product.getQuantity() >= 0 && product.getQuantity() <= 100)
                                updatedProduct.setQuantity(product.getQuantity());
                            else {
                                logger.error("endpoint.product.self.api.patch - Quantity Incorrect");
                                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                            }
                        }
                        updatedProduct.setDate_last_updated(OffsetDateTime.now().toString());
                        productRepository.save(updatedProduct);
                        logger.info("endpoint.user.self.api.get - Details Updated");
                        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
                    }
                    else {
                        System.out.println("Product not found");
                        logger.error("endpoint.product.self.api.patch - Quantity Incorrect");
                        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                    }
                }
                else {
                    System.out.println("Password incorrect");
                    logger.error("endpoint.product.self.api.patch - Password Incorrect");
                    return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
                }
            }
            else {
                System.out.println("User not there");
                logger.error("endpoint.product.self.api.patch - User Absent");
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }


        }
        catch (Exception e) {
            System.out.println("Exception:" + e);
            logger.error("endpoint.product.self.api.put - exception: " +e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

    }






    @GetMapping(value = "/product/{product_id}/image/{image_id}")
    public ResponseEntity<Image> getImage(HttpServletRequest request, @PathVariable int product_id, @PathVariable int image_id) {
        try {
            System.out.println("product/{product_id}/image/{image_id}");
            statsDClient.incrementCounter("endpoint.user.self.pic.api.get");
            logger.info("endpoint.user.self.pic.api.get hit successfully");

            //check user credentials and get userid
            String upd = request.getHeader("authorization");
            if (upd == null || upd.isEmpty()) {
                logger.error("endpoint.image.self.api.get - Missing Credentials");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String pair = new String(Base64.decodeBase64(upd.substring(6)));
            String userName = pair.split(":")[0];
            String password = pair.split(":")[1];

            System.out.println("username: " + userName);
            System.out.println("password: " + password);

            Optional<User> inputUser = userRepository.findByUsername(userName);
            Optional<Product> inputProduct = productRepository.findById(product_id);
            Optional<Image> img = imageRepository.findById(image_id);
            //Optional<Image> img;
            if (inputUser.isPresent()) {
                if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {

                    System.out.println("Password matched");
                    if (inputProduct.isPresent()) {
                        logger.info("endpoint.user.self.api.get - User Found");
                        if (inputUser.get().getId() != inputProduct.get().getOwner_user_id() ) {
                            logger.error("endpoint.image.self.api.get - Incorrect Updates");
                            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                        }

                        if (img.isPresent()) {
                            if(img.get().getProductId() != product_id){
                                logger.error("endpoint.image.self.api.get - Incorrect Updates");
                                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                            }
                            logger.info("endpoint.image.self.api.get - Details fetched");
                            return new ResponseEntity<>(img.get(), HttpStatus.OK);
                        } else {
                            logger.error("endpoint.image.self.api.get - Image Absent");
                            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                        }
                    } else {
                        logger.error("endpoint.image.self.api.get - Product Absent");
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                } else {
                    logger.error("endpoint.image.self.api.get - Password Incorrect");
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            }
            logger.error("endpoint.image.self.api.get - User Absent");
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }catch (Exception e) {
            System.out.println("Exception:" + e);
            logger.error("endpoint.product.self.api.put - exception: " +e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }




    @GetMapping(value = "/product/{product_id}/image")
    public ResponseEntity<?> getImage(HttpServletRequest request, @PathVariable int product_id) {
        try {
            statsDClient.incrementCounter("endpoint.image.self.api.get");
            logger.info("endpoint.image.self.api.get hit successfully");

            System.out.println("product/{product_id}/image/");

            //check user credentials and get userid
            String upd = request.getHeader("authorization");
            if (upd == null || upd.isEmpty()) {
                logger.error("endpoint.image.self.api.get - Missing Credentials");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String pair = new String(Base64.decodeBase64(upd.substring(6)));
            String userName = pair.split(":")[0];
            String password = pair.split(":")[1];

            System.out.println("username: " + userName);
            System.out.println("password: " + password);

            Optional<User> inputUser = userRepository.findByUsername(userName);
            Optional<Product> inputProduct = productRepository.findById(product_id);
            List<Image> images = imageRepository.findByProductId(product_id);
           // List<Image> jsonObjectList = new ArrayList<>();


            //Optional<Image> img;
            if (inputUser.isPresent()) {
                if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {

                    System.out.println("Password matched");
                    if (inputProduct.isPresent()) {
                        if (inputUser.get().getId() != inputProduct.get().getOwner_user_id()) {
                            logger.error("endpoint.image.self.api.get Forbidden Act");
                            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                        }
                        for (Image i : images) {

                            return new ResponseEntity<>(images, HttpStatus.OK);
                        }
                        logger.info("endpoint.image.self.api.get - Details fetched");

                    } else {
                        logger.error("endpoint.image.self.api.get - Product Absent");
                        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                    }
                } else {
                    logger.error("endpoint.image.self.api.get - Password Incorrect");
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } else {
                logger.error("endpoint.image.self.api.get - User Absent");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);

            }
            //return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }catch (Exception e) {
            System.out.println("Exception:" + e);
            logger.error("endpoint.product.self.api.put - exception: " +e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
        logger.error("endpoint.image.self.api.get - Something went wrong");
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }




    @DeleteMapping(value = "/product/{product_id}/image/{image_id}")

    public ResponseEntity<String> deleteImage(HttpServletRequest request, @PathVariable int product_id, @PathVariable int image_id) {
        try {
            statsDClient.incrementCounter("endpoint.image.self.api.delete");
            logger.info("endpoint.image.self.api.delete hit successfully");
            System.out.println("/product/{product_id}/image/{image_id}");

            //check user credentials and get userid
            String upd = request.getHeader("authorization");
            if (upd == null || upd.isEmpty()) {
                logger.error("endpoint.image.self.api.delete - Missing Credentials");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String pair = new String(Base64.decodeBase64(upd.substring(6)));
            String userName = pair.split(":")[0];
            String password = pair.split(":")[1];

            System.out.println("username: " + userName);
            System.out.println("password: " + password);

            Optional<User> inputUser = userRepository.findByUsername(userName);
            Optional<Product> inputProduct = productRepository.findById(product_id);
            Optional<Image> img = imageRepository.findById(image_id);;

            if (inputUser.isPresent()) {

                if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {
                    if (inputProduct.isPresent()) {
                    if (inputUser.get().getId() != inputProduct.get().getOwner_user_id()) {
                        logger.error("endpoint.image.self.api.delete Forbidden Act");
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                    if (img.isPresent()) {

                        String result = service.deleteFileFromS3Bucket(img.get().getFile_name(),product_id);
                        //String bucket_name = service.uploadFile(productId + "/" + profilePic.getOriginalFilename(), profilePic);
                        imageRepository.delete(img.get());
                        logger.info("endpoint.image.self.api.delete Delete Successful");
                        return new ResponseEntity<>(result, HttpStatus.NO_CONTENT);
                    } else {
                        logger.error("endpoint.image.self.api.delete - Image Absent");
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    }
                    }else{
                        logger.error("endpoint.image.self.api.delete - Product Absent");
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    }
                } else {
                    logger.error("endpoint.image.self.api.delete - Password Incorrect");
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } else {
                logger.error("endpoint.image.self.api.delete - User Absent");
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }

            //return new ResponseEntity<>(HttpStatus.CREATED);

        }catch (Exception e) {
            System.out.println("Exception:" + e);
            logger.error("endpoint.product.self.api.put - exception: " +e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

    }

    @PostMapping(value = "/product/{productId}/image")
    public ResponseEntity<Image> createImage(@RequestParam(value="profilePic") MultipartFile profilePic,
                                             HttpServletRequest request, @PathVariable int productId){
        try {
            statsDClient.incrementCounter("endpoint.image.self.api.post");
            logger.info("endpoint.image.self.api.post hit successfully");
                System.out.println("In post image");
                //check user credentials and get userid
                String upd = request.getHeader("authorization");
                if (upd == null || upd.isEmpty()) {
                    logger.error("endpoint.image.self.api.post - Missing Credentials");
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
                System.out.println("auth check");

                String pair = new String(Base64.decodeBase64(upd.substring(6)));
                String userName = pair.split(":")[0];
                String password = pair.split(":")[1];

                System.out.println("optional check");

                Optional<User> inputUser = userRepository.findByUsername(userName);
                //Optional<User> inputUser = userRepository.findByUsername(userName);
                Optional<Product> inputProduct = productRepository.findById(productId);

                Image img;

                //Forbidden check to code
                if (inputUser.isPresent()) {

                    if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {

                        if(inputProduct.isPresent()) {

                            if(inputUser.get().getId() != inputProduct.get().getOwner_user_id()){
                                logger.error("endpoint.image.self.api.post Forbidden Act");
                                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                            }
                            List<Image> images = imageRepository.findByProductId(productId);
                            for(Image i: images){

                                System.out.println("before if");
                                System.out.println(i.getFile_name());
                                System.out.println(profilePic.getOriginalFilename());

                                if(i.getFile_name().matches(profilePic.getOriginalFilename())) {
                                    System.out.println("File already exists");
                                    logger.error("endpoint.image.self.api.post Forbidden Act");
                                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                                }
                            }
                            System.out.println("after for");



                            System.out.println("File Content Type: " + profilePic.getContentType());
                            if (!profilePic.getContentType().startsWith("image/")) {
                                System.out.println("inappropriate");
                                logger.error("endpoint.image.self.api.post Forbidden Act");
                                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                            }
                            System.out.println("before s3 save");

                            String bucket_name = service.uploadFile(productId + "/" + profilePic.getOriginalFilename(), profilePic);

                            System.out.println("after s3 save");

                            String url = bucket_name + "/" + inputUser.get().getId() + "/" + profilePic.getOriginalFilename();

                            img = new Image(productId, profilePic.getOriginalFilename(), url);
                            System.out.println("before save");
                            imageRepository.save(img);
                            System.out.println("after save");
                        }
                        else{
                            System.out.println("product absent");
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                        }
                    } else {
                        logger.error("endpoint.image.self.api.post Password Incorrect");
                        return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                    }
                } else {
                    logger.error("endpoint.image.self.api.post User Absent");
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
                logger.info("endpoint.image.self.api.post Image Saved");
                return new ResponseEntity<>(img, HttpStatus.CREATED);
        }catch (Exception e) {
            System.out.println("Exception:" + e);
            logger.error("endpoint.product.self.api.put - exception: " +e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }


    @GetMapping("/product/{productId}")
    public ResponseEntity<Product> getProductByID(HttpServletRequest request, @PathVariable int productId) {

        try {
            statsDClient.incrementCounter("endpoint.product.self.api.get");
            logger.info("endpoint.product.self.api.get hit successfully");

            System.out.println("In get");
            Optional<Product> inputProduct = productRepository.findById(productId);
            if (inputProduct.isPresent()) {
                logger.info("endpoint.product.self.api.get Product Retrieved");
                return new ResponseEntity<>(inputProduct.get(), HttpStatus.OK);
            } else {
                System.out.println("No product of mentioned id");
                logger.error("endpoint.product.self.api.get Product Absent");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e);
            logger.error("endpoint.product.self.api.put - exception: " +e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Product> deleteProduct(HttpServletRequest request, @PathVariable int productId) {
        System.out.println("In delete");
        try {
            statsDClient.incrementCounter("endpoint.product.self.api.delete");
            logger.info("endpoint.product.self.api.delete hit successfully");
            String upd = request.getHeader("authorization");
            System.out.println(upd);
            if (upd == null || upd.isEmpty()) {
                logger.error("endpoint.product.self.api.delete Product Absent");
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }

            String pair = new String(Base64.decodeBase64(upd.substring(6)));
            String userName = pair.split(":")[0];
            String password = pair.split(":")[1];
            System.out.println("username: " + userName);
            System.out.println("password: " + password);
            Optional<User> inputUser = userRepository.findByUsername(userName);
            Optional<Product> inputProduct = productRepository.findById(productId);

            if (inputUser.isPresent()) {
                if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {
                    System.out.println("Password matched");
                    if (inputProduct.isPresent()) {
                        if (inputUser.get().getId() != inputProduct.get().getOwner_user_id()){
                            System.out.println("Cannot delete others product");
                            logger.error("endpoint.product.self.api.delete Forbidden Act");
                            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
                        }
                        productRepository.delete(inputProduct.get());
                        System.out.println("Deleted product");
                        logger.info("endpoint.product.self.api.delete Product Deleted");
                        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
                    } else {
                        System.out.println("Product not present");
                        logger.error("endpoint.product.self.api.delete Product Absent");
                        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                    }

                } else {
                    System.out.println("Password incorrect");
                    logger.error("endpoint.product.self.api.delete Password Incorrect");
                    return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
                }
            } else {
                System.out.println("User not there");
                logger.error("endpoint.product.self.api.delete User Absent");
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e);
            logger.error("endpoint.product.self.api.put - exception: " +e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
