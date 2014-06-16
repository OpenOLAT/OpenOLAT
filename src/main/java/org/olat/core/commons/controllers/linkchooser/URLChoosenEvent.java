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
package org.olat.core.commons.controllers.linkchooser;

import org.olat.core.gui.control.Event;

/**
 * Description:<br>
 * This event is fired when a URL has been selected by whatever process.
 * The URL could be a link to another HTML page or a media URL.
 * <P>
 * Initial Date: Mar 14 2007 <br>
 * 
 * @author Florian Gnägi, frentix GmbH, http://www.frentix.com
 */
public class URLChoosenEvent extends Event {

	private static final long serialVersionUID = 4271699081190253734L;
	private final String url;
	private final String displayName;
	private final String htmlTarget;
	private final String iconCssClass;
	private final int width;
	private final int height;

	/**
	 * Constructor for this even.
	 * @param url The URL that has been selected
	 */
	public URLChoosenEvent(String url) {
		this(url, null, null, null, -1, -1);
	}
	
	public URLChoosenEvent(String url, String displayName, String htmlTarget, String iconCssClass, int width, int height) {
		super("urlchoosenevent");
		this.url = url;
		this.displayName = displayName;
		this.htmlTarget = htmlTarget;
		this.iconCssClass = iconCssClass;
		this.width = width;
		this.height = height;
	}

	/**
	 * @return the selected URL
	 */
	public String getURL() {
		return url;
	}

	/**
	 * @return the display name of the link (can be null)
	 */
	public String getDisplayName() {
		return displayName;
	}

	/**
	 * @return The HTML target
	 */
	public String getHtmlTarget() {
		return htmlTarget;
	}

	/**
	 * @return the css class icon for the file
	 */
	public String getIconCssClass() {
		return iconCssClass;
	}

	public int getWidth() {
		return width;
	}

	public int getHeight() {
		return height;
	}
}
