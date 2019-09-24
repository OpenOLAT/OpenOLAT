/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.portfolio.model;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

/**
 * Description:<br>
 * Object to hold settings for the artefact filter
 * 
 * if a new filter is added, make sure to add it to filterList and to check for emptiness in isFilterEmpty(). 
 * <P>
 * Initial Date:  21.07.2010 <br>
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
public class EPFilterSettings {
	

	private List<String> tagFilter = new ArrayList<>();
	private List<String> typeFilter = new ArrayList<>();
	private String textFilter = "";
	private List<Date> dateFilter = new ArrayList<>();
	private List<Object> filterList = new ArrayList<>();
	private String filterName;
	private String filterId = "";
	private boolean noTagFilterSet = false;

	/**
	 * @return Returns the filterName.
	 */
	public String getFilterName() {
		return filterName;
	}

	/**
	 * @param filterName The filterName to set.
	 */
	public void setFilterName(String filterName) {
		this.filterName = filterName;
	}

	/**
	 * 
	 */
	public EPFilterSettings() {
		filterList.add(tagFilter);
		filterList.add(typeFilter);
		filterList.add(textFilter);
		filterList.add(dateFilter);
		setFilterIdToUniqueId();
	}
	
	/**
	 * @return Returns the internal filterId.
	 */
	public String getFilterId() {
		return filterId;
	}
	
	/**
	 * set or reset the filterID automatically to a unique id
	 */
	public void setFilterIdToUniqueId(){
		filterId = UUID.randomUUID().toString();
	}

	/**
	 * @return Returns the filterList.
	 */
	public List<Object> getFilterList() {
		return filterList;
	}
		
	/**
	 * @return Returns the tagFilter.
	 */
	public List<String> getTagFilter() {
		return tagFilter;
	}
	/**
	 * @param tagFilter The tagFilter to set.
	 */
	public void setTagFilter(List<String> tagFilter) {
		this.tagFilter = tagFilter;
		noTagFilterSet = false;
	}
	
	// use this to set tag filter to "none" to filter resources without a tag!
	public void setNoTagFilter() {
		this.tagFilter = new ArrayList<>();
		noTagFilterSet = true;
	}
	
	public boolean isNoTagFilterSet() {
		return tagFilter.isEmpty() && noTagFilterSet;
	}
	
	/**
	 * @return Returns the typeFilter.
	 */
	public List<String> getTypeFilter() {
		return typeFilter;
	}
	/**
	 * @param typeFilter The typeFilter to set.
	 */
	public void setTypeFilter(List<String> typeFilter) {
		this.typeFilter = typeFilter;
	}
	/**
	 * @return Returns the textFilter.
	 */
	public String getTextFilter() {
		return textFilter;
	}
	/**
	 * @param textFilter The textFilter to set.
	 */
	public void setTextFilter(String textFilter) {
		this.textFilter = textFilter;
	}
	/**
	 * @return Returns the dateFilter.
	 */
	public List<Date> getDateFilter() {
		return dateFilter;
	}
	/**
	 * @param dateFilter must be two dates (from, to), where the first is before second.
	 */
	public void setDateFilter(List<Date> dateFilter) {
		this.dateFilter = dateFilter;
	}
	
	/**
	 * returns true if no filter is set.
	 * @return
	 */
	public boolean isFilterEmpty() {
		if (getTagFilter().isEmpty() && getTypeFilter().isEmpty() && 
				getTextFilter().equals("") && getDateFilter().isEmpty() && !noTagFilterSet) { 
			return true; 
			}
		return false;
	}
	
	public EPFilterSettings cloneAfterFullText() {
		EPFilterSettings clone = new EPFilterSettings();
		if(tagFilter != null) {
			clone.tagFilter = new ArrayList<>(tagFilter);
		}
		if(typeFilter != null) {
			clone.typeFilter = new ArrayList<>(typeFilter);
		}
		if(dateFilter != null) {
			clone.dateFilter = new ArrayList<>(dateFilter);
		}
		clone.filterName = filterName;
		clone.filterId = filterId;
		return clone;
	}
}
