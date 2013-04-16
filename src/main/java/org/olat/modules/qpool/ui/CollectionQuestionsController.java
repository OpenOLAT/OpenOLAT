/**
 * <a href="http://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="http://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, http://www.frentix.com
 * <p>
 */
package org.olat.modules.qpool.ui;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.ui.datasource.CollectionOfItemsSource;

/**
 * 
 * Initial date: 16.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CollectionQuestionsController extends QuestionsController {

	private Link deleteSource, renameSource;
	private RenameController renameCtrl;
	private DialogBoxController confirmDeleteSourceBox;
	private CloseableCalloutWindowController renameCallout;
	
	private QuestionItemCollection collection;
	private final QPoolService qpoolService;

	public CollectionQuestionsController(UserRequest ureq, WindowControl wControl, CollectionOfItemsSource source) {
		super(ureq, wControl, source);
		this.collection = source.getCollection();
		qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
	}

	@Override
	protected void initVelocityContainer(VelocityContainer mainVc) {
		deleteSource = LinkFactory.createButton("delete.collection", mainVc, this);
		renameSource = LinkFactory.createButton("rename.collection", mainVc, this);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(deleteSource == source) {
			doConfirmDeleteSource(ureq);
		} else if(renameSource == source) {
			doOpenRenameCallout(ureq);
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == confirmDeleteSourceBox) {
			boolean delete = DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event);
			if(delete) {
				doDelete(ureq);
			}
		} else if(source == renameCtrl) {
			if(Event.CHANGED_EVENT == event) {
				String newName = renameCtrl.getName();
				doRename(ureq, newName);
			}
			renameCallout.deactivate();
		}
		super.event(ureq, source, event);
	}
	
	private void doOpenRenameCallout(UserRequest ureq) {
		removeAsListenerAndDispose(renameCtrl);
		renameCtrl = new RenameController(ureq, getWindowControl());
		listenTo(renameCtrl);
		
		removeAsListenerAndDispose(renameCallout);
		renameCallout = new CloseableCalloutWindowController(ureq, getWindowControl(), renameCtrl.getInitialComponent(),
				renameSource, "", true, null);
		listenTo(renameCallout);
		renameCallout.activate();	
	}
	
	private void doRename(UserRequest ureq, String newName) {
		collection = qpoolService.renameCollection(collection, newName);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.COLL_CHANGED));
	}

	private void doConfirmDeleteSource(UserRequest ureq) {
		confirmDeleteSourceBox = activateYesNoDialog(ureq, null, translate("confirm.delete.source"), confirmDeleteSourceBox);
	}
	
	private void doDelete(UserRequest ureq) {
		qpoolService.deleteCollection(collection);
		fireEvent(ureq, new QPoolEvent(QPoolEvent.COLL_DELETED));
	}
	
	private static class RenameController extends FormBasicController {
		
		private TextElement nameEl;
		
		public RenameController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl, LAYOUT_VERTICAL);
			
			initForm(ureq);
		}
		
		public String getName() {
			return nameEl.getValue();
		}

		@Override
		protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
			nameEl = uifactory.addTextElement("newname", "collection.name", 32, "", formLayout);
			uifactory.addFormSubmitButton("ok", formLayout);
		}
		
		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void formOK(UserRequest ureq) {
			fireEvent(ureq, Event.CHANGED_EVENT);
		}
	}
}
