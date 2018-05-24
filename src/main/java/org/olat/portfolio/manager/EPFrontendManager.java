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
package org.olat.portfolio.manager;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.collaboration.CollaborationTools;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.tagging.manager.TaggingManager;
import org.olat.core.commons.services.tagging.model.Tag;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.Coordinator;
import org.olat.core.util.coordinate.SyncerCallback;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.assessment.manager.AssessmentNotificationsHandler;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.group.BusinessGroup;
import org.olat.group.DeletableGroupData;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
import org.olat.modules.webFeed.portfolio.LiveBlogArtefactHandler;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.model.EPFilterSettings;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPMapShort;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPTargetResource;
import org.olat.portfolio.model.structel.ElementType;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.model.structel.PortfolioStructureRef;
import org.olat.properties.NarrowedPropertyManager;
import org.olat.properties.Property;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.manager.RepositoryEntryDAO;
import org.olat.resource.OLATResource;
import org.olat.search.SearchResults;
import org.olat.search.model.AbstractOlatDocument;
import org.olat.search.model.ResultDocument;
import org.olat.search.service.indexer.identity.PortfolioArtefactIndexer;
import org.olat.search.service.searcher.SearchClient;
import org.olat.user.UserDataDeletable;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Description:<br>
 * Manager for common used tasks for ePortfolio. Should be used for all calls
 * from controllers. will itself use all other managers to
 * manipulate artefacts or structureElements and policies.
 * 
 * <P>
 * Initial Date: 11.06.2010 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, http://www.frentix.com
 */
@Service("epFrontendManager")
public class EPFrontendManager implements UserDataDeletable, DeletableGroupData {
	
	private static final OLog log = Tracing.createLoggerFor(EPFrontendManager.class);

	@Autowired
	private Coordinator coordinator;
	@Autowired
	private EPArtefactManager artefactManager;
	@Autowired
	private EPStructureManager structureManager;
	@Autowired
	private TaggingManager taggingManager;
	@Autowired
	private AssessmentNotificationsHandler assessmentNotificationsHandler;
	@Autowired
	private DB dbInstance;
	@Autowired
	private SearchClient searchClient;
	@Autowired
	private EPSettingsManager settingsManager; 
	@Autowired
	private EPPolicyManager policyManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private PortfolioModule portfolioModule;
	@Autowired
	private RepositoryEntryDAO repositoryEntryDao;
	@Autowired
	private AssessmentService assessmentService;
	
	/**
	 * Check if a user has any kind of EP v1 resources: artefacts, a owned map or a shared map
	 * @param identity
	 * @return true: yes, has some EP v1 stuff; false: has no EP v1 resources
	 */
	public boolean hasMapOrArtefact(Identity identity) {
		return artefactManager.hasArtefactPool(identity) || structureManager.hasMap(identity) || structureManager.hasStructureElementsFromOthersWithoutPublic(identity);
	}
	
	/**
	 * Create and persist an artefact of the given type
	 * 
	 * @param type
	 * @return The persisted artefact
	 */
	public AbstractArtefact createAndPersistArtefact(Identity identity, String type) {
		return artefactManager.createAndPersistArtefact(identity, type);
	}

	/**
	 * Persists the artefact and returns the new version
	 * 
	 * @param artefact
	 * @return The last version of the artefact
	 */
	public AbstractArtefact updateArtefact(AbstractArtefact artefact) {
		return artefactManager.updateArtefact(artefact);
	}

	/**
	 * delete an artefact and also its vfs-artefactContainer
	 * all used tags will also be deleted.
	 * @param artefact
	 */
	public void deleteArtefact(AbstractArtefact artefact) {
		List<PortfolioStructure> linksToArtefact = structureManager.getAllReferencesForArtefact(artefact);
		for (PortfolioStructure portfolioStructure : linksToArtefact) {
			structureManager.removeArtefactFromStructure(artefact, portfolioStructure);
		}
		// load again as session might be closed between
		artefact = artefactManager.loadArtefactByKey(artefact.getKey());
		artefactManager.deleteArtefact(artefact);
	}
	
	@Override
	public void deleteUserData(Identity identity, String newDeletedUserName) {
		deleteUsersArtefacts(identity);

		List<PortfolioStructure> userPersonalMaps = getStructureElementsForUser(identity, ElementType.DEFAULT_MAP, ElementType.STRUCTURED_MAP);
		for (PortfolioStructure portfolioStructure : userPersonalMaps) {
			deletePortfolioStructure(portfolioStructure);
		}
	}
	
	@Override
	public boolean deleteGroupDataFor(BusinessGroup group) {
		final NarrowedPropertyManager npm = NarrowedPropertyManager.getInstance(group);
		final Property mapKeyProperty = npm.findProperty(null, null, CollaborationTools.PROP_CAT_BG_COLLABTOOLS, CollaborationTools.KEY_PORTFOLIO);
		if (mapKeyProperty != null) {
			final Long mapKey = mapKeyProperty.getLongValue();
			final String version = mapKeyProperty.getStringValue();
			if(!"2".equals(version)) {
				final PortfolioStructure map = loadPortfolioStructureByKey(mapKey);
				if(map != null) {
					deletePortfolioStructure(map);
				}
			}
			return true;
		}
		return false;
	}
	
	/**
	 * delete all artefacts from this users including used tags for them
	 * @param ident
	 */
	public void deleteUsersArtefacts(Identity ident){
		List<AbstractArtefact> userArtefacts = artefactManager.getArtefactPoolForUser(ident);
		if (userArtefacts != null){
			for (AbstractArtefact abstractArtefact : userArtefacts) {
				deleteArtefact(abstractArtefact);
			}
		}
	}
	
