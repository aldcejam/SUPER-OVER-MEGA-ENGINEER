package com.supersys.analysis;

import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.web.server.LocalServerPort;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.notNullValue;

public class ProjectAnalysisIntegrationTest extends AbstractIntegrationTest {

    @LocalServerPort
    private int port;

    @BeforeEach
    void setUp() {
        RestAssured.port = port;
    }

    @Test
    void shouldFindAllProjectsViaGraphQL() {
        String graphqlQuery = """
            {
              "query": "{ findAllProjects { id name budget } }"
            }
            """;

        given()
            .contentType(ContentType.JSON)
            .body(graphqlQuery)
        .when()
            .post("/graphql")
        .then()
            .statusCode(200)
            .body("data.findAllProjects", notNullValue());
    }
}
