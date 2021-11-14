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
package org.olat.commons.memberlist.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.commons.memberlist.model.CurriculumElementInfos;
import org.olat.commons.memberlist.model.CurriculumMemberInfos;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.MainPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSConstants;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.nodes.members.Member;
import org.olat.user.DisplayPortraitManager;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 30.05.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MembersPrintController extends BasicController {
	
	private final String avatarBaseURL;
	private final VelocityContainer mainVC;
	
	private final List<UserPropertyHandler> userPropertyPrintHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private DisplayPortraitManager portraitManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	
	public MembersPrintController(UserRequest ureq, WindowControl wControl, Translator translator, List<Identity> owners,
			List<Identity> coaches, List<Identity> participants, List<Identity> waiting, Map<Long,CurriculumMemberInfos> curriculumInfos,
			boolean showOwners, boolean showCoaches, boolean showParticipants, boolean showWaiting, boolean deduplicateList, String title) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(translator, getTranslator(), getLocale()));

		mainVC = createVelocityContainer("print");
		mainVC.contextPut("courseTitle", title);

		avatarBaseURL = registerCacheableMapper(ureq, "avatars-members-high-quality", new UserAvatarHQMapper());
		mainVC.contextPut("avatarBaseURL", avatarBaseURL);
		
		Roles roles = ureq.getUserSession().getRoles();
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		userPropertyPrintHandlers = userManager.getUserPropertyHandlersFor(MembersDisplayRunController.USER_PROPS_PRINT_ID, isAdministrativeUser);
		
		Set<Identity> duplicateCatcher = deduplicateList ? new HashSet<>() : null;
		if(showOwners && owners != null && !owners.isEmpty()) {
			initFormMemberList("owners", translate("members.owners"), owners, duplicateCatcher, curriculumInfos);
		}
		if(showCoaches && coaches != null && !coaches.isEmpty()) {
			initFormMemberList("coaches", translate("members.coaches"), coaches, duplicateCatcher, curriculumInfos);
		}
		if(showParticipants && participants != null && !participants.isEmpty()) {
			initFormMemberList("participants", translate("members.participants"), participants, duplicateCatcher, curriculumInfos);
		}
		if(showWaiting && waiting != null && !waiting.isEmpty()) {
			initFormMemberList("waiting", translate("members.waiting"), waiting, duplicateCatcher, curriculumInfos);
		}

		MainPanel mainPanel = new MainPanel("membersPrintPanel");
		mainPanel.setContent(mainVC);
		putInitialPanel(mainPanel);
	}
	
	private void initFormMemberList(String name, String label, List<Identity> members, Set<Identity> duplicateCatcher, Map<Long,CurriculumMemberInfos> curriculumInfos) {
		if(duplicateCatcher == null) {
			duplicateCatcher = new HashSet<>();
		}

		List<Member> memberWrappers = new ArrayList<>(members.size());
		for(Identity identity:members) {
			if(duplicateCatcher.contains(identity)) {
				continue;
			}
			duplicateCatcher.add(identity);
			memberWrappers.add(createMember(identity, curriculumInfos));
		}

		VelocityContainer listVC = createVelocityContainer("printList");
		listVC.contextPut("label", label);
		listVC.contextPut("avatarBaseURL", avatarBaseURL);
		listVC.contextPut("members", memberWrappers);
		listVC.contextPut("typecss", "o_" + name);

		listVC.contextPut("userPropertyPrintHandlers", userPropertyPrintHandlers);
		// add lookup table so the avatar properties can be read out from the member object that contains the full list of attributes
		Map<String, Integer> handlerLookupMap = new HashMap<>();
		for(int i=userPropertyPrintHandlers.size(); i-->0; ) {
			UserPropertyHandler handler = userPropertyPrintHandlers.get(i);
			handlerLookupMap.put(handler.getName(), i);
		}
		listVC.contextPut("handlerLookupMap", handlerLookupMap);
		
		mainVC.put(name, listVC);
	}
	
	private Member createMember(Identity identity, Map<Long,CurriculumMemberInfos> curriculumInfos) {
		boolean hasPortrait = portraitManager.hasPortrait(identity);

		String portraitCssClass;
		String gender = identity.getUser().getProperty(UserConstants.GENDER, Locale.ENGLISH);
		if ("male".equalsIgnoreCase(gender)) {
			portraitCssClass = DisplayPortraitManager.DUMMY_MALE_BIG_CSS_CLASS;
		} else if ("female".equalsIgnoreCase(gender)) {
			portraitCssClass = DisplayPortraitManager.DUMMY_FEMALE_BIG_CSS_CLASS;
		} else {
			portraitCssClass = DisplayPortraitManager.DUMMY_BIG_CSS_CLASS;
		}
		String fullname = userManager.getUserDisplayName(identity);
		
		CurriculumElementInfos curriculumElementInfos = null;
		if(curriculumInfos != null) {
			CurriculumMemberInfos infos = curriculumInfos.get(identity.getKey());
			if(infos != null && !infos.getCurriculumInfos().isEmpty()) {
				curriculumElementInfos = infos.getCurriculumInfos().get(0);
			}
		}
		return new Member(identity, fullname, curriculumElementInfos, userPropertyPrintHandlers, getLocale(), hasPortrait, portraitCssClass);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private class UserAvatarHQMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(relPath != null) {
				if(relPath.startsWith("/")) {
					relPath = relPath.substring(1, relPath.length());
				}
				
				int endKeyIndex = relPath.indexOf('/');
				if(endKeyIndex > 0) {
					String idKey = relPath.substring(0, endKeyIndex);
					Long key = Long.parseLong(idKey);
					Identity identity = securityManager.loadIdentityByKey(key);
					VFSLeaf portrait = portraitManager.getLargestVFSPortrait(identity);
					if(portrait.canMeta() == VFSConstants.YES) {
						portrait = vfsRepositoryService.getThumbnail(portrait, 300, 300, false);
					}
					
					if(portrait != null) {
						return new VFSMediaResource(portrait);
					}
				}
			}
			return new NotFoundMediaResource();
		}
	}
}
