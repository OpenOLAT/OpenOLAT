
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
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.logging.log4j.Logger;
import org.junit.Test;
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
public class I18nConsolidation {
	
	private static final Logger log = Tracing.createLoggerFor(I18nConsolidation.class);

	private static final Pattern resolvingKeyPattern = Pattern.compile("\\$\\{?([\\w\\.\\-]*):([\\w\\.\\-]*[\\w\\-])\\}?");
	
	@Test
	public void consolidate() {
		
		Path root = Paths.get("/Users/srosse/Developer/Work/ws_sidedfeatures/OpenOLAT/src/main/java"); 
		List<String> pathList = I18nMaintenance.pathWithKeys(root);
		
		String[] props = new String[] { "LocalStrings_de.properties", "LocalStrings_fr.properties", "LocalStrings_en.properties", "LocalStrings_it.properties",
				"LocalStrings_pl.properties", "LocalStrings_pt_BR.properties", "LocalStrings_ru.properties", "LocalStrings_zh_CN.properties",
				"LocalStrings_tr.properties" };
		for(String prop: props) {
			for(String i18nPath:pathList) {
				cleanup(root, i18nPath, prop);
			}
		}
	}
	
	private void cleanup(Path root, String path, String prop) {
		Path i18nPath = root.resolve(path);
		
		File deFile = new File(i18nPath.toFile(), prop);
		if(deFile.exists()) {
			SortedProperties properties = loadProperties(deFile);
			List<Object> keys = new ArrayList<>(properties.keySet());
			
			boolean changed = false;
			for(Object key:keys) {
				String val = properties.getProperty((String)key);
				if((val.contains("org.olat.ims.qti") || val.contains("org.olat.modules.iq")) && !val.contains("org.olat.ims.qti21")) {
					
					Matcher matcher = resolvingKeyPattern.matcher(val);
					while (matcher.find()) {
						String toResolvedBundle = matcher.group(1);
						
						
						if("org.olat.ims.qti".equals(toResolvedBundle)
								|| toResolvedBundle.startsWith("org.olat.ims.qti.")
								|| "org.olat.modules.iq".equals(toResolvedBundle)) {
						
							String toResolvedKey = matcher.group(2);
							
							SortedProperties source = loadProperties(root, toResolvedBundle, prop);
							if(source != null) {
								String newVal = source.getProperty(toResolvedKey);
								if(newVal == null) {
									System.out.println("");
								} else {
									properties.setProperty((String)key, newVal);
									changed = true;
								}
							}
						}
					}
				}
			}
			
			if(changed) {
				storeProperties(deFile, properties);
			}
		}
	}
	
	private SortedProperties loadProperties(Path root, String bundle, String prop) {
		Path i18nPath = root.resolve(bundle.replace('.', '/')).resolve("_i18n");
		File file = new File(i18nPath.toFile(), prop);
		return loadProperties(file);
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

}
