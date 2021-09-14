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
package org.olat.modules.immunityProof.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.immunityProof.ImmunityProof;
import org.olat.modules.immunityProof.ImmunityProofModule;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 07.09.2021<br>
 *
 * @author aboeckle, alexander.boeckle@frentix.com, http://www.frentix.com
 */
public class ImmunityProofAdminController extends BasicController implements Activateable2 {

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segments;
	
	private final Link configurationLink;
	private final Link commissionersLink;
	private final Link entranceCodesLink;
	
	private ImmunityProofConfigurationController configurationController;
	private ImmunityProofManageCommissionersController commissionersController;
	private ImmunityProofEntranceCodesController entranceCodesController;
	
	@Autowired
	private ImmunityProofModule immunityProofModule;
	
	public ImmunityProofAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));
		
		mainVC = createVelocityContainer("immunity_proof_admin");
		
		configurationLink = org.olat.core.gui.components.link.LinkFactory.createLink("configuration", mainVC, this);
		commissionersLink = org.olat.core.gui.components.link.LinkFactory.createLink("commissioners", mainVC, this);
		entranceCodesLink = org.olat.core.gui.components.link.LinkFactory.createLink("entrance.codes", mainVC, this);
		
		segments = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segments.setDontShowSingleSegment(true);
		segments.addSegment(configurationLink, true);
		
		initSegments();
		putInitialPanel(mainVC);
		openConfiguration(ureq);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segments) {
            if (event instanceof SegmentViewEvent) {
                SegmentViewEvent sve = (SegmentViewEvent)event;
                String segmentCName = sve.getComponentName();
                Component clickedLink = mainVC.getComponent(segmentCName);
                if (clickedLink == configurationLink) {
                    openConfiguration(ureq);
                } else if (clickedLink == commissionersLink){
                    openCommissioners(ureq);
                } else if (clickedLink == entranceCodesLink) {
                    openQRCodes(ureq);
                }
            }
        }
	}
	
	@Override
    protected void event(UserRequest ureq, Controller source, Event event) {
        if (source == configurationController) {
            if (event == Event.CHANGED_EVENT) {
                initSegments();
            }
        }
    }

	@Override
	protected void doDispose() {
		
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {

	}
	
	private void initSegments() {
		if (immunityProofModule.isEnabled()) {
			if (!segments.getSegments().contains(commissionersLink)) {
				segments.addSegment(commissionersLink, false);
			}
			if (!segments.getSegments().contains(entranceCodesLink)) {
				segments.addSegment(entranceCodesLink, false);
			}
		} else {
			segments.removeSegment(commissionersLink);
			segments.removeSegment(entranceCodesLink);
		}
	}
	
	private void openConfiguration(UserRequest ureq) {
		if (configurationController == null) {
            OLATResourceable ores = OresHelper.createOLATResourceableInstance("Configuration", 0l);
            WindowControl bwControl = addToHistory(ureq, ores, null);
            configurationController = new ImmunityProofConfigurationController(ureq, bwControl);
            listenTo(configurationController);
        } else {
            addToHistory(ureq, configurationController);
        }

        mainVC.put("segmentCmp", configurationController.getInitialComponent());
	}
	
	private void openCommissioners(UserRequest ureq) {
		if (commissionersController == null) {
            OLATResourceable ores = OresHelper.createOLATResourceableInstance("Commissioners", 0l);
            WindowControl bwControl = addToHistory(ureq, ores, null);
            commissionersController = new ImmunityProofManageCommissionersController(ureq, bwControl);
            listenTo(commissionersController);
        } else {
            addToHistory(ureq, commissionersController);
        }

        mainVC.put("segmentCmp", commissionersController.getInitialComponent());
	}
	private void openQRCodes(UserRequest ureq) {
		if (entranceCodesController == null) {
            OLATResourceable ores = OresHelper.createOLATResourceableInstance("EntranceCodes", 0l);
            WindowControl bwControl = addToHistory(ureq, ores, null);
            entranceCodesController = new ImmunityProofEntranceCodesController(ureq, bwControl);
            listenTo(entranceCodesController);
        } else {
            addToHistory(ureq, entranceCodesController);
        }

        mainVC.put("segmentCmp", entranceCodesController.getInitialComponent());
	}

}
