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

package org.olat.portfolio;

import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.id.OLATResourceable;
import org.olat.portfolio.manager.EPFrontendManager;
import org.olat.portfolio.model.structel.EPDefaultMap;
import org.olat.portfolio.model.structel.EPStructuredMap;
import org.olat.portfolio.model.structel.EPStructuredMapTemplate;
import org.olat.portfolio.model.structel.PortfolioStructureMap;
import org.olat.portfolio.model.structel.StructureStatusEnum;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;


/**
 * 
 * Description:<br>
 * EPSecurityCallbackFactory
 * 
 * <P>
 * Initial Date:  18 nov. 2010 <br>
 * @author srosse
 */
public class EPSecurityCallbackFactory {
	

	public static EPSecurityCallback getSecurityCallback(UserRequest ureq, PortfolioStructureMap map, EPFrontendManager ePFMgr) {
		if(map instanceof EPDefaultMap) {
			return getSecurityCallbackMap(ureq, map, ePFMgr);	
		} else if (map instanceof EPStructuredMap) {
			return getSecurityCallbackStructuredMap(ureq, map, ePFMgr);
		} else if (map instanceof EPStructuredMapTemplate) {
			return getSecurityCallbackTemplate(ureq, map, ePFMgr);
		}
		return new EPSecurityCallbackOwner(false, false, false);
	}
	
	public static boolean isLockNeeded(EPSecurityCallback secCallback) {
		return secCallback.canAddArtefact() || secCallback.canAddPage() || secCallback.canAddStructure()
			|| secCallback.canEditStructure() || secCallback.canEditReflexion();
	}
	
	public static EPSecurityCallback updateAfterFailedLock(EPSecurityCallback secCallback) {
		boolean canEditStructure = false;
		boolean canEditReflexion = false;
		boolean canEditTags = false;
		boolean canShare = secCallback.canShareMap();
		boolean canAddArtefact = false;
		boolean canRemoveArtefactFromStruct = false;
		boolean canAddStructure = false;
		boolean canAddPage = false;
		boolean canView = secCallback.canView();
		boolean canCommentAndRate = secCallback.canCommentAndRate();
		boolean canSubmitAssess = false;
		boolean restrictionsEnabled = secCallback.isRestrictionsEnabled();
		boolean isOwner = secCallback.isOwner();

		return new EPSecurityCallbackImpl(canEditStructure, canEditReflexion, canEditTags, canShare, canAddArtefact, canRemoveArtefactFromStruct, canAddStructure, canAddPage,
				canView, canCommentAndRate, canSubmitAssess, restrictionsEnabled, isOwner);
	}
	
	/**
	 * EPDefault: owner can edit them (add structure, artefacts), viewers can comments
	 * @param ureq
	 * @param map
	 * @param ePFMgr
	 * @return
	 */
	protected static EPSecurityCallback getSecurityCallbackMap(UserRequest ureq, PortfolioStructureMap map, EPFrontendManager ePFMgr) {
		boolean isOwner = ePFMgr.isMapOwner(ureq.getIdentity(), map.getOlatResource());
		boolean isVisible = ePFMgr.isMapVisible(ureq.getIdentity(), map.getOlatResource());
		
		boolean canEditStructure = isOwner;
		boolean canEditReflexion = isOwner;
		boolean canEditTags = isOwner;
		boolean canShare = isOwner;
		boolean canAddArtefact = isOwner;
		boolean canRemoveArtefactFromStruct = isOwner;
		boolean canAddStructure = isOwner;
		boolean canAddPage = isOwner;
		boolean canView = isVisible;
		boolean canCommentAndRate = isVisible || isOwner;
		boolean canSubmitAssess = false;
		boolean restrictionsEnabled = false;
		
		return new EPSecurityCallbackImpl(canEditStructure, canEditReflexion, canEditTags, canShare, canAddArtefact, canRemoveArtefactFromStruct, canAddStructure, canAddPage,
				canView, canCommentAndRate, canSubmitAssess, restrictionsEnabled, isOwner);
	}
	
