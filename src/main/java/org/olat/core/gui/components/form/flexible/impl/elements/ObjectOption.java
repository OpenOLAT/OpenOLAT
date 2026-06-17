/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: Sep 1, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public interface ObjectOption {
	
	String getKey();
	
	public String getOptionCss();
	
	String getTitle();
	
	String getSubTitle();

	String getImageSrc();

	String getImageAlt();

	String getImageHtml();

	public class ObjectOptionValues implements ObjectOption {

		public static final String CSS_TITLE_ONLY = "o_object_selection_title_only";

		private final String key;
		private final String optionCss;
		private final String title;
		private final String subTitle;
		private final String imageSrc;
		private final String imageAlt;
		private final String imageHtml;

		public ObjectOptionValues(String key, String title, String subTitle) {
			this(key, null, title, subTitle, null, null, null);
		}

		public ObjectOptionValues(String key, String optionCss, String title, String subTitle, String imageSrc, String imageAlt, String imageHtml) {
			this.key = key;
			this.optionCss = optionCss;
			this.title = title;
			this.subTitle = subTitle;
			this.imageSrc = imageSrc;
			this.imageAlt = imageAlt;
			this.imageHtml = imageHtml;
		}

		@Override
		public String getKey() {
			return key;
		}

		@Override
		public String getOptionCss() {
			return optionCss;
		}

		@Override
		public String getTitle() {
			return title;
		}

		@Override
		public String getSubTitle() {
			return subTitle;
		}

		@Override
		public String getImageSrc() {
			return imageSrc;
		}

		@Override
		public String getImageAlt() {
			return imageAlt;
		}

		@Override
		public String getImageHtml() {
			return imageHtml;
		}

	}
	
	public static <T> String createFullPath(List<T> path, Function<T, String> valueExtractor, boolean endDivider) {
		String fillPath = path.stream().map(t -> valueExtractor.apply(t)).collect(Collectors.joining(" / "));
		if (endDivider && StringHelper.containsNonWhitespace(fillPath)) {
			fillPath += " /";
		}
		return fillPath;
	}

}
