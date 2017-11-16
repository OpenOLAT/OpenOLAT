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

import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceAuditLog.Action;
import org.olat.modules.taxonomy.TaxonomyCompetenceRef;
import org.olat.modules.taxonomy.TaxonomyCompetenceTypes;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyLevelManagedFlag;
import org.olat.modules.taxonomy.TaxonomyLevelRef;
import org.olat.modules.taxonomy.TaxonomyLevelType;
import org.olat.modules.taxonomy.TaxonomyLevelTypeRef;
import org.olat.modules.taxonomy.TaxonomyLevelTypeToType;
import org.olat.modules.taxonomy.TaxonomyRef;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyInfos;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyServiceImpl implements TaxonomyService {
	
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyLevelTypeDAO taxonomyLevelTypeDao;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDao;
	@Autowired
	private TaxonomyLevelTypeToTypeDAO taxonomyLevelTypeToTypeDao;
	@Autowired
	private TaxonomyCompetenceAuditLogDAO taxonomyCompetenceAuditLogDao;
	
	public Taxonomy createTaxonomy(String identifier, String displayName, String description, String externalId) {
		return taxonomyDao.createTaxonomy(identifier, displayName, description, externalId);
	}

	@Override
	public Taxonomy getTaxonomy(TaxonomyRef ref) {
		return taxonomyDao.loadByKey(ref.getKey());
	}

	@Override
	public Taxonomy updateTaxonomy(Taxonomy taxonomy) {
		return taxonomyDao.updateTaxonomy(taxonomy);
	}

	@Override
	public VFSContainer getDocumentsLibrary(Taxonomy taxonomy) {
		return taxonomyDao.getDocumentsLibrary(taxonomy);
	}

	@Override
	public VFSContainer getLostAndFoundDirectory(Taxonomy taxonomy) {
		return taxonomyDao.getLostAndFoundDirectoryLibrary(taxonomy);
	}

	@Override
	public List<Taxonomy> getTaxonomyList() {
		return taxonomyDao.getTaxonomyList();
	}

	@Override
	public List<TaxonomyInfos> getTaxonomyInfosList() {
		return taxonomyDao.getTaxonomyInfosList();
	}

	@Override
	public TaxonomyLevel createTaxonomyLevel(String identifier, String displayName, String description,
			String externalId, TaxonomyLevelManagedFlag[] flags, TaxonomyLevel parent, Taxonomy taxonomy) {
		return taxonomyLevelDao.createTaxonomyLevel(identifier, displayName, description,
				externalId, flags,
				parent, null, taxonomy);
	}

	@Override
	public List<TaxonomyLevel> getTaxonomyLevels(TaxonomyRef ref) {
		return taxonomyLevelDao.getLevels(ref);
	}

	@Override
	public TaxonomyLevel getTaxonomyLevel(TaxonomyLevelRef ref) {
		if(ref == null || ref.getKey() == null) return null;
		return taxonomyLevelDao.loadByKey(ref.getKey());
	}	

	@Override
	public List<TaxonomyLevel> getTaxonomyLevelParentLine(TaxonomyLevel taxonomyLevel, Taxonomy taxonomy) {
		return taxonomyLevelDao.getParentLine(taxonomyLevel, taxonomy);
	}

	@Override
	public TaxonomyLevel updateTaxonomyLevel(TaxonomyLevel level) {
		return taxonomyLevelDao.updateTaxonomyLevel(level);
	}

	@Override
	public TaxonomyLevel moveTaxonomyLevel(TaxonomyLevel level, TaxonomyLevel newParentLevel) {
		return taxonomyLevelDao.moveTaxonomyLevel(level, newParentLevel);
	}

	@Override
	public boolean canDeleteTaxonomyLevel(TaxonomyLevelRef taxonomyLevel) {
		return taxonomyLevelDao.canDelete(taxonomyLevel);
	}

	@Override
	public boolean deleteTaxonomyLevel(TaxonomyLevelRef taxonomyLevel) {
		// save the documents
		TaxonomyLevel reloadedTaxonomyLevel = taxonomyLevelDao.loadByKey(taxonomyLevel.getKey());
		VFSContainer library = taxonomyLevelDao.getDocumentsLibrary(reloadedTaxonomyLevel);
		Taxonomy taxonomy = reloadedTaxonomyLevel.getTaxonomy();
		VFSContainer lostAndFound = taxonomyDao.getLostAndFoundDirectoryLibrary(taxonomy);
		String dir = StringHelper.transformDisplayNameToFileSystemName(reloadedTaxonomyLevel.getIdentifier());
		dir += "_" + taxonomyLevel.getKey();
		VFSContainer lastStorage = lostAndFound.createChildContainer(dir);
		VFSManager.copyContent(library, lastStorage);
		return taxonomyLevelDao.delete(reloadedTaxonomyLevel);
	}

	@Override
	public VFSContainer getDocumentsLibrary(TaxonomyLevel level) {
		return taxonomyLevelDao.getDocumentsLibrary(level);
	}

	@Override
	public TaxonomyLevelType createTaxonomyLevelType(String identifier, String displayName, String description,
			String externalId, Taxonomy taxonomy) {
		return taxonomyLevelTypeDao.createTaxonomyLevelType(identifier, displayName, description, externalId, taxonomy);
	}

	@Override
	public TaxonomyLevelType getTaxonomyLevelType(TaxonomyLevelTypeRef ref) {
		if(ref == null || ref.getKey() == null) return null;
		return taxonomyLevelTypeDao.loadTaxonomyLevelTypeByKey(ref.getKey());
	}
	
	@Override
	public TaxonomyLevelType updateTaxonomyLevelType(TaxonomyLevelType leveltype) {
		return taxonomyLevelTypeDao.updateTaxonomyLevelType(leveltype);
	}

	@Override
	public TaxonomyLevelType updateTaxonomyLevelType(TaxonomyLevelType leveltype, List<TaxonomyLevelType> allowSubTypes) {
		taxonomyLevelTypeToTypeDao.setAllowedSubType(leveltype, allowSubTypes);
		return taxonomyLevelTypeDao.updateTaxonomyLevelType(leveltype);
	}

	@Override
	public TaxonomyLevelType cloneTaxonomyLevelType(TaxonomyLevelTypeRef levelType) {
		TaxonomyLevelType clonedType = taxonomyLevelTypeDao.cloneTaxonomyLevelType(levelType);
		List<TaxonomyLevelTypeToType> allowSubTypesToTypes = taxonomyLevelTypeToTypeDao.getAllowedSubTypes(levelType);
		if(allowSubTypesToTypes.size() > 0) {
			for(TaxonomyLevelTypeToType allowSubTypeToType:allowSubTypesToTypes) {
				taxonomyLevelTypeToTypeDao.addAllowedSubType(clonedType, allowSubTypeToType.getAllowedSubTaxonomyLevelType());
			}
		}
		return clonedType;
	}

	@Override
	public boolean deleteTaxonomyLevelType(TaxonomyLevelTypeRef levelType) {
		if(taxonomyLevelTypeDao.hasLevels(levelType)) {
			return false;
		}
		taxonomyLevelTypeToTypeDao.deleteAllowedSubTypes(levelType);
		taxonomyLevelTypeDao.deleteTaxonomyLevelType(levelType);
		return true;
	}

	@Override
	public void taxonomyLevelTypeAllowSubType(TaxonomyLevelType levelType, TaxonomyLevelType allowSubType) {
		List<TaxonomyLevelTypeToType> typeToTypes = taxonomyLevelTypeToTypeDao.getAllowedSubTypes(levelType, allowSubType);
		if(typeToTypes.isEmpty()) {
			taxonomyLevelTypeToTypeDao.addAllowedSubType(levelType, allowSubType);
		}
	}

	@Override
	public void taxonomyLevelTypeDisallowSubType(TaxonomyLevelType levelType, TaxonomyLevelType disallowSubType) {
		taxonomyLevelTypeToTypeDao.disallowedSubType(levelType, disallowSubType);
	}

	@Override
	public List<TaxonomyLevelType> getTaxonomyLevelTypes(TaxonomyRef taxonomy) {
		return taxonomyLevelTypeDao.loadTaxonomyLevelTypeByTaxonomy(taxonomy);
	}

	@Override
	public boolean hasTaxonomyCompetences(TaxonomyRef taxonomy, IdentityRef identity) {
		return taxonomyCompetenceDao.hasCompetenceByTaxonomy(taxonomy, identity);
	}

	@Override
	public List<TaxonomyCompetence> getTaxonomyCompetences(TaxonomyRef taxonomy, IdentityRef identity) {
		return taxonomyCompetenceDao.getCompetenceByTaxonomy(taxonomy, identity);
	}

	@Override
	public boolean hasCompetence(TaxonomyRef taxonomy, IdentityRef identity, TaxonomyCompetenceTypes... competences) {
		return taxonomyCompetenceDao.hasCompetenceByTaxonomy(taxonomy, identity, competences);
	}

	@Override
	public List<TaxonomyCompetence> getTaxonomyLevelCompetences(TaxonomyLevel taxonomyLevel) {
		return taxonomyCompetenceDao.getCompetenceByLevel(taxonomyLevel);
	}

	@Override
	public TaxonomyCompetence getTaxonomyCompetence(TaxonomyCompetenceRef competence) {
		return taxonomyCompetenceDao.loadCompetenceByKey(competence.getKey());
	}

	@Override
	public List<TaxonomyCompetence> getTaxonomyCompetences(IdentityRef identity, TaxonomyCompetenceTypes... types) {
		return taxonomyCompetenceDao.getCompetences(identity, types);
	}

	@Override
	public List<TaxonomyCompetence> getTaxonomyLevelCompetences(TaxonomyLevelRef taxonomyLevel, IdentityRef identity) {
		return taxonomyCompetenceDao.getCompetenceByLevel(taxonomyLevel, identity);
	}

	@Override
	public TaxonomyCompetence addTaxonomyLevelCompetences(TaxonomyLevel taxonomyLevel, Identity identity, TaxonomyCompetenceTypes competence) {
		return taxonomyCompetenceDao.createTaxonomyCompetence(competence, taxonomyLevel, identity);
	}

	@Override
	public void removeTaxonomyLevelCompetence(TaxonomyCompetence competence) {
		taxonomyCompetenceDao.deleteCompetence(competence);
	}

	@Override
	public void auditLog(Action action, String before, String after, String message, TaxonomyRef taxonomy,
			TaxonomyCompetence competence, IdentityRef assessedIdentity, IdentityRef author) {
		taxonomyCompetenceAuditLogDao.auditLog(action, before, after, message, taxonomy, competence, assessedIdentity, author);
	}

	@Override
	public String toAuditXml(TaxonomyCompetence competence) {
		return taxonomyCompetenceAuditLogDao.toXml(competence);
	}
}
