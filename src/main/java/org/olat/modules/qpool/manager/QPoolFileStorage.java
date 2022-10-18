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

import java.io.File;
import java.util.Date;
import java.util.List;

import jakarta.annotation.PostConstruct;

import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.vfs.FileStorage;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.modules.qpool.QuestionPoolModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * 
 * 
 * Initial date: 07.03.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("qpoolFileStorage")
public class QPoolFileStorage {

	@Autowired
	private QuestionPoolModule qpoolModule;
	
	private FileStorage fileStorage;
	
	@PostConstruct
	public void init() {
		VFSContainer rootContainer = qpoolModule.getRootContainer();
		fileStorage = new FileStorage(rootContainer);
	}

	public String generateDir() {
		return fileStorage.generateDir();
	}
	
	public String generateDir(String uuid) {
		return fileStorage.generateDir(uuid);
	}

	public VFSContainer getContainer(String dir) {
		return fileStorage.getContainer(dir);
	}
	
	public File getDirectory(String dir) {
		VFSContainer container = fileStorage.getContainer(dir);
		return ((LocalImpl)container).getBasefile();
	}

	public void deleteDir(String dir) {
		VFSContainer backupContainer = getBackupContainer(dir);
		if (backupContainer != null) {
			backupContainer.delete();
		}
		VFSContainer container = fileStorage.getContainer(dir);
		if (container != null) {
			container.delete();
		}
	}

	public void backupDir(String dir, Identity savedBy) {
		VFSContainer backupContainer = createBackupSubContainer(dir);
		List<VFSItem> origin = getContainer(dir).getItems();
		for (VFSItem item: origin) {
			backupContainer.copyFrom(item, savedBy);
		}
	}

	private VFSContainer createBackupSubContainer(String dir) {
		String backupDir = getBackupSubDir(dir);
		return fileStorage.getContainer(backupDir);
	}

	private String getBackupSubDir(String dir) {
		return getBackupDir(dir) + Formatter.formatDatetimeFilesystemSave(new Date(System.currentTimeMillis()));
	}
	
	public VFSContainer getBackupContainer(String dir) {
		String backupDir = getBackupDir(dir);
		return getContainer(backupDir);
	}

	private String getBackupDir(String dir) {
		String dirWithoutEndingSlash = dir.substring(0, dir.length() - 1);
		return dirWithoutEndingSlash + "_backup/";
	}
}