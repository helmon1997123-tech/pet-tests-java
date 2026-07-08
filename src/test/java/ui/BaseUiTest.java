package ui;

import com.microsoft.playwright.Browser;
import com.microsoft.playwright.BrowserContext;
import com.microsoft.playwright.BrowserType;
import com.microsoft.playwright.Page;
import com.microsoft.playwright.Playwright;
import com.microsoft.playwright.Tracing;
import io.qameta.allure.Attachment;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import util.ConfigReader;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

@ExtendWith(BaseUiTest.PerTestCleanup.class)
public class BaseUiTest {

    protected static final String BASE_URL = ConfigReader.baseUrl();
    private static final boolean HEADLESS = !"false".equals(System.getenv("HEADED"));

    protected static Playwright playwright;
    protected static Browser browser;
    protected BrowserContext context;
    protected Page page;

    @BeforeAll
    static void launchBrowser() {
        playwright = Playwright.create();
        browser = playwright.chromium().launch(
                new BrowserType.LaunchOptions().setHeadless(HEADLESS));
    }

    @AfterAll
    static void closeBrowser() {
        browser.close();
        playwright.close();
    }

    @BeforeEach
    void newContext() {
        context = browser.newContext();
        context.tracing().start(new Tracing.StartOptions()
                .setScreenshots(true)
                .setSnapshots(true)
                .setSources(true));
        page = context.newPage();
        page.navigate(BASE_URL);
    }

    // Закрытие контекста намеренно вынесено из @AfterEach в TestWatcher (см. PerTestCleanup ниже):
    // @AfterEach выполняется ДО вызова testFailed(), поэтому скриншот/трейс с уже закрытого
    // контекста снять нельзя — это была скрытая причина части "TargetClosedError" при падениях.

    @Attachment(value = "screenshot", type = "image/png")
    private byte[] screenshot() {
        return page.screenshot(new Page.ScreenshotOptions());
    }

    @Attachment(value = "trace", type = "application/zip")
    private byte[] attachTrace(Path tracePath) throws IOException {
        return Files.readAllBytes(tracePath);
    }

    /**
     * Закрывает контекст браузера после каждого теста.
     * При падении — сначала сохраняет скриншот и Playwright-трейс (screenshots+snapshots+sources)
     * в Allure; трейс можно открыть через `npx playwright show-trace trace.zip` или на trace.playwright.dev.
     */
    static class PerTestCleanup implements TestWatcher {

        @Override
        public void testFailed(ExtensionContext extensionContext, Throwable cause) {
            asBaseUiTest(extensionContext).ifPresent(base -> {
                if (base.page != null) {
                    base.screenshot();
                }
                stopTracingWithSave(base);
            });
            closeContext(extensionContext);
        }

        @Override
        public void testSuccessful(ExtensionContext extensionContext) {
            asBaseUiTest(extensionContext).ifPresent(PerTestCleanup::stopTracingWithoutSave);
            closeContext(extensionContext);
        }

        @Override
        public void testAborted(ExtensionContext extensionContext, Throwable cause) {
            asBaseUiTest(extensionContext).ifPresent(PerTestCleanup::stopTracingWithoutSave);
            closeContext(extensionContext);
        }

        @Override
        public void testDisabled(ExtensionContext extensionContext, Optional<String> reason) {
            // контекст для отключённого теста не создавался — закрывать нечего
        }

        private void closeContext(ExtensionContext extensionContext) {
            asBaseUiTest(extensionContext).ifPresent(base -> {
                if (base.context != null) {
                    base.context.close();
                }
            });
        }

        private static void stopTracingWithSave(BaseUiTest base) {
            try {
                Path tracePath = Files.createTempFile("trace", ".zip");
                base.context.tracing().stop(new Tracing.StopOptions().setPath(tracePath));
                base.attachTrace(tracePath);
            } catch (IOException e) {
                // проблема с сохранением трейса не должна портить основной отчёт по тесту
            }
        }

        private static void stopTracingWithoutSave(BaseUiTest base) {
            base.context.tracing().stop();
        }

        private Optional<BaseUiTest> asBaseUiTest(ExtensionContext extensionContext) {
            Object instance = extensionContext.getRequiredTestInstance();
            return instance instanceof BaseUiTest base ? Optional.of(base) : Optional.empty();
        }
    }
}