	public boolean isArtefactClosed(AbstractArtefact artefact) {
		return artefactManager.isArtefactClosed(artefact);
	}

	public PortfolioStructure removeArtefactFromStructure(AbstractArtefact artefact, PortfolioStructure structure) {
		return structureManager.removeArtefactFromStructure(artefact, structure);
	}
	
	/**
	 * Create and persist a link between a structure element and an artefact.
	 * 
	 * @param author The author of the link
	 * @param artefact The artefact to link
	 * @param structure The structure element
	 * @return The link
	 */
	public boolean addArtefactToStructure(Identity author, AbstractArtefact artefact, PortfolioStructure structure) {
		return structureManager.addArtefactToStructure(author, artefact, structure);
	}
	
	/**
	 * move artefact from old to new structure
	 * do so by removing and re-adding to new target
	 * @param artefact
	 * @param oldParStruct
	 * @param newParStruct
	 * @return true if adding was successful
	 */
	public boolean moveArtefactFromStructToStruct(AbstractArtefact artefact, PortfolioStructure oldParStruct, PortfolioStructure newParStruct) {
		return structureManager.moveArtefactFromStructToStruct(artefact, oldParStruct, newParStruct);
	}
	
	public boolean moveArtefactInStruct(AbstractArtefact artefact, PortfolioStructure parStruct, int position) {
		return structureManager.moveArtefactInStruct(artefact, parStruct, position);
	}
	
	
	/**
	 * move a structure to a new parent-structure and removes old link
	 * @param structToBeMvd
	 * @param oldParStruct
	 * @param newParStruct
	 * @return true if no exception occured
	 */
	public boolean moveStructureToNewParentStructure(PortfolioStructure structToBeMvd, PortfolioStructure oldParStruct, PortfolioStructure newParStruct, int destinationPos){
		return structureManager.moveStructureToNewParentStructure(structToBeMvd, oldParStruct, newParStruct, destinationPos);
	}
	
	/**
	 * move a structures order within the same parent, allows manual sorting.
	 * @param structToBeMvd
	 * @param destinationPos where it should be placed
	 * @return true if it went ok, false otherwise
	 */
	public boolean moveStructureToPosition(PortfolioStructure structToBeMvd, int destinationPos){
		return structureManager.reOrderStructures(loadStructureParent(structToBeMvd), structToBeMvd, destinationPos);
	}
	
	/**
	 * set the reflexion for the link structureElement <-> artefact
	 * this can be a different reflexion than the one of the artefact. 
	 * Reflects why the artefact was added to this structure!
	 * @param artefact
	 * @param structure
	 * @param reflexion
	 * @return
	 */
	public boolean setReflexionForArtefactToStructureLink(AbstractArtefact artefact, PortfolioStructure structure, String reflexion){
		return structureManager.setReflexionForArtefactToStructureLink(artefact, structure, reflexion);		
	}
	
	/**
	 * get the reflexion set on the link structureElement <-> artefact
	 * this can be a different reflexion than the one of the artefact. 
	 * Reflects why the artefact was added to this structure!
	 * @param artefact
	 * @param structure
	 * @return String reflexion 
	 */
	public String getReflexionForArtefactToStructureLink(AbstractArtefact artefact, PortfolioStructure structure){
		return structureManager.getReflexionForArtefactToStructureLink(artefact, structure);
	}
	
	/**
	 * counts amount of artefact in all structures and every child element
	 * @param structure
	 * @return
	 */
	public int countArtefactsRecursively(PortfolioStructure structure) {
		return structureManager.countArtefactsRecursively(structure, 0);
	}
	
	public int countArtefactsInMap(PortfolioStructureMap map) {
		return structureManager.countArtefactsRecursively(map);
	}
	
	/**
	 * looks if the given artefact exists in the PortfolioStructure
	 * @param artefact
	 * @param structure
	 * @return
	 */
	public boolean isArtefactInStructure(AbstractArtefact artefact, PortfolioStructure structure){
		return structureManager.isArtefactInStructure(artefact, structure);
	}

	/**
	 * load all artefacts with given businesspath.
	 * setting an Identity to restrict to is optional.
	 * this mostly is just to lookup for existence of already collected artefacts from same source
	 * @param businessPath
	 * @param author (optional)
	 * @return
	 */
	public List<AbstractArtefact> loadArtefactsByBusinessPath(String businessPath, Identity author){
		return artefactManager.loadArtefactsByBusinessPath(businessPath, author);		
	}
	
	public Map<String,Long> getNumOfArtefactsByStartingBusinessPath(String businessPath, IdentityRef author){
		return artefactManager.loadNumOfArtefactsByStartingBusinessPath(businessPath, author);		
	}
	
	/**
	 * List artefacts for indexing
	 * @param author (optional)
	 * @param firstResult (optional)
	 * @param maxResults (optional)
	 * @return
	 */
	public List<AbstractArtefact> getArtefacts(Identity author, int firstResult, int maxResults) {
		return artefactManager.getArtefacts(author, null, firstResult, maxResults);
	}
	
	/**
	 * Load the artefact by its primary key
	 * 
	 * @param key The primary key
	 * @return The artefact or null if nothing found
	 */
	public AbstractArtefact loadArtefactByKey(Long key) {
		return artefactManager.loadArtefactByKey(key);		
	}
	
	/**
	 * get the users choice of attributes or a default
	 * 
	 * @return
	 */
	public Map<String, Boolean> getArtefactAttributeConfig(Identity ident) {
		return settingsManager.getArtefactAttributeConfig(ident);
	}

