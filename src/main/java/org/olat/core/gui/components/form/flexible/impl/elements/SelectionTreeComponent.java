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

import java.util.Map;

import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.ComponentRenderer;
import org.olat.core.gui.components.form.flexible.elements.SelectionElement;
import org.olat.core.gui.components.form.flexible.impl.FormBaseComponentImpl;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.translator.Translator;
import org.olat.core.util.tree.INodeFilter;

/**
 * @author patrickb
 *
 */
class SelectionTreeComponent extends FormBaseComponentImpl {

	private SelectionElement selectionElement;
	private final TreeModel treeModel;
	private final INodeFilter selectableFilter;
	private Map<String, Component> subComponents;
	private final static ComponentRenderer RENDERER = new SelectionTreeComponentRenderer(); 

	/**
	 * @param name
	 */
	public SelectionTreeComponent(String name, Translator translator, SelectionElement selectionElement,
			TreeModel treeModel, INodeFilter selectableFilter) {
		super(name, translator);
		this.selectionElement = selectionElement;
		this.treeModel = treeModel;
		this.selectableFilter = selectableFilter;
	}

	/* (non-Javadoc)
	 * @see org.olat.core.gui.components.Component#getHTMLRendererSingleton()
	 */
	@Override
	public ComponentRenderer getHTMLRendererSingleton() {
		return RENDERER;
	}

	SelectionElement getSelectionElement() {
		return selectionElement;
	}

	TreeModel getTreeModel() {
		return treeModel;
	}

	INodeFilter getSelectableFilter() {
		return selectableFilter;
	}

	/**
	 * @param checkboxitems
	 */
	protected void setComponents(Map<String, Component> checkboxitems) {
		this.subComponents = checkboxitems;
	}

	protected Map<String, Component> getSubComponents(){
		return this.subComponents;
	}
	
}
