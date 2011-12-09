/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
*/
package org.olat.test.util.selenium;

import java.io.OutputStreamWriter;
import java.lang.reflect.Method;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import junit.framework.AssertionFailedError;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.olat.test.util.selenium.log.LoggingSeleniumWrapper;
import org.olat.test.util.setup.OlatLoginInfos;

import com.thoughtworks.selenium.DefaultSelenium;
import com.thoughtworks.selenium.Selenium;
import com.thoughtworks.selenium.SeleniumException;

/**
 * Creates and manages the selenium instances.
 * 
 * @author stefan
 *
 */
public class SeleniumManager {

	private static final Logger seleniumLogger_;

	static {
        seleniumLogger_ = Logger.getLogger("Selenium");
        seleniumLogger_.setLevel(Level.INFO);
        ConsoleAppender appender = new ConsoleAppender();
        appender.setLayout(new PatternLayout("%d [%t] %-5p %c{1} %x -_._- %m%n"));
        appender.setWriter(new OutputStreamWriter(System.out));
		seleniumLogger_.addAppender(appender);
	}

	private final String nameOfTest_;

	private List<Selenium> seleniums_ = new LinkedList<Selenium>();
	
	/**
	 * 
	 * @param nameOfTest
	 */
	public SeleniumManager(String nameOfTest) {
		nameOfTest_ = nameOfTest;
	}
	
	/**
	 * Creates a new DefaultSelenium, wraps it into a LoggingSeleniumWrapper, and opens Window. <br/>
	 * It adds the newly created selenium instance to the seleniums list.
	 * 
	 * @param loginInfos
	 * @return
	 */
	public Selenium createSelenium(OlatLoginInfos loginInfos) {
		String hostname = loginInfos.getSeleniumHostname();
		String browserId = loginInfos.getSeleniumBrowserId();
		String initialBrowserUrl = loginInfos.getFullOlatServerUrl();
        int port = getDefaultPort();
        System.out.println("connecting to "+hostname+" port "+port+"..." + " browserId: " + browserId + " initialBrowserUrl: " + initialBrowserUrl);
        Selenium selenium = null;
        
        for(int i=1; i<6; i++) {
        	try{
        		selenium = new DefaultSelenium(hostname, port, browserId, initialBrowserUrl);
        		System.out.println("createSelenium - DefaultSelenium created");

        		selenium.start();
        		//Thread.sleep(100000);
        		System.out.println("createSelenium - DefaultSelenium started");
        		// the next line can fail at times
        		selenium.setContext(this.getClass().getSimpleName() + "." + nameOfTest_);
        		//selenium.setBrowserLogLevel("debug");

        		System.out.println("connected.");

        		selenium = new LoggingSeleniumWrapper(selenium, seleniumLogger_);

        		System.out.println("createSelenium: launching browser (url="+loginInfos.getFullOlatServerUrl()+")");

        		// the next line can fail at times
        		selenium.openWindow(loginInfos.getFullOlatServerUrl(), "olat");

        		System.out.println("createSelenium: success so far.");
        		// success
        		break;
        	} catch (SeleniumException se) {
        		selenium = null;
        		System.out.println("createSelenium: browser creation failed...:");
        		se.printStackTrace(System.out);
        		System.out.println("createSelenium: retrying in "+i+"*5 sec...");
        		try {
        			Thread.sleep(i*5000);
        		} catch (InterruptedException e) {
        			e.printStackTrace(System.out);
        		}
        		System.out.println("createSelenium: ok, retrying now...");
        	} catch (Exception ex) {
        		System.out.println("createSelenium: browser creation failed...:");
        		ex.printStackTrace(System.out);
        		System.out.println("createSelenium: retrying in "+i+"*5 sec...");
        	}
        }
        
        if (selenium==null) {
        	System.out.println("createSelenium: COULD NOT CREATE A BROWSER, EVEN THOUGH I TRIED 5 TIMES WITH 5 SEC DELAYS. GIVING UP HERE.");
        	throw new AssertionFailedError("Could not create browser even though I tried 5 times with 5 sec delays, giving up");
        }
    	System.out.println("createSelenium: waiting 5sec.");
    	selenium.waitForPopUp("olat", "30000");
		System.out.println("createSelenium: selecting the olat window");
		selenium.selectWindow("olat");
		if (selenium.isTextPresent("OLAT-Benutzername")) {
			// then we have to switch to english...
			selenium.select("//select[contains(@name, 'language_SELBOX')]", "label=English");
			for (int second = 0;; second++) {
				if (second >= 120) throw new AssertionError("timeout while checking for language change to English to occur");
				try { if (selenium.isTextPresent("OLAT user name")) break; } catch (Exception e) {}
				try {
					Thread.sleep(500);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
        
        seleniums_.add(selenium);
        return selenium;
    }

	/**
	 * Closes all selenium instances found in the seleniums list.
	 */
	public void closeSeleniums() {
		System.out.println("*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*");
		System.out.println("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
		System.out.println("* Getting body texts of all selenium browsers:  *");
		int i = 0;
		for (Iterator<Selenium> it = seleniums_.iterator(); it.hasNext();) {
			Selenium s = (Selenium) it.next();
			System.out.println("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
			System.out.println("* Selenium["+(i++)+","+s+"].getBodyText():                     *");
			System.out.println("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
			try{
				System.out.println(s.getBodyText());
    		} catch(Exception e) {
    			//e.printStackTrace(System.out);
    			System.out.println("Couldn't fetch body of Selenium["+(i-1)+"]: "+e);
    		}
    		/*try{
    			String filename = System.currentTimeMillis()+".png";
    			System.out.println("Capturing screenshot into: "+filename);
				s.captureEntirePageScreenshot(filename);
    		} catch(Exception e) {
    			System.out.println("Couldn't capture screenshot :(  - "+e);
    		}*/
		}
		System.out.println("* Done with getting body of all seleniums       *");
		System.out.println("-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-*-");
		System.out.println("*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*=*");
    	System.out.println("Killing the selenium servers...");
		for (Iterator<Selenium> it = seleniums_.iterator(); it.hasNext();) {
			Selenium s = it.next();
			doClose(s);
		}
	}
	
	public boolean anySeleniumBrowserHasKnownIssue3857() {
		for (Iterator<Selenium> it = seleniums_.iterator(); it.hasNext();) {
			Selenium s = (Selenium) it.next();
			try{
				final String body = s.getBodyText();
				if (body.contains("Please do not use the `Reload` or `Back` button of your browser.")) {
					return true;
				}
			} catch(Exception e) {
				// ignore
			}
		}
		return false;
	}

	private static void doClose(Selenium selenium) {
    	try{
    		selenium.close();
    	} catch(Exception e) {
    		e.printStackTrace();
    	} finally {
    		try{
    			selenium.stop();
    		} catch(Exception e) {
    			e.printStackTrace();
    		}
    	}

    }
    
	/**
	 * Default port is 4444.
	 * @return
	 */
    public static int getDefaultPort() {
        try {
            Class c = Class.forName("org.openqa.selenium1.server.SeleniumServer");
            Method getDefaultPort = c.getMethod("getDefaultPort", new Class[0]);
            Integer portNumber = (Integer)getDefaultPort.invoke(null, new Object[0]);
            return portNumber.intValue();
        } catch (Exception e) {
            return 4444;
        }
    }

}
