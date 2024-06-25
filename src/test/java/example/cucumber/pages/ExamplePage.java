package example.cucumber.pages;

import com.codeborne.selenide.Condition;
import com.codeborne.selenide.ElementsCollection;
import com.codeborne.selenide.SelenideElement;
import org.springframework.util.Assert;

import java.util.List;
import java.util.Objects;

import static com.codeborne.selenide.Selenide.$$x;
import static com.codeborne.selenide.Selenide.$x;

public class ExamplePage {

    public final ElementsCollection listLink = $$x("//input");

    private final SelenideElement textarea = $x("//textarea");

    private final SelenideElement searchButton = $x("//div[@jsname]//input[@value='Поиск в Google']");

    private final SelenideElement resultStats = $x("//div[@id='result-stats']");

    public void pressButtonPage() {
        searchButton.click();
    }

    public Boolean linkIsVisible(String link) {
        SelenideElement currentButton = listLink.asFixedIterable().stream()
                .filter(b -> Objects.equals(b.getAttribute("aria-label"), link))
                .findFirst()
                .orElse(null);
        Assert.notNull(currentButton, "Не найдена ссылка " + link);
        return currentButton.is(Condition.visible);
    }

    public void send(String text) {
        textarea.sendKeys(text);
    }

    public long resultCount() {
        return Integer.parseInt(resultStats.should(Condition.visible.because("Не найдены результаты"))
                .text()
                .split("\\(")[0]
                .replaceAll("\\W+", ""));
    }

}
