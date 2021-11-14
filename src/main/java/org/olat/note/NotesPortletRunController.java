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

package org.olat.note;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LayoutMain3ColsController;
import org.olat.core.commons.fullWebApp.popup.BaseFullWebappPopupLayoutFactory;
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
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.popup.PopupBrowserWindow;
import org.olat.core.gui.control.generic.portal.AbstractPortletRunController;
import org.olat.core.gui.control.generic.portal.PortletDefaultTableDataModel;
import org.olat.core.gui.control.generic.portal.PortletEntry;
import org.olat.core.gui.control.generic.portal.PortletToolSortingControllerImpl;
import org.olat.core.gui.control.generic.portal.SortingCriteria;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Description:<br>
 * Run view controller for the groups list portlet
 * <P>
 * Initial Date:  11.07.2005 <br>
 * @author gnaegi
 */
public class NotesPortletRunController extends AbstractPortletRunController<Note> implements GenericEventListener {
	
	private static final String CMD_LAUNCH = "cmd.launch";

	private TableController tableCtr;	
	private NoteSortingTableDataModel notesListModel;
	private VelocityContainer notesVC;
	private Identity cOwner;
	private Link showAllLink;
	private final OLATResourceable eventBusThisIdentityOres;
	
	@Autowired
	private NoteManager nm;
	
	/**
	 * Constructor
	 * @param ureq
	 * @param component
	 */
	public NotesPortletRunController(WindowControl wControl, UserRequest ureq, Translator trans,
			String portletName, int defaultMaxEntries) { 
		super(wControl, ureq, trans, portletName, defaultMaxEntries);		
		this.cOwner = ureq.getIdentity();
		
		sortingTermsList.add(SortingCriteria.ALPHABETICAL_SORTING);
		sortingTermsList.add(SortingCriteria.DATE_SORTING);
		
		this.notesVC = this.createVelocityContainer("notesPortlet");
		showAllLink = LinkFactory.createLink("notesPortlet.showAll", notesVC, this);
		showAllLink.setIconRightCSS("o_icon o_icon_start");
			
		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(trans.translate("notesPortlet.nonotes"), null, "o_icon_notes");
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
		DefaultColumnDescriptor cd0 = new DefaultColumnDescriptor("notesPortlet.bgname", 0, CMD_LAUNCH, trans.getLocale());
		cd0.setIsPopUpWindowAction(true, "height=550, width=750, location=no, menubar=no, resizable=yes, status=no, scrollbars=yes, toolbar=no");
		tableCtr.addColumnDescriptor(cd0);
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("notesPortlet.type", 1, null, trans.getLocale(), ColumnDescriptor.ALIGNMENT_RIGHT));

		this.sortingCriteria = getPersistentSortingConfiguration(ureq);
		reloadModel( sortingCriteria);
		this.notesVC.put("table", tableCtr.getInitialComponent());
		
		putInitialPanel(notesVC);
		
