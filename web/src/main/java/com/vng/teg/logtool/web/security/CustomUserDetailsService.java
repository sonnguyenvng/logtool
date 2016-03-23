package com.vng.teg.logtool.web.security;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

/**
 * Created by Son on 8/6/14.
 */

public class CustomUserDetailsService implements UserDetailsService {
    public UserDetails loadUserByUsername(String userId)
            throws UsernameNotFoundException {
        UserDetails userDetail = new org.springframework.security.core.userdetails.User(userId, "", true, true, true, true, null);

        return userDetail;
    }

}
