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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.IntegerElement;
import org.olat.core.gui.components.form.flexible.elements.MultipleSelectionElement;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.course.editor.StatusDescription;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.FailedEvaluationType;
import org.olat.course.run.scoring.ScoreCalculator;

/**
 * Description:<br>
 * This form is used to generate score and passed expression for structure
 * course nodes in an easy way. See EditScoreCalculationExpertForm for the
 * expert way.
 * <p>
 * 
 * @author gnaegi
 */
public class EditScoreCalculationEasyForm extends FormBasicController {

	private MultipleSelectionElement hasScore, hasPassed;
	private SingleSelection scoreType, passedType, failedType;
	private MultipleSelectionElement scoreNodeIdents, passedNodeIdents;
	private IntegerElement passedCutValue;
	private ScoreCalculator sc;
	
	private static final String DELETED_NODE_IDENTIFYER = "deletedNode";
	private List<CourseNode> assessableNodesList;
	private List<CourseNode> nodeIdentList;
	
  /**
	 * @param name
	 * @param trans
	 * @param scoreCalculator
	 * @param nodeIdentList
	 */
	public EditScoreCalculationEasyForm(UserRequest ureq, WindowControl wControl, ScoreCalculator scoreCalculator, List<CourseNode> nodeIdentList) {
		super(ureq, wControl);
			
		sc = scoreCalculator;
		this.assessableNodesList = nodeIdentList;
		this.nodeIdentList = nodeIdentList;

		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		hasScore = uifactory.addCheckboxesHorizontal("scform.hasScore", formLayout, new String[]{"xx"}, new String[]{null});
		hasScore.select("xx", sc != null && sc.getSumOfScoreNodes() != null && sc.getSumOfScoreNodes().size() > 0);
		hasScore.addActionListener(FormEvent.ONCLICK);
		hasScore.setElementCssClass("o_sel_has_score");
		
		String[] scoreTypeKeys = new String[] {
				ScoreCalculator.SCORE_TYPE_SUM,
				ScoreCalculator.SCORE_TYPE_AVG
		};
		String[] scoreTypeValues = new String[] {
				translate("scform.scoretype.sum"),
				translate("scform.scoretype.avg")
		};
			
		scoreType = uifactory.addRadiosHorizontal("scoreType", null, formLayout, scoreTypeKeys, scoreTypeValues);
		scoreType.setVisible(hasScore.isSelected(0));
		if (sc != null && sc.getScoreType() != null && !sc.getScoreType().equals(ScoreCalculator.SCORE_TYPE_NONE)) {
			scoreType.select(sc.getScoreType(), true);
		} else {
			scoreType.select(ScoreCalculator.SCORE_TYPE_SUM, true);
		}
		scoreType.addActionListener(FormEvent.ONCLICK);
		
		List<String> sumOfScoreNodes = (sc == null ? null : sc.getSumOfScoreNodes());
		scoreNodeIdents = initNodeSelectionElement(formLayout, "scform.scoreNodeIndents", sc, sumOfScoreNodes, nodeIdentList);
		scoreNodeIdents.setVisible(hasScore.isSelected(0));

		uifactory.addSpacerElement("spacer", formLayout, false);
		
		hasPassed = uifactory.addCheckboxesHorizontal("scform.passedtype", formLayout, new String[]{"xx"}, new String[]{null});
		hasPassed.select("xx", sc != null && sc.getPassedType() != null && !sc.getPassedType().equals(ScoreCalculator.PASSED_TYPE_NONE));
		hasPassed.addActionListener(FormEvent.ONCLICK); // Radios/Checkboxes need onclick because of IE bug OLAT-5753
		hasPassed.setElementCssClass("o_sel_has_passed");
		
		String[] passedTypeKeys = new String[] {
				ScoreCalculator.PASSED_TYPE_CUTVALUE,
				ScoreCalculator.PASSED_TYPE_INHERIT
		};
		String[] passedTypeValues = new String[] {
				translate("scform.passedtype.cutvalue"),
				translate("scform.passedtype.inherit")
		};
			
		passedType = uifactory.addRadiosVertical("passedType", null, formLayout, passedTypeKeys, passedTypeValues);
		passedType.setVisible(hasPassed.isSelected(0));
		if (sc != null && sc.getPassedType() != null && !sc.getPassedType().equals(ScoreCalculator.PASSED_TYPE_NONE)) {
			passedType.select(sc.getPassedType(), true);
		} else {
			passedType.select(ScoreCalculator.PASSED_TYPE_CUTVALUE, true);
		}
		passedType.addActionListener(FormEvent.ONCLICK);
		
		int cutinitval = 0;
		if (sc != null) cutinitval = sc.getPassedCutValue();
		passedCutValue = uifactory.addIntegerElement("scform.passedCutValue", cutinitval, formLayout);
		passedCutValue.setDisplaySize(4);
		passedCutValue.setVisible(passedType.isVisible() && passedType.isSelected(0));
		passedCutValue.setMandatory(true);
				
		passedNodeIdents = initNodeSelectionElement(
				formLayout, "scform.passedNodeIndents", sc, (sc == null ? null : sc.getPassedNodes()), nodeIdentList
		);
		passedNodeIdents.setVisible(passedType.isVisible() && passedType.isSelected(1));
		
		String[] failedTypeKeys = new String[]{
				FailedEvaluationType.failedAsNotPassed.name(),
				FailedEvaluationType.failedAsNotPassedAfterEndDate.name()
		};
		String[] failedTypeValues = new String[]{
				translate(FailedEvaluationType.failedAsNotPassed.name()),
				translate(FailedEvaluationType.failedAsNotPassedAfterEndDate.name())
		};
		
		failedType = uifactory.addDropdownSingleselect("scform.failedtype", formLayout, failedTypeKeys, failedTypeValues, null);
		failedType.setVisible(failedType.isSelected(0));
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
		
		FormLayoutContainer buttonGroupLayout = FormLayoutContainer.createButtonLayout("buttonGroupLayout", getTranslator());
		formLayout.add(buttonGroupLayout);
		uifactory.addFormSubmitButton("submit", buttonGroupLayout);
		uifactory.addFormCancelButton("cancel", buttonGroupLayout, ureq, getWindowControl());
	}
	
