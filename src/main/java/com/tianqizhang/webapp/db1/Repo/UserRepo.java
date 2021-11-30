package com.tianqizhang.webapp.db1.Repo;

import com.tianqizhang.webapp.db1.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepo extends JpaRepository<User, String> {

    /**
     * equivalent to 'select * from user where name=?'
     */
    User findByUsername(String username);
}
