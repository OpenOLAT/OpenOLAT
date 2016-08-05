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
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeCondition;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeConditionChild;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeIf;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeProcessing;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeRule;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;

/**
 * 
 * Initial date: 11.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestBuilder {
	
	private final AssessmentTest assessmentTest;
	private final AssessmentHtmlBuilder htmlBuilder;
	
	private Double cutValue;
	private Double maxScore;
	private OutcomeCondition cutValueRule;
	
	private boolean testScore = false;
	private OutcomeRule testScoreRule;
	
	private TestFeedbackBuilder passedFeedback;
	private TestFeedbackBuilder failedFeedback;
	private List<TestFeedbackBuilder> additionalFeedbacks = new ArrayList<>();
	
	public AssessmentTestBuilder(AssessmentTest assessmentTest) {
		this.assessmentTest = assessmentTest;
		htmlBuilder = new AssessmentHtmlBuilder();
		extract();
	}
	
	private void extract() {
		extractMaxScore();
		extractRules();
		extractFeedbacks();
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
						testScore = true;
						testScoreRule = outcomeRule;
					}
				}
				
				// pass rule
				if(outcomeRule instanceof OutcomeCondition) {
					OutcomeCondition outcomeCondition = (OutcomeCondition)outcomeRule;
					boolean findIf = findSetOutcomeValue(outcomeCondition.getOutcomeIf(), QTI21Constants.PASS_IDENTIFIER);
					boolean findElse = findSetOutcomeValue(outcomeCondition.getOutcomeElse(), QTI21Constants.PASS_IDENTIFIER);
					if(findIf && findElse) {
						cutValue = extractCutValue(outcomeCondition.getOutcomeIf());
						cutValueRule = outcomeCondition;
					}
				}
			}
		}
	}
	
	public static Double extractCutValue(OutcomeIf outcomeIf) {
		if(outcomeIf != null && outcomeIf.getExpressions().size() > 0) {
			Expression gte = outcomeIf.getExpressions().get(0);
			if(gte.getExpressions().size() > 1) {
				Expression baseValue = gte.getExpressions().get(1);
				if(baseValue instanceof BaseValue) {
					BaseValue value = (BaseValue)baseValue;
					if(value.getSingleValue() instanceof FloatValue) {
						return ((FloatValue)value.getSingleValue()).doubleValue();
					}
				}
			}
		}
		return null;
	}
	
	public static boolean findSetOutcomeValue(OutcomeConditionChild outcomeConditionChild, Identifier identifier) {
		if(outcomeConditionChild == null
				|| outcomeConditionChild.getOutcomeRules() == null
				|| outcomeConditionChild.getOutcomeRules().isEmpty()) return false;
		
		List<OutcomeRule> outcomeRules = outcomeConditionChild.getOutcomeRules();
		for(OutcomeRule outcomeRule:outcomeRules) {
			SetOutcomeValue setOutcomeValue = (SetOutcomeValue)outcomeRule;
			if(identifier.equals(setOutcomeValue.getIdentifier())) {
				return true;
			}
		}
		
		return false;
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
	
	public AssessmentTest getAssessmentTest() {
		return assessmentTest;
	}
	
	public boolean isExportScore() {
		return testScore;
	}
	
	public void setExportScore(boolean export) {
		testScore = export;
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
		if(assessmentTest.getOutcomeProcessing() == null) {
			assessmentTest.setOutcomeProcessing(new OutcomeProcessing(assessmentTest));
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
	}
	
	/* Overall score of this test
	<setOutcomeValue identifier="SCORE">
		<sum>
			<testVariables variableIdentifier="SCORE" />
		</sum>
	</setOutcomeValue>
	*/
	private void buildTestScore() {
		if(testScore) {
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
		} else if(testScoreRule != null) {
			assessmentTest.getOutcomeProcessing().getOutcomeRules().remove(testScoreRule);
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
			if(cutValueRule != null && cutValueRule.getOutcomeIf().getExpressions().size() > 0) {
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
			assessmentTest.getOutcomeDeclarations().remove(cutValueRule);
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
