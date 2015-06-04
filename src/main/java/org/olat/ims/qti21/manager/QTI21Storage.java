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
package org.olat.ims.qti21.manager;

import java.io.File;

import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.util.vfs.FileStorage;
import org.olat.core.util.vfs.VFSContainer;
import org.springframework.stereotype.Service;

/**
 * Factory for the file storage
 * 
 * 
 * Initial date: 21.05.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class QTI21Storage {
	
	public File getDirectory(String relativeDir) {
		OlatRootFolderImpl rootContainer = getQtiSerializationPath();
		File directory = new File(rootContainer.getBasefile(), relativeDir);
		if(!directory.exists()) {
			directory.mkdirs();
		}
		return directory;
	}

	public String getRelativeDir() {
		VFSContainer rootContainer = getQtiSerializationPath();
		FileStorage storage = new FileStorage(rootContainer);
		return storage.generateDir();
	}
	
    private OlatRootFolderImpl getQtiSerializationPath() {
    	return new OlatRootFolderImpl("/qtiassessment/", null);
	}
	
	

}
