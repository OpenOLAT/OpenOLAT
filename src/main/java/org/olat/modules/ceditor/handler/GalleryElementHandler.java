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
package org.olat.modules.ceditor.handler;

import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentEventListener;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementCategory;
import org.olat.modules.ceditor.PageElementHandler;
import org.olat.modules.ceditor.PageElementInspectorController;
import org.olat.modules.ceditor.PageElementStore;
import org.olat.modules.ceditor.PageRunElement;
import org.olat.modules.ceditor.PageService;
import org.olat.modules.ceditor.RenderingHints;
import org.olat.modules.ceditor.SimpleAddPageElementHandler;
import org.olat.modules.ceditor.model.GalleryElement;
import org.olat.modules.ceditor.model.GallerySettings;
import org.olat.modules.ceditor.model.GalleryType;
import org.olat.modules.ceditor.model.jpa.GalleryPart;
import org.olat.modules.ceditor.ui.GalleryEditorController;
import org.olat.modules.ceditor.ui.GalleryInspectorController;
import org.olat.modules.ceditor.ui.GalleryRunController;
import org.olat.modules.ceditor.ui.PageEditorV2Controller;

/**
 * Initial date: 2024-04-18<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class GalleryElementHandler implements PageElementHandler, PageElementStore<GalleryElement>, SimpleAddPageElementHandler, ComponentEventListener {

	@Override
	public String getType() {
		return "gallery";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_images";
	}

	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.media;
	}

	@Override
	public int getSortOrder() {
		return 20;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints options) {
		if (element instanceof GalleryPart galleryPart) {
			return new GalleryRunController(ureq, wControl, galleryPart, options.isEditable());
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof GalleryPart galleryPart) {
			return new GalleryEditorController(ureq, wControl, galleryPart, this);
		}
		return null;
	}

	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof GalleryPart galleryPart) {
			return new GalleryInspectorController(ureq, wControl, galleryPart, this);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		Translator translator = Util.createPackageTranslator(PageEditorV2Controller.class, locale);
		GalleryPart galleryPart = new GalleryPart();
		GallerySettings gallerySettings = galleryPart.getSettings();
		gallerySettings.setTitle(translator.translate("gallery.title.default"));
		gallerySettings.setType(GalleryType.slideshow);
		gallerySettings.setColumns(3);
		gallerySettings.setRows(2);
		galleryPart.setSettings(gallerySettings);
		return galleryPart;
	}

	@Override
	public GalleryElement savePageElement(GalleryElement element) {
		return CoreSpringFactory.getImpl(PageService.class).updatePart((GalleryPart) element);
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
	}
}
