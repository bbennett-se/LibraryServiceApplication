package com.library.user.dto;

import com.library.user.entity.UserStatus;
import jakarta.validation.constraints.NotNull;

public record UpdateStatusRequest(
        @NotNull UserStatus status
) {
}
