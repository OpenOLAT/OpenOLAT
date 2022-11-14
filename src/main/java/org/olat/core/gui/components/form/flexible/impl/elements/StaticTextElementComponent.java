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
*/
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.StaticTextElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.render.DomWrapperElement;

class StaticTextElementComponent extends FormBaseComponentImpl {

	private static final ComponentRenderer RENDERER = new StaticTextElementRenderer();
	private StaticTextElement wrapper;

	public StaticTextElementComponent(StaticTextElement element) {
		super(element.getName());
		this.wrapper = element;
	}
	
	public String getValue(){
		return wrapper.getValue();
	}
	
	@Override
	public FormItem getFormItem() {
		return wrapper;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	public Form getRootForm() {
		return wrapper.getRootForm();
	}

	public int getAction() {
		return wrapper.getAction();
	}
	
	/**
	 * The DOM element type that is used to wrap this StaticTextElementComponent
	 * DomWrapperElement.p is the default setting
	 * @return
	 */
	public DomWrapperElement getDomWrapperElement() {
		return wrapper.getDomWrapperElement();
	}

}
