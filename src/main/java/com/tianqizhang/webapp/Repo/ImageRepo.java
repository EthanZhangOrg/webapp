package com.tianqizhang.webapp.Repo;

import com.tianqizhang.webapp.Models.Image;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ImageRepo extends JpaRepository<Image, String> {
    /**
     * equivalent to 'select * from image where fileName=?'
     */
    Image findByUserId(String userId);
}
