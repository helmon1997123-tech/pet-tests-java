package tests;

import io.qameta.allure.Description;
import io.qameta.allure.Epic;
import io.qameta.allure.Feature;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static io.restassured.RestAssured.given;
import static io.restassured.module.jsv.JsonSchemaValidator.matchesJsonSchemaInClasspath;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.greaterThan;

@Epic("Automation Exercise API")
@Feature("Products")
public class ProductsApiTest extends BaseTest {

    @Test
    @Description("GET /api/productsList -> 200, список товаров соответствует схеме")
    void getAllProductsList() {
        given()
        .when()
            .get("/api/productsList")
        .then()
            .statusCode(200)
            .body(matchesJsonSchemaInClasspath("schemas/products-list-schema.json"))
            .body("responseCode", equalTo(200))
            .body("products.size()", greaterThan(0));
    }

    @Test
    @Description("POST /api/productsList -> метод не поддерживается, responseCode 405")
    void postToProductsListNotSupported() {
        given()
        .when()
            .post("/api/productsList")
        .then()
            .statusCode(200) // сайт всегда отвечает 200 на транспортном уровне
            .body("responseCode", equalTo(405));
    }

    @Test
    @Description("GET /api/brandsList -> 200, список брендов не пуст")
    void getAllBrandsList() {
        given()
        .when()
            .get("/api/brandsList")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(200))
            .body("brands.size()", greaterThan(0));
    }

    @Test
    @Description("PUT /api/brandsList -> метод не поддерживается, responseCode 405")
    void putToBrandsListNotSupported() {
        given()
        .when()
            .put("/api/brandsList")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(405));
    }

    @ParameterizedTest(name = "searchProduct с термином \"{0}\"")
    @ValueSource(strings = {"top", "dress", "jean"})
    @Description("POST /api/searchProduct с разными поисковыми запросами -> 200, товары найдены")
    void searchProductWithVariousTerms(String term) {
        given()
            .formParam("search_product", term)
        .when()
            .post("/api/searchProduct")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(200))
            .body("products.size()", greaterThan(0));
    }

    @Test
    @Description("POST /api/searchProduct без параметра -> responseCode 400")
    void searchProductWithoutParam() {
        given()
        .when()
            .post("/api/searchProduct")
        .then()
            .statusCode(200)
            .body("responseCode", equalTo(400));
    }
}
