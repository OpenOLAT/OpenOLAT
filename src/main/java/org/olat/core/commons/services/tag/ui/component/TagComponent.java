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
package org.olat.core.commons.services.tag.ui.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.services.tag.TagInfo;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentCollection;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.control.Event;
import org.olat.core.util.StringHelper;

/**
 * Initial date: Jun 26, 2024
 *
 * @author skapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class TagComponent extends AbstractComponent implements ComponentCollection, ComponentEventListener {

	private static final ComponentRenderer RENDERER = new TagComponentRenderer();
	private static final String TAG_NOT_SELECTED_CSS = "o_tag o_selection_tag o_tag_clickable";
	private String TAG_SELECTED_CSS = "o_tag o_selection_tag o_tag_clickable o_tag_selected";

	private final List<TagInfo> tagInfos;
	private final List<Link> tagLinks;


	public TagComponent(String name, List<TagInfo> tagInfos, boolean isRemoving) {
		super(name);
		this.tagInfos = tagInfos;

		// override default of selected tags, different UI for removing tags
		if (isRemoving) {
			TAG_SELECTED_CSS = "o_tag o_selection_tag o_tag_clickable o_tag_selected_remove";
		}

		this.tagLinks = new ArrayList<>(tagInfos.size());
		for (TagInfo tagInfo : tagInfos) {
			String tagDisplayName = StringHelper.escapeHtml(tagInfo.getDisplayName()) + " " + tagInfo.getCount();
			Link tagLink = LinkFactory.createLink(null, "tag_" + tagInfo.getKey(), "toggle", tagDisplayName, getTranslator(), null, this, Link.NONTRANSLATED);
			tagLink.setElementCssClass(TAG_NOT_SELECTED_CSS);
			tagLink.setDomReplacementWrapperRequired(false);
			tagLink.setUserObject(tagInfo);
			tagLinks.add(tagLink);
		}
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//
	}

	@Override
	public Component getComponent(String name) {
		return tagLinks.stream()
				.filter(link -> link.getComponentName().equals(name))
				.findFirst().orElse(null);
	}

	@Override
	public Iterable<Component> getComponents() {
		return new ArrayList<>(tagLinks);
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			toggleLinkCss(link);
			fireEvent(ureq, new TagComponentEvent(link));
		}
	}

	public void toggleLinkCss(Link link) {
		TagInfo tagInfo = (TagInfo) link.getUserObject();
		if (link.getElementCssClass().equals(TAG_SELECTED_CSS)) {
			tagInfo.setSelected(false);
			link.setElementCssClass(TAG_NOT_SELECTED_CSS);
		} else {
			tagInfo.setSelected(true);
			link.setElementCssClass(TAG_SELECTED_CSS);
		}
	}

	public void updateTagSelection(Set<Long> selectedTagKeys) {
		for (Link tagLink : tagLinks) {
			TagInfo tagInfo = (TagInfo) tagLink.getUserObject();
			if (selectedTagKeys.contains(tagInfo.getKey())) {
				tagInfo.setSelected(true);
				tagLink.setElementCssClass(TAG_SELECTED_CSS);
			} else {
				tagInfo.setSelected(false);
				tagLink.setElementCssClass(TAG_NOT_SELECTED_CSS);
			}
		}
	}

	public List<TagInfo> getTagInfos() {
		return tagInfos;
	}

	public List<Link> getTagLinks() {
		return tagLinks;
	}
}
