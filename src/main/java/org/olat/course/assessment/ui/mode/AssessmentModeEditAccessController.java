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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;
import java.util.stream.Collectors;

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
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeToArea;
import org.olat.course.assessment.AssessmentModeToCurriculumElement;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.course.assessment.model.AssessmentModeManagedFlag;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.CurriculumElementSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeEditAccessController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };

	private SingleSelection targetEl;
	private FormLink chooseAreasButton;
	private FormLink chooseGroupsButton;
	private FormLink chooseCurriculumElementsButton;
	private TextElement ipListEl;
	private FormLayoutContainer chooseGroupsCont;
	private MultipleSelectionElement ipsEl;
	private MultipleSelectionElement forCoachEl;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmCtrl;
	private AreaSelectionController areaChooseCtrl;
	private GroupSelectionController groupChooseCtrl;
	private CurriculumElementSelectionController curriculumElementChooseCtrl;
	
	private List<Long> areaKeys;
	private List<String> areaNames;
	private List<Long> groupKeys;
	private List<String> groupNames;
	private List<Long> curriculumElementKeys;
	private List<String> curriculumElementNames;
	
	private RepositoryEntry entry;
	private AssessmentMode assessmentMode;
	private final OLATResourceable courseOres;
	
	@Autowired
	private BGAreaManager areaMgr;
	@Autowired
	private CurriculumModule curriculumModule;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private BusinessGroupService businessGroupService;
	@Autowired
	private AssessmentModeCoordinationService modeCoordinationService;
	
	public AssessmentModeEditAccessController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry entry, AssessmentMode assessmentMode) {
		super(ureq, wControl);
		this.entry = entry;
		courseOres = OresHelper.clone(entry.getOlatResource());
		if(assessmentMode.getKey() == null) {
			this.assessmentMode = assessmentMode;
		} else {
			this.assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}
		initForm(ureq);
	}
	
	public AssessmentMode getAssessmentMode() {
		return assessmentMode;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_assessment_mode_edit_form");
		setFormContextHelp("manual_user/e-assessment/Assessment_mode/");
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		if(StringHelper.containsNonWhitespace(assessmentMode.getStartElement())) {
			CourseNode startElement = course.getRunStructure().getNode(assessmentMode.getStartElement());
			if(startElement == null) {
				setFormWarning("warning.missing.start.element");
			}
		}
		
		if(StringHelper.containsNonWhitespace(assessmentMode.getElementList())) {
			String elements = assessmentMode.getElementList();
			for(String element:elements.split(",")) {
				CourseNode node = course.getRunStructure().getNode(element);
				if(node == null) {
					setFormWarning("warning.missing.element");
				}
			}
		}
		
		Status status = assessmentMode.getStatus();
		
		//ips
		ipsEl = uifactory.addCheckboxesHorizontal("ips", "mode.ips", formLayout, onKeys, onValues);
		ipsEl.select(onKeys[0], assessmentMode.isRestrictAccessIps());
		ipsEl.addActionListener(FormEvent.ONCHANGE);
		ipsEl.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.ips));
		String ipList = assessmentMode.getIpList();
		ipListEl = uifactory.addTextAreaElement("mode.ips.list", "mode.ips.list", 16000, 4, 60, false, false, ipList, formLayout);
		ipListEl.setMaxLength(16000);
		ipListEl.setVisible(assessmentMode.isRestrictAccessIps());
		ipListEl.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.ips));
		
		SelectionValues targetKeyValues = new SelectionValues();
		boolean curriculumEnabled = curriculumModule.isEnabled();
		targetKeyValues.add(SelectionValues.entry(AssessmentMode.Target.course.name(), translate("target.course")));
		targetKeyValues.add(SelectionValues.entry(AssessmentMode.Target.groups.name(), translate("target.groups")));
		if(curriculumEnabled) {
			targetKeyValues.add(SelectionValues.entry(AssessmentMode.Target.curriculumEls.name(), translate("target.curriculumElements")));
		}
		String allLabel = curriculumEnabled ? translate("target.courseGroupsAndCurriculums") : translate("target.courseAndGroups");
		targetKeyValues.add(SelectionValues.entry(AssessmentMode.Target.courseAndGroups.name(), allLabel));
		targetEl = uifactory.addRadiosVertical("audience", "mode.target", formLayout, targetKeyValues.keys(), targetKeyValues.values());
		targetEl.setElementCssClass("o_sel_assessment_mode_audience");
		targetEl.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.participants));
		Target target = assessmentMode.getTargetAudience();
		if(target != null) {
			for(String audienceKey:targetKeyValues.keys()) {
				if(audienceKey.equals(target.name())) {
					targetEl.select(audienceKey, true);
				}
			}
		}
		if(!targetEl.isOneSelected()) {
			targetEl.select(targetKeyValues.keys()[0], true);
		}
		//choose groups / curriculum
		String groupPage = velocity_root + "/choose_groups.html";
		chooseGroupsCont = FormLayoutContainer.createCustomFormLayout("chooseGroups", getTranslator(), groupPage);
		chooseGroupsCont.setRootForm(mainForm);
		formLayout.add(chooseGroupsCont);
		
		chooseGroupsButton = uifactory.addFormLink("choose.groups", chooseGroupsCont, Link.BUTTON);
		chooseGroupsButton.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.participants));
		chooseAreasButton = uifactory.addFormLink("choose.areas", chooseGroupsCont, Link.BUTTON);
		chooseAreasButton.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.participants));
		chooseCurriculumElementsButton = uifactory.addFormLink("choose.curriculum.elements", chooseGroupsCont, Link.BUTTON);
		chooseCurriculumElementsButton.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.participants));
		chooseCurriculumElementsButton.setVisible(curriculumEnabled);

		selectAssessmentModeToBusinessGroups(assessmentMode.getGroups());
		selectAssessmentModeToAreas(assessmentMode.getAreas());
		selectAssessmentModeToCurriculumElements(assessmentMode.getCurriculumElements());

		forCoachEl = uifactory.addCheckboxesHorizontal("forcoach", "mode.for.coach", formLayout, onKeys, onValues);
		forCoachEl.select(onKeys[0], assessmentMode.isApplySettingsForCoach());
		forCoachEl.setEnabled(status != Status.end
				&& !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.coaches));
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("button", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		if(status != Status.end && !AssessmentModeManagedFlag.isManaged(assessmentMode, AssessmentModeManagedFlag.access)) {
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}
	
	protected void selectAssessmentModeToBusinessGroups(Set<AssessmentModeToGroup> assessmentModeToGroups) {
		Set<BusinessGroup> businessGroups = assessmentModeToGroups.stream()
				.map(AssessmentModeToGroup::getBusinessGroup)
				.collect(Collectors.toSet());
		selectBusinessGroups(businessGroups);
	}
	
	protected void selectBusinessGroups(Set<BusinessGroup> assessmentModeGroups) {
		groupKeys = new ArrayList<>();
		groupNames = new ArrayList<>();
		for(BusinessGroup group:assessmentModeGroups) {
			groupKeys.add(group.getKey());
			groupNames.add(StringHelper.escapeHtml(group.getName()));
		}
		chooseGroupsCont.getFormItemComponent().contextPut("groupNames", groupNames);
	}
	
	protected void selectAssessmentModeToAreas(Set<AssessmentModeToArea> assessmentModeToAreas) {
		Set<BGArea> areas = assessmentModeToAreas.stream()
				.map(AssessmentModeToArea::getArea)
				.collect(Collectors.toSet());
		selectAreas(areas);
	}
	
	protected void selectAreas(Set<BGArea> assessmentModeAreas) {
		areaKeys = new ArrayList<>();
		areaNames = new ArrayList<>();
		for(BGArea area: assessmentModeAreas) {
			areaKeys.add(area.getKey());
			areaNames.add(StringHelper.escapeHtml(area.getName()));
		}
		chooseGroupsCont.getFormItemComponent().contextPut("areaNames", areaNames);
	}
	
	protected void selectAssessmentModeToCurriculumElements(Set<AssessmentModeToCurriculumElement> assessmentModeToCurriculumElements) {
		Set<CurriculumElement> curriculumElements = assessmentModeToCurriculumElements.stream()
				.map(AssessmentModeToCurriculumElement::getCurriculumElement)
				.collect(Collectors.toSet());
		selectCurriculumElements(curriculumElements);
	}
	
	protected void selectCurriculumElements(Set<CurriculumElement> assessmentModeCurriculumElements) {
		curriculumElementKeys = new ArrayList<>();
		curriculumElementNames = new ArrayList<>();
		for(CurriculumElement element: assessmentModeCurriculumElements) {
			curriculumElementKeys.add(element.getKey());
			curriculumElementNames.add(StringHelper.escapeHtml(element.getDisplayName()));
		}
		chooseGroupsCont.getFormItemComponent().contextPut("curriculumElementNames", curriculumElementNames);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(groupChooseCtrl == source) {
			if(Event.DONE_EVENT == event || Event.CHANGED_EVENT == event) {
				groupKeys = groupChooseCtrl.getSelectedKeys();
				List<String> newGroupNames = groupChooseCtrl.getSelectedNames();
				groupNames.clear();
				for(String newGroupName:newGroupNames) {
					groupNames.add(StringHelper.escapeHtml(newGroupName));
				}
				chooseGroupsCont.getFormItemComponent().contextPut("groupNames", groupNames);
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(areaChooseCtrl == source) {
			if(Event.DONE_EVENT == event || Event.CHANGED_EVENT == event) {
				areaKeys = areaChooseCtrl.getSelectedKeys();
				List<String> newAreaNames = areaChooseCtrl.getSelectedNames();
				areaNames.clear();
				for(String newAreaName:newAreaNames) {
					areaNames.add(StringHelper.escapeHtml(newAreaName));
				}
				chooseGroupsCont.getFormItemComponent().contextPut("areaNames", areaNames);
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(curriculumElementChooseCtrl == source) {
			if(Event.DONE_EVENT == event || Event.CHANGED_EVENT == event) {
				curriculumElementKeys = curriculumElementChooseCtrl.getSelectedKeys();
				List<String> newCurriculumElementNames = curriculumElementChooseCtrl.getSelectedNames();
				curriculumElementNames.clear();
				for(String newCurriculumElementName:newCurriculumElementNames) {
					curriculumElementNames.add(StringHelper.escapeHtml(newCurriculumElementName));
				}
				chooseGroupsCont.getFormItemComponent().contextPut("curriculumElementNames", curriculumElementNames);
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				save(ureq, true);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(curriculumElementChooseCtrl);
		removeAsListenerAndDispose(groupChooseCtrl);
		removeAsListenerAndDispose(areaChooseCtrl);
		removeAsListenerAndDispose(cmc);
		curriculumElementChooseCtrl = null;
		groupChooseCtrl = null;
		areaChooseCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		targetEl.clearError();
		if(targetEl.isOneSelected()) {
			Target target = AssessmentMode.Target.valueOf(targetEl.getSelectedKey());
			if(target == Target.courseAndGroups ) {
				if(groupKeys.isEmpty() && areaKeys.isEmpty() && curriculumElementKeys.isEmpty()) {
					targetEl.setErrorKey("error.group.missing", null);
					allOk &= false;
				}	
			} else if(target == Target.groups) {
				if(groupKeys.isEmpty() && areaKeys.isEmpty()) {
					targetEl.setErrorKey("error.group.missing", null);
					allOk &= false;
				}
			} else if(target == Target.curriculumEls) {
				if(curriculumElementKeys.isEmpty()) {
					targetEl.setErrorKey("error.curriculum.missing", null);
					allOk &= false;
				}
			}
		} else {
			targetEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		ipListEl.clearError();
		if(ipsEl.isAtLeastSelected(1)) {
			String value = ipListEl.getValue();
			if(!StringHelper.containsNonWhitespace(value)) {
				ipListEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(value.length() > ipListEl.getMaxLength()) {
				ipListEl.setErrorKey("form.error.toolong", new String[] { Integer.toString(ipListEl.getMaxLength()) } );
				allOk &= false;
			} else {
				allOk &= validIpList(ipListEl.getValue());
			}
		}
		return allOk;
	}
	
	/**
	 * Try to begin some validation of the list but the list allowed
	 * a lot of possibilities.
	 * 
	 * @param ipList The list of IPs
	 * @return true if valid
	 */
	private boolean validIpList(String ipList) {
		boolean allOk = true;
		
		for(StringTokenizer tokenizer = new StringTokenizer(ipList, "\n\r", false); tokenizer.hasMoreTokens(); ) {
			String ipRange = tokenizer.nextToken();
			if(StringHelper.containsNonWhitespace(ipRange) && ipRange.startsWith("/")) {
				ipListEl.setErrorKey("error.ip.range.cannot.start.slash", new String[] { ipRange } );
				allOk &= false;
			}
		}
		
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(assessmentMode.getKey() != null) {
			assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}
		
		Status currentStatus = assessmentMode.getStatus();
		if(assessmentMode.isManualBeginEnd()) {
			//manual start don't change the status of the assessment
			save(ureq, false);
		} else {
			Status nextStatus = modeCoordinationService.evaluateStatus(assessmentMode.getBegin(), assessmentMode.getLeadTime(),
					assessmentMode.getEnd(), assessmentMode.getFollowupTime());
			if(currentStatus == nextStatus) {
				save(ureq, true);
			} else {
				String title = translate("confirm.status.change.title");
	
				String text;
				switch(nextStatus) {
					case none: text = translate("confirm.status.change.none"); break;
					case leadtime: text = translate("confirm.status.change.leadtime"); break;
					case assessment: text = translate("confirm.status.change.assessment"); break;
					case followup: text = translate("confirm.status.change.followup"); break;
					case end: text = translate("confirm.status.change.end"); break;
					default: text = "ERROR";
				}
				confirmCtrl = activateOkCancelDialog(ureq, title, text, confirmCtrl);
			}
		}
	}
	
	private void save(UserRequest ureq, boolean forceStatus) {
		if(assessmentMode.getKey() != null) {
			assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}

		String targetKey = targetEl.getSelectedKey();
		Target target = AssessmentMode.Target.valueOf(targetKey);
		assessmentMode.setTargetAudience(target);
		
		boolean ipRestriction = ipsEl.isAtLeastSelected(1);
		assessmentMode.setRestrictAccessIps(ipRestriction);
		if(ipRestriction) {
			assessmentMode.setIpList(ipListEl.getValue());
		} else {
			assessmentMode.setIpList(null);
		}
		
		assessmentMode.setApplySettingsForCoach(forCoachEl.isAtLeastSelected(1));

		//mode need to be persisted for the following relations
		if(assessmentMode.getKey() == null) {
			assessmentMode = assessmentModeMgr.persist(assessmentMode);
		}
		
		saveRelations(target);

		assessmentMode = assessmentModeMgr.merge(assessmentMode, forceStatus);
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		ChangeAssessmentModeEvent changedEvent = new ChangeAssessmentModeEvent(assessmentMode, entry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(changedEvent, ChangeAssessmentModeEvent.ASSESSMENT_MODE_ORES);
	}

	public void saveRelations(Target target) {
		AssessmentModeHelper.updateBusinessGroupRelations(groupKeys, assessmentMode, target,
				assessmentModeMgr, businessGroupService);
		AssessmentModeHelper.updateAreaRelations(areaKeys, assessmentMode, target,
				assessmentModeMgr, areaMgr);
		AssessmentModeHelper.updateCurriculumElementsRelations(curriculumElementKeys, assessmentMode, target,
				assessmentModeMgr, curriculumService);
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(ipsEl == source) {
			ipListEl.setVisible(ipsEl.isAtLeastSelected(1));
		} else if(chooseAreasButton == source) {
			doChooseAreas(ureq);
		} else if(chooseGroupsButton == source) {
			doChooseGroups(ureq);
		} else if(chooseCurriculumElementsButton == source) {
			doChooseCurriculumElements(ureq);
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doChooseAreas(UserRequest ureq) {
		if(guardModalController(areaChooseCtrl)) return;

		ICourse course = CourseFactory.loadCourse(courseOres);
		CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();
		areaChooseCtrl = new AreaSelectionController(ureq, getWindowControl(), true, groupManager, areaKeys);
		listenTo(areaChooseCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", areaChooseCtrl.getInitialComponent(),
				true, translate("popup.chooseareas"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseGroups(UserRequest ureq) {
		if(guardModalController(groupChooseCtrl)) return;
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();
		groupChooseCtrl = new GroupSelectionController(ureq, getWindowControl(), true, groupManager, groupKeys);
		listenTo(groupChooseCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", groupChooseCtrl.getInitialComponent(),
				true, translate("popup.choosegroups"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseCurriculumElements(UserRequest ureq) {
		if(guardModalController(curriculumElementChooseCtrl)) return;
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();
		curriculumElementChooseCtrl = new CurriculumElementSelectionController(ureq, getWindowControl(), groupManager, curriculumElementKeys);
		listenTo(curriculumElementChooseCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", curriculumElementChooseCtrl.getInitialComponent(),
				true, translate("popup.choosecurriculumelements"), true);
		listenTo(cmc);
		cmc.activate();
	}
}