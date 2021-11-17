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
package org.olat.ims.qti21.ui.editor.testsexport;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController.InputType;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController.SingleKey;
import org.olat.group.ui.lifecycle.TranslationBundle;

/**
 * 
 * Initial date: 15 nov. 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TestsExportAdminController extends FormBasicController {

	private CloseableModalController cmc;
	private SingleKeyTranslatorController translatorCtrl; 
	
	public TestsExportAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.title");
		
		StaticTextElement viewEl = uifactory.addStaticTextElement("view.additional.sheet", "admin.additional.sheet.label", "", formLayout);
		viewEl.setElementCssClass("o_omit_margin");
		FormLink translationLink = uifactory.addFormLink("translate.additional.sheet", "translation.edit", null, formLayout, Link.BUTTON);
		TranslationBundle bundle = new TranslationBundle("admin.additional.sheet.label", null, "admin.additional.sheet.description", viewEl, translationLink);
		translationLink.setUserObject(bundle);
		bundle.update(getTranslator());

	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(translatorCtrl == source) {
			((TranslationBundle)translatorCtrl.getUserObject()).update(getTranslator());
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(translatorCtrl);
		removeAsListenerAndDispose(cmc);
		translatorCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			if(source.getUserObject() instanceof TranslationBundle) {
				doTranslate(ureq, (TranslationBundle)source.getUserObject());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doTranslate(UserRequest ureq, TranslationBundle bundle) {
		if(guardModalController(translatorCtrl)) return;

		SingleKey descriptionKey = new SingleKey(bundle.getBodyI18nKey(), InputType.RICH_TEXT_ELEMENT);
		translatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), List.of(descriptionKey),
				TestsExportAdminController.class, "");
		translatorCtrl.setUserObject(bundle);
		listenTo(translatorCtrl);

		String title = translate("translate.title", translate(bundle.getLabelI18nKey()));
		cmc = new CloseableModalController(getWindowControl(), "close", translatorCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
}
