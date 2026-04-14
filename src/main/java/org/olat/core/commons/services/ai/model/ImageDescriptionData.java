/**
 * <a href="https://www.openolat.org">
 * OpenOlat - Online Learning and Training</a><br>
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
package org.olat.core.commons.services.ai.model;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.util.StringHelper;

import dev.langchain4j.model.output.structured.Description;

/**
 * Parsed data from chat response containing image description metadata.
 *
 * Initial date: 19.03.2026<br>
 *
 * @author gnaegi@frentix.com, https://www.frentix.com
 *
 */
public class ImageDescriptionData {
	@Description("Short descriptive title, max 10 words")
	private String title;
	@Description("2-3 sentences describing the image in detail, suitable for full-text search")
	private String description;
	@Description("Accessible description for screen readers, precise and informative, only relevant details, very short, avoids redundancy, does not start with 'Image of' or 'Picture of'")
	private String altText;
	@Description("Academic or professional subject area, 1-2 words, e.g. Biology, Computer Science, Marketing, History, Mathematics, Medicine, Art")
	private String subject;
	@Description("Exactly one of: horizontal, vertical, square")
	private String orientation;
	@Description("1-2 dominant colors; use b&w for grayscale; empty list if no clear dominant color")
	private List<String> colorTags = new ArrayList<>();
	@Description("1-2 categories describing the image content")
	private List<String> categoryTags = new ArrayList<>();
	@Description("1-4 descriptive tags, stock-photo style, singular form (e.g. tree not trees)")
	private List<String> keywords = new ArrayList<>();

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getAltText() {
		return altText;
	}

	public void setAltText(String altText) {
		this.altText = altText;
	}

	public String getSubject() {
		return subject;
	}

	public void setSubject(String subject) {
		this.subject = subject;
	}

	public String getOrientation() {
		return orientation;
	}

	public void setOrientation(String orientation) {
		this.orientation = orientation;
	}

	public List<String> getColorTags() {
		return colorTags;
	}

	public void setColorTags(List<String> colorTags) {
		this.colorTags = colorTags;
	}

	public void addColorTag(String colorTag) {
		if (StringHelper.containsNonWhitespace(colorTag)) {
			this.colorTags.add(colorTag);
		}
	}

	public List<String> getCategoryTags() {
		return categoryTags;
	}

	public void setCategoryTags(List<String> categoryTags) {
		this.categoryTags = categoryTags;
	}

	public void addCategoryTag(String categoryTag) {
		if (StringHelper.containsNonWhitespace(categoryTag)) {
			this.categoryTags.add(categoryTag);
		}
	}

	public List<String> getKeywords() {
		return keywords;
	}

	public void setKeywords(List<String> keywords) {
		this.keywords = keywords;
	}

	public void addKeyword(String keyword) {
		if (StringHelper.containsNonWhitespace(keyword)) {
			this.keywords.add(keyword);
		}
	}
}
