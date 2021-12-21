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

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.modules.wiki.WikiMainController;
import org.olat.modules.wiki.WikiPage;
import org.olat.user.UserManager;

/**
 * Initial Date: May 30, 2006 <br>
 * 
 * @author guido
 */
public class HistoryTableDateModel extends DefaultTableDataModel<WikiPage> implements TableDataModel<WikiPage> {

	private final Translator trans;
	private final UserManager userManager;

	public HistoryTableDateModel(List<WikiPage> entries, Translator trans) {
		super(entries);
		this.trans = trans;
		userManager = CoreSpringFactory.getImpl(UserManager.class);
	}

	private static final int COLUMN_COUNT = 3;

	@Override
	public int getColumnCount() {
		return COLUMN_COUNT;
	}

	@Override
	public Object getValueAt(int row, int col) {
		WikiPage page = getObject(row);
		switch (col) {
			case 0:
				return String.valueOf(page.getVersion());
			case 1:
				return new Date(page.getModificationTime());
			case 2:
				return String.valueOf(page.getViewCount());
			case 3:
				long key = page.getModifyAuthor();
				return userManager.getUserDisplayName(Long.valueOf(key));
			case 4:
				int v = page.getVersion();
				if(v == 0) return "";
				return String.valueOf(v-1) + " " +trans.translate("to") + " " + v;
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
