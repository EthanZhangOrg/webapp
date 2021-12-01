package com.tianqizhang.webapp.dynamodb.Models;

import com.amazonaws.services.dynamodbv2.datamodeling.*;

import java.security.SecureRandom;
import java.util.Base64;

@DynamoDBTable(tableName = "User-Tokens")
public class DynamodbUser {

    private static final SecureRandom secureRandom = new SecureRandom(); //threadsafe
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder(); //threadsafe

    public static String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    public DynamodbUser(String user_email) {
        this.user_email = user_email;
        this.token = generateNewToken();
        this.expirationTime = System.currentTimeMillis() / 1000 + 5 * 60;
        this.used = false;
    }

    public String getUser_email() {
        return user_email;
    }

    public void setUser_email(String user_email) {
        this.user_email = user_email;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public long getExpirationTime() {
        return expirationTime;
    }

    public void setExpirationTime(long expirationTime) {
        this.expirationTime = expirationTime;
    }

    public Boolean getUsed() {
        return used;
    }

    public void setUsed(Boolean used) {
        this.used = used;
    }

    @DynamoDBHashKey(attributeName = "user_email")
    private String user_email;

    @DynamoDBAttribute(attributeName = "token")
    private String token;

    @DynamoDBAttribute(attributeName = "expirationTime")
    private long expirationTime;

    @DynamoDBAttribute(attributeName = "used")
    @DynamoDBTyped(DynamoDBMapperFieldModel.DynamoDBAttributeType.BOOL)
    private Boolean used;
}
