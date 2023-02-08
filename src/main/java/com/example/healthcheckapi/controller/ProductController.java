package com.example.healthcheckapi.controller;

import com.example.healthcheckapi.model.Product;
import com.example.healthcheckapi.model.User;
import com.example.healthcheckapi.repository.ProductRepository;
import com.example.healthcheckapi.repository.UserRepository;
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
                            product.getQuantity() > 100)//|| ((Object)product.getQuantity()).getClass().getSimpleName() != "Integer")
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
                //return new ResponseEntity<>() to do
            }
        } catch (Exception e) {
            System.out.println("exception: " + e);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        return null;
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
                        if (inputProduct.get().getSku() != product.getSku()) {
                            System.out.println("Cannot change Sku");
                            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                        }
                        if (product.getQuantity() > 0 && product.getQuantity() < 100) {
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
    public ResponseEntity<Product> updateProductPatch(@PathVariable int productId, @RequestBody Product product, HttpServletRequest request) {

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
                        if (inputUser.get().getId() != inputProduct.get().getOwner_user_id())
                            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
                        if (inputProduct.get().getSku() != product.getSku()) {
                            System.out.println("Cannot change Sku");
                            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
                        }
                        if (product.getQuantity() > 0 && product.getQuantity() < 100) {
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
                        if (inputUser.get().getId() != inputProduct.get().getOwner_user_id())
                            return new ResponseEntity<>(null, HttpStatus.FORBIDDEN);
                        productRepository.delete(inputProduct.get());
                        return new ResponseEntity<>(null, HttpStatus.NO_CONTENT);
                    } else {
                        System.out.println("Product not present");
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
}
