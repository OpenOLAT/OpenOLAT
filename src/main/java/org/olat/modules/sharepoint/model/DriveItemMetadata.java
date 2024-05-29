/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.sharepoint.model;

import org.olat.core.commons.services.vfs.model.VFSTransientMetadata;
import org.olat.core.util.vfs.VFSExternalItem;
import org.olat.core.util.vfs.VFSExternalMetadata;

/**
 * 
 * Initial date: 29 mai 2024<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DriveItemMetadata extends VFSTransientMetadata implements VFSExternalMetadata {
	
	private VFSExternalItem item;
	private String thumbnailUrl;
	private String largeThumbnailUrl;
	
	public DriveItemMetadata() {
		//
	}

	@Override
	public String getThumbnailUrl() {
		return thumbnailUrl;
	}

	public void setThumbnailUrl(String thumbnailUrl) {
		this.thumbnailUrl = thumbnailUrl;
	}

	@Override
	public String getLargeThumbnailUrl() {
		return largeThumbnailUrl;
	}

	public void setLargeThumbnailUrl(String largeThumbnailUrl) {
		this.largeThumbnailUrl = largeThumbnailUrl;
	}

	@Override
	public VFSExternalItem getItem() {
		return item;
	}
	
	public void setItem(VFSExternalItem item) {
		this.item = item;
	}
}
