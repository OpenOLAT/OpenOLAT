package org.olat.test.util.selenium;

import org.olat.test.util.setup.OlatLoginInfos;
import org.olat.test.util.setup.context.Context;

import com.thoughtworks.selenium.Selenium;

/**
 * Provides login, logout functionality.
 * 
 * @author stefan
 *
 */
public class OlatLoginHelper {

	/**
	 * Logout is anywhere available in OLAT.
	 * @param selenium
	 */
	public static void olatLogout(Selenium selenium) {
		selenium.click("ui=tabs::logOut");
		selenium.waitForPageToLoad("30000");
	}
	
	/**
	 * 
	 * @param selenium
	 * @param loginInfos
	 */
	public static void olatLogin(Selenium selenium, OlatLoginInfos loginInfos)  {
		String username = loginInfos.getUsername();
		String password = loginInfos.getPassword();
		String language = loginInfos.getLanguage();
		boolean success = false;
		try {
			for (int second = 0;; second++) {
				if (second >= 120)
					fail("timeout. Could not get to see 'OLAT - Online Learning And Training'... is OLAT down?");
				System.out.println("title now: " + selenium.getTitle());
				try {
					if ("OLAT - Online Learning And Training".equals(selenium
							.getTitle()))
						break;
				} catch (Exception e) {
				}
				sleep(500);
			}
			if (selenium.isTextPresent("OLAT-Benutzername")
					&& !language.equals("Deutsch")) {
				// then we have to switch to english...
				selenium.select("//select[contains(@name, 'language_SELBOX')]", "label=" + language);
				for (int second = 0;; second++) {
					if (second >= 120)
						fail("timeout. Could not switch to English in DMZ!");
					try {
						if (selenium.isTextPresent("OLAT user name"))
							break;
					} catch (Exception e) {
					}
					sleep(500);
				}
			}
			assertEquals("OLAT - Online Learning And Training", selenium
					.getTitle());
			login: while (true) {
				System.out.println("logging in to " + selenium.getLocation()
						+ " as " + username);
				
				inputUserNameAndPassword(selenium, username, password);				
				
				//sleep(5000);
				if(selenium.isTextPresent("Terms of use") ) {
					//accept disclaimer					
					selenium.click("ui=dmz::disclaimerCheckbox()");
					selenium.click("ui=dmz::acceptDisclaimer(acceptLabel=Accept)");
					selenium.waitForPageToLoad("30000");
					System.out.println("accept disclaimer	- EN");
				} else if(selenium.isTextPresent("Nutzungsbedingungen")) {
					selenium.click("ui=dmz::disclaimerCheckbox()");
					selenium.click("ui=dmz::acceptDisclaimer(acceptLabel=Akzeptieren)");
					selenium.waitForPageToLoad("30000");
					System.out.println("accept disclaimer	- DE");
				}				
				
				System.out.println("waiting for home page to open...");
				for (int second = 0;; second++) {
					if (second >= 120)
						fail("timeout. Could not login with username="+username+", password="+password);
					System.out.println("fetching title...");
					System.out.println("title now: " + selenium.getTitle());
					try {
						if ("OLAT - Home".equals(selenium.getTitle()))
							break;
					} catch (Exception e) {
					}
					sleep(500);
				}
				System.out.println("Great, Home opened!");

				if (selenium.isElementPresent("ui=home::menu_settings()")
						&& language.equals("English")) {
					// language is correct
					break;
				}
				if (selenium.isElementPresent("ui=home::menu_einstellungen()")
						&& language.equals("Deutsch")) {
					// language is correct
					break;
				}

				if (!selenium.isElementPresent("ui=home::menu_einstellungen()")
						&& !selenium
								.isElementPresent("ui=home::menu_settings()")) {
					fail("only supporting default language German or English for switching to another language at the moment");
				}

				if (selenium.isElementPresent("ui=home::menu_einstellungen()")) {
					// then we're in german, lets switch to english
					System.out
							.println("We're in german, lets set the correct language");
					selenium.click("ui=home::menu_einstellungen()");
					selenium.waitForPageToLoad("30000");
					selenium
							.click("ui=home::content_einstellungen_tabs_system()");
					selenium.waitForPageToLoad("60000");
					selenium.select(
							"ui=home::content_einstellungen_system_sprache()",
							"label=" + language);
					selenium
							.click("ui=home::content_einstellungen_system_speichern()");
					selenium.waitForPageToLoad("30000");
					System.out.println("HTMLSOURCE: "
							+ selenium.getHtmlSource());
					sleep(600);
					if (selenium.isElementPresent("ui=tabs::logOut()")) {
						System.out.println("logOut present!");
					} else {
						for(int k=0; k<30; k++) {
							while(!selenium.isElementPresent("ui=tabs::logOut()")) {
								System.out.println("logOut not present!");
								sleep(500);
							}
						}
						if (!selenium.isElementPresent("ui=tabs::logOut()")) {
							System.out.println("logOut not present!");
						}
					}
					selenium.click("ui=tabs::logOut()");
				} else {
					// then we're in german, lets switch to english
					System.out
							.println("We're in english, lets set the correct language");
					selenium.click("ui=home::menu_settings()");
					selenium.waitForPageToLoad("30000");
					selenium.click("ui=home::content_settings_tabs_system()");
					selenium.waitForPageToLoad("30000");
					selenium
							.select(
									"ui=home::content_settings_system_general_language()",
									"label=" + language);
					selenium
							.click("ui=home::content_settings_system_general_save()");
					selenium.waitForPageToLoad("30000");
					System.out.println("HTMLSOURCE: "
							+ selenium.getHtmlSource());
					sleep(600);
					if (selenium.isElementPresent("ui=tabs::logOut()")) {
						System.out.println("logOut present!");
					} else {
						System.out.println("logOut not present!");
					}
					selenium.click("ui=tabs::logOut()");
				}

				System.out.println("waiting for logout to be done");
				selenium.waitForPageToLoad("30000");
				for (int second = 0;; second++) {
					if (second >= 120)
						fail("timeout. Could not go back to DMZ to see 'OLAT - Online Learning And Training', did the logOut fail ?");
					System.out.println("title now: " + selenium.getTitle());
					try {
						if ("OLAT - Online Learning And Training"
								.equals(selenium.getTitle()))
							break;
					} catch (Exception e) {
					}
					sleep(500);
				}
				continue login;
			}
			System.out.println("login of user " + username + " to url "
					+ selenium.getLocation() + " successful");
			success = true;
		} finally {
			System.out.println("OLATLOGIN FINALLY. success="+success);
			if (!success) {
				System.out.println("Body: "+selenium.getBodyText());
			}
		}
	}
	
