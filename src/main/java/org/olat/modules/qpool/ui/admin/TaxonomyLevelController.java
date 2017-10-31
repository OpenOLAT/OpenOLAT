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
package org.olat.modules.qpool.ui.admin;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.util.Util;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.ui.QuestionsController;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 17.04.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyLevelController extends FormBasicController {

	private FormLink editButton, deleteButton;
	private StaticTextElement pathEl, fieldEl;
	private CloseableModalController cmc;
	private TaxonomyLevelEditController editCtrl;
	private DialogBoxController confirmDeleteCtrl;
	
	private TaxonomyLevel taxonomyLevel;
	@Autowired
	private QPoolService qpoolService;
	
	public TaxonomyLevelController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(QuestionsController.class, ureq.getLocale()));
		initForm(ureq);
		//make it invisible, no level to show
		flc.setVisible(false);
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("edit");
		
		pathEl = uifactory.addStaticTextElement("parentLine", "classification.taxonomy.parents", "", formLayout);
		fieldEl = uifactory.addStaticTextElement("taxonomylevel", "classification.taxonomy.level", "", formLayout);

		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		buttonsCont.setRootForm(mainForm);
		formLayout.add(buttonsCont);
		editButton = uifactory.addFormLink("edit", buttonsCont, Link.BUTTON);
		deleteButton = uifactory.addFormLink("delete", buttonsCont, Link.BUTTON);
	}
	
	public TaxonomyLevel getTaxonomyLevel() {
		return taxonomyLevel;
	}
	
	public void setTaxonomyLevel(TaxonomyLevel taxonomyLevel) {
		this.taxonomyLevel = taxonomyLevel;
		
		flc.setVisible(taxonomyLevel != null);
		if(taxonomyLevel != null) {
			String parentLine = null;
			if(this.taxonomyLevel != null) {
				parentLine = taxonomyLevel.getMaterializedPathIdentifiers();
			} else {
				parentLine = "/";
			}
			pathEl.setValue(parentLine);
			fieldEl.setValue(taxonomyLevel.getDisplayName());
		}
		initialPanel.setDirty(true);
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editButton == source) {
			doEditLevel(ureq);
		} else if(deleteButton == source) {
			doConfirmDelete(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				setTaxonomyLevel(editCtrl.getTaxonomyLevel());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		} else if(source == confirmDeleteCtrl) {
			if(DialogBoxUIFactory.isOkEvent(event) || DialogBoxUIFactory.isYesEvent(event)) {
				doDelete(ureq);
			}
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editCtrl);
		removeAsListenerAndDispose(cmc);
		editCtrl = null;
		cmc = null;
	}
	
	private void doConfirmDelete(UserRequest ureq) {
		String title = translate("delete.taxonomyLevel");
		String text = translate("delete.taxonomyLevel.confirm", new String[]{ taxonomyLevel.getDisplayName() });
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
	}
	
	private void doDelete(UserRequest ureq) {
		if(qpoolService.deleteTaxonomyLevel(taxonomyLevel)) {
			showInfo("taxonomyLevel.deleted");
			fireEvent(ureq, Event.CHANGED_EVENT);
		} else {
			showError("taxonomyLevel.notdeleted");
		}
	}

	private void doEditLevel(UserRequest ureq) {
		if(taxonomyLevel == null) return;
		
		TaxonomyLevel parentLevel = taxonomyLevel.getParent();
		removeAsListenerAndDispose(editCtrl);
		editCtrl = new TaxonomyLevelEditController(ureq, getWindowControl(), parentLevel, taxonomyLevel);
		listenTo(editCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"),
				editCtrl.getInitialComponent(), true, translate("edit.taxonomyLevel"));
		cmc.activate();
		listenTo(cmc);
	}
}