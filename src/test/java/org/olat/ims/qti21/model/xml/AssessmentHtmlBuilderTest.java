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
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.Interaction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.TextEntryInteraction;
import uk.ac.ed.ph.jqtiplus.node.item.interaction.choice.SimpleChoice;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;

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
	public void filter_alt() {
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
	}

	@Test
	public void filter() throws IOException {
		String content = "<html><p>Test \u00EA<strong><span><img src='img.jpg'></span></strong></p><p>Test 2</p></html>";

		AssessmentItem item = new AssessmentItem();
		ItemBody helper = new ItemBody(item);
		new AssessmentHtmlBuilder().appendHtml(helper, content);

		List<Block> paragraphs = helper.getBlocks();
		Assert.assertNotNull(paragraphs);
		Assert.assertEquals(2, paragraphs.size());
		
		// The serializer can throw some exceptions if it doens't like the model
		// we want to serialize.
		StringOutput sb = new StringOutput();
		QtiSerializer qtiSerializer = new QtiSerializer(new JqtiExtensionManager());
		qtiSerializer.serializeJqtiObject(helper, new StreamResult(sb));
		String serializedQti = sb.toString();
		Assert.assertTrue(serializedQti.contains("img.jpg"));
		sb.close();
	}

	@Test
	public void serializeVideo() throws IOException {
		String content = "<p><span id=\"olatFlashMovieViewer213060\" class=\"olatFlashMovieViewer\" style=\"display:block;border:solid 1px #000; width:320px; height:240px;\">\n"
			+ "<script src=\"/raw/fx-111111x11/movie/player.js\" type=\"text/javascript\"></script>\n"
			+ "<script type=\"text/javascript\" defer=\"defer\">// <![CDATA[\n"
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
		sb.close();
	}
}
