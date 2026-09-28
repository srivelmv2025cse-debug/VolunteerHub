package com.example.demo.volenteerhub.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record VolunteerRequest(
        @NotBlank String name,
        @NotBlank @Email String email,
        @Size(max = 30)
        @Pattern(regexp = "^\\+?[0-9][0-9(). -]{5,28}[0-9]$", message = "Phone must be a valid phone number.")
        String phone) {
}
