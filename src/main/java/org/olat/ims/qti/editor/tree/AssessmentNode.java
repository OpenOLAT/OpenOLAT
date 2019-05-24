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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableController;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.memento.Memento;
import org.olat.ims.qti.editor.AssessmentController;
import org.olat.ims.qti.editor.QTIEditorMainController;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.editor.beecom.objects.Assessment;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;
import org.olat.ims.qti.editor.beecom.objects.Section;

/**
 * Initial Date: Nov 18, 2004 <br>
 * 
 * @author patrick
 */
public class AssessmentNode extends GenericQtiNode {

	private Assessment assmnt;
	private QTIEditorPackage qtiPackage;
	private TabbedPane myTabbedPane;

	/**
	 * @param ass
	 * @param qtiPackage
	 */
	public AssessmentNode(Assessment ass, QTIEditorPackage qtiPackage) {
		this.assmnt = ass;
		this.qtiPackage = qtiPackage;
		setMenuTitleAndAlt(ass.getTitle());
		setUserObject(ass.getIdent());
		if (qtiPackage.getQTIDocument().isSurvey()) setIconCssClass("o_mi_iqsurv");
		else setIconCssClass("o_mi_iqtest");
	}

	/**
	 * Set's the node's title and alt text (truncates title)
	 * 
	 * @param title
	 */
	@Override
	public void setMenuTitleAndAlt(String title) {
		super.setMenuTitleAndAlt(title);
		assmnt.setTitle(title);
	}

	@Override
	public TabbedPane createEditTabbedPane(UserRequest ureq, WindowControl wControl, Translator trnsltr,
			QTIEditorMainController editorMainController) {
		if (myTabbedPane == null) {
			myTabbedPane = new TabbedPane("tabbedPane", ureq.getLocale());
			TabbableController tabbCntrllr = new AssessmentController(assmnt, qtiPackage, ureq, wControl,
					editorMainController.isRestrictedEdit(), editorMainController.isBlockedEdit());
			tabbCntrllr.addTabs(myTabbedPane);
			tabbCntrllr.addControllerListener(editorMainController);
		}
		return myTabbedPane;
	}

	@Override
	public void childNodeChanges() {
		//
	}

	/**
	 * @return Assessment
	 */
	public Assessment getAssessment() {
		return assmnt;
	}

	/**
	 * @see org.olat.ims.qti.editor.tree.IQtiNode#insertQTIObjectAt(QTIObject,
	 *      int)
	 */
	public void insertQTIObjectAt(QTIObject object, int position) {
		List sections = assmnt.getSections();
		sections.add(position, object);
	}

	/**
	 * @see org.olat.ims.qti.editor.tree.IQtiNode#removeQTIObjectAt(int)
	 */
	public QTIObject removeQTIObjectAt(int position) {
		List<Section> sections = assmnt.getSections();
		return sections.remove(position);
	}

	/**
	 * @see org.olat.ims.qti.editor.tree.IQtiNode#getQTIObjectAt(int)
	 */
	public QTIObject getQTIObjectAt(int position) {
		List sections = assmnt.getSections();
		return (QTIObject) sections.get(position);
	}

	/**
	 * @see org.olat.ims.qti.editor.tree.IQtiNode#getUnderlyingQTIObject()
	 */
	public QTIObject getUnderlyingQTIObject() {
		return assmnt;
	}

	public Memento createMemento() {
		// so far only TITLE and OBJECTIVES are stored in the memento
		QtiNodeMemento qnm = new QtiNodeMemento();
		Map<String,Object> qtiState = new HashMap<>();
		qtiState.put("ID", assmnt.getIdent());
		qtiState.put("TITLE", assmnt.getTitle());
		qtiState.put("OBJECTIVES", assmnt.getObjectives());
		qnm.setQtiState(qtiState);
		return qnm;
	}

	public void setMemento(Memento state) {
		//
	}

	public String createChangeMessage(Memento mem) {
		String retVal = null;
		if (mem instanceof QtiNodeMemento) {
			QtiNodeMemento qnm = (QtiNodeMemento) mem;
			Map<String,Object> qtiState = qnm.getQtiState();
			String oldTitle = (String) qtiState.get("TITLE");
			String newTitle = assmnt.getTitle();
			String titleChange = null;
			String oldObjectives = (String) qtiState.get("OBJECTIVES");
			String newObjectives = assmnt.getObjectives();
			String objectChange = null;
			retVal = "\nMetadata changes:";
			if ((oldTitle != null && !oldTitle.equals(newTitle)) || (newTitle != null && !newTitle.equals(oldTitle))) {
				titleChange = "\n\nold title: \n\t" + formatVariable(oldTitle) + "\n\nnew title: \n\t" + formatVariable(newTitle);
				retVal += titleChange;
			}
			if ((oldObjectives != null && !oldObjectives.equals(newObjectives))
					|| (newObjectives != null && !newObjectives.equals(oldObjectives))) {
				objectChange = "\n\nold objectives: \n\t" + formatVariable(oldObjectives) + "\n\nnew objectives: \n\t"
						+ formatVariable(newObjectives);
				retVal += objectChange;
			}
			return retVal;
		}
		return "undefined";
	}
}