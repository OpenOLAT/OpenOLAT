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
* Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <p>
*/
package org.olat.core.gui.components.form.flexible.impl.elements;

import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.translator.Translator;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SingleSelectionElementComponent
 * 
 * <P>
 * Initial Date:  31.12.2006 <br>
 * @author patrickb
 */
class RadioElementComponent extends FormBaseComponentImpl {

	private static final ComponentRenderer RENDERER = new RadioElementRenderer();
	private SingleSelection selectionWrapper;
	private int which;

	public RadioElementComponent(String name, Translator translator, SingleSelection selectionWrapper, int which) {
		super(name, translator);
		this.selectionWrapper = selectionWrapper;
		this.which = which;
	}

	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	String getGroupingName(){
		return selectionWrapper.getName();
	}
	
	int getWhichWeAre(){
		return which;
	}

	String getKey() {
		return selectionWrapper.getKey(which);
	}

	public String getValue() {
		return selectionWrapper.getValue(which);
	}

	public boolean isSelected() {
		return selectionWrapper.isSelected(which);
	}
	
	public int getAction(){
		return selectionWrapper.getAction();
	}
	
	public Form getRootForm(){
		return selectionWrapper.getRootForm();
	}
	
	public String getSelectionElementFormDisId(){
		return selectionWrapper.getFormDispatchId();
	}
	
}
