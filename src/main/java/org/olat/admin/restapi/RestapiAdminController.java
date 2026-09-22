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
package org.olat.admin.restapi;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.commons.calendar.CalendarModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.helpers.Settings;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.certificate.CertificatesModule;
import org.olat.group.BusinessGroupModule;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.lecture.LectureModule;
import org.olat.repository.RepositoryModule;
import org.olat.restapi.RestModule;
import org.olat.restapi.RestModule.ApiAccess;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.user.UserModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * This is a controller to configure the REST API and the
 * managed courses, groups and calendars.
 * 
 * <P>
 * Initial Date:  21 sept. 2009 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, https://ww.frentix.com
 */
public class RestapiAdminController extends FormBasicController {

	private static final String ON_KEY = "on";
	private static final String[] keys = { ON_KEY };
	
	private FormToggle enabledButton;
	private SingleSelection accessApiEl;
	private MultipleSelectionElement generateApiKeyEl;
	
	private MultipleSelectionElement managedRepoEl;
	private MultipleSelectionElement managedGroupsEl;
	private MultipleSelectionElement managedCalendarEl;
	private MultipleSelectionElement managedRelationRole;
	private MultipleSelectionElement managedCertificatesEl;
	private MultipleSelectionElement managedUserPortraitEl;
	private MultipleSelectionElement managedLecturesEl;
	private MultipleSelectionElement managedCurriculumEl;
	private MultipleSelectionElement managedAssessmentModeEl;
	private FormLayoutContainer docLinkFlc;
	
	private FormLayoutContainer auditCont;
	private FormToggle auditEnabledEl;
	private MultipleSelectionElement auditReadsEl;
	private MultipleSelectionElement auditBodyEl;
	private IntegerElement auditBodyMaxSizeEl;
	private IntegerElement auditRetentionDaysEl;
	
	private DialogBoxController confirmCalendarDisableCrtl;
	
	@Autowired
	private RestModule restModule;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private CalendarModule calendarModule;
	@Autowired
	private BusinessGroupModule groupModule;
	@Autowired
	private RepositoryModule repositoryModule;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private AssessmentModule assessmentModule;
	@Autowired
	private CertificatesModule certificateModule;
	@Autowired
	private UserModule userModule;

