package ui.pages;

import com.microsoft.playwright.Page;

public class AccountPage {

    private final Page page;

    public AccountPage(Page page) {
        this.page = page;
    }

    public boolean isAccountCreatedVisible() {
        return page.getByText("ACCOUNT CREATED!").isVisible();
    }

    public AccountPage continueToAccount() {
        page.locator("[data-qa='continue-button']").click();
        return this;
    }

    public boolean isLoggedInAs(String name) {
        return page.getByText("Logged in as " + name).isVisible();
    }

    public void deleteAccount() {
        page.locator("a[href='/delete_account']").click();
    }

    public boolean isAccountDeletedVisible() {
        return page.getByText("ACCOUNT DELETED!").isVisible();
    }
}
