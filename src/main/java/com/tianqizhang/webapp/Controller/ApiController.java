package com.tianqizhang.webapp.Controller;

import com.alibaba.fastjson.JSON;
import com.tianqizhang.webapp.Models.User;
import com.tianqizhang.webapp.Repo.UserRepo;
import com.tianqizhang.webapp.Services.MyUserDetailsService;
import org.mindrot.bcrypt.BCrypt;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;

@RestController
public class ApiController {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @GetMapping(value = "/")
    public String getPage() {
        return "Welcome!";
    }

    @GetMapping(value = "/v1/user/self")
    public ResponseEntity<JSON> getUserInfo(@RequestHeader Map<String, String> headers) {

        String token = headers.get("authorization").split(" ")[1];
        String usernameAndPassword = new String(Base64.getDecoder().decode(token));
        String username = usernameAndPassword.split(":")[0];
        String password = usernameAndPassword.split(":")[1];

        User user = userRepo.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(null,
                    HttpStatus.UNAUTHORIZED);
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
            return new ResponseEntity<>(null,
                    HttpStatus.UNAUTHORIZED);
        }

        Map<String, Object> usermap = new HashMap<>();
        usermap.put("id", user.getId());
        usermap.put("first_name", user.getFirst_name());
        usermap.put("last_name", user.getLast_name());
        usermap.put("username", username);
        usermap.put("account_created", user.getAccount_created());
        usermap.put("account_updated", user.getAccount_updated());

        return new ResponseEntity<>(new JSONObject(usermap),
                HttpStatus.OK);
    }

    @PutMapping(value = "/v1/user/self")
    public ResponseEntity<JSON> updateUserInfo(@RequestHeader Map<String, String> headers, @RequestBody String jsonstr) {

        String token = headers.get("authorization").split(" ")[1];
        String usernameAndPassword = new String(Base64.getDecoder().decode(token));
        String username = usernameAndPassword.split(":")[0];
        String password = usernameAndPassword.split(":")[1];

        User user = userRepo.findByUsername(username);
        if (user == null) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

        if (!BCrypt.checkpw(password, user.getPassword())) {
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

        userRepo.save(user);
        return new ResponseEntity<>(null,
                HttpStatus.NO_CONTENT);
    }

    @PostMapping(value = "/v1/user")
    public ResponseEntity<JSON> createUser(@RequestBody String jsonstr) {
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

        if (userRepo.findByUsername(username) != null) {
            return new ResponseEntity<>(null,
                    HttpStatus.BAD_REQUEST);
        }

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

        userRepo.save(user);
        return new ResponseEntity<>(null,
                HttpStatus.CREATED);
    }

}
