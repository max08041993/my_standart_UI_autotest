package example.cucumber.steps;

import example.cucumber.pages.ExamplePage;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;

import static com.codeborne.selenide.Selenide.open;
import static example.cucumber.steps.CucumberHooks.addScreenshotToAllure;
import static org.assertj.core.api.Assertions.assertThat;

public class LoginSteps extends ExamplePage {


    @When("^Открываю страницу (.*)$")
    public void openPage(String url) {
        open(url);
    }

    @When("^Нажимаю кнопку поиска$")
    public void sendButton() {
        pressButtonPage();
    }

    @When("^Проверяю что отображается поле (.*)$")
    public void check(String link) {
        assertThat(linkIsVisible(link))
                .as("Не отображается поле " + link)
                .isTrue();
    }

    @When("^Ввожу в поле ввода (.*)$")
    public void sendText(String text) {
        send(text);
    }

    @Then("^Проверяю что найдено более (\\d+) результатов$")
    public void checkResultCont(long count) {
        try {
            assertThat(resultCount())
                    .isGreaterThan(count);
        } catch (AssertionError e){
            addScreenshotToAllure();
            throw e;
        }
    }
}
