package backend_api;

import com.aventstack.extentreports.ExtentReports;
import com.aventstack.extentreports.ExtentTest;
import com.aventstack.extentreports.reporter.ExtentSparkReporter;
import com.aventstack.extentreports.reporter.configuration.Theme;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.openqa.selenium.Alert;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.FluentWait;
import org.openqa.selenium.support.ui.Select;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.ITestResult;
import org.testng.TestNG;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Parameters;
import org.testng.annotations.Test;
import org.testng.asserts.SoftAssert;
import org.testng.xml.XmlClass;
import org.testng.xml.XmlSuite;
import org.testng.xml.XmlTest;

import java.io.File;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;

/**
 * SeleniumTestSuite.java — Complete Implementation
 * AUT : https://the-internet.herokuapp.com
 * Covers: All 6 TODOs (Page Objects, Setup, Teardown, DataProvider, 8 TCs, main)
 */
public class SeleniumTestSuite {

    // =========================================================================
    // == CONSTANTS (given) ==
    // =========================================================================
    private static final String BASE_URL   = "https://the-internet.herokuapp.com";
    private static final String VALID_USER = "tomsmith";
    private static final String VALID_PASS = "SuperSecretPassword!";
    private static final int    TIMEOUT    = 15;
    private static final String REPORTS_DIR      = "test-reports";
    private static final String SCREENSHOTS_DIR  = REPORTS_DIR + "/screenshots";

    // == LOGGING (given) ==
    private static final Logger log = LogManager.getLogger(SeleniumTestSuite.class);

    // == EXTENT REPORTS (given) ==
    private static ExtentReports extent = null;
    private static final ThreadLocal<ExtentTest> extentTest = new ThreadLocal<>();

    // == WEBDRIVER - ThreadLocal (given) ==
    private static final ThreadLocal<WebDriver>     driverTL = new ThreadLocal<>();
    private static final ThreadLocal<WebDriverWait> waitTL   = new ThreadLocal<>();

    public static WebDriver     getDriver() { return driverTL.get(); }
    public static WebDriverWait getWait()   { return waitTL.get();   }

    // =========================================================================
    // TODO 1 — PAGE OBJECT MODEL (4 static inner classes)
    // =========================================================================

    // -----------------------------------------------------------------
    // TODO 1a: LoginPage
    // -----------------------------------------------------------------
    public static class LoginPage {
        private final WebDriver driver;

        private static final By USERNAME  = By.id("username");
        private static final By PASSWORD  = By.id("password");
        private static final By LOGIN_BTN = By.cssSelector("button[type='submit']");
        private static final By SUCCESS   = By.cssSelector(".flash.success");
        private static final By ERROR     = By.cssSelector(".flash.error");
        private static final By HEADING   = By.tagName("h2");

        public LoginPage(WebDriver driver) {
            this.driver = driver;
            driver.get(BASE_URL + "/login");
        }

        /** Clears and types username; returns this for chaining. */
        public LoginPage enterUsername(String u) {
            driver.findElement(USERNAME).clear();
            driver.findElement(USERNAME).sendKeys(u);
            return this;
        }

        /** Clears and types password; returns this for chaining. */
        public LoginPage enterPassword(String p) {
            driver.findElement(PASSWORD).clear();
            driver.findElement(PASSWORD).sendKeys(p);
            return this;
        }

        public void clickLogin() {
            driver.findElement(LOGIN_BTN).click();
        }

        public String getSuccessMessage() {
            return driver.findElement(SUCCESS).getText().trim();
        }

        public String getErrorMessage() {
            return driver.findElement(ERROR).getText().trim();
        }

        public String getPageHeading() {
            return driver.findElement(HEADING).getText().trim();
        }

        public boolean isUsernameDisplayed() {
            return driver.findElement(USERNAME).isDisplayed();
        }
    }

    // -----------------------------------------------------------------
    // TODO 1b: DropdownPage
    // -----------------------------------------------------------------
    public static class DropdownPage {
        private final WebDriver driver;

        private static final By DROPDOWN = By.id("dropdown");

        public DropdownPage(WebDriver driver) {
            this.driver = driver;
            driver.get(BASE_URL + "/dropdown");
        }

        private Select select() {
            return new Select(driver.findElement(DROPDOWN));
        }

        public void selectByVisibleText(String text) {
            select().selectByVisibleText(text);
        }

