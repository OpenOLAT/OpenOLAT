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
package org.olat.core.commons.services.help;

import java.util.Date;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.util.EntityUtils;
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.httpclient.HttpClientService;

/**
 * Helper to create openolat confluence help links.  
 * 
 * Help links have the following form:<br/>
 * https://confluence.openolat.org/display/OO100DE/OpenOLAT+10+Benutzerhandbuch
 * 
 * Initial date: 07.01.2015<br>
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConfluenceHelper {
	private static final Logger logger = Tracing.createLoggerFor(ConfluenceHelper.class);

	private static final Map<String, String> spaces = new ConcurrentHashMap<>();
	private static final Map<String, String> translatedPages = new ConcurrentHashMap<>();
	private static final Map<String, Date> translatTrials = new ConcurrentHashMap<>();

	private static final String confluenceBaseUrl = "https://confluence.openolat.org";
	private static final String confluenceDisplayUrl = confluenceBaseUrl + "/display";
	private static final String confluencePagesUrl = confluenceBaseUrl + "/pages/";
	
	public static final Locale EN_Locale = new Locale("en");
	public static final Locale DE_Locale = new Locale("de");

	public static final String getURL(Locale locale, String page) {
		StringBuilder sb = new StringBuilder(64);
		sb.append(confluenceDisplayUrl);
		String space = spaces.get(locale.toString());
		if (space == null) {
			// Generate space only once per language, version does not change at
			// runtime
			String version = Settings.getVersion();
			space = generateSpace(version, locale);
			spaces.putIfAbsent(locale.toString(), space);
		}
		sb.append(space);
		if (page != null) {
			int anchorPos = page.indexOf("#");
			if (anchorPos != -1) {
				// Page with anchor: real page name + anchor
				String realPage = page.substring(0, anchorPos);
				String anchor = page.substring(anchorPos + 1);

				// Special case for non-en spaces: CustomWare Redirection Plugin
				// can not redirect pages with anchors. We need to fix it here
				// by fetching the page and lookup the redirect path. Ugly, but
				// we see no other option here.
				if (!locale.getLanguage().equals(EN_Locale.getLanguage())) {
					String redirectedPage = getPageFromAlias(getURL(locale, realPage));
					if (redirectedPage != null) {
						realPage = redirectedPage;
					}
					// else realPage part stays the same - anchor won't work but
					// at least the right page is loading
				}

				// Confluence has some super-fancy way to addressing pages with
				// anchors
				sb.append(realPage.replaceAll(" ", "%20"));
				sb.append("#").append(realPage.replaceAll(" ", "")).append("-").append(anchor);

			} else {
				// Page without anchor
				sb.append(page.replaceAll(" ", "%20"));
			}
		}
		return sb.toString();
	}

	/**
	 * Convert 10.0 -> 100<br/>
	 * Convert 10.1.1 -> 101
	 * 
	 * 
	 * @param version
	 * @param locale
	 * @return
	 */
	protected static final String generateSpace(String version, Locale locale) {
		StringBuilder sb = new StringBuilder();
		sb.append("/OO");
		
		version = version.replace("pre", "0");
		int firstPointIndex = version.indexOf('.');
		if (firstPointIndex > 0) {
			sb.append(version.substring(0, firstPointIndex));
			int secondPointIndex = version.indexOf('.', firstPointIndex + 1);
			if (secondPointIndex > firstPointIndex) {
				sb.append(version.substring(firstPointIndex + 1, secondPointIndex));
			} else if (firstPointIndex + 1 < version.length()) {
				String subVersion = version.substring(firstPointIndex + 1);
				char[] subVersionArr = subVersion.toCharArray();
				for (int i = 0; i < subVersionArr.length && Character.isDigit(subVersionArr[i]); i++) {
					sb.append(subVersionArr[i]);
				}
			} else {
				sb.append("0");
			}
		} else {
			char[] versionArr = version.toCharArray();
			for (int i = 0; i < versionArr.length && Character.isDigit(versionArr[i]); i++) {
				sb.append(versionArr[i]);
			}
			// add minor version
			sb.append("0");
		}

		if (locale.getLanguage().equals(DE_Locale.getLanguage())) {
			sb.append("DE/");
		} else {
			sb.append("EN/");
		}

		return sb.toString();
	}
	
	public static final Component createHelpPageLink(UserRequest ureq, String title, String tooltip, String iconCSS, String elementCSS,
			String page) {
		ExternalLink helpLink = new ExternalLink("topnav.help." + page);
		helpLink.setName(title);
		helpLink.setTooltip(tooltip);
		helpLink.setIconLeftCSS(iconCSS);
		helpLink.setElementCssClass(elementCSS);
		helpLink.setTarget("oohelp");
		helpLink.setUrl(getURL(ureq.getLocale(), page));
		return helpLink;
	}

	/**
	 * Fetch the redirected page name for the given URL. Note that this is
	 * executed asynchronously, meaning that the first time this method is
	 * executed for a certain URL it will return null. As soon as the code could
	 * get the redirection from the confluence server it will return the
	 * redirected page name instead.
	 * 
	 * @param aliasUrl
	 * @return The translated page name or NULL if not found
	 */
	private static final String getPageFromAlias(String aliasUrl) {
		if (StringHelper.containsNonWhitespace(aliasUrl)) {
			String translatedPage = translatedPages.get(aliasUrl);
			if (translatedPage != null) {
				return translatedPage;
			}
			// Not in cache. Start a background thread to fetch the translated
			// page from the confluence. Since this can take several seconds, we
			// exit here with null. Next time the page is loaded the translated
			// page will be in the cache. 
			
			HttpClientService httpClientService = CoreSpringFactory.getImpl(HttpClientService.class);
			// Do this only once per 30 mins per page. Confluence might be down
			// or another user already trigger the fetch.
			Date lastTrial = translatTrials.get(aliasUrl);
			Date now = new Date();
			if (lastTrial == null || lastTrial.getTime() < (now.getTime() - (1800 * 1000))) {
				translatTrials.put(aliasUrl, now);				
				new Thread() {
					@Override
					public void run() {
						try(CloseableHttpClient httpClient = httpClientService.createThreadSafeHttpClient(false)) {
							// Phase 1: lookup alias redirect
							HttpGet httpMethod = new HttpGet(aliasUrl);
							httpMethod.setHeader("User-Agent", Settings.getFullVersionInfo());
							HttpResponse response = httpClient.execute(httpMethod);
							int httpStatusCode = response.getStatusLine().getStatusCode();
							// Looking at the HTTP status code tells us whether a
							// user with the given MSN name exists.
							if (httpStatusCode == HttpStatus.SC_OK) {
								String body = EntityUtils.toString(response.getEntity());
								// Page contains a javascript redirect call, extract
								// redirect location
								int locationPos = body.indexOf("location.replace('");
								if (locationPos == -1) {
									return;
								}
								int endPos = body.indexOf("'", locationPos + 18);
								if (endPos == -1) {
									return;
								}
								// Remove the path to extract the page name
								String path = body.substring(locationPos + 18, endPos);
								String translatedPath = path.substring(path.lastIndexOf("/") + 1);
								translatedPath = translatedPath.replaceAll("\\+", " ");
								
								// Phase 2:Lookup real page name in confluence
								// if this just a stupid confluence page ID
								// instead of the page name. This totally breaks
								// the anchor mechanism which is broken anyway,
								// we need the real page name. For some reason
								// confluence does not always adress pages using
								// the page name, sometimes the page ID is used.
								// Anchors do not work on such pages. For iso
								// latin pages this should be fine for most
								// cases.
								if (translatedPath.indexOf("viewpage.action?pageId") != -1) {
									String redirectUrl = confluencePagesUrl + translatedPath;
									httpMethod = new HttpGet(redirectUrl);
									httpMethod.setHeader("User-Agent", Settings.getFullVersionInfo());
									response = httpClient.execute(httpMethod);
									httpStatusCode = response.getStatusLine().getStatusCode();
									// Looking at the HTTP status code tells us whether a
									// user with the given MSN name exists.
									if (httpStatusCode == HttpStatus.SC_OK) {
										body = EntityUtils.toString(response.getEntity());
										// Page contains a javascript redirect call, extract
										// redirect location
										int titlePos = body.indexOf("ajs-page-title");
										if (titlePos == -1) {
											return;
										}
										endPos = body.indexOf("\"", titlePos + 25);
										if (endPos == -1) {
											return;
										}
										// Remove the path to extract the page name
										path = body.substring(titlePos + 25, endPos);
										translatedPath = path.substring(path.lastIndexOf("/") + 1);
										translatedPath = translatedPath.replaceAll("\\+", " ");
										// Check if this just a stupid page ID instead
										// of the page name. This totally breaks the
										// anchor stuff, we need the real page name. For
										// iso latin pages this should be fine for most cases.
									}								
								}
								
								// We're done. Put to cache for next retrieval
								translatedPages.putIfAbsent(aliasUrl, translatedPath);
							}
						} catch (Exception e) {
							logger.warn("Error while getting help page from EN alias", e);
						}
					}
				}.start();
			}
			return null;

		}
		return null;
	}
}
