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
package org.olat.core.commons.fullWebApp;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.Windows;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.ExternalLink;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.ChiefController;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.ScreenMode.Mode;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.assessment.AssessmentModeNotificationEvent;

/**
 * 
 * Initial date: 22 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UnlockGuardController extends BasicController implements LockGuardController, GenericEventListener {
	
	private final Link mainContinueButton;
	private final ExternalLink mainSEBQuitButton;
	private final VelocityContainer mainVC;
	
	private boolean pushUpdate = false;
	
	/**
	 *
	 * @param ureq
	 * @param wControl
	 * @param modes List of assessments
	 * @param forcePush Async popup need forcePush=true
	 */
	public UnlockGuardController(UserRequest ureq, WindowControl wControl, boolean forcePush) {
		super(ureq, wControl);
		this.pushUpdate = forcePush;
		
		mainVC = createVelocityContainer("unlock");
		mainVC.contextPut("checked", hasSEBHeaders(ureq) ? "checked" : "not-checked");
		
		mainContinueButton = LinkFactory.createCustomLink("continue-main", "continue-main", "unlock.continue.main", Link.BUTTON, mainVC, this);
		mainContinueButton.setElementCssClass("o_sel_assessment_continue");
		mainContinueButton.setCustomEnabledLinkCSS("btn btn-primary");
		mainContinueButton.setCustomDisabledLinkCSS("o_disabled btn btn-default");
		mainContinueButton.setVisible(false);
		mainVC.put("continue-main", mainContinueButton);
		
		mainSEBQuitButton = LinkFactory.createExternalLink("quit-seb-main", translate("unlock.seb.quit"), "");
		mainSEBQuitButton.setElementCssClass("btn btn-default btn-primary o_sel_assessment_quit");
		mainSEBQuitButton.setName(translate("unlock.seb.quit"));
		mainSEBQuitButton.setVisible(false);
		mainVC.put("quit-main", mainSEBQuitButton);
		
		syncAssessmentModes();
		
		putInitialPanel(mainVC);
		
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
	
	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, AssessmentModeNotificationEvent.ASSESSMENT_MODE_NOTIFICATION);
        super.doDispose();
	}
	
	@Override
	public boolean updateLockRequests(UserRequest ureq) {
		boolean f;
		if(pushUpdate) {
			syncAssessmentModes();
			f = true;
			pushUpdate = false;
		} else {
			f = false;
		}
		return f;
	}
	
	private void syncAssessmentModes() {
		String quitUrl = getSEBQuitURLFromLastUnlockedResource();
		
		mainContinueButton.setVisible(true);
		if(StringHelper.containsNonWhitespace(quitUrl)) {
			mainSEBQuitButton.setUrl(quitUrl);
			mainSEBQuitButton.setVisible(true);
			// prefer the quit URL in SEB
			mainContinueButton.setVisible(false);
		} else {
			mainSEBQuitButton.setVisible(false);
		}
	
		String unlockInfos = getUnlockInfos();
		mainVC.contextPut("unlockInfos", unlockInfos);
		mainVC.setDirty(true);
	}
	
	private String getUnlockInfos() {
		LockResourceInfos infos = getWindowControl().getWindowBackOffice().getChiefController().getLastUnlockedResourceInfos();
		if(infos != null && infos.getLockMode() != null) {
			return infos.getLockMode().getUnlockInfos(getLocale());
		}
		return translate("unlock.infos");
	}
	
	@Override
	public String getModalTitle() {
		LockResourceInfos infos = getWindowControl().getWindowBackOffice().getChiefController().getLastUnlockedResourceInfos();
		if(infos != null && infos.getLockMode() != null) {
			return infos.getLockMode().getUnlockModalTitle(getLocale());
		}
		return translate("unlock.modal.title");
	}
	
	private String getSEBQuitURLFromLastUnlockedResource() {
		LockResourceInfos infos = getWindowControl().getWindowBackOffice().getChiefController().getLastUnlockedResourceInfos();
		if(infos != null && infos.getLockMode() != null && infos.getLockMode().hasLinkToQuitSEB()) {
			return infos.getLockMode().getLinkToQuitSEB();
		}
		return null;
	}

	@Override
	public void event(Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source instanceof Link link) {
			String cmd = link.getCommand();
			if("continue".equals(cmd) || "continue-main".equals(cmd)) {
				continueAfterAssessmentMode(ureq);
			}
		}
	}
	
	private void continueAfterAssessmentMode(UserRequest ureq) {	
		//make sure to see the navigation bar
		ChiefController cc = Windows.getWindows(ureq).getChiefController(ureq);
		cc.getScreenMode().setMode(Mode.standard, null);
			
		fireEvent(ureq, new Event("continue"));
		String businessPath = "[MyCoursesSite:0]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}
}