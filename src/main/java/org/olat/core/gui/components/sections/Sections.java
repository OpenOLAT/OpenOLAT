/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.sections;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentRenderer;

/**
 * A list of independently collapsible sections, each with a title and a
 * content component. Rendering and collapse/expand are pure client-side
 * (Bootstrap collapse) - no server round-trip.
 *
 * Initial date: 18 Aug 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class Sections extends AbstractComponent implements ComponentCollection {

	private static final ComponentRenderer RENDERER = new SectionsRenderer();

	private List<Section> sections = new ArrayList<>(1);

	protected Sections(String name) {
		super(name);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	public List<Section> getSections() {
		return sections;
	}

	public void setSections(List<Section> sections) {
		this.sections = sections == null ? new ArrayList<>() : sections;
		setDirty(true);
	}

	@Override
	public Component getComponent(String name) {
		for (Section section : sections) {
			if (name.equals(section.getContent().getComponentName())) {
				return section.getContent();
			}
		}
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> components = new ArrayList<>(sections.size());
		for (Section section : sections) {
			components.add(section.getContent());
		}
		return components;
	}

}
