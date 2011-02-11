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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 1999-2006 at Multimedia- & E-Learning Services (MELS),<br>
 * University of Zurich, Switzerland.
 * <p>
 */
package org.olat.core.gui.components.form.flexible.impl.elements.table;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElment;
import org.olat.core.gui.components.form.flexible.impl.FormItemImpl;


/**
 * 
 * @author Christian Guretzki
 */
public class FlexiTableElementImpl extends FormItemImpl implements FlexiTableElment {
  
	private FlexiTableDataModel tableModel;
	private FlexiTableComponent component;
	
	
	public FlexiTableElementImpl(String name, FlexiTableDataModel tableModel) {
		super(name);
		this.tableModel = tableModel;
		component = new FlexiTableComponent(this);
	}
	
	
	/**
	 * @see org.olat.core.gui.components.form.flexible.FormItemImpl#evalFormRequest(org.olat.core.gui.UserRequest)
	 */
	@Override
	public void evalFormRequest(UserRequest ureq) {
		String paramId = String.valueOf(component.getFormDispatchId());
		String value = getRootForm().getRequestParameter(paramId);
		if (value != null) {
			//TODO:cg:XXX do something with value TextElement e.g. setValue(value);
			// mark associated component dirty, that it gets rerendered
			component.setDirty(true);
		}
	}


	@Override
	@SuppressWarnings("unused")
	public void validate(List validationResults) {
		//static text must not validate
	}

	@Override
	public void reset() {
		// static text can not be resetted
	};
	
	@Override
	protected void rootFormAvailable() {
		//root form not interesting for Static text
	}

	protected Component getFormItemComponent() {
		return component;
	}
	
	public FlexiTableDataModel getTableDataModel() {
		return tableModel;
	}

}
