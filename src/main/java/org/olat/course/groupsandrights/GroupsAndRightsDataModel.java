/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.course.groupsandrights;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.translator.Translator;
import org.olat.group.right.BGRightsRole;

/**
 * 
 * Initial date: 23 janv. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
 public class GroupsAndRightsDataModel extends DefaultFlexiTableDataModel<BGRightsRow> {
	
	private final Translator translator;

	public GroupsAndRightsDataModel(FlexiTableColumnModel columnModel, Translator translator) {
		super(columnModel);
		this.translator = translator;
	}

	@Override
	public Object getValueAt(int row, int col) {
		BGRightsRow rightsRow = getObject(row);
		if(col == 0) {
			return rightsRow;
		} else if (col == 1) {
			BGRightsRole role = rightsRow.getRole();
			switch(role) {
				case tutor: return  rightsRow.getResourceType() == BGRightsResourceType.businessGroup
						? translator.translate("tutor") : translator.translate("repo.tutor");
				case participant: return  rightsRow.getResourceType() == BGRightsResourceType.businessGroup
						? translator.translate("participant") : translator.translate("repo.participant");
			}
			return "";
		}
		
		//rights
		int rightPos = col - 2;
		return rightsRow.getRightsEl().get(rightPos).getSelection();
	}
}
