package pages;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SearchResultsPage {

    private static final String RESULT_CARD = "div[data-component-type='s-search-result']";
    private static final String TITLE = "h2 span";
    private static final String PRICE_WHOLE = "span.a-price-whole";
    private static final String SPONSORED_MARKER = ".puis-sponsored-label-text";
    private static final String PRICE_RANGE_OPTION = "li[id^='p_36/dynamic-picker-']";
    private static final String PRICE_RANGE_ANCHOR = "a span";
    private static final String SORT_SELECT = "select#s-result-sort-select";
    private static final String PRICE_ASC_VALUE = "price-asc-rank";

    private final WebDriver driver;
    private final WebDriverWait wait;

    public SearchResultsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public boolean areResultsDisplayed() {
        try {
            wait.until(d -> !d.findElements(By.cssSelector(RESULT_CARD)).isEmpty());
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean isSearchResponseReady() {
        try {
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.s-main-slot")));
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasMeaningfulContent() {
        try {
            WebElement slot = wait.until(
                    ExpectedConditions.visibilityOfElementLocated(By.cssSelector("div.s-main-slot")));
            String text = slot.getText();
            return text != null && text.trim().length() > 200;
        } catch (Exception e) {
            return false;
        }
    }

    public boolean hasServerErrorState() {
        try {
            String text = driver.findElement(By.tagName("body")).getText().toLowerCase(Locale.ROOT);
            String[] markers = {
                    "something went wrong",
                    "server error",
                    "bad gateway",
                    "503",
                    "temporary error",
                    "please try again later"
            };
            for (String marker : markers) {
                if (text.contains(marker)) {
                    return true;
                }
            }
        } catch (Exception ignored) {
        }
        return false;
    }

    public boolean hasNoResultsState() {
        try {
            String text = driver.findElement(By.tagName("body")).getText();
            return Pattern.compile("(?i)no results for|no matches|did you mean|"
                    + "try checking your spelling|no products matched").matcher(text).find();
        } catch (Exception ignored) {
        }
        return false;
    }

    public List<String> getProductTitles() {
        List<String> titles = new ArrayList<>();
        for (WebElement card : getOrganicCards()) {
            List<WebElement> titleElements = card.findElements(By.cssSelector(TITLE));
            if (!titleElements.isEmpty()) {
                String text = titleElements.get(0).getText().trim();
                if (!text.isEmpty()) {
                    titles.add(text);
                }
            }
        }
        return titles;
    }

    public long countRelevantTitles(List<String> keywords) {
        return getProductTitles().stream()
                .filter(title -> keywords.stream()
                        .anyMatch(keyword -> title.toLowerCase(Locale.ROOT)
                                .contains(keyword.toLowerCase(Locale.ROOT))))
                .count();
    }

    public List<Double> getProductPrices() {
        List<Double> prices = new ArrayList<>();
        for (WebElement card : getOrganicCards()) {
            List<WebElement> priceElements = card.findElements(By.cssSelector(PRICE_WHOLE));
            if (priceElements.isEmpty()) {
                continue;
            }
            Double price = parseWholePrice(priceElements.get(0).getText());
            if (price != null) {
                prices.add(price);
            }
        }
        return prices;
    }

    public int getPriceRangeOptionCount() {
        return driver.findElements(By.cssSelector(PRICE_RANGE_OPTION)).size();
    }

    public int findBoundedRangeIndex() {
        List<WebElement> options = driver.findElements(By.cssSelector(PRICE_RANGE_OPTION));
        if (options.isEmpty()) {
            return -1;
        }
        for (int i = 0; i < options.size(); i++) {
            double[] bounds = parseRangeBounds(getPriceRangeLabel(i));
            if (bounds != null && bounds[0] > 0 && bounds[1] != Double.MAX_VALUE) {
                return i;
            }
        }
        return options.size() - 1;
    }

    public String getPriceRangeLabel(int index) {
        List<WebElement> options = driver.findElements(By.cssSelector(PRICE_RANGE_OPTION));
        if (options.isEmpty() || index >= options.size()) {
            return null;
        }
        try {
            return options.get(index).findElement(By.cssSelector(PRICE_RANGE_ANCHOR)).getText().trim();
        } catch (Exception e) {
            return null;
        }
    }

    public String applyPriceRange(int index) {
        List<WebElement> options = driver.findElements(By.cssSelector(PRICE_RANGE_OPTION));
        if (options.isEmpty() || index < 0 || index >= options.size()) {
            return null;
        }
        String label = getPriceRangeLabel(index);
        WebElement anchor = options.get(index).findElement(By.cssSelector("a"));
        WebElement marker = getResultMarker();
        anchor.click();
        waitForResultsReload(marker);
        return label;
    }

    public void sortByPriceLowToHigh() {
        WebElement sortSelect = wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(SORT_SELECT)));
        WebElement marker = getResultMarker();
        new Select(sortSelect).selectByValue(PRICE_ASC_VALUE);
        waitForResultsReload(marker);
    }

    public String getSelectedSortValue() {
        try {
            return new Select(driver.findElement(By.cssSelector(SORT_SELECT))).getFirstSelectedOption()
                    .getAttribute("value");
        } catch (Exception e) {
            return "";
        }
    }

    public boolean openFirstOrganicProduct() {
        try {
            WebElement titleSpan = null;
            for (WebElement card : driver.findElements(By.cssSelector(RESULT_CARD))) {
                if (!card.findElements(By.cssSelector(SPONSORED_MARKER)).isEmpty()) {
                    continue;
                }
                List<WebElement> spans = card.findElements(By.cssSelector(TITLE));
                if (!spans.isEmpty()) {
                    titleSpan = spans.get(0);
                    break;
                }
            }
            if (titleSpan == null) {
                return false;
            }

            WebElement anchor = titleSpan;
            while (anchor != null && !"a".equalsIgnoreCase(anchor.getTagName())) {
                anchor = anchor.findElement(By.xpath(".."));
            }
            if (anchor == null) {
                return false;
            }

            wait.until(ExpectedConditions.elementToBeClickable(anchor));
            String originalWindow = driver.getWindowHandle();
            ((JavascriptExecutor) driver)
                    .executeScript("arguments[0].scrollIntoView({block:'center'}); arguments[0].click();", anchor);

            try {
                wait.until(d -> d.getWindowHandles().size() > 1 || d.getCurrentUrl().contains("/dp/"));
            } catch (Exception ignored) {
            }

            if (driver.getWindowHandles().size() > 1) {
                for (String handle : driver.getWindowHandles()) {
                    if (!handle.equals(originalWindow)) {
                        driver.switchTo().window(handle);
                        break;
                    }
                }
            }

            try {
                wait.until(d -> d.getCurrentUrl().contains("/dp/")
                        && !d.findElements(By.cssSelector("#productTitle")).isEmpty());
            } catch (Exception ignored) {
            }
            return driver.getCurrentUrl().contains("/dp/");
        } catch (Exception e) {
            return false;
        }
    }

    public double[] parseRangeBounds(String label) {
        if (label == null || label.isBlank()) {
            return null;
        }
        Matcher matcher = Pattern.compile("\\d{1,3}(?:,\\d{3})*").matcher(label);
        List<String> numbers = new ArrayList<>();
        while (matcher.find()) {
            numbers.add(matcher.group().replace(",", ""));
        }
        if (numbers.isEmpty()) {
            return null;
        }
        double first = Double.parseDouble(numbers.get(0));
        String lower = label.toLowerCase(Locale.ROOT);
        if (lower.contains("up to")) {
            return new double[]{0, first};
        }
        if (lower.contains("over")) {
            return new double[]{first, Double.MAX_VALUE};
        }
        if (numbers.size() > 1) {
            double second = Double.parseDouble(numbers.get(1));
            return new double[]{Math.min(first, second), Math.max(first, second)};
        }
        return new double[]{first, first};
    }

    private List<WebElement> getOrganicCards() {
        List<WebElement> organic = new ArrayList<>();
        for (WebElement card : driver.findElements(By.cssSelector(RESULT_CARD))) {
            if (card.findElements(By.cssSelector(SPONSORED_MARKER)).isEmpty()) {
                organic.add(card);
            }
        }
        return organic;
    }

    private WebElement getResultMarker() {
        try {
            return driver.findElement(By.cssSelector(RESULT_CARD));
        } catch (Exception e) {
            return null;
        }
    }

    private void waitForResultsReload(WebElement marker) {
        if (marker != null) {
            try {
                wait.until(ExpectedConditions.stalenessOf(marker));
            } catch (Exception ignored) {
            }
        }
        try {
            wait.until(d -> !d.findElements(By.cssSelector(RESULT_CARD)).isEmpty());
        } catch (Exception ignored) {
        }
    }

    private Double parseWholePrice(String text) {
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
}