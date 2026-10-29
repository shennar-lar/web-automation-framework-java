package logoutautomation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LogoutAutomationProfilePage {
    private static final By PROFILE_CONTROL =
            By.cssSelector("[data-testid='view-profile-link']");
    private static final By EMAIL_INPUT =
            By.cssSelector("#inputProfileEmail");
    private static final By CURRENT_PASSWORD_INPUT =
            By.cssSelector("#inputProfileCurrentPassword");
    private static final By NEW_PASSWORD_INPUT =
            By.cssSelector("#inputProfileNewPassword");
    private static final By SAVE_BUTTON =
            By.cssSelector("#profileWrapper > div > form > div:nth-child(5) > button");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LogoutAutomationProfilePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void open() {
        wait.until(ExpectedConditions.elementToBeClickable(PROFILE_CONTROL)).click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT));
    }

    public String getEmailAddress() {
        WebElement email = wait.until(
                ExpectedConditions.visibilityOfElementLocated(EMAIL_INPUT)
        );
        return email.getAttribute("value");
    }

    public void updatePassword(String currentPassword, String newPassword) {
        WebElement currentPasswordInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(CURRENT_PASSWORD_INPUT)
        );
        currentPasswordInput.clear();
        currentPasswordInput.sendKeys(currentPassword);

        WebElement newPasswordInput = wait.until(
                ExpectedConditions.visibilityOfElementLocated(NEW_PASSWORD_INPUT)
        );
        newPasswordInput.clear();
        newPasswordInput.sendKeys(newPassword);

        wait.until(ExpectedConditions.elementToBeClickable(SAVE_BUTTON)).click();
    }
}
