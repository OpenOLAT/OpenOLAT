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

import java.io.IOException;
import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.gui.render.StringOutput;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.table.Table;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.content.Hottext;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.utils.QueryUtils;

/**
 * Test the conversion from TinyMCE HTML code to the QTI Works object
 * model.
 * 
 * 
 * Initial date: 10.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentHtmlBuilderTest {
	
	@Test
	public void serializer() {
		AssessmentItem item = new AssessmentItem();
		SimpleChoice helper = new SimpleChoice(item);
		P p = new P(helper);
		TextRun text = new TextRun(p, "Hello world");
		p.getInlines().add(text);
		helper.getFlowStatics().add(p);
		
		String content = new AssessmentHtmlBuilder().flowStaticString(helper.getFlowStatics());
		Assert.assertTrue(content.contains(">Hello world<"));
	}
	
	@Test
	public void isEmpty() {
		String html = "Hello world";
		boolean contains = new AssessmentHtmlBuilder().containsSomething(html);
		Assert.assertTrue(contains);
		
		html = "<p>&nbsp;</p>";
		contains = new AssessmentHtmlBuilder().containsSomething(html);
		Assert.assertTrue(contains);
		
		html = "<p></p>";
		contains = new AssessmentHtmlBuilder().containsSomething(html);
		Assert.assertTrue(contains);
		
		html = "  ";
		contains = new AssessmentHtmlBuilder().containsSomething(html);
		Assert.assertFalse(contains);
	}
	
	@Test
	public void appendHtml() throws IOException {
		String content = "<p>Test <textEntryInteraction responseIdentifier=\"RESPONSE_1\"/> </p>";

		AssessmentItem item = new AssessmentItem();
		ItemBody helper = new ItemBody(item);
		new AssessmentHtmlBuilder().appendHtml(helper, content);

		List<Interaction> interactions = helper.findInteractions();
		Assert.assertNotNull(interactions);
		Assert.assertEquals(1, interactions.size());
		Interaction interaction = interactions.get(0);
		Assert.assertTrue(interaction instanceof TextEntryInteraction);
		Assert.assertNotNull(interaction.getResponseIdentifier());
		Assert.assertEquals("RESPONSE_1", interaction.getResponseIdentifier().toString());
		
		// The serializer can throw some exceptions if it doesn't like the model
		// we want to serialize.
		StringOutput sb = new StringOutput();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		qtiSerializer.serializeJqtiObject(helper, new StreamResult(sb));
		String serializedQti = sb.toString();
		Assert.assertTrue(serializedQti.contains("textEntryInteraction"));
		sb.close();
	}
	
	@Test
	public void appendHtml_namespace() throws IOException {
		String content = "<p xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.imsglobal.org/xsd/imsqti_v2p1\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsqti_v2p1 http://www.imsglobal.org/xsd/imsqti_v2p1.xsd\">European rocket <textEntryInteraction responseIdentifier=\"RESPONSE_2\" data-qti-solution=\"Ariane\" openolatType=\"string\" placeholderText=\"ari\"/>New text<textEntryInteraction responseIdentifier=\"RESPONSE_1\" data-qti-solution=\"Falcon9\" openolatType=\"string\" placeholderText=\"falc\"/></p>";

		AssessmentItem item = new AssessmentItem();
		ItemBody helper = new ItemBody(item);
		new AssessmentHtmlBuilder().appendHtml(helper, content);

		List<Interaction> interactions = helper.findInteractions();
		Assert.assertNotNull(interactions);
		Assert.assertEquals(2, interactions.size());
		TextEntryInteraction entry1 = (TextEntryInteraction)interactions.get(0);
		Assert.assertEquals("RESPONSE_2", entry1.getResponseIdentifier().toString());
		TextEntryInteraction entry2 = (TextEntryInteraction)interactions.get(1);
		Assert.assertEquals("RESPONSE_1", entry2.getResponseIdentifier().toString());
		
		List<Block> blocks = helper.getBlocks();
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertTrue(blocks.get(0) instanceof P);
		
		// The serializer can throw some exceptions if it doesn't like the model
		// we want to serialize.
		StringOutput sb = new StringOutput();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		qtiSerializer.serializeJqtiObject(helper, new StreamResult(sb));
		String serializedQti = sb.toString();
		Assert.assertTrue(serializedQti.contains("textEntryInteraction"));
		sb.close();
	}
	
	@Test
	public void appendHtml_textEntryInteractions() throws IOException {
		String content = "<p>Usefull for circles <textentryinteraction responseidentifier=\"RESPONSE_2\" data-qti-solution=\"Pi\" data-qti-solution-empty=\"false\"></textentryinteraction>New <br>text<textentryinteraction responseidentifier=\"RESPONSE_1\" data-qti-solution=\"Ln\" data-qti-solution-empty=\"false\"></textentryinteraction></p>";

		AssessmentItem item = new AssessmentItem();
		ItemBody helper = new ItemBody(item);
		new AssessmentHtmlBuilder().appendHtml(helper, content);

		List<Interaction> interactions = helper.findInteractions();
		Assert.assertNotNull(interactions);
		Assert.assertEquals(2, interactions.size());
		TextEntryInteraction entry1 = (TextEntryInteraction)interactions.get(0);
		Assert.assertEquals("RESPONSE_2", entry1.getResponseIdentifier().toString());
		TextEntryInteraction entry2 = (TextEntryInteraction)interactions.get(1);
		Assert.assertEquals("RESPONSE_1", entry2.getResponseIdentifier().toString());
		
		List<Block> blocks = helper.getBlocks();
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertTrue(blocks.get(0) instanceof P);
		
		// The serializer can throw some exceptions if it doesn't like the model
		// we want to serialize.
		StringOutput sb = new StringOutput();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		qtiSerializer.serializeJqtiObject(helper, new StreamResult(sb));
		String serializedQti = sb.toString();
		Assert.assertTrue(serializedQti.contains("textEntryInteraction"));
		sb.close();
	}
	
	@Test
	public void appendHtml_textEntryInteractionsAutoClosed() throws IOException {
		String content = "<p xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns=\"http://www.imsglobal.org/xsd/imsqti_v2p1\" xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsqti_v2p1 http://www.imsglobal.org/xsd/imsqti_v2p1.xsd\">Usefull for circles <textEntryInteraction responseIdentifier=\"RESPONSE_2\" data-qti-solution=\"Pi\" openolatType=\"string\" placeholderText=\"314\" />New text<textEntryInteraction responseIdentifier=\"RESPONSE_1\" data-qti-solution=\"Ln\" openolatType=\"string\" placeholderText=\"lognat\" /></p>";
	
		AssessmentItem item = new AssessmentItem();
		ItemBody helper = new ItemBody(item);
		new AssessmentHtmlBuilder().appendHtml(helper, content);

		List<Interaction> interactions = helper.findInteractions();
		Assert.assertNotNull(interactions);
		Assert.assertEquals(2, interactions.size());
		TextEntryInteraction entry1 = (TextEntryInteraction)interactions.get(0);
		Assert.assertEquals("RESPONSE_2", entry1.getResponseIdentifier().toString());
		TextEntryInteraction entry2 = (TextEntryInteraction)interactions.get(1);
		Assert.assertEquals("RESPONSE_1", entry2.getResponseIdentifier().toString());
		
		List<Block> blocks = helper.getBlocks();
		Assert.assertNotNull(blocks);
		Assert.assertEquals(1, blocks.size());
		Assert.assertTrue(blocks.get(0) instanceof P);
		
		// The serializer can throw some exceptions if it doesn't like the model
		// we want to serialize.
		StringOutput sb = new StringOutput();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		qtiSerializer.serializeJqtiObject(helper, new StreamResult(sb));
		String serializedQti = sb.toString();
		Assert.assertTrue(serializedQti.contains("textEntryInteraction"));
		sb.close();
	}

	@Test
	public void appendHtml_accidentalHtmlRootElement() throws IOException {
		String content = "<html><p>Test \u00EA<strong><span><img src='img.jpg'></span></strong></p><p>Test 2</p></html>";

		AssessmentItem item = new AssessmentItem();
		ItemBody helper = new ItemBody(item);
		new AssessmentHtmlBuilder().appendHtml(helper, content);

		List<Block> paragraphs = helper.getBlocks();
		Assert.assertNotNull(paragraphs);
		Assert.assertEquals(2, paragraphs.size());
		Assert.assertTrue(paragraphs.get(0) instanceof P);
		Assert.assertTrue(paragraphs.get(1) instanceof P);
		
		// The serializer can throw some exceptions if it doesn't like the model
		// we want to serialize.
		StringOutput sb = new StringOutput();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		qtiSerializer.serializeJqtiObject(helper, new StreamResult(sb));
		String serializedQti = sb.toString();
		Assert.assertTrue(serializedQti.contains("img.jpg"));
		Assert.assertTrue(serializedQti.contains("Test \u00EA"));// check encoding
		Assert.assertTrue(serializedQti.contains("Test 2"));
		Assert.assertFalse(serializedQti.contains("<html"));
		sb.close();
	}
	
	@Test
	public void appendHtml_table() throws IOException {
		String content = "<p>This is a hot<hottext identifier=\"ht2efcb885fc4149bdf3fcf567b44a78\">text</hottext> et un dans une table:</p>\n" + 
				"<table class=\"b_default\" style=\"height: 109px;\" width=\"440\">\n" + 
				"<tbody>\n" + 
				"<tr>\n" + 
				"<td style=\"width: 146.34375px;\">Ceci</td>\n" + 
				"<td style=\"width: 146.34375px;\">est</td>\n" + 
				"<td style=\"width: 146.34375px;\">une</td>\n" + 
				"</tr>\n" + 
				"<tr>\n" + 
				"<td style=\"width: 146.34375px;\">table</td>\n" + 
				"<td style=\"width: 146.34375px;\">donc</td>\n" + 
				"<td style=\"width: 146.34375px; border-color: #ed9595; background-color: #edc5c5;\"><hottext identifier=\"ht8096a52c17f663ab9d73b7071609\">table</hottext></td>\n" + 
				"</tr>\n" + 
				"</tbody>\n" + 
				"</table>";

		AssessmentItem item = new AssessmentItem();
		ItemBody helper = new ItemBody(item);
		new AssessmentHtmlBuilder().appendHtml(helper, content);

		List<Block> paragraphs = helper.getBlocks();
		Assert.assertNotNull(paragraphs);
		Assert.assertEquals(2, paragraphs.size());
		Assert.assertTrue(paragraphs.get(0) instanceof P);
		Assert.assertTrue(paragraphs.get(1) instanceof Table);
		
		List<Hottext> hottexts = QueryUtils.search(Hottext.class, helper.getBlocks());
		Assert.assertNotNull(hottexts);
		Assert.assertEquals(2, hottexts.size());
		Assert.assertEquals("ht2efcb885fc4149bdf3fcf567b44a78", hottexts.get(0).getIdentifier().toString());
		Assert.assertEquals("ht8096a52c17f663ab9d73b7071609", hottexts.get(1).getIdentifier().toString());
		
		// The serializer can throw some exceptions if it doesn't like the model
		// we want to serialize.
		StringOutput sb = new StringOutput();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		qtiSerializer.serializeJqtiObject(helper, new StreamResult(sb));
		String serializedQti = sb.toString();
		Assert.assertTrue(serializedQti.contains("ht2efcb885fc4149bdf3fcf567b44a78"));
		Assert.assertTrue(serializedQti.contains("ht8096a52c17f663ab9d73b7071609"));
		Assert.assertTrue(serializedQti.contains("background-color: #edc5c5;"));
		Assert.assertTrue(serializedQti.contains("width: 146.34375px;"));
		Assert.assertFalse(serializedQti.contains("ns:identifier"));// check namespaced attributes
		Assert.assertFalse(serializedQti.contains("ns:style"));
		sb.close();
	}

	@Test
	public void appendHtml_serializeVideo() throws IOException {
		String content = "<p><span id=\"olatFlashMovieViewer213060\" class=\"olatFlashMovieViewer\" style=\"display:block;border:solid 1px #000; width:320px; height:240px;\">\n"
			+ "<script src=\"/raw/fx-111111x11/movie/player.js\"></script>\n"
			+ "<script defer=\"defer\">// <![CDATA[\n"
			+ "BPlayer.insertPlayer(\"demo-video.mp4\",\"olatFlashMovieViewer213060\",320,240,0,0,\"video\",undefined,false,false,true,undefined);\n"
			+ "// ]]></script>\n"
			+ "</span></p>";

		AssessmentItem item = new AssessmentItem();
		ItemBody helper = new ItemBody(item);
		new AssessmentHtmlBuilder().appendHtml(helper, content);

		List<Block> paragraphs = helper.getBlocks();
		Assert.assertNotNull(paragraphs);
		Assert.assertEquals(1, paragraphs.size());

		StringOutput sb = new StringOutput();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		qtiSerializer.serializeJqtiObject(helper, new StreamResult(sb));
		String serializedQti =  sb.toString();
		Assert.assertNotNull(serializedQti);
		Assert.assertTrue(serializedQti.contains("object"));
		Assert.assertFalse(serializedQti.contains("span"));
		Assert.assertFalse(serializedQti.contains("script"));
		Assert.assertFalse(serializedQti.contains("ns:data"));
		sb.close();
	}
}
