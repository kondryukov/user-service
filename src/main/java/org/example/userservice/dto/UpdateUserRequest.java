package org.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

@Schema(description = "Request on updating already existing user")
public record UpdateUserRequest(

        @Schema(description = "User name", example = "Name")
        @Size(max = 50)
        String name,

        @Schema(description = "User email", example = "name@mail.ru")
        @Email
        @Size(max = 254)
        String email,

        @Schema(description = "User age", example = "21")
        Integer age
) {
    @Override
    public String email() {
        if (email == null) {
            return null;
        }
        return email.trim().isEmpty() ? null : email;
    }
}
