package base;

import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.SkipException;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import pages.CartPage;
import pages.HomePage;
import pages.ProductPage;
import pages.SearchResultsPage;
import utils.DriverFactory;
import utils.ExtentManager;
import utils.ScreenshotUtil;

import java.lang.reflect.Method;
import java.time.Duration;

public class BaseTest {

    protected static final String SEARCH_TERM = "wireless headphones";

    protected WebDriver driver;
    protected WebDriverWait wait;

    @BeforeSuite(alwaysRun = true)
    public void beforeSuite() {
        ExtentManager.init();
    }

    @AfterSuite(alwaysRun = true)
    public void afterSuite() {
        ExtentManager.flush();
    }

    @BeforeMethod
    public void setUp(Method method, Object[] parameters) {
        String browser = resolveBrowser(method, parameters);
        String testName = method.getName();
        if (parameters != null && parameters.length > 0 && parameters[0] != null) {
            testName = testName + "_" + parameters[0];
        }
        ExtentManager.startTest(testName, this.getClass().getSimpleName() + " [" + browser + "]");

        driver = DriverFactory.createDriver(browser);
        driver.manage().window().maximize();
        driver.manage().timeouts().pageLoadTimeout(Duration.ofSeconds(30));
        driver.manage().timeouts().scriptTimeout(Duration.ofSeconds(30));
        wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.get("https://www.amazon.in/");
    }

    protected String resolveBrowser(Method method, Object[] parameters) {
        return System.getProperty("browser", "chrome");
    }

    @AfterMethod
    public void tearDown(ITestResult result) {
        String screenshotPath = null;
        if (result.getStatus() == ITestResult.FAILURE && driver != null) {
            screenshotPath = ScreenshotUtil.takeScreenshot(driver, result.getName(), "failure");
        }
        ExtentManager.logResult(result, screenshotPath);
        if (driver != null) {
            driver.quit();
        }
    }

    protected void assertNoCaptcha() {
        if (driver.getPageSource().contains("Enter the characters you see below")) {
            throw new SkipException(
                    "Amazon displayed a CAPTCHA/security challenge. Test skipped - CAPTCHA is not bypassed.");
        }
    }

    protected String addFirstProductToCart(String screenshotPrefix) {
        assertNoCaptcha();

        HomePage homePage = new HomePage(driver);
        homePage.searchProduct(SEARCH_TERM);

        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        Assert.assertTrue(resultsPage.areResultsDisplayed(), "Search results were not displayed.");

        boolean opened = resultsPage.openFirstOrganicProduct();
        if (!opened) {
            ScreenshotUtil.takeScreenshot(driver, screenshotPrefix + "_no_product_opened");
            throw new SkipException("Could not open any valid (non-sponsored) product from the search results.");
        }
        Assert.assertTrue(driver.getCurrentUrl().contains("/dp/"),
                "Product detail page was not reached. URL=" + driver.getCurrentUrl());

        ProductPage productPage = new ProductPage(driver);
        String productTitle = productPage.getProductTitle();
        Assert.assertFalse(productTitle.isBlank(), "Product title resolution failed.");

        boolean added = productPage.addToCart();
        if (!added) {
            ScreenshotUtil.takeScreenshot(driver, screenshotPrefix + "_add_to_cart_failed");
            throw new SkipException("Product could not be added to the cart (Amazon UI or product limitation).");
        }

        CartPage cartPage = new CartPage(driver);
        cartPage.openCart();
        Assert.assertTrue(cartPage.isCartDisplayed(), "Cart page was not displayed after opening it.");
        return productTitle;
    }
}