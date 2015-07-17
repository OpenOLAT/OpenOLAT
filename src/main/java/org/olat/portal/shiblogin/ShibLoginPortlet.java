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
* <a href=“http://www.openolat.org“>
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
* Initial code contributed and copyrighted by<br>
* BPS Bildungsportal Sachsen, GmbH http://www.bps-system.de
* <p>
*/
package org.olat.portal.shiblogin;

import java.util.Map;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.logging.OLATSecurityException;
import org.olat.core.util.Util;
import org.olat.shibboleth.ShibbolethModule;
import org.olat.shibboleth.SwitchShibbolethAuthenticationConfigurator;

/**
 * 
 * Description:<br>
 * Iframe portlet to embedd content from another server in the portal. 
 * The configuration must have an element uri and height. Title and description are optional elements.
 * They use the locale code for each language (eg. title_de, description_en)
 */
public class ShibLoginPortlet extends AbstractPortlet {
	
	private String cssWrapperClass = "o_portlet_shibboleth";
	private Controller runCtr;
	private SwitchShibbolethAuthenticationConfigurator config;
	
	public ShibLoginPortlet(SwitchShibbolethAuthenticationConfigurator config) {
		this.config = config;
	}
	
	/**
	 * The portlet ins only enabled if the configuration of the portlet
	 * say so AND if the shibboleth authentication is enabled too.
	 * @see org.olat.core.configuration.AbstractConfigOnOff#isEnabled()
	 */
	@Override
	public boolean isEnabled() {
		return CoreSpringFactory.getImpl(ShibbolethModule.class).isEnableShibbolethLogins() && super.isEnabled();
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	@Override
	public String getTitle() {
		String title = getConfiguration().get("title_" + getTranslator().getLocale().toString());
		if (title == null) {
				title = getTranslator().translate("portlet.title");
		}
		return title;
	}	

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getDescription()
	 */
	@Override
	public String getDescription() {
		String desc = getConfiguration().get("description_" + getTranslator().getLocale().toString());
		if (desc == null) {
			desc = getTranslator().translate("portlet.description");
		}
		return desc;
	}	

	/**
	 * @see org.olat.gui.control.generic.portal.AbstractPortlet#createInstance(org.olat.gui.control.WindowControl, org.olat.gui.UserRequest, java.util.Map)
	 */
	@Override
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map<String,String> configuration) {
		if (!CoreSpringFactory.getImpl(ShibbolethModule.class).isEnableShibbolethLogins())
			throw new OLATSecurityException("Got shibboleth wayf form request but shibboleth is not enabled.");		
		ShibLoginPortlet p = new ShibLoginPortlet(config);
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(Util.createPackageTranslator(this.getClass(), ureq.getLocale()));
		// override css class if configured
		String cssClass = configuration.get("cssWrapperClass");
		if (cssClass != null) p.setCssWrapperClass(cssClass);
		return p;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getInitialRunComponent(org.olat.gui.control.WindowControl, org.olat.gui.UserRequest)
	 */
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(this.runCtr != null) runCtr.dispose();
		this.runCtr = new ShibLoginPortletRunController(ureq, wControl, config);
    return runCtr.getInitialComponent();
	}

	/**
	 * @see org.olat.gui.control.Disposable#dispose(boolean)
	 */
	public void dispose() {
		disposeRunComponent();
	}
	
	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getCssClass()
	 */
	public String getCssClass() {
		return cssWrapperClass;
	}

	/**
	 * Helper used to overwrite the default css class with the configured class
	 * @param cssWrapperClass
	 */
	void setCssWrapperClass(String cssWrapperClass) {
		this.cssWrapperClass = cssWrapperClass;
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#disposeRunComponent(boolean)
	 */
	public void disposeRunComponent() {
		if (runCtr != null) {
			runCtr.dispose();
			runCtr = null;
		}
	}

}
