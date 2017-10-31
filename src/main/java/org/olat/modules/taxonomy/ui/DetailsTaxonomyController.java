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

import org.olat.core.commons.editor.htmleditor.HTMLEditorController;
import org.olat.core.commons.editor.htmleditor.WysiwygFactory;
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
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 27 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class DetailsTaxonomyController extends FormBasicController {
	
	private FormLink editButton, editTypesButton, editInfoPageButton;
	private StaticTextElement displayNameEl;
	
	private CloseableModalController cmc;
	private HTMLEditorController editInfoPageCtrl;
	private EditTaxonomyController editTaxonomyCtrl;
	private TaxonomyLevelTypesEditController editTypesCtrl;
	
	private Taxonomy taxonomy;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public DetailsTaxonomyController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		displayNameEl = uifactory.addStaticTextElement("taxonomy.displayname", "taxonomy.displayname", "", formLayout);
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		
		editButton = uifactory.addFormLink("edit", buttonsCont, Link.BUTTON);
		editTypesButton = uifactory.addFormLink("edit.level.types", buttonsCont, Link.BUTTON);
		editInfoPageButton = uifactory.addFormLink("edit.info.page", buttonsCont, Link.BUTTON);
	}
	
	public void setTaxonomy(Taxonomy taxonomy) {
		this.taxonomy = taxonomy;
		if(taxonomy == null) {
			displayNameEl.setValue("");
		} else {
			displayNameEl.setValue(taxonomy.getDisplayName());
		}
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(editButton == source) {
			doEditTaxonomy(ureq);
		} else if(editTypesButton == source) {
			doEditTaxonomyLevelTypes(ureq);
		} else if(editInfoPageButton == source) {
			doEditInfoPage(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(editTaxonomyCtrl == source) {
			if(event == Event.DONE_EVENT) {
				setTaxonomy(editTaxonomyCtrl.getTaxonomy());
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editTypesCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(editInfoPageCtrl == source) {
			if(event == Event.DONE_EVENT) {
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(editInfoPageCtrl);
		removeAsListenerAndDispose(editTaxonomyCtrl);
		removeAsListenerAndDispose(editTypesCtrl);
		removeAsListenerAndDispose(cmc);
		editInfoPageCtrl = null;
		editTaxonomyCtrl = null;
		editTypesCtrl = null;
		cmc = null;
	}

	private void doEditTaxonomy(UserRequest ureq) {
		if(editTaxonomyCtrl != null) return;
		
		editTaxonomyCtrl = new EditTaxonomyController(ureq, getWindowControl(), taxonomy);
		listenTo(editTaxonomyCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editTaxonomyCtrl.getInitialComponent(), true, translate("edit.level.types"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditTaxonomyLevelTypes(UserRequest ureq) {
		if(editTypesCtrl != null) return;
		
		editTypesCtrl = new TaxonomyLevelTypesEditController(ureq, getWindowControl(), taxonomy);
		listenTo(editTypesCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editTypesCtrl.getInitialComponent(), true, translate("edit.level.types"));
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doEditInfoPage(UserRequest ureq) {
		if(editInfoPageCtrl != null) return;
		
		VFSContainer container = taxonomyService.getTaxonomyInfoPageContainer(taxonomy);
		String pageRelPath = "index.html";
		if(container.resolve(pageRelPath) == null) {
			container.createChildLeaf(pageRelPath);
		}
		editInfoPageCtrl = WysiwygFactory.createWysiwygControllerWithInternalLink(ureq, getWindowControl(), container, pageRelPath, true, null);
		listenTo(editInfoPageCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), "close", editInfoPageCtrl.getInitialComponent(), true, translate("edit.info.page"));
		listenTo(cmc);
		cmc.activate();
	}
}
