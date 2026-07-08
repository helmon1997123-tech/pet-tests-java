package ui;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import ui.pages.AccountPage;
import ui.pages.SignupLoginPage;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Automation Exercise UI")
@Feature("Signup / Login")
public class SignupLoginTest extends BaseUiTest {

    @ParameterizedTest(name = "{0}")
    @MethodSource("invalidLoginScenarios")
    @Description("Логин с неверными данными -> сообщение об ошибке")
    void invalidLoginShowsError(String scenario, String password) {
        SignupLoginPage login = new SignupLoginPage(page);
        login.login("no-such-user-" + UUID.randomUUID() + "@example.com", password);

        assertTrue(login.isLoginErrorVisible());
    }

    static Stream<Arguments> invalidLoginScenarios() {
        return Stream.of(
                Arguments.of("короткий пароль", "123"),
                Arguments.of("длинный случайный пароль", "P@ssw0rd-" + UUID.randomUUID())
        );
    }

    @Test
    @Description("Полный цикл: регистрация -> подтверждение -> логин виден в шапке -> удаление аккаунта")
    void signupAndDeleteAccountLifecycle() {
        String name = "Anatoliy QA";
        String email = "qa.pet.project.ui." + UUID.randomUUID() + "@example.com";

        SignupLoginPage loginPage = new SignupLoginPage(page);
        AccountPage account = loginPage.signup(name, email)
                .fillAndSubmit("P@ssw0rd123");

        assertTrue(account.isAccountCreatedVisible(), "Должно появиться 'ACCOUNT CREATED!'");

        account.continueToAccount();
        assertTrue(account.isLoggedInAs(name), "В шапке должно быть 'Logged in as " + name + "'");

        account.deleteAccount();
        assertTrue(account.isAccountDeletedVisible(), "Должно появиться 'ACCOUNT DELETED!'");
    }
}
