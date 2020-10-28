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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.logging.AssertException;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * Description: <br>
 * The panel implements a place holder component with a stack to hold zero, one
 * or more components. Only the highest component on the stack is shown.
 * 
 * @author Felix Jost
 */
public class SimpleStackedPanel extends AbstractComponent implements StackedPanel {
	private static final Logger log = Tracing.createLoggerFor(SimpleStackedPanel.class);
	private static final ComponentRenderer RENDERER = new PanelRenderer();

	private Component curContent;
	private String stackCssClass;
	protected final List<Component> stackList = new ArrayList<>(3); // allow access to extending classes

	/**
	 * @param name
	 */
	public SimpleStackedPanel(String name) {
		super(name);
	}
	
	/**
	 * @param name
	 * @param elementCssClass wrapper CSS class added to the stack
	 */
	public SimpleStackedPanel(String name, String elementCssClass) {
		super(name);
		setCssClass(elementCssClass);
	}

	/**
	 * since the Panel does and shown nothing (is only a convenient boundary to
	 * put components into, and to swap them), we dispatch the request to the
	 * delegate
	 * @param ureq
	 */
	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		log.error("a panel should never dispatch a request (unless it has droppables, which it has not), ureq = {}", ureq);
	}

	/**
	 * @return
	 */
	@Override
	public Component getContent() {
		return curContent;
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

	/**
	 * clears the stack and sets the base content anew.
	 * 
	 * @param newContent the newContent. if null, then the panel will be empty
	 */
	@Override
	public void setContent(Component newContent) {
		stackList.clear();
		if (newContent != null) {
			pushContent(newContent);
		} else {
			curContent = null;
		}
		setDirty(true);
	}

	/**
	 * @param newContent may not be null
	 */
	@Override
	public void pushContent(Component newContent) {
		stackList.add(newContent);
		curContent = newContent;
		setDirty(true);
	}

	@Override
	public Component popContent() {
		int stackHeight = stackList.size();
		if (stackHeight < 1) throw new AssertException("stack was empty!");
		if (curContent == null) throw new AssertException("stackHeight not zero, but curContent was null!");
		Component popedComponent = stackList.remove(stackHeight - 1); // remove the top component
		if (stackHeight == 1) { // after pop, the content is null
			curContent = null;
		} else { // stackHeight > 1
			curContent = stackList.get(stackHeight - 2);
		}
		setDirty(true);
		return popedComponent;
	}

	@Override
	public String getExtendedDebugInfo() {
		StringBuilder sb = new StringBuilder();
		int size = stackList.size();
		for (int i = 0; i < size; i++) {
			Component comp = stackList.get(i); // may be null
			String compName = (comp == null ? "NULL" : comp.getComponentName());
			sb.append(compName).append(" | ");
		}
		return "stacksize:" + size + ", active:" + sb.toString();
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	public void setCssClass(String stackCss) {
		this.stackCssClass = stackCss;
	}
	
	@Override
	public String getCssClass() {
		return this.stackCssClass;
	}

}