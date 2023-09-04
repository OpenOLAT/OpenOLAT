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
package org.olat.modules.ceditor.ui.component;

import java.util.Collections;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * 
 * Initial date: 4 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InspectorPanelComponent extends AbstractComponent implements ComponentCollection {

	private static final ComponentRenderer RENDERER = new InspectorPanelRenderer();

	private final Component inspector;
	
	public InspectorPanelComponent(Component inspector) {
		super(inspector.getDispatchID() + "_inspector");
		this.inspector = inspector;
		setDomReplacementWrapperRequired(false);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	/**
	 * @return
	 */
	public Component getContent() {
		return inspector;
	}
	
	public boolean isInspectorVisible() {
		return inspector.isVisible();
	}
	
	public void setInspectorVisible(boolean visible) {
		inspector.setVisible(visible);
		setDirty(true);
	}

	@Override
	public Component getComponent(String name) {
		if(inspector != null && inspector.getComponentName().equals(name)) {
			return inspector;
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		if(inspector == null) {
			return Collections.emptyList();
		}
		return Collections.singletonList(inspector);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
}