	/**
	 * Initializes the node selection form elements first check if the form has a
	 * selection on a node that has been deleted in since the last edition of this
	 * form. if so, set remember this to later add a dummy placeholder for the
	 * deleted node. We do not just ignore this since the form would look ok then
	 * to the user, the generated rule visible in the expert mode however would
	 * still be invalid. user must explicitly uncheck this deleted node reference.
	 * 
	 * @param elemId name of the generated form element
	 * @param scoreCalculator
	 * @param selectedNodeList List of course nodes that are preselected
	 * @param allNodesList List of all assessable course nodes
	 * @return StaticMultipleSelectionElement The configured form element
	 */
	private MultipleSelectionElement initNodeSelectionElement(FormItemContainer formLayout, String elemId, ScoreCalculator scoreCalculator,
			List<String> selectedNodeList, List<CourseNode> allNodesList) {
		
		boolean addDeletedNodeIdent = false;		
		if (scoreCalculator != null && selectedNodeList != null) {
			for (Iterator<String> iter = selectedNodeList.iterator(); iter.hasNext();) {
				String nodeIdent = iter.next();
				boolean found = false;
				for (Iterator<CourseNode> nodeIter = allNodesList.iterator(); nodeIter.hasNext();) {
					CourseNode node = nodeIter.next();
					if (node.getIdent().equals(nodeIdent)) {
						found = true;           
					}					
				}
				if (!found) addDeletedNodeIdent = true;
			}
		}

		String[] nodeKeys = new String[allNodesList.size() + (addDeletedNodeIdent ? 1 : 0)];
		String[] nodeValues = new String[allNodesList.size() + (addDeletedNodeIdent ? 1 : 0)];
		for (int i = 0; i < allNodesList.size(); i++) {
			CourseNode courseNode = allNodesList.get(i);
			nodeKeys[i] = courseNode.getIdent();
			nodeValues[i] = courseNode.getShortName() + " (Id:" + courseNode.getIdent() + ")";
		}
		// add a deleted dummy node at last position
		if (addDeletedNodeIdent) {
			nodeKeys[allNodesList.size()] = DELETED_NODE_IDENTIFYER;
			nodeValues[allNodesList.size()] = translate("scform.deletedNode");
		}
		
		MultipleSelectionElement mse = uifactory.addCheckboxesVertical(elemId, formLayout, nodeKeys, nodeValues, 2);
		// preselect nodes from configuration
		if (scoreCalculator != null && selectedNodeList != null) {
			for (Iterator<String> iter = selectedNodeList.iterator(); iter.hasNext();) {
				String nodeIdent = iter.next();
				boolean found = false;
				for (Iterator<CourseNode> nodeIter = allNodesList.iterator(); nodeIter.hasNext();) {
					CourseNode node = nodeIter.next();
					if (node.getIdent().equals(nodeIdent)) {
						found = true;
					}
				}
				if (found) {
					mse.select(nodeIdent, true);
				} else {
					mse.select(DELETED_NODE_IDENTIFYER, true);
				}
			}
		}		
		return mse;
	}

	@Override
	protected void doDispose() {
		//
	}

