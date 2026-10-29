package InfoPanel;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;

public class InfoPanelDomInspection {
    private static final Duration WAIT = Duration.ofSeconds(15);

    public static void main(String[] args) {
        String email = requiredEnv("KOEL_EMAIL");
        String password = requiredEnv("KOEL_PASSWORD");
        String[] stage = {"Chrome launch"};
        WebDriver driver = null;
        try {
            WebDriverManager.chromedriver().setup();
            ChromeOptions options = new ChromeOptions();
            options.addArguments("--remote-allow-origins=*");
            driver = new ChromeDriver(options);
            logStage("1. Chrome launched");

            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
            driver.manage().timeouts().pageLoadTimeout(WAIT);
            stage[0] = "Koel page load";
            driver.get("https://qa.koel.app/#!/home");
            logStage("2. Koel page loaded");
            WebDriverWait wait = new WebDriverWait(driver, WAIT);

            stage[0] = "login";
            By emailInput = By.cssSelector(
                    "[data-testid='login-form'] input[type='email']"
            );
            wait.until(ExpectedConditions.visibilityOfElementLocated(emailInput))
                    .sendKeys(email);
            wait.until(ExpectedConditions.visibilityOfElementLocated(By.cssSelector(
                    "[data-testid='login-form'] input[type='password']"
            ))).sendKeys(password);
            wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector(
                    "[data-testid='login-form'] button[type='submit']"
            ))).click();

            wait.until(d -> d.findElements(emailInput).isEmpty());
            logStage("3. Login completed (credential values suppressed)");

            stage[0] = "song discovery";
            List<WebElement> playControls = driver.findElements(By.xpath(
                    "//*[self::button or self::a or @role='button']"
                            + "[contains(translate(@aria-label, 'PLAY', 'play'), 'play')"
                            + " or contains(translate(@title, 'PLAY', 'play'), 'play')"
                            + " or normalize-space(translate(., 'PLAY', 'play'))='play']"
            ));
            WebElement playControl = playControls.stream()
                    .filter(WebElement::isDisplayed)
                    .filter(element -> {
                        String label = (element.getAttribute("aria-label") + " "
                                + element.getAttribute("title") + " "
                                + element.getText()).toLowerCase();
                        return !label.contains("pause");
                    })
                    .findFirst()
                    .orElse(null);
            List<WebElement> candidates = driver.findElements(By.xpath(
                    "//*[self::tr or self::li or @role='row' or @role='listitem']"
            ));
            List<WebElement> visibleCandidates = candidates.stream()
                    .filter(WebElement::isDisplayed)
                    .toList();
            System.out.println("Visible song/row candidate count: "
                    + visibleCandidates.size());
            for (WebElement candidate : visibleCandidates) {
                System.out.println("SONG/ROW CANDIDATE: " + describe(candidate));
            }
            if (visibleCandidates.isEmpty()) {
                logStage("4. Song not located; no visible row candidates were found");
            } else {
                logStage("4. Visible song/row candidate located");
            }

            stage[0] = "playback";
            if (playControl != null) {
                System.out.println("PLAY CONTROL: " + describe(playControl));
                wait.until(ExpectedConditions.elementToBeClickable(playControl)).click();
                boolean playing = wait.until(InfoPanelDomInspection::isAudioPlaying);
                if (playing) {
                    logStage("5. Playback started (audio element reports playing)");
                } else {
                    throw new IllegalStateException(
                            "Audio playback was not observed after clicking Play"
                    );
                }
            } else if (isAudioPlaying(driver)) {
                logStage("5. Playback already active (audio element reports playing)");
            } else {
                logStage("5. Playback not started; no visible Play control found");
            }

            stage[0] = "INFO button discovery";
            By infoLocator = By.xpath(
                    "//*[self::button or self::a or @role='button']"
                            + "[translate(normalize-space(.), 'info', 'INFO')='INFO'"
                            + " or translate(@aria-label, 'info', 'INFO')='INFO'"
                            + " or translate(@title, 'info', 'INFO')='INFO']"
            );
            List<WebElement> exactInfoControls = driver.findElements(infoLocator);
            System.out.println("VISIBLE INFO CONTROLS:");
            for (WebElement control : exactInfoControls) {
                if (control.isDisplayed()) {
                    System.out.println(describe(control));
                }
            }
            WebElement info = exactInfoControls.stream()
                    .filter(WebElement::isDisplayed)
                    .findFirst()
                    .orElse(null);
            if (info == null) {
                throw new IllegalStateException(
                        "No visible exact INFO control was found"
                );
            }
            logStage("6. INFO button located");

            stage[0] = "Info panel inspection";
            wait.until(ExpectedConditions.elementToBeClickable(info)).click();

