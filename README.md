# pet-tests-java

Пет-проект на Java: API + UI автотесты для одного сайта - https://automationexercise.com.
Взял его специально, потому что там и нормальный UI-магазин, и открытый REST API без ключей и регистрации - удобно тренироваться на обоих сразу.

Стек:
- API - RestAssured + JUnit5 + Allure
- UI - Playwright for Java + JUnit5 + Allure, POM
- плюс пара E2E-тестов, где API и UI работают вместе

## Что нужно для запуска

JDK 17, Maven 3.8+. У меня всё крутится на Ubuntu, но код кроссплатформенный - API-тесты вообще без разницы, на чём гонять, а с UI на Windows/Mac обычно всё заводится сразу после `install chromium`. На Linux почти наверняка придётся доставить пару системных библиотек руками (см. ниже, сам через это прошёл).

## Запуск

```bash
# один раз, перед первым запуском UI-тестов
mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install chromium"

mvn test                    # всё сразу
mvn test -Dtest="tests.**"  # только API
mvn test -Dtest="ui.**"     # только UI
mvn test -Dtest="e2e.**"    # E2E (создали через API - залогинились через UI)

HEADED=true mvn test -Dtest="ui.**"   # с окном браузера, а не headless

mvn allure:serve
```

Никаких ключей и регистраций не нужно, сайт полностью открытый.

### Если нужно сменить URL

`base.url` лежит в `src/test/resources/config.properties`. Переопределить можно без правки файлов:
```bash
mvn test -Dbase.url=https://example.com
mvn test -Denv=staging   # подхватит config-staging.properties
```

### Если Linux ругается на зависимости браузера

Playwright'у нужны системные библиотеки, которых на чистом Ubuntu часто просто нет. Если видишь `Host system is missing dependencies to run browsers`:

```bash
sudo mvn exec:java -D exec.mainClass=com.microsoft.playwright.CLI -D exec.args="install-deps"
```

Если не помогло (у меня один раз apt не нашёл нужный пакет под конкретную версию Ubuntu) - ставь вручную, имена смотри в тексте ошибки:
```bash
sudo apt-get update
sudo apt-get install -y libicu74 libvpx9 libavif16
```

## Что где лежит
src/test/java/tests/                 - API
BaseTest.java                      - базовый спек, baseUri из ConfigReader
ProductsApiTest.java               - productsList / brandsList / searchProduct (параметризован по терминам поиска) + негативы на 405/400
AccountApiTest.java                - полный цикл аккаунта: создать, залогинить, проверить, обновить, удалить, убедиться, что удалился
(email с UUID, чтобы прогоны не конфликтовали друг с другом)
src/test/java/ui/                    - UI, Page Object Model
BaseUiTest.java                     - браузер, контекст на каждый тест, скриншот + Playwright-трейс в Allure при падении
(важно: закрытие контекста вынесено в TestWatcher, а не в @AfterEach,
иначе к моменту скриншота контекст уже закрыт и снимать нечего)
SignupLoginTest.java                - невалидный логин (параметризован), полный цикл регистрация, логин, удаление
CartTest.java                       - корзина
CheckoutFlowTest.java               - полный путь: регистрация, корзина, оформление, оплата дамми-картой, подтверждение
pages/ProductsPage.java              - тут пришлось повозиться: у каждого товара на странице ДВА элемента "Add to cart"
(один в оверлее на ховере, второй под фото), клик скоупится внутри карточки товара
pages/CheckoutPage.java, PaymentPage.java, и остальные - стандартные POM-страницы
src/test/java/e2e/
ApiToUiLoginTest.java               - создаём аккаунт через API, логинимся под теми же данными через UI
src/test/java/util/ConfigReader.java - читает base.url, поддерживает -Denv= и -Dbase.url=
src/test/resources/
config.properties, config-staging.properties
schemas/products-list-schema.json
.github/workflows/ci.yml - прогон в GitHub Actions с allure-отчётом в артефактах

## Про связь API и UI

У API и UI тестов свои независимые аккаунты (с уникальным UUID в почте), чтобы не мешать друг другу. Но есть отдельный `e2e/ApiToUiLoginTest`, который специально проверяет, что бэкенд и фронт не разъехались: создаёт юзера через API и логинится под ним через настоящий браузер.

## Заметки на будущее

- Ещё бы параметризованных негативов - кривые email-форматы и всякое такое, чисто для практики
- Один раз ловил `SSLHandshakeException` / `CircularRedirectException` при прогоне с домашнего компа - оказалось, это не баг в коде, а антибот-защита сайта душит старый Apache HttpClient (который под капотом у RestAssured) по TLS-отпечатку. Браузер и GitHub Actions с другого IP работают нормально. Если тесты внезапно перестали проходить локально, а в CI всё зелёное - вот в чём дело, а не в коде
- Можно ещё сохранять видео прогона (`context.setVideoDir()`), а не только трейс - пока не делал