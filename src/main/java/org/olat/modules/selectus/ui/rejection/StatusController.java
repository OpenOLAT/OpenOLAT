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
package org.olat.modules.selectus.ui.rejection;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextBoxListElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.textboxlist.TextBoxItem;
import org.olat.core.gui.components.textboxlist.TextBoxItemImpl;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingPositionSecurityCallback;
import org.olat.modules.selectus.TaggingService;
import org.olat.modules.selectus.model.Category;
import org.olat.modules.selectus.model.mail.EmailVariables;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.comparator.TextBoxItemComparator;

/**
 * 
 * Initial date: 13 nov. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class StatusController extends StepFormBasicController {

	private TextElement statusCommentEl;
	private TextElement statusDayElement;
	private TextElement statusYearElement;
	private SingleSelection statusMonthElement;
	private SingleSelection changeStatusEl;
	private TextBoxListElement categoriesEl;
	private FormLayoutContainer statusContainer;
	
	private final String[] monthKeys = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11"};
	private final String[] monthValues = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12"};
	
	private final EmailVariables emailVar;
	private final RecruitingPositionSecurityCallback secCallback;
	
	private CloseableModalController cmc;
	private AttachmentWarningController attachmentWarningCtrl;

	@Autowired
	private TaggingService taggingService;
	@Autowired
	private RecruitingModule recruitingModule;
	
	public StatusController(UserRequest ureq, WindowControl wControl, StepsRunContext runContext,
			Form form, EmailVariables emailVar, RecruitingPositionSecurityCallback secCallback) {
		super(ureq, wControl, form, runContext, LAYOUT_DEFAULT, null);
		setTranslator(Util.createPackageTranslator(PositionController.class, getLocale(), getTranslator()));
		this.emailVar = emailVar;
		this.secCallback = secCallback;
		
		for(int i=monthKeys.length; i-->0; ) {
			monthValues[i] = translate("month.long." + i);
		}

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(recruitingModule.isCategoriesEnabledFor(emailVar.getPosition())) {
			setFormDescription("wizard.mail.status.categories.description");
		} else {
			setFormDescription("wizard.mail.status.description");
		}
		formLayout.setElementCssClass("o_sel_rejection_mail_to_status");
		
		SelectionValues statusKeyValues = new SelectionValues();
		statusKeyValues.add(SelectionValues.entry("-", translate("change.status.not")));
		ApplicationStatus[] availableStatus = recruitingModule.getApplicationAvailableStatus();
		for(ApplicationStatus status:availableStatus) {
			statusKeyValues.add(SelectionValues.entry(status.name(), translate("application.status.".concat(status.name()))));
		}

		changeStatusEl = uifactory.addDropdownSingleselect("change.status", formLayout, statusKeyValues.keys(), statusKeyValues.values(), null);
		changeStatusEl.addActionListener(FormEvent.ONCHANGE);
		changeStatusEl.select("-", true);
		
		// status date container
		String pageDeadline = velocity_root + "/edit_deadline.html";
		statusContainer = FormLayoutContainer.createCustomFormLayout("status.container", getTranslator(), pageDeadline);
		statusContainer.setRootForm(mainForm);
		statusContainer.setLabel("edit.application.status.date", null);
		formLayout.add(statusContainer);
		statusContainer.setVisible(false);

		statusDayElement = uifactory.addTextElement("deadline.day", "", 2, null, statusContainer);
		statusDayElement.setDomReplacementWrapperRequired(false);
		statusDayElement.setDisplaySize(2);
		statusDayElement.setMandatory(true);
		
		statusMonthElement = uifactory.addDropdownSingleselect("deadline.month", "", statusContainer, monthKeys, monthValues, null);
		statusMonthElement.setDomReplacementWrapperRequired(false);
		statusMonthElement.setMandatory(true);
		
		statusYearElement = uifactory.addTextElement("deadline.year", "", 4, null, statusContainer);
		statusYearElement.setDomReplacementWrapperRequired(false);
		statusYearElement.setDisplaySize(4);
		statusYearElement.setMandatory(true);
		
		setStatusDate(new Date());
		
		statusCommentEl = uifactory.addTextAreaElement("statusComment", "edit.application.status.comment",
				32000, 4, 60, false, false, false, null, formLayout);
		statusCommentEl.setVisible(false);
		
		if(recruitingModule.isCategoriesEnabledFor(emailVar.getPosition())) {
			categoriesEl = uifactory.addTextBoxListElement("add.categories", "add.categories", null,
					new ArrayList<>(), formLayout, getTranslator());
			categoriesEl.setHelpText(translate("categories.plural.hint"));
			categoriesEl.setAllowDuplicates(false);
			categoriesEl.setAllowNewValues(secCallback.canEditPositionCategories());

			List<TextBoxItem> allCategoryItems = getAllCategories();
			categoriesEl.setAutoCompleteContent(allCategoryItems);
		}
	}
	
	private List<TextBoxItem> getAllCategories() {
		List<TextBoxItem> items = new ArrayList<>();
		List<Category> allCategories = taggingService.getAvailableCategoriesFor(emailVar.getPosition());
		if(secCallback.canEditApplicationAdministrativeCategories()) {
			for(Category category:allCategories) {
				String name = "a:".concat(category.getName());
				items.add(new TextBoxItemImpl(name, name, category.getColor(), true, category));
			}
		}
		for(Category category:allCategories) {
			items.add(new TextBoxItemImpl(category.getName(), category.getName(), category.getColor(), true, category));
		}
		Collections.sort(items, new TextBoxItemComparator());
		return items;
	}
	
	public String getApplicationsGroups() {
		StringBuilder sb = new StringBuilder();
		if(emailVar.getApplicationsGroups() != null && !emailVar.getApplicationsGroups().isEmpty()) {
			for(String group:emailVar.getApplicationsGroups()) {
				if(sb.length() > 0) sb.append(", ");
				sb.append(group);
			}
		}
		return sb.toString();
	}
	
	private void updateStatusCommentPlaceholder() {
		if(changeStatusEl.isOneSelected()) {
			if("-".equals(changeStatusEl.getSelectedKey())) {
				statusCommentEl.setPlaceholderKey(null, null);
			} else {
				ApplicationStatus status = ApplicationStatus.valueOf(changeStatusEl.getSelectedKey());
				if(status == ApplicationStatus.active) {
					statusCommentEl.setPlaceholderKey(null, null);
				} else {
					statusCommentEl.setPlaceholderKey("edit.application.status.comment.placeholder", null);
				}
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(attachmentWarningCtrl == source) {
			cmc.deactivate();
			cleanUp();
			if(event == Event.DONE_EVENT) {
				formOK(ureq);
			}
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(attachmentWarningCtrl);
		removeAsListenerAndDispose(cmc);
		attachmentWarningCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		changeStatusEl.clearError();
		if(!changeStatusEl.isOneSelected()) {
			changeStatusEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		statusContainer.clearError();
		if(changeStatusEl.isOneSelected() && !"-".equals(changeStatusEl.getSelectedKey()) && getStatusDate() == null) {
			statusContainer.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(changeStatusEl == source) {
			boolean visible = (changeStatusEl.isOneSelected() && !"-".equals(changeStatusEl.getSelectedKey()));
			statusCommentEl.setVisible(visible);
			boolean dateVisible = visible && !ApplicationStatus.active.name().equals(changeStatusEl.getSelectedKey());
			statusContainer.setVisible(dateVisible);
			statusDayElement.setVisible(dateVisible);
			statusMonthElement.setVisible(dateVisible);
			statusYearElement.setVisible(dateVisible);
			
			updateStatusCommentPlaceholder();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formFinish(UserRequest ureq) {
		if(emailVar.isShowAttachmentWarning()) {
			attachmentWarningCtrl = new AttachmentWarningController(ureq, getWindowControl(), emailVar);
			listenTo(attachmentWarningCtrl);
			
			cmc = new CloseableModalController(getWindowControl(), "c", attachmentWarningCtrl.getInitialComponent(), translate("rejection.quick.view"));
			cmc.activate();
			listenTo(cmc);
		} else {
			formOK(ureq);
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(categoriesEl != null) {
			emailVar.setCategoriesToAdd(categoriesEl.getValueList());
		}
		if(changeStatusEl.isOneSelected() && !"-".equals(changeStatusEl.getSelectedKey())) {
			ApplicationStatus status = ApplicationStatus.valueOf(changeStatusEl.getSelectedKey());
			emailVar.setApplicationStatus(status);
			emailVar.setApplicationStatusDate(getStatusDate());
			emailVar.setApplicationStatusComment(statusCommentEl.getValue());
		}
		
		fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
	}
	
	private Date getStatusDate() {
		if(!statusContainer.isVisible()) return null;
		
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
	
	private void setStatusDate(Date date) {
		String day = "";
		String month = "0";
		String year = "";
		
		if(date == null) {
			date = new Date();
		}
		Calendar cal = Calendar.getInstance();
		cal.setTime(date);
		day = Integer.toString(cal.get(Calendar.DATE));
		month = Integer.toString(cal.get(Calendar.MONTH));
		year = Integer.toString(cal.get(Calendar.YEAR));
		
		statusDayElement.setValue(day);
		statusMonthElement.select(month, true);
		statusYearElement.setValue(year);
	}
}