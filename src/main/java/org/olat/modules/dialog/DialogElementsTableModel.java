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

package org.olat.modules.dialog;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.components.table.BaseTableDataModelWithoutFilter;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.Table;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.course.nodes.dialog.DialogConfigForm;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.fo.ForumCallback;

/**
 * Description:<br>
 * Table model for run mode of course node "file dialog"
 * <P>
 * Initial Date: 08.11.2005 <br>
 * 
 * @author guido
 */
public class DialogElementsTableModel extends BaseTableDataModelWithoutFilter<DialogElement> implements TableDataModel<DialogElement> {

	private static final int COLUMN_COUNT = 9;
	private List<DialogElement> entries = new ArrayList<DialogElement>();
	protected Translator translator;
	private ForumCallback callback;
	private ModuleConfiguration config;

	/**
	 * @param translator
	 */
	public DialogElementsTableModel(Translator translator, ForumCallback callback, ModuleConfiguration config) {
		this.translator = translator;
		this.callback = callback;
		this.config = config;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getRowCount()
	 */
	public int getRowCount() {
		return entries.size();
	}

	/**
	 * @param num
	 * @return
	 */
	public DialogElement getEntryAt(int num) {
		return entries.get(num);
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		DialogElement entry = getEntryAt(row);
		switch (col) {
			case 0:
				if (entry.getFilename().length() > 30) { return entry.getFilename().substring(0, 30) + "..."; }
				return entry.getFilename();
			case 1:
				return entry.getAuthor();
			case 2:
				return entry.getFileSize();
			case 3:
				return entry.getDate();
			case 4:
				return entry.getNewMessages();
			case 5:
				return entry.getMessagesCount();
			default:
				return "ERROR";
		}
	}

	public void addColumnDescriptors(TableController tableCtr) {
		Locale loc = translator.getLocale();
		if (callback != null) {
			tableCtr.addColumnDescriptor(new FileDownloadColumnDescriptor("table.header.filename", 0, loc));
		} else {
			tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.filename", 0, null, loc));
		}
		if (callback != null) {
			StaticColumnDescriptor statColdesc = new StaticColumnDescriptor(DialogElementsController.ACTION_START_FORUM, "table.header.forum",
					translator.translate("dialog.start"));
			// if configured open forum as popup
			String integration = (String) config.get(DialogConfigForm.DIALOG_CONFIG_INTEGRATION);
			if (integration.equals(DialogConfigForm.CONFIG_INTEGRATION_VALUE_POPUP)) {
				statColdesc.setIsPopUpWindowAction(true, DefaultColumnDescriptor.DEFAULT_POPUP_ATTRIBUTES);
			}
			tableCtr.addColumnDescriptor(statColdesc);
		}
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author", 1, null, loc));
		tableCtr.addColumnDescriptor(false, new DefaultColumnDescriptor("table.header.size", 2, null, loc));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.date", 3, null, loc));
		if (callback != null) tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.newmessages", 4, null, loc));
		if (callback != null) tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.messages", 5, null, loc));
		// callback is null in course editor environement where we dont need
		// security calls
		if (callback != null && callback.mayDeleteMessageAsModerator()) tableCtr.addColumnDescriptor(new StaticColumnDescriptor(
				DialogElementsController.ACTION_DELETE_ELEMENT, "table.header.action", translator.translate("delete")));
	}

	/**
	 * Set entries to be represented by this table model.
	 * 
	 * @param entries
	 */
	public void setEntries(List<DialogElement> entries) {
		this.entries = entries;
	}
	
	private static class FileDownloadColumnDescriptor extends DefaultColumnDescriptor {
		
		public FileDownloadColumnDescriptor(String headerKey, int dataColumn, Locale locale) {
			super(headerKey, dataColumn, null, locale);
		}

		@Override
		public void renderValue(StringOutput sb, int row, Renderer renderer) {
			if(renderer == null) {//download
				int sortedRow = table.getSortedRow(row);
				Object entry = table.getTableDataModel().getValueAt(sortedRow, getDataColumn());
				if(entry != null) {
					sb.append(entry.toString());
				}
			} else {
				URLBuilder ubu = renderer.getUrlBuilder();
				ubu = ubu.createCopyFor(getTable());
				
				int sortedRow = table.getSortedRow(row);
				Object entry = table.getTableDataModel().getValueAt(sortedRow, getDataColumn());
	
				StringOutput link = new StringOutput();
				ubu.buildURI(link, new String[] { Table.COMMANDLINK_ROWACTION_CLICKED, Table.COMMANDLINK_ROWACTION_ID }, new String[] { String.valueOf(sortedRow), DialogElementsController.ACTION_SHOW_FILE }); // url
				sb.append("<a href=\"javascript:o_openPopUp('").append(link).append(entry.toString()).append("','fileview','600','700','no')\">")
				  .append(entry.toString())
				  .append("</a>");
			}
		}
	}
}
