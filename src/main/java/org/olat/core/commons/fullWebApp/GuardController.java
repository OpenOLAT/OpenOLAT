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

import java.util.ArrayList;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 déc. 2023<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class GuardController extends BasicController {
	
	private final String address;
	
	private LockGuardController ctrl;
	private CloseableModalController cmc;
	
	@Autowired
	private List<LockGuardSPI> guardSpiList;
	
	public GuardController(UserRequest ureq, WindowControl wControl, List<LockRequest> requests, boolean forcePush) {
		super(ureq, wControl);
		address = ureq.getHttpReq().getRemoteAddr();
		putInitialPanel(new Panel("assessment-mode-chooser"));
		
		VelocityContainer mainVC = createVelocityContainer("guard");

		if(requests == null || requests.isEmpty()) {
			ctrl = new UnlockGuardController(ureq, wControl, forcePush);
			listenTo(ctrl);
			mainVC.put("guard", ctrl.getInitialComponent());
		} else {
			List<LockRequest> allRequests = new ArrayList<>(requests);
			List<GuardSPIToRequests> guardsToRequests = triageOfRequests(allRequests);
			
			// Check if a guard has an higher priority
			GuardSPIToRequests guard = guardsToRequests.stream()
					.filter(GuardSPIToRequests::isHighPriority)
					.findFirst().orElse(null);
			if(guard == null) {
				guard = guardsToRequests.get(0);
			}
			// Present only a guard at once
			LockGuardController typedGuardCtrl = guard.createController(ureq, wControl, address, forcePush);
			listenTo(typedGuardCtrl);
			mainVC.put("guard", typedGuardCtrl.getInitialComponent());
			ctrl = typedGuardCtrl;
		}
		
		cmc = new CloseableModalController(getWindowControl(), translate("close"), mainVC, true, ctrl.getModalTitle(), false);	
		cmc.activate();
		listenTo(cmc);
	}
	
	private List<GuardSPIToRequests> triageOfRequests(List<LockRequest> allRequests) {
		List<GuardSPIToRequests> spiToRequest = new ArrayList<>();
		for(LockGuardSPI guardSpi:guardSpiList) {
			List<LockRequest> filteredRequests  = guardSpi.filterRequests(allRequests);
			if(filteredRequests != null && !filteredRequests.isEmpty()) {
				spiToRequest.add(new GuardSPIToRequests(guardSpi, filteredRequests));
			}		
		}
		return spiToRequest;
	}
	
	public record GuardSPIToRequests(LockGuardSPI guardSpi, List<LockRequest> requests) {
		
		public boolean isHighPriority() {
			return guardSpi().isPriority();
		}

		public LockGuardController createController(UserRequest ureq, WindowControl wControl, String address, boolean forcePush) {
			return guardSpi().createGuardController(ureq, wControl, requests(), address, forcePush);
		}
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
	protected void event(UserRequest ureq, Controller source, Event event) {
		if("continue".equals(event.getCommand()) || event instanceof LockRequestEvent) {
			deactivate();
		}
		fireEvent(ureq, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	/**
	 * @param ureq The user request
	 * @return true if need update
	 */
	public boolean updateLockRequests(UserRequest ureq) {
		if(ctrl != null) {
			return ctrl.updateLockRequests(ureq);
		}
		return false;
	}
}
