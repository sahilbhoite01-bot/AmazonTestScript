package tests;

import base.BaseTest;
import org.testng.Assert;
import org.testng.annotations.Test;
import pages.HomePage;
import pages.SearchResultsPage;

public class ExploratoryTest extends BaseTest {

    private static final String INVALID_QUERY = "@@@###XYZ123!!!";

    @Test
    public void TC009_InvalidSearch() {
        assertNoCaptcha();

        HomePage homePage = new HomePage(driver);
        homePage.searchProduct(INVALID_QUERY);

        SearchResultsPage resultsPage = new SearchResultsPage(driver);

        boolean urlIsSearch = driver.getCurrentUrl().contains("s?k=")
                || driver.getCurrentUrl().contains("/s?");
        System.out.println("TC009: URL=" + driver.getCurrentUrl());
        System.out.println("TC009: title=" + driver.getTitle());
        Assert.assertTrue(urlIsSearch,
                "The application did not respond with a search page. URL=" + driver.getCurrentUrl());

        Assert.assertFalse(driver.getTitle().isBlank(),
                "Page title is blank - the application did not respond normally.");

        Assert.assertTrue(resultsPage.isSearchResponseReady(),
                "The search response container never rendered.");

        Assert.assertTrue(resultsPage.hasMeaningfulContent(),
                "The search response body appears blank - possible blank page.");

        Assert.assertFalse(resultsPage.hasServerErrorState(),
                "The search response contains unexpected server-error markers.");

        boolean noResults = resultsPage.hasNoResultsState();
        boolean anyResults = resultsPage.areResultsDisplayed();
        System.out.println("TC009: no-results state detected=" + noResults
                + ", result cards displayed=" + anyResults);

        Assert.assertTrue(noResults || anyResults,
                "Neither a no-results state nor a results state was displayed.");

        if (noResults) {
            System.out.println("TC009: Amazon displayed a no-result state - handled gracefully.");
        } else {
            System.out.println("TC009: Amazon displayed an appropriate results state"
                    + " for the invalid query - handled gracefully.");
        }
    }
}