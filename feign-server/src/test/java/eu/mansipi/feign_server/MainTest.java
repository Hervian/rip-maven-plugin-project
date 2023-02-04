package eu.mansipi.feign_server;

import com.codeborne.selenide.WebDriverRunner;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.util.Strings;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.phantomjs.PhantomJSDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/*import static com.codeborne.selenide.Selenide.$;
import static com.codeborne.selenide.Selenide.open;*/

/**
 * Guides:
 * https://blog.ttulka.com/double-testing/
 */
@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)
public class MainTest {

  private static Logger log =  LogManager.getLogger(MainTest.class);

  WebDriver driver;

  @BeforeAll
  static void setupClass() {
    WebDriverManager.chromedriver().setup();
  }

  @BeforeEach
  void setupTest() {
    System.out.println("Instantiating ChromeDriver");
    driver = new ChromeDriver();
  }

  @AfterEach
  void teardown() {
    driver.quit();
  }

  @Test
  public void test() {
    driver.get("http://localhost:8080/openapi/swagger-ui.html");
    String urlToOpenapiDoc = driver.findElement(By.className("download-url-input")).getAttribute("value");

    log.debug("Title: " + driver.getTitle());
    log.debug("current URL: " + driver.getCurrentUrl());
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    driver.get(urlToOpenapiDoc);
    Assert.assertFalse(driver.getTitle().contains("404"));
  }

  /**
   * Source: https://github.com/klieber/phantomjs-maven-plugin and https://stackoverflow.com/q/32678881/6095334
   * Requirements: the test is dependent on phantomjs being installed - this is handled by a plugin in thte pom - and on
   *
   */
 /* @Test
  public void HeadlessTest() {
    WebDriver driver = null;
    try {
      *//*System.out.println(System.getProperty("phantomjs.binary"));
      System.setProperty("phantomjs.binary.path", System.getProperty("phantomjs.binary"));
      driver = new ChromeDriver();*//*

      ChromeOptions options=new ChromeOptions();
      options.addArguments("headless");
      driver=new ChromeDriver(options);

      driver.get("http://localhost:8080/openapi/swagger-ui.html");
      String urlToOpenapiDoc = driver.findElement(By.className("download-url-input")).getAttribute("value");

      log.debug("Title: " + driver.getTitle());
      log.debug("current URL: " + driver.getCurrentUrl());
      driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

      driver.get(urlToOpenapiDoc);
      Assert.assertFalse(driver.getTitle().contains("404"));
    } finally {
      if (driver!=null) {
        driver.quit();
      }
    }
*/
    // WebDriverRunner.setWebDriver(driver);
/*
    open("http://localhost:8080/openapi/swagger-ui.html");
    String urlToOpenapiDoc = $(By.className("download-url-input")).getAttribute("value");

    Assert.assertTrue(Strings.isNotBlank(urlToOpenapiDoc));*/

    /*WebDriver driver = new HtmlUnitDriver(true);

    driver.get("http://localhost:8080/openapi/swagger-ui.html");
    log.debug("Title: " + driver.getTitle());
    log.debug("current URL: " + driver.getCurrentUrl());
    driver.manage().timeouts().implicitlyWait(5, TimeUnit.SECONDS);

    String urlToOpenapiDoc = driver.findElement(By.className("download-url-input")).getAttribute("value");

    driver.get(urlToOpenapiDoc);
    //log.debug("swagger.json: " + )
    Assert.assertFalse(driver.getTitle().contains("404"));

    driver.close();
  }*/

}
