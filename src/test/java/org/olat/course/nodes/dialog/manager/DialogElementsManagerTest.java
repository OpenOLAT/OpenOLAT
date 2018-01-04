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
package org.olat.course.nodes.dialog.manager;

import java.util.UUID;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.course.nodes.dialog.DialogElement;
import org.olat.course.nodes.dialog.DialogElementsManager;
import org.olat.modules.fo.Forum;
import org.olat.repository.RepositoryEntry;
import org.olat.test.JunitTestHelper;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 3 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DialogElementsManagerTest extends OlatTestCase {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private DialogElementsManager dialogElementsManager;
	
	@Test
	public void createDialogElement() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("session-1");
		String subIdent = UUID.randomUUID().toString();
		DialogElement element = dialogElementsManager.createDialogElement(entry, author, "task_d.txt", 234l, subIdent);
		dbInstance.commitAndCloseSession();
		
		Assert.assertNotNull(element.getKey());
		Assert.assertNotNull(element.getForum());
		Assert.assertEquals(author, element.getAuthor());
		Assert.assertEquals("task_d.txt", element.getFilename());
		Assert.assertEquals(Long.valueOf(234l), element.getSize());
		Assert.assertEquals(subIdent, element.getSubIdent());
		Assert.assertEquals(entry, element.getEntry());
	}
	
	@Test
	public void  getDialogElementByForumKey() {
		RepositoryEntry entry = JunitTestHelper.createAndPersistRepositoryEntry();
		Identity author = JunitTestHelper.createAndPersistIdentityAsRndUser("session-1");
		String subIdent = UUID.randomUUID().toString();
		DialogElement element = dialogElementsManager.createDialogElement(entry, author, "task_e.txt", 235l, subIdent);
		dbInstance.commitAndCloseSession();
		
		Forum forum = element.getForum();
		DialogElement loadedElement = dialogElementsManager.getDialogElementByForum(forum.getKey());
		Assert.assertNotNull(loadedElement.getKey());
		Assert.assertEquals(forum, loadedElement.getForum());
		Assert.assertEquals(author, loadedElement.getAuthor());
		Assert.assertEquals("task_e.txt", loadedElement.getFilename());
		Assert.assertEquals(Long.valueOf(235l), loadedElement.getSize());
		Assert.assertEquals(subIdent, loadedElement.getSubIdent());
		Assert.assertEquals(entry, loadedElement.getEntry());	
	}

}
