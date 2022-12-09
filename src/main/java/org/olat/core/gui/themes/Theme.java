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
package org.olat.core.gui.themes;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.impl.StaticMediaDispatcher;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.GUISettings;
import org.olat.core.helpers.Settings;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.WebappHelper;

/**
 * <h3>Description:</h3> A class that represents a GUI theme
 * <p>
 * Initial Date: 31.03.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class Theme {

	private static final Logger log = Tracing.createLoggerFor(Theme.class);
	
	public static final String DEFAULTTHEME = "light";
	private static final String CUSTOM_JS_FILENAME = "theme.js";
	private static final String CUSTOM_FAVICON_ICO_FILENAME = "favicon.ico"; // legacy
	private static final String CUSTOM_FAVICON_PNG16_FILENAME = "meta/favicon16.png";
	private static final String CUSTOM_FAVICON_PNG32_FILENAME = "meta/favicon32.png";
	private static final String CUSTOM_FAVICON_PNG64_FILENAME = "meta/favicon64.png";
	private static final String CUSTOM_APPICON_PNG180_FILENAME = "meta/appicon180.png";
	private static final String CUSTOM_TILEICON_PNG70_FILENAME = "meta/tileicon70.png";
	private static final String CUSTOM_TILEICON_PNG150_FILENAME = "meta/tileicon150.png";
	private static final String CUSTOM_TILEICON_PNG310_FILENAME = "meta/tileicon310.png";
	private static final String CUSTOM_MANIFEST_FILENAME = "meta/manifest.json";
	private static final String CUSTOM_MS_APPLICATION_CONFIG_FILENAM = "meta/msapplication-config.xml";
	private static final String CUSTOM_EMAIL_CSS_FILENAME = "email.css";
	
	private String identifyer;
	private String baseURI;
	private String relPathToThemesDir;
	private String htmlHeaderElements;
	private String emailCss;

	/**
	 * Theme is cached and shared with all sessions.
	 * 
	 * @param name
	 *            The unique theme identifyer
	 */
	public Theme(String themeIdentifyer) {
		init(themeIdentifyer);
	}

	/**
	 * @return The identifyer of the theme
	 */
	public String getIdentifyer() {
		return identifyer;
	}

	/**
	 * @return The base URI for this theme, e.g.
	 *         'http://www.myserver.com/olat/raw/61x/themes/default/'
	 */
	public String getBaseURI() {
		return baseURI;
	}

	/**
	 * @return the http header elements used to implement this theme
	 */
	public String renderHTMLHeaderElements() {
		return htmlHeaderElements;
	}
	
	/**
	 * returns the relative path to the custom js <br />
	 * ( check first with <code>hasCustomJS()</code> )<br />
	 * <p>
	 * Example usage:<br />
	 * <br />
	 * 
	 * <code>if (currTheme.hasCustomJS()) <br />
	 * 	  CustomJSComponent customJS = new CustomJSComponent("customThemejs", new String[] { currTheme.getFullPathToCustomJS() });</code>
	 * </p>
	 * 
	 * @return the path to the custom layout js ::
	 *         themes/frentix/theme.js
	 */
	public String getRelPathToCustomJS() {
		return relPathToThemesDir + CUSTOM_JS_FILENAME;
	}
	
	public String getEmailCss() {
		return emailCss;
	}

	/**
	 * Update values in this theme with the values from the given identifyer.
	 * 
	 * @param theme
	 */
	public void init(String themeIdentifyer) {
		this.identifyer = themeIdentifyer;
		// Themes are delivered as static resources by StaticMediaDispatcher
		this.relPathToThemesDir = "themes/" + themeIdentifyer + "/";
		StringOutput themePath = new StringOutput();
		StaticMediaDispatcher.renderStaticURI(themePath, relPathToThemesDir);
		this.baseURI = themePath.toString();
		
		// Build theme header include string with resources available in the theme
		this.htmlHeaderElements = buildHTMLHeaderElements();
		
		this.emailCss = loadEmailCss();
	}

	
	/**
	 * Helper to generate the theme relevant html header elements
	 * @return
	 */
	private String buildHTMLHeaderElements() {
		StringBuilder sb = new StringBuilder(512);
		File themeFolder = getThemeFolder();
		// Include the theme css file
		sb.append("<link id='o_theme_css' href='").append(baseURI).append("theme.css' rel='stylesheet'>\n");
		// Include the email css file. It is necessary because AntiSAMY filters the styles in the OpenOLAT email module.
		Path cssPath = getEmailCssPath();
		if (Files.exists(cssPath)) {
			sb.append("<link id='o_email_css' href='").append(baseURI).append("email.css' rel='stylesheet'>\n");
		}
		// Include custom theme javascript file, for login caroussel, js-based layout patches etc
		if (new File(themeFolder,CUSTOM_JS_FILENAME).exists()) {
			sb.append("<script src='").append(baseURI).append(CUSTOM_JS_FILENAME).append("'></script>\n");
		}
		// Include the favicons in legacy .ico format and others in png format and different resolutions
		if (new File(themeFolder,CUSTOM_FAVICON_ICO_FILENAME).exists()) {	
			sb.append("<link rel='icon' href='").append(baseURI).append(CUSTOM_FAVICON_ICO_FILENAME).append("' type='image/x-icon'>\n");
		}
		if (new File(themeFolder,CUSTOM_FAVICON_PNG16_FILENAME).exists()) {
			sb.append("<link rel='icon' href='").append(baseURI).append(CUSTOM_FAVICON_PNG16_FILENAME).append("' type='image/png' sizes='16x16'>\n");
		}
		if (new File(themeFolder,CUSTOM_FAVICON_PNG32_FILENAME).exists()) {
			sb.append("<link rel='icon' href='").append(baseURI).append(CUSTOM_FAVICON_PNG32_FILENAME).append("' type='image/png' sizes='32x32'>\n");
		}
		if (new File(themeFolder,CUSTOM_FAVICON_PNG64_FILENAME).exists()) {
			sb.append("<link rel='icon' href='").append(baseURI).append(CUSTOM_FAVICON_PNG64_FILENAME).append("' type='image/png' sizes='64x64'>\n");
		}
		// Include high-res apple app/touch icon
		if (new File(themeFolder,CUSTOM_APPICON_PNG180_FILENAME).exists()) {
			sb.append("<link rel='apple-touch-icon' href='").append(baseURI).append(CUSTOM_APPICON_PNG180_FILENAME).append("' type='image/png' sizes='180x180'>\n");
		}
		// Include Google manifest file
		if (new File(themeFolder,CUSTOM_MANIFEST_FILENAME).exists()) {
			sb.append("<link rel='manifest' href='").append(baseURI).append(CUSTOM_MANIFEST_FILENAME).append("'>\n");
		}
		// Include Microsoft application config file (make sure any referenced image in the file has absolute path configuration
		if (new File(themeFolder,CUSTOM_MS_APPLICATION_CONFIG_FILENAM).exists()) {
			sb.append("<meta name='msapplication-config' content='").append(baseURI).append(CUSTOM_MS_APPLICATION_CONFIG_FILENAM).append("'>\n");
		} else {
			sb.append("<meta name='msapplication-TileColor' content='").append("#ffffff").append("'>\n");
			if (new File(themeFolder,CUSTOM_TILEICON_PNG70_FILENAME).exists()) {
				sb.append("<meta name='msapplication-square70x70logo' content='").append(baseURI).append(CUSTOM_TILEICON_PNG70_FILENAME).append("'>\n");
			}
			if (new File(themeFolder,CUSTOM_TILEICON_PNG150_FILENAME).exists()) {
				sb.append("<meta name='msapplication-square150x150logo' content='").append(baseURI).append(CUSTOM_TILEICON_PNG150_FILENAME).append("'>\n");
			}
			if (new File(themeFolder,CUSTOM_TILEICON_PNG310_FILENAME).exists()) {
				sb.append("<meta name='msapplication-square310x310logo' content='").append(baseURI).append(CUSTOM_TILEICON_PNG310_FILENAME).append("'>\n");
			}
		}
		
		return sb.toString();
	}
	
	private String loadEmailCss() {
		Path css = getEmailCssPath();
		if (Files.exists(css)) {
			try {
				return new String(Files.readAllBytes(css));
			} catch (IOException e) {
				log.error("Loading the email CSS file of the the theme failed.", e);
			}
		}
		return "";
	}

	private Path getEmailCssPath() {
		File themeFolder = getThemeFolder();
		return themeFolder.toPath().resolve(CUSTOM_EMAIL_CSS_FILENAME);
	}

	private File getThemeFolder() {
		// 1) lookup theme in release files
		String staticThemesPath = getThemesFolderPath();
		String guiThemIdentifyer = CoreSpringFactory.getImpl(GUISettings.class).getGuiThemeIdentifyer();
		File themeFolder = new File(staticThemesPath, guiThemIdentifyer);
		if (!themeFolder.exists() && Settings.getGuiCustomThemePath() != null) {
			// 2) fallback to custom themes folder
			themeFolder = new File(Settings.getGuiCustomThemePath(), guiThemIdentifyer);
		}
		return themeFolder;
	}

	private String getThemesFolderPath() {
		String staticThemesPath = WebappHelper.getContextRealPath("/static/themes/");
		if (staticThemesPath == null) {
			staticThemesPath = WebappHelper.getContextRoot() + "/static/themes/";
		}
		return staticThemesPath;
	}
}
