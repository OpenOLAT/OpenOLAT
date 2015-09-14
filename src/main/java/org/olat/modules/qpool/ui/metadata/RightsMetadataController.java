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
package org.olat.modules.qpool.ui.metadata;

import java.util.ArrayList;
import java.util.List;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.model.QLicense;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QPoolEvent;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RightsMetadataController extends FormBasicController  {
	
	private FormLink editLink;
	private StaticTextElement descriptionEl;
	private MultipleSelectionElement copyrightEl;
	private FormLayoutContainer authorCont;
	
	private final QPoolService qpoolService;
	private final UserManager userManager;
	
	private final boolean edit;
	
	public RightsMetadataController(UserRequest ureq, WindowControl wControl, QuestionItem item, boolean edit) {
		super(ureq, wControl, "view");
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		this.edit = edit;
		initForm(ureq);
		setItem(item);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("rights");
		if(edit) {
			editLink = uifactory.addFormLink("edit", "edit", null, formLayout, Link.BUTTON_XSMALL);
			editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
		}
		
		String authorListPage = velocity_root + "/author_list.html";
		authorCont = FormLayoutContainer.createCustomFormLayout("owners", getTranslator(), authorListPage);
		authorCont.setLabel("rights.owners", null);
		formLayout.add(authorCont);
		authorCont.setRootForm(mainForm);
		
		String[] keys = new String[]{ "on" };
		String[] values = new String[]{ "" };
		copyrightEl = uifactory.addCheckboxesHorizontal("rights.copyright", "rights.copyright", formLayout, keys, values);
		copyrightEl.setEnabled(false);
	
		descriptionEl = uifactory.addStaticTextElement("rights.description", "", formLayout);
	}
	
	public void setItem(QuestionItem item) {
		QLicense copyright = item.getLicense();
		if(copyright != null) {
			copyrightEl.select("on", true);
			descriptionEl.setVisible(true);
			
			String licenseKey = copyright.getLicenseKey();
			if(licenseKey != null && licenseKey.startsWith("perso-")) {
				descriptionEl.setValue(copyright.getLicenseText());
			} else {
				descriptionEl.setValue(copyright.getLicenseKey());
			}
		} else {
			copyrightEl.select("on", false);
			descriptionEl.setVisible(false);
			descriptionEl.setValue("");
		}
		
		List<Identity> authors = qpoolService.getAuthors(item);
		List<String> authorLinks = new ArrayList<String>(authors.size());
		int pos = 0;
		for(Identity author:authors) {
			String name = userManager.getUserDisplayName(author);
			FormLink link = uifactory.addFormLink("author_" + pos++, name, null, authorCont, Link.NONTRANSLATED);
			link.setUserObject(author);
			authorLinks.add(link.getComponent().getComponentName());
		}
		authorCont.contextPut("authors", authorLinks);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editLink == source) {
			fireEvent(ureq, new QPoolEvent(QPoolEvent.EDIT));
		} else if(source instanceof FormLink && ((FormLink)source).getUserObject() instanceof Identity) {
			Identity author = (Identity)((FormLink)source).getUserObject();
			String businessPath = "[Identity:" + author.getKey() + "]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
}