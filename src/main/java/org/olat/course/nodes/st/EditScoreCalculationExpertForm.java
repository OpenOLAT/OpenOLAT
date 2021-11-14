/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/

package org.olat.course.nodes.st;

/**
 * Initial Date:  22.03.04
 *
 * @author Felix Jost
 * 
 */

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.condition.Condition;
import org.olat.course.condition.interpreter.ConditionErrorMessage;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.editor.CourseEditorEnv;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.FailedEvaluationType;
import org.olat.course.run.scoring.ScoreCalculator;
import org.olat.course.run.userview.UserCourseEnvironment;
/**
 * @author guido
 * 
 */
class EditScoreCalculationExpertForm extends FormBasicController {
	private static final String[] EXAMPLE_PASSED = new String[]{"getPassed(\"69741247660309\")"};
	private static final String[] EXAMPLE_SCORE  = new String[]{"getScore(\"69741247660309\") * 2"};
	private TextElement tscoreexpr, tpassedexpr;
	private SingleSelection failedType;
	private UserCourseEnvironment euce;
	private ScoreCalculator sc;
	private List<CourseNode> assessableNodesList;
	private List<String> testElemWithNoResource = new ArrayList<>();
	
	private Translator pt = null;
	/**
	 * Constructor for a score calculation edit form 
	 * @param name
	 */
	public EditScoreCalculationExpertForm(UserRequest ureq, WindowControl wControl, ScoreCalculator sc, UserCourseEnvironment euce, List<CourseNode> assessableNodesList) {
		super(ureq, wControl);
		this.sc = sc;
		this.euce = euce;
		this.assessableNodesList = assessableNodesList;
	
		initForm(ureq);
	}
	
	
	private void setKeys (UserRequest ureq, FormItem fi, ConditionErrorMessage[] cem) {
		
		if (pt == null) {
			pt = Util.createPackageTranslator(Condition.class, ureq.getLocale());
		}
		
		//the error message
		fi.setErrorKey("rules.error", new String[]{
				pt.translate(cem[0].getErrorKey(), cem[0].getErrorKeyParams())
		});
		
		if (cem[0].getSolutionMsgKey() != null && !"".equals(cem[0].getSolutionMsgKey())) {
			//and a hint or example to clarify the error message
			fi.setExampleKey("rules.error", new String[]{
					pt.translate(cem[0].getSolutionMsgKey(), cem[0].getErrorKeyParams())
			});
		}
	}

	@Override
	public boolean validateFormLogic (UserRequest ureq) {
		String scoreExp = tscoreexpr.getValue().trim();
		if (StringHelper.containsNonWhitespace(scoreExp)) {		
			
			CourseEditorEnv cev = euce.getCourseEditorEnv();
			ConditionExpression ce = new ConditionExpression("score",scoreExp);
			ConditionErrorMessage[] cerrmsgs = cev.validateConditionExpression(ce);
			
			if (cerrmsgs != null && cerrmsgs.length>0) {
				setKeys (ureq, tscoreexpr, cerrmsgs);
				return false;
			}			
			testElemWithNoResource = getInvalidNodeDescriptions(ce);						
		}

		String passedExp = tpassedexpr.getValue().trim();
		if (StringHelper.containsNonWhitespace(passedExp)) {
			
			CourseEditorEnv cev = euce.getCourseEditorEnv();
			ConditionExpression ce = new ConditionExpression("passed",passedExp);
			ConditionErrorMessage[] cerrmsgs = cev.validateConditionExpression(ce);
			
			if (cerrmsgs != null && cerrmsgs.length>0) {
				setKeys (ureq, tpassedexpr,  cerrmsgs);
				return false;
			}
		}
		
		//reset HINTS
		tscoreexpr.setExampleKey("rules.example", EXAMPLE_SCORE);
		tpassedexpr.setExampleKey("rules.example", EXAMPLE_PASSED);						
		return true;
	}

	/**
	 * @param sc
	 */
	public void setScoreCalculator(ScoreCalculator sc) {
		this.sc = sc;
		tscoreexpr.setValue(sc == null? "" : sc.getScoreExpression());
		tpassedexpr.setValue(sc == null? "" : sc.getPassedExpression());
	}
	
