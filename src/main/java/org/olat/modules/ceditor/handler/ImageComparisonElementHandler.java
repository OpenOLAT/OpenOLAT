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
import org.olat.modules.ceditor.model.ImageComparisonElement;
import org.olat.modules.ceditor.model.ImageComparisonOrientation;
import org.olat.modules.ceditor.model.ImageComparisonSettings;
import org.olat.modules.ceditor.model.ImageComparisonType;
import org.olat.modules.ceditor.model.jpa.ImageComparisonPart;
import org.olat.modules.ceditor.ui.ImageComparisonInspectorController;
import org.olat.modules.ceditor.ui.ImageComparisonRunController;
import org.olat.modules.ceditor.ui.PageEditorV2Controller;

/**
 * Initial date: 2024-05-15<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ImageComparisonElementHandler implements PageElementHandler, PageElementStore<ImageComparisonElement>, SimpleAddPageElementHandler, ComponentEventListener {
	@Override
	public String getType() {
		return "imagecomparison";
	}

	@Override
	public String getIconCssClass() {
		return "o_icon_image_comparison";
	}

	@Override
	public PageElementCategory getCategory() {
		return PageElementCategory.content;
	}

	@Override
	public PageRunElement getContent(UserRequest ureq, WindowControl wControl, PageElement element, RenderingHints options) {
		if (element instanceof ImageComparisonPart imageComparisonPart) {
			return new ImageComparisonRunController(ureq, wControl, imageComparisonPart, options.isEditable());
		}
		return null;
	}

	@Override
	public Controller getEditor(UserRequest ureq, WindowControl wControl, PageElement element) {
		return null;
	}

	@Override
	public PageElementInspectorController getInspector(UserRequest ureq, WindowControl wControl, PageElement element) {
		if (element instanceof ImageComparisonPart imageComparisonPart) {
			return new ImageComparisonInspectorController(ureq, wControl, imageComparisonPart, this);
		}
		return null;
	}

	@Override
	public PageElement createPageElement(Locale locale) {
		Translator translator = Util.createPackageTranslator(PageEditorV2Controller.class, locale);
		ImageComparisonPart imageComparisonPart = new ImageComparisonPart();
		ImageComparisonSettings imageComparisonSettings = imageComparisonPart.getSettings();
		imageComparisonSettings.setOrientation(ImageComparisonOrientation.horizontal);
		imageComparisonSettings.setType(ImageComparisonType.standard);
		imageComparisonPart.setSettings(imageComparisonSettings);
		return imageComparisonPart;
	}

	@Override
	public ImageComparisonElement savePageElement(ImageComparisonElement element) {
		return CoreSpringFactory.getImpl(PageService.class).updatePart((ImageComparisonPart) element);
	}

	@Override
	public void dispatchEvent(UserRequest ureq, Component source, Event event) {
	}
}
