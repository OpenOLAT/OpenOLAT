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
package org.olat.modules.qpool.manager;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

import org.junit.Assert;
import org.junit.Test;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 11.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FileStorageTest extends OlatTestCase {
	
	@Autowired
	private QPoolFileStorage qpoolFileStorage;
	
	@Test
	public void testGenerateDir() {
		String uuid = UUID.randomUUID().toString();
		String dir = qpoolFileStorage.generateDir(uuid);
		Assert.assertNotNull(dir);
		VFSContainer container = qpoolFileStorage.getContainer(dir);
		Assert.assertTrue(container.exists());
	}

	/**
	 * With the same uuid, generate 2 different directories
	 */
	@Test
	public void testGenerateDir_testUnicity() {
		String fakeUuid = "aabbccddeeff";
		String dir1 = qpoolFileStorage.generateDir(fakeUuid);
		String dir2 = qpoolFileStorage.generateDir(fakeUuid);
		
		//check
		Assert.assertNotNull(dir1);
		Assert.assertNotNull(dir2);
		Assert.assertFalse(dir1.equals(dir2));
	}
	
	@Test
	public void testDeleteDir() {
		String uuid = UUID.randomUUID().toString();
		String dir = qpoolFileStorage.generateDir(uuid);
		VFSContainer container = qpoolFileStorage.getContainer(dir);
		container.createChildLeaf("abc.txt");
		container.createChildLeaf("xyzc.txt");
		qpoolFileStorage.backupDir(dir, null);
		Assert.assertTrue(container.getItems().size() > 0);

		qpoolFileStorage.deleteDir(dir);
		
		String containerName = container.getName();
		Assert.assertTrue(container.getParentContainer().resolve(containerName) == null);
		String backupName = containerName + "_backup";
		Assert.assertTrue(container.getParentContainer().resolve(backupName) == null);
	}
	
	@Test
	public void testBackupDir() {
		String uuid = UUID.randomUUID().toString();
		String dir = qpoolFileStorage.generateDir(uuid);
		VFSContainer container = qpoolFileStorage.getContainer(dir);
		String name1 = "abc.txt";
		container.createChildLeaf(name1);
		String name2 = "xyzc.txt";
		container.createChildLeaf(name2);

		qpoolFileStorage.backupDir(dir, null);
		
		VFSContainer backupContainer = qpoolFileStorage.getBackupContainer(dir);
		assertThat(backupContainer).isNotNull();
		VFSContainer backupSubContainer = (VFSContainer) backupContainer.getItems().get(0);
		List<VFSItem> items = backupSubContainer.getItems();
		assertThat(items).hasSize(2);
		List<String> names = items.stream().map(VFSItem::getName).collect(Collectors.toList());
		assertThat(names).contains(name1, name2);
	}

}
