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
package org.olat.modules.forms.ui;

import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

import org.olat.core.gui.translator.Translator;


/**
 * 
 * Initial date: 07.02.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
final class MimeTypeSetFactory {
	
	private static final String TYPE_ALL_KEY = "file.upload.mime.type.all";
	
	private static Map<String, Set<String>> mimeTypeSets = new LinkedHashMap<>();
	static {
		mimeTypeSets.put(TYPE_ALL_KEY, null);
		Set<String> pdf = new HashSet<>();
		pdf.add("application/pdf");
		mimeTypeSets.put("file.upload.mime.type.pdf", Collections.unmodifiableSet(pdf));
		Set<String> images = new HashSet<>();
		images.add("image/gif");
		images.add("image/jpg");
		images.add("image/jpeg");
		images.add("image/png");
		mimeTypeSets.put("file.upload.mime.type.image", Collections.unmodifiableSet(images));
		Set<String> audios = new HashSet<>();
		audios.add("audio/aac");
		audios.add("audio/mp4");
		audios.add("audio/mpeg");
		audios.add("audio/ogg");
		audios.add("audio/wav");
		audios.add("audio/webm");
		mimeTypeSets.put("file.upload.mime.type.audio", Collections.unmodifiableSet(audios));
		Set<String> videos = new HashSet<>();
		videos.add("video/mp4");
		mimeTypeSets.put("file.upload.mime.type.video", Collections.unmodifiableSet(videos));
	}
	
	private MimeTypeSetFactory() {
		// noninstantiable
	}
	
	static String[] getKeys() {
		Set<String> keys = mimeTypeSets.keySet();
		return keys.toArray(new String[keys.size()]);
	}
	
	static String[] getValues(Translator translator) {
		String[] keys = getKeys();
		String[] translations = new String[keys.length];
		for (int i = 0; i < keys.length; i++) {
			translations[i] = translator.translate(keys[i]);
		}
		return translations;
	}
	
	static String getAllMimeTypesKey() {
		return TYPE_ALL_KEY;
	}

	static Set<String> getMimeTypes(String key) {
		return mimeTypeSets.get(key);
	}
	
	static boolean hasPreview(String key) {
		return "file.upload.mime.type.image".equals(key) || "file.upload.mime.type.video".equals(key);
	}
	
}
