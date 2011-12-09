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
package org.olat.core.gui.components.form.flexible.impl.components;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.Container;
import org.olat.core.gui.components.form.flexible.FormBaseComponentIdProvider;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * FIXME: MAYBE REMOVE!!
 * 
 * <P>
 * Initial Date:  11.01.2007 <br>
 * @author patrickb
 */
public abstract class FormVelocityContainer extends Container implements FormBaseComponentIdProvider {

	private static final ComponentRenderer RENDERER = new FormVelocityContainerRenderer();
	VelocityContainer wrappedVc;
	
	public FormVelocityContainer(String componentName, String page, Translator trans, Controller listeningController) {
		super("fvc_"+componentName);
		wrappedVc = new VelocityContainer(componentName, page, trans, listeningController);
	}

	public String getFormDispatchId(){
		return DISPPREFIX+super.getDispatchID();
	}

	/**
	 * @see org.olat.core.gui.components.velocity.VelocityContainer#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		wrappedVc.dispatchRequest(ureq);		
	}

	public VelocityContainer getVelocityContainer() {
		return wrappedVc;
	}
}
