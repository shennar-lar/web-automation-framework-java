import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.BeforeSuite;

import java.time.Duration;

public class BaseTest {

    // ThreadLocal ensures every parallel test thread gets its own isolated browser session
    private static final ThreadLocal<WebDriver> threadDriver = new ThreadLocal<>();

    // Kept for backward compatibility with existing tests
    protected WebDriver driver;

    @BeforeSuite
    public static void setupClass() {
        WebDriverManager.chromedriver().setup();
    }

    public static WebDriver getDriver() {
        return threadDriver.get();
    }

    public static void setDriver(WebDriver driverInstance) {
        threadDriver.set(driverInstance);
    }

    @BeforeMethod
    public void setUp() {
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");

        // Automatically enables headless mode if running inside GitHub Actions (CI)
        if (System.getenv("CI") != null || Boolean.parseBoolean(System.getProperty("headless", "false"))) {
            options.addArguments("--headless=new");
            options.addArguments("--disable-gpu");
            options.addArguments("--no-sandbox");
            options.addArguments("--disable-dev-shm-usage");
            options.addArguments("--window-size=1920,1080");
        }

        WebDriver localDriver = new ChromeDriver(options);
        setDriver(localDriver);
        this.driver = getDriver();

        getDriver().manage().timeouts().implicitlyWait(Duration.ofSeconds(10));
        getDriver().manage().window().maximize();
        getDriver().get("https://qa.koel.app/");
    }

    @AfterMethod(alwaysRun = true)
    public void tearDown() {
        if (getDriver() != null) {
            getDriver().quit();
            threadDriver.remove(); // Prevents memory leaks in parallel test runners
        }
    }

    protected static String requiredEnv(String name) {
        String value = System.getenv(name);
        if (value == null || value.trim().isEmpty()) {
            throw new IllegalStateException(
                    "Set the " + name + " environment variable"
            );
        }
        return value;
    }
}