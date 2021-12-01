package com.tianqizhang.webapp.Controller;

import com.alibaba.fastjson.JSON;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.model.PublishRequest;
import com.tianqizhang.webapp.db1.Models.Image;
import com.tianqizhang.webapp.db1.Models.User;
import com.tianqizhang.webapp.db1.Repo.ImageRepo;
import com.tianqizhang.webapp.db1.Repo.UserRepo;
import com.tianqizhang.webapp.dynamodb.Models.DynamodbUser;
import lombok.extern.slf4j.Slf4j;
import org.mindrot.jbcrypt.BCrypt;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import com.alibaba.fastjson.JSONObject;

import com.timgroup.statsd.StatsDClient;
import com.timgroup.statsd.NonBlockingStatsDClient;

import static com.tianqizhang.webapp.db1.Models.User.verifyUser;

@Slf4j
@RestController
public class ApiController {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private ImageRepo imageRepo;

    @Value("${s3.bucket.name}")
    private String bucketName;

    @Autowired
    private AmazonS3 s3Client;

    @Autowired
    private DynamoDBMapper dynamoDBMapper;

    @Autowired
    private AmazonSNSClient amazonSNSClient;

    @Value("${sns.topic.arn}")
    private String topic_arn;

    private static final StatsDClient statsd = new NonBlockingStatsDClient(null, "localhost", 8125);

    final Logger logger = LoggerFactory.getLogger(ApiController.class);

    @GetMapping(value = "/")
    public ResponseEntity<String> sayHello() {
        return new ResponseEntity<>("hello world!",
                HttpStatus.OK);
    }

