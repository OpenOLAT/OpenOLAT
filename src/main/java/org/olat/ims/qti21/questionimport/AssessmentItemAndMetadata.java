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
package org.olat.ims.qti21.questionimport;

import java.util.Locale;

import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
import org.olat.ims.qti21.model.xml.AssessmentItemBuilder;
import org.olat.ims.qti21.model.xml.AssessmentItemMetadata;
import org.olat.ims.qti21.model.xml.ManifestBuilder;
import org.olat.ims.qti21.model.xml.ManifestMetadataBuilder;

/**
 * 
 * Initial date: 16.02.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentItemAndMetadata extends AssessmentItemMetadata {

	private String description;
	
	private final AssessmentItemBuilder item;
	
	public AssessmentItemAndMetadata(AssessmentItemBuilder item) {
		this.item = item;
		setQuestionType(item.getQuestionType());
	}

	public AssessmentItemBuilder getItemBuilder() {
		return item;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setTitle(String title) {
		item.setTitle(title);
	}
	
	@Override
	public void fromBuilder(ManifestMetadataBuilder metadata) {
		super.fromBuilder(metadata);
		description = metadata.getDescription();
		if(StringHelper.containsNonWhitespace(metadata.getTitle()) && !StringHelper.containsNonWhitespace(item.getTitle())) {
			setTitle(metadata.getTitle());
		}
	}
	
	@Override
	public void toBuilder(ManifestMetadataBuilder metadata, Locale locale) {
		super.toBuilder(metadata, locale);
		metadata.setTechnicalFormat(ManifestBuilder.ASSESSMENTITEM_MIMETYPE);
		if(StringHelper.containsNonWhitespace(item.getTitle())) {
			metadata.setTitle(item.getTitle(), locale.getLanguage());
		}
		if(StringHelper.containsNonWhitespace(description)) {
			String cleanedDescription = FilterFactory.getHtmlTagsFilter().filter(description);
			metadata.setDescription(cleanedDescription, locale.getLanguage());
		}
	}
}
