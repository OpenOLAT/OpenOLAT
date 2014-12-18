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
package org.olat.course.assessment.ui;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.RichTextElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMode;
import org.olat.course.assessment.AssessmentMode.Target;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeToGroup;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
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

	private SingleSelection targetEl;
	private IntegerElement leadTimeEl;
	private DateChooser beginEl, endEl;
	private FormLink chooseGroupsButton, chooseAreasButton, chooseElementsButton;
	private TextElement nameEl, ipListEl, safeExamBrowserKeyEl;
	private RichTextElement descriptionEl, safeExamBrowserHintEl;
	private FormLayoutContainer chooseGroupsCont, chooseElementsCont;
	private MultipleSelectionElement ipsEl, safeExamBrowserEl, forCoachEl, courseElementsRestrictionEl;
	
	private CloseableModalController cmc;
	private AreaSelectionController areaChooseCtrl;
	private GroupSelectionController groupChooseCtrl;
	private ChooseElementsController chooseElementsCtrl;
	
	private List<Long> areaKeys;
	private List<String> areaNames;
	private List<Long> groupKeys;
	private List<String> groupNames;
	private List<String> elementKeys;
	private List<String> elementNames;
	
	private AssessmentMode assessmentMode;
	private final OLATResourceable courseOres;
	
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private BusinessGroupService businessGroupService;
	
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
		if(StringHelper.containsNonWhitespace(assessmentMode.getName())) {
			setFormTitle("form.mode.title", new String[]{ assessmentMode.getName() });
		} else {
			setFormTitle("form.mode.title.add");
		}
		setFormDescription("form.mode.description");
		
		String name = assessmentMode.getName();
		nameEl = uifactory.addTextElement("mode.name", "mode.name", 255, name, formLayout);
		nameEl.setMandatory(true);
		
		String desc = assessmentMode.getDescription();
		descriptionEl = uifactory.addRichTextElementForStringData("mode.description", "mode.description",
				desc, 6, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		
		beginEl = uifactory.addDateChooser("mode.begin", assessmentMode.getBegin(), formLayout);
		beginEl.setDateChooserTimeEnabled(true);
		beginEl.setMandatory(true);
		
		int leadTime = assessmentMode.getLeadTime();
		if(leadTime < 0) {
			leadTime = 0;
		}
		leadTimeEl = uifactory.addIntegerElement("mode.leadTime", leadTime, formLayout);
		leadTimeEl.setDisplaySize(3);
		
		endEl = uifactory.addDateChooser("mode.end", assessmentMode.getEnd(), formLayout);
		endEl.setDateChooserTimeEnabled(true);
		endEl.setMandatory(true);
		
		String[] audienceKeys = new String[] {
			AssessmentMode.Target.courseAndGroups.name(),
			AssessmentMode.Target.course.name(),
			AssessmentMode.Target.groups.name()
		};
		String[] audienceValues = new String[] {
			translate("target.courseAndGroups"),
			translate("target.course"),
			translate("target.groups")
		};
		targetEl = uifactory.addRadiosVertical("audience", "mode.target", formLayout, audienceKeys, audienceValues);
		Target target = assessmentMode.getTargetAudience();
		if(target != null) {
			for(String audienceKey:audienceKeys) {
				if(audienceKey.equals(target.name())) {
					targetEl.select(audienceKey, true);
				}
			}
		}
		if(!targetEl.isOneSelected()) {
			targetEl.select(audienceKeys[0], true);
		}
		//choose groups
		String groupPage = velocity_root + "/choose_groups.html";
		chooseGroupsCont = FormLayoutContainer.createCustomFormLayout("chooseGroups", getTranslator(), groupPage);
		chooseGroupsCont.setRootForm(mainForm);
		formLayout.add(chooseGroupsCont);
		
		chooseGroupsButton = uifactory.addFormLink("choose.groups", chooseGroupsCont, Link.BUTTON);
		chooseAreasButton = uifactory.addFormLink("choose.areas", chooseGroupsCont, Link.BUTTON);

		groupKeys = new ArrayList<>();
		groupNames = new ArrayList<>();
		for(AssessmentModeToGroup modeToGroup: assessmentMode.getGroups()) {
			BusinessGroup group = modeToGroup.getBusinessGroup();
			groupKeys.add(group.getKey());
			groupNames.add(group.getName());
		}
		chooseGroupsCont.getFormItemComponent().contextPut("groupNames", groupNames);
		areaKeys = new ArrayList<>();
		areaNames = new ArrayList<>();
		chooseGroupsCont.getFormItemComponent().contextPut("areaNames", areaNames);
	
		//course elements
		courseElementsRestrictionEl = uifactory.addCheckboxesHorizontal("cer", "mode.course.element.restriction", formLayout, onKeys, onValues);
		courseElementsRestrictionEl.addActionListener(FormEvent.ONCHANGE);
		courseElementsRestrictionEl.select(onKeys[0], assessmentMode.isRestrictAccessElements());
		
		String coursePage = velocity_root + "/choose_elements.html";
		chooseElementsCont = FormLayoutContainer.createCustomFormLayout("chooseElements", getTranslator(), coursePage);
		chooseElementsCont.setRootForm(mainForm);
		formLayout.add(chooseElementsCont);
		chooseElementsCont.setVisible(assessmentMode.isRestrictAccessElements());
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		CourseEditorTreeModel treeModel = course.getEditorTreeModel();
		
		elementKeys = new ArrayList<>();
		elementNames = new ArrayList<>();
		String elements = assessmentMode.getElementList();
		if(StringHelper.containsNonWhitespace(elements)) {
			for(String element:elements.split(",")) {
				elementKeys.add(element);
				elementNames.add(getCourseNodeName(element, treeModel));
			}
		}
		chooseElementsCont.getFormItemComponent().contextPut("elementNames", elementNames);
		
		chooseElementsButton = uifactory.addFormLink("choose.elements", chooseElementsCont, Link.BUTTON);
		
		//ips
		ipsEl = uifactory.addCheckboxesHorizontal("ips", "mode.ips", formLayout, onKeys, onValues);
		ipsEl.select(onKeys[0], assessmentMode.isRestrictAccessIps());
		ipsEl.addActionListener(FormEvent.ONCHANGE);
		String ipList = assessmentMode.getIpList();
		ipListEl = uifactory.addTextAreaElement("mode.ips.list", "mode.ips.list", 4096, 4, 60, false, ipList, formLayout);
		ipListEl.setVisible(assessmentMode.isRestrictAccessIps());
		
		safeExamBrowserEl = uifactory.addCheckboxesHorizontal("safeexam", "mode.safeexambrowser", formLayout, onKeys, onValues);
		safeExamBrowserEl.select(onKeys[0], assessmentMode.isSafeExamBrowser());
		safeExamBrowserEl.addActionListener(FormEvent.ONCHANGE);
		String key = assessmentMode.getSafeExamBrowserKey();
		safeExamBrowserKeyEl = uifactory.addTextAreaElement("safeexamkey", "mode.safeexambrowser.key", 4096, 6, 60, false, key, formLayout);
		safeExamBrowserKeyEl.setVisible(assessmentMode.isSafeExamBrowser());
		String hint = assessmentMode.getSafeExamBrowserHint();
		safeExamBrowserHintEl = uifactory.addRichTextElementForStringData("safeexamhint", "mode.safeexambrowser.hint",
				hint, 10, -1, false, null, null, formLayout, ureq.getUserSession(), getWindowControl());
		safeExamBrowserHintEl.setVisible(assessmentMode.isSafeExamBrowser());
		
		forCoachEl = uifactory.addCheckboxesHorizontal("forcoach", "mode.for.coach", formLayout, onKeys, onValues);
		forCoachEl.select(onKeys[0], assessmentMode.isApplySettingsForCoach());
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("button", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormSubmitButton("save", buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
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
				groupNames = groupChooseCtrl.getSelectedNames();
				chooseGroupsCont.getFormItemComponent().contextPut("groupNames", groupNames);
				flc.setDirty(true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(areaChooseCtrl == source) {
			if(Event.DONE_EVENT == event || Event.CHANGED_EVENT == event) {
				areaKeys = areaChooseCtrl.getSelectedKeys();
				areaNames = areaChooseCtrl.getSelectedNames();
				chooseGroupsCont.getFormItemComponent().contextPut("areaNames", areaNames);
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
		} else if(cmc == source) {
			cmc.deactivate();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(chooseElementsCtrl);
		removeAsListenerAndDispose(groupChooseCtrl);
		removeAsListenerAndDispose(areaChooseCtrl);
		removeAsListenerAndDispose(cmc);
		chooseElementsCtrl = null;
		groupChooseCtrl = null;
		areaChooseCtrl = null;
		cmc = null;
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
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
		
		courseElementsRestrictionEl.clearError();
		if(courseElementsRestrictionEl.isAtLeastSelected(1)) {
			if(elementKeys.isEmpty()) {
				endEl.setErrorKey("form.legende.mandatory", null);
				allOk &= false;
			}
		}
		
		return allOk & super.validateFormLogic(ureq);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		assessmentMode.setName(nameEl.getValue());
		assessmentMode.setDescription(descriptionEl.getValue());
		assessmentMode.setBegin(beginEl.getDate());
		if(leadTimeEl.getIntValue() > 0) {
			assessmentMode.setLeadTime(leadTimeEl.getIntValue());
		} else {
			assessmentMode.setLeadTime(0);
		}
		assessmentMode.setEnd(endEl.getDate());
		String targetKey = targetEl.getSelectedKey();
		assessmentMode.setTargetAudience(AssessmentMode.Target.valueOf(targetKey));

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
			assessmentMode = assessmentModeMgr.save(assessmentMode);
		}

		if(groupKeys.isEmpty()) {
			if(assessmentMode.getGroups().size() > 0) {
				assessmentMode.getGroups().clear();
			}
		} else {
			Set<Long> currentKeys = new HashSet<>();
			for(Iterator<AssessmentModeToGroup> modeToGroupIt=assessmentMode.getGroups().iterator(); modeToGroupIt.hasNext(); ) {
				Long currentKey = modeToGroupIt.next().getBusinessGroup().getKey();
				if(!groupKeys.contains(currentKey)) {
					modeToGroupIt.remove();
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

		assessmentMode = assessmentModeMgr.save(assessmentMode);
		fireEvent(ureq, Event.CHANGED_EVENT);
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
		} else if(chooseElementsButton == source) {
			doChooseElements(ureq);
		}
		
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doChooseElements(UserRequest ureq) {
		if(chooseElementsCtrl != null) return;

		chooseElementsCtrl = new ChooseElementsController(ureq, getWindowControl(), elementKeys, courseOres);
		listenTo(chooseElementsCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", chooseElementsCtrl.getInitialComponent(),
				true, getTranslator().translate("popup.chooseareas"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseAreas(UserRequest ureq) {
		if(areaChooseCtrl != null) return;

		ICourse course = CourseFactory.loadCourse(courseOres);
		CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();
		areaChooseCtrl = new AreaSelectionController(ureq, getWindowControl(), true, groupManager, areaKeys);
		listenTo(areaChooseCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", areaChooseCtrl.getInitialComponent(),
				true, getTranslator().translate("popup.chooseareas"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doChooseGroups(UserRequest ureq) {
		if(groupChooseCtrl != null) return;
		
		ICourse course = CourseFactory.loadCourse(courseOres);
		CourseGroupManager groupManager = course.getCourseEnvironment().getCourseGroupManager();
		groupChooseCtrl = new GroupSelectionController(ureq, getWindowControl(), true, groupManager, groupKeys);
		listenTo(groupChooseCtrl);

		cmc = new CloseableModalController(getWindowControl(), "close", groupChooseCtrl.getInitialComponent(),
				true, translate("popup.choosegroups"));
		listenTo(cmc);
		cmc.activate();
	}
}