            By tabLocator = By.xpath(
                    "//*[self::button or self::a or @role='tab']"
                            + "[normalize-space(.)='LYRICS'"
                            + " or normalize-space(.)='ARTIST'"
                            + " or normalize-space(.)='ALBUM']"
            );
            wait.until(d -> d.findElements(tabLocator).stream()
                    .anyMatch(WebElement::isDisplayed));

            System.out.println("VISIBLE INFO TABS:");
            for (WebElement tab : driver.findElements(tabLocator)) {
                if (tab.isDisplayed()) {
                    System.out.println(describe(tab));
                }
            }

            Object panel = ((JavascriptExecutor) driver).executeScript(
                    "const tabs = [...document.querySelectorAll('button,a,[role=tab]')]"
                            + ".filter(e => ['LYRICS','ARTIST','ALBUM']"
                            + ".includes((e.innerText || '').trim()));"
                            + "if (!tabs.length) return null;"
                            + "let p = tabs[0].parentElement;"
                            + "while (p && !tabs.every(t => p.contains(t))) p=p.parentElement;"
                            + "if (!p) return null;"
                            + "return {tag:p.tagName,id:p.id,cls:p.className,"
                            + "testid:p.getAttribute('data-testid'),"
                            + "visible:!!(p.offsetWidth||p.offsetHeight||p.getClientRects().length),"
                            + "html:p.outerHTML.slice(0,4000)};"
            );
            System.out.println("SHARED TAB CONTAINER: " + panel);
            printContentAndShuffleCandidates(driver);
            logStage("7. Info panel inspected");
        } catch (Exception failure) {
            reportFailure(stage[0], failure, email, password);
            throw new IllegalStateException(
                    "Inspection failed at stage: " + stage[0]
                            + " (" + failure.getClass().getName() + ")"
            );
        } finally {
            if (driver != null) {
                try {
                    driver.quit();
                } catch (Exception quitFailure) {
                    reportFailure("Chrome shutdown", quitFailure, email, password);
                    throw new IllegalStateException(
                            "Inspection failed during Chrome shutdown ("
                                    + quitFailure.getClass().getName() + ")"
                    );
                }
            }
        }
    }

    private static boolean isAudioPlaying(WebDriver driver) {
        Object playing = ((JavascriptExecutor) driver).executeScript(
                "return !!document.querySelector('audio')"
                        + " && !document.querySelector('audio').paused;"
        );
        return Boolean.TRUE.equals(playing);
    }

    private static void logStage(String message) {
        System.out.println("[INSPECTION] " + message);
    }

    private static void reportFailure(
            String stage,
            Exception failure,
            String email,
            String password
    ) {
        System.err.println("[INSPECTION] FAILED during " + stage);
        StringWriter trace = new StringWriter();
        failure.printStackTrace(new PrintWriter(trace));
        System.err.print(redact(trace.toString(), email, password));
    }

    private static String redact(String message, String email, String password) {
        if (message == null) {
            return "";
        }
        String safeMessage = message.replace(email, "[REDACTED]");
        if (!password.isEmpty()) {
            safeMessage = safeMessage.replace(password, "[REDACTED]");
        }
        return safeMessage;
    }

    private static void printContentAndShuffleCandidates(WebDriver driver) {
        List<WebElement> elements = driver.findElements(By.xpath(
                "//*[self::button or self::a or @role='button' or @role='tab'"
                        + " or self::img or self::audio]"
        ));
        System.out.println("VISIBLE CONTENT / SHUFFLE CANDIDATES:");
        for (WebElement element : elements) {
            if (element.isDisplayed()) {
                String text = element.getText();
                String title = element.getAttribute("title");
                String label = element.getAttribute("aria-label");
                String alt = element.getAttribute("alt");
                if (element.getTagName().equals("img")
                        || (text != null && !text.trim().isEmpty())
                        || (title != null && !title.trim().isEmpty())
                        || (label != null && !label.trim().isEmpty())
                        || (alt != null && !alt.trim().isEmpty())) {
                    System.out.println(describe(element));
                }
            }
        }
    }

    private static String describe(WebElement element) {
        return "tag=" + element.getTagName()
                + ", text=\"" + clean(element.getText()) + "\""
                + ", id=\"" + clean(element.getAttribute("id")) + "\""
                + ", data-testid=\"" + clean(element.getAttribute("data-testid")) + "\""
                + ", aria-label=\"" + clean(element.getAttribute("aria-label")) + "\""
                + ", title=\"" + clean(element.getAttribute("title")) + "\""
                + ", role=\"" + clean(element.getAttribute("role")) + "\""
                + ", class=\"" + clean(element.getAttribute("class")) + "\""
                + ", src=\"" + clean(element.getAttribute("src")) + "\"";
    }

    private static String clean(String value) {
        return value == null ? "" : value.replace("\r", " ").replace("\n", " ")
                .replace("\"", "'");
    }

    private static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Required environment variable is missing: " + name
            );
        }
        return value;
    }
}
