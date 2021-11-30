package com.tianqizhang.webapp.db2.Repo;

import com.tianqizhang.webapp.db2.Models.User;
import org.springframework.data.jpa.repository.JpaRepository;

public interface UserRepoReplica extends JpaRepository<User, String> {

    /**
     * equivalent to 'select * from user where name=?'
     */
    User findByUsername(String username);
}
