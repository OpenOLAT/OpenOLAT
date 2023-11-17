/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.ims.lti13.ui;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.ims.lti13.LTI13ContentItem;
import org.olat.ims.lti13.LTI13Context;
import org.olat.ims.lti13.ui.events.LTI13ContentItemStartEvent;

/**
 * 
 * Initial date: 13 sept. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class LTI13ContentItemsListController extends BasicController {
	
	private final VelocityContainer mainVC;
	
	private int count = 0;
	private final LTI13Context context;
	
	public LTI13ContentItemsListController(UserRequest ureq, WindowControl wControl,
			LTI13Context context, List<LTI13ContentItem> contentItems) {
		super(ureq, wControl);
		this.context = context;
		
		mainVC = createVelocityContainer("contentitems");
		initContentItems(contentItems);
		putInitialPanel(mainVC);
	}
	
	private void initContentItems(List<LTI13ContentItem> contentItems) {
		List<ContentItem> items = new ArrayList<>(contentItems.size());
		for(LTI13ContentItem contentItem:contentItems) {
			Link openLink = LinkFactory.createCustomLink("open_" + (++count), "open", translate("start.resource.link"), Link.LINK | Link.NONTRANSLATED, mainVC, this);
			openLink.setUserObject(contentItem);
			openLink.setIconRightCSS("o_icon o_icon_start");
			items.add(new ContentItem(contentItem, openLink));
		}
		mainVC.contextPut("items", items);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link link && link.getUserObject() instanceof LTI13ContentItem contentItem) {
			fireEvent(ureq, new LTI13ContentItemStartEvent(context, contentItem));
		}
	}
	
	public record ContentItem(LTI13ContentItem contentItem, Link openLink) implements Comparator<ContentItem> {
		
		public String type() {
			return contentItem.getType().name();
		}
		
		public String openLinkName() {
			return openLink.getComponentName();
		}
		
		public String html() {
			return toVelocity(contentItem.getHtml());
		}
		
		public String title() {
			return toVelocity(contentItem.getTitle());
		}
		
		public String text() {
			return toVelocity(contentItem.getText());
		}
		
		public String url() {
			return toVelocity(contentItem.getUrl());
		}
		
		public boolean hasThumbnail() {
			return StringHelper.containsNonWhitespace(contentItem.getThumbnailUrl());
		}
		
		public String thumbnailUrl() {
			return contentItem.getThumbnailUrl();
		}
		
		public Long thumbnailWidth() {
			return contentItem.getThumbnailWidth();
		}
		
		public Long thumbnailHeight() {
			return contentItem.getThumbnailHeight();
		}
		
		private String toVelocity(String val) {
			return val == null ? "" : val;
		}

		@Override
		public int compare(ContentItem o1, ContentItem o2) {
			return o1.contentItem().getKey().compareTo(o2.contentItem().getKey());
		}

		@Override
		public boolean equals(Object obj) {
			if(this == obj) {
				return true;
			}
			if(obj instanceof ContentItem item) {
				return contentItem().equals(item.contentItem());
			}
			return false;
		}

		@Override
		public int hashCode() {
			return contentItem.hashCode();
		}
	}
}
