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
package org.olat.modules.lecture.ui.coach;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewRendererType;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.modules.lecture.ui.LectureRepositoryAdminController;
import org.olat.modules.lecture.ui.LectureRoles;
import org.olat.modules.lecture.ui.LecturesSecurityCallback;

/**
 * 
 * Initial date: 9 août 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class MultiSearchesController extends BasicController implements Activateable2 {

	private Link teachersLink;
	private final Link entriesLink;
	private final Link curriculumsLink;
	private final Link participantsLink;
	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	
	private final LecturesSecurityCallback secCallback;

	private TeachersSearchController teachersSearchCtrl;
	private ParticipantsSearchController participantsSearchCtrl;
	private RepositoryEntriesSearchController entriesSearchCtrl;
	private CurriculumsSearchController curriculumsSearchCtrl;
	
	public MultiSearchesController(UserRequest ureq, WindowControl wControl, LecturesSecurityCallback secCallback) {
		super(ureq, wControl, Util.createPackageTranslator(LectureRepositoryAdminController.class, ureq.getLocale()));
		this.secCallback = secCallback;
		
		mainVC = createVelocityContainer("coaching");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		segmentView.setRendererType(SegmentViewRendererType.linked);
		participantsLink = LinkFactory.createLink("search.participants", mainVC, this);
		segmentView.addSegment(participantsLink, true);
		
		if(secCallback.viewAs() != LectureRoles.teacher) {
			teachersLink = LinkFactory.createLink("search.teachers", mainVC, this);
			segmentView.addSegment(teachersLink, false);
		}
		
		entriesLink = LinkFactory.createLink("search.entries", mainVC, this);
		segmentView.addSegment(entriesLink, false);
		curriculumsLink = LinkFactory.createLink("search.curriculums", mainVC, this);
		segmentView.addSegment(curriculumsLink, false);
		
		putInitialPanel(mainVC);
		doOpenParticipantsSearch(ureq);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) {
			return;
		}
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if("Participants".equalsIgnoreCase(type)) {
			doOpenParticipantsSearch(ureq);
			segmentView.select(participantsLink);
		} else if("Teachers".equalsIgnoreCase(type)) {
			doOpenTeachersSearch(ureq);
			segmentView.select(teachersLink);
		} else if("RepositoryEntries".equalsIgnoreCase(type)) {
			doOpenEntriesSearch(ureq);
			segmentView.select(entriesLink);
		} else if("Curriculums".equalsIgnoreCase(type)) {
			doOpenCurriculumsSearch(ureq);
			segmentView.select(curriculumsLink);
		} else if("Identity".equalsIgnoreCase(type)) {
			doOpenParticipantsSearch(ureq).activate(ureq, entries, null);
			segmentView.select(participantsLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == participantsLink) {
					doOpenParticipantsSearch(ureq);
				} else if (clickedLink == teachersLink) {
					doOpenTeachersSearch(ureq);
				} else if (clickedLink == entriesLink) {
					doOpenEntriesSearch(ureq);
				} else if (clickedLink == curriculumsLink) {
					doOpenCurriculumsSearch(ureq);
				}
			}
		}
	}
	
	private ParticipantsSearchController doOpenParticipantsSearch(UserRequest ureq) {
		if(participantsSearchCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Participants"), null);
			participantsSearchCtrl = new ParticipantsSearchController(ureq, swControl, secCallback);
			listenTo(participantsSearchCtrl);
		}
		addToHistory(ureq, participantsSearchCtrl);
		mainVC.put("segmentCmp", participantsSearchCtrl.getInitialComponent());
		return participantsSearchCtrl;
	}
	
	private void doOpenTeachersSearch(UserRequest ureq) {
		if(teachersSearchCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Teachers"), null);
			teachersSearchCtrl = new TeachersSearchController(ureq, swControl, secCallback);
			listenTo(teachersSearchCtrl);
		}
		addToHistory(ureq, teachersSearchCtrl);
		mainVC.put("segmentCmp", teachersSearchCtrl.getInitialComponent()); 
	}
	
	private void doOpenEntriesSearch(UserRequest ureq) {
		if(entriesSearchCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("RepositoryEntries"), null);
			entriesSearchCtrl = new RepositoryEntriesSearchController(ureq, swControl, secCallback);
			listenTo(entriesSearchCtrl);
		}
		addToHistory(ureq, entriesSearchCtrl);
		mainVC.put("segmentCmp", entriesSearchCtrl.getInitialComponent()); 
	}
	
	private void doOpenCurriculumsSearch(UserRequest ureq) {
		if(curriculumsSearchCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType("Curriculums"), null);
			curriculumsSearchCtrl = new CurriculumsSearchController(ureq, swControl, secCallback);
			listenTo(curriculumsSearchCtrl);
		}
		addToHistory(ureq, curriculumsSearchCtrl);
		mainVC.put("segmentCmp", curriculumsSearchCtrl.getInitialComponent()); 
	}
}
