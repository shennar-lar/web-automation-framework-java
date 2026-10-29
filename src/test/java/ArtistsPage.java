import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

public class ArtistsPage {
    private final WebDriver driver;
    private final WebDriverWait wait;

    private final By artistsLink =
            By.cssSelector("a[href='#!/artists']");
    private final By heading =
            By.xpath("//h1[normalize-space()='Artists']");

    private final By artistNames =
            By.cssSelector("a.name[href^='#!/artist/']");

    public ArtistsPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, Duration.ofSeconds(15));
    }

    public void open() {
        wait.until(ExpectedConditions.elementToBeClickable(artistsLink))
                .click();
        wait.until(ExpectedConditions.visibilityOfElementLocated(heading));
    }

    public String getHeading() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(heading))
                .getText().trim();
    }

    public List<String> getVisibleArtistNames() {
        return wait.until(d -> {
            List<String> names = new ArrayList<>();

            for (WebElement element : d.findElements(artistNames)) {
                if (element.isDisplayed()) {
                    String name = element.getText().trim();
                    if (!name.isEmpty()) {
                        names.add(name);
                    }
                }
            }

            return names.size() >= 2 ? names : null;
        });
    }
}