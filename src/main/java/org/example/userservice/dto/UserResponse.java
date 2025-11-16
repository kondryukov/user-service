package org.example.userservice.dto;

import io.swagger.v3.oas.annotations.media.Schema;

import java.util.Date;

@Schema(description = "Response with user data")
public record UserResponse(

        @Schema(description = "Unique identification of user", example = "123")
        Long id,

        @Schema(description = "User name", example = "Name")
        String name,

        @Schema(description = "User email", example = "name@mail.ru")
        String email,

        @Schema(description = "User age", example = "21")
        Integer age,

        @Schema(description = "Time of user creation", example = "2025-11-08T11:45:51.077Z")
        Date createdAt,

        @Schema(description = "Time of last user update", example = "2025-11-09T11:45:51.077Z")
        Date updatedAt
) {
}
