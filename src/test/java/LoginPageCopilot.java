import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public class LoginPageCopilot {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By emailInput =
            By.cssSelector("[data-testid='login-form'] input[type='email']");
    private final By passwordInput =
            By.cssSelector("[data-testid='login-form'] input[type='password']");
    private final By loginButton =
            By.cssSelector("[data-testid='login-form'] button[type='submit']");
    private final By loginErrorMessage = By.xpath(
            "//*[normalize-space(.)='Email format is incorrect' "
                    + "and not(.//*[normalize-space(.)='Email format is incorrect'])]"
                    + " | "
                    + "//*[normalize-space(.)=\"Couldn't log you in\" "
                    + "and not(.//*[normalize-space(.)=\"Couldn't log you in\"])]"
    );

    public LoginPageCopilot(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void login(String email, String password) {
        WebElement emailField =
                wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput));
        emailField.clear();
        emailField.sendKeys(email);

        WebElement passwordField =
                wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput));
        passwordField.clear();
        passwordField.sendKeys(password);

        submitLogin();
    }

    public void submitLoginWithEmptyFields() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput)).clear();
        wait.until(ExpectedConditions.visibilityOfElementLocated(passwordInput)).clear();
        submitLogin();
    }

    private void submitLogin() {
        wait.until(ExpectedConditions.elementToBeClickable(loginButton)).click();
    }

    public String getLoginErrorMessage() {
        return wait.until(d -> {
            List<WebElement> messages = d.findElements(loginErrorMessage);

            for (WebElement message : messages) {
                if (message.isDisplayed()) {
                    return message.getText().trim();
                }
            }

            return null;
        });
    }
}
