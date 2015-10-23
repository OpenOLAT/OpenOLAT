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

import java.util.List;

import org.olat.core.gui.components.table.DefaultTableDataModel;
import org.olat.core.gui.translator.Translator;
import org.olat.course.assessment.model.AssessmentNodeData;

/**
 * Description:<br>
 * Use the IndentedNodeRenderer to render the node element!
 * Initial Date:  11.08.2005 <br>
 *
 * @author gnaegi
 */
public class NodeAssessmentTableDataModel extends DefaultTableDataModel<AssessmentNodeData> {
		private Translator trans;
		private boolean nodesSelectable;
	
		/**
		 * Constructor for the node assessment table
		 * @param objects List maps containing the user and node data using the keys defined in AssessmentHelper
		 * @param trans The table model translator
		 * @param nodesSelectable true: show node select link where available, false: don't show
		 * any node select link
		 */
    public NodeAssessmentTableDataModel(List<AssessmentNodeData> objects, Translator trans, boolean nodesSelectable) {
        super(objects);
        this.trans = trans;
        this.nodesSelectable = nodesSelectable;
    }

    /**
     * @see org.olat.core.gui.components.table.TableDataModel#getColumnCount()
     */
    @Override
    public int getColumnCount() {
        return 7;
    }

    /**
     * @see org.olat.core.gui.components.table.TableDataModel#getValueAt(int, int)
     */
    @Override
    public Object getValueAt(int row, int col) {
    	AssessmentNodeData nodeData = getObject(row);
    	switch (col) {
			case 0: return nodeData;// rendered using the indentedNodeRenderer
			case 1: return nodeData.getShortTitle();
			case 2: return nodeData.getAttempts();
			case 3: return nodeData.getScore();
			case 4: return nodeData.getPassed();
			case 5:
				// selection command
				if (nodesSelectable && nodeData.isSelectable()) {
					return trans.translate("select");
				}
				return null;
			case 6: return nodeData.getMinScore();
			case 7: return nodeData.getMaxScore();
			case 8: return nodeData.getAssessmentStatus();
			default: return "error";
		}
    }
}