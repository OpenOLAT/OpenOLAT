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

import javax.annotation.PostConstruct;

import org.olat.core.util.vfs.FileStorage;
import org.olat.core.util.vfs.LocalImpl;
import org.olat.core.util.vfs.VFSContainer;
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
}