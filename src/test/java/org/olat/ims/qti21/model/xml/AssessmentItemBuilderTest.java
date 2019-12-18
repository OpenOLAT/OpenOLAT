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
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.fileresource.types.ImsQTI21Resource.PathResourceLocator;
import org.olat.ims.qti21.model.xml.interactions.EssayAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.EntryType;
import org.olat.ims.qti21.model.xml.interactions.HotspotAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.KPrimAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.MultipleChoiceAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.SingleChoiceAssessmentItemBuilder;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.ExtendedTextInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.HotspotInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.MatchInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleMatchSet;
import uk.ac.ed.ph.jqtiplus.node.item.response.declaration.ResponseDeclaration;
import uk.ac.ed.ph.jqtiplus.provision.BadResourceException;
import uk.ac.ed.ph.jqtiplus.reading.AssessmentObjectXmlLoader;
import uk.ac.ed.ph.jqtiplus.reading.QtiXmlReader;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.validation.ItemValidationResult;
import uk.ac.ed.ph.jqtiplus.xmlutils.locators.ResourceLocator;

/**
 * 
 * Initial date: 20.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@RunWith(Parameterized.class)
public class AssessmentItemBuilderTest {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentItemBuilderTest.class);
	
	@Parameters
    public static Collection<Object[]> data() {
        return Arrays.asList(new Object[][] {
                { Boolean.TRUE },
                { Boolean.FALSE }
        });
    }
    
    private Boolean build;
    
    public AssessmentItemBuilderTest(Boolean build) {
    	this.build = build;
    }
	
	@Test
	public void findFeedbacks()  throws URISyntaxException {
		URL itemUrl = AssessmentItemBuilderTest.class.getResource("assessment-item-single-choice-feedbacks.xml");
		AssessmentItem assessmentItem = loadAssessmentItem(itemUrl);
		
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		SingleChoiceAssessmentItemBuilder itemBuilder = new SingleChoiceAssessmentItemBuilder(assessmentItem, qtiSerializer);
		if(build.booleanValue()) {
			itemBuilder.build();
		}
		
		ModalFeedbackBuilder correctFeedback = itemBuilder.getCorrectFeedback();
		Assert.assertNotNull(correctFeedback);
		Assert.assertTrue(correctFeedback.isCorrectRule());
		
		ModalFeedbackBuilder incorrectFeedback = itemBuilder.getIncorrectFeedback();
		Assert.assertNotNull(incorrectFeedback);
		Assert.assertTrue(incorrectFeedback.isIncorrectRule());
	}

	/**
	 * Check if a bare bone single choice created with our builder make a valid assessmentItem.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void buildAssessmentItem_singleChoice() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		SingleChoiceAssessmentItemBuilder itemBuilder = new SingleChoiceAssessmentItemBuilder("Single choice", "New answer", qtiSerializer);

		if(build.booleanValue()) {
			itemBuilder.build();
		}
		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();

		ItemValidationResult itemResult = serializeAndReload(assessmentItem);
        
        AssessmentItem reloadedItem = itemResult.getResolvedAssessmentItem().getItemLookup().extractIfSuccessful();
        List<Interaction> interactions = reloadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof ChoiceInteraction);
	}
	
	/**
	 * Check if a bare bone multiple choice created with our builder make a valid assessmentItem.
	 * 
	 * @throws IOException
	 * @throws URISyntaxException
	 */
	@Test
	public void buildAssessmentItem_multipleChoice() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		MultipleChoiceAssessmentItemBuilder itemBuilder = new MultipleChoiceAssessmentItemBuilder("Multiple choice", "New answer", qtiSerializer);
		if(build.booleanValue()) {
			itemBuilder.build();
		}
		
		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
		ItemValidationResult itemResult = serializeAndReload(assessmentItem);
        
        AssessmentItem reloadedItem = itemResult.getResolvedAssessmentItem().getItemLookup().extractIfSuccessful();
        List<Interaction> interactions = reloadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof ChoiceInteraction);
	}
	
	@Test
	public void buildAssessmentItem_essay() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		EssayAssessmentItemBuilder itemBuilder = new EssayAssessmentItemBuilder("Essay", qtiSerializer);
		if(build.booleanValue()) {
			itemBuilder.build();
		}
		
		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
		ItemValidationResult itemResult = serializeAndReload(assessmentItem);
        
        AssessmentItem reloadedItem = itemResult.getResolvedAssessmentItem().getItemLookup().extractIfSuccessful();
        List<Interaction> interactions = reloadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof ExtendedTextInteraction);
	}
	
	@Test
	public void buildAssessmentItem_textEntry() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		FIBAssessmentItemBuilder itemBuilder = new FIBAssessmentItemBuilder("Gap text", EntryType.text, qtiSerializer);
		if(build.booleanValue()) {
			itemBuilder.build();
		}
		
		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
		ItemValidationResult itemResult = serializeAndReload(assessmentItem);
        
        AssessmentItem reloadedItem = itemResult.getResolvedAssessmentItem().getItemLookup().extractIfSuccessful();
        List<Interaction> interactions = reloadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof TextEntryInteraction);
        TextEntryInteraction interaction = (TextEntryInteraction)interactions.get(0);
        Assert.assertNotNull(interaction.getResponseIdentifier());
        ResponseDeclaration responseDeclaration = reloadedItem.getResponseDeclaration(interaction.getResponseIdentifier());
        Assert.assertNotNull(responseDeclaration);
	}
	
	@Test
	public void buildAssessmentItem_hotspot() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		HotspotAssessmentItemBuilder itemBuilder = new HotspotAssessmentItemBuilder("Hotspot", qtiSerializer);
		if(build.booleanValue()) {
			itemBuilder.build();
		}
		
		AssessmentItem assessmentItem = itemBuilder.getAssessmentItem();
		ItemValidationResult itemResult = serializeAndReload(assessmentItem);
        
        AssessmentItem reloadedItem = itemResult.getResolvedAssessmentItem().getItemLookup().extractIfSuccessful();
        List<Interaction> interactions = reloadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof HotspotInteraction);
        HotspotInteraction interaction = (HotspotInteraction)interactions.get(0);
        Assert.assertNotNull(interaction.getResponseIdentifier());
        ResponseDeclaration responseDeclaration = reloadedItem.getResponseDeclaration(interaction.getResponseIdentifier());
        Assert.assertNotNull(responseDeclaration);
	}
	
	@Test
	public void buildAssessmentItem_kprim() throws IOException, URISyntaxException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		KPrimAssessmentItemBuilder itemBuilder = new  KPrimAssessmentItemBuilder("Kprim", "New answer", qtiSerializer);
		if(build.booleanValue()) {
			itemBuilder.build();
		}
		
		AssessmentItem singleChoiceItem = itemBuilder.getAssessmentItem();
		ItemValidationResult itemResult = serializeAndReload(singleChoiceItem);
        
        AssessmentItem reloadedItem = itemResult.getResolvedAssessmentItem().getItemLookup().extractIfSuccessful();
        List<Interaction> interactions = reloadedItem.getItemBody().findInteractions();
        Assert.assertNotNull(interactions);
        Assert.assertEquals(1, interactions.size());
        Assert.assertTrue(interactions.get(0) instanceof MatchInteraction);
        MatchInteraction interaction = (MatchInteraction)interactions.get(0);
        Assert.assertNotNull(interaction.getResponseIdentifier());
        ResponseDeclaration responseDeclaration = reloadedItem.getResponseDeclaration(interaction.getResponseIdentifier());
        Assert.assertNotNull(responseDeclaration);
        
        Assert.assertEquals(2, interaction.getSimpleMatchSets().size());
        SimpleMatchSet matchSet = interaction.getSimpleMatchSets().get(0);
        Assert.assertEquals(4, matchSet.getSimpleAssociableChoices().size());
        SimpleMatchSet correctWrongSet = interaction.getSimpleMatchSets().get(1);
        Assert.assertEquals(2, correctWrongSet.getSimpleAssociableChoices().size());
	}

	/**
	 * The method serialize as xml the assessmentItem, load it, validate it and return the itemResult.
	 * 
	 * @param assessmentItem
	 * @return
	 * @throws IOException
	 */
	protected static ItemValidationResult serializeAndReload(AssessmentItem assessmentItem) throws IOException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		File tmpDir = new File(WebappHelper.getTmpDir(), "itembuilder" + UUID.randomUUID());
		tmpDir.mkdirs();

		File outputFile = new File(tmpDir, "sc_item.xml");
		try(FileOutputStream out = new FileOutputStream(outputFile)) {
			qtiSerializer.serializeJqtiObject(assessmentItem, out);	
		} catch(Exception e) {
			log.error("", e);
		}

		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(outputFile.toPath());
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ItemValidationResult itemResult = assessmentObjectXmlLoader.loadResolveAndValidateItem(outputFile.toURI());

        BadResourceException e = itemResult.getResolvedAssessmentItem().getItemLookup().getBadResourceException();
        if(e != null) {
			StringBuilder err = new StringBuilder();
			BadRessourceHelper.extractMessage(e, err);
			log.error(err.toString());
		}
        
        FileUtils.deleteDirsAndFiles(tmpDir.toPath());
        Assert.assertFalse("Has errors", (itemResult.getModelValidationErrors().size() > 0));
        
        return itemResult;
	}
	
	protected static AssessmentItem loadAssessmentItem(URL itemUrl) throws URISyntaxException {
		QtiXmlReader qtiXmlReader = new QtiXmlReader(new JqtiExtensionManager());
		ResourceLocator fileResourceLocator = new PathResourceLocator(Paths.get(itemUrl.toURI()));
        AssessmentObjectXmlLoader assessmentObjectXmlLoader = new AssessmentObjectXmlLoader(qtiXmlReader, fileResourceLocator);
        ResolvedAssessmentItem item = assessmentObjectXmlLoader.loadAndResolveAssessmentItem(itemUrl.toURI());
		return item.getItemLookup().getRootNodeHolder().getRootNode();
	}

}
