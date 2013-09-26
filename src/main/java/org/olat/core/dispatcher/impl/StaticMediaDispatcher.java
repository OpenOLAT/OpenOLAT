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
package org.olat.core.dispatcher.impl;

import java.io.File;
import java.io.UnsupportedEncodingException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.core.dispatcher.Dispatcher;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.helpers.Settings;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.LogDelegator;
import org.olat.core.util.WebappHelper;

/**
 * <h3>Description:</h3> A dispatcher that delivers raw static files without any
 * servlet intervention or security checks directly from the webapp/static
 * directory.
 * <p>
 * The URL contains the web app version ID to make sure browsers always fetch
 * the newest version after a new release to prevent browser caching issues.
 * <p>
 * This should only be used to deliver basic files from the body.html and some
 * other static resource. When developing modules, put all your static files
 * like js libraries or other resource into the _static resources folder and
 * include them using the JSAndCSSComponent.java or get the URL to those
 * resources from the ClassPathStaticDispatcher.java
 * <p>
 * Initial Date: 16.05.2007 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */
public class StaticMediaDispatcher extends LogDelegator implements Dispatcher {
	public static String STATIC_DIR_NAME = "/static";
	public static String NOVERSION = "_noversion_";
	private static String mapperPath;

	/**
	 * Constructor
	 * 
	 * @param mapperPathFromConfig
	 */
	public StaticMediaDispatcher(String mapperPathFromConfig) {
		mapperPath = mapperPathFromConfig;
	}

	/**
	 * @see org.olat.core.dispatcher.Dispatcher#execute(javax.servlet.http.HttpServletRequest,
	 *      javax.servlet.http.HttpServletResponse, java.lang.String)
	 */
	@Override
	public void execute(HttpServletRequest request, HttpServletResponse response, String uriPrefix) {
		String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			// huh? What's this, send not found, don't know what to do here
			if (isLogDebugEnabled()) {
				logDebug("PathInfo is null for static request URI::" + request.getRequestURI(), null);
			}
			ServletUtil.serveResource(request, response, new NotFoundMediaResource("error"));
			return;
		}
		// remove uri prefix and version from request if available
		String staticRelPath = null;
		if (pathInfo.indexOf(NOVERSION) != -1) {
			// no version provided - only remove mapper
			staticRelPath = pathInfo.substring(mapperPath.length() + 1 + NOVERSION.length(), pathInfo.length());
		}
		else {
			// version provided - remove it
			String version = Settings.getBuildIdentifier();
			int start = mapperPath.length() + 1 + version.length();
			int end = pathInfo.length();
			if(start <= end) {
				staticRelPath = pathInfo.substring(start, end);
			} else {
				ServletUtil.serveResource(request, response, new NotFoundMediaResource("error"));
				return;
			}
		}
		
		// remove any .. in the path
		String normalizedRelPath = normalizePath(staticRelPath);
		if (normalizedRelPath == null) {
			if (isLogDebugEnabled()) {
				logDebug("Path is null after noralizing for static request URI::" + request.getRequestURI(), null);
			}
			ServletUtil.serveResource(request, response, new NotFoundMediaResource("error"));
			return;
		}
		// create the file from the path
		String staticAbsPath;
		if(Settings.isDebuging() && WebappHelper.getWebappSourcePath() != null) {
			staticAbsPath = WebappHelper.getWebappSourcePath() + STATIC_DIR_NAME;
		} else {
			staticAbsPath = WebappHelper.getContextRoot() + STATIC_DIR_NAME;
		}
		File staticFile = new File(staticAbsPath, normalizedRelPath);
		
		// try loading themes from custom themes folder if configured 
		if (!staticFile.exists() && normalizedRelPath.contains("/themes/") && Settings.getGuiCustomThemePath() != null) {
			File customThemesDir = Settings.getGuiCustomThemePath();
			String path = staticFile.getAbsolutePath();
			path = path.substring(path.indexOf("/static/themes/") + 15);
			staticFile = new File(customThemesDir, path);
		}

		// only serve if file exists
		if (!staticFile.exists()) {
			if (isLogDebugEnabled()) {
				logDebug("File does not exist for URI::" + request.getRequestURI(), null);
			}
			// try fallback without version ID
			staticRelPath = pathInfo.substring(mapperPath.length() , pathInfo.length());
			normalizedRelPath = normalizePath(staticRelPath);
			staticAbsPath = WebappHelper.getContextRoot() + STATIC_DIR_NAME + normalizedRelPath;
			staticFile = new File(staticAbsPath);
			if (!staticFile.exists()) {
				ServletUtil.serveResource(request, response, new NotFoundMediaResource("error"));
				return;
			} 
			// log as error, file exists but wrongly mapped
			logWarn("File exists but not mapped using version - use StaticMediaDispatch methods to create URL of static files! invalid URI::" + request.getRequestURI(), null);			
		}

