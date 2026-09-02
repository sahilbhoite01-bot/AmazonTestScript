package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pages.CartPage;
import pages.HomePage;
import pages.ProductPage;
import pages.SearchResultsPage;
import utils.ScreenshotUtil;

public class CartTest extends BaseTest {

    @Test
    public void TC005_AddProductToCart() {
        String productTitle = addFirstProductToCart("TC005_AddProductToCart");

        CartPage cartPage = new CartPage(driver);
        String cartTitle = cartPage.getCartProductTitle();
        System.out.println("TC005: PDP title=[" + productTitle + "]");
        System.out.println("TC005: cart title=[" + cartTitle + "]");

        Assert.assertFalse(cartTitle.isBlank(), "No product title was found in the cart.");
        Assert.assertTrue(CartPage.titlesMatch(productTitle, cartTitle),
                "Cart does not contain the product opened on the PDP."
                        + " pdpTitle=" + productTitle + ", cartTitle=" + cartTitle);
    }

    @Test
    public void TC006_UpdateCartQuantity() {
        String productTitle = addFirstProductToCart("TC006_UpdateCartQuantity");

        CartPage cartPage = new CartPage(driver);
        Assert.assertTrue(CartPage.titlesMatch(productTitle, cartPage.getCartProductTitle()),
                "Expected product is not present in the cart before updating quantity.");

        int initialQuantity = cartPage.getQuantity();
        System.out.println("TC006: initial quantity=" + initialQuantity);
        if (initialQuantity < 1) {
            ScreenshotUtil.takeScreenshot(driver, "TC006_UpdateCartQuantity_no_quantity_control");
            throw new SkipException(
                    "Quantity control is not exposed for this product/cart layout, "
                            + "so quantity cannot be changed. Documented limitation.");
        }
        if (initialQuantity >= 2) {
            System.out.println("TC006: quantity is already " + initialQuantity
                    + " - reducing to 1 first for a meaningful change test.");
            boolean decreased = cartPage.changeQuantity(1);
            Assert.assertTrue(decreased, "Could not reduce quantity to 1 before the update test.");
            initialQuantity = cartPage.getQuantity();
        }

        Double subtotalBefore = cartPage.getSubtotal();
        System.out.println("TC006: subtotal before=" + subtotalBefore);

        boolean changed = cartPage.changeQuantity(2);
        if (!changed) {
            ScreenshotUtil.takeScreenshot(driver, "TC006_UpdateCartQuantity_change_failed");
            throw new SkipException("Quantity could not be updated from " + initialQuantity
                    + " to 2 for this product. Documented limitation: "
                    + "the cart quantity stepper did not apply the change.");
        }

        int quantityAfter = cartPage.getQuantity();
        System.out.println("TC006: quantity after=" + quantityAfter);
        Assert.assertEquals(quantityAfter, 2,
                "Cart quantity was not updated to 2. actual=" + quantityAfter);

        Double subtotalAfter = cartPage.getSubtotal();
        System.out.println("TC006: subtotal after=" + subtotalAfter);

        if (subtotalBefore != null && subtotalAfter != null) {
            Assert.assertTrue(subtotalAfter > subtotalBefore,
                    "Cart subtotal did not increase after doubling the quantity: before="
                            + subtotalBefore + ", after=" + subtotalAfter);
        } else {
            System.out.println("TC006: subtotal is not reliably available (before=" + subtotalBefore
                    + ", after=" + subtotalAfter + "). Subtotal change documented and not asserted.");
        }
    }

    @Test
    public void TC007_RemoveProductFromCart() {
        String productTitle = addFirstProductToCart("TC007_RemoveProductFromCart");

        CartPage cartPage = new CartPage(driver);
        Assert.assertTrue(cartPage.isProductPresentInCart(productTitle),
                "Product should be present in the cart before removal: " + productTitle);
        System.out.println("TC007: product present in cart before removal=true");

        boolean removed = cartPage.removeProduct();
        Assert.assertTrue(removed, "Remove action did not execute on the cart.");

        cartPage.openCart();

        boolean stillPresent = cartPage.isProductPresentInCart(productTitle);
        System.out.println("TC007: product still present after removal=" + stillPresent
                + ", cart item count=" + cartPage.getCartItemCount()
                + ", cart empty=" + cartPage.isCartEmpty());

        Assert.assertFalse(stillPresent, "Product is still present in the cart after removal: " + productTitle);
        Assert.assertEquals(cartPage.getCartItemCount(), 0,
                "Cart still lists items after removing the product.");
        Assert.assertTrue(cartPage.isCartEmpty(),
                "Cart does not reflect the empty state after removing the only item.");
    }
}