package utils;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;

public class DriverFactory {

    public static WebDriver createDriver() {
        return createDriver(System.getProperty("browser", "chrome"));
    }

    public static WebDriver createDriver(String browser) {
        switch (browser.toLowerCase()) {
            case "chrome":
                return createChromeDriver();
            case "edge":
                return createEdgeDriver();
            default:
                throw new IllegalArgumentException("Unsupported browser: " + browser);
        }
    }

    private static WebDriver createChromeDriver() {
        WebDriverManager.chromedriver().setup();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        return new ChromeDriver(options);
    }

    private static WebDriver createEdgeDriver() {
        String driverPath = locateEdgeDriver();
        if (driverPath != null) {
            System.setProperty("webdriver.edge.driver", driverPath);
        } else {
            WebDriverManager.edgedriver().setup();
        }
        EdgeOptions options = new EdgeOptions();
        options.addArguments("--start-maximized");
        options.addArguments("--disable-notifications");
        options.addArguments("--disable-popup-blocking");
        return new EdgeDriver(options);
    }

    private static String locateEdgeDriver() {
        String fromSystemProperty = System.getProperty("webdriver.edge.driver");
        if (fromSystemProperty != null
                && new java.io.File(fromSystemProperty).isFile()) {
            return fromSystemProperty;
        }
        String edgeMajorVersion = detectEdgeMajorVersion();
        java.io.File cacheRoot = new java.io.File(System.getProperty("user.home")
                + java.io.File.separator + ".cache"
                + java.io.File.separator + "selenium"
                + java.io.File.separator + "edgedriver"
                + java.io.File.separator + "win64");
        java.io.File[] versionDirs = cacheRoot.isDirectory()
                ? cacheRoot.listFiles(java.io.File::isDirectory)
                : null;
        if (versionDirs == null) {
            return null;
        }
        for (java.io.File versionDir : versionDirs) {
            if (edgeMajorVersion != null
                    && !versionDir.getName().startsWith(edgeMajorVersion)) {
                continue;
            }
            java.io.File driver = new java.io.File(versionDir, "msedgedriver.exe");
            if (driver.isFile()) {
                return driver.getAbsolutePath();
            }
        }
        for (java.io.File versionDir : versionDirs) {
            java.io.File driver = new java.io.File(versionDir, "msedgedriver.exe");
            if (driver.isFile()) {
                return driver.getAbsolutePath();
            }
        }
        return null;
    }

    private static String detectEdgeMajorVersion() {
        String[] edgeCandidates = {
                System.getenv("PROGRAMFILES(X86)") + "\\Microsoft\\Edge\\Application\\msedge.exe",
                System.getenv("PROGRAMFILES") + "\\Microsoft\\Edge\\Application\\msedge.exe"
        };
        for (String edgePath : edgeCandidates) {
            if (!new java.io.File(edgePath).isFile()) {
                continue;
            }
            try {
                ProcessBuilder builder = new ProcessBuilder("powershell.exe", "-NoProfile", "-Command",
                        "(Get-Item -LiteralPath '" + edgePath.replace("'", "''")
                                + "').VersionInfo.ProductVersion");
                Process process = builder.start();
                String output = new String(process.getInputStream().readAllBytes(),
                        java.nio.charset.StandardCharsets.UTF_8).trim();
                process.waitFor();
                if (!output.isEmpty() && output.split("\\.").length >= 3) {
                    return output.split("\\.")[0];
                }
            } catch (Exception ignored) {
            }
        }
        return null;
    }
}
