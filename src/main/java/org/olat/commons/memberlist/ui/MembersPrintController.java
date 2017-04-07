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

import java.util.List;

import javax.servlet.http.HttpServletRequest;

import org.olat.core.commons.modules.bc.meta.MetaInfo;
import org.olat.core.commons.modules.bc.meta.tagged.MetaTagged;
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
import org.olat.core.util.Util;
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
	
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private DisplayPortraitManager portraitManager;
	
	public MembersPrintController(UserRequest ureq, WindowControl wControl, List<UserPropertyHandler> userPropertyHandlers, 
			Translator translator, List<Member> owners, List<Member> coaches, List<Member> participants, List<Member> waiting, 
			boolean showOwners, boolean showCoaches, boolean showParticipants, boolean showWaiting, String title) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(translator, getTranslator(), getLocale()));

		avatarBaseURL = registerCacheableMapper(ureq, "avatars-members-high-quality", new UserAvatarHQMapper());
		this.userPropertyHandlers = userPropertyHandlers;
		
		mainVC = createVelocityContainer("print");
		mainVC.contextPut("courseTitle", title);
		mainVC.contextPut("avatarBaseURL", avatarBaseURL);
		mainVC.contextPut("userPropertyHandlers", userPropertyHandlers);
		
		if(showOwners && owners != null && owners.size() > 0) {
			initFormMemberList("owners", translate("members.owners"), owners);
		}
		if(showCoaches && coaches != null && coaches.size() > 0) {
			initFormMemberList("coaches", translate("members.coaches"), coaches);
		}
		if(showParticipants && participants != null && participants.size() > 0) {
			initFormMemberList("participants", translate("members.participants"), participants);
		}
		if(showWaiting && waiting != null && waiting.size() > 0) {
			initFormMemberList("waiting", translate("members.waiting"), waiting);
		}

		MainPanel mainPanel = new MainPanel("membersPrintPanel");
		mainPanel.setContent(mainVC);
		putInitialPanel(mainPanel);
	}
	
	private void initFormMemberList(String name, String label, List<Member> members) {
		VelocityContainer listVC = createVelocityContainer("printList");
		listVC.contextPut("label", label);
		listVC.contextPut("avatarBaseURL", avatarBaseURL);
		listVC.contextPut("members", members);
		listVC.contextPut("userPropertyHandlers", userPropertyHandlers);
		listVC.contextPut("typecss", "o_" + name);
		mainVC.put(name, listVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private class UserAvatarHQMapper implements Mapper {
		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			MediaResource rsrc = null;
			if(relPath != null) {
				if(relPath.startsWith("/")) {
					relPath = relPath.substring(1, relPath.length());
				}
				
				int endKeyIndex = relPath.indexOf('/');
				if(endKeyIndex > 0) {
					String idKey = relPath.substring(0, endKeyIndex);
					Long key = Long.parseLong(idKey);
					String username = userManager.getUsername(key);
					VFSLeaf portrait = portraitManager.getLargestVFSPortrait(username);
					if(portrait instanceof MetaTagged) {
						MetaInfo meta = ((MetaTagged)portrait).getMetaInfo();
						portrait = meta.getThumbnail(300, 300, false);
					}
					
					if(portrait == null) {
						return new NotFoundMediaResource(relPath);
					}
					return new VFSMediaResource(portrait);
				}
			}
			return rsrc;
		}
	}
}
