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
package org.olat.course.assessment.ui.mode;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LockResourceInfos;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.countdown.CountDownComponent;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.assessment.AssessmentMode.EndStatus;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModeCoordinationService;
import org.olat.course.assessment.AssessmentModeManager;
import org.olat.course.assessment.AssessmentModeNotificationEvent;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.model.TransientAssessmentMode;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 18.12.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentModeGuardController extends BasicController implements GenericEventListener {

	private final Link mainContinueButton;
	private final ExternalLink mainSEBQuitButton;
	private final VelocityContainer mainVC;
	private final CloseableModalController cmc;
	
	private final String address;
	
	private boolean pushUpdate = false;
	private List<TransientAssessmentMode> modes;
	
	private final ResourceGuards guards = new ResourceGuards();
	
	@Autowired
	private AssessmentModule assessmentModule;
	@Autowired
	private AssessmentModeManager assessmentModeMgr;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	@Autowired
	private AssessmentModeCoordinationService assessmentModeCoordinationService;
	
	/**
	 *
	 * @param ureq
	 * @param wControl
	 * @param modes List of assessments
	 * @param forcePush Async popup need forcePush=true
	 */
	public AssessmentModeGuardController(UserRequest ureq, WindowControl wControl, List<TransientAssessmentMode> modes, boolean forcePush) {
		super(ureq, wControl);
		putInitialPanel(new Panel("assessment-mode-chooser"));

		this.modes = modes;
		this.pushUpdate = forcePush;
		address = ureq.getHttpReq().getRemoteAddr();
		
		mainVC = createVelocityContainer("choose_mode");
		mainVC.contextPut("guards", guards);
		mainVC.contextPut("checked", hasSEBHeaders(ureq) ? "checked" : "not-checked");
		
		mainContinueButton = LinkFactory.createCustomLink("continue-main", "continue-main", "current.mode.continue", Link.BUTTON, mainVC, this);
		mainContinueButton.setElementCssClass("o_sel_assessment_continue");
		mainContinueButton.setCustomEnabledLinkCSS("btn btn-primary");
		mainContinueButton.setCustomDisabledLinkCSS("o_disabled btn btn-default");
		mainContinueButton.setVisible(false);
		mainVC.put("continue-main", mainContinueButton);
		
		mainSEBQuitButton = LinkFactory.createExternalLink("quit-seb-main", translate("current.mode.seb.quit"), "");
		mainSEBQuitButton.setElementCssClass("btn btn-default btn-primary o_sel_assessment_quit");
		mainSEBQuitButton.setName(translate("current.mode.seb.quit"));
		mainSEBQuitButton.setVisible(false);
		mainVC.put("quit-main", mainSEBQuitButton);
		
		syncAssessmentModes(ureq);

		cmc = new CloseableModalController(getWindowControl(), translate("close"), mainVC, true, translate("current.mode"), false);	
		cmc.activate();
		listenTo(cmc);
		
		//register for assessment mode
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
	}
	
	public boolean hasSEBHeaders(UserRequest ureq) {
		HttpServletRequest request = ureq.getHttpReq();
		String safeExamHash1 = request.getHeader("x-safeexambrowser-requesthash");
		String safeExamHash2 = request.getHeader("x-safeexambrowser-configkeyhash");
		return StringHelper.containsNonWhitespace(safeExamHash1)
				|| StringHelper.containsNonWhitespace(safeExamHash2);
	}
	
	public void deactivate() {
		try {
			cmc.deactivate();
			removeAsListenerAndDispose(cmc);
		} catch (Exception e) {
			logWarn("", e);
		}
	}
	
	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
        super.doDispose();
	}
	
	public boolean updateAssessmentMode(UserRequest ureq) {
		boolean f;
		if(pushUpdate) {
			syncAssessmentModes(ureq);
			f = true;
			pushUpdate = false;
		} else {
			f = false;
		}
		return f;
	}
	
	private void syncAssessmentModes(UserRequest ureq) {
		List<ResourceGuard> modeWrappers = new ArrayList<>();
		
		String quitUrl = null;
		for(TransientAssessmentMode mode:modes) {
			if(mode != null) {
				ResourceGuard wrapper = syncAssessmentMode(ureq, mode);
				if(wrapper != null) {
					modeWrappers.add(wrapper);
				}
				if(mode.hasLinkToQuitSEB()) {
					quitUrl = mode.getLinkToQuitSEB();
				}
			}
		}
		
		guards.setList(modeWrappers);
		mainContinueButton.setVisible(modeWrappers.isEmpty());
		// See if we can use a quit URL for SEB
		if(modeWrappers.isEmpty()) {
			if(quitUrl == null) {
				quitUrl = getSEBQuitURLFromLastUnlockedResource();
			}
			if(StringHelper.containsNonWhitespace(quitUrl)) {
				mainSEBQuitButton.setUrl(quitUrl);
				mainSEBQuitButton.setVisible(true);
				// prefer the quit URL in SEB
				mainContinueButton.setVisible(false);
			} else {
				mainSEBQuitButton.setVisible(false);
			}
		} else {
			mainSEBQuitButton.setVisible(false);
		}
		mainVC.setDirty(true);
	}
	
	private String getSEBQuitURLFromLastUnlockedResource() {
		LockResourceInfos infos = getWindowControl().getWindowBackOffice().getChiefController().getLastUnlockedResourceInfos();
		if(infos.getLockMode() != null && infos.getLockMode().hasLinkToQuitSEB()) {
			return infos.getLockMode().getLinkToQuitSEB();
		}
		return null;
	}
	
	private ResourceGuard syncAssessmentMode(UserRequest ureq, TransientAssessmentMode mode) {
		Date now = new Date();
		Date beginWithLeadTime = mode.getBeginWithLeadTime();
		Date endWithFollowupTime = mode.getEndWithFollowupTime();
		//check if the mode must not be guarded anymore
		if(mode.isManual() && ((Status.end.equals(mode.getStatus()) && EndStatus.all.equals(mode.getEndStatus())) || Status.none.equals(mode.getStatus()))) {
			return null;
		} else if(!mode.isManual() && (beginWithLeadTime.after(now) || now.after(endWithFollowupTime))) {
			return null;
		} 
		
		ResourceGuard guard = guards.getGuardFor(mode);
		if(guard == null) {
			guard = createGuard(mode);
		}

		StringBuilder sb = new StringBuilder();
		boolean allowed = true;
		if(mode.getIpList() != null) {
			boolean ipInRange = assessmentModeMgr.isIpAllowed(mode.getIpList(), address);
			if(!ipInRange) {
				sb.append("<h4><i class='o_icon o_icon_warn o_icon-fw'>&nbsp;</i>");
				sb.append(translate("error.ip.range"));
				sb.append("</h4>");
				sb.append(translate("error.ip.range.desc", address));
			}
			allowed &= ipInRange;
		}
		if(StringHelper.containsNonWhitespace(mode.getSafeExamBrowserKey())) {
			boolean safeExamCheck = isSafelyAllowed(ureq, mode.getSafeExamBrowserKey(), null);
			if(!safeExamCheck) {
				sb.append("<h4><i class='o_icon o_icon_warn o_icon-fw'>&nbsp;</i>");
				sb.append(translate("error.safe.exam"));
				sb.append("</h4>");
				sb.append(translate("error.safe.exam.desc", assessmentModule.getSafeExamBrowserDownloadUrl()));
			}
			allowed &= safeExamCheck;
		} else if(StringHelper.containsNonWhitespace(mode.getSafeExamBrowserConfigPList())) {
			boolean safeExamCheck = isSafelyAllowed(ureq, null, mode.getSafeExamBrowserConfigPListKey());
			if(!safeExamCheck) {
				sb.append("<h4><i class='o_icon o_icon_warn o_icon-fw'>&nbsp;</i>");
				sb.append(translate("error.safe.exam"));
				sb.append("</h4>");
				sb.append(translate("error.safe.exam.desc", assessmentModule.getSafeExamBrowserDownloadUrl()));
				
				guard.getDownloadSEBButton().setVisible(true);
				guard.getDownloadSEBConfigurationButton().setVisible(mode.isSafeExamBrowserConfigDownload());
			}
			allowed &= safeExamCheck;
		}
		
		guard.getCountDown().setDate(mode.getBegin());

		String state;
		if(allowed) {
			Link go = guard.getGo();
			Link cont = guard.getContinue();
			ExternalLink quit = guard.getQuitSEB();
			state = updateButtons(mode, now, go, cont, quit);
			if(go.isVisible()) {
				assessmentModeCoordinationService.waitFor(getIdentity(), mode);
			}
		} else {
			state = "error";
		}
		
		guard.sync(state, sb.toString(), mode, getLocale());
		return guard;
	}
	
	private boolean isSafelyAllowed(UserRequest ureq, String safeExamBrowserKeys, String configurationKey) {
		String safeExamHash = ureq.getParameter("configKey");
		String url = ureq.getParameter("urlForKeyHash");
		String browserExamKey = ureq.getParameter("browserExamKey");
		getLogger().debug("SEB requests parameters - configkey: {}, url: {}, browser exam key: {}", safeExamHash, url, browserExamKey);
		return assessmentModeMgr.isSafelyAllowed(ureq.getHttpReq(), safeExamBrowserKeys, configurationKey)
				|| assessmentModeMgr.isSafelyAllowedJs(safeExamHash, url, safeExamBrowserKeys, configurationKey);
	}
	
	private String updateButtons(TransientAssessmentMode mode, Date now, Link go, Link cont, ExternalLink quit) {
		String state;
		if(mode.isManual()) {
			state = updateButtonsManual(mode, go, cont, quit);
		} else {
			state = updateButtonsAuto(mode, now, go, cont, quit);
		}
		return state;
	}
	
	private String updateButtonsManual(TransientAssessmentMode mode, Link go, Link cont, ExternalLink quitSEB) {
		String state;
		if(Status.leadtime == mode.getStatus()) {
			state = Status.leadtime.name();
			go.setEnabled(false);
			go.setVisible(true);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(Status.assessment == mode.getStatus() || isDisadvantageCompensationExtension(mode)) {
			state = Status.assessment.name();
			go.setEnabled(true);
			go.setVisible(true);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(Status.followup == mode.getStatus()) {
			state = Status.followup.name();
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(Status.end == mode.getStatus()) {
			state = Status.end.name();
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(true);
			cont.setVisible(true);
			quitSEB.setEnabled(mode.hasLinkToQuitSEB());
			quitSEB.setVisible(mode.hasLinkToQuitSEB());
		} else {
			state = "error";
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		}
		return state;
	}
	
	private boolean isDisadvantageCompensationExtension(TransientAssessmentMode mode) {
		if(mode.getEndStatus() == EndStatus.withoutDisadvantage
				&& (mode.getStatus() == Status.followup || mode.getStatus() == Status.end)) {
			return disadvantageCompensationService.isActiveDisadvantageCompensation(getIdentity(),
					new RepositoryEntryRefImpl(mode.getRepositoryEntryKey()), mode.getElementList());
		}
		return false;
	}
	
	private String updateButtonsAuto(TransientAssessmentMode mode, Date now, Link go, Link cont, ExternalLink quitSEB) {
		Date begin = mode.getBegin();
		Date beginWithLeadTime = mode.getBeginWithLeadTime();
		Date end = mode.getEnd();
		Date endWithLeadTime = mode.getEndWithFollowupTime();
		
		String state;
		if(beginWithLeadTime.compareTo(now) <= 0 && begin.compareTo(now) > 0) {
			state = Status.leadtime.name();
			go.setEnabled(false);
			go.setVisible(true);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(begin.compareTo(now) <= 0 && end.compareTo(now) > 0) {
			state = Status.assessment.name();
			go.setEnabled(true);
			go.setVisible(true);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(end.compareTo(now) <= 0 && endWithLeadTime.compareTo(now) > 0) {
			state = Status.followup.name();
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		} else if(endWithLeadTime.compareTo(now) <= 0 || Status.end == mode.getStatus()) {
			state = Status.end.name();
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(true);
			cont.setVisible(true);
			quitSEB.setEnabled(mode.hasLinkToQuitSEB());
			quitSEB.setVisible(mode.hasLinkToQuitSEB());
		} else {
			state = "error";
			go.setEnabled(false);
			go.setVisible(false);
			cont.setEnabled(false);
			cont.setVisible(false);
			quitSEB.setEnabled(false);
			quitSEB.setVisible(false);
		}
		return state;
	}
	
	private ResourceGuard createGuard(TransientAssessmentMode mode) {
		String id = Long.toString(CodeHelper.getRAMUniqueID());

		Link goButton = LinkFactory.createCustomLink("go-" + id, "go", "current.mode.start", Link.BUTTON, mainVC, this);
		goButton.setElementCssClass("o_sel_assessment_start");
		goButton.setCustomEnabledLinkCSS("btn btn-primary");
		goButton.setCustomDisabledLinkCSS("o_disabled btn btn-default");
		
		Link continueButton = LinkFactory.createCustomLink("continue-" + id, "continue", "current.mode.continue", Link.BUTTON, mainVC, this);
		continueButton.setCustomEnabledLinkCSS("btn btn-primary");
		continueButton.setCustomDisabledLinkCSS("o_disabled btn btn-default");
		
		String quitUrl = mode.getLinkToQuitSEB();
		ExternalLink quitSEBLink = LinkFactory.createExternalLink("download.seb-" + id, translate("current.mode.seb.quit"), quitUrl);
		quitSEBLink.setName(translate("current.mode.seb.quit"));
		quitSEBLink.setTooltip(translate("current.mode.seb.quit"));
		quitSEBLink.setElementCssClass("btn btn-default");
		
		Link downloadSEBConfigurationButton = LinkFactory.createCustomLink("download-seb-config-" + id, "download.seb.config", "download.seb.config", Link.BUTTON, mainVC, this);
		downloadSEBConfigurationButton.setVisible(false);
		
		String sebUrl = assessmentModule.getSafeExamBrowserDownloadUrl();
		ExternalLink downloadSEBLink = LinkFactory.createExternalLink("download.seb-" + id, translate("download.seb"), sebUrl);
		downloadSEBLink.setName(translate("download.seb"));
		downloadSEBLink.setTooltip(translate("download.seb"));
		downloadSEBLink.setElementCssClass("btn btn-default");
		
		CountDownComponent countDown = new CountDownComponent("count-" + id, mode.getBegin(), getTranslator());
		countDown.setI18nKey("current.mode.in");
		
		ResourceGuard guard = new ResourceGuard(mode.getModeKey(), goButton, continueButton, quitSEBLink, downloadSEBLink, downloadSEBConfigurationButton, countDown);
		mainVC.put(goButton.getComponentName(), goButton);
		mainVC.put(continueButton.getComponentName(), continueButton);
		mainVC.put(countDown.getComponentName(), countDown);
		mainVC.put(downloadSEBConfigurationButton.getComponentName(), downloadSEBConfigurationButton);
		mainVC.put(downloadSEBLink.getComponentName(), downloadSEBLink);
		
		goButton.setUserObject(guard);
		continueButton.setUserObject(guard);
		downloadSEBConfigurationButton.setUserObject(guard);
		return guard;
	}

	@Override
	public void event(Event event) {
		 if (event instanceof AssessmentModeNotificationEvent) {
			try {
				processAssessmentModeNotificationEvent((AssessmentModeNotificationEvent)event);
			} catch (Exception e) {
				logError("", e);
			}
		}
	}
	
	private void processAssessmentModeNotificationEvent(AssessmentModeNotificationEvent event) {
		if(getIdentity() != null && event.getAssessedIdentityKeys() != null
				&& event.getAssessedIdentityKeys().contains(getIdentity().getKey())) {

			boolean update = false;
			TransientAssessmentMode mode = event.getAssessementMode();
			List<TransientAssessmentMode> updatedModes = new ArrayList<>();
			for(TransientAssessmentMode currentMode:modes) {
				if(currentMode.getModeKey().equals(mode.getModeKey())) {
					updatedModes.add(mode);
					update |= (currentMode.getStatus() != mode.getStatus());
					
				} else {
					updatedModes.add(currentMode);
					update |= true;
				}
			}
			modes = updatedModes;
			pushUpdate |= update;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link) {
			Link link = (Link)source;
			String cmd = link.getCommand();
			if("go".equals(cmd)) {
				ResourceGuard guard = (ResourceGuard)link.getUserObject();
				launchAssessmentMode(ureq, guard.getReference());
			} else if("continue".equals(cmd) || "continue-main".equals(cmd)) {
				ResourceGuard guard = (ResourceGuard)link.getUserObject();
				continueAfterAssessmentMode(ureq, guard);
			} else if("download.seb.config".equals(cmd)) {
				ResourceGuard guard = (ResourceGuard)link.getUserObject();
				downloadSebConfiguration(ureq, guard);
			}
		} else if(source == mainVC) {
			if("checkSEBKeys".equals(event.getCommand())) {
				syncAssessmentModes(ureq);
				mainVC.contextPut("checked", "checked");
			}
		}
	}
	
	private void continueAfterAssessmentMode(UserRequest ureq, ResourceGuard selectedGuard) {
		List<ResourceGuard> lastGuards = new ArrayList<>();
		for(ResourceGuard currentGuard:guards.getList()) {
			if(currentGuard != selectedGuard) {
				lastGuards.add(currentGuard);
			}
		}
		guards.setList(lastGuards);
		
		boolean canContinue = guards.getSize() == 0;
		if(canContinue) {
			cmc.deactivate();
			
			//make sure to see the navigation bar
			ChiefController cc = Windows.getWindows(ureq).getChiefController(ureq);
			cc.getScreenMode().setMode(Mode.standard, null);
			
			fireEvent(ureq, new Event("continue"));
			String businessPath = "[MyCoursesSite:0]";
			NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
		} else {
			mainVC.setDirty(true);
		}
	}
	
	/**
	 * Remove the list of assessment modes and lock the chief controller.
	 * 
	 * 
	 * @param ureq
	 * @param mode
	 */
	private void launchAssessmentMode(UserRequest ureq, TransientAssessmentMode mode) {
		cmc.deactivate();
	
		ureq.getUserSession().setAssessmentModes(null);
		OLATResourceable resource = mode.getResource();
		ureq.getUserSession().setLockResource(resource, mode);
		getWindowControl().getWindowBackOffice().getChiefController().lockResource(resource);
		fireEvent(ureq, new ChooseAssessmentModeEvent(mode));
		
		String businessPath = "[RepositoryEntry:" + mode.getRepositoryEntryKey() + "]";
		if(StringHelper.containsNonWhitespace(mode.getStartElementKey())) {
			businessPath += "[CourseNode:" + mode.getStartElementKey() + "]";
		}
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());

		assessmentModeCoordinationService.start(getIdentity(), mode);
	}
	
	private void downloadSebConfiguration(UserRequest ureq, ResourceGuard guard) {
		MediaResource resource;
		if(guard != null && StringHelper.containsNonWhitespace(guard.getSafeExamBrowserConfigPList())) {
			resource = new SafeExamBrowserConfigurationMediaResource(guard.getSafeExamBrowserConfigPList());
		} else {
			resource = new NotFoundMediaResource();
		}
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}
	
	public static final class ResourceGuard {

		private String status;
		private String errors;
		private final Link goButton;
		private final Link continueButton;
		private final ExternalLink quitSEBButton;
		private final ExternalLink downloadSEBButton;
		private final Link downloadSEBConfigurationButton;
		
		private final Long modeKey;
		private String name;
		private String displayName;
		private String description;
		private String safeExamBrowserHint;
		private String safeExamBrowserConfigPList;
		
		private String begin;
		private String end;
		private String leadTime;
		private String followupTime;
		
		private TransientAssessmentMode reference;
		
		private CountDownComponent countDown;
		
		public ResourceGuard(Long modeKey, Link goButton, Link continueButton, ExternalLink quitSebButton,
				ExternalLink downloadSEBButton, Link downloadSEBConfigurationButton, CountDownComponent countDown) {
			this.modeKey = modeKey;
			this.goButton = goButton;
			this.countDown = countDown;
			this.quitSEBButton = quitSebButton;
			this.continueButton = continueButton;
			this.downloadSEBButton = downloadSEBButton;
			this.downloadSEBConfigurationButton = downloadSEBConfigurationButton;
		}
		
		public void sync(String newStatus, String newErrors, TransientAssessmentMode mode, Locale locale) {
			errors = newErrors;
			status = newStatus;
			
			reference = mode;
			name = mode.getName();
			displayName = mode.getDisplayName();
			description = mode.getDescription();
			safeExamBrowserHint = mode.getSafeExamBrowserHint();
			safeExamBrowserConfigPList = mode.getSafeExamBrowserConfigPList();
			
			Formatter f = Formatter.getInstance(locale);
			begin = f.formatDateAndTime(mode.getBegin());
			end = f.formatDateAndTime(mode.getEnd());
			
			if(mode.getFollowupTime() > 0) {
				followupTime = Integer.toString(mode.getFollowupTime());
			} else {
				followupTime = null;
			}
			
			if(mode.getLeadTime() > 0) {
				leadTime = Integer.toString(mode.getLeadTime());
			} else {
				leadTime = null;
			}
		}
		
		public Long getModeKey() {
			return modeKey;
		}
		
		public TransientAssessmentMode getReference() {
			return reference;
		}

		public String getName() {
			return name;
		}
		
		public String getDescription() {
			return description;
		}
		
		public String getSafeExamBrowserHint() {
			return safeExamBrowserHint;
		}
		
		public String getSafeExamBrowserConfigPList() {
			return safeExamBrowserConfigPList;
		}

		public String getDisplayName() {
			return displayName;
		}
		
		public String getBegin() {
			return begin;
		}
		
		public String getEnd() {
			return end;
		}
		
		public String getLeadTime() {
			return leadTime;
		}
		
		public String getFollowupTime() {
			return followupTime;
		}
		
		public String getStatus() {
			return status;
		}

		public void setStatus(String status) {
			this.status = status;
		}

		public String getErrors() {
			return errors;
		}

		public void setErrors(String errors) {
			this.errors = errors;
		}

		public Link getGo() {
			return goButton;
		}
		
		public Link getContinue() {
			return continueButton;
		}
		
		public ExternalLink getQuitSEB() {
			return quitSEBButton;
		}
		
		public ExternalLink getDownloadSEBButton() {
			return downloadSEBButton;
		}

		public Link getDownloadSEBConfigurationButton() {
			return downloadSEBConfigurationButton;
		}

		public CountDownComponent getCountDown() {
			return countDown;
		}
	}
	
	public static class ResourceGuards {
		
		private List<ResourceGuard> guards = new ArrayList<>();
		
		public int getSize() {
			return guards.size();
		}
		
		public ResourceGuard getGuardFor(TransientAssessmentMode mode) {
			ResourceGuard guard = null;
			for(ResourceGuard g:getList()) {
				if(g.getModeKey().equals(mode.getModeKey())) {
					guard = g;
				}
			}
			return guard;
		}

		public List<ResourceGuard> getList() {
			return guards;
		}

		public void setList(List<ResourceGuard> guards) {
			this.guards = guards;
		}
	}
}