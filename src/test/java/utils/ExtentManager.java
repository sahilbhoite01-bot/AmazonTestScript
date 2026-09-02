package utils;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import org.testng.ITestResult;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;

public class ExtentManager {

    private static final String REPORT_FILE = "test-output/ExtentReport.html";

    private static ExtentReports extent;
    private static ExtentTest currentTest;

    private ExtentManager() {
    }

    public static synchronized void init() {
        if (extent != null) {
            return;
        }
        File reportFile = new File(REPORT_FILE);
        File parent = reportFile.getParentFile();
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }
        ExtentSparkReporter extentSparkReporter = new ExtentSparkReporter(reportFile);
        extentSparkReporter.config().setDocumentTitle("Amazon India QA Automation - Test Report");
        extentSparkReporter.config().setReportName("Amazon India QA Automation - Extent Report");
        extent = new ExtentReports();
        extent.attachReporter(extentSparkReporter);
        extent.setSystemInfo("Application", "Amazon India (amazon.in)");
        extent.setSystemInfo("OS", System.getProperty("os.name") + " " + System.getProperty("os.version"));
        extent.setSystemInfo("Java", System.getProperty("java.version"));
    }

    public static synchronized void startTest(String testName, String category) {
        if (extent == null) {
            init();
        }
        currentTest = extent.createTest(testName);
        currentTest.assignCategory(category);
    }

    public static synchronized void logResult(ITestResult result, String screenshotPath) {
        if (currentTest == null) {
            return;
        }
        long durationMs = result.getEndMillis() - result.getStartMillis();
        switch (result.getStatus()) {
            case ITestResult.SUCCESS:
                currentTest.pass("PASS");
                break;
            case ITestResult.FAILURE:
                Throwable throwable = result.getThrowable();
                currentTest.fail(throwable != null
                        ? throwable.toString()
                        : "FAILED - no exception detail recorded");
                break;
            case ITestResult.SKIP:
                Throwable skipCause = result.getThrowable();
                currentTest.skip(skipCause != null
                        ? skipCause.getMessage()
                        : "SKIPPED");
                break;
            default:
                break;
        }
        currentTest.info("Execution time: " + durationMs + " ms");
        attachScreenshot(screenshotPath);
    }

    public static synchronized void flush() {
        if (extent != null) {
            extent.flush();
        }
    }

    private static void attachScreenshot(String screenshotPath) {
        if (screenshotPath == null || currentTest == null) {
            return;
        }
        try {
            Path path = Paths.get(screenshotPath);
            if (!Files.exists(path)) {
                return;
            }
            String base64 = Base64.getEncoder().encodeToString(Files.readAllBytes(path));
            currentTest.addScreenCaptureFromBase64String(base64, "Failure screenshot");
        } catch (IOException | RuntimeException ignored) {
        }
    }

    public static String getReportPath() {
        return new File(REPORT_FILE).getAbsolutePath();
    }
}