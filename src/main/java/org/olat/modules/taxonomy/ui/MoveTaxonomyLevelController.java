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
import org.olat.core.gui.components.tree.MenuTreeItem;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.util.nodes.INode;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyAllTreesBuilder;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MoveTaxonomyLevelController extends FormBasicController {
	
	private MenuTreeItem taxonomyEl;
	private GenericTreeModel taxonomyModel;
	
	private final Taxonomy taxonomy;
	private TaxonomyLevel levelToMove;
	private TaxonomyLevel movedLevel;
	
	@Autowired
	private TaxonomyService taxonomyService;
	
	public MoveTaxonomyLevelController(UserRequest ureq, WindowControl wControl,
			Taxonomy taxonomy, TaxonomyLevel levelToMove) {
		super(ureq, wControl, "move_taxonomy_level");
		this.taxonomy = taxonomy;
		this.levelToMove = levelToMove;
		initForm(ureq);
		loadModel();
	}
	
	public TaxonomyLevel getMovedTaxonomyLevel() {
		return movedLevel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		taxonomyModel = new GenericTreeModel();
		taxonomyEl = uifactory.addTreeMultiselect("taxonomy", null, formLayout, taxonomyModel, this);
		taxonomyEl.setMultiSelect(false);
		taxonomyEl.setRootVisible(true);

		uifactory.addFormCancelButton("cancel", formLayout, ureq, getWindowControl());
		uifactory.addFormSubmitButton("move.taxonomy.level", formLayout);
	}
	
	private void loadModel() {
		new TaxonomyAllTreesBuilder().loadTreeModel(taxonomyModel, taxonomy);
		TreeNode nodeToMove = taxonomyModel
				.getNodeById(TaxonomyAllTreesBuilder.nodeKey(levelToMove));
		nodeToMove.removeAllChildren();
		TaxonomyAllTreesBuilder.sort(taxonomyModel.getRootNode());
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		if(taxonomyEl.getSelectedNode() == null) {
			taxonomyEl.setErrorKey("error.select.target.level", null);
			allOk &= false;
		} else if(isParent()) {
			taxonomyEl.setErrorKey("error.target.no.parent", null);
			allOk &= false;
		}

		return allOk;
	}
	
	private boolean isParent() {
		TreeNode nodeToMove = taxonomyModel
				.getNodeById(TaxonomyAllTreesBuilder.nodeKey(levelToMove));
		TreeNode selectedNode = taxonomyEl.getSelectedNode();
		if(selectedNode == taxonomyModel.getRootNode()) {
			return false;//can move to root
		}
		for(INode node=nodeToMove; node != null; node = node.getParent()) {
			if(selectedNode == node) {
				return true;
			}
		}
		return false;
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(isParent()) {
			showWarning("error.target.no.parent");
		} else {
			TreeNode selectedNode = taxonomyEl.getSelectedNode();
			if(selectedNode == taxonomyModel.getRootNode()) {
				movedLevel = taxonomyService.moveTaxonomyLevel(levelToMove, null);
			} else {
				TaxonomyLevel newParentLevel = (TaxonomyLevel)selectedNode.getUserObject();
				movedLevel = taxonomyService.moveTaxonomyLevel(levelToMove, newParentLevel);
			}
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}
}
