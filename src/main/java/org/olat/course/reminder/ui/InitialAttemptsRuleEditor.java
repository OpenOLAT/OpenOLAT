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
package org.olat.course.reminder.ui;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.ArrayHelper;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.reminder.CourseNodeFragment;
import org.olat.course.reminder.rule.InitialAttemptsRuleSPI;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class InitialAttemptsRuleEditor extends RuleEditorFragment implements CourseNodeFragment {
	
	private static final String[] unitKeys = new String[]{
		LaunchUnit.day.name(), LaunchUnit.week.name(), LaunchUnit.month.name(), LaunchUnit.year.name()
	};
	
	private TextElement valueEl;
	private SingleSelection courseNodeEl, unitEl;
	private String[] nodeKeys;
	private String[] nodeValues;
	
	private final RepositoryEntry entry;

	public InitialAttemptsRuleEditor(ReminderRule rule, RepositoryEntry entry) {
		super(rule);
		this.entry = entry;
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		String page = Util.getPackageVelocityRoot(this.getClass()) + "/initial_attempts.html";
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		
		Translator trans = formLayout.getTranslator();
		FormLayoutContainer ruleCont = FormLayoutContainer
				.createCustomFormLayout("attempts.".concat(id), formLayout.getTranslator(), page);
		ruleCont.setRootForm(formLayout.getRootForm());
		formLayout.add(ruleCont);
		ruleCont.getFormItemComponent().contextPut("id", id);
		
		ICourse course = CourseFactory.loadCourse(entry);
		
		
		String currentValue = null;
		String currentUnit = null;
		String currentCourseNode = null;
		
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			currentUnit = r.getRightUnit();
			currentValue = r.getRightOperand();
			currentCourseNode = r.getLeftOperand();
		}
		
		List<CourseNode> attemptableNodes = new ArrayList<>();
		searchAttemptableNodes(course.getRunStructure().getRootNode(), attemptableNodes);
		searchAttemptableNodes(course.getEditorTreeModel().getRootNode(), attemptableNodes);
		
		nodeKeys = new String[attemptableNodes.size()];
		nodeValues = new String[attemptableNodes.size()];
		
		for(int i=0; i<attemptableNodes.size(); i++) {
			CourseNode attemptableNode = attemptableNodes.get(i);
			nodeKeys[i] = attemptableNode.getIdent();
			nodeValues[i] = attemptableNode.getShortTitle() + " ( " + attemptableNode.getIdent() + " )";
		}
		
		courseNodeEl = uifactory.addDropdownSingleselect("coursenodes.".concat(id), null, ruleCont, nodeKeys, nodeValues, null);
		courseNodeEl.setDomReplacementWrapperRequired(false);
		boolean nodeSelected = false;
		if(currentCourseNode != null) {
			for(String nodeKey:nodeKeys) {
				if(currentCourseNode.equals(nodeKey)) {
					courseNodeEl.select(nodeKey, true);
					nodeSelected = true;
				}
			}
		}
		if(!nodeSelected && nodeKeys.length > 0) {
			courseNodeEl.select(nodeKeys[0], true);
		}
		if(StringHelper.containsNonWhitespace(currentCourseNode) && !nodeSelected) {
			courseNodeEl.setErrorKey("error.course.node.found", null);
		}

		valueEl = uifactory.addTextElement("attemptvalue.".concat(id), null, 128, currentValue, ruleCont);
		valueEl.setDomReplacementWrapperRequired(false);
		valueEl.setDisplaySize(3);

		String[] unitValues = new String[] {
				trans.translate(LaunchUnit.day.name()), trans.translate(LaunchUnit.week.name()),
				trans.translate(LaunchUnit.month.name()), trans.translate(LaunchUnit.year.name())
		};

		unitEl = uifactory.addDropdownSingleselect("attemptunit.".concat(id), null, ruleCont, unitKeys, unitValues, null);
		unitEl.setDomReplacementWrapperRequired(false);
		boolean selected = false;
		if(currentUnit != null) {
			for(String unitKey:unitKeys) {
				if(currentUnit.equals(unitKey)) {
					unitEl.select(unitKey, true);
					selected = true;
				}
			}
		}
		if(!selected) {
			unitEl.select(unitKeys[1], true);	
		}
		
		return ruleCont;
	}
	
	private void searchAttemptableNodes(CourseNode courseNode, List<CourseNode> nodes) {
		addAttempteableNode(courseNode, nodes);
		
		for(int i=0; i<courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode)courseNode.getChildAt(i);
			searchAttemptableNodes(child, nodes);
		}
	}
	
	private void searchAttemptableNodes(TreeNode editorTreeNode, List<CourseNode> nodes) {
		if (editorTreeNode instanceof CourseEditorTreeNode) {
			CourseNode courseNode = ((CourseEditorTreeNode)editorTreeNode).getCourseNode();
			addAttempteableNode(courseNode, nodes);
		}
		
		for(int i=0; i<editorTreeNode.getChildCount(); i++) {
			TreeNode child = (TreeNode)editorTreeNode.getChildAt(i);
			searchAttemptableNodes(child, nodes);
		}
	}

	private void addAttempteableNode(CourseNode courseNode, List<CourseNode> nodes) {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(entry, courseNode);
		if (assessmentConfig.hasAttempts() && !nodes.contains(courseNode)) {
			nodes.add(courseNode);
		}
	}

	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = true;
		
		courseNodeEl.clearError();
		if(!courseNodeEl.isOneSelected()) {
			courseNodeEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		unitEl.clearError();
		if(!unitEl.isOneSelected()) {
			unitEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}
		
		valueEl.clearError();
		if(!StringHelper.containsNonWhitespace(valueEl.getValue())) {
			valueEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		} else {
			allOk &= validateInt(valueEl);
		}
		
		return allOk;
	}
	
	private boolean validateInt(TextElement el) {
		boolean allOk = true;
		
		if(el.isVisible()) {
			String value = el.getValue();
			if(StringHelper.containsNonWhitespace(value)) {
				try {
					Integer.parseInt(value);
				} catch(Exception e) {
					allOk = false;
					el.setErrorKey("error.wrong.int", null);
				}
			}
		}

		return allOk;
	}

	@Override
	public ReminderRule getConfiguration() {
		ReminderRuleImpl configuredRule = null; 
		if(courseNodeEl.isOneSelected() && unitEl.isOneSelected() && StringHelper.containsNonWhitespace(valueEl.getValue())) {
			configuredRule = new ReminderRuleImpl();
			configuredRule.setType(InitialAttemptsRuleSPI.class.getSimpleName());
			configuredRule.setLeftOperand(courseNodeEl.getSelectedKey());
			configuredRule.setOperator(">");
			configuredRule.setRightOperand(valueEl.getValue());
			configuredRule.setRightUnit(unitEl.getSelectedKey());
		}
		return configuredRule;
	}
	
	@Override
	public void limitSelection(String nodeIdent) {
		if (StringHelper.containsNonWhitespace(nodeIdent)) {
			int nodeIdentIndex = -1;
			for (int i = 0; i < nodeKeys.length; i++) {
				String key = nodeKeys[i];
				if (key.equals(nodeIdent)) {
					nodeIdentIndex = i;
				}
			}
			if (nodeIdentIndex >= 0) {
				String[] limitKeys = new String[] { nodeKeys[nodeIdentIndex] };
				String[] limitValues = new String[] { nodeValues[nodeIdentIndex] };
				courseNodeEl.setKeysAndValues(limitKeys, limitValues, null);
			} else {
				courseNodeEl.setKeysAndValues(ArrayHelper.emptyStrings(), ArrayHelper.emptyStrings(), null);
			}
		} else {
			courseNodeEl.setKeysAndValues(nodeKeys, nodeValues, null);
		}
	}

	@Override
	public void select(String nodeIdent) {
		if (Arrays.asList(courseNodeEl.getKeys()).contains(nodeIdent)) {
			courseNodeEl.select(nodeIdent, true);
		}
	}
	
}
