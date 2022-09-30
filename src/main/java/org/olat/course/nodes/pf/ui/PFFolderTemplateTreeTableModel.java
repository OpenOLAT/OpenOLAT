/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.nodes.pf.ui;

import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTreeTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;

import java.util.List;

/**
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, https://www.frentix.com
 */
public class PFFolderTemplateTreeTableModel extends DefaultFlexiTreeTableDataModel<PFFolderTemplateRow> {

    public PFFolderTemplateTreeTableModel(FlexiTableColumnModel tableColumnModel) {
        super(tableColumnModel);
    }

    @Override
    public boolean hasChildren(int row) {
        PFFolderTemplateRow level = getObject(row);
        return level != null && level.getNumberOfChildren() != 0;
    }

    @Override
    public Object getValueAt(int row, int col) {
        PFFolderTemplateRow level = getObject(row);
        switch (PFFolderTemplateCols.values()[col]) {
        case folderName:
            return level.getFolderName();
        case numOfChildren:
            return level.getNumberOfChildren();
        case createSubFolder:
            return level.getCreateSubFolderLink();
        case toolsLink:
            return level.getToolsLink();
        default:
            return "ERROR";
        }
    }

    @Override
    public void filter(String searchString, List<FlexiTableFilter> filters) {
        // No filters needed in this usecase
    }
}