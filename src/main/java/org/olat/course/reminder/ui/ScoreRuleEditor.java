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
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.reminder.CourseNodeFragment;
import org.olat.course.reminder.rule.ScoreRuleSPI;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 09.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ScoreRuleEditor extends RuleEditorFragment implements CourseNodeFragment {
	
	private static final String[] operatorKeys = new String[]{ "<", "<=", "=", "=>", ">", "!=" };
	
	private TextElement valueEl;
	private SingleSelection courseNodeEl, operatorEl;
	
	private final RepositoryEntry entry;
	
	public ScoreRuleEditor(ReminderRule rule, RepositoryEntry entry) {
		super(rule);
		this.entry = entry;
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		String page = Util.getPackageVelocityRoot(this.getClass()) + "/score.html";
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		
		FormLayoutContainer ruleCont = FormLayoutContainer
				.createCustomFormLayout("attempts.".concat(id), formLayout.getTranslator(), page);
		ruleCont.setRootForm(formLayout.getRootForm());
		formLayout.add(ruleCont);
		ruleCont.getFormItemComponent().contextPut("id", id);
		
		ICourse course = CourseFactory.loadCourse(entry);
		
		
		String currentValue = null;
		String currentOperator = null;
		String currentCourseNode = null;
		
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			currentOperator = r.getOperator();
			currentValue = r.getRightOperand();
			currentCourseNode = r.getLeftOperand();
		}
		
		List<CourseNode> attemptableNodes = new ArrayList<>();
		searchScoreableNodes(course.getRunStructure().getRootNode(), attemptableNodes);
		searchScoreableNodes(course.getEditorTreeModel().getRootNode(), attemptableNodes);
		
		String[] nodeKeys = new String[attemptableNodes.size()];
		String[] nodeValues = new String[attemptableNodes.size()];
		
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

		operatorEl = uifactory.addDropdownSingleselect("operators.".concat(id), null, ruleCont, operatorKeys, operatorKeys, null);
		operatorEl.setDomReplacementWrapperRequired(false);
		boolean opSelected = false;
		if(currentOperator != null) {
			for(String operatorKey:operatorKeys) {
				if(currentOperator.equals(operatorKey)) {
					operatorEl.select(operatorKey, true);
					opSelected = true;
				}
			}
		}
		if(!opSelected) {
			operatorEl.select(operatorKeys[2], true);
		}

		valueEl = uifactory.addTextElement("value.".concat(id), null, 128, currentValue, ruleCont);
		valueEl.setDomReplacementWrapperRequired(false);
		valueEl.setDisplaySize(3);
		
		return ruleCont;
	}
	
	private void searchScoreableNodes(CourseNode courseNode, List<CourseNode> nodes) {
		addScoreableNode(nodes, courseNode);
		
		for(int i=0; i<courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode)courseNode.getChildAt(i);
			searchScoreableNodes(child, nodes);
		}
	}
	
	private void searchScoreableNodes(TreeNode editorTreeNode, List<CourseNode> nodes) {
		if (editorTreeNode instanceof CourseEditorTreeNode) {
			CourseNode courseNode = ((CourseEditorTreeNode)editorTreeNode).getCourseNode();
			addScoreableNode(nodes, courseNode);
		}
		
		for(int i=0; i<editorTreeNode.getChildCount(); i++) {
			TreeNode child = (TreeNode)editorTreeNode.getChildAt(i);
			searchScoreableNodes(child, nodes);
		}
	}

	private void addScoreableNode(List<CourseNode> nodes, CourseNode courseNode) {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(courseNode);
		if (Mode.none != assessmentConfig.getScoreMode() && !nodes.contains(courseNode)) {
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
		
		operatorEl.clearError();
		if(!operatorEl.isOneSelected()) {
			operatorEl.setErrorKey("form.mandatory.hover", null);
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
		if(courseNodeEl.isOneSelected() && operatorEl.isOneSelected() && StringHelper.containsNonWhitespace(valueEl.getValue())) {
			configuredRule = new ReminderRuleImpl();
			configuredRule.setType(ScoreRuleSPI.class.getSimpleName());
			configuredRule.setLeftOperand(courseNodeEl.getSelectedKey());
			configuredRule.setOperator(operatorEl.getSelectedKey());
			configuredRule.setRightOperand(valueEl.getValue());
		}
		return configuredRule;
	}

	@Override
	public void setCourseNodeIdent(String nodeIdent) {
		if (Arrays.asList(courseNodeEl.getKeys()).contains(nodeIdent)) {
			courseNodeEl.select(nodeIdent, true);
		}
	}
	
}
