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

import java.util.List;

import javax.xml.transform.stream.StreamResult;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.basic.TextRun;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;
import uk.ac.ed.ph.jqtiplus.node.item.AssessmentItem;
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
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentHtmlBuilderTest.class);
	
	@Test
	public void serializer() {
		AssessmentItem item = new AssessmentItem();
		SimpleChoice helper = new SimpleChoice(item);
		P p = new P(helper);
		TextRun text = new TextRun(p, "Hello world");
		p.getInlines().add(text);
		helper.getFlowStatics().add(p);
		
		String content = new AssessmentHtmlBuilder().flowStaticString(helper.getFlowStatics());
		System.out.println(content);
	}
	

	@Test
	public void filter() {
		String content = "<html><p>Test \u00EA<strong><span><img = src='img.jpg'></span></strong></p><p>Test 2</p></html>";

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
		log.info(sb.toString());
	}
}
