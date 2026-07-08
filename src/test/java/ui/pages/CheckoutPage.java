package ui.pages;

import com.microsoft.playwright.Page;

public class CheckoutPage {

    private final Page page;

    public CheckoutPage(Page page) {
        this.page = page;
    }

    public boolean isAddressReviewVisible() {
        return page.getByText("Address Details").isVisible()
                && page.getByText("Review Your Order").isVisible();
    }

    public CheckoutPage enterOrderComment(String comment) {
        page.locator("textarea[name='message']").fill(comment);
        return this;
    }

    public PaymentPage placeOrder() {
        page.getByText("Place Order").click();
        return new PaymentPage(page);
    }
}
