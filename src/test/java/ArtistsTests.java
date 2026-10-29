import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.List;

public class ArtistsTests extends BaseTest {

    @Test
    public void tc001ArtistsAreDisplayed() {
        LoginPage loginPage = new LoginPage(driver);
        loginPage.login(
                requiredEnv("KOEL_EMAIL"),
                requiredEnv("KOEL_PASSWORD")
        );

        ArtistsPage artistsPage = new ArtistsPage(driver);
        artistsPage.open();

        Assert.assertEquals(
                artistsPage.getHeading(),
                "Artists",
                "The Artists page should be displayed"
        );

        List<String> names = artistsPage.getVisibleArtistNames();

        Assert.assertTrue(
                names.size() >= 2,
                "Multiple artists should be displayed"
        );
        Assert.assertTrue(
                names.contains("AKMV-18"),
                "AKMV-18 should be visible"
        );
        Assert.assertTrue(
                names.contains("BoxCat Games"),
                "BoxCat Games should be visible"
        );
    }
}
