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
package org.olat.ims.qti21.questionimport;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Locale;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.interactions.FIBAssessmentItemBuilder.NumericalEntry;
import org.olat.ims.qti21.model.xml.interactions.InlineChoiceAssessmentItemBuilder;
import org.olat.test.JunitTestHelper;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.expression.operator.ToleranceMode;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.InlineChoiceInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.InlineChoice;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;

/**
 * 
 * Initial date: 7 oct. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CSVToAssessmentItemConverterTest {
	
	@Test
	public void importFib() throws IOException {
		ImportOptions options = new ImportOptions();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		CSVToAssessmentItemConverter converter = new CSVToAssessmentItemConverter(options, Locale.ENGLISH, qtiSerializer);
		try(InputStream inStream = JunitTestHelper.class.getResourceAsStream("file_resources/qti21/import_qti21_fib.txt")) {
			String input = IOUtils.toString(inStream, StandardCharsets.UTF_8);
			converter.parse(input);
		} catch(IOException e) {
			throw e;
		}
		
		List<AssessmentItemAndMetadata> itemsAndData = converter.getItems();
		Assert.assertNotNull(itemsAndData);
		Assert.assertEquals(2, itemsAndData.size());
		
		// first item
		AssessmentItemAndMetadata firstItem = itemsAndData.get(0);
		AssessmentItemBuilder firstBuilder = firstItem.getItemBuilder();
		Assert.assertEquals(1.0d, firstBuilder.getMaxScoreBuilder().getScore().doubleValue(), 0.00001);
		
		List<Interaction> interactions = firstBuilder.getAssessmentItem().getItemBody().findInteractions();
		Assert.assertEquals(1, interactions.size());
		TextEntryInteraction textEntry = (TextEntryInteraction)interactions.get(0);
		Assert.assertEquals(Integer.valueOf(21), textEntry.getExpectedLength());
		
		// second item
		AssessmentItemAndMetadata secondItem = itemsAndData.get(1);
		AssessmentItemBuilder secondBuilder = secondItem.getItemBuilder();
		Assert.assertEquals(3.0d, secondBuilder.getMaxScoreBuilder().getScore().doubleValue(), 0.00001);
		
		List<Interaction> threeInteractions = secondBuilder.getAssessmentItem().getItemBody().findInteractions();
		Assert.assertEquals(3, threeInteractions.size());
		TextEntryInteraction textEntry1 = (TextEntryInteraction)threeInteractions.get(0);
		Assert.assertEquals(Integer.valueOf(2), textEntry1.getExpectedLength());
		TextEntryInteraction textEntry2 = (TextEntryInteraction)threeInteractions.get(1);
		Assert.assertEquals(Integer.valueOf(3), textEntry2.getExpectedLength());
		TextEntryInteraction textEntry3 = (TextEntryInteraction)threeInteractions.get(2);
		Assert.assertEquals(Integer.valueOf(4), textEntry3.getExpectedLength());
	}
	
	@Test
	public void importNumerical() throws IOException {
		ImportOptions options = new ImportOptions();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		CSVToAssessmentItemConverter converter = new CSVToAssessmentItemConverter(options, Locale.ENGLISH, qtiSerializer);
		try(InputStream inStream = JunitTestHelper.class.getResourceAsStream("file_resources/qti21/import_qti21_numerical.txt")) {
			String input = IOUtils.toString(inStream, StandardCharsets.UTF_8);
			converter.parse(input);
		} catch(IOException e) {
			throw e;
		}
		
		List<AssessmentItemAndMetadata> itemsAndData = converter.getItems();
		Assert.assertNotNull(itemsAndData);
		Assert.assertEquals(1, itemsAndData.size());
		
		// first item
		AssessmentItemAndMetadata item = itemsAndData.get(0);
		FIBAssessmentItemBuilder builder = (FIBAssessmentItemBuilder)item.getItemBuilder();
		Assert.assertEquals(3.0d, builder.getMaxScoreBuilder().getScore().doubleValue(), 0.00001);
		
		List<Interaction> interactions = builder.getAssessmentItem().getItemBody().findInteractions();
		Assert.assertEquals(3, interactions.size());
		
		// check first
		TextEntryInteraction textEntry1 = (TextEntryInteraction)interactions.get(0);
		NumericalEntry numerical1 = (NumericalEntry)builder.getEntry(textEntry1.getResponseIdentifier().toString());
		Assert.assertEquals(42.0d, numerical1.getSolution(), 0.00001);
		Assert.assertNull(numerical1.getLowerTolerance());
		Assert.assertNull(numerical1.getUpperTolerance());
		Assert.assertEquals(ToleranceMode.EXACT, numerical1.getToleranceMode());

		TextEntryInteraction textEntry2 = (TextEntryInteraction)interactions.get(1);
		NumericalEntry numerical2 = (NumericalEntry)builder.getEntry(textEntry2.getResponseIdentifier().toString());
		Assert.assertEquals(1.41d, numerical2.getSolution(), 0.00001);
		Assert.assertEquals(0.01d, numerical2.getLowerTolerance(), 0.00001);
		Assert.assertEquals(0.01d, numerical2.getUpperTolerance(), 0.00001);
		Assert.assertEquals(ToleranceMode.ABSOLUTE, numerical2.getToleranceMode());
		
		TextEntryInteraction textEntry3 = (TextEntryInteraction)interactions.get(2);
		NumericalEntry numerical3 = (NumericalEntry)builder.getEntry(textEntry3.getResponseIdentifier().toString());
		Assert.assertEquals(20.0d, numerical3.getSolution(), 0.00001);
		Assert.assertEquals(10.0d, numerical3.getLowerTolerance(), 0.00001);
		Assert.assertEquals(10.0d, numerical3.getUpperTolerance(), 0.00001);
		Assert.assertEquals(ToleranceMode.RELATIVE, numerical3.getToleranceMode());
	}
	
	@Test
	public void importInlineChoices() throws IOException {
		ImportOptions options = new ImportOptions();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		CSVToAssessmentItemConverter converter = new CSVToAssessmentItemConverter(options, Locale.ENGLISH, qtiSerializer);
		try(InputStream inStream = JunitTestHelper.class.getResourceAsStream("file_resources/qti21/import_qti21_inlinechoices.txt")) {
			String input = IOUtils.toString(inStream, StandardCharsets.UTF_8);
			converter.parse(input);
		} catch(IOException e) {
			throw e;
		}
		
		List<AssessmentItemAndMetadata> itemsAndData = converter.getItems();
		Assert.assertNotNull(itemsAndData);
		Assert.assertEquals(1, itemsAndData.size());
		
		// first item
		AssessmentItemAndMetadata item = itemsAndData.get(0);
		AssessmentItemBuilder itemBuilder = item.getItemBuilder();
		Assert.assertEquals(1.0d, itemBuilder.getMaxScoreBuilder().getScore().doubleValue(), 0.00001);
		
		List<Interaction> interactions = itemBuilder.getAssessmentItem().getItemBody().findInteractions();
		Assert.assertEquals(4, interactions.size());
		
		InlineChoiceInteraction inlineChoiceInteraction = (InlineChoiceInteraction)interactions.get(0);
		List<InlineChoice> inlineChoices = inlineChoiceInteraction.getInlineChoices();
		Assert.assertEquals("", InlineChoiceAssessmentItemBuilder.getText(inlineChoices.get(0)));
		Assert.assertEquals(".", InlineChoiceAssessmentItemBuilder.getText(inlineChoices.get(1)));
		Assert.assertEquals(";", InlineChoiceAssessmentItemBuilder.getText(inlineChoices.get(2)));
		Assert.assertEquals("!", InlineChoiceAssessmentItemBuilder.getText(inlineChoices.get(3)));
	}
}