	/**
	 * persist the users chosen attributes to show as a property
	 * 
	 * @param ident
	 * @param artAttribConfig
	 */
	public void setArtefactAttributeConfig(Identity ident, Map<String, Boolean> artAttribConfig) {
		settingsManager.setArtefactAttributeConfig(ident, artAttribConfig);
	}

	/**
	 * get all persisted filters from a given user
	 * @param ident
	 * @return filtersettings or list with an empty filter, if none were found
	 */
	public List<EPFilterSettings> getSavedFilterSettings(Identity ident){
		return settingsManager.getSavedFilterSettings(ident);
	}
	
	/**
	 * persist users filter settings as property, only save such with a name
	 * @param ident
	 * @param filterList
	 */
	public void setSavedFilterSettings(Identity ident, List<EPFilterSettings> filterList){
		settingsManager.setSavedFilterSettings(ident, filterList);
	}
	
	/**
	 * remove a given filter from users list
	 * @param ident
	 * @param filterName 
	 */
	public void deleteFilterFromUsersList(Identity ident, String filterID){
		settingsManager.deleteFilterFromUsersList(ident, filterID);
	}
	
	/**
	 * get the last selected PortfolioStructure of this user
	 * @param ident Identity
	 * @return the loaded PortfolioStructure
	 */
	public PortfolioStructure getUsersLastUsedPortfolioStructure (Identity ident){
		Long structKey = settingsManager.getUsersLastUsedPortfolioStructureKey(ident);
		if (structKey != null) {
			PortfolioStructure struct = structureManager.loadPortfolioStructureByKey(structKey);
			return struct;
		}
		return null;
	}
	
	/**
	 * get the users prefered viewing mode for artefacts (either table / preview)
	 * @param ident
	 * @return
	 */
	public String getUsersPreferedArtefactViewMode(Identity ident, String context){
		return settingsManager.getUsersPreferedArtefactViewMode(ident, context);
	}
	
	/**
	 * persist the users prefered viewing mode for artefacts (either table / preview)
	 * @param ident
	 * @param preferedMode
	 */
	public void setUsersPreferedArtefactViewMode(Identity ident, String preferedMode, String context){
		settingsManager.setUsersPreferedArtefactViewMode(ident, preferedMode, context);
	}
	
	/**
	 * persist the last uses PortfolioStructure to use it later on
	 * @param ident Identity
	 * @param struct
	 */
	public void setUsersLastUsedPortfolioStructure(Identity ident, PortfolioStructure struct){
		settingsManager.setUsersLastUsedPortfolioStructure(ident, struct);
	}
	
	/**
	 * returns an array of tags for given artefact
	 * 
	 * @param artefact
	 * @return null if none are found
	 */
	public List<String> getArtefactTags(AbstractArtefact artefact) {
		return artefactManager.getArtefactTags(artefact);
	}

	/**
	 * add a tag to an artefact (will save a tag pointing to this artefact)
	 * 
	 * @param identity
	 * @param artefact
	 * @param tag
	 */
	public void setArtefactTag(Identity identity, AbstractArtefact artefact, String tag) {
		artefactManager.setArtefactTag(identity, artefact, tag);
	}

	/**
	 * add a List of tags to an artefact
	 * 
	 * @param identity
	 * @param artefact
	 * @param tags
	 */
	public void setArtefactTags(Identity identity, AbstractArtefact artefact, List<String> tags) {
		artefactManager.setArtefactTags(identity, artefact, tags);
	}

	/**
	 * get all maps wherein (or in sub-structures) the given artefact is linked.
	 * 
	 * @param artefact
	 * @return
	 */
	public List<PortfolioStructure> getReferencedMapsForArtefact(AbstractArtefact artefact) {
		return structureManager.getReferencedMapsForArtefact(artefact);
	}

	/**
	 * get all artefacts for the given identity this represents the artefact pool
	 * 
	 * @param ident
	 * @return
	 */
	public List<AbstractArtefact> getArtefactPoolForUser(Identity ident) {
		return artefactManager.getArtefactPoolForUser(ident);
	}
	
	public EPArtefactTagCloud getArtefactsAndTagCloud(Identity identity, List<String> tags) {
		return artefactManager.getArtefactsAndTagCloud(identity, tags);
	}

	/**
	 * filter the provided list of artefacts with different filters
	 * 
	 * @param allArtefacts the list to manipulate on
	 * @param filterSettings Settings for the filter to work on
	 * @return
	 */
	public List<AbstractArtefact> filterArtefactsByFilterSettings(EPFilterSettings filterSettings, Identity identity, Roles roles) {
		List<Long> artefactKeys = fulltextSearchAfterArtefacts(filterSettings, identity, roles);
		if(artefactKeys == null || artefactKeys.isEmpty()) {
			List<AbstractArtefact> allArtefacts = artefactManager.getArtefactPoolForUser(identity);
			return artefactManager.filterArtefactsByFilterSettings(allArtefacts, filterSettings);
		}
		
		List<AbstractArtefact> artefacts = artefactManager.getArtefacts(identity, artefactKeys, 0, 500);
		// remove the text-filter when the lucene-search got some results before
		EPFilterSettings settings = filterSettings.cloneAfterFullText();
		return artefactManager.filterArtefactsByFilterSettings(artefacts, settings);
	}
	
