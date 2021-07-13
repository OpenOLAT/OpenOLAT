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
package org.olat.modules.edusharing.manager;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import org.olat.modules.edusharing.EdusharingHtmlElement;
import org.olat.modules.edusharing.EdusharingHtmlService;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EdusharingHtmlServiceImpl implements EdusharingHtmlService {

	// The attribute names have to be registered in OpenOLATPolicy (tags img and a).
	private static final Pattern ES_PATTERN_NODE = Pattern.compile("<([^<]*?)es_identifier(.*?)>");
	private static final String ATTRIBUTE_VALUE_REGEX = "\\s*=\\s*['|\"](.+?)['|\"]";
	private static final Pattern ES_PATTERN_IDENTIFIER = Pattern.compile("es_identifier" + ATTRIBUTE_VALUE_REGEX);
	private static final Pattern ES_PATTERN_OBJECT_URL = Pattern.compile("es_objecturl" + ATTRIBUTE_VALUE_REGEX);
	private static final Pattern ES6_PATTERN_OBJECT_URL = Pattern.compile("objectUrl" + "\\s*&#61;\\s*(.+?)&amp");
	private static final Pattern ES_PATTERN_VERSION = Pattern.compile("es_version" + ATTRIBUTE_VALUE_REGEX);
	private static final Pattern ES_PATTERN_MIME_TYPE = Pattern.compile("es_mimetype" + ATTRIBUTE_VALUE_REGEX);
	private static final Pattern ES_PATTERN_MEDIA_TYPE = Pattern.compile("es_mediatype" + ATTRIBUTE_VALUE_REGEX);
	private static final Pattern ES_PATTERN_WIDTH = Pattern.compile("es_width" + ATTRIBUTE_VALUE_REGEX);
	private static final Pattern ES_PATTERN_HIGHT = Pattern.compile("es_height" + ATTRIBUTE_VALUE_REGEX);
	
	@Override
	public List<EdusharingHtmlElement> parse(String html) {
		return getNodes(html)
				.stream()
				.map(this::getHtmlElement)
				.collect(Collectors.toList());
	}
	
	List<String> getNodes(String html) {
		Matcher matcher = ES_PATTERN_NODE.matcher(html);
		List<String> esNodes = new ArrayList<>();
		while(matcher.find()){
			esNodes.add(matcher.group());
		}
		return esNodes;
	}
	
	EdusharingHtmlElement getHtmlElement(String node) {
		String identifier = getAttributeValue(node, ES_PATTERN_IDENTIFIER);
		String objectUrl = getAttributeValue(node, ES_PATTERN_OBJECT_URL);
		if (objectUrl == null) {
			objectUrl = getAttributeValue(node, ES6_PATTERN_OBJECT_URL);
		}
		if (identifier == null || objectUrl == null) return null;
		
		EdusharingHtmlElement element = new EdusharingHtmlElement(identifier , objectUrl);
		String version = getAttributeValue(node, ES_PATTERN_VERSION);
		element.setVersion(version);
		String mimeType = getAttributeValue(node, ES_PATTERN_MIME_TYPE);
		element.setMimeType(mimeType);
		String mediaType = getAttributeValue(node, ES_PATTERN_MEDIA_TYPE);
		element.setMediaType(mediaType);
		String width = getAttributeValue(node, ES_PATTERN_WIDTH);
		element.setWidth(width);
		String hight = getAttributeValue(node, ES_PATTERN_HIGHT);
		element.setHight(hight);
		
		return element;
	}
	
	private String getAttributeValue(String node, Pattern pattern) {
		try {
			Matcher matcher = pattern.matcher(node);
			boolean found = matcher.find();
			if (found) {
				return matcher.group(1);
			}
		} catch (Exception e) {
			// may happen
		}
		return null;
	}

	@Override
	public String deleteNode(String html, String identifier) {
		int identifierIndex = html.indexOf(identifier);
		int nodeStart = html.lastIndexOf("<", identifierIndex);
		int nodeEnd = html.indexOf(">", identifierIndex);
		String cleaned = nodeStart > 0 && nodeEnd > 0
				? html.substring(0, nodeStart) + html.substring(nodeEnd + 1)
				: html;
		return cleaned;
	}
	
}
