package com.example.healthcheckapi.model;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;
import java.util.Objects;

@Entity
@Table(name = "image")
public class Image {
    @Id
    @GeneratedValue(strategy=GenerationType.AUTO)
    private int id;


    @Column(name = "product_id")
    private int product_id;


    @Column(name = "file_name")
    private String file_name;


    @Column(name = "s3_bucket_path")
    private String s3_bucket_path;

    private String date_created;

    public Image() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getProduct_id() {
        return product_id;
    }

    public void setProduct_id(int product_id) {
        this.product_id = product_id;
    }

    public String getFile_name() {
        return file_name;
    }

    public void setFile_name(String file_name) {
        this.file_name = file_name;
    }

    public String getS3_bucket_path() {
        return s3_bucket_path;
    }

    public void setS3_bucket_path(String s3_bucket_path) {
        this.s3_bucket_path = s3_bucket_path;
    }

    public String getDate_created() {
        return date_created;
    }

    public void setDate_created(String date_created) {
        this.date_created = date_created;
    }

    public Image(int product_id, String file_name, String s3_bucket_path) {
        this.product_id = product_id;
        this.file_name = file_name;
        this.s3_bucket_path = s3_bucket_path;
        this.date_created = OffsetDateTime.now().toString();
    }

    @Override
    public boolean equals(Object o) {

        if (this == o)
            return true;
        if (!(o instanceof Image))
            return false;
        Image img = (Image) o;
        return Objects.equals(this.id, img.id)
                && Objects.equals(this.product_id, img.product_id)
                && Objects.equals(this.file_name, img.file_name)
                && Objects.equals(this.s3_bucket_path, img.s3_bucket_path)
                && Objects.equals(this.date_created, img.date_created);
    }

//    @Override
//    public int hashCode() {
//        return Objects.hash(this.image_id, this.product_id,this.file_name, this.s3_bucket_path, this.date_created);
//    }

    @Override
    public String toString() {
        return super.toString(); //To change body of generated methods, choose Tools | Templates.
    }
}
