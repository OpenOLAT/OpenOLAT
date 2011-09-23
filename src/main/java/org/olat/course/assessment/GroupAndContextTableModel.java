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

package org.olat.course.assessment;

import java.util.List;

import org.apache.commons.lang.StringEscapeUtils;
import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.components.table.TableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Formatter;
import org.olat.core.util.filter.FilterFactory;
import org.olat.course.groupsandrights.ui.DefaultContextTranslationHelper;
import org.olat.group.BusinessGroup;

/**
 * Description:<BR/>
 * A table model that can return group name, group description and the group context name
 * for a group.
 * <P/>
 * Initial Date:  Sep 1, 2004
 *
 * @author gnaegi
 */
public class GroupAndContextTableModel extends DefaultTableDataModel implements TableDataModel {
    private static final int COLUMN_COUNT = 4;    
    private Translator trans;

    /**
     * Constructor for the group and context table model
     * @param groups
     * @param trans translator for the business group context
     */
    public GroupAndContextTableModel(List groups, Translator trans) {
    		super(groups);
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
        BusinessGroup businessGroup = (BusinessGroup) objects.get(row);
        switch (col) {
            case 0 :
                return StringEscapeUtils.escapeHtml(businessGroup.getName()).toString();
            case 1 :
            	String tmp = businessGroup.getDescription();
            	tmp = FilterFactory.getHtmlTagsFilter().filter(tmp);
                tmp = Formatter.truncate(tmp, 256);
                return tmp;
            case 2 : 
            	String name = DefaultContextTranslationHelper.translateIfDefaultContextName(businessGroup.getGroupContext(), trans);
      				name = StringEscapeUtils.escapeHtml(name).toString();
      				return name;
            default :
                return "ERROR";
        }
    }

    /**
     * @param row
     * @return the business group at the given row
     */
    public BusinessGroup getBusinessGroupAt(int row) {
        return (BusinessGroup) objects.get(row);
    }

}