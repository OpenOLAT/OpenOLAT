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
package org.olat.modules.forms.handler;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.DataStorage;
import org.olat.modules.ceditor.model.StoredData;

/**
 * 
 * Initial date: 21 févr. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class FormDataElementStorage implements DataStorage {
	
	private File dataDirectory;
	
	public FormDataElementStorage(File dataDirectory) {
		this.dataDirectory = dataDirectory;
	}

	@Override
	public File getFile(StoredData metadata) {
		File dir = new File(dataDirectory, metadata.getStoragePath());
		return new File(dir, metadata.getRootFilename());
	}

	@Override
	public StoredData save(String filename, File file, StoredData metadata)
	throws IOException {
		if(!StringHelper.containsNonWhitespace(filename)) {
			filename = file.getName();
		}
		
		File saved = new File(dataDirectory, filename);
		if(saved.exists()) {
			filename = FileUtils.rename(saved);
			saved = new File(dataDirectory, filename);
		}
		
		Files.move(file.toPath(), saved.toPath(), StandardCopyOption.REPLACE_EXISTING);
		metadata.setRootFilename(filename);
		metadata.setStoragePath("");
		return metadata;
	}

	@Override
	public StoredData copy(StoredData original, StoredData copy) throws IOException {
		File imageFile = getFile(original);
		String cloneFileName = FileUtils.rename(imageFile);
		File cloneFile = new File(imageFile.getParent(), cloneFileName);
		
		Files.copy(imageFile.toPath(), cloneFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
		copy.setRootFilename(cloneFile.getName());
		copy.setStoragePath(original.getStoragePath());
		return copy;
	}
}
