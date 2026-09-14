package com.example.ecommerce.dto;

import lombok.Builder;

import java.util.List;
import java.util.Map;

/**
 * Response body for POST /ask.
 *
 * <ul>
 *   <li>{@code sql}           – the SELECT query that was generated and executed</li>
 *   <li>{@code results}       – raw rows from PostgreSQL as a list of column→value maps</li>
 *   <li>{@code answer}– a human-readable summary produced by a second LLM call</li>
 * </ul>
 */
@Builder
public record AskResponse(

        String answer

) {}