        public void selectByValue(String val) {
            select().selectByValue(val);
        }

        public void selectByIndex(int idx) {
            select().selectByIndex(idx);
        }

        public String getSelectedText() {
            return select().getFirstSelectedOption().getText();
        }
    }

    // -----------------------------------------------------------------
    // TODO 1c: AlertsPage
    // -----------------------------------------------------------------
    public static class AlertsPage {
        private final WebDriver driver;

        private static final By BTN_ALERT   = By.xpath("//button[text()='Click for JS Alert']");
        private static final By BTN_CONFIRM = By.xpath("//button[text()='Click for JS Confirm']");
        private static final By BTN_PROMPT  = By.xpath("//button[text()='Click for JS Prompt']");
        private static final By RESULT      = By.id("result");

        public AlertsPage(WebDriver driver) {
            this.driver = driver;
            driver.get(BASE_URL + "/javascript_alerts");
        }

        public void triggerAlert()   { driver.findElement(BTN_ALERT).click();   }
        public void triggerConfirm() { driver.findElement(BTN_CONFIRM).click(); }
        public void triggerPrompt()  { driver.findElement(BTN_PROMPT).click();  }

        public String getAlertText() {
            return driver.switchTo().alert().getText();
        }

        public void acceptAlert() {
            driver.switchTo().alert().accept();
        }

        public void dismissAlert() {
            driver.switchTo().alert().dismiss();
        }

        public void sendKeysAndAccept(String t) {
            Alert alert = driver.switchTo().alert();
            alert.sendKeys(t);
            alert.accept();
        }

        public String getResult() {
            return driver.findElement(RESULT).getText().trim();
        }
    }

    // -----------------------------------------------------------------
    // TODO 1d: DynamicPage
    // -----------------------------------------------------------------
    public static class DynamicPage {
        private final WebDriver driver;

        private static final By START  = By.cssSelector("#start button");
        private static final By FINISH = By.cssSelector("#finish h4");

        public DynamicPage(WebDriver driver, int example) {
            this.driver = driver;
            driver.get(BASE_URL + "/dynamic_loading/" + example);
        }

        public void clickStart() {
            driver.findElement(START).click();
        }

        /** Explicit wait — waits for FINISH element to become visible, returns its text. */
        public String waitExplicit(WebDriverWait wait) {
            return wait.until(
                ExpectedConditions.visibilityOfElementLocated(FINISH)
            ).getText().trim();
        }

        /** Fluent wait — 20 s timeout, 500 ms polling, ignores NoSuchElementException. */
        public String waitFluent() {
            return new FluentWait<>(driver)
                .withTimeout(Duration.ofSeconds(20))
                .pollingEvery(Duration.ofMillis(500))
                .ignoring(org.openqa.selenium.NoSuchElementException.class)
                .until(d -> d.findElement(FINISH))
                .getText().trim();
        }
    }

    // =========================================================================
    // == EXTENT REPORT LIFECYCLE — GIVEN (do not modify) ==
    // =========================================================================
    @BeforeSuite(alwaysRun = true)
    public void initReporting() {
        new File(REPORTS_DIR).mkdirs();
        new File(SCREENSHOTS_DIR).mkdirs();
        String ts   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
        String path = REPORTS_DIR + "/Report_" + ts + ".html";
        ExtentSparkReporter spark = new ExtentSparkReporter(path);
        spark.config().setDocumentTitle("Selenium TestNG Report");
        spark.config().setReportName("The Internet - Automation Suite");
        spark.config().setTheme(Theme.DARK);
        extent = new ExtentReports();
        extent.attachReporter(spark);
        extent.setSystemInfo("AUT", BASE_URL);
        extent.setSystemInfo("OS", System.getProperty("os.name"));
        log.info("ExtentReports ready -> {}", path);
    }

    @AfterSuite(alwaysRun = true)
    public void flushReporting() {
        if (extent != null) extent.flush();
        log.info("ExtentReports flushed.");
    }

    // =========================================================================
    // == Utilities — GIVEN (do not modify) ==
    // =========================================================================
    private String captureScreenshot(String name) {
        try {
            File src = ((TakesScreenshot) getDriver()).getScreenshotAs(OutputType.FILE);
            String ts   = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss"));
            String dest = SCREENSHOTS_DIR + "/" + name + "_" + ts + ".png";
            Files.copy(src.toPath(), Paths.get(dest));
            log.info("Screenshot -> {}", dest);
            return dest;
        } catch (Exception e) {
            log.error("Screenshot failed: {}", e.getMessage());
            return null;
        }
    }

