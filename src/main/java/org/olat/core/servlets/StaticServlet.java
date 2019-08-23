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
package org.olat.core.servlets;

import org.olat.admin.layout.StaticDirectory;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.olat.admin.sysinfo.manager.CustomStaticFolderManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.helpers.Settings;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.springframework.beans.factory.annotation.Autowired;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * 
 * Deliver the file in /raw/
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StaticServlet extends HttpServlet {

	private static final long serialVersionUID = -2430002903299685192L;
	private static final Logger log = Tracing.createLoggerFor(StaticServlet.class);
	private static final long CACHE_DURATION_IN_SECOND = 60l * 60l * 24l * 8l; // 8 days
	private static final long CACHE_DURATION_IN_MS = CACHE_DURATION_IN_SECOND  * 1000;

	@Autowired
	private StaticDirectory[] staticDirectories;

	public StaticServlet() {
		CoreSpringFactory.autowireObject(this);
	}
	public static final String STATIC_DIR_NAME = "/static";
	public static final String NOVERSION = "_noversion_";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp)
	throws ServletException, IOException {
		String userAgent = req.getHeader("User-Agent");
		if(userAgent != null && userAgent.indexOf("BitKinex") >= 0) {
			//BitKinex isn't allow to see this context
			resp.sendError(HttpServletResponse.SC_FORBIDDEN);
		} else {
			super.service(req, resp);
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response)
	throws ServletException, IOException {
		final String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			// huh? What's this, send not found, don't know what to do here
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else if (pathInfo.contains(NOVERSION)) {
			// no version provided - only remove mapper
			String staticRelPath = pathInfo.substring(NOVERSION.length() + 1, pathInfo.length());
			String normalizedRelPath = ServletUtil.normalizePath(staticRelPath);
			if (normalizedRelPath == null) {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			} else if(normalizedRelPath.endsWith("transparent.gif")){
				deliverStatic(request, response, pathInfo, normalizedRelPath, true);
			} else {
				deliverStatic(request, response, pathInfo, normalizedRelPath, false);
			}
		} else if (pathInfo.startsWith(STATIC_DIR_NAME)) {
			String staticRelPath = pathInfo.substring(STATIC_DIR_NAME.length() + 1, pathInfo.length());
			//customizing 
			CustomStaticFolderManager folderManager = CoreSpringFactory.getImpl(CustomStaticFolderManager.class);
			File file = new File(folderManager.getRootFile(), staticRelPath);
			if(file.exists()) {
				if(file.isDirectory()) {
					response.sendError(HttpServletResponse.SC_FORBIDDEN);
				} else {
					MediaResource resource = new FileMediaResource(file);
		    		ServletUtil.serveResource(request, response, resource);
				}
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		} else {
			// version provided - remove it
			int start = pathInfo.indexOf("/", 2);
			int end = pathInfo.length();
			if(start <= end) {
				String staticRelPath = pathInfo.substring(start, end);
				String normalizedRelPath = ServletUtil.normalizePath(staticRelPath);
				if (normalizedRelPath == null) {
					response.sendError(HttpServletResponse.SC_NOT_FOUND);
				} else {
					boolean expiration = !Settings.isDebuging();
					deliverStatic(request, response, pathInfo, normalizedRelPath, expiration);
				}
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
		}	
	}

	public static URL getStaticResource(StaticDirectory[] staticDirectories,
										String subPath) {
		URL result = null;
		try {
			for (StaticDirectory staticDirectory : staticDirectories) {
				String path = "/" + staticDirectory.getName() + subPath;
				result = CoreSpringFactory.servletContext.getResource(path);
				if (result != null) {
					break;
				}
			}
			if (result == null) {
				result = CoreSpringFactory.servletContext.getResource(subPath);
			}
		} catch (MalformedURLException e) {
			assert false : e;
		}
		return result;
	}
	
	private void deliverStatic(HttpServletRequest request, HttpServletResponse response,
		String pathInfo, String normalizedRelPath, boolean expiration)
	throws IOException {
		// create the file from the path
		String staticAbsPath;
		if(Settings.isDebuging() && WebappHelper.getWebappSourcePath() != null) {
			staticAbsPath = WebappHelper.getWebappSourcePath() + STATIC_DIR_NAME;
			expiration &= false;
		} else {
			staticAbsPath = WebappHelper.getContextRealPath(STATIC_DIR_NAME);
			if(staticAbsPath == null) {
				staticAbsPath = WebappHelper.getContextRoot() + STATIC_DIR_NAME;
			}
			expiration &= true;
		}

		URL url = getStaticResource(staticDirectories, normalizedRelPath);

		if (url == null) {
			// try loading themes from custom themes folder if configured
			if (Settings.getGuiCustomThemePath() != null && normalizedRelPath.contains("/themes/")) {
				File customThemesDir = Settings.getGuiCustomThemePath();
				String path = new File(staticAbsPath, normalizedRelPath).getAbsolutePath();
				path = path.substring(path.indexOf("/static/themes/") + 15);
				url = new File(customThemesDir + path).toURI().toURL();
				expiration &= false;//themes can be update any time
			} else if (normalizedRelPath.contains("/js/images/ui-")) {
				normalizedRelPath = normalizedRelPath.replace("/js/images/ui-", "/js/jquery/ui/images/ui-");
				url = request.getServletContext().getResource(staticAbsPath + normalizedRelPath);
			}

			// only serve if file exists
			if (url == null) {
				// try fallback without version ID
				String fallbackPath = pathInfo.substring(1, pathInfo.length());
				fallbackPath = ServletUtil.normalizePath(fallbackPath);
				String fallbackAbsPath = WebappHelper.getContextRealPath(STATIC_DIR_NAME + fallbackPath);
				if (fallbackAbsPath == null) {
					url = request.getServletContext().getResource(fallbackAbsPath);
					if (url == null) {
						String realPath = request.getServletContext().getRealPath("/static" + normalizedRelPath);
						url = request.getServletContext().getResource(realPath);
					}
				}
				// log as error, file exists but wrongly mapped
				log.warn("File exists but not mapped using version - use StaticMediaDispatch methods to create URL of static files! invalid request URI " + request.getRequestURI() + " initiated by " + request.getHeader("referer"));
			}
		}

		if (url == null) {
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
		} else if(url.getFile().endsWith("/")) {
			//directory listing is forbidden
			response.sendError(HttpServletResponse.SC_FORBIDDEN);
		} else {
			deliverFile(request, response, url, expiration);
		}
	}
	
	private void deliverFile(HttpServletRequest request, HttpServletResponse response, URL url, boolean expiration) throws IOException {
		URLConnection urlConnection = url.openConnection();
		long lastModified = urlConnection.getLastModified();
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if (ifModifiedSince >= (lastModified / 1000L) * 1000L) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		} else {
			response.setDateHeader("Last-Modified", lastModified);
			if (expiration) {
				long now = System.currentTimeMillis();
				//res being the HttpServletResponse of the request
				response.addHeader("Cache-Control", "max-age=" + CACHE_DURATION_IN_SECOND);
				response.setDateHeader("Expires", now + CACHE_DURATION_IN_MS);
			}

			String fileName = url.getFile();
			String mimeType = WebappHelper.getMimeType(fileName);
			response.setContentType(mimeType);
			response.setContentLengthLong(urlConnection.getContentLengthLong());

			try (InputStream in = urlConnection.getInputStream()) {
				FileUtils.cpio(in, response.getOutputStream(), "static");
			} catch(Exception ex) {
				log.error("", ex);
			}
		}	
	}
}
