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

import static org.olat.ims.qti21.QTI21Constants.MINSCORE_CLX_IDENTIFIER;
import static org.olat.ims.qti21.QTI21Constants.SCORE_CLX_IDENTIFIER;
import static org.olat.ims.qti21.QTI21Constants.SCORE_IDENTIFIER;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.helpers.Settings;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;

import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.expression.ExpressionParent;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.general.Variable;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.And;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Gte;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Lt;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Match;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Multiple;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.expression.outcome.TestVariables;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.shared.FieldValue;
import uk.ac.ed.ph.jqtiplus.node.shared.declaration.DefaultValue;
import uk.ac.ed.ph.jqtiplus.node.test.AbstractPart;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;
import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.Ordering;
import uk.ac.ed.ph.jqtiplus.node.test.SectionPart;
import uk.ac.ed.ph.jqtiplus.node.test.SubmissionMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedback;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedbackAccess;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.node.test.View;
import uk.ac.ed.ph.jqtiplus.node.test.VisibilityMode;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeCondition;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeElse;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeIf;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeProcessing;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.types.ComplexReferenceIdentifier;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.IdentifierValue;

/**
 * 
 * Initial date: 04.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestFactory {
	
	/**
	 * Create an assessmentTest object but without items
	 * 
	 * @param title
	 * @return
	 */
	public static AssessmentTest createAssessmentTest(String title, String sectionTitle) {
		AssessmentTest assessmentTest = new AssessmentTest();
		assessmentTest.setIdentifier(IdentifierGenerator.newAsString("test"));
		assessmentTest.setTitle(title);
		assessmentTest.setToolName(QTI21Constants.TOOLNAME);
		assessmentTest.setToolVersion(Settings.getVersion());
		
		// outcome score
		OutcomeDeclaration scoreOutcomeDeclaration = new OutcomeDeclaration(assessmentTest);
		scoreOutcomeDeclaration.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
		scoreOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		scoreOutcomeDeclaration.setBaseType(BaseType.FLOAT);
		assessmentTest.getOutcomeDeclarations().add(scoreOutcomeDeclaration);
		
		//test part
		TestPart part = createTestPart(assessmentTest);
		appendAssessmentSection(sectionTitle, part);
		
		//outcome processing
		OutcomeProcessing outcomeProcessing = new OutcomeProcessing(assessmentTest);
		assessmentTest.setOutcomeProcessing(outcomeProcessing);
		
		SetOutcomeValue outcomeRule = new SetOutcomeValue(outcomeProcessing);
		outcomeRule.setIdentifier(QTI21Constants.SCORE_IDENTIFIER);
		
		Sum sum = new Sum(outcomeRule);
		outcomeRule.getExpressions().add(sum);
		
		TestVariables testVariables = new TestVariables(sum);
		testVariables.setVariableIdentifier(QTI21Constants.SCORE_IDENTIFIER);
		sum.getExpressions().add(testVariables);

		outcomeProcessing.getOutcomeRules().add(outcomeRule);

		return assessmentTest;
	}
	
	public static OutcomeDeclaration createOutcomeDeclaration(AssessmentTest assessmentTest, Identifier identifier, Double defaultValue) {
		OutcomeDeclaration outcomeDeclaration = new OutcomeDeclaration(assessmentTest);
		outcomeDeclaration.setIdentifier(identifier);
		outcomeDeclaration.setCardinality(Cardinality.SINGLE);
		outcomeDeclaration.setBaseType(BaseType.FLOAT);
		
		DefaultValue defaultVal = new DefaultValue(outcomeDeclaration);
		outcomeDeclaration.setDefaultValue(defaultVal);
		
		FieldValue fieldValue = new FieldValue(defaultVal);
		FloatValue identifierValue = new FloatValue(defaultValue);
		fieldValue.setSingleValue(identifierValue);
		
		defaultVal.getFieldValues().add(fieldValue);
		return outcomeDeclaration;
	}
	
	public static void updateDefaultValue(OutcomeDeclaration outcomeDeclaration, Double defaultValue) {
		outcomeDeclaration.setBaseType(BaseType.FLOAT);
		
		DefaultValue defaultVal = outcomeDeclaration.getDefaultValue();
		defaultVal.getFieldValues().clear();
		
		FieldValue fieldValue = new FieldValue(defaultVal);
		FloatValue identifierValue = new FloatValue(defaultValue);
		fieldValue.setSingleValue(identifierValue);
		
		defaultVal.getFieldValues().add(fieldValue);
	}
	
	public static OutcomeDeclaration createOutcomeDeclaration(AssessmentTest assessmentTest, Identifier identifier, boolean defaultValue) {
		OutcomeDeclaration outcomeDeclaration = new OutcomeDeclaration(assessmentTest);
		outcomeDeclaration.setIdentifier(identifier);
		outcomeDeclaration.setCardinality(Cardinality.SINGLE);
		outcomeDeclaration.setBaseType(BaseType.BOOLEAN);
		
		DefaultValue defaultVal = new DefaultValue(outcomeDeclaration);
		outcomeDeclaration.setDefaultValue(defaultVal);
		
		FieldValue fieldValue = new FieldValue(defaultVal);
		BooleanValue booleanValue = defaultValue ? BooleanValue.TRUE : BooleanValue.FALSE;
		fieldValue.setSingleValue(booleanValue);
		
		defaultVal.getFieldValues().add(fieldValue);
		return outcomeDeclaration;
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
	public static OutcomeCondition createCutValueRule(AssessmentTest assessmentTest, Double cutValue) {
		OutcomeCondition outcomeCondition = new OutcomeCondition(assessmentTest);
		
		{//if
			OutcomeIf outcomeIf = new OutcomeIf(outcomeCondition);
			outcomeCondition.setOutcomeIf(outcomeIf);
			
			Gte gte = new Gte(outcomeIf);
			outcomeIf.setExpression(gte);
			{
				appendSumOfTestVariables(gte);

				BaseValue passed = new BaseValue(gte);
				passed.setBaseTypeAttrValue(BaseType.FLOAT);
				passed.setSingleValue(new FloatValue(cutValue.floatValue()));
				gte.getExpressions().add(passed);
			}
			
			SetOutcomeValue passedOutcomeValue = new SetOutcomeValue(outcomeIf);
			passedOutcomeValue.setIdentifier(QTI21Constants.PASS_IDENTIFIER);
			outcomeIf.getOutcomeRules().add(passedOutcomeValue);
			
			BaseValue passed = new BaseValue(passedOutcomeValue);
			passed.setBaseTypeAttrValue(BaseType.BOOLEAN);
			passed.setSingleValue(BooleanValue.TRUE);
			passedOutcomeValue.setExpression(passed);
		}
		
		{//else
			OutcomeElse outcomeElse = new OutcomeElse(outcomeCondition);
			outcomeCondition.setOutcomeElse(outcomeElse);
			
			SetOutcomeValue notPassedOutcomeValue = new SetOutcomeValue(outcomeElse);
			notPassedOutcomeValue.setIdentifier(QTI21Constants.PASS_IDENTIFIER);
			outcomeElse.getOutcomeRules().add(notPassedOutcomeValue);
			
			BaseValue notPassed = new BaseValue(notPassedOutcomeValue);
			notPassed.setBaseTypeAttrValue(BaseType.BOOLEAN);
			notPassed.setSingleValue(BooleanValue.FALSE);
			notPassedOutcomeValue.setExpression(notPassed);
		}

		return outcomeCondition;
	}
	
	/*
    <outcomeCondition>
      <outcomeIf>
        <lt>
          <variable identifier="SCORE"/>
          <variable identifier="MINSCORE"/>
        </lt>
        <setOutcomeValue identifier="SCORE">
          <variable identifier="MINSCORE"/>
        </setOutcomeValue>
      </outcomeIf>
    </outcomeCondition>
    */
	public static OutcomeCondition createMinScoreRule(AssessmentTest assessmentTest) {
		OutcomeCondition outcomeCondition = new OutcomeCondition(assessmentTest);
		OutcomeIf outcomeIf = new OutcomeIf(outcomeCondition);
		outcomeCondition.setOutcomeIf(outcomeIf);
		
		Lt lt = new Lt(outcomeIf);
		outcomeIf.setExpression(lt);
		
		Variable scoreVar = new Variable(lt);
		scoreVar.setIdentifier(SCORE_CLX_IDENTIFIER);
		lt.getExpressions().add(scoreVar);
		
		Variable minScoreVar = new Variable(lt);
		minScoreVar.setIdentifier(MINSCORE_CLX_IDENTIFIER);
		lt.getExpressions().add(minScoreVar);
		
		SetOutcomeValue setOutcomeValue = new SetOutcomeValue(outcomeIf);
		setOutcomeValue.setIdentifier(SCORE_IDENTIFIER);
		
		Variable minScoreOutcomeVar = new Variable(setOutcomeValue);
		minScoreOutcomeVar.setIdentifier(MINSCORE_CLX_IDENTIFIER);
		setOutcomeValue.setExpression(minScoreOutcomeVar);
		outcomeIf.getOutcomeRules().add(setOutcomeValue);
		
		return outcomeCondition;
	}
	
	/*
	<sum>
		<testVariables variableIdentifier="SCORE" />
	</sum>
	 */
	public static Sum appendSumOfTestVariables(ExpressionParent parent) {
		Sum sum = new Sum(parent);
		parent.getExpressions().add(sum);
		
		TestVariables testVariables = new TestVariables(sum);
		sum.getExpressions().add(testVariables);
		testVariables.setVariableIdentifier(QTI21Constants.SCORE_IDENTIFIER);
		return sum;
	}
	
	public static void appendAssessmentItem(AssessmentSection section, String itemFilename)
	throws URISyntaxException {
		AssessmentItemRef item = new AssessmentItemRef(section);
		item.setIdentifier(IdentifierGenerator.newAsIdentifier("ai"));
		item.setHref(new URI(itemFilename));
		section.getSectionParts().add(item);
	}
	
	public static TestPart createTestPart(AssessmentTest assessmentTest) {
		TestPart part = new TestPart(assessmentTest);
		part.setIdentifier(IdentifierGenerator.newAsIdentifier("tp"));
		part.setNavigationMode(NavigationMode.NONLINEAR);
		part.setSubmissionMode(SubmissionMode.INDIVIDUAL);
		assessmentTest.getTestParts().add(part);
		
		// test par item session control
		ItemSessionControl itemSessionControl = new ItemSessionControl(part);
		itemSessionControl.setAllowComment(Boolean.TRUE);
		itemSessionControl.setAllowReview(Boolean.FALSE);
		itemSessionControl.setAllowSkipping(Boolean.TRUE);//default is true
		itemSessionControl.setShowFeedback(Boolean.FALSE);
		itemSessionControl.setShowSolution(Boolean.FALSE);
		itemSessionControl.setMaxAttempts(0);
		part.setItemSessionControl(itemSessionControl);
		return part;
	}
	
	/**
	 * create an assessmentSection with an empty rubricBlock for candidate,
	 * not shuffled but visible and fixed.
	 * @param part
	 * @return
	 */
	public static AssessmentSection appendAssessmentSection(String title, TestPart part) {
		return appendAssessmentSectionInternal(title, part);
	}
	
	public static AssessmentSection appendAssessmentSection(String title, AssessmentSection part) {
		return appendAssessmentSectionInternal(title, part);
	}
	
	private static final AssessmentSection appendAssessmentSectionInternal(String title, AbstractPart part) {
		boolean shuffledSections = shuffledSections(part);
	
		// section
		AssessmentSection section = new AssessmentSection(part);
		if(shuffledSections) {
			section.setFixed(Boolean.FALSE);
			section.setKeepTogether(Boolean.TRUE);
		} else {
			section.setFixed(Boolean.TRUE);
		}

		section.setVisible(Boolean.TRUE);
		section.setTitle(title);
		section.setIdentifier(IdentifierGenerator.newAsIdentifier("sect"));
		if(part instanceof TestPart) {
			((TestPart)part).getAssessmentSections().add(section);
		} else if(part instanceof AssessmentSection) {
			((AssessmentSection)part).getSectionParts().add(section);
		}

		// section ordering
		Ordering ordering = new Ordering(section);
		ordering.setShuffle(false);
		section.setOrdering(ordering);
		
		// section rubric block
		RubricBlock rubricBlock = new RubricBlock(section);
		rubricBlock.setViews(Collections.singletonList(View.CANDIDATE));
		section.getRubricBlocks().add(rubricBlock);
		
		ItemSessionControl itemSessionControl = new ItemSessionControl(section);
		section.setItemSessionControl(itemSessionControl);
		return section;
	}
	
	/**
	 * Check if the specified part is a section, only as sections
	 * as children and that it matches the rules to shuffle the
	 * section, parent section shuffles, child sections are
	 * not fixed. 
	 * 
	 * @param part A part
	 * @return true if part will shuffle the sections under it
	 */
	public static final boolean shuffledSections(AbstractPart part) {
		if(!(part instanceof AssessmentSection)) return false;
		
		AssessmentSection section = (AssessmentSection)part;
		if(section.getOrdering() != null && section.getOrdering().getShuffle()) {
			boolean allFloating = true;
			
			List<SectionPart> sectionParts = section.getSectionParts();
			for(SectionPart sectionPart:sectionParts) {
				if(sectionPart instanceof AssessmentSection) {
					boolean fixed = ((AssessmentSection)sectionPart).getFixed();
					allFloating &= !fixed;
				} else {
					allFloating &= false;
				}
			}
			
			return allFloating;
		}
		return false;
	}
	
	/*
	<outcomeDeclaration identifier="FEEDBACKMODAL" cardinality="multiple" baseType="identifier" view="testConstructor" />
	*/
	public static final OutcomeDeclaration createTestFeedbackModalOutcomeDeclaration(AssessmentTest assessmentTest) {
		OutcomeDeclaration feedbackOutcomeDeclaration = new OutcomeDeclaration(assessmentTest);
		feedbackOutcomeDeclaration.setIdentifier(QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
		feedbackOutcomeDeclaration.setCardinality(Cardinality.MULTIPLE);
		feedbackOutcomeDeclaration.setBaseType(BaseType.IDENTIFIER);
		
		List<View> views = new ArrayList<>();
		views.add(View.TEST_CONSTRUCTOR);
		feedbackOutcomeDeclaration.setViews(views);
		
		return feedbackOutcomeDeclaration;
	}
	
	/*
	<testFeedback identifier="Feedback952020414" outcomeIdentifier="FEEDBACKMODAL" showHide="show" access="atEnd" title="Correct answer">
		<p>This is the correct answer</p>
	</testFeedback>
	 */
	public static final TestFeedback createTestFeedbackModal(AssessmentTest assessmentTest, Identifier identifier, String title, String text) {
		TestFeedback testFeedback = new TestFeedback(assessmentTest);
		testFeedback.setOutcomeValue(identifier);
		testFeedback.setOutcomeIdentifier(QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
		testFeedback.setVisibilityMode(VisibilityMode.SHOW_IF_MATCH);
		testFeedback.setTestFeedbackAccess(TestFeedbackAccess.AT_END);
		testFeedback.setTitle(title);
		
		new AssessmentHtmlBuilder().appendHtml(testFeedback, text);
		
		return testFeedback;
	}
	/*
	<outcomeCondition>
		<outcomeIf>
			<and>
				<match>
					<baseValue baseType="boolean">
						false
					</baseValue>
					<variable identifier="PASS" />
				</match>
			</and>
			<setOutcomeValue identifier="FEEDBACKMODAL">
				<multiple>
					<variable identifier="FEEDBACKMODAL" />
					<baseValue baseType="identifier">
						Feedback1757237693
					</baseValue>
				</multiple>
			</setOutcomeValue>
		</outcomeIf>
	</outcomeCondition>
	 */
	public static final OutcomeCondition createTestFeedbackModalCondition(AssessmentTest assessmentTest, boolean condition, Identifier feedbackIdentifier) {
		OutcomeCondition outcomeCondition = new OutcomeCondition(assessmentTest);
		OutcomeIf outcomeIf = new OutcomeIf(outcomeCondition);
		outcomeCondition.setOutcomeIf(outcomeIf);
		
		{//condition
			And and = new And(outcomeIf);
			outcomeIf.getExpressions().add(and);
			
			Match match = new Match(and);
			and.getExpressions().add(match);
			
			BaseValue feedbackVal = new BaseValue(match);
			feedbackVal.setBaseTypeAttrValue(BaseType.BOOLEAN);
			feedbackVal.setSingleValue(condition ? BooleanValue.TRUE : BooleanValue.FALSE);
			match.getExpressions().add(feedbackVal);
			
			Variable variable = new Variable(match);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(QTI21Constants.PASS));
			match.getExpressions().add(variable);
		}
		
		{//outcome
			SetOutcomeValue setOutcomeValue = new SetOutcomeValue(outcomeIf);
			setOutcomeValue.setIdentifier(QTI21Constants.FEEDBACKMODAL_IDENTIFIER);
			outcomeIf.getOutcomeRules().add(setOutcomeValue);
			
			Multiple multiple = new Multiple(setOutcomeValue);
			setOutcomeValue.getExpressions().add(multiple);
			
			Variable variable = new Variable(multiple);
			variable.setIdentifier(ComplexReferenceIdentifier.parseString(QTI21Constants.FEEDBACKMODAL));
			multiple.getExpressions().add(variable);
			
			BaseValue feedbackVal = new BaseValue(multiple);
			feedbackVal.setBaseTypeAttrValue(BaseType.IDENTIFIER);
			feedbackVal.setSingleValue(new IdentifierValue(feedbackIdentifier));
			multiple.getExpressions().add(feedbackVal);
		}
		
		return outcomeCondition;
	}

}
