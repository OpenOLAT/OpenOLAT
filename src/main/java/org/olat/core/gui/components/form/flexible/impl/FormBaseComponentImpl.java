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
package org.olat.core.gui.components.form.flexible.impl;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.AbstractComponent;
import org.olat.core.gui.components.form.flexible.FormBaseComponent;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.AssertException;

/**
 * Description:<br>
 * provides a html valid form dispatch id needed in the "form component" renderers.
 * This is then the id which can be found in the hidden field for submit info.  
 * 
 * <P>
 * Initial Date:  11.01.2007 <br>
 * @author patrickb
 */
public abstract class FormBaseComponentImpl extends AbstractComponent implements FormBaseComponent {
	
	private String layout;

	public FormBaseComponentImpl(String name) {
		super(name);
	}
	
	public FormBaseComponentImpl(String id, String name) {
		super(id, name);
	}
	
	public FormBaseComponentImpl(String name, Translator translator) {
		super(name, translator);
	}
	
	public FormBaseComponentImpl(String id, String name, Translator translator) {
		super(id, name, translator);
	}

	@Override
	public String getLayout() {
		return layout;
	}

	@Override
	public void setLayout(String layout) {
		this.layout = layout;
	}

	@Override
	public String getFormDispatchId(){		
		return DISPPREFIX.concat(super.getDispatchID());
	}

	@Override
	protected void doDispatchRequest(UserRequest ureq) {
		//Form elements must render themselves in a way that the dispatching
		//is done by the form manager receiving the dispatch request with the 
		//form elements dispatch id.
		throw new AssertException("The form element <"+getComponentName()+"> with id <"+getFormDispatchId()+"> should not be dispatched by GUI framework:");
	}
}
