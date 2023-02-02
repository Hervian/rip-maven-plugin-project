package eu.mansipi.feign_server;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.runner.RunWith;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.htmlunit.HtmlUnitDriver;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.concurrent.TimeUnit;

/**
 * Guides:
 * https://blog.ttulka.com/double-testing/
 */
/*@RunWith(SpringRunner.class)
@ExtendWith(SpringExtension.class)
@SpringBootTest(classes = Main.class, webEnvironment = SpringBootTest.WebEnvironment.DEFINED_PORT)*/
public class MainTest {

  private static Logger log =  LogManager.getLogger(MainTest.class);

  /*@Test //Work in progress. Must run after rip-maven-plugin is completely done.
  public void HeadlessTest() {
    WebDriver driver = new HtmlUnitDriver(true);
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
