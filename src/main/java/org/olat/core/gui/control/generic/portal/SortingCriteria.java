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
* <p>
*/

package org.olat.core.gui.control.generic.portal;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;

import org.olat.core.logging.AssertException;

/**
 * 
 * Description:<br>
 * Encapsulates the sorting info for a portlet.
 * The info is intended to be stored as a map so the conversion businessObject -> Map
 * and vice versa is also provided here.
 * 
 * <P>
 * Initial Date:  15.11.2007 <br>
 * @author Lavinia Dumitrescu
 */
public class SortingCriteria {
	
	public static final int ALPHABETICAL_SORTING = 0;
	public static final int DATE_SORTING = 1;
	public static final int TYPE_SORTING = 2;
	
	public static final int AUTO_SORTING = 0;
	public static final int MANUAL_SORTING = 1;
	
	private int sortingType = AUTO_SORTING; //default
	private int maxEntries = 6; //default value
	private int sortingTerm = ALPHABETICAL_SORTING; //0=Alphabetically, 1=Date, 2=Type
	private List<Integer> sortingTermsList = new ArrayList<>();
	private boolean ascending;
	
	//storage map keys
	private static final String SORTING_TYPE_KEY = "type"; //values: 0=auto, 1=manually sorting
	private static final String NUM_ENTRIES_KEY = "num"; //values: any two digits positive Integer
	private static final String SORTING_TERM_KEY = "term"; //values: 0=Alphabetically, 1=Date, 2=Type
	private static final String SORTING_DIRECTION = "direction"; //values: 1=ascending, 0=descending
	
	/**
	 * Auto sorting constructor.
	 *
	 */
	public SortingCriteria(List<Integer> sortingTermsList, int maxEntries) {		
		this(AUTO_SORTING, maxEntries, ALPHABETICAL_SORTING, true, sortingTermsList);
	}
	
	private SortingCriteria(int sortingType, int maxEntries, int sortingTerm, boolean ascending, List<Integer> sortingTermsList) {		
		this.setSortingType(sortingType);
		this.maxEntries = maxEntries;
		this.setSortingTerm(sortingTerm);
		this.ascending = ascending;
		this.sortingTermsList = sortingTermsList;
	}
	
	public SortingCriteria(Map<String,Integer> paramMap, List<Integer> sortingTermsList, int defaultMaxEntries) {
		this(sortingTermsList, defaultMaxEntries);
		if(paramMap!=null) {
			if(paramMap.containsKey(SORTING_TYPE_KEY)) {
				setSortingType(paramMap.get(SORTING_TYPE_KEY));
				if(getSortingType()==AUTO_SORTING) {
					maxEntries = paramMap.get(NUM_ENTRIES_KEY);
					ascending = paramMap.get(SORTING_DIRECTION)==1;
					setSortingTerm(paramMap.get(SORTING_TERM_KEY));
				}
			}			  
		}
	}
	
	/**
	 * Manual sorting constructor.
	 *
	 */
	public SortingCriteria(){
		this.setSortingType(MANUAL_SORTING);
	}
	
	/**
	 * 
	 * @return a Map with the current sortingCriteria attribute values.
	 */
	public Map<String, Integer> getPersistable() {
		Map<String, Integer> returnMap = new Hashtable<>();
		if (AUTO_SORTING == this.getSortingType()) {
			returnMap.put(SORTING_TYPE_KEY, AUTO_SORTING);
			returnMap.put(NUM_ENTRIES_KEY, this.getMaxEntries());
			returnMap.put(SORTING_DIRECTION, this.isAscending() ? 1 : 0);
			returnMap.put(SORTING_TERM_KEY, this.getSortingTerm());
		} else if (MANUAL_SORTING == this.getSortingType()) {
			returnMap.put(SORTING_TYPE_KEY, MANUAL_SORTING);
		}
		return returnMap;
	}

	public boolean isAscending() {
		return ascending;
	}

	public void setAscending(boolean ascending) {
		this.ascending = ascending;
	}

	public int getMaxEntries() {
		return maxEntries;
	}

	public void setMaxEntries(int maxEntries) {
		if(maxEntries<=0) {
			throw new AssertException("invalid number of max portlet entries");
		}
		this.maxEntries = maxEntries;
	}

	public int getSortingTerm() {
		return sortingTerm;
	}

	public void setSortingTerm(int sortingTerm) {
		if(sortingTerm<0 || sortingTerm>2) {
			throw new AssertException("invalid sorting term for portlet");
		}
		this.sortingTerm = sortingTerm;
	}

	public int getSortingType() {
		return sortingType;
	}

	public void setSortingType(int sortingType) {
		if(sortingTerm<0 || sortingTerm>1) {
			throw new AssertException("invalid sorting type for portlet");
		}
		this.sortingType = sortingType;
	}

	public List<Integer> getSortingTermsList() {
		return sortingTermsList;
	}
	
	
}