		if (isLogDebugEnabled()) {
			logDebug("Serving resource URI::" + request.getRequestURI(), null);
		}
		// Everything is ok, serve resource
		MediaResource resource = new FileMediaResource(staticFile);
		ServletUtil.serveResource(request, response, resource);
	}

	/**
	 * Return a context-relative path, beginning with a "/", that represents the
	 * canonical version of the specified path
	 * <p>
	 * ".." and "." elements are resolved out. If the specified path attempts to
	 * go outside the boundaries of the current context (i.e. too many ".." path
	 * elements are present), return <code>null</code> instead.
	 * <p>
	 * 
	 * @author Mike Stock
	 * 
	 * @param path Path to be normalized
	 * @return the normalized path
	 */
	public static String normalizePath(String path) {
		if (path == null) return null;

		// Create a place for the normalized path
		String normalized = path;

		try { // we need to decode potential UTF-8 characters in the URL
			normalized = new String(normalized.getBytes(), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			throw new AssertException("utf-8 encoding must be supported on all java platforms...");
		}

		if (normalized.equals("/.")) return "/";

		// Normalize the slashes and add leading slash if necessary
		if (normalized.indexOf('\\') >= 0) normalized = normalized.replace('\\', '/');
		if (!normalized.startsWith("/")) normalized = "/" + normalized;

		// Resolve occurrences of "//" in the normalized path
		while (true) {
			int index = normalized.indexOf("//");
			if (index < 0) break;
			normalized = normalized.substring(0, index) + normalized.substring(index + 1);
		}

		// Resolve occurrences of "/./" in the normalized path
		while (true) {
			int index = normalized.indexOf("/./");
			if (index < 0) break;
			normalized = normalized.substring(0, index) + normalized.substring(index + 2);
		}

		// Resolve occurrences of "/../" in the normalized path
		while (true) {
			int index = normalized.indexOf("/../");
			if (index < 0) break;
			if (index == 0) return (null); // Trying to go outside our context
			int index2 = normalized.lastIndexOf('/', index - 1);
			normalized = normalized.substring(0, index2) + normalized.substring(index + 3);
		}

		// Return the normalized path that we have completed
		return (normalized);
	}

	/**
	 * Note: use only rarely - all non-generic js libs and css classes should be
	 * included using JsAndCssComponent, and all images should be referenced with
	 * the css background-image capability. <br>
	 * renders a uri which is mounted to the webapp/static/ directory of your web
	 * application.
	 * <p>
	 * This method will add a version ID to the path that guarantees that the
	 * browser fetches the file again when you release a new version of your
	 * application.
	 * 
	 * @param target
	 * @param URI e.g. img/specialimagenotpossiblewithcss.jpg
	 */
	public static void renderStaticURI(StringOutput target, String URI) {
		renderStaticURI(target, URI, true);
	}
	
	public static String getStaticURI(String uri) {
		StringOutput target = new StringOutput();
		renderStaticURI(target, uri, true);
		return target.toString();
	}

	/**
	 * Render a static URL to resource. This is only used in special cases, in
	 * most scenarios you should use the JSAndCssComponent
	 * 
	 * @param target The output target
	 * @param URI e.g. img/specialimagenotpossiblewithcss.jpg
	 * @param addVersionID true: the build version is added to the URL to force
	 *          browser reload the resource when releasing a new version; false:
	 *          don't add version (but allow browsers to cache even when resource
	 *          has changed). Only use false when really needed
	 */
	public static void renderStaticURI(StringOutput target, String URI, boolean addVersionID) {
		String root = WebappHelper.getServletContextPath();
		target.append(root); // e.g /olat
		target.append(mapperPath); // e.g. /raw/
		// Add version to make URL change after new release and force browser to
		// load new static files
		if (addVersionID) {
			target.append(Settings.getBuildIdentifier());
		} else {
			target.append(NOVERSION);			
		}
		target.append("/");			
		if (URI != null) target.append(URI);
	}

	/**
	 * Create a static URI for this relative URI. Helper method in case no String
	 * output is available. 
 	 * <p>
	 * This method will add a version ID to the path that guarantees that the
	 * browser fetches the file again when you release a new version of your
	 * application.

	 * @param URI e.g. img/specialimagenotpossiblewithcss.jpg
	 * @return
	 */
	public static String createStaticURIFor(String URI) {
		return createStaticURIFor(URI, true);
	}
	
	/**
	 * Create a static URI for this relative URI. Helper method in case no String
	 * output is available. 
	 * 
	 * @param URI e.g. img/specialimagenotpossiblewithcss.jpg
	 * @param addVersionID true: the build version is added to the URL to force
	 *          browser reload the resource when releasing a new version; false:
	 *          don't add version (but allow browsers to cache even when resource
	 *          has changed). Only use false when really needed
	 * @return
	 */
	public static String createStaticURIFor(String URI, boolean addVersionID) {
		StringOutput so = new StringOutput();
		renderStaticURI(so, URI, addVersionID);
		return so.toString();
	}
	
	public static String getStaticMapperPath() {
		return mapperPath;
	}
	
}