    @PutMapping(value = "/v1/user/self")
    public ResponseEntity<JSON> updateUserInfo(@RequestHeader Map<String, String> headers, @RequestBody String jsonstr) {

        long apiCallStart = System.currentTimeMillis();

        statsd.incrementCounter("updateUserInfo");

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

        JSONObject userJsonObject = JSONObject.parseObject(jsonstr);

        if (!userJsonObject.containsKey("username") || userJsonObject.keySet().size() != 4) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        for (String key : userJsonObject.keySet()) {
            // Check if the username is correct. User can't update other user's info.
            if (key.equals("username")) {
                if (!userJsonObject.getString(key).equals(username)) {
                    return new ResponseEntity<>(null,
                            HttpStatus.BAD_REQUEST);
                }
            }

            // Attempt to update any other field should return 400 Bad Request HTTP response code.
            if (!key.equals("first_name") && !key.equals("last_name") && !key.equals("password") && !key.equals("username")) {
                return new ResponseEntity<>(null,
                        HttpStatus.BAD_REQUEST);
            }
        }

        User.updateUser(user,
                userJsonObject.getString("first_name"),
                userJsonObject.getString("last_name"),
                userJsonObject.getString("password"));

        long databaseSaveStart = System.currentTimeMillis();

        userRepo.save(user);

        statsd.recordExecutionTimeToNow("saveUserToDatabase-DatabaseSave", databaseSaveStart);

        statsd.recordExecutionTimeToNow("updateUserInfo-APICall", apiCallStart);

        return new ResponseEntity<>(null,
                HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/v1/user")
    public ResponseEntity<JSON> createUser(@RequestBody String jsonstr) {

        long apiCallStart = System.currentTimeMillis();

        statsd.incrementCounter("createUser");

        JSONObject userJsonObject = JSONObject.parseObject(jsonstr);

        String username = userJsonObject.getString("username");

        if (username == null) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        if (!username.matches(regex)) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        long databaseQueryStart = System.currentTimeMillis();

        if (userRepo.findByUsername(username) != null) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        statsd.recordExecutionTimeToNow("queryUserByUsername-DatabaseQuery", databaseQueryStart);

        if (userJsonObject.getString("first_name") == null ||
                userJsonObject.getString("last_name") == null ||
                userJsonObject.getString("password") == null) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        User user = new User(userJsonObject.getString("first_name"),
                userJsonObject.getString("last_name"),
                userJsonObject.getString("password"),
                userJsonObject.getString("username"));

        long databaseSaveStart = System.currentTimeMillis();

        userRepo.save(user);

        statsd.recordExecutionTimeToNow("saveUserToDatabase-DatabaseSave", databaseSaveStart);

        Map<String, Object> usermap = new HashMap<>();
        usermap.put("id", user.getId());
        usermap.put("first_name", user.getFirst_name());
        usermap.put("last_name", user.getLast_name());
        usermap.put("username", username);
        usermap.put("account_created", user.getAccount_created());
        usermap.put("account_updated", user.getAccount_updated());
        usermap.put("verified", user.getVerified());
        usermap.put("verified_on", user.getVerified_on());

        statsd.recordExecutionTimeToNow("createUser-APICall", apiCallStart);

        // save token in dynamodb
        DynamodbUser dynamodbUser = new DynamodbUser(username);
        dynamoDBMapper.save(dynamodbUser);

        // publish msg to sns topic
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("email", username);
        msgMap.put("token", dynamodbUser.getToken());
        msgMap.put("msg_type", "JsonString");
        String msg = new JSONObject(msgMap).toString();

        logger.info("the msg which will be published into sns topic is: " + msg);

        PublishRequest publishRequest = new PublishRequest(topic_arn, msg);
        amazonSNSClient.publish(publishRequest);

        logger.info("message published!");

        return new ResponseEntity<>(new JSONObject(usermap),
                HttpStatus.CREATED);
    }

    @PostMapping(value = "/v1/user/self/pic")
    private ResponseEntity<JSON> uploadUserPic(@RequestHeader Map<String, String> headers, HttpEntity<byte[]> requestEntity) {

        long apiCallStart = System.currentTimeMillis();

        statsd.incrementCounter("uploadUserPic");

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

        byte[] fileBytes = requestEntity.getBody();
        String contentType = headers.get("content-type");
        String suffix;
        if (Objects.equals(contentType, "image/png")) {
            suffix = ".png";
        } else if (Objects.equals(contentType, "image/jpeg") || Objects.equals(contentType, "image/jpg")){
            suffix = ".jpg";
        } else {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        if (fileBytes == null) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        String userId = user.getId();

        // check if the previous image exist

        long imageDatabaseQueryStart = System.currentTimeMillis();

        Image previousImage = imageRepo.findByUserId(userId);

        statsd.recordExecutionTimeToNow("queryImageInfoByUserId-DatabaseQuery", imageDatabaseQueryStart);

        if (previousImage != null) {
            long s3DeletionStart = System.currentTimeMillis();
            s3Client.deleteObject(bucketName, userId + "/" + previousImage.getFileName());
            statsd.recordExecutionTimeToNow("deleteImage-S3ServiceCall", s3DeletionStart);
            long imageDatabaseDeletionStart = System.currentTimeMillis();
            imageRepo.delete(previousImage);
            statsd.recordExecutionTimeToNow("deleteImage-DatabaseQuery", imageDatabaseDeletionStart);
        }

        String fileName = System.currentTimeMillis() + "_pic" + suffix;
        File file = convertBinaryToFile(fileBytes, fileName);

        long s3PutStart = System.currentTimeMillis();

        s3Client.putObject(new PutObjectRequest(bucketName, userId + "/" + fileName, file));

        statsd.recordExecutionTimeToNow("putImage-S3ServiceCall", s3PutStart);

        file.delete();

        String fileUrl = s3Client.getUrl(bucketName, userId + "/" + fileName).toString();
        Image image = new Image(fileName, fileUrl, userId);

        long imageDatabaseSaveStart = System.currentTimeMillis();

        imageRepo.save(image);

        statsd.recordExecutionTimeToNow("saveImageToDatabase-DatabaseSave", imageDatabaseSaveStart);

        Map<String, Object> imageMap = new HashMap<>();
        imageMap.put("file_name", image.getFileName());
        imageMap.put("id", image.getId());
        imageMap.put("url", image.getUrl());
        imageMap.put("upload_date", image.getUploadDate());
        imageMap.put("user_id", image.getUserId());

        statsd.recordExecutionTimeToNow("uploadUserPic-APICall", apiCallStart);

        return new ResponseEntity<>(new JSONObject(imageMap),
                HttpStatus.CREATED);
    }

    private File convertBinaryToFile(byte[] binary, String fileName) {
        File file = new File(Objects.requireNonNull(fileName));
        try (FileOutputStream fos = new FileOutputStream(file)) {
            fos.write(binary);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return file;
    }

    @DeleteMapping (value = "/v1/user/self/pic")
    private ResponseEntity<JSON> deleteUserPic(@RequestHeader Map<String, String> headers) {

        long apiCallStart = System.currentTimeMillis();
        statsd.incrementCounter("deleteUserPic");

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

        String userId = user.getId();

        long imageDatabaseQueryStart = System.currentTimeMillis();

        Image image = imageRepo.findByUserId(userId);

        statsd.recordExecutionTimeToNow("queryImageInfoByUserId-DatabaseQuery", imageDatabaseQueryStart);

        if (image == null) {
            return new ResponseEntity<>(null,
                    HttpStatus.NOT_FOUND);
        }

        long s3DeletionStart = System.currentTimeMillis();
        s3Client.deleteObject(bucketName, userId + "/" + image.getFileName());
        statsd.recordExecutionTimeToNow("deleteImage-S3ServiceCall", s3DeletionStart);

        long imageDatabaseDeletionStart = System.currentTimeMillis();
        imageRepo.delete(image);
        statsd.recordExecutionTimeToNow("deleteImage-DatabaseQuery", imageDatabaseDeletionStart);

        statsd.recordExecutionTimeToNow("deleteUserPic-APICall", apiCallStart);

        return new ResponseEntity<>(null,
                HttpStatus.NO_CONTENT);
    }

    @GetMapping(value = "/v1/verifyUserEmail")
    public ResponseEntity<JSON> verifyUserEmail(@RequestParam String email, @RequestParam String token) {
        DynamodbUser dynamodbUser = dynamoDBMapper.load(DynamodbUser.class, email);

        if (dynamodbUser == null) {
            logger.info("This dynamodbUser doesn't exist! Email is: " + email);
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        if (dynamodbUser.getToken().equals(token)) {
            User user = userRepo.findByUsername(email);
            logger.info("Start verify user! Email is: " + email);
            verifyUser(user);
            logger.info("Verified! Email is: " + email);
            userRepo.save(user);
        } else {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }

        return new ResponseEntity<>(null, HttpStatus.OK);
    }
}
