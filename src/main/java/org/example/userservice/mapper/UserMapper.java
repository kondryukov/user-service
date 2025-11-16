package org.example.userservice.mapper;

import org.example.userservice.domain.User;
import org.example.userservice.dto.CreateUserRequest;
import org.example.userservice.dto.UpdateUserRequest;
import org.example.userservice.dto.UserResponse;
import org.springframework.stereotype.Component;

@Component
public final class UserMapper {

    public UserResponse toResponse(User user) {
        return new UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getAge(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

    public User fromCreate(CreateUserRequest request) {
        User user = new User();
        user.setName(request.name());
        user.setEmail(request.email());
        user.setAge(request.age());
        return user;
    }

    public void applyUpdate(UpdateUserRequest request, User user) {
        if (request.name() != null) user.setName(request.name());
        if (request.age() != null) user.setAge(request.age());
    }
}
