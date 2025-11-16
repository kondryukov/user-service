package org.example.userservice;

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
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Date;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    UserRepository userRepository;

    @Mock
    UserMapper mapper;

    @Mock
    UserKafkaProducer userKafkaProducer;

    @InjectMocks
    UserService service;

    @Test
    void createThrowsConflictWhenEmailExists() {
        when(userRepository.existsUserByEmail("name@mail.ru")).thenReturn(true);

        RuntimeException exception = assertThrows(ConflictException.class,
                () -> service.createUser(new CreateUserRequest("name", "name@mail.ru", 1)));
        assertThat(exception.getMessage()).isEqualTo("Email already in use");
    }

    @Test
    void updateThrowsNotFoundWhenUserMissing() {
        when(userRepository.findById(42L)).thenReturn(Optional.empty());
        RuntimeException exception = assertThrows(NotFoundException.class,
                () -> service.updateUser(42L, new UpdateUserRequest("name", "name@mail.ru", 1)));
        assertThat(exception.getMessage()).isEqualTo("User not found");
    }

    @Test
    void createUser() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);

        CreateUserRequest createUserRequest = new CreateUserRequest("name", "naME@mail.ru", 1);
        UserResponse expectedResponse = new UserResponse(1L, "name", "name@mail.ru", 1, date, date);

        when(userRepository.existsUserByEmail("name@mail.ru")).thenReturn(false);
        when(mapper.fromCreate(createUserRequest)).thenReturn(user);
        when(mapper.toResponse(user)).thenReturn(expectedResponse);
        when(userRepository.save(user)).thenReturn(user);

        ArgumentCaptor<UserEvent> userEventArgumentCaptor = ArgumentCaptor.forClass(UserEvent.class);
        UserResponse actualResponse = service.createUser(createUserRequest);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(userKafkaProducer, times(1)).sendUserToKafka(userEventArgumentCaptor.capture());

        UserEvent actualEvent = userEventArgumentCaptor.getValue();

        assertThat(actualEvent.getEmail()).isEqualTo("name@mail.ru");
        assertThat(actualEvent.getOperation()).isEqualTo(OperationType.CREATE);
        verify(userRepository).existsUserByEmail("name@mail.ru");
        verify(userRepository).save(user);
        verifyNoMoreInteractions(userKafkaProducer);
    }

    @Test
    void readUser() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);
        UserResponse userResponse = new UserResponse(1L, "name", "name@mail.ru", 1, date, date);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(userResponse);

        assertThat(service.readUser(1L)).isEqualTo(userResponse);
    }

    @Test
    void readNotExistingUser() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());
        assertThrows(NotFoundException.class, () -> service.readUser(1L));
    }

    @Test
    void updateUser() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(null, "newName@mail.ru", 123);
        UserResponse expectedResponse = new UserResponse(1L, "name", "newname@mail.ru", 1, date, date);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsUserByEmail("newname@mail.ru")).thenReturn(false);
        when(mapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse actualResponse = service.updateUser(1L, updateUserRequest);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(userRepository).findById(1L);
        verify(userRepository).existsUserByEmail("newname@mail.ru");
        verify(mapper).toResponse(user);
    }

    @Test
    void updateUserAllNull() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(null, null, null);
        UserResponse expectedResponse = new UserResponse(1L, "name", "name@mail.ru", 123, date, date);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse actualResponse = service.updateUser(1L, updateUserRequest);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsUserByEmail(anyString());
        verify(mapper).applyUpdate(updateUserRequest, user);
        verify(mapper).toResponse(user);
    }

    @Test
    void updateUserBlankEmail() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);
        UpdateUserRequest updateUserRequest = new UpdateUserRequest("newName", "", 12);
        UserResponse expectedResponse = new UserResponse(1L, "newName", "name@mail.ru", 12, date, date);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(mapper.toResponse(user)).thenReturn(expectedResponse);

        UserResponse actualResponse = service.updateUser(1L, updateUserRequest);

        assertThat(actualResponse).isEqualTo(expectedResponse);
        verify(userRepository).findById(1L);
        verify(userRepository, never()).existsUserByEmail(anyString());
        verify(mapper).applyUpdate(updateUserRequest, user);
        verify(mapper).toResponse(user);
    }

    @Test
    void updateUserDuplicateEmail() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);
        UpdateUserRequest updateUserRequest = new UpdateUserRequest(null, "newName@mail.ru", 123);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));
        when(userRepository.existsUserByEmail("newname@mail.ru")).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.updateUser(1L, updateUserRequest));
        assertThat(exception.getMessage()).isEqualTo("Email already in use");
    }

    @Test
    void deleteUser() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);

        when(userRepository.findById(1L)).thenReturn(Optional.of(user));

        ArgumentCaptor<UserEvent> userEventArgumentCaptor = ArgumentCaptor.forClass(UserEvent.class);
        service.removeUserById(1L);

        verify(userKafkaProducer, times(1)).sendUserToKafka(userEventArgumentCaptor.capture());

        UserEvent actualEvent = userEventArgumentCaptor.getValue();
        UserEvent expectedEvent = new UserEvent();
        expectedEvent.setEmail("name@mail.ru");
        expectedEvent.setOperation(OperationType.DELETE);


        assertThat(actualEvent).isNotEqualTo(null);
        assertThat(actualEvent).isNotEqualTo(user);
        assertThat(actualEvent.hashCode()).isEqualTo(expectedEvent.hashCode());
        assertThat(actualEvent).isEqualTo(actualEvent);
        assertThat(actualEvent).isEqualTo(expectedEvent);

        verify(userRepository).findById(1L);
        verifyNoMoreInteractions(userKafkaProducer);
    }

    @Test
    void removeUserByNotExistingId() {
        when(userRepository.findById(1L)).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class,
                () -> service.removeUserById(1L));
    }

    @Test
    void mailValidAndUnique() {
        String email = "name@mail.ru";
        when(userRepository.existsUserByEmail(email)).thenReturn(true);

        ConflictException exception = assertThrows(ConflictException.class,
                () -> service.mailUnique(email));
        assertThat(exception.getMessage()).isEqualTo("Email already in use");
    }
}
