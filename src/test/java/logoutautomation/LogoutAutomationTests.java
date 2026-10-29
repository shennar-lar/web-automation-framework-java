package logoutautomation;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.Assert;
import org.testng.annotations.Test;

import java.time.Duration;
import java.util.List;

public class LogoutAutomationTests extends LogoutAutomationBaseTest {
    private static final By LOGOUT_CONTROL =
            By.cssSelector("[data-testid='btn-logout']");
    private static final By LOGIN_ERROR_MESSAGE = By.xpath(
            "//*[normalize-space(.)='Email format is incorrect' "
                    + "and not(.//*[normalize-space(.)='Email format is incorrect'])]"
                    + " | "
                    + "//*[normalize-space(.)=\"Couldn't log you in\" "
                    + "and not(.//*[normalize-space(.)=\"Couldn't log you in\"])]"
    );

    @Test
    public void tcLogout001_logoutButtonIsVisibleBesideProfile() {
        LogoutAutomationLoginPage loginPage = new LogoutAutomationLoginPage(driver);
        loginPage.login(requiredEnv("KOEL_EMAIL"), requiredEnv("KOEL_PASSWORD"));

        LogoutAutomationHomePage homePage = new LogoutAutomationHomePage(driver);
        homePage.waitForLogoutControl();
        homePage.open();

        Assert.assertTrue(
                homePage.waitForProfileControl().isDisplayed(),
                "Profile control should be visible on Home"
        );
        Assert.assertTrue(
                homePage.waitForLogoutControl().isDisplayed(),
                "Logout control should be visible on Home"
        );
        Assert.assertTrue(
                homePage.isLogoutToRightOfProfileAndOnSameRow(),
                "Logout should be to the right of Profile on the same row"
        );
        Assert.assertEquals(
                homePage.getLogoutTitle(),
                "Log student out",
                "Logout should have the expected title"
        );
    }

    @Test
    public void tcLogout002_successfulLogoutReturnsToLoginPage() {
        LogoutAutomationLoginPage loginPage = new LogoutAutomationLoginPage(driver);
        loginPage.login(requiredEnv("KOEL_EMAIL"), requiredEnv("KOEL_PASSWORD"));

        LogoutAutomationHomePage homePage = new LogoutAutomationHomePage(driver);
        homePage.waitForLogoutControl();
        homePage.open();
        Assert.assertTrue(
                homePage.waitForLogoutControl().isDisplayed(),
                "Visible Logout control confirms the user is authenticated"
        );

        homePage.clickLogout();
        Assert.assertTrue(
                loginPage.isLoginPageDisplayed(),
                "Login page should be displayed after logout"
        );
    }

    @Test(
            description = "Changes the password for the explicitly approved test account, verifies the new password, logs out, and restores the original password."
    )
    public void tcLogout005_logoutAfterPasswordUpdate() throws Throwable {
        String email = requiredEnv("KOEL_EMAIL");
        String originalPassword = requiredEnv("KOEL_PASSWORD");
        String newPassword = requiredEnv("KOEL_NEW_PASSWORD");
        Assert.assertNotEquals(
                newPassword,
                originalPassword,
                "The replacement password must differ from the original password"
        );

        boolean passwordUpdateAttempted = false;
        Throwable testFailure = null;
        try {
            LogoutAutomationLoginPage loginPage = new LogoutAutomationLoginPage(driver);
            loginPage.login(email, originalPassword);
            waitForAuthenticatedSession(driver);

            LogoutAutomationProfilePage profilePage =
                    new LogoutAutomationProfilePage(driver);
            profilePage.open();
            String originalEmail = profilePage.getEmailAddress();
            Assert.assertNotNull(originalEmail, "Profile email should be present");
            Assert.assertFalse(originalEmail.trim().isEmpty(), "Profile email should not be empty");

            passwordUpdateAttempted = true;
            profilePage.updatePassword(originalPassword, newPassword);

            ensureLoginPage(loginPage);
            Assert.assertTrue(
                    tryLoginAndWaitForAuthentication(email, newPassword),
                    "The new password should authenticate successfully"
            );

            profilePage.open();
            Assert.assertEquals(
                    profilePage.getEmailAddress(),
                    originalEmail,
                    "The email address should remain unchanged after the password update"
            );

            LogoutAutomationHomePage homePage = new LogoutAutomationHomePage(driver);
            homePage.open();
            homePage.clickLogout();
            Assert.assertTrue(
                    loginPage.isLoginPageDisplayed(),
                    "Login page should be displayed after logout"
            );
        } catch (Throwable failure) {
            testFailure = failure;
            throw failure;
        } finally {
            if (passwordUpdateAttempted) {
                try {
                    restoreOriginalPassword(email, originalPassword, newPassword);
                } catch (Throwable recoveryFailure) {
                    if (testFailure != null) {
                        testFailure.addSuppressed(recoveryFailure);
                    } else {
                        throw recoveryFailure;
                    }
                }
            }
        }
    }

    private void restoreOriginalPassword(
            String email,
            String originalPassword,
            String newPassword
    ) {
        LogoutAutomationLoginPage loginPage = new LogoutAutomationLoginPage(driver);
        ensureLoginPage(loginPage);

        if (!tryLoginAndWaitForAuthentication(email, newPassword)) {
            Assert.assertTrue(
                    tryLoginAndWaitForAuthentication(email, originalPassword),
                    "Recovery could not authenticate using either approved password; "
                            + "manual account recovery is required"
            );
            logoutAndVerifyLoginPage(loginPage);
            return;
        }

        LogoutAutomationProfilePage profilePage =
                new LogoutAutomationProfilePage(driver);
        profilePage.open();
        profilePage.updatePassword(newPassword, originalPassword);

        ensureLoginPage(loginPage);
        Assert.assertTrue(
                tryLoginAndWaitForAuthentication(email, originalPassword),
                "The original password should authenticate after restoration"
        );
        logoutAndVerifyLoginPage(loginPage);
    }

    private void ensureLoginPage(LogoutAutomationLoginPage loginPage) {
        List<WebElement> logoutControls = driver.findElements(LOGOUT_CONTROL);
        if (!logoutControls.isEmpty() && logoutControls.get(0).isDisplayed()) {
            new LogoutAutomationHomePage(driver).clickLogout();
        }
        Assert.assertTrue(
                loginPage.isLoginPageDisplayed(),
                "Login page should be displayed before authenticating"
        );
    }

    private boolean tryLoginAndWaitForAuthentication(String email, String password) {
        LogoutAutomationLoginPage loginPage = new LogoutAutomationLoginPage(driver);
        Assert.assertTrue(
                loginPage.isLoginPageDisplayed(),
                "Login page should be displayed before submitting credentials"
        );
        loginPage.login(email, password);

        return new WebDriverWait(driver, Duration.ofSeconds(15)).until(d -> {
            for (WebElement logoutControl : d.findElements(LOGOUT_CONTROL)) {
                if (logoutControl.isDisplayed()) {
                    return true;
                }
            }
            for (WebElement error : d.findElements(LOGIN_ERROR_MESSAGE)) {
                if (error.isDisplayed()) {
                    return false;
                }
            }
            return null;
        });
    }

    private void waitForAuthenticatedSession(WebDriver webDriver) {
        new WebDriverWait(webDriver, Duration.ofSeconds(15))
                .until(d -> d.findElements(LOGOUT_CONTROL).stream()
                        .anyMatch(WebElement::isDisplayed));
    }

    private void logoutAndVerifyLoginPage(LogoutAutomationLoginPage loginPage) {
        new LogoutAutomationHomePage(driver).open();
        new LogoutAutomationHomePage(driver).clickLogout();
        Assert.assertTrue(
                loginPage.isLoginPageDisplayed(),
                "Login page should be displayed after logout"
        );
    }
}
