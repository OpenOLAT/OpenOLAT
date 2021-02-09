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
package org.olat.core.gui.components.form.flexible.impl.elements.richText;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.olat.core.configuration.AbstractSpringModule;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 9 févr. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class RichTextElementModule extends AbstractSpringModule {
	
	@Value("${rich.text.fonts:Andale Mono=andale mono,times;Arial=arial,helvetica,sans-serif;Arial Black=arial black,avant garde;Book Antiqua=book antiqua,palatino;Comic Sans MS=comic sans ms,sans-serif;Courier New=courier new,courier;Georgia=georgia,palatino;Helvetica=helvetica;Impact=impact,chicago;Symbol=symbol;Tahoma=tahoma,arial,helvetica,sans-serif;Terminal=terminal,monaco;Times New Roman=times new roman,times;Trebuchet MS=trebuchet ms,geneva;Verdana=verdana,geneva}")
	private String fonts;
	@Value("${rich.text.additional.fonts:}")
	private String additionalFonts;
	@Value("${rich.text.fontsizes:8pt 9pt 10pt 11pt 12pt 14pt 18pt 24pt 30pt 36pt 48pt 60pt 72pt 96pt}")
	private String fontSizes;
	
	
	private List<Font> fontList;
	
	@Autowired
	public RichTextElementModule(CoordinatorManager coordinatorManager) {
		super(coordinatorManager);
	}

	@Override
	public void init() {
		initFromChangedProperties();
	}

	@Override
	protected void initFromChangedProperties() {
		List<Font> list = new ArrayList<>();
		list.addAll(toFonts(fonts));
		list.addAll(toFonts(additionalFonts));
		Collections.sort(list);
		fontList = List.copyOf(list);
	}
	
	public String getFontSizes() {
		return fontSizes;
	}
	
	public List<Font> getFontList() {
		return fontList == null ? List.of() : fontList;
	}
	
	private List<Font> toFonts(String value) {
		List<Font> list = new ArrayList<>();
		String[] fontLines = value.split("[;]");
		for(String fontLine:fontLines) {
			String[] infos = fontLine.split("[=]");
			if(infos.length == 2) {
				list.add(new Font(infos[0], infos[1]));
			}
		}
		return list;
	}
	
	public String getFonts() {
		return fonts;
	}

	public String getAdditionalFonts() {
		return additionalFonts;
	}
	
	public static class Font implements Comparable<Font> {
		
		private final String name;
		private final String alternative;
		
		public Font(String name, String alternative) {
			this.name = name;
			this.alternative = alternative;
		}
		
		public String getName() {
			return name;
		}
		
		public String getAlternative() {
			return alternative;
		}

		@Override
		public int compareTo(Font o) {
			return name.compareTo(o.name);
		}
	}
}
