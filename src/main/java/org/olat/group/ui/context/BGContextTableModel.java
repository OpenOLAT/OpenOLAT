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

package org.olat.group.ui.context;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.course.groupsandrights.ui.DefaultContextTranslationHelper;
import org.olat.group.context.BGContext;

/**
 * Description:<BR>
 * The business group table model contains a list of business groups and can
 * display the business groups
 * <P>
 * Initial Date: Jan 24, 2005
 * 
 * @author gnaegi
 */
public class BGContextTableModel extends DefaultTableDataModel implements TableDataModel {
	private Translator trans;
	private boolean showType;
	private boolean showDefault;

	/**
	 * Constructor for the business group table model
	 * 
	 * @param groupContexts The list of group contexts
	 * @param trans
	 * @param showType true: show type row
	 * @param showDefault true: show isDefaultContext flag
	 */
	public BGContextTableModel(List groupContexts, Translator trans, boolean showType, boolean showDefault) {
		super(groupContexts);
		this.trans = trans;
		this.showType = showType;
		this.showDefault = showDefault;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
	 */
	public int getColumnCount() {
		int column = 2;
		if (showType) column++;
		if (showDefault) column++;
		return column;
	}

	/**
	 * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
	 */
	public Object getValueAt(int row, int col) {
		BGContext context = (BGContext) objects.get(row);
		switch (col) {
			case 0:
				String name = DefaultContextTranslationHelper.translateIfDefaultContextName(context, trans);
				name = StringEscapeUtils.escapeHtml(name).toString();
				return name;
			case 1:
				String description = context.getDescription();
				description = FilterFactory.getHtmlTagsFilter().filter(description);
				description = Formatter.truncate(description, 256);
				return description;
			case 2:
				if (showType) return trans.translate(context.getGroupType());
				else return new Boolean(context.isDefaultContext());
			case 3:
				return new Boolean(context.isDefaultContext());
			default:
				return "ERROR";
		}
	}

	/**
	 * @param row
	 * @return BGContext from given row
	 */
	public BGContext getGroupContextAt(int row) {
		return (BGContext) objects.get(row);
	}

}