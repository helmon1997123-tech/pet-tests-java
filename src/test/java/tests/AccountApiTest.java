package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

/**
 * Аккаунт создаётся уникальным на каждый запуск (email с UUID), чтобы тесты
 * не зависели от состояния сайта между прогонами. Методы внутри класса
 * выполняются по порядку — это единый жизненный цикл одного аккаунта.
 */
@Epic("Automation Exercise API")
@Feature("Account lifecycle")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class AccountApiTest extends BaseTest {

    private static final String EMAIL = "qa.pet.project." + UUID.randomUUID() + "@example.com";
    private static final String PASSWORD = "P@ssw0rd123";

    @Test
    @Order(1)
    @Description("POST /api/createAccount -> 201, User created!")
    void createAccount() {
        given()
            .formParam("name", "Anatoliy QA")
            .formParam("email", EMAIL)
            .formParam("password", PASSWORD)
            .formParam("title", "Mr")
            .formParam("birth_date", "1")
            .formParam("birth_month", "1")
            .formParam("birth_year", "1990")
            .formParam("firstname", "Anatoliy")
            .formParam("lastname", "QA")
            .formParam("company", "Demiand")
            .formParam("address1", "Test street 1")
            .formParam("address2", "")
            .formParam("country", "Latvia")
            .formParam("zipcode", "LV-1010")
            .formParam("state", "Riga")
            .formParam("city", "Riga")
            .formParam("mobile_number", "+37100000000")
        .when()
            .post("/api/createAccount")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(201))
            .body("message", containsString("User created!"));
    }

    @Test
    @Order(2)
    @Description("POST /api/verifyLogin с валидными данными -> User exists!")
    void verifyLoginValid() {
        given()
            .formParam("email", EMAIL)
            .formParam("password", PASSWORD)
        .when()
            .post("/api/verifyLogin")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(200))
            .body("message", equalTo("User exists!"));
    }

    @Test
    @Order(3)
    @Description("GET /api/getUserDetailByEmail -> данные совпадают с созданными")
    void getUserDetailByEmail() {
        given()
            .queryParam("email", EMAIL)
        .when()
            .get("/api/getUserDetailByEmail")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(200))
            .body("user.email", equalTo(EMAIL))
            .body("user.first_name", equalTo("Anatoliy"));
    }

    @Test
    @Order(4)
    @Description("PUT /api/updateAccount -> 200, User updated!")
    void updateAccount() {
        given()
            .formParam("name", "Anatoliy QA Updated")
            .formParam("email", EMAIL)
            .formParam("password", PASSWORD)
            .formParam("title", "Mr")
            .formParam("birth_date", "1")
            .formParam("birth_month", "1")
            .formParam("birth_year", "1990")
            .formParam("firstname", "Anatoliy")
            .formParam("lastname", "Updated")
            .formParam("company", "Demiand")
            .formParam("address1", "Test street 2")
            .formParam("address2", "")
            .formParam("country", "Latvia")
            .formParam("zipcode", "LV-1010")
            .formParam("state", "Riga")
            .formParam("city", "Riga")
            .formParam("mobile_number", "+37100000001")
        .when()
            .put("/api/updateAccount")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(200))
            .body("message", containsString("User updated!"));
    }

    @Test
    @Order(5)
    @Description("DELETE /api/deleteAccount -> 200, Account deleted!")
    void deleteAccount() {
        given()
            .formParam("email", EMAIL)
            .formParam("password", PASSWORD)
        .when()
            .delete("/api/deleteAccount")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(200))
            .body("message", containsString("Account deleted!"));
    }

    @Test
    @Order(6)
    @Description("После удаления verifyLogin с теми же данными -> User not found!")
    void verifyLoginAfterDeleteFails() {
        given()
            .formParam("email", EMAIL)
            .formParam("password", PASSWORD)
        .when()
            .post("/api/verifyLogin")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(404))
            .body("message", equalTo("User not found!"));
    }

    @ParameterizedTest(name = "verifyLogin без обязательного поля: email=\"{0}\", password=\"{1}\"")
    @CsvSource({
            "'', P@ssw0rd123",
            "someone@example.com, ''"
    })
    @Description("POST /api/verifyLogin без email или без password -> responseCode 400")
    void verifyLoginMissingRequiredField(String email, String password) {
        var request = given();
        if (!email.isBlank()) {
            request.formParam("email", email);
        }
        if (!password.isBlank()) {
            request.formParam("password", password);
        }
        request
        .when()
            .post("/api/verifyLogin")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(400));
    }

    @Test
    @Description("DELETE /api/verifyLogin -> метод не поддерживается, responseCode 405")
    void deleteVerifyLoginNotSupported() {
        given()
        .when()
            .delete("/api/verifyLogin")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(405));
    }
}
