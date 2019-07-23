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

package org.olat.core.gui.control.generic.portal;

import java.util.HashMap;
import java.util.Map;

import org.olat.core.configuration.AbstractConfigOnOff;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * Abstract class that must be implemented by all portlets. 
 * <P>
 * Initial Date:  08.07.2005 <br>
 * @author gnaegi
 */
public abstract class AbstractPortlet extends AbstractConfigOnOff implements Portlet {
	private Map<String,String> configuration = new HashMap<>();
	private String name;
	private Translator trans;
	private int defaultMaxEntries = 6;

	/**
	 * @return The configuration map
	 */
	public Map<String,String> getConfiguration() {
		return this.configuration;
	}

	/**
	 * @param configuration The configuration map
	 */
	public void setConfiguration(Map<String,String> configuration) {
		this.configuration = configuration;
	}

	/**
	 * @see org.olat.core.gui.control.generic.portal.Portlet#getName()
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * Bean method used by spring to load value from configuration 
	 * @param name The unique name of this portlet
	 */
	public void setName(String name) {
		this.name = name;
	}

	public int getDefaultMaxEntries() {
		return defaultMaxEntries;
	}

	public void setDefaultMaxEntries(int defaultMaxEntries) {
		this.defaultMaxEntries = defaultMaxEntries;
	}

	/**
	 * @see org.olat.core.gui.control.generic.portal.Portlet#setTranslator(org.olat.core.gui.translator.Translator)
	 */
	public void setTranslator(Translator translator) {
		this.trans = translator;
	}

	/**
	 * @see org.olat.core.gui.control.generic.portal.Portlet#getTranslator()
	 */
	public Translator getTranslator() {
		return this.trans;
	}
	
	/**
	 * This must be overriden if there are any tools to be exposed.
	 * @see org.olat.core.gui.control.generic.portal.Portlet#getTools()
	 */
	public PortletToolController getTools(UserRequest ureq, WindowControl wControl) {
		return null;
	}
}
