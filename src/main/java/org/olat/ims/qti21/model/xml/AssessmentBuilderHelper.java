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
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.stream.StreamResult;

import org.olat.core.gui.render.StringOutput;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.SAXException;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.exception.QtiModelException;
import uk.ac.ed.ph.jqtiplus.node.LoadingContext;
import uk.ac.ed.ph.jqtiplus.node.content.ItemBody;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
import uk.ac.ed.ph.jqtiplus.node.content.basic.FlowStatic;
import uk.ac.ed.ph.jqtiplus.serialization.QtiSerializer;

/**
 * 
 * Initial date: 09.12.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentBuilderHelper {
	
	private static final OLog log = Tracing.createLoggerFor(AssessmentBuilderHelper.class);
	
	private final QtiSerializer qtiSerializer;
	
	public AssessmentBuilderHelper() {
		JqtiExtensionManager jqtiExtensionManager = new JqtiExtensionManager();
		qtiSerializer = new QtiSerializer(jqtiExtensionManager);
	}
	
	public AssessmentBuilderHelper(QtiSerializer qtiSerializer) {
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
	
	public List<Block> parseHtml(String html) {
		//tinymce bad habits
		if(html.startsWith("<p>&nbsp;")) {
			html = html.replace("<p>&nbsp;", "<p>");
		}
		Document document = htmlToDOM("<html>" + html + "</html>");
		LoadingContext context = new HTMLLoadingContext();
		
		ItemBody helper = new ItemBody(null);
		helper.load(document.getDocumentElement(), context);	
		return helper.getBlocks();
	}
	
	/**
	 * This method use the standard XML parser. It's not really
	 * good but QTIWorks want DOM Level 2 elements.
	 * 
	 * @param content
	 * @return
	 */
	private Document htmlToDOM(String content) {
		try {
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			factory.setValidating(false);
			factory.setNamespaceAware(true);
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(new ByteArrayInputStream(content.getBytes()));
			return doc;
		} catch (ParserConfigurationException | SAXException | IOException e) {
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
