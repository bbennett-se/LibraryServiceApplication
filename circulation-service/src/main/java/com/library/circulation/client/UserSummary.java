package com.library.circulation.client;

import java.util.UUID;

public record UserSummary(
        UUID id,
        String status   //"ACTIVE" OR "SUSPENDED"
){}
