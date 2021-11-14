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
package org.olat.course.editor.overview;

import static org.olat.core.gui.components.util.SelectionValues.entry;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.editor.EditorMainController;
import org.olat.course.learningpath.LearningPathConfigs;
import org.olat.course.learningpath.LearningPathService;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.learningpath.ui.LearningPathNodeConfigController;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.assessment.model.AssessmentObligation;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 Jan 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class BulkChangeController extends FormBasicController {
	
	private static final String[] EMPTY_VALUES = new String[]{ "" };
	private static final String KEY_TITLE = "long";
	private static final String KEY_DESCRIPTION = "description";

	private MultipleSelectionElement displayEl;
	private MultipleSelectionElement ignoreInCourseAssessmentEl;
	private TextElement durationEl;
	private SingleSelection obligationEl;
	
	private Map<MultipleSelectionElement, FormLayoutContainer> checkboxContainer = new HashMap<>();
	private final List<MultipleSelectionElement> checkboxSwitch = new ArrayList<>();

	private final ICourse course;
	private final List<CourseNode> courseNodes;
	private final boolean ignoreInCourseAssessmentAvailable;
	private final boolean learningPath;

	@Autowired
	private NodeAccessService nodeAccessService;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	@Autowired
	private LearningPathService learningPathService;

	public BulkChangeController(UserRequest ureq, WindowControl wControl, ICourse course, List<CourseNode> courseNodes) {
		super(ureq, wControl, LAYOUT_VERTICAL);
		setTranslator(Util.createPackageTranslator(EditorMainController.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(LearningPathNodeConfigController.class, getLocale(), getTranslator()));
		this.course = course;
		this.courseNodes = courseNodes;
		this.ignoreInCourseAssessmentAvailable = !nodeAccessService.isScoreCalculatorSupported(NodeAccessType.of(course));
		this.learningPath = LearningPathNodeAccessProvider.TYPE.equals(NodeAccessType.of(course).getType());
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initGeneralForm(formLayout);
		
		if (ignoreInCourseAssessmentAvailable) {
			initAssessmentForm(formLayout);
		}
		
		if (learningPath) {
			initLearningPathForm(formLayout);
		}

		FormLayoutContainer buttonsWrapperCont = FormLayoutContainer.createDefaultFormLayout("global", getTranslator());
		buttonsWrapperCont.setRootForm(mainForm);
		formLayout.add("buttonsWrapper", buttonsWrapperCont);
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		buttonsWrapperCont.add(buttonsCont);
		uifactory.addFormSubmitButton("ok", "ok", buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
	}

	private void initGeneralForm(FormItemContainer formLayout) {
		FormLayoutContainer generalCont = FormLayoutContainer.createDefaultFormLayout("general", getTranslator());
		generalCont.setFormTitle(translate("bulk.general"));
		generalCont.setRootForm(mainForm);
		formLayout.add(generalCont);
		
		SelectionValues displayKV = new SelectionValues();
		displayKV.add(entry(KEY_TITLE, translate("nodeConfigForm.title.long")));
		displayKV.add(entry(KEY_DESCRIPTION, translate("nodeConfigForm.metadata.all")));
		displayEl = uifactory.addCheckboxesVertical("nodeConfigForm.display_options", generalCont, displayKV.keys(), displayKV.values(), 1);
		decorate(displayEl, generalCont);
	}
	
	private void initAssessmentForm(FormItemContainer formLayout) {
		FormLayoutContainer assessmentCont = FormLayoutContainer.createDefaultFormLayout("assessment", getTranslator());
		assessmentCont.setFormTitle(translate("bulk.assessment"));
		assessmentCont.setRootForm(mainForm);
		formLayout.add(assessmentCont);
		
		ignoreInCourseAssessmentEl = uifactory.addCheckboxesHorizontal("ignore.in.course.assessment", assessmentCont,
				new String[] { "xx" }, new String[] { translate("ignore") });
		decorate(ignoreInCourseAssessmentEl, assessmentCont);
	}
	
	private void initLearningPathForm(FormItemContainer formLayout) {
		FormLayoutContainer lpCont = FormLayoutContainer.createDefaultFormLayout("learningPath", getTranslator());
		lpCont.setFormTitle(translate("bulk.learning.path"));
		lpCont.setRootForm(mainForm);
		formLayout.add(lpCont);
		
		durationEl = uifactory.addTextElement("config.duration", 128, "", lpCont);
		decorate(durationEl, lpCont);
		
		SelectionValues obligationKV = new SelectionValues();
		obligationKV.add(entry(AssessmentObligation.mandatory.name(), translate("config.obligation.mandatory")));
		obligationKV.add(entry(AssessmentObligation.optional.name(), translate("config.obligation.optional")));
		obligationEl = uifactory.addRadiosHorizontal("config.obligation", lpCont, obligationKV.keys(), obligationKV.values());
		obligationEl.select(obligationEl.getKey(0), true);
		decorate(obligationEl, lpCont);
	}

	private FormItem decorate(FormItem item, FormLayoutContainer formLayout) {
		String itemName = item.getName();
		MultipleSelectionElement checkbox = uifactory.addCheckboxesHorizontal("cbx_" + itemName, itemName, formLayout,
				new String[] { itemName }, EMPTY_VALUES);
		checkbox.select(itemName, false);
		checkbox.addActionListener(FormEvent.ONCLICK);
		checkbox.setUserObject(item);
		checkboxSwitch.add(checkbox);

		item.setLabel(null, null);
		item.setVisible(false);
		item.setUserObject(checkbox);
		
		checkboxContainer.put(checkbox, formLayout);
		formLayout.moveBefore(checkbox, item);
		return checkbox;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (checkboxSwitch.contains(source)) {
			MultipleSelectionElement checkbox = (MultipleSelectionElement)source;
			FormItem item = (FormItem)checkbox.getUserObject();
			item.setVisible(checkbox.isAtLeastSelected(1));
			checkboxContainer.get(checkbox).setDirty(true);
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if (learningPath) {
			allOk = validateInteger(durationEl, 1, 10000, true, "error.positiv.int");
		}
		
		return allOk;
	}
	
	public static boolean validateInteger(TextElement el, int min, int max, boolean mandatory, String i18nKey) {
		boolean allOk = true;
		el.clearError();
		if(el.isEnabled() && el.isVisible()) {
			String val = el.getValue();
			if(StringHelper.containsNonWhitespace(val)) {
				try {
					int value = Integer.parseInt(val);
					if(min > value) {
						allOk = false;
					} else if(max < value) {
						allOk = false;
					}
				} catch (NumberFormatException e) {
					allOk = false;
				}
			} else if (mandatory) {
				allOk = false;
			}
		}
		if (!allOk) {
			el.setErrorKey(i18nKey, null);
		}
		return allOk;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		for (CourseNode courseNode : courseNodes) {
			formOKGeneral(courseNode);
			if (ignoreInCourseAssessmentAvailable) {
				formOKAssessment(courseNode);
			}
			if (learningPath) {
				formOKLearningPath(courseNode);
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	private void formOKGeneral(CourseNode courseNode) {
		if (isEnabled(displayEl)) {
			String displayOption = getDisplayOption();
			courseNode.setDisplayOption(displayOption);
		}
	}
	
	private String getDisplayOption() {
		String displayOption = CourseNode.DISPLAY_OPTS_CONTENT;
		
		if (displayEl.isKeySelected(KEY_TITLE)) {
			if (displayEl.isKeySelected(KEY_DESCRIPTION)) {
				displayOption = CourseNode.DISPLAY_OPTS_TITLE_DESCRIPTION_CONTENT;
			} else {
				displayOption = CourseNode.DISPLAY_OPTS_TITLE_CONTENT;
			}
		} else if (displayEl.isKeySelected(KEY_DESCRIPTION)) {
			displayOption = CourseNode.DISPLAY_OPTS_DESCRIPTION_CONTENT;
		}
		
		return displayOption;
	}

	private void formOKAssessment(CourseNode courseNode) {
		if (isEnabled(ignoreInCourseAssessmentEl)) {
			boolean ignoreInCourseAssessment = ignoreInCourseAssessmentEl.isAtLeastSelected(1);
			AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
			assessmentConfig.setIgnoreInCourseAssessment(ignoreInCourseAssessment);
		}
	}
	
	private void formOKLearningPath(CourseNode courseNode) {
		CourseEditorTreeNode editorTreeNode = course.getEditorTreeModel().getCourseEditorNodeById(courseNode.getIdent());
		LearningPathConfigs learningPathConfigs = learningPathService.getConfigs(courseNode, editorTreeNode.getParent());
		if (isEnabled(durationEl)) {
			Integer duration = Integer.valueOf(durationEl.getValue());
			learningPathConfigs.setDuration(duration);
		}
		
		if (isEnabled(obligationEl) && obligationEl.isOneSelected() ) {
			AssessmentObligation obligation = AssessmentObligation.valueOf(obligationEl.getSelectedKey());
			if (learningPathConfigs.getAvailableObligations().contains(obligation)) {
				learningPathConfigs.setObligation(obligation);
			}
		}
	}

	private boolean isEnabled(FormItem item) {
		if (item == null) return false;
		
		return ((MultipleSelectionElement)item.getUserObject()).isAtLeastSelected(1);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
