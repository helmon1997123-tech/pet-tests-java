package ui.pages;

import com.microsoft.playwright.Locator;
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

        // isVisible() проверяет состояние немедленно и не ждёт перерисовки страницы после клика -
        // без явного waitFor() проверка срабатывает раньше, чем сообщение успевает появиться
        Locator successMessage = page.getByText("Your order has been placed successfully!");
        successMessage.waitFor(new Locator.WaitForOptions().setTimeout(10000));
        return successMessage.isVisible();
    }
}