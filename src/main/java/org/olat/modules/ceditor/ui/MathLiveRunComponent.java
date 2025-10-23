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
package org.olat.modules.ceditor.ui;

import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.math.MathLiveComponent;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.text.TextFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.render.DomWrapperElement;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.modules.ceditor.PageElement;
import org.olat.modules.ceditor.PageElementEditorController;
import org.olat.modules.ceditor.model.MathElement;
import org.olat.modules.ceditor.model.MathSettings;
import org.olat.modules.ceditor.model.jpa.MathPart;
import org.olat.modules.ceditor.ui.event.ChangePartEvent;

/**
 * 
 * Initial date: 8 sept. 2022<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MathLiveRunComponent extends PageRunComponent {

	private static final AtomicInteger idGenerator = new AtomicInteger();

	public MathLiveRunComponent(Component component) {
		super(component);
	}
	
	@Override
	public void dispatchEvent(UserRequest ureq, Controller source, Event event) {
		if ((source instanceof ModalInspectorController || source instanceof PageElementEditorController)
				&& event instanceof ChangePartEvent changePartEvent) {
			PageElement element = changePartEvent.getElement();
			if (element instanceof MathElement mathElement) {
				if (getComponent() instanceof MathLiveComponent mathLiveComponent) {
					if (StringHelper.containsNonWhitespace(mathElement.getContent())) {
						mathLiveComponent.setValue(mathElement.getContent());
						mathLiveComponent.setOuterWrapperClass(MathElement.toCssClass(mathElement.getMathSettings()));
					} else {
						setComponent(getPlaceholderComponent(mathElement, ureq.getLocale()));
					}
				}
				if (getComponent() instanceof TextComponent) {
					if (StringHelper.containsNonWhitespace(mathElement.getContent())) {
						setComponent(getReadOnlyComponent(mathElement));
					}
				}
			}
		}
	}
	
	public static Component getEditableComponent(MathPart mathPart, Locale locale) {
		if (StringHelper.containsNonWhitespace(mathPart.getContent())) {
			return getReadOnlyComponent(mathPart);
		} else {
			return getPlaceholderComponent(mathPart, locale);
		}
	}
	
	public static MathLiveComponent getReadOnlyComponent(MathElement mathElement) {
		MathLiveComponent cmp = new MathLiveComponent("mathCmp" + CodeHelper.getRAMUniqueID());
		String outerWrapperClass = MathElement.toCssClass(mathElement.getMathSettings());
		cmp.setOuterWrapperClass(outerWrapperClass);
		cmp.setDomWrapperElement(DomWrapperElement.span);
		cmp.setValue(mathElement.getContent());
		cmp.setElementCssClass("o_ce_math");
		return cmp;
	}
	
	public static TextComponent getPlaceholderComponent(MathElement mathElement, Locale locale) {
		String placeholder = Util.createPackageTranslator(TextRunComponent.class, locale).translate("math.placeholder");
		String htmlContent = MathElement.toHtmlPlaceholder(placeholder);
		MathSettings mathSettings = mathElement.getMathSettings();
		String cssClass = MathElement.toCssClassWithMarkerClass(mathSettings);
		return TextFactory.createTextComponentFromString("math_" + idGenerator.incrementAndGet(),
				htmlContent, cssClass, false, null);
	}
}
