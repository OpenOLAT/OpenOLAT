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

import org.olat.admin.securitygroup.gui.GroupController;
import org.olat.admin.securitygroup.gui.IdentitiesAddEvent;
import org.olat.admin.securitygroup.gui.IdentitiesRemoveEvent;
import org.olat.core.commons.services.license.LicenseModule;
import org.olat.core.commons.services.license.LicenseService;
import org.olat.core.commons.services.license.LicenseType;
import org.olat.core.commons.services.license.ResourceLicense;
import org.olat.core.commons.services.license.ui.LicenseSelectionConfig;
import org.olat.core.commons.services.license.ui.LicenseUIFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
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
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.qpool.MetadataSecurityCallback;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.modules.qpool.manager.QuestionPoolLicenseHandler;
import org.olat.modules.qpool.model.QuestionItemImpl;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.qpool.ui.events.QItemEdited;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 05.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RightsMetadataEditController extends FormBasicController {
	
	private Link managerOwners;
	private SingleSelection licenseEl;
	private TextElement licensorEl;
	private TextElement licenseFreetextEl;
	private StaticTextElement licenseTextEl;
	private FormLayoutContainer authorCont;
	private FormLayoutContainer buttonsCont;
	private FormSubmit okButton;

	private CloseableModalController cmc;
	private GroupController groupController;

	private QuestionItem item;
	private MetadataSecurityCallback securityCallback;
	private ResourceLicense license;
	
	@Autowired
	private QPoolService qpoolService;
	@Autowired
	private UserManager userManager;
	@Autowired
	private LicenseService licenseService;
	@Autowired
	private LicenseModule licenseModule;
	@Autowired
	private QuestionPoolLicenseHandler licenseHandler;

	public RightsMetadataEditController(UserRequest ureq, WindowControl wControl, QuestionItem item,
			MetadataSecurityCallback securityCallback, boolean wideLayout) {
		super(ureq, wControl, wideLayout ? LAYOUT_DEFAULT : LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(QuestionsController.class, getLocale(), getTranslator()));
		
		this.item = item;
		this.securityCallback = securityCallback;
		
		initForm(ureq);
		setReadOnly();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String authorListPage = velocity_root + "/author_list.html";
		authorCont = FormLayoutContainer.createCustomFormLayout("owners", getTranslator(), authorListPage);
		authorCont.setLabel("rights.owners", null);
		formLayout.add(authorCont);
		authorCont.setRootForm(mainForm);
		reloadAuthors();
		
		VelocityContainer vc = authorCont.getFormItemComponent();
		managerOwners = LinkFactory.createButton("manage.owners", vc, this);
		authorCont.put("manage.owners", managerOwners);
		
		if (licenseModule.isEnabled(licenseHandler)) {
			license = loadLicense();
			
			if(license != null) {
				LicenseSelectionConfig licenseSelectionConfig = LicenseUIFactory
						.createLicenseSelectionConfig(licenseHandler, license.getLicenseType());
				licenseEl = uifactory.addDropdownSingleselect("rights.license", formLayout,
						licenseSelectionConfig.getLicenseTypeKeys(),
						licenseSelectionConfig.getLicenseTypeValues(getLocale()));
				licenseEl.setElementCssClass("o_sel_repo_license");
				licenseEl.setMandatory(licenseSelectionConfig.isLicenseMandatory());
				if (licenseSelectionConfig.getSelectionLicenseTypeKey() != null) {
					licenseEl.select(licenseSelectionConfig.getSelectionLicenseTypeKey(), true);
				}
				licenseEl.addActionListener(FormEvent.ONCHANGE);
				
				licensorEl = uifactory.addTextElement("rights.licensor", 1000, license.getLicensor(), formLayout);
	
				String freetext = licenseService.isFreetext(license.getLicenseType()) ? license.getFreetext() : "";
				licenseFreetextEl = uifactory.addTextAreaElement("rights.freetext", 4, 72, freetext, formLayout);
				LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
	
				licenseTextEl = uifactory.addStaticTextElement("rights.licenseText", "", "", formLayout);
				updateLicenseText();
	
				buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
				buttonsCont.setRootForm(mainForm);
				formLayout.add(buttonsCont);
				uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
				okButton = uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
			}
		}
	}
	
	private ResourceLicense loadLicense() {
		ResourceLicense resourceLicense = null;
		if(item.getResourceableId() != null) {
			resourceLicense = licenseService.loadOrCreateLicense(item);
		}
		return resourceLicense;
	}
	
	private void updateLicenseText() {
		if (licenseTextEl != null && licenseEl != null && licenseEl.isOneSelected()) {
			String selectedKey = licenseEl.getSelectedKey();
			LicenseType licenseType = licenseService.loadLicenseTypeByKey(selectedKey);
			licenseTextEl.setValue(getLicenseText(licenseType));
			boolean noLicenseSelected = licenseService.isNoLicense(licenseType);
			boolean freetextSelected = licenseService.isFreetext(licenseType);
			boolean licenseTextVisible = !noLicenseSelected && !freetextSelected;
			licenseTextEl.setVisible(licenseTextVisible);;
		}
	}

	private String getLicenseText(LicenseType licenseType) {
		StringBuilder sb = new StringBuilder("");
		if (licenseType != null) {
			String css = licenseType.getCssClass();
			if (StringHelper.containsNonWhitespace(css)) {
				sb.append("<div><i class='o_icon ");
				sb.append(css);
				sb.append("'> </i></div>");
			}
			String licenseTypeText = licenseType.getText() != null? licenseType.getText(): "";
			String formattedLicenseText = Formatter.formatURLsAsLinks(Formatter.escWithBR(licenseTypeText).toString(), true);
			if (StringHelper.containsNonWhitespace(formattedLicenseText)) {
				sb.append(formattedLicenseText);
			}
		}
		return sb.toString();
	}
	
	private void setReadOnly() {
		boolean canEditMetadata = securityCallback.canEditMetadata();
		managerOwners.setVisible(securityCallback.canEditAuthors());
		if (licenseEl != null) {
			licenseEl.setEnabled(canEditMetadata);
		}
		if (licensorEl != null) {
			licensorEl.setEnabled(canEditMetadata);
		}
		if (licenseFreetextEl != null) {
			licenseFreetextEl.setEnabled(canEditMetadata);
		}
		if (buttonsCont != null) {
			buttonsCont.setVisible(canEditMetadata);
		}
	}
	
	public void setItem(QuestionItem item, MetadataSecurityCallback securityCallback) {
		this.item = item;
		this.securityCallback = securityCallback;
		this.license = licenseService.loadOrCreateLicense(item);
		updateLicenseText();
		if (securityCallback != null) {
			setReadOnly();
		}
	}

	private void reloadAuthors() {
		if(item.getKey() != null) {
			List<Identity> authors = qpoolService.getAuthors(item);
			List<String> authorLinks = new ArrayList<>(authors.size());
			for(Identity author:authors) {
				String name = userManager.getUserDisplayName(author);
				authorLinks.add(name);
			}
			authorCont.contextPut("authors", authorLinks);
		}
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
		} else if(source == cmc) {
			fireEvent(ureq, new QItemEdited(item));
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
		 if (source == licenseEl) {
			LicenseUIFactory.updateVisibility(licenseEl, licensorEl, licenseFreetextEl);
			updateLicenseText();
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
		boolean allOk = super.validateFormLogic(ureq);

		if (licenseEl != null) {
			licenseEl.clearError();
			if (LicenseUIFactory.validateLicenseTypeMandatoryButNonSelected(licenseEl)) {
				licenseEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(item instanceof QuestionItemImpl) {
			QuestionItemImpl itemImpl = (QuestionItemImpl)item;
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(getIdentity(),
					Action.UPDATE_QUESTION_ITEM_METADATA);
			builder.withBefore(itemImpl);
			
			if (licenseModule.isEnabled(licenseHandler)) {
				if (licenseEl != null && licenseEl.isOneSelected()) {
					String licenseTypeKey = licenseEl.getSelectedKey();
					LicenseType licneseType = licenseService.loadLicenseTypeByKey(licenseTypeKey);
					license.setLicenseType(licneseType);
				}
				String licensor = null;
				String freetext = null;
				if (licensorEl != null && licensorEl.isVisible()) {
					licensor = StringHelper.containsNonWhitespace(licensorEl.getValue())? licensorEl.getValue(): null;
				}
				if (licenseFreetextEl != null && licenseFreetextEl.isVisible()) {
					freetext = StringHelper.containsNonWhitespace(licenseFreetextEl.getValue())? licenseFreetextEl.getValue(): null;
				}
				license.setLicensor(licensor);
				license.setFreetext(freetext);
				license = licenseService.update(license);
				licensorEl.setValue(license.getLicensor());
				licenseFreetextEl.setValue(license.getFreetext());
			}
			
			item = qpoolService.updateItem(item);
			builder.withAfter(itemImpl);
			qpoolService.persist(builder.create());
			fireEvent(ureq, new QItemEdited(item));
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