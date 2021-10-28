package com.tianqizhang.webapp.Models;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.UUID;

@Entity
public class Image {
    @Id
    private String id;

    public Image(String fileName, String url, String userId) {
        this.id = UUID.randomUUID().toString();
        this.fileName = fileName;
        this.url = url;
        SimpleDateFormat sdf1 = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS");
        this.uploadDate = sdf1.format(new Date());
        this.userId = userId;
    }

    @Column
    private String fileName;

    @Column
    private String url;

    public String getId() {
        return id;
    }

    public String getFileName() {
        return fileName;
    }

    public String getUrl() {
        return url;
    }

    public String getUploadDate() {
        return uploadDate;
    }

    public String getUserId() {
        return userId;
    }

    @Column
    private String uploadDate;

    public Image() {

    }

    @Column
    private String userId;
}
