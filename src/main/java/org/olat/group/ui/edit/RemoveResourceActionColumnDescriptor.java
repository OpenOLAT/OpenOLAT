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
package org.olat.group.ui.edit;

import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.translator.Translator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryManagedFlag;
import org.olat.repository.ui.RepositoryTableModel;

/**
 * The remove link appear only if the repository is not managed (flag groups)
 * 
 * Initial date: 12.07.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class RemoveResourceActionColumnDescriptor extends DefaultColumnDescriptor {

	private final Translator translator;
	
	public RemoveResourceActionColumnDescriptor(final String headerKey, final int dataColumn, final Translator translator) {
		super(headerKey, dataColumn, null, translator.getLocale());
		this.translator = translator;
	}

	@Override
	public String getAction(int row) {
		int sortedRow = table.getSortedRow(row);
		
		Object memberObj = table.getTableDataModel().getValueAt(sortedRow, getDataColumn());
		if(memberObj instanceof RepositoryEntry) {
			//owner, participant, or in waiting list can leave
			RepositoryEntry item = (RepositoryEntry)memberObj;
			boolean managed = RepositoryEntryManagedFlag.isManaged(item.getManagedFlags(), RepositoryEntryManagedFlag.groups);
			if(managed) {
				return null;
			}
		}
		return RepositoryTableModel.TABLE_ACTION_SELECT_LINK;
	}

	@Override
	public void renderValue(StringOutput sb, int row, Renderer renderer) {
		int sortedRow = table.getSortedRow(row);
		Object memberObj = table.getTableDataModel().getValueAt(sortedRow, getDataColumn());
		
		boolean managed = false;
		if(memberObj instanceof RepositoryEntry) {
			//owner, participant, or in waiting list can leave
			RepositoryEntry item = (RepositoryEntry)memberObj;
			managed = RepositoryEntryManagedFlag.isManaged(item.getManagedFlags(), RepositoryEntryManagedFlag.groups);
		}
		
		if(!managed) {
			sb.append(translator.translate(getHeaderKey()));
		}
	}
}