	private List<Long> fulltextSearchAfterArtefacts(EPFilterSettings filterSettings, Identity identity, Roles roles) {
		String query = filterSettings.getTextFilter();
		if (StringHelper.containsNonWhitespace(query)) {
			try {
				List<String> queries = new ArrayList<String>();
				appendAnd(queries, AbstractOlatDocument.RESERVED_TO, ":\"", identity.getKey().toString(), "\"");
				appendAnd(queries, "(", AbstractOlatDocument.DOCUMENTTYPE_FIELD_NAME, ":(", PortfolioArtefactIndexer.TYPE, "*))");
				SearchResults searchResults = searchClient.doSearch(query, queries, identity, roles, 0, 1000, false);

				List<Long> keys = new ArrayList<Long>();
				if (searchResults != null) {
					String marker = AbstractArtefact.class.getSimpleName();
					for (ResultDocument doc : searchResults.getList()) {
						String businessPath = doc.getResourceUrl();
						int start = businessPath.indexOf(marker);
						if (start > 0) {
							start += marker.length() + 1;
							int stop = businessPath.indexOf(']', start);
							if (stop < businessPath.length()) {
								String keyStr = businessPath.substring(start, stop);
								try {
									keys.add(Long.parseLong(keyStr));
								} catch (Exception e) {
									log.error("Not a primary key: " + keyStr, e);
								}
							}
						}
					}
				}
				return keys;
			} catch (Exception e) {
				log.error("", e);
				return Collections.emptyList();
			}
		} else return Collections.emptyList();
	}

	private void appendAnd(List<String> queries, String... strings) {
		StringBuilder query = new StringBuilder();
		for(String string:strings) {
			query.append(string);
		}
		
		if(query.length() > 0) {
			queries.add(query.toString());
		}
	}

	/**
	 * returns defined amount of users mostly used tags, sorted by occurrence of tag
	 * @param ident
	 * @param amount nr of tags to return, if 0: the default (5) will be
	 *          returned, if -1: you will get all
	 * @return a combined map with tags including occurrence and tag 
	 * format: "house (7), house" 
	 */
	public Map<String, String> getUsersMostUsedTags(Identity ident, Integer amount) {
		amount = (amount == 0) ? 5 : amount;
		List<String> outp = new ArrayList<>();
		
		Map<String, String> res = new HashMap<>();
		List<Map<String, Integer>> bla = taggingManager.getUserTagsWithFrequency(ident);
		for (Map<String, Integer> map : bla) {
			String caption = map.get("tag") + " (" + map.get("nr") + ")";
			outp.add(caption);
			res.put(caption, String.valueOf(map.get("tag")));
			if (amount == res.size()) break;
		}

		return res;
	}
	
	/**
	 * get all tags a user owns, ordered and without duplicates
	 * @param ident
	 * @return
	 */
	public List<String> getUsersTags(Identity ident) {
		return taggingManager.getUserTagsAsString(ident);
	}
	
	/**
	 * get all tags restricted to Artefacts a user owns, ordered and without duplicates
	 * @param ident
	 * @return
	 */
	public List<String> getUsersTagsOfArtefactType(Identity ident) {
		return taggingManager.getUserTagsOfTypeAsString(ident, AbstractArtefact.class.getSimpleName());
	}
	
	
	/**
	 * lookup resources for a given tags
	 * @param tagList
	 * @return
	 */
	public Set<OLATResourceable> getResourcesByTags(List<Tag> tagList) {
		return taggingManager.getResourcesByTags(tagList);
	}

	/**
	 * get all tags for a given resource
	 * @param ores
	 * @return
	 */
	public List<Tag> loadTagsForResource(OLATResourceable ores) {
		return taggingManager.loadTagsForResource(ores, null, null);
	}
	
	/**
	 * sync map with its former source (template)
	*/
	public boolean synchronizeStructuredMapToUserCopy(PortfolioStructureMap map) {
		if(map == null) return false;
		
		final EPStructuredMap userMap = (EPStructuredMap)map;
		Boolean synched = coordinator.getSyncer().doInSync(map.getOlatResource(), new SyncerCallback<Boolean>() {
			public Boolean execute() {
				if (userMap.getStructuredMapSource() == null) { return Boolean.FALSE; }
				// need to reload it, I don't know why
				Long templateKey = userMap.getStructuredMapSource().getKey();
				userMap.setLastSynchedDate(new Date());
				PortfolioStructure template = structureManager.loadPortfolioStructureByKey(templateKey);
				structureManager.syncStructureRecursively(template, userMap, true);
				return Boolean.TRUE;
			}
		});

		return synched.booleanValue();
	}

	/**
	 * Assign a structure map to user. In other words, make a copy of the template
	 * and set the user as an author.
	 * 
	 * @param identity
	 * @param portfolioStructureStructuredMapTemplate
	 */
	public PortfolioStructureMap assignStructuredMapToUser(final Identity identity, final PortfolioStructureMap mapTemplate,
			final RepositoryEntry courseEntry, String targetSubPath, final String targetBusinessPath, final Date deadline) {
		// doInSync is here to check for nested doInSync exception in first place
		final OLATResource ores = courseEntry.getOlatResource();
		final String subPath = targetSubPath;

		PortfolioStructureMap map = coordinator.getSyncer().doInSync(mapTemplate.getOlatResource(), new SyncerCallback<PortfolioStructureMap>() {
			@Override
			public PortfolioStructureMap execute() {
				PortfolioStructureMap template = (PortfolioStructureMap)structureManager.loadPortfolioStructureByKey(mapTemplate.getKey());
				String title = template.getTitle();
				String description = template.getDescription();
				PortfolioStructureMap copy = structureManager
						.createPortfolioStructuredMap(template, identity, title, description, ores, subPath, targetBusinessPath);
				if(copy instanceof EPStructuredMap) {
					((EPStructuredMap)copy).setDeadLine(deadline);
				}
				structureManager.copyStructureRecursively(template, copy, true);
				
				RepositoryEntry referenceEntry = repositoryEntryDao.loadByResourceKey(template.getOlatResource().getKey());
				assessmentService.updateAssessmentEntry(identity, courseEntry, targetSubPath, referenceEntry, AssessmentEntryStatus.inProgress);
				return copy;
			}
		});
		return map;
	}
	
