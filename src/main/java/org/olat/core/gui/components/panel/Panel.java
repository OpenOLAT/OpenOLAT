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
package org.olat.core.gui.components.panel;

import java.util.Collections;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class Panel extends AbstractComponent implements ComponentCollection {
	
	private static final ComponentRenderer RENDERER = new PanelRenderer();
	private static final Logger log = Tracing.createLoggerFor(Panel.class);

	private Component curContent;
	/**
	 * @param name
	 */
	public Panel(String name) {
		super(name);
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
	public String getExtendedDebugInfo() {
		StringBuilder sb = new StringBuilder();
		String compName = (curContent == null ? "NULL" : curContent.getComponentName());
		sb.append(compName).append(" | ");
		return "stacksize:1, active:" + sb.toString();
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}