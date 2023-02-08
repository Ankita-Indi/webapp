package com.example.healthcheckapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "product")

public class Product {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;

    @NotEmpty
    @NotNull(message="Product name cannot be missing or empty")
    @Column(name = "name")
    private String name;

    @NotEmpty
    @NotNull(message="Description cannot be missing or empty")
    @Column(name = "description")
    private String description;

    @NotEmpty
    @NotNull(message="Sku cannot be missing or empty")
    @Column(name = "sku", unique = true, nullable = false)
    private String sku;

    @NotEmpty @NotNull(message="Manufacturer cannot be missing or empty")
    @Column(name = "manufacturer")
    private String manufacturer;

    @NotNull(message="Quantity can be between 0 and 100")
//    @Size(min = 0, max = 100)
    @Column(name = "quantity")
    private int quantity;

    private String date_added;
    private String date_last_updated;

//    @ManyToOne
//    @JoinColumn(name = "owner_user_id", nullable = false)
    @Column(name = "owner_user_id")
    private int owner_user_id;

    public Product(String name, String description, String sku, String manufacturer, int quantity, int owner_user_id) {
//        this.id = UUID.randomUUID().toString();
//        this.id = id;
        this.name = name;
        this.description = description;
        this.sku = sku;
        this.manufacturer = manufacturer;
        this.quantity = quantity;
        this.owner_user_id = owner_user_id;
        this.date_added = OffsetDateTime.now().toString();
        this.date_last_updated = OffsetDateTime.now().toString();
    }

    public Product() {

    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getSku() {
        return sku;
    }

    public void setSku(String sku) {
        this.sku = sku;
    }

    public String getManufacturer() {
        return manufacturer;
    }

    public void setManufacturer(String manufacturer) {
        this.manufacturer = manufacturer;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }

    public String getDate_added() {
        return date_added;
    }

    public void setDate_added(String date_added) {
        this.date_added = date_added;
    }

    public String getDate_last_updated() {
        return date_last_updated;
    }

    public void setDate_last_updated(String date_last_updated) {
        this.date_last_updated = date_last_updated;
    }

    public int getOwner_user_id() {
        return owner_user_id;
    }

    public void setOwner_user_id(int owner_user_id) {
        this.owner_user_id = owner_user_id;
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof Product))
            return false;
        Product product = (Product) o;
        return Objects.equals(this.id, product.id)
                && Objects.equals(this.name, product.name)
                && Objects.equals(this.description, product.description)
                && Objects.equals(this.sku, product.sku)
                && Objects.equals(this.manufacturer, product.manufacturer)
                && Objects.equals(this.quantity, product.quantity)
                && Objects.equals(this.owner_user_id, product.owner_user_id);
    }

 //   @Override
//    public int hashCode() {
//        return Objects.hash(this.id, this.first_name, this.last_name, this.username);
//    }

    @Override
    public String toString() {
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }

}
