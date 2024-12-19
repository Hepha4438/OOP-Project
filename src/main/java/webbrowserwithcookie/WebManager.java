package webbrowserwithcookie;

import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class WebManager {
    public static WebDriver driver;
    public static WebDriverWait wait;
    private static int channel = 1;
    public static JavascriptExecutor js;

    public WebManager(WebDriver driver, Duration timeout) {
        WebManager.driver = driver;
        wait = new WebDriverWait(driver, timeout);
    }

    public int getChannel(){
        return channel;
    }

    public void setChannel(int a){
        channel=a;
    }

    public static void createDriverWithNewCookies() {
        if (driver != null) {
            driver.quit();
        }
        driver = new ChromeDriver();
        wait = new WebDriverWait(driver, Duration.ofSeconds(30));
        js = (JavascriptExecutor) driver;
        WebManager.driver.get("https://twitter.com/login");
        String fileCookiesPath = String.format("twitter_cookies/twitterCookies%d.ser", channel % 6);
        CookieManager.loadCookiesFromFile(driver,fileCookiesPath);
        channel++;
    }
}
