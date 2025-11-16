package org.example.userservice;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.example.userservice.controller.UserController;
import org.example.userservice.dto.CreateUserRequest;
import org.example.userservice.dto.UpdateUserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.hateoas.UserModelAssembler;
import org.example.userservice.service.UserService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Date;
import java.util.List;

import static org.hamcrest.Matchers.endsWith;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(UserController.class)
@Import(UserModelAssembler.class)
class UserHateoasTest {

    @Autowired
    MockMvc mockMvc;

    @Autowired
    ObjectMapper objectMapper;

    @MockitoBean
    public UserService userService;


    @Test
    void getUserByIdHateoas() throws Exception {
        UserResponse response = new UserResponse(
                1L, "name", "name@mail.ru", 123, new Date(), new Date()
        );
        when(userService.readUser(1L)).thenReturn(response);

        mockMvc.perform(get("/users/read/{id}", 1L))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))

                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.email").value("name@mail.ru"))

                .andExpect(jsonPath("$._links.self.href", endsWith("/users/read/1")))
                .andExpect(jsonPath("$._links.all-users.href", endsWith("/users")))
                .andExpect(jsonPath("$._links.update.href", endsWith("/users/update/1")))
                .andExpect(jsonPath("$._links.delete.href", endsWith("/users/delete/1")));
    }

    @Test
    void getAllUsersHateoas() throws Exception {
        UserResponse response1 = new UserResponse(
                1L, "name", "name@mail.ru", 123, new Date(), new Date()
        );
        UserResponse response2 = new UserResponse(
                2L, "name1", "name1@mail.ru", 12, new Date(), new Date()
        );
        when(userService.getUsers()).thenReturn(List.of(response1, response2));

        mockMvc.perform(get("/users")
                        .accept(MediaTypes.HAL_JSON))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaTypes.HAL_JSON))

                .andExpect(jsonPath("$._links.self.href", endsWith("/users")))

                .andExpect(jsonPath("$._embedded.userResponseList[0].id").value(1))
                .andExpect(jsonPath("$._embedded.userResponseList[0]._links.self.href",
                        endsWith("/users/read/1")))

                .andExpect(jsonPath("$._embedded.userResponseList[1].id").value(2))
                .andExpect(jsonPath("$._embedded.userResponseList[1]._links.self.href",
                        endsWith("/users/read/2")))

                .andExpect(jsonPath("$._links.create.href", endsWith("/users/create")));
    }

    @Test
    void createUserHateoas() throws Exception {
        CreateUserRequest request = new CreateUserRequest("name", "name@mail.ru", 123);

        UserResponse response = new UserResponse(
                11L, "name", "name@mail.ru", 123, new Date(), new Date()
        );

        when(userService.createUser(ArgumentMatchers.any(CreateUserRequest.class))).thenReturn(response);

        mockMvc.perform(post("/users/create")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(11))
                .andExpect(jsonPath("$._links.self.href", endsWith("users/read/11")))
                .andExpect(jsonPath("$._links.all-users.href", endsWith("users")))
                .andExpect(jsonPath("$._links.update.href", endsWith("users/update/11")))
                .andExpect(jsonPath("$._links.delete.href", endsWith("users/delete/11")));
    }

    @Test
    void updateUserHateoas() throws Exception {
        var request = new UpdateUserRequest("updated", "name@mail.ru", 12);
        var response = new UserResponse(12L, "updated", "name@mail.ru", 12, new Date(), new Date());

        when(userService.updateUser(12L, request)).thenReturn(response);

        mockMvc.perform(put("/users/update/{id}", 12L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(content().contentType(MediaTypes.HAL_JSON_VALUE))
                .andExpect(jsonPath("$.id").value(12))
                .andExpect(jsonPath("$.name").value("updated"))
                .andExpect(jsonPath("$._links.self.href", endsWith("/users/read/12")))
                .andExpect(jsonPath("$._links.update.href", endsWith("/users/update/12")));
    }
}
