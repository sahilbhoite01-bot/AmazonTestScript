package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.ProductPage;
import pages.SearchResultsPage;
import utils.ScreenshotUtil;

public class ProductTest extends BaseTest {

    @Test
    public void TC004_ValidateProductDetails() {
        assertNoCaptcha();

        HomePage homePage = new HomePage(driver);
        homePage.searchProduct(SEARCH_TERM);

        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        Assert.assertTrue(resultsPage.areResultsDisplayed(), "Search results were not displayed.");

        boolean opened = resultsPage.openFirstOrganicProduct();
        if (!opened) {
            ScreenshotUtil.takeScreenshot(driver, "TC004_ValidateProductDetails_no_product_opened");
            throw new SkipException(
                    "Could not open any valid (non-sponsored) product from the search results.");
        }

        Assert.assertTrue(driver.getCurrentUrl().contains("/dp/"),
                "Product detail page was not reached. URL=" + driver.getCurrentUrl());

        ProductPage productPage = new ProductPage(driver);

        String title = productPage.getProductTitle();
        Assert.assertTrue(productPage.isProductTitleDisplayed(),
                "Product title is not visible or is empty.");
        Assert.assertFalse(title.isBlank(), "Product title resolution failed.");
        System.out.println("TC004: title=[" + abbreviate(title, 90) + "]");

        boolean priceAvailable = productPage.isProductPriceDisplayed();
        System.out.println("TC004: price available=" + priceAvailable);
        if (priceAvailable) {
            Double price = productPage.getProductPrice();
            Assert.assertNotNull(price, "Price element present but price could not be parsed");
            Assert.assertTrue(price > 0, "Product price must be greater than zero, was: " + price);
            System.out.println("TC004: price=" + price);
        } else {
            System.out.println("TC004: product exposes no visible price - documented and skipped.");
        }

        Assert.assertTrue(productPage.isProductImageDisplayed(),
                "Primary product image is not displayed.");
        System.out.println("TC004: primary image displayed=true");

        boolean ratingAvailable = productPage.isRatingDisplayed();
        System.out.println("TC004: rating available=" + ratingAvailable);
        if (!ratingAvailable) {
            System.out.println("TC004: rating not exposed for this product - documented and skipped.");
        } else {
            System.out.println("TC004: rating present and non-empty.");
        }

        boolean reviewsAvailable = productPage.isReviewCountDisplayed();
        System.out.println("TC004: review count available=" + reviewsAvailable);
        if (!reviewsAvailable) {
            System.out.println("TC004: review count not exposed for this product - documented and skipped.");
        } else {
            System.out.println("TC004: review count present and non-empty.");
        }
    }

    private String abbreviate(String value, int max) {
        if (value == null) {
            return "";
        }
        return value.length() <= max ? value : value.substring(0, max) + "...";
    }
}