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
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.model.QLicense;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.modules.qpool.ui.metadata.MetaUIFactory.KeyValues;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RightsMetadataEditController extends FormBasicController {
	
	private FormSubmit okButton;
	private Link managerOwners;
	private SingleSelection copyrightEl;
	private TextElement descriptionEl;
	private FormLayoutContainer authorCont;

	private CloseableModalController cmc;
	private GroupController groupController;

	private QuestionItem item;
	private final UserManager userManager;
	private final QPoolService qpoolService;

	public RightsMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		initForm(ureq);
	}
	
	private KeyValues licenseKeys;

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("rights");
		
		String authorListPage = velocity_root + "/author_list.html";
		authorCont = FormLayoutContainer.createCustomFormLayout("owners", getTranslator(), authorListPage);
		authorCont.setLabel("rights.owners", null);
		formLayout.add(authorCont);
		authorCont.setRootForm(mainForm);
		reloadAuthors();
		
		VelocityContainer vc = authorCont.getFormItemComponent();
		managerOwners = LinkFactory.createButton("manage.owners", vc, this);
		authorCont.put("manage.owners", managerOwners);

		licenseKeys = MetaUIFactory.getQLicenseKeyValues(qpoolService);

		copyrightEl = uifactory.addDropdownSingleselect("rights.copyright", "rights.copyright", formLayout,
				licenseKeys.getKeys(), licenseKeys.getValues(), null);
		copyrightEl.addActionListener(FormEvent.ONCHANGE);
		String description;
		QLicense copyright = item.getLicense();
		if(copyright == null) {
			description = "";
			copyrightEl.select(licenseKeys.getFirstKey(), true);
		} else if(isKey(copyright)) {
			description = copyright.getLicenseText();
			copyrightEl.select(copyright.getLicenseKey(), true);
		} else {
			description = copyright.getLicenseText();
			copyrightEl.select(licenseKeys.getLastKey(), true);
		}
		
		if(description == null) {
			description = "";
		}

		descriptionEl = uifactory.addTextAreaElement("rights.description", "rights.description", 1000, 6, 40, true, description, formLayout);
		descriptionEl.setVisible(copyrightEl.getSelectedKey().equals(licenseKeys.getLastKey()));

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		okButton = uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private void reloadAuthors() {
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
	
	private boolean isKey(QLicense value) {
		for(String key:licenseKeys.getKeys()) {
			if(key.equals(value.getLicenseKey())) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == groupController) {
			if(event instanceof IdentitiesAddEvent ) { 
				IdentitiesAddEvent identitiesAddedEvent = (IdentitiesAddEvent) event;
				List<Identity> list = identitiesAddedEvent.getAddIdentities();
        qpoolService.addAuthors(list, Collections.<QuestionItemShort>singletonList(item));
        identitiesAddedEvent.getAddedIdentities().addAll(list);
			} else if (event instanceof IdentitiesRemoveEvent) {
				IdentitiesRemoveEvent identitiesRemoveEvent = (IdentitiesRemoveEvent) event;
				List<Identity> list = identitiesRemoveEvent.getRemovedIdentities();
        qpoolService.removeAuthors(list, Collections.<QuestionItemShort>singletonList(item));
			}
			reloadAuthors();
			//cmc.deactivate();
			//cleanUp();
		} else if(source == cmc) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(groupController);
		removeAsListenerAndDispose(cmc);
		groupController = null;
		cmc = null;
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == managerOwners) {
			doOpenAuthorManager(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == copyrightEl) {
			String selectedKey = copyrightEl.getSelectedKey();
			descriptionEl.setVisible(selectedKey.equals(licenseKeys.getLastKey()));
			flc.setDirty(true);
		} else if(source == managerOwners) {
			okButton.getComponent().setDirty(false);
			doOpenAuthorManager(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		allOk &= MetaUIFactory.validateRights(copyrightEl, descriptionEl, licenseKeys, true);
		return allOk &= super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			formOKRights(itemImpl, copyrightEl, descriptionEl, licenseKeys, qpoolService);
		}
		item = qpoolService.updateItem(item);
		fireEvent(ureq, new QItemEdited(item));
	}
	
	protected static void formOKRights(QuestionItemImpl itemImpl, SingleSelection copyrightEl, TextElement descriptionEl,
			KeyValues licenseKeys, QPoolService qpoolService) {
		String selectedKey = copyrightEl.getSelectedKey();
		if(selectedKey.equals(licenseKeys.getFirstKey())) {
			itemImpl.setLicense(null);
		} else if(selectedKey.equals(licenseKeys.getLastKey())) {
			if (itemImpl.getLicense() != null && itemImpl.getLicense().isDeletable()) {
				String licenseText = descriptionEl.getValue();
				itemImpl.getLicense().setLicenseText(licenseText);
				qpoolService.updateLicense(itemImpl.getLicense());
			} else {
				String licenseText = descriptionEl.getValue();
				QLicense license = qpoolService.createLicense("perso-" + UUID.randomUUID().toString(), licenseText);
				itemImpl.setLicense(license);
			}
		} else {
			QLicense license = qpoolService.getLicense(selectedKey);
			itemImpl.setLicense(license);
		}
	}
	
	private void doOpenAuthorManager(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			
			groupController = new GroupController(ureq, getWindowControl(), true, true, false, true,
					false, false, itemImpl.getOwnerGroup());
			listenTo(groupController);

			cmc = new CloseableModalController(getWindowControl(), translate("close"),
					groupController.getInitialComponent(), true, translate("manage.owners"));
			cmc.activate();
			listenTo(cmc);
		}
	}
}