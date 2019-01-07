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
package org.olat.modules.taxonomy.manager;

import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.Group;
import org.olat.basesecurity.manager.GroupDAO;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.persistence.DB;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyImpl;
import org.olat.modules.taxonomy.model.TaxonomyInfos;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyDAO implements InitializingBean{

	private File rootDirectory, taxonomyDirectory;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private GroupDAO groupDao;
	
	@Override
	public void afterPropertiesSet() {
		File bcrootDirectory = new File(FolderConfig.getCanonicalRoot());
		rootDirectory = new File(bcrootDirectory, TaxonomyService.DIRECTORY);
		taxonomyDirectory = new File(rootDirectory, TaxonomyService.DIRECTORY);
		if(!taxonomyDirectory.exists()) {
			taxonomyDirectory.mkdirs();
		}
	}
	
	public Taxonomy createTaxonomy(String identifier, String displayName, String description, String externalId) {
		TaxonomyImpl taxonomy = new TaxonomyImpl();
		taxonomy.setCreationDate(new Date());
		taxonomy.setLastModified(taxonomy.getCreationDate());
		if(StringHelper.containsNonWhitespace(identifier)) {
			taxonomy.setIdentifier(identifier);
		} else {
			taxonomy.setIdentifier(UUID.randomUUID().toString());
		}
		taxonomy.setDisplayName(displayName);
		taxonomy.setDescription(description);
		taxonomy.setExternalId(externalId);
		Group group = groupDao.createGroup();
		taxonomy.setGroup(group);
		dbInstance.getCurrentEntityManager().persist(taxonomy);
		String storage = createStorage(taxonomy, "directory");
		taxonomy.setDirectoryPath(storage);
		String lostFoundStorage = createStorage(taxonomy, "lostfound");
		taxonomy.setDirectoryLostFoundPath(lostFoundStorage);
		taxonomy = dbInstance.getCurrentEntityManager().merge(taxonomy);
		taxonomy.getGroup();
		return taxonomy;
	}
	
	public Taxonomy loadByKey(Long key) {
		List<Taxonomy> taxonomies = dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadTaxonomyByKey", Taxonomy.class)
				.setParameter("taxonomyKey", key)
				.getResultList();
		return taxonomies == null || taxonomies.isEmpty() ? null : taxonomies.get(0);
	}
	
	public Taxonomy updateTaxonomy(Taxonomy taxonomy) {
		((TaxonomyImpl)taxonomy).setLastModified(new Date());
		return dbInstance.getCurrentEntityManager().merge(taxonomy);
	}
	
	public List<Taxonomy> getTaxonomyList() {
		return dbInstance.getCurrentEntityManager()
				.createNamedQuery("loadAllTaxonomy", Taxonomy.class)
				.getResultList();
	}
	
	public List<TaxonomyInfos> getTaxonomyInfosList() {
		StringBuilder sb = new StringBuilder(256);
		sb.append("select tax, ")
		  .append(" (select count(level.key) from ctaxonomylevel level")
		  .append("  where level.taxonomy.key=tax.key")
		  .append(" ) as numOfLevels")
		  .append(" from ctaxonomy tax");

		List<Object[]> objectsList = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Object[].class)
				.getResultList();
		
		List<TaxonomyInfos> infos = new ArrayList<>(objectsList.size());
		for(Object[] objects:objectsList) {
			Taxonomy taxonomy = (Taxonomy)objects[0];
			Number numOfLevels = (Number)objects[1];
			infos.add(new TaxonomyInfos(taxonomy, numOfLevels == null ? 0 : numOfLevels.intValue()));
		}
		return infos;
	}
	
	public String createStorage(Taxonomy taxonomy, String type) {
		File storage = new File(taxonomyDirectory, taxonomy.getKey().toString());
		File directory = new File(storage, type);
		directory.mkdirs();
		Path relativePath = rootDirectory.toPath().relativize(directory.toPath());
		return relativePath.toString();
	}
	
	public VFSContainer getDocumentsLibrary(Taxonomy taxonomy) {
		String path = ((TaxonomyImpl)taxonomy).getDirectoryPath();
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		path = "/" + TaxonomyService.DIRECTORY + path;
		return VFSManager.olatRootContainer(path, null);
	}
	
	public VFSContainer getLostAndFoundDirectoryLibrary(Taxonomy taxonomy) {
		String path = ((TaxonomyImpl)taxonomy).getDirectoryLostFoundPath();
		if(!path.startsWith("/")) {
			path = "/" + path;
		}
		path = "/" + TaxonomyService.DIRECTORY + path;
		return VFSManager.olatRootContainer(path, null);
	}
}
