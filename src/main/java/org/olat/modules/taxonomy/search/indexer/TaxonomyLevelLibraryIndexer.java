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
package org.olat.modules.taxonomy.search.indexer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.olat.core.util.tree.TreeHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.manager.TaxonomyTreeBuilder;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.model.TaxonomyTreeNode;
import org.olat.modules.taxonomy.model.TaxonomyTreeNodeType;
import org.olat.modules.taxonomy.ui.TaxonomyUIFactory;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.DefaultIndexer;
import org.olat.search.service.indexer.FolderIndexerAccess;
import org.olat.search.service.indexer.FolderIndexerWorker;
import org.olat.search.service.indexer.OlatFullIndexer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20 oct. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("taxonomyLevelLibraryIndexer")
public class TaxonomyLevelLibraryIndexer extends DefaultIndexer {
	
	@Autowired
	private TaxonomyService taxonomyService;

	@Override
	public String getSupportedTypeName() {
		return "TaxonomyLevel";
	}

	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object object, OlatFullIndexer indexerWriter)
	throws IOException, InterruptedException {
		if(object instanceof TaxonomyLevel) {
			TaxonomyLevel level = (TaxonomyLevel)object;
			VFSContainer library = taxonomyService.getDocumentsLibrary(level);
			if(library != null) {
				Translator taxonomyTranslator = Util.createPackageTranslator(TaxonomyUIFactory.class, I18nModule.getDefaultLocale());
				SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
				searchResourceContext.setBusinessControlFor(level);
				searchResourceContext.setTitle(TaxonomyUIFactory.translateDisplayName(taxonomyTranslator, level));
				searchResourceContext.setDescription(TaxonomyUIFactory.translateDescription(taxonomyTranslator, level));
				searchResourceContext.setLastModified(level.getLastModified());
				searchResourceContext.setCreatedDate(level.getCreationDate());

				FolderIndexerWorker runnableFolderIndexer = new  FolderIndexerWorker();
				runnableFolderIndexer.setAccessRule(FolderIndexerAccess.FULL_ACCESS);
				runnableFolderIndexer.setParentResourceContext(searchResourceContext);
				runnableFolderIndexer.setContainer(library);
				runnableFolderIndexer.setIndexWriter(indexerWriter);
				runnableFolderIndexer.setFilePath("");
				indexerWriter.submit(runnableFolderIndexer);
			}
		}
	}

	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		if(roles.isAdministrator() || roles.isSystemAdmin()) return true;
		
		if("TaxonomyLevel".equals(contextEntry.getOLATResourceable().getResourceableTypeName())) {
			Long levelKey = contextEntry.getOLATResourceable().getResourceableId();
			TaxonomyLevel level = taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(levelKey));
			TaxonomyTreeBuilder builder = new TaxonomyTreeBuilder(level.getTaxonomy(), identity, null, false, true, "Templates", null);
			TreeModel model = builder.buildTreeModel();
			List<TreeNode> flat = new ArrayList<>();
			TreeHelper.makeTreeFlat(model.getRootNode(), flat);
			for(TreeNode node:flat) {
				TaxonomyTreeNode taxonomyNode = (TaxonomyTreeNode)node;
				if(taxonomyNode.getType() == TaxonomyTreeNodeType.taxonomyLevel
						&& level.equals(taxonomyNode.getTaxonomyLevel())) {
					if(taxonomyNode.isDocumentsLibraryEnabled() && taxonomyNode.isCanRead()) {
						return true;
					}
				}
			}
		}
		return false;
	}
}