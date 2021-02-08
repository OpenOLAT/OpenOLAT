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

package org.olat.commons.calendar.ui;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import org.olat.commons.calendar.CalendarManager;
import org.olat.commons.calendar.model.KalendarEvent;
import org.olat.commons.calendar.model.KalendarEventLink;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;

/**
 * 
 * <h3>Description:</h3>
 * A controller to handle a list of absolute links for the calendar.
 * <p>
 * <h4>Events fired by this Controller</h4>
 * <ul>
 * <li>DONE_EVENT</li>
 * <li>CANCELLED_EVENT</li>
 * </ul>
 * <p>
 * Initial Date:  16 déc. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, www.frentix.com
 */
public class ExternalLinksController extends FormBasicController {
	
	public static final String EXTERNAL_LINKS_PROVIDER = "external-links";
	
	private static final String VELOCITY_ROOT = Util.getPackageVelocityRoot(ExternalLinksController.class);
	
	private FormLink newButton;
	private final KalendarEvent kalendarEvent;
	private List<LinkWrapper> externalLinks;
	private FormLayoutContainer linksContainer;

	public ExternalLinksController(UserRequest ureq, WindowControl wControl, KalendarEvent kalendarEvent) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setBasePackage(CalendarManager.class);
		
		this.kalendarEvent = kalendarEvent;
		
		externalLinks = new ArrayList<>();
		List<KalendarEventLink> links = kalendarEvent.getKalendarEventLinks();
		for(KalendarEventLink link:links) {
			if(EXTERNAL_LINKS_PROVIDER.equals(link.getProvider())) {
				externalLinks.add(new LinkWrapper(link));
			}
		}
		if(externalLinks.isEmpty()) {
			LinkWrapper newLinkWrapper = createLinkWrapper();
			externalLinks.add(newLinkWrapper);
		}
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("tab.links.extern");
		
		String page = VELOCITY_ROOT + "/calExternalLinks.html";
		linksContainer = FormLayoutContainer.createCustomFormLayout("links", getTranslator(), page);
		formLayout.add(linksContainer);
		linksContainer.setRootForm(mainForm);

		for(LinkWrapper link:externalLinks) {
			addNewFormLink(link, linksContainer);
		}
		linksContainer.contextPut("links", externalLinks);
		
		FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("ok-cancel", getTranslator());
		formLayout.add(buttonLayout);
		buttonLayout.setRootForm(mainForm);
		
