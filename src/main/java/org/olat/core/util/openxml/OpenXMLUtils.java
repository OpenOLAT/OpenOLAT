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
package org.olat.core.util.openxml;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.StringReader;
import java.util.zip.ZipOutputStream;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.stream.FactoryConfigurationError;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.services.image.Size;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.io.ShieldOutputStream;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

/**
 * 
 * Initial date: 04.09.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class OpenXMLUtils {
	
	private static final Logger log = Tracing.createLoggerFor(OpenXMLUtils.class);

	public static final double emusPerInch = 914400.0d;
	public static final double emusPerCm = 360000.0d;

	public static final int convertPixelToEMUs(int pixel, int dpi, double resizeRatio) {
		double rezDpi = dpi * 1.0d;
		return (int)(((pixel / rezDpi) * emusPerInch) * resizeRatio);
	}

	public static final DocumentBuilder getDocumentBuilder (boolean expandEntityRef, boolean namespaceAware, boolean validating) throws ParserConfigurationException {
		DocumentBuilderFactory df = DocumentBuilderFactory.newInstance();
		df.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
		df.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
		// default: true
		df.setExpandEntityReferences(expandEntityRef);
		// default: false
		df.setNamespaceAware(namespaceAware);
		// default: false
		df.setValidating(validating);
		return df.newDocumentBuilder();
	}
	
	public static final OpenXMLSize convertPixelToEMUs(Size img, int dpi, double maxWidthCm) {
		int widthPx = img.getWidth();
		int heightPx = img.getHeight();
		double resizeRatio = 1.0d;

		double widthEmus = convertPixelToEMUs(widthPx, dpi, resizeRatio);
		double heightEmus = convertPixelToEMUs(heightPx, dpi, resizeRatio);

		double maxWidthEmus = maxWidthCm * emusPerCm;

		if (widthEmus > maxWidthEmus) {
			resizeRatio = maxWidthEmus / widthEmus;
			double ratio = heightEmus / widthEmus;
			widthEmus = maxWidthEmus;
			heightEmus = widthEmus * ratio;
		}
		return new OpenXMLSize(widthPx, heightPx, (int)widthEmus, (int)heightEmus, resizeRatio);
	}
	
	public static int getSpanAttribute(String name, Attributes attrs) {
		name = name.toLowerCase();
		int span = -1;
		for(int i=attrs.getLength(); i-->0; ) {
			String attrName = attrs.getQName(i);
			if(name.equals(attrName.toLowerCase())) {
				String val = attrs.getValue(i);
				if(StringHelper.isLong(val)) {
					return Integer.parseInt(val);
				}
			}	
		}
		return span < 1 ? 1 : span;
	}
	
	public static boolean contains(Node parent, String nodeName) {
		boolean found = false;
		for(Node node=parent.getFirstChild(); node!=null; node=node.getNextSibling()) {
			if(nodeName.equals(node.getNodeName())) {
				found = true;
			}
		}
		return found;
	}
	
	public static final XMLStreamWriter createStreamWriter(ZipOutputStream out) {
		try {
			return XMLOutputFactory.newInstance().createXMLStreamWriter(new ShieldOutputStream(out), "UTF-8");
		} catch (XMLStreamException | FactoryConfigurationError e) {
			log.error("", e);
			return null;
		}
	}
	
	public static final Document createDocument() {
		try {
			DocumentBuilder builder = getDocumentBuilder(true, false, true);
			return builder.newDocument();
		} catch (ParserConfigurationException e) {
			log.error("", e);
			return null;
		}
	}
	
	/**
	 * Create a document from a template, validation is turned off. The
	 * method doesn't close the input stream.
	 * @param in
	 * @return
	 */
	public static final Document createDocument(InputStream in) {
		try {
			DocumentBuilder builder = getDocumentBuilder(true, false, false);
			return builder.parse(in);
		} catch (ParserConfigurationException | IOException | SAXException e) {
			log.error("", e);
			return null;
		}
	}
	
	public static final Document createDocument(String in) {
		try {
			DocumentBuilder builder = getDocumentBuilder(true, false, false);
			return builder.parse(new InputSource(new StringReader(in)));
		} catch (ParserConfigurationException | IOException | SAXException e) {
			log.error("", e);
			return null;
		}
	}
	
	public static final void writeTo(Document document, OutputStream out, boolean indent) {
		try {
			// Use a Transformer for output
			TransformerFactory tFactory = TransformerFactory.newInstance();
			tFactory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
			Transformer transformer = tFactory.newTransformer();
			if(indent) {
				transformer.setOutputProperty(OutputKeys.INDENT, "yes");
				transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
			}

			DOMSource source = new DOMSource(document);
			Result result = new StreamResult(out);
			transformer.transform(source, result);
		} catch (TransformerFactoryConfigurationError | TransformerException e) {
			log.error("", e);
		}
	}
}
