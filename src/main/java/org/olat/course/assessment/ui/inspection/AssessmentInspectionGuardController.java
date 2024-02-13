/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.course.assessment.ui.inspection;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.NewControllerFactory;
import org.olat.core.commons.fullWebApp.LockGuardController;
import org.olat.core.commons.fullWebApp.LockRequest;
import org.olat.core.commons.fullWebApp.LockRequestEvent;
import org.olat.core.commons.fullWebApp.LockResourceInfos;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.ExternalLinkItem;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.helpers.Settings;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.UserSession;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.assessment.AssessmentInspectionLog.Action;
import org.olat.course.assessment.AssessmentInspectionService;
import org.olat.course.assessment.AssessmentMode.Status;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.manager.IpListValidator;
import org.olat.course.assessment.manager.SafeExamBrowserValidator;
import org.olat.course.assessment.model.TransientAssessmentInspection;
import org.olat.course.assessment.ui.mode.SafeExamBrowserConfigurationMediaResource;
import org.olat.course.nodes.CourseNodeFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentInspectionGuardController extends FormBasicController implements LockGuardController, GenericEventListener {
	
	private FormLink mainContinueButton;
	private ExternalLinkItem mainSEBQuitButton;

	private final String address;
	private final String mapperUri;
	private List<TransientAssessmentInspection> inspections;
	private final ResourceGuards guards = new ResourceGuards();

	@Autowired
	private AssessmentModule assessmentModule;
	@Autowired
	private AssessmentInspectionService inspectionService;
	
	public AssessmentInspectionGuardController(UserRequest ureq, WindowControl wControl,
			List<TransientAssessmentInspection> inspections, String address) {
		super(ureq, wControl, "choose_inspection");
		this.address = address;
		this.inspections = inspections;
		mapperUri = registerCacheableMapper(ureq, "seb-settings", new SettingsMapper(guards));
		
		initForm(ureq);
		syncInspections(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			layoutCont.contextPut("inspections", guards);
			layoutCont.contextPut("checked", SafeExamBrowserValidator.hasSEBHeaders(ureq) ? "checked" : "not-checked");
		}
		
		mainContinueButton = uifactory.addFormLink("continue-main", "current.inspection.continue", null, formLayout, Link.BUTTON);
		mainContinueButton.setElementCssClass("o_sel_assessment_continue");
		mainContinueButton.setCustomEnabledLinkCSS("btn btn-primary");
		mainContinueButton.setCustomDisabledLinkCSS("o_disabled btn btn-default");
		mainContinueButton.setVisible(false);
		
		mainSEBQuitButton = uifactory.addExternalLink("quit-seb-main", "current.mode.seb.quit", null, null, formLayout);
		mainSEBQuitButton.setElementCssClass("btn btn-default btn-primary o_sel_assessment_quit");
		mainSEBQuitButton.setName(translate("current.mode.seb.quit"));
		mainSEBQuitButton.setTooltip(translate("current.mode.seb.quit"));
		mainSEBQuitButton.setVisible(false);
		formLayout.add("quit-seb-main", mainSEBQuitButton);
	}

	private void syncInspections(UserRequest ureq) {
		List<ResourceGuard> guardsList = new ArrayList<>();
		
		String quitUrl = null;
		for(TransientAssessmentInspection inspection:inspections) {
			if(inspection != null) {
				ResourceGuard guard = syncAssessmentInspection(ureq, inspection);
				if(guard != null) {
					guardsList.add(guard);
				}
				if(inspection.hasLinkToQuitSEB()) {
					quitUrl = inspection.getLinkToQuitSEB();
				}
			
			}
		}
		guards.setList(guardsList);

		mainContinueButton.setVisible(guardsList.isEmpty());
		
		// See if we can use a quit URL for SEB
		if(guards.isEmpty()) {
			String unlockMessage = getUnlockMessage();
			if(StringHelper.containsNonWhitespace(unlockMessage)) {
				flc.contextPut("unlockMessage", unlockMessage);
			} else {
				flc.contextPut("unlockMessage", translate("close.inspection.infos"));
			}
			
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
		flc.setDirty(true);
	}
	
	private String getUnlockMessage() {
		TransientAssessmentInspection lastInspection = getLastUnlockedResource();
		if(lastInspection != null && lastInspection.hasLinkToQuitSEB()) {
			return lastInspection.getUnlockInfos(getLocale());
		}
		return null;
	}
	
	private String getSEBQuitURLFromLastUnlockedResource() {
		TransientAssessmentInspection lastInspection = getLastUnlockedResource();
		if(lastInspection != null && lastInspection.hasLinkToQuitSEB()) {
			return lastInspection.getLinkToQuitSEB();
		}
		return null;
	}
	
	private TransientAssessmentInspection getLastUnlockedResource() {
		LockResourceInfos infos = getWindowControl().getWindowBackOffice().getChiefController().getLastUnlockedResourceInfos();
		if(infos != null && infos.getLockMode() instanceof TransientAssessmentInspection inspection) {
			return inspection;
		}
		return null;
	}
	
	private ResourceGuard syncAssessmentInspection(UserRequest ureq, TransientAssessmentInspection inspection) {
		Date now = new Date();
		Date from = inspection.getFromDate();
		Date to = inspection.getToDate();
		//check if the inspection must not be guarded anymore
		if(Status.end.equals(inspection.getStatus()) || Status.followup.equals(inspection.getStatus())
				|| from.after(now) || now.after(to)) {
			return null;
		}

		ResourceGuard guard = createGuard(inspection);

		StringBuilder sb = new StringBuilder();
		boolean allowed = true;
		if(inspection.getIpList() != null) {
			boolean ipInRange = IpListValidator.isIpAllowed(inspection.getIpList(), address);
			if(!ipInRange) {
				sb.append("<h4><i class='o_icon o_icon_warn o_icon-fw'> </i> ")
				  .append(translate("error.ip.range")).append("</h4>")
				  .append(translate("error.ip.range.desc", address));
				inspectionService.log(Action.wrongIpRange, null, address, inspection, getIdentity());
			}
			allowed &= ipInRange;
		}
		if(StringHelper.containsNonWhitespace(inspection.getSafeExamBrowserKey())) {
			boolean safeExamCheck = isSafelyAllowed(ureq, inspection.getSafeExamBrowserKey(), null);
			if(!safeExamCheck) {
				sb.append("<h4><i class='o_icon o_icon_warn o_icon-fw'>&nbsp;</i>")
				  .append(translate("error.safe.exam")).append("</h4>")
				  .append(translate("error.safe.exam.desc", assessmentModule.getSafeExamBrowserDownloadUrl()));
			}
			allowed &= safeExamCheck;
		} else if(StringHelper.containsNonWhitespace(inspection.getSafeExamBrowserConfigPList())) {
			boolean safeExamCheck = isSafelyAllowed(ureq, null, inspection.getSafeExamBrowserConfigPListKey());
			if(!safeExamCheck) {
				sb.append("<h4><i class='o_icon o_icon_warn o_icon-fw'>&nbsp;</i>")
				  .append(translate("error.safe.exam")).append("</h4>")
				  .append(translate("error.safe.exam.desc", assessmentModule.getSafeExamBrowserDownloadUrl()));
				
				guard.getDownloadSEBButton().setVisible(true);
				guard.getDownloadSEBConfigurationButton().setVisible(inspection.isSafeExamBrowserConfigDownload());
			}
			allowed &= safeExamCheck;
		}

		String state;
		if(allowed) {
			FormLink start = guard.getStartInspection();
			state = updateButtons(inspection, now, start);
		} else {
			state = "error";
		}
		guard.sync(state, sb.toString());
		return guard;
	}
	
	private ResourceGuard createGuard(TransientAssessmentInspection inspection) {
		String id = Long.toString(CodeHelper.getRAMUniqueID());
		
		FormLink startInspectionButton = uifactory.addFormLink("start-" + id, "start", "current.inspection.start", null, flc, Link.BUTTON);
		startInspectionButton.setElementCssClass("o_sel_assessment_start");
		startInspectionButton.setCustomEnabledLinkCSS("btn btn-primary");
		startInspectionButton.setCustomDisabledLinkCSS("o_disabled btn btn-default");
		
		FormLink cancelInspectionButton = uifactory.addFormLink("cancel-" + id, "cancel", "cancel", null, flc, Link.BUTTON);
		cancelInspectionButton.setElementCssClass("o_sel_assessment_cancel");
		
		TextElement accessCodeEl = uifactory.addTextElement("code-" + id, "access.code", "access.code", 128, "", flc);
		accessCodeEl.setVisible(StringHelper.containsNonWhitespace(inspection.getAccessCode()));
		
		String fromTo = getFromTo(inspection);
		String duration = translate("inspection.duration", Integer.toString(inspection.getDurationInSeconds() / 60));
		String iconCssClass = CourseNodeFactory.getInstance()
				.getCourseNodeConfigurationEvenForDisabledBB(inspection.getCourseNodeType()).getIconCSSClass();
		
		String quitUrl = inspection.getLinkToQuitSEB();
		ExternalLinkItem quitSEBButton = uifactory.addExternalLink("download.seb-" + id, translate("current.mode.seb.quit"), quitUrl, null, flc);
		quitSEBButton.setName(translate("current.mode.seb.quit"));
		quitSEBButton.setTooltip(translate("current.mode.seb.quit"));
		quitSEBButton.setElementCssClass("btn btn-default");
		quitSEBButton.setVisible(inspection.hasLinkToQuitSEB());
		
		String setUrl = Settings.createServerURI() + mapperUri + "/" + inspection.getInspectionKey() + "/" + SafeExamBrowserConfigurationMediaResource.SEB_SETTINGS_FILENAME;
		ExternalLinkItem downloadSEBConfigurationButton = uifactory.addExternalLink("download-seb-config-" + id, "download.seb.config", setUrl, "_blank", flc);
		downloadSEBConfigurationButton.setElementCssClass("btn btn-default");
		downloadSEBConfigurationButton.setName(translate("download.seb.config"));
		downloadSEBConfigurationButton.setTarget("_self");
		downloadSEBConfigurationButton.setVisible(false);
		
		String sebUrl = assessmentModule.getSafeExamBrowserDownloadUrl();
		ExternalLinkItem downloadSEBLink = uifactory.addExternalLink("download.seb-" + id, translate("download.seb"), sebUrl, "_blank", flc);
		downloadSEBLink.setName(translate("download.seb"));
		downloadSEBLink.setTooltip(translate("download.seb"));
		downloadSEBLink.setElementCssClass("btn btn-default");
		downloadSEBLink.setVisible(false);

		ResourceGuard guard = new ResourceGuard(inspection, iconCssClass, fromTo, duration,
				startInspectionButton, cancelInspectionButton, accessCodeEl,
				quitSEBButton, downloadSEBLink, downloadSEBConfigurationButton);
		startInspectionButton.setUserObject(guard);
		cancelInspectionButton.setUserObject(guard);
		accessCodeEl.setUserObject(guard);
		
		return guard;
	}
	
	private String getFromTo(TransientAssessmentInspection inspection) {
		if(inspection.getFromDate() != null && inspection.getToDate() != null) {
			Date from = inspection.getFromDate();
			Date to = inspection.getToDate();
			Formatter format = Formatter.getInstance(getLocale());
			if(DateUtils.isSameDay(from, to) && DateUtils.isSameDay(from, new Date())) {
				return translate("inspection.fromto.same.day", format.formatTimeShort(from), format.formatTimeShort(to));
			} 
			return translate("inspection.fromto", format.formatDateAndTime(from), format.formatDateAndTime(to));
		}
		return "-";
	}
	
	private boolean isSafelyAllowed(UserRequest ureq, String safeExamBrowserKeys, String configurationKey) {
		String safeExamHash = ureq.getParameter("configKey");
		String url = ureq.getParameter("urlForKeyHash");
		String browserExamKey = ureq.getParameter("browserExamKey");
		getLogger().info("SEB requests parameters - configkey: {}, url: {}, browser exam key: {}", safeExamHash, url, browserExamKey);
		return SafeExamBrowserValidator.isSafelyAllowed(ureq.getHttpReq(), safeExamBrowserKeys, configurationKey)
				|| SafeExamBrowserValidator.isSafelyAllowedJs(safeExamHash, url, safeExamBrowserKeys, configurationKey);
	}
	
	private String updateButtons(TransientAssessmentInspection inspection, Date now, FormLink start) {
		String state;
		
		Date begin = inspection.getFromDate();
		Date end = inspection.getToDate();
		if(begin.compareTo(now) <= 0 && end.compareTo(now) > 0) {
			state = Status.assessment.name();
			start.setEnabled(true);
			start.setVisible(true);
		} else if(end.compareTo(now) <= 0 || Status.end == inspection.getStatus()) {
			state = Status.end.name();
			start.setEnabled(false);
			start.setVisible(false);
		} else {
			state = "error";
			start.setEnabled(false);
			start.setVisible(false);
		}
		
		return state;
	}

	@Override
	public String getModalTitle() {
		return translate("inspection.overview.title");
	}

	@Override
	public boolean updateLockRequests(UserRequest ureq) {
		return false;
	}

	@Override
	public void event(Event event) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("start".equals(cmd) && link.getUserObject() instanceof ResourceGuard guard) {
				launchAssessmentInspection(ureq, guard);
			} else if("cancel".equals(cmd)) {
				markAsCancelled(ureq, link.getUserObject());
				continueAfterAssessmentInspection(ureq, link.getUserObject());
			} else if(("continue".equals(cmd) || "continue-main".equals(cmd))) {
				continueAfterAssessmentInspection(ureq, link.getUserObject());
			}
		} else if("ONCLICK".equals(event.getCommand()) && "checkSEBKeys".equals(ureq.getParameter("cid"))) {
			syncInspections(ureq);
			flc.contextPut("checked", "checked");
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void markAsCancelled(UserRequest ureq, Object selectedGuard) {
		UserSession usess = ureq.getUserSession();
		if(selectedGuard instanceof ResourceGuard resourceGuard) {
			usess.addCancelledLockRequest(resourceGuard.getInspection());
		} else {
			for(TransientAssessmentInspection inspection:inspections) {
				usess.addCancelledLockRequest(inspection);
			}
		}
	}
	
	private void continueAfterAssessmentInspection(UserRequest ureq, Object selectedGuard) {
		if(selectedGuard instanceof ResourceGuard resourceGuard) {
			resourceGuard.getInspection().setStatus(Status.followup);
		}
		ureq.getUserSession().setLockRequests(null);
		
		//make sure to see the navigation bar
		ChiefController cc = Windows.getWindows(ureq).getChiefController(ureq);
		cc.getScreenMode().setMode(Mode.standard, null);
			
		fireEvent(ureq, new Event("continue"));
		NewControllerFactory.getInstance().launch("[MyCoursesSite:0]", ureq, getWindowControl());
	}
	
	private void launchAssessmentInspection(UserRequest ureq, ResourceGuard guard) {
		TransientAssessmentInspection inspection = guard.getInspection();
		
		guard.getAccessCode().clearError();
		if(StringHelper.containsNonWhitespace(inspection.getAccessCode())) {
			if(!StringHelper.containsNonWhitespace(guard.getAccessCode().getValue())) {
				guard.getAccessCode().setErrorKey("form.legende.mandatory");
				return;
			}
			if(!inspection.getAccessCode().equals(guard.getAccessCode().getValue())) {
				guard.getAccessCode().setErrorKey("error.access.code");
				inspectionService.log(Action.incorrectAccessCode, null, guard.getAccessCode().getValue(), inspection, getIdentity());
				return;
			}
		}
		
		ureq.getUserSession().setLockRequests(null);
		OLATResourceable resource = inspection.getResource();
		ureq.getUserSession().setLockResource(resource, inspection);
		getWindowControl().getWindowBackOffice().getChiefController().lockResource(resource);
		fireEvent(ureq, new LockRequestEvent(LockRequestEvent.CHOOSE_ASSESSMENT_INSPECTION, inspection));
		
		String businessPath = "[AssessmentInspection:" + inspection.getInspectionKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());

		inspectionService.startInspection(getIdentity(), inspection);
	}
	
	public static class ResourceGuards {
		
		private List<ResourceGuard> guards = new ArrayList<>();
		
		public boolean isEmpty() {
			return guards.isEmpty();
		}
		
		public int getSize() {
			return guards.size();
		}
		
		public ResourceGuard getGuardFor(TransientAssessmentInspection mode) {
			ResourceGuard guard = null;
			for(ResourceGuard g:getList()) {
				if(g.getRequestKey().equals(mode.getRequestKey())) {
					guard = g;
				}
			}
			return guard;
		}
		
		public List<LockRequest> toLockRequests() {
			return guards.stream()
					.map(ResourceGuard::getInspection)
					.map(LockRequest.class::cast)
					.toList();
		}

		public List<ResourceGuard> getList() {
			return guards;
		}

		public void setList(List<ResourceGuard> guards) {
			this.guards = guards;
		}
	}
	
	public static class SettingsMapper implements Mapper {
		
		private final ResourceGuards guards;
		
		public SettingsMapper(ResourceGuards guards) {
			this.guards = guards;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			if(relPath.endsWith(SafeExamBrowserConfigurationMediaResource.SEB_SETTINGS_FILENAME)) {
				int index = relPath.indexOf(SafeExamBrowserConfigurationMediaResource.SEB_SETTINGS_FILENAME) - 1;
				if(index > 0) {
					String guardKey = relPath.substring(0, index);
					int lastIndex = guardKey.lastIndexOf('/');
					if(lastIndex >= 0 && guardKey.length() > lastIndex + 1) {
						guardKey = guardKey.substring(lastIndex + 1);
						if(StringHelper.isLong(guardKey)) {
							for(ResourceGuard guard:guards.getList()) {
								if(guardKey.equals(guard.getInspectionKey().toString())) {
									return new SafeExamBrowserConfigurationMediaResource(guard.getSafeExamBrowserConfigPList());
								}							
							}
						}
					}
				}
			}
			
			if(!guards.getList().isEmpty()) {
				ResourceGuard guard = guards.getList().get(0);		
				return new SafeExamBrowserConfigurationMediaResource(guard.getSafeExamBrowserConfigPList());
			}
			return new NotFoundMediaResource();
		}
	}
	
	public static class ResourceGuard {
		
		private final FormLink startInspectionButton;
		private final FormLink cancelInspectionButton;
		private final TextElement accessCodeEl;
		private final ExternalLinkItem quitSEBButton;
		private final ExternalLinkItem downloadSEBButton;
		private final ExternalLinkItem downloadSEBConfigurationButton;

		private String errors;
		private String status;
		
		private final String fromTo;
		private final String duration;
		private final String iconCssClass;
		private final String safeExamBrowserHint;
		private final String safeExamBrowserConfigPList;
		private final TransientAssessmentInspection inspection;
		
		public ResourceGuard(TransientAssessmentInspection inspection, String iconCssClass, String fromTo, String duration,
				FormLink startInspectionButton, FormLink cancelInspectionButton, TextElement accessCodeEl,
				ExternalLinkItem quitSEBButton, ExternalLinkItem downloadSEBButton, ExternalLinkItem downloadSEBConfigurationButton) {
			this.inspection = inspection;
			this.fromTo = fromTo;
			this.duration = duration;
			this.iconCssClass = iconCssClass;
			safeExamBrowserHint = inspection.getSafeExamBrowserHint();
			safeExamBrowserConfigPList = inspection.getSafeExamBrowserConfigPList();
			this.startInspectionButton = startInspectionButton;
			this.cancelInspectionButton = cancelInspectionButton;
			this.accessCodeEl = accessCodeEl;
			this.quitSEBButton = quitSEBButton;
			this.downloadSEBButton = downloadSEBButton;
			this.downloadSEBConfigurationButton = downloadSEBConfigurationButton;
		}
		
		public Long getInspectionKey() {
			return inspection.getInspectionKey();
		}
		
		public Long getRequestKey() {
			return inspection.getRequestKey();
		}
		
		public TransientAssessmentInspection getInspection() {
			return inspection;
		}
		
		public String getFromTo() {
			return fromTo;
		}
		
		public String getDuration() {
			return duration;
		}
		
		public String getCourseDisplayName() {
			return inspection.getCourseDisplayName();
		}
		
		public String getCourseExternalRef() {
			return inspection.getCourseExternalRef();
		}
		
		public String getCourseNodeName() {
			return inspection.getCourseNodeName();
		}
		
		public String getCourseNodeIconCSSClass() {
			return iconCssClass;
		}
		
		public String getStatus() {
			return status;
		}
		
		public String getErrors() {
			return errors;
		}
		
		public String getSafeExamBrowserHint() {
			return safeExamBrowserHint;
		}

		public String getSafeExamBrowserConfigPList() {
			return safeExamBrowserConfigPList;
		}

		public FormLink getStartInspection() {
			return startInspectionButton;
		}
		
		public FormLink getCancelInspection() {
			return cancelInspectionButton;
		}
		
		public TextElement getAccessCode() {
			return accessCodeEl;
		}

		public ExternalLinkItem getQuitSEBButton() {
			return quitSEBButton;
		}

		public ExternalLinkItem getDownloadSEBButton() {
			return downloadSEBButton;
		}

		public ExternalLinkItem getDownloadSEBConfigurationButton() {
			return downloadSEBConfigurationButton;
		}
		
		public void sync(String newStatus, String newErrors) {
			this.status = newStatus;
			this.errors = newErrors;
		}
	}
}
