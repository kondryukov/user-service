package org.example.userservice.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.userservice.domain.User;
import org.example.userservice.dto.CreateUserRequest;
import org.example.userservice.dto.UpdateUserRequest;
import org.example.userservice.dto.UserResponse;
import org.example.userservice.hateoas.UserModelAssembler;
import org.example.userservice.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.hateoas.CollectionModel;
import org.springframework.hateoas.EntityModel;
import org.springframework.hateoas.MediaTypes;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;


@RestController
@RequestMapping(value = "/users", produces = {MediaTypes.HAL_JSON_VALUE, MediaType.APPLICATION_JSON_VALUE })
@Tag(name = "Users Module", description = "User management operations")
public class UserController {

    private final UserService service;
    private final UserModelAssembler assembler;
    Logger logger = LoggerFactory.getLogger(UserController.class);

    public UserController(UserService service, UserModelAssembler assembler) {
        this.service = service;
        this.assembler = assembler;
    }

    @ApiResponse(responseCode = "200", description = "List of users")
    @Operation(summary = "Get all users",
            description = "Get all existing users. The response is User List where each element " +
                    "is User object with id, name, email, age, creation time and last update time.")
    @GetMapping
    public CollectionModel<EntityModel<UserResponse>> getAllUsers() {
        return assembler.toCollectionModel(service.getUsers());
    }

    @ApiResponse(responseCode = "200", description = "User is found",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = User.class))})
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @Operation(summary = "Read user",
            description = "Read user by id. The response is User object with" +
                    " id, name, email, age, creation time and last update time.")
    @GetMapping("read/{id}")
    public EntityModel<UserResponse> getUserById(
            @Parameter(description = "ID of user to be retrieved", required = true)
            @PathVariable
            Long id
    ) {
        UserResponse user = service.readUser(id);
        return assembler.toModel(user);
    }


    @ApiResponse(responseCode = "200", description = "User is updated",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = User.class))})
    @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content)
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email is already in use", content = @Content)
    @Operation(summary = "Update an user",
            description = "Update an existing user. The response is updated User object with id, name, email, age, creation time and last update time.")
    @PutMapping(path = "update/{id}", consumes = MediaType.APPLICATION_JSON_VALUE)
    public EntityModel<UserResponse> updateUser(
            @PathVariable
            @Parameter(description = "ID of user to be updated", required = true)
            Long id,

            @Valid @RequestBody
            UpdateUserRequest request
    ) {
        return assembler.toModel(service.updateUser(id, request));
    }

    @ApiResponse(responseCode = "201", description = "User is created",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = User.class))})
    @ApiResponse(responseCode = "400", description = "Invalid data", content = @Content)
    @ApiResponse(responseCode = "409", description = "Email is already in use", content = @Content)
    @Operation(summary = "Create user",
            description = "Create user. The response is User object with id, name, email, age, creation time and last update time.")
    @PostMapping(path = "/create", consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<EntityModel<UserResponse>> createUser(
            @Valid
            @RequestBody
            CreateUserRequest request,

            UriComponentsBuilder uriBuilder
    ) {
        logger.info("Received request to create user");
        UserResponse created = service.createUser(request);
        logger.info("Created: {}", created);
        EntityModel<UserResponse> model = assembler.toModel(created);
        return ResponseEntity
                .created(uriBuilder.path("/users/read/{id}")
                        .buildAndExpand(created.id())
                        .toUri())
                .body(model);
    }

    @ApiResponse(responseCode = "204", description = "User is deleted",
            content = {@Content(mediaType = "application/json", schema = @Schema(implementation = User.class))})
    @ApiResponse(responseCode = "404", description = "User not found", content = @Content)
    @Operation(summary = "Delete user",
            description = "Delete existing user. There is no response body")
    @DeleteMapping("delete/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteUser(
            @PathVariable
            @Parameter(description = "ID of user to be deleted", required = true)
            Long id
    ) {
        service.removeUserById(id);
        return ResponseEntity.noContent().build();
    }
}