	public RestapiAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "rest");
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initRestForm(formLayout);
		initAuditForm(formLayout);
		initManagedForm(formLayout);
	}

	private void initRestForm(FormItemContainer formLayout) {
		FormLayoutContainer restCont = uifactory.addDefaultFormLayout("restCont", null, formLayout);
		restCont.setFormTitle(translate("rest.title"));
		restCont.setFormInfo(translate("rest.intro"));
		restCont.setFormContextHelp("manual_admin/administration/REST_API/");

		boolean restEnabled = restModule.isEnabled();
		String[] valueOn = new String[] { getTranslator().translate("rest.on") };
		enabledButton = uifactory.addToggleButton("rest.enabled", "rest.enabled", translate("on"), translate("off"), restCont);
		if(restEnabled) {
			enabledButton.toggleOn();
		} else {
			enabledButton.toggleOff();
		}
		enabledButton.addActionListener(FormEvent.ONCHANGE);
		
		generateApiKeyEl = uifactory.addCheckboxesHorizontal("generate.api.key", restCont, keys, valueOn);
		generateApiKeyEl.addActionListener(FormEvent.ONCHANGE);
		generateApiKeyEl.select(ON_KEY, restModule.isUserAllowedGenerateApiKey());
		generateApiKeyEl.setVisible(restEnabled);
		
		SelectionValues accessApiPK = new SelectionValues();
		accessApiPK.add(SelectionValues.entry(ApiAccess.all.name(), translate("api.access.all")));
		accessApiPK.add(SelectionValues.entry(ApiAccess.apikey.name(), translate("api.access.apikey")));
		accessApiEl = uifactory.addDropdownSingleselect("api.access", restCont, accessApiPK.keys(), accessApiPK.values());
		accessApiEl.addActionListener(FormEvent.ONCHANGE);
		accessApiEl.setVisible(restEnabled);
		accessApiEl.select(restModule.getApiAccess().name(), true);
		
		docLinkFlc = uifactory.addCustomFormLayout("doc_link", "rest.doc.openapi.title", velocity_root + "/docLink.html", restCont);
		docLinkFlc.setVisible(restEnabled);
		
		String openApiLink = Settings.getServerContextPathURI() + RestSecurityHelper.SUB_CONTEXT + "/openapi.json";
		docLinkFlc.contextPut("openApiLink", openApiLink);
		String swaggerUiUrl = Settings.getServerContextPathURI() + RestSecurityHelper.SUB_CONTEXT + "/api-docs/";
		docLinkFlc.contextPut("swaggerUiLink", swaggerUiUrl);
	}

	private void initAuditForm(FormItemContainer formLayout) {
		boolean restEnabled = restModule.isEnabled();
		auditCont = uifactory.addDefaultFormLayout("auditCont", null, formLayout);
		auditCont.setFormTitle(translate("auditlog.title"));
		auditCont.setFormInfo(translate("auditlog.intro"));
		auditCont.setFormContextHelp("manual_admin/administration/REST_API/#audit-log");
		auditCont.setVisible(restEnabled);
		
		auditEnabledEl = uifactory.addToggleButton("auditlog.enabled", "auditlog.enabled", translate("on"), translate("off"), auditCont);
		auditEnabledEl.addActionListener(FormEvent.ONCHANGE);
		if(restModule.isAuditLogEnabled()) {
			auditEnabledEl.toggleOn();
		} else {
			auditEnabledEl.toggleOff();
		}

		String[] valueOn = new String[] { getTranslator().translate("rest.on") };
		auditReadsEl = uifactory.addCheckboxesHorizontal("auditlog.reads", auditCont, keys, valueOn);
		auditReadsEl.select(ON_KEY, restModule.isAuditLogReads());
		auditReadsEl.setExampleKey("auditlog.reads.hint", null);
		
		auditBodyEl = uifactory.addCheckboxesHorizontal("auditlog.body", auditCont, keys, valueOn);
		auditBodyEl.addActionListener(FormEvent.ONCHANGE);
		auditBodyEl.select(ON_KEY, restModule.isAuditLogBody());
		auditBodyEl.setExampleKey("auditlog.body.hint", null);
		
		auditBodyMaxSizeEl = uifactory.addIntegerElement("auditlog.body.maxsize", "auditlog.body.maxsize", restModule.getAuditLogBodyMaxSize(), auditCont);
		auditBodyMaxSizeEl.setMinValueCheck(1024, "form.error.positive.integer");
		auditBodyMaxSizeEl.setMaxValueCheck(1048576, "form.error.positive.integer");
		
		auditRetentionDaysEl = uifactory.addIntegerElement("auditlog.retention.days", "auditlog.retention.days", restModule.getAuditLogRetentionDays(), auditCont);
		auditRetentionDaysEl.setMinValueCheck(0, "form.error.positive.integer");
		updateAuditVisibility();
	}

	private void initManagedForm(FormItemContainer formLayout) {
		FormLayoutContainer managedCont = uifactory.addDefaultFormLayout("managedCont", null, formLayout);
		managedCont.setFormTitle(translate("managed.objects.title"));
		managedCont.setFormInfo(translate("managed.info"));
		managedCont.setFormContextHelp("manual_admin/administration/REST_API/#managed");

		String[] valueOn = new String[] { getTranslator().translate("rest.on") };
		managedGroupsEl = uifactory.addCheckboxesHorizontal("managed.group", managedCont, keys, valueOn);
		managedGroupsEl.select(keys[0], groupModule.isManagedBusinessGroups());
		
		managedRepoEl = uifactory.addCheckboxesHorizontal("managed.repo", managedCont, keys, valueOn);
		managedRepoEl.select(keys[0], repositoryModule.isManagedRepositoryEntries());
		
		managedAssessmentModeEl = uifactory.addCheckboxesHorizontal("managed.assessment.modes", managedCont, keys, valueOn);
		managedAssessmentModeEl.select(keys[0], assessmentModule.isManagedAssessmentModes());
		
		managedLecturesEl = uifactory.addCheckboxesHorizontal("managed.lectures", managedCont, keys, valueOn);
		managedLecturesEl.select(keys[0], lectureModule.isLecturesManaged());
		
		managedCurriculumEl = uifactory.addCheckboxesHorizontal("managed.curriculum", managedCont, keys, valueOn);
		managedCurriculumEl.select(keys[0], curriculumModule.isCurriculumManaged());
		
		managedCalendarEl = uifactory.addCheckboxesHorizontal("managed.cal", managedCont, keys, valueOn);
		managedCalendarEl.select(keys[0], calendarModule.isManagedCalendars());
		managedCalendarEl.addActionListener(FormEvent.ONCHANGE);
		
		managedRelationRole = uifactory.addCheckboxesHorizontal("managed.relation.role", managedCont, keys, valueOn);
		managedRelationRole.select(keys[0], securityModule.isRelationRoleManaged());
		
		managedCertificatesEl = uifactory.addCheckboxesHorizontal("managed.certificates", managedCont, keys, valueOn);
		managedCertificatesEl.select(keys[0], certificateModule.isManagedCertificates());
		
		managedUserPortraitEl = uifactory.addCheckboxesHorizontal("managed.user.portrait", managedCont, keys, valueOn);
		managedUserPortraitEl.select(keys[0], userModule.isPortraitManaged());
		
		FormLayoutContainer buttonsCont = uifactory.addButtonsFormLayout("buttonsCont", null, formLayout);
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmCalendarDisableCrtl) {
			if (!DialogBoxUIFactory.isYesEvent(event) && !DialogBoxUIFactory.isOkEvent(event)) {
				managedCalendarEl.select(managedCalendarEl.getKey(0), true);
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		accessApiEl.clearError();
		if(accessApiEl.isVisible() && !accessApiEl.isOneSelected()) {
			accessApiEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		auditBodyMaxSizeEl.clearError();
		if(auditBodyMaxSizeEl.isVisible() && !auditBodyMaxSizeEl.validateIntValue()) {
			allOk &= false;
		}
		
		auditRetentionDaysEl.clearError();
		if(auditRetentionDaysEl.isVisible() && !auditRetentionDaysEl.validateIntValue()) {
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == enabledButton) {
			updateEnable();
		} else if (source == managedCalendarEl) {
			if (!managedCalendarEl.isAtLeastSelected(1)) {
				doConfirmCalendarDisabled(ureq);
			}
		} else if(source == auditEnabledEl || source == auditBodyEl) {
			updateAuditVisibility();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		boolean restEnabled = enabledButton.isOn();
		restModule.setEnabled(restEnabled);
		
		if(restEnabled) {
			restModule.setUserAllowedGenerateApiKey(generateApiKeyEl.isAtLeastSelected(1));
			
			if(accessApiEl.isVisible() && accessApiEl.isOneSelected()) {
				restModule.setApiAccess(ApiAccess.valueOf(accessApiEl.getSelectedKey()));
			}

			restModule.setAuditLogEnabled(auditEnabledEl.isOn());
			restModule.setAuditLogReads(auditReadsEl.isAtLeastSelected(1));
			restModule.setAuditLogBody(auditBodyEl.isAtLeastSelected(1));
			restModule.setAuditLogBodyMaxSize(auditBodyMaxSizeEl.getIntValue());
			restModule.setAuditLogRetentionDays(auditRetentionDaysEl.getIntValue());
			
		} else {
			restModule.setAuditLogEnabled(false);
		}
		
		repositoryModule.setManagedRepositoryEntries(managedRepoEl.isAtLeastSelected(1));
		calendarModule.setManagedCalendars(managedCalendarEl.isAtLeastSelected(1));
		groupModule.setManagedBusinessGroups(managedGroupsEl.isAtLeastSelected(1));
		securityModule.setRelationRoleManaged(managedRelationRole.isAtLeastSelected(1));
		userModule.setPortraitManaged(managedUserPortraitEl.isAtLeastSelected(1));
		lectureModule.setLecturesManaged(managedLecturesEl.isAtLeastSelected(1));
		curriculumModule.setCurriculumManaged(managedCurriculumEl.isAtLeastSelected(1));
		assessmentModule.setManagedAssessmentModes(managedAssessmentModeEl.isAtLeastSelected(1));
		certificateModule.setManagedCertificates(managedCertificatesEl.isAtLeastSelected(1));

		getWindowControl().setInfo(translate("info.saved"));
	}
	
	private void updateEnable() {
		boolean restEnabled = enabledButton.isOn();
	
		docLinkFlc.setVisible(restEnabled);
		generateApiKeyEl.setVisible(restEnabled);
		accessApiEl.setVisible(restEnabled);
		auditCont.setVisible(restEnabled);
		
		// Set default values by on and off
		accessApiEl.select(ApiAccess.apikey.name(), true);
		generateApiKeyEl.uncheckAll();
		
		if(restEnabled && !restModule.isAuditLogEnabled()) {
			// an open API is only auditable with the audit log switched on
			auditEnabledEl.toggleOn();
			updateAuditVisibility();
		}
	}
	
	private void updateAuditVisibility() {
		boolean on = auditEnabledEl.isOn();
		auditReadsEl.setVisible(on);
		auditBodyEl.setVisible(on);
		auditBodyMaxSizeEl.setVisible(on && auditBodyEl.isAtLeastSelected(1));
		auditRetentionDaysEl.setVisible(on);
	}
	
	private void doConfirmCalendarDisabled(UserRequest ureq) {
		String title = translate("confirm.calendar.disabled.title");
		String text = translate("confirm.calendar.disabled.text");
		confirmCalendarDisableCrtl = activateYesNoDialog(ureq, title, text, confirmCalendarDisableCrtl);
	}
}
