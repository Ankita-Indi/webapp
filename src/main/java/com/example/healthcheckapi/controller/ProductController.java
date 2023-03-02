package com.example.healthcheckapi.controller;

import com.example.healthcheckapi.model.Product;
import com.example.healthcheckapi.model.Image;
import com.example.healthcheckapi.model.User;
import com.example.healthcheckapi.repository.ImageRepository;
import com.example.healthcheckapi.repository.ProductRepository;
import com.example.healthcheckapi.repository.UserRepository;
import com.example.healthcheckapi.service.ImageStorageService;
import com.fasterxml.jackson.annotation.JsonTypeInfo.Id;
import org.springframework.web.multipart.MultipartFile;

import org.apache.tomcat.util.codec.binary.Base64;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.repository.Query;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Collection;
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
    ImageStorageService service;

    public ProductController() {
        System.out.println("in product controller constructor");
    }

    @PostMapping("/product")
    public ResponseEntity<Product> createProduct(@RequestBody Product product, HttpServletRequest request) {
        System.out.println("In post");
        try {
            String upd = request.getHeader("authorization");
            System.out.println(upd);
            if (upd == null || upd.isEmpty()) {
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
                        System.out.println("Product failed basic checks");
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                    }

                    // check if already exists

                    System.out.println("Setting for post request");
                    Optional<Product> u = productRepository.findBySku(product.getSku());
                    System.out.println(u);


                    System.out.println("checking if product sku is present");

                    if (u.isPresent()) {
                        System.out.println("In sku present check");
                        return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                    }

                    Product p = new Product(product.getName(), product.getDescription(), product.getSku(),
                            product.getManufacturer(), product.getQuantity(), inputUser.get().getId());


                    Product _product = productRepository.save(p);


                    System.out.println("product saved in db");
                    return new ResponseEntity<>(_product, HttpStatus.CREATED);
                } else {
                    System.out.println("Password does not match");
                    return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
                }
            } else {
                System.out.println("User does not exist");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("exception: " + e);
            return new ResponseEntity<>(null,HttpStatus.BAD_REQUEST);

        }
    }


    @PutMapping("/product/{productId}")
