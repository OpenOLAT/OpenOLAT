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
package org.olat.repository.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.IdentityShort;
import org.olat.basesecurity.SecurityGroup;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.UserCourseInformations;
import org.olat.course.assessment.UserEfficiencyStatement;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.modules.coach.manager.CoachingService;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.ui.ProgressValue;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.SearchRepositoryEntryParameters;
import org.olat.repository.model.RepositoryEntryMembership;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;

/**
 * 
 * Initial date: 20.11.2012<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class AbstractRepositoryEntryListController extends BasicController {
	
	private final Panel mainPanel;
	private final VelocityContainer wideVC;
	private final VelocityContainer condensedVC;
	
	private Link allCoursesLink;
	
	private final MarkManager markManager;
	private final UserManager userManager;
	private final CoachingService coachService;
	private final BaseSecurityManager securityManager;
	private final RepositoryManager repositoryManager;
	private final UserCourseInformationsManager courseInfoManager;
	
	private SearchRepositoryEntryParameters currentParams;
	
	private Map<Long,String> namesCache = new HashMap<Long,String>();

	public AbstractRepositoryEntryListController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(RepositoryManager.class, getLocale(), getTranslator()));
		
		markManager = CoreSpringFactory.getImpl(MarkManager.class);
		userManager = CoreSpringFactory.getImpl(UserManager.class);
		coachService = CoreSpringFactory.getImpl(CoachingService.class);
		securityManager = CoreSpringFactory.getImpl(BaseSecurityManager.class);
		repositoryManager = CoreSpringFactory.getImpl(RepositoryManager.class);
		courseInfoManager = CoreSpringFactory.getImpl(UserCourseInformationsManager.class);

		String mapperThumbnailUrl = registerCacheableMapper(ureq, "repositoryentryImage", new RepositoryEntryImageMapper());
		
		String velocityRoot = Util.getPackageVelocityRoot(AbstractRepositoryEntryListController.class);
		wideVC = new VelocityContainer("vc_list_wide", velocityRoot + "/list_1.html", getTranslator(), this);
		wideVC.contextPut("thumbnails", Boolean.TRUE);
		wideVC.contextPut("mapperURL", mapperThumbnailUrl);
		condensedVC = new VelocityContainer("vc_list_condensed", velocityRoot + "/list_3.html", getTranslator(), this);
		condensedVC.contextPut("thumbnails", Boolean.TRUE);
		condensedVC.contextPut("mapperURL", mapperThumbnailUrl);
		
		allCoursesLink = LinkFactory.createButton("allcourses", wideVC, this);
		condensedVC.put("allcourses", allCoursesLink);

		mainPanel = putInitialPanel(condensedVC);
	}
	
	@Override
	protected void doDispose() {
		//
	} 

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == allCoursesLink) {
			doSwitch();
		} else if (source instanceof Link) {
			Link link = (Link)source;
			if("select".equals(link.getCommand())) {
				String businessPath = "[RepositoryEntry:" + link.getUserObject() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			}
		}
	}
	
	protected void doSwitch() {
		boolean wide = mainPanel.getContent() == wideVC;
		if(wide) {
			updateModel(currentParams, false);
			mainPanel.setContent(condensedVC);
		} else {
			updateModel(currentParams, true);
			mainPanel.setContent(wideVC);
		}
	}
	
	protected void updateModel(SearchRepositoryEntryParameters params, boolean wide) {
		currentParams = params;
		
		int max = wide ? -1 : 9;
		List<RepositoryEntry> repoEntries = repositoryManager.genericANDQueryWithRolesRestriction(params, 0, max, true);
		//List<OLATResourceAccess> withOffers = acService.filterRepositoryEntriesWithAC(repoEntries);
		//System.out.println("Offers: " + withOffers.size());
		
		List<OLATResource> resources = new ArrayList<OLATResource>(repoEntries.size());
		List<SecurityGroup> authorsSecGroup = new ArrayList<SecurityGroup>(repoEntries.size());
		for(RepositoryEntry entry:repoEntries) {
			resources.add(entry.getOlatResource());
			authorsSecGroup.add(entry.getOwnerGroup());
		}

		List<UserCourseInformations> courseInfos = courseInfoManager.getUserCourseInformations(getIdentity(), resources);
		List<RepositoryEntryMembership> authors = repositoryManager.getOwnersMembership(repoEntries);
		
		List<Long> identityTofinds = new ArrayList<Long>();
		for(RepositoryEntryMembership author:authors) {
			Long authorKey = author.getIdentityKey();
			if(!namesCache.containsKey(authorKey)) {
				identityTofinds.add(authorKey);
			}	
		}
		
		if(!identityTofinds.isEmpty()) {
			List<IdentityShort> identities = securityManager.findShortIdentitiesByKey(identityTofinds);
			for(IdentityShort identity:identities) {
				namesCache.put(identity.getKey(), userManager.getUserDisplayName(identity));
			}
		}

		List<Mark> marks = markManager.getMarks(getIdentity(), Collections.singletonList("RepositoryEntry"));
		Set<Long> repoEntryKeys = new HashSet<Long>(marks.size());
		for(Mark mark:marks) {
			repoEntryKeys.add(mark.getOLATResourceable().getResourceableId());
		}
		
		List<EfficiencyStatementEntry> statements = coachService.getEfficencyStatements(getIdentity(), repoEntries);
		
		VelocityContainer mainVC = wide ? wideVC : condensedVC;

		List<RepositoryEntryDetails> items = new ArrayList<RepositoryEntryDetails>();
		for(RepositoryEntry entry:repoEntries) {
			RepositoryEntryDetails details = new RepositoryEntryDetails();
			details.setKey(entry.getKey());
			details.setDisplayName(entry.getDisplayname());
			
			String cmpId = "sel_" + entry.getKey();
			Link link = LinkFactory.createCustomLink(cmpId, "select", cmpId, Link.LINK + Link.NONTRANSLATED, mainVC, this);
			link.setCustomDisplayText(details.getDisplayName());
			link.setUserObject(entry.getKey());
			details.setSelectLinkName(cmpId);
			
			VFSLeaf image = repositoryManager.getImage(entry);
			if(image != null) {
				details.setThumbnailAvailable(true);
				details.setThumbnailRelPath(image.getName());
			}
			
			StringBuilder sb = new StringBuilder();
			for(RepositoryEntryMembership membership:authors) {
				if(entry.getKey().equals(membership.getOwnerRepoKey())) {
					String authorName = namesCache.get(membership.getIdentityKey());
					if(StringHelper.containsNonWhitespace(authorName)) {
						if(sb.length() > 0) {
							sb.append("; ");
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
					details.setProgress(getProgress(userStatement));
					details.setPassed(userStatement.getPassed());
					details.setScore(AssessmentHelper.getRoundedScore(userStatement.getScore()));
				}
			}
			
			items.add(details);
		}
		
		if(wide) {
			wideVC.contextPut("items", items);
			condensedVC.contextPut("items", Collections.emptyList());
		} else {
			wideVC.contextPut("items", Collections.emptyList());
			condensedVC.contextPut("items", items);
		}
	}
	
	private ProgressValue getProgress(UserEfficiencyStatement s) {
		if(s == null || s.getTotalNodes() == null) {
			ProgressValue val = new ProgressValue();
			val.setTotal(100);
			val.setGreen(0);
			return val;
		}
		
		ProgressValue val = new ProgressValue();
		val.setTotal(s.getTotalNodes().intValue());
		val.setGreen(s.getAttemptedNodes() == null ? 0 : s.getAttemptedNodes().intValue());
		return val;
	}


	
	

}