	/**
	 * Enter username and password, and press login, no matter whether the login form is in English or German.
	 * @param selenium
	 * @param username
	 * @param password
	 */
	public static void inputUserNameAndPassword(Selenium selenium, String username, String password) {
	  //if shib login, go to olat login
	  if(selenium.isElementPresent("ui=dmz::wayf()")) {
	    //switch to olat login
	    if(selenium.isElementPresent("ui=commons::anyLink(linkText=Weiter)")) {
		  selenium.click("ui=commons::anyLink(linkText=Weiter)");
		  selenium.waitForPageToLoad("60000");
		} else if(selenium.isElementPresent("ui=commons::anyLink(linkText=Next)")) {
		  selenium.click("ui=commons::anyLink(linkText=Next)");
		  selenium.waitForPageToLoad("60000");  
		}		
	  }
	  //assuming that we are on the olat login page...
	  if(selenium.isElementPresent("ui=commons::flexiForm_labeledTextInput(formElementLabel=OLAT-Benutzername)")) {
	    selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=OLAT-Benutzername)", username);
		sleep(5000);			  
		selenium.type("ui=commons::flexiForm_labeledPasswordInput(formElementLabel=OLAT-Passwort)", password);
		sleep(5000);
	  } else if(selenium.isElementPresent("ui=commons::flexiForm_labeledTextInput(formElementLabel=OLAT user name)")) {
		  selenium.type("ui=commons::flexiForm_labeledTextInput(formElementLabel=OLAT user name)", username);
		  selenium.type("ui=commons::flexiForm_labeledPasswordInput(formElementLabel=OLAT password)", password);
	  } 
	  selenium.click("ui=dmz::login()");
	  selenium.waitForPageToLoad("30000");
	}

	private static void sleep(int time) {
		while(true) {
			try {
				Thread.sleep(time);
				return;
			} catch (InterruptedException e) {
				// do it again
			}
		}
	}

	private static void assertEquals(String expected, String actual) {
		if (expected==null && actual==null) {
			return;
		} else if (expected==null || actual==null) {
			throw new AssertionError("expected "+expected+" but got "+actual);
		} else if (!expected.equals(actual)) {
			throw new AssertionError("expected "+expected+" but got "+actual);
		}
	}

	static void fail(String msg) {
		throw new AssertionError("failure: "+msg);
	}
	
	/**
	 * Try to login but gets an error message.
	 * @param nodeId
	 * @param username
	 * @param password
	 * @return Returns true, if it gets an error msg at login.
	 */
	public static boolean loginExpectingError(int nodeId, String username, String password) throws Exception{
		Selenium selenium = Context.getContext().createSelenium(Context.getContext().getOlatLoginInfo(nodeId, username, password));
		assertEquals("OLAT - Online Learning And Training", selenium.getTitle());		
		System.out.println("logging in to " + selenium.getLocation() + " as " + username);
		inputUserNameAndPassword(selenium, username, password);	

		boolean cannotLogin = SeleniumHelper.isTextPresent(selenium, "OLAT user name or password invalid", 20);	//English error message		
		cannotLogin |= SeleniumHelper.isTextPresent(selenium, "Fehler", 20);	// German error message
		/*if(!cannotLogin) {
			fail("Expected to get an error message while login!");
		}*/
		selenium.click("ui=dmz::loginErrorOK()");	
		return cannotLogin;
	}

}
