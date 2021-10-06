package com.tianqizhang.webapp.Controller;

import com.tianqizhang.webapp.Models.AuthenticationRequest;
import com.tianqizhang.webapp.Models.AuthenticationResponse;
import com.tianqizhang.webapp.Models.User;
import com.tianqizhang.webapp.Repo.UserRepo;
import com.tianqizhang.webapp.Services.MyUserDetailsService;
import com.tianqizhang.webapp.Utils.JwtUtil;
import com.tianqizhang.webapp.Utils.RestResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;
import com.alibaba.fastjson.JSONObject;

@RestController
public class ApiController {
    @Autowired
    private UserRepo userRepo;

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private JwtUtil jwtTokenUtil;

    @Autowired
    private MyUserDetailsService userDetailsService;

    @GetMapping(value = "/")
    public String getPage() {
        return "Welcome!";
    }

    @GetMapping(value = "/v1/user/self")
    public RestResponse getUserInfo(@RequestHeader Map<String, String> headers) {
        String username;
        try{
            String token = headers.get("authorization").split(" ")[1];
            username = JwtUtil.extractUsername(token);
        } catch (Exception e) {
            return RestResponse.buildUnauthorized(null);
        }

        User user = userRepo.findByUsername(username);

        Map<String, Object> usermap = new HashMap<>();
        usermap.put("id", user.getId());
        usermap.put("first_name", user.getFirst_name());
        usermap.put("last_name", user.getLast_name());
        usermap.put("username", username);
        usermap.put("account_created", user.getAccount_created());
        usermap.put("account_updated", user.getAccount_updated());

        return RestResponse.buildSuccess(new JSONObject(usermap));
    }

    @PutMapping(value = "/v1/user/self")
    public RestResponse updateUserInfo(@RequestHeader Map<String, String> headers, @RequestBody String jsonstr) {
        String username;
        try{
            String token = headers.get("authorization").split(" ")[1];
            username = JwtUtil.extractUsername(token);
        } catch (Exception e) {
            return RestResponse.buildUnauthorized(null);
        }

        User user = userRepo.findByUsername(username);
        JSONObject userJsonObject = JSONObject.parseObject(jsonstr);

        if (!userJsonObject.containsKey("username")) {
            return RestResponse.buildUnauthorized(null);
        }

        for (String key : userJsonObject.keySet()) {
            // Check if the username is correct. User can't update other user's info.
            if (key.equals("username")) {
                if (!userJsonObject.getString(key).equals(username)) {
                    return RestResponse.buildUnauthorized(null);
                }
            }

            // Attempt to update any other field should return 400 Bad Request HTTP response code.
            if (!key.equals("first_name") && !key.equals("last_name") && !key.equals("password") && !key.equals("username")) {
                return RestResponse.buildBadRequest(null);
            }
        }

        User.updateUser(user,
                userJsonObject.getString("first_name"),
                userJsonObject.getString("last_name"),
                userJsonObject.getString("password"));

        userRepo.save(user);
        return RestResponse.buildSuccess(null);
    }

    @PostMapping(value = "/v1/user")
    public RestResponse createUser(@RequestBody String jsonstr) {
        JSONObject userJsonObject = JSONObject.parseObject(jsonstr);

        String username = userJsonObject.getString("username");

        if (username == null) {
            return RestResponse.buildBadRequest(null);
        }

        String regex = "^[a-zA-Z0-9_!#$%&'*+/=?`{|}~^.-]+@[a-zA-Z0-9.-]+$";
        if (!username.matches(regex)) {
            return RestResponse.buildBadRequest(null);
        }

        if (userRepo.findByUsername(username) != null) {
            return RestResponse.buildBadRequest(null);
        }

        if (userJsonObject.getString("first_name") == null ||
                userJsonObject.getString("last_name") == null ||
                userJsonObject.getString("password") == null) {
            return RestResponse.buildBadRequest(null);
        }

        User user = new User(userJsonObject.getString("first_name"),
                userJsonObject.getString("last_name"),
                userJsonObject.getString("password"),
                userJsonObject.getString("username"));

        userRepo.save(user);
        return RestResponse.buildSuccess(null);
    }

    @RequestMapping(value = "/v1/user/authenticate", method = RequestMethod.POST)
    public ResponseEntity<?> createAuthenticationToken(@RequestBody AuthenticationRequest authenticationRequest) throws Exception {

        try {
            authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(authenticationRequest.getUsername(), authenticationRequest.getPassword())
            );
        }
        catch (BadCredentialsException e) {
            throw new Exception("Incorrect username or password", e);
        }


        final UserDetails userDetails = userDetailsService
                .loadUserByUsername(authenticationRequest.getUsername());

        final String jwt = jwtTokenUtil.generateToken(userDetails);

        return ResponseEntity.ok(new AuthenticationResponse(jwt));
    }


}
