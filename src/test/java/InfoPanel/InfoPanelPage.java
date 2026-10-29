package InfoPanel;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.TimeoutException;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.interactions.WheelInput.ScrollOrigin;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;

public class InfoPanelPage {
    private static final Duration WAIT = Duration.ofSeconds(8);
    private static final Duration QUEUE_UPDATE_WAIT = Duration.ofSeconds(5);
    private static final Duration QUEUE_STABLE_WINDOW = Duration.ofMillis(200);
    private static final Duration QUEUE_POLL_INTERVAL = Duration.ofMillis(200);

    // Replace each null with the verified locator. Keeping these together makes
    // unresolved UI details easy to identify without introducing fake selectors.
    private static final By TODO_INFO_BUTTON_LOCATOR =
            By.cssSelector("button[data-testid='toggle-extra-panel-btn']");
    private static final By TODO_INFO_PANEL_LOCATOR =
            By.cssSelector("section#extra[data-testid='extra-panel']");
    private static final By TODO_LYRICS_TAB_LOCATOR = By.id("extraTabLyrics");
    private static final By TODO_LYRICS_CONTENT_LOCATOR =
            By.cssSelector("#lyrics .content");
    private static final By TODO_ARTIST_TAB_LOCATOR = By.id("extraTabArtist");
    private static final By TODO_ARTIST_NAME_LOCATOR =
            By.cssSelector("#extraPanelArtist h1.name span");
    private static final By TODO_ALBUM_TAB_LOCATOR = By.id("extraTabAlbum");
    private static final By TODO_ALBUM_NAME_LOCATOR =
            By.cssSelector("#extraPanelAlbum h1.name span");
    private static final By TODO_ALBUM_COVER_LOCATOR =
            By.cssSelector("#extraPanelAlbum span.cover");
    private static final By TODO_SONG_BY_NAME_LOCATOR =
            By.cssSelector("#songsWrapper tr.song-item");
    private static final By ALBUM_PAGE_SONG_ROWS_LOCATOR =
            By.cssSelector("table.items tr.song-item");
    private static final By ALL_SONGS_LINK_LOCATOR =
            By.cssSelector("a[href='#!/songs']");
    private static final By SONGS_WRAPPER_LOCATOR =
            By.cssSelector("#songsWrapper");
    private static final By CURRENT_QUEUE_LINK_LOCATOR =
            By.cssSelector("a[href='#!/queue']");
    private static final By ALBUMS_LINK_LOCATOR =
            By.cssSelector("a[href='#!/albums']");
    private static final By ALBUMS_WRAPPER_LOCATOR =
            By.cssSelector("section#albumsWrapper");
    private static final By ALBUMS_SCROLL_CONTAINER_LOCATOR =
            By.cssSelector("#albumsWrapper .main-scroll-wrap");
    private static final Function<String, By> TODO_ARTIST_BY_NAME_LOCATOR = null;
    private static final By TODO_ALBUM_BY_NAME_LOCATOR =
            By.cssSelector("a[href^='#!/album/']");
    private static final By TODO_PLAYING_SONG_LOCATOR =
            By.cssSelector("#progressPane h3.title");
    private static final By TODO_ARTIST_SHUFFLE_BUTTON_LOCATOR =
            By.cssSelector("#extraPanelArtist button.shuffle.control");
    private static final By TODO_ALBUM_SHUFFLE_BUTTON_LOCATOR =
            By.cssSelector("#extraPanelAlbum button.shuffle.control");
    private static final By TODO_QUEUE_TRACKS_LOCATOR =
            By.cssSelector("#queueWrapper tr.song-item");
    private static final By QUEUE_TRACK_TITLE_LOCATOR =
            By.cssSelector("td.title");

    private final WebDriver driver;
    private final WebDriverWait wait;
    private WebElement selectedSongRow;
    private String selectedSongTitle;

