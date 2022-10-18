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
package org.olat.course.style.manager;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.WebappHelper;
import org.olat.course.style.ImageSource;
import org.olat.course.style.ImageSourceType;
import org.olat.course.style.model.ImageSourceImpl;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 23 Jun 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Component
public class SystemImageStorage {
	
	private static final Logger log = Tracing.createLoggerFor(SystemImageStorage.class);
	
	private static final Path ROOT_PATH = Paths.get(WebappHelper.getUserDataRoot(), "customizing", "backgrounds", "courseheader");
	
	@PostConstruct
	public void init() throws IOException {
		if (!Files.exists(ROOT_PATH)) {
			Files.createDirectories(ROOT_PATH);
		}
	}
	
	public void initProvidedSystemImages(String path) throws Exception {
		URL providedUrl = SystemImageStorage.class.getResource("_system_images/" + path + "/");
		File providedDir = new File(providedUrl.toURI());
		FileUtils.copyDirContentsToDir(providedDir, ROOT_PATH.toFile(), false, "");
	}

	public File store(File file, String filename) {
		File targetFile = ROOT_PATH.resolve(filename).toFile();
		FileUtils.copyFileToFile(file, targetFile, false);
		return targetFile;
	}
	
	public File load(String filename) {
		try (Stream<Path> stream = Files.walk(ROOT_PATH, 1)) {
			Optional<File> file = stream
				.filter(path -> path.getFileName().toString().equalsIgnoreCase(filename))
				.filter(path -> !Files.isDirectory(path))
				.map(Path::toFile)
				.findFirst();
			if (file.isPresent()) {
				return file.get();
			}
		} catch (Exception e) {
			log.error("", e);
		}
		return null;
	}
	
	public List<ImageSource> loadAll() {
		try (Stream<Path> stream = Files.walk(ROOT_PATH, 1)) {
			return stream
					.filter(file -> !Files.isDirectory(file))
					.filter(file -> !FileUtils.isMetaFilename(file.toFile().getName()))
					.map(this::createImageSource)
					.collect(Collectors.toList());
		} catch (Exception e) {
			log.error("", e);
		}
		return Collections.emptyList();
	}

	private ImageSource createImageSource(Path path) {
		return createImageSource(path.getFileName().toString());
	}
	
	public ImageSource createImageSource(String filename) {
		ImageSourceImpl imageSource = new ImageSourceImpl();
		imageSource.setType(ImageSourceType.system);
		imageSource.setFilename(filename);
		return imageSource;
	}

	public boolean exists(String filename) {
		return Files.exists(ROOT_PATH.resolve(filename));
	}

	public void delete(String filename) {
		try {
			Files.deleteIfExists(ROOT_PATH.resolve(filename));
		} catch (IOException e) {
			log.error("", e);
		}
	}

}
