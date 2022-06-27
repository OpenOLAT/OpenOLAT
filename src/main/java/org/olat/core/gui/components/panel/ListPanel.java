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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;

/**
 * 
 * Initial date: 23.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ListPanel extends AbstractComponent implements ComponentCollection {
	
	private static final Logger log = Tracing.createLoggerFor(ListPanel.class);
	
	private static final ComponentRenderer RENDERER = new ListPanelRenderer();
	private final List<Component> content = new ArrayList<>(3);
	
	private String cssClass;
	
	/**
	 * By default is domReplacementWrapperRequired set to false.
	 * 
	 * 
	 * @param name
	 * @param cssClass
	 */
	public ListPanel(String name, String cssClass) {
		super(name);
		this.cssClass = cssClass;
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

	public String getCssClass() {
		return cssClass;
	}

	public void setCssClass(String cssClass) {
		this.cssClass = cssClass;
	}

	public void addContent(Component newContent) {
		content.add(newContent);
		setDirty(true);
	}
	
	public boolean removeContent(Component toRemove) {
		boolean removed = content.remove(toRemove);
		setDirty(true);
		return removed;
	}

	@Override
	public boolean isDirty() {
		boolean dirty = false;
		for(Component cmp:content) {
			dirty |= cmp.isDirty();
		}
		return dirty || super.isDirty();
	}

	@Override
	public Component getComponent(String name) {
		Component curContent = null;
		for(Component cmp:content) {
			if(cmp.getComponentName().equals(name)) {
				curContent = cmp;
			}
		}
		return curContent;
	}

	@Override
	public Iterable<Component> getComponents() {
		return content;
	}

	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}