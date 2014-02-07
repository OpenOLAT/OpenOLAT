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
package org.olat.core.gui.components.util;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.RenderUtil;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * This component forwards any validate calls (which take place after dispatching, but prior to rendering) to the listening controller.
 * Useful when the controller received e.g. a model invalidation from the system even bus.
 * Instead of immediately updating the model from the db or similar, which is expensive,
 * the controller can wait till it receives a validate event.<br>
 * This component does not render anything.<br>
 * the validate method is called just before the rendering of the -whole tree- takes place, so e.g.
 * lazy fetching can be implemented, or issueing a request for a new moduleUri
 * (e.g. for CPComponent, so that the browser loads images correctly).<br>
 * only called when the component is visible
 * 
 * <P>
 * Initial Date: 10.03.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class ValidateForwardComponent extends AbstractComponent {

	/**
	 * @param name
	 */
	ValidateForwardComponent(String name) {
		super(name);
	}

	/**
	 * @param name
	 * @param translator
	 */
	ValidateForwardComponent(String name, Translator translator) {
		super(name, translator);
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.Component#doDispatchRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		// nothing to do here
	}
		/**
		 * called just before the rendering of the -whole tree- takes place, so e.g.
		 * lazy fetching can be implemented, or issueing a request for a new moduleUri
		 * (e.g. for CPComponent, so that the browser loads images correctly). only
		 * called when the component is visible
		 */
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		fireEvent(ureq, ComponentUtil.VALIDATE_EVENT);
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RenderUtil.NOTHING_RENDERER;
	}

}
