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
* <p>
*/ 

package org.olat.commons.servlets.pathhandlers;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.olat.commons.servlets.util.ResourceDescriptor;
import org.olat.core.logging.StartupException;
import org.olat.core.util.UserSession;
import org.olat.core.util.WebappHelper;


/**
 * Description: <BR/>the ContextHelpFilePathHandler handles request for help
 * files (context sensitive help files, which are in the static folder
 * (webapp/static/help). they are "normal" html files encoded in utf-8. This
 * handler here adds the handling of non-existing files (for translations not
 * ready yet). It shows a message in the current locale that the file is not
 * translated yet and offers a fallback to the default locale. <P/>
 * 
 * @author Felix Jost
 */
public class ContextHelpFilePathHandler implements PathHandler {

	private String root = null;

	/**
	 * Path handler delivering files.
	 */
	public ContextHelpFilePathHandler() {
		super();
	}

	/**
	 * @see org.olat.commons.servlets.pathhandlers.PathHandler#init(com.anthonyeden.lib.config.Configuration)
	 */
	public void init(String path) {
		if (path == null) return;
		if (path != null) {
			File f = new File(path);
			if (f.isAbsolute()) {
				setRoot(path);
			} else {
				setRoot(WebappHelper.getContextRoot() + "/" + path);
			}
		} else throw new StartupException("ContextHelpFilePathHandler did not find mandatory <root> element:" + path);
	}

	/**
	 * @see org.olat.commons.servlets.pathhandlers.PathHandler#getInputStream(javax.servlet.http.HttpServletRequest,
	 *      org.olat.commons.servlets.util.ResourceDescriptor)
	 */
	public InputStream getInputStream(HttpServletRequest request, ResourceDescriptor rd) {
		try {
			File f = new File(root + rd.getRelPath());
			return new BufferedInputStream( new FileInputStream(f) );
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @see org.olat.commons.servlets.pathhandlers.PathHandler#getResourceDescriptor(javax.servlet.http.HttpServletRequest,
	 *      java.lang.String)
	 */
	public ResourceDescriptor getResourceDescriptor(HttpServletRequest request, String relPath) {
		try {
			File f = new File(root + relPath);
			if (f.isDirectory()) return null;
			
			if (!f.exists()) { // file not found ->
				int i = 1;
				i ++;
			}
			//FIXME:fj: handle appropriately
			Locale loc = UserSession.getUserSession(request).getLocale();
			
			ResourceDescriptor rd = new ResourceDescriptor(relPath);
			rd.setLastModified(f.lastModified());
			rd.setSize(f.length());
			String mimeType = WebappHelper.getMimeType(relPath);
			if (mimeType == null) mimeType = "application/octet-stream";
			rd.setContentType(mimeType);
			return rd;
		} catch (Exception e) {
			return null;
		}
	}

	/**
	 * @return The canonical root of this file handler.
	 */
	public String getRoot() {
		return root;
	}

	/**
	 * Set the canonical root of this file handler.
	 * 
	 * @param newRoot
	 */
	public void setRoot(String newRoot) {
		root = newRoot;
	}

}