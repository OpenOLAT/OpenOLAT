/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.curriculum.ui;

import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.UserConstants;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.user.AboutMeController;
import org.olat.user.HomePageConfig;
import org.olat.user.HomePageConfigManager;
import org.olat.user.HomePageDisplayController;
import org.olat.user.PortraitUser;
import org.olat.user.UserAvatarMapper;
import org.olat.user.UserManager;
import org.olat.user.UserModule;
import org.olat.user.UserPortraitComponent;
import org.olat.user.UserPortraitFactory;
import org.olat.user.UserPortraitService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: Feb 4, 2025<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class CurriculumElementInfoCoachesController extends BasicController {
	
	private final static String CMD_VISITING_CARD = "visiting.card";
	private final static String CMD_ABOUT_ME = "about.me";

	private final VelocityContainer mainVC;
	
	private CloseableModalController cmc;
	private HomePageDisplayController infoCtrl;
	private LightboxController lightboxCtrl;
	private AboutMeController aboutMeCtrl;
	
	private final String avatarMaperBaseUrl;
	private final List<CoachRow> coachRows;

	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private UserModule userModule;
	@Autowired
	private UserManager userManager;
	@Autowired
	private UserPortraitService userPortraitService;
	@Autowired
	private HomePageConfigManager homePageConfigManager;

	public CurriculumElementInfoCoachesController(UserRequest ureq, WindowControl wControl, CurriculumElement curriculumElement) {
		super(ureq, wControl);
		avatarMaperBaseUrl = registerCacheableMapper(ureq, "users-avatars", new UserAvatarMapper(true));
		
		mainVC = createVelocityContainer("curriculum_element_coaches");
		putInitialPanel(mainVC);
		
		List<CurriculumElement> curriculumElements = curriculumService.getCurriculumElementsDescendants(curriculumElement);
		curriculumElements.add(curriculumElement);
		SearchMemberParameters searchParams = new SearchMemberParameters(curriculumElements);
		searchParams.setRoles(List.of(CurriculumRoles.coach));
		Set<Identity> coaches = curriculumService.getCurriculumElementsMembers(searchParams)
				.stream()
				.map(CurriculumMember::getIdentity)
				.collect(Collectors.toSet());
		
		Map<Long, PortraitUser> identityKeyToPortraitUser = userPortraitService.createPortraitUsers(coaches)
				.stream()
				.collect(Collectors.toMap(PortraitUser::getIdentityKey, Function.identity()));
		
		coachRows = new ArrayList<>(coaches.size());
		for (Identity coach : coaches) {
			CoachRow coachRow = new CoachRow();
			coachRow.setDisplayName(userManager.getUserDisplayName(coach));
			
			forgePortrait(coachRow, identityKeyToPortraitUser.get(coach.getKey()));
			forgeVisitingCardLink(coachRow, coach);
			forgeLinkedInLink(coachRow, coach);
			forgeAboutMeLink(coachRow, coach);
			
			coachRows.add(coachRow);
		}
		Collator collator = Collator.getInstance(getLocale());
		Collections.sort(coachRows, (r1, r2) -> collator.compare(r1.getDisplayName(), r2.getDisplayName()));
		
		mainVC.contextPut("rows", coachRows);
	}
	
	private void forgePortrait(CoachRow coachRow, PortraitUser portraitUser) {
		UserPortraitComponent portraitComp = UserPortraitFactory
				.createUserPortrait("up_" + portraitUser.getIdentityKey(), mainVC, getLocale(), avatarMaperBaseUrl);
		portraitComp.setPortraitUser(portraitUser);
		portraitComp.setDisplayPresence(false);
		coachRow.setPortraitComp(portraitComp);
	}

	private void forgeVisitingCardLink(CoachRow coachRow, Identity coach) {
		Link link = LinkFactory.createCustomLink("vc_" + coach.getKey(), CMD_VISITING_CARD, "infos.coaches.visiting.card", Link.LINK, mainVC, this);
		link.setElementCssClass("o_nowrap");
		link.setAriaRole("button");
		link.setUserObject(coach);
		link.setUrl(Settings.getServerContextPathURI() + "/url/HomeSite/" + coach.getKey());
		coachRow.setVisitingCardLink(link);
	}
	
	private void forgeLinkedInLink(CoachRow coachRow, Identity coach) {
		String linkedInUrl = coach.getUser().getProperty(UserConstants.LINKED_IN);
		if (!StringHelper.containsNonWhitespace(linkedInUrl)) {
			return;
		}
		
		ExternalLink link = new ExternalLink("li_" + coach.getKey(), linkedInUrl);
		link.setName(translate("infos.coaches.linkedin"));
		link.setUrl(linkedInUrl);
		link.setTarget("_blank");
		link.setElementCssClass("o_nowrap");
		mainVC.put(link.getComponentName(), link);
		coachRow.setLinkedInLink(link);
	}
	
	private void forgeAboutMeLink(CoachRow coachRow, Identity coach) {
		if (coachRow.getLinkedInLink() != null || !userModule.isUserAboutMeEnabled()) {
			return;
		}
		
		HomePageConfig homePageConfig = homePageConfigManager.loadConfigFor(coach);
		if (homePageConfig == null || !StringHelper.containsNonWhitespace(homePageConfig.getTextAboutMe())) {
			return;
		}
		
		Link link = LinkFactory.createCustomLink("am_" + coach.getKey(), CMD_ABOUT_ME, "infos.coaches.about.me", Link.LINK, mainVC, this);
		link.setElementCssClass("o_nowrap");
		link.setAriaRole("button");
		link.setUserObject(coach);
		coachRow.setAboutMeLink(link);
	}

	public boolean isEmpty() {
		return coachRows.isEmpty();
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == cmc) {
			cleanUp();
		} else if (source == lightboxCtrl) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(aboutMeCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		removeAsListenerAndDispose(infoCtrl);
		removeAsListenerAndDispose(cmc);
		aboutMeCtrl = null;
		lightboxCtrl = null;
		infoCtrl = null;
		cmc = null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source instanceof Link link) {
			if (CMD_VISITING_CARD.equals(link.getCommand()) && link.getUserObject() instanceof Identity coach) {
				doOpenVisitingCard(ureq, coach);
			} else if (CMD_ABOUT_ME.equals(link.getCommand()) && link.getUserObject() instanceof Identity coach) {
				doOpenAboutMe(ureq, coach);
			}
		}
	}
	
	private void doOpenVisitingCard(UserRequest ureq, Identity coach) {
		if (guardModalController(infoCtrl)) return;
		
		HomePageConfig homePageConfig = homePageConfigManager.loadConfigFor(coach);
		infoCtrl = new HomePageDisplayController(ureq, getWindowControl(), coach, homePageConfig);
		infoCtrl.setOnlineStatusVisible(getIdentity() != null); // Hide in web catalog
		listenTo(infoCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), infoCtrl.getInitialComponent(),
				true, translate("infos.coaches.visiting.card.title"));
		listenTo(cmc);
		cmc.activate();
	}

	private void doOpenAboutMe(UserRequest ureq, Identity coach) {
		if (guardModalController(aboutMeCtrl)) return;
		
		removeAsListenerAndDispose(aboutMeCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		
		HomePageConfig homePageConfig = homePageConfigManager.loadConfigFor(coach);
		aboutMeCtrl = new AboutMeController(ureq, getWindowControl(), homePageConfig);
		listenTo(aboutMeCtrl);
		
		lightboxCtrl = new LightboxController(ureq, getWindowControl(), aboutMeCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
	}

	public static final class CoachRow {
		
		private String displayName;
		private UserPortraitComponent portraitComp;
		private Link visitingCardLink;
		private ExternalLink linkedInLink;
		private Link aboutMeLink;
		
		public String getDisplayName() {
			return displayName;
		}
		
		public void setDisplayName(String displayName) {
			this.displayName = displayName;
		}
		
		public String getPortraitCompName() {
			return portraitComp.getComponentName();
		}
		
		public UserPortraitComponent getPortraitComp() {
			return portraitComp;
		}
		
		public void setPortraitComp(UserPortraitComponent portraitComp) {
			this.portraitComp = portraitComp;
		}
		
		public String getVisitingCardLinkName() {
			return visitingCardLink != null ? visitingCardLink.getComponentName(): null;
		}
		
		public Link getVisitingCardLink() {
			return visitingCardLink;
		}
		
		public void setVisitingCardLink(Link visitingCardLink) {
			this.visitingCardLink = visitingCardLink;
		}
		
		public String getLinkedInLinkName() {
			return linkedInLink != null? linkedInLink.getComponentName(): null;
		}
		
		public ExternalLink getLinkedInLink() {
			return linkedInLink;
		}
		
		public void setLinkedInLink(ExternalLink linkedInLink) {
			this.linkedInLink = linkedInLink;
		}
		
		public String getAboutMeLinkName() {
			return aboutMeLink != null? aboutMeLink.getComponentName(): null;
		}
		
		public Link getAboutMeLink() {
			return aboutMeLink;
		}
		
		public void setAboutMeLink(Link aboutMeLink) {
			this.aboutMeLink = aboutMeLink;
		}
		
	}
	
}
