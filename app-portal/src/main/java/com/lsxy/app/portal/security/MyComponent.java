package com.lsxy.app.portal.security;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Created by Tandy on 2016/6/7.
 */
@Component
public class MyComponent {
    private String userName;
    public MyComponent(){
        this.userName = "hello";
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }
}