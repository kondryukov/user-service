package org.example.userservice.service;

import jakarta.validation.Valid;
import org.example.userservice.domain.User;
import org.example.userservice.dto.CreateUserRequest;
import org.example.userservice.dto.UpdateUserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.events.OperationType;
import org.example.userservice.events.UserEvent;
import org.example.userservice.exception.types.ConflictException;
import org.example.userservice.exception.types.NotFoundException;
import org.example.userservice.mapper.UserMapper;
import org.example.userservice.messaging.UserKafkaProducer;
import org.example.userservice.repository.UserRepository;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Component
@Service
public class UserService {

    private final UserMapper userMapper;
    private final UserRepository userRepository;
    private final UserKafkaProducer userKafkaProducer;

    public UserService(UserRepository userRepository, UserMapper userMapper, UserKafkaProducer userKafkaProducer) {
        this.userRepository = userRepository;
        this.userMapper = userMapper;
        this.userKafkaProducer = userKafkaProducer;
    }

    @Transactional
    public UserResponse createUser(@Valid CreateUserRequest request) {
        String normalizedEmail = request.email().trim().toLowerCase();
        mailUnique(normalizedEmail);
        User user = userMapper.fromCreate(request);
        userRepository.save(user);

        UserEvent userEvent = new UserEvent();
        userEvent.setEmail(normalizedEmail);
        userEvent.setOperation(OperationType.CREATE);
        userKafkaProducer.sendUserToKafka(userEvent);

        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse readUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        return userMapper.toResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Long id, @Valid UpdateUserRequest request) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));
        if (request.email() != null) {
            String normalizedEmail = request.email().trim().toLowerCase();
            mailUnique(normalizedEmail);
            user.setEmail(normalizedEmail);
        }
        userMapper.applyUpdate(request, user);
        return userMapper.toResponse(user);
    }

    @Transactional
    public void removeUserById(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new NotFoundException("User not found"));

        UserEvent userEvent = new UserEvent();
        userEvent.setEmail(user.getEmail());
        userEvent.setOperation(OperationType.DELETE);
        userKafkaProducer.sendUserToKafka(userEvent);

        userRepository.deleteById(id);

    }

    @Transactional
    public List<UserResponse> getUsers() {
        List<User> users = userRepository.findAll();
        List<UserResponse> userResponseList = new ArrayList<>();
        for (User user : users) {
            userResponseList.add(userMapper.toResponse(user));
        }
        return userResponseList;
    }

    public void mailUnique(String email) {
        String normalizedEmail = email.trim().toLowerCase();
        if (userRepository.existsUserByEmail(normalizedEmail)) {
            throw new ConflictException("Email already in use");
        }
    }
}
