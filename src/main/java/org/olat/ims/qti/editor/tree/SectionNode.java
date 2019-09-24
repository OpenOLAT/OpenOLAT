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
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.memento.Memento;
import org.olat.ims.qti.editor.QTIEditorMainController;
import org.olat.ims.qti.editor.QTIEditorPackage;
import org.olat.ims.qti.editor.SectionController;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.QTIObject;
import org.olat.ims.qti.editor.beecom.objects.Section;

/**
 * Initial Date: Nov 21, 2004 <br>
 * @author patrick
 */
public class SectionNode extends GenericQtiNode {

	private Section section;
	private QTIEditorPackage qtiPackage;
	private TabbedPane myTabbedPane;
	private SectionController sectionCtrl;
	
	
	public SectionNode(Section theSection) {
		super(theSection.getIdent());
		section = theSection;
		setMenuTitleAndAlt(section.getTitle());
		setUserObject(section.getIdent());
		setIconCssClass("o_mi_qtisection");
	}

	/**
	 * @param theSection
	 * @param qtiPackage
	 */
	public SectionNode(Section theSection, QTIEditorPackage qtiPackage) {
		section = theSection;
		this.qtiPackage = qtiPackage;
		setMenuTitleAndAlt(section.getTitle());
		setUserObject(section.getIdent());
		setIconCssClass("o_mi_qtisection");
	}

	/**
	 * Set's the node's title and alt text (truncates title)
	 * @param title
	 */
	@Override
	public void setMenuTitleAndAlt(String title) {
		super.setMenuTitleAndAlt(title);
		section.setTitle(title);
	}

	@Override
	public TabbedPane createEditTabbedPane(UserRequest ureq, WindowControl wControl, Translator trnsltr, QTIEditorMainController editorMainController) {
		if (myTabbedPane == null) {
			myTabbedPane = new TabbedPane("tabbedPane", ureq.getLocale());
			sectionCtrl = new SectionController(section, qtiPackage, ureq, wControl,
					editorMainController.isRestrictedEdit(), editorMainController.isBlockedEdit());
			sectionCtrl.addTabs(myTabbedPane);
			sectionCtrl.addControllerListener(editorMainController);
		}
		return myTabbedPane;
	}

	@Override
	public void childNodeChanges() {
		if(sectionCtrl != null) {
			sectionCtrl.childNodeChanges();
		}
	}

	/**
	 * @see org.olat.ims.qti.editor.tree.IQtiNode#insertQTIObjectAt(org.olat.ims.qti.editor.beecom.objects.QTIObject, int)
	 */
	public void insertQTIObjectAt(QTIObject object, int position) {
		List<Item> items = section.getItems();
		if(object instanceof Item) {
			items.add(position, (Item)object);
		}
	}

	/**
	 * @see org.olat.ims.qti.editor.tree.IQtiNode#removeQTIObjectAt(int)
	 */
	public QTIObject removeQTIObjectAt(int position) {
		List<Item> items = section.getItems();
		return items.remove(position);
	}

	/**
	 * @see org.olat.ims.qti.editor.tree.IQtiNode#getQTIObjectAt(int)
	 */
	public QTIObject getQTIObjectAt(int position) {
		List<Item> items = section.getItems();
		return items.get(position);
	}

	@Override
	public QTIObject getUnderlyingQTIObject() {
		return section;
	}

	@Override
	public Memento createMemento() {
		//so far only TITLE and OBJECTIVES are stored in the memento
		QtiNodeMemento qnm = new  QtiNodeMemento();
		Map<String,Object> qtiState = new HashMap<>();
		qtiState.put("ID",section.getIdent());
		qtiState.put("TITLE",section.getTitle());
		qtiState.put("OBJECTIVES",section.getObjectives());
		qnm.setQtiState(qtiState);
		return qnm;
	}

	@Override
	public void setMemento(Memento state) {
		//
	}

	public String createChangeMessage(Memento mem) {
		String retVal = null;
		if(mem instanceof QtiNodeMemento){
			QtiNodeMemento qnm = (QtiNodeMemento)mem;
			Map<String,Object> qtiState = qnm.getQtiState();
			String oldTitle = (String)qtiState.get("TITLE");
			String newTitle = section.getTitle();
			String titleChange = null;
			String oldObjectives  = (String)qtiState.get("OBJECTIVES");
			String newObjectives = section.getObjectives();
			String objectChange = null;
			retVal = "\nSection metadata changed:";
			if((oldTitle!=null && !oldTitle.equals(newTitle))||(newTitle!=null && !newTitle.equals(oldTitle))){
				titleChange ="\n\nold title: \n\t"+ formatVariable(oldTitle)+"\n\nnew title: \n\t"+formatVariable(newTitle);
				retVal += titleChange;
			}
			if((oldObjectives!=null && !oldObjectives.equals(newObjectives))||(newObjectives!=null && !newObjectives.equals(oldObjectives))){
				objectChange ="\n\nold objectives: \n\t"+formatVariable(oldObjectives)+"\n\nnew objectives: \n\t"+formatVariable(newObjectives);
				retVal += objectChange;
			}
			return retVal;
		}
		return "undefined";
	}

}