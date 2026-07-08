package ui.pages;

import com.microsoft.playwright.Page;

public class CartPage {

    private final Page page;

    public CartPage(Page page) {
        this.page = page;
    }

    public int itemsCount() {
        return page.locator("#cart_info_table tbody tr").count();
    }

    public boolean isLoaded() {
        return page.locator("#cart_info_table").isVisible();
    }

    public CheckoutPage proceedToCheckout() {
        page.getByText("Proceed To Checkout").click();
        return new CheckoutPage(page);
    }
}
