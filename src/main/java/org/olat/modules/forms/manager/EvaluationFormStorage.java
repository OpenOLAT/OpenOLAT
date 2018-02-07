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
package org.olat.modules.forms.manager;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

import javax.annotation.PostConstruct;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFileImpl;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.vfs.VFSLeaf;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EvaluationFormStorage {
	
	private static final OLog log = Tracing.createLoggerFor(EvaluationFormStorage.class);
	
	private static final String EVALUATION_FORMS_DIRECTORY = "evaluation_form";
	private static final String RESPONSES_DIRECTORY = "responses";
	
	private Path bcrootDirectory;
	private Path rootDirectory;
	private Path responsesDirectory;
	
	@PostConstruct
	public void initDirectories() {
		bcrootDirectory = Paths.get(FolderConfig.getCanonicalRoot());
		rootDirectory = Paths.get(FolderConfig.getCanonicalRoot(), EVALUATION_FORMS_DIRECTORY);
		responsesDirectory = Paths.get(rootDirectory.toString(), RESPONSES_DIRECTORY);
		try {
			Files.createDirectories(responsesDirectory);
		} catch (Exception e) {
			log.error("Creation of evaluation forms responses directory failed! Path: " + responsesDirectory, e);
		}
	}

	Path getResponsesRoot() {
		return responsesDirectory;
	}

	/**
	 * Save a file in the responses store.
	 * 
	 * @param file the file to save
	 * @param filename the filename of the new file
	 * @param responseIdentifier the identifier of the evaluation form response
	 * @return the relative path of the saved file
	 */
	public Path save(File file, String filename, String responseIdentifier) throws IOException {
		Path responsePath = getResponsePath(responseIdentifier, filename);
		Path sourcePath = file.toPath();
		Files.createDirectories(responsePath.getParent());
		Files.copy(sourcePath, responsePath, StandardCopyOption.REPLACE_EXISTING);
		return getRelativePath(responsePath);
	}

	public File load(Path relativePath) {
		return getAbsolutePath(relativePath).toFile();
	}
	
	public VFSLeaf resolve(Path relativePath) {
		return new OlatRootFileImpl("/" + relativePath.toString(), null);
	}

	public void delete(Path relativePath) {
		Path absolutePath = getAbsolutePath(relativePath);
		try {
			Files.deleteIfExists(absolutePath);
		} catch (IOException e) {
			log.warn("Cannot properly delete evaluation form response file. Path: " + absolutePath, e);
		}
	}
	
	private Path getResponsePath(String responseIdentifier, String filename) {
		return Paths.get(
				responsesDirectory.toString(),
				getIndexTooken(responseIdentifier),
				responseIdentifier,
				filename);
	}
	
	private String getIndexTooken(String responseIdentifier) {
		return responseIdentifier.replace("-", "").substring(0, 2).toLowerCase();
	}
	
	private Path getRelativePath(Path path) {
		return bcrootDirectory.relativize(path);
	}

	private Path getAbsolutePath(Path relativePath) {
		return bcrootDirectory.resolve(relativePath);
	}

}
