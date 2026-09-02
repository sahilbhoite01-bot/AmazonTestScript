package utils;

import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class ScreenshotUtil {

    private static final String SCREENSHOT_DIR = "screenshots";
    private static final DateTimeFormatter TIMESTAMP_FORMAT =
            DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    public static String takeScreenshot(WebDriver driver, String testName) {
        return takeScreenshot(driver, testName, "");
    }

    public static String takeScreenshot(WebDriver driver, String testName, String statusKeyword) {
        try {
            Path screenshotDir = Paths.get(SCREENSHOT_DIR);
            if (!Files.exists(screenshotDir)) {
                Files.createDirectories(screenshotDir);
            }

            String status = (statusKeyword == null || statusKeyword.isBlank())
                    ? ""
                    : statusKeyword + "_";
            String timestamp = LocalDateTime.now().format(TIMESTAMP_FORMAT);
            String fileName = testName + "_" + status + timestamp + ".png";

            File sourceFile = ((TakesScreenshot) driver).getScreenshotAs(OutputType.FILE);
            File destinationFile = screenshotDir.resolve(fileName).toFile();
            Files.copy(sourceFile.toPath(), destinationFile.toPath());

            return destinationFile.getAbsolutePath();
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
