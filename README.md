# pet-tests-java

Java-пет-проект: API + UI тесты против одного сайта — https://automationexercise.com
(демо-магазин, специально сделан для практики автотестов; есть и полноценный UI, и открытый REST API без ключа).

- **API**: RestAssured + JUnit5 + Allure
- **UI**: Playwright for Java + JUnit5 + Allure, Page Object Model
- **E2E**: связка API + UI в одном тесте

## Требования

- JDK 17
- Maven 3.8+

Работает на Windows / macOS / Linux. API-тесты не зависят от ОС.
UI-тесты на Windows/macOS обычно заводятся сразу после `install chromium` — Playwright ставит браузер
со всеми зависимостями. На Linux (чистый Ubuntu/Debian) почти наверняка потребуется отдельно доставить
системные библиотеки — см. ниже.

## Запуск

```bash
# один раз перед первым запуском UI-тестов — ставит браузеры Playwright
mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"

mvn test                    # всё разом: api + ui + e2e
mvn test -Dtest="tests.**"  # только API
mvn test -Dtest="ui.**"     # только UI
mvn test -Dtest="e2e.**"    # только E2E (API create -> UI login)

HEADED=true mvn test -Dtest="ui.**"   # по умолчанию headless; HEADED=true — с окном браузера

mvn allure:serve
```

Ключ/регистрация не нужны — все эндпоинты и страницы сайта открыты.

### Конфигурация URL

`base.url` берётся из `src/test/resources/config.properties`. Переопределить без правки файлов:
```bash
mvn test -Dbase.url=https://example.com   # точечный оверрайд
mvn test -Denv=staging                    # подхватит config-staging.properties
```

### Системные зависимости (Linux)

Chromium от Playwright требует нескольких системных библиотек, которых может не быть на свежем Ubuntu/Debian.
Если при запуске UI-тестов видишь `Host system is missing dependencies to run browsers`:

```bash
sudo mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install-deps"
```

Если команда не помогает (например, apt не может разрешить нужный пакет), ставь вручную по имени из вывода ошибки:
```bash
sudo apt-get update
sudo apt-get install -y libicu74 libvpx9 libavif16   # версии пакетов зависят от версии Ubuntu
```

## Структура

```
src/test/java/tests/                 — API-тесты
  BaseTest.java                      — baseUri (из ConfigReader), allure-filter
  ProductsApiTest.java               — productsList / brandsList / searchProduct (параметризован по терминам) + 405/400 негативы
  AccountApiTest.java                — createAccount -> verifyLogin -> getUserDetailByEmail
                                        -> updateAccount -> deleteAccount -> verifyLogin(404)
                                        + параметризованный негатив (без email / без password)
                                        (аккаунт создаётся с уникальным email на каждый прогон)

src/test/java/ui/                    — UI-тесты (Page Object Model)
  BaseUiTest.java                     — запуск браузера, контекст на тест, скриншот + Playwright-трейс
                                         в Allure при падении (cleanup вынесен в TestWatcher,
                                         чтобы не закрывать контекст до снятия скриншота/трейса)
  SignupLoginTest.java                — параметризованный невалидный логин, полный цикл регистрация->логин->удаление
  CartTest.java                       — добавление 1/2 товаров в корзину
  CheckoutFlowTest.java               — регистрация -> корзина -> checkout -> оплата дамми-картой -> подтверждение
  pages/SignupLoginPage.java
  pages/AccountInfoPage.java
  pages/AccountPage.java
  pages/ProductsPage.java              — клик скоупится внутри карточки товара (.product-image-wrapper),
                                          т.к. у каждого товара 2 элемента "Add to cart" (оверлей + под фото)
  pages/CartPage.java
  pages/CheckoutPage.java              — блок адреса/обзора заказа, комментарий, кнопка Place Order
  pages/PaymentPage.java               — дамми-данные карты, подтверждение оплаты

src/test/java/e2e/                   — сквозные тесты, комбинирующие API и UI в одном сценарии
  ApiToUiLoginTest.java               — createAccount через API -> логин теми же данными через UI

src/test/java/util/
  ConfigReader.java                   — читает base.url из config.properties, поддержка -Denv= и -Dbase.url=

src/test/resources/schemas/
  products-list-schema.json          — json-schema валидация ответа productsList
src/test/resources/
  config.properties                  — base.url по умолчанию
  config-staging.properties          — пример альтернативного окружения

.github/workflows/ci.yml — GitHub Actions: установка браузеров, allure-report артефакт
```

## Связь API и UI

Помимо независимых наборов (у каждого свой аккаунт с UUID в email), есть выделенный E2E-тест
(`e2e/ApiToUiLoginTest`), который явно проверяет согласованность бэкенда и фронта: аккаунт создаётся
через API, а затем логин под теми же данными проверяется через реальный UI.

## Что дальше можно добавить
- больше параметризованных негативных кейсов (невалидные email-форматы, XSS/SQLi в текстовых полях — в рамках учебных целей)
- retry для нестабильных сетевых сбоев (изредка ловится `SSLHandshakeException` на стороне сайта)
- видео полного прогона (`context.setVideoDir()` при создании контекста), не только трейс
