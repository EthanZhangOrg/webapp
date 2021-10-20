package com.tianqizhang.webapp.Models;

import org.mindrot.jbcrypt.BCrypt;

import javax.persistence.*;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Entity
public class User  {

    public User(String first_name, String last_name, String password, String username) {
        this.id = UUID.randomUUID().toString();
        this.first_name = first_name;
        this.last_name = last_name;
        this.password = BCrypt.hashpw(password, BCrypt.gensalt());
        this.username = username;
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String date = sdf1.format(new Date());
        this.account_created = date;
        this.account_updated = date;
    }

    public User() {

    }

    public static User updateUser(User user, String first_name, String last_name, String password) {
        if (first_name != null) {
            user.setFirst_name(first_name);
        }
        if (last_name != null) {
            user.setLast_name(last_name);
        }
        if (password != null) {
            user.setPassword(BCrypt.hashpw(password, BCrypt.gensalt()));
        }
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        String date = sdf1.format(new Date());
        user.setAccount_updated(date);

        return user;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
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

    @Id
    private String id;

    @Column
    private String first_name;

    @Column
    private String last_name;

    @Column
    private String password;

    @Column
    private String username;

    @Column
    private String account_created;

    @Column
    private String account_updated;
}
