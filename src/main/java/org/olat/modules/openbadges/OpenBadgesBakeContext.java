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
package org.olat.modules.openbadges;

import org.olat.core.util.StringHelper;
import org.olat.modules.openbadges.v2.Assertion;

import org.json.JSONObject;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

/**
 * Initial date: 2023-05-09<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class OpenBadgesBakeContext {
	private static final String KEYWORD_KEY = "keyword";
	private static final String KEYWORD_VALUE = "openbadges";
	private static final String COMPRESSION_KEY = "compression";
	private static final String COMPRESSION_VALUE = "0";
	private static final String COMPRESSION_METHOD_KEY = "compressionMethod";
	private static final String COMPRESSION_METHOD_VALUE = "0";
	private static final String TEXT_KEY = "text";

	private String keyword;
	private String compression;
	private String compressionMethod;
	private String text;
	private Assertion textAsAssertion;

	protected OpenBadgesBakeContext() {
		setKeyword(KEYWORD_VALUE);
		setCompression(COMPRESSION_VALUE);
		setCompressionMethod(COMPRESSION_METHOD_VALUE);
		setText("");
	}

	public OpenBadgesBakeContext(NamedNodeMap attributes) throws IllegalArgumentException {
		this();
		for (int i = 0; i < attributes.getLength(); i++) {
			Node attribute = attributes.item(i);
			if (KEYWORD_KEY.equals(attribute.getNodeName())) {
				if (!KEYWORD_VALUE.equals(attribute.getNodeValue())) {
					throw new IllegalArgumentException("Keyword must be " + KEYWORD_VALUE);
				}
			} else if (COMPRESSION_KEY.equals(attribute.getNodeName())) {
				setCompression(attribute.getNodeValue());
			} else if (COMPRESSION_METHOD_KEY.equals(attribute.getNodeName())) {
				setCompressionMethod(attribute.getNodeValue());
			} else if (TEXT_KEY.equals(attribute.getNodeName())) {
				setText(attribute.getNodeValue());
			}
		}
	}

	private void setKeyword(String keyword) {
		this.keyword = keyword;
	}

	public String getKeyword() {
		return keyword;
	}

	public String getCompression() {
		return compression;
	}

	private void setCompression(String compression) {
		this.compression = compression;
	}

	public String getCompressionMethod() {
		return compressionMethod;
	}

	private void setCompressionMethod(String compressionMethod) {
		this.compressionMethod = compressionMethod;
	}

	public String getText() {
		return text;
	}

	private void setText(String text) {
		this.text = text;
		if (StringHelper.containsNonWhitespace(text)) {
			JSONObject jsonObject = new JSONObject(text);
			textAsAssertion = new Assertion(jsonObject);
		}
	}

	public Assertion getTextAsAssertion() {
		return textAsAssertion;
	}
}
