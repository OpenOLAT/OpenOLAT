
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
package org.olat.core.util.i18n;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.SortedProperties;

/**
 * A utility class to manipulate i18n languages files.
 * 
 * 
 * Initial date: 1 avr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class I18nMaintenance {
	
	private static final Logger log = Tracing.createLoggerFor(I18nMaintenance.class);
	
	// @Test
	public void removeUnusedKeys() {
		
		Path root = Paths.get("fake/OpenOLAT/src/main/java"); 
		List<String> pathList = pathWithKeys(root);
		
		for(String i18nPath:pathList) {
			cleanup(root, i18nPath);
		}
	}
	
	private void cleanup(Path root, String path) {
		Path i18nPath = root.resolve(path);
		
		File deFile = new File(i18nPath.toFile(), "LocalStrings_de.properties");
		File file = new File(i18nPath.toFile(), "LocalStrings_fr.properties");
		if(deFile.exists() && file.exists()) {
			SortedProperties deProps = loadProperties(deFile);
			SortedProperties frProps = loadProperties(file);
			
			boolean changed = false;
			List<Object> frKeys = new ArrayList<>(frProps.keySet());
			for(Object frKey:frKeys) {
				if(!deProps.containsKey(frKey)) {
					frProps.remove(frKey);
					changed = true;
				}
			}
			
			if(changed) {
				storeProperties(file, frProps);
			}
		} else {
			log.error("Doesn't exists: {}", deFile, file);
		}
	}
	
	private SortedProperties loadProperties(File file) {
		try(InputStream is = new FileInputStream(file);
				BufferedInputStream bis = new BufferedInputStream(is, FileUtils.BSIZE)) {
			SortedProperties props = new SortedProperties();
			props.load(bis);
			return props;
		} catch(IOException e) {
			log.error("", e);
			return null;
		}
	}
	
	private void storeProperties(File file, SortedProperties props) {
		try(OutputStream out = new FileOutputStream(file)) {
			props.store(out, null);
		} catch(IOException e) {
			log.error("", e);
		}
	}
	
	private List<String> pathWithKeys(final Path root) {
		try {
			final List<String> pathList = new ArrayList<>();
			Files.walkFileTree(root, new SimpleFileVisitor<Path>() {
				
				public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
					if(attrs.isDirectory()) {
						String dirName = dir.getFileName().toString();
						if("_i18n".equals(dirName)) {
							String relPath = root.relativize(dir).toString();
							pathList.add(relPath);
						}
					}
					return FileVisitResult.CONTINUE;
				}
			});
			return pathList;
		} catch (IOException e) {
			log.error("", e);
			return List.of();
		}
	}
}
