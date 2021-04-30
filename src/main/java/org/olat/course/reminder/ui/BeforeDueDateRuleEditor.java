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
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.reminder.ReminderRule;
import org.olat.modules.reminder.RuleEditorFragment;
import org.olat.modules.reminder.model.ReminderRuleImpl;
import org.olat.modules.reminder.rule.LaunchUnit;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 10.04.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class BeforeDueDateRuleEditor extends RuleEditorFragment {
	
	private static final String[] unitKeys = new String[]{
		LaunchUnit.day.name(), LaunchUnit.week.name(), LaunchUnit.month.name(), LaunchUnit.year.name()
	};
	
	private TextElement valueEl;
	private SingleSelection courseNodeEl, unitEl;
	
	protected final String ruleType;
	private final RepositoryEntry entry;
	
	public BeforeDueDateRuleEditor(ReminderRule rule, RepositoryEntry entry, String ruleType) {
		super(rule);
		this.entry = entry;
		this.ruleType = ruleType;
	}

	@Override
	public FormItem initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String page = Util.getPackageVelocityRoot(BeforeDueDateRuleEditor.class) + "/date_rule.html";
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		
		Translator trans = formLayout.getTranslator();
		FormLayoutContainer ruleCont = FormLayoutContainer
				.createCustomFormLayout("taks.".concat(id), trans, page);
		ruleCont.setRootForm(formLayout.getRootForm());
		formLayout.add(ruleCont);
		
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
		searchNodesWithDeadlines(course.getRunStructure().getRootNode(), attemptableNodes);
		
		String[] nodeKeys = new String[attemptableNodes.size()];
		String[] nodeValues = new String[attemptableNodes.size()];
		
		for(int i=0; i<attemptableNodes.size(); i++) {
			CourseNode attemptableNode = attemptableNodes.get(i);
			nodeKeys[i] = attemptableNode.getIdent();
			nodeValues[i] = attemptableNode.getShortTitle() + " ( " + attemptableNode.getIdent() + " )";
		}
		
		courseNodeEl = uifactory.addDropdownSingleselect("coursenodes", null, ruleCont, nodeKeys, nodeValues, null);
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

		valueEl = uifactory.addTextElement("value", null, 128, currentValue, ruleCont);
		valueEl.setDomReplacementWrapperRequired(false);
		valueEl.setDisplaySize(3);

		String[] unitValues = new String[] {
				trans.translate(LaunchUnit.day.name()), trans.translate(LaunchUnit.week.name()),
				trans.translate(LaunchUnit.month.name()), trans.translate(LaunchUnit.year.name())
		};

		unitEl = uifactory.addDropdownSingleselect("unit", null, ruleCont, unitKeys, unitValues, null);
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
	
	private void searchNodesWithDeadlines(CourseNode courseNode, List<CourseNode> nodes) {
		if(isNodeWithDeadline(courseNode)) {
			nodes.add(courseNode);
		}
		
		for(int i=0; i<courseNode.getChildCount(); i++) {
			CourseNode child = (CourseNode)courseNode.getChildAt(i);
			searchNodesWithDeadlines(child, nodes);
		}
	}

	public abstract boolean isNodeWithDeadline(CourseNode courseNode);

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
			configuredRule.setType(ruleType);
			configuredRule.setLeftOperand(courseNodeEl.getSelectedKey());
			configuredRule.setOperator("<");
			configuredRule.setRightOperand(valueEl.getValue());
			configuredRule.setRightUnit(unitEl.getSelectedKey());
		}
		return configuredRule;
	}
}