	/**
	 * Low level function to copy the structure of elements, with or without the artefacts
	 * @param source
	 * @param target
	 * @param withArtefacts
	 */
	public void copyStructureRecursively(PortfolioStructure source, PortfolioStructure target, boolean withArtefacts) {
		structureManager.copyStructureRecursively(source, target, withArtefacts);
	}
	
	/**
	 * Return the structure elements of the given type without permission control. Need this for indexing.
	 * @param firstResult
	 * @param maxResults
	 * @param type
	 * @return
	 */
	public List<PortfolioStructure> getStructureElements(int firstResult, int maxResults, ElementType... type) {
		return structureManager.getStructureElements(firstResult, maxResults, type);
	}
	

	/**
	 * get all Structure-Elements linked to identity over a security group (owner)
	 * 
	 * @param ident
	 * @return
	 */
	public List<PortfolioStructure> getStructureElementsForUser(Identity identity, ElementType... type) {
		return structureManager.getStructureElementsForUser(identity, type);
	}
	
	/**
	 * Get all Structure-Elements linked which the identity can see over a policy,
	 * 
	 * @param ident The identity which what see maps
	 * @param chosenOwner Limit maps from this identity
	 * @param type Limit maps to this or these types
	 * @return
	 */
	public List<PortfolioStructure> getStructureElementsFromOthers(final Identity ident, final Identity chosenOwner, final ElementType... type) {
		return structureManager.getStructureElementsFromOthersLimited(ident, chosenOwner, 0, 0, type);
	}
	
	/**
	 * Get part of the Structure-Elements linked which the identity can see over a policy.
	 * The range of elements returned is specified by limitFrom and limitTo (used for paging)
	 * 
	 * @param ident The identity which what see maps
	 * @param chosenOwner Limit maps from this identity
	 * @param limitFrom  Limit maps  
	 * @param limitTo   Limit maps
	 * @param type Limit maps to this or these types
	 * @return
	 */
	public List<PortfolioStructure> getStructureElementsFromOthers(final Identity ident, final Identity chosenOwner, int limitFrom, int limitTo, final ElementType... type) {
		return structureManager.getStructureElementsFromOthersLimited(ident, chosenOwner, limitFrom, limitTo, type);
	}
	
	/**
	 * Get the number of all Structure-Elements linked which the identity can see over a policy,
	 * 
	 * @param ident The identity which what see maps
	 * @param chosenOwner Limit maps from this identity
	 * @param type Limit maps to this or these types
	 * @return
	 */
	public int countStructureElementsFromOthers(final Identity ident, final Identity chosenOwner, final ElementType... types) {
		return structureManager.countStructureElementsFromOthers(ident, chosenOwner, types);
	}

	/**
	 * Get all Structure-Elements linked which the identity can see over a policy,
	 * WITHOUT those that are public to all OLAT users ( GROUP_OLATUSERS )
	 * !! this should be used, to save performance when there are a lot of public shared maps!!
	 * @param ident The identity which what see maps
	 * @param chosenOwner Limit maps from this identity
	 * @param type Limit maps to this or these types
	 * @return
	 */
	public List<PortfolioStructure> getStructureElementsFromOthersWithoutPublic(IdentityRef ident, IdentityRef choosenOwner,
			ElementType... types){
		return structureManager.getStructureElementsFromOthersWithoutPublic(ident, choosenOwner, types);
	}
	
	/**
	 * Return the list of artefacts glued to this structure element
	 * @param structure
	 * @return A list of artefacts
	 */
	public List<AbstractArtefact> getArtefacts(PortfolioStructure structure) {
		return structureManager.getArtefacts(structure);
	}

	/**
	 * FXOLAT-431
	 * 
	 * @param map
	 * @return
	 *
	public List<AbstractArtefact> getAllArtefactsInMap(EPAbstractMap map){
		return structureManager.getAllArtefactsInMap(map);
	}
	*/
	
	/**
	 * get statistics about how much of the required (min, equal) collect-restrictions have been fulfilled.
	 * 
	 * @param structure
	 * @return array with "done" at 0 and "to be done" at 1, or "null" if no restrictions apply
	 */
	public String[] getRestrictionStatistics(PortfolioStructure structure) {
		Integer[] stats = structureManager.getRestrictionStatistics(structure);
		if(stats == null) {
			return null;
		} else {
			return new String[]{stats[0].toString(), stats[1].toString()};
		}
	}
	
	/**
	 * same as getRestrictionStatistics(PortfolioStructure structure) but recursively for a map.
	 * get statistics about how much of the required (min, equal) collect-restrictions have been fulfilled.
	 * 
	 * @param structure
	 * @return array with "done" at 0 and "to be done" at 1, or "null" if no restrictions apply
	 */
	public String[] getRestrictionStatisticsOfMap(final PortfolioStructureMap structure) {
		Integer[] stats = structureManager.getRestrictionStatisticsOfMap(structure, 0, 0);
		
		return new String[]{stats[0].toString(), stats[1].toString()};
	}

