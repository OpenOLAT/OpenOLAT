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

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;

import org.olat.core.helpers.Settings;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.IdentifierGenerator;

import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.expression.ExpressionParent;
import uk.ac.ed.ph.jqtiplus.node.expression.general.BaseValue;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Gte;
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
import uk.ac.ed.ph.jqtiplus.node.test.SubmissionMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.node.test.View;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeCondition;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeElse;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeIf;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeProcessing;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.BooleanValue;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;

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
	public static AssessmentTest createAssessmentTest(String title) {
		AssessmentTest assessmentTest = new AssessmentTest();
		assessmentTest.setIdentifier(IdentifierGenerator.newAsString());
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
		createAssessmentSection(part);
		
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
		//if
		{
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
		item.setIdentifier(IdentifierGenerator.newAsIdentifier());
		item.setHref(new URI(itemFilename));
		section.getSectionParts().add(item);
	}
	
	public static TestPart createTestPart(AssessmentTest assessmentTest) {
		TestPart part = new TestPart(assessmentTest);
		part.setIdentifier(IdentifierGenerator.newAsIdentifier());
		part.setNavigationMode(NavigationMode.NONLINEAR);
		part.setSubmissionMode(SubmissionMode.INDIVIDUAL);
		assessmentTest.getTestParts().add(part);
		
		// test par item session control
		ItemSessionControl itemSessionControl = new ItemSessionControl(part);
		itemSessionControl.setAllowComment(Boolean.FALSE);
		itemSessionControl.setAllowReview(Boolean.FALSE);
		itemSessionControl.setAllowSkipping(Boolean.FALSE);
		itemSessionControl.setShowFeedback(Boolean.FALSE);
		itemSessionControl.setShowSolution(Boolean.FALSE);
		part.setItemSessionControl(itemSessionControl);
		return part;
	}
	
	public static AssessmentSection createAssessmentSection(TestPart part) {
		return createAssessmentSectionInternal(part);
	}
	
	public static AssessmentSection createAssessmentSection(AssessmentSection part) {
		return createAssessmentSectionInternal(part);
	}
	
	private final static AssessmentSection createAssessmentSectionInternal(AbstractPart part) {
		// section
		AssessmentSection section = new AssessmentSection(part);
		section.setFixed(Boolean.TRUE);
		section.setVisible(Boolean.TRUE);
		section.setTitle("New section");
		section.setIdentifier(IdentifierGenerator.newAsIdentifier());
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
		
		return section;
	}

}
