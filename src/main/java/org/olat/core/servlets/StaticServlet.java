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

import java.io.File;
import java.io.IOException;

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
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
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
	private static final OLog log = Tracing.createLoggerFor(StaticServlet.class);

	public static String STATIC_DIR_NAME = "/static";
	public static String NOVERSION = "_noversion_";

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
		final boolean debug = log.isDebug();
		final String pathInfo = request.getPathInfo();
		if (pathInfo == null) {
			// huh? What's this, send not found, don't know what to do here
			if (debug) {
				log.debug("PathInfo is null for static request URI::" + request.getRequestURI(), null);
			}
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// remove uri prefix and version from request if available
		String staticRelPath;
		if (pathInfo.indexOf(NOVERSION) != -1) {
			// no version provided - only remove mapper
			staticRelPath = pathInfo.substring(NOVERSION.length() + 1, pathInfo.length());
		} else if (pathInfo.startsWith(STATIC_DIR_NAME)) {
			staticRelPath = pathInfo.substring(STATIC_DIR_NAME.length() + 1, pathInfo.length());
			//customizing 
			CustomStaticFolderManager folderManager = CoreSpringFactory.getImpl(CustomStaticFolderManager.class);
			File file = new File(folderManager.getRootFile(), staticRelPath);
			if(file.exists()) {
				MediaResource resource = new FileMediaResource(file);
		    	ServletUtil.serveResource(request, response, resource);
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
			}
			return;
		} else {
			// version provided - remove it
			String version;
			if(StringHelper.containsNonWhitespace(WebappHelper.getRevisionNumber())) {
				version = WebappHelper.getRevisionNumber() + ":" + WebappHelper.getChangeSet();
			} else {
				version = Settings.getBuildIdentifier();
			}	
			int start = version.length() + 1;
			int end = pathInfo.length();
			if(start <= end) {
				staticRelPath = pathInfo.substring(start, end);
			} else {
				response.sendError(HttpServletResponse.SC_NOT_FOUND);
				return;
			}
		}
		
		// remove any .. in the path
		String normalizedRelPath = ServletUtil.normalizePath(staticRelPath);
		if (normalizedRelPath == null) {
			if (debug) {
				log.debug("Path is null after noralizing for static request URI::" + request.getRequestURI(), null);
			}
			response.sendError(HttpServletResponse.SC_NOT_FOUND);
			return;
		}
		// create the file from the path
		String staticAbsPath;
		if(Settings.isDebuging() && WebappHelper.getWebappSourcePath() != null) {
			staticAbsPath = WebappHelper.getWebappSourcePath() + STATIC_DIR_NAME;
		} else {
			staticAbsPath = WebappHelper.getContextRealPath(STATIC_DIR_NAME);
		}
		
		File staticFile = new File(staticAbsPath, normalizedRelPath);
		if (!staticFile.exists()) {
			// try loading themes from custom themes folder if configured 
			if(normalizedRelPath.contains("/themes/") && Settings.getGuiCustomThemePath() != null) {
				File customThemesDir = Settings.getGuiCustomThemePath();
				String path = staticFile.getAbsolutePath();
				path = path.substring(path.indexOf("/static/themes/") + 15);
				staticFile = new File(customThemesDir, path);
			}
			
			// only serve if file exists
			if (!staticFile.exists()) {
				if (debug) {
					log.debug("File does not exist for URI::" + request.getRequestURI(), null);
				}
				// try fallback without version ID
				String fallbackPath = pathInfo.substring(1, pathInfo.length());
				fallbackPath = ServletUtil.normalizePath(fallbackPath);
				String fallbackAbsPath = WebappHelper.getContextRealPath(STATIC_DIR_NAME + fallbackPath);
				if(fallbackAbsPath != null) {
					staticFile = new File(fallbackAbsPath);
					if (!staticFile.exists()) {
						String realPath = request.getServletContext().getRealPath("/static" + normalizedRelPath);
						staticFile = new File(realPath);
						if(!staticFile.exists()) {
							response.sendError(HttpServletResponse.SC_NOT_FOUND);
							return;
						}
					}
				}
				// log as error, file exists but wrongly mapped
				log.warn("File exists but not mapped using version - use StaticMediaDispatch methods to create URL of static files! invalid URI::" + request.getRequestURI(), null);			
			}
		}

		if (debug) {
			log.debug("Serving resource URI::" + request.getRequestURI(), null);
		}
		
    	MediaResource resource = new FileMediaResource(staticFile);
    	ServletUtil.serveResource(request, response, resource);
	}
}
