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
package org.olat.modules.taxonomy;

import java.util.Collection;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.id.Identity;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.modules.taxonomy.model.TaxonomyInfos;
import org.olat.modules.taxonomy.model.TaxonomyLevelSearchParameters;

/**
 * 
 * Initial date: 18 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface TaxonomyService {
	
	public static final String DIRECTORY = "taxonomy";
	
	public Taxonomy createTaxonomy(String identifier, String displayName, String description, String externalId);
	
	/**
	 * Reload a taxonomy
	 * @param ref The reference of the taxonomy
	 * @return A freshly loaded taxonomy with its base group
	 */
	public Taxonomy getTaxonomy(TaxonomyRef ref);
	
	public Taxonomy updateTaxonomy(Taxonomy taxonomy);
	
	/**
	 * @return The list of taxonomy trees available in the system.
	 */
	public List<Taxonomy> getTaxonomyList();
	
	/**
	 * @return The list of taxonomy informations available in hte system.
	 */
	public List<TaxonomyInfos> getTaxonomyInfosList();
	

	public TaxonomyLevel createTaxonomyLevel(String identifier, String displayName, String description, String externalId,
			TaxonomyLevelManagedFlag[] flags, TaxonomyLevel parent, Taxonomy taxonomy);
	
	/**
	 * 
	 * @param taxonomyLevel The taxonomy level to check
	 * @return true if the level has no dependency anymore
	 */
	public boolean canDeleteTaxonomyLevel(TaxonomyLevelRef taxonomyLevel);
	
	/**
	 * The operation move the following elements from the source taxonomy
	 * level to the target taxonomy level:
	 * <ul>
	 *  <li>The documents
	 *  <li>The children
	 *  <li>The competence
	 *  <li>Replace the taxonomy level from question items
	 * </ul>
	 * 
	 * @param taxonomyLevel The taxonomy level source
	 * @param mergeTo The taxonomy level target (optional)
	 * @return true if the level can be deleted
	 */
	public boolean deleteTaxonomyLevel(TaxonomyLevelRef taxonomyLevel, TaxonomyLevelRef mergeTo);
	
	/**
	 * @param ref The root taxonomy (optional)
	 * @return A list of levels
	 */
	public List<TaxonomyLevel> getTaxonomyLevels(TaxonomyRef ref);
	
	/**
	 * @param ref The root taxonomy (optional)
	 * @param searchParams Search parameters
	 * @return A list of levels
	 */
	public List<TaxonomyLevel> getTaxonomyLevels(TaxonomyRef ref, TaxonomyLevelSearchParameters searchParams);
	
	/**
	 * Load a taxonomy level by is reference.
	 * 
	 * @param ref The taxonomy level key
	 * @return The freshly loaded taxonomy level
	 */
	public TaxonomyLevel getTaxonomyLevel(TaxonomyLevelRef ref);

	public List<TaxonomyLevel> getTaxonomyLevelsByRefs(Collection<? extends TaxonomyLevelRef> refs);
	
	public List<TaxonomyLevel> getTaxonomyLevelsByKeys(Collection<Long> keys);
	
	/**
	 * 
	 * @param taxonomyLevel The taxonomy level
	 * @param taxonomy The taxonomy
	 * @return The list of levels
	 */
	public List<TaxonomyLevel> getTaxonomyLevelParentLine(TaxonomyLevel taxonomyLevel, Taxonomy taxonomy);
	
	/**
	 * Update the level. If the identifier was changed, the method will
	 * update all the children materialized identifiers path.
	 * 
	 * @param level The level to update
	 * @return The updated level
	 */
	public TaxonomyLevel updateTaxonomyLevel(TaxonomyLevel level);
	
	/**
	 * Move the level. The method will lock the taxonomy the method
	 * update all the children materialized keys and identifiers path.
	 * 
	 * @param level The level to move
	 * @param newParentLevel The new parent or null if the level will be root
	 * @return The moved level
	 */
	public TaxonomyLevel moveTaxonomyLevel(TaxonomyLevel level, TaxonomyLevel newParentLevel);
	
	/**
	 * Get the documents directory for the specified taxonomy level.
	 * 
	 * @param level The taxonomy level
	 * @return A directory
	 */
	public VFSContainer getDocumentsLibrary(TaxonomyLevel level);
	
	/**
	 * Get the documents library for the specified taxonomy.
	 * 
	 * @param taxonomy The taxonomy
	 * @return A directory
	 */
	public VFSContainer getDocumentsLibrary(Taxonomy taxonomy);
	
	/**
	 * @param taxonomy The taxonomy
	 * @return A directory where the documents of deleted levels are stored
	 */
	public VFSContainer getLostAndFoundDirectory(Taxonomy taxonomy);
	

	public TaxonomyLevelType createTaxonomyLevelType(String identifier, String displayName, String description, String externalId, boolean allowedAsCompetence, Taxonomy taxonomy);
	
	/**
	 * 
	 * @param ref The reference
	 * @return A freshly loaded taxonomy level type
	 */
	public TaxonomyLevelType getTaxonomyLevelType(TaxonomyLevelTypeRef ref);

	public TaxonomyLevelType updateTaxonomyLevelType(TaxonomyLevelType levelType);
	
	/**
	 * Update the taxonomy level type
	 * @param levelType
	 * @param allowSubTypes
	 * @return The merged taxonomy level type
	 */
	public TaxonomyLevelType updateTaxonomyLevelType(TaxonomyLevelType levelType, List<TaxonomyLevelType> allowSubTypes);
	
	/**
	 * Make a clone the level type.
	 * @param levelType The level type to clone
	 * @return A persisted clonde level type.
	 */
	public TaxonomyLevelType cloneTaxonomyLevelType(TaxonomyLevelTypeRef levelType);
	
	
	public boolean deleteTaxonomyLevelType(TaxonomyLevelTypeRef levelType);
	
	/**
	 * Add directly an allowed taxonomy level type to the specified taxonomy level type.
	 * @param levelType The taxonomy level type to enhance
	 * @param allowSubType The taxonomy level type to allow
	 */
	public void taxonomyLevelTypeAllowSubType(TaxonomyLevelType levelType, TaxonomyLevelType allowSubType);
	
	/**
	 * Remove directly an allowed sub type.
	 * 
	 * @param levelType The parent taxonomy level type
	 * @param disallowSubType The taxonomy level type to remove from the allowed list
	 */
	public void taxonomyLevelTypeDisallowSubType(TaxonomyLevelType levelType, TaxonomyLevelType disallowSubType);
	
	/**
	 * Has a specific competence for a taxonomy level.
	 * 
	 * @param taxonomyLevel
	 * @param identity
	 * @param date
	 * @param competenceTypes
	 * @return
	 */
	public boolean hasCompetenceByLevel(TaxonomyLevelRef taxonomyLevel, IdentityRef identity, Date date, TaxonomyCompetenceTypes... competenceTypes);
	
	/**
	 * The available types for a specific taxonomy.
	 * 
	 * @param taxonomy The taxonomy (mandatory)
	 * @return A list of taxonomy level types
	 */
	public List<TaxonomyLevelType> getTaxonomyLevelTypes(TaxonomyRef taxonomy);
	
	/**
	 * Has some currently valid competence in a taxonomy. The expiration
	 * is checked without time informations, only date.
	 * 
	 * @param taxonomy The taxonomy (mandatory)
	 * @param identity The user to check (mandatory)
	 * @param date The date to compare the expiration date (mandatory)
	 * @return true if some competence was found.
	 */
	public boolean hasTaxonomyCompetences(TaxonomyRef taxonomy, IdentityRef identity, Date date);
	

	public TaxonomyCompetence getTaxonomyCompetence(TaxonomyCompetenceRef competence);
	
	/**
	 * Return all the competence without checking the expiration date.
	 * 
	 * @param identity
	 * @param types
	 * @return
	 */
	public List<TaxonomyCompetence> getTaxonomyCompetences(IdentityRef identity, TaxonomyCompetenceTypes... types);
	
	/**
	 * Get the competences in a taxonomy tree of the specified user. The expiration
	 * is checked without time informations, only date.
	 * 
	 * @param taxonomy The taxonomy (mandatory)
	 * @param identity The user to check (mandatory)
	 * @param date The date to compare the expiration date (mandatory)
	 * @return A list of currently valid competence
	 */
	public List<TaxonomyCompetence> getTaxonomyCompetences(TaxonomyRef taxonomy, IdentityRef identity, Date date);
	
	/**
	 * @param taxonomyLevels A list of taxonomy levels
	 * @return The number of competences related to the specified list of taxonomy levels.
	 */
	public int countTaxonomyCompetences(List<? extends TaxonomyLevelRef> taxonomyLevels);
	
	/**
	 * @param taxonomy The taxonomy (mandatory)
	 * @param identity The user to check (mandatory)
	 * @param competences The list of competences to search
	 * @return true if the user has some of the specified competence in the taxonomy tree
	 */
	public boolean hasTaxonomyCompetences(TaxonomyRef taxonomy, IdentityRef identity, Date date, TaxonomyCompetenceTypes... competences);
	
	/**
	 * The competence at a specified level of the taxonomy tree.
	 * @param taxonomyLevel The taxonomy level (mandatory)
	 * @return A list of competences
	 */
	public List<TaxonomyCompetence> getTaxonomyLevelCompetences(TaxonomyLevel taxonomyLevel);
	
	/**
	 * The competences at a specific level for the specified user. The expiration date is
	 * not checked.
	 * 
	 * @param taxonomyLevel The taxonomy level (mandatory)
	 * @param identity The user (mandatory)
	 * @return A list of taxonomy competences
	 */
	public List<TaxonomyCompetence> getTaxonomyLevelCompetences(TaxonomyLevelRef taxonomyLevel, IdentityRef identity);
	
	/**
	 * Add a specific competence to a user.
	 * 
	 * @param taxonomyLevel
	 * @param identities
	 * @param comptence
	 */
	public TaxonomyCompetence addTaxonomyLevelCompetences(TaxonomyLevel taxonomyLevel, Identity identity,
			TaxonomyCompetenceTypes competence, Date expiration, TaxonomyCompetenceLinkLocations linkLocation);

	public TaxonomyCompetence addTaxonomyLevelCompetences(TaxonomyLevel taxonomyLevel, Identity identity,
			TaxonomyCompetenceTypes competence, Date expiration);
	
	public TaxonomyCompetence updateTaxonomyLevelCompetence(TaxonomyCompetence competence);

	/**
	 * Delete the competence
	 * 
	 * @param competence The competence to remove
	 */
	public void removeTaxonomyLevelCompetence(TaxonomyCompetence competence);
	
	/**
	 * @param taxonomyLevels A list of taxonomy levels
	 * @return The number of questions and other elements related to the specified taxonomy levels
	 */
	public int countRelations(List<? extends TaxonomyLevelRef> taxonomyLevels);
	
	/**
	 * @param taxonomyLevels A list of taxonomy levels
	 * @return The number of surveys related to the specified taxonomy levels
	 */
	public int countQualityManagementsRelations(List<? extends TaxonomyLevelRef> taxonomyLevels);
	
	/**
	 * 
	 * @param action
	 * @param before
	 * @param after
	 * @param message
	 * @param taxonomy
	 * @param competence
	 * @param assessedIdentity
	 * @param author
	 */
	public void auditLog(TaxonomyCompetenceAuditLog.Action action, String before, String after, String message,
			TaxonomyRef taxonomy, TaxonomyCompetence competence,
			IdentityRef assessedIdentity, IdentityRef author);
	
	/**
	 * Standardized conversion from object to XML used by
	 * the audit log.
	 * 
	 * @param competence The competence
	 * @return A XML representation of the competence.
	 */
	public String toAuditXml(TaxonomyCompetence competence);
}
