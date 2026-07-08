package ui.pages;

import com.microsoft.playwright.Locator;
import com.microsoft.playwright.Page;
import util.ConfigReader;

public class ProductsPage {

    private final Page page;

    public ProductsPage(Page page) {
        this.page = page;
        page.navigate(ConfigReader.baseUrl() + "/products");
    }

    public ProductsPage addProductToCartByIndex(int productIndex) {
        Locator productCard = page.locator(".product-image-wrapper").nth(productIndex);
        productCard.hover();
        productCard.locator("a:has-text('Add to cart')").first().click();
        page.getByText("Continue Shopping").first().click();
        return this;
    }

    public CartPage openCart() {
        page.locator("a[href='/view_cart']").first().click();
        return new CartPage(page);
    }
}
