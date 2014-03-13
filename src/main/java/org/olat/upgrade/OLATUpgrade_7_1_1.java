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
package org.olat.upgrade;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Constants;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.persistence.DBQuery;
import org.olat.core.commons.services.commentAndRating.CommentAndRatingSecurityCallback;
import org.olat.core.commons.services.commentAndRating.manager.UserCommentsDAO;
import org.olat.core.commons.services.commentAndRating.model.UserComment;
import org.olat.core.id.Identity;
import org.olat.portfolio.PortfolioModule;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.artefacts.AbstractArtefact;
import org.olat.portfolio.model.structel.EPPage;
import org.olat.portfolio.model.structel.EPStructureElement;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.ElementType;
import org.olat.portfolio.model.structel.PortfolioStructure;
import org.olat.resource.OLATResource;
import org.olat.upgrade.model.BGContextImpl;
import org.olat.upgrade.model.BusinessGroupUpgrade;
import org.olat.upgrade.model.RepositoryEntryUpgrade;

/**
 * Description:<br>
 * upgrade code for OLAT 7.1.0 -> OLAT 7.1.1
 * - fixing invalid structures being built by synchronisation, see OLAT-6316 and OLAT-6306
 * - merges all yet found data to last valid node 
 * 
 * <P>
 * Initial Date: 24.03.2011 <br>
 * 
 * @author Roman Haag, roman.haag@frentix.com, www.frentix.com
 */
public class OLATUpgrade_7_1_1 extends OLATUpgrade {


	private static final String TASK_CLEANUP_TEMPLATES = "Cleanup template maps on db directly";
	private static final String TASK_CHECK_AND_FIX_TEMPLATEMAPS = "Check templates and fix portfolio-task child elements";
	private static final String MIGRATE_SECURITY_GROUP = "Migrate repository entry security groups";
	private static final int REPO_ENTRIES_BATCH_SIZE = 20;
	private static final String VERSION = "OLAT_7.1.1";
	private boolean portfolioCourseNodeEnabled;
	private EPFrontendManager ePFMgr;
	private UserCommentsDAO commentAndRatingService;
	private PortfolioModule epfModule;
	
	
	public OLATUpgrade_7_1_1(PortfolioModule epfModule) {
		super();
		this.epfModule = epfModule;		
	}

	public void setPortfolioCourseNodeEnabled(boolean portfolioCourseNodeEnabled){
		this.portfolioCourseNodeEnabled = portfolioCourseNodeEnabled;
	}
	

	@Override
	public boolean doPreSystemInitUpgrade(UpgradeManager upgradeManager) {
		return false;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			// has never been called, initialize
			uhd = new UpgradeHistoryData();
		} else {
			if (uhd.isInstallationComplete()) return false;
		}
		
		if((portfolioCourseNodeEnabled && epfModule.isEnabled())) {
			// get template maps with invalid references and fix them 
			fixInvalidMapReferences(upgradeManager, uhd);
			// remove invalid references on db
			fixInvalidTemplateMaps(upgradeManager, uhd);
		}
		
		migrateSecurityGroups(upgradeManager, uhd);

