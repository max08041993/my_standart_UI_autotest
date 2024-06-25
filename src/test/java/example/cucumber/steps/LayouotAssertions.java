package example.cucumber.steps;

import com.codeborne.selenide.Selenide;
import com.github.javafaker.Faker;
import com.github.romankh3.image.comparison.ImageComparison;
import com.github.romankh3.image.comparison.ImageComparisonUtil;
import com.github.romankh3.image.comparison.model.ImageComparisonResult;
import com.github.romankh3.image.comparison.model.ImageComparisonState;
import com.github.romankh3.image.comparison.model.Rectangle;
import io.cucumber.datatable.DataTable;
import io.qameta.allure.Attachment;
import org.junit.jupiter.api.Assertions;
import org.openqa.selenium.OutputType;
import org.springframework.stereotype.Component;
import org.testng.Assert;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import static com.codeborne.selenide.Selenide.executeJavaScript;

@Component
public class LayouotAssertions {

    private void addImgToAllure(String name, File file) {
        try {
            byte[] image = Files.readAllBytes(file.toPath());
            CucumberHooks.threadLocalScenario.get().attach(image, "image/png", name);
        } catch (IOException e) {
            throw new RuntimeException("""
                    Не смог прочитать файл по указанному пути.
                    Проверьте размер Актуального скриншота, возможно он отличается от Ожидаемого.
                    При отсутствии видимых отличий, заменить в проекте Ожидаемый скриншот на Актуальный""");
        }
    }

    private String expectedScreensDir() {
        String expectedScreensDir = "src/test/resources/vivica/layout/screens/";
        Selenide.Wait()
                .until(webDriver -> Objects.equals(executeJavaScript("return document.readyState"), "complete"));
        Selenide.sleep(2000);
        return expectedScreensDir;
    }

    private void assertionsLoyaut(ImageComparisonResult result, ImageComparison imageComparison, File actualScreenshot, File expectedScreenshot, File resultDestination) {
        if (!result.getImageComparisonState()
                .equals(ImageComparisonState.MATCH)) {
            addImgToAllure("Текущий результат", actualScreenshot);
            addImgToAllure("Ожидаемый результат", expectedScreenshot);
            addImgToAllure("Различия", resultDestination);
        } else {
            try {
                addImgToAllure("Проверяемый объект", resultDestination);
            } catch (RuntimeException runtimeException){
                addImgToAllure("Ожидаемый результат", expectedScreenshot);
            }
        }
        Assertions.assertEquals(ImageComparisonState.MATCH, result.getImageComparisonState()
                , "Актуальный результат на сайте отличается от эталонного, см. скрин Различия \n"
                        + "Список координат несоответствия:\n"
                        + getMismatchCoordinates(imageComparison.createMask()));
    }

    private String getMismatchCoordinates(List<Rectangle> result) {
        StringBuilder coordinatesMessage = new StringBuilder();
        coordinatesMessage.append("| minX | minY | maxX | maxY |\n");
        for (Rectangle coordinate : result) {
            coordinatesMessage
                    .append("| ")
                    .append(coordinate.getMinPoint()
                            .getX())
                    .append(" | ")
                    .append(coordinate.getMinPoint()
                            .getY())
                    .append(" | ")
                    .append(coordinate.getMaxPoint()
                            .getX())
                    .append(" | ")
                    .append(coordinate.getMaxPoint()
                            .getY())
                    .append(" |\n");
        }
        return coordinatesMessage.toString().replaceAll(".0", "");
    }

