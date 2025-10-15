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
	
	String getTitle();
	
	String getSubTitle();
	
	String getSubTitleFull();
	
	String getImageSrc();
	
	String getImageAlt();
	
	public class ObjectOptionValues implements ObjectOption {
		
		private final String key;
		private final String title;
		private final String subTitle;
		private final String subTitleFull;
		private final String imageSrc;
		private final String imageAlt;
		
		public ObjectOptionValues(String key, String title, String subTitle, String subTitleFull) {
			this(key, title, subTitle, subTitleFull, null, null);
		}
		
		public ObjectOptionValues(String key, String title, String subTitle, String subTitleFull, String imageSrc, String imageAlt) {
			this.key = key;
			this.title = title;
			this.subTitle = subTitle;
			this.subTitleFull = subTitleFull;
			this.imageSrc = imageSrc;
			this.imageAlt = imageAlt;
		}

		@Override
		public String getKey() {
			return key;
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
		public String getSubTitleFull() {
			return subTitleFull;
		}
		
		@Override
		public String getImageSrc() {
			return imageSrc;
		}
		
		@Override
		public String getImageAlt() {
			return imageAlt;
		}
		
	}
	
	public static <T> String createShortPath(List<T> path, Function<T, String> valueExtractor) {
		StringBuilder sb = new StringBuilder();
		if (path.size() == 1) {
			sb.append(valueExtractor.apply(path.get(0)));
			sb.append(" /");
		} else if (path.size() == 2) {
			sb.append(valueExtractor.apply(path.get(0)));
			sb.append(" / ");
			sb.append(valueExtractor.apply(path.get(1)));
			sb.append(" /");
		} else if (path.size() > 2) {
			sb.append(valueExtractor.apply(path.get(0)));
			sb.append(" / ... / ");
			sb.append(valueExtractor.apply(path.get(path.size() - 1)));
			sb.append(" /");
		}
		return sb.toString();
	}
	
	public static <T> String createFullPath(List<T> path, Function<T, String> valueExtractor) {
		String fillPath = path.stream().map(t -> valueExtractor.apply(t)).collect(Collectors.joining(" / "));
		if (StringHelper.containsNonWhitespace(fillPath)) {
			fillPath += " /";
		}
		return fillPath;
	}

}
