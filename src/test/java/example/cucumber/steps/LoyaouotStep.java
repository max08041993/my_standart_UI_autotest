package example.cucumber.steps;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.When;
import org.springframework.beans.factory.annotation.Autowired;

public class LoyaouotStep {

    @Autowired
    LayouotAssertions layouotAssertions;

    @When("^Проверяю верстку страницы (.*) полностью$")
    public void checkWebsiteLayout(String name){
        layouotAssertions.assertScreen(name);
    }

    @When("^Проверяю верстку страницы (.*) с исключением из проверки блока minX = (.*), minY = (.*), maxX = (.*), maxY = (.*)$")
    public void checkWebsiteLayoutInt(String name, int minX, int minY, int maxX, int maxY){
        layouotAssertions.assertScreen(name, minX, minY, maxX, maxY);
    }

    @When("^Проверяю верстку страницы (.*) с исключением из проверки блоков$")
    public void checkWebsiteLayoutDt(String name, DataTable dt){
        layouotAssertions.assertScreen(name, dt);
    }

    @When("^Проверяю верстку страницы (.*) только этого блока minX = (.*), minY = (.*), maxX = (.*), maxY = (.*)$")
    public void checkIsJumpRejected(String name, int minX, int minY, int maxX, int maxY){
        layouotAssertions.checkOnlyTheArea(name, minX, minY, maxX, maxY);
    }

    @When("^Проверяю верстку страницы (.*) только этих блоков$")
    public void checkIsJumpRejected(String name, DataTable dt){
        layouotAssertions.checkOnlyTheArea(name, dt);
    }

}
