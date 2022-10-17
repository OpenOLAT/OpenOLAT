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
package org.olat.modules.portfolio.model;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Transient;

import org.olat.core.util.StringHelper;
import org.olat.modules.ceditor.ContentEditorXStream;
import org.olat.modules.ceditor.model.ImageElement;
import org.olat.modules.ceditor.model.ImageSettings;
import org.olat.modules.ceditor.model.StoredData;
import org.olat.modules.portfolio.Media;
import org.olat.modules.portfolio.handler.ImageHandler;

/**
 * 
 * Initial date: 17.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Entity(name="pfmediapart")
public class MediaPart extends AbstractPart implements ImageElement {

	private static final long serialVersionUID = -5902348088983758191L;
	
	@ManyToOne(targetEntity=MediaImpl.class,fetch=FetchType.LAZY,optional=false)
	@JoinColumn(name="fk_media_id", nullable=false, insertable=true, updatable=false)
	private Media media;

	public Media getMedia() {
		return media;
	}

	public void setMedia(Media media) {
		this.media = media;
	}
	
	@Override
	public StoredData getStoredData() {
		return media;
	}

	@Override
	public ImageSettings getImageSettings() {
		if(ImageHandler.IMAGE_TYPE.equals(getType()) && StringHelper.containsNonWhitespace(getLayoutOptions())) {
			return ContentEditorXStream.fromXml(getLayoutOptions(), ImageSettings.class);
		}
		return null;
	}

	@Override
	@Transient
	public String getType() {
		return media.getType();
	}

}
