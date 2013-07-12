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
package org.olat.course.member;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.group.BusinessGroupManagedFlag;
import org.olat.group.ui.main.BGTableItem;
import org.olat.group.ui.main.BusinessGroupTableModelWithType;

/**
 * The remove link appear only if the group is not managed
 * 
 * Initial date: 10.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RemoveActionColumnDescriptor extends DefaultColumnDescriptor {

	private final Translator translator;
	
	public RemoveActionColumnDescriptor(final String headerKey, final int dataColumn, final Translator translator) {
		super(headerKey, dataColumn, null, translator.getLocale());
		this.translator = translator;
	}

	@Override
	public String getAction(int row) {
		int sortedRow = table.getSortedRow(row);
		
		Object memberObj = table.getTableDataModel().getValueAt(sortedRow, BusinessGroupTableModelWithType.Cols.wrapper.ordinal());
		if(memberObj instanceof BGTableItem) {
			//owner, participant, or in waiting list can leave
			BGTableItem item = (BGTableItem)memberObj;
			boolean managed = BusinessGroupManagedFlag.isManaged(item.getManagedFlags(), BusinessGroupManagedFlag.resources);
			if(managed) {
				return null;
			}
		}
		return CourseBusinessGroupListController.TABLE_ACTION_UNLINK;
	}

	@Override
	public void renderValue(StringOutput sb, int row, Renderer renderer) {
		int sortedRow = table.getSortedRow(row);
		Object memberObj = table.getTableDataModel().getValueAt(sortedRow, BusinessGroupTableModelWithType.Cols.wrapper.ordinal());
		
		boolean managed = false;
		if(memberObj instanceof BGTableItem) {
			//owner, participant, or in waiting list can leave
			BGTableItem item = (BGTableItem)memberObj;
			managed = BusinessGroupManagedFlag.isManaged(item.getManagedFlags(), BusinessGroupManagedFlag.resources);
		}
		
		if(!managed) {
			sb.append(translator.translate(getHeaderKey()));
		}
	}
}
