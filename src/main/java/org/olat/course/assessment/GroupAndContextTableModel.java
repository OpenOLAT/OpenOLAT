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

package org.olat.course.assessment;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.filter.FilterFactory;
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
public class GroupAndContextTableModel extends DefaultTableDataModel<BusinessGroup> {
    private static final int COLUMN_COUNT = 3;    

    /**
     * Constructor for the group and context table model
     * @param groups
     * @param trans translator for the business group context
     */
    public GroupAndContextTableModel(List<BusinessGroup> groups) {
    	super(groups);
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
    @Override
    public Object getValueAt(int row, int col) {
        BusinessGroup businessGroup = objects.get(row);
        switch (col) {
            case 0 :
            	String groupName = businessGroup.getName();
            	return StringHelper.containsNonWhitespace(groupName) ? businessGroup.getName() : "???";
            case 1 :
            	String tmp = businessGroup.getDescription();
            	tmp = FilterFactory.getHtmlTagsFilter().filter(tmp);
            	tmp = Formatter.truncate(tmp, 256);
            	return tmp;
            default :
                return "ERROR";
        }
    }

	@Override
	public Object createCopyWithEmptyList() {
		return new GroupAndContextTableModel(new ArrayList<BusinessGroup>());
	}
}