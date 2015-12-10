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
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.stream.StreamResult;

import org.cyberneko.html.parsers.SAXParser;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.exception.QtiModelException;
import uk.ac.ed.ph.jqtiplus.node.AbstractNode;
import uk.ac.ed.ph.jqtiplus.node.LoadingContext;
import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;
import uk.ac.ed.ph.jqtiplus.xmlutils.SimpleDomBuilderHandler;

/**
 * Do the ugly job to convert the Tiny MCE HTML code to the object model
 * of QTI Works
 * 
 * Initial date: 10.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentHtmlBuilder {
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentHtmlBuilder.class);
	
	private final QtiSerializer qtiSerializer;
	
	public AssessmentHtmlBuilder() {
		JqtiExtensionManager jqtiExtensionManager = new JqtiExtensionManager();
		qtiSerializer = new QtiSerializer(jqtiExtensionManager);
	}
	
	public AssessmentHtmlBuilder(QtiSerializer qtiSerializer) {
		this.qtiSerializer = qtiSerializer;
	}
	
	public String toString(List<FlowStatic> statics) {
		StringOutput sb = new StringOutput();
		if(statics != null && statics.size() > 0) {
			for(FlowStatic flowStatic:statics) {
				qtiSerializer.serializeJqtiObject(flowStatic, new StreamResult(sb));
			}
		}
		return sb.toString();
	}
	
	public void appendHtml(AbstractNode parent, String htmlFragment) {
		//tinymce bad habits
		if(htmlFragment.startsWith("<p>&nbsp;")) {
			htmlFragment = htmlFragment.replace("<p>&nbsp;", "<p>");
		}
		//wrap around <html> to have a root element
		Document document = filter("<html>" + htmlFragment + "</html>");
		parent.getNodeGroups().load(document.getDocumentElement(), new HTMLLoadingContext());
	}

	private Document filter(String content) {
		try {
			SAXParser parser = new SAXParser();
			parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
			parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			SimpleDomBuilderHandler contentHandler = new SimpleDomBuilderHandler(document);
			parser.setContentHandler(contentHandler);
			parser.parse(new InputSource(new ByteArrayInputStream(content.getBytes())));
			return document;
		} catch (SAXException e) {
			log.error("", e);
			return null;
		} catch (IOException e) {
			log.error("", e);
			return null;
		} catch (Exception e) {
			log.error("", e);
			return null;
		}
	}
	
	private static final class HTMLLoadingContext implements LoadingContext {

		@Override
		public JqtiExtensionManager getJqtiExtensionManager() {
			return null;
		}

		@Override
		public void modelBuildingError(QtiModelException exception, Node badNode) {
			//
		}
	}
}
