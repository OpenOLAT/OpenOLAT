/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.  
* <p>
*/
package org.olat.core.gui.components.form.flexible.impl.elements;

import java.util.Date;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.control.JSAndCSSAdder;
import org.olat.core.gui.render.ValidationResult;
import org.olat.core.gui.translator.Translator;

/**
 * <P>
 * Initial Date: 19.01.2007 <br>
 * 
 * @author patrickb
 */
class JSDateChooserComponent extends FormBaseComponentImpl {
	
	private static final ComponentRenderer RENDERER = new JSDateChooserRenderer();
	private JSDateChooser element;

	public JSDateChooserComponent(JSDateChooser element) {
		super(element.getName());
		this.element = element;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}
	
	/**
	 * @see org.olat.core.gui.components.Component#getDispatchID()
	 * without this: the events are sent from the text-component, but the id will be the one from the datechooser-span
	 * so the component cant be found while dispatching. and therefore eventhandling won't work.
	 * by using the id from the text-component, it should work as expected.
	 * See OLAT-4735.
	 */
	@Override
	public String getDispatchID() {
		return element.getTextElementComponent().getDispatchID();
	}

	@Override
	public void validate(UserRequest ureq, ValidationResult vr) {
		super.validate(ureq, vr);
		
		JSAndCSSAdder jsa = vr.getJsAndCSSAdder();
		jsa.addRequiredStaticJsFile("js/jquery/ui/jquery-ui-1.11.4.custom.datepicker.min.js");
	}

	public Date getDate() {
		return element.getDate();
	}
	
	public Date getSecondDate() {
		return element.getSecondDate();
	}

	public String getValue() {
		return element.getValue();
	}
	
	public String getSecondValue() {
		return element.getSecondValue();
	}
	
	public TextElementComponent getTextElementComponent() {
		return element.getTextElementComponent();
	}

	public String getDateChooserDateFormat() {
		return element.getDateChooserDateFormat();
	}
	
	public boolean isTimeOnlyEnabled() {
		return element.isTimeOnly();
	}

	public boolean isDateChooserTimeEnabled() {
		return element.isDateChooserTimeEnabled();
	}
	
	public boolean isDefaultTimeAtEndOfDay() {
		return element.isDefaultTimeAtEndOfDay();
	}
	
	public boolean isButtonsEnabled() {
		return element.isButtonsEnabled();
	}
	
	public boolean isSecondDate() {
		return element.isSecondDate();
	}

	public boolean isSameDay() {
		return element.isSameDay();
	}

	public Translator getElementTranslator() {
		return element.getTranslator();
	}
	
	public String getSeparator() {
		return element.getSeparator();
	}

	public String getExampleDateString() {
		return element.getExampleDateString();
	}
	
	public JSDateChooser getFormItem() {
		return element;
	}
}
