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

import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.UUID;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSManager;
import org.olat.modules.portfolio.manager.PortfolioPageToTaxonomyCompetenceDAO;
import org.olat.modules.quality.manager.QualityDataCollectionDAO;
import org.olat.modules.taxonomy.Taxonomy;
import org.olat.modules.taxonomy.TaxonomyCompetence;
import org.olat.modules.taxonomy.TaxonomyCompetenceAuditLog.Action;
import org.olat.modules.taxonomy.TaxonomyCompetenceLinkLocations;
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
import org.olat.modules.taxonomy.model.TaxonomyLevelSearchParameters;
import org.olat.user.UserDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class TaxonomyServiceImpl implements TaxonomyService, UserDataDeletable {
	
	@Autowired
	private TaxonomyDAO taxonomyDao;
	@Autowired
	private TaxonomyLevelDAO taxonomyLevelDao;
	@Autowired
	private TaxonomyRelationsDAO taxonomyRelationsDao;
	@Autowired
	private TaxonomyLevelTypeDAO taxonomyLevelTypeDao;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDao;
	@Autowired
	private TaxonomyLevelTypeToTypeDAO taxonomyLevelTypeToTypeDao;
	@Autowired
	private TaxonomyCompetenceAuditLogDAO taxonomyCompetenceAuditLogDao;
	@Autowired
	private QualityDataCollectionDAO dataCollectionDao;
	@Autowired
	private PortfolioPageToTaxonomyCompetenceDAO portfolioPageToTaxonomyCompetenceDAO;
	@Autowired
	private TaxonomyCompetenceDAO taxonomyCompetenceDAO;
	
	@Override
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
	public List<TaxonomyLevel> getTaxonomyLevels(TaxonomyRef ref, TaxonomyLevelSearchParameters searchParams) {
		if(searchParams == null) {
			return taxonomyLevelDao.getLevels(ref);
		}
		return taxonomyLevelDao.searchLevels(ref, searchParams);
	}

	@Override
	public TaxonomyLevel getTaxonomyLevel(TaxonomyLevelRef ref) {
		if(ref == null || ref.getKey() == null) return null;
		return taxonomyLevelDao.loadByKey(ref.getKey());
	}

	@Override
	public List<TaxonomyLevel> getTaxonomyLevelsByRefs(Collection<? extends TaxonomyLevelRef> refs) {
		return taxonomyLevelDao.loadLevels(refs);
	}
	
	@Override
	public List<TaxonomyLevel> getTaxonomyLevelsByKeys(Collection<Long> keys) {
		return taxonomyLevelDao.loadLevelsByKeys(keys);
	}

	@Override
	public List<TaxonomyLevel> getTaxonomyLevelParentLine(TaxonomyLevel taxonomyLevel, Taxonomy taxonomy) {
		return taxonomyLevelDao.getParentLine(taxonomyLevel, taxonomy);
	}

	@Override
	public TaxonomyLevel updateTaxonomyLevel(TaxonomyLevel level) {
		if (level.getType() != null) {
			checkLevelTypeCompetences(level.getType());
		}
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
	public boolean deleteTaxonomyLevel(TaxonomyLevelRef taxonomyLevel, TaxonomyLevelRef mergeTo) {
		// save the documents
		TaxonomyLevel reloadedTaxonomyLevel = taxonomyLevelDao.loadByKey(taxonomyLevel.getKey());
		if(mergeTo != null) {
			TaxonomyLevel reloadedMergeTo = taxonomyLevelDao.loadByKey(mergeTo.getKey());
			merge(reloadedTaxonomyLevel, reloadedMergeTo);
		} else {
			VFSContainer library = taxonomyLevelDao.getDocumentsLibrary(reloadedTaxonomyLevel);
			if(library != null) {
				Taxonomy taxonomy = reloadedTaxonomyLevel.getTaxonomy();
				VFSContainer lostAndFound = taxonomyDao.getLostAndFoundDirectoryLibrary(taxonomy);
				String dir = StringHelper.transformDisplayNameToFileSystemName(reloadedTaxonomyLevel.getIdentifier());
				dir += "_" + taxonomyLevel.getKey();
				VFSContainer lastStorage = lostAndFound.createChildContainer(dir);
				if(lastStorage == null) {
					VFSItem storageItem = lostAndFound.resolve(dir);
					if(storageItem instanceof VFSContainer) {
						lastStorage = (VFSContainer)storageItem;
					} else {
						lastStorage = lostAndFound.createChildContainer(UUID.randomUUID().toString());
					}
				}
				
				VFSManager.copyContent(library, lastStorage);
			}
			//delete the competences
			taxonomyCompetenceDao.deleteCompetences(taxonomyLevel);
			//questions
			taxonomyRelationsDao.removeFromQuestionItems(taxonomyLevel);
		}

		return taxonomyLevelDao.delete(reloadedTaxonomyLevel);
	}

	private void merge(TaxonomyLevel taxonomyLevel, TaxonomyLevel mergeTo) {
		//documents
		VFSContainer sourceLibrary = taxonomyLevelDao.getDocumentsLibrary(taxonomyLevel);
		VFSContainer targetLibrary = taxonomyLevelDao.getDocumentsLibrary(mergeTo);
		VFSManager.copyContent(sourceLibrary, targetLibrary);
		
		//children
		List<TaxonomyLevel> children = taxonomyLevelDao.getChildren(taxonomyLevel);
		for(TaxonomyLevel child:children) {
			taxonomyLevelDao.moveTaxonomyLevel(child, mergeTo);
		}
		//move the competences
		taxonomyCompetenceDao.replace(taxonomyLevel, mergeTo);
		//questions
		taxonomyRelationsDao.replaceQuestionItem(taxonomyLevel, mergeTo);
	}

	@Override
	public VFSContainer getDocumentsLibrary(TaxonomyLevel level) {
		return taxonomyLevelDao.getDocumentsLibrary(level);
	}

	@Override
	public TaxonomyLevelType createTaxonomyLevelType(String identifier, String displayName, String description,
			String externalId, boolean allowedAsCompetence, Taxonomy taxonomy) {
		return taxonomyLevelTypeDao.createTaxonomyLevelType(identifier, displayName, description, externalId, allowedAsCompetence, taxonomy);
	}

	@Override
	public TaxonomyLevelType getTaxonomyLevelType(TaxonomyLevelTypeRef ref) {
		if(ref == null || ref.getKey() == null) return null;
		return taxonomyLevelTypeDao.loadTaxonomyLevelTypeByKey(ref.getKey());
	}
	
	@Override
	public TaxonomyLevelType updateTaxonomyLevelType(TaxonomyLevelType leveltype) {
		checkLevelTypeCompetences(leveltype);
		
		return taxonomyLevelTypeDao.updateTaxonomyLevelType(leveltype);
	}

	@Override
	public TaxonomyLevelType updateTaxonomyLevelType(TaxonomyLevelType leveltype, List<TaxonomyLevelType> allowSubTypes) {
		taxonomyLevelTypeToTypeDao.setAllowedSubType(leveltype, allowSubTypes);
		
		checkLevelTypeCompetences(leveltype);
		
		return taxonomyLevelTypeDao.updateTaxonomyLevelType(leveltype);
	}
	
	public void checkLevelTypeCompetences(TaxonomyLevelType levelType) {
		if (!levelType.isAllowedAsCompetence()) {			
			portfolioPageToTaxonomyCompetenceDAO.deleteRelationsByLevelType(levelType);
		}
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
	public boolean hasCompetenceByLevel(TaxonomyLevelRef taxonomyLevel, IdentityRef identity, Date date,
			TaxonomyCompetenceTypes... competenceTypes) {
		return taxonomyCompetenceDao.hasCompetenceByLevel(taxonomyLevel, identity, date, competenceTypes);
	}

	@Override
	public List<TaxonomyLevelType> getTaxonomyLevelTypes(TaxonomyRef taxonomy) {
		return taxonomyLevelTypeDao.loadTaxonomyLevelTypeByTaxonomy(taxonomy);
	}

	@Override
	public boolean hasTaxonomyCompetences(TaxonomyRef taxonomy, IdentityRef identity, Date date) {
		return taxonomyCompetenceDao.hasCompetenceByTaxonomy(taxonomy, identity, date);
	}

	@Override
	public List<TaxonomyCompetence> getTaxonomyCompetences(TaxonomyRef taxonomy, IdentityRef identity, Date date) {
		return taxonomyCompetenceDao.getCompetencesByTaxonomy(taxonomy, identity, date);
	}

	@Override
	public boolean hasTaxonomyCompetences(TaxonomyRef taxonomy, IdentityRef identity, Date date, TaxonomyCompetenceTypes... competences) {
		return taxonomyCompetenceDao.hasCompetenceByTaxonomy(taxonomy, identity, date, competences);
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
	public int countTaxonomyCompetences(List<? extends TaxonomyLevelRef> taxonomyLevels) {
		return taxonomyCompetenceDao.countTaxonomyCompetences(taxonomyLevels);
	}

	@Override
	public TaxonomyCompetence addTaxonomyLevelCompetences(TaxonomyLevel taxonomyLevel, Identity identity,
			TaxonomyCompetenceTypes competence, Date expiration, TaxonomyCompetenceLinkLocations linkLocation) {
		return taxonomyCompetenceDao.createTaxonomyCompetence(competence, taxonomyLevel, identity, expiration, linkLocation);
	}
	
	@Override
	public TaxonomyCompetence addTaxonomyLevelCompetences(TaxonomyLevel taxonomyLevel, Identity identity,
			TaxonomyCompetenceTypes competence, Date expiration) {
		return taxonomyCompetenceDao.createTaxonomyCompetence(competence, taxonomyLevel, identity, expiration, TaxonomyCompetenceLinkLocations.MANUAL_INTERNAL);
	}

	@Override
	public TaxonomyCompetence updateTaxonomyLevelCompetence(TaxonomyCompetence competence) {
		return taxonomyCompetenceDao.updateCompetence(competence);
	}

	@Override
	public void removeTaxonomyLevelCompetence(TaxonomyCompetence competence) {
		portfolioPageToTaxonomyCompetenceDAO.deleteRelation(competence);
		taxonomyCompetenceDao.deleteCompetence(competence);
	}

	@Override
	public int countRelations(List<? extends TaxonomyLevelRef> taxonomyLevels) {
		return taxonomyRelationsDao.countQuestionItems(taxonomyLevels);
	}
	
	@Override
	public int countQualityManagementsRelations(List<? extends TaxonomyLevelRef> taxonomyLevels) {
		return dataCollectionDao.countDataCollectionsByTaxonomyLevel(taxonomyLevels);
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

	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		taxonomyCompetenceDao.deleteCompetences(identity);
	}
}
