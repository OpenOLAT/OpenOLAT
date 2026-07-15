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
package org.olat.modules.selectus.ui.application;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.model.category.ApplicationCategoryInfos;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.comparator.TextBoxItemComparator;
import org.olat.modules.selectus.ui.components.DateCellRenderer;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ApplicationEditStatusController extends FormBasicController {

	private SingleSelection statusElement;
	
	private TextElement statusDayElement;
	private SingleSelection statusMonthElement;
	private TextElement statusYearElement;
	private FormLayoutContainer statusContainer;
	
	private TextElement statusCommentEl;

	private TextBoxListElement committeeCategoriesEl;
	
	private Application application;
	private final boolean editableStatus;
	private final boolean canEditCategories;
	private final boolean categoriesEnabled;
	private final Position position;
	private final RecruitingPositionSecurityCallback secCallback;
	
	private final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private final String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
	
	private final SelectionValues availableStatusKV;
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public ApplicationEditStatusController(UserRequest ureq, WindowControl wControl, Form rootForm,
			Application application, Position position, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		if(rootForm != null) {
			mainForm = rootForm;
			flc.setRootForm(rootForm);
			mainForm.addSubFormListener(this);
		}
		editableStatus = secCallback.canEditApplicationStatus();
		canEditCategories = secCallback.canEditApplicationCategories();
		this.position = position;
		this.secCallback = secCallback;
		categoriesEnabled = recruitingModule.isCategoriesEnabledFor(position);
		
		ApplicationStatus[] availableStatus = recruitingModule.getApplicationAvailableStatus();
		availableStatusKV = new SelectionValues();
		for(int i=0; i<availableStatus.length; i++) {
			String label = translate("application.status.".concat(availableStatus[i].name()));
			availableStatusKV.add(SelectionValues.entry(availableStatus[i].name(), label));
		}

		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}
		
		this.application = application;
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(categoriesEnabled) {
			setFormDescription("edit.application.status.with.categories.explanation");
		} else {
			setFormDescription("edit.application.status.explanation");
		}
		
		//status
		String page = velocity_root + "/edit_application_status.html";
		FormLayoutContainer submittedByStaffCont = FormLayoutContainer.createCustomFormLayout("edit.app.status", getTranslator(), page);
		submittedByStaffCont.setLabel("edit.application.submittedByStaff", null);
		submittedByStaffCont.setRootForm(mainForm);
		submittedByStaffCont.contextPut("submittedByStaff", Boolean.valueOf(application.isSubmittedByStaff()));
		formLayout.add(submittedByStaffCont);
		
		if(application.getCreationDate() != null) {
			String submittedDate = DateCellRenderer.format(application.getCreationDate(), getLocale());
			uifactory.addStaticTextElement("submittedDate", "edit.application.submittedDate", submittedDate, formLayout);
		}
		
		String appStatus = application.getApplicationStatus().name();
		if(!availableStatusKV.containsKey(appStatus) && ApplicationStatus.valid(appStatus)) {
			String label = translate("application.status.".concat(appStatus));
			availableStatusKV.add(SelectionValues.entry(appStatus, label));
		}
		statusElement = uifactory.addDropdownSingleselect("edit.application.status", formLayout,
				availableStatusKV.keys(), availableStatusKV.values(), null);
		statusElement.setElementCssClass("o_sel_edit_application_status");
		statusElement.addActionListener(FormEvent.ONCHANGE);
		statusElement.setEnabled(editableStatus);
		if(availableStatusKV.containsKey(appStatus)) {
			statusElement.select(appStatus, true);
		}
		
		// status date container
		String pageDeadline = velocity_root + "/edit_deadline.html";
		statusContainer = FormLayoutContainer.createCustomFormLayout("status.container", getTranslator(), pageDeadline);
		statusContainer.setRootForm(mainForm);
		statusContainer.setLabel("edit.application.status.date", null);
		formLayout.add(statusContainer);

		statusDayElement = uifactory.addTextElement("deadline.day", "", 2, null, statusContainer);
		statusDayElement.setDomReplacementWrapperRequired(false);
		statusDayElement.setDisplaySize(2);
		statusDayElement.setMandatory(true);
		statusDayElement.setEnabled(editableStatus);
		
		statusMonthElement = uifactory.addDropdownSingleselect("deadline.month", "", statusContainer, monthKeys, monthValues, null);
		statusMonthElement.setDomReplacementWrapperRequired(false);
		statusMonthElement.setMandatory(true);
		statusMonthElement.setEnabled(editableStatus);
		
		statusYearElement = uifactory.addTextElement("deadline.year", "", 4, null, statusContainer);
		statusYearElement.setDomReplacementWrapperRequired(false);
		statusYearElement.setDisplaySize(4);
		statusYearElement.setMandatory(true);
		statusYearElement.setEnabled(editableStatus);
		
		updateStatusDate();
		
		String comment = application.getStatusComment();
		statusCommentEl = uifactory.addTextAreaElement("statusComment", "edit.application.status.comment",
				32000, 4, 60, false, false, false, comment, formLayout);
		statusCommentEl.setElementCssClass("o_sel_edit_application_status_comment");
		statusCommentEl.setEnabled(editableStatus);
		
		updateStatusCommentPlaceholder();

		if(categoriesEnabled) {
			List<TextBoxItem> currentCategoriesNames = getCategories();
			Collections.sort(currentCategoriesNames, new TextBoxItemComparator());
			committeeCategoriesEl = uifactory.addTextBoxListElement("committee.categories", "categories", null, currentCategoriesNames, formLayout, getTranslator());
			committeeCategoriesEl.setEnabled(secCallback.canEditApplicationCategories());
			committeeCategoriesEl.setElementCssClass("o_app_details_categories");
			
			if(canEditCategories) {
				String hint = translate("add.categories.details.hint");
				if(secCallback.canEditPositionCategories()) {
					hint += "<br>" + translate("add.categories.plural.new.hint");
				}
				if(secCallback.canEditApplicationAdministrativeCategories()) {
					hint += "<br>" + translate("add.categories.plural.administrative.hint");
				}
				hint += "<br>" + translate("remove.categories.details.hint");
				committeeCategoriesEl.setHelpText(hint);
				committeeCategoriesEl.setAllowDuplicates(false);
				committeeCategoriesEl.setAllowNewValues(secCallback.canEditApplicationCategories());
				List<TextBoxItem> allItems = getAllCategories();
				committeeCategoriesEl.setAutoCompleteContent(allItems);
			}
		}
	}
	
	private List<TextBoxItem> getAllCategories() {
		List<TextBoxItem> items = new ArrayList<>();
		if(secCallback.canEditApplicationCategories() || secCallback.canEditApplicationAdministrativeCategories()) {
			List<Category> allCategories = taggingService.getAvailableCategoriesFor(position);
			if(secCallback.canEditApplicationAdministrativeCategories()) {
				for(Category category:allCategories) {
					String name = "a:".concat(category.getName());
					items.add(new TextBoxItemImpl(name, name, category.getColor(), true, category));
				}
			}
			for(Category category:allCategories) {
				items.add(new TextBoxItemImpl(category.getName(), category.getName(), category.getColor(), true, category));
			}
		}
		Collections.sort(items, new TextBoxItemComparator());
		return items;
	}
	
	private List<TextBoxItem> getCategories() {
		List<ApplicationCategoryInfos> currentCategories = taggingService.getApplicationCategories(position, application,
				secCallback.canSeeApplicationAdministrativeCategories());
		boolean canEditAdministrative = secCallback.canEditApplicationAdministrativeCategories();
		return currentCategories.stream()
				.map(cat -> new TextBoxItemImpl(cat.tagName(), cat.tagName(), cat.getCategory().getColor(),
						canEditAdministrative || !cat.isAdministrative(),
						cat))
				.collect(Collectors.toList());
	}

	@Override
	protected void doDispose() {
		mainForm.removeSubFormListener(this);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == statusElement) {
			updateStatusDate();
			updateStatusCommentPlaceholder();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void updateStatusCommentPlaceholder() {
		if(statusElement.isOneSelected()) {
			ApplicationStatus status = ApplicationStatus.valueOf(statusElement.getSelectedKey());
			if(status == ApplicationStatus.active) {
				statusCommentEl.setPlaceholderKey(null, null);
			} else {
				statusCommentEl.setPlaceholderKey("edit.application.status.comment.placeholder", null);
			}
		}
	}
	
	private void updateStatusDate() {
		if(statusElement.isOneSelected()) {
			ApplicationStatus status = ApplicationStatus.valueOf(statusElement.getSelectedKey());
			switch(status) {
				case active: setStatusDate(application.getCreationDate(), false); break;
				case onhold: setStatusDate(application.getOnholdDate(), true); break;
				case withdrawn: setStatusDate(application.getWithdrawnDate(), true); break;
				case rejected: setStatusDate(application.getRejectedDate(), true); break;
				case noteligible: setStatusDate(application.getNotEligibleDate(), true); break;
				case granted: setStatusDate(application.getGrantedDate(), true); break;
				case hired: setStatusDate(application.getHiredDate(), true); break;
				default: setStatusDate(new Date(), false); break;
			}
		}
	}
	
	private void setStatusDate(Date date, boolean editable) {
		if(date == null) {
			date = new Date();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		String day = Integer.toString(cal.get(Calendar.DATE));
		String month = Integer.toString(cal.get(Calendar.MONTH));
		String year = Integer.toString(cal.get(Calendar.YEAR));
		
		statusDayElement.setValue(day);
		statusDayElement.setEnabled(editable);
		statusMonthElement.select(month, true);
		statusMonthElement.setEnabled(editable);
		statusYearElement.setValue(year);
		statusYearElement.setEnabled(editable);
	}

	public Application commitChanges(Application app) {
		
		ApplicationStatus currentStatus = app.getApplicationStatus();
		
		String before = auditService.toAuditXml(app);
		
		ApplicationStatus status = ApplicationStatus.valueOf(statusElement.getSelectedKey());
		app.setApplicationStatus(status);
		Date statusDate = getStatusDate();
		switch(status) {
			case onhold: app.setOnholdDate(statusDate); break;
			case withdrawn: app.setWithdrawnDate(statusDate); break;
			case rejected: app.setRejectedDate(statusDate); break;
			case noteligible: app.setNotEligibleDate(statusDate); break;
			case granted: app.setGrantedDate(statusDate); break;
			case hired: app.setHiredDate(statusDate); break;
			default: break;
		}

		app.setStatusComment(statusCommentEl.getValue());

		application = erFrontendManager.saveTempApplication(app, false);

		//sync categories
		if(committeeCategoriesEl != null && committeeCategoriesEl.isEnabled()) {
			List<String> selectedCategories = committeeCategoriesEl.getValueList();
			taggingService.setCategories(app, selectedCategories, secCallback.canEditApplicationAdministrativeCategories(),
					position, getIdentity(), getLocale());
		}

		String after  = auditService.toAuditXml(application);
		String appId = application.getId() == null ? null : application.getId().toString();
		if(currentStatus != status) {
			if(status == ApplicationStatus.active) {
				String messageI18n = "audit.log.application.revert.".concat(currentStatus.name());
				String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(application, getLocale()), appId };
				auditService.auditApplicationLog(currentStatus.revertAction(), ActionTarget.application, before, after,
						messageI18n, messageArgs, getTranslator(), app.getPosition(), app, getIdentity());
			} else {
				String messageI18n = "audit.log.application.".concat(status.name());
				String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(application, getLocale()), appId };
				auditService.auditApplicationLog(status.action(), ActionTarget.application, before, after,
						messageI18n, messageArgs, getTranslator(), app.getPosition(), app, getIdentity());
			} 
		}
		
		return application;
	}
	
	private Date getStatusDate() {
		String dayStr = statusDayElement.getValue();
		String monthStr = statusMonthElement.getSelectedKey();
		String yearStr = statusYearElement.getValue();
		
		try {
			int day = Integer.parseInt(dayStr);
			int month = Integer.parseInt(monthStr);
			int year = Integer.parseInt(yearStr);
			return getDate(day, month, year);
		} catch (NumberFormatException e) {
			logDebug("Cannot parse date from: " + dayStr + "." + monthStr + "." + yearStr);
			return null;
		}
	}
	
	private Date getDate(int day, int month, int year) {
		Calendar cal = Calendar.getInstance();
		cal.set(Calendar.YEAR, year);
		cal.set(Calendar.MONTH, month);
		cal.set(Calendar.DATE, day);
		cal.set(Calendar.HOUR, 0);
		cal.set(Calendar.MINUTE, 0);
		cal.set(Calendar.SECOND, 0);
		cal.set(Calendar.MILLISECOND, 0);
		return cal.getTime();
	}
	
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		statusElement.clearError();
		if(!statusElement.isOneSelected()) {
			statusElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		statusContainer.clearError();
		if(statusElement.isOneSelected() && getStatusDate() == null) {
			statusContainer.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
}
