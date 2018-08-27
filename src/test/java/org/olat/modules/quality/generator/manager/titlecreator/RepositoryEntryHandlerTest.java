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
package org.olat.modules.quality.generator.manager.titlecreator;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.UUID;

import org.junit.Test;
import org.olat.modules.quality.generator.TitleCreator;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27.08.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class RepositoryEntryHandlerTest extends OlatTestCase {
	
	@Autowired
	private TitleCreator titleCreator;
	
	@Test
	public void shouldMergeDisplayName() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		String value = random();
		entry.setDisplayname(value);
		String template = "$" + RepositoryEntryHandler.DISPLAY_NAME;
		
		String merged = titleCreator.merge(template, asList(entry));
		
		assertThat(merged).isEqualTo(value);
		
	}
	
	private String random() {
		return UUID.randomUUID().toString();
	}


}
