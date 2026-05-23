package com.library.user.controller;


import com.library.user.dto.CreateUserRequest;
import com.library.user.dto.UpdateStatusRequest;
import com.library.user.dto.UpdateUserRequest;
import com.library.user.dto.UserResponse;
import com.library.user.entity.Role;
import com.library.user.entity.User;
import com.library.user.entity.UserStatus;
import com.library.user.service.UserService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping
    public ResponseEntity<UserResponse> create(@Valid @RequestBody CreateUserRequest request) {

        User user = userService.create(request);
        return ResponseEntity
                .created(URI.create("/api/users/" + user.getId()))
                .body(UserResponse.from(user));
    }

    @GetMapping("/{id}")
    public UserResponse getById(@PathVariable UUID id) {
        return UserResponse.from(userService.findById(id));

    }

    @GetMapping
    public Page<UserResponse> getAll(
            @RequestParam(required = false) Role role,
            @RequestParam(required = false) UserStatus status,
            Pageable pageable
    ) {
        return userService.findAll(role, status, pageable)
                .map(UserResponse::from);
    }

    @PutMapping("/{id}")
    public UserResponse update(@PathVariable UUID id,
                               @Valid @RequestBody UpdateStatusRequest request) {
        return UserResponse.from(userService.updateStatus(id, request.status()));
    }

    @PatchMapping("/{id}/status")
    public UserResponse updateStatus(@PathVariable UUID id,
                                     @Valid @RequestBody UpdateStatusRequest request) {
        return UserResponse.from(userService.updateStatus(id, request.status()));
    }

    @DeleteMapping("/{id}/status")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        userService.delete(id);
    }

}
