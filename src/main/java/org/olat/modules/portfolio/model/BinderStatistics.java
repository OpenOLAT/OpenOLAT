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

import java.util.Date;

import org.olat.modules.portfolio.BinderLight;

/**
 * Some binder infos and statistics
 * 
 * Initial date: 21.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BinderStatistics implements BinderLight {
	
	private final Long key;
	private final String title;
	private final String imagePath;
	private final Date lastModified;
	private final int numOfSections;
	private final int numOfPages;
	private final String status;
	private final int numOfComments;
	private final String entryDisplayname;
	
	public BinderStatistics(Long key, String title, String imagePath, Date lastModified, int numOfSections, int numOfPages,
			String status, String entryDisplayname, int numOfComments) {
		this.key = key;
		this.title = title;
		this.imagePath = imagePath;
		this.lastModified = lastModified;
		this.numOfSections = numOfSections;
		this.numOfPages = numOfPages;
		this.status = status;
		this.entryDisplayname = entryDisplayname;
		this.numOfComments = numOfComments;
	}
	
	@Override
	public Long getKey() {
		return key;
	}
	
	public String getTitle() {
		return title;
	}

	@Override
	public String getImagePath() {
		return imagePath;
	}
	
	public Date getLastModified() {
		return lastModified;
	}
	
	public int getNumOfSections() {
		return numOfSections;
	}
	
	public int getNumOfPages() {
		return numOfPages;
	}
	
	public String getStatus() {
		return status;
	}
	
	public int getNumOfComments() {
		return numOfComments;
	}

	public String getEntryDisplayname() {
		return entryDisplayname;
	}

	
}
