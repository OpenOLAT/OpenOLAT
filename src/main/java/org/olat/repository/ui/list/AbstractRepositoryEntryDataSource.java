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
package org.olat.repository.ui.list;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.ResultInfos;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataSourceDelegate;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntryLifecycle;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.repository.ui.PriceMethod;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 28.01.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractRepositoryEntryDataSource implements FlexiTableDataSourceDelegate<RepositoryEntryRow> {
	
	private final Identity identity;
	private RepositoryEntryDataSourceUIFactory uifactory;
	
	protected final ACService acService;
	protected final MarkManager markManager;
	protected final UserManager userManager;
	protected final CoachingService coachService;
	protected final BaseSecurity securityManager;
	protected final RepositoryManager repositoryManager;
	protected final UserCourseInformationsManager courseInfoManager;
	
	private Integer count;
	
	public AbstractRepositoryEntryDataSource(Identity identity, RepositoryEntryDataSourceUIFactory uifactory) {
		this.identity = identity;
		this.uifactory = uifactory;
		
		acService = CoreSpringFactory.getImpl(ACService.class);
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		coachService = CoreSpringFactory.getImpl(CoachingService.class);
		securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		courseInfoManager = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);
	}
	
	public Identity getIdentity() {
		return identity;
	}

	@Override
	public int getRowCount() {
		if(count == null) {
			count = getNumOfRepositoryEntries();
		}
		return count.intValue();
	}
	
	protected abstract int getNumOfRepositoryEntries();

	@Override
	public List<RepositoryEntryRow> reload(List<RepositoryEntryRow> rows) {
		return Collections.emptyList();
	}

	@Override
	public abstract ResultInfos<RepositoryEntryRow> getRows(String query, List<String> condQueries, int firstResult, int maxResults,
			SortKey... orderBy);
	
	protected List<RepositoryEntryRow> processModel(List<RepositoryEntry> repoEntries) {
		List<OLATResource> resources = new ArrayList<OLATResource>(repoEntries.size());
		Set<Long> markedResources = new HashSet<Long>(repoEntries.size() * 2 + 1);
		for(RepositoryEntry entry:repoEntries) {
			resources.add(entry.getOlatResource());
			markedResources.add(entry.getResourceableId());
		}

		List<UserCourseInformations> courseInfos = courseInfoManager.getUserCourseInformations(identity, resources);
		List<RepositoryEntryMembership> authors = repositoryManager.getOwnersMembership(repoEntries);
		
		Set<Long> authorKeys = new HashSet<>();
		for(RepositoryEntryMembership membership:authors) {
			authorKeys.add(membership.getIdentityKey());
		}
		Map<Long,String> authorNames = userManager.getUserDisplayNamesByKey(authorKeys);
		markManager.filterMarks(identity, "RepositoryEntry", markedResources);
		List<OLATResourceAccess> resourcesWithOffer = acService.filterRepositoryEntriesWithAC(repoEntries);
		List<EfficiencyStatementEntry> statements = coachService.getEfficencyStatements(identity, repoEntries);

		List<RepositoryEntryRow> items = new ArrayList<RepositoryEntryRow>();
		for(RepositoryEntry entry:repoEntries) {
			RepositoryEntryRow details = new RepositoryEntryRow();
			details.setKey(entry.getKey());
			details.setDisplayName(entry.getDisplayname());
			details.setDescription(entry.getDescription());
			details.setMarked(markedResources.contains(entry.getResourceableId()));
			details.setOLATResourceable(OresHelper.clone(entry.getOlatResource()));
			
			RepositoryEntryLifecycle lifecycle = entry.getLifecycle();
			if(lifecycle != null) {
				details.setLifecycleStart(lifecycle.getValidFrom());
				details.setLifecycleEnd(lifecycle.getValidTo());
				if(!lifecycle.isPrivateCycle()) {
					details.setLifecycle(lifecycle.getLabel());
					details.setLifecycleSoftKey(lifecycle.getSoftKey());
				}
			}
			
			uifactory.forgeMarkLink(details);
			uifactory.forgeSelectLink(details);
			uifactory.forgeStartLink(details);
			uifactory.forgeDetailsLink(details);
			uifactory.forgeRatings(details, entry);

			VFSLeaf image = repositoryManager.getImage(entry);
			if(image != null) {
				details.setThumbnailRelPath(uifactory.getMapperThumbnailUrl() + "/" + image.getName());
			}
			
			StringBuilder sb = new StringBuilder();
			for(RepositoryEntryMembership membership:authors) {
				if(membership.isOwner() && entry.getKey().equals(membership.getRepoKey())) {
					String authorName = authorNames.get(membership.getIdentityKey());
					if(StringHelper.containsNonWhitespace(authorName)) {
						if(sb.length() > 0) {
							sb.append(" &amp; ");
						}
						sb.append(authorName);
					}
				}
			}
			if(sb.length() > 0) {
				details.setAuthor(sb.toString());
			}
			
			
			
			for(UserCourseInformations courseInfo:courseInfos) {
				if(courseInfo.getResource().equals(entry.getOlatResource())) {
					details.setInitialLaunch(courseInfo.getInitialLaunch());
					details.setRecentLaunch(courseInfo.getRecentLaunch());
					details.setVisit(courseInfo.getVisit());
					details.setTimeSpend(courseInfo.getTimeSpend());
				}
			}
			
			for(EfficiencyStatementEntry statement:statements) {
				if(statement.getUserEfficencyStatement() != null && statement.getCourse().equals(entry)) {
					UserEfficiencyStatement userStatement = statement.getUserEfficencyStatement();

					details.setPassed(userStatement.getPassed());
					details.setScore(AssessmentHelper.getRoundedScore(userStatement.getScore()));
				}
			}
			
			List<PriceMethod> types = new ArrayList<PriceMethod>();
			if (entry.isMembersOnly()) {
				// members only always show lock icon
				types.add(new PriceMethod("", "b_access_membersonly_icon"));
			} else {
				// collect access control method icons
				OLATResource resource = entry.getOlatResource();
				for(OLATResourceAccess resourceAccess:resourcesWithOffer) {
					if(resource.getKey().equals(resourceAccess.getResource().getKey())) {
						for(PriceMethodBundle bundle:resourceAccess.getMethods()) {
							String type = (bundle.getMethod().getMethodCssClass() + "_icon").intern();
							String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
							types.add(new PriceMethod(price, type));
						}
					}
				}
			}
			
			if(!types.isEmpty()) {
				details.setAccessTypes(types);
			}
			
			items.add(details);
			
		}
		return items;
	}
}
