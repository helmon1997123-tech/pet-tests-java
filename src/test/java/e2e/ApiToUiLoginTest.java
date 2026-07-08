package e2e;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import io.restassured.RestAssured;
import io.restassured.parsing.Parser;
import org.junit.jupiter.api.Test;
import ui.BaseUiTest;
import ui.pages.AccountPage;
import ui.pages.SignupLoginPage;
import util.ConfigReader;

import java.util.UUID;

import static io.restassured.RestAssured.given;
import static org.hamcrest.Matchers.equalTo;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Проверяет, что бэкенд и фронт согласованы: аккаунт, созданный через API,
 * должен реально позволять залогиниться через UI теми же данными.
 */
@Epic("End-to-end")
@Feature("API create -> UI login")
public class ApiToUiLoginTest extends BaseUiTest {

    @Test
    @Description("createAccount через API -> логин под теми же email/password через UI -> шапка показывает имя")
    void accountCreatedViaApiCanLoginViaUi() {
        RestAssured.registerParser("text/html", Parser.JSON);

        String name = "Anatoliy QA";
        String email = "qa.pet.project.e2e." + UUID.randomUUID() + "@example.com";
        String password = "P@ssw0rd123";

        given()
            .baseUri(ConfigReader.baseUrl())
            .formParam("name", name)
            .formParam("email", email)
            .formParam("password", password)
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
            .body("responseCode", equalTo(201));

        try {
            SignupLoginPage loginPage = new SignupLoginPage(page);
            loginPage.login(email, password);

            AccountPage account = new AccountPage(page);
            assertTrue(account.isLoggedInAs(name),
                    "После логина по данным, созданным через API, в шапке должно быть 'Logged in as " + name + "'");
        } finally {
            // подчищаем за собой вне зависимости от исхода UI-шагов
            given()
                .baseUri(ConfigReader.baseUrl())
                .formParam("email", email)
                .formParam("password", password)
            .when()
                .delete("/api/deleteAccount")
            .then()
                .statusCode(200);
        }
    }
}
