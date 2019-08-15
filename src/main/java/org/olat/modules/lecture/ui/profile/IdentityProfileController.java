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
package org.olat.modules.lecture.ui.profile;

import java.util.List;

import org.olat.admin.user.UserShortDescription;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.BreadcrumbedStackedPanel;
import org.olat.core.gui.components.tabbedpane.TabbedPane;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.modules.lecture.ui.AppealListRepositoryController;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;
import org.olat.modules.lecture.ui.ParticipantLecturesOverviewController;
import org.olat.modules.lecture.ui.coach.DispensationsController;
import org.olat.user.DisplayPortraitController;

/**
 * 
 * Initial date: 31 juil. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityProfileController extends BasicController implements Activateable2 {
	
	private Link addAbsence;
	private Link addNoticeOfAbsence;
	private Link addDispensation;
	private Link backLink;
	
	private TabbedPane tabPane;
	private final VelocityContainer mainVC;
	
	private DispensationsController dispensationsCtrl;
	private AppealListRepositoryController appealsCtrl;
	private DailyOverviewProfilController dailyOverviewCtrl;
	private ParticipantLecturesOverviewController lecturesCtrl;
	
	public IdentityProfileController(UserRequest ureq, WindowControl wControl, Identity profiledIdentity,
			LecturesSecurityCallback secCallback, boolean withBack) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		
		mainVC = createVelocityContainer("profile");
		if(withBack) {
			backLink = LinkFactory.createLinkBack(mainVC, this);
			mainVC.put("back", backLink);
		}
		
		DisplayPortraitController portraitCtr = new DisplayPortraitController(ureq, getWindowControl(), profiledIdentity, true, false);
		listenTo(portraitCtr);
		mainVC.put("portrait", portraitCtr.getInitialComponent());
		UserShortDescription userDescr = new UserShortDescription(ureq, getWindowControl(), profiledIdentity);
		listenTo(userDescr);
		mainVC.put("userDescr", userDescr.getInitialComponent());
		
		//new absence, new notice of absence, new dispensation
		addAbsence = LinkFactory.createButton("add.absence", mainVC, this);
		addAbsence.setIconLeftCSS("o_icon o_icon_add");
		addAbsence.setVisible(secCallback.canAddAbsences());

		addNoticeOfAbsence = LinkFactory.createButton("add.notice.absence", mainVC, this);
		addNoticeOfAbsence.setIconLeftCSS("o_icon o_icon_add");
		addNoticeOfAbsence.setVisible(secCallback.canAddNoticeOfAbsences());

		addDispensation = LinkFactory.createButton("add.dispensation", mainVC, this);
		addDispensation.setIconLeftCSS("o_icon o_icon_add");
		addDispensation.setVisible(secCallback.canAddDispensations());

		tabPane = new TabbedPane("tabPane", getLocale());
		tabPane.setElementCssClass("o_sel_lectures_profile");
		tabPane.addListener(this);
		mainVC.put("tabPane", tabPane);
		
		// day overview
		dailyOverviewCtrl = new DailyOverviewProfilController(ureq, getWindowControl(), profiledIdentity, secCallback);
		listenTo(dailyOverviewCtrl);
		tabPane.addTab(translate("cockpit.day.overview"), dailyOverviewCtrl);
		
		// list of lectures
		tabPane.addTab(translate("user.overview.lectures"), uureq -> {
			lecturesCtrl = new ParticipantLecturesOverviewController(uureq, getWindowControl(), profiledIdentity, null,
					true, true, true, true, true, false);
			listenTo(lecturesCtrl);
			BreadcrumbedStackedPanel lecturesPanel = new BreadcrumbedStackedPanel("lectures", getTranslator(), lecturesCtrl);
			lecturesPanel.pushController(translate("user.overview.lectures"), lecturesCtrl);
			lecturesCtrl.setBreadcrumbPanel(lecturesPanel);
			lecturesPanel.setInvisibleCrumb(1);
			return lecturesPanel;
		});

		// dispensation
		tabPane.addTab(translate("user.overview.dispensation"), uureq -> {
			dispensationsCtrl = new DispensationsController(uureq, getWindowControl(), null, secCallback, false, false);
			listenTo(dispensationsCtrl);
			return dispensationsCtrl.getInitialComponent();
		});

		// appeals
		tabPane.addTab(translate("user.overview.appeals"), uureq -> {
			appealsCtrl = new AppealListRepositoryController(uureq, getWindowControl(), profiledIdentity, secCallback);
			listenTo(appealsCtrl);
			return appealsCtrl.getInitialComponent();
		});
		
		putInitialPanel(mainVC);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == backLink) {
			fireEvent(ureq, Event.BACK_EVENT);
		}
	}
}