		uifactory.addFormSubmitButton("ok", "save", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	private void addNewFormLink(LinkWrapper link, FormLayoutContainer layoutContainer) {
		// add link target
		String id = link.getId();
		String uri = link.getLink().getURI();
		if(!StringHelper.containsNonWhitespace(uri)) {
			uri = "https://";
		}
		TextElement url = uifactory.addTextElement("url_".concat(id), null, -1, uri, layoutContainer);
		url.clearError();
		url.setDisplaySize(60);
		url.setMandatory(true);
		link.setUrl(url);
		
		// add link description
		TextElement name = uifactory.addTextElement("displayName_".concat(id), null, -1, link.getLink().getDisplayName(), layoutContainer);
		name.clearError();
		name.setDisplaySize(40);
		name.setMandatory(true);
		link.setName(name);
		
		// add link add action button
		FormLink addButton = uifactory.addFormLink("add_".concat(id), "table.add", "table.add", layoutContainer, Link.BUTTON);
		addButton.setUserObject(link);
		link.setAddButton(addButton);

		// add link deletion action button
		FormLink delButton = uifactory.addFormLink("del_".concat(id), "table.delete", "table.delete", layoutContainer, Link.BUTTON);
		delButton.setUserObject(link);
		link.setDelButton(delButton);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		for(LinkWrapper link:externalLinks) {
			link.getUrl().clearError();
			link.getName().clearError();
			if(!link.isEmpty()) {
				String url = link.getUrl().getValue();
				if(!StringHelper.containsNonWhitespace(url)) {
					link.getUrl().setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				} else {
					try {
						String host = new URL(url).getHost();
						if(host == null) {
							link.getUrl().setErrorKey("tab.links.extern.url.invalid", null);
						}
					} catch(MalformedURLException e) {
						link.getUrl().setErrorKey("tab.links.extern.url.invalid", null);
						allOk &= false;
					}
				}
				
				String name = link.getName().getValue();
				if(!StringHelper.containsNonWhitespace(name)) {
					link.getName().setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == newButton) {
			String id = UUID.randomUUID().toString().replace("-", "");
			KalendarEventLink link = new KalendarEventLink(EXTERNAL_LINKS_PROVIDER, id, "", "", "");
			LinkWrapper linkWrapper = new LinkWrapper(link);
			externalLinks.add(linkWrapper);
			addNewFormLink(linkWrapper, linksContainer);
		} else if (source.getUserObject() instanceof LinkWrapper){
			LinkWrapper link = (LinkWrapper)source.getUserObject();
			if(link.getDelButton().equals(source)) {
				externalLinks.remove(link);
			} else if (link.getAddButton().equals(source)) {
				int index = externalLinks.indexOf(link);
				LinkWrapper newLinkWrapper = createLinkWrapper();
				addNewFormLink(newLinkWrapper, linksContainer);
				if(index >= 0 && index + 1 < externalLinks.size()) {
					externalLinks.add(index + 1, newLinkWrapper);
				} else {
					externalLinks.add(newLinkWrapper);
				}
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		List<KalendarEventLink> links = kalendarEvent.getKalendarEventLinks();
		
		List<LinkWrapper> filledWrappers = new ArrayList<>();
		for(LinkWrapper linkWrapper:externalLinks) {
			if(!linkWrapper.isEmpty()) {
				filledWrappers.add(linkWrapper);
			}
		}
		
		//add and update links
		Set<String> usedUuids = new HashSet<>();
		for(LinkWrapper linkWrapper:filledWrappers) {
			boolean found = false;
			usedUuids.add(linkWrapper.getId());
			for(KalendarEventLink link:links) {
				if(link.getId().equals(linkWrapper.getId())) {
					link.setURI(linkWrapper.getUrl().getValue());
					link.setDisplayName(linkWrapper.getName().getValue());
					link.setIconCssClass("o_icon o_icon_link_extern");
					found = true;
				}
			}
			if(!found) {
				KalendarEventLink newLink = linkWrapper.getLink();
				newLink.setURI(linkWrapper.getUrl().getValue());
				newLink.setDisplayName(linkWrapper.getName().getValue());
				newLink.setIconCssClass("o_icon o_icon_link_extern");
				links.add(newLink);
			}
		}
		
		//remove deleted links
		for(Iterator<KalendarEventLink> it=links.iterator(); it.hasNext(); ) {
			KalendarEventLink link = it.next();
			if(EXTERNAL_LINKS_PROVIDER.equals(link.getProvider()) && !usedUuids.contains(link.getId())) {
				it.remove();
			}
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}


	private LinkWrapper createLinkWrapper() {
		String id = UUID.randomUUID().toString().replace("-", "");
		KalendarEventLink newLink = new KalendarEventLink(EXTERNAL_LINKS_PROVIDER, id, "", "", "");
		return new LinkWrapper(newLink);
	}

	public class LinkWrapper {
		
		private TextElement url;
		private TextElement name;
		private FormLink delButton;
		private FormLink addButton;
		private final KalendarEventLink link;
		
		public LinkWrapper(KalendarEventLink link) {
			this.link = link;
		}
		
		public String getId() {
			return link.getId();
		}
		
		public boolean isEmpty() {
			return url == null || !StringHelper.containsNonWhitespace(url.getValue());
		}

		public TextElement getUrl() {
			return url;
		}
		
		public void setUrl(TextElement url) {
			this.url = url;
		}

		public TextElement getName() {
			return name;
		}

		public void setName(TextElement name) {
			this.name = name;
		}

		public FormLink getDelButton() {
			return delButton;
		}

		public void setDelButton(FormLink delButton) {
			this.delButton = delButton;
		}

		public FormLink getAddButton() {
			return addButton;
		}

		public void setAddButton(FormLink addButton) {
			this.addButton = addButton;
		}

		public KalendarEventLink getLink() {
			return link;
		}
	}
}