    public InfoPanelPage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, WAIT);
    }

    public void openInfoPanel() {
        logWorkflowDiagnostic("openInfoPanel: start");
        By panel = locator(TODO_INFO_PANEL_LOCATOR, "TODO_INFO_PANEL_LOCATOR");
        if (!isInfoPanelOpen()) {
            WebElement infoButton = wait.until(
                    ExpectedConditions.elementToBeClickable(TODO_INFO_BUTTON_LOCATOR)
            );
            infoButton.click();
        }
        wait.until(d -> isInfoPanelOpen());
        logWorkflowDiagnostic("openInfoPanel: success");
    }

    public void closeInfoPanel() {
        if (isInfoPanelOpen()) {
            click(TODO_INFO_BUTTON_LOCATOR, "TODO_INFO_BUTTON_LOCATOR");
        }
        wait.until(d -> !isInfoPanelOpen());
    }

    public boolean isInfoPanelVisible() {
        return isInfoPanelOpen();
    }

    private boolean isInfoPanelOpen() {
        By panel = locator(TODO_INFO_PANEL_LOCATOR, "TODO_INFO_PANEL_LOCATOR");
        List<WebElement> panels = driver.findElements(panel);
        return panels.stream().anyMatch(infoPanel ->
                infoPanel.isDisplayed()
                        && hasClass(infoPanel.getAttribute("class"), "showing")
        );
    }

    public void openLyricsTab() {
        clickAndWaitForSelectedTab(TODO_LYRICS_TAB_LOCATOR,
                "TODO_LYRICS_TAB_LOCATOR");
    }

    public String getLyrics() {
        return visibleText(TODO_LYRICS_CONTENT_LOCATOR, "TODO_LYRICS_CONTENT_LOCATOR");
    }

    public void openArtistTab() {
        clickAndWaitForSelectedTab(TODO_ARTIST_TAB_LOCATOR,
                "TODO_ARTIST_TAB_LOCATOR");
    }

    public String getArtistName() {
        return visibleText(TODO_ARTIST_NAME_LOCATOR, "TODO_ARTIST_NAME_LOCATOR");
    }

    public void openAlbumTab() {
        clickAndWaitForSelectedTab(TODO_ALBUM_TAB_LOCATOR,
                "TODO_ALBUM_TAB_LOCATOR");
    }

    public void waitForAlbumName(String expectedAlbumName) {
        String expectedName = requiredValue(expectedAlbumName, "album name");
        By albumName = locator(TODO_ALBUM_NAME_LOCATOR,
                "TODO_ALBUM_NAME_LOCATOR");
        wait.until(ExpectedConditions.visibilityOfElementLocated(albumName));
        wait.until(ExpectedConditions.textToBePresentInElementLocated(
                albumName, expectedName
        ));
    }

    public String getAlbumName() {
        return visibleText(TODO_ALBUM_NAME_LOCATOR, "TODO_ALBUM_NAME_LOCATOR");
    }

    public void openAlbumPageFromInfoPanel(
            String expectedAlbumName,
            String albumRoute
    ) {
        String expectedName = requiredValue(expectedAlbumName, "album name");
        String expectedRoute = requiredValue(albumRoute, "album route");
        waitForAlbumName(expectedName);
        WebElement albumName = wait.until(ExpectedConditions.elementToBeClickable(
                locator(TODO_ALBUM_NAME_LOCATOR, "TODO_ALBUM_NAME_LOCATOR")
        ));
        albumName.click();
        wait.until(d -> {
            String currentUrl = d.getCurrentUrl();
            int fragmentStart = currentUrl.indexOf('#');
            return fragmentStart >= 0
                    && expectedRoute.equals(currentUrl.substring(fragmentStart));
        });
    }

    public String getAlbumCoverSource() {
        WebElement cover = wait.until(ExpectedConditions.visibilityOfElementLocated(
                locator(TODO_ALBUM_COVER_LOCATOR, "TODO_ALBUM_COVER_LOCATOR")
        ));
        return cover.getCssValue("background-image");
    }

    public boolean isAlbumCoverVisible() {
        By coverLocator = locator(
                TODO_ALBUM_COVER_LOCATOR, "TODO_ALBUM_COVER_LOCATOR"
        );
        return wait.until(d -> {
            WebElement cover = d.findElement(coverLocator);
            String backgroundImage = cover.getCssValue("background-image");
            return cover.isDisplayed()
                    && backgroundImage != null
                    && backgroundImage.startsWith("url(")
                    && !backgroundImage.equals("none");
        });
    }

    public boolean isAlbumCoverLoaded() {
        By coverLocator = locator(
                TODO_ALBUM_COVER_LOCATOR, "TODO_ALBUM_COVER_LOCATOR"
        );
        WebElement cover = wait.until(
                ExpectedConditions.visibilityOfElementLocated(coverLocator)
        );
        String backgroundImage = cover.getCssValue("background-image");
        if (backgroundImage == null || !backgroundImage.contains("url(")) {
            return false;
        }

        driver.manage().timeouts().scriptTimeout(WAIT);
        Object loaded = ((JavascriptExecutor) driver).executeAsyncScript(
                "const backgroundImage = arguments[0];"
                        + "const done = arguments[arguments.length - 1];"
                        + "const match = backgroundImage.match(/url\\([\"']?(.*?)[\"']?\\)/);"
                        + "if (!match) { done(false); return; }"
                        + "const image = new Image();"
                        + "let settled = false;"
                        + "const finish = value => {"
                        + "  if (!settled) { settled = true; done(value); }"
                        + "};"
                        + "image.onload = () => finish(image.naturalWidth > 0);"
                        + "image.onerror = () => finish(false);"
                        + "image.src = match[1];"
                        + "if (image.complete) finish(image.naturalWidth > 0);",
                backgroundImage
        );
        return Boolean.TRUE.equals(loaded);
    }

    public void selectSong(String songName) {
        String expectedSong = requiredValue(songName, "song name");
        wait.until(ExpectedConditions.elementToBeClickable(ALL_SONGS_LINK_LOCATOR))
                .click();
        WebElement songsWrapper = wait.until(
                ExpectedConditions.visibilityOfElementLocated(SONGS_WRAPPER_LOCATOR)
        );
        By songRows = locator(TODO_SONG_BY_NAME_LOCATOR,
                "TODO_SONG_BY_NAME_LOCATOR");
        By titleInRow = By.cssSelector("td.title");
        WebElement scrollElement = findSongsScrollElement(songsWrapper);
        scrollToTop(scrollElement);

        long previousScrollTop = -1;
        while (true) {
            selectedSongRow = findSongRow(songRows, titleInRow, expectedSong);
            if (selectedSongRow != null) {
                selectedSongTitle = expectedSong;
                return;
            }

            Number scrollTop = getScrollTop(scrollElement);
            Number clientHeight = getClientHeight(scrollElement);
            Number scrollHeight = getScrollHeight(scrollElement);
            long currentScrollTop = scrollTop.longValue();
            long maxScrollTop = Math.max(
                    0L, scrollHeight.longValue() - clientHeight.longValue()
            );
            if (currentScrollTop >= maxScrollTop
                    || currentScrollTop == previousScrollTop) {
                break;
            }
            previousScrollTop = currentScrollTop;
            long nextScrollTop = Math.min(
                    maxScrollTop,
                    currentScrollTop + Math.max(1L, clientHeight.longValue() * 4 / 5)
            );
            setScrollTop(scrollElement, nextScrollTop);

            wait.until(d -> getScrollTop(scrollElement).longValue() > currentScrollTop
                    || findSongRow(songRows, titleInRow, expectedSong) != null);
        }

        throw unresolved("Song was not found in All Songs: " + expectedSong);
    }

    public void selectArtist(String artistName) {
        clickNamed(TODO_ARTIST_BY_NAME_LOCATOR, "TODO_ARTIST_BY_NAME_LOCATOR",
                requiredValue(artistName, "artist name"));
    }

    public void selectAlbum(String albumName) {
        String expectedAlbum = requiredValue(albumName, "album name");
        By albums = locator(TODO_ALBUM_BY_NAME_LOCATOR,
                "TODO_ALBUM_BY_NAME_LOCATOR");
        WebElement albumLink = wait.until(d -> {
            for (WebElement link : d.findElements(albums)) {
                if (link.isDisplayed()
                        && expectedAlbum.equals(link.getText().trim())) {
                    return link;
                }
            }
            return null;
        });
        wait.until(ExpectedConditions.elementToBeClickable(albumLink)).click();
    }

    public void selectAlbum(String albumName, String albumRoute) {
        String expectedAlbum = requiredValue(albumName, "album name");
        String expectedRoute = requiredValue(albumRoute, "album route");
        WebElement albumsLink = waitForAlbumStep(
                "Albums sidebar link to become clickable",
                ExpectedConditions.elementToBeClickable(ALBUMS_LINK_LOCATOR)
        );
        clickAlbumStep("Albums sidebar", albumsLink);

        waitForAlbumStep(
                "Albums container to become visible",
                ExpectedConditions.visibilityOfElementLocated(ALBUMS_WRAPPER_LOCATOR)
        );
        By albumLinkLocator = By.cssSelector(
                "section#albumsWrapper a[href='" + expectedRoute + "']"
        );
        WebElement albumLink = scrollAlbumIntoContainer(
                albumLinkLocator, expectedRoute
        );
        if (!expectedAlbum.equals(albumLink.getText().trim())) {
            throw unresolved("Album link " + expectedRoute + " displayed unexpected "
                    + "name: " + albumLink.getText().trim());
        }
        System.out.println("[InfoPanel] selectAlbum: found " + expectedAlbum
                + " at " + expectedRoute);
        albumLink = waitForAlbumStep(
                "album link " + expectedRoute + " to become visible",
                ExpectedConditions.visibilityOfElementLocated(albumLinkLocator)
        );
        WebElement clickableAlbumLink = waitForAlbumStep(
                "album link " + expectedRoute + " to become clickable",
                ExpectedConditions.elementToBeClickable(albumLinkLocator)
        );
        clickAlbumStep("album link " + expectedRoute, clickableAlbumLink);

        waitForAlbumStep("navigation to " + expectedRoute,
                d -> {
                    String currentUrl = d.getCurrentUrl();
                    int fragmentStart = currentUrl.indexOf('#');
                    return fragmentStart >= 0
                            && expectedRoute.equals(currentUrl.substring(fragmentStart));
                });
    }

    private WebElement scrollAlbumIntoContainer(
            By albumLinkLocator,
            String albumRoute
    ) {
        WebElement scrollContainer = waitForAlbumStep(
                "Albums scroll container to become visible",
                ExpectedConditions.visibilityOfElementLocated(
                        ALBUMS_SCROLL_CONTAINER_LOCATOR
                )
        );
        JavascriptExecutor javascript = (JavascriptExecutor) driver;
        ScrollOrigin scrollOrigin = ScrollOrigin.fromElement(scrollContainer);
        System.out.println("[InfoPanel] selectAlbum: scrolling Albums container "
                + "#albumsWrapper .main-scroll-wrap");

        while (true) {
            List<WebElement> matchingLinks = driver.findElements(albumLinkLocator);
            System.out.println("[InfoPanel] selectAlbum: DOM lookup found "
                    + matchingLinks.size() + " matching link(s) for " + albumRoute);
            if (!matchingLinks.isEmpty()) {
                System.out.println("[InfoPanel] selectAlbum: album link found; "
                        + "bringing it into view");
                try {
                    new Actions(driver).moveToElement(matchingLinks.get(0)).perform();
                } catch (WebDriverException e) {
                    throw unresolved("Found " + albumRoute
                            + " but could not bring it into view: "
                            + e.getClass().getSimpleName() + ": " + e.getMessage());
                }
                if (!isWithinScrollContainer(
                        matchingLinks.get(0), scrollContainer
                )) {
                    throw unresolved("Found " + albumRoute
                            + " but Selenium did not bring it into the Albums "
                            + "container viewport.");
                }
                System.out.println("[InfoPanel] selectAlbum: album link brought "
                        + "into view");
                return matchingLinks.get(0);
            }

            Number scrollTop = (Number) javascript.executeScript(
                    "return arguments[0].scrollTop;", scrollContainer
            );
            Number clientHeight = (Number) javascript.executeScript(
                    "return arguments[0].clientHeight;", scrollContainer
            );
            double currentTop = scrollTop.doubleValue();
            Number scrollHeight = (Number) javascript.executeScript(
                    "return arguments[0].scrollHeight;", scrollContainer
            );
            double maxTop = Math.max(
                    0, scrollHeight.doubleValue() - clientHeight.doubleValue()
            );
            if (currentTop >= maxTop) {
                throw unresolved("Could not find " + albumRoute
                        + ": reached the end of the Albums container at scrollTop="
                        + currentTop + " with 0 matching links in the DOM.");
            }
            int scrollAmount = (int) Math.min(
                    Integer.MAX_VALUE,
                    Math.max(2000, clientHeight.doubleValue() * 3)
            );
            try {
                new Actions(driver)
                        .scrollFromOrigin(scrollOrigin, 0, scrollAmount)
                        .perform();
                System.out.println("[InfoPanel] selectAlbum: scrolled Albums "
                        + "container down by " + scrollAmount + "px");
            } catch (WebDriverException e) {
                throw unresolved("Failed scrolling Albums container toward "
                        + albumRoute + ": " + e.getClass().getSimpleName()
                        + ": " + e.getMessage());
            }

            try {
                new WebDriverWait(driver, Duration.ofMillis(800)).until(d -> {
                    List<WebElement> currentLinks = d.findElements(albumLinkLocator);
                    if (!currentLinks.isEmpty()) {
                        return true;
                    }
                    Number updatedTop = (Number) javascript.executeScript(
                            "return arguments[0].scrollTop;", scrollContainer
                    );
                    Number updatedHeight = (Number) javascript.executeScript(
                            "return arguments[0].scrollHeight;", scrollContainer
                    );
                    return updatedTop.doubleValue() > currentTop
                            || updatedHeight.doubleValue() > scrollHeight.doubleValue();
                });
            } catch (TimeoutException e) {
                Number finalTop = (Number) javascript.executeScript(
                        "return arguments[0].scrollTop;", scrollContainer
                );
                Number finalHeight = (Number) javascript.executeScript(
                        "return arguments[0].scrollHeight;", scrollContainer
                );
                List<WebElement> finalLinks = driver.findElements(albumLinkLocator);
                if (!finalLinks.isEmpty()) {
                    continue;
                }
                if (finalTop.doubleValue()
                        >= Math.max(0, finalHeight.doubleValue()
                                - clientHeight.doubleValue())) {
                    throw unresolved("Could not find " + albumRoute
                            + ": reached the end of the Albums container at scrollTop="
                            + finalTop.doubleValue() + " with "
                            + finalLinks.size() + " matching link(s) in the DOM.");
                }
                throw unresolved("Failed scrolling Albums container toward "
                        + albumRoute + ": scrollTop remained at "
                        + finalTop.doubleValue() + " of "
                        + Math.max(0, finalHeight.doubleValue()
                                - clientHeight.doubleValue())
                        + " after a " + scrollAmount + "px wheel action; "
                        + finalLinks.size() + " matching link(s) in the DOM.");
            }
        }
    }

    private boolean isWithinScrollContainer(
            WebElement element,
            WebElement scrollContainer
    ) {
        return Boolean.TRUE.equals(((JavascriptExecutor) driver).executeScript(
                "const container = arguments[0];"
                        + "const element = arguments[1];"
                        + "const containerRect = container.getBoundingClientRect();"
                        + "const elementRect = element.getBoundingClientRect();"
                        + "const top = containerRect.top + container.clientTop;"
                        + "const bottom = top + container.clientHeight;"
                        + "return elementRect.bottom > top && elementRect.top < bottom;",
                scrollContainer,
                element
        ));
    }

    private <T> T waitForAlbumStep(
            String step,
            Function<WebDriver, T> condition
    ) {
        System.out.println("[InfoPanel] selectAlbum: waiting for " + step);
        try {
            T result = wait.until(condition);
            System.out.println("[InfoPanel] selectAlbum: succeeded - " + step);
            return result;
        } catch (TimeoutException e) {
            System.out.println("[InfoPanel] selectAlbum: FAILED - " + step);
            throw e;
        }
    }

    private void clickAlbumStep(String step, WebElement element) {
        System.out.println("[InfoPanel] selectAlbum: clicking " + step);
        try {
            element.click();
            System.out.println("[InfoPanel] selectAlbum: succeeded - clicked " + step);
        } catch (WebDriverException e) {
            System.out.println("[InfoPanel] selectAlbum: FAILED - clicking " + step
                    + " (" + e.getClass().getSimpleName() + ")");
            throw e;
        }
    }

    public String getPlayingSongName() {
        return visibleText(TODO_PLAYING_SONG_LOCATOR, "TODO_PLAYING_SONG_LOCATOR");
    }

    public void playSelectedSong() {
        if (selectedSongRow == null || selectedSongTitle == null) {
            throw unresolved("Select a song before calling playSelectedSong().");
        }
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});",
                selectedSongRow
        );
        System.out.println("[InfoPanel] playSelectedSong: immediately before "
                + "Selenium double-click on row for " + selectedSongTitle);
        try {
            new Actions(driver).doubleClick(selectedSongRow).perform();
            System.out.println("[InfoPanel] playSelectedSong: Selenium "
                    + "double-click completed for " + selectedSongTitle);
        } catch (WebDriverException e) {
            System.out.println("[InfoPanel] playSelectedSong: double-click FAILED "
                    + "for " + selectedSongTitle + " ("
                    + e.getClass().getSimpleName() + ": " + e.getMessage() + ")");
            throw e;
        }
        wait.until(d -> selectedSongTitle.equals(
                d.findElement(TODO_PLAYING_SONG_LOCATOR).getText().trim()
        ));
        System.out.println("[InfoPanel] playSelectedSong: playback verified for "
                + selectedSongTitle);
    }

    public void playSongOnCurrentAlbumPage(String albumRoute, String songTitle) {
        logWorkflowDiagnostic("playSongOnCurrentAlbumPage: start");
        String expectedRoute = requiredValue(albumRoute, "album route");
        String expectedSong = requiredValue(songTitle, "album page song title");
        System.out.println("[InfoPanel] album shuffle: waiting for route "
                + expectedRoute);
        wait.until(d -> {
            String currentUrl = d.getCurrentUrl();
            int fragmentStart = currentUrl.indexOf('#');
            return fragmentStart >= 0
                    && expectedRoute.equals(currentUrl.substring(fragmentStart));
        });
        System.out.println("[InfoPanel] album shuffle: route is open; locating "
                + expectedSong);
        By titleInRow = By.cssSelector("td.title");
        selectedSongRow = wait.until(d -> {
            for (WebElement row : d.findElements(ALBUM_PAGE_SONG_ROWS_LOCATOR)) {
                if (row.isDisplayed()) {
                    String title = row.findElement(titleInRow).getText().trim();
                    if (expectedSong.equals(title)) {
                        selectedSongTitle = expectedSong;
                        return row;
                    }
                }
            }
            return null;
        });
        System.out.println("[InfoPanel] album shuffle: found visible album song "
                + expectedSong);
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});",
                selectedSongRow
        );
        System.out.println("[InfoPanel] album shuffle: immediately before "
                + "Selenium double-click on " + expectedSong);
        try {
            new Actions(driver).doubleClick(selectedSongRow).perform();
            System.out.println("[InfoPanel] album shuffle: Selenium double-click "
                    + "completed for " + expectedSong);
        } catch (WebDriverException e) {
            System.out.println("[InfoPanel] album shuffle: double-click failed for "
                    + expectedSong + " (" + e.getClass().getSimpleName()
                    + ": " + e.getMessage() + ")");
            throw e;
        }
        wait.until(d -> expectedSong.equals(
                d.findElement(TODO_PLAYING_SONG_LOCATOR).getText().trim()
        ));
        logWorkflowDiagnostic("playSongOnCurrentAlbumPage: success");
    }

    public void shuffleByArtist() {
        click(TODO_ARTIST_SHUFFLE_BUTTON_LOCATOR,
                "TODO_ARTIST_SHUFFLE_BUTTON_LOCATOR");
        waitForQueueToSettle();
    }

    public void shuffleByAlbum() {
        click(TODO_ALBUM_SHUFFLE_BUTTON_LOCATOR,
                "TODO_ALBUM_SHUFFLE_BUTTON_LOCATOR");
        waitForQueueToSettle();
    }

    public void openCurrentQueue() {
        logWorkflowDiagnostic("openCurrentQueue: start");
        wait.until(ExpectedConditions.elementToBeClickable(CURRENT_QUEUE_LINK_LOCATOR))
                .click();
        By queueTracks = locator(TODO_QUEUE_TRACKS_LOCATOR,
                "TODO_QUEUE_TRACKS_LOCATOR");
        wait.until(d -> d.findElements(queueTracks).stream()
                .anyMatch(WebElement::isDisplayed));
        getQueueOrder();
        logWorkflowDiagnostic("openCurrentQueue: success");
    }

    public List<String> getQueueOrder() {
        logWorkflowDiagnostic("getQueueOrder: start");
        By queueTracks = locator(TODO_QUEUE_TRACKS_LOCATOR,
                "TODO_QUEUE_TRACKS_LOCATOR");
        List<String> queueOrder = wait.until(d -> {
            List<String> order = readQueueOrder(queueTracks);
            return !order.isEmpty() && order.stream().allMatch(title -> !title.isEmpty())
                    ? order
                    : null;
        });
        System.out.println("[InfoPanel] getQueueOrder: retrieved "
                + queueOrder.size() + " queue tracks");
        logWorkflowDiagnostic("getQueueOrder: success");
        return queueOrder;
    }

    private void logWorkflowDiagnostic(String event) {
        long visibleQueueSize = driver.findElements(TODO_QUEUE_TRACKS_LOCATOR)
                .stream()
                .filter(WebElement::isDisplayed)
                .count();
        boolean infoButtonPresent = driver.findElements(TODO_INFO_BUTTON_LOCATOR)
                .stream()
                .anyMatch(WebElement::isDisplayed);
        System.out.println("[InfoPanel] " + event
                + " | visibleQueueSize=" + visibleQueueSize
                + " | infoButtonPresent=" + infoButtonPresent);
    }

    private void waitForQueueToSettle() {
        By queueTracks = locator(TODO_QUEUE_TRACKS_LOCATOR,
                "TODO_QUEUE_TRACKS_LOCATOR");
        AtomicReference<List<String>> previousOrder = new AtomicReference<>();
        AtomicLong stableSince = new AtomicLong();
        WebDriverWait queueWait = new WebDriverWait(
                driver, QUEUE_UPDATE_WAIT, QUEUE_POLL_INTERVAL
        );

        queueWait.until(d -> {
            List<String> currentOrder = readQueueOrder(queueTracks);
            if (currentOrder.isEmpty()
                    || currentOrder.stream().anyMatch(String::isEmpty)) {
                previousOrder.set(null);
                stableSince.set(0L);
                return null;
            }

            long now = System.nanoTime();
            if (!currentOrder.equals(previousOrder.get())) {
                previousOrder.set(currentOrder);
                stableSince.set(now);
                return null;
            }

            if (now - stableSince.get() >= QUEUE_STABLE_WINDOW.toNanos()) {
                return currentOrder;
            }
            return null;
        });
    }

    private List<String> readQueueOrder(By queueTracks) {
        List<String> order = new ArrayList<>();
        for (WebElement row : driver.findElements(queueTracks)) {
            if (row.isDisplayed()) {
                order.add(row.findElement(QUEUE_TRACK_TITLE_LOCATOR)
                        .getText().trim());
            }
        }
        return order;
    }

    private void logInfoPanelState(String timing) {
        List<WebElement> panels = driver.findElements(TODO_INFO_PANEL_LOCATOR);
        if (panels.isEmpty()) {
            System.out.println("[InfoPanel] " + timing
                    + ": panel not found; visible=false; class=<absent>");
            return;
        }
        for (int i = 0; i < panels.size(); i++) {
            WebElement panel = panels.get(i);
            System.out.println("[InfoPanel] " + timing
                    + ": panel[" + i + "] visible=" + panel.isDisplayed()
                    + ", class=\"" + panel.getAttribute("class") + "\"");
        }
    }

    private WebElement findSongRow(By songRows, By titleLocator, String expectedTitle) {
        for (WebElement row : driver.findElements(songRows)) {
            if (row.isDisplayed()
                    && expectedTitle.equals(
                            row.findElement(titleLocator).getText().trim()
                    )) {
                return row;
            }
        }
        return null;
    }

    private WebElement findSongsScrollElement(WebElement songsWrapper) {
        Object scrollElement = ((JavascriptExecutor) driver).executeScript(
                "let element = arguments[0];"
                        + "while (element && element !== document.body) {"
                        + "  const style = window.getComputedStyle(element);"
                        + "  if (element.scrollHeight > element.clientHeight"
                        + "      && /(auto|scroll)/.test(style.overflowY)) {"
                        + "    return element;"
                        + "  }"
                        + "  element = element.parentElement;"
                        + "}"
                        + "return document.scrollingElement;",
                songsWrapper
        );
        if (!(scrollElement instanceof WebElement)) {
            throw unresolved("Could not determine the All Songs scroll container.");
        }
        return (WebElement) scrollElement;
    }

    private void scrollToTop(WebElement scrollElement) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollTop = 0;", scrollElement
        );
    }

    private Number getScrollTop(WebElement scrollElement) {
        return (Number) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].scrollTop;", scrollElement
        );
    }

    private Number getClientHeight(WebElement scrollElement) {
        return (Number) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].clientHeight;", scrollElement
        );
    }

    private Number getScrollHeight(WebElement scrollElement) {
        return (Number) ((JavascriptExecutor) driver).executeScript(
                "return arguments[0].scrollHeight;", scrollElement
        );
    }

    private void setScrollTop(WebElement scrollElement, long scrollTop) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollTop = arguments[1];",
                scrollElement,
                scrollTop
        );
    }

    private void click(By unresolvedLocator, String placeholderName) {
        wait.until(ExpectedConditions.elementToBeClickable(
                locator(unresolvedLocator, placeholderName)
        )).click();
    }

    private void clickAndWaitForSelectedTab(By tabLocator, String placeholderName) {
        By tab = locator(tabLocator, placeholderName);
        wait.until(ExpectedConditions.elementToBeClickable(tab)).click();
        wait.until(d -> "true".equals(d.findElement(tab).getAttribute("aria-selected")));
    }

    private void clickNamed(
            Function<String, By> unresolvedLocator,
            String placeholderName,
            String name
    ) {
        if (unresolvedLocator == null) {
            throw unresolved(placeholderName + " has not been supplied.");
        }
        By namedLocator = unresolvedLocator.apply(name);
        if (namedLocator == null) {
            throw unresolved(placeholderName + " returned no locator for: " + name);
        }
        wait.until(ExpectedConditions.elementToBeClickable(namedLocator)).click();
    }

    private String visibleText(By unresolvedLocator, String placeholderName) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(
                locator(unresolvedLocator, placeholderName)
        )).getText().trim();
    }

    private static By locator(By value, String placeholderName) {
        if (value == null) {
            throw unresolved(placeholderName + " has not been supplied.");
        }
        return value;
    }

    private static boolean hasClass(String classAttribute, String className) {
        return classAttribute != null
                && (" " + classAttribute.trim() + " ").contains(" " + className + " ");
    }

    private static String requiredValue(String value, String valueName) {
        if (value == null || value.trim().isEmpty()) {
            throw unresolved("Test data for " + valueName + " has not been supplied.");
        }
        return value.trim();
    }

    private static IllegalStateException unresolved(String detail) {
        return new IllegalStateException(
                "InfoPanel tests are not ready to run: " + detail
        );
    }

    public void playAnySongOnCurrentAlbumPage(String albumRoute) {
        // Verify we are on the correct album page
        wait.until(d -> d.getCurrentUrl().endsWith(albumRoute));

        // Locate the first playable song on the album page
        By songLocator = By.cssSelector(
                "div.song-list-wrap.album tr.song-item"
        );

        WebElement songRow = wait.until(
                ExpectedConditions.visibilityOfElementLocated(songLocator)
        );

        String songTitle = songRow.findElement(
                By.cssSelector("td.title")
        ).getText().trim();

        // Double-click to start playback
        new Actions(driver).doubleClick(songRow).perform();

        // Verify that the music player displays the selected song
        wait.until(d -> songTitle.equals(
                d.findElement(TODO_PLAYING_SONG_LOCATOR).getText().trim()
        ));

        System.out.println(
                "[InfoPanel] Playing album song: " + songTitle
        );
    }
}
