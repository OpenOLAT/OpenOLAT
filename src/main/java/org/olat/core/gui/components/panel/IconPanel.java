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
import org.olat.core.gui.components.link.Link;
import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 28 Oct 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class IconPanel extends AbstractComponent implements ComponentCollection {
	
	private static final ComponentRenderer RENDERER = new IconPanelRenderer();
	private final List<Component> links = new ArrayList<>(3);
	private Component content;
	private String iconCssClass;
	private String title;
	private String tagline;
	
	public IconPanel(String name) {
		super(name);
		setDomReplacementWrapperRequired(false);
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public boolean isDirty() {
		boolean dirty = false;
		for (Component link : links) {
			dirty |= link.isDirty();
		}
		if (content != null) {
			dirty |= content.isDirty();
		}
		return dirty || super.isDirty();
	}

	@Override
	public Component getComponent(String name) {
		for (Component link : links) {
			if (link.getComponentName().equals(name)) {
				return link;
			}
		}
		
		if (content != null && content.getComponentName().equals(name)) {
			return content;
		}
		
		return null;
	}

	@Override
	public Iterable<Component> getComponents() {
		List<Component> allComponent = new ArrayList<>(links.size() + 1);
		allComponent.addAll(links);
		if (content != null) {
			allComponent.add(content);
		}
		return allComponent;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public Component getContent() {
		return content;
	}

	public void setContent(Component content) {
		this.content = content;
		setDirty(true);
	}

	public String getIconCssClass() {
		return iconCssClass;
	}

	public void setIconCssClass(String iconCssClass) {
		this.iconCssClass = iconCssClass;
		setDirty(true);
	}
	
	public void addLink(Link link) {
		if (link != null) {
			links.add(link);
			setDirty(true);
		}
	}
	
	public void removeLink(String name) {
		if (StringHelper.containsNonWhitespace(name)) {
			links.removeIf(link -> link.getComponentName().equals(name));
			setDirty(true);
		}
	}

	public void removeAllLinks() {
		links.clear();
		setDirty(true);
	}

	List<Component> getLinks() {
		return links;
	}
	
	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
		setDirty(true);
	}

	public String getTagline() {
		return tagline;
	}

	public void setTagline(String tagline) {
		this.tagline = tagline;
	}

}
