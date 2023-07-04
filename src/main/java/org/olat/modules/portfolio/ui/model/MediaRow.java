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
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.cemedia.MediaLight;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaRow implements MediaLight {
	
	private final MediaLight media;
	private final Date collectionDate;

	private final String cssClass;
	private final VFSLeaf thumbnail;
	private final FormLink openFormLink;
	private boolean versioned;
	
	private List<String> tags;
	private List<String> taxonomyLevelsNames;
	private List<TaxonomyLevel> taxonomyLevels;
	
	public MediaRow(MediaLight media, Date collectionDate, VFSLeaf thumbnail, FormLink openFormLink, String cssClass) {
		this.media = media;
		this.cssClass = cssClass;
		this.thumbnail = thumbnail;
		this.collectionDate = collectionDate;
		this.openFormLink = openFormLink;
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
	public String getTitle() {
		return media.getTitle();
	}

	@Override
	public Date getCollectionDate() {
		return collectionDate;
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
		return thumbnail != null;
	}
	
	public VFSLeaf getThumbnail() {
		return thumbnail;
	}
	
	public String getThumbnailName() {
		return thumbnail == null ? null : thumbnail.getName();
	}

}
