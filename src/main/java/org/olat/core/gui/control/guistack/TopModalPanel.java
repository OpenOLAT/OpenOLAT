/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.core.gui.control.guistack;

import java.util.Collections;

import org.apache.logging.log4j.Logger;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.panel.SimpleStackedPanel;
import org.olat.core.gui.components.panel.StackedPanel;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 28 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
class TopModalPanel extends AbstractComponent implements StackedPanel  {
	
	private static final Logger log = Tracing.createLoggerFor(SimpleStackedPanel.class);
	private static final ComponentRenderer RENDERER = new TopModalPanelRenderer();

	protected static final int TOP_MODAL_ZINDEX = 70000;
	
	private Component curContent;
	private String cssClass;

	/**
	 * @param name
	 */
	protected TopModalPanel(String name) {
		super(name);
		setDomReplacementWrapperRequired(false);
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
		curContent = newContent;
		setDirty(true);
	}

	@Override
	public Component popContent() {
		Component cmp = curContent;
		curContent = null;
		setDirty(true);
		return cmp;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}
	
	@Override
	public String getCssClass() {
		return this.cssClass;
	}
}
