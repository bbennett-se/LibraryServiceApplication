package com.library.user.service;


import com.library.user.dto.CreateUserRequest;
import com.library.user.dto.UpdateUserRequest;
import com.library.user.entity.Role;
import com.library.user.entity.User;
import com.library.user.entity.UserStatus;
import com.library.user.exception.EmailAlreadyExistsException;
import com.library.user.exception.UserNotFoundException;
import com.library.user.repository.UserRepository;
import jakarta.validation.constraints.Email;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User create(CreateUserRequest request) {
        if(userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }
        User user = new User();
        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(request.role());
        user.setStatus(UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public User findById(UUID id) {
        return userRepository.findById(id)
                .orElseThrow( () -> new UserNotFoundException(id));
    }

    @Transactional(readOnly = true)
    public Page<User> findAll(Role role, UserStatus status, Pageable pageable) {
        if(role != null && status != null) {
            return userRepository.findByRoleAndStatus(role, status, pageable);
        }

        if(role != null) {
            return userRepository.findByRole(role, pageable);
        }

        if(status != null) {
            return userRepository.findByStatus(status, pageable);
        }

        return userRepository.findAll(pageable);
    }

    public User update(UUID id, UpdateUserRequest request) {
        User user = findById(id);

        if(!user.getEmail().equals(request.email())
        && userRepository.existsByEmail(request.email())) {
            throw new EmailAlreadyExistsException(request.email());
        }

        user.setEmail(request.email());
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setRole(request.role());

        return user;
    }

    public User updateStatus( UUID id, UserStatus status) {
        User user = findById(id);
        user.setStatus(status);
        return user;
    }

    public void delete(UUID id) {
        if(!userRepository.existsById(id)) {
            throw new UserNotFoundException(id);
        }

        userRepository.deleteById(id);
    }
}
