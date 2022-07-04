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
package org.olat.modules.catalog.ui.admin;

import static org.olat.core.gui.components.util.SelectionValues.entry;
import static org.olat.modules.catalog.CatalogV2Module.HEADER_BG_IMAGE_MIME_TYPES;
import static org.olat.modules.catalog.CatalogV2Module.TAXONOMY_LEVEL_LAUNCHER_STYLE_RECTANGLE;
import static org.olat.modules.catalog.CatalogV2Module.TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FileElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.FileElementEvent;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.Util;
import org.olat.core.util.ValidationStatus;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.modules.catalog.CatalogV2Module;
import org.olat.modules.catalog.ui.CatalogV2UIFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20 May 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CatalogLayoutController extends FormBasicController {
	
	private StaticTextElement titleEl;
	private FormLink titleLink;
	private FileElement headerBgImageEl;
	private SingleSelection launcherTaxonomyLevelStyleEl;
	
	private CloseableModalController cmc;
	private SingleKeyTranslatorController titleTranslatorCtrl;
	
	@Autowired
	private CatalogV2Module catalogModule;

	public CatalogLayoutController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, Util.createPackageTranslator(CatalogV2UIFactory.class, ureq.getLocale()));
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("admin.layout");
		
		FormLayoutContainer nameCont = FormLayoutContainer.createButtonLayout("nameCont", getTranslator());
		nameCont.setLabel("admin.header.search.title", null);
		nameCont.setElementCssClass("o_inline_cont");
		nameCont.setRootForm(mainForm);
		formLayout.add(nameCont);
		
		String translateLauncherName = translate("header.search.title");
		titleEl = uifactory.addStaticTextElement("admin.header.search.title", null, translateLauncherName, nameCont);
		
		titleLink = uifactory.addFormLink("admin.header.search.title.edit", nameCont);
		
		headerBgImageEl = uifactory.addFileElement(getWindowControl(), getIdentity(), "admin.header.bg.image", "admin.header.bg.image", formLayout);
		headerBgImageEl.setExampleKey("admin.header.bg.image.example", null);
		headerBgImageEl.limitToMimeType(HEADER_BG_IMAGE_MIME_TYPES, "error.mimetype", null);
		headerBgImageEl.setMaxUploadSizeKB(2048, null, null);
		headerBgImageEl.setPreview(ureq.getUserSession(), true);
		headerBgImageEl.setDeleteEnabled(true);
		headerBgImageEl.addActionListener(FormEvent.ONCHANGE);
		if (catalogModule.hasHeaderBgImage()) {
			headerBgImageEl.setPreview(ureq.getUserSession(), true);
			headerBgImageEl.setInitialFile(catalogModule.getHeaderBgImage());
		}
		
		SelectionValues styleSV = new SelectionValues();
		styleSV.add(entry(TAXONOMY_LEVEL_LAUNCHER_STYLE_RECTANGLE, translate("admin.launcher.taxonomy.level.rectangle")));
		styleSV.add(entry(TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE, translate("admin.launcher.taxonomy.level.square")));
		launcherTaxonomyLevelStyleEl = uifactory.addRadiosVertical("admin.launcher.taxonomy.level.style", formLayout, styleSV.keys(), styleSV.values());
		launcherTaxonomyLevelStyleEl.addActionListener(FormEvent.ONCHANGE);
		if (TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE.equals(catalogModule.getLauncherTaxonomyLevelStyle())) {
			launcherTaxonomyLevelStyleEl.select(TAXONOMY_LEVEL_LAUNCHER_STYLE_SQUARE, true);
		} else {
			launcherTaxonomyLevelStyleEl.select(TAXONOMY_LEVEL_LAUNCHER_STYLE_RECTANGLE, true);
		}
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == titleLink) {
			doLauncherName(ureq);
		} else if (headerBgImageEl == source) {
			if (event instanceof FileElementEvent) {
				if (FileElementEvent.DELETE.equals(event.getCommand())) {
					catalogModule.deleteHeaderBgImage();
					headerBgImageEl.setInitialFile(null);
					if (headerBgImageEl.getUploadFile() != null) {
						headerBgImageEl.reset();
					}
				}
			} else if (headerBgImageEl.isUploadSuccess()) {
				headerBgImageEl.clearError();
				List<ValidationStatus> validationResults = new ArrayList<>();
				headerBgImageEl.validate(validationResults);
				if (validationResults.isEmpty()) {
					catalogModule.deleteHeaderBgImage();
					File headerBgImage = headerBgImageEl.moveUploadFileTo(catalogModule.getHeaderBgDirectory());
					catalogModule.setHeaderBgImageFilename(headerBgImage.getName());
					headerBgImageEl.setInitialFile(catalogModule.getHeaderBgImage());
				} else {
					headerBgImageEl.reset();
				}
			}
		} else if (launcherTaxonomyLevelStyleEl == source) {
			catalogModule.setLauncherTaxonomyLevelStyle(launcherTaxonomyLevelStyleEl.getSelectedKey());
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == titleTranslatorCtrl) {
			titleEl.setValue(translate("header.search.title"));
			cmc.deactivate();
			cleanUp();
		} else if (cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(titleTranslatorCtrl);
		removeAsListenerAndDispose(cmc);
		titleTranslatorCtrl = null;
		cmc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doLauncherName(UserRequest ureq) {
		if (guardModalController(titleTranslatorCtrl)) return;
		
		titleTranslatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), "header.search.title", CatalogV2UIFactory.class);
		listenTo(titleTranslatorCtrl);

		removeAsListenerAndDispose(cmc);
		cmc = new CloseableModalController(getWindowControl(), "close", titleTranslatorCtrl.getInitialComponent(), true,
				translate("admin.header.search.title"));
		listenTo(cmc);
		cmc.activate();
	}

}
