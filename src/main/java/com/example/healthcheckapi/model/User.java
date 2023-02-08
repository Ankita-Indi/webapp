package com.example.healthcheckapi.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.GenericGenerator;

import javax.persistence.*;
import javax.validation.constraints.Email;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.Objects;
import java.util.UUID;

@Entity
@Table(name = "user")
public class User {

//    @Id
//    @GeneratedValue(generator = "UUID")
//    @GenericGenerator(
//            name = "UUID",
//            strategy = "org.hibernate.id.UUIDGenerator"
//    )
//    @Column(name = "id", unique = true, nullable = false, updatable = false)
//    private String id;

    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;

    @NotEmpty
    @NotNull(message="First name cannot be missing or empty")
    @Column(name = "first_name")
    private String first_name;

    @NotEmpty @NotNull(message="Last name cannot be missing or empty")
    @Column(name = "last_name")
    private String last_name;

    @NotEmpty @JsonProperty(access = JsonProperty.Access.WRITE_ONLY)
    @Column(name = "password")
    private String password;

    @Email(message="Email not valid") @NotEmpty
    @Column(name = "username", unique = true, nullable = false)
    @NotNull(message="Email cannot be missing or empty")
    private String username;


    private String account_created;
    private String account_updated;

    public User() {

    }

    public User(String first_name, String last_name, String password, String username) {
//        this.id = UUID.randomUUID().toString();
        this.id = id;
        this.first_name = first_name;
        this.last_name = last_name;
        this.password = password;
        this.username = username;
        this.account_created = OffsetDateTime.now().toString();
        this.account_updated = OffsetDateTime.now().toString();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof User))
            return false;
        User employee = (User) o;
        return Objects.equals(this.id, employee.id)
                && Objects.equals(this.first_name, employee.first_name)
                && Objects.equals(this.last_name, employee.last_name)
                && Objects.equals(this.username, employee.username);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id, this.first_name, this.last_name, this.username);
    }

    @Override
    public String toString() {
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }


    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getFirst_name() {
        return first_name;
    }

    public void setFirst_name(String first_name) {
        this.first_name = first_name;
    }

    public String getLast_name() {
        return last_name;
    }

    public void setLast_name(String last_name) {
        this.last_name = last_name;
    }


    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getAccount_created() {
        return account_created;
    }

    public void setAccount_created(String account_created) {
        this.account_created = account_created;
    }

    public String getAccount_updated() {
        return account_updated;
    }

    public void setAccount_updated(String account_updated) {
        this.account_updated = account_updated;
    }
}
