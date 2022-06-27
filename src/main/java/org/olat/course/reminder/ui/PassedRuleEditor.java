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
import java.util.Set;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.util.SelectionValues;
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
import org.olat.course.assessment.handler.AssessmentConfig.Mode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.reminder.CourseNodeFragment;
import org.olat.course.reminder.rule.PassedRuleSPI;
import org.olat.course.reminder.rule.PassedRuleSPI.Status;
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
public class PassedRuleEditor extends RuleEditorFragment implements CourseNodeFragment {
	
	private SingleSelection courseNodeEl;
	private MultipleSelectionElement statusEl;
	private String[] nodeKeys;
	private String[] nodeValues;
	
	private final RepositoryEntry entry;

	public PassedRuleEditor(ReminderRule rule, RepositoryEntry entry) {
		super(rule);
		this.entry = entry;
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		String page = Util.getPackageVelocityRoot(this.getClass()) + "/passed.html";
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		
		FormLayoutContainer ruleCont = FormLayoutContainer
				.createCustomFormLayout("attempts.".concat(id), formLayout.getTranslator(), page);
		ruleCont.setRootForm(formLayout.getRootForm());
		formLayout.add(ruleCont);
		ruleCont.getFormItemComponent().contextPut("id", id);
		
		ICourse course = CourseFactory.loadCourse(entry);

		String currentStatus = null;
		String currentCourseNode = null;
		if(rule instanceof ReminderRuleImpl) {
			ReminderRuleImpl r = (ReminderRuleImpl)rule;
			currentStatus = r.getRightOperand();
			currentCourseNode = r.getLeftOperand();
		}
		
		List<CourseNode> attemptableNodes = new ArrayList<>();
		searchPassedNodes(course.getRunStructure().getRootNode(), attemptableNodes);
		searchPassedNodes(course.getEditorTreeModel().getRootNode(), attemptableNodes);
		
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
		
		Translator trans = formLayout.getTranslator();
		SelectionValues statusSV = new SelectionValues();
		statusSV.add(SelectionValues.entry(PassedRuleSPI.Status.gradedPassed.name(), trans.translate("passed")));
		statusSV.add(SelectionValues.entry(PassedRuleSPI.Status.gradedFailed.name(), trans.translate("failed")));
		statusSV.add(SelectionValues.entry(PassedRuleSPI.Status.notGraded.name(), trans.translate("not.graded")));
		statusEl = uifactory.addCheckboxesHorizontal("status.".concat(id), null, ruleCont, statusSV.keys(), statusSV.values());
		statusEl.setDomReplacementWrapperRequired(false);
		Set<Status> status = Status.split(currentStatus);
		for (Status value : status) {
			if (statusEl.getKeys().contains(value.name())) {
				statusEl.select(value.name(), true);
				
			}
		}
		if(!statusEl.isAtLeastSelected(1)) {
			statusEl.select(PassedRuleSPI.Status.gradedFailed.name(), true);
			statusEl.select(PassedRuleSPI.Status.notGraded.name(), true);
		}
		
		return ruleCont;
	}
	
	private void searchPassedNodes(CourseNode courseNode, List<CourseNode> nodes) {
		addPassedNode(nodes, courseNode);
		
		for(int i=0; i<courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode)courseNode.getChildAt(i);
			searchPassedNodes(child, nodes);
		}
	}
	
	private void searchPassedNodes(TreeNode editorTreeNode, List<CourseNode> nodes) {
		if (editorTreeNode instanceof CourseEditorTreeNode) {
			CourseNode courseNode = ((CourseEditorTreeNode)editorTreeNode).getCourseNode();
			addPassedNode(nodes, courseNode);
		}

		
		for(int i=0; i<editorTreeNode.getChildCount(); i++) {
			TreeNode child = (TreeNode)editorTreeNode.getChildAt(i);
			searchPassedNodes(child, nodes);
		}
	}

	private void addPassedNode(List<CourseNode> nodes, CourseNode courseNode) {
		CourseAssessmentService courseAssessmentService = CoreSpringFactory.getImpl(CourseAssessmentService.class);
		AssessmentConfig assessmentConfig = courseAssessmentService.getAssessmentConfig(entry, courseNode);
		if (Mode.none != assessmentConfig.getPassedMode() && !nodes.contains(courseNode)) {
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
		
		statusEl.clearError();
		if(!statusEl.isAtLeastSelected(1)) {
			statusEl.setErrorKey("form.mandatory.hover", null);
			allOk &= false;
		}

		return allOk;
	}

	@Override
	public ReminderRule getConfiguration() {
		ReminderRuleImpl configuredRule = null; 
		if(courseNodeEl.isOneSelected() && statusEl.isAtLeastSelected(1)) {
			configuredRule = new ReminderRuleImpl();
			configuredRule.setType(PassedRuleSPI.class.getSimpleName());
			configuredRule.setLeftOperand(courseNodeEl.getSelectedKey());
			configuredRule.setOperator("=");
			Set<Status> status = statusEl.getSelectedKeys().stream().map(PassedRuleSPI.Status::valueOf).collect(java.util.stream.Collectors.toSet());
			String statusValues = PassedRuleSPI.Status.join(status);
			configuredRule.setRightOperand(statusValues);
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
