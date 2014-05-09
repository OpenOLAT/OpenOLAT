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

import java.util.List;
import java.util.Locale;

import org.olat.NewControllerFactory;
import org.olat.core.gui.components.table.DefaultTableDataModel;

/**
 *  Initial Date:  Dec 10, 2004
 * 
 *  @author Alexander Schneider
 */

class NoteListTableDataModel extends DefaultTableDataModel<Note> {

	private final Locale locale;
	
	/**
	 * @param objects
	 * @param locale
	 */
	public NoteListTableDataModel(List<Note> objects, Locale locale) {
		super(objects);
		this.locale = locale;
	}

	@Override
	public final Object getValueAt(int row, int col) {
		Note n = getObject(row);
		switch (col) {
			case 0 :
			  return n.getNoteTitle();
			case 1 :
				String resType = n.getResourceTypeName();
				return (resType == null ? "n/a" : NewControllerFactory.translateResourceableTypeName(resType, locale));
			default :
				return "error";
		}
	}

	@Override
	public int getColumnCount() {
		return 2;
	}
}
