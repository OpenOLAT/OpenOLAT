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

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.olat.admin.sysinfo.manager.CustomStaticFolderManager;
import org.olat.core.CoreSpringFactory;
import org.olat.core.dispatcher.DispatcherModule;
import org.olat.core.gui.media.FileMediaResource;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.helpers.Settings;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;

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

	public static final String STATIC_DIR_NAME = "/static";
	public static final String NOVERSION = "_noversion_";

	@Override
	protected void service(HttpServletRequest req, HttpServletResponse resp) {
		String userAgent = req.getHeader("User-Agent");
		if(userAgent != null && userAgent.indexOf("BitKinex") >= 0) {
			//BitKinex isn't allow to see this context
			DispatcherModule.sendForbidden(resp);
		} else {
			try {
				super.service(req, resp);
			} catch (ServletException | IOException e) {
				log.error("", e);
			}
		}
	}

	@Override
	protected void doGet(HttpServletRequest request, HttpServletResponse response) {
		final String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			// huh? What's this, send not found, don't know what to do here
			DispatcherModule.sendNotFound(response);
		} else if (pathInfo.indexOf(NOVERSION) != -1) {
			// no version provided - only remove mapper
			String staticRelPath = pathInfo.substring(NOVERSION.length() + 1, pathInfo.length());
			String normalizedRelPath = ServletUtil.normalizePath(staticRelPath);
			if (normalizedRelPath == null) {
				DispatcherModule.sendNotFound(response);
			} else if(normalizedRelPath.endsWith("transparent.gif")){
				deliverStatic(request, response, pathInfo, normalizedRelPath, true);
			} else {
				deliverStatic(request, response, pathInfo, normalizedRelPath, false);
			}
		} else if (pathInfo.startsWith(STATIC_DIR_NAME)) {
			String staticRelPath = pathInfo.substring(STATIC_DIR_NAME.length() + 1, pathInfo.length());
			String normalizedRelPath = ServletUtil.normalizePath(staticRelPath);
			if (normalizedRelPath == null) {
				DispatcherModule.sendNotFound(response);
			} else {
				//customizing 
				CustomStaticFolderManager folderManager = CoreSpringFactory.getImpl(CustomStaticFolderManager.class);
				File file = new File(folderManager.getRootFile(), normalizedRelPath);
				if(file.exists()) {
					if(file.isDirectory()) {
						DispatcherModule.sendForbidden(response);
					} else {
						MediaResource resource = new FileMediaResource(file);
			    		ServletUtil.serveResource(request, response, resource);
					}
				} else {
					DispatcherModule.sendNotFound(response);
				}
			}
		} else {
			// version provided - remove it
			int start = pathInfo.indexOf("/", 2);
			int end = pathInfo.length();
			if(start >= 2 && start <= end) {
				String staticRelPath = pathInfo.substring(start, end);
				String normalizedRelPath = ServletUtil.normalizePath(staticRelPath);
				if (normalizedRelPath == null) {
					DispatcherModule.sendNotFound(response);
				} else {
					boolean expiration = !Settings.isDebuging();
					deliverStatic(request, response, pathInfo, normalizedRelPath, expiration);
				}
			} else {
				DispatcherModule.sendNotFound(response);
			}
		}	
	}
	
	private void deliverStatic(HttpServletRequest request, HttpServletResponse response,
		String pathInfo, String normalizedRelPath, boolean expiration) {

		boolean notFound = false;
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

		File staticFile = new File(staticAbsPath, normalizedRelPath);
		if (!staticFile.exists()) {
			// try loading themes from custom themes folder if configured 
			if(normalizedRelPath.contains("/themes/") && Settings.getGuiCustomThemePath() != null) {
				File customThemesDir = Settings.getGuiCustomThemePath();
				String path = staticFile.getAbsolutePath();
				path = path.substring(path.indexOf("/static/themes/") + 15);
				staticFile = new File(customThemesDir, path);
			} else if(normalizedRelPath.contains("/js/images/ui-")) {
				normalizedRelPath = normalizedRelPath.replace("/js/images/ui-", "/js/jquery/ui/images/ui-");
				staticFile = new File(staticAbsPath, normalizedRelPath);
			}
			
			// only serve if file exists
			if (!staticFile.exists()) {
				// try fallback without version ID
				String fallbackPath = pathInfo.substring(1, pathInfo.length());
				fallbackPath = ServletUtil.normalizePath(fallbackPath);
				String fallbackAbsPath = WebappHelper.getContextRealPath(STATIC_DIR_NAME + fallbackPath);
				if(fallbackAbsPath != null) {
					staticFile = new File(fallbackAbsPath);
					if (!staticFile.exists()) {
						String realPath = request.getServletContext().getRealPath(STATIC_DIR_NAME + normalizedRelPath);
						staticFile = new File(realPath);
						if(!staticFile.exists()) {
							notFound = true;
						}
					}
				}
				// log as error, file exists but wrongly mapped
				log.warn("File exists but not mapped using version - use StaticMediaDispatch methods to create URL of static files! invalid URI::{}", request.getRequestURI());			
			}
		}
		
		if(notFound) {
			DispatcherModule.sendNotFound(response);
		} else if(staticFile.isDirectory()) {
			//directory listing is forbidden
			DispatcherModule.sendForbidden(response);
		} else {
			deliverFile(request, response, staticFile, expiration);
		}
	}
	
	private void deliverFile(HttpServletRequest request, HttpServletResponse response, File file, boolean expiration) {
		long lastModified = file.lastModified();
		long ifModifiedSince = request.getDateHeader("If-Modified-Since");
		if (ifModifiedSince >= (lastModified / 1000L) * 1000L) {
			response.setStatus(HttpServletResponse.SC_NOT_MODIFIED);
		} else {
			response.setDateHeader("Last-Modified", lastModified);
			if(expiration) {
				long now = System.currentTimeMillis();
				//res being the HttpServletResponse of the request
				response.addHeader("Cache-Control", "max-age=" + CACHE_DURATION_IN_SECOND);
				response.setDateHeader("Expires", now + CACHE_DURATION_IN_MS);
			}
			
			String mimeType = WebappHelper.getMimeType(file.getName());
			response.setContentType(mimeType);
			response.setContentLengthLong(file.length());

			try(InputStream in = new FileInputStream(file);
					BufferedInputStream bis = new BufferedInputStream(in, FileUtils.BSIZE)) {
				FileUtils.cpio(bis, response.getOutputStream(), "static");
			} catch(IOException e) {
				ServletUtil.handleIOException("", e);
			} catch(Exception ex) {
				log.error("", ex);
			}
		}	
	}
}
