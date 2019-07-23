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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.components.form.flexible.elements.MultiSelectionTree;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.util.tree.INodeFilter;
import org.olat.core.util.tree.TreeHelper;

/**
 * Description:<br>
 * TODO: patrickb Class Description for SelectionTree
 * 
 * <P>
 * Initial Date: 21.01.2008 <br>
 * 
 * @author patrickb
 */
public class MultiSelectionTreeImpl extends MultipleSelectionElementImpl implements MultiSelectionTree {
	
	private INodeFilter selectableFilter;
	private TreeModel treemodel;
	
	public MultiSelectionTreeImpl(String name, TreeModel treemodel, INodeFilter selectableFilter) {
		super(name);
		this.selectableFilter = selectableFilter;
		this.treemodel = treemodel;
		initKeyValuePairsFromTreeModel();
		component = new SelectionTreeComponent(name, this, treemodel, selectableFilter);
	}

	private void initKeyValuePairsFromTreeModel() {
		List<TreeNode> flatModel = new ArrayList<>();
		TreeHelper.makeTreeFlat(treemodel.getRootNode(), flatModel);
		keys = new String[flatModel.size()];
		values = new String[keys.length];
		int i = 0;
		for (Iterator<TreeNode> iterator = flatModel.iterator(); iterator.hasNext();) {
			TreeNode treeNode = iterator.next();
			keys[i] = treeNode.getIdent();
			values[i] = treeNode.getTitle();
			i++;
		}
	}

	@Override
	protected void initSelectionElements() {
		boolean createValues = (values == null) || (values.length == 0);
		if (createValues) {
			values = new String[keys.length];
			for (int i = 0; i < keys.length; i++) {
				values[i] = translator.translate(keys[i]);
			}
		}
		// keys,values initialized
		// create and add radio elements
		Map<String, CheckboxElement> checkboxitems = new HashMap<>(); 
		for (int i = 0; i < keys.length; i++) {
			TreeNode tn = treemodel.getNodeById(keys[i]);
			if(selectableFilter.isVisible(tn)) {
				// apply css class of tree node to checkbox label wrapper as well
				String cssClass = tn.getCssClass();
				String checkName = getName() + "_" + keys[i];
				CheckboxElement ssec = new CheckboxElement(checkName, this, i, null, cssClass);
				//ssec.setEnabled(selectableFilter.isSelectable(tn));
				checkboxitems.put(keys[i], ssec);
			}
		}
		((SelectionTreeComponent)component).setComponents(checkboxitems);
	}

	/*
	 * Methods below are needed to set the correct component dirty because this
	 * weired construct in this class otherwhise sets an invisible component
	 * (MultipleSelectoinElementImpl) dirty
	 */
	
	@Override
	public void reset() {
		super.reset();
		initKeyValuePairsFromTreeModel();
		initSelectionElements();
		component.setDirty(true);
	}

	@Override
	public void select(String key, boolean select) {
		super.select(key, select);
		// set menu tree dirty to render new values
		component.setDirty(true);
	}

	@Override
	public void selectAll() {
		super.selectAll();
		// set menu tree dirty to render new values
		component.setDirty(true);
	}

	@Override
	public void setEnabled(boolean isEnabled) {
		super.setEnabled(isEnabled);
		// set menu tree dirty to render new values
		component.setDirty(true);
	}

	@Override
	public void setKeysAndValues(String[] keys, String[] values, String[] cssClasses, String[] iconLeftCSS) {
		super.setKeysAndValues(keys, values, cssClasses, iconLeftCSS);
		// set menu tree dirty to render new values
		component.setDirty(true);
	}

	@Override
	public void setSelectedValues(String[] values) {
		super.setSelectedValues(values);
		// set menu tree dirty to render new values
		component.setDirty(true);
	}

	@Override
	public void setVisible(boolean isVisible) {
		super.setVisible(isVisible);
		// set menu tree dirty to render new values
		component.setDirty(true);
	}

	@Override
	public void uncheckAll() {
		super.uncheckAll();
		// set menu tree dirty to render new values
		component.setDirty(true);
	}
	
}
