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
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;

import org.olat.modules.selectus.ApplicationStatus;
import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.SalutationGenerator;
import org.olat.modules.selectus.model.Application;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.model.ApplicationRow;

/**
 * 
 * Initial date: 28 oct. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BatchApplicationStatusController extends FormBasicController {

	private TextElement statusCommentEl;
	private SingleSelection statusElement;
	private DateChooser statusDateElement;
	
	private List<ApplicationRow> applications;
	
	
	private final String[] statusKeys;
	private final String[] statusValues;
	
	@Autowired
	private AuditService auditService;
	@Autowired
	private RecruitingModule recruitingModule;
	@Autowired
	private RecruitingService erFrontendManager;
	@Autowired @Qualifier("salutationGenerator")
	private SalutationGenerator salutationGenerator;

	public BatchApplicationStatusController(UserRequest ureq, WindowControl wControl,
			List<? extends ApplicationRow> applications) {
		super(ureq, wControl, Util.createPackageTranslator(PositionController.class, ureq.getLocale()));

		ApplicationStatus[] availableStatus = recruitingModule.getApplicationAvailableStatus();
		statusKeys = new String[availableStatus.length];
		statusValues = new String[availableStatus.length];
		for(int i=availableStatus.length; i-->0; ) {
			statusKeys[i] = availableStatus[i].name();
			statusValues[i] = translate("application.status.".concat(availableStatus[i].name()));
		}
		
		this.applications = new ArrayList<>(applications);
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		StringBuilder names = new StringBuilder();
		for(ApplicationRow app:applications) {
			String fullname = salutationGenerator.getFullname(app.getApplication(), getLocale());
			if(StringHelper.containsNonWhitespace(fullname)) {
				if(names.length() > 0) names.append(", ");
				names.append(fullname);
			}
		}
		setFormDescription("batch.status.explanation", new String[] { names.toString() });

		statusElement = uifactory.addDropdownSingleselect("edit.application.status", formLayout,  statusKeys, statusValues, null);
		statusElement.setElementCssClass("o_sel_edit_application_status");
		statusElement.select(statusKeys[0], true);
		statusElement.addActionListener(FormEvent.ONCHANGE);
		
		statusDateElement = uifactory.addDateChooser("edit.application.status.date", "edit.application.status.date", null, formLayout);
		updateStatusDate();
		
		statusCommentEl = uifactory.addTextAreaElement("statusComment", "edit.application.status.comment",
				32000, 4, 60, false, false, false, "", formLayout);
		statusCommentEl.setElementCssClass("o_sel_edit_application_status_comment");
		updateStatusCommentPlaceholder();
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormSubmitButton("save", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
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
			if(applications.size() == 1) {
				ApplicationLight application = applications.get(0).getApplication();
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
			} else {
				statusDateElement.setEnabled(status != ApplicationStatus.active);
				if(status == ApplicationStatus.active) {
					statusDateElement.setDate(null);
				} else if(statusDateElement.getDate() == null) {
					statusDateElement.setDate(new Date());
				}
			}
		}
	}
	
	private void setStatusDate(Date date, boolean editable) {
		if(date == null) {
			date = new Date();
		}
		statusDateElement.setDate(date);
		statusDateElement.setEnabled(editable);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		statusElement.clearError();
		if(!statusElement.isOneSelected()) {
			statusElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		statusDateElement.clearError();
		if(statusElement.isOneSelected()
				&& !ApplicationStatus.active.name().equals(statusElement.getSelectedKey())
				&& statusDateElement.getDate() == null) {
			statusDateElement.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		ApplicationStatus status = ApplicationStatus.valueOf(statusElement.getSelectedKey());
		Date statusDate = statusDateElement.getDate();
		
		for(ApplicationRow app:applications) {
			Application application = erFrontendManager.getApplicationByKey(app.getKey());
			updateStatus(application, statusDate, status);
		}
		
		fireEvent(ureq, Event.DONE_EVENT);
	}
	
	public void updateStatus(Application app, Date statusDate, ApplicationStatus status) {
		ApplicationStatus currentStatus = app.getApplicationStatus();
		
		String before = auditService.toAuditXml(app);
		
		app.setApplicationStatus(status);
		switch(status) {
			case onhold: app.setOnholdDate(statusDate); break;
			case withdrawn: app.setWithdrawnDate(statusDate); break;
			case rejected: app.setRejectedDate(statusDate); break;
			case noteligible: app.setNotEligibleDate(statusDate); break;
			case granted: app.setGrantedDate(statusDate); break;
			case hired: app.setHiredDate(statusDate); break;
			default: break;
		}
		
		String comment = statusCommentEl.getValue();
		if(StringHelper.containsNonWhitespace(app.getStatusComment())) {
			app.setStatusComment(app.getStatusComment() + "\n" + comment);
		} else {
			app.setStatusComment(comment);
		}
		app = erFrontendManager.saveTempApplication(app, false);

		String after  = auditService.toAuditXml(app);
		String appId = app.getId() == null ? null : app.getId().toString();
		if(currentStatus != status) {
			if(status == ApplicationStatus.active) {
				String messageI18n = "audit.log.application.revert.".concat(currentStatus.name());
				String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(app, getLocale()), appId };
				auditService.auditApplicationLog(currentStatus.revertAction(), ActionTarget.application, before, after,
						messageI18n, messageArgs, getTranslator(), app.getPosition(), app, getIdentity());
			} else {
				String messageI18n = "audit.log.application.".concat(status.name());
				String[] messageArgs = new String[] { salutationGenerator.getTitleFullname(app, getLocale()), appId };
				auditService.auditApplicationLog(status.action(), ActionTarget.application, before, after,
						messageI18n, messageArgs, getTranslator(), app.getPosition(), app, getIdentity());
			} 
		}
	}
}
