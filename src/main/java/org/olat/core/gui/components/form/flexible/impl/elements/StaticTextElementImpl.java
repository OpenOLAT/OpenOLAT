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

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;
import org.olat.core.gui.render.DomWrapperElement;

/**
 * Initial Date:  02.02.2007 <br>
 * @author patrickb
 */
public class StaticTextElementImpl extends FormItemImpl implements StaticTextElement {

	private String value;
	private final StaticTextElementComponent component;
	private DomWrapperElement domWrapperElement = DomWrapperElement.p;
	
	public StaticTextElementImpl(String name, String value) {
		super(name);
		this.value = value;
		component = new StaticTextElementComponent(this);
	}

	@Override
	public void evalFormRequest(UserRequest ureq) {
		// static text must not evaluate
	}

	@Override
	public void reset() {
		// static text can not be resetted
	}

	@Override
	public String getForId() {
		return null;//text is not a form control
	}

	@Override
	protected Component getFormItemComponent() {
		return component;
	}

	@Override
	public void setElementCssClass(String elementCssClass) {
		super.setElementCssClass(elementCssClass);
		component.setElementCssClass(elementCssClass);
	}

	@Override
	protected void rootFormAvailable() {
		//root form not interesting for Static text
	}

	@Override
	public String getValue() {
		return value;
	}

	@Override
	public void setValue(String replacementValue) {
		value = replacementValue;
		getFormItemComponent().setDirty(true);
	}
	
	@Override
	public DomWrapperElement getDomWrapperElement() {
		return domWrapperElement;
	}

	@Override
	public void setDomWrapperElement(DomWrapperElement domWrapperElement) {
		this.domWrapperElement = domWrapperElement;
	}

}
