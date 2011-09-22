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

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.ControllerFactory;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * TODO: gnaegi Class Description for BookmarkListMiniModel
 * 
 * <P>
 * Initial Date:  03.08.2005 <br>
 * @author gnaegi
 */
public class BookmarkListMiniModel extends DefaultTableDataModel implements TableDataModel {
	private Translator trans;
	
	/**
	 * @param list of bookmarks
	 * @param trans
	 */
	public BookmarkListMiniModel(List bookmarks, Translator trans) {
		super(bookmarks);
		this.trans = trans;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return 2;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		Bookmark bookmark = (Bookmark)  objects.get(row);
		switch (col) {
		case 0:
			String name = bookmark.getTitle();
			name = StringEscapeUtils.escapeHtml(name).toString();
			return name;
		case 1:
			String resType = bookmark.getDisplayrestype();
			return ControllerFactory.translateResourceableTypeName(resType, trans.getLocale());
		default:
			return "ERROR";
		}
	}

	/**
	 * @param row
	 * @return the bookmark at the given row
	 */
	public Bookmark getBookmarkAt(int row) {
		return  (Bookmark)  objects.get(row);
	}

}