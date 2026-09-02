package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ProductPage {

    private static final String PRODUCT_TITLE = "#productTitle";
    private static final String PRIMARY_IMAGE = "#landingImage";
    private static final String IMAGE_WRAPPER = "#imgTagWrapperId img";
    private static final String RATING_POPOVER = "#acrPopover";
    private static final String STAR_ALT = "i.a-icon-star .a-icon-alt";
    private static final String REVIEW_TEXT = "#acrCustomerReviewText";
    private static final String ADD_TO_CART_BUTTON = "#add-to-cart-button";
    private static final String CART_COUNT = "#nav-cart-count";

    private final WebDriver driver;
    private final WebDriverWait wait;

    public ProductPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public String getProductTitle() {
        try {
            List<WebElement> elements = driver.findElements(By.cssSelector(PRODUCT_TITLE));
            if (elements.isEmpty()) {
                return "";
            }
            String text = elements.get(0).getText();
            return text == null ? "" : text.trim();
        } catch (Exception e) {
            return "";
        }
    }

    public boolean isProductTitleDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(By.cssSelector(PRODUCT_TITLE)));
            List<WebElement> elements = driver.findElements(By.cssSelector(PRODUCT_TITLE));
            if (elements.isEmpty()) {
                return false;
            }
            String text = elements.get(0).getText();
            return text != null && !text.isBlank();
        } catch (Exception e) {
            return false;
        }
    }

    public Double getProductPrice() {
        String text = readCorePriceText();
        if (text == null) {
            return null;
        }
        String digits = text.replaceAll("[^0-9]", "");
        if (digits.isEmpty()) {
            return null;
        }
        try {
            return Double.parseDouble(digits);
        } catch (NumberFormatException e) {
            return null;
        }
    }

    public boolean isProductPriceDisplayed() {
        return readCorePriceText() != null;
    }

    public boolean isProductImageDisplayed() {
        try {
            wait.until(ExpectedConditions.presenceOfElementLocated(
                    By.cssSelector(PRIMARY_IMAGE + ", " + IMAGE_WRAPPER)));
            List<WebElement> primary = driver.findElements(By.cssSelector(PRIMARY_IMAGE));
            if (!primary.isEmpty()) {
                return isImageRendered(primary.get(0));
            }
            List<WebElement> wrapper = driver.findElements(By.cssSelector(IMAGE_WRAPPER));
            return !wrapper.isEmpty() && isImageRendered(wrapper.get(0));
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isRatingDisplayed() {
        return readRatingValue() != null;
    }

    public boolean isReviewCountDisplayed() {
        String review = readReviewCountValue();
        if (review == null) {
            return false;
        }
        return !review.replaceAll("[^0-9]", "").isEmpty();
    }

    public boolean addToCart() {
        try {
            WebElement button = wait.until(
                    ExpectedConditions.elementToBeClickable(By.cssSelector(ADD_TO_CART_BUTTON)));
            String before = cartCount();
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", button);
            button.click();
            try {
                wait.until(d -> {
                    String current = cartCount();
                    return current != null && !current.equals(before);
                });
            } catch (Exception ignored) {
            }
            String after = cartCount();
            return before == null || !after.equals(before);
        } catch (Exception e) {
            return false;
        }
    }

    private String cartCount() {
        try {
            String count = driver.findElement(By.cssSelector(CART_COUNT)).getText().trim();
            return count.isEmpty() ? null : count;
        } catch (Exception e) {
            return null;
        }
    }

    private String readCorePriceText() {
        String[] containers = {
                "#corePriceDisplay_desktop_feature_div",
                "#corePrice_feature_div",
                "#apex_desktop",
                "#buybox"
        };
        for (String container : containers) {
            List<WebElement> wholePrices =
                    driver.findElements(By.cssSelector(container + " span.a-price-whole"));
            for (WebElement element : wholePrices) {
                String text = element.getText();
                if (text != null && !text.trim().isEmpty()) {
                    return text.trim();
                }
            }
        }
        return null;
    }

    private boolean isImageRendered(WebElement image) {
        String src = image.getAttribute("src");
        return image.isDisplayed() && src != null && !src.isBlank();
    }

    private String readRatingValue() {
        try {
            for (WebElement popover : driver.findElements(By.cssSelector(RATING_POPOVER))) {
                String aria = popover.getAttribute("aria-label");
                if (aria != null && aria.contains("out of 5")) {
                    return aria.trim();
                }
            }
            for (WebElement star : driver.findElements(By.cssSelector(STAR_ALT))) {
                Object text = ((JavascriptExecutor) driver)
                        .executeScript("return arguments[0].textContent;", star);
                if (text != null && text.toString().contains("out of 5")) {
                    return text.toString().trim();
                }
            }
            Matcher matcher = Pattern.compile("(\\d(?:\\.\\d)?)\\s*out of\\s*5")
                    .matcher(driver.getPageSource());
            if (matcher.find()) {
                return matcher.group(0);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    private String readReviewCountValue() {
        try {
            for (WebElement element : driver.findElements(By.cssSelector(REVIEW_TEXT))) {
                String text = element.getText();
                if (text != null && !text.trim().isEmpty()) {
                    return text.trim();
                }
            }
            Matcher matcher = Pattern.compile("(\\d[\\d,]*)\\s*ratings?")
                    .matcher(driver.getPageSource());
            if (matcher.find()) {
                return matcher.group(0);
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }
}