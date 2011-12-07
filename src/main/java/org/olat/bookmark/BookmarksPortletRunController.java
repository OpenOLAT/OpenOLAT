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
* <p>
*/ 

package org.olat.bookmark;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.ControllerFactory;
import org.olat.NewControllerFactory;
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
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryManager;

/**
 * Description:<br>
 * Run view controller for the groups list portlet
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class BookmarksPortletRunController extends AbstractPortletRunController implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";
	
	private TableController tableCtr;
	private BookmarkPortletTableDataModel bookmarkListModel;
	private VelocityContainer bookmarksVC;		
	private Link showAllLink;
	private final OLATResourceable eventBusAllIdentitiesOres;
	private final OLATResourceable eventBusThisIdentityOres;
	private final Locale locale;
			
	/**
	 * Constructor
	 * @param ureq
	 * @param component
	 */
	public BookmarksPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans, String portletName) { 		
		super(wControl, ureq, trans, portletName);
		this.locale = ureq.getLocale();
		this.sortingTermsList.add(SortingCriteria.TYPE_SORTING);
		this.sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
		this.sortingTermsList.add(SortingCriteria.DATE_SORTING);
		
		this.bookmarksVC = this.createVelocityContainer("bookmarksPortlet");
		showAllLink = LinkFactory.createLink("bookmarksPortlet.showAll", bookmarksVC, this);
				
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("bookmarksPortlet.nobookmarks"));
		tableConfig.setDisplayTableHeader(false);
		tableConfig.setCustomCssClass("b_portlet_table");
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
		
		this.sortingCriteria = getPersistentSortingConfiguration(ureq);
		reloadModel(sortingCriteria);

		this.bookmarksVC.put("table", tableCtr.getInitialComponent());						
		putInitialPanel(bookmarksVC);
		
		//register for events targeted at this Identity
		eventBusThisIdentityOres = OresHelper.createOLATResourceableInstance(Identity.class, identity.getKey());
    //TODO: LD: use this:	//ureq.getUserSession().getSingleUserEventCenter().registerFor(this, ureq.getIdentity(), eventBusOres);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), eventBusThisIdentityOres);
		
		//register for events targeted at all Identities (e.g. delete bookmark for a course if a course is deleted)
		eventBusAllIdentitiesOres = OresHelper.createOLATResourceableType(Identity.class);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(),eventBusAllIdentitiesOres);
	}
	
	/**
	 * Gets all bookmarks for this identity and converts the list into an PortletEntry list.
	 * @param ureq
	 * @return
	 */
	private List<PortletEntry> getAllPortletEntries() {
		BookmarkManager mb = BookmarkManager.getInstance();
		List bookmarkList = mb.findBookmarksByIdentity(identity);
		return convertBookmarkToPortletEntryList(bookmarkList);
	}
	
	/**
	 * Converts list.
	 * @param items
	 * @return
	 */
	private List<PortletEntry> convertBookmarkToPortletEntryList(List<Bookmark> items) {
		List<PortletEntry> convertedList = new ArrayList<PortletEntry>();
		Iterator<Bookmark> listIterator = items.iterator();
		while(listIterator.hasNext()) {
			convertedList.add(new BookmarkPortletEntry(listIterator.next()));
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
			BookmarkManager mb = BookmarkManager.getInstance();
			List bookmarkList = mb.findBookmarksByIdentity(identity);
			
			bookmarkList = getSortedList(bookmarkList, sortingCriteria );
			
			List<PortletEntry> entries = convertBookmarkToPortletEntryList(bookmarkList);
			bookmarkListModel = new BookmarkPortletTableDataModel(entries, this.locale);
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
	protected void reloadModel(List<PortletEntry> sortedItems) {					
		bookmarkListModel = new BookmarkPortletTableDataModel(sortedItems, this.locale);
		tableCtr.setTableDataModel(bookmarkListModel);
	}
	
	

	public void event(Event event) {		
		if(event instanceof BookmarkEvent) {			
			if(((BookmarkEvent)event).getUsername().equals(identity.getName()) || ((BookmarkEvent)event).isAllUsersEvent()) {
			  reloadModel(sortingCriteria);			  
			}
		}		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {		
		if (source == showAllLink){
			// activate homes tab in top navigation and active bookmarks menu item
			String resourceUrl = "[HomeSite:" + ureq.getIdentity().getKey() + "][bookmarks:0]";
			BusinessControl bc = BusinessControlFactory.getInstance().createFromString(resourceUrl);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
			NewControllerFactory.getInstance().launch(ureq, bwControl);
		} 
	}

	/**
	 * @see org.olat.core.gui.control.ControllerEventListener#dispatchEvent(org.olat.core.gui.UserRequest, org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		super.event(ureq, source, event);
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				if (actionid.equals(CMD_LAUNCH)) {
					int rowid = te.getRowId();
					Bookmark bookmark = bookmarkListModel.getBookmarkAt(rowid);
					BookmarkManager.getInstance().launchBookmark(bookmark, ureq, getWindowControl());
				}
			}
		} 
	}

  
  /**
   * Retrieve the persistent manually sorted items for the current portlet.
   * @param ureq
   * @return
   */
  private List getPersistentManuallySortedItems() {
  	List<PortletEntry> entries = getAllPortletEntries();
		return this.getPersistentManuallySortedItems(entries);		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, eventBusThisIdentityOres);
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, eventBusAllIdentitiesOres);
		super.doDispose();
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl istance.
	 */
	protected PortletToolSortingControllerImpl createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {			
			List<PortletEntry> entries = getAllPortletEntries();
			PortletDefaultTableDataModel tableDataModel = new BookmarkManualSortingTableDataModel(entries, ureq.getLocale());
			
			List sortedItems = getPersistentManuallySortedItems(); 
			
			portletToolsController = new PortletToolSortingControllerImpl(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
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
	protected Comparator getComparator(final SortingCriteria sortingCriteria) {
		return new Comparator(){			
			public int compare(final Object o1, final Object o2) {
				BookmarkImpl bookmark1 = (BookmarkImpl)o1;
				BookmarkImpl bookmark2 = (BookmarkImpl)o2;		
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
	class BookmarkPortletTableDataModel extends PortletDefaultTableDataModel {
		private Locale locale;

		public BookmarkPortletTableDataModel(List<PortletEntry> objects, Locale locale) {
			super(objects, 2);
			this.locale = locale;
		}
		
		public Object getValueAt(int row, int col) {
			PortletEntry entry = getObject(row);
			Bookmark bookmark = (Bookmark)  entry.getValue();
			switch (col) {
			case 0:
				String name = getBookmarkTitle(bookmark);
				name = StringEscapeUtils.escapeHtml(name).toString();
				return name;
			case 1:
				String resType = bookmark.getDisplayrestype();
				return ControllerFactory.translateResourceableTypeName(resType, getTranslator().getLocale());
			default:
				return "ERROR";
			}
    }
		
		public Bookmark getBookmarkAt(int row) {
			return  (Bookmark)  getObject(row).getValue();
		}
		
		/**
		 * Get displayname of a bookmark entry. If bookmark entry a RepositoryEntry 
		 * and is this RepositoryEntry closed then add a prefix to the title.
		 */
		private String getBookmarkTitle(Bookmark bookmark) {
			String title = bookmark.getTitle();
			RepositoryEntry repositoryEntry = RepositoryManager.getInstance().lookupRepositoryEntry(bookmark.getOlatreskey());
			if (repositoryEntry != null && RepositoryManager.getInstance().createRepositoryEntryStatus(repositoryEntry.getStatusCode()).isClosed()) {
				PackageTranslator pT = new PackageTranslator(RepositoryEntryStatus.class.getPackage().getName(), locale);
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
	class BookmarkManualSortingTableDataModel extends PortletDefaultTableDataModel {
		private Locale locale;

		/**
		 * @param objects
		 * @param locale
		 */
		public BookmarkManualSortingTableDataModel(List<PortletEntry> objects, Locale locale) {
			super(objects, 4);
			this.locale = locale;
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
		 */
		public final Object getValueAt(int row, int col) {
			PortletEntry entry = getObject(row);
			Bookmark bm = (BookmarkImpl) entry.getValue();
			switch (col) {
				case 0:
					return bm.getTitle();
				case 1:
					String desc = bm.getDescription();
					return (desc == null ? "n/a" : desc);
				case 2:
					String resType = bm.getDisplayrestype();
					return (resType == null ? "n/a" : ControllerFactory.translateResourceableTypeName(resType, locale));
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
	class BookmarkPortletEntry implements PortletEntry {
  	private Bookmark value;
  	private Long key;
  	
  	public BookmarkPortletEntry(Bookmark bookmark) {
  		value = bookmark;
  		key = bookmark.getKey();
  	}
  	
  	public Long getKey() {
  		return key;
  	}
  	
  	public Bookmark getValue() {
  		return value;
  	}
  }
		
}
