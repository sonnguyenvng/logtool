package com.vng.teg.logtool.web.security;

import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.web.filter.GenericFilterBean;

import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Created by Son on 8/13/14.
 */
public class AuthenticationTokenProcessingFilter extends GenericFilterBean {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response,
                         FilterChain chain) throws IOException, ServletException {
        HttpServletRequest httpServletRequest = (HttpServletRequest)request;
        boolean hasError = false, disable = false;
        String error = null;
        if(hasError || disable){
//            RestResultDTO restResultDTO = RestUtils.createInvalidOutput(errorCode);
//            restResultDTO.setData(error);
//            Gson gson = new GsonBuilder().serializeNulls().create();
//            response.getOutputStream().println(gson.toJson(restResultDTO));
        }else{
            // continue thru the filter chain
            chain.doFilter(request, response);
        }
    }

    AuthenticationManager authenticationManager;

    public AuthenticationManager getAuthenticationManager() {
        return authenticationManager;
    }
    public void setAuthenticationManager(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

}
