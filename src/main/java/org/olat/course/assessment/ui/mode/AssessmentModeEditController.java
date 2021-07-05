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
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.StringTokenizer;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
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
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.CurriculumElementSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.area.BGArea;
import org.olat.group.area.BGAreaManager;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumModule;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 12.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeEditController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };
	private static final String[] startModeKeys = new String[] { "automatic", "manual" };

	private SingleSelection targetEl;
	private SingleSelection startModeEl;
	private IntegerElement leadTimeEl;
	private IntegerElement followupTimeEl;
	private DateChooser beginEl;
	private DateChooser endEl;
	private StaticTextElement startElementEl;
	private FormLink chooseGroupsButton;
	private FormLink chooseAreasButton;
	private FormLink chooseStartElementButton;
	private FormLink chooseElementsButton;
	private FormLink chooseCurriculumElementsButton;
	private TextElement nameEl;
	private TextElement ipListEl;
	private TextElement safeExamBrowserKeyEl;
	private RichTextElement descriptionEl;
	private RichTextElement safeExamBrowserHintEl;
	private FormLayoutContainer chooseGroupsCont;
	private FormLayoutContainer chooseElementsCont;
	private MultipleSelectionElement ipsEl;
	private MultipleSelectionElement forCoachEl;
	private MultipleSelectionElement safeExamBrowserEl;
	private MultipleSelectionElement courseElementsRestrictionEl;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmCtrl;
	private AreaSelectionController areaChooseCtrl;
	private GroupSelectionController groupChooseCtrl;
	private ChooseElementsController chooseElementsCtrl;
	private ChooseStartElementController chooseStartElementCtrl;
	private CurriculumElementSelectionController curriculumElementChooseCtrl;
	
	private List<Long> areaKeys;
	private List<String> areaNames;
	private List<Long> groupKeys;
	private List<String> groupNames;
	private List<Long> curriculumElementKeys;
	private List<String> curriculumElementNames;
	private List<String> elementKeys;
	private List<String> elementNames;
	private String startElementKey;
	
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
	
	public AssessmentModeEditController(UserRequest ureq, WindowControl wControl,
			OLATResourceable courseOres, AssessmentMode assessmentMode) {
		super(ureq, wControl);
		this.courseOres = OresHelper.clone(courseOres);
		if(assessmentMode.getKey() == null) {
			this.assessmentMode = assessmentMode;
		} else {
			this.assessmentMode = assessmentModeMgr.getAssessmentModeById(assessmentMode.getKey());
		}
		initForm(ureq);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		formLayout.setElementCssClass("o_sel_assessment_mode_edit_form");
		setFormContextHelp("Assessment mode");

		if(StringHelper.containsNonWhitespace(assessmentMode.getName())) {
			setFormTitle("form.mode.title", new String[]{ assessmentMode.getName() });
		} else {
			setFormTitle("form.mode.title.add");
		}
		setFormDescription("form.mode.description");
		
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
		String name = assessmentMode.getName();
		nameEl = uifactory.addTextElement("mode.name", "mode.name", 255, name, formLayout);
		nameEl.setElementCssClass("o_sel_assessment_mode_name");
		nameEl.setMandatory(true);
		nameEl.setEnabled(status != Status.followup && status != Status.end);
		
		String desc = assessmentMode.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringData("mode.description", "mode.description",
				desc, 6, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		descriptionEl.getEditorConfiguration().setPathInStatusBar(false);
		descriptionEl.setEnabled(status != Status.followup && status != Status.end);
		
		beginEl = uifactory.addDateChooser("mode.begin", assessmentMode.getBegin(), formLayout);
		beginEl.setElementCssClass("o_sel_assessment_mode_begin");
		beginEl.setDateChooserTimeEnabled(true);
		beginEl.setMandatory(true);
		beginEl.setEnabled(status == Status.none || status == Status.leadtime);
		
		int leadTime = assessmentMode.getLeadTime();
		if(leadTime < 0) {
			leadTime = 0;
		}
		leadTimeEl = uifactory.addIntegerElement("mode.leadTime", leadTime, formLayout);
		leadTimeEl.setElementCssClass("o_sel_assessment_mode_leadtime");
		leadTimeEl.setDisplaySize(3);
		leadTimeEl.setEnabled(status == Status.none || status == Status.leadtime);
		
		endEl = uifactory.addDateChooser("mode.end", assessmentMode.getEnd(), formLayout);
		endEl.setElementCssClass("o_sel_assessment_mode_end");
		endEl.setDateChooserTimeEnabled(true);
		endEl.setDefaultValue(beginEl);
		endEl.setMandatory(true);
		endEl.setEnabled(status != Status.end);
		
		int followupTime = assessmentMode.getFollowupTime();
		if(followupTime < 0) {
			followupTime = 0;
		}
		followupTimeEl = uifactory.addIntegerElement("mode.followupTime", followupTime, formLayout);
		followupTimeEl.setElementCssClass("o_sel_assessment_mode_followuptime");
		followupTimeEl.setDisplaySize(3);
		followupTimeEl.setEnabled(status != Status.end);
		
		String[] startModeValues = new String[] {
				translate("mode.beginend.automatic"), translate("mode.beginend.manual")
		};
		startModeEl = uifactory.addDropdownSingleselect("mode.beginend", formLayout, startModeKeys, startModeValues, null);
		startModeEl.setElementCssClass("o_sel_assessment_mode_start_mode");
		if(assessmentMode.isManualBeginEnd()) {
			startModeEl.select(startModeKeys[1], true);
		} else {
			startModeEl.select(startModeKeys[0], true);
		}
		startModeEl.setEnabled(status != Status.end);
		
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
		targetEl.setEnabled(status != Status.end);
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
		chooseGroupsButton.setEnabled(status != Status.end);
		chooseAreasButton = uifactory.addFormLink("choose.areas", chooseGroupsCont, Link.BUTTON);
		chooseAreasButton.setEnabled(status != Status.end);
		chooseCurriculumElementsButton = uifactory.addFormLink("choose.curriculum.elements", chooseGroupsCont, Link.BUTTON);
		chooseCurriculumElementsButton.setEnabled(status != Status.end);
		chooseCurriculumElementsButton.setVisible(curriculumEnabled);

		selectBusinessGroups(assessmentMode.getGroups());
		selectAreas(assessmentMode.getAreas());
		selectCurriculumElements(assessmentMode.getCurriculumElements());

		//course elements
		courseElementsRestrictionEl = uifactory.addCheckboxesHorizontal("cer", "mode.course.element.restriction", formLayout, onKeys, onValues);
		courseElementsRestrictionEl.addActionListener(FormEvent.ONCHANGE);
		courseElementsRestrictionEl.select(onKeys[0], assessmentMode.isRestrictAccessElements());
		courseElementsRestrictionEl.setEnabled(status != Status.end);
		
		String coursePage = velocity_root + "/choose_elements.html";
		chooseElementsCont = FormLayoutContainer.createCustomFormLayout("chooseElements", getTranslator(), coursePage);
		chooseElementsCont.setRootForm(mainForm);
		formLayout.add(chooseElementsCont);
		chooseElementsCont.setVisible(assessmentMode.isRestrictAccessElements());
		
		CourseEditorTreeModel treeModel = course.getEditorTreeModel();
		
		elementKeys = new ArrayList<>();
		elementNames = new ArrayList<>();
		String elements = assessmentMode.getElementList();
		if(StringHelper.containsNonWhitespace(elements)) {
			for(String element:elements.split(",")) {
				String courseNodeName = getCourseNodeName(element, treeModel);
				if(StringHelper.containsNonWhitespace(courseNodeName)) {
					elementKeys.add(element);
					elementNames.add(courseNodeName);
				}
			}
		}
		chooseElementsCont.getFormItemComponent().contextPut("elementNames", elementNames);
		
		chooseElementsButton = uifactory.addFormLink("choose.elements", chooseElementsCont, Link.BUTTON);
		chooseElementsButton.setEnabled(status != Status.end);
		
		startElementKey = assessmentMode.getStartElement();
		String startElementName = "";
		if(StringHelper.containsNonWhitespace(startElementKey)) {
			startElementName = getCourseNodeName(startElementKey, treeModel);
		}
		startElementEl = uifactory.addStaticTextElement("mode.start.element", "mode.start.element", startElementName, formLayout);
		chooseStartElementButton = uifactory.addFormLink("choose.start.element", formLayout, Link.BUTTON);
		chooseStartElementButton.setEnabled(status != Status.end);

		//ips
		ipsEl = uifactory.addCheckboxesHorizontal("ips", "mode.ips", formLayout, onKeys, onValues);
		ipsEl.select(onKeys[0], assessmentMode.isRestrictAccessIps());
		ipsEl.addActionListener(FormEvent.ONCHANGE);
		ipsEl.setEnabled(status != Status.end);
		String ipList = assessmentMode.getIpList();
		ipListEl = uifactory.addTextAreaElement("mode.ips.list", "mode.ips.list", 16000, 4, 60, false, false, ipList, formLayout);
		ipListEl.setMaxLength(16000);
		ipListEl.setVisible(assessmentMode.isRestrictAccessIps());
		ipListEl.setEnabled(status != Status.end);
		
		safeExamBrowserEl = uifactory.addCheckboxesHorizontal("safeexam", "mode.safeexambrowser", formLayout, onKeys, onValues);
		safeExamBrowserEl.select(onKeys[0], assessmentMode.isSafeExamBrowser());
		safeExamBrowserEl.addActionListener(FormEvent.ONCHANGE);
		safeExamBrowserEl.setEnabled(status != Status.end);
		String key = assessmentMode.getSafeExamBrowserKey();
		safeExamBrowserKeyEl = uifactory.addTextAreaElement("safeexamkey", "mode.safeexambrowser.key", 16000, 6, 60, false, false, key, formLayout);
		safeExamBrowserKeyEl.setMaxLength(16000);
		safeExamBrowserKeyEl.setVisible(assessmentMode.isSafeExamBrowser());
		safeExamBrowserKeyEl.setEnabled(status != Status.end);
		String hint = assessmentMode.getSafeExamBrowserHint();
		safeExamBrowserHintEl = uifactory.addRichTextElementForStringData("safeexamhint", "mode.safeexambrowser.hint",
				hint, 10, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		safeExamBrowserHintEl.setVisible(assessmentMode.isSafeExamBrowser());
		safeExamBrowserHintEl.setEnabled(status != Status.end);
		
		forCoachEl = uifactory.addCheckboxesHorizontal("forcoach", "mode.for.coach", formLayout, onKeys, onValues);
		forCoachEl.select(onKeys[0], assessmentMode.isApplySettingsForCoach());
		forCoachEl.setEnabled(status != Status.end);
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("button", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		if(status != Status.end) {
			uifactory.addFormSubmitButton("save", buttonCont);
		}
	}
	
	protected void selectBusinessGroups(Set<AssessmentModeToGroup> assessmentModeToGroups) {
		groupKeys = new ArrayList<>();
		groupNames = new ArrayList<>();
		for(AssessmentModeToGroup modeToGroup:assessmentModeToGroups) {
			BusinessGroup group = modeToGroup.getBusinessGroup();
			groupKeys.add(group.getKey());
			groupNames.add(StringHelper.escapeHtml(group.getName()));
		}
		chooseGroupsCont.getFormItemComponent().contextPut("groupNames", groupNames);
	}
	
	protected void selectAreas(Set<AssessmentModeToArea> assessmentModeToAreas) {
		areaKeys = new ArrayList<>();
		areaNames = new ArrayList<>();
		for(AssessmentModeToArea modeToArea: assessmentModeToAreas) {
			BGArea area = modeToArea.getArea();
			areaKeys.add(area.getKey());
			areaNames.add(StringHelper.escapeHtml(area.getName()));
		}
		chooseGroupsCont.getFormItemComponent().contextPut("areaNames", areaNames);
	}
	
	protected void selectCurriculumElements(Set<AssessmentModeToCurriculumElement> assessmentModeToCurriculumElements) {
		curriculumElementKeys = new ArrayList<>();
		curriculumElementNames = new ArrayList<>();
		for(AssessmentModeToCurriculumElement modeToElement: assessmentModeToCurriculumElements) {
			CurriculumElement element = modeToElement.getCurriculumElement();
			curriculumElementKeys.add(element.getKey());
			curriculumElementNames.add(StringHelper.escapeHtml(element.getDisplayName()));
		}
		chooseGroupsCont.getFormItemComponent().contextPut("curriculumElementNames", curriculumElementNames);
	}
	
	private String getCourseNodeName(String ident, CourseEditorTreeModel treeModel) {
		String name = null;
		CourseNode courseNode = treeModel.getCourseNode(ident);
		if(courseNode != null) {
			name = courseNode.getShortTitle();
		}
		return name;
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
		} else if(chooseElementsCtrl == source) {
			if(Event.DONE_EVENT == event || Event.CHANGED_EVENT == event) {
				elementKeys = chooseElementsCtrl.getSelectedKeys();
				elementNames = chooseElementsCtrl.getSelectedNames();
				chooseElementsCont.getFormItemComponent().contextPut("elementNames", elementNames);
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(chooseStartElementCtrl == source) {
			if(Event.DONE_EVENT == event || Event.CHANGED_EVENT == event) {
				startElementKey = chooseStartElementCtrl.getSelectedKey();
				String elementName = chooseStartElementCtrl.getSelectedName();
				startElementEl.setValue(elementName);
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
		removeAsListenerAndDispose(chooseStartElementCtrl);
		removeAsListenerAndDispose(chooseElementsCtrl);
		removeAsListenerAndDispose(groupChooseCtrl);
		removeAsListenerAndDispose(areaChooseCtrl);
		removeAsListenerAndDispose(cmc);
		curriculumElementChooseCtrl = null;
		chooseStartElementCtrl = null;
		chooseElementsCtrl = null;
		groupChooseCtrl = null;
		areaChooseCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		nameEl.clearError();
		if(StringHelper.containsNonWhitespace(nameEl.getValue())) {
			//too long
		} else {
			nameEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		endEl.clearError();
		beginEl.clearError();
		if(beginEl.getDate() == null) {
			beginEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		if(endEl.getDate() == null) {
			endEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		if(beginEl.getDate() != null && endEl.getDate() != null
				&& beginEl.getDate().compareTo(endEl.getDate()) >= 0) {
			beginEl.setErrorKey("error.begin.after.end", null);
			endEl.setErrorKey("error.begin.after.end", null);
			allOk &= false;
		}
		
		courseElementsRestrictionEl.clearError();
		if(courseElementsRestrictionEl.isAtLeastSelected(1) && elementKeys.isEmpty()) {
			courseElementsRestrictionEl.setErrorKey("error.course.element.mandatory", null);
			allOk &= false;
		}
		
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
		
		safeExamBrowserKeyEl.clearError();
		if(safeExamBrowserEl.isAtLeastSelected(1)) {
			String value = safeExamBrowserKeyEl.getValue();
			if(!StringHelper.containsNonWhitespace(value)) {
				safeExamBrowserKeyEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			} else if(value.length() > safeExamBrowserKeyEl.getMaxLength()) {
				safeExamBrowserKeyEl.setErrorKey("form.error.toolong", new String[] { Integer.toString(safeExamBrowserKeyEl.getMaxLength()) } );
				allOk &= false;
			}
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
		Date begin = beginEl.getDate();
		Date end = endEl.getDate();
		int followupTime = followupTimeEl.getIntValue();
		int leadTime = leadTimeEl.getIntValue();

		Status currentStatus = assessmentMode.getStatus();
		if(startModeEl.isOneSelected() && startModeEl.isSelected(1)) {
			//manual start don't change the status of the assessment
			save(ureq, false);
		} else {
			Status nextStatus = modeCoordinationService.evaluateStatus(begin, leadTime, end, followupTime);
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

		assessmentMode.setName(nameEl.getValue());
		assessmentMode.setDescription(descriptionEl.getValue());
		
		assessmentMode.setBegin(beginEl.getDate());
		if(leadTimeEl.getIntValue() > 0) {
			assessmentMode.setLeadTime(leadTimeEl.getIntValue());
		} else {
			assessmentMode.setLeadTime(0);
		}
		
		assessmentMode.setEnd(endEl.getDate());
		if(followupTimeEl.getIntValue() > 0) {
			assessmentMode.setFollowupTime(followupTimeEl.getIntValue());
		} else {
			assessmentMode.setFollowupTime(0);
		}
		
		if(startModeEl.isOneSelected() && startModeEl.isSelected(1)) {
			assessmentMode.setManualBeginEnd(true);
		} else {
			assessmentMode.setManualBeginEnd(false);
		}
		
		String targetKey = targetEl.getSelectedKey();
		Target target = AssessmentMode.Target.valueOf(targetKey);
		assessmentMode.setTargetAudience(target);

		boolean elementRestrictions = courseElementsRestrictionEl.isAtLeastSelected(1);
		assessmentMode.setRestrictAccessElements(elementRestrictions);
		if(elementRestrictions) {
			StringBuilder sb = new StringBuilder();
			for(String elementKey:elementKeys) {
				if(sb.length() > 0) sb.append(",");
				sb.append(elementKey);
			}
			assessmentMode.setElementList(sb.toString());
		} else {
			assessmentMode.setElementList(null);
		}
		
		if(StringHelper.containsNonWhitespace(startElementKey)) {
			assessmentMode.setStartElement(startElementKey);
		} else {
			assessmentMode.setStartElement(null);
		}
		
		boolean ipRestriction = ipsEl.isAtLeastSelected(1);
		assessmentMode.setRestrictAccessIps(ipRestriction);
		if(ipRestriction) {
			assessmentMode.setIpList(ipListEl.getValue());
		} else {
			assessmentMode.setIpList(null);
		}
		
		boolean safeExamEnabled = safeExamBrowserEl.isAtLeastSelected(1);
		assessmentMode.setSafeExamBrowser(safeExamEnabled);
		if(safeExamEnabled) {
			assessmentMode.setSafeExamBrowserKey(safeExamBrowserKeyEl.getValue());
			assessmentMode.setSafeExamBrowserHint(safeExamBrowserHintEl.getValue());
		} else {
			assessmentMode.setSafeExamBrowserKey(null);
			assessmentMode.setSafeExamBrowserHint(null);
		}
		
		assessmentMode.setApplySettingsForCoach(forCoachEl.isAtLeastSelected(1));

		//mode need to be persisted for the following relations
		if(assessmentMode.getKey() == null) {
			assessmentMode = assessmentModeMgr.persist(assessmentMode);
		}
		
		updateBusinessGroupRelations(target);
		updateAreaRelations(target);
		updateCurriculumElementsRelations(target);

		assessmentMode = assessmentModeMgr.merge(assessmentMode, forceStatus);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void updateCurriculumElementsRelations(Target target) {
		if(curriculumElementKeys.isEmpty() || target == Target.course || target == Target.groups) {
			if(!assessmentMode.getCurriculumElements().isEmpty()) {
				List<AssessmentModeToCurriculumElement> currentElements = new ArrayList<>(assessmentMode.getCurriculumElements());
				for(AssessmentModeToCurriculumElement modeToElement:currentElements) {
					assessmentModeMgr.deleteAssessmentModeToCurriculumElement(modeToElement);
				}
				assessmentMode.getCurriculumElements().clear();
			}
		} else {
			Set<Long> currentKeys = new HashSet<>();
			List<AssessmentModeToCurriculumElement> currentElements = new ArrayList<>(assessmentMode.getCurriculumElements());
			for(AssessmentModeToCurriculumElement modeToElement:currentElements) {
				Long currentKey = modeToElement.getCurriculumElement().getKey();
				if(!curriculumElementKeys.contains(currentKey)) {
					assessmentMode.getCurriculumElements().remove(modeToElement);
					assessmentModeMgr.deleteAssessmentModeToCurriculumElement(modeToElement);
				} else {
					currentKeys.add(currentKey);
				}
			}
			
			for(Long curriculumElementKey:curriculumElementKeys) {
				if(!currentKeys.contains(curriculumElementKey)) {
					CurriculumElement element = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
					AssessmentModeToCurriculumElement modeToElement = assessmentModeMgr.createAssessmentModeToCurriculumElement(assessmentMode, element);
					assessmentMode.getCurriculumElements().add(modeToElement);
				}
			}
		}
	}
	
	private void updateAreaRelations(Target target) {
		//update areas
		if(areaKeys.isEmpty() || target == Target.course || target == Target.curriculumEls) {
			if(!assessmentMode.getAreas().isEmpty()) {
				List<AssessmentModeToArea> currentAreas = new ArrayList<>(assessmentMode.getAreas());
				for(AssessmentModeToArea modeToArea:currentAreas) {
					assessmentModeMgr.deleteAssessmentModeToArea(modeToArea);
				}
				assessmentMode.getAreas().clear();
			}
		} else {
			Set<Long> currentKeys = new HashSet<>();
			List<AssessmentModeToArea> currentAreas = new ArrayList<>(assessmentMode.getAreas());
			for(AssessmentModeToArea modeToArea:currentAreas) {
				Long currentKey = modeToArea.getArea().getKey();
				if(!areaKeys.contains(currentKey)) {
					assessmentMode.getAreas().remove(modeToArea);
					assessmentModeMgr.deleteAssessmentModeToArea(modeToArea);
				} else {
					currentKeys.add(currentKey);
				}
			}
			
			for(Long areaKey:areaKeys) {
				if(!currentKeys.contains(areaKey)) {
					BGArea area = areaMgr.loadArea(areaKey);
					AssessmentModeToArea modeToArea = assessmentModeMgr.createAssessmentModeToArea(assessmentMode, area);
					assessmentMode.getAreas().add(modeToArea);
				}
			}
		}
	}
	
	private void updateBusinessGroupRelations(Target target) {
		//update groups
		if(groupKeys.isEmpty() || target == Target.course || target == Target.curriculumEls) {
			if(!assessmentMode.getGroups().isEmpty()) {
				List<AssessmentModeToGroup> currentGroups = new ArrayList<>(assessmentMode.getGroups());
				for(AssessmentModeToGroup modeToGroup:currentGroups) {
					assessmentModeMgr.deleteAssessmentModeToGroup(modeToGroup);
				}
				assessmentMode.getGroups().clear();
			}
		} else {
			Set<Long> currentKeys = new HashSet<>();
			List<AssessmentModeToGroup> currentGroups = new ArrayList<>(assessmentMode.getGroups());
			for(AssessmentModeToGroup modeToGroup:currentGroups) {
				Long currentKey = modeToGroup.getBusinessGroup().getKey();
				if(!groupKeys.contains(currentKey)) {
					assessmentMode.getGroups().remove(modeToGroup);
					assessmentModeMgr.deleteAssessmentModeToGroup(modeToGroup);
				} else {
					currentKeys.add(currentKey);
				}
			}
			
			for(Long groupKey:groupKeys) {
				if(!currentKeys.contains(groupKey)) {
					BusinessGroup group = businessGroupService.loadBusinessGroup(groupKey);
					AssessmentModeToGroup modeToGroup = assessmentModeMgr.createAssessmentModeToGroup(assessmentMode, group);
					assessmentMode.getGroups().add(modeToGroup);
				}
			}
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(ipsEl == source) {
			ipListEl.setVisible(ipsEl.isAtLeastSelected(1));
		} else if(safeExamBrowserEl == source) {
			boolean enabled = safeExamBrowserEl.isAtLeastSelected(1);
			safeExamBrowserKeyEl.setVisible(enabled);
			safeExamBrowserHintEl.setVisible(enabled);
		} else if(courseElementsRestrictionEl == source) {
			boolean enabled = courseElementsRestrictionEl.isAtLeastSelected(1);
			chooseElementsCont.setVisible(enabled);
		} else if(chooseAreasButton == source) {
			doChooseAreas(ureq);
		} else if(chooseGroupsButton == source) {
			doChooseGroups(ureq);
		} else if(chooseCurriculumElementsButton == source) {
			doChooseCurriculumElements(ureq);
		} else if(chooseElementsButton == source) {
			doChooseElements(ureq);
		} else if(chooseStartElementButton == source) {
			doChooseStartElement(ureq);
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doChooseElements(UserRequest ureq) {
		if(guardModalController(chooseElementsCtrl)) return;

		chooseElementsCtrl = new ChooseElementsController(ureq, getWindowControl(), elementKeys, courseOres);
		listenTo(chooseElementsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", chooseElementsCtrl.getInitialComponent(),
				true, translate("popup.chooseelements"), true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseStartElement(UserRequest ureq) {
		if(guardModalController(chooseElementsCtrl)) return;
		
		List<String> allowedKeys = courseElementsRestrictionEl.isAtLeastSelected(1)
				? new ArrayList<>(elementKeys) : null;
		chooseStartElementCtrl = new ChooseStartElementController(ureq, getWindowControl(), startElementKey, allowedKeys, courseOres);
		listenTo(chooseStartElementCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", chooseStartElementCtrl.getInitialComponent(),
				true, translate("popup.choosestartelement"), true);
		listenTo(cmc);
		cmc.activate();
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