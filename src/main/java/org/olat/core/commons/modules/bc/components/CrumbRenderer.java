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
	public final void render(FolderComponent fc, StringOutput sb, URLBuilder ubu, boolean renderLinks, boolean iframePostEnabled) {
		StringOutput pathLink = new StringOutput();
		ubu.buildURI(pathLink, null, null, iframePostEnabled ? AJAXFlags.MODE_TOBGIFRAME : AJAXFlags.MODE_NORMAL);

		// append toplevel node
		sb.append("<div class=\"b_briefcase_breadcrumb\">/&nbsp;");
		if (renderLinks) {
			sb.append("<a href=\"");
			sb.append(pathLink);
			sb.append("\"");
			if (iframePostEnabled) { // add ajax iframe target
				StringOutput so = new StringOutput();
				ubu.appendTarget(so);
				sb.append(so.toString());
			}
			sb.append(">");
		}
		sb.append(fc.getRootContainer().getName());
		if (renderLinks)
			sb.append("</a>");
		
		StringTokenizer st = new StringTokenizer(fc.getCurrentContainerPath(), "/", false);
		while (st.hasMoreElements()) {
			String token = st.nextToken();
			pathLink.append(StringHelper.urlEncodeUTF8(token));
			sb.append("&nbsp;/&nbsp;");
			if (st.hasMoreElements() && renderLinks) {
				sb.append("<a href=\"");
				sb.append(pathLink.toString());
				sb.append("\"");
				if (iframePostEnabled) { // add ajax iframe target
					StringOutput so = new StringOutput();
					ubu.appendTarget(so);
					sb.append(so.toString());
				}
				sb.append(">");
				sb.append(token);
				sb.append("</a>");
			} else {
				sb.append(token);
			}
			pathLink.append("/");
		}
		sb.append("</div>");
	}
}