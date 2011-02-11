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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2007 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
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
	private String url;

	/**
	 * Constructor for this even.
	 * @param url The URL that has been selected
	 */
	public URLChoosenEvent(String url) {
		super("urlchoosenevent");
		this.url = url;
	}

	/**
	 * @return the selected URL
	 */
	public String getURL() {
		return url;
	}
}
