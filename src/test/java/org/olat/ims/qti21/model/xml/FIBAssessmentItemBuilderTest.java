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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.apache.logging.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.ims.qti21.QTI21Constants;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.EntryType;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntry;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.TextEntryAlternative;
import org.olat.ims.qti21.model.xml.interactions.SimpleChoiceAssessmentItemBuilder.ScoreEvaluation;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.types.Identifier;
import uk.ac.ed.ph.jqtiplus.types.ResponseData;
import uk.ac.ed.ph.jqtiplus.types.StringResponseData;
import uk.ac.ed.ph.jqtiplus.validation.ItemValidationResult;
import uk.ac.ed.ph.jqtiplus.value.FloatValue;
import uk.ac.ed.ph.jqtiplus.value.Value;

/**
 * 
 * Initial date: 13 déc. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FIBAssessmentItemBuilderTest {
	
	private static final Logger log = Tracing.createLoggerFor(FIBAssessmentItemBuilderTest.class);
	
	@Test
	public void createTextEntry_text_duplicatesForbidden_scoreAll() throws IOException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		FIBAssessmentItemBuilder itemBuilder = new FIBAssessmentItemBuilder("Only texts", EntryType.text, qtiSerializer);
		
		List<TextEntryAlternative> entryAlternatives = toAlternatives(1.0d, "Berset", "Sommaruga", "Cassis");

		String responseIdentifier1 = itemBuilder.generateResponseIdentifier();
		TextEntry entry1 = itemBuilder.createTextEntry(responseIdentifier1);
		entry1.setAlternatives(entryAlternatives);
		entry1.setSolution("Sommaruga");
		entry1.setScore(1.0d);

		String responseIdentifier2 = itemBuilder.generateResponseIdentifier();
		TextEntry entry2 = itemBuilder.createTextEntry(responseIdentifier2);
		entry2.setAlternatives(entryAlternatives);
		entry2.setSolution("Berset");
		entry2.setScore(1.0d);

		itemBuilder.setQuestion("<p>New text <textEntryInteraction responseIdentifier=\"" + responseIdentifier1 + "\" data-qti-solution=\"gap\" openolatType=\"string\"/> <textEntryInteraction responseIdentifier=\"" + responseIdentifier2 + "\" data-qti-solution=\"gap\" openolatType=\"string\"/></p>");
		itemBuilder.setMinScore(0.0d);
		itemBuilder.setMaxScore(2.0d);
		itemBuilder.setAllowDuplicatedAnswers(false);
		itemBuilder.setScoreEvaluationMode(ScoreEvaluation.allCorrectAnswers);
		itemBuilder.build();
		
		ItemValidationResult itemResult = AssessmentItemBuilderTest.serializeAndReload(itemBuilder.getAssessmentItem());
        AssessmentItem reloadedItem = itemResult.getResolvedAssessmentItem().getItemLookup().extractIfSuccessful();
        List<Interaction> interactions = reloadedItem.getItemBody().findInteractions();
        Assert.assertEquals(2, interactions.size());
        
        
        File itemFile = new File(WebappHelper.getTmpDir(), "fibAssessmentItem" + UUID.randomUUID() + ".xml");
		try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(itemBuilder.getAssessmentItem(), out);
		} catch(Exception e) {
			log.error("", e);
		}
		
		{// correct answers
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Sommaruga"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Berset"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(2.0d), score);
		}
		
		{// twice same answer
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Sommaruga"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Sommaruga"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(0.0d), score);
		}
		
		{// wrong answer
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Werner"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Johnatan"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(0.0d), score);
		}

		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	

	@Test
	public void createTextEntry_text_duplicatesForbidden_scorePerAnswers() throws IOException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		FIBAssessmentItemBuilder itemBuilder = new FIBAssessmentItemBuilder("Only texts", EntryType.text, qtiSerializer);
		
		List<TextEntryAlternative> entryAlternatives = toAlternatives(1.0d, "Jupiter", "Saturne", "Uranus", "Neptune");
		
		String responseIdentifier1 = itemBuilder.generateResponseIdentifier();
		TextEntry entry1 = itemBuilder.createTextEntry(responseIdentifier1);
		entry1.setAlternatives(entryAlternatives);
		entry1.setSolution("Jupiter");
		entry1.setScore(1.0d);

		String responseIdentifier2 = itemBuilder.generateResponseIdentifier();
		TextEntry entry2 = itemBuilder.createTextEntry(responseIdentifier2);
		entry2.setAlternatives(entryAlternatives);
		entry2.setSolution("Saturne");
		entry2.setScore(1.0d);
		
		String responseIdentifier3 = itemBuilder.generateResponseIdentifier();
		TextEntry entry3 = itemBuilder.createTextEntry(responseIdentifier3);
		entry3.setAlternatives(entryAlternatives);
		entry3.setSolution("Uranus");
		entry3.setScore(1.0d);

		itemBuilder.setQuestion("<p>Plan\u00E8te <textEntryInteraction responseIdentifier=\"" + responseIdentifier1 + "\" data-qti-solution=\"gap\" openolatType=\"string\"/> <textEntryInteraction responseIdentifier=\"" + responseIdentifier2 + "\" data-qti-solution=\"gap\" openolatType=\"string\"/> <textEntryInteraction responseIdentifier=\"" + responseIdentifier3 + "\"/></p>");
		itemBuilder.setMinScore(0.0d);
		itemBuilder.setMaxScore(3.0d);
		itemBuilder.setAllowDuplicatedAnswers(false);
		itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		itemBuilder.build();
		
		ItemValidationResult itemResult = AssessmentItemBuilderTest.serializeAndReload(itemBuilder.getAssessmentItem());
        AssessmentItem reloadedItem = itemResult.getResolvedAssessmentItem().getItemLookup().extractIfSuccessful();
        List<Interaction> interactions = reloadedItem.getItemBody().findInteractions();
        Assert.assertEquals(3, interactions.size());
 
        File itemFile = new File(WebappHelper.getTmpDir(), "fibAssessmentItem" + UUID.randomUUID() + ".xml");
		try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(itemBuilder.getAssessmentItem(), out);
		} catch(Exception e) {
			log.error("", e);
		}
		
		{// correct answers
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Jupiter"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Saturne"));
	        responseMap.put(Identifier.parseString(responseIdentifier3), new StringResponseData("uranus"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(3.0d), score);
		}
		
		{// twice the same answer
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Jupiter"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Saturne"));
	        responseMap.put(Identifier.parseString(responseIdentifier3), new StringResponseData("Jupiter"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(2.0d), score);
		}
		
		{// 3x the same answer
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Jupiter"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Jupiter"));
	        responseMap.put(Identifier.parseString(responseIdentifier3), new StringResponseData("Jupiter"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(1.0d), score);
		}
		
		{// wrong answer
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Ceres"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Terre"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(0.0d), score);
		}
	
		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	
	@Test
	public void createTextEntry_text_duplicatesForbidden_nothingShared_scorePerAnswers() throws IOException {
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		FIBAssessmentItemBuilder itemBuilder = new FIBAssessmentItemBuilder("Only texts", EntryType.text, qtiSerializer);
		
		List<TextEntryAlternative> bigAlternatives =  toAlternatives(1.0, "Jupiter", "Saturne", "Uranus", "Neptune");
		String responseIdentifier1 = itemBuilder.generateResponseIdentifier();
		TextEntry entry1 = itemBuilder.createTextEntry(responseIdentifier1);
		entry1.setAlternatives(bigAlternatives);
		entry1.setSolution("Jupiter");
		entry1.setScore(1.0d);
		
		List<TextEntryAlternative> smallAlternatives =  toAlternatives(1.0, "Terre", "Mercure", "Mars", "Venus");
		String responseIdentifier2 = itemBuilder.generateResponseIdentifier();
		TextEntry entry2 = itemBuilder.createTextEntry(responseIdentifier2);
		entry2.setAlternatives(smallAlternatives);
		entry2.setSolution("Terre");
		entry2.setScore(1.0d);

		List<TextEntryAlternative> verySmallAlternatives =  toAlternatives(1.0, "Pluton", "Ceres");
		String responseIdentifier3 = itemBuilder.generateResponseIdentifier();
		TextEntry entry3 = itemBuilder.createTextEntry(responseIdentifier3);
		entry3.setAlternatives(verySmallAlternatives);
		entry3.setSolution("Pluton");
		entry3.setScore(1.0d);

		itemBuilder.setQuestion("<p>Plan\u00E8te <textEntryInteraction responseIdentifier=\"" + responseIdentifier1 + "\" data-qti-solution=\"gap\" openolatType=\"string\"/> <textEntryInteraction responseIdentifier=\"" + responseIdentifier2 + "\" data-qti-solution=\"gap\" openolatType=\"string\"/> <textEntryInteraction responseIdentifier=\"" + responseIdentifier3 + "\"/></p>");
		itemBuilder.setMinScore(0.0d);
		itemBuilder.setMaxScore(3.0d);
		itemBuilder.setAllowDuplicatedAnswers(false);
		itemBuilder.setScoreEvaluationMode(ScoreEvaluation.perAnswer);
		itemBuilder.build();
		
		ItemValidationResult itemResult = AssessmentItemBuilderTest.serializeAndReload(itemBuilder.getAssessmentItem());
        AssessmentItem reloadedItem = itemResult.getResolvedAssessmentItem().getItemLookup().extractIfSuccessful();
        List<Interaction> interactions = reloadedItem.getItemBody().findInteractions();
        Assert.assertEquals(3, interactions.size());
 
        File itemFile = new File(WebappHelper.getTmpDir(), "fibAssessmentItem" + UUID.randomUUID() + ".xml");
		try(FileOutputStream out = new FileOutputStream(itemFile)) {
			qtiSerializer.serializeJqtiObject(itemBuilder.getAssessmentItem(), out);
		} catch(Exception e) {
			log.error("", e);
		}
		
		{// correct answers
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Jupiter"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Mars"));
	        responseMap.put(Identifier.parseString(responseIdentifier3), new StringResponseData("Ceres"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(3.0d), score);
		}
		
		{// twice the same answer, second is wrong
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Jupiter"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Jupiter"));
	        responseMap.put(Identifier.parseString(responseIdentifier3), new StringResponseData("Ceres"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(2.0d), score);
		}
		
		{// 3x the same answer, two are wrong
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Jupiter"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Jupiter"));
	        responseMap.put(Identifier.parseString(responseIdentifier3), new StringResponseData("Jupiter"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(1.0d), score);
		}
		
		{// wrong answer
			Map<Identifier, ResponseData> responseMap = new HashMap<>();
	        responseMap.put(Identifier.parseString(responseIdentifier1), new StringResponseData("Ceres"));
	        responseMap.put(Identifier.parseString(responseIdentifier2), new StringResponseData("Jupiter"));
			ItemSessionController itemSessionController = RunningItemHelper.run(itemFile, responseMap);
			Value score = itemSessionController.getItemSessionState().getOutcomeValue(QTI21Constants.SCORE_IDENTIFIER);
			Assert.assertEquals(new FloatValue(0.0d), score);
		}
	
		FileUtils.deleteDirsAndFiles(itemFile.toPath());
	}
	
	private List<TextEntryAlternative> toAlternatives(double score, String... strings) {
		List<TextEntryAlternative> entryAlternatives = new ArrayList<>();
		for(String string:strings) {
			TextEntryAlternative alternative = new TextEntryAlternative();
			alternative.setAlternative(string);
			alternative.setScore(score);
			entryAlternatives.add(alternative);
		}
		return entryAlternatives;
	}


}