	/**
	 * @see org.olat.core.gui.components.Form#validate(org.olat.core.gui.UserRequest, Identity)
	 */
	@Override
	public boolean validateFormLogic(UserRequest ureq) {
		boolean rv = true;
		if (hasScore.isSelected(0)) {
			if (scoreNodeIdents.getSelectedKeys().size() == 0) {
				scoreNodeIdents.setErrorKey("scform.scoreNodeIndents.error", null);
				rv = false;
			} else if (scoreNodeIdents.getSelectedKeys().contains(DELETED_NODE_IDENTIFYER)) {
				scoreNodeIdents.setErrorKey("scform.deletedNode.error", null);
				rv = false;
			} else {
				scoreNodeIdents.clearError();
			}
		}
		
		if (hasPassed.isSelected(0)) {
			if (passedType.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_INHERIT)) {
				if (passedNodeIdents.getSelectedKeys().size() == 0) {
					passedNodeIdents.setErrorKey("scform.passedNodeIndents.error", null);
					rv = false;
				} else {
					passedNodeIdents.clearError();
				}
			} else if (passedType.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_CUTVALUE)) {
				if (!hasScore.isSelected(0)) {
					passedType.setErrorKey("scform.passedType.error", null);
					rv = false;
				} else {
					passedType.clearError();
				}
			}
		}
	
		return rv;
	}
	
	private void updateUI() {
		scoreType.setVisible(hasScore.isSelected(0));
		scoreNodeIdents.setVisible(hasScore.isSelected(0));
		if (!scoreNodeIdents.isVisible()) {
			scoreNodeIdents.clearError();
		}
		passedType.setVisible(hasPassed.isSelected(0));
		failedType.setVisible(hasPassed.isSelected(0));
		
		passedCutValue.setVisible(passedType.isVisible() && passedType.isSelected(0));
		if (!passedCutValue.isVisible()) {
			passedCutValue.setIntValue(0);
			passedCutValue.clearError();
		}
		passedNodeIdents.setVisible(passedType.isVisible() && passedType.isSelected(1));
		if (!passedNodeIdents.isVisible()) {
			passedNodeIdents.clearError();
		}
	}

	/**
	 * @return ScoreCalcualtor or null if no score calculator is set
	 */
	public ScoreCalculator getScoreCalulator() {
		if (!hasScore.isSelected(0) && !hasPassed.isSelected(0)) {
			return null;
		}

		// 1) score configuration
		if (hasScore.isSelected(0)) {
			String scoreTypeSelection = scoreType.isOneSelected()
					? scoreType.getSelectedKey()
					: ScoreCalculator.SCORE_TYPE_SUM;
			sc.setScoreType(scoreTypeSelection);
			sc.setSumOfScoreNodes(new ArrayList<>(scoreNodeIdents.getSelectedKeys()));
		} else {
			//reset
			sc.setScoreType(ScoreCalculator.SCORE_TYPE_NONE);
			sc.setSumOfScoreNodes(null);
		}
		
		// 2) passed configuration
		if (!hasPassed.isSelected(0)) {
			sc.setPassedType(ScoreCalculator.PASSED_TYPE_NONE);
		} else if (passedType.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_CUTVALUE)) {
			sc.setPassedType(ScoreCalculator.PASSED_TYPE_CUTVALUE);
			sc.setPassedCutValue(passedCutValue.getIntValue());
		} else if (passedType.getSelectedKey().equals(ScoreCalculator.PASSED_TYPE_INHERIT)) {
			sc.setPassedType(ScoreCalculator.PASSED_TYPE_INHERIT);
			sc.setPassedNodes(new ArrayList<>(passedNodeIdents.getSelectedKeys()));
		}

		// update score and passed expression from easy mode configuration
		sc.setScoreExpression(sc.getScoreExpressionFromEasyModeConfiguration());
		sc.setPassedExpression(sc.getPassedExpressionFromEasyModeConfiguration());
		sc.setFailedType(FailedEvaluationType.valueOf(failedType.getSelectedKey()));

		if (sc.getScoreExpression() == null && sc.getPassedExpression() == null) {
			return null;
		}
		sc.setExpertMode(false);
		return sc;
	}
	
	public boolean hasScore (){
		return hasScore.isSelected(0);
	}
	
	/**
	 *   
	 * @return Returns a list with the invalid node descriptions, 
	 * 				("invalid" is a node that is not associated with a test resource)
	 */
	public List<String> getInvalidNodeDescriptions() {
		List<String> testElemWithNoResource = new ArrayList<>();
		List<String> selectedNodesIds = new ArrayList<>(scoreNodeIdents.getSelectedKeys());		
		for (Iterator<CourseNode> nodeIter = assessableNodesList.iterator(); nodeIter.hasNext();) {
			CourseNode node = nodeIter.next();
			if (selectedNodesIds.contains(node.getIdent())) {				
				StatusDescription isConfigValid = node.isConfigValid();
				if (isConfigValid != null && isConfigValid.isError()) {
					String nodeDescription = node.getShortName() + " (Id:" + node.getIdent() + ")";
					if (!testElemWithNoResource.contains(nodeDescription)) {
						testElemWithNoResource.add(nodeDescription);
					}
				}
			}
		}
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
	protected void formInnerEvent (UserRequest ureq, FormItem source, FormEvent event) {
		if (source == passedType) {
			passedType.clearError();
		}
		updateUI();
	}
}
