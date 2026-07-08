package ui.pages;

import com.microsoft.playwright.Page;
import util.ConfigReader;

public class SignupLoginPage {

    private final Page page;

    public SignupLoginPage(Page page) {
        this.page = page;
        page.navigate(ConfigReader.baseUrl() + "/login");
    }

    public SignupLoginPage login(String email, String password) {
        page.locator("input[data-qa='login-email']").fill(email);
        page.locator("input[data-qa='login-password']").fill(password);
        page.locator("button[data-qa='login-button']").click();
        return this;
    }

    public boolean isLoginErrorVisible() {
        return page.getByText("Your email or password is incorrect!").isVisible();
    }

    public AccountInfoPage signup(String name, String email) {
        page.locator("input[data-qa='signup-name']").fill(name);
        page.locator("input[data-qa='signup-email']").fill(email);
        page.locator("button[data-qa='signup-button']").click();
        return new AccountInfoPage(page);
    }
}