	/**
	 * Check the collect restriction against the structure element
	 * @param structure
	 * @return
	 */
	public boolean checkCollectRestriction(PortfolioStructure structure) {
		return structureManager.checkCollectRestriction(structure);
	}
	
	public boolean checkCollectRestrictionOfMap(PortfolioStructureMap structure) {
		return checkAllCollectRestrictionRec(structure);
	}
	
	protected boolean checkAllCollectRestrictionRec(PortfolioStructure structure) {
		boolean allOk = structureManager.checkCollectRestriction(structure);
		List<PortfolioStructure> children = structureManager.loadStructureChildren(structure);
		for(PortfolioStructure child:children) {
			allOk &= checkAllCollectRestrictionRec(child);
		}
		return allOk;
	}
	
	/**
	 * Create a map for a user
	 * @param root
	 * @param identity
	 * @param title
	 * @param description
	 * @return
	 */
	public PortfolioStructureMap createAndPersistPortfolioDefaultMap(Identity identity, String title,
			String description) {
		PortfolioStructureMap map = structureManager.createPortfolioDefaultMap(identity, title, description);
		structureManager.savePortfolioStructure(map);
		return map;
	}
	
	/**
	 * Create a map for a group
	 * @param root
	 * @param group
	 * @param title
	 * @param description
	 * @return
	 */
	public PortfolioStructureMap createAndPersistPortfolioDefaultMap(String title, String description) {
		PortfolioStructureMap map = structureManager.createPortfolioDefaultMap(title, description);
		structureManager.savePortfolioStructure(map);
		return map;
	}

	/**
	 * Create a structured map, based on template.
	 * 
	 * @param identity The author/owner of the map
	 * @param title
	 * @param description
	 * @return The structure element
	 */
	public PortfolioStructureMap createAndPersistPortfolioStructuredMap(PortfolioStructureMap template,
			Identity identity, String title, String description, OLATResourceable targetOres, String targetSubPath, String targetBusinessPath) {
		PortfolioStructureMap map = structureManager.createPortfolioStructuredMap(template, identity, title, description,
				targetOres, targetSubPath, targetBusinessPath);
		structureManager.savePortfolioStructure(map);
		return map;
	}

	/**
	 * create a structure-element
	 * @param root
	 * @param title
	 * @param description
	 * @return
	 */
	public PortfolioStructure createAndPersistPortfolioStructureElement(PortfolioStructure root, String title, String description) {
		EPStructureElement newStruct = (EPStructureElement) structureManager.createPortfolioStructure(root, title, description);
		if (root != null) structureManager.addStructureToStructure(root, newStruct, -1);
		structureManager.savePortfolioStructure(newStruct);
		return newStruct;
	}

	/**
	 * create a page
	 * @param root
	 * @param title
	 * @param description
	 * @return
	 */
	public PortfolioStructure createAndPersistPortfolioPage(PortfolioStructure root, String title, String description) {
		EPPage newPage = (EPPage) structureManager.createPortfolioPage(root, title, description);
		if (root != null) structureManager.addStructureToStructure(root, newPage, -1);
		structureManager.savePortfolioStructure(newPage);
		return newPage;
	}
	
	/**
	 * This method is reserved to the repository. It removes the template
	 * completely
	 * @param pStruct
	 */
	public void deletePortfolioMapTemplate(OLATResourceable res) {
		structureManager.deletePortfolioMapTemplate(res);
	}

	/**
	 * delete a portfoliostructure recursively with its childs
	 * @param pStruct
	 */
	public void deletePortfolioStructure(PortfolioStructure pStruct) {
		structureManager.removeStructureRecursively(pStruct);
	}

	/**
	 * save or update a structure
	 * @param pStruct
	 */
	public void savePortfolioStructure(PortfolioStructure pStruct) {
		structureManager.savePortfolioStructure(pStruct);
	}
	
	/**
	 * Number of children
	 */
	public int countStructureChildren(PortfolioStructure structure) {
		return structureManager.countStructureChildren(structure);
	}
	
	/**
	 * Load a protfolio structure by its resource
	 * @param ores
	 * @return
	 */
	public PortfolioStructure loadPortfolioStructure(OLATResourceable ores) {
		return structureManager.loadPortfolioStructure(ores);
	}
	
	/**
	 * Load a protfolio structure by its resourceable id
	 * @param ores
	 * @return
	 */
	public EPMapShort loadMapShortByResourceId(Long resId) {
		return structureManager.loadMapShortByResourceId(resId);
	}
	
	/**
	 * Load a portfolio structure by its primary key. DON'T USE THIS METHOD
	 * TO RELOAD AN OBJECT. If you want do this, use the method
	 * reloadPortfolioStructure(PortfolioStructure structure)
	 * @param key cannot be null
	 * @return The structure element or null if not found
	 */
	public PortfolioStructure loadPortfolioStructureByKey(Long key){
		return structureManager.loadPortfolioStructureByKey(key);
	}
	
	public PortfolioStructure loadPortfolioStructureByKey(PortfolioStructureRef ref){
		return structureManager.loadPortfolioStructureByKey(ref.getKey());
	}
	
	/**
	 * Reload a portfolio structure
	 * @param structure cannot be null
	 * @return The reloaded structure element
	 */
	public PortfolioStructure reloadPortfolioStructure(PortfolioStructure structure){
		return structureManager.reloadPortfolioStructure(structure);
	}
	
	/**
	 * Load the OLAT resource with the primary of the structure element
	 * @param key cannot be null
	 * @return The resource or null if not found
	 */
	public OLATResource loadOlatResourceFromByKey(Long key) {
		return structureManager.loadOlatResourceFromStructureElByKey(key);
	}
	
