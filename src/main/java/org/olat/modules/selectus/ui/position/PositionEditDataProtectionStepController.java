/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.ui.position;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.util.SyntheticUserRequest;
import org.olat.core.util.Util;
import org.springframework.beans.factory.annotation.Autowired;

import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.app_wizard.DataProtectionStepController;

/**
 * 
 * Initial date: 12 sept. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class PositionEditDataProtectionStepController extends FormBasicController  implements PositionEditableController {

	private List<FormLink> previewButtons = new ArrayList<>(2);
	
	private Position position;
	
	private CloseableModalController cmc;
	private DataProtectionStepController dataProtectionCtrl;

	@Autowired
	private RecruitingModule recruitingModule;
	
	public PositionEditDataProtectionStepController(UserRequest ureq, WindowControl wControl, Position position) {
		super(ureq, wControl, "edit_data_protection", Util.createPackageTranslator(PositionController.class, ureq.getLocale()));
		this.position = position;
		initForm(ureq);
	}

	@Override
	public Position getPosition() {
		return position;
	}

	@Override
	public void updatePosition(Position updatedPosition) {
		this.position = updatedPosition;
		initPreviewButtons(flc);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		initPreviewButtons(formLayout);
		formLayout.contextPut("previewButtons", previewButtons);
	}
	
	private void initPreviewButtons(FormItemContainer formLayout) {
		if(!previewButtons.isEmpty()) {
			for(FormLink previewButton:previewButtons) {
				formLayout.remove(previewButton);
			}
			previewButtons.clear();
		}
		
		List<Locale> locales  = recruitingModule.getPositionLocales(position);
		for(Locale locale:locales) {
			String link;
			if(locales.size() == 1) {
				link = translate("edit.template.preview");
			} else {
				link = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
			}
			FormLink previewButton = uifactory.addFormLink("preview_".concat(locale.getLanguage()), "preview", link, null, formLayout, Link.BUTTON | Link.NONTRANSLATED);
			previewButton.setUserObject(locale);
			previewButtons.add(previewButton);
		}
		formLayout.contextPut("previewButtons", previewButtons);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(dataProtectionCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(dataProtectionCtrl);
		removeAsListenerAndDispose(cmc);
		dataProtectionCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("preview".equals(link.getCmd())) {
				doPreview(ureq, (Locale)link.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doPreview(UserRequest ureq, Locale locale) {
		if(!locale.getLanguage().equals(getLocale().getLanguage())) {
			ureq = new SyntheticUserRequest(getIdentity(), locale);
		}
		
		dataProtectionCtrl = new DataProtectionStepController(ureq, getWindowControl(), position);
		listenTo(dataProtectionCtrl);
		
		String title;
		if(previewButtons.size() == 1) {
			title = translate("edit.template.preview");
		} else {
			title = translate("edit.template.preview_ml", new String[] { locale.getLanguage() });
		}
		cmc = new CloseableModalController(getWindowControl(), "c", dataProtectionCtrl.getInitialComponent(), title);
		listenTo(cmc);
		cmc.activate();
	}
}
