package com.tianqizhang.webapp;

import com.alibaba.fastjson.JSONObject;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapper;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBMapperConfig;
import com.amazonaws.services.dynamodbv2.datamodeling.DynamoDBSaveExpression;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.ExpectedAttributeValue;
import com.amazonaws.services.sns.AmazonSNSClient;
import com.amazonaws.services.sns.AmazonSNSClientBuilder;
import com.amazonaws.services.sns.model.PublishRequest;
import com.tianqizhang.webapp.dynamodb.Models.DynamodbUser;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.HashMap;
import java.util.Map;

class WebappApplicationTests {
    @Test
    void test() {
        String username = "ethanzhang1997@gmail.com";

//        Map<String, Object> msgMap = new HashMap<>();
//        msgMap.put("email", username);
//        msgMap.put("token", "12345678");
//        msgMap.put("msg_type", "JsonString");
//        String msg = new JSONObject(msgMap).toString();
//        System.out.println(msg);

//        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("AKIAQSRZMGLTK7Z5WCNH", "OuIg/6wbo263kJeULY9Lfjb2JWP1xnM6nDaJ7GtB")))
//                .withRegion(Regions.US_EAST_1)
//                .build();
//        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(client, DynamoDBMapperConfig.DEFAULT);
//        DynamodbUser dynamodbUser = new DynamodbUser(username);
//        dynamoDBMapper.save(dynamodbUser);
    }

//    @Test
//    void test1() {
//        String username = "ethanzhang1997@gmail.com";
//        AmazonSNSClient amazonSNSClient = (AmazonSNSClient)AmazonSNSClientBuilder
//                .standard()
//                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("AKIAQSRZMGLTK7Z5WCNH", "OuIg/6wbo263kJeULY9Lfjb2JWP1xnM6nDaJ7GtB")))
//                .withRegion(Regions.US_EAST_1)
//                .build();
//
//        String topic_arn = "arn:aws:sns:us-east-1:039848784614:csye6225-fall2021-topic";
//
//        // publish msg to sns topic
//        Map<String, Object> msgMap = new HashMap<>();
//        msgMap.put("email", username);
//        msgMap.put("token", "testToken");
//        msgMap.put("msg_type", "JsonString");
//        String msg = new JSONObject(msgMap).toString();
//        PublishRequest publishRequest = new PublishRequest(topic_arn, msg);
//        amazonSNSClient.publish(publishRequest);
//    }
//
//    @Test
//    void test2() {
//        AmazonDynamoDB client = AmazonDynamoDBClientBuilder.standard()
//                .withCredentials(new AWSStaticCredentialsProvider(new BasicAWSCredentials("AKIAQSRZMGLTK7Z5WCNH", "OuIg/6wbo263kJeULY9Lfjb2JWP1xnM6nDaJ7GtB")))
//                .withRegion(Regions.US_EAST_1)
//                .build();
//        DynamoDBMapper dynamoDBMapper = new DynamoDBMapper(client, DynamoDBMapperConfig.DEFAULT);
//        DynamodbUser dynamodbUser = dynamoDBMapper.load(DynamodbUser.class, "ethanzhang1997@gmail.com");
//
//        if (dynamodbUser == null) {
//            System.out.println("user == null");
//        }
//        System.out.println("token: " + dynamodbUser.getToken());
//    }
}
