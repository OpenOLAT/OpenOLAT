package org.olat.ims.qti21.model.xml;

import java.io.File;
import java.io.FileOutputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.content.variable.RubricBlock;
import uk.ac.ed.ph.jqtiplus.node.outcome.declaration.OutcomeDeclaration;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentSection;
import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;
import uk.ac.ed.ph.jqtiplus.node.test.NavigationMode;
import uk.ac.ed.ph.jqtiplus.node.test.Ordering;
import uk.ac.ed.ph.jqtiplus.node.test.SubmissionMode;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.node.test.TimeLimits;
import uk.ac.ed.ph.jqtiplus.node.test.View;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.OutcomeProcessing;
import uk.ac.ed.ph.jqtiplus.node.test.outcome.processing.SetOutcomeValue;
import uk.ac.ed.ph.jqtiplus.notification.Notification;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.validation.TestValidationResult;
import uk.ac.ed.ph.jqtiplus.value.BaseType;
import uk.ac.ed.ph.jqtiplus.value.Cardinality;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

public class AssessmentTestPackageTest {
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentTestPackageTest.class);
	
	@Test
	public void buildAssessmentTest() throws URISyntaxException {

		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());

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
		item.setHref(new URI("test-item.xml"));
		section.getSectionParts().add(item);
		
		
		
		
		
		//outcome processing
		OutcomeProcessing outcomeProcessing = new OutcomeProcessing(assessmentTest);
		assessmentTest.setOutcomeProcessing(outcomeProcessing);
		
		SetOutcomeValue outcomeRule = new SetOutcomeValue(outcomeProcessing);
		
		
		outcomeProcessing.getOutcomeRules().add(outcomeRule);
		
		//feedbacks
		assessmentTest.getTestFeedbacks();
		
		
		qtiSerializer.serializeJqtiObject(assessmentTest, System.out);
		System.out.println("\n-------------");
		
		File outputFile = new File("/Users/srosse/Desktop/QTI 2.1/generated_test.xml");
		if(outputFile.exists()) {
			outputFile.delete();
			outputFile = new File("/Users/srosse/Desktop/QTI 2.1/generated_test.xml");
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

        System.out.println("Has errors: " + test.hasErrors());
        for(Notification notification: test.getErrors()) {
        	System.out.println(notification.getQtiNode() + " : " + notification.getMessage());
        }
        Assert.assertFalse(test.hasErrors());
	}
}