	/**
	 * Retrieve the parent of the structure
	 * @param structure
	 * @return
	 */
	public PortfolioStructure loadStructureParent(PortfolioStructureRef structure) {
		return structureManager.loadStructureParent(structure);
	}
	
	
	/**
	 * Retrieve the children structures
	 * @param structure
	 * @return
	 */
	public List<PortfolioStructure> loadStructureChildren(PortfolioStructure structure) {
		return structureManager.loadStructureChildren(structure);
	}
	
	/**
	 * 
	 * @param structure
	 * @param firstResult
	 * @param maxResults
	 * @return
	 */
	public List<PortfolioStructure> loadStructureChildren(PortfolioStructure structure, int firstResult, int maxResults) {
		return structureManager.loadStructureChildren(structure, firstResult, maxResults);
	}

	public PortfolioStructureMap loadPortfolioStructureMap(Identity identity, PortfolioStructureMap template,
			OLATResourceable targetOres, String targetSubPath, String targetBusinessPath){
		//sync the map with the template on opening it in gui, not on loading!
		return structureManager.loadPortfolioStructuredMap(identity, template, targetOres, targetSubPath, targetBusinessPath);
	}
	
	/**
	 * 
	 * @param identity Cannot be null
	 * @param targetOres Cannot be null
	 * @param targetSubPath
	 * @param targetBusinessPath
	 * @return
	 */
	public List<PortfolioStructureMap> loadPortfolioStructureMaps(Identity identity,
			OLATResourceable targetOres, String targetSubPath, String targetBusinessPath){
		//sync the map with the template on opening it in gui, not on loading!
		return structureManager.loadPortfolioStructuredMaps(identity, targetOres, targetSubPath, targetBusinessPath);
	}
	
	/**
	 * get the "already in use" state of a structuredMapTemplate
	 * @param template
	 * @param targetOres
	 * @param targetSubPath
	 * @param targetBusinessPath
	 * @return
	 */
	public boolean isTemplateInUse(PortfolioStructureMap template, OLATResourceable targetOres,
			String targetSubPath, String targetBusinessPath) {
		return structureManager.isTemplateInUse(template, targetOres, targetSubPath, targetBusinessPath);
	}

	/**
	 * get root vfs-container where artefact file-system data is persisted
	 * @return
	 */
	public VFSContainer getArtefactsRoot(){
		return artefactManager.getArtefactsRoot();
	}
	
	/**
	 * get vfs-container of a specific artefact
	 * @param artefact
	 * @return
	 */
	public VFSContainer getArtefactContainer(AbstractArtefact artefact) {
		return artefactManager.getArtefactContainer(artefact);
	}
	
	/**
	 * get a temporary folder to store files while in wizzard
	 * @param ident
	 * @return
	 */
	public VFSContainer getArtefactsTempContainer(Identity ident){
		return artefactManager.getArtefactsTempContainer(ident);
	}

	/**
	 * as large fulltext-content of an artefact is persisted on filesystem, use this method to get fulltext
	 * 
	 * @param artefact
	 * @return
	 */
	public String getArtefactFullTextContent(AbstractArtefact artefact){
		return artefactManager.getArtefactFullTextContent(artefact);
	}
	
	/**
	 * Check if the identity is the owner of this portfolio resource.
	 * @param identity
	 * @param ores
	 * @return
	 */
	public boolean isMapOwner(Identity identity, OLATResourceable ores) {
		return structureManager.isMapOwner(identity, ores);
	}
	
	public boolean isMapOwner(Identity identity, Long mapKey) {
		return structureManager.isMapOwner(identity, mapKey);
	}
	
	/**
	 * Check if the identity is owner of the portfolio resource or
	 * in a valid policy.
	 * @param identity
	 * @param ores
	 * @return
	 */
	public boolean isMapVisible(IdentityRef identity, OLATResourceable ores) {
		return structureManager.isMapVisible(identity, ores);
	}
	
	public boolean isMapShared(PortfolioStructureMap map) {
		return isMapShared(map.getOlatResource());
	}
		
	public boolean isMapShared(OLATResource resource) {
		return policyManager.isMapShared(resource);
	}
	
	/**
	 * Return a list of wrapper containing the read policies of the map
	 * @param map
	 */
	public List<EPMapPolicy> getMapPolicies(PortfolioStructureMap map) {
		return policyManager.getMapPolicies(map);
	}

	/**
	 * Update the map policies of a map. The missing policies are deleted!
	 * @param map
	 * @param policyWrappers
	 */
	public PortfolioStructureMap updateMapPolicies(PortfolioStructureMap map, List<EPMapPolicy> policyWrappers) {
		return policyManager.updateMapPolicies(map, policyWrappers);
	}
	
	/**
	 * submit and close a structured map from a portfolio task
	 * @param map
	 */
	public void submitMap(PortfolioStructureMap map) {
		submitMap(map, true, Role.user);
	}
	
