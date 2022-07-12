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
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.InsertionPoint.Position;
import org.olat.core.gui.components.tree.InsertionTreeModel;
import org.olat.core.gui.components.tree.MenuTree;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.manager.TaxonomyAllTreesBuilder;

/**
 * 
 * Initial date: 26 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class TaxonomyTreeController extends BasicController {
	
	private final MenuTree taxonomiesTree; 
	private final TaxonomyTreesModel taxonomyModel;

	public TaxonomyTreeController(UserRequest ureq, WindowControl wControl, Taxonomy taxonomy) {
		super(ureq, wControl);

		VelocityContainer mainVC = createVelocityContainer("admin_trees");

		taxonomiesTree = new MenuTree("taxonomyTree");
		taxonomyModel = new TaxonomyTreesModel();
		taxonomiesTree.setTreeModel(taxonomyModel);
		taxonomiesTree.setSelectedNode(taxonomiesTree.getTreeModel().getRootNode());
		taxonomiesTree.setDropEnabled(false);
		taxonomiesTree.setRootVisible(false);
		taxonomiesTree.addListener(this);
		new TaxonomyAllTreesBuilder(getLocale()).loadTreeModel(taxonomyModel, taxonomy);

		mainVC.put("trees", taxonomiesTree);
		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	public class TaxonomyTreesModel extends GenericTreeModel implements InsertionTreeModel {

		private static final long serialVersionUID = -3141702425361197931L;
		
		@Override
		public boolean isSource(TreeNode node) {
			return false;
		}

		@Override
		public Position[] getInsertionPosition(TreeNode node) {
			Position[] positions;
			if(node.getUserObject() instanceof TaxonomyLevel) {
				positions = new Position[] { Position.down, Position.under };
			} else if(node.getUserObject() instanceof Taxonomy) {
				positions = new Position[] { Position.under };
			} else {
				positions = new Position[0];
			}
			return positions;
		}
	}
}