    private void step(String msg) {
        log.info("STEP: {}", msg);
        extentTest.get().info(msg);
    }

    private void sleep(long ms) {
        try { Thread.sleep(ms); } catch (InterruptedException e) { Thread.currentThread().interrupt(); }
    }

    // =========================================================================
    // TODO 2 — @BeforeMethod: Browser Setup
    // =========================================================================
    @Parameters({"browser"})
    @BeforeMethod(alwaysRun = true)
    public void setupBrowser(@org.testng.annotations.Optional("chrome") String browser, Method method) {

        // 1. Structured log line
        log.info("▶ [{}] -> {}", browser.toUpperCase(), method.getName());

        WebDriver d;

        if ("firefox".equalsIgnoreCase(browser)) {
            // 2a. Firefox (headless)
            WebDriverManager.firefoxdriver().setup();
            FirefoxOptions ffOpts = new FirefoxOptions();
            ffOpts.addArguments("--headless");
            d = new FirefoxDriver(ffOpts);
        } else {
            // 2b. Chrome (default, headless)
            WebDriverManager.chromedriver().setup();
            ChromeOptions chromeOpts = new ChromeOptions();
            chromeOpts.addArguments("--headless=new");
            chromeOpts.addArguments("--no-sandbox");
            chromeOpts.addArguments("--disable-dev-shm-usage");
            chromeOpts.addArguments("--window-size=1920,1080");
            d = new ChromeDriver(chromeOpts);
        }

        // 3. Implicit wait & maximize
        d.manage().timeouts().implicitlyWait(Duration.ofSeconds(5));
        d.manage().window().maximize();

        // 4. Bind to thread-safe storage
        driverTL.set(d);
        waitTL.set(new WebDriverWait(d, Duration.ofSeconds(TIMEOUT)));

        // 5. Create ExtentReport node — derive description from @Test annotation
        Test testAnnotation = method.getAnnotation(Test.class);
        String description = (testAnnotation != null && !testAnnotation.description().isEmpty())
                ? testAnnotation.description()
                : method.getName();

        extentTest.set(
            extent.createTest("[" + browser + "] - " + method.getName(), description)
        );
    }

    // =========================================================================
    // TODO 3 — @AfterMethod: Teardown + Screenshot on Failure
    // =========================================================================
    @AfterMethod(alwaysRun = true)
    public void teardownBrowser(ITestResult result) {

        switch (result.getStatus()) {

            case ITestResult.FAILURE:
                log.error("✘ FAILED: {}", result.getName());
                String ss = captureScreenshot(result.getName());
                extentTest.get().fail(result.getThrowable());
                try {
                    if (ss != null) {
                        extentTest.get().addScreenCaptureFromPath(ss, "Failure Screenshot");
                    }
                } catch (Exception e) {
                    log.error("Could not attach screenshot: {}", e.getMessage());
                }
                break;

            case ITestResult.SKIP:
                log.warn("⚠ SKIPPED: {}", result.getName());
                extentTest.get().skip("Test Skipped");
                break;

            default: // SUCCESS
                log.info("✔ PASSED: {}", result.getName());
                extentTest.get().pass("Test Passed");
                break;
        }

        // Global cleanup — always quit & remove thread locals
        WebDriver d = driverTL.get();
        if (d != null) {
            d.quit();
            driverTL.remove();
            waitTL.remove();
        }
    }

    // =========================================================================
    // TODO 4 — @DataProvider
    // =========================================================================
    @DataProvider(name = "loginData", parallel = false)
    public Object[][] loginData() {
        return new Object[][] {
            { "tomsmith",    "SuperSecretPassword!", "success", "Valid credentials" },
            { "tomsmith",    "wrongpassword",        "failure", "Wrong password"    },
            { "invaliduser", "SuperSecretPassword!", "failure", "Wrong username"    },
            { "",            "password11",           "failure", "Empty credentials" }
        };
    }

    // =========================================================================
    // TODO 5 — TEST CASES (8 methods)
    // =========================================================================

