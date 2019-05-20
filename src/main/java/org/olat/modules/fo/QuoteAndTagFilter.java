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
package org.olat.modules.fo;

import java.io.IOException;
import java.io.StringReader;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.Filter;
import org.w3c.dom.DocumentFragment;
import org.w3c.dom.Node;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import nu.validator.htmlparser.dom.HtmlDocumentBuilder;

public class QuoteAndTagFilter implements Filter {
	
	private static final Logger log = Tracing.createLoggerFor(QuoteAndTagFilter.class);

	@Override
	public String filter(String original) {
		try {
			HtmlDocumentBuilder parser = new HtmlDocumentBuilder();
			DocumentFragment document = parser.parseFragment(new InputSource(new StringReader(original)), "");
			StringBuilder sb = new StringBuilder();
			scanNode(document, sb);
			return sb.toString();
		} catch (SAXException | IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	private void scanNode(Node node, StringBuilder sb) {
		for(Node child=node; child != null; child=child.getNextSibling()) {
			if(child.hasAttributes()) {
				Node nodeclass = child.getAttributes().getNamedItem("class");
				if(nodeclass != null) {
					String value = nodeclass.getNodeValue();
					if("b_quote_wrapper".equals(value) || "o_quote_wrapper".equals(value)) {
						continue;
					}
				}
			}
			if(child.hasChildNodes()) {
				scanNode(child.getFirstChild(), sb);
			}
			if(child.getNodeType() == Node.TEXT_NODE) {
				String text = child.getNodeValue();
				if(StringHelper.containsNonWhitespace(text)) {
					if(sb.length() > 0) {
						sb.append(' ');
					}
					sb.append(text);
				}
			}
		}
	}
}
