import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By emailInput =
            By.cssSelector("[data-testid='login-form'] input[type='email']");
    private final By passwordInput =
            By.cssSelector("[data-testid='login-form'] input[type='password']");
    private final By loginButton =
            By.cssSelector("[data-testid='login-form'] button[type='submit']");

    public LoginPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void login(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput))
                .sendKeys(email);
        driver.findElement(passwordInput).sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(loginButton))
                .click();
    }
}