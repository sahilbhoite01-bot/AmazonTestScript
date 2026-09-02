package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.HomePage;
import pages.LoginPage;
import pages.ProductPage;
import pages.SearchResultsPage;
import utils.ScreenshotUtil;

import java.util.Set;

public class CheckoutTest extends BaseTest {

    @Test
    public void TC008_ProceedToCheckout() {
        String productTitle = addFirstProductToCart("TC008_ProceedToCheckout");

        CartPage cartPage = new CartPage(driver);
        Assert.assertTrue(cartPage.isProductPresentInCart(productTitle),
                "Product expected in the cart before proceeding to checkout: " + productTitle);
        System.out.println("TC008: product present in cart=true");

        boolean clicked = cartPage.clickProceedToBuy();
        Assert.assertTrue(clicked, "Proceed to Buy button was not clickable or did not execute.");

        Set<String> handlesBefore = driver.getWindowHandles();
        try {
            wait.until(d -> {
                boolean newWindowOpened =
                        d.getWindowHandles().stream().anyMatch(handle -> !handlesBefore.contains(handle));
                return newWindowOpened || !d.getCurrentUrl().contains("cart");
            });
        } catch (Exception ignored) {
        }

        for (String handle : driver.getWindowHandles()) {
            if (!handlesBefore.contains(handle)) {
                driver.switchTo().window(handle);
                break;
            }
        }

        assertNoCaptcha();

        LoginPage loginPage = new LoginPage(driver);
        boolean loginStage = loginPage.isLoginPageDisplayed();
        boolean heading = loginPage.isSignInHeadingDisplayed();
        boolean emailField = loginPage.isEmailOrMobileFieldDisplayed();
        boolean submit = loginPage.isSignInSubmitDisplayed();

        System.out.println("TC008: reached URL=" + driver.getCurrentUrl());
        System.out.println("TC008: page title=" + driver.getTitle());
        System.out.println("TC008: login page displayed=" + loginStage
                + ", sign-in heading=" + heading
                + ", email/mobile field=" + emailField
                + ", sign-in submit=" + submit);

        Assert.assertTrue(loginStage,
                "Amazon did not route the user to the sign-in/authentication stage."
                        + " URL=" + driver.getCurrentUrl() + ", title=" + driver.getTitle());
        Assert.assertTrue(heading, "Sign-in heading was not displayed on the reached page.");
        Assert.assertTrue(emailField, "Email/mobile entry field was not displayed on the reached page.");

        System.out.println("TC008: Validation stopped at the sign-in stage - no credentials were submitted.");
    }
}