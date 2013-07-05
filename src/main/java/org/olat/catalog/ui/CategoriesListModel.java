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
package org.olat.catalog.ui;

import java.util.List;
import java.util.Locale;

import org.olat.catalog.CatalogEntry;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.StaticColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * TODO: gnaegi Class Description for CategoriesListModel
 * 
 * <P>
 * Initial Date: 30.05.2008 <br>
 * 
 * @author gnaegi
 */
public class CategoriesListModel extends DefaultTableDataModel<CatalogEntry> {
	private int cols = 1;
	public static final String ACTION_GOTO = "goto";
	public static final String ACTION_DELETE = "delete";

	/**
	 * Constructor
	 * 
	 * @param catalogEntries
	 * @param locale
	 */
	public CategoriesListModel(List<CatalogEntry> catalogEntries, Locale locale) {
		super(catalogEntries);
		setLocale(locale);
	}

	/**
	 * Add the column descriptors for this table
	 * 
	 * @param tableCtr
	 * @param showRemoveAction true: remove link will be showed; false: remove
	 *          link is not showed
	 * @param trans Translator for translating the delete action
	 */
	public void addColumnDescriptors(TableController tableCtr, boolean showRemoveAction, Translator trans) {
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("categorieslist.header.category", 0, ACTION_GOTO, getLocale()));
		if (showRemoveAction) {
			tableCtr.addColumnDescriptor(new StaticColumnDescriptor(ACTION_DELETE, "categorieslist.header.action", trans.translate("delete")));
			cols = 2;
		}
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getColumnCount()
	 */
	@Override
	public int getColumnCount() {
		return cols;
	}

	/**
	 * @see org.olat.core.gui.components.table.DefaultTableDataModel#getValueAt(int,
	 *      int)
	 */
	@Override
	public Object getValueAt(int row, int col) {
		CatalogEntry entry = getObject(row);
		switch (col) {
			case 0:
				// calculate cat entry path: travel up to the root node
				String path = "";
				CatalogEntry tempEntry = entry;
				while (tempEntry != null) {
					path = "/" + tempEntry.getName() + path;
					tempEntry = tempEntry.getParent();
				}
				return path;

			default:
				return "error";
		}
	}
}
