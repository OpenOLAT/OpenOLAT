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
package org.olat.modules.qpool.ui.tree;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Locale;

import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.tree.DnDTreeModel;
import org.olat.core.gui.components.tree.GenericTreeModel;
import org.olat.core.gui.components.tree.GenericTreeNode;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.group.BusinessGroup;
import org.olat.modules.qpool.Pool;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItemCollection;
import org.olat.modules.qpool.QuestionStatus;
import org.olat.modules.qpool.ui.QuestionPoolMainEditorController;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;

/**
 * 
 * Initial date: 15.02.2013<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QuestionPoolMenuTreeModel extends GenericTreeModel implements DnDTreeModel {

	private static final long serialVersionUID = -665560407090871912L;

	private final TooledStackedPanel stackPanel;
	private final Identity identity;
	private final Roles roles;
	private final QPoolService qpoolService;
	private final Translator translator;
	
	private TreeNode myNode;
	private TreeNode myQuestionsNode;
	private GenericTreeNode reviewNode;
	private GenericTreeNode finalNode;
	private TreeNode sharesNode;
	
	public QuestionPoolMenuTreeModel(TooledStackedPanel stackPanel, Identity identity, Roles roles, Locale locale) {
		this(	stackPanel,
				identity,
				roles,
				Util.createPackageTranslator(QuestionPoolMainEditorController.class, locale),
				CoreSpringFactory.getImpl(QPoolService.class)
				);
	}
	
	public QuestionPoolMenuTreeModel(TooledStackedPanel stackPanel, Identity identity, Roles roles, Translator translator,
			QPoolService qpoolService) {
		super();
		this.stackPanel = stackPanel;
		this.identity = identity;
		this.roles = roles;
		this.translator = translator;
		this.qpoolService = qpoolService;
		buildTreeModel();
	}

	public TreeNode getMyNode() {
		return myNode;
	}

	public TreeNode getMyQuestionsNode() {
		return myQuestionsNode;
	}

	public TreeNode getSharesNode() {
		return sharesNode;
	}
	
	public Collection<String> getDefaultOpenNodeIds() {
		Collection<String> openNodeIds = new ArrayList<>(4);
		if (myNode != null) {
			openNodeIds.add(myNode.getIdent());
		}
		if (reviewNode != null) {
			openNodeIds.add(reviewNode.getIdent());
		}
		if (finalNode != null) {
			openNodeIds.add(finalNode.getIdent());
		}
		if (sharesNode != null) {
			openNodeIds.add(sharesNode.getIdent());
		}
		return openNodeIds;
	}

	@Override
	public boolean isNodeDroppable(TreeNode node) {
		if (node instanceof MyQuestionsTreeNode
				|| node instanceof MarkedQuestionsTreeNode
				|| node instanceof CollectionTreeNode
				|| node instanceof PoolTreeNode
				|| node instanceof BusinessGroupTreeNode) {
			return true;
		}
		return false;
	}

	@Override
	public boolean isNodeDraggable(TreeNode node) {
		return false;
	}
	
	private void buildTreeModel() {
		TreeNode rootNode = new GenericTreeNode(translator.translate("topnav.qpool"));
		setRootNode(rootNode);
		
		//question database
		myNode = new PresentationTreeNode(translator.translate("menu.database"));
		rootNode.addChild(myNode);
		buildMySubTreeModel();
		
		//review process
		buildReviewSubTreeModel(rootNode);
		buildFinalSubTreeModel(rootNode);
		
		//pools + shares
		sharesNode = new SharePresentationTreeNode(translator.translate("menu.share"));
		rootNode.addChild(sharesNode);	
		buildShareSubTreeModel();
		
		//administration
		if(roles.isOLATAdmin() || roles.isPoolAdmin()) {
			TreeNode adminNode = new QuestionPoolAdminStatisticsTreeNode(translator.translate("menu.admin"));
			rootNode.addChild(adminNode);
			buildAdminSubTreeModel(adminNode);
		}
	}
	
	public void buildMySubTreeModel() {
		myNode.removeAllChildren();
		buildMyTreeNode(myNode);
		buildMarkedTreeNode(myNode);
		buildMyTaxonomyNodes(myNode);
		buildCollectionTreeNodes(myNode);
	}

	private void buildMyTreeNode(TreeNode parentNode) {
		myQuestionsNode = new MyQuestionsTreeNode(stackPanel, translator.translate("menu.database.my"));
		parentNode.addChild(myQuestionsNode);
	}

	private void buildMarkedTreeNode(TreeNode parentNode) {
		TreeNode node = new MarkedQuestionsTreeNode(stackPanel, translator.translate("menu.database.favorit"));
		parentNode.addChild(node);
	}
	
	private void buildMyTaxonomyNodes(TreeNode parentNode) {
		List<TaxonomyLevel> taxonomyLevels = qpoolService.getTaxonomyLevel(identity, TaxonomyCompetenceTypes.have);
		for(TaxonomyLevel taxonomyLevel:taxonomyLevels) {
			TreeNode node = new TaxonomyLevelTreeNode(stackPanel, taxonomyLevel, QuestionStatus.draft, identity);
			parentNode.addChild(node);
		}
	}

	private void buildCollectionTreeNodes(TreeNode parentNode) {
		List<QuestionItemCollection> collections = qpoolService.getCollections(identity);
		for(QuestionItemCollection coll: collections) {
			TreeNode node = new CollectionTreeNode(stackPanel, coll);
			parentNode.addChild(node);
		}
	}
	
	public void buildReviewSubTreeModel(TreeNode rootNode) {
		List<TaxonomyLevel> taxonomyLevels = qpoolService.getTaxonomyLevel(identity, TaxonomyCompetenceTypes.teach);
		if(!taxonomyLevels.isEmpty()) {
			reviewNode = new GenericTreeNode(translator.translate("menu.review"));
			reviewNode.setTitle(translator.translate("menu.review"));
			rootNode.addChild(reviewNode);
			
			for(TaxonomyLevel taxonomyLevel:taxonomyLevels) {
				TreeNode node = new TaxonomyLevelTreeNode(stackPanel, taxonomyLevel, QuestionStatus.review, null);
				reviewNode.addChild(node);
			}
		}
	}
	
	public void buildFinalSubTreeModel(TreeNode rootNode) {
		List<TaxonomyLevel> taxonomyLevels = qpoolService.getTaxonomyLevel(identity, TaxonomyCompetenceTypes.manage);
		if (!taxonomyLevels.isEmpty()) {
			finalNode = new GenericTreeNode(translator.translate("menu.final"));
			finalNode.setTitle(translator.translate("menu.final"));
			rootNode.addChild(finalNode);
			for(TaxonomyLevel taxonomyLevel:taxonomyLevels) {
				TreeNode node = new TaxonomyLevelTreeNode(stackPanel, taxonomyLevel, QuestionStatus.finalVersion, null);
				finalNode.addChild(node);
			}
		}
	}
	
	public void buildShareSubTreeModel() {
		sharesNode.removeAllChildren();
		buildPoolTreeNodes(sharesNode);
		buildBusinessGroupTreeNodes(sharesNode);
	}

	private void buildPoolTreeNodes(TreeNode parentNode) {
		List<Pool> pools = qpoolService.getPools(identity, roles);
		for(Pool pool:pools) {
			TreeNode node = new PoolTreeNode(stackPanel, pool);
			parentNode.addChild(node);
		}
	}

	private void buildBusinessGroupTreeNodes(TreeNode parentNode) {
		List<BusinessGroup> groups = qpoolService.getResourcesWithSharedItems(identity);
		for(BusinessGroup group:groups) {
			TreeNode node = new BusinessGroupTreeNode(stackPanel, group);
			parentNode.addChild(node);
		}
	}
	
	private void buildAdminSubTreeModel(TreeNode adminNode) {
		if(!roles.isOLATAdmin() && !roles.isPoolAdmin()) return;
		adminNode.removeAllChildren();
		
		TreeNode node = new TaxonomyAdminTreeNode(translator.translate("menu.admin.studyfields"));
		adminNode.addChild(node);
		
		node = new PoolsAdminTreeNode(translator.translate("menu.admin.pools"));
		adminNode.addChild(node);
		
		node = new QItemTypesAdminTreeNode(translator.translate("menu.admin.types"));
		adminNode.addChild(node);
		
		node = new QEducationalContextsAdminTreeNode(translator.translate("menu.admin.levels"));
		adminNode.addChild(node);

		node = new QLicensesAdminTreeNode(translator.translate("menu.admin.licenses"));
		adminNode.addChild(node);
	}
		
}
