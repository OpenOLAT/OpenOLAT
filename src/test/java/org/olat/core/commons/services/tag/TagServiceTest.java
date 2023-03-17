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
package org.olat.core.commons.services.tag;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 14 Mar 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TagServiceTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	
	@Autowired
	private TagService sut;

	@Test
	public void shouldGetOrCreateTags() {
		String displayName1 = JunitTestHelper.random();
		Tag tag1 = sut.getOrCreateTag(displayName1);
		String displayName2 = JunitTestHelper.random();
		dbInstance.commitAndCloseSession();
		
		List<Tag> tags = sut.getOrCreateTags(List.of(displayName1, displayName2));
		
		assertThat(tags).contains(tag1);
		assertThat(tags).extracting(Tag::getDisplayName).containsExactlyInAnyOrder(displayName1, displayName2);
	}

}
