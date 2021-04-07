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
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryModule;

/**
 * Description:
 * 
 * @author Sabina Jeger
 */
public class BookmarksController extends BasicController {

	private TableController tableCtr;
	private DialogBoxController dc;

	/** constructor constant to search for all repository entry types * */
	private final List<String> types = Collections.singletonList("RepositoryEntry");
	
	private final MarkManager markManager;
	private final RepositoryManager repositoryManager;

	/**
	 * Constructor for bookmark list and manage controller. The controller can be
	 * configured using the allowEdit flag in the constructor and restrict the
	 * search to specific repository entry types using the type attribute.
	 * 
	 * @param ureq The user request
	 * @param wControl The window controller
	 */
	public BookmarksController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		repositoryManager = RepositoryManager.getInstance();

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setDownloadOffered(false);
		tableConfig.setTableEmptyMessage(translate("bookmarks.nobookmarks"), null, "o_icon_bookmark_header");
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(), getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.bm.title", 0, "choose", getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.bm.resource", 1, null, getLocale()));
		DefaultColumnDescriptor descCol = new DefaultColumnDescriptor("table.bm.description", 2, null, getLocale());
		descCol.setEscapeHtml(EscapeMode.antisamy);
		tableCtr.addColumnDescriptor(descCol);
		tableCtr.addColumnDescriptor(new StaticColumnDescriptor("delete", "table.header.delete", translate("action.delete")));
		listenTo(tableCtr);

		populateBmTable();
		putInitialPanel(tableCtr.getInitialComponent());
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
				Bookmark bookmark = (Bookmark)tableCtr.getTableDataModel().getObject(rowid);
				if (actionid.equals("choose")) {
					String businessPath = bookmark.getBusinessPath();
					NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
				} else if (actionid.equals("delete")) {
					dc = activateYesNoDialog(ureq, null, translate("bookmark.delete.willyou"), dc);
					dc.setUserObject(bookmark);
				}
			}
		} else if (source == dc) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				Bookmark bookmark = (Bookmark)dc.getUserObject();
				markManager.removeMark(bookmark.getOLATResourceable(), getIdentity(), bookmark.getResSubPath());
				showInfo("bookmark.delete.successfull");
				populateBmTable();
			}
		}
	}

	private void populateBmTable() {
		List<Mark> marks = markManager.getMarks(getIdentity(), types);
		List<Bookmark> bookmarks = convertMarkToBookmark(marks);
		TableDataModel<Bookmark> tdm = new BmTableDataModel(bookmarks, getLocale());
		tableCtr.setTableDataModel(tdm);
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
			} else {
				markManager.removeMark(mark);
			}
		}
		return convertedList;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
		// disposed by BasicController		
	}
	
	private class BmTableDataModel extends DefaultTableDataModel<Bookmark> {
		private Locale locale;

		/**
		 * @param objects
		 * @param locale
		 */
		public BmTableDataModel(List<Bookmark> objects, Locale locale) {
			super(objects);
			this.locale = locale;
		}

		/**
		 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
		 */
		public final Object getValueAt(int row, int col) {
			Bookmark bm = getObject(row);
			switch (col) {
				case 0:
					return getBookmarkTitle(bm);
				case 1:
					String resType = bm.getDisplayrestype();
					return (resType == null ? "n/a" : NewControllerFactory.translateResourceableTypeName(resType, locale));
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
			if (bookmark.getEntryStatus().decommissioned()) {
				Translator pT = Util.createPackageTranslator(RepositoryModule.class, locale);
				title = "[" + pT.translate("title.prefix.closed") + "] ".concat(title);
			}
			return title;
		}
	}
}
