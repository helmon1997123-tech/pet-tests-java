package ui.pages;

import com.microsoft.playwright.Page;

public class AccountInfoPage {

    private final Page page;

    public AccountInfoPage(Page page) {
        this.page = page;
    }

    public AccountPage fillAndSubmit(String password) {
        page.locator("#id_gender1").check(); // Mr
        page.locator("#password").fill(password);
        page.locator("#days").selectOption("10");
        page.locator("#months").selectOption("5");
        page.locator("#years").selectOption("1990");

        page.locator("#first_name").fill("Anatoliy");
        page.locator("#last_name").fill("QA");
        page.locator("#company").fill("Demiand");
        page.locator("#address1").fill("Test street 1");
        page.locator("#country").selectOption("Canada");
        page.locator("#state").fill("Riga");
        page.locator("#city").fill("Riga");
        page.locator("#zipcode").fill("LV-1010");
        page.locator("#mobile_number").fill("+37100000000");

        page.locator("button[data-qa='create-account']").click();
        return new AccountPage(page);
    }
}
