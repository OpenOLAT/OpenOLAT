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

package org.olat.ims.qti.editor.tree;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.nodes.INode;
import org.olat.ims.qti.editor.QTIEditorMainController;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;

/**
 * Initial Date: Nov 18, 2004 <br>
 * 
 * @author patrick
 */
public interface IQtiNode extends INode {

	/**
	 * @param ureq
	 * @param wControl
	 * @param trnsltr
	 * @param editorMainController
	 * @return Controller
	 */
	public TabbedPane createEditTabbedPane(UserRequest ureq, WindowControl wControl, Translator trnsltr,
			QTIEditorMainController editorMainController);
	
	public void childNodeChanges();

	/**
	 * @return The underlying QTI Object.
	 */
	public QTIObject getUnderlyingQTIObject();

	/**
	 * Return the QTI Object at the given position.
	 * 
	 * @param position
	 * @return QTI Object
	 */
	public QTIObject getQTIObjectAt(int position);

	/**
	 * Insert a QTI node at the specific position.
	 * 
	 * @param object
	 * @param position
	 */
	public void insertQTIObjectAt(QTIObject object, int position);

	/**
	 * Remove the QTI node at the specific position.
	 * 
	 * @param position
	 * @return The removed node
	 */
	public QTIObject removeQTIObjectAt(int position);
}