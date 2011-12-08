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

package org.olat.group.ui.main;

import java.util.Collections;
import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.group.BusinessGroup;

/**
 * @author gnaegi
 */
public class BusinessGroupTableModelWithType extends DefaultTableDataModel implements TableDataModel {
	private final int columnCount;
	private Translator trans;

	/**
	 * @param owned list of business groups
	 */
	public BusinessGroupTableModelWithType(List<BGTableItem> owned, Translator trans, int columnCount) {
		super(owned);
		this.trans = trans;
		//fxdiff VCRP-1,2: access control of resources
		this.columnCount = columnCount;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		return columnCount;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		BGTableItem wrapped = (BGTableItem)objects.get(row);
		BusinessGroup businessGroup = wrapped.getBusinessGroup();
		switch (col) {
			case 0:
				String name = businessGroup.getName();
				name = StringEscapeUtils.escapeHtml(name).toString();
				return name;
			case 1:
				String description = businessGroup.getDescription();
				description = FilterFactory.getHtmlTagsFilter().filter(description);
				description = Formatter.truncate(description, 256);
				return description;
			case 2:
				return trans.translate(businessGroup.getType());
			case 3:
				return wrapped.getAllowLeave();
			case 4:
				return wrapped.getAllowDelete();
			case 5:
				return wrapped.getResources();
			//fxdiff VCRP-1,2: access control of resources
			case 6:
				return new Boolean(wrapped.isAccessControl());
			case 7:
				if(wrapped.isMember()) return trans.translate("select");
				return trans.translate("table.access");
			case 8:
				return wrapped.getAccessTypes();
				
			default:
				return "ERROR";
		}
	}
	
	@Override
	//fxdiff VCRP-1,2: access control of resources
	public Object createCopyWithEmptyList() {
		return new BusinessGroupTableModelWithType(Collections.<BGTableItem>emptyList(), trans, columnCount);
	}

	/**
	 * @param owned
	 */
	public void setEntries(List<BGTableItem> owned) {
		this.objects = owned;
	}

	/**
	 * @param row
	 * @return the business group at the given row
	 */
	public BusinessGroup getBusinessGroupAt(int row) {
		BGTableItem wrapped = (BGTableItem)objects.get(row);
		return wrapped.getBusinessGroup();
	}
}