	private void submitMap(PortfolioStructureMap map, boolean logActivity, Role by) {
		if(!(map instanceof EPStructuredMap)) return;//add an exception
		
		EPStructuredMap submittedMap = (EPStructuredMap)map;
		structureManager.submitMap(submittedMap);

		EPTargetResource resource = submittedMap.getTargetResource();
		OLATResourceable courseOres = resource.getOLATResourceable();
		ICourse course = CourseFactory.loadCourse(courseOres);
		AssessmentManager am = course.getCourseEnvironment().getAssessmentManager();
		CourseNode courseNode = course.getRunStructure().getNode(resource.getSubPath());

		List<Identity> owners = policyManager.getOwners(submittedMap);
		for(Identity owner:owners) {
			if (courseNode != null) { // courseNode might have been deleted meanwhile
				IdentityEnvironment ienv = new IdentityEnvironment(); 
				ienv.setIdentity(owner);
				UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
				if(logActivity) {
					am.incrementNodeAttempts(courseNode, owner, uce, by);
				} else {
					am.incrementNodeAttemptsInBackground(courseNode, owner, uce);
				}
				
				RepositoryEntry referenceEntry = courseNode.getReferencedRepositoryEntry();
				RepositoryEntry courseEntry = course.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
				assessmentService.updateAssessmentEntry(owner, courseEntry, courseNode.getIdent(), referenceEntry, AssessmentEntryStatus.inReview);
			}
			assessmentNotificationsHandler.markPublisherNews(owner, course.getResourceableId());
			log.audit("Map " + map + " from " + owner.getKey() + " has been submitted.");
		}
	}
	
	/**
	 * Close all maps after the deadline if there is a deadline. It can be a long running
	 * process if a lot of maps are involved.
	 */
	public void closeMapAfterDeadline() {
		List<PortfolioStructureMap> mapsToClose = structureManager.getOpenStructuredMapAfterDeadline();
		int count = 0;
		for(PortfolioStructureMap mapToClose:mapsToClose) {
			submitMap(mapToClose, false, Role.auto);
			if(count % 5 == 0) {
				// this possibly takes longer than connection timeout, so do intermediatecommits.
				dbInstance.intermediateCommit();
			}
		}
	}
	
	/**
	 * get a valid name of style for a given PortfolioStructure
	 * if style is not enabled anymore, the default will be used.
	 * @param struct
	 * @return the set style or the default from config if nothing is set.
	 */
	public String getValidStyleName(PortfolioStructure struct){
		// first style in list is the default, can be named default.
		List<String> allStyles = portfolioModule.getAvailableMapStyles();
		if (allStyles == null || allStyles.size() == 0) throw new AssertException("at least one style (that also exists in brasato.css must be configured for maps.");
		String styleName = ((EPStructureElement)struct).getStyle();
		if(StringHelper.containsNonWhitespace(styleName) && allStyles.contains(styleName)) {
			return styleName;		
		} 
		return allStyles.get(0); 
	}
	
	/**
	 * The structure will be without any check on the DB copied. All the
	 * children structures MUST be loaded. This method is to use with the
	 * output of XStream at examples.
	 * @param root
	 * @param identity
	 * @return The persisted structure
	 */
	public PortfolioStructureMap importPortfolioMapTemplate(PortfolioStructure root, OLATResource resource) {
		return structureManager.importPortfolioMapTemplate(root, resource);
	}
	
	
	/**
	 * check if given identity has access to this feed.
	 * reverse lookup feed -> artefact -> shared map
	 * @param feed
	 * @param identity
	 * @return
	 */
	public boolean checkFeedAccess(OLATResourceable feed, Identity identity){
		String feedBP = LiveBlogArtefactHandler.LIVEBLOG + feed.getResourceableId() + "]";
		List<AbstractArtefact> artefact = loadArtefactsByBusinessPath(feedBP, null);
		if (artefact != null && artefact.size() == 1) {
			List<PortfolioStructure> linkedMaps = getReferencedMapsForArtefact(artefact.get(0));
			for (PortfolioStructure map : linkedMaps) {
				if (isMapVisible(identity, map)){
					return true;
				}
			}
			// see OLAT-6282: allow the owner of the artefact to view the feed, even if its not any longer in any map.
			if (linkedMaps.size() == 0 && artefact.get(0).getAuthor().equalsByPersistableKey(identity)){
				return true;
			}
		}
		return false;
	}
	
	/**
	 * returns all Owners of the given map as comma-separated list
	 * @param map
	 * @return
	 */
	public String getAllOwnersAsString(PortfolioStructureMap map){
		if(map.getGroups() == null) {
			return null;
		}
		List<Identity> ownerIdents = policyManager.getOwners(map);
		List<String> identNames = new ArrayList<String>();
		for (Identity identity : ownerIdents) {
			String fullName = userManager.getUserDisplayName(identity);
			if(fullName != null) {
				identNames.add(fullName);
			}
		}
		return StringHelper.formatAsCSVString(identNames);
	}
	
	/**
	 * returns the first Owner for the given Map.
	 * 
	 * @param map
	 * @return
	 */
	public String getFirstOwnerAsString(PortfolioStructureMap map){
		if(map.getGroups() == null) {
			return "n/a";
		}
		List<Identity> ownerIdents = policyManager.getOwners(map);
		if(ownerIdents.size() > 0){
			Identity id = ownerIdents.get(0);
			return userManager.getUserDisplayName(id);
		}
		return "n/a";
	}
	
	public String getFirstOwnerAsString(EPMapShort map){
		if(map.getGroups() == null) {
			return "n/a";
		}
		List<Identity> ownerIdents = policyManager.getOwners(map);
		if(ownerIdents.size() > 0){
			Identity id = ownerIdents.get(0);
			return userManager.getUserDisplayName(id);
		}
		return "n/a";
	}
	
	/**
	 * returns the first OwnerIdentity for the given Map.
	 * 
	 * @param map
	 * @return
	 */
	public Identity getFirstOwnerIdentity(PortfolioStructureMap map){
		if(map.getGroups() == null) {
			return null;
		}
		List<Identity> ownerIdents = policyManager.getOwners(map);
		if (ownerIdents.size() > 0) {
			Identity id = ownerIdents.get(0);
			return id;
		}
		return null;
	}
}