import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.testng.Assert;
import org.testng.annotations.Test;

public class LoginValidationCopilotTests extends BaseTest {

    @Test
    public void tcLogin003_invalidEmailFormatShowsError() {
        String loginUrl = driver.getCurrentUrl();
        LoginPageCopilot loginPage = new LoginPageCopilot(driver);
        loginPage.login(
                "invalid-email",
                requiredEnv("KOEL_PASSWORD")
        );

        WebElement emailField = driver.findElement(
                By.cssSelector("[data-testid='login-form'] input[type='email']")
        );
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;

        Object emailIsValid = javascriptExecutor.executeScript(
                "return arguments[0].validity.valid;",
                emailField
        );
        String validationMessage = (String) javascriptExecutor.executeScript(
                "return arguments[0].validationMessage;",
                emailField
        );

        Assert.assertEquals(
                emailIsValid,
                Boolean.FALSE,
                "Chrome should consider the invalid email format invalid"
        );
        Assert.assertNotNull(validationMessage);
        Assert.assertFalse(
                validationMessage.trim().isEmpty(),
                "Chrome should provide a native validation message"
        );
        Assert.assertEquals(
                driver.getCurrentUrl(),
                loginUrl,
                "The user should remain on the login page"
        );
    }

    @Test
    public void tcLogin004_invalidPasswordShowsError() {
        LoginPageCopilot loginPage = new LoginPageCopilot(driver);
        loginPage.login(
                requiredEnv("KOEL_EMAIL"),
                requiredEnv("KOEL_INVALID_PASSWORD")
        );

        Assert.assertEquals(
                loginPage.getLoginErrorMessage(),
                "Couldn't log you in"
        );
    }

    @Test
    public void tcLogin005_emptyFieldsShowError() {
        String loginUrl = driver.getCurrentUrl();
        LoginPageCopilot loginPage = new LoginPageCopilot(driver);
        loginPage.submitLoginWithEmptyFields();

        WebElement emailField = driver.findElement(
                By.cssSelector("[data-testid='login-form'] input[type='email']")
        );
        JavascriptExecutor javascriptExecutor = (JavascriptExecutor) driver;

        Object emailValueMissing = javascriptExecutor.executeScript(
                "return arguments[0].validity.valueMissing;",
                emailField
        );
        String validationMessage = (String) javascriptExecutor.executeScript(
                "return arguments[0].validationMessage;",
                emailField
        );

        Assert.assertEquals(
                emailValueMissing,
                Boolean.TRUE,
                "Chrome should report the empty email field as missing"
        );
        Assert.assertNotNull(validationMessage);
        Assert.assertFalse(
                validationMessage.trim().isEmpty(),
                "Chrome should provide a native validation message"
        );
        Assert.assertEquals(
                driver.getCurrentUrl(),
                loginUrl,
                "The user should remain on the login page"
        );
    }
}
