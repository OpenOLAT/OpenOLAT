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

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;

import org.junit.Test;
import org.olat.modules.edusharing.EdusharingHtmlElement;

/**
 * 
 * Initial date: 10 Dec 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class EdusharingHtmlServiceImplTest {
	
	EdusharingHtmlServiceImpl sut = new EdusharingHtmlServiceImpl();
	
	@Test
	public void shouldExtractEsNodes() {
		String esNode1 = "<img id = '123' data-es_identifier='abc1' width='123' />";
		String esNode2 = "<img id = '123' data-es_identifier='abc2' width='123'>";
		String esNode3 = "<a href=\"https://frentix.com\" data-es_identifier='abc2'>";
		String otherNode = "<img id = '123' data-es_id='abc' width='123'>";
		
		String html = new StringBuilder()
				.append("<div>")
				.append(esNode1)
				.append("some text")
				.append(esNode2).append("description").append("</img>")
				.append(otherNode)
				.append(esNode3)
				.append("</div>")
				.toString();
		
		List<String> nodes = sut.getNodes(html);
		
		assertThat(nodes)
			.containsExactlyInAnyOrder(esNode1, esNode2, esNode3)
			.doesNotContain(otherNode);
	}
	
	@Test
	public void shouldGetAttributeValues() {
		String identifier = random();
		String objectUrl = random();
		String version = random();
		String mimeType = random();
		String mediaType = random();
		String width = "100";
		String hight = "200";
		String node = new StringBuilder()
				.append("<img id = '123' ")
				.append("data-es_identifier='").append(identifier).append("' ")
				.append("data-es_objecturl=\"").append(objectUrl).append("\" ")
				.append("data-es_version='").append(version).append("' ")
				.append("data-es_mimetype ='").append(mimeType).append("' ")
				.append("data-es_mediatype='").append(mediaType).append("' ")
				.append("data-es_width='").append(width).append("' ")
				.append("data-es_height='").append(hight).append("' ")
				.append("width='123'>")
				.toString();
		
		EdusharingHtmlElement htmlElement = sut.getHtmlElement(node);
		
		assertThat(htmlElement.getIdentifier()).isEqualTo(identifier);
	}

	@Test
	public void shouldDeleteEsNodes() {
		String node1Identifier = "abc1" ;
		String esNode1 = "<img id = '123' data-es_identifier='" + node1Identifier + "' width='123' />";
		String esNode2 = "<img id = '123' data-es_identifier='abc2' width='123'>";
		String esNode3 = "<a href=\"https://frentix.com\" data-es_identifier='abc2'>";
		String otherNode = "<img id = '123' data-es_id='abc' width='123'>";
		
		String html = new StringBuilder()
				.append("<div>")
				.append(esNode1)
				.append("some text")
				.append(esNode2).append("description").append("</img>")
				.append(otherNode)
				.append(esNode3)
				.append("</div>")
				.toString();
		String expected = new StringBuilder()
				.append("<div>")
				.append("some text")
				.append(esNode2).append("description").append("</img>")
				.append(otherNode)
				.append(esNode3)
				.append("</div>")
				.toString();
		
		String cleaned =  sut.deleteNode(html, esNode1);
		
		assertThat(cleaned).isEqualTo(expected);
	}
	
	private String random() {
		return UUID.randomUUID().toString();
	}

}
