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
package org.olat.modules.docpool.search.indexer;

import java.io.IOException;
import java.util.List;

import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.docpool.DocumentPoolModule;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.model.TaxonomyRefImpl;
import org.olat.modules.taxonomy.search.indexer.TaxonomyLibraryIndexer;
import org.olat.search.service.SearchResourceContext;
import org.olat.search.service.indexer.OlatFullIndexer;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 10 nov. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("documentPoolIndexer")
public class DocumentPoolIndexer extends TaxonomyLibraryIndexer implements InitializingBean {
	
	@Autowired
	private DocumentPoolModule documentPoolModule;

	@Override
	public String getSupportedTypeName() {
		return "DocumentPool";
	}

	@Override
	public void doIndex(SearchResourceContext parentResourceContext, Object object, OlatFullIndexer indexerWriter)
			throws IOException, InterruptedException {
		String taxonomyTreeKey = documentPoolModule.getTaxonomyTreeKey();
		if(StringHelper.isLong(taxonomyTreeKey)) {
			Long taxonomyKey = new Long(taxonomyTreeKey);
			Taxonomy taxonomy = taxonomyService.getTaxonomy(new TaxonomyRefImpl(taxonomyKey));

			OLATResourceable docPoolOres = OresHelper.createOLATResourceableInstanceWithoutCheck(getSupportedTypeName(), 0l);
			SearchResourceContext searchResourceContext = new SearchResourceContext(parentResourceContext);
			searchResourceContext.setBusinessControlFor(docPoolOres);
			searchResourceContext.setTitle(taxonomy.getDisplayName());
			searchResourceContext.setDescription(taxonomy.getDescription());
			searchResourceContext.setLastModified(taxonomy.getLastModified());
			searchResourceContext.setCreatedDate(taxonomy.getCreationDate());
			doIndexTaxonomyLibrary(searchResourceContext, taxonomy, indexerWriter);
		}
	}

	@Override
	public boolean checkAccess(ContextEntry contextEntry, BusinessControl businessControl, Identity identity, Roles roles) {
		if(!documentPoolModule.isTemplatesDirectoryEnabled()) {
			//discard templates content
			List<ContextEntry> entries = businessControl.getEntriesDownTheControls();
			for(ContextEntry entry:entries) {
				if("Templates".equals(entry.getOLATResourceable().getResourceableTypeName())) {
					return false;
				}
			}
		}
		return super.checkAccess(contextEntry, businessControl, identity, roles);
	}
}
