package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;

public class LoginPage {

    private static final String SIGNIN_FORM = "#ap_signin_form";
    private static final String EMAIL_FIELD = "#ap_email";
    private static final String EMAIL_FIELD_ALT = "input[name='email']";
    private static final String SIGNIN_TITLE = "#ap_signin_title";
    private static final String SIGNIN_SUBMIT = "#signInSubmit";

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public boolean isLoginPageDisplayed() {
        try {
            wait.until(d -> !d.findElements(By.cssSelector(SIGNIN_FORM + ", " + EMAIL_FIELD + ", "
                    + EMAIL_FIELD_ALT)).isEmpty());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSignInHeadingDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(SIGNIN_TITLE)));
            return true;
        } catch (Exception e) {
            for (WebElement heading : driver.findElements(By.cssSelector("h1"))) {
                String text = heading.getText();
                if (text != null) {
                    String lower = text.toLowerCase(Locale.ROOT);
                    if (lower.contains("sign in") || lower.contains("login")) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    public boolean isEmailOrMobileFieldDisplayed() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(EMAIL_FIELD + ", " + EMAIL_FIELD_ALT)));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSignInSubmitDisplayed() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(SIGNIN_SUBMIT)));
            return true;
        } catch (Exception e) {
            List<WebElement> submits = driver.findElements(By.cssSelector(SIGNIN_SUBMIT));
            return !submits.isEmpty();
        }
    }
}