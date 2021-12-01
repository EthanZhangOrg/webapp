package com.tianqizhang.webapp.Controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.amazonaws.services.s3.AmazonS3;
import com.tianqizhang.webapp.db2.Models.Image;
import com.tianqizhang.webapp.db2.Models.User;
import com.tianqizhang.webapp.db2.Repo.ImageRepoReplica;
import com.tianqizhang.webapp.db2.Repo.UserRepoReplica;
import com.timgroup.statsd.NonBlockingStatsDClient;
import com.timgroup.statsd.StatsDClient;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RestController;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
public class GetRequestController {

    @Autowired
    private UserRepoReplica userRepo;

    @Autowired
    private ImageRepoReplica imageRepo;

    @Value("${s3.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    private static final StatsDClient statsd = new NonBlockingStatsDClient(null, "localhost", 8125);

    @GetMapping(value = "/v1/user/self")
    public ResponseEntity<JSON> getUserInfo(@RequestHeader Map<String, String> headers) {

        statsd.incrementCounter("getUserInfo");
        long apiCallStart = System.currentTimeMillis();

        String token = headers.get("authorization").split(" ")[1];
        String usernameAndPassword = new String(Base64.getDecoder().decode(token));
        String username = usernameAndPassword.split(":")[0];
        String password = usernameAndPassword.split(":")[1];

        long databaseQueryStart = System.currentTimeMillis();

        User user = userRepo.findByUsername(username);

        statsd.recordExecutionTimeToNow("queryUserByUsername-DatabaseQuery", databaseQueryStart);

        if (user == null) {
            return new ResponseEntity<>(null,
                    HttpStatus.UNAUTHORIZED);
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            return new ResponseEntity<>(null,
                    HttpStatus.UNAUTHORIZED);
        }

        if (!user.getVerified()) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        Map<String, Object> usermap = new HashMap<>();
        usermap.put("id", user.getId());
        usermap.put("first_name", user.getFirst_name());
        usermap.put("last_name", user.getLast_name());
        usermap.put("username", username);
        usermap.put("account_created", user.getAccount_created());
        usermap.put("account_updated", user.getAccount_updated());
        usermap.put("verified", user.getVerified());
        usermap.put("verified_on", user.getVerified_on());

        statsd.recordExecutionTimeToNow("getUserInfo-APICall", apiCallStart);

        return new ResponseEntity<>(new JSONObject(usermap),
                HttpStatus.OK);
    }

    @GetMapping(value = "/v1/user/self/pic")
    private ResponseEntity<JSON> getUserPic(@RequestHeader Map<String, String> headers) {

        long apiCallStart = System.currentTimeMillis();
        statsd.incrementCounter("getUserPic");

        String token = headers.get("authorization").split(" ")[1];
        String usernameAndPassword = new String(Base64.getDecoder().decode(token));
        String username = usernameAndPassword.split(":")[0];
        String password = usernameAndPassword.split(":")[1];

        long databaseQueryStart = System.currentTimeMillis();

        User user = userRepo.findByUsername(username);

        statsd.recordExecutionTimeToNow("queryUserByUsername-DatabaseQuery", databaseQueryStart);

        if (user == null) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        if (!user.getVerified()) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        String userId = user.getId();

        long imageDatabaseQueryStart = System.currentTimeMillis();

        Image image = imageRepo.findByUserId(userId);

        statsd.recordExecutionTimeToNow("queryImageInfoByUserId-DatabaseQuery", imageDatabaseQueryStart);

        if (image == null) {
            return new ResponseEntity<>(null,
                    HttpStatus.NOT_FOUND);
        }

        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("file_name", image.getFileName());
        imageMap.put("id", image.getId());
        imageMap.put("url", image.getUrl());
        imageMap.put("upload_date", image.getUploadDate());
        imageMap.put("user_id", image.getUserId());

        statsd.recordExecutionTimeToNow("getUserPic-APICall", apiCallStart);

        return new ResponseEntity<>(new JSONObject(imageMap),
                HttpStatus.OK);
    }
}
