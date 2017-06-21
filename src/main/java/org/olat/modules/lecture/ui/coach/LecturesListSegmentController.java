package org.olat.modules.lecture.ui.coach;

import java.util.List;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.modules.lecture.model.LectureBlockIdentityStatistics;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21 juin 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class LecturesListSegmentController extends BasicController {
	
	private final VelocityContainer mainVC;
	private SegmentViewComponent segmentView;
	private Link exportLink, aggregatedListLink, detailledListLink;
	
	private LecturesListController detailledListCtrl;
	private LecturesListController aggregatedListCtrl;
	
	private final String propsIdentifier;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final List<LectureBlockIdentityStatistics> statistics;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public LecturesListSegmentController(UserRequest ureq, WindowControl wControl,
			List<LectureBlockIdentityStatistics> statistics,
			List<UserPropertyHandler> userPropertyHandlers, String propsIdentifier) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.statistics = statistics;
		this.propsIdentifier = propsIdentifier;
		this.userPropertyHandlers = userPropertyHandlers;
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		
		mainVC = createVelocityContainer("segmented_list");
		
		exportLink = LinkFactory.createButton("export", mainVC, this);
		exportLink.setIconLeftCSS("o_icon o_icon_download");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		aggregatedListLink = LinkFactory.createLink("aggregated.list", mainVC, this);
		segmentView.addSegment(aggregatedListLink, true);
		doOpenAggregatedListController(ureq);
		detailledListLink = LinkFactory.createLink("detailled.list", mainVC, this);
		segmentView.addSegment(detailledListLink, false);

		putInitialPanel(mainVC);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(event instanceof SegmentViewEvent) {
			SegmentViewEvent sve = (SegmentViewEvent)event;
			String segmentCName = sve.getComponentName();
			Component clickedLink = mainVC.getComponent(segmentCName);
			if (clickedLink == aggregatedListLink) {
				doOpenAggregatedListController(ureq);
			} else if (clickedLink == detailledListLink) {
				doOpenDetailledListController(ureq);
			}
		} else if(source == exportLink) {
			doExportStatistics(ureq);
		}
	}

	@Override
	protected void doDispose() {
		//
	}
	
	private Controller doOpenAggregatedListController(UserRequest ureq) {
		if(aggregatedListCtrl == null) {
			List<LectureBlockIdentityStatistics> aggregatedStatistics = LecturesSearchController.groupByIdentity(statistics);
			aggregatedListCtrl = new LecturesListController(ureq, getWindowControl(),
					aggregatedStatistics, userPropertyHandlers, propsIdentifier, false, false);
			listenTo(aggregatedListCtrl);
		}
		mainVC.put("segmentCmp", aggregatedListCtrl.getInitialComponent());
		return null;
	}
	
	private Controller doOpenDetailledListController(UserRequest ureq) {
		if(detailledListCtrl == null) {
			detailledListCtrl = new LecturesListController(ureq, getWindowControl(),
					statistics, userPropertyHandlers, propsIdentifier, true, false);
			listenTo(detailledListCtrl);
		}
		mainVC.put("segmentCmp", detailledListCtrl.getInitialComponent());
		return detailledListCtrl;
	}
	
	private void doExportStatistics(UserRequest ureq) {
		LecturesStatisticsExport export = new LecturesStatisticsExport(statistics, userPropertyHandlers, isAdministrativeUser, getTranslator());
		ureq.getDispatchResult().setResultingMediaResource(export);
	}
}
