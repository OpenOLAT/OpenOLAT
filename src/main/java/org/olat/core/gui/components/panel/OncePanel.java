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
package org.olat.core.gui.components.panel;

import java.util.Collections;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.render.ValidationResult;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Description:<br>
 * A Panel which shows a certain content only one render time, and then is hidden until a new content is set.
 * useful for e.g. a message on the screen which should automatically disappear after having been rendered once until
 * a new message appears.
 * <P>
 * Initial Date: 19.01.2007 <br>
 * @author Felix Jost, http://www.goodsolutions.ch
 */
public class OncePanel extends AbstractComponent implements ComponentCollection {
	private static final Logger log = Tracing.createLoggerFor(OncePanel.class);
	private static final ComponentRenderer RENDERER = new PanelRenderer();

	private boolean hideOnNextValidate;
	private Component curContent;
	
	/**
	 * @param name
	 */
	public OncePanel(String name) {
		super(name);
	}

	/**
	 * @return
	 */
	public Component getContent() {
		return curContent;
	}
	
	/**
	 * clears the stack and sets the base content anew.
	 * 
	 * @param newContent the newContent. if null, then the panel will be empty
	 */
	public void setContent(Component newContent) {
		curContent = newContent;
		setDirty(true);
	}

	@Override
	public Component getComponent(String name) {
		if(curContent != null && curContent.getComponentName().equals(name)) {
			return curContent;
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		if(curContent == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(curContent);
	}
	
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		log.error("a panel should never dispatch a request (unless it has droppables, which it has not), ureq = "+ureq);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		// after a change, flag that the content will be displayed only once
		if (isDirty()) {
			hideOnNextValidate = true;
		} else {
			// not dirty, check if flag is set.
			if (hideOnNextValidate) {
				hideOnNextValidate = false;
				setContent(null); // set dirty flag and causes a rerendering
			}
		}
	}

	
	
}
