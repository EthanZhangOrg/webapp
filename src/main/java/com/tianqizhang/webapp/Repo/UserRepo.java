package com.tianqizhang.webapp.Repo;

import com.tianqizhang.webapp.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, String> {

    /**
     * equivalent to 'select * from user where name=?'
     */
    User findByUsername(String username);
}
