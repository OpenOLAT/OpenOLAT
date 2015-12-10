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

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.cyberneko.html.parsers.DOMParser;
import org.jcodec.common.Assert;
import org.junit.Test;
import org.olat.core.util.filter.FilterFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.exception.QtiModelException;
import uk.ac.ed.ph.jqtiplus.node.LoadingContext;
import uk.ac.ed.ph.jqtiplus.node.QtiNode;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.xhtml.text.P;

/**
 * 
 * Initial date: 07.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class ConvertHTMLTestTest {
	
	@Test
	public void convert() {
		String content = "<html><p>Test</p><p>Test 2</p></html>";
		Document partialDocument = getDoc(content);
		Element rootElement = partialDocument.getDocumentElement();
	

		
		MyLoadingContext context = new MyLoadingContext();
		
		Element paragraphEl = (Element)rootElement.getFirstChild();
		String tagName = paragraphEl.getTagName();
		String localName = paragraphEl.getLocalName();

		ItemBody itemBody = new ItemBody(null);
		itemBody.load(rootElement, context);
		
		Assert.assertNotNull(localName);
		Assert.assertEquals(2, itemBody.getBlocks().size());

		
		
	}
	
	private Document getDoc(String content) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(content.getBytes()));
			return doc;
		} catch (ParserConfigurationException | SAXException | IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return null;
		}
	}
	
	private Document getDocument(String content) {
		try {
			DOMParser parser = new DOMParser();
	        parser.setFeature("http://xml.org/sax/features/validation", false);
	       
			parser.setFeature( "http://cyberneko.org/html/features/override-namespaces", true);
			parser.setFeature ( "http://xml.org/sax/features/namespaces", true );
			parser.setProperty("http://cyberneko.org/html/properties/names/elems", "upper" ); // has no effect, cannot override xerces configuration
			parser.setProperty( "http://cyberneko.org/html/properties/names/attrs", "upper" ); // has no effect, cannot override xerces configuration
			parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment",true);
			parser.parse(new InputSource(new StringReader(content)));
			return parser.getDocument();
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}
	
	
	private List<P> getText(QtiNode choice, String htmlContent) {
		String text = FilterFactory.getHtmlTagsFilter().filter(htmlContent);
		P firstChoiceText = AssessmentItemFactory.getParagraph(choice, text);
		List<P> blocks = new ArrayList<>();
		blocks.add(firstChoiceText);
		return blocks;
	}
	
	private static final class MyLoadingContext implements LoadingContext {

		@Override
		public JqtiExtensionManager getJqtiExtensionManager() {
			//
			return null;
		}

		@Override
		public void modelBuildingError(QtiModelException exception, Node badNode) {
			//
		}
		
	}

}
