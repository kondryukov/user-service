package org.example.userservice;


import org.example.userservice.domain.User;
import org.example.userservice.dto.CreateUserRequest;
import org.example.userservice.dto.UpdateUserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.mapper.UserMapper;
import org.junit.jupiter.api.Test;

import java.util.Date;

import static org.assertj.core.api.Assertions.assertThat;

class UserMapperTest {

    private final UserMapper mapper = new UserMapper();

    @Test
    void fromCreate() {
        CreateUserRequest request = new CreateUserRequest("name", "name@mail.ru", 12);
        User user = mapper.fromCreate(request);

        assertThat(user.getEmail()).isEqualTo("name@mail.ru");
        assertThat(user.getName()).isEqualTo("name");
        assertThat(user.getAge()).isEqualTo(12);
    }

    @Test
    void toResponse() {
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);

        UserResponse actual = mapper.toResponse(user);

        assertThat(actual.email()).isEqualTo("name@mail.ru");
        assertThat(actual.name()).isEqualTo("name");
        assertThat(actual.age()).isEqualTo(123);
    }

    @Test
    void applyUpdate() {
        UpdateUserRequest request = new UpdateUserRequest("newName", "name@mail.ru", 12);
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);

        mapper.applyUpdate(request, user);

        assertThat(user.getName()).isEqualTo("newName");
        assertThat(user.getAge()).isEqualTo(12);
    }

    @Test
    void applyUpdateAllNull() {
        UpdateUserRequest request = new UpdateUserRequest(null, "name@mail.ru", null);
        Date date = new Date();
        User user = new User(1L, "name", "name@mail.ru", 123, date, date);

        mapper.applyUpdate(request, user);

        assertThat(user.getName()).isEqualTo("name");
        assertThat(user.getAge()).isEqualTo(123);
    }
}
