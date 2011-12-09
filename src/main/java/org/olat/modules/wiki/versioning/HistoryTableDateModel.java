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

package org.olat.modules.wiki.versioning;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.wiki.WikiMainController;
import org.olat.modules.wiki.WikiPage;

/**
 * Description:<br>
 * TODO: guido Class Description for HistoryTableDateModel
 * <P>
 * Initial Date: May 30, 2006 <br>
 * 
 * @author guido
 */
public class HistoryTableDateModel extends DefaultTableDataModel implements TableDataModel {

	private Translator trans;

	public HistoryTableDateModel(List entries, Translator trans) {
		super(entries);
		this.trans = trans;
	}

	private static final int COLUMN_COUNT = 3;

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		WikiPage page = (WikiPage) objects.get(row);
		switch (col) {
			case 0:
				return String.valueOf(page.getVersion());
			case 1:
				return new Date(page.getModificationTime());
			case 2:
				return String.valueOf(page.getViewCount());
			case 3:
				long key = page.getModifyAuthor();
				return key != 0 ? BaseSecurityManager.getInstance().loadIdentityByKey(Long.valueOf(page.getModifyAuthor())).getName() :"n/a";
				//TODO:gs:a loadIdenitiesByKeys(List keys) would be much more performant as each lookup get one database lookup
			case 4:
				int v = page.getVersion();
				if(v == 0) return new String("");
				return String.valueOf(v-1) + " " +trans.translate("to") + " " + String.valueOf(v);
			case 5:
				return page.getUpdateComment();
			default:
				return "ERROR";
		}
	}

	public void addColumnDescriptors(TableController tableCtr) {
		Locale loc = trans.getLocale();
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.version",0,WikiMainController.ACTION_SHOW,loc));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.date",1,null,loc));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.viewcount",2,null,loc));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.author",3,null,loc));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.compare",4,WikiMainController.ACTION_COMPARE,loc));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.comment",5,null,loc));
		
	}

}
