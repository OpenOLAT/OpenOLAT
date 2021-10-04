package org.olat.modules.immunityproof.ui;

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
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.immunityproof.ImmunityProof;
import org.olat.modules.immunityproof.ImmunityProofModule;
import org.olat.modules.immunityproof.ui.event.ImmunityProofAddedEvent;
import org.springframework.beans.factory.annotation.Autowired;

public class ImmunityProofCreateWrapperController extends BasicController {

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segments;

	private final Link manualLink;
	private final Link automaticLink;

	private final Identity editedIdentity;
	private final boolean usedAsCovidCommissioner;

	private ImmunityProofCreateAutomaticallyController automaticController;
	private ImmunityProofCreateManuallyController manualController;

	@Autowired
	private ImmunityProofModule immunityProofModule;

	protected ImmunityProofCreateWrapperController(UserRequest ureq, WindowControl wControl, Identity editedIdentiy,
			boolean usedAsCovidCommissioner) {
		super(ureq, wControl);

		setTranslator(Util.createPackageTranslator(ImmunityProof.class, getLocale(), getTranslator()));

		this.editedIdentity = editedIdentiy;
		this.usedAsCovidCommissioner = usedAsCovidCommissioner;

		mainVC = createVelocityContainer("immunity_proof_create_wrapper");

		manualLink = org.olat.core.gui.components.link.LinkFactory.createLink("create.manually", mainVC, this);
		automaticLink = org.olat.core.gui.components.link.LinkFactory.createLink("create.automatically", mainVC, this);

		segments = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segments.setDontShowSingleSegment(true);

		initSegments();
		putInitialPanel(mainVC);

		if (immunityProofModule.isScanningEnabled() && !usedAsCovidCommissioner) {
			openAutomaticScan(ureq);
		} else {
			openManualCreation(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segments) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent) event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == manualLink) {
					openManualCreation(ureq);
				} else if (clickedLink == automaticLink) {
					openAutomaticScan(ureq);
				}
			}
		}
		
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (event.equals(Event.CANCELLED_EVENT)) {
			cleanUp();
		} else if (source == automaticController) {
			if (event == Event.DONE_EVENT) {
				segments.setVisible(false);

				fireEvent(ureq, new ImmunityProofAddedEvent(editedIdentity));
			}
		} 

		fireEvent(ureq, event);
	}

	private void initSegments() {
		if (!segments.getSegments().contains(automaticLink) && immunityProofModule.isScanningEnabled()) {
			segments.addSegment(automaticLink, false);
		}

		if (!segments.getSegments().contains(manualLink)) {
			segments.addSegment(manualLink, false);
		}
	}

	private void openAutomaticScan(UserRequest ureq) {
		if (automaticController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Scan", 0l);
			WindowControl bwControl = addToHistory(ureq, ores, null);
			automaticController = new ImmunityProofCreateAutomaticallyController(ureq, bwControl, editedIdentity,
					usedAsCovidCommissioner);
			listenTo(automaticController);
		} else {
			addToHistory(ureq, automaticController);
		}

		segments.select(automaticLink);

		mainVC.put("segmentCmp", automaticController.getInitialComponent());
	}

	private void openManualCreation(UserRequest ureq) {
		if (manualController == null) {
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Manual", 0l);
			WindowControl bwControl = addToHistory(ureq, ores, null);
			manualController = new ImmunityProofCreateManuallyController(ureq, bwControl, editedIdentity,
					usedAsCovidCommissioner);
			listenTo(manualController);
		} else {
			addToHistory(ureq, manualController);
		}

		segments.select(manualLink);

		mainVC.put("segmentCmp", manualController.getInitialComponent());
	}

	private void cleanUp() {
		removeAsListenerAndDispose(automaticController);
		removeAsListenerAndDispose(manualController);

		automaticController = null;
		manualController = null;
	}

	@Override
	protected void doDispose() {
		
	}

}
