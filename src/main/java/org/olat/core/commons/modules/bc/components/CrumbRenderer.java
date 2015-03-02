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
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.commons.modules.bc.components;

import java.util.StringTokenizer;

import org.olat.core.gui.control.winmgr.AJAXFlags;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.util.StringHelper;

/**
 * Initial Date:  08.07.2003
 *
 * @author Mike Stock
 */
public class CrumbRenderer {
	
	/**
	 * Return a path-like html fragment for the given briefcase path.
	 * @param dir
	 * @param ubu
	 * @param renderLinks
	 * @param iframePostEnabled
	 * @return	HTML fragment of briefcase path
	 */
	public final void render(FolderComponent fc, StringOutput sb, URLBuilder ubu, boolean iframePostEnabled) {
		StringOutput pathLink = new StringOutput();
		ubu.buildURI(pathLink, null, null, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);

		StringOutput so = new StringOutput();
		ubu.appendTarget(so);
		
		// append toplevel node
		sb.append("<ol class='breadcrumb'>")
		  .append("<li><a href='").append(pathLink).append("'");
		if (iframePostEnabled) { // add ajax iframe target
			sb.append(so.toString());
		}
		sb.append(">")
		  .append(StringHelper.escapeHtml(fc.getRootContainer().getName())).append("</a></li>");
		
		String path = fc.getCurrentContainerPath();
		StringTokenizer st = new StringTokenizer(path, "/", false);
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			if(pathLink.length() > 0) {
				pathLink.append("/");
			}
			pathLink.append(ubu.encodeUrl(token));
			if (st.hasMoreElements()) {
				sb.append("<li><a href='").append(pathLink).append("'");
				if (iframePostEnabled) { // add ajax iframe target
					sb.append(so.toString());
				}
				sb.append(">").append(StringHelper.escapeHtml(token)).append("</a></li>");
			} else {
				sb.append("<li class='active'>").append(StringHelper.escapeHtml(token)).append("</li>");
			}
		}
		sb.append("</ol>");
	}
}