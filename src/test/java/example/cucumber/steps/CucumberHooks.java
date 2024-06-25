package example.cucumber.steps;

import com.codeborne.selenide.WebDriverRunner;
import example.utils.ScenarioContext;
import example.utils.WebDriverFactory;
import io.cucumber.java.*;
import org.apache.logging.log4j.LogManager;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;

public class CucumberHooks {

    public static ThreadLocal<Scenario> threadLocalScenario = ThreadLocal.withInitial(() -> null);

    @SuppressWarnings("unused")
    public static final org.apache.logging.log4j.Logger logger = LogManager.getLogger();

    @BeforeAll
    public static void CucumberSetUpSuite() {
        logger.info("Cucumber Suite SetUp");
    }

    @Before
    public void setUser(Scenario scenario) {
        WebDriverFactory.getWebDriverInstance();
        threadLocalScenario.set(scenario);
    }

    @After
    public void logoutAndCLeanData() {
        if (threadLocalScenario.get().getStatus()
                .toString()
                .equals("FAILED")) {
            logger.error("Scenario: " + threadLocalScenario.get().getName() + "          Status: " + threadLocalScenario.get().getStatus());
        } else {
            logger.info("Scenario: " + threadLocalScenario.get().getName() + "          Status: " + threadLocalScenario.get().getStatus());
        }
        ScenarioContext.clearContext();
        if (threadLocalScenario.get().isFailed()) addScreenshotToAllure();
        WebDriverFactory.closeWebDriverInstance();
    }

    @AfterAll
    public static void CucumberTearDownSuite() {
        logger.info("Cucumber Suite Tear Down");
    }

    public static void addScreenshotToAllure() {
        TakesScreenshot ts = (TakesScreenshot) WebDriverRunner.getWebDriver();
        byte[] screenshot = ts.getScreenshotAs(OutputType.BYTES);
        threadLocalScenario.get().attach(screenshot, "image/png", "Скриншот");
    }

}
