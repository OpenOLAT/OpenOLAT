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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
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
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.condition.AreaSelectionController;
import org.olat.course.condition.CurriculumElementSelectionController;
import org.olat.course.condition.GroupSelectionController;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 24 janv. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeEditRestrictionController extends FormBasicController {

	private static final String[] onKeys = new String[]{ "on" };
	private static final String[] onValues = new String[]{ "" };

	private StaticTextElement startElementEl;
	private FormLink chooseStartElementButton;
	private FormLink chooseElementsButton;

	private FormLayoutContainer chooseElementsCont;
	private MultipleSelectionElement courseElementsRestrictionEl;
	
	private CloseableModalController cmc;
	private DialogBoxController confirmCtrl;
	private AreaSelectionController areaChooseCtrl;
	private GroupSelectionController groupChooseCtrl;
	private ChooseElementsController chooseElementsCtrl;
	private ChooseStartElementController chooseStartElementCtrl;
	private CurriculumElementSelectionController curriculumElementChooseCtrl;
	
	private List<String> elementKeys;
	private List<String> elementNames;
	private String startElementKey;
	
	private RepositoryEntry entry;
	private AssessmentMode assessmentMode;
	private final OLATResourceable courseOres;
	
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private AssessmentModeCoordinationService modeCoordinationService;
	
	public AssessmentModeEditRestrictionController(UserRequest ureq, WindowControl wControl,
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
		
		FormLayoutContainer buttonCont = FormLayoutContainer.createButtonLayout("button", getTranslator());
		formLayout.add(buttonCont);
		uifactory.addFormCancelButton("cancel", buttonCont, ureq, getWindowControl());
		if(status != Status.end) {
			uifactory.addFormSubmitButton("save", buttonCont);
		}
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
		if(chooseElementsCtrl == source) {
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
		
		courseElementsRestrictionEl.clearError();
		if(courseElementsRestrictionEl.isAtLeastSelected(1) && elementKeys.isEmpty()) {
			courseElementsRestrictionEl.setErrorKey("error.course.element.mandatory", null);
			allOk &= false;
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
				save(ureq, false);
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

		//mode need to be persisted for the following relations
		if(assessmentMode.getKey() == null) {
			assessmentMode = assessmentModeMgr.persist(assessmentMode);
		}

		assessmentMode = assessmentModeMgr.merge(assessmentMode, forceStatus);
		fireEvent(ureq, Event.CHANGED_EVENT);
		
		ChangeAssessmentModeEvent changedEvent = new ChangeAssessmentModeEvent(assessmentMode, entry);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(changedEvent, ChangeAssessmentModeEvent.ASSESSMENT_MODE_ORES);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(courseElementsRestrictionEl == source) {
			boolean enabled = courseElementsRestrictionEl.isAtLeastSelected(1);
			chooseElementsCont.setVisible(enabled);
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
}