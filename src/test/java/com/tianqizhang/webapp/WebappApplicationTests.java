package com.tianqizhang.webapp;

import com.alibaba.fastjson.JSONObject;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.HashMap;
import java.util.Map;

class WebappApplicationTests {
    @Test
    void test() {
        Map<String, Object> msgMap = new HashMap<>();
        msgMap.put("email", "username");
        msgMap.put("token", "dynamodbUser.getToken()");
        msgMap.put("msg_type", "JsonString");
        String msg = new JSONObject(msgMap).toString();
        System.out.println(msg);
    }
}
