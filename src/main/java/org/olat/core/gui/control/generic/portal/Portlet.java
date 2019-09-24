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

import java.util.Map;

import org.olat.core.configuration.ConfigOnOff;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Disposable;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.translator.Translator;


public interface Portlet extends ConfigOnOff,Disposable{

	/**
	 * @return The (translated) title for the portlet
	 */
	public String getTitle();

	/**
	 * @return The (translated) description for the portlet
	 */
	public String getDescription();

	/**
	 * Factory method to create a portlet of the current type using the given configuration
	 * @param wControl
	 * @param ureq
	 * @param configuration
	 * @return AbstractPortlet
	 */
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> portletConfig);

	/**
	 * Get the run component of this portlet
	 * @param wControl
	 * @param ureq
	 * @return Component
	 */
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq);

	/**
	 * Dispose the run component and all its resources without disposing the portlet.
	 */
	public void disposeRunComponent();
	
	/**
	 * @return The configuration map
	 */
	public Map<String,String> getConfiguration();

	/**
	 * @param configuration The configuration map
	 */
	public void setConfiguration(Map<String,String> configuration);

	/**
	 * @return The unique name of this portlet. 
	 */
	public String getName();

	/**
	 * Bean method used by spring to load value from configuration 
	 * @param name The unique name of this portlet
	 */
	public void setName(String name);
	
	/**
	 * @return String name of wrapper css class that provides the portlet icon and other styles
	 */
	public String getCssClass();

	/**
	 * @param translator to be used in this portlet
	 */
	public void setTranslator(Translator translator);
	
	/**
	 * @return translator used in this portlet
	 */
	public Translator getTranslator();
	
	/**
	 * 
	 * @return a tools controller for this Portlet. It could be null 
	 * 	if the Portlet does't want to provide any additional tools.
	 * 	If you provide tools make sure have a working controller when you switch into the edit mode.
	 */
	public PortletToolController getTools(UserRequest ureq, WindowControl wControl);


}