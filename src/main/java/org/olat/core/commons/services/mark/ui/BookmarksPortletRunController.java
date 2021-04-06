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

package org.olat.core.commons.services.mark.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import org.olat.NewControllerFactory;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.Util;
import org.olat.core.util.filter.FilterFactory;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;

/**
 * Description:<br>
 * Run view controller for the groups list portlet
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class BookmarksPortletRunController extends AbstractPortletRunController<Bookmark> {
	
	private static final String CMD_LAUNCH = "cmd.launch";
	
	private TableController tableCtr;
	private BookmarkPortletTableDataModel bookmarkListModel;
	private VelocityContainer bookmarksVC;		
	private Link showAllLink;
	
	private final MarkManager markManager;
	private final RepositoryManager repositoryManager;
	private final List<String> types = Collections.singletonList("RepositoryEntry");
			
	/**
	 * Constructor
	 * @param ureq
	 * @param component
	 */
	public BookmarksPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans,
			String portletName, int defaultMaxEntries) { 		
		super(wControl, ureq, trans, portletName, defaultMaxEntries);
		sortingTermsList.add(SortingCriteria.TYPE_SORTING);
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		
		repositoryManager = RepositoryManager.getInstance();
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		
		bookmarksVC = createVelocityContainer("bookmarksPortlet");
		showAllLink = LinkFactory.createLink("bookmarksPortlet.showAll", bookmarksVC, this);
		showAllLink.setIconRightCSS("o_icon o_icon_start");
				
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("bookmarksPortlet.nobookmarks"), null, "o_icon_bookmark_header");
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("o_portlet_table");
		tableConfig.setDisplayRowCount(false);
		tableConfig.setPageingEnabled(false);
		tableConfig.setDownloadOffered(false);
		//disable the default sorting for this table
		tableConfig.setSortingEnabled(false); 
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), trans);
		listenTo(tableCtr);
		// dummy header key, won't be used since setDisplayTableHeader is set to false
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("bookmarksPortlet.bgname", 0, CMD_LAUNCH, trans.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("bookmarksPortlet.type", 1, null, trans.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));
		
		sortingCriteria = getPersistentSortingConfiguration(ureq);
		reloadModel(sortingCriteria);

		bookmarksVC.put("table", tableCtr.getInitialComponent());						
		putInitialPanel(bookmarksVC);
	}
	
	/**
	 * Gets all bookmarks for this identity and converts the list into an PortletEntry list.
	 * @param ureq
	 * @return
	 */
	private List<PortletEntry<Bookmark>> getAllPortletEntries() {
		List<Mark> markList = markManager.getMarks(getIdentity(), types);
		List<Bookmark> bookmarkList = convertMarkToBookmark(markList);
		return convertBookmarkToPortletEntryList(bookmarkList);
	}
	
	/**
	 * Converts list.
	 * @param items
	 * @return
	 */
	private List<PortletEntry<Bookmark>> convertBookmarkToPortletEntryList(List<Bookmark> items) {
		List<PortletEntry<Bookmark>> convertedList = new ArrayList<>();
		for(Bookmark mark:items) {
			convertedList.add(new BookmarkPortletEntry(mark));
		}
		return convertedList;
	}
	
	private  List<Bookmark> convertMarkToBookmark(List<Mark> items) {
		List<Bookmark> convertedList = new ArrayList<>();
		
		List<Long> reKeys = new ArrayList<>();
		for(Mark mark:items) {
			reKeys.add(mark.getOLATResourceable().getResourceableId());
		}
		
		List<RepositoryEntry> repositoryEntries = repositoryManager.lookupRepositoryEntries(reKeys);
		Map<Long,RepositoryEntry> keyToRepositoryEntryMap = new HashMap<>();
		for(RepositoryEntry repositoryEntry:repositoryEntries) {
			keyToRepositoryEntryMap.put(repositoryEntry.getKey(), repositoryEntry);
		}

		for(Mark mark:items) {
			RepositoryEntry repositoryEntry = keyToRepositoryEntryMap.get(mark.getOLATResourceable().getResourceableId());
			if(repositoryEntry != null) {
				convertedList.add(new Bookmark(mark, repositoryEntry));
			}
		}
		return convertedList;
	}
	
	/**
	 * Reloads the bookmarks table model according with the input SortingCriteria.
	 * It first evaluate the sortingCriteria type; if auto get bookmarks from BookmarkManager
	 * and sort the item list according with the sortingCriteria. 
	 * Else get the manually sorted list.
	 * @param identity
	 * @param sortingCriteria
	 */
	protected void reloadModel(SortingCriteria sortingCriteria) {
		if (sortingCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
			List<Mark> markList = markManager.getMarks(getIdentity(), types);
			List<Bookmark> bookmarkList = convertMarkToBookmark(markList);
			bookmarkList = getSortedList(bookmarkList, sortingCriteria );

			List<PortletEntry<Bookmark>> entries = convertBookmarkToPortletEntryList(bookmarkList);
			bookmarkListModel = new BookmarkPortletTableDataModel(entries, getLocale());
			tableCtr.setTableDataModel(bookmarkListModel);
		} else {
			reloadModel(this.getPersistentManuallySortedItems());
		}
	}
	
	/**
	 * Sets the table model if the sorted items list is already available.
	 * @param ureq
	 * @param sortedItems
	 */
	protected void reloadModel(List<PortletEntry<Bookmark>> sortedItems) {					
		bookmarkListModel = new BookmarkPortletTableDataModel(sortedItems, getLocale());
		tableCtr.setTableDataModel(bookmarkListModel);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {		
		if (source == showAllLink){
			// activate homes tab in top navigation and active bookmarks menu item
			String resourceUrl = "[MyCoursesSite:0][Favorits:0]";
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		} 
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_LAUNCH)) {
					int rowid = te.getRowId();
					PortletEntry<Bookmark> bookmark = bookmarkListModel.getObject(rowid);
					String businessPath = bookmark.getValue().getBusinessPath();
					NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
				}
			}
		} 
	}

  
  /**
   * Retrieve the persistent manually sorted items for the current portlet.
   * @param ureq
   * @return
   */
  private List<PortletEntry<Bookmark>> getPersistentManuallySortedItems() {
  	List<PortletEntry<Bookmark>> entries = getAllPortletEntries();
		return getPersistentManuallySortedItems(entries);		
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl istance.
	 */
	protected PortletToolSortingControllerImpl<Bookmark> createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {			
			List<PortletEntry<Bookmark>> entries = getAllPortletEntries();
			PortletDefaultTableDataModel<Bookmark> tableDataModel = new BookmarkManualSortingTableDataModel(entries, ureq.getLocale());
			
			List<PortletEntry<Bookmark>> sortedItems = getPersistentManuallySortedItems(); 
			
			portletToolsController = new PortletToolSortingControllerImpl<>(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
			listenTo(portletToolsController);
			portletToolsController.setConfigManualSorting(true);
			portletToolsController.setConfigAutoSorting(true);
		}		
		return portletToolsController;
	}
	
	/**
	 * Comparator implementation used for sorting bookmarks entries according with the
	 * input sortingCriteria.
	 * <p>
	 * @param sortingCriteria
	 * @return a Comparator for the input sortingCriteria
	 */
	protected Comparator<Bookmark> getComparator(final SortingCriteria sortingCriteria) {
		return new Comparator<Bookmark>(){			
			public int compare(final Bookmark bookmark1, final Bookmark bookmark2) {	
				int comparisonResult = 0;
			  if(sortingCriteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
			  	comparisonResult = collator.compare(bookmark1.getTitle(), bookmark2.getTitle());			  				  	
			  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
			  	comparisonResult = bookmark1.getCreationDate().compareTo(bookmark2.getCreationDate());
			  } else if(sortingCriteria.getSortingTerm()==SortingCriteria.TYPE_SORTING) {
			  	comparisonResult = bookmark1.getDisplayrestype().compareTo(bookmark2.getDisplayrestype());
			  }
			  if(!sortingCriteria.isAscending()) {
			  	//if not isAscending return (-comparisonResult)			  	
			  	return -comparisonResult;
			  }
			  return comparisonResult;
			}};
	}
	
	/**
	 * 
	 * PortletDefaultTableDataModel implementation for the current portlet.
	 * 
	 * <P>
	 * Initial Date:  10.12.2007 <br>
	 * @author Lavinia Dumitrescu
	 */
	private static class BookmarkPortletTableDataModel extends PortletDefaultTableDataModel<Bookmark> {
		private final Locale locale;

		public BookmarkPortletTableDataModel(List<PortletEntry<Bookmark>> objects, Locale locale) {
			super(objects, 2);
			this.locale = locale;
		}
		
		public Object getValueAt(int row, int col) {
			PortletEntry<Bookmark> entry = getObject(row);
			Bookmark bookmark = entry.getValue();
			switch (col) {
			case 0:
				String name = getBookmarkTitle(bookmark);
				return name;
			case 1:
				String resType = bookmark.getDisplayrestype();
				return NewControllerFactory.translateResourceableTypeName(resType, locale);
			default:
				return "ERROR";
			}
    }
		
		/**
		 * Get displayname of a bookmark entry. If bookmark entry a RepositoryEntry 
		 * and is this RepositoryEntry closed then add a prefix to the title.
		 */
		private String getBookmarkTitle(Bookmark bookmark) {
			String title = bookmark.getTitle();
			
			RepositoryEntryStatusEnum status = bookmark.getEntryStatus();
			if (status.decommissioned()) {
				Translator pT = Util.createPackageTranslator(RepositoryModule.class, locale);
				title = "[" + pT.translate("title.prefix.closed") + "] ".concat(title);
			}
			return title;
		}
	}
	
	
	/**
	 * 
	 * Description:<br>
	 * TableDataModel implementation for the bookmark manual sorting.
	 * 
	 * <P>
	 * Initial Date:  23.11.2007 <br>
	 * @author Lavinia Dumitrescu
	 */
	private static class BookmarkManualSortingTableDataModel extends PortletDefaultTableDataModel<Bookmark> {
		private final Locale locale;

		/**
		 * @param objects
		 * @param locale
		 */
		public BookmarkManualSortingTableDataModel(List<PortletEntry<Bookmark>> objects, Locale locale) {
			super(objects, 4);
			this.locale = locale;
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
		 */
		public final Object getValueAt(int row, int col) {
			PortletEntry<Bookmark> entry = getObject(row);
			Bookmark bm = entry.getValue();
			switch (col) {
				case 0:
					return bm.getTitle();
				case 1:
					String desc = bm.getDescription();
					return (desc == null ? "n/a" : FilterFactory.getHtmlTagsFilter().filter(desc));
				case 2:
					String resType = bm.getDisplayrestype();
					return (resType == null ? "n/a" : NewControllerFactory.translateResourceableTypeName(resType, locale));
				case 3:
					Date date = bm.getCreationDate();
					//return DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, getTranslator().getLocale()).format(date);
					//return date else the sorting doesn't work properly
					return date;
				default:
					return "error";
			}
		}
	}
	
	/**
	 * 
	 * PortletEntry impl for Bookmark values.
	 * 
	 * <P>
	 * Initial Date:  10.12.2007 <br>
	 * @author Lavinia Dumitrescu
	 */
	private static class BookmarkPortletEntry implements PortletEntry<Bookmark> {
  	private final Bookmark mark;
  	
  	public BookmarkPortletEntry(Bookmark mark) {
  		this.mark = mark;
  	}
  	
  	public Long getKey() {
  		return mark.getKey();
  	}
  	
  	public Bookmark getValue() {
  		return mark;
  	}
  }	
}