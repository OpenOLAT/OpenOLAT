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

package org.olat.course.archiver;

import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiSortableColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.course.assessment.model.AssessmentNodeData;

/**
 * Initial Date:  Jun 23, 2004
 *
 * @author gnaegi
 *
 * Comment: 
 * Use the IndentedNodeRenderer to render the node element!
 */
public class NodeTableDataModel extends DefaultFlexiTableDataModel<AssessmentNodeData> {

    public NodeTableDataModel(FlexiTableColumnModel columnModel) {
        super(columnModel);
    }

    @Override
	public boolean isSelectable(int row) {
		return getObject(row).isSelectable();
	}

	@Override
    public Object getValueAt(int row, int col) {
    	AssessmentNodeData nodeData = getObject(row);
    	switch (NodeCols.values()[col]) {
			case data: return nodeData;// rendered using the indentedNodeRenderer
			case select: return Boolean.valueOf(nodeData.isSelectable());
			default: return "error";
		}
    } 

	public enum NodeCols implements FlexiSortableColumnDef {
    	data("table.header.node"),
    	select("table.action.select");
		
		private final String i18nKey;
		
		private NodeCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}

		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}

		@Override
		public boolean sortable() {
			return false;
		}

		@Override
		public String sortKey() {
			return name();
		}
    }
}