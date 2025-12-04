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
package org.olat.modules.creditpoint.ui;

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyState;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.emptystate.EmptyStateFactory;
import org.olat.core.gui.components.scope.Scope;
import org.olat.core.gui.components.scope.ScopeEvent;
import org.olat.core.gui.components.scope.ScopeFactory;
import org.olat.core.gui.components.scope.ScopeSelection;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.certificationprogram.ui.CertificationHelper;
import org.olat.modules.creditpoint.CreditPointService;
import org.olat.modules.creditpoint.CreditPointSystem;
import org.olat.modules.creditpoint.CreditPointSystemStatus;
import org.olat.modules.creditpoint.CreditPointWallet;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 4 juil. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CreditPointUserController extends BasicController {
	
	private static final String SYSTEM_PREFIX = "system_";
	
	private final VelocityContainer mainVC;
	private ScopeSelection systemsSelection;
	
	private final Identity assessedIdentity;
	private final List<CreditPointSystem> systems;
	private final CreditPointSecurityCallback secCallback;
	
	private CreditPointUserTransactionsController transactionsCtrl;
	
	@Autowired
	private CreditPointService creditPointService;
	
	public CreditPointUserController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, ureq.getIdentity(), CreditPointSecurityCallbackFactory.userToolSecurityCallback());
	}
	
	public CreditPointUserController(UserRequest ureq, WindowControl wControl,
			Identity assessedIdentity, CreditPointSecurityCallback secCallback) {
		super(ureq, wControl);
		this.secCallback = secCallback;
		this.assessedIdentity = assessedIdentity;
		systems = creditPointService.getCreditPointSystems(assessedIdentity);
		
		mainVC = createVelocityContainer("user_systems");
		if(assessedIdentity.equals(ureq.getIdentity())) {
			mainVC.contextPut("withHeader", Boolean.TRUE);
			mainVC.contextPut("title", translate("my.credit.point"));
		} else {
			mainVC.contextPut("withHeader", Boolean.FALSE);
			mainVC.contextPut("title", translate("credit.point"));
		}
		
		initScopes(ureq, true);
		putInitialPanel(mainVC);
	}
	
	private ScopeSelection initScopes(UserRequest ureq, boolean openDetails) {
		List<CreditPointWallet> wallets = creditPointService.getWallets(assessedIdentity);
		List<Scope> systemScopes = new ArrayList<>();
		for(CreditPointSystem system:systems) {
			if(system.getStatus() == CreditPointSystemStatus.active) {
				CreditPointWallet wallet = wallets.stream()
						.filter(w -> system.equals(w.getCreditPointSystem()))
						.findFirst().orElse(null);
				
				String hint = wallet == null
						? StringHelper.escapeHtml(system.getLabel())
						: StringHelper.escapeHtml(CertificationHelper.creditPointsToString(wallet.getBalance(), system));
				systemScopes.add(ScopeFactory.createScope(SYSTEM_PREFIX + system.getKey(),
					StringHelper.escapeHtml(system.getName()), hint));
			}
		}
		
		if(systemsSelection != null) {
			systemsSelection.removeListener(this);
		}
		systemsSelection = ScopeFactory.createScopeSelection("systemsSelection", mainVC, this, systemScopes);
		
		if(systemScopes.isEmpty()) {
			initEmptyState();
		} else {
			systemsSelection.setSelectedKey(systemScopes.get(0).getKey());
			if(openDetails) {
				doSelect(ureq, systemsSelection.getSelectedKey());
			}
		}
		
		return systemsSelection;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(transactionsCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				initScopes(ureq, false);
			}
		}
		
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(systemsSelection == source) {
			if (event instanceof ScopeEvent se) {
				doSelect(ureq, se.getSelectedKey());
			}
		}
	}
	
	private void initEmptyState() {
		EmptyStateConfig emptyState = EmptyStateConfig.builder()
				.withIconCss("o_icon_coins")
				.withMessageI18nKey("error.no.credit.point.system")
				.build();
		EmptyState emptyStateCmp = EmptyStateFactory.create("emptyStateCmp", null, this, emptyState);
		emptyStateCmp.setTranslator(getTranslator());
		mainVC.put("transactions", emptyStateCmp);
	}
	
	private void doSelect(UserRequest ureq, String key) {
		CreditPointSystem system = getCreditPointSystem(key);
		doSelect(ureq, system);
	}
	
	private void doSelect(UserRequest ureq, CreditPointSystem system) {
		removeAsListenerAndDispose(transactionsCtrl);
		
		CreditPointWallet wallet = creditPointService.getOrCreateWallet(assessedIdentity, system);
		transactionsCtrl = new CreditPointUserTransactionsController(ureq, getWindowControl(), wallet, secCallback);
		listenTo(transactionsCtrl);
		
		mainVC.put("transactions", transactionsCtrl.getInitialComponent());
	}
	
	private CreditPointSystem getCreditPointSystem(String key) {
		return systems.stream()
				.filter(sys -> (SYSTEM_PREFIX.concat(sys.getKey().toString())).equals(key))
				.findFirst().orElse(null);
	}
}
