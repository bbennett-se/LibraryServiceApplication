package com.library.circulation.client;

import java.util.UUID;

public record CopySummary(
        UUID id,
        UUID itemId,
        String status //"AVAILABLE", "CHECKED_OUT", "LOST", "DAMAGED"
) {}
