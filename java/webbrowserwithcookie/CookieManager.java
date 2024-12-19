package webbrowserwithcookie;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.time.Duration;
import java.util.Set;

import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.WebDriverWait;

public class CookieManager {
	public static WebDriver driver;

	// Method to save cookies to a file
	public static void saveCookiesToFile(WebDriver driver, String filePath) {
		try (ObjectOutputStream out = new ObjectOutputStream(new FileOutputStream(filePath))) {
			Set<Cookie> cookies = driver.manage().getCookies();
			out.writeObject(cookies);
			System.out.println("Cookies được lưu vào file: " + filePath);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// Method to load cookies from a file
	public static void loadCookiesFromFile(WebDriver driver, String filePath) {
		try (ObjectInputStream in = new ObjectInputStream(new FileInputStream(filePath))) {
			Set<Cookie> cookies = (Set<Cookie>) in.readObject();
			for (Cookie cookie : cookies) {
				driver.manage().addCookie(cookie);
			}
			driver.navigate().refresh();
			System.out.println("Cookies được tải từ file: " + filePath);
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
		}
	}

	private static void createCookies(String mail, String name, String password, int i) {
		WebDriver localDriver = new ChromeDriver(); // Khởi tạo driver mới
		WebInteractor helper = new WebInteractor(Duration.ofSeconds(30));
		CookieManager.driver = localDriver; // Gán driver vào CookieManager.driver
		localDriver.get("https://twitter.com/login");

		try {
			WebDriverWait wait = new WebDriverWait(localDriver, Duration.ofSeconds(10));
			WebElement usernameField = helper.waitForElement(By.cssSelector(
					"[autocapitalize='sentences'][autocomplete='username'][autocorrect='on'][name='text'][spellcheck='true']"));
			if (usernameField != null)
				usernameField.sendKeys(mail);

			WebElement nextButton = helper.waitForElement(By.xpath("//span[text()='Next']"));
			if (nextButton != null)
				nextButton.click();

			WebElement nameField = helper.waitForElement(By.cssSelector("[inputmode='text']"));
			if (nameField != null) {
				nameField.sendKeys(name);
				WebElement nextField = helper.waitForElement(By.cssSelector("[data-testid='ocfEnterTextNextButton']"));
				if (nextField != null)
					nextField.click();
			}

			WebElement passwordField = helper
					.waitForElement(By.cssSelector("[name=\"password\"][spellcheck=\"true\"]"));
			if (passwordField != null)
				passwordField.sendKeys(password);

			WebElement loginButton = helper
					.waitForElement(By.cssSelector("[data-testid=\"LoginForm_Login_Button\"][type=\"button\"]"));
			if (loginButton != null)
				loginButton.click();
			helper.waitForPageToLoad(2000);
			saveCookiesToFile(driver, "twitterCookies" + i + ".ser");
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			localDriver.quit();
			CookieManager.driver = null; // Đặt lại về null sau khi dùng xong
		}
	}

	public static void main(String[] args) {
		String[][] accounts = { {}, {}, {}, {}, {}, {} };
		int i = 0;
		for (String[] account : accounts) {
			String mail = account[0];
			String name = account[1];
			String password = account[2];

			createCookies(mail, name, password, i);
			i++;
		}
	}

}