    // --- TC001 ---
    @Test(groups = {"smoke", "regression"},
          description = "TC001: Valid login redirects to /secure",
          priority = 1)
    public void TC001_ValidLogin() {
        step("Enter valid credentials and log in");
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterUsername(VALID_USER)
                 .enterPassword(VALID_PASS)
                 .clickLogin();

        // Explicit wait for success flash
        getWait().until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".flash.success")));

        String successMessage = loginPage.getSuccessMessage();
        Assert.assertTrue(successMessage.contains("You logged into a secure area!"),
                "Success message mismatch: " + successMessage);

        String currentUrl = getDriver().getCurrentUrl();
        Assert.assertTrue(currentUrl.endsWith("/secure"),
                "URL should end with /secure but was: " + currentUrl);

        extentTest.get().pass("Login successful.");
    }

    // --- TC002 ---
    @Test(groups = {"smoke", "negative"},
          description = "TC002: Invalid login shows error flash",
          priority = 2)
    public void TC002_InvalidLogin() {
        step("Enter invalid credentials");
        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterUsername("badUser")
                 .enterPassword("badPass")
                 .clickLogin();

        getWait().until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(".flash.error")));

        String errorMessage = loginPage.getErrorMessage();
        Assert.assertTrue(errorMessage.contains("Your username is invalid!"),
                "Error message mismatch: " + errorMessage);
    }

    // --- TC003 ---
    @Test(groups = {"smoke"},
          description = "TC003: SoftAssert on page attributes",
          priority = 3)
    public void TC003_SoftAssertions() {
        LoginPage loginPage = new LoginPage(getDriver());
        SoftAssert soft = new SoftAssert();

        soft.assertTrue(getDriver().getTitle().contains("The Internet"),
                "Page title should contain 'The Internet'");
        soft.assertEquals(loginPage.getPageHeading(), "Login Page",
                "Page heading mismatch");
        soft.assertTrue(getDriver().getCurrentUrl().contains("login"),
                "URL should contain 'login'");
        soft.assertTrue(loginPage.isUsernameDisplayed(),
                "Username field should be displayed");

        soft.assertAll(); // MUST be last
    }

    // --- TC004 ---
    @Test(groups = {"smoke", "regression"},
          description = "TC004: Verify all 3 Select strategies",
          priority = 4)
    public void TC004_DropdownSelection() {
        DropdownPage dp = new DropdownPage(getDriver());

        dp.selectByVisibleText("Option 1");
        Assert.assertEquals(dp.getSelectedText(), "Option 1",
                "Expected 'Option 1' after selectByVisibleText");

        dp.selectByValue("2");
        Assert.assertEquals(dp.getSelectedText(), "Option 2",
                "Expected 'Option 2' after selectByValue");

        dp.selectByIndex(1);
        Assert.assertEquals(dp.getSelectedText(), "Option 1",
                "Expected 'Option 1' after selectByIndex(1)");
    }

    // --- TC005 ---
    @Test(groups = {"regression"},
          description = "TC005: Accept alert, dismiss confirm, send keys to prompt",
          priority = 5)
    public void TC005_AlertHandling() {
        AlertsPage ap = new AlertsPage(getDriver());

        // Part A — JS Alert: trigger → verify text → accept → verify result
        step("Part A: Accept JS Alert");
        ap.triggerAlert();
        getWait().until(ExpectedConditions.alertIsPresent());
        Assert.assertEquals(ap.getAlertText(), "I am a JS Alert");
        ap.acceptAlert();
        Assert.assertEquals(ap.getResult(), "You successfully clicked an alert");

        // Part B — JS Confirm: trigger → dismiss → verify result
        step("Part B: Dismiss JS Confirm");
        ap.triggerConfirm();
        getWait().until(ExpectedConditions.alertIsPresent());
        ap.dismissAlert();
        Assert.assertEquals(ap.getResult(), "You clicked: Cancel");

        // Part C — JS Prompt: trigger → send keys + accept → verify result
        step("Part C: Send keys to JS Prompt");
        ap.triggerPrompt();
        getWait().until(ExpectedConditions.alertIsPresent());
        ap.sendKeysAndAccept("Hello TestNG");
        Assert.assertEquals(ap.getResult(), "You entered: Hello TestNG");
    }

    // --- TC006 ---
    @Test(groups = {"regression"},
          description = "TC006: Open new window, verify title, close and return",
          priority = 6)
    public void TC006_MultipleWindows() {
        // Inline — no Page Object
        getDriver().get(BASE_URL + "/windows");

        String original = getDriver().getWindowHandle();
        step("Original handle: " + original);

        getDriver().findElement(By.linkText("Click Here")).click();
        getWait().until(ExpectedConditions.numberOfWindowsToBe(2));

        // Find the newly opened window
        String newHandle = getDriver().getWindowHandles()
                .stream()
                .filter(h -> !h.equals(original))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("New window handle not found"));

        getDriver().switchTo().window(newHandle);

        // Wait for title to load — page may not be ready immediately after switch
        getWait().until(ExpectedConditions.titleIs("New Window"));

        Assert.assertEquals(getDriver().getTitle(), "New Window",
                "New window title should be 'New Window'");

        getDriver().close();
        getDriver().switchTo().window(original);

        Assert.assertEquals(getDriver().getWindowHandles().size(), 1,
                "Only the original window should remain");
    }

    // --- TC007 ---
    @Test(groups = {"regression", "waits"},
          description = "TC007: Explicit Wait and Fluent Wait demo",
          priority = 7)
    public void TC007_WaitStrategies() {
        // Part A — Explicit Wait (example 1)
        step("Part A: Explicit Wait on /dynamic_loading/1");
        DynamicPage dp1 = new DynamicPage(getDriver(), 1);
        dp1.clickStart();
        sleep(300); // Thread.sleep demonstration only
        String textA = dp1.waitExplicit(getWait());
        Assert.assertEquals(textA, "Hello World!", "Explicit wait text mismatch");

        // Part B — Fluent Wait (example 2)
        step("Part B: Fluent Wait on /dynamic_loading/2");
        getDriver().get(BASE_URL + "/dynamic_loading/2");
        DynamicPage dp2 = new DynamicPage(getDriver(), 2);
        dp2.clickStart();
        String textB = dp2.waitFluent();
        Assert.assertEquals(textB, "Hello World!", "Fluent wait text mismatch");
    }

    // --- TC008 ---
    @Test(groups = {"regression", "data-driven"},
          description = "TC008: Data-driven login - 4 scenarios",
          dataProvider = "loginData",
          priority = 8)
    public void TC008_DataDrivenLogin(String username, String password,
                                      String expected, String scenario) {
        step("Scenario: [" + scenario + "] | User: " + username);
        extentTest.get().info("Scenario: " + scenario);

        LoginPage loginPage = new LoginPage(getDriver());
        loginPage.enterUsername(username)
                 .enterPassword(password)
                 .clickLogin();

        if ("success".equals(expected)) {
            // Wait until URL reflects /secure
            getWait().until(ExpectedConditions.urlContains("/secure"));
            Assert.assertTrue(getDriver().getCurrentUrl().contains("/secure"),
                    "Expected URL to contain /secure for scenario: " + scenario);
        } else {
            // Wait for error flash and verify it is not empty
            getWait().until(ExpectedConditions.visibilityOfElementLocated(
                    By.cssSelector(".flash.error")));
            String errorText = loginPage.getErrorMessage();
            Assert.assertFalse(errorText.isEmpty(),
                    "Error message should not be empty for scenario: " + scenario);
        }
    }

    // =========================================================================
    // TODO 6 — main(): Programmatic TestNG Execution
    // =========================================================================
    public static void main(String[] args) {

        // 1. Configure XmlSuite
        XmlSuite suite = new XmlSuite();
        suite.setName("The Internet - Selenium Suite");
        suite.setParallel(XmlSuite.ParallelMode.NONE);
        suite.setThreadCount(2);
        suite.setDataProviderThreadCount(2);
        suite.setParameters(Collections.singletonMap("browser", "chrome"));

        // 2. Configure XmlTest
        XmlTest xmlTest = new XmlTest(suite);
        xmlTest.setName("Full Regression");
        xmlTest.setClasses(Collections.singletonList(
                new XmlClass(SeleniumTestSuite.class.getName())));

        // Uncomment to run only the smoke group:
        // xmlTest.setIncludedGroups(Collections.singletonList("smoke"));

        // 3. Run with programmatic TestNG runner
        TestNG runner = new TestNG();
        runner.setXmlSuites(Collections.singletonList(suite));
        runner.run();

        // 4. Done
        System.out.println("Reports generated in " + REPORTS_DIR + "/");
    }
}
