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
package org.olat.repository.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.id.context.BusinessControlFactory;

/**
 * Used in the view elements
 * 
 * 
 * Initial date: 20.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class RepositoryEntryDetails {
	
	private boolean marked;
	private boolean selected;
	private boolean thumbnail;
	
	private Long key;
	private String name;
	private String author;
	private String description;
	private String thumbnailRelPath;
	
	private String score;
	private Boolean passed;
	
	public int visit;
	public long timeSpend;
	public Date initialLaunch;
	public Date recentLaunch;
	
	private String markLinkName;
	private String selectLinkName;
	
	private List<String> buttonNames;
	
	public RepositoryEntryDetails() {
		//
	}
	
	public String getCssClass() {
		return "o_CourseModule_icon";
	}

	public Long getKey() {
		return key;
	}

	public void setKey(Long key) {
		this.key = key;
	}

	public String getDisplayName() {
		return name;
	}
	
	public void setDisplayName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getSelectLinkName() {
		return selectLinkName;
	}

	public void setSelectLinkName(String name) {
		selectLinkName = name;
	}

	public String getMarkLinkName() {
		return markLinkName;
	}

	public void setMarkLinkName(String markLinkName) {
		this.markLinkName = markLinkName;
	}

	/**
	 * Possibly immutable
	 * @return
	 */
	public List<String> getButtonNames() {
		if(buttonNames == null) {
			return Collections.emptyList();
		}
		return buttonNames;
	}

	public void addButtonName(String buttonName) {
		if(buttonNames == null) {
			buttonNames = new ArrayList<String>(5);
		}
		buttonNames.add(buttonName);
	}

	public String getBusinessPath() {
		
		String url = BusinessControlFactory.getInstance().getRelativeURLFromBusinessPathString("[RepositoryEntry:" + key + "]");

		return url;
	}
	
	public String getAuthor() {
		return author;
	}
	
	public void setAuthor(String author) {
		this.author = author;
	}
	
	public String getThumbnailRelPath() {
		return thumbnailRelPath;
	}
	
	public void setThumbnailRelPath(String path) {
		this.thumbnailRelPath = path;
	}
	
	public boolean isThumbnailAvailable() {
		return thumbnail;
	}
	
	public void setThumbnailAvailable(boolean thumbnail) {
		this.thumbnail = thumbnail;
	}

	public boolean isMarked() {
		return marked;
	}

	public void setMarked(boolean marked) {
		this.marked = marked;
	}

	public boolean isSelected() {
		return selected;
	}

	public void setSelected(boolean selected) {
		this.selected = selected;
	}

	public String getScore() {
		return score;
	}

	public void setScore(String score) {
		this.score = score;
	}

	public boolean isPassed() {
		return passed != null && passed.booleanValue();
	}

	public void setPassed(Boolean passed) {
		this.passed = passed;
	}
	
	public boolean isFailed() {
		return passed != null && !passed.booleanValue();
	}

	public int getVisit() {
		return visit;
	}

	public void setVisit(int visit) {
		this.visit = visit;
	}

	public long getTimeSpend() {
		return timeSpend;
	}

	public void setTimeSpend(long timeSpend) {
		this.timeSpend = timeSpend;
	}

	public Date getInitialLaunch() {
		return initialLaunch;
	}

	public void setInitialLaunch(Date initialLaunch) {
		this.initialLaunch = initialLaunch;
	}

	public Date getRecentLaunch() {
		return recentLaunch;
	}

	public void setRecentLaunch(Date recentLaunch) {
		this.recentLaunch = recentLaunch;
	}
}
