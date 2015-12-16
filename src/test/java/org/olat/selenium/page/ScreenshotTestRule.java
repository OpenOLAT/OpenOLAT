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
package org.olat.selenium.page;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;

import org.junit.rules.MethodRule;
import org.junit.runners.model.FrameworkMethod;
import org.junit.runners.model.Statement;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebDriverException;
import org.openqa.selenium.remote.RemoteWebDriver;

/**
 * 
 * 
 * 
 * Initial date: 15.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class  ScreenshotTestRule implements MethodRule {
	
	private List<WebDriver> browserList;
	
	public void setBrowsers(WebDriver... browsers) {
		browserList = new ArrayList<>();
		if(browsers != null && browsers.length > 0 && browsers[0] != null) {
			for(WebDriver browser:browsers) {
				if(browser != null) {
					browserList.add(browser);
				}
			}
		}
	}
	
	
	@Override
    public Statement apply(final Statement statement, final FrameworkMethod frameworkMethod, final Object o) {
        return new Statement() {
            @Override
            public void evaluate() throws Throwable {
                try {
                    statement.evaluate();
                } catch (Throwable t) {
                    captureScreenshot(frameworkMethod.getName(), t);
                    throw t; // rethrow to allow the failure to be reported to JUnit
                }
            }

            public void captureScreenshot(String fileName, Throwable t) {
            	List<WebDriver> toShootList = new ArrayList<>();
            	if(t instanceof WebDriverException) {
            		WebDriverException driverException = (WebDriverException)t;
            		String infos = driverException.getAdditionalInformation();
            		for(WebDriver browser:browserList) {
            			if(browser instanceof RemoteWebDriver) {
            				String sessionId = ((RemoteWebDriver)browser).getSessionId().toString();
            				if(infos.contains(sessionId)) {
            					toShootList.add(browser);
            				}
            			}
            		}
            	} 
            	if(toShootList.isEmpty()) {
            		toShootList.addAll(browserList);
            	}
            	
                try {
                	int count = 0;
                	for(WebDriver browser:toShootList) {
	                	if(browser instanceof TakesScreenshot) {
		                    new File("target/surefire-reports/").mkdirs(); // Insure directory is there
		                    FileOutputStream out = new FileOutputStream("target/surefire-reports/screenshot-" + fileName + "_" + (count++)+ ".png");
		                    out.write(((TakesScreenshot) browser).getScreenshotAs(OutputType.BYTES));
		                    out.close();
                		}
                	}
                } catch (Exception e) {
                    // No need to crash the tests if the screenshot fails
                }
            }
        };
    }
}