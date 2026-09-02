# Amazon India QA Automation Assignment

## 1. Objective

This project automates selected critical scenarios from the Amazon manual QA assignment using
Selenium WebDriver. It covers the core customer journey on Amazon India (amazon.in) - search,
product validation, cart operations, checkout flow, exploratory/invalid-input behaviour and a
cross-browser regression journey - so the repeatable scenarios from manual testing run
automatically and produce a readable test report.

## 2. Application Under Test

- **Application**: Amazon India (https://www.amazon.in/)
- Manual testing sheet used as the source of scenarios; automation targets the repeatable regression subset.

## 3. Technology Stack

- Java 17
- Selenium WebDriver 4.27.0
- TestNG 7.10.2
- Maven
- Page Object Model (POM) design pattern
- WebDriverManager 5.9.0
- ExtentReports 5.1.2 (reporting)

## 4. Test Coverage

| TC ID | Module | Scenario |
|------|--------|----------|
| TC-001 | Search | Search for wireless headphones |
| TC-002 | Search | Apply price filter |
| TC-003 | Search | Sort low to high |
| TC-004 | Product | Validate product details |
| TC-005 | Cart | Add product to cart |
| TC-006 | Cart | Update quantity |
| TC-007 | Cart | Remove product |
| TC-008 | Checkout | Proceed to checkout |
| TC-009 | Exploratory | Invalid search |
| TC-010 | Cross Browser | Chrome + Edge critical journey |

## 5. Framework Architecture

The framework uses the **Page Object Model (POM)**. Each Amazon page is represented by a class
that encapsulates its locators and the actions a tester performs on it. Tests drive pages through
those objects instead of touching "raw" Selenium locators, which keeps selectors in one place and
makes the tests readable and maintainable.

- **BaseTest** (`base/BaseTest.java`) - central setup/teardown. Creates the browser driver
  (Chrome or Edge), maximises the window, applies timeouts, navigates to the Amazon homepage,
  starts an ExtentReports test entry for every test method and, on failure, captures a screenshot
  and writes the outcome into the report. It also holds shared helpers (`assertNoCaptcha`,
  `addFirstProductToCart`).
- **Page Objects** (`pages/*.java`) - `HomePage`, `SearchResultsPage`, `ProductPage`, `CartPage`,
  `LoginPage`. Each exposes semantic methods such as `searchProduct`, `applyPriceRange`,
  `addToCart`, `changeQuantity`, `removeProduct`, `clickProceedToBuy`.
- **Test Classes** (`tests/*.java`) - one class per module: `SearchTest`, `ProductTest`,
  `CartTest`, `CheckoutTest`, `ExploratoryTest`, `CrossBrowserTest`. Each `@Test` method maps to one
  TC ID and asserts the expected behaviour.
- **Utilities** (`utils/`) - `DriverFactory` builds the correct WebDriver (Chrome/Edge);
  `ScreenshotUtil` saves failure screenshots; `ExtentManager` drives the ExtentReports HTML report.
- **DriverFactory** resolves the Edge driver locally (version-matched) because the legacy
  `msedgedriver.azureedge.net` host no longer resolves, then falls back to WebDriverManager.

## 6. Project Structure

```
Amazon TestScript/
├── pom.xml                         # Maven build configuration
├── testng.xml                      # TestNG suite definition
├── README.md
├── src/test/java/
│   ├── base/
│   │   └── BaseTest.java           # setup/teardown, report hooks, shared helpers
│   ├── pages/
│   │   ├── HomePage.java
│   │   ├── SearchResultsPage.java
│   │   ├── ProductPage.java
│   │   ├── CartPage.java
│   │   └── LoginPage.java
│   ├── tests/
│   │   ├── SearchTest.java         # TC-001, TC-002, TC-003
│   │   ├── ProductTest.java        # TC-004
│   │   ├── CartTest.java           # TC-005, TC-006, TC-007
│   │   ├── CheckoutTest.java       # TC-008
│   │   ├── ExploratoryTest.java    # TC-009
│   │   └── CrossBrowserTest.java   # TC-010 (Chrome + Edge)
│   └── utils/
│       ├── DriverFactory.java      # Chrome/Edge WebDriver creation
│       ├── ScreenshotUtil.java     # screenshot-on-failure
│       └── ExtentManager.java      # ExtentReports HTML report
├── screenshots/                    # generated on failure (not committed)
├── test-output/
│   └── ExtentReport.html           # generated HTML report (not committed)
└── target/                         # build output (not committed)
```

## 7. Prerequisites

- Java 17 or higher
- Maven 3.6+
- Google Chrome installed
- Microsoft Edge installed
- Internet connection (the amazon.in pages and WebDriverManager downloads require it)

## 8. How to Run

Run the whole suite (all TCs, Chrome + Edge for TC-010):

```bash
mvn clean test
```

Run only one module:

```bash
mvn test "-Dtest=SearchTest"
mvn test "-Dtest=ProductTest"
mvn test "-Dtest=CartTest"
mvn test "-Dtest=CheckoutTest"
mvn test "-Dtest=ExploratoryTest"
mvn test "-Dtest=CrossBrowserTest"
```

Run the whole suite via the TestNG suite file:

```bash
mvn test "-DsuiteXmlFile=testng.xml"
```

Result of `mvn clean test` (latest run): 11/11 tests passed (0 failures, 0 skipped).

## 9. Browser Execution

- Default browser is **Chrome** (`System.getProperty("browser", "chrome")`).
- Standard tests can be forced to a browser:

```bash
mvn test -Dbrowser=chrome
mvn test -Dbrowser=edge
```

- **TC-010 runs the same critical journey on both browsers** using a TestNG `@DataProvider`.
  It can be filtered:

```bash
mvn test "-Dtest=CrossBrowserTest"                       # both Chrome and Edge
mvn test "-Dtest=CrossBrowserTest" "-Dcross.browser=chrome"
mvn test "-Dtest=CrossBrowserTest" "-Dcross.browser=edge"
```

- These two browsers were chosen because the automation environment is **Windows-based**.
  Safari is not covered because there is no macOS environment.
- Edge driver note: Microsoft retired `msedgedriver.azureedge.net` (DNS no longer resolves).
  `DriverFactory.locateEdgeDriver()` matches the installed Edge version against a locally cached
  `msedgedriver.exe` (e.g. `~/.cache/selenium/edgedriver/win64/<version>`) and only falls back to
  WebDriverManager otherwise.

## 10. Reporting

Reports are generated in the project folder:

- HTML test report: `test-output/ExtentReport.html` (open it in a browser)
- TestNG/Maven reports: `target/surefire-reports/index.html`

The ExtentReports report is produced by `ExtentManager` (a `@BeforeSuite`/`@AfterSuite` hook in
`BaseTest`). For every test it records:

- test name and TC ID (the test method name contains the TC ID, e.g. `TC001_SearchForWirelessHeadphones`)
- result status (PASS / FAIL / SKIP)
- execution time in milliseconds
- failure reason (exception message on FAIL, skip reason on SKIP)
- screenshot on failure (embedded into the report)

## 11. Screenshots

- On any test **failure**, `ScreenshotUtil` saves a screenshot to the `screenshots/` folder and it
  is embedded into the HTML report.
- Screenshot names are meaningful, e.g.
  `TC001_SearchForWirelessHeadphones_failure_20260901_211133.png`.
- Screenshots are also taken at selected decision points (e.g. "insufficient prices", "no product
  opened") to support honest SKIP documentation.
- The `screenshots/` folder only contains failure/debug captures; temporary files are not kept.

## 12. Limitations

Documented Amazon behaviours encountered while building these tests:

- **CAPTCHA / login walls**: Amazon may present a CAPTCHA or require login on automated traffic.
  Tests detect the "Enter the characters you see below" challenge and **skip** rather than bypass.
  Checkout (TC-008) intentionally stops at the sign-in page.
- **Dynamic UI**: Amazon renders product cards, add-to-cart, quantity and checkout controls
  differently across templates and offers; locators target stable IDs/attributes and multiple
  fallbacks are used.
- **Login requirement**: full checkout, order placement and address steps need credentials and
  are therefore out of scope.
- **Product availability**: results vary per day; price filter range options and some product
  detail fields (rating/review count) are not guaranteed for every offer, so those are logged and
  skipped when genuinely absent.
- **Dynamic prices**: prices change frequently; tests validate relationships (in-range, ascending,
  subtotal increases) rather than fixed values.
- **Location-specific content**: amazon.in content (offers, filters, language) depends on location,
  sign-in state and session; a fresh guest session is used for each test.

## 13. Security

- **No real credentials are used.**
- **No payment information is used.**
- **No order is placed** - TC-008 stops at the authentication stage and verifies the routing only.
- **CAPTCHA / security mechanisms are not bypassed** - if a challenge appears, the test is skipped.

## 14. Manual + Automation Approach

**Manual testing** covered functional testing (search, filters, sorting, product details, cart,
checkout routing), exploratory testing (invalid/edge-case input such as TC-009's garbage query),
UAT-style end-to-end walks of the key buying journey, and cross-browser verification on Chrome and
Edge.

**Automation** was then applied to the **repeatable regression scenarios** from that manual effort -
the steps that are deterministic enough to encode as Selenium tests and that catch regressions
when re-run (TC-001 to TC-010). The suite runs the critical journey on both Chrome and Edge so the
manual cross-browser passes are guarded going forward, and every run produces a PASS/FAIL/SKIP
report with timings and failure screenshots.#   A m a z o n T e s t S c r i p t  
 