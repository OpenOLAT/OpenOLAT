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
import java.util.Map;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * Use the IndentedNodeRenderer to render the node element!
 * Initial Date:  11.08.2005 <br>
 *
 * @author gnaegi
 */
public class NodeAssessmentTableDataModel extends DefaultTableDataModel {
		private Translator trans;
		private boolean nodesSelectable;
	
		/**
		 * Constructor for the node assessment table
		 * @param objects List maps containing the user and node data using the keys defined in AssessmentHelper
		 * @param trans The table model translator
		 * @param nodesSelectable true: show node select link where available, false: don't show
		 * any node select link
		 */
    public NodeAssessmentTableDataModel(List<Map<String,Object>> objects, Translator trans, boolean nodesSelectable) {
        super(objects);
        this.trans = trans;
        this.nodesSelectable = nodesSelectable;
    }

    /**
     * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
     */
    public int getColumnCount() {
    		// node, details, attempts, score, passed
        return 6;
    }

    /**
     * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
     */
    public Object getValueAt(int row, int col) {
    	Map<String,Object> nodeData = (Map<String, Object>) getObject(row);
    	switch (col) {
				case 0:
					// rendered using the indentedNodeRenderer
					return nodeData;
				case 1:
					return nodeData.get(AssessmentHelper.KEY_TITLE_SHORT);
				case 2:
					return nodeData.get(AssessmentHelper.KEY_ATTEMPTS);
				case 3:
					return nodeData.get(AssessmentHelper.KEY_SCORE);
				case 4:
					return nodeData.get(AssessmentHelper.KEY_PASSED);
				case 5:
					// selection command
					Boolean courseNodeEditable = (Boolean) nodeData.get(AssessmentHelper.KEY_SELECTABLE);
					if (nodesSelectable && courseNodeEditable.booleanValue()) return trans.translate("select");
					else return null;
				default:
					return "error";
			}
    }
    
}
