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
package org.olat.modules.lecture.ui;

import org.olat.admin.restapi.RestapiAdminController;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FormSubmit;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Roles;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.lecture.LectureModule;
import org.olat.modules.lecture.LectureService;
import org.olat.modules.lecture.RepositoryEntryLectureConfiguration;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.settings.ReloadSettingsEvent;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Configure a course / repository entry for lecture.
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LectureRepositorySettingsController extends FormBasicController {
	
	private static final String[] onKeys = new String[] { "on" };
	private static final String[] overrideKeys = new String[] { "yes", "no" };
	

	private FormLink overrideLink;
	private FormLink unOverrideLink;
	private FormSubmit saveButton;
	
	private SingleSelection overrideEl;
	private TextElement attendanceRateEl;
	private TextElement assessmentIpsEl;
	private TextElement assessmentLeadTimeEl;
	private TextElement assessmentFollowupTimeEl;
	private TextElement assessmentSafeExamBrowserEl;
	private MultipleSelectionElement enableEl;
	private MultipleSelectionElement rollCallEnabledEl;
	private MultipleSelectionElement calculateAttendanceRateEl;
	private MultipleSelectionElement teacherCalendarSyncEl;
	private MultipleSelectionElement courseCalendarSyncEl;
	private MultipleSelectionElement enableAssessmentModeEl;
	
	private RepositoryEntry entry;
	private final boolean readOnly;
	private boolean overrideManaged = false;
	private final boolean lectureConfigManaged;
	private boolean overrideModuleDefaults = false;
	private RepositoryEntryLectureConfiguration lectureConfig;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LectureModule lectureModule;
	@Autowired
	private LectureService lectureService;
	@Autowired
	private RepositoryService repositoryService;
	
	public LectureRepositorySettingsController(UserRequest ureq, WindowControl wControl, RepositoryEntry entry, boolean readOnly) {
		super(ureq, wControl, "repository_settings");
		
		this.entry = entry;
		this.readOnly = readOnly;
		lectureConfig = lectureService.getRepositoryEntryLectureConfiguration(entry);
		overrideModuleDefaults = lectureConfig.isOverrideModuleDefault();
		lectureConfigManaged = RepositoryEntryManagedFlag.isManaged(entry, RepositoryEntryManagedFlag.lectureconfig);
		
		initForm(ureq);
		updateOverride();
		updateVisibility();
	}
	
	public boolean isLectureEnabled() {
		return enableEl.isAtLeastSelected(1);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("lecture.course.admin.title");
		setFormContextHelp("Lectures and absences");
		formLayout.setElementCssClass("o_sel_repo_lecture_settings_form");
		if(lectureConfigManaged) {
			String flags = entry.getManagedFlagsString() == null ? "" : entry.getManagedFlagsString().trim();
			String flagsFormatted = null;
			if (flags.length() > 0) {
				// use translator from REST admin package to import managed flags context help strings
				Translator managedTrans = Util.createPackageTranslator(RestapiAdminController.class, getLocale(), getTranslator());
				StringBuilder flagList = new StringBuilder();
				flagList.append("<ul>");
				for (String flag : flags.split(",")) {
					flagList.append("<li>");
					flagList.append(managedTrans.translate("managed.flags.course." + flag));
					flagList.append("</li>");
				}
				flagList.append("</ul>");
				flagsFormatted = flagList.toString();
				
			} else {
				flagsFormatted = flags;
			}
			setFormWarning("form.managedflags.intro", new String[]{ flagsFormatted });
			
			if(isAllowedToOverrideManaged(ureq)) {
				overrideLink = uifactory.addFormLink("override.lecture", formLayout, Link.BUTTON);
				overrideLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
			
				unOverrideLink = uifactory.addFormLink("unoverride.lecture", formLayout, Link.BUTTON);
				unOverrideLink.setIconLeftCSS("o_icon o_icon-fw o_icon_refresh");
				unOverrideLink.setVisible(false);
			}
		}
		
		String[] onValues = new String[] { translate("on") };
		enableEl = uifactory.addCheckboxesHorizontal("lecture.admin.enabled", formLayout, onKeys, onValues);
		enableEl.setEnabled(!lectureConfigManaged && !readOnly);
		enableEl.setElementCssClass("o_sel_repo_lecture_enable");
		enableEl.addActionListener(FormEvent.ONCHANGE);
		if(lectureConfig.isLectureEnabled()) {
			enableEl.select(onKeys[0], true);
		}
		
		String[] overrideValues = new String[]{ translate("config.override.yes"), translate("config.override.no") };
		overrideEl = uifactory.addRadiosHorizontal("config.override", formLayout, overrideKeys, overrideValues);
		overrideEl.setEnabled(!lectureConfigManaged && !readOnly);
		overrideEl.setElementCssClass("o_sel_repo_lecture_override");
		overrideEl.addActionListener(FormEvent.ONCHANGE);
		if(lectureConfig.isOverrideModuleDefault()) {
			overrideEl.select(overrideKeys[0], true);//yes
		} else {
			overrideEl.select(overrideKeys[1], true);//no
		}
		if(!lectureModule.isCanOverrideStandardConfiguration() && !lectureConfig.isOverrideModuleDefault()) {
			overrideEl.setEnabled(false);
		}
		
		rollCallEnabledEl = uifactory.addCheckboxesHorizontal("config.rollcall.enabled", formLayout, onKeys, onValues);
		rollCallEnabledEl.addActionListener(FormEvent.ONCHANGE);
		
		calculateAttendanceRateEl = uifactory.addCheckboxesHorizontal("config.calculate.attendance.rate", formLayout, onKeys, onValues);
		attendanceRateEl = uifactory.addTextElement("lecture.attendance.rate.default", "lecture.attendance.rate.default", 4, "", formLayout);
		teacherCalendarSyncEl = uifactory.addCheckboxesHorizontal("config.sync.teacher.calendar", formLayout, onKeys, onValues);
		courseCalendarSyncEl = uifactory.addCheckboxesHorizontal("config.sync.course.calendar", formLayout, onKeys, onValues);
		
		// assessment mode
		enableAssessmentModeEl = uifactory.addCheckboxesHorizontal("lecture.assessment.mode.enabled", formLayout, onKeys, onValues);
		enableAssessmentModeEl.addActionListener(FormEvent.ONCHANGE);
		
		assessmentLeadTimeEl = uifactory.addTextElement("lecture.assessment.mode.leading.time", "lecture.assessment.mode.leading.time", 8, "", formLayout);
		assessmentFollowupTimeEl = uifactory.addTextElement("lecture.assessment.mode.followup.time", "lecture.assessment.mode.followup.time", 8, "", formLayout);
		assessmentIpsEl = uifactory.addTextElement("lecture.assessment.mode.ips", "lecture.assessment.mode.ips", 8, "", formLayout);
		assessmentSafeExamBrowserEl = uifactory.addTextElement("lecture.assessment.mode.seb", "lecture.assessment.mode.seb", 8, "", formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		saveButton = uifactory.addFormSubmitButton("save", buttonsCont);
		saveButton.setVisible((!lectureConfigManaged || overrideManaged) && !readOnly);
	}
	
	protected boolean isAllowedToOverrideManaged(UserRequest ureq) {
		if(entry != null) {
			Roles roles = ureq.getUserSession().getRoles();
			return roles.isAdministrator() && repositoryService.hasRoleExpanded(getIdentity(), entry,
					OrganisationRoles.administrator.name());
		}
		return false;
	}
	
	private void updateOverride() {
		updateOverrideElement(rollCallEnabledEl, lectureConfig.getRollCallEnabled(), lectureModule.isRollCallDefaultEnabled());
		updateOverrideElement(calculateAttendanceRateEl, lectureConfig.getCalculateAttendanceRate(), lectureModule.isRollCallCalculateAttendanceRateDefaultEnabled());
		updateOverrideElement(teacherCalendarSyncEl, lectureConfig.getTeacherCalendarSyncEnabled(), lectureModule.isTeacherCalendarSyncEnabledDefault());
		updateOverrideElement(courseCalendarSyncEl, lectureConfig.getCourseCalendarSyncEnabled(), lectureModule.isCourseCalendarSyncEnabledDefault());
	
		double attendanceRate;
		if(!overrideModuleDefaults || lectureConfig.getRequiredAttendanceRate() == null) {
			attendanceRate = lectureModule.getRequiredAttendanceRateDefault();
		} else {
			attendanceRate = lectureConfig.getRequiredAttendanceRate().doubleValue();
		}
		long attendanceRatePerCent = Math.round(attendanceRate * 100.0d);
		attendanceRateEl.setValue(Long.toString(attendanceRatePerCent));
		attendanceRateEl.setEnabled(overrideModuleDefaults && (!lectureConfigManaged || overrideManaged));

		updateOverrideElement(enableAssessmentModeEl, lectureConfig.getAssessmentModeEnabled(), lectureModule.isAssessmentModeEnabledDefault());
		updateOverrideElement(assessmentLeadTimeEl, lectureConfig.getAssessmentModeLeadTime(), lectureModule.getAssessmentModeLeadTime());
		updateOverrideElement(assessmentFollowupTimeEl, lectureConfig.getAssessmentModeFollowupTime(), lectureModule.getAssessmentModeFollowupTime());
		updateOverrideElement(assessmentIpsEl, lectureConfig.getAssessmentModeAdmissibleIps(), lectureModule.getAssessmentModeAdmissibleIps());
		updateOverrideElement(assessmentSafeExamBrowserEl, lectureConfig.getAssessmentModeSebKeys(), lectureModule.getAssessmentModeSebKeys());
	}
	
	private void updateOverrideElement(MultipleSelectionElement el, Boolean entryConfig, boolean defaultValue) {
		boolean enable = overrideModuleDefaults && entryConfig != null ? entryConfig.booleanValue() : defaultValue ;
		if(enable) {
			el.select(onKeys[0], true);
		} else {
			el.uncheckAll();
		}
		el.setEnabled(overrideModuleDefaults && overrideEl.isEnabled() && (!lectureConfigManaged || overrideManaged) && !readOnly);
	}
	
	private void updateOverrideElement(TextElement el, Integer entryConfig, int defaultValue) {
		int val = overrideModuleDefaults && entryConfig != null ? entryConfig.intValue() : defaultValue ;
		if(val > 0) {
			el.setValue(Integer.toString(val));
		} else {
			el.setValue("");
		}
		el.setEnabled(overrideModuleDefaults && overrideEl.isEnabled() && (!lectureConfigManaged || overrideManaged));
	}
	
	private void updateOverrideElement(TextElement el, String entryConfig, String defaultValue) {
		String val = overrideModuleDefaults && entryConfig != null ? entryConfig : defaultValue ;
		el.setValue(val);
		el.setEnabled(overrideModuleDefaults && overrideEl.isEnabled() && (!lectureConfigManaged || overrideManaged));
	}
	
	private void updateVisibility() {
		boolean lectureEnabled = enableEl.isAtLeastSelected(1);
		boolean rollCallEnabled = rollCallEnabledEl.isAtLeastSelected(1);
		overrideEl.setVisible(lectureEnabled);
		rollCallEnabledEl.setVisible(lectureEnabled);
		calculateAttendanceRateEl.setVisible(lectureEnabled && rollCallEnabled);
		attendanceRateEl.setVisible(lectureEnabled && rollCallEnabled);
		teacherCalendarSyncEl.setVisible(lectureEnabled);
		courseCalendarSyncEl.setVisible(lectureEnabled);
		enableAssessmentModeEl.setVisible(lectureEnabled);

		boolean assessmentModeEnabled = enableAssessmentModeEl.isVisible()
				&& enableAssessmentModeEl.isAtLeastSelected(1);
		assessmentIpsEl.setVisible(assessmentModeEnabled);
		assessmentLeadTimeEl.setVisible(assessmentModeEnabled);
		assessmentFollowupTimeEl.setVisible(assessmentModeEnabled);
		assessmentSafeExamBrowserEl.setVisible(assessmentModeEnabled);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(overrideEl == source) {
			if(overrideEl.isOneSelected()) {
				overrideModuleDefaults = overrideEl.isSelected(0);
				updateOverride();
				updateVisibility();
			}
		} else if(enableEl == source) {
			updateOverride();
			updateVisibility();
		} else if(rollCallEnabledEl == source || enableAssessmentModeEl == source) {
			updateVisibility();
		} else if (source == overrideLink) {
			doOverrideManagedResource();
		} else if (source == unOverrideLink) {
			doUnOverrideManagedResource();
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		overrideEl.clearError();
		if(!overrideEl.isOneSelected()) {
			overrideEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		if(assessmentFollowupTimeEl.isVisible() && assessmentFollowupTimeEl.isEnabled()) {
			allOk &= validateInteger(assessmentFollowupTimeEl);
		}
		if(assessmentLeadTimeEl.isVisible() && assessmentLeadTimeEl.isEnabled()) {
			allOk &= validateInteger(assessmentLeadTimeEl);
		}
		
		//override -> check rate
		attendanceRateEl.clearError();
		if(overrideEl.isSelected(0)) {
			if(rollCallEnabledEl.isAtLeastSelected(1) && calculateAttendanceRateEl.isAtLeastSelected(1)) {
				if(StringHelper.containsNonWhitespace(attendanceRateEl.getValue())) {
					try {
						long rateInPercent = Long.parseLong(attendanceRateEl.getValue());
						if(rateInPercent <= 0 || rateInPercent > 100) {
							attendanceRateEl.setErrorKey("error.integer.between", new String[] {"1", "100"});
							allOk &= false;
						}
					} catch(NumberFormatException e) {
						attendanceRateEl.setErrorKey("form.error.nointeger", null);
						allOk &= false;
					}
				} else {
					attendanceRateEl.setErrorKey("form.legende.mandatory", null);
					allOk &= false;
				}
			}
		}
		
		return allOk;
	}
	
	private boolean validateInteger(TextElement el) {
		boolean allOk = true;
		
		el.clearError();
		if(!StringHelper.containsNonWhitespace(el.getValue())) {
			el.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		} else {
			try {
				int value = Integer.parseInt(el.getValue());
				if(value < 0) {
					el.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				el.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		}
		
		return allOk;	
	}

	@Override
	protected void formOK(UserRequest ureq) {
		boolean enabled = enableEl.isAtLeastSelected(1);
		lectureConfig.setLectureEnabled(enabled);
		boolean override = overrideEl.isSelected(0);
		if(enabled && override) {
			lectureConfig.setOverrideModuleDefault(override);
			lectureConfig.setRollCallEnabled(rollCallEnabledEl.isAtLeastSelected(1));
			
			//reset values
			lectureConfig.setCalculateAttendanceRate(null);
			lectureConfig.setRequiredAttendanceRate(null);
			if(rollCallEnabledEl.isAtLeastSelected(1)) {
				lectureConfig.setCalculateAttendanceRate(calculateAttendanceRateEl.isAtLeastSelected(1));
				if(calculateAttendanceRateEl.isAtLeastSelected(1)) {
					try {
						long rateInPercent = Long.parseLong(attendanceRateEl.getValue());
						double rate = rateInPercent <= 0 ? 0.0d : rateInPercent / 100.0d;
						lectureConfig.setRequiredAttendanceRate(rate);
					} catch (NumberFormatException e) {
						logError("", e);
					}
				}
			}

			lectureConfig.setTeacherCalendarSyncEnabled(teacherCalendarSyncEl.isAtLeastSelected(1));
			lectureConfig.setCourseCalendarSyncEnabled(courseCalendarSyncEl.isAtLeastSelected(1));
		} else {
			lectureConfig.setOverrideModuleDefault(false);
			lectureConfig.setRollCallEnabled(null);
			lectureConfig.setCalculateAttendanceRate(null);
			lectureConfig.setRequiredAttendanceRate(null);
			lectureConfig.setTeacherCalendarSyncEnabled(null);
			lectureConfig.setCourseCalendarSyncEnabled(null);
		}
		
		boolean assessmentModeEnabled = enableAssessmentModeEl.isAtLeastSelected(1) && enabled && override;
		if(assessmentModeEnabled) {
			lectureConfig.setAssessmentModeEnabled(Boolean.TRUE);
			lectureConfig.setAssessmentModeFollowupTime(Integer.parseInt(assessmentFollowupTimeEl.getValue()));
			lectureConfig.setAssessmentModeLeadTime(Integer.parseInt(assessmentLeadTimeEl.getValue()));
			lectureConfig.setAssessmentModeAdmissibleIps(assessmentIpsEl.getValue());
			lectureConfig.setAssessmentModeSebKeys(assessmentSafeExamBrowserEl.getValue());
		} else {
			if(enabled && override) {
				lectureConfig.setAssessmentModeEnabled(Boolean.FALSE);
			} else {
				lectureConfig.setAssessmentModeEnabled(null);
			}
			lectureConfig.setAssessmentModeFollowupTime(null);
			lectureConfig.setAssessmentModeLeadTime(null);
			lectureConfig.setAssessmentModeAdmissibleIps(null);
			lectureConfig.setAssessmentModeSebKeys(null);
		}
		
		lectureConfig = lectureService.updateRepositoryEntryLectureConfiguration(lectureConfig);
		dbInstance.commit();
		lectureService.syncCalendars(entry);
		fireEvent(ureq, new ReloadSettingsEvent(false, false, true, false));
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	private void doOverrideManagedResource() {
		overrideManagedResource(true);
	}
	
	private void doUnOverrideManagedResource() {
		overrideManagedResource(false);
	}
	
	private void overrideManagedResource(boolean override) {
		overrideManaged = override;
		
		overrideLink.setVisible(!overrideManaged);
		unOverrideLink.setVisible(overrideManaged);
		
		saveButton.setVisible(overrideManaged);
		enableEl.setEnabled(overrideManaged);
		overrideEl.setEnabled(overrideManaged);
		updateOverride();
	}
}