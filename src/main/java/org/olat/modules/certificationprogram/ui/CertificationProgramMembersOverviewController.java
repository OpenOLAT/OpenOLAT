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
package org.olat.modules.certificationprogram.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.DateUtils;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters.Type;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 29 août 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramMembersOverviewController extends BasicController implements Activateable2 {
	
	private static final String CMD_CERTIFIED = "Certified";
	private static final String CMD_REMOVED = "Removed";
	
	private final ScopeSelection scopesEl;
	private final VelocityContainer mainVC;
	private final TooledStackedPanel toolbarPanel;
	
	private CertificationProgram certificationProgram;
	private final CertificationProgramSecurityCallback secCallback;
	
	private CertificationProgramRemovedMembersController removedMembersCtrl;
	private CertificationProgramCertifiedMembersController certifiedMembersCtrl;
	
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificationProgramMembersOverviewController(UserRequest ureq, WindowControl wControl, TooledStackedPanel toolbarPanel,
			CertificationProgram certificationProgram, CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.toolbarPanel = toolbarPanel;
		this.certificationProgram = certificationProgram;

		List<Scope> scopes = buildScopes(ureq);
		mainVC = createVelocityContainer("members_overview");
		scopesEl = ScopeFactory.createScopeSelection("scopes", mainVC, this, scopes);
		mainVC.put("scopes", scopesEl);
		putInitialPanel(mainVC);
		
		doOpenCertifiedMembersList(ureq);
	}
	
	private String getScopeHint(long numOf) {
		if(numOf <= 1) {
			return translate("members.scope.num.sing", Long.toString(numOf));
		}
		return translate("members.scope.num.plur", Long.toString(numOf));
	}
	
	private List<Scope> buildScopes(UserRequest ureq) {
		List<Scope> scopes = new ArrayList<>();
		Date referenceDate = DateUtils.getEndOfDay(ureq.getRequestTimestamp());
		CertificationProgramMemberSearchParameters searchParams = new CertificationProgramMemberSearchParameters(certificationProgram);
		searchParams.setType(Type.CERTIFIED);
		long numOfCertified = certificationProgramService.countMembers(searchParams, referenceDate);
		String certifiedHint = getScopeHint(numOfCertified);
		scopes.add(ScopeFactory.createScope(CMD_CERTIFIED, translate("members.scope.certified"), certifiedHint, "o_icon o_icon_certificate"));
		searchParams.setType(Type.REMOVED);
		long numOfRemoved = certificationProgramService.countMembers(searchParams, referenceDate);
		String removedHint = getScopeHint(numOfRemoved);
		scopes.add(ScopeFactory.createScope(CMD_REMOVED, translate("members.scope.removed"), removedHint, "o_icon o_icon_qual_exec_future"));
		return scopes;
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(certifiedMembersCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				updateScopes(ureq);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(scopesEl == source) {
			if (event instanceof ScopeEvent se) {
				if(CMD_CERTIFIED.equals(se.getSelectedKey())) {
					doOpenCertifiedMembersList(ureq);
				} else if(CMD_REMOVED.equals(se.getSelectedKey())) {
					doOpenRemovedMembersList(ureq);
				}
			}
		}
	}
	
	private void updateScopes(UserRequest ureq) {
		String selectedScope = scopesEl.getSelectedKey();
		List<Scope> scopes = buildScopes(ureq);
		scopesEl.setScopes(scopes);
		scopesEl.setSelectedKey(selectedScope);
	}
	
	private void doOpenCertifiedMembersList(UserRequest ureq) {
		removeAsListenerAndDispose(certifiedMembersCtrl);
		
		certifiedMembersCtrl = new CertificationProgramCertifiedMembersController(ureq, getWindowControl(), toolbarPanel,
				certificationProgram, secCallback);
		listenTo(certifiedMembersCtrl);
		mainVC.put("component", certifiedMembersCtrl.getInitialComponent());
	}
	
	private void doOpenRemovedMembersList(UserRequest ureq) {
		removeAsListenerAndDispose(removedMembersCtrl);
		
		removedMembersCtrl = new CertificationProgramRemovedMembersController(ureq, getWindowControl(), toolbarPanel,
				certificationProgram, secCallback);
		listenTo(removedMembersCtrl);
		mainVC.put("component", removedMembersCtrl.getInitialComponent());
	}

}
