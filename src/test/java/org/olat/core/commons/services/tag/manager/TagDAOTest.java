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
package org.olat.core.commons.services.tag.manager;

import static org.assertj.core.api.Assertions.assertThat;
import static org.olat.test.JunitTestHelper.random;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tag.Tag;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TagDAOTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;

	@Autowired
	private TagDAO sut;
	
	@Test
	public void shouldCreate() {
		String displayName = random();
		Tag tag = sut.createTag(displayName);
		dbInstance.commitAndCloseSession();
		
		assertThat(tag.getCreationDate()).isNotNull();
		assertThat(tag.getDisplayName()).isEqualTo(displayName);
	}
	
	@Test
	public void shouldDelete() {
		Tag tag = createRandomTag();
		
		sut.delete(tag);
		dbInstance.commitAndCloseSession();
		
		tag = sut.loadTag(tag);
		assertThat(tag).isNull();
	}
	
	@Test
	public void shouldLoadByKey() {
		Tag tag = createRandomTag();
		
		Tag reloadedTag = sut.loadTag(tag);
		
		assertThat(reloadedTag).isEqualTo(tag);
	}
	
	@Test
	public void shouldLoadByDisplayName() {
		Tag tag = createRandomTag();
		
		Tag reloadedTag = sut.loadTag(tag.getDisplayName());
		
		assertThat(reloadedTag).isEqualTo(tag);
	}
	
	@Test
	public void shouldLoadByDisplayNames() {
		Tag tag1 = createRandomTag();
		Tag tag2 = createRandomTag();
		createRandomTag();
		
		List<Tag> tags = sut.loadTags(List.of(tag1.getDisplayName(), tag2.getDisplayName(), random()));
		
		assertThat(tags).containsExactlyInAnyOrder(tag1, tag2);
	}
	
	private Tag createRandomTag() {
		String tagValue = random();
		Tag tag = sut.createTag(tagValue);
		dbInstance.commitAndCloseSession();
		return tag;
	}
	
}