//    @RequestMapping(value = "/product/{productId}", method = {RequestMethod.DELETE})
    public ResponseEntity<Product> updateProduct(@PathVariable int productId, @RequestBody Product product, HttpServletRequest request) {

        System.out.println("In put");
        try {
            String upd = request.getHeader("authorization");
            System.out.println(upd);
            if (upd == null || upd.isEmpty()) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
            if ((product.getName() == null || product.getName().isEmpty())
                    || (product.getManufacturer() == null || product.getManufacturer().isEmpty())
                    || (product.getSku() == null || product.getSku().isEmpty())
                    || product.getDate_added() != null
                    || product.getDate_last_updated() != null
                    || product.getOwner_user_id() != null) {
                System.out.println("Something is null or absent");
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
                        if (inputUser.get().getId() != inputProduct.get().getOwner_user_id())
                            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);

                        if (!inputProduct.get().getSku().matches(product.getSku())) {
                            System.out.println("Cannot change Sku");
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
                            return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
                            } else {
                                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                            }
                    } else {
                        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                    }
                } else {
                    return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
                }
            } else {
                System.out.println("User not there");
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }

        } catch (Exception e) {
            System.out.println("Exception:" + e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

    }

    @PatchMapping("/product/{productId}")
    public ResponseEntity<Product> updateProductPatch(@PathVariable int productId, HttpServletRequest request, @RequestBody Product product) {

        System.out.println("In patch");
        try {
            String upd = request.getHeader("authorization");
            System.out.println(upd);
            if (upd == null || upd.isEmpty()) {
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
                            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

                        }
                        if (inputUser.get().getId() != inputProduct.get().getOwner_user_id())
                            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
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
                        else{
                            if(product.getQuantity() >= 0 && product.getQuantity() <= 100)
                                updatedProduct.setQuantity(product.getQuantity());
                            else
                                return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                        }
                        updatedProduct.setDate_last_updated(OffsetDateTime.now().toString());
                        productRepository.save(updatedProduct);
                        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
                    }
                    else {
                        System.out.println("Product not found");
                        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                    }
                }
                else {
                    System.out.println("Password incorrect");
                    return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
                }
            }
            else {
                System.out.println("User not there");
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }


        }
        catch (Exception e) {
            System.out.println("Exception:" + e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

    }


    



    @GetMapping(value = "/product/{product_id}/image/{image_id}")
    public ResponseEntity<Image> getImage(HttpServletRequest request, @PathVariable int product_id, @PathVariable int image_id) {

        System.out.println("product/{product_id}/image/{image_id}");

        //check user credentials and get userid
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
        Optional<Product> inputProduct = productRepository.findById(product_id);
        Optional<Image> img = imageRepository.findById(image_id);
        //Optional<Image> img;
        if (inputUser.isPresent()) {
            if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {

                System.out.println("Password matched");
                if (inputProduct.isPresent()) {
                    if (inputUser.get().getId() != inputProduct.get().getOwner_user_id() ||
                            img.get().getProduct_id() != product_id) {
                        return new ResponseEntity<>(HttpStatus.FORBIDDEN);
                    }
                    if (img.isPresent()) {
                        return new ResponseEntity<>(img.get(), HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                    }
                } else {
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
    }

    @DeleteMapping(value = "/product/{product_id}/image/{image_id}")

    public ResponseEntity<String> deleteImage(HttpServletRequest request, @PathVariable int product_id, @PathVariable int image_id) {

        System.out.println("/product/{product_id}/image/{image_id}");

        //check user credentials and get userid
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
        Optional<Product> inputProduct = productRepository.findById(product_id);
        Optional<Image> img;

        if (inputUser.isPresent()) {

            if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {

                User user = inputUser.get();
                Product product = inputProduct.get();
                img = imageRepository.findById(user.getId());

                if (img.isPresent()) {

                    String result = service.deleteFileFromS3Bucket(img.get().getS3_bucket_path(),img.get().getProduct_id());
                    imageRepository.delete(img.get());
                    return new ResponseEntity<>(result, HttpStatus.NO_CONTENT);
                }
                else {
                    return new ResponseEntity<>(HttpStatus.NOT_FOUND);
                }
            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping(value = "/product/{productId}/image")
    public ResponseEntity<Image> createImage(@RequestParam(value="profilePic") MultipartFile profilePic,
                                             HttpServletRequest request, @PathVariable int productId){
        System.out.println("In post image");
        //check user credentials and get userid
        String upd = request.getHeader("authorization");
        if (upd == null || upd.isEmpty()) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        String pair = new String(Base64.decodeBase64(upd.substring(6)));
        String userName = pair.split(":")[0];
        String password = pair.split(":")[1];

        Optional<User> inputUser = userRepository.findByUsername(userName);
        //Optional<User> inputUser = userRepository.findByUsername(userName);
        Optional<Product> inputProduct = productRepository.findById(productId);
        Image img;
        if (inputUser.isPresent()) {
            if (bCryptPasswordEncoder.matches(password, inputUser.get().getPassword())) {

                //matches password complete-- add code here
                User user = inputUser.get();
                Product product = inputProduct.get();

                System.out.println("File Content Type: " + profilePic.getContentType());
                if(!profilePic.getContentType().startsWith("image/"))
                    return new ResponseEntity<>(HttpStatus.BAD_REQUEST);

                String bucket_name = service.uploadFile( productId + "/" + profilePic.getOriginalFilename(), profilePic);

                String url = bucket_name + "/" + user.getId() + "/" + profilePic.getOriginalFilename();

                img = new Image(productId, profilePic.getOriginalFilename(), url);
                imageRepository.save(img);

            } else {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            }
        } else {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        return new ResponseEntity<>(img, HttpStatus.CREATED);
    }




    
        







    @GetMapping("/product/{productId}")
    public ResponseEntity<Product> getProductByID(HttpServletRequest request, @PathVariable int productId) {

        try {
            System.out.println("In get");
            Optional<Product> inputProduct = productRepository.findById(productId);
            if (inputProduct.isPresent()) {
                return new ResponseEntity<>(inputProduct.get(), HttpStatus.OK);
            } else {
                System.out.println("No product of mentioned id");
                return new ResponseEntity<>(HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/product/{productId}")
    public ResponseEntity<Product> deleteProduct(HttpServletRequest request, @PathVariable int productId) {
        System.out.println("In delete");
        try {
            String upd = request.getHeader("authorization");
            System.out.println(upd);
            if (upd == null || upd.isEmpty()) {
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
                            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
                        }
                        productRepository.delete(inputProduct.get());
                        System.out.println("Deleted product");
                        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
                    } else {
                        System.out.println("Product not present");
                        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
                    }

                } else {
                    System.out.println("Password incorrect");
                    return new ResponseEntity<>(null, HttpStatus.UNAUTHORIZED);
                }
            } else {
                System.out.println("User not there");
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
        } catch (Exception e) {
            System.out.println("Exception:" + e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}