		uhd.setInstallationComplete(true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
		log.audit("Finished OLATUpgrade_7_1_1 successfully!");
		return true;
	}
	
	
	private void fixInvalidTemplateMaps(UpgradeManager upgradeManager, UpgradeHistoryData uhd){
		if (!uhd.getBooleanDataValue(TASK_CLEANUP_TEMPLATES)) {
			String query = "UPDATE o_ep_struct_el SET struct_el_source=NULL WHERE struct_el_source not in (Select `structure_id` from (Select * from `o_ep_struct_el`) as t) ;";
			executePlainSQLDBStatement(query, upgradeManager.getDataSource());
			
			log.audit("run on DB: removed invalid template references to source-objects");
			
			uhd.setBooleanDataValue(TASK_CLEANUP_TEMPLATES, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	private void fixInvalidMapReferences(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(TASK_CHECK_AND_FIX_TEMPLATEMAPS)) {
			log.audit("+---------------------------------------------------------------------------------------+");
			log.audit("+... check templates and collect lost sub-structures to actualy bound pages/structs  ...+");
			log.audit("+---------------------------------------------------------------------------------------+");
			ePFMgr = (EPFrontendManager) CoreSpringFactory.getBean("epFrontendManager");

			int amount = 10;
			int count = countPortfolioTemplates();
			log.audit("found a total of " + count + " portfolio templates. check stepwise with stepsize: " + amount);
			for (int start = 0; start < count; start = start + amount) {				
				List<PortfolioStructure> templates = ePFMgr.getStructureElements(start, amount, ElementType.TEMPLATE_MAP);
				log.audit("start at " + start + " . Will check (next) " + templates.size() + " portfolio task for irregularities.");
				templates = getInvalidTemplates(templates);
				log.audit("#1: found " + templates.size() + " templates that look invalid and might have produced bad portfolio-tasks!");

				for (PortfolioStructure templateStruct : templates) {
					log.audit(" #2: handling Structured Maps (maps collected from a portfolio task) for template " + templateStruct.getKey() + " "
							+ templateStruct.getTitle());

					// get corresponding portfolio-tasks taken from templates
					List<PortfolioStructure> structMaps = getStructuredMapsLinkedToTemplate(templateStruct);
					log.audit("  this task has been taken " + structMaps.size() + " times.");
					
					for (PortfolioStructure structuredMap : structMaps) {
						log.audit("  #3: now work on StructuredMap: " + structuredMap);	
						processStructuredMapDeeply(structuredMap);
					} // loop struct-maps
					
					// fix template with children
					fixTemplateEntry(templateStruct);
					
					DBFactory.getInstance().intermediateCommit();

				} // loop templates

			} // steps
			DBFactory.getInstance().intermediateCommit();
		}
		uhd.setBooleanDataValue(TASK_CHECK_AND_FIX_TEMPLATEMAPS, true);
		upgradeManager.setUpgradesHistory(uhd, VERSION);
	}

	
	private void processStructuredMapDeeply(PortfolioStructure structuredMap){		
		// check children (StructureElements) first, because of later recursive removal!
		List<PortfolioStructure> linkedChildren = ePFMgr.loadStructureChildren(structuredMap);
		for (PortfolioStructure linkedPage : linkedChildren) {
			log.audit("    #4: processing structs of page: " + linkedPage);
			processStructure(linkedPage);			
		}
		
		log.audit("    #5: processing pages of map: " + structuredMap);
		processStructure(structuredMap);		
	}
	
	/**
	 * call the needed methods to really do the magic.
	 */
	private void processStructure(PortfolioStructure structuredMap) {
		List<PortfolioStructure> linkedChildren = ePFMgr.loadStructureChildren(structuredMap);
		for (PortfolioStructure childStruct : linkedChildren) {
				
			// reload dbchildren in each run, because of filtering
			List<PortfolioStructure> dbChildren;
			if (childStruct instanceof EPPage) {
				dbChildren = loadMapChildrenByInternalFK(childStruct, structuredMap);
			} else {
				dbChildren = loadMapChildrenByInternalFK(childStruct, structuredMap.getRoot());
			}
			

			// filter the struct that is already linked by a struct->struct link
			// and filter such with other struct-source
			filterLinkedStructs(childStruct, dbChildren);
			log.audit("       found " + dbChildren.size() + " faulty children (of " + childStruct + ") linked by internal fk to be merged and removed afterwards.");
		
			// merge artefacts to the linked struct
			mergeLinkedArtefactsToFinalStruct(childStruct, dbChildren);

			// collect comments on structures and merge to target
			mergeCommentsToFinalStruct(childStruct, dbChildren);
			
			// ratings are not merged, see OLAT-6306

			// remove the old elements
			removeWrongElements(dbChildren);
			
			// fix root-reference on EPStructureElements
			if (!(childStruct instanceof EPPage)) {
				if (childStruct.getRoot().equals(childStruct.getRootMap())) {
					EPStructureElement realParentRoot = (EPStructureElement) ePFMgr.loadStructureParent(childStruct);
					((EPStructureElement)childStruct).setRoot(realParentRoot);
					ePFMgr.savePortfolioStructure(childStruct);
				}
			}

		} // loop children
		
		DBFactory.getInstance().intermediateCommit();
	}
	
	
	
	
	private void fixTemplateEntry(PortfolioStructure template){
		List<PortfolioStructure> temps = new ArrayList<PortfolioStructure>();
		recurseIntoTemplateAndCheck(new ArrayList<PortfolioStructure>(), temps, template);
		for (PortfolioStructure portfolioStructure : temps) {
			((EPStructureElement)portfolioStructure).setStructureElSource(null);
			ePFMgr.savePortfolioStructure(portfolioStructure);
		}
	}
	
	private void removeWrongElements(List<PortfolioStructure> wrongStructs){
		for (PortfolioStructure portfolioStructure : wrongStructs) {
			ePFMgr.deletePortfolioStructure(portfolioStructure);			
		}		
	}
	
	private List<PortfolioStructure> getInvalidTemplates(List<PortfolioStructure> templates) {
		List<PortfolioStructure> temps = new ArrayList<PortfolioStructure>();
		for (PortfolioStructure portfolioStructure : templates) {
			recurseIntoTemplateAndCheck(temps, new ArrayList<PortfolioStructure>(),portfolioStructure);
		}
		// get unique root-templates 
		HashSet<PortfolioStructure> h = new HashSet<PortfolioStructure>(temps);
		temps.clear();
		temps.addAll(h);
		return temps;
	}
	
	private void recurseIntoTemplateAndCheck(List<PortfolioStructure> resultingMaps, List<PortfolioStructure> resultingStructs, PortfolioStructure struct){
		List<PortfolioStructure> children = ePFMgr.loadStructureChildren(struct);
		if (children == null || children.isEmpty()) return;
		
		for (PortfolioStructure portfolioStructure : children) {
			if (((EPStructureElement)portfolioStructure).getStructureElSource() != null) {
				resultingMaps.add(portfolioStructure.getRootMap());
				resultingStructs.add(portfolioStructure);
			}
				
			recurseIntoTemplateAndCheck(resultingMaps, resultingStructs, portfolioStructure);			
		}	
		return;		
	}
	
	private void mergeCommentsToFinalStruct(PortfolioStructure finalStruct, List<PortfolioStructure> wrongStructs){
		if (wrongStructs == null || wrongStructs.isEmpty()) return;
		
		List<UserComment> collectedComments = new ArrayList<UserComment>();
		// collect all comments out there
		for (PortfolioStructure portfolioStructure : wrongStructs) {
			if (!(portfolioStructure instanceof EPPage)) return; // no comments on StructureElements!
			List<UserComment> oldComments = commentAndRatingService.getComments(portfolioStructure.getRootMap(), portfolioStructure.getKey().toString());
			collectedComments.addAll(oldComments);
			commentAndRatingService.deleteAllComments(portfolioStructure.getRootMap(), portfolioStructure.getKey().toString());
		}
		log.audit("       found " + collectedComments.size() + " comments for this structure, will be merged to new destination.");
		
		if (collectedComments.size() == 0) return;		

		Identity ident = collectedComments.get(0).getCreator();
		UserComment topComment = commentAndRatingService.createComment(ident, finalStruct.getRootMap(), finalStruct.getKey().toString(), "The following comments were restored from a migration task to rescue lost data.");
		// attach all to this info-comment
		for (UserComment userComment : collectedComments) {			
			UserComment attachedComment = commentAndRatingService.replyTo(topComment, userComment.getCreator(), userComment.getComment());
			// set original date
			attachedComment.setCreationDate(userComment.getCreationDate());
			commentAndRatingService.updateComment(attachedComment, attachedComment.getComment());
		}		
	}
	
	private void mergeLinkedArtefactsToFinalStruct(PortfolioStructure finalStruct, List<PortfolioStructure> wrongStructs){
		if (wrongStructs == null || wrongStructs.isEmpty()) return;
		// temporarily remove the collectrestriction, will be added by next sync. 
		// TODO: somehow user must be warned, that map may contain too much artefacts on one node
		finalStruct.getCollectRestrictions().clear();
		ePFMgr.savePortfolioStructure(finalStruct);
		int artefactCount = 0;
		for (PortfolioStructure portfolioStructure : wrongStructs) {
			portfolioStructure = ePFMgr.loadPortfolioStructureByKey(portfolioStructure.getKey());
			List<AbstractArtefact> artefacts = ePFMgr.getArtefacts(portfolioStructure);
			for (AbstractArtefact abstractArtefact : artefacts) {
				if (!ePFMgr.isArtefactInStructure(abstractArtefact, finalStruct)){
					artefactCount++;
					ePFMgr.moveArtefactFromStructToStruct(abstractArtefact, portfolioStructure, finalStruct);
				} else {
					log.audit("An Artefact " + abstractArtefact + " has already been added to new target, therefore will be removed from wrong structure.");
					// TODO: maybe we should save the reflexion on the link artefact -> structure!
					ePFMgr.removeArtefactFromStructure(abstractArtefact, portfolioStructure);					
				}
			}			
		}
		log.audit("       merged " + artefactCount + " artefacts to new destinations.");
	}
	
	
	private void filterLinkedStructs(PortfolioStructure filterBase, List<PortfolioStructure> dbChildren){
		for (Iterator<PortfolioStructure> iterator = dbChildren.iterator(); iterator.hasNext();) {
			PortfolioStructure portfolioStructure = iterator.next();
			long filterBaseSourceKey = ((EPStructureElement)filterBase).getStructureElSource();
			long structSourceKey = ((EPStructureElement)portfolioStructure).getStructureElSource();
			
			if (portfolioStructure.getKey() == filterBase.getKey() || filterBaseSourceKey != structSourceKey){
				iterator.remove();
			}
		}		
	}	
	
	
	// helper to get child not by link, but internal direct link to root-element
	private List<PortfolioStructure> loadMapChildrenByInternalFK(PortfolioStructure linkedChildStruct, PortfolioStructure root){
		if (root == null) return null;
		StringBuilder sb = new StringBuilder();
		sb.append("select stEl from ").append(EPStructureElement.class.getName()).append(" stEl");
//		sb.append(" where stEl.root=:rootEl and stEl.structureElSource=:sourceEl"); // filtered by filterLinkedStructs()
		sb.append(" where stEl.root=:rootEl");
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setEntity("rootEl", root);
//		query.setLong("sourceEl", ((EPStructureElement)linkedChildStruct).getStructureElSource());
		@SuppressWarnings("unchecked")
		List<PortfolioStructure> resources = query.list();
		return resources;		
	}
	
	private int countPortfolioTemplates(){
		StringBuilder sb = new StringBuilder();
		sb.append("select count(stEl) from ").append(EPStructureElement.class.getName()).append(" stEl");
		sb.append(" where stEl.class in (" + EPStructuredMapTemplate.class.getName() + ")");
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		Number count = (Number)query.uniqueResult();
		return count.intValue();
	}
	
	private List<PortfolioStructure> getStructuredMapsLinkedToTemplate(PortfolioStructure template){
		StringBuilder sb = new StringBuilder();
		sb.append("select map from ").append(EPStructuredMap.class.getName()).append(" map")
			.append(" where map.structuredMapSource=:template");
		
		DBQuery query = DBFactory.getInstance().createQuery(sb.toString());
		query.setEntity("template", template);
		@SuppressWarnings("unchecked")
		List<PortfolioStructure> maps = query.list();
		return maps;
	}
	

	//fxdiff VCRP-1: access control repository entry
	private void migrateSecurityGroups(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		if (!uhd.getBooleanDataValue(MIGRATE_SECURITY_GROUP)) {
			log.audit("+-----------------------------------------------------------------------------+");
			log.audit("+... Migrate the repository entry security groups from business groups     ...+");
			log.audit("+-----------------------------------------------------------------------------+");

			int counter = 0;
			List<RepositoryEntryUpgrade> entries;
			do {
				entries = queryEntries(counter);
				for(RepositoryEntryUpgrade entry:entries) {
					createRepoEntrySecurityGroups(entry);
					migrateRepoEntrySecurityGroups(entry);
				}
				counter += entries.size();
				log.audit("Processed entries: " + entries.size());
			} while(entries.size() == REPO_ENTRIES_BATCH_SIZE);
			
			log.audit("+... Migration processed " + counter + " repository entries     ...+");

			uhd.setBooleanDataValue(MIGRATE_SECURITY_GROUP, true);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
	}
	
	private void createRepoEntrySecurityGroups(RepositoryEntryUpgrade entry) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		
		boolean save = false;
		if(entry.getTutorGroup() == null) {
			// security group for tutors / coaches
			SecurityGroup tutorGroup = securityManager.createAndPersistSecurityGroup();
			// member of this group may modify member's membership
			securityManager.createAndPersistPolicy(tutorGroup, Constants.PERMISSION_ACCESS, entry.getOlatResource());
			// members of this group are always tutors also
			securityManager.createAndPersistPolicy(tutorGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_TUTOR);
			entry.setTutorGroup(tutorGroup);
	
			securityManager.createAndPersistPolicy(entry.getTutorGroup(), Constants.PERMISSION_COACH, entry.getOlatResource());

			DBFactory.getInstance().commit();
			save = true;
		}
		
		if(entry.getParticipantGroup() == null) {
			// security group for participants
			SecurityGroup participantGroup = securityManager.createAndPersistSecurityGroup();
			// member of this group may modify member's membership
			securityManager.createAndPersistPolicy(participantGroup, Constants.PERMISSION_ACCESS, entry.getOlatResource());
			// members of this group are always participants also
			securityManager.createAndPersistPolicy(participantGroup, Constants.PERMISSION_HASROLE, Constants.ORESOURCE_PARTICIPANT);
			entry.setParticipantGroup(participantGroup);
	
			securityManager.createAndPersistPolicy(entry.getParticipantGroup(), Constants.PERMISSION_PARTI, entry.getOlatResource());

			DBFactory.getInstance().commit();
			save = true;
		}
		
		if(save) {
			DBFactory.getInstance().updateObject(entry);
		}
	}
	
	private void migrateRepoEntrySecurityGroups(RepositoryEntryUpgrade entry) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();

		List<BGContextImpl> contexts = findBGContextsForResource(entry.getOlatResource(), true, true);
		for(BGContextImpl context:contexts) {
			List<BusinessGroupUpgrade> groups = getGroupsOfBGContext(context);
			for(BusinessGroupUpgrade group:groups) {
				//migrate tutors
				if(group.getOwnerGroup() != null) {
					int count = 0;
					List<Identity> owners = securityManager.getIdentitiesOfSecurityGroup(group.getOwnerGroup());
					SecurityGroup tutorGroup = entry.getTutorGroup();
					for(Identity owner:owners) {
						if(securityManager.isIdentityInSecurityGroup(owner, tutorGroup)) {
							continue;
						}
						securityManager.addIdentityToSecurityGroup(owner, tutorGroup);
						if(count++ % 20 == 0) {
							DBFactory.getInstance().intermediateCommit();
						}
					}
					DBFactory.getInstance().intermediateCommit();
				}
				
				//migrate participants
				if(group.getPartipiciantGroup() != null) {
					int count = 0;
					List<Identity> participants = securityManager.getIdentitiesOfSecurityGroup(group.getPartipiciantGroup());
					SecurityGroup participantGroup = entry.getParticipantGroup();
					for(Identity participant:participants) {
						if(securityManager.isIdentityInSecurityGroup(participant, participantGroup)) {
							continue;
						}
						securityManager.addIdentityToSecurityGroup(participant, participantGroup);
						if(count++ % 20 == 0) {
							DBFactory.getInstance().intermediateCommit();
						}
					}
	
					DBFactory.getInstance().intermediateCommit();
				}
			}
		}
	}
	
	private List<BusinessGroupUpgrade> getGroupsOfBGContext(BGContextImpl bgContext) {
		String q = "select bg from org.olat.upgrade.model.BusinessGroupImpl bg where bg.groupContextKey = :contextKey";
		DBQuery query = DBFactory.getInstance().createQuery(q);
		query.setLong("contextKey", bgContext.getKey());
		@SuppressWarnings("unchecked")
		List<BusinessGroupUpgrade> groups = query.list();
		return groups;
	}
	
	public List<RepositoryEntryUpgrade> queryEntries(int firstResult) {
		StringBuilder sb = new StringBuilder();
		sb.append("select v from ").append(RepositoryEntryUpgrade.class.getName()).append(" v inner join fetch v.olatResource as res order by v.key asc");
		DBQuery dbquery = DBFactory.getInstance().createQuery(sb.toString());
		dbquery.setFirstResult(firstResult);
		dbquery.setMaxResults(REPO_ENTRIES_BATCH_SIZE);
		@SuppressWarnings("unchecked")
		List<RepositoryEntryUpgrade> entries = dbquery.list();
		return entries;
	}
	
	private List<BGContextImpl> findBGContextsForResource(OLATResource resource, boolean defaultContexts, boolean nonDefaultContexts) {
		DB db = DBFactory.getInstance();
		StringBuilder q = new StringBuilder();
		q.append(" select context from org.olat.group.context.BGContextImpl as context,");
		q.append(" org.olat.group.context.BGContext2Resource as bgcr");
		q.append(" where bgcr.resource = :resource");
		q.append(" and bgcr.groupContext = context");

		boolean checkDefault = defaultContexts != nonDefaultContexts;
		if (checkDefault){
			q.append(" and context.defaultContext = :isDefault");
		}
		DBQuery query = db.createQuery(q.toString());
		query.setEntity("resource", resource);
		if (checkDefault){
			query.setBoolean("isDefault", defaultContexts ? true : false);
		}
		@SuppressWarnings("unchecked")
		List<BGContextImpl> contexts = query.list();
		return contexts;
	}

	

	@Override
	public String getVersion() {
		return VERSION;
	}


	/**
	 * allow everything to do with comments/rating
	 */
	public class FullCommentAndRatingSecCallback implements CommentAndRatingSecurityCallback {
		
		public boolean canViewComments() {
			return true;
		}

		public boolean canCreateComments() {
			return true;
		}

		public boolean canReplyToComment(UserComment comment) {
			return true;
		}

		public boolean canUpdateComment(UserComment comment, List<UserComment> allComments) {
			return true;
		}

		public boolean canDeleteComment(UserComment comment) {
			return true;
		}

		public boolean canViewRatingAverage() {
			return true;
		}

		public boolean canViewOtherUsersRatings() {
			return true;
		}

		public boolean canRate() {
			return true;
		}

	}
	
}
