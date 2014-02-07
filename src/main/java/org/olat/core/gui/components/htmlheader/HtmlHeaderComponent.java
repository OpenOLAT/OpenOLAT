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

package org.olat.core.gui.components.htmlheader;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * Initial Date: Jun 14, 2004
 * 
 * @author gnaegi
 */
public class HtmlHeaderComponent extends AbstractComponent {
	private static final ComponentRenderer RENDERER = new HtmlHeaderRenderer();

	private String jsBodyOnLoad;
	private String headerInclude;

	/**
	 * Constructor for java script onload and html header component
	 * 
	 * @param componentName
	 * @param jsBodyOnLoad
	 * @param headerInclude
	 */
	public HtmlHeaderComponent(String componentName, String jsBodyOnLoad, String headerInclude) {
		super(componentName);
		this.jsBodyOnLoad = jsBodyOnLoad;
		this.headerInclude = headerInclude;
	}

	/**
	 * @see org.olat.core.gui.components.Component#dispatchRequest(org.olat.core.gui.UserRequest)
	 */
	protected void doDispatchRequest(UserRequest ureq) {
	// do nothing
	}

	/**
	 * @return Returns the headerInclude.
	 */
	protected String getHeaderInclude() {
		return headerInclude;
	}

	/**
	 * @param headerInclude The headerInclude to set.
	 */
	public void setHeaderInclude(String headerInclude) {
		setDirty(true);
		this.headerInclude = headerInclude;
	}

	/**
	 * @return Returns the jsBodyOnLoad.
	 */
	protected String getJsBodyOnLoad() {
		return jsBodyOnLoad;
	}

	/**
	 * @param jsBodyOnLoad The jsBodyOnLoad to set.
	 */
	public void setJsBodyOnLoad(String jsBodyOnLoad) {
		setDirty(true);
		this.jsBodyOnLoad = jsBodyOnLoad;
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
