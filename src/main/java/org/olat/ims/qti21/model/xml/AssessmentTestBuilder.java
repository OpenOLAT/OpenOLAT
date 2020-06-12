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
package org.olat.ims.qti21.model.xml;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;

import uk.ac.ed.ph.jqtiplus.node.expression.Expression;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.expression.outcome.TestVariables;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedback;
import uk.ac.ed.ph.jqtiplus.node.test.TimeLimits;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeCondition;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeProcessing;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeRule;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;

/**
 * This builder only build OpenOLAT QTI tests.
 * 
 * 
 * Initial date: 11.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestBuilder {
	
	private final AssessmentTest assessmentTest;
	private final AssessmentHtmlBuilder htmlBuilder;
	
	private final boolean editable;
	
	private Double cutValue;
	private Double maxScore;
	private Long maximumTimeLimits;
	private OutcomeRule testScoreRule;
	private OutcomeCondition cutValueRule;
	private OutcomeCondition minScoreRule;
	
	private TestFeedbackBuilder passedFeedback;
	private TestFeedbackBuilder failedFeedback;
	private List<TestFeedbackBuilder> additionalFeedbacks = new ArrayList<>();
	
	public AssessmentTestBuilder(AssessmentTest assessmentTest) {
		this.assessmentTest = assessmentTest;
		htmlBuilder = new AssessmentHtmlBuilder();
		editable = "OpenOLAT".equals(assessmentTest.getToolName());
		extract();
	}
	
	private void extract() {
		extractMaxScore();
		extractRules();
		extractFeedbacks();
		extractTimeLimits();
	}
	
	private void extractMaxScore() {
		maxScore = QtiNodesExtractor.extractMaxScore(assessmentTest);
	}
	
	private void extractRules() {
		if(assessmentTest.getOutcomeProcessing() != null) {
			List<OutcomeRule> outcomeRules = assessmentTest.getOutcomeProcessing().getOutcomeRules();
			for(OutcomeRule outcomeRule:outcomeRules) {
				// export test score
				if(outcomeRule instanceof SetOutcomeValue) {
					SetOutcomeValue setOutcomeValue = (SetOutcomeValue)outcomeRule;
					if(QTI21Constants.SCORE_IDENTIFIER.equals(setOutcomeValue.getIdentifier())) {
						testScoreRule = outcomeRule;
					}
				}
				
				// pass rule
				if(outcomeRule instanceof OutcomeCondition) {
					OutcomeCondition outcomeCondition = (OutcomeCondition)outcomeRule;
					boolean findIf = QtiNodesExtractor.findSetOutcomeValue(outcomeCondition.getOutcomeIf(), QTI21Constants.PASS_IDENTIFIER);
					boolean findElse = QtiNodesExtractor.findSetOutcomeValue(outcomeCondition.getOutcomeElse(), QTI21Constants.PASS_IDENTIFIER);
					if(findIf && findElse) {
						cutValue = QtiNodesExtractor.extractCutValue(outcomeCondition.getOutcomeIf());
						cutValueRule = outcomeCondition;
					}
					
					boolean findMinIf = QtiNodesExtractor.findLtValue(outcomeCondition.getOutcomeIf(), QTI21Constants.MINSCORE_IDENTIFIER)
							&& QtiNodesExtractor.findLtValue(outcomeCondition.getOutcomeIf(), QTI21Constants.SCORE_IDENTIFIER);
					if(findMinIf) {
						minScoreRule = outcomeCondition;
					}
				}
			}
		}
	}
	
	private void extractFeedbacks() {
		List<TestFeedback> feedbacks = assessmentTest.getTestFeedbacks();
		for(TestFeedback feedback:feedbacks) {
			TestFeedbackBuilder feedbackBuilder = new TestFeedbackBuilder(assessmentTest, feedback);
			if(feedbackBuilder.isPassedRule()) {
				passedFeedback = feedbackBuilder;
			} else if(feedbackBuilder.isFailedRule()) {
				failedFeedback = feedbackBuilder;
			} else {
				additionalFeedbacks.add(feedbackBuilder);
			}
		}
	}
	
	private void extractTimeLimits() {
		TimeLimits timeLimits = assessmentTest.getTimeLimits();
		if(timeLimits != null && timeLimits.getMaximum() != null) {
			maximumTimeLimits = timeLimits.getMaximum().longValue();
		}
	}
	
	public boolean isEditable() {
		return editable;
	}
	
	public AssessmentTest getAssessmentTest() {
		return assessmentTest;
	}
	
	public Double getCutValue() {
		return cutValue;
	}
	
	public void setCutValue(Double cutValue) {
		this.cutValue = cutValue;
	}
	
	public Double getMaxScore() {
		return maxScore;
	}

	public void setMaxScore(Double maxScore) {
		this.maxScore = maxScore;
	}

	/**
	 * @return The maximum time for the test in seconds.
	 */
	public Long getMaximumTimeLimits() {
		return maximumTimeLimits;
	}

	/**
	 * The maximum time to solve the test in seconds.
	 * @param maximumTimeLimits A positove value in seconds or null
	 */
	public void setMaximumTimeLimits(Long maximumTimeLimits) {
		this.maximumTimeLimits = maximumTimeLimits;
	}

	public TestFeedbackBuilder getPassedFeedback() {
		return passedFeedback;
	}
	
	public TestFeedbackBuilder createPassedFeedback() {
		passedFeedback = new TestFeedbackBuilder(assessmentTest, null);
		return passedFeedback;
	}
	
	public TestFeedbackBuilder getFailedFeedback() {
		return failedFeedback;
	}
	
	public TestFeedbackBuilder createFailedFeedback() {
		failedFeedback = new TestFeedbackBuilder(assessmentTest, null);
		return failedFeedback;
	}

	public AssessmentTest build() {
		if(!editable) {
			return assessmentTest;
		}
		
		if(assessmentTest.getOutcomeProcessing() == null) {
			assessmentTest.setOutcomeProcessing(new OutcomeProcessing(assessmentTest));
		}
		
		if(maximumTimeLimits != null) {
			TimeLimits timeLimits = new TimeLimits(assessmentTest);
			timeLimits.setMaximum(maximumTimeLimits.doubleValue());
			assessmentTest.setTimeLimits(timeLimits);
		} else {
			assessmentTest.setTimeLimits(null);
		}
		
		buildScore();
		buildTestScore();
		buildCutValue();
		buildFeedback();
		
		//clean up
		if(assessmentTest.getOutcomeProcessing().getOutcomeRules().isEmpty()) {
			assessmentTest.setOutcomeProcessing(null);
		}
		return assessmentTest;
	}
	

	
	private void buildScore() {
		OutcomeDeclaration scoreDeclaration = assessmentTest.getOutcomeDeclaration(QTI21Constants.SCORE_IDENTIFIER);
		if(scoreDeclaration == null) {
			scoreDeclaration = AssessmentTestFactory.createOutcomeDeclaration(assessmentTest, QTI21Constants.SCORE_IDENTIFIER, 0.0d);
			assessmentTest.getOutcomeDeclarations().add(0, scoreDeclaration);
		}
		
		if(maxScore != null) {
			OutcomeDeclaration maxScoreDeclaration = assessmentTest.getOutcomeDeclaration(QTI21Constants.MAXSCORE_IDENTIFIER);
			if(maxScoreDeclaration == null) {
				maxScoreDeclaration = AssessmentTestFactory.createOutcomeDeclaration(assessmentTest, QTI21Constants.MAXSCORE_IDENTIFIER, maxScore);
				assessmentTest.getOutcomeDeclarations().add(0, maxScoreDeclaration);
			} else {//update value
				AssessmentTestFactory.updateDefaultValue(maxScoreDeclaration, maxScore);
			}
		} else {
			OutcomeDeclaration maxScoreDeclaration = assessmentTest.getOutcomeDeclaration(QTI21Constants.MAXSCORE_IDENTIFIER);
			if(maxScoreDeclaration != null) {
				assessmentTest.getOutcomeDeclarations().remove(maxScoreDeclaration);
			}
		}
		
		// add min. score
		OutcomeDeclaration minScoreDeclaration = assessmentTest.getOutcomeDeclaration(QTI21Constants.MINSCORE_IDENTIFIER);
		if(minScoreDeclaration == null) {
			minScoreDeclaration = AssessmentTestFactory.createOutcomeDeclaration(assessmentTest, QTI21Constants.MINSCORE_IDENTIFIER, 0.0d);
			assessmentTest.getOutcomeDeclarations().add(0, minScoreDeclaration);
		} else {//update value
			AssessmentTestFactory.updateDefaultValue(minScoreDeclaration, 0.0d);
		}
	}
	
	/* Overall score of this test
	<setOutcomeValue identifier="SCORE">
		<sum>
			<testVariables variableIdentifier="SCORE" />
		</sum>
	</setOutcomeValue>
	*/
	private void buildTestScore() {
		if(testScoreRule == null) {
			SetOutcomeValue scoreRule = new SetOutcomeValue(assessmentTest);
			scoreRule.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
			
			Sum sum = new Sum(scoreRule);
			scoreRule.getExpressions().add(sum);
			
			TestVariables testVariables = new TestVariables(sum);
			sum.getExpressions().add(testVariables);
			testVariables.setVariableIdentifier(QTI21Constants.SCORE_IDENTIFIER);
			
			assessmentTest.getOutcomeProcessing().getOutcomeRules().add(0, scoreRule);
			testScoreRule = scoreRule;
		}
		
		if(minScoreRule == null) {
			OutcomeCondition scoreRule = AssessmentTestFactory.createMinScoreRule(assessmentTest);
			assessmentTest.getOutcomeProcessing().getOutcomeRules().add(1, scoreRule);
			minScoreRule = scoreRule;
		}
	}
	
	/*	Passed
	<outcomeCondition>
		<outcomeIf>
			<gte>
				<sum>
					<testVariables variableIdentifier="SCORE" />
				</sum>
				<baseValue baseType="float">
					1
				</baseValue>
			</gte>
			<setOutcomeValue identifier="PASS">
				<baseValue baseType="boolean">
					true
				</baseValue>
			</setOutcomeValue>
		</outcomeIf>
		<outcomeElse>
			<setOutcomeValue identifier="PASS">
				<baseValue baseType="boolean">
					false
				</baseValue>
			</setOutcomeValue>
		</outcomeElse>
	</outcomeCondition>
	*/
	private void buildCutValue() {
		if(cutValue != null) {
			OutcomeDeclaration passDeclaration = assessmentTest.getOutcomeDeclaration(QTI21Constants.PASS_IDENTIFIER);
			if(passDeclaration == null) {
				passDeclaration = AssessmentTestFactory.createOutcomeDeclaration(assessmentTest, QTI21Constants.PASS_IDENTIFIER, false);
				assessmentTest.getOutcomeDeclarations().add(passDeclaration);
			}
			
			boolean updated = false;
			if(cutValueRule != null && !cutValueRule.getOutcomeIf().getExpressions().isEmpty()) {
				Expression gte = cutValueRule.getOutcomeIf().getExpressions().get(0);
				if(gte.getExpressions().size() > 1) {
					Expression baseValue = gte.getExpressions().get(1);
					if(baseValue instanceof BaseValue) {
						BaseValue value = (BaseValue)baseValue;
						value.setSingleValue(new FloatValue(cutValue.doubleValue()));
						updated = true;
					}
				}
			}
			
			if(!updated) {
				assessmentTest.getOutcomeProcessing().getOutcomeRules().remove(cutValueRule);
				cutValueRule = AssessmentTestFactory.createCutValueRule(assessmentTest, cutValue);
				assessmentTest.getOutcomeProcessing().getOutcomeRules().add(cutValueRule);
			}
		} else if(cutValueRule != null) {
			OutcomeDeclaration passDeclaration = assessmentTest.getOutcomeDeclaration(QTI21Constants.PASS_IDENTIFIER);
			if(passDeclaration != null) {
				assessmentTest.getOutcomeDeclarations().remove(passDeclaration);
			}
			assessmentTest.getOutcomeProcessing().getOutcomeRules().remove(cutValueRule);
		}
	}
	
	private void buildFeedback() {
		//remove outcome rules
		List<OutcomeRule> outcomeRules = assessmentTest.getOutcomeProcessing().getOutcomeRules();
		for(Iterator<OutcomeRule> outcomeRuleIt=outcomeRules.iterator(); outcomeRuleIt.hasNext(); ) {
			OutcomeRule outcomeRule = outcomeRuleIt.next();
			if(outcomeRule instanceof OutcomeCondition) {
				OutcomeCondition outcomeCondition = (OutcomeCondition)outcomeRule;
				if(outcomeCondition.getOutcomeIf() != null && outcomeCondition.getOutcomeIf().getOutcomeRules().size() == 1) {
					OutcomeRule outcomeValue = outcomeCondition.getOutcomeIf().getOutcomeRules().get(0);
					if(outcomeValue instanceof SetOutcomeValue) {
						SetOutcomeValue setOutcomeValue = (SetOutcomeValue)outcomeValue;
						if(QTI21Constants.FEEDBACKMODAL_IDENTIFIER.equals(setOutcomeValue.getIdentifier())) {
							outcomeRuleIt.remove();
						}	
					}
				}
			}
		}
		
		//set the feedbackmodal outcome declaration if needed
		if(passedFeedback != null || failedFeedback != null) {
			OutcomeDeclaration outcomeDeclaration = assessmentTest.getOutcomeDeclaration(QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
			if(outcomeDeclaration == null) {
				OutcomeDeclaration feedbackModalOutcomeDeclaration = AssessmentTestFactory
						.createTestFeedbackModalOutcomeDeclaration(assessmentTest);
				assessmentTest.getOutcomeDeclarations().add(feedbackModalOutcomeDeclaration);
			}
		}

		if(passedFeedback != null) {
			buildFeedback(passedFeedback, true);
		}
		
		if(failedFeedback != null) {
			buildFeedback(failedFeedback, false);
		}
	}
	
	private void buildFeedback(TestFeedbackBuilder feedbackBuilder, boolean passed) {
		if(htmlBuilder.containsSomething(feedbackBuilder.getText())) {
			TestFeedback testFeedback;
			if(feedbackBuilder.getTestFeedback() == null) {
				testFeedback = AssessmentTestFactory
						.createTestFeedbackModal(assessmentTest, IdentifierGenerator.newAsIdentifier("fm") , feedbackBuilder.getTitle(), feedbackBuilder.getText());
				assessmentTest.getTestFeedbacks().add(testFeedback);
			} else {
				testFeedback = feedbackBuilder.getTestFeedback();
				testFeedback.setTitle(feedbackBuilder.getTitle());
				htmlBuilder.appendHtml(testFeedback, feedbackBuilder.getText());
			}
			OutcomeCondition outcomeCondition = AssessmentTestFactory
					.createTestFeedbackModalCondition(assessmentTest, passed, testFeedback.getOutcomeValue());
			assessmentTest.getOutcomeProcessing().getOutcomeRules().add(outcomeCondition);
		} else if(feedbackBuilder.getTestFeedback() != null) {
			assessmentTest.getTestFeedbacks().remove(feedbackBuilder.getTestFeedback());
		}
	}
}
