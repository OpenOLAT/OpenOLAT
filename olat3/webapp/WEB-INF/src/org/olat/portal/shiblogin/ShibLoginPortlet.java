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
 * University of Zurich, Switzerland.<p>
 * 
 * Description:<br>
 * Iframe portlet to embedd content from another server in the portal. 
 * The configuration must have an element uri and height. Title and description are optional elements.
 * They use the locale code for each language (eg. title_de, description_en)
 * <P>
 * Initial Date:  08.07.2005 <br>
 * @author BPS (<a href="http://www.bps-system.de/">BPS Bildungsportal Sachsen GmbH</a>)
 **/

package org.olat.portal.shiblogin;

import java.util.Map;

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

public class ShibLoginPortlet extends AbstractPortlet {
	
	private String cssWrapperClass = "b_portlet_iframe";
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
		return ShibbolethModule.isEnableShibbolethLogins() && super.isEnabled();
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		String title = (String)getConfiguration().get("title_" + getTranslator().getLocale().toString());
		if (title == null) {
				title = getTranslator().translate("portlet.title");
		}
		return title;
	}	

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getDescription()
	 */
	public String getDescription() {
		String desc = (String)getConfiguration().get("description_" + getTranslator().getLocale().toString());
		if (desc == null) {
			desc = getTranslator().translate("portlet.description");
		}
		return desc;
	}	

	/**
	 * @see org.olat.gui.control.generic.portal.AbstractPortlet#createInstance(org.olat.gui.control.WindowControl, org.olat.gui.UserRequest, java.util.Map)
	 */
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map configuration) {
		if (!ShibbolethModule.isEnableShibbolethLogins())
			throw new OLATSecurityException("Got shibboleth wayf form request but shibboleth is not enabled.");		
		ShibLoginPortlet p = new ShibLoginPortlet(config);
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(Util.createPackageTranslator(this.getClass(), ureq.getLocale()));
		// override css class if configured
		String cssClass = (String)configuration.get("cssWrapperClass");
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
