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
package org.olat.ims.qti21.ui.assessment;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.CorrectionManagerImpl;
import org.olat.ims.qti21.model.ParentPartItemRefs;
import org.olat.ims.qti21.model.xml.QtiNodesExtractor;
import org.olat.ims.qti21.ui.assessment.model.CorrectionAssessmentItemRow;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;

/**
 * 
 * Initial date: 10 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BulkScoreController extends FormBasicController {
	
	private TextElement pointsEl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private CorrectionManagerImpl correctionManager;
	
	private final Mode mode;
	private final Double minScore;
	private final Double maxScore;
	private final AssessmentItem assessmentItem;
	private final CorrectionOverviewModel model;
	private final CorrectionAssessmentItemRow itemRow;
	
	public BulkScoreController(UserRequest ureq, WindowControl wControl,
			CorrectionOverviewModel model, CorrectionAssessmentItemRow itemRow, Mode mode) {
		super(ureq, wControl);
		this.mode = mode;
		this.model = model;
		this.itemRow = itemRow;
		assessmentItem = itemRow.getItem();
		minScore = QtiNodesExtractor.extractMinScore(assessmentItem);
		maxScore = QtiNodesExtractor.extractMaxScore(assessmentItem);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		String questionTitle = assessmentItem.getTitle();
		String infoI18nKey = (mode == Mode.ADD) ? "point.add.info" : "point.set.info";
		setFormInfo(infoI18nKey, new String[] { questionTitle } );
		
		String i18nKey = (mode == Mode.ADD) ? "points.to.add" : "points.to.set";
		pointsEl = uifactory.addTextElement("points", i18nKey, 8, null, formLayout);
		if(minScore != null && maxScore != null) {
			pointsEl.setExampleKey("correction.min.max.score",
				new String[] { AssessmentHelper.getRoundedScore(minScore), AssessmentHelper.getRoundedScore(maxScore) });
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		String submitI18nKey = (mode == Mode.ADD) ? "point.add" : "point.set";
		uifactory.addFormSubmitButton("apply", submitI18nKey, buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);

		pointsEl.clearError();
		if(StringHelper.containsNonWhitespace(pointsEl.getValue())) {
			try {
				// check with the parse algorithm of BigDecimal first
				new BigDecimal(pointsEl.getValue()).doubleValue();
				double score = Double.parseDouble(pointsEl.getValue());
				if(mode == Mode.ADD && score < 0.000001d) {
					allOk &= false;
					pointsEl.setErrorKey("correction.min.max.score.zero", null);
				} else if((minScore != null && score < minScore.doubleValue())
						|| (maxScore != null && score > maxScore.doubleValue())) {
					allOk &= false;
					pointsEl.setErrorKey("correction.min.max.score",
							new String[] { AssessmentHelper.getRoundedScore(minScore),  AssessmentHelper.getRoundedScore(maxScore) });
				}
			} catch (NumberFormatException e) {
				logWarn("Cannot parse the score: " + pointsEl.getValue(), null);
				pointsEl.setErrorKey("error.double.format", null);
				allOk &= false;
			}
		}
		
		return allOk;
	}
	
	private BigDecimal getPoints() {
		if (StringHelper.containsNonWhitespace(pointsEl.getValue())) {
			String mScore = pointsEl.getValue();
			if(mScore.indexOf(',') >= 0) {
				mScore = mScore.replace(",", ".");
			}
			return new BigDecimal(mScore);
		}
		return null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		doAddPoints();
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doAddPoints() {
		BigDecimal points = getPoints();
		if(points == null) return;
		
		AssessmentItemRef itemRef = itemRow.getItemRef();
		List<Identity> assessedIdentities = model.getAssessedIdentities();
		for(Identity assessedIdentity: assessedIdentities) {
			TestSessionState testSessionState = model.getTestSessionStates().get(assessedIdentity);
			AssessmentTestSession candidateSession = model.getLastSessions().get(assessedIdentity);
			if(candidateSession != null && testSessionState != null) {
				List<TestPlanNode> nodes = testSessionState.getTestPlan().getNodes(itemRef.getIdentifier());
				if(nodes.size() == 1) {
					addPoints(assessedIdentity, candidateSession, testSessionState, nodes.get(0), points);
					dbInstance.commit();
				}
			}
		}
	}
	
	private void addPoints(Identity assessedIdentity, AssessmentTestSession candidateSession,
			TestSessionState testSessionState, TestPlanNode itemNode, BigDecimal score) {

		ResolvedAssessmentTest resolvedAssessmentTest = model.getResolvedAssessmentTest();
		try(AssessmentSessionAuditLogger candidateAuditLogger = qtiService.getAssessmentSessionAuditLogger(candidateSession, false)) {
		
			TestPlanNodeKey testPlanNodeKey = itemNode.getKey();
			String stringuifiedIdentifier = testPlanNodeKey.getIdentifier().toString();
			
			ParentPartItemRefs parentParts = AssessmentTestHelper
					.getParentSection(testPlanNodeKey, testSessionState, resolvedAssessmentTest);
			AssessmentItemSession itemSession = qtiService
					.getOrCreateAssessmentItemSession(candidateSession, parentParts, stringuifiedIdentifier);

			evaluateScore(itemSession, score);
			
			itemSession = qtiService.updateAssessmentItemSession(itemSession);
			
			candidateAuditLogger.logCorrection(candidateSession, itemSession, getIdentity());
			
			candidateSession = qtiService.recalculateAssessmentTestSessionScores(candidateSession.getKey());
			model.updateLastSession(assessedIdentity, candidateSession);
			
			if(model.getCourseNode() != null && model.getCourseEnvironment() != null) {
				AssessmentTest assessmentTest = resolvedAssessmentTest.getRootNodeLookup().extractIfSuccessful();
				correctionManager.updateCourseNode(candidateSession, assessmentTest,
						model.getCourseNode(), model.getCourseEnvironment(), getIdentity());
			}
		} catch(IOException e) {
			logError("", e);
		}
	}
	
	private void evaluateScore(AssessmentItemSession itemSession, BigDecimal points) {
		if(mode == Mode.ADD) {
			BigDecimal currentScore = itemSession.getManualScore();
			if(currentScore == null) {
				currentScore = itemSession.getScore();
			}
			if(currentScore != null) {
				BigDecimal endScore = currentScore.add(points);
				if(maxScore != null && maxScore.doubleValue() < endScore.doubleValue()) {
					endScore = BigDecimal.valueOf(maxScore);
				}
				itemSession.setManualScore(endScore);
			} else if(points.doubleValue() > 0.0d) {
				itemSession.setManualScore(points);
			}
		} else if(mode == Mode.SET) {
			itemSession.setManualScore(points);
		}
	}
	
	public enum Mode {
		ADD,
		SET
	}
}
