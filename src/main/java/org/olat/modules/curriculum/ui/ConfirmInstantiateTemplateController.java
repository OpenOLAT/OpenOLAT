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
package org.olat.modules.curriculum.ui;

import java.util.Date;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.DateChooserOrientation;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRuntimeType;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.SearchAuthorRepositoryEntryViewParams;
import org.olat.repository.ui.RepositoyUIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 févr. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class ConfirmInstantiateTemplateController extends FormBasicController {
	
	private DateChooser periodEl;
	private TextElement externalRefEl;
	private TextElement displayNameEl;
	
	private final RepositoryEntry template;
	private final CurriculumElement element;
	private RepositoryEntry instantiatedEntry;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	
	public ConfirmInstantiateTemplateController(UserRequest ureq, WindowControl wControl,
			CurriculumElement element, RepositoryEntry template) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
		this.element = element;
		this.template = template;

		initForm(ureq);
	}
	
	public RepositoryEntry getInstantiatedEntry() {
		return instantiatedEntry;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormDescription("instantiate.template.info");
		
		String displayName = element.getDisplayName();
		displayNameEl = uifactory.addTextElement("displayName", "repository.entry.displayName", 255, displayName, formLayout);
		displayNameEl.setInlineValidationOn(true);
		displayNameEl.setMandatory(true);
		if(displayNameEl.isEnabled() && !StringHelper.containsNonWhitespace(displayName)) {
			displayNameEl.setFocus(true);
		}
		
		String identifier = buildDefaultIdentifier();
		externalRefEl = uifactory.addTextElement("identifier", "curriculum.element.identifier", 64, identifier, formLayout);
		externalRefEl.setInlineValidationOn(true);
		externalRefEl.setMandatory(true);
		
		Date beginDate = element.getBeginDate();
		Date endDate = element.getEndDate();
		periodEl = uifactory.addDateChooser("cif.dates", "cif.dates", beginDate, formLayout);
		periodEl.setSecondDate(true);
		periodEl.setSecondDate(endDate);
		periodEl.setSeparator("to.separator");
		periodEl.setOrientation(DateChooserOrientation.top);

		String type = translate("runtime.type." + RepositoryEntryRuntimeType.curricular);
		uifactory.addStaticTextElement("cif.runtime.type", type, formLayout);
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttons", null, formLayout);
		uifactory.addFormSubmitButton("instantiate.template", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}
	
	private String buildDefaultIdentifier() {
		StringBuilder sb = new StringBuilder();
		if(StringHelper.containsNonWhitespace(element.getIdentifier())) {
			sb.append(element.getIdentifier());
		}
		
		if(StringHelper.containsNonWhitespace(template.getExternalRef())) {
			if(sb.length() > 0) {
				sb.append("_");
			}
			sb.append(template.getExternalRef());
		}
		return sb.toString();
	}
	
	@Override
	protected boolean validateFormItem(UserRequest ureq, FormItem item) {
		boolean ok = super.validateFormItem(ureq, item);
		
		if(item == displayNameEl) {
			validateDisplaynameUnique(ureq);
		} else if(item == externalRefEl) {
			validateExtRefUnique(ureq);
		}
		
		return ok;
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		periodEl.clearError();
		if (periodEl.getDate() != null && periodEl.getSecondDate() != null && periodEl.getDate().after(periodEl.getSecondDate())) {
			periodEl.setErrorKey("error.first.date.after.second.date");
			allOk &= false;
		}
		
		boolean displaynameOk = RepositoyUIFactory.validateTextElement(displayNameEl, true, 110);
		if (displaynameOk) {
			validateDisplaynameUnique(ureq);
		} else {
			allOk &= false;
		}
		
		boolean extRefOk = RepositoyUIFactory.validateTextElement(externalRefEl, false, 255);
		if (extRefOk) {
			validateExtRefUnique(ureq);
		} else {
			allOk &= false;
		}
		
		return allOk;
	}
	
	private void validateDisplaynameUnique(UserRequest ureq) {
		displayNameEl.clearWarning();
		if (StringHelper.containsNonWhitespace(displayNameEl.getValue()) && !StringHelper.containsNonWhitespace(externalRefEl.getValue())) {
			SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			params.setStatus(RepositoryEntryStatusEnum.preparationToPublished());
			params.setExactSearch(true);
			params.setDisplayname(displayNameEl.getValue().toLowerCase());
			if (repositoryService.countAuthorView(params) > 0) {
				displayNameEl.setWarningKey("error.exists.displayname");
			}
		}
	}
	
	private void validateExtRefUnique(UserRequest ureq) {
		externalRefEl.clearWarning();
		if (StringHelper.containsNonWhitespace(externalRefEl.getValue())) {
			SearchAuthorRepositoryEntryViewParams params = new SearchAuthorRepositoryEntryViewParams(getIdentity(), ureq.getUserSession().getRoles());
			params.setStatus(RepositoryEntryStatusEnum.preparationToPublished());
			params.setExactSearch(true);
			params.setReference(externalRefEl.getValue().toLowerCase());
			if (repositoryService.countAuthorView(params) > 0) {
				externalRefEl.setWarningKey("error.exists.ext.ref");
			}
		}
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		String displayname = displayNameEl.getValue();
		String externalRef = externalRefEl.getValue();
		Date beginDate = periodEl.getDate();
		Date endDate = periodEl.getSecondDate();		
		instantiatedEntry = curriculumService.instantiateTemplate(template, element,
				displayname, externalRef, beginDate, endDate, getIdentity());
		
		// Make sure all is committed before sending event
		dbInstance.commit();
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
