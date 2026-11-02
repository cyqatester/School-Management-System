package com.example.school;
import io.restassured.RestAssured;

import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;

@SpringBootTest(webEnvironment=SpringBootTest.WebEnvironment.RANDOM_PORT,
                properties={"school.data-file=${java.io.tmpdir}/school-demo-test-${random.uuid}.json"})
class ApiIntegrationTest {
    @LocalServerPort 
    int port;

    @BeforeEach 
    void setPort(){
        RestAssured.port=port;
    }

    @Test void apiRequiresAuthentication(){
        given()
            .redirects().follow(false)
            .when()
            .get("/api/me")
            .then()
            .statusCode(302)
            .header("Location",containsString("/login"));
    }
}