	/**
	 * @return ScoreCalcualtor
	 */
	public ScoreCalculator getScoreCalulator() {
		String scoreExp = tscoreexpr.getValue().trim();
		String passedExp = tpassedexpr.getValue().trim();
		if (scoreExp.equals("") && passedExp.equals("")) return null;
		if (passedExp.equals("")) passedExp = null;
		if (scoreExp.equals("")) scoreExp= null;
		
		sc.setScoreExpression(scoreExp);
		sc.setPassedExpression(passedExp);
		sc.setFailedType(FailedEvaluationType.valueOf(failedType.getSelectedKey()));
		sc.setExpertMode(true);
		return sc;
	}
	
	/**
	 * Get the list with the node description of the "invalid" nodes.
	 * The "invalid" nodes are not associated with any test resource so they are actually not assessable.
	 * @param ce
	 * @return
	 */
	private List<String> getInvalidNodeDescriptions(ConditionExpression ce) {
		List<String> nodeDescriptionList = new ArrayList<>();
		if (ce != null) {
			Set<String> selectedNodesIds = ce.getSoftReferencesOf("courseNodeId");
			for (Iterator<CourseNode> nodeIter = assessableNodesList.iterator(); nodeIter.hasNext();) {
				CourseNode node = nodeIter.next();
				if (selectedNodesIds.contains(node.getIdent())) {
					StatusDescription isConfigValid = node.isConfigValid();
					if (isConfigValid != null && isConfigValid.isError()) {
						String nodeDescription = node.getShortName() + " (Id:" + node.getIdent() + ")";
						if (!nodeDescriptionList.contains(nodeDescription)) {
							nodeDescriptionList.add(nodeDescription);
						}
					}
				}
			}
		}
		return nodeDescriptionList;
	}
	
	public List<String> getInvalidNodeDescriptions() {
		return testElemWithNoResource;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent (ureq, Event.DONE_EVENT);
	}
	
	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent (ureq, Event.CANCELLED_EVENT);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		tscoreexpr = uifactory.addTextAreaElement("tscoreexpr", "scorecalc.score", 5000, 6, 45, true, false, sc.getScoreExpression(), formLayout);
		tscoreexpr.setExampleKey("rules.example", EXAMPLE_SCORE);
		
		tpassedexpr = uifactory.addTextAreaElement("tpassedexpr", "scorecalc.passed", 5000, 6, 45, true, false, sc.getPassedExpression(), formLayout);
		tpassedexpr.setExampleKey("rules.example", EXAMPLE_PASSED);

		String[] failedTypeKeys = new String[]{
				FailedEvaluationType.failedAsNotPassed.name(),
				FailedEvaluationType.failedAsNotPassedAfterEndDate.name()
		};
		String[] failedTypeValues = new String[]{
				translate(FailedEvaluationType.failedAsNotPassed.name()),
				translate(FailedEvaluationType.failedAsNotPassedAfterEndDate.name())
		};
		
		failedType = uifactory.addDropdownSingleselect("scform.failedtype", formLayout, failedTypeKeys, failedTypeValues, null);
		failedType.addActionListener(FormEvent.ONCLICK);
		FailedEvaluationType failedTypeValue = sc.getFailedType() == null ? FailedEvaluationType.failedAsNotPassed : sc.getFailedType();
		boolean failedSelected = false;
		for(String failedTypeKey:failedTypeKeys) {
			if(failedTypeKey.equals(failedTypeValue.name())) {
				failedType.select(failedTypeKey, true);
				failedSelected = true;
			}
		}
		if(!failedSelected) {
			failedType.select(failedTypeKeys[0], true);
		}
		
		
		// Button layout
		final FormLayoutContainer buttonLayout = FormLayoutContainer.createButtonLayout("button_layout", getTranslator());
		formLayout.add(buttonLayout);
		uifactory.addFormSubmitButton("submit", buttonLayout);
		uifactory.addFormCancelButton("cancel", buttonLayout, ureq, getWindowControl());
	}
}