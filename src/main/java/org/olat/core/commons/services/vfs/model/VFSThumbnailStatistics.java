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
package org.olat.core.commons.services.vfs.model;

/**
 * 
 * Initial date: 10 Jan 2020<br>
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 *
 */
public class VFSThumbnailStatistics {

	private Long thumbnailsAmount;
	private Long thumbnailsSize;

	public VFSThumbnailStatistics() {
		thumbnailsAmount = Long.valueOf(0);
		thumbnailsSize = Long.valueOf(0);
	}
	
	public VFSThumbnailStatistics(Long thumbnailsAmount, Long thumbnailsSize) {
		this.thumbnailsAmount = thumbnailsAmount;
		this.thumbnailsSize = thumbnailsSize;
	}
	
	public long getThumbnailsAmount() {
		return thumbnailsAmount == null ? 0 : thumbnailsAmount.longValue();
	}

	public long getThumbnailsSize() {
		return thumbnailsSize == null ? 0 : thumbnailsSize.longValue();
	}
}
