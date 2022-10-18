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
import java.util.Comparator;
import java.util.UUID;

import jakarta.annotation.PostConstruct;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 04.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
class EvaluationFormStorage {
	
	private static final Logger log = Tracing.createLoggerFor(EvaluationFormStorage.class);
	
	private static final String EVALUATION_FORMS_DIRECTORY = "evaluation_form";
	private static final String RESPONSES_DIRECTORY = "responses";
	private static final String TMP_DIRECTORY = "evaluation_form_tmp";

	
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
	
	File createTmpDir() {
		return getTmpDir().resolve(CodeHelper.getUniqueID()).toFile();
	}

	void deleteTmpDirs() {
		FileUtils.deleteDirsAndFiles(getTmpDir().toFile(), true, false);
		log.info("Evaluation form tmp dir cleaned: " + getTmpDir().toString());
	}

	private Path getTmpDir() {
		return Paths.get(WebappHelper.getTmpDir(), TMP_DIRECTORY);
	}

	Path getResponsesRoot() {
		return responsesDirectory;
	}

	/**
	 * Save a file in the responses store.
	 * 
	 * @param file the file to save
	 * @param filename the filename of the new file
	 * @return the relative path of the saved file
	 */
	Path save(File file, String filename) throws IOException {
		Path responsePath = getResponsePath(filename);
		Path sourcePath = file.toPath();
		Files.createDirectories(responsePath.getParent());
		Files.copy(sourcePath, responsePath, StandardCopyOption.REPLACE_EXISTING);
		return getRelativePath(responsePath);
	}

	File load(Path relativePath) {
		return getAbsolutePath(relativePath).toFile();
	}
	
	VFSLeaf resolve(Path relativePath) {
		return VFSManager.olatRootLeaf("/" + relativePath.toString());
	}

	void copyTo(Path relativePath, File targetDir) {
		File file = getAbsolutePath(relativePath).toFile();
		FileUtils.copyFileToDir(file, targetDir, "copy evaluation form upload file");
	}

	void delete(Path relativePath) {
		Path absolutePath = null;
		try {
			Path parentDir = relativePath.getParent();
			absolutePath = getAbsolutePath(parentDir);
		} catch (Exception e) {
			log.warn("Cannot find absolute path to delete file of evaluation form response file. Path: " + relativePath, e);
		}
		if (absolutePath != null) {
			deleteFiles(absolutePath);
		}
	}

	private void deleteFiles(Path absolutePath) {
		try {
			Files.walk(absolutePath)
					.sorted(Comparator.reverseOrder())
					.map(Path::toFile)
					.forEach(File::delete);
		} catch (IOException e) {
			log.warn("Cannot properly delete evaluation form response file. Path: " + absolutePath, e);
		}
	}
	
	private Path getResponsePath(String filename) {
		String responseDirectory = UUID.randomUUID().toString().replace("-", "").toLowerCase();
		return Paths.get(
				responsesDirectory.toString(),
				getIndexTooken(responseDirectory),
				responseDirectory,
				filename);
	}
	
	private String getIndexTooken(String responseDirectory) {
		return responseDirectory.substring(0, 3);
	}
	
	private Path getRelativePath(Path path) {
		return bcrootDirectory.relativize(path);
	}

	private Path getAbsolutePath(Path relativePath) {
		return bcrootDirectory.resolve(relativePath);
	}

}
