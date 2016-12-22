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

import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.model.AssessmentNodeData;

/**
 * Initial Date:  Jun 23, 2004
 *
 * @author gnaegi
 *
 * Comment: 
 * Use the IndentedNodeRenderer to render the node element!
 */
public class NodeTableDataModel extends DefaultTableDataModel<AssessmentNodeData> {
	private final Translator trans;
	
		/**
		 * Constructor for the node table
		 * @param objects List maps containing the node data using the keys defined in AssessmentHelper
		 * @param trans The table model translator
		 * any node select link
		 */
    public NodeTableDataModel(List<AssessmentNodeData> objects, Translator trans) {
        super(objects);
        this.trans = trans;
    }

    /**
     * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
    	// node, select
        return 2;
    }

    /**
     * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int col) {
    	AssessmentNodeData nodeData = getObject(row);
    	switch (col) {
			case 0: return nodeData;// rendered using the indentedNodeRenderer
			case 1: return nodeData.isSelectable() ? trans.translate("select") : null;
			case 2: return nodeData.getMinScore();
			case 3: return nodeData.getMaxScore();
			case 4: return nodeData.isOnyx() ? trans.translate("table.action.showOnyxReporter") : "";
			default: return "error";
		}
    }

    public enum Cols {
    	data,
    	select,
    	min,
    	max,
    	onyxReport
    }
 
}
