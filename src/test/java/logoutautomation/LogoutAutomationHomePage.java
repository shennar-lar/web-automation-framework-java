package logoutautomation;

import org.openqa.selenium.By;
import org.openqa.selenium.Rectangle;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LogoutAutomationHomePage {
    private static final String HOME_URL = "https://qa.koel.app/#!/home";
    private static final By PROFILE_CONTROL =
            By.cssSelector("[data-testid='view-profile-link']");
    private static final By LOGOUT_CONTROL =
            By.cssSelector("[data-testid='btn-logout']");

    private final WebDriver driver;
    private final WebDriverWait wait;

    public LogoutAutomationHomePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void open() {
        driver.get(HOME_URL);
        waitForLogoutControl();
    }

    public WebElement waitForProfileControl() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(PROFILE_CONTROL));
    }

    public WebElement waitForLogoutControl() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(LOGOUT_CONTROL));
    }

    public String getLogoutTitle() {
        return waitForLogoutControl().getAttribute("title");
    }

    public boolean isLogoutToRightOfProfileAndOnSameRow() {
        Rectangle profile = waitForProfileControl().getRect();
        Rectangle logout = waitForLogoutControl().getRect();

        boolean isToRight = logout.getX() >= profile.getX() + profile.getWidth();
        boolean overlapsVertically =
                logout.getY() < profile.getY() + profile.getHeight()
                        && profile.getY() < logout.getY() + logout.getHeight();
        return isToRight && overlapsVertically;
    }

    public void clickLogout() {
        wait.until(ExpectedConditions.elementToBeClickable(LOGOUT_CONTROL)).click();
    }
}
