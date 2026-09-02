package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CartPage {

    private static final String CART_URL = "https://www.amazon.in/gp/cart/view.html";
    private static final String ACTIVE_CART = "#sc-active-cart";
    private static final String EMPTY_CART = "#sc-empty-cart";
    private static final String CART_ITEM = ".sc-list-item";
    private static final String PRODUCT_TITLE = ".sc-product-title";
    private static final String QUANTITY_FIELD = "fieldset[name='sc-quantity']";
    private static final String QUANTITY_VALUE = QUANTITY_FIELD + " [data-a-selector='value']";
    private static final String QUANTITY_STEPPER = ".sc-quantity-stepper";
    private static final String INCREMENT_BUTTON = QUANTITY_FIELD + " button[name='increment'], "
            + QUANTITY_FIELD + " button[data-action='a-stepper-increment']";
    private static final String DECREMENT_BUTTON = QUANTITY_FIELD + " button[name='decrement'], "
            + QUANTITY_FIELD + " button[data-action='a-stepper-decrement']";
    private static final String DELETE_BUTTON = "input[value='Delete']";
    private static final String SUBTOTAL_BUYBOX = "#sc-subtotal-amount-buybox";
    private static final String PROCEED_TO_BUY = "#sc-buy-box-ptc-button";
    private static final String CART_COUNT = "#nav-cart-count";

    private final WebDriver driver;
    private final WebDriverWait wait;

    public CartPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void openCart() {
        driver.get(CART_URL);
        wait.until(d -> !d.findElements(By.cssSelector(ACTIVE_CART + ", " + EMPTY_CART)).isEmpty());
    }

    public boolean isCartDisplayed() {
        try {
            wait.until(d -> !d.findElements(By.cssSelector(ACTIVE_CART + ", " + EMPTY_CART)).isEmpty());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public int getCartItemCount() {
        return driver.findElements(By.cssSelector(CART_ITEM)).size();
    }

    public String getCartProductTitle() {
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

    public boolean isProductPresentInCart(String productTitle) {
        try {
            for (WebElement element : driver.findElements(By.cssSelector(PRODUCT_TITLE))) {
                String text = element.getText();
                if (text != null && titlesMatch(productTitle, text)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public int getQuantity() {
        try {
            for (WebElement element : driver.findElements(By.cssSelector(QUANTITY_VALUE))) {
                String text = element.getText();
                if (text != null && !text.isBlank()) {
                    int value = parsePositiveInt(text);
                    if (value > 0) {
                        return value;
                    }
                }
            }
            for (WebElement element : driver.findElements(By.cssSelector(QUANTITY_STEPPER))) {
                String text = element.getText();
                if (text != null && !text.isBlank()) {
                    int value = parsePositiveInt(text);
                    if (value > 0) {
                        return value;
                    }
                }
            }
        } catch (Exception ignored) {
        }
        return -1;
    }

    public boolean changeQuantity(int quantity) {
        if (quantity < 1) {
            return false;
        }
        int current = getQuantity();
        if (current < 1) {
            try {
                WebElement fieldset = driver.findElement(By.cssSelector(QUANTITY_FIELD));
                ((JavascriptExecutor) driver).executeScript(
                        "arguments[0].scrollIntoView({block:'center'});", fieldset);
                fieldset.click();
                current = getQuantity();
            } catch (Exception ignored) {
            }
            if (current < 1) {
                return false;
            }
        }
        if (current == quantity) {
            return true;
        }
        int steps = quantity - current;
        if (steps < 0 && current <= 1) {
            return false;
        }
        for (int i = 0; i < Math.abs(steps); i++) {
            if (!clickQuantityButton(steps > 0)) {
                return false;
            }
            if (!waitForQuantityChange(current)) {
                return false;
            }
            int updated = getQuantity();
            if (updated < 1) {
                return false;
            }
            current = updated;
        }
        return current == quantity;
    }

    public boolean removeProduct() {
        try {
            List<WebElement> deleteButtons = driver.findElements(By.cssSelector(DELETE_BUTTON));
            if (deleteButtons.isEmpty()) {
                return false;
            }
            WebElement delete = deleteButtons.get(0);
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", delete);
            String before = navCartCount();
            if (before != null && before.equals("0")) {
                return false;
            }
            delete.click();
            wait.until(d -> {
                String current = navCartCount();
                return current != null && (before == null || !current.equals(before));
            });
            return confirmRemovalAfterDelete();
        } catch (Exception e) {
            return false;
        }
    }

    private boolean confirmRemovalAfterDelete() {
        try {
            wait.until(d -> {
                d.get(CART_URL);
                return d.findElements(By.cssSelector(CART_ITEM)).isEmpty();
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public Double getSubtotal() {
        try {
            WebElement subtotal = driver.findElement(By.cssSelector(SUBTOTAL_BUYBOX));
            String text = subtotal.getText();
            if (text == null || text.isBlank()) {
                return null;
            }
            String digits = text.replace(",", "").replaceAll("[^0-9.]", "");
            if (digits.isEmpty()) {
                return null;
            }
            return Double.parseDouble(digits);
        } catch (Exception e) {
            return null;
        }
    }

    public boolean clickProceedToBuy() {
        try {
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(PROCEED_TO_BUY))).click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isCartEmpty() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(EMPTY_CART)));
            String text = driver.findElement(By.cssSelector(EMPTY_CART)).getText();
            return text != null && text.toLowerCase(Locale.ROOT).contains("empty");
        } catch (Exception e) {
            return false;
        }
    }

    public static boolean titlesMatch(String expected, String actual) {
        String normalizedExpected = normalizeTitle(expected);
        String normalizedActual = normalizeTitle(actual);
        if (normalizedExpected.isEmpty() || normalizedActual.isEmpty()) {
            return false;
        }
        if (normalizedExpected.equals(normalizedActual)) {
            return true;
        }
        if (normalizedExpected.contains(normalizedActual) || normalizedActual.contains(normalizedExpected)) {
            return true;
        }
        return commonWordOverlap(normalizedExpected, normalizedActual);
    }

    private static String normalizeTitle(String value) {
        if (value == null) {
            return "";
        }
        String normalized = value.toLowerCase(Locale.ROOT).replaceAll("\\s+", " ").trim();
        return normalized.replaceAll("[…]{1,}\\s*$", "").replaceAll("\\.{2,}\\s*$", "").trim();
    }

    private static boolean commonWordOverlap(String first, String second) {
        String[] firstWords = first.split(" ");
        String[] secondWords = second.split(" ");
        int matches = 0;
        for (String word : firstWords) {
            if (word.length() < 3) {
                continue;
            }
            for (String other : secondWords) {
                if (word.equals(other)) {
                    matches++;
                    break;
                }
            }
        }
        int min = Math.min(firstWords.length, secondWords.length);
        return min > 0 && matches >= Math.min(3, min);
    }

    private boolean clickQuantityButton(boolean increment) {
        try {
            WebElement button = wait.until(ExpectedConditions.elementToBeClickable(
                    By.cssSelector(increment ? INCREMENT_BUTTON : DECREMENT_BUTTON)));
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block:'center'});", button);
            button.click();
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private boolean waitForQuantityChange(int previous) {
        try {
            wait.until(d -> {
                int current = getQuantity();
                return current > 0 && current != previous;
            });
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private String navCartCount() {
        try {
            String count = driver.findElement(By.cssSelector(CART_COUNT)).getText().trim();
            return count.isEmpty() ? null : count;
        } catch (Exception e) {
            return null;
        }
    }

    private int parsePositiveInt(String text) {
        Matcher matcher = Pattern.compile("\\d+").matcher(text);
        if (!matcher.find()) {
            return -1;
        }
        try {
            return Integer.parseInt(matcher.group());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}