/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.nodes.topicbroker.ui;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.Date;
import java.util.List;

import org.hibernate.LazyInitializationException;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.SpacerElement;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.duedate.DueDateConfig;
import org.olat.course.duedate.DueDateService;
import org.olat.course.duedate.ui.DueDateConfigFormItem;
import org.olat.course.duedate.ui.DueDateConfigFormatter;
import org.olat.course.editor.NodeEditController;
import org.olat.course.nodes.TopicBrokerCourseNode;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.topicbroker.ui.TBUIFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryLifecycleDAO;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 May 2024<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class TBConfigController extends FormBasicController implements Controller {
	
	private static final List<String> RELATIVE_TO_DATES = List.of(DueDateService.TYPE_COURSE_START);

	private TextElement enrollmentsPerIdentityEl;
	private FormLayoutContainer selectionsPerIdentityCont;
	private TextElement selectionsPerIdentityEl;
	private MultipleSelectionElement participantCanEl;
	private FormToggle relativeDatesEl;
	private DueDateConfigFormItem selectionPeriodEl;
	private FormLayoutContainer selectionPeriodDurationCont;
	private TextElement selectionPeriodDurationEl;
	private DateChooser selectionPeriodAbsEl;
	private SingleSelection enrollmentAutoEl;
	private SpacerElement withdrawEndSpacer;
	private FormLayoutContainer withdrawEndRelCont;
	private TextElement withdrawEndRelativeEl;
	private DateChooser withdrawEndAbsEl;

	private final RepositoryEntry repositoryEntry;
	private final TopicBrokerCourseNode courseNode;
	private final ModuleConfiguration moduleConfig;
	
	@Autowired
	private RepositoryEntryLifecycleDAO repositoryEntryLifecycleDao;

	protected TBConfigController(UserRequest ureq, WindowControl wControl, Form mainForm,
			RepositoryEntry repositoryEntry, TopicBrokerCourseNode courseNode) {
		super(ureq, wControl, LAYOUT_VERTICAL, null, mainForm);
		setTranslator(Util.createPackageTranslator(TBUIFactory.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(DueDateConfigFormItem.class, getLocale(), getTranslator()));
		this.repositoryEntry = repositoryEntry;
		this.courseNode = courseNode;
		moduleConfig = courseNode.getModuleConfiguration();
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FormLayoutContainer enrollmentCont = FormLayoutContainer.createDefaultFormLayout("enrollment", getTranslator());
		enrollmentCont.setRootForm(mainForm);
		enrollmentCont.setFormTitle(translate("config.enrollment.title"));
		formLayout.add(enrollmentCont);
		
		uifactory.addStaticTextElement("config.enrollment.method", translate("strategy.fair"), enrollmentCont);
		
		String enrollmentsPerIdentity = moduleConfig.getStringValue(TopicBrokerCourseNode.CONFIG_KEY_ENROLLMENTS_PER_PARTICIPANT);
		enrollmentsPerIdentityEl = uifactory.addTextElement("config.enrollments.per.participant", 10, enrollmentsPerIdentity, enrollmentCont);
		enrollmentsPerIdentityEl.setMandatory(true);
		
		selectionsPerIdentityCont = FormLayoutContainer.createCustomFormLayout("selectionsCont", getTranslator(), velocity_root + "/item_suffix.html");
		selectionsPerIdentityCont.setLabel("config.selections.per.participant", null);
		selectionsPerIdentityCont.setMandatory(false);
		selectionsPerIdentityCont.setRootForm(mainForm);
		enrollmentCont.add(selectionsPerIdentityCont);
		
		String selectionsPerIdentity = moduleConfig.getStringValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTIONS_PER_PARTICIPANT);
		selectionsPerIdentityEl = uifactory.addTextElement("config.selections.per.participant", 10, selectionsPerIdentity, selectionsPerIdentityCont);
		selectionsPerIdentityEl.setMandatory(true);
		selectionsPerIdentityEl.setDisplaySize(15);
		selectionsPerIdentityCont.contextPut("itemName", selectionsPerIdentityEl.getName());
		selectionsPerIdentityCont.contextPut("suffix", translate("config.selections.per.participant.max"));
		
		SelectionValues participantCanSV = new SelectionValues();
		participantCanSV.add(entry(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_REDUCE_ENROLLMENTS, translate("config.participant.can.reduce.enrollments")));
		participantCanSV.add(entry(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_WITHDRAW, translate("config.participant.can.withdraw")));
		participantCanEl = uifactory.addCheckboxesVertical("config.participant.can", enrollmentCont, participantCanSV.keys(), participantCanSV.values(), 1);
		participantCanEl.addActionListener(FormEvent.ONCHANGE);
		participantCanEl.select(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_REDUCE_ENROLLMENTS, moduleConfig.getBooleanSafe(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_REDUCE_ENROLLMENTS));
		participantCanEl.select(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_WITHDRAW, moduleConfig.getBooleanSafe(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_WITHDRAW));
	
		FormLayoutContainer periodCont = FormLayoutContainer.createDefaultFormLayout("period", getTranslator());
		periodCont.setRootForm(mainForm);
		periodCont.setFormTitle(translate("config.selection.period.title"));
		formLayout.add(periodCont);
		
		relativeDatesEl = uifactory.addToggleButton("relative.dates","relative.dates", translate("on"), translate("off"), periodCont);
		relativeDatesEl.addActionListener(FormEvent.ONCHANGE);
		relativeDatesEl.toggle(moduleConfig.getBooleanSafe(TopicBrokerCourseNode.CONFIG_KEY_RELATIVE_DATES));
		
		DueDateConfig selectionPeriodStartConfig = courseNode.getDueDateConfig(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_START);
		RepositoryEntryLifecycle lifecycle = getRepositoryEntryLifecycle(repositoryEntry);
		if ((lifecycle != null && lifecycle.getValidFrom() != null) || relativeDatesEl.isOn()) {
			SelectionValues relativeToDatesKV = new SelectionValues();
			DueDateConfigFormatter.create(getLocale()).addCourseRelativeToDateTypes(relativeToDatesKV, RELATIVE_TO_DATES);
			selectionPeriodEl = DueDateConfigFormItem.create("config.selection.period", relativeToDatesKV,
					relativeDatesEl.isOn(), selectionPeriodStartConfig);
			selectionPeriodEl.setLabel("config.selection.period", null);
			selectionPeriodEl.setRelative(true);
			selectionPeriodEl.setMandatory(true);
			periodCont.add(selectionPeriodEl);
			
			selectionPeriodDurationCont = FormLayoutContainer.createCustomFormLayout("durationCont", getTranslator(), velocity_root + "/item_suffix.html");
			selectionPeriodDurationCont.setLabel("config.selection.period.duration", null);
			selectionPeriodDurationCont.setMandatory(false);
			selectionPeriodDurationCont.setRootForm(mainForm);
			periodCont.add(selectionPeriodDurationCont);
			
			String selectionPeriodDuration = moduleConfig.getStringValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_DURATION);
			selectionPeriodDurationEl = uifactory.addTextElement("config.selection.period.duration", 10, selectionPeriodDuration, selectionPeriodDurationCont);
			selectionPeriodDurationEl.setMandatory(true);
			selectionPeriodDurationEl.setDisplaySize(15);
			selectionPeriodDurationCont.contextPut("itemName", selectionPeriodDurationEl.getName());
			selectionPeriodDurationCont.contextPut("suffix", translate("config.selection.period.duration.days"));
		} else {
			relativeDatesEl.setVisible(false);
		}
		
		Date selectionPeriodStart = DueDateConfig.isAbsolute(selectionPeriodStartConfig)? selectionPeriodStartConfig.getAbsoluteDate(): null;
		Date selectionPeriodEnd = moduleConfig.getDateValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_END);
		selectionPeriodAbsEl = uifactory.addDateChooser("config.selection.period.abs", "config.selection.period", selectionPeriodStart, periodCont);
		selectionPeriodAbsEl.setDateChooserTimeEnabled(true);
		selectionPeriodAbsEl.setSecondDate(true);
		selectionPeriodAbsEl.setSeparator("to.separator");
		selectionPeriodAbsEl.setMandatory(true);
		if (selectionPeriodStart != null) {
			selectionPeriodAbsEl.setSecondDate(selectionPeriodEnd);
		}
		
		SelectionValues enrollmentSV = new SelectionValues();
		enrollmentSV.add(SelectionValues.entry(Boolean.TRUE.toString(), translate("config.enrollment.auto.auto")));
		enrollmentSV.add(SelectionValues.entry(Boolean.FALSE.toString(), translate("config.enrollment.auto.manually")));
		enrollmentAutoEl = uifactory.addRadiosHorizontal("config.enrollment.auto", periodCont, enrollmentSV.keys(), enrollmentSV.values());
		enrollmentAutoEl.select(String.valueOf(moduleConfig.getBooleanSafe(TopicBrokerCourseNode.CONFIG_KEY_ENROLLMENT_AUTO)), true);
		
		withdrawEndSpacer = uifactory.addSpacerElement("withdraw.spacer", periodCont, false);
		
		withdrawEndRelCont = FormLayoutContainer.createCustomFormLayout("withdrawEndRel", getTranslator(), velocity_root + "/item_suffix.html");
		withdrawEndRelCont.setLabel("config.withdraw.end", null);
		withdrawEndRelCont.setVisible(false);
		withdrawEndRelCont.setRootForm(mainForm);
		periodCont.add(withdrawEndRelCont);
		
		String withdrawEndRelative = moduleConfig.getStringValue(TopicBrokerCourseNode.CONFIG_KEY_WITHDRAW_END_RELATIVE);
		withdrawEndRelativeEl = uifactory.addTextElement("config.withdraw.end", 10, withdrawEndRelative, withdrawEndRelCont);
		withdrawEndRelativeEl.setDisplaySize(15);
		withdrawEndRelativeEl.setVisible(false);
		withdrawEndRelCont.contextPut("itemName", withdrawEndRelativeEl.getName());
		withdrawEndRelCont.contextPut("suffix", translate("config.withdraw.end.after.selection.period"));
		
		Date withdrawEndAbs = moduleConfig.getDateValue(TopicBrokerCourseNode.CONFIG_KEY_WITHDRAW_END);
		withdrawEndAbsEl = uifactory.addDateChooser("config.withdraw.end.abs", "config.withdraw.end", withdrawEndAbs, periodCont);
		
		updateSelectionPeriodUI();
	}

	private void updateSelectionPeriodUI() {
		boolean relativDates = relativeDatesEl.isOn();
			
		if (selectionPeriodEl != null) {
			selectionPeriodEl.setVisible(relativDates);
			selectionPeriodDurationCont.setVisible(relativDates);
			selectionPeriodDurationEl.setVisible(relativDates);
			selectionPeriodAbsEl.setVisible(!relativDates);
		}
		
		boolean canWithdraw = participantCanEl.getSelectedKeys().contains(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_WITHDRAW);
		withdrawEndSpacer.setVisible(canWithdraw);
		withdrawEndRelCont.setVisible(canWithdraw && relativDates);
		withdrawEndRelativeEl.setVisible(canWithdraw && relativDates);
		withdrawEndAbsEl.setVisible(canWithdraw && !relativDates);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == participantCanEl) {
			updateSelectionPeriodUI();
		} else if (source == relativeDatesEl) {
			updateSelectionPeriodUI();
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		enrollmentsPerIdentityEl.clearError();
		if (StringHelper.containsNonWhitespace(enrollmentsPerIdentityEl.getValue())) {
			try {
				int meetingDeletionDays = Integer.parseInt(enrollmentsPerIdentityEl.getValue());
				if (meetingDeletionDays < 0) {
					enrollmentsPerIdentityEl.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				enrollmentsPerIdentityEl.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		} else {
			enrollmentsPerIdentityEl.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		selectionsPerIdentityCont.clearError();
		if (StringHelper.containsNonWhitespace(selectionsPerIdentityEl.getValue())) {
			try {
				int meetingDeletionDays = Integer.parseInt(selectionsPerIdentityEl.getValue());
				if (meetingDeletionDays < 0) {
					selectionsPerIdentityCont.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			} catch (NumberFormatException e) {
				selectionsPerIdentityCont.setErrorKey("form.error.positive.integer");
				allOk &= false;
			}
		} else {
			selectionsPerIdentityCont.setErrorKey("form.legende.mandatory");
			allOk &= false;
		}
		
		if (selectionPeriodEl != null) {
			allOk &= selectionPeriodEl.validate();
		}
		
		if (selectionPeriodDurationEl != null) {
			selectionPeriodDurationCont.clearError();
			if (selectionPeriodDurationEl.isVisible()) {
				if (StringHelper.containsNonWhitespace(selectionPeriodDurationEl.getValue())) {
					try {
						int selectionPeriodDays = Integer.parseInt(selectionPeriodDurationEl.getValue());
						if (selectionPeriodDays < 0) {
							selectionPeriodDurationCont.setErrorKey("form.error.positive.integer");
							allOk &= false;
						}
					} catch (NumberFormatException e) {
						selectionPeriodDurationCont.setErrorKey("form.error.positive.integer");
						allOk &= false;
					}
				} else {
					selectionPeriodDurationCont.setErrorKey("form.legende.mandatory");
					allOk &= false;
				}
			}
		}
		
		selectionPeriodAbsEl.clearError();
		if (selectionPeriodAbsEl.isVisible()) {
			if (selectionPeriodAbsEl.getDate() == null || selectionPeriodAbsEl.getSecondDate() == null) {
				selectionPeriodAbsEl.setErrorKey("form.legende.mandatory");
				allOk &= false;
			} else if (selectionPeriodAbsEl.getDate().after(selectionPeriodAbsEl.getSecondDate())) {
				selectionPeriodAbsEl.setErrorKey("error.first.date.after.second.date");
				allOk &= false;
			}
		}
		
		withdrawEndRelCont.clearError();
		if (withdrawEndRelativeEl.isVisible()) {
			if (StringHelper.containsNonWhitespace(withdrawEndRelativeEl.getValue())) {
				try {
					int withdrawEndRelative = Integer.parseInt(withdrawEndRelativeEl.getValue());
					if (withdrawEndRelative < 0) {
						withdrawEndRelCont.setErrorKey("form.error.positive.integer");
						allOk &= false;
					}
				} catch (NumberFormatException e) {
					withdrawEndRelCont.setErrorKey("form.error.positive.integer");
					allOk &= false;
				}
			}
		}
		
		withdrawEndAbsEl.clearError();
		if (withdrawEndAbsEl.isVisible()) {
			if (selectionPeriodAbsEl.getSecondDate() != null && withdrawEndAbsEl.getDate() != null
					&& withdrawEndAbsEl.getDate().before(selectionPeriodAbsEl.getSecondDate())) {
				withdrawEndAbsEl.setErrorKey("error.withdraw.end.before.selection.end");
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		moduleConfig.setStringValue(TopicBrokerCourseNode.CONFIG_KEY_ENROLLMENTS_PER_PARTICIPANT, enrollmentsPerIdentityEl.getValue());
		moduleConfig.setStringValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTIONS_PER_PARTICIPANT, selectionsPerIdentityEl.getValue());
		moduleConfig.setBooleanEntry(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_REDUCE_ENROLLMENTS, participantCanEl.isKeySelected(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_REDUCE_ENROLLMENTS));
		moduleConfig.setBooleanEntry(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_WITHDRAW, participantCanEl.isKeySelected(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_WITHDRAW));
		
		boolean relativDates = relativeDatesEl.isVisible() && relativeDatesEl.isOn();
		moduleConfig.setBooleanEntry(TopicBrokerCourseNode.CONFIG_KEY_RELATIVE_DATES, relativDates);
		
		DueDateConfig selectionPeriodDueDateConfig;
		if (relativDates) {
			selectionPeriodDueDateConfig = selectionPeriodEl.getDueDateConfig();
			
			moduleConfig.setDateValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_END, null);
			moduleConfig.setStringValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_DURATION, selectionPeriodDurationEl.getValue());
			
			moduleConfig.setStringValue(TopicBrokerCourseNode.CONFIG_KEY_WITHDRAW_END_RELATIVE, withdrawEndRelativeEl.getValue());
			moduleConfig.remove(TopicBrokerCourseNode.CONFIG_KEY_WITHDRAW_END);
		} else {
			selectionPeriodDueDateConfig = DueDateConfig.absolute(selectionPeriodAbsEl.getDate());
			
			moduleConfig.setDateValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_END, selectionPeriodAbsEl.getSecondDate());
			moduleConfig.remove(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_DURATION);
			
			moduleConfig.remove(TopicBrokerCourseNode.CONFIG_KEY_WITHDRAW_END_RELATIVE);
			moduleConfig.setDateValue(TopicBrokerCourseNode.CONFIG_KEY_WITHDRAW_END, withdrawEndAbsEl.getDate());
		}
		moduleConfig.setIntValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_START_RELATIVE, selectionPeriodDueDateConfig.getNumOfDays());
		moduleConfig.setStringValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_START_RELATIVE_TO, selectionPeriodDueDateConfig.getRelativeToType());
		moduleConfig.setDateValue(TopicBrokerCourseNode.CONFIG_KEY_SELECTION_START, selectionPeriodDueDateConfig.getAbsoluteDate());
		
		moduleConfig.setBooleanEntry(TopicBrokerCourseNode.CONFIG_KEY_ENROLLMENT_AUTO, Boolean.valueOf(enrollmentAutoEl.getSelectedKey()));
		
		// clean up
		if (!moduleConfig.getBooleanSafe(TopicBrokerCourseNode.CONFIG_KEY_PARTICIPANT_CAN_WITHDRAW)) {
			moduleConfig.remove(TopicBrokerCourseNode.CONFIG_KEY_WITHDRAW_END_RELATIVE);
			moduleConfig.remove(TopicBrokerCourseNode.CONFIG_KEY_WITHDRAW_END);
		}
		
		// The configuration is now ready for the validation in the course node
		fireEvent(ureq, NodeEditController.NODECONFIG_CHANGED_EVENT);
	}
	
	private RepositoryEntryLifecycle getRepositoryEntryLifecycle(RepositoryEntry re) {
		try {
			RepositoryEntryLifecycle lifecycle = re.getLifecycle();
			if(lifecycle != null) {
				lifecycle.getValidTo();
			}
			return lifecycle;
		} catch (LazyInitializationException e) {
			return repositoryEntryLifecycleDao.loadByEntry(re);
		}
	}

}
