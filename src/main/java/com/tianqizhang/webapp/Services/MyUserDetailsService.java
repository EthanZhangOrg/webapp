package com.tianqizhang.webapp.Services;

import com.tianqizhang.webapp.CustomUserDetails;
import com.tianqizhang.webapp.Models.User;
import com.tianqizhang.webapp.Repo.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class MyUserDetailsService implements UserDetailsService {

    @Autowired
    private UserRepo userRepo;

    @Override
    public UserDetails loadUserByUsername(String s) throws UsernameNotFoundException {
        return new CustomUserDetails(userRepo.findByUsername(s));
    }
}
