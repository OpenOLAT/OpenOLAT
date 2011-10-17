package org.olat.test.util.setup;

import java.net.MalformedURLException;
import java.net.URL;

/**
 * Simple wrapper object around login information
 * to log into OLAT.
 * <p>
 * The information provided here should be sufficient to
 * create a selenium session with it and log into OLAT.
 * Hence we need the hostname of the selenium server
 * and the hostname + login details of the OLAT server.
 * @author Stefan
 *
 */
public class OlatLoginInfos {

	private final String seleniumHostname_;
	private final String seleniumBrowserId_;
	private final URL fullOlatServerUrl_;
	private final String username_;
	private final String password_;
	
	public OlatLoginInfos(String seleniumHostname, String seleniumBrowserId,
							String fullOlatServerUrl, String username, String password) throws MalformedURLException {
		if (isNullOrEmpty(seleniumHostname)) {
			throw new IllegalArgumentException("seleniumHostname is null or empty");
		}
		if (isNullOrEmpty(seleniumBrowserId)) {
			throw new IllegalArgumentException("seleniumBrowserId is null or empty");
		}
		if (isNullOrEmpty(fullOlatServerUrl)) {
			throw new IllegalArgumentException("fullOlatServerUrl is null or empty");
		}
		if (isNullOrEmpty(username)) {
			throw new IllegalArgumentException("username is null or empty");
		}
		if (isNullOrEmpty(password)) {
			throw new IllegalArgumentException("password is null or empty");
		}
		seleniumHostname_ = seleniumHostname;
		seleniumBrowserId_ = seleniumBrowserId;
		fullOlatServerUrl_ = new URL(fullOlatServerUrl);
		username_ = username;
		password_ = password;
	}
	
	private boolean isNullOrEmpty(String param) {
		return param==null || param.length()==0;
	}

	public String getSeleniumHostname() {
		return seleniumHostname_;
	}
	
	public String getSeleniumBrowserId() {
		return seleniumBrowserId_;
	}
	
	public String getFullOlatServerUrl() {
		return fullOlatServerUrl_.toExternalForm();
	}
	
	public String getUsername() {
		return username_;
	}
	
	public String getPassword() {
		return password_;
	}

	public String getLanguage() {
		return "English";
	}
}
