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
package org.olat.admin.user.groups;

import java.util.Locale;

import org.olat.admin.user.groups.BusinessGroupTableModelWithType.Cols;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.group.BusinessGroupShort;
import org.olat.group.ui.main.BusinessGroupNameCellRenderer;

/**
 * Show and sort business group name decorated
 * 
 * Initial date: 13.08.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class BusinessGroupNameColumnDescriptor extends CustomRenderColumnDescriptor {
	
	public BusinessGroupNameColumnDescriptor(String action, Locale locale) {
		super(Cols.name.i18n(), Cols.name.ordinal(), action, locale, ColumnDescriptor.ALIGNMENT_LEFT,
				new BusinessGroupNameCellRenderer());
	}

	@Override
	public int compareTo(int rowa, int rowb) {
		Object a = table.getTableDataModel().getValueAt(rowa,dataColumn);
		Object b = table.getTableDataModel().getValueAt(rowb,dataColumn);
		
		if(a instanceof BusinessGroupShort && b instanceof BusinessGroupShort) {
			BusinessGroupShort g1 = (BusinessGroupShort)a;
			BusinessGroupShort g2 = (BusinessGroupShort)b;
			return super.compareString(g1.getName(), g2.getName());
		}
		return super.compareTo(rowa, rowb);
	}
}