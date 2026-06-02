package com.library.circulation.client;

import org.springframework.stereotype.Component;

import java.util.UUID;

//STUB IMPLEMENTATION
//TODO: HTTP IMPLEMENTATION
@Component
public class UserServiceClient {
    public UserSummary getUser (UUID userId) {
        //change RestClient call to: http://user-service:8083/api/users/{userId}
        return new UserSummary(userId, "ACTIVE");
    }
}
