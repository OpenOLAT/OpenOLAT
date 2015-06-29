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

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.xml.sax.SAXParseException;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.Sum;
import uk.ac.ed.ph.jqtiplus.node.expression.outcome.TestVariables;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;
import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.Ordering;
import uk.ac.ed.ph.jqtiplus.node.test.SubmissionMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestFeedback;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.node.test.TimeLimits;
import uk.ac.ed.ph.jqtiplus.node.test.View;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeProcessing;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.notification.Notification;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiModelBuildingError;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlInterpretationException.InterpretationFailureReason;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.validation.TestValidationResult;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.xmlutils.XmlParseResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 04.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentTestPackageTest {
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentTestPackageTest.class);
	
	
	protected AssessmentTest createAssessmentTest(File itemFile) throws URISyntaxException {

		AssessmentTest assessmentTest = new AssessmentTest();
		assessmentTest.setIdentifier("id" + UUID.randomUUID());
		assessmentTest.setTitle("My test");
		assessmentTest.setToolName("OpenOLAT");
		assessmentTest.setToolVersion("11.0");
		
		//outcome declarations
		OutcomeDeclaration scoreOutcomeDeclaration = new OutcomeDeclaration(assessmentTest);
		scoreOutcomeDeclaration.setIdentifier(Identifier.parseString("SCORE"));
		scoreOutcomeDeclaration.setCardinality(Cardinality.SINGLE);
		scoreOutcomeDeclaration.setBaseType(BaseType.FLOAT);
		assessmentTest.getOutcomeDeclarations().add(scoreOutcomeDeclaration);
		
		
		//time limits
		TimeLimits timeLimits = new TimeLimits(assessmentTest);
		timeLimits.setAllowLateSubmission(Boolean.FALSE);
		timeLimits.setMinimum(0.0d);
		timeLimits.setMaximum(1800.0);
		assessmentTest.setTimeLimits(timeLimits);
		
		//test parts
		TestPart part = new TestPart(assessmentTest);
		part.setIdentifier(Identifier.parseString("id" + UUID.randomUUID()));
		part.setNavigationMode(NavigationMode.NONLINEAR);
		part.setSubmissionMode(SubmissionMode.INDIVIDUAL);
		assessmentTest.getTestParts().add(part);
		
		//part -> item session control
		ItemSessionControl itemSessionControl = new ItemSessionControl(part);
		itemSessionControl.setAllowComment(Boolean.TRUE);
		itemSessionControl.setAllowReview(Boolean.TRUE);
		itemSessionControl.setAllowSkipping(Boolean.TRUE);
		itemSessionControl.setMaxAttempts(12);
		itemSessionControl.setShowFeedback(Boolean.TRUE);
		itemSessionControl.setShowSolution(Boolean.TRUE);
		part.setItemSessionControl(itemSessionControl);
		
		// section
		AssessmentSection section = new AssessmentSection(part);
		section.setFixed(Boolean.TRUE);
		section.setVisible(Boolean.TRUE);
		section.setTitle("My section");
		section.setIdentifier(Identifier.parseString("id" + UUID.randomUUID()));
		part.getAssessmentSections().add(section);
		
		Ordering ordering = new Ordering(section);
		ordering.setShuffle(false);
		section.setOrdering(ordering);
		
		RubricBlock rubrickBlock = new RubricBlock(section);
		rubrickBlock.setViews(Collections.singletonList(View.CANDIDATE));
		section.getRubricBlocks().add(rubrickBlock);
		
		AssessmentItemRef item = new AssessmentItemRef(section);
		item.setIdentifier(Identifier.parseString("id" + UUID.randomUUID()));
		item.setHref(new URI(itemFile.getName()));
		section.getSectionParts().add(item);

		//outcome processing
		OutcomeProcessing outcomeProcessing = new OutcomeProcessing(assessmentTest);
		assessmentTest.setOutcomeProcessing(outcomeProcessing);
		
		SetOutcomeValue outcomeRule = new SetOutcomeValue(outcomeProcessing);
		outcomeRule.setIdentifier(Identifier.parseString("SCORE"));
		
		Sum sum = new Sum(outcomeRule);
		outcomeRule.getExpressions().add(sum);
		
		TestVariables testVariables = new TestVariables(sum);
		testVariables.setVariableIdentifier(Identifier.parseString("SCORE"));
		sum.getExpressions().add(testVariables);
		
		
		outcomeProcessing.getOutcomeRules().add(outcomeRule);
		
		//feedbacks
		List<TestFeedback> testFeedbacks = assessmentTest.getTestFeedbacks();
		TestFeedback testFeedback = new TestFeedback(assessmentTest);
		//testFeedback.
		
		return assessmentTest;
	}
		
		
	@Test
	public void buildAssessmentTest() throws URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		
		File fullPackage = new File("/Users/srosse/Desktop/QTI/Full/");
		File itemFile = new File(fullPackage, "generated_item.xml");
		AssessmentTest assessmentTest = createAssessmentTest(itemFile);
		
		qtiSerializer.serializeJqtiObject(assessmentTest, System.out);
		System.out.println("\n-------------");
		
		File outputFile = new File("/Users/srosse/Desktop/QTI/generated_test.xml");
		if(outputFile.exists()) {
			outputFile.delete();
			outputFile = new File("/Users/srosse/Desktop/QTI/generated_test.xml");
		}
		try(FileOutputStream out = new FileOutputStream(outputFile)) {
			qtiSerializer.serializeJqtiObject(assessmentTest, out);	
		} catch(Exception e) {
			log.error("", e);
		}

		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(outputFile.toPath());
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        TestValidationResult test = assessmentObjectXmlLoader.loadResolveAndValidateTest(outputFile.toURI());

        System.out.println("Has errors: " + (test.getModelValidationErrors().size() > 0));
        for(Notification notification: test.getModelValidationErrors()) {
        	System.out.println(notification.getQtiNode() + " : " + notification.getMessage());
        }
        
        BadResourceException e = test.getResolvedAssessmentTest().getTestLookup().getBadResourceException();
        if(e instanceof QtiXmlInterpretationException) {
        	QtiXmlInterpretationException qe = (QtiXmlInterpretationException)e;
        	if(qe.getQtiModelBuildingErrors() != null) {
	        	for(QtiModelBuildingError error :qe.getQtiModelBuildingErrors()) {
	        		String localName = error.getElementLocalName();
	        		String msg = error.getException().getMessage();
	        		int lineNumber = error.getElementLocation().getLineNumber();
	        		System.out.println(lineNumber + " :: " + localName + " :: " + msg);
	        	}
        	}
        	
        	if(qe.getInterpretationFailureReason() != null) {
        		InterpretationFailureReason reason = qe.getInterpretationFailureReason();
        		System.out.println("Failure: " + reason);
        	}
        	
        	if(qe.getXmlParseResult() != null) {
        		XmlParseResult result = qe.getXmlParseResult();
        		if(result.getWarnings() != null) {
        			for(SAXParseException saxex : result.getWarnings()) {
        				int lineNumber = saxex.getLineNumber();
        				int columnNumber = saxex.getColumnNumber();
        				String msg = saxex.getMessage();
    	        		System.out.println("Error: " + lineNumber + ":" + columnNumber + " :: " + msg);
        			}
        		}
        		
        		if(result.getErrors() != null) {
        			for(SAXParseException saxex : result.getErrors()) {
        				int lineNumber = saxex.getLineNumber();
        				int columnNumber = saxex.getColumnNumber();
        				String msg = saxex.getMessage();
    	        		System.out.println("Error: " + lineNumber + ":" + columnNumber + " :: " + msg);
        			}
        		}
        		
        		if(result.getFatalErrors() != null) {
        			for(SAXParseException saxex : result.getFatalErrors()) {
        				int lineNumber = saxex.getLineNumber();
        				int columnNumber = saxex.getColumnNumber();
        				String msg = saxex.getMessage();
    	        		System.out.println("Fatal: " + lineNumber + ":" + columnNumber + " :: " + msg);
        			}
        		}
        	}
        }
        
        Assert.assertTrue(test.getModelValidationErrors().isEmpty());
	}
}