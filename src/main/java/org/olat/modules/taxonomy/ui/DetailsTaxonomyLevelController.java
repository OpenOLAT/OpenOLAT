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
package org.olat.modules.taxonomy.ui;

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
import org.olat.core.util.vfs.OlatRelPathImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DetailsTaxonomyLevelController extends FormBasicController {
	
	private FormLink editButton, competencesButton;
	
	private StaticTextElement displayNameEl;
	private StaticTextElement externalIdEl;
	private StaticTextElement directoryPathEl;
	
	private CloseableModalController cmc;
	private TaxonomyLevelCompetenceController comptenceCtrl;
	private EditTaxonomyLevelController editTaxonomyLevelCtrl;
	
	private TaxonomyLevel taxonomyLevel;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public DetailsTaxonomyLevelController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		displayNameEl = uifactory.addStaticTextElement("level.displayname", "level.displayname", "", formLayout);
		externalIdEl = uifactory.addStaticTextElement("level.externalId", "level.externalId", "", formLayout);
		directoryPathEl = uifactory.addStaticTextElement("level.directory.path", "level.directory.path", "", formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		
		editButton = uifactory.addFormLink("edit", buttonsCont, Link.BUTTON);
		competencesButton = uifactory.addFormLink("edit.competences", buttonsCont, Link.BUTTON);
	}
	
	public void setTaxonomyLevel(TaxonomyLevel level) {
		taxonomyLevel = level;
		if(level == null) {
			externalIdEl.setValue("");
			displayNameEl.setValue("");
			directoryPathEl.setValue("");
		} else {
			externalIdEl.setValue(level.getExternalId() == null ? "" : level.getExternalId());
			displayNameEl.setValue(level.getDisplayName());
			VFSContainer container = taxonomyService.getDocumentsLibrary(level);
			if(container instanceof OlatRelPathImpl) {
				directoryPathEl.setValue(((OlatRelPathImpl)container).getRelPath());
			} else {
				directoryPathEl.setValue("");
			}
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editTaxonomyLevelCtrl == source) {
			if(event == Event.DONE_EVENT) {
				setTaxonomyLevel(editTaxonomyLevelCtrl.getTaxonomyLevel());
			}
			cmc.deactivate();
			cleanUp();
		} else if(comptenceCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editTaxonomyLevelCtrl);
		removeAsListenerAndDispose(comptenceCtrl);
		removeAsListenerAndDispose(cmc);
		editTaxonomyLevelCtrl = null;
		comptenceCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editButton == source) {
			doEdit(ureq);
		} else if(competencesButton == source) {
			doCompetence(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doEdit(UserRequest ureq) {
		if(editTaxonomyLevelCtrl != null) return;
		
		TaxonomyLevel reloadedLevel = taxonomyService.getTaxonomyLevel(taxonomyLevel);
		editTaxonomyLevelCtrl = new EditTaxonomyLevelController(ureq, getWindowControl(), reloadedLevel);
		listenTo(editTaxonomyLevelCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editTaxonomyLevelCtrl.getInitialComponent(), true, translate("edit"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doCompetence(UserRequest ureq) {
		if(comptenceCtrl != null) return;
		
		TaxonomyLevel reloadedLevel = taxonomyService.getTaxonomyLevel(taxonomyLevel);
		comptenceCtrl = new TaxonomyLevelCompetenceController(ureq, getWindowControl(), reloadedLevel);
		listenTo(comptenceCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", comptenceCtrl.getInitialComponent(), true, translate("edit.competences"));
		listenTo(cmc);
		cmc.activate();
	}
}
