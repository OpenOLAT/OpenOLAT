/*
<LICENCE>

Copyright (c) 2008, University of Southampton
All rights reserved.

Redistribution and use in source and binary forms, with or without modification,
are permitted provided that the following conditions are met:

  * Redistributions of source code must retain the above copyright notice, this
	list of conditions and the following disclaimer.

  *	Redistributions in binary form must reproduce the above copyright notice,
	this list of conditions and the following disclaimer in the documentation
	and/or other materials provided with the distribution.

  *	Neither the name of the University of Southampton nor the names of its
	contributors may be used to endorse or promote products derived from this
	software without specific prior written permission.

THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
(INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
(INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.

</LICENCE>
*/

package org.olat.ims.qti21;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import javax.xml.XMLConstants;
import javax.xml.namespace.NamespaceContext;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.NodeList;
import org.xml.sax.InputSource;

public class QTI21ContentPackage {
	public static final String MANIFEST_FILE_NAME = "imsmanifest.xml";
	private static final String [] TEST_EXPRESSION =
	{
		"/imscp:manifest/imscp:resources/imscp:resource[@type='imsqti_test_xmlv2p1']/@href",
		"/imscp:manifest/imscp:resources/imscp:resource[@type='imsqti_assessment_xmlv2p1']/@href" /* incorrect -- for backwards compatibility */
	};
	private static final String [] ITEM_EXPRESSION =
	{
		"/imscp:manifest/imscp:resources/imscp:resource[@type='imsqti_item_xmlv2p0']/@href",
		"/imscp:manifest/imscp:resources/imscp:resource[@type='imsqti_item_xmlv2p1']/@href",
		"/imscp:manifest/imscp:resources/imscp:resource[@type='imsqti_item_xml_v2p1']/@href" /* for aqurate */
	};

	private class QTINamespaceContext implements NamespaceContext
	{
		private static final String DEFAULT_NAME_SPACE = "imscp";
		private static final String DEFAULT_NAME_SPACE_URI = "http://www.imsglobal.org/xsd/imscp_v1p1";

		private String prefix;
		private String uri;

		public QTINamespaceContext()
		{
			this(DEFAULT_NAME_SPACE, DEFAULT_NAME_SPACE_URI);
		}

		public QTINamespaceContext(String prefix, String uri)
		{
			this.prefix = prefix;
			this.uri = uri;
		}

		public String getNamespaceURI(String prefix)
		{
			if (prefix.equals(this.prefix))
				return uri;
			else if (prefix.equals(XMLConstants.XML_NS_PREFIX))
				return XMLConstants.XML_NS_URI;
			else if (prefix.equals(XMLConstants.XMLNS_ATTRIBUTE))
				return XMLConstants.XMLNS_ATTRIBUTE_NS_URI;
			else
				return "";
		}

		public String getPrefix(String uri)
		{
			if (uri.equals(this.uri))
				return prefix;
			else if (uri.equals(XMLConstants.XML_NS_URI))
				return XMLConstants.XML_NS_PREFIX;
			else if (uri.equals(XMLConstants.XMLNS_ATTRIBUTE_NS_URI))
				return XMLConstants.XMLNS_ATTRIBUTE;
			else
				return null;
		}

		public Iterator<String> getPrefixes(String namespaceURI)
		{
			Collection<String> collection = new ArrayList<>();
			collection.add(getPrefix(namespaceURI));

			return collection.iterator();
		}
	}
	
	private Path manifestFile;

	public QTI21ContentPackage(Path file) {
		this.manifestFile = file;
	}

	public Path getManifest() {
		return manifestFile;
	}
	
	public boolean hasTest() {
		if(manifestFile == null || manifestFile.getParent() == null) {
			return false;
		}
		
		NodeList list = null;
		for (String s : TEST_EXPRESSION) {
			list = getNodeList(s);
			if (list.getLength() > 0) {
				break;
			}
		}

		if (list == null || list.getLength() == 0) {
			return false;
		}

		Path testPath = manifestFile.getParent().resolve(list.item(0).getNodeValue());
		return testPath != null && Files.exists(testPath);
	}

	public Path getTest() throws IOException {
		NodeList list = null;
		for (String s : TEST_EXPRESSION) {
			list = getNodeList(s);
			if (list.getLength() > 0)
				break;
		}

		if (list == null || list.getLength() == 0) {
			throw new IOException("Cannot find test.");
		}

		return manifestFile.getParent().resolve(list.item(0).getNodeValue());
	}

	public Path[] getItems() {
		List<NodeList> list = new ArrayList<>();
		int length=0;
		for (String s : ITEM_EXPRESSION) {
			NodeList l = getNodeList(s);
			list.add(l);
			length +=  l.getLength();
		}

		Path[] result = new Path[length];

		int j=0;
		for (NodeList l : list) {
			for (int i=0; i < l.getLength(); i++, j++)
				result[j] = manifestFile.getParent().resolve(l.item(i).getNodeValue());
		}
		
		return result;
	}

	private NodeList getNodeList(String expression) {
		try (InputStream input=Files.newInputStream(manifestFile)) {
			XPath xpath = XPathFactory.newInstance().newXPath();
			xpath.setNamespaceContext(new QTINamespaceContext());
			InputSource source = new InputSource(input);

			return (NodeList) xpath.evaluate(expression, source, XPathConstants.NODESET);
		} catch (IOException | XPathException ex) {
			throw new RuntimeException("", ex);
		}
	}
}
