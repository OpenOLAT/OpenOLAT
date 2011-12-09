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

package org.olat.bookmark;

import java.util.List;
import java.util.Locale;

import org.olat.ControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.PackageTranslator;
import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryStatus;
import org.olat.repository.RepositoryManager;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class ManageBookmarkController extends BasicController {

	private BmTableDataModel tdm;
	private BookmarkImpl chosenBm = null;
	private BookmarkManager bm = BookmarkManager.getInstance();
	private VelocityContainer myContent;

	private Panel bmarea;
	private String searchType; // can be set to a bookmark.olatrestype to
															// restrict the bookmark searches
	private TableController tableCtr;
	private AddAndEditBookmarkController abc;
	private DialogBoxController dc;
	private CloseableModalController cmc;

	/** constructor constant to search for all repository entry types * */
	public static final String SEARCH_TYPE_ALL = "all";

	/**
	 * Constructor for bookmark list and manage controller. The controller can be
	 * configured using the allowEdit flag in the constructor and restrict the
	 * search to specific repository entry types using the type attribute.
	 * 
	 * @param ureq The user request
	 * @param wControl The window controller
	 * @param allowEdit true: table allows edit and delete of bookmarks, false:
	 *          only launch possible
	 * @param type Type of repository entries to be displayed or SEARCH_TYPE_ALL
	 *          to display all bookmarks
	 */
	public ManageBookmarkController(UserRequest ureq, WindowControl wControl, boolean allowEdit, String type) {
		super(ureq, wControl);

		myContent = createVelocityContainer("bookmarks");
		bmarea = new Panel("bmarea");
		myContent.put("bmarea", bmarea);

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(false);
		tableConfig.setTableEmptyMessage(translate("bookmarks.nobookmarks"));
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.bm.title", 0, "choose", ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.bm.resource", 1, null, ureq.getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.bm.description", 2, null, ureq.getLocale()));
		listenTo(tableCtr);

		if (allowEdit) {
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor("edit", "table.header.edit", myContent.getTranslator().translate(
					"action.edit")));
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor("delete", "table.header.delete", myContent.getTranslator().translate(
					"action.delete")));
		}
		// Set default search type to search for all bookmarks
		searchType = type;

		populateBmTable(ureq.getIdentity(), ureq.getLocale());
		bmarea.setContent(tableCtr.getInitialComponent());
		
		putInitialPanel(myContent);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
	  // no events		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Controller source, Event event) {
		// if row has been cklicked
		if (source == tableCtr) {
			if (event.getCommand().equals(Table.COMMANDLINK_ROWACTION_CLICKED)) {
				TableEvent te = (TableEvent) event;
				String actionid = te.getActionId();
				int rowid = te.getRowId();
				this.chosenBm = (BookmarkImpl) tdm.getObject(rowid);
				if (actionid.equals("choose")) {
					// launch bookmark 
					BookmarkManager.getInstance().launchBookmark(chosenBm, ureq, getWindowControl());
					return;
				} else if (actionid.equals("edit")) {
					if (abc != null) abc.dispose();
					abc = new AddAndEditBookmarkController(ureq, getWindowControl(), chosenBm);					
					listenTo(abc);
					cmc = new CloseableModalController(getWindowControl(), "close", abc.getInitialComponent());
					cmc.insertHeaderCss();
					cmc.activate();
				} else if (actionid.equals("delete")) {
					dc = activateYesNoDialog(ureq, null, translate("bookmark.delete.willyou"), dc);
					return;
				}
			}
		} else if (source == abc){
				cmc.deactivate();
				chosenBm = null;
				if (event.getCommand().equals("done")) {
					// edit was done
					populateBmTable(ureq.getIdentity(), ureq.getLocale());
				}
		} else if (source == dc){
				if (DialogBoxUIFactory.isYesEvent(event)) {
					bm.deleteBookmark(chosenBm);
					showInfo("bookmark.delete.successfull");
					populateBmTable(ureq.getIdentity(), ureq.getLocale());
				}
				chosenBm = null;
			}
		}

	private void populateBmTable(Identity ident, Locale locale) {
		List l;
		if (searchType.equals(SEARCH_TYPE_ALL)) {
			l = bm.findBookmarksByIdentity(ident);
		} else {
			// in all other cases the sql query has a where clause that uses this type
			l = bm.findBookmarksByIdentity(ident, searchType);
		}
		tdm = new BmTableDataModel(l, locale);
		tableCtr.setTableDataModel(tdm);
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// disposed by BasicController		
	}

}

/**
 * <pre>
 *  
 *   Initial Date:  Jul 29, 2003
 *  
 *   @author jeger
 *   
 *   Comment:  
 *   The bookmark table data model.
 *   
 * </pre>
 */

class BmTableDataModel extends DefaultTableDataModel {

	private Locale locale;

	/**
	 * @param objects
	 * @param locale
	 */
	public BmTableDataModel(List objects, Locale locale) {
		super(objects);
		this.locale = locale;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public final Object getValueAt(int row, int col) {
		Bookmark bm = (BookmarkImpl) getObject(row);
		switch (col) {
			case 0:
				return getBookmarkTitle(bm);
			case 1:
				String resType = bm.getDisplayrestype();
				return (resType == null ? "n/a" : ControllerFactory.translateResourceableTypeName(resType, locale));
			case 2:
				String desc = bm.getDescription();
				return (desc == null ? "n/a" : desc);
			default:
				return "error";
		}
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 3;
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