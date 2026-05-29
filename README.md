# Selenium Test Suite

A comprehensive automated testing framework built with Java, Selenium WebDriver, and TestNG, designed to test the UI components of [The Internet](https://the-internet.herokuapp.com).

## Overview

This repository demonstrates best practices in test automation by implementing the **Page Object Model (POM)** design pattern. It provides a robust, scalable, and data-driven approach to web testing, complete with detailed HTML reporting and CI/CD integration via GitHub Actions.

## Key Features

- **Page Object Model (POM)**: Clean separation of page interactions and test logic via encapsulated page classes (`LoginPage`, `DropdownPage`, `AlertsPage`, `DynamicPage`).
- **TestNG Integration**: Leverages TestNG for powerful test execution, prioritizing, grouping, and parallel execution capabilities.
- **Data-Driven Testing**: Uses `@DataProvider` to run scenarios against multiple datasets automatically.
- **Advanced Wait Strategies**: Implements and demonstrates the use of both Explicit Waits (`WebDriverWait`) and Fluent Waits to handle dynamic UI elements smoothly.
- **Automated Reporting**: Integrated with **ExtentReports 5** to automatically generate rich, dark-themed HTML reports, including automatic screenshot captures for any failed tests.
- **CI/CD Ready**: Includes a `.github/workflows/run-tests.yml` configuration to automatically run the full suite on Ubuntu runners upon every push to the `main` branch.

## Test Coverage

The suite consists of 8 main test cases covering a variety of common web interactions:
1. **TC001_ValidLogin**: Validates successful authentication and secure area redirection.
2. **TC002_InvalidLogin**: Verifies error messages for negative login attempts.
3. **TC003_SoftAssertions**: Validates multiple page attributes simultaneously using TestNG `SoftAssert`.
4. **TC004_DropdownSelection**: Demonstrates interaction with dropdowns using `selectByVisibleText`, `selectByValue`, and `selectByIndex`.
5. **TC005_AlertHandling**: Handles JavaScript Alerts, Confirms, and Prompts (accepting, dismissing, and sending keys).
6. **TC006_MultipleWindows**: Manages window switching, waiting for new pages to load, and verifying content across multiple tabs.
7. **TC007_WaitStrategies**: Tests dynamically loading elements that appear after a delay.
8. **TC008_DataDrivenLogin**: Executes multiple login combinations iteratively via a data provider.

## Running the Tests Locally

You can easily run the tests via Maven from the root directory:
```bash
mvn clean test
```

Alternatively, you can run the suite programmatically by executing the `main` method in `SeleniumTestSuite.java`. 

After execution, open the generated HTML report located at `test-reports/Report_[timestamp].html` to view the results.
