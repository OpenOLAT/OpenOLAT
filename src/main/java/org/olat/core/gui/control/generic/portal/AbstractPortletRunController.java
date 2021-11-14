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

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.prefs.Preferences;

/**
 * Provides sorting functionality to the XXXPortletRunController.
 * @author Lavinia Dumitrescu
 *
 */
public abstract class AbstractPortletRunController<T> extends BasicController {

	private static final String SORTING_CRITERIA_PREF = "sortingCriteria";
	protected static final String SORTED_ITEMS_PREF = "sortedItems";
	
	protected PortletToolSortingControllerImpl<T> portletToolsController;
	protected final Collator collator;
	protected SortingCriteria sortingCriteria;
	protected ArrayList<Integer> sortingTermsList = new ArrayList<>();
	private final String portletName;
	protected final Preferences guiPreferences; 
	private final int defaultMaxEntries;

	
	public AbstractPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans,
			String portletName, int defaultMaxEntries) {
		super(ureq, wControl, trans);		
		collator = Collator.getInstance();
		this.portletName = portletName;
		this.defaultMaxEntries = defaultMaxEntries;
		this.guiPreferences = ureq.getUserSession().getGuiPreferences();
	}
	
	public int getDefaultMaxEntries() {
		return defaultMaxEntries;
	}

	/**
	 * Handles portletToolsController events.
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == portletToolsController) {
			if (event.getCommand().equals(PortletToolSortingControllerImpl.COMMAND_AUTO_SORTING)) {				
				sortingCriteria = portletToolsController.getSortingCriteria();
				reloadModel(sortingCriteria);
				saveSortingConfiguration(ureq, sortingCriteria);
			} else if(event.getCommand().equals(PortletToolSortingControllerImpl.COMMAND_MANUAL_SORTING)) {
				List<PortletEntry<T>> sortedItems = portletToolsController.getSortedItems();
				reloadModel(sortedItems);
				saveManuallySortedItems(ureq, sortedItems);
				sortingCriteria = new SortingCriteria();
				portletToolsController.setSortingCriteria(sortingCriteria);
			}
		}		
	}
	
	/**
	 * Store the sortingCriteria for the current portlet. Auto sorting was choosed.
	 * @param ureq
	 * @param sortingCriteria
	 */
	protected void saveSortingConfiguration(UserRequest ureq, SortingCriteria sortingCriteria) {		
		Preferences guiPreferences = ureq.getUserSession().getGuiPreferences();
		guiPreferences.putAndSave(Map.class, getPreferenceKey(SORTING_CRITERIA_PREF), sortingCriteria.getPersistable());
		if(sortingCriteria.getSortingType()==SortingCriteria.AUTO_SORTING) {
			Map<Long, Integer> storedPrefs = (Map<Long, Integer>) guiPreferences.get(Map.class, getPreferenceKey(SORTED_ITEMS_PREF));
			if (storedPrefs != null) {
		    //if auto sorting choosed, remove any manually sorting info
		    List<PortletEntry<T>> sortedItems = new ArrayList<>();
		    guiPreferences.putAndSave(Map.class, getPreferenceKey(SORTED_ITEMS_PREF), getSortedItemsMap(sortedItems));
			}
		}
	}
	
	/**
	 * Retrieves the persistent sortingCriteria for the current portlet for the current user.
	 * Check the persistent sorting type (auto/manual) if none found auto is the default.
	 * This has to be implemented by every portlet since the portlets could have different
	 * set of sorting terms.
	 * @param ureq
	 * @return the persistent sortingCriteria if any found, else a default sortingCriteria.
	 */
	protected SortingCriteria getPersistentSortingConfiguration(UserRequest ureq) {
  	SortingCriteria returnSortingCriteria = null;

		Preferences guiPreferences = ureq.getUserSession().getGuiPreferences();
		Map<String, Integer> storedPrefs = (Map<String, Integer>)guiPreferences.get(Map.class, getPreferenceKey(SORTING_CRITERIA_PREF));
		
		if (storedPrefs != null) {
			returnSortingCriteria = new SortingCriteria( storedPrefs, sortingTermsList, defaultMaxEntries);
		} else {
			returnSortingCriteria = createDefaultSortingCriteria();
		}
		return returnSortingCriteria;
  }
	
	protected SortingCriteria createDefaultSortingCriteria() {
		return new SortingCriteria(sortingTermsList, defaultMaxEntries);
	}
	
	/**
   * Stores the manually sorted items.
   * @param ureq
   * @param sortedItems
   */
	protected void saveManuallySortedItems(UserRequest ureq, List<PortletEntry<T>> sortedItems) {
  	Preferences guiPreferences = ureq.getUserSession().getGuiPreferences();
  	//store manual sorting type
  	SortingCriteria manualSortingCriteria = new SortingCriteria();
  	guiPreferences.putAndSave(Map.class, getPreferenceKey(SORTING_CRITERIA_PREF), manualSortingCriteria.getPersistable());	
  	//store the sorted items
  	guiPreferences.putAndSave(Map.class, getPreferenceKey(SORTED_ITEMS_PREF), getSortedItemsMap(sortedItems));
  }
	
	/**
	 * Creates a map with the item persistableKey as key and with the item index as value.
	 * @param sortedItems
	 * @return a Map<Long,Integer> with the item persistableKey as key and with the item index as value.
	 */
	private Map<Long,Integer> getSortedItemsMap(List<PortletEntry<T>> sortedItems) {
		Hashtable<Long,Integer> persistableMap = new Hashtable<>(); 
		Iterator<PortletEntry<T>> listIterator = sortedItems.iterator();
		for(int i=0; listIterator.hasNext(); i++) {
			persistableMap.put(listIterator.next().getKey(),new Integer(i)); 
		}
		return persistableMap;
	}
	
	/**
	 * Retrieves the persistent manually sorted items for the current portlet and sorts the 
	 * input PortletEntry list.
	 * <p>
	 * @param ureq
	 * @param persistableList
	 * @return the manually sorted persistable list
	 */
  protected List<PortletEntry<T>> getPersistentManuallySortedItems(List<PortletEntry<T>> portletEntryList) {
		List<PortletEntry<T>> selected = new ArrayList<>();
		@SuppressWarnings("unchecked")
		Map<Long, Integer> storedPrefs = (Map<Long, Integer>) guiPreferences.get(Map.class, getPreferenceKey(SORTED_ITEMS_PREF));
		if (storedPrefs != null) {			
			Iterator<PortletEntry<T>> listIterator = portletEntryList.iterator();
			while (listIterator.hasNext()) {
				PortletEntry<T> portletEntry = listIterator.next();
				if (storedPrefs.containsKey(portletEntry.getKey())) {
					selected.add(portletEntry);
				}
			}
			Collections.sort(selected, getPortletEntryComparator(storedPrefs));
		}
		return selected;
	}
  
  /**
	 * Generates preference keys.
	 * @param prefix
	 * @return
	 */
	protected String getPreferenceKey(String prefix) {
		return prefix + portletName;
	}
	
	/**
	 * Sorts the itemList according with the sortingCriteria. 
	 * The list contains portlet specific objects. 
	 * Each portlet has different types of objects (e.g. BookmarkImpl, BusinessGroup, etc.)
	 * @param itemList
	 * @param sortingCriteria
	 * @param itemComparator
	 * @return
	 */
	protected List<T> getSortedList(List<T> itemList, SortingCriteria sortingCriteria) {
		Comparator<T> itemComparator = getComparator(sortingCriteria);
		Collections.sort(itemList, itemComparator);
		// check here is asscending or descending and return the first max entries
		int maxEntries = sortingCriteria.getMaxEntries();
		List<T> returnList = itemList.subList(0, Math.min(itemList.size(), maxEntries));
		return returnList;
	}
	
	/**
	 * 
	 * @param sortingCriteria
	 * @return a Comparator used for sorting entries according with the input sortingCriteria.
	 * 
	 */
	protected abstract Comparator<T> getComparator(SortingCriteria sortingCriteria);
	
	/**
	 * Reloads the portlet's table tableDataModel according with the sorting criteria.
	 * @param ureq
	 * @param sortingCriteria
	 */
	protected abstract void reloadModel(SortingCriteria sortingCriteria);
	
	/**
	 * Reloads the portlet's table tableDataModel according with the sortedItems list.
	 * @param ureq
	 * @param sortedItems
	 */
	protected abstract void reloadModel(List<PortletEntry<T>> sortedItems);

	/**
	 * Compares PortletEntrys.
	 * Comparator implementation used for sorting PortletEntry entries according with the
	 * choosen manually order (available in the sortedItems map).
	 * @param sortedItems
	 * @return
	 */
	protected Comparator<PortletEntry<T>> getPortletEntryComparator(final Map<Long,Integer> sortedItems) {
		return new Comparator<PortletEntry<T>>(){			
			public int compare(final PortletEntry<T> portletEntry1, final PortletEntry<T> portletEntry2) {
				int comparisonResult = 0;
				Integer portletEntry1Index = sortedItems.get(portletEntry1.getKey());
				Integer portletEntry2Index = sortedItems.get(portletEntry2.getKey()); 
				comparisonResult = portletEntry1Index.compareTo(portletEntry2Index);
			  return comparisonResult;
			}};
	}
	
	@Override
	protected void doDispose() {
		if(portletToolsController!=null) {
			portletToolsController.dispose();
		  portletToolsController = null;
		}
        super.doDispose();
	}
		
}
