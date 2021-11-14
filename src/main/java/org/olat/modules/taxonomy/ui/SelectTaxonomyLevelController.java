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
package org.olat.modules.taxonomy.ui;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyAllTreesBuilder;

/**
 * 
 * Initial date: 27 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class SelectTaxonomyLevelController extends FormBasicController {
	
	private GenericTreeModel taxonomyTreesModel;
	
	private MenuTreeItem taxonomyTreesEl;
	
	private final TaxonomyCompetenceTypes competenceType;
	
	public SelectTaxonomyLevelController(UserRequest ureq, WindowControl wControl, TaxonomyCompetenceTypes competenceType) {
		super(ureq, wControl, "select_level");
		this.competenceType = competenceType;
		
		initForm(ureq);
		loadModel();
	}
	public TaxonomyCompetenceTypes getCompetenceType() {
		return competenceType;
	}
	
	public TaxonomyLevel getSelectedTaxonomyLevel() {
		TreeNode selectedNode = taxonomyTreesEl.getSelectedNode();
		if(selectedNode instanceof GenericTreeNode) {
			GenericTreeNode selectedTaxonomyTreeNode = (GenericTreeNode)selectedNode;
			if(selectedTaxonomyTreeNode.getUserObject() instanceof TaxonomyLevel) {
				return (TaxonomyLevel)selectedTaxonomyTreeNode.getUserObject();
			}
		}
		return null;		
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		taxonomyTreesModel = new GenericTreeModel();
		taxonomyTreesEl = uifactory.addTreeMultiselect("taxonomy", null, formLayout, taxonomyTreesModel, this);
		taxonomyTreesEl.setRootVisible(false);

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("ok", formLayout);
	}
	
	private void loadModel() {
		new TaxonomyAllTreesBuilder().loadTreeModel(taxonomyTreesModel);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
}
