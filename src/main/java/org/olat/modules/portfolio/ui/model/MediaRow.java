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
package org.olat.modules.portfolio.ui.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.model.DublinCoreMetadata;
import org.olat.modules.cemedia.MediaLight;
import org.olat.modules.cemedia.MediaVersion;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.video.VideoFormatExtended;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaRow implements MediaLight {
	
	private final MediaLight media;
	private final MediaVersion version;

	private final String cssClass;
	private final FormLink openFormLink;
	private boolean versioned;
	private boolean hasThumbnail;
	private final String thumbnailName;
	
	private List<String> tags;
	private List<String> taxonomyLevelsNames;
	private List<TaxonomyLevel> taxonomyLevels;
	
	public MediaRow(MediaLight media, MediaVersion version, boolean hasThumbnail, FormLink openFormLink, String cssClass) {
		this.media = media;
		this.version = version;
		this.cssClass = cssClass;
		this.hasThumbnail = hasThumbnail;
		this.openFormLink = openFormLink;
		if(version == null) {
			thumbnailName = "";
		} else {
			thumbnailName = (version.getCollectionDate() == null ? 0l : version.getCollectionDate().getTime()) + "/" + version.getRootFilename();
		}
	}
	
	@Override
	public Long getKey() {
		return media.getKey();
	}
	
	@Override
	public Date getCreationDate() {
		return media.getCreationDate();
	}
	
	@Override
	public String getResourceableTypeName() {
		return media.getResourceableTypeName();
	}

	@Override
	public Long getResourceableId() {
		return media.getKey();
	}

	@Override
	public String getTitle() {
		return media.getTitle();
	}

	@Override
	public Date getCollectionDate() {
		return version == null ? null : version.getCollectionDate();
	}

	@Override
	public String getType() {
		return media.getType();
	}

	public String getIconCssClass() {
		return cssClass;
	}

	@Override
	public String getDescription() {
		return media.getDescription();
	}

	@Override
	public String getBusinessPath() {
		return media.getBusinessPath();
	}
	
	public boolean isVersioned() {
		return versioned;
	}

	public void setVersioned(boolean versioned) {
		this.versioned = versioned;
	}
	
	public MediaVersion getVersion() {
		return version;
	}

	public List<String> getTags() {
		return tags;
	}
	
	public boolean hasTags() {
		return tags != null && !tags.isEmpty();
	}
	
	public void addTag(String tag) {
		if(tags == null) {
			tags = new ArrayList<>();
		}
		if(StringHelper.containsNonWhitespace(tag) && !tags.contains(tag)) {
			tags.add(tag);
		}
	}

	public List<TaxonomyLevel> getTaxonomyLevels() {
		return taxonomyLevels;
	}
	
	public boolean hasTaxonomyLevels() {
		return taxonomyLevels != null && !taxonomyLevels.isEmpty();
	}

	public void addTaxonomyLevel(TaxonomyLevel taxonomyLevel, String levelName) {
		if(taxonomyLevels == null) {
			taxonomyLevels = new ArrayList<>();
		}
		if(!taxonomyLevels.contains(taxonomyLevel)) {
			taxonomyLevels.add(taxonomyLevel);
		}
		if(taxonomyLevelsNames == null) {
			taxonomyLevelsNames = new ArrayList<>();
		}
		if(!taxonomyLevelsNames.contains(levelName)) {
			taxonomyLevelsNames.add(levelName);
		}
	}
	
	public List<String> getTaxonomyLevelsNames() {
		return taxonomyLevelsNames;
	}

	public FormLink getOpenFormItem() {
		return openFormLink;
	}
	
	public boolean isThumbnailAvailable() {
		return version != null && hasThumbnail;
	}
	
	public void setThumbnailAvailable(boolean available) {
		this.hasThumbnail = available;
	}
	
	public String getThumbnailName() {
		return thumbnailName;
	}

	public Object getSource() {
		if (media instanceof DublinCoreMetadata dublinCoreMetadata) {
			if (StringHelper.containsNonWhitespace(dublinCoreMetadata.getSource())) {
				return dublinCoreMetadata.getSource();
			}
		}
		return null;
	}

	public Object getPlatform(Translator translator) {
		if (getVersion().hasUrl()) {
			VideoFormatExtended videoFormat = VideoFormatExtended.valueOfUrl(getVersion().getVersionMetadata().getUrl());
			if (videoFormat != null) {
				return translator.translate(videoFormat.getI18nKey());
			}
		}
		return null;
	}
}
