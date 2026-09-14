package com.example.ecommerce.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;

/**
 * Request body for POST /ask.
 * The question field contains the natural language query from the user.
 */
@Builder
public record AskRequest(

        @NotBlank(message = "Question must not be blank")
        @Size(max = 500, message = "Question must be at most 500 characters")
        String question

) {}
