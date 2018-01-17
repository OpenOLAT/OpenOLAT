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

import java.util.ArrayList;
import java.util.List;

import org.olat.modules.portfolio.PageStatus;
import org.olat.modules.portfolio.PageUserStatus;

/**
 * 
 * Initial date: 12 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SearchSharePagesParameters {
	
	private String searchString;
	private boolean bookmarkOnly;
	private List<PageStatus> excludedStatus = new ArrayList<>(6);
	private List<PageUserStatus> excludedUserStatus = new ArrayList<>(6);

	public boolean isBookmarkOnly() {
		return bookmarkOnly;
	}

	public void setBookmarkOnly(boolean bookmarkOnly) {
		this.bookmarkOnly = bookmarkOnly;
	}

	public String getSearchString() {
		return searchString;
	}

	public void setSearchString(String searchString) {
		this.searchString = searchString;
	}
	
	public  List<PageStatus> getExcludedPageStatus() {
		return excludedStatus;
	}
	
	public void addExcludedPageStatus(PageStatus... status) {
		if(status != null && status.length > 0) {
			for(PageStatus state:status) {
				if(state != null && !excludedStatus.contains(state)) {
					excludedStatus.add(state);
				}
			}
		}
	}
	
	public  List<PageUserStatus> getExcludedPageUserStatus() {
		return excludedUserStatus;
	}
	
	public void addExcludedPageUserStatus(PageUserStatus... status) {
		if(status != null && status.length > 0) {
			for(PageUserStatus state:status) {
				if(state != null && !excludedUserStatus.contains(state)) {
					excludedUserStatus.add(state);
				}
			}
		}
	}
}