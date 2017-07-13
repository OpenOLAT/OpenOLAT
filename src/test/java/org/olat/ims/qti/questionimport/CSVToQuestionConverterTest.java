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
package org.olat.ims.qti.questionimport;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.core.gui.translator.Translator;
import org.olat.ims.qti.editor.QTIEditHelper;
import org.olat.ims.qti.editor.beecom.objects.ChoiceQuestion;
import org.olat.ims.qti.editor.beecom.objects.FIBQuestion;
import org.olat.ims.qti.editor.beecom.objects.FIBResponse;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Material;
import org.olat.ims.qti.editor.beecom.objects.Mattext;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;
import org.olat.ims.qti.editor.beecom.objects.Question;
import org.olat.ims.qti.editor.beecom.objects.Response;
import org.olat.test.KeyTranslator;

/**
 * 
 * Initial date: 24.09.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSVToQuestionConverterTest {
	
	@Test
	public void importMultipleChoice() throws IOException, URISyntaxException {
		URL importTxtUrl = CSVToQuestionConverterTest.class.getResource("question_import_mc.txt");
		Assert.assertNotNull(importTxtUrl);
		File importTxt = new File(importTxtUrl.toURI());
		String input = FileUtils.readFileToString(importTxt, "UTF-8");
		
		Translator translator = new KeyTranslator(Locale.ENGLISH);
		ImportOptions options = new ImportOptions();
		options.setShuffle(true);
		CSVToQuestionConverter converter = new CSVToQuestionConverter(translator, options);
		converter.parse(input);
		
		List<ItemAndMetadata> items = converter.getItems();
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		
		ItemAndMetadata importedItem = items.get(0);
		Item item = importedItem.getItem();
		Assert.assertNotNull(item);
		Assert.assertEquals("Fussball: Austragungsort", item.getTitle());
		Assert.assertEquals("Die Fussball WM wird alle vier Jahre von einem anderen Land ausgerichtet.", item.getObjectives());
		
		Assert.assertEquals(Question.TYPE_MC, item.getQuestion().getType());
		Assert.assertTrue(item.getQuestion() instanceof ChoiceQuestion);
		Material questionMat = item.getQuestion().getQuestion();
		Assert.assertNotNull(questionMat);
		Assert.assertNotNull(questionMat.getElements());
		Assert.assertEquals(1, questionMat.getElements().size());
		QTIObject questionMatEl = questionMat.getElements().get(0);
		Assert.assertTrue(questionMatEl instanceof Mattext);
		String text = ((Mattext)questionMatEl).getContent();
		Assert.assertEquals("In welchen L\u00E4ndern wurde zwischen dem Jahr 2000 und 2015 eine Fussball Weltmeisterschaft ausgetragen?", text);
		
		ChoiceQuestion question = (ChoiceQuestion)item.getQuestion();
		Assert.assertNotNull(question.getResponses());
		Assert.assertEquals(7, question.getResponses().size());
		
		List<Response> responses = question.getResponses();
		Assert.assertEquals(1.0f, responses.get(0).getPoints(), 0.0001);
		Assert.assertEquals(1.0f, responses.get(1).getPoints(), 0.0001);
		Assert.assertEquals(1.0f, responses.get(2).getPoints(), 0.0001);
		Assert.assertEquals(-1.0f, responses.get(3).getPoints(), 0.0001);
		Assert.assertEquals(-1.0f, responses.get(4).getPoints(), 0.0001);
		Assert.assertEquals(-1.0f, responses.get(5).getPoints(), 0.0001);
		Assert.assertEquals(-1.0f, responses.get(6).getPoints(), 0.0001);

		//after it will be set to true for all of them
		Assert.assertTrue(responses.get(0).isCorrect());
		Assert.assertTrue(responses.get(1).isCorrect());
		Assert.assertTrue(responses.get(2).isCorrect());
		Assert.assertFalse(responses.get(3).isCorrect());
		Assert.assertFalse(responses.get(4).isCorrect());
		Assert.assertFalse(responses.get(5).isCorrect());
		Assert.assertFalse(responses.get(6).isCorrect());

		String feedbackMastery = QTIEditHelper.getFeedbackMasteryText(item);
		Assert.assertEquals("Bravo! Die Antwort ich absolut korrekt.", feedbackMastery);
		String feedbackFail = QTIEditHelper.getFeedbackFailText(item);
		Assert.assertEquals("Leider falsch. Probieren Sie es noch einmal.", feedbackFail);
		
	}
	
	@Test
	public void importFillInBlanck() throws IOException, URISyntaxException {
		URL importTxtUrl = CSVToQuestionConverterTest.class.getResource("question_import_fib.txt");
		Assert.assertNotNull(importTxtUrl);
		File importTxt = new File(importTxtUrl.toURI());
		String input = FileUtils.readFileToString(importTxt, "UTF-8");
		
		Translator translator = new KeyTranslator(Locale.ENGLISH);
		ImportOptions options = new ImportOptions();
		options.setShuffle(true);
		CSVToQuestionConverter converter = new CSVToQuestionConverter(translator, options);
		converter.parse(input);
		
		List<ItemAndMetadata> items = converter.getItems();
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		
		ItemAndMetadata importedItem = items.get(0);
		Item item = importedItem.getItem();
		Assert.assertNotNull(item);
		Assert.assertEquals(Question.TYPE_FIB, item.getQuestion().getType());
		Assert.assertTrue(item.getQuestion() instanceof FIBQuestion);

		FIBQuestion question = (FIBQuestion)item.getQuestion();
		List<Response> responses = question.getResponses();
		Assert.assertNotNull(responses);
		Assert.assertEquals(7, responses.size());
		//check java type
		for(Response response:responses) {
			Assert.assertTrue(response instanceof FIBResponse);
		}
		
		//check type
		Assert.assertEquals(FIBResponse.TYPE_CONTENT, ((FIBResponse)responses.get(0)).getType());
		Assert.assertEquals(FIBResponse.TYPE_BLANK, ((FIBResponse)responses.get(1)).getType());
		Assert.assertEquals(FIBResponse.TYPE_CONTENT, ((FIBResponse)responses.get(2)).getType());
		Assert.assertEquals(FIBResponse.TYPE_BLANK, ((FIBResponse)responses.get(3)).getType());
		Assert.assertEquals(FIBResponse.TYPE_CONTENT, ((FIBResponse)responses.get(4)).getType());
		Assert.assertEquals(FIBResponse.TYPE_BLANK, ((FIBResponse)responses.get(5)).getType());
		Assert.assertEquals(FIBResponse.TYPE_CONTENT, ((FIBResponse)responses.get(6)).getType());
		
		//check size
		Assert.assertEquals(2, ((FIBResponse)responses.get(1)).getSize());
		Assert.assertEquals(2, ((FIBResponse)responses.get(3)).getSize());
		Assert.assertEquals(2, ((FIBResponse)responses.get(5)).getSize());
		
		//check max length
		Assert.assertEquals(2, ((FIBResponse)responses.get(1)).getMaxLength());
		Assert.assertEquals(2, ((FIBResponse)responses.get(3)).getMaxLength());
		Assert.assertEquals(2, ((FIBResponse)responses.get(5)).getMaxLength());
	}
	
	@Test
	public void importFillInBlanck_en_metadata() throws IOException, URISyntaxException {
		URL importTxtUrl = CSVToQuestionConverterTest.class.getResource("question_import_fib_en_metadata.txt");
		Assert.assertNotNull(importTxtUrl);
		File importTxt = new File(importTxtUrl.toURI());
		String input = FileUtils.readFileToString(importTxt, "UTF-8");
		
		Translator translator = new KeyTranslator(Locale.ENGLISH);
		ImportOptions options = new ImportOptions();
		options.setShuffle(true);
		CSVToQuestionConverter converter = new CSVToQuestionConverter(translator, options);
		converter.parse(input);
		
		List<ItemAndMetadata> items = converter.getItems();
		Assert.assertNotNull(items);
		Assert.assertEquals(1, items.size());
		
		ItemAndMetadata importedItem = items.get(0);
		Item item = importedItem.getItem();
		Assert.assertNotNull(item);
		Assert.assertEquals(Question.TYPE_FIB, item.getQuestion().getType());
		Assert.assertTrue(item.getQuestion() instanceof FIBQuestion);

		FIBQuestion question = (FIBQuestion)item.getQuestion();
		List<Response> responses = question.getResponses();
		Assert.assertNotNull(responses);
		Assert.assertEquals(2, responses.size());
		//check java type
		for(Response response:responses) {
			Assert.assertTrue(response instanceof FIBResponse);
		}
		
		//check type
		Assert.assertEquals(FIBResponse.TYPE_CONTENT, ((FIBResponse)responses.get(0)).getType());
		Assert.assertEquals(FIBResponse.TYPE_BLANK, ((FIBResponse)responses.get(1)).getType());
		//check size
		Assert.assertEquals(20, ((FIBResponse)responses.get(1)).getSize());
		//check max length
		Assert.assertEquals(50, ((FIBResponse)responses.get(1)).getMaxLength());
	}
		
}