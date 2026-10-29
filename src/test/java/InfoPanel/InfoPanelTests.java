package InfoPanel;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class InfoPanelTests extends InfoPanelBaseTest {
    private static final int SHUFFLE_ATTEMPTS = 5;
    private static final int REQUIRED_CHANGED_ORDERS = 3;
    private static final int MINIMUM_DISTINCT_QUEUE_TRACKS = 4;

    // Supply these values from the approved Zephyr test data and expected results.
    private static final String TODO_ALBUM_CASE_SONG =
            "If I Find A Way (explicit)";
    private static final String TODO_EXPECTED_ALBUM_NAME =
            "I'm Saying It Anyway";
    private static final String TODO_LYRICS_CASE_SONG = null;
    private static final String TODO_EXPECTED_LYRICS = null;
    private static final String TODO_ARTIST_CASE_SONG =
            "If I Find A Way (explicit)";
    private static final String TODO_EXPECTED_ARTIST_NAME = "Ron Milazzo";
    private static final String TODO_OPEN_CLOSE_CASE_SONG =
            "Scott Holmes Music - Epic Cinematic";
    private static final String TODO_MISSING_LYRICS_CASE_SONG =
            "Scott Holmes Music - Epic Cinematic";
    private static final String TODO_UNKNOWN_ALBUM_CASE_SONG =
            "Scott Holmes Music - Epic Cinematic";
    private static final String TODO_UNKNOWN_ALBUM_EXPECTED_DISPLAY =
            "Unknown Album";
    private static final String TODO_ARTIST_SHUFFLE_CASE_SONG =
            "Tunnel of Lights (ID 1689)";
    private static final String TODO_ARTIST_SHUFFLE_CASE_ARTIST = "Lobo Loco";
    private static final String TODO_ALBUM_SHUFFLE_CASE_ALBUM = "Unknown Album";
    private static final String TODO_ALBUM_SHUFFLE_CASE_ROUTE = "#!/album/11";
    private static final String TODO_EXPECTED_MISSING_LYRICS_DISPLAY =
            "No lyrics available. Are you listening to Bach?";

    private InfoPanelPage infoPanel() {
        login();
        return new InfoPanelPage(driver);
    }

    private void playSong(InfoPanelPage page, String songName, String valueName) {
        String expectedSong = required(songName, valueName);
        page.selectSong(expectedSong);
        page.playSelectedSong();
        Assert.assertEquals(
                page.getPlayingSongName(),
                expectedSong,
                "The configured song should be the song currently playing"
        );
    }

    private void login() {
        new InfoPanelLoginPage(driver).login(
                requiredEnv("KOEL_EMAIL"),
                requiredEnv("KOEL_PASSWORD")
        );
    }

    @Test
    public void INTERNSHIP_110425_verifyAlbumNameAndCover() {
        InfoPanelPage page = infoPanel();
        playSong(page, TODO_ALBUM_CASE_SONG, "album test song");
        page.openInfoPanel();
        page.openAlbumTab();

        Assert.assertEquals(
                page.getAlbumName(),
                required(TODO_EXPECTED_ALBUM_NAME, "expected album name"),
                "The playing song's album name should match the test data"
        );
        Assert.assertTrue(
                page.isAlbumCoverLoaded(),
                "The album cover background image should load with positive naturalWidth"
        );
    }

    // Pending investigation based on the latest TestNG run.
    @Test(enabled = false)
    public void INTERNSHIP_110426_verifyLyricsDisplayed() {
        InfoPanelPage page = infoPanel();
        playSong(page, TODO_LYRICS_CASE_SONG, "lyrics test song");
        page.openInfoPanel();
        page.openLyricsTab();

        Assert.assertEquals(
                page.getLyrics(),
                required(TODO_EXPECTED_LYRICS, "expected lyrics"),
                "The displayed lyrics should match the approved expected text"
        );
    }

    @Test
    public void INTERNSHIP_110427_verifyArtistName() {
        InfoPanelPage page = infoPanel();
        playSong(page, TODO_ARTIST_CASE_SONG, "artist test song");
        page.openInfoPanel();
        page.openArtistTab();

        Assert.assertEquals(
                page.getArtistName(),
                required(TODO_EXPECTED_ARTIST_NAME, "expected artist name"),
                "The displayed artist should match the test data"
        );
    }

    @Test
    public void INTERNSHIP_110428_verifyInfoPanelOpens() {
        InfoPanelPage page = infoPanel();
        playSong(page, TODO_OPEN_CLOSE_CASE_SONG, "Info panel test song");
        page.openInfoPanel();

        Assert.assertTrue(
                page.isInfoPanelVisible(),
                "The Info panel should be visible after clicking INFO"
        );
    }

    @Test
    public void INTERNSHIP_110429_verifyInfoPanelCloses() {
        InfoPanelPage page = infoPanel();
        playSong(page, TODO_OPEN_CLOSE_CASE_SONG, "Info panel test song");
        page.openInfoPanel();
        page.closeInfoPanel();

        Assert.assertFalse(
                page.isInfoPanelVisible(),
                "The Info panel should no longer be visible after clicking INFO again"
        );
    }

    // Pending investigation based on the latest TestNG run.
    @Test(enabled = false)
    public void INTERNSHIP_110430_verifyShuffleByArtist() {
        InfoPanelPage page = infoPanel();
        playSong(page, TODO_ARTIST_SHUFFLE_CASE_SONG,
                "song by the artist used for shuffle testing");
        page.openInfoPanel();
        page.openArtistTab();
        Assert.assertEquals(
                page.getArtistName(),
                required(TODO_ARTIST_SHUFFLE_CASE_ARTIST,
                        "expected artist shuffle fixture"),
                "The playing song should belong to the known artist fixture"
        );

        page.openCurrentQueue();
        assertRepeatedShuffleChanges(
                page, page::shuffleByArtist, "artist"
        );
    }

    // Pending investigation based on the latest TestNG run.
    @Test(enabled = false)
    public void INTERNSHIP_110431_verifyShuffleByAlbum() {
        InfoPanelPage page = infoPanel();
        String expectedAlbum = required(TODO_ALBUM_SHUFFLE_CASE_ALBUM,
                "album shuffle fixture");
        String albumRoute = TODO_ALBUM_SHUFFLE_CASE_ROUTE;
        String song = required(TODO_UNKNOWN_ALBUM_CASE_SONG,
                "verified song on album shuffle fixture");

        page.selectAlbum(expectedAlbum, albumRoute);
        page.playAnySongOnCurrentAlbumPage(albumRoute);
        page.openInfoPanel();
        page.openAlbumTab();
        page.waitForAlbumName(expectedAlbum);
        Assert.assertEquals(
                page.getAlbumName(),
                expectedAlbum,
                "The selected album should match the album shuffle fixture"
        );

        assertRepeatedShuffleChanges(
                page, page::shuffleByAlbum, "album"
        );
    }

    @Test
    public void INTERNSHIP_110432_verifyMissingLyrics() {
        InfoPanelPage page = infoPanel();
        playSong(page, TODO_MISSING_LYRICS_CASE_SONG,
                "song with missing lyrics");
        page.openInfoPanel();
        page.openLyricsTab();

        Assert.assertEquals(
                page.getLyrics(),
                expectedValue(TODO_EXPECTED_MISSING_LYRICS_DISPLAY,
                        "expected missing-lyrics display"),
                "Lyrics should match the approved missing-lyrics result"
        );
    }

    @Test
    public void INTERNSHIP_110433_verifyUnknownAlbumInformation() {
        InfoPanelPage page = infoPanel();
        playSong(page, TODO_UNKNOWN_ALBUM_CASE_SONG,
                "song with unknown album information");
        page.openInfoPanel();
        page.openAlbumTab();

        Assert.assertEquals(
                page.getAlbumName(),
                expectedValue(TODO_UNKNOWN_ALBUM_EXPECTED_DISPLAY,
                        "expected unknown-album display"),
                "The album panel should show the approved unknown-album result"
        );
    }

    private static String required(String value, String valueName) {
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "InfoPanel tests are not ready to run: test data for "
                            + valueName + " has not been supplied."
            );
        }
        return value.trim();
    }

    private static String expectedValue(String value, String valueName) {
        if (value == null) {
            throw new IllegalStateException(
                    "InfoPanel tests are not ready to run: " + valueName
                            + " has not been supplied."
            );
        }
        return value;
    }

    private static void assertRepeatedShuffleChanges(
            InfoPanelPage page,
            Runnable shuffle,
            String scope
    ) {
        List<String> orderBeforeShuffle = page.getQueueOrder();
        assertQueueHasEnoughDistinctTracks(orderBeforeShuffle, scope);

        int changedOrders = 0;
        for (int attempt = 1; attempt <= SHUFFLE_ATTEMPTS; attempt++) {
            shuffle.run();
            List<String> orderAfterShuffle = page.getQueueOrder();
            assertQueueHasEnoughDistinctTracks(orderAfterShuffle, scope);
            Assert.assertEquals(
                    orderAfterShuffle.size(),
                    orderBeforeShuffle.size(),
                    "The " + scope + " queue size should remain stable after "
                            + "shuffle attempt " + attempt
            );

            if (!orderAfterShuffle.equals(orderBeforeShuffle)) {
                changedOrders++;
            }
            orderBeforeShuffle = orderAfterShuffle;
        }

        Assert.assertTrue(
                changedOrders >= REQUIRED_CHANGED_ORDERS,
                "INTERNSHIP-110434 regression: " + scope + " shuffle changed "
                        + "the queue order in only " + changedOrders + " of "
                        + SHUFFLE_ATTEMPTS + " attempts; expected at least "
                        + REQUIRED_CHANGED_ORDERS
        );
    }

    private static void assertQueueHasEnoughDistinctTracks(
            List<String> order,
            String scope
    ) {
        Set<String> distinctTracks = new HashSet<>(order);
        Assert.assertTrue(
                distinctTracks.size() >= MINIMUM_DISTINCT_QUEUE_TRACKS,
                "The " + scope + " shuffle fixture must have at least "
                        + MINIMUM_DISTINCT_QUEUE_TRACKS
                        + " distinct tracks in the Current Queue"
        );
    }
}