		eventBusThisIdentityOres = OresHelper.createOLATResourceableInstance(Identity.class, getIdentity().getKey());    
		CoordinatorManager.getInstance().getCoordinator().getEventBus().registerFor(this, ureq.getIdentity(), eventBusThisIdentityOres);
	}
	
	/**
	 * 
	 * @param ureq
	 * @return
	 */
	private List<PortletEntry<Note>> getAllPortletEntries() {
		List<Note> noteList = nm.listUserNotes(cOwner);
		return convertNoteToPortletEntryList(noteList);		
	}
	
	/**
	 * 
	 * @param items
	 * @return
	 */
	private List<PortletEntry<Note>> convertNoteToPortletEntryList(List<Note> items) {
		List<PortletEntry<Note>> convertedList = new ArrayList<>();
		Iterator<Note> listIterator = items.iterator();
		while(listIterator.hasNext()) {
			convertedList.add(new NotePortletEntry(listIterator.next()));
		}
		return convertedList;
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.generic.portal.AbstractPortletRunController#reloadModel(org.olat.core.gui.UserRequest, org.olat.core.gui.control.generic.portal.SortingCriteria)
	 */
	protected void reloadModel(SortingCriteria sortCriteria) {
		if (sortCriteria.getSortingType() == SortingCriteria.AUTO_SORTING) {
			List<Note> noteList = nm.listUserNotes(cOwner);
			noteList = getSortedList(noteList, sortCriteria );
			List<PortletEntry<Note>> entries = convertNoteToPortletEntryList(noteList);
			notesListModel = new NoteSortingTableDataModel(entries, getLocale());
			tableCtr.setTableDataModel(notesListModel);
		} else {
			reloadModel(this.getPersistentManuallySortedItems());
		}
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.generic.portal.AbstractPortletRunController#reloadModel(org.olat.core.gui.UserRequest, java.util.List)
	 */
	protected void reloadModel(List<PortletEntry<Note>> sortedItems) {
		notesListModel = new NoteSortingTableDataModel(sortedItems, getLocale());
		tableCtr.setTableDataModel(notesListModel);
	}
	
	/**
	 * Listen to NoteEvents for this identity. 
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {		
		if(event instanceof NoteEvent) {			
			if(((NoteEvent)event).getIdentityKey().equals(getIdentity().getKey())) {
			  reloadModel(sortingCriteria);						  
			}
		}		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest, org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
			if (source == showAllLink) {
				// fxdiff: activate homes tab in top navigation and activate correct menu item
				String resourceUrl = "[HomeSite:" + ureq.getIdentity().getKey() + "][notelist:0]";
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
					final NotePortletEntry pe = (NotePortletEntry) notesListModel.getObject(rowid);
					final Note note = pe.getValue();
					// will not be disposed on course run dispose, popus up as new browserwindow
					ControllerCreator ctrlCreator = new ControllerCreator() {
						public Controller createController(UserRequest lureq, WindowControl lwControl) {
							Controller nc = new NoteController(lureq, lwControl, note);
							// use on column layout
							LayoutMain3ColsController layoutCtr = new LayoutMain3ColsController(lureq, lwControl, nc);
							layoutCtr.addDisposableChildController(nc); // dispose content on layout dispose
							return layoutCtr;
						}					
					};
					//wrap the content controller into a full header layout
					ControllerCreator layoutCtrlr = BaseFullWebappPopupLayoutFactory.createAuthMinimalPopupLayout(ureq, ctrlCreator);
					//open in new browser window
					PopupBrowserWindow pbw = getWindowControl().getWindowBackOffice().getWindowManager().createNewPopupBrowserWindowFor(ureq, layoutCtrlr);
					pbw.open(ureq);
					//
				}
			}
		}
	}

	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus().deregisterFor(this, eventBusThisIdentityOres);
		super.doDispose();
		portletToolsController = null;
        super.doDispose();
	}
	
	/**
	 * Retrieves the persistent sortingCriteria and the persistent manually sorted, if any, 
	 * creates the table model for the manual sorting, and instantiates the PortletToolSortingControllerImpl.
	 * @param ureq
	 * @param wControl
	 * @return a PortletToolSortingControllerImpl instance.
	 */
	protected PortletToolSortingControllerImpl<Note> createSortingTool(UserRequest ureq, WindowControl wControl) {
		if(portletToolsController==null) {			
			
			List<PortletEntry<Note>> entries = getAllPortletEntries();
			PortletDefaultTableDataModel<Note> tableDataModel = new NoteManualSortingTableDataModel(entries);
			List<PortletEntry<Note>> sortedItems = getPersistentManuallySortedItems(); 
			
			portletToolsController = new PortletToolSortingControllerImpl<>(ureq, wControl, getTranslator(), sortingCriteria, tableDataModel, sortedItems);
			portletToolsController.setConfigManualSorting(true);
			portletToolsController.setConfigAutoSorting(true);
			portletToolsController.addControllerListener(this);
		}		
		return portletToolsController;
	}
	
	/**
	 * 
	 * @param ureq
	 * @return
	 */
	private List<PortletEntry<Note>> getPersistentManuallySortedItems() {
		List<PortletEntry<Note>> entries = getAllPortletEntries();
		return getPersistentManuallySortedItems(entries);		
	}
	
	/**
	 * 
	 * @see org.olat.core.gui.control.generic.portal.AbstractPortletRunController#getComparator(org.olat.core.gui.control.generic.portal.SortingCriteria)
	 */
	protected Comparator<Note> getComparator(final SortingCriteria criteria) {
		return new Comparator<Note>(){			
			public int compare(final Note note1, final Note note2) {	
				int comparisonResult = 0;
			  if(criteria.getSortingTerm()==SortingCriteria.ALPHABETICAL_SORTING) {			  	
			  	comparisonResult = collator.compare(StringHelper.escapeHtml(note1.getNoteTitle()).toString(), StringHelper.escapeHtml(note2.getNoteTitle()).toString());			  		  	
			  } else if(criteria.getSortingTerm()==SortingCriteria.DATE_SORTING) {
			  	comparisonResult = note1.getLastModified().compareTo(note2.getLastModified());
			  } 
			  if(!criteria.isAscending()) {
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
	 * Initial Date:  18.12.2007 <br>
	 * @author Lavinia Dumitrescu
	 */
	private class NoteSortingTableDataModel extends PortletDefaultTableDataModel<Note> {
		private Locale locale;
		
		public NoteSortingTableDataModel(List<PortletEntry<Note>> objects, Locale locale) {
			super(objects, 2);
			this.locale = locale;
		}

		@Override
		public final Object getValueAt(int row, int col) {				
			Note note = getObject(row).getValue();
			switch (col) {
				case 0:					
					return note.getNoteTitle();
				case 1:								
					String resType = note.getResourceTypeName();
					return (resType == null ? "n/a" : NewControllerFactory.translateResourceableTypeName(resType, locale));
				default:
					return "error";
			}
		}
	}
	
	/**
	 * 
	 * Different from the above model only in the second column value.
	 * 
	 * <P>
	 * Initial Date:  18.12.2007 <br>
	 * @author Lavinia Dumitrescu
	 */
	private class NoteManualSortingTableDataModel extends PortletDefaultTableDataModel<Note>  {				
		/**
		 * @param objects
		 * @param locale
		 */
		public NoteManualSortingTableDataModel(List<PortletEntry<Note>> objects) {
			super(objects, 2);			
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
		 */
		public final Object getValueAt(int row, int col) {				
			Note note = getObject(row).getValue();
			switch (col) {
				case 0:					
					return note.getNoteTitle();
				case 1:								
					Date lastUpdate = note.getLastModified();
					return lastUpdate;
				default:
					return "error";
			}
		}
	}
	
	/**
	 * 
	 * PortletEntry impl for Note values.
	 * 
	 * <P>
	 * Initial Date:  10.12.2007 <br>
	 * @author Lavinia Dumitrescu
	 */
	class NotePortletEntry implements PortletEntry<Note> {
  	private Note value;
  	private Long key;
  	
  	public NotePortletEntry(Note note) {
  		value = note;
  		key = note.getKey();
  	}
  	
  	public Long getKey() {
  		return key;
  	}
  	
  	public Note getValue() {
  		return value;
  	}
  }

}
