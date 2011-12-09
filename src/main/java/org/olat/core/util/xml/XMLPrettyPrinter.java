/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/ 

package org.olat.core.util.xml;


/**
 * Description:<br>
 * TODO: Felix Class Description for XMLPrettyPrinter
 * <P>
 * Initial Date: 09.09.2005 <br>
 * 
 * @author Felix
 */
public class XMLPrettyPrinter {
/*
	public static String prettyPrint(String in) {
		StringBuilder sb = new StringBuilder();
		DefaultHandler handler = new SAXIndent(sb);
		// Parse the input with the default (non-validating) parser
		SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
		saxParser.parse(new InputSource(new StringReader(in)), handler);
		try {
			StringWriter sw = new StringWriter();
			Transformer t = TransformerFactory.newInstance().newTransformer();
			t.setOutputProperty(OutputKeys.INDENT, "yes");
			t.setOutputProperty(OutputKeys.METHOD, "xml");	
			t.setOutputProperty(OutputKeys.STANDALONE, "yes");	
			t.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");	
			
			t.transform(new StreamSource(new StringReader(componentListenerInfo)), new StreamResult(sw));
			String res = sw.getBuffer().toString();
			componentListenerInfo = res;
		} catch (Exception e) {
			// ignore
		}
		String r = componentListenerInfo;
	}
*/
}
/*
class SAXIndent extends DefaultHandler {
	private static String sNEWLINE = "\n";
	private StringBuilder sb;

	SAXIndent(StringBuilder sb) {
		this.sb = sb;
	}

	public void startDocument() throws SAXException {
		echoString(sNEWLINE + "<?xml ...?>" + sNEWLINE + sNEWLINE);
	}

	public void endDocument() throws SAXException {
		echoString(sNEWLINE);
	}

	public void startElement(String namespaceURI, String localName, String qName, Attributes attrs) throws SAXException {
		echoTextBuffer();
		String eName = ("".equals(localName)) ? qName : localName;
		echoString("<" + eName); // element name
		if (attrs != null) {
			for (int i = 0; i < attrs.getLength(); i++) {
				String aName = attrs.getLocalName(i); // Attr name
				if ("".equals(aName)) aName = attrs.getQName(i);
				echoString(" " + aName + "=\"" + attrs.getValue(i) + "\"");
			}
		}
		echoString(">");
	}

	public void endElement(String namespaceURI, String localName, // local name
			String qName) // qualified name
			throws SAXException {
		echoTextBuffer();
		String eName = ("".equals(localName)) ? qName : localName;
		echoString("</" + eName + ">"); // element name
	}

	public void characters(char[] buf, int offset, int len) throws SAXException {
		String s = new String(buf, offset, len);
		sb.append(s);
	}

	// ---- Helper methods ----

	// Display text accumulated in the character buffer
	private void echoTextBuffer() throws SAXException {
		if (textBuffer == null) return;
		echoString(textBuffer.toString());
		textBuffer = null;
	}

	// Wrap I/O exceptions in SAX exceptions, to
	// suit handler signature requirements
	private void echoString(String s) throws SAXException {
		try {
			if (null == out) out = new OutputStreamWriter(System.out, "UTF8");
			out.write(s);
			out.flush();
		} catch (IOException ex) {
			throw new SAXException("I/O error", ex);
		}
	}
}
*/
