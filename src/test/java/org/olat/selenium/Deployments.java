/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.selenium;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.logging.log4j.Logger;
import org.eu.ingwar.tools.arquillian.extension.suite.annotations.ArquillianSuiteDeployment;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.ClassRule;
import org.olat.core.logging.Tracing;
import org.olat.test.ArquillianDeployments;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.devtools.DevTools;

import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxDriverLogLevel;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.firefox.FirefoxProfile;
import org.openqa.selenium.firefox.GeckoDriverService;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;
import org.zapodot.junit.ldap.EmbeddedLdapRule;
import org.zapodot.junit.ldap.EmbeddedLdapRuleBuilder;

import com.dumbster.smtp.SimpleSmtpServer;

/**
 * 
 * Initial date: 12 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@ArquillianSuiteDeployment
public class Deployments {
	
	private static final Logger log = Tracing.createLoggerFor(Deployments.class);

	private static SimpleSmtpServer dumbster;
	static {
		try {
			dumbster = SimpleSmtpServer.start(SimpleSmtpServer.AUTO_SMTP_PORT);
		} catch (IOException e) {
			log.error("", e);
		}
	}

	private static List<WebDriver> drivers = new ArrayList<>();
	
	@Deployment(testable = false)
	public static WebArchive createDeployment() {
		Map<String,String> overrideSettings = new HashMap<>();
		if(dumbster != null) {
			overrideSettings.put("smtp.port", String.valueOf(dumbster.getPort()));
			overrideSettings.put("smtp.host", "localhost");
			log.info("Simple smtp server started on port: " + dumbster.getPort());
		}
		//overrideSettings.put("ldap.enable", "false");
		return ArquillianDeployments.createDeployment(overrideSettings);
	}
	
	@ClassRule
	public static final EmbeddedLdapRule embeddedLdapRule = EmbeddedLdapRuleBuilder
	        .newInstance()
	        .usingDomainDsn("dc=olattest,dc=org")
	        .importingLdifs("org/olat/ldap/junittestdata/olattest.ldif")
	        .bindingToAddress("localhost")
	        .bindingToPort(1389)
	        .build();
	
	@After
	public void afterTest() {
		if(dumbster != null) {
			dumbster.reset();
		}
	
		for(int i=drivers.size(); i-->1; ) {
			drivers.remove(i).quit();
		}
	}
	
	@AfterClass
	public static void close() {
		quitWebdrivers();
	}
	
	public static void quitWebdrivers() {
		for(WebDriver driver:drivers) {
			driver.quit();
		}
	}
	
	protected SimpleSmtpServer getSmtpServer() {
		return dumbster;
	}
	
	protected WebDriver getWebDriver(int id) {
		if(id == 0 && !drivers.isEmpty()) {
			return drivers.get(0);
		}
		
		WebDriver driver = createWebDriver(id);
		drivers.add(driver);
		driver.manage().window().setSize(new Dimension(1024,800));
		startDevTools(driver);
		return driver;
	}
	
	protected WebDriver createWebDriver(int id) {
		String browser = System.getProperty("webdriver.browser");
		WebDriver driver;
		if("safari".equals(browser) && id == 0) {
			driver = new SafariDriver(new SafariOptions());
		} else if("edge".equals(browser)) {
			EdgeOptions options = new EdgeOptions();
			options.addArguments("--disable-features=msHubApps", "--disable-infobars");
			options.setExperimentalOption("excludeSwitches", List.of("enable-automation"));
			driver = new EdgeDriver(options);
		} else if("firefox".equals(browser)) {
			FirefoxOptions options = new FirefoxOptions();
			options.setLogLevel(FirefoxDriverLogLevel.TRACE);
			FirefoxProfile profile = new FirefoxProfile();
			profile.setPreference("fission.webContentIsolationStrategy", Integer.valueOf(0));
			profile.setPreference("fission.bfcacheInParent", Boolean.FALSE);
			options.setProfile(profile);
			driver = new FirefoxDriver(GeckoDriverService.createDefaultService(), options);
		} else {
			ChromeOptions options = new ChromeOptions();
			options.setExperimentalOption("excludeSwitches", Arrays.asList("enable-automation"));
			
			Map<String, Object> prefs = new HashMap<>();
			prefs.put("credentials_enable_service", Boolean.FALSE);
			prefs.put("profile.password_manager_enabled", Boolean.FALSE);
			options.setExperimentalOption("prefs", prefs);
			driver = new ChromeDriver(ChromeDriverService.createDefaultService(), options);
		}
		return driver;
	}
	
	protected void startDevTools(WebDriver driver) {
		try {
			if(driver instanceof ChromeDriver chromeDriver) {
				DevTools devTools = chromeDriver.getDevTools();
				devTools.createSessionIfThereIsNotOne();
				devTools.send(org.openqa.selenium.devtools.v127.log.Log.enable());
				devTools.send(org.openqa.selenium.devtools.v127.console.Console.enable());
				devTools.addListener(org.openqa.selenium.devtools.v127.log.Log.entryAdded(), logEntry -> {
					log.warn("Chrome: {} {}", logEntry.getLevel(), logEntry.getText());
				});
				devTools.addListener(org.openqa.selenium.devtools.v127.console.Console.messageAdded(), logEntry -> {
					log.warn("Chrome console: {} {}", logEntry.getLevel(), logEntry.getText());
				});
			}
		} catch (Exception e) {
			log.error("Cannot start dev tools", e);
		}
	}
}
