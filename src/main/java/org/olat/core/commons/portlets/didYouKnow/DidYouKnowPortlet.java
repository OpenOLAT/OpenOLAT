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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/ 

package org.olat.core.commons.portlets.didYouKnow;

import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortlet;
import org.olat.core.gui.control.generic.portal.Portlet;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.util.Util;

/**
 * Description:<br>
 * Displays a random tip about the OLAT usage
 * <P>
 * Initial Date:  08.07.2005 <br>
 * @author gnaegi
 */
public class DidYouKnowPortlet extends AbstractPortlet {
	private Controller runCtr;

	
	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getTitle()
	 */
	public String getTitle() {
		return getTranslator().translate("didYouKnow.title");
	}	

	/**
	 * @see org.olat.gui.control.generic.portal.AbstractPortlet#createInstance(org.olat.core.gui.control.WindowControl, org.olat.core.gui.UserRequest, java.util.Map)
	 */
	public Portlet createInstance(WindowControl wControl, UserRequest ureq, Map configuration) {
		Portlet p = new DidYouKnowPortlet();
		p.setName(this.getName());
		p.setConfiguration(configuration);
		p.setTranslator(new PackageTranslator(Util.getPackageName(DidYouKnowPortlet.class), ureq.getLocale()));
		return p;
	}


	/**
	 * @see org.olat.core.gui.control.Disposable#dispose(boolean)
	 */
	public void dispose() {
		disposeRunComponent();
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getInitialRunComponent(org.olat.core.gui.control.WindowControl, org.olat.core.gui.UserRequest)
	 */
	public Component getInitialRunComponent(WindowControl wControl, UserRequest ureq) {
		if(this.runCtr != null) runCtr.dispose();
		this.runCtr = new DidYouKnowPortletRunController(ureq, wControl);
		return this.runCtr.getInitialComponent();
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getDescription()
	 */
	public String getDescription() {
		return getTranslator().translate("didYouKnow.description");
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#getCssClass()
	 */
	public String getCssClass() {
		return "b_portlet_dyk";
	}

	/**
	 * @see org.olat.gui.control.generic.portal.Portlet#disposeRunComponent(boolean)
	 */
	public void disposeRunComponent() {
		if (this.runCtr != null) {
			this.runCtr.dispose();
			this.runCtr = null;
		}
	}

}
