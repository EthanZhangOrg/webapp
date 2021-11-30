package com.tianqizhang.webapp.db2.Repo;

import com.tianqizhang.webapp.db2.Models.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepoReplica extends JpaRepository<Image, String> {
    /**
     * equivalent to 'select * from image where fileName=?'
     */
    Image findByUserId(String userId);
}
