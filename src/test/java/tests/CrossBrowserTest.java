package tests;

import base.BaseTest;
import org.openqa.selenium.By;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.HomePage;
import pages.ProductPage;
import pages.SearchResultsPage;

import java.lang.reflect.Method;

public class CrossBrowserTest extends BaseTest {

    @DataProvider(name = "browsers", parallel = false)
    public Object[][] browsers() {
        String only = System.getProperty("cross.browser");
        if (only != null && only.equalsIgnoreCase("chrome")) {
            return new Object[][]{{"chrome"}};
        }
        if (only != null && only.equalsIgnoreCase("edge")) {
            return new Object[][]{{"edge"}};
        }
        return new Object[][]{{"chrome"}, {"edge"}};
    }

    @Override
    protected String resolveBrowser(Method method, Object[] parameters) {
        if (parameters != null && parameters.length > 0 && parameters[0] != null) {
            return parameters[0].toString();
        }
        return System.getProperty("browser", "chrome");
    }

    @Test(dataProvider = "browsers")
    public void TC010_CrossBrowserCriticalJourney(String browser) {
        System.out.println("TC010: browser=" + browser + " - beginning critical journey");

        if (driver == null) {
            Assert.fail("No browser driver was created for browser: " + browser);
        }

        assertNoCaptcha();

        Assert.assertTrue(driver.getCurrentUrl().contains("amazon.in"),
                "Amazon homepage did not load. URL=" + driver.getCurrentUrl());
        wait.until(ExpectedConditions.visibilityOfElementLocated(By.id("twotabsearchtextbox")));
        System.out.println("TC010: [" + browser + "] 1. homepage loaded");

        HomePage homePage = new HomePage(driver);
        homePage.searchProduct(SEARCH_TERM);
        System.out.println("TC010: [" + browser + "] 2. search executed");

        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        Assert.assertTrue(resultsPage.areResultsDisplayed(),
                "[" + browser + "] Search results were not displayed.");
        System.out.println("TC010: [" + browser + "] 3. search results displayed");

        boolean opened = resultsPage.openFirstOrganicProduct();
        if (!opened) {
            throw new SkipException(
                    "[" + browser + "] Could not open any valid (non-sponsored) product from the search results.");
        }
        Assert.assertTrue(driver.getCurrentUrl().contains("/dp/"),
                "[" + browser + "] Product detail page was not reached. URL=" + driver.getCurrentUrl());
        System.out.println("TC010: [" + browser + "] 4. product page opened");

        ProductPage productPage = new ProductPage(driver);
        String productTitle = productPage.getProductTitle();
        Assert.assertFalse(productTitle.isBlank(), "[" + browser + "] Product title resolution failed.");

        boolean added = productPage.addToCart();
        if (!added) {
            throw new SkipException(
                    "[" + browser + "] Product could not be added to the cart (Amazon UI or product limitation).");
        }
        System.out.println("TC010: [" + browser + "] 5. add to cart executed");

        CartPage cartPage = new CartPage(driver);
        cartPage.openCart();
        Assert.assertTrue(cartPage.isCartDisplayed(),
                "[" + browser + "] Cart page was not displayed after opening it.");
        System.out.println("TC010: [" + browser + "] 6. cart opened");

        Assert.assertTrue(cartPage.isProductPresentInCart(productTitle),
                "[" + browser + "] Product expected in the cart was not found. title=" + productTitle);
        System.out.println("TC010: [" + browser + "] 7. product verified in cart - journey complete");
    }
}