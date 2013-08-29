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

package org.olat.ims.qti.editor;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.tabbedpane.TabbedPaneChangedEvent;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.tabbable.TabbableDefaultController;
import org.olat.core.gui.translator.Translator;
import org.olat.ims.qti.editor.beecom.objects.Item;
import org.olat.ims.qti.editor.beecom.objects.Question;

/**
 * Initial Date: Nov 21, 2004 <br>
 * 
 * @author patrick
 */
public class ItemNodeTabbedFormController extends TabbableDefaultController {
	
	//private static final String VC_ROOT = Util.getPackageVelocityRoot(QTIEditorMainController.class);	
	private Item item;
	private QTIEditorPackage qtiPackage;
	private ItemMetadataFormController metadataCtr;
	private FeedbackFormController feedbackCtr;
	private Panel feedbackPanel = new Panel("feedbackPanel");

	private boolean restrictedEdit;
	
	/**
	 * @param item
	 * @param qtiPackage
	 * @param wControl
	 * @param trnsltr
	 */
	public ItemNodeTabbedFormController(Item item, QTIEditorPackage qtiPackage, UserRequest ureq, WindowControl wControl, Translator trnsltr,
			boolean restrictedEdit) {
		super(ureq, wControl);
		this.restrictedEdit = restrictedEdit;
		metadataCtr = new ItemMetadataFormController(ureq, getWindowControl(), item, qtiPackage, restrictedEdit);
		this.listenTo(metadataCtr);
		this.item = item;
		this.qtiPackage = qtiPackage;
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.control.Controller, org.olat.core.gui.control.Event)
	 */
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == metadataCtr && event.equals(Event.DONE_EVENT)) {
			qtiPackage.serializeQTIDocument();
		}	else {
			// Pass events over to the parent controller
			fireEvent(ureq, event);
		}
		
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#event(org.olat.core.gui.UserRequest,
	 *      org.olat.core.gui.components.Component, org.olat.core.gui.control.Event)
	 */
	public void event(UserRequest ureq, Component source, Event event) {
		if (event instanceof TabbedPaneChangedEvent) {
			TabbedPaneChangedEvent tabbedPaneEvent = (TabbedPaneChangedEvent) event;
			if (feedbackPanel.equals(tabbedPaneEvent.getNewComponent())) {
				if (feedbackCtr != null) removeAsListenerAndDispose(feedbackCtr);
				feedbackCtr = new FeedbackFormController(ureq, getWindowControl(), qtiPackage, item, restrictedEdit);
				// feedback controller sends out NodeBeforeChangeEvents which must be propagated				
				this.listenTo(feedbackCtr);
				feedbackPanel.setContent(feedbackCtr.getInitialComponent());
			}
		}
	}

	/**
	 * @see org.olat.core.gui.control.DefaultController#doDispose(boolean)
	 */
	protected void doDispose() {
	// metadataCtr and feedbackCtr are registered as child controllers
	}

	/**
	 * @see org.olat.core.gui.control.generic.tabbable.TabbableController#addTabs(org.olat.core.gui.components.TabbedPane)
	 */
	public void addTabs(TabbedPane tabbedPane) {
		// add as listener to get tab activation events
		tabbedPane.addListener(this);
		
		if (item.isAlient()) {
			// this is an unknown type.
			tabbedPane.addTab(translate("tab.metadata"),this.createVelocityContainer("tab_itemAlien"));			
			return;
		}

		boolean isSurvey = qtiPackage.getQTIDocument().isSurvey();
		int questionType = item.getQuestion().getType();
		
		tabbedPane.addTab(translate("tab.metadata"), metadataCtr.getInitialComponent());

		Controller ctrl = null;
		
		switch (questionType) {
			case Question.TYPE_SC:
				ctrl = new ChoiceItemController(item, qtiPackage, getTranslator(), getWindowControl(), restrictedEdit);
				break;
			case Question.TYPE_MC:
				ctrl = new ChoiceItemController(item, qtiPackage, getTranslator(), getWindowControl(), restrictedEdit);
				break;
			case Question.TYPE_KPRIM:
				ctrl = new ChoiceItemController(item, qtiPackage, getTranslator(), getWindowControl(), restrictedEdit);
				break;
			case Question.TYPE_FIB:
				ctrl = new FIBItemController(item, qtiPackage, getTranslator(), getWindowControl(), restrictedEdit);
				break;
			case Question.TYPE_ESSAY:
				ctrl = new EssayItemController(item, qtiPackage, getTranslator(), getWindowControl(), restrictedEdit);
				break;
		}
		if (ctrl != null) { // if item was identified
			tabbedPane.addTab(translate("tab.question"), ctrl.getInitialComponent());			
			listenTo(ctrl);
			if (!isSurvey && questionType != Question.TYPE_ESSAY) {
				tabbedPane.addTab(translate("tab.feedback"), feedbackPanel);
			}
			Controller itemPreviewController = new ItemPreviewController(getWindowControl(), item, qtiPackage, getTranslator());
			tabbedPane.addTab(translate("tab.preview"), itemPreviewController.getInitialComponent());
			tabbedPane.addListener(itemPreviewController);
		}
	}
}