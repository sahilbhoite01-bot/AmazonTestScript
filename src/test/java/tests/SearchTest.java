package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.SkipException;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.SearchResultsPage;
import utils.ScreenshotUtil;

import java.util.Arrays;
import java.util.List;

public class SearchTest extends BaseTest {

    private static final List<String> RELEVANT_KEYWORDS =
            Arrays.asList("wireless", "headphones", "headset", "earbuds");

    @Test
    public void TC001_SearchForWirelessHeadphones() {
        assertNoCaptcha();

        HomePage homePage = new HomePage(driver);
        homePage.searchProduct(SEARCH_TERM);

        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        Assert.assertTrue(resultsPage.areResultsDisplayed(), "Search results were not displayed.");

        List<String> titles = resultsPage.getProductTitles();
        long relevant = resultsPage.countRelevantTitles(RELEVANT_KEYWORDS);
        System.out.println("TC001: total titles=" + titles.size() + ", relevant=" + relevant
                + ", keywords=" + RELEVANT_KEYWORDS);

        Assert.assertFalse(titles.isEmpty(), "No product titles were extracted from the search results.");

        Assert.assertTrue(relevant >= 1,
                "No displayed result matched any relevant keyword " + RELEVANT_KEYWORDS);

        Assert.assertTrue(relevant >= titles.size() / 2.0,
                "Less than half of the displayed results were relevant to headphones. relevant="
                        + relevant + ", total=" + titles.size() + ", keywords=" + RELEVANT_KEYWORDS);
    }

    @Test
    public void TC002_ApplyPriceFilter() {
        assertNoCaptcha();

        HomePage homePage = new HomePage(driver);
        homePage.searchProduct(SEARCH_TERM);

        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        Assert.assertTrue(resultsPage.areResultsDisplayed(),
                "Search results were not displayed before applying the price filter.");

        Assert.assertTrue(resultsPage.getPriceRangeOptionCount() > 0,
                "No predefined price range options were available on the Search Results page.");

        int rangeIndex = resultsPage.findBoundedRangeIndex();
        String rangeLabel = resultsPage.applyPriceRange(rangeIndex);
        Assert.assertNotNull(rangeLabel,
                "Price range could not be applied - the filter control became unavailable.");

        double[] bounds = resultsPage.parseRangeBounds(rangeLabel);
        Assert.assertNotNull(bounds, "Could not parse the price range label: " + rangeLabel);
        System.out.println("TC002: applied range [" + rangeLabel + "], bounds ["
                + bounds[0] + ", " + (bounds[1] == Double.MAX_VALUE ? "infinity" : bounds[1]) + "]");

        Assert.assertTrue(driver.getCurrentUrl().contains("p_36"),
                "URL does not reflect the applied price filter (rh=p_36). URL=" + driver.getCurrentUrl());

        List<Double> prices = resultsPage.getProductPrices();
        System.out.println("TC002: visible organic prices after filter: " + prices);

        if (prices.size() < 2) {
            ScreenshotUtil.takeScreenshot(driver, "TC002_ApplyPriceFilter_insufficient_prices");
            throw new SkipException("Fewer than two priced organic results after applying [" + rangeLabel
                    + "], cannot validate the filter. prices=" + prices);
        }

        long inRange = prices.stream()
                .filter(price -> price >= bounds[0] && price <= bounds[1])
                .count();
        Assert.assertTrue(inRange >= prices.size() * 0.8,
                "Most visible prices are outside the applied range " + java.util.Arrays.toString(bounds)
                        + ". prices=" + prices);
    }

    @Test
    public void TC003_SortPriceLowToHigh() {
        assertNoCaptcha();

        HomePage homePage = new HomePage(driver);
        homePage.searchProduct(SEARCH_TERM);

        SearchResultsPage resultsPage = new SearchResultsPage(driver);
        Assert.assertTrue(resultsPage.areResultsDisplayed(),
                "Search results were not displayed before sorting.");

        resultsPage.sortByPriceLowToHigh();

        Assert.assertTrue(driver.getCurrentUrl().contains("price-asc-rank"),
                "Sort by 'Price: Low to High' was not reflected in the URL. URL=" + driver.getCurrentUrl());
        Assert.assertEquals(resultsPage.getSelectedSortValue(), "price-asc-rank",
                "Sort dropdown did not retain 'Price: Low to High' after reload.");

        List<Double> prices = resultsPage.getProductPrices();
        System.out.println("TC003: extracted prices: " + prices);

        if (prices.size() < 2) {
            ScreenshotUtil.takeScreenshot(driver, "TC003_SortPriceLowToHigh_insufficient_prices");
            throw new SkipException("Fewer than two priced organic results to validate ascending order. prices="
                    + prices);
        }

        for (int i = 1; i < prices.size(); i++) {
            Assert.assertTrue(prices.get(i) >= prices.get(i - 1),
                    "Prices are not in ascending order at index " + i + ": "
                            + prices.get(i - 1) + " > " + prices.get(i) + " (full list=" + prices + ")");
        }
    }
}