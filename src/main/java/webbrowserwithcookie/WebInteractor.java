package webbrowserwithcookie;

import java.time.Duration;
import java.util.List;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import static webbrowserwithcookie.WebManager.driver;
import static webbrowserwithcookie.WebManager.wait;
import static webbrowserwithcookie.WebManager.js;

public class WebInteractor {
    
    public WebInteractor(Duration timeout) {
        wait = new WebDriverWait(driver, timeout);
    }

    public void setWait(Duration timeout){
        wait= new WebDriverWait(driver,timeout);
    }

    public WebElement waitForElement(By element) {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(element));
    }

    public static void waitForPageToLoad(Integer milliseconds) {
        if (milliseconds == null) {
            new WebDriverWait(driver, Duration.ofSeconds(40)).until(
                webDriver -> ((JavascriptExecutor) webDriver).executeScript("return document.readyState").equals("complete")
            );
        } else {
            try {
                Thread.sleep(milliseconds);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                e.printStackTrace();
            }
        }
    }


    public void fetcher(Set<String> links, Integer maxSizeOfList, By element, By getlink ) {

    	if (maxSizeOfList==null){
            maxSizeOfList= Integer.MAX_VALUE;
        }

        this.waitForElement(element);

        List<WebElement> eleList = driver.findElements(element);
        for (WebElement i : eleList) {
        	try {
                WebElement hrefElement = i.findElement(getlink);
                String link = hrefElement.getAttribute("href");
                links.add(link);
                System.out.println(link);
            } catch (Exception ignored){
                continue;
            };

            if(links.size() >= maxSizeOfList){
                return;
            }
        }
        js.executeScript("window.scrollTo(0, document.body.scrollHeight);");
        waitForPageToLoad(2000);
    }
}
