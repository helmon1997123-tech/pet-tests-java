package ui;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import ui.pages.AccountPage;
import ui.pages.CartPage;
import ui.pages.CheckoutPage;
import ui.pages.PaymentPage;
import ui.pages.ProductsPage;
import ui.pages.SignupLoginPage;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Epic("Automation Exercise UI")
@Feature("Checkout")
public class CheckoutFlowTest extends BaseUiTest {

    @Test
    @Description("Регистрация -> товар в корзину -> оформление заказа -> оплата дамми-картой -> подтверждение -> удаление аккаунта")
    void placeOrderSuccessfully() {
        String name = "Anatoliy QA";
        String email = "qa.pet.project.checkout." + UUID.randomUUID() + "@example.com";
        String password = "P@ssw0rd123";

        SignupLoginPage loginPage = new SignupLoginPage(page);
        AccountPage account = loginPage.signup(name, email).fillAndSubmit(password);
        account.continueToAccount();

        ProductsPage products = new ProductsPage(page);
        CartPage cart = products.addProductToCartByIndex(0).openCart();

        CheckoutPage checkout = cart.proceedToCheckout();
        assertTrue(checkout.isAddressReviewVisible(), "Должны быть видны детали адреса и обзор заказа");

        PaymentPage payment = checkout
                .enterOrderComment("Pet-project test order, please ignore")
                .placeOrder();

        assertTrue(payment.fillDummyCard().confirmOrder(),
                "Должно появиться сообщение об успешном оформлении заказа");

        account.deleteAccount();
        assertTrue(account.isAccountDeletedVisible(), "Должно появиться 'ACCOUNT DELETED!'");
    }
}
