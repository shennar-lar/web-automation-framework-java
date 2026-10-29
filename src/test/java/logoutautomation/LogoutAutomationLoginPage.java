package logoutautomation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LogoutAutomationLoginPage {
    private static final By EMAIL_INPUT =
            By.cssSelector("[data-testid='login-form'] input[type='email']");
    private static final By PASSWORD_INPUT =
            By.cssSelector("[data-testid='login-form'] input[type='password']");
    private static final By LOGIN_BUTTON =
            By.cssSelector("[data-testid='login-form'] button[type='submit']");

    private final WebDriverWait wait;

    public LogoutAutomationLoginPage(WebDriver driver) {
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void login(String email, String password) {
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT))
                .sendKeys(email);
        wait.until(ExpectedConditions.visibilityOfElementLocated(PASSWORD_INPUT))
                .sendKeys(password);
        wait.until(ExpectedConditions.elementToBeClickable(LOGIN_BUTTON))
                .click();
    }

    public boolean isLoginPageDisplayed() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT))
                .isDisplayed();
    }
}
