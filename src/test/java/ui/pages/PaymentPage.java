package ui.pages;

import com.microsoft.playwright.Page;

public class PaymentPage {

    private final Page page;

    public PaymentPage(Page page) {
        this.page = page;
    }

    public PaymentPage fillDummyCard() {
        page.locator("input[data-qa='name-on-card']").fill("Anatoliy QA");
        page.locator("input[data-qa='card-number']").fill("4242424242424242");
        page.locator("input[data-qa='cvc']").fill("123");
        page.locator("input[data-qa='expiry-month']").fill("12");
        page.locator("input[data-qa='expiry-year']").fill("2030");
        return this;
    }

    public boolean confirmOrder() {
        page.locator("[data-qa='pay-button']").click();
        return page.getByText("Congratulations! Your order has been placed successfully!").isVisible();
    }
}