	/**
	 * EPStructuredMap: owner can edit as long as map is not closed
	 * @param ureq
	 * @param map
	 * @param ePFMgr
	 * @return
	 */
	protected static EPSecurityCallback getSecurityCallbackStructuredMap(UserRequest ureq, PortfolioStructureMap map, EPFrontendManager ePFMgr) {
		boolean isOwner = ePFMgr.isMapOwner(ureq.getIdentity(), map.getOlatResource());
		boolean isCoach = false;
		boolean isVisible = ePFMgr.isMapVisible(ureq.getIdentity(), map.getOlatResource());
		boolean open = !StructureStatusEnum.CLOSED.equals(map.getStatus());
		
		boolean canEditStructure = false;
		boolean canEditReflexion = isOwner && open;
		boolean canEditTags = isOwner && open;
		boolean canShare = (isOwner || isCoach);
		boolean canAddArtefact = isOwner && open;
		boolean canRemoveArtefactFromStruct = isOwner && open;
		boolean canAddStructure = false;
		boolean canAddPage = false;
		boolean canView = isVisible || isCoach;
		boolean canCommentAndRate = isVisible || isCoach || isOwner;
		boolean canSubmitAssess = isOwner;
		boolean restrictionsEnabled = true;
		
		return new EPSecurityCallbackImpl(canEditStructure, canEditReflexion, canEditTags, canShare, canAddArtefact, canRemoveArtefactFromStruct, canAddStructure, canAddPage,
				canView, canCommentAndRate, canSubmitAssess, restrictionsEnabled, isOwner);
	}
	
	/**
	 * Owner or admin have the right to edit structure if the flag CLOSED is not set. Their
	 * some restrictions if the map is already in use.
	 * @param ureq
	 * @param map
	 * @param ePFMgr
	 * @return
	 */
	protected static EPSecurityCallback getSecurityCallbackTemplate(UserRequest ureq, PortfolioStructureMap map, EPFrontendManager ePFMgr) {
		RepositoryService repositoryService = CoreSpringFactory.getImpl(RepositoryService.class);
		RepositoryManager repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		
		OLATResourceable mres = map.getOlatResource();
		RepositoryEntry repoEntry = repositoryManager.lookupRepositoryEntry(mres, false);
		
		boolean isAdministrator = repositoryService.hasRoleExpanded(ureq.getIdentity(), repoEntry,
				OrganisationRoles.administrator.name(), OrganisationRoles.learnresourcemanager.name());

		//owner of repository entry or owner of map is the same
		boolean isOwner = repositoryService.hasRole(ureq.getIdentity(), repoEntry, GroupRoles.owner.name())
				|| ePFMgr.isMapOwner(ureq.getIdentity(), map.getOlatResource());
		boolean canLaunch = isAdministrator || isOwner ||
				repositoryManager.isAllowedToLaunch(ureq.getIdentity(), ureq.getUserSession().getRoles(), repoEntry);
		boolean open = !StructureStatusEnum.CLOSED.equals(map.getStatus());
		
		boolean canEditStructure = (isOwner || isAdministrator) && open;
		boolean canEditReflexion = isOwner && open;
		boolean canEditTags = isOwner && open;
		boolean canShare = false;
		boolean canAddArtefact = false;
		boolean canRemoveArtefactFromStruct = (isOwner || isAdministrator) && open;
		boolean canAddStructure = (isOwner || isAdministrator) && open;
		boolean canAddPage = (isOwner || isAdministrator) && open;
		boolean canView = canLaunch;
		boolean canCommentAndRate = false;
		boolean canSubmitAssess = false;
		boolean restrictionsEnabled = true;//for author

		return new EPSecurityCallbackImpl(canEditStructure, canEditReflexion, canEditTags, canShare, canAddArtefact, canRemoveArtefactFromStruct, canAddStructure, canAddPage,
				canView, canCommentAndRate, canSubmitAssess, restrictionsEnabled, isOwner);
	}
}
