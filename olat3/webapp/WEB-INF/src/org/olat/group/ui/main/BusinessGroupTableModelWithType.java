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
	private static final int COLUMN_COUNT = 5;
	private Translator trans;

	/**
	 * @param owned list of business groups
	 */
	public BusinessGroupTableModelWithType(List owned, Translator trans) {
		super(owned);
		this.trans = trans;
	}

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
		Object[] wrapped = (Object[]) objects.get(row);
		;
		BusinessGroup businessGroup = (BusinessGroup) wrapped[0];
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
				return wrapped[1];
			case 4:
				return wrapped[2];
			default:
				return "ERROR";
		}
	}

	/**
	 * @param owned
	 */
	public void setEntries(List owned) {
		this.objects = owned;
	}

	/**
	 * @param row
	 * @return the business group at the given row
	 */
	public BusinessGroup getBusinessGroupAt(int row) {
		Object[] wrapped = (Object[]) objects.get(row);
		;
		return (BusinessGroup) wrapped[0];
	}

}