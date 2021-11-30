package com.tianqizhang.webapp.db1.Repo;

import com.tianqizhang.webapp.db1.Models.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepo extends JpaRepository<Image, String> {
    /**
     * equivalent to 'select * from image where fileName=?'
     */
    Image findByUserId(String userId);
}
