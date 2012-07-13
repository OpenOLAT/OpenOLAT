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
package org.olat.admin.user.groups;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.Policy;
import org.olat.core.CoreSpringFactory;
import org.olat.core.gui.components.textboxlist.ResultMapProvider;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.resource.OLATResource;

/**
 * Description:<br>
 * search for groups by an OR search in groups and courses-LR (each title and description)
 * 
 * <P>
 * Initial Date: 02.05.2011 <br>
 * 
 * @author Roman Haag, frentix GmbH, roman.haag@frentix.com
 */
public class GroupSearchResultProvider implements ResultMapProvider {

	private final BusinessGroupService businessGroupService;
	private RepositoryManager repoM;
	private Translator pT;
	private String typeFilter;
	private final Identity identity;
	private static final int MAX_RESULTS = 50;

	public GroupSearchResultProvider(Identity identity, Locale locale, String typeFilter){
		this.identity = identity;
		businessGroupService = CoreSpringFactory.getImpl(BusinessGroupService.class);
		repoM = RepositoryManager.getInstance();
		pT = Util.createPackageTranslator(this.getClass(), locale);
		this.typeFilter = typeFilter;
	}
	
	/**
	 * @see org.olat.core.gui.components.textboxlist.ResultMapProvider#getAutoCompleteContent(java.lang.String,
	 *      java.util.Map)
	 */
	@Override
	public void getAutoCompleteContent(String searchValue, Map<String, String> resMap) {
		Map<Long, String> tempResult = new HashMap<Long, String>();
		
		// split searchterms and search for each of them
		String[] searchTerms = searchValue.split(" ");
		List<String> searchTermsArr = Arrays.asList(searchTerms);
		for (String searchString : searchTermsArr) {
			searchForOneTerm(searchString, tempResult);
		}
		
		// search for the whole multi-word string
		if (searchTermsArr.size() > 1) searchForOneTerm(searchValue, tempResult);		
		
		// build results
		int count = 0;
		for (Entry<Long, String> entry : tempResult.entrySet()) {
			count++;
			Long key = entry.getKey();
			String groupTitle = entry.getValue();			
			resMap.put(groupTitle, String.valueOf(key));
			if (count > MAX_RESULTS) {
				break;
			}
		}
	}
	
	
	private void searchForOneTerm(String searchValue, Map<Long, String> tempResult){
		// search groups itself		
		SearchBusinessGroupParams params = new SearchBusinessGroupParams();
		params.addTypes(typeFilter);
		params.setNameOrDesc('%' + searchValue + '%');
		List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, null, 0, -1);
		for (BusinessGroup group : groups) {			
			if (group.getOwnerGroup() != null && group.getPartipiciantGroup() != null) {
				List<RepositoryEntry> repoEntries = businessGroupService.findRepositoryEntries(groups, 0, -1);
				for (RepositoryEntry rEntry : repoEntries) {
					if (!tempResult.containsKey(group.getKey())) {
						tempResult.put(group.getKey(), getCombinedRepoName(group, rEntry));
					}
				}

				List<Policy> ownerPol = BaseSecurityManager.getInstance().getPoliciesOfSecurityGroup(group.getOwnerGroup());
				for (Policy policy : ownerPol) {
					OLATResource groupRes = policy.getOlatResource();
					RepositoryEntry repoEntry = repoM.lookupRepositoryEntry(groupRes, false);
					if (!tempResult.containsKey(group.getKey())) {
						tempResult.put(group.getKey(), getCombinedRepoName(group, repoEntry));
					}
				}

				if (!tempResult.containsKey(group.getKey())) {
					tempResult.put(group.getKey(), prepareGroupName(group));
				}
			}			
		}
		
		// search by key if it is one
		Long key = null; 
		try {
			key = Long.parseLong(searchValue);			
		} catch (Exception e) {
			// no key
		}
		if (key != null) {
			BusinessGroup group = businessGroupService.loadBusinessGroup(key);
			if (group != null && !tempResult.containsKey(group.getKey())) {
				tempResult.put(group.getKey(), prepareGroupName(group));
			}
		}
		
		// do a search for LR of type course, add all contained groups (learn/right)
		ArrayList<String> courseTypes = new ArrayList<String>(Arrays.asList(CourseModule.getCourseTypeName()));
		Roles searchRoles = new Roles(true, true, true, true, false, true, false);
		List<RepositoryEntry> allRepoEntries = new ArrayList<RepositoryEntry>(); 
		List<RepositoryEntry> repoEntriesByTitle = repoM.genericANDQueryWithRolesRestriction(searchValue, null, null, courseTypes, identity, searchRoles, null);
		List<RepositoryEntry> repoEntriesByDesc = repoM.genericANDQueryWithRolesRestriction(null, null, searchValue, courseTypes, identity, searchRoles, null);
		allRepoEntries.addAll(repoEntriesByDesc);
		allRepoEntries.addAll(repoEntriesByTitle);
		
		for (RepositoryEntry repositoryEntry : allRepoEntries) {
			ICourse course = CourseFactory.loadCourse(repositoryEntry.getOlatResource());
			List<BusinessGroup> allCourseGroups = course.getCourseEnvironment().getCourseGroupManager().getAllBusinessGroups();
			for (BusinessGroup group : allCourseGroups) {
				if (!tempResult.containsKey(group.getKey())) {
					tempResult.put(group.getKey(), getCombinedRepoName(group, repositoryEntry));
				}
			}
		}
	}
	
	
	private String getCombinedRepoName(BusinessGroup group, RepositoryEntry repoEntry) {
		if (repoEntry != null) {
			String groupName = pT.translate("group.result.course", new String[] { repoEntry.getDisplayname(), prepareGroupName(group) } ) ;
			return groupName;
		} else if (group != null) {
			return prepareGroupName(group);
		}
		return "";
	}
	
	private String prepareGroupName(BusinessGroup group) {
		return pT.translate("group.result.group", new String[]{ group.getName() });		
	}
	
}
