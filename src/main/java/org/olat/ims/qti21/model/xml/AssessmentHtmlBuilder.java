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
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.w3c.dom.Attr;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import uk.ac.ed.ph.jqtiplus.JqtiExtensionManager;
import uk.ac.ed.ph.jqtiplus.exception.QtiModelException;
import uk.ac.ed.ph.jqtiplus.node.AbstractNode;
import uk.ac.ed.ph.jqtiplus.node.LoadingContext;
import uk.ac.ed.ph.jqtiplus.node.content.basic.Block;
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
	
	public boolean containsSomething(String html) {
		return StringHelper.containsNonWhitespace(FilterFactory.getHtmlTagsFilter().filter(html));
	}
	
	public String flowStaticString(List<? extends FlowStatic> statics) {
		StringOutput sb = new StringOutput();
		if(statics != null && statics.size() > 0) {
			for(FlowStatic flowStatic:statics) {
				qtiSerializer.serializeJqtiObject(flowStatic, new StreamResult(sb));
			}
		}
		return cleanUpNamespaces(sb);
	}
	
	public String blocksString(List<? extends Block> statics) {
		StringOutput sb = new StringOutput();
		if(statics != null && statics.size() > 0) {
			for(Block flowStatic:statics) {
				qtiSerializer.serializeJqtiObject(flowStatic, new StreamResult(sb));
			}
		}
		
		return cleanUpNamespaces(sb);
	}
	
	private String cleanUpNamespaces(StringOutput sb) {
		String content = sb.toString();
		content = content.replace("xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"", "");
		content = content.replace("xmlns=\"http://www.imsglobal.org/xsd/imsqti_v2p1\"", "");
		content = content.replace("xsi:schemaLocation=\"http://www.imsglobal.org/xsd/imsqti_v2p1 http://www.imsglobal.org/xsd/imsqti_v2p1.xsd\"", "");
		return content.trim();
	}
	
	public void appendHtml(AbstractNode parent, String htmlFragment) {
		if(StringHelper.containsNonWhitespace(htmlFragment)) {
			htmlFragment = htmlFragment.trim();
			//tinymce bad habits
			if(StringHelper.isHtml(htmlFragment)) {
				if(htmlFragment.startsWith("<p>&nbsp;")) {
					htmlFragment = htmlFragment.replace("<p>&nbsp;", "<p>");
				}
			} else {
				htmlFragment = "<p>" + htmlFragment + "</p>";
			}
			//wrap around <html> to have a root element for neko
			Document document = filter("<html>" + htmlFragment + "</html>");
			Element docElement = document.getDocumentElement();
			cleanUpNamespaces(docElement);
			parent.getNodeGroups().load(docElement, new HTMLLoadingContext());
		}
	}
	
	private void cleanUpNamespaces(Element element) {
		Attr xsiattr = element.getAttributeNode("xmlns:xsi");
		if(xsiattr != null && "http://www.w3.org/2001/XMLSchema-instance".equals(xsiattr.getValue())) {
			element.removeAttribute("xmlns:xsi");
		}
		Attr attr = element.getAttributeNode("xmlns");
		if(attr != null && "http://www.imsglobal.org/xsd/imsqti_v2p1".equals(attr.getValue())) {
			element.removeAttribute("xmlns");
		}
		
		for(Node child=element.getFirstChild(); child != null; child = child.getNextSibling()) {
			if(child instanceof Element) {
				cleanUpNamespaces((Element)child);
			}
		}
	}

	private Document filter(String content) {
		try {
			SAXParser parser = new SAXParser();
			parser.setProperty("http://cyberneko.org/html/properties/names/elems", "lower");
			parser.setFeature("http://cyberneko.org/html/features/balance-tags/document-fragment", true);
			parser.setProperty("http://cyberneko.org/html/properties/default-encoding", "UTF-8");
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document document = builder.newDocument();
			HtmlToDomBuilderHandler contentHandler = new HtmlToDomBuilderHandler(document);
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
	
	/**
	 * Convert:<br>
	 * <ul>
	 * 		<li>textentryinteraction -> camel cased textEntryInteraction</li>
	 * </ul>
	 * 
	 * Initial date: 26.02.2016<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private static class HtmlToDomBuilderHandler extends SimpleDomBuilderHandler {
		
		public HtmlToDomBuilderHandler(Document document) {
			super(document);
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) {
			if("textentryinteraction".equals(localName)) {
				localName = qName = "textEntryInteraction";
				attributes = new AttributesDelegate(attributes);
			}
			super.startElement(uri, localName, qName, attributes);
		}

		@Override
		public void endElement(String uri, String localName, String qName) {
			if("textentryinteraction".equals(localName)) {
				localName = qName = "textEntryInteraction";
			}
			super.endElement(uri, localName, qName);
		}
	}
	
	/**
	 * Convert:<br>
	 * <ul>
	 * 		<li>responseidentifier -> camel cased responseIdentifier</li>
	 * 		<li>and other attributes of textEntryInteraction</li>
	 * </ul>
	 * 
	 * Initial date: 26.02.2016<br>
	 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
	 *
	 */
	private static class AttributesDelegate implements Attributes {
		
		private final Attributes attributes;
		
		public AttributesDelegate(Attributes attributes) {
			this.attributes = attributes;
		}

		@Override
		public int getLength() {
			return attributes.getLength();
		}

		@Override
		public String getURI(int index) {
			return attributes.getURI(index);
		}

		@Override
		public String getLocalName(int index) {
			String localName = attributes.getLocalName(index);
			return translateAttributeName(localName);
		}

		@Override
		public String getQName(int index) {
			String qName = attributes.getQName(index);
			return translateAttributeName(qName);
		}
		
		private final String translateAttributeName(String attrName) {
			if(attrName != null) {
				switch(attrName) {
					case "responseidentifier": return "responseIdentifier";
					case "placeholdertext": return "placeholderText";
					case "expectedlength": return "expectedLength";
					case "patternmask": return "patternMask";
					default: return attrName;
				}
			}
			return attrName;
		}

		@Override
		public String getType(int index) {
			return attributes.getType(index);
		}

		@Override
		public String getValue(int index) {
			return attributes.getValue(index);
		}

		@Override
		public int getIndex(String uri, String localName) {
			return attributes.getIndex(uri, localName);
		}

		@Override
		public int getIndex(String qName) {
			return attributes.getIndex(qName.toLowerCase());
		}

		@Override
		public String getType(String uri, String localName) {
			return attributes.getType(uri, localName);
		}

		@Override
		public String getType(String qName) {
			return attributes.getType(qName);
		}

		@Override
		public String getValue(String uri, String localName) {
			return attributes.getValue(uri, localName);
		}

		@Override
		public String getValue(String qName) {
			return attributes.getValue(qName);
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
