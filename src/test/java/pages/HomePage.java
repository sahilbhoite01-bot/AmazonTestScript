package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class HomePage {

    private static final String SEARCH_BOX = "twotabsearchtextbox";
    private static final String SEARCH_BUTTON = "nav-search-submit-button";

    private final WebDriver driver;
    private final WebDriverWait wait;

    public HomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void searchProduct(String product) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id(SEARCH_BOX))).clear();
        driver.findElement(By.id(SEARCH_BOX)).sendKeys(product);
        wait.until(ExpectedConditions.elementToBeClickable(By.id(SEARCH_BUTTON))).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.s-main-slot")));
    }
}