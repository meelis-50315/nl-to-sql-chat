package com.example.ecommerce.service;

import com.example.ecommerce.dto.AskResponse;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class NlToSqlService {

    private static final String SQL_SYSTEM_PROMPT_TEMPLATE = """
            You are a %s query generator for an e-commerce database.
            
            SCHEMA:
            customers(id UUID PK, email TEXT, first_name TEXT, last_name TEXT, phone TEXT)
            products(id UUID PK, name TEXT, price NUMERIC, stock INTEGER, category TEXT)
            orders(id UUID PK, customer_id UUID, status TEXT, total_amount NUMERIC)
            order_items(id UUID PK, order_id UUID, product_id UUID, quantity INTEGER, unit_price NUMERIC)
            
            RULES:
            1. Return ONLY a single raw SQL SELECT statement - no markdown or explanation.
            2. If you cannot answer from this schema, return: CANNOT_ANSWER
            3. Never generate INSERT, UPDATE, DELETE, DROP or any non-SELECT statement.
            4. Use only %s SQL syntax and functions.
            """;

    private static final String SQL_USER_PROMPT_TEMPLATE = """
            Generate SQL syntax for the question below
            
            %s
            """;

    private static final String SUMMARY_SYSTEM_PROMPT = """
            You are a helpful assistant that summarizes data clearly.
            """;

    private static final String SUMMARY_USER_PROMPT_TEMPLATE = """
            Summarize this database query result in 1-3 plain sentences.
            Don't mention SQL or technical terms. Be specific about numbers.
            
            Question: %s
            Results (%d rows): %s
            """;

    private final JdbcTemplate jdbcTemplate;
    private final ChatClient chatClient;
    private final String sqlSystemPrompt;

    public NlToSqlService(JdbcTemplate jdbcTemplate, ChatClient.Builder builder,
                          @Value("${nlsql.dialect}") String dialect) {
        this.jdbcTemplate = jdbcTemplate;
        this.chatClient = builder.build();
        this.sqlSystemPrompt = SQL_SYSTEM_PROMPT_TEMPLATE.formatted(dialect, dialect);
    }

    public AskResponse ask(String userQuestion) {
        String generatedSql = generateSql(userQuestion);
        List<Map<String, Object>> databaseResults = jdbcTemplate.queryForList(generatedSql);

        return generateResponse(userQuestion, databaseResults);
    }

    private AskResponse generateResponse(String userQuestion, List<Map<String, Object>> databaseResults) {
        String answer = formatResult(userQuestion, databaseResults).answer();

        return AskResponse.builder()
                .answer(answer)
                .build();
    }

    private AskResponse formatResult(String userQuestion, List<Map<String, Object>> databaseResults) {
        String userPrompt = SUMMARY_USER_PROMPT_TEMPLATE.formatted(
                userQuestion,
                databaseResults.size(),
                databaseResults);

        return callLlm(SUMMARY_SYSTEM_PROMPT, userPrompt);
    }

    private String generateSql(String userQuestion) {
        String generatedSql = callLlm(sqlSystemPrompt, SQL_USER_PROMPT_TEMPLATE.formatted(userQuestion)).answer();
        validateSqlQuery(generatedSql);

        return generatedSql;
    }

    private void validateSqlQuery(String generatedSql) {
        String upperCaseSql = generatedSql.toUpperCase();

        if (!upperCaseSql.startsWith("SELECT")) {
            throw new IllegalArgumentException("Only SELECT queries are allowed");
        }

        if (upperCaseSql.matches(".*\\b(DROP|DELETE|INSERT|UPDATE|TRUNCATE|ALTER|GRANT|COPY|CALL|DO)\\b.*")) {
            throw new IllegalArgumentException("Query contains forbidden SQL keywords");
        }
    }

    private AskResponse callLlm(String systemPrompt, String userPrompt) {
        AskResponse response = chatClient.prompt()
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .responseEntity(AskResponse.class)
                .getEntity();

        if (response == null) {
            throw new IllegalStateException("AI model returned an empty response");
        }

        return response;
    }
}