    public void assertScreen(String expectedFileName) {
        String expectedScreensDir = expectedScreensDir();
        File actualScreenshot = Selenide.screenshot(OutputType.FILE);
        File expectedScreenshot = new File(expectedScreensDir + expectedFileName);
        if (!expectedScreenshot.exists()) {
            if (actualScreenshot != null) {
                addImgToAllure("actual", actualScreenshot);
            }
            throw new IllegalArgumentException("В папке " + expectedScreensDir
                    + " не найден ожидаемый скриншот. Актуальный скриншот можно получить из Allure отчета");
        }
        BufferedImage expectedImage = ImageComparisonUtil.readImageFromResources(expectedScreensDir + expectedFileName);
        BufferedImage actualImage = null;
        if (actualScreenshot != null) {
            actualImage = ImageComparisonUtil.readImageFromResources(actualScreenshot.toPath()
                    .toString());
        }
        File resultDestination = new File("target/diff_" + expectedFileName);
        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage, resultDestination)
                .setDifferenceRectangleFilling(true, 30) // Заливка областей с отличием и процент НЕ прозрачности
                .setMinimalRectangleSize(64); // Число минимального размера прямоугольника. Считайте как (ширина х высота).
        ImageComparisonResult result = imageComparison.compareImages();
        assertionsLoyaut(result, imageComparison, actualScreenshot, expectedScreenshot, resultDestination);
    }

    public void assertScreen(String expectedFileName, int minX, int minY, int maxX, int maxY) {
        Assert.assertTrue(minX < maxX, "Координаты исключения по Х не корректны,\n" +
                "значение левого верхнего угла по Х = " + minX + "\n" +
                "больше правого нижнего угла по Х = " + maxX);
        Assert.assertTrue(minY < maxY, "Координаты исключения по Y не корректны,\n" +
                "значение левого верхнего угла по Y = " + minY + "\n" +
                "больше правого нижнего угла по Y = " + maxY);
        String expectedScreensDir = expectedScreensDir();
        File actualScreenshot = Selenide.screenshot(OutputType.FILE);
        File expectedScreenshot = new File(expectedScreensDir + expectedFileName);
        if (!expectedScreenshot.exists()) {
            if (actualScreenshot != null) {
                addImgToAllure("actual", actualScreenshot);
            }
            throw new IllegalArgumentException("В папке " + expectedScreensDir
                    + " не найден ожидаемый скриншот. Актуальный скриншот можно получить из Allure отчета");
        }
        BufferedImage expectedImage = ImageComparisonUtil.readImageFromResources(expectedScreensDir + expectedFileName);
        assert actualScreenshot != null;
        BufferedImage actualImage = ImageComparisonUtil.readImageFromResources(actualScreenshot.toPath()
                .toString());
        File resultDestination = new File("target/diff_" + new Faker().internet()
                .uuid() + "_" + expectedFileName);
        List<Rectangle> excludedAreas = new ArrayList<>();
        excludedAreas.add(new Rectangle(minX, minY, maxX, maxY)); // x, y, width, height
        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage, resultDestination)
                .setExcludedAreas(excludedAreas)
                .setDrawExcludedRectangles(true) // Флаг, говорящий: рисовать исключенные прямоугольники или нет.
                .setExcludedRectangleFilling(true, 60) // Заливка не проверяемых областей и процент НЕ прозрачности
                .setDifferenceRectangleFilling(true, 30) // Заливка областей с отличием и процент НЕ прозрачности
                .setMinimalRectangleSize(64); // Число минимального размера прямоугольника. Считайте как (ширина х высота).;
        ImageComparisonResult result = imageComparison.compareImages();
        assertionsLoyaut(result, imageComparison, actualScreenshot, expectedScreenshot, resultDestination);
    }

    public void assertScreen(String expectedFileName, DataTable dt) {
        String expectedScreensDir = expectedScreensDir();
        File actualScreenshot = Selenide.screenshot(OutputType.FILE);
        File expectedScreenshot = new File(expectedScreensDir + expectedFileName);
        if (!expectedScreenshot.exists()) {
            addImgToAllure("actual", actualScreenshot);
            throw new IllegalArgumentException("В папке " + expectedScreensDir
                    + " не найден ожидаемый скриншот. Актуальный скриншот можно получить из Allure отчета");
        }
        BufferedImage expectedImage = ImageComparisonUtil.readImageFromResources(expectedScreensDir + expectedFileName);
        BufferedImage actualImage = ImageComparisonUtil.readImageFromResources(actualScreenshot.toPath()
                .toString());
        File resultDestination = new File("target/diff_" + new Faker().internet()
                .uuid() + "_" + expectedFileName);
        List<Map<String, Integer>> allData = dt.asMaps(String.class, Integer.class);
        List<Rectangle> excludedAreas = new ArrayList<>();
        allData.forEach(mapArea -> {
            Assert.assertTrue(mapArea.get("minX") < mapArea.get("maxX"), "Координаты исключения по Х не корректны,\n" +
                    "значение левого верхнего угла по Х minX = " + mapArea.get("minX") + "\n" +
                    "  больше правого нижнего угла по Х maxX = " + mapArea.get("maxX"));
            Assert.assertTrue(mapArea.get("minY") < mapArea.get("maxY"), "Координаты исключения по Y не корректны,\n" +
                    "значение левого верхнего угла по Y minY = " + mapArea.get("minY") + "\n" +
                    "  больше правого нижнего угла по Y maxY = " + mapArea.get("maxY"));
            excludedAreas.add(new Rectangle(mapArea.get("minX"), mapArea.get("minY"), mapArea.get("maxX"), mapArea.get("maxY")));
        });
        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage, resultDestination)
                .setExcludedAreas(excludedAreas) // Список прямоугольников, которые следует игнорировать при сравнении изображений.
                .setDrawExcludedRectangles(true) // Флаг, говорящий: рисовать исключенные прямоугольники или нет.
                .setExcludedRectangleFilling(true, 60) // Заливка не проверяемых областей и процент НЕ прозрачности
                .setDifferenceRectangleFilling(true, 30) // Заливка областей с отличием и процент НЕ прозрачности
                .setMinimalRectangleSize(64); // Число минимального размера прямоугольника. Считайте как (ширина х высота).
        ImageComparisonResult result = imageComparison.compareImages();
        assertionsLoyaut(result, imageComparison, actualScreenshot, expectedScreenshot, resultDestination);
    }

    public void checkOnlyTheArea(String expectedFileName, int minX, int minY, int maxX, int maxY) {
        String expectedScreensDir = expectedScreensDir();
        File actualScreenshot = Selenide.screenshot(OutputType.FILE);
        File expectedScreenshot = new File(expectedScreensDir + expectedFileName);
        if (!expectedScreenshot.exists()) {
            if (actualScreenshot != null) {
                addImgToAllure("actual", actualScreenshot);
            }
            throw new IllegalArgumentException("В папке " + expectedScreensDir
                    + " не найден ожидаемый скриншот. Актуальный скриншот можно получить из Allure отчета");
        }
        BufferedImage expectedImage = ImageComparisonUtil.readImageFromResources(expectedScreensDir + expectedFileName);
        assert actualScreenshot != null;
        BufferedImage actualImage = ImageComparisonUtil.readImageFromResources(actualScreenshot.toPath()
                .toString());
        File resultDestination = new File("target/diff_" + new Faker().internet()
                .uuid() + "_" + expectedFileName);

        List<Rectangle> excludedAreas = calculateExcludedAreas(minX, minY, maxX, maxY, expectedImage.getWidth(), expectedImage.getHeight());

        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage, resultDestination)
                .setExcludedAreas(excludedAreas) // Список прямоугольников, которые следует игнорировать при сравнении изображений.
                .setDrawExcludedRectangles(true) // Флаг, говорящий: рисовать исключенные прямоугольники или нет.
                .setExcludedRectangleFilling(true, 60) // Заливка не проверяемых областей и процент НЕ прозрачности
                .setDifferenceRectangleFilling(true, 30) // Заливка областей с отличием и процент НЕ прозрачности
                .setMinimalRectangleSize(64); // Число минимального размера прямоугольника. Считайте как (ширина х высота).
        ImageComparisonResult result = imageComparison.compareImages();
        assertionsLoyaut(result, imageComparison, actualScreenshot, expectedScreenshot, resultDestination);
    }

    private List<Rectangle> calculateExcludedAreas(int minX, int minY, int maxX, int maxY, int imageWidth, int imageHeight) {
        List<Rectangle> excludedAreas = new ArrayList<>();
        excludedAreas.add(new Rectangle(0, 0, minX, imageHeight));
        excludedAreas.add(new Rectangle(minX, 0, maxX, minY));
        excludedAreas.add(new Rectangle(minX, maxY, maxX, imageHeight));
        excludedAreas.add(new Rectangle(maxX, 0, imageWidth, imageHeight));
        return excludedAreas;
    }

    public void checkOnlyTheArea(String expectedFileName, DataTable dt) {
        String expectedScreensDir = expectedScreensDir();
        File actualScreenshot = Selenide.screenshot(OutputType.FILE);
        File expectedScreenshot = new File(expectedScreensDir + expectedFileName);
        if (!expectedScreenshot.exists()) {
            if (actualScreenshot != null) {
                addImgToAllure("actual", actualScreenshot);
            }
            throw new IllegalArgumentException("В папке " + expectedScreensDir
                    + " не найден ожидаемый скриншот. Актуальный скриншот можно получить из Allure отчета");
        }
        BufferedImage expectedImage = ImageComparisonUtil.readImageFromResources(expectedScreensDir + expectedFileName);
        assert actualScreenshot != null;
        BufferedImage actualImage = ImageComparisonUtil.readImageFromResources(actualScreenshot.toPath()
                .toString());
        File resultDestination = new File("target/diff_" + new Faker().internet()
                .uuid() + "_" + expectedFileName);

        List<Rectangle> excludedAreas = calculateExcludedAreas(dt, expectedImage.getWidth(), expectedImage.getHeight());
        ImageComparison imageComparison = new ImageComparison(expectedImage, actualImage, resultDestination)
                .setExcludedAreas(excludedAreas)
                .setDrawExcludedRectangles(true)
                .setExcludedRectangleFilling(true, 60)
                .setDifferenceRectangleFilling(true, 30)
                .setMinimalRectangleSize(64);
        ImageComparisonResult result = imageComparison.compareImages();
        assertionsLoyaut(result, imageComparison, actualScreenshot, expectedScreenshot, resultDestination);
    }

    private List<Rectangle> calculateExcludedAreas(DataTable coordinatesDataTable, int imageWidth, int imageHeight) {
        List<Rectangle> excludedAreas = new ArrayList<>();
        List<Map<String, Integer>> coordinates = coordinatesDataTable.asMaps(String.class, Integer.class);
        for (Map<String, Integer> row : coordinates) {
            int minX = row.get("minX");
            int minY = row.get("minY");
            int maxX = row.get("maxX");
            int maxY = row.get("maxY");
            // Учитываем размеры изображения при вычислении координат прямоугольников
            minX = Math.max(0, Math.min(minX, imageWidth));
            minY = Math.max(0, Math.min(minY, imageHeight));
            maxX = Math.max(0, Math.min(maxX, imageWidth));
            maxY = Math.max(0, Math.min(maxY, imageHeight));
            // Добавляем прямоугольную область, которую нужно исключить из текущего изображения
            excludedAreas.add(new Rectangle(minX, minY, maxX, maxY));
        }
        // Находим прямоугольные области, которые не пересекаются с областями исключения
        List<Rectangle> includedAreas = new ArrayList<>();
        includedAreas.add(new Rectangle(0, 0, imageWidth, imageHeight)); // Добавляем включающую всё изображение область
        for (Rectangle excludedArea : excludedAreas) {
            List<Rectangle> updatedIncludedAreas = new ArrayList<>();
            for (Rectangle includedArea : includedAreas) {
                if (!includedArea.isOverlapping(excludedArea)) {
                    // Если области не пересекаются, добавляем включающую область в список
                    updatedIncludedAreas.add(includedArea);
                } else {
                    // Если области пересекаются, разбиваем включающую область на части и добавляем их в список
                    Rectangle intersection = new Rectangle(
                            Math.max(includedArea.getMinPoint().x, excludedArea.getMinPoint().x),
                            Math.max(includedArea.getMinPoint().y, excludedArea.getMinPoint().y),
                            Math.min(includedArea.getMaxPoint().x, excludedArea.getMaxPoint().x),
                            Math.min(includedArea.getMaxPoint().y, excludedArea.getMaxPoint().y)
                    );
                    // Добавляем прямоугольники до и после пересечения
                    if (includedArea.getMinPoint().x < intersection.getMinPoint().x) {
                        updatedIncludedAreas.add(new Rectangle(
                                includedArea.getMinPoint().x,
                                includedArea.getMinPoint().y,
                                intersection.getMinPoint().x,
                                includedArea.getMaxPoint().y
                        ));
                    }
                    if (includedArea.getMaxPoint().x > intersection.getMaxPoint().x) {
                        updatedIncludedAreas.add(new Rectangle(
                                intersection.getMaxPoint().x,
                                includedArea.getMinPoint().y,
                                includedArea.getMaxPoint().x,
                                includedArea.getMaxPoint().y
                        ));
                    }
                    if (includedArea.getMinPoint().y < intersection.getMinPoint().y) {
                        updatedIncludedAreas.add(new Rectangle(
                                intersection.getMinPoint().x,
                                includedArea.getMinPoint().y,
                                intersection.getMaxPoint().x,
                                intersection.getMinPoint().y
                        ));
                    }
                    if (includedArea.getMaxPoint().y > intersection.getMaxPoint().y) {
                        updatedIncludedAreas.add(new Rectangle(
                                intersection.getMinPoint().x,
                                intersection.getMaxPoint().y,
                                intersection.getMaxPoint().x,
                                includedArea.getMaxPoint().y
                        ));
                    }
                }
            }
            includedAreas = updatedIncludedAreas;
        }
        return includedAreas;
    }

}
