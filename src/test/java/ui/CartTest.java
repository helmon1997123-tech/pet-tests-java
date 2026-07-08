package ui;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import ui.pages.CartPage;
import ui.pages.ProductsPage;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Automation Exercise UI")
@Feature("Cart")
public class CartTest extends BaseUiTest {

    @Test
    @Description("Добавление одного товара -> в корзине 1 позиция")
    void addSingleProductToCart() {
        ProductsPage products = new ProductsPage(page);
        CartPage cart = products.addProductToCartByIndex(0).openCart();

        assertTrue(cart.isLoaded());
        assertEquals(1, cart.itemsCount());
    }

    @Test
    @Description("Добавление двух разных товаров -> в корзине 2 позиции")
    void addTwoProductsToCart() {
        ProductsPage products = new ProductsPage(page);
        CartPage cart = products
                .addProductToCartByIndex(0)
                .addProductToCartByIndex(1)
                .openCart();

        assertEquals(2, cart.itemsCount());
    }
}
