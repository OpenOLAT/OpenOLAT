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
package org.olat.modules.portfolio.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.modules.portfolio.Page;
import org.olat.modules.portfolio.PagePart;
import org.olat.modules.portfolio.PortfolioService;
import org.olat.modules.portfolio.model.HTMLPart;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 08.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PageEditorController extends FormBasicController {

	private FormLink addHtmlLink;
	private List<HTMLEditorFragment> fragments = new ArrayList<>();

	private Page page;
	private int counter = 0;
	
	@Autowired
	private PortfolioService portfolioService;
	
	public PageEditorController(UserRequest ureq, WindowControl wControl, Page page) {
		super(ureq, wControl, "page_editor");
		this.page = page;
		
		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		addHtmlLink = uifactory.addFormLink("add.html", "add.html", "add.html", null, formLayout, Link.BUTTON);
		addHtmlLink.setIconLeftCSS("o_icon o_icon_add_html");
			
		uifactory.addFormSubmitButton("save", formLayout);

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.getFormItemComponent().contextPut("pageTitle", page.getTitle());
		}
	}
	
	private void loadModel(UserRequest ureq) {
		UserSession usess = ureq.getUserSession();
		List<PagePart> parts = portfolioService.getPageParts(page);
		List<HTMLEditorFragment> newFragments = new ArrayList<>(parts.size());
		for(PagePart part:parts) {
			HTMLEditorFragment fragment = createFragment(part, usess);
			if(fragment != null) {
				newFragments.add(fragment);
			}
		}
		fragments = newFragments;
		flc.getFormItemComponent().contextPut("fragments", newFragments);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(addHtmlLink == source) {
			doAddHTMLFragment(ureq);
		} else if(source.getUserObject() instanceof HTMLEditorFragment) {
			HTMLEditorFragment fragment = (HTMLEditorFragment)source.getUserObject();
			if(fragment.getFormItem() == source) {
				String htmlVal = fragment.getFormItem().getValue();
				PagePart part = fragment.getPart();
				part.setContent(htmlVal);
				fragment.setPart(portfolioService.updatePart(part));
				flc.setDirty(true);	
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent fe) {
		//ok -> set container dirty
		super.propagateDirtinessToContainer(fiSrc, fe);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for(HTMLEditorFragment fragment:fragments) {
			String htmlVal = fragment.getFormItem().getValue();
			String currentHtmlVal = fragment.getPart().getContent();
			
			if((currentHtmlVal == null && StringHelper.containsNonWhitespace(htmlVal))
					|| (htmlVal == null && StringHelper.containsNonWhitespace(currentHtmlVal))
					|| (currentHtmlVal != null && !currentHtmlVal.equals(htmlVal))) {
				PagePart part = fragment.getPart();
				part.setContent(htmlVal);
				fragment.setPart(portfolioService.updatePart(part));
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	private void doAddHTMLFragment(UserRequest ureq) {
		String content = "<p>Hello world</p>";
		HTMLPart htmlPart = new HTMLPart();
		htmlPart.setContent(content);
		htmlPart = portfolioService.appendNewPagePart(page, htmlPart);
		HTMLEditorFragment fragment = createFragment(htmlPart, ureq.getUserSession());
		fragments.add(fragment);
		flc.setDirty(true);
	}
	
	private HTMLEditorFragment createFragment(PagePart part, UserSession usess) {
		if(part instanceof HTMLPart) {
			HTMLPart htmlPart = (HTMLPart)part;
			HTMLEditorFragment editorFragment = new HTMLEditorFragment(htmlPart);
			
			String cmpId = "html-" + (++counter);
			String content = htmlPart.getContent();
			RichTextElement htmlItem = uifactory.addRichTextElementForStringDataCompact(cmpId, null, content, 25, 80, null, flc, usess, getWindowControl());
			//htmlItem.getEditorConfiguration().setInline(true);
			editorFragment.setFormItem(htmlItem);
			return editorFragment;
		}
		return null;
		
	}
	
	public static class HTMLEditorFragment {
		
		private PagePart part;
		private RichTextElement formItem;
		
		public HTMLEditorFragment(HTMLPart part) {
			this.part = part;
		}

		public PagePart getPart() {
			return part;
		}
		
		public void setPart(PagePart part) {
			this.part = part;
		}

		public RichTextElement getFormItem() {
			return formItem;
		}

		public void setFormItem(RichTextElement formItem) {
			this.formItem = formItem;
			formItem.setUserObject(this);
		}
		
		public String getComponentName() {
			return formItem.getComponent().getComponentName();
		}
	}
}
