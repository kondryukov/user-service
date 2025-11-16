package org.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

@Schema(description = "Request on creating new user")
public record CreateUserRequest(

        @Schema(description = "User name", example = "Name")
        @NotBlank
        @Size(max = 50)
        String name,

        @Schema(description = "User email", example = "name@mail.ru")
        @NotBlank
        @Email
        @Size(max = 254)
        String email,

        @Schema(description = "User age", example = "21")
        Integer age) {
}