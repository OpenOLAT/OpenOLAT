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
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.modules.portfolio.MediaLight;

/**
 * 
 * Initial date: 20.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MediaRow implements MediaLight {
	
	private final MediaLight media;

	private final String cssClass;
	private final VFSLeaf thumbnail;
	private final FormLink openFormLink;
	
	private List<String> categories;
	
	public MediaRow(MediaLight media, VFSLeaf thumbnail, FormLink openFormLink, String cssClass) {
		this.media = media;
		this.cssClass = cssClass;
		this.thumbnail = thumbnail;
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
		return media.getCollectionDate();
	}

	@Override
	public String getType() {
		return media.getType();
	}

	public String getIconCssClass() {
		return cssClass;
	}

	@Override
	public String getStoragePath() {
		return media.getStoragePath();
	}

	@Override
	public String getRootFilename() {
		return media.getRootFilename();
	}

	@Override
	public String getDescription() {
		return media.getDescription();
	}

	@Override
	public String getBusinessPath() {
		return media.getBusinessPath();
	}
	
	public List<String> getCategories() {
		return categories;
	}
	
	public void addCategory(String category) {
		if(categories == null) {
			categories = new ArrayList<>();
		}
		categories.add(category);
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
