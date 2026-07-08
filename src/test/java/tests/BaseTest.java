package tests;

import io.qameta.allure.restassured.AllureRestAssured;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.parsing.Parser;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeAll;
import util.ConfigReader;

public class BaseTest {

    protected static final String BASE_URL = ConfigReader.baseUrl();

    @BeforeAll
    static void setup() {
        // сайт отдаёт JSON с Content-Type: text/html — без этого RestAssured
        // не распознаёт тело как JSON и body("path", matcher) не работает
        RestAssured.registerParser("text/html", Parser.JSON);

        RequestSpecification spec = new RequestSpecBuilder()
                .setBaseUri(BASE_URL)
                // без этого сайт видит дефолтный User-Agent Apache-HttpClient и заворачивает
                // запросы в circular redirect / TLS handshake failure, принимая клиент за бота
                .addHeader("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
                        + "AppleWebKit/537.36 (KHTML, like Gecko) Chrome/126.0.0.0 Safari/537.36")
                .addFilter(new AllureRestAssured())
                .build();
        RestAssured.requestSpecification = spec;
    }
}