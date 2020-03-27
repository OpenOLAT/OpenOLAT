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
package org.olat.core.commons.services.doceditor.ui;

import java.util.List;

import org.olat.core.commons.services.doceditor.collabora.ui.CollaboraAdminController;
import org.olat.core.commons.services.doceditor.office365.ui.Office365AdminController;
import org.olat.core.commons.services.doceditor.onlyoffice.ui.OnlyOfficeAdminController;
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
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;

/**
 * 
 * Initial date: 8 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AdminDocEditorController extends BasicController implements Activateable2  {

	private static final String COLLABORA_RES_TYPE = "Collabora";
	private static final String ONLY_OFFICE_RES_TYPE = "OnlyOffice";
	private static final String OFFICE365_RES_TYPE = "Office365";
	private static final String DOCUMENTS_IN_USE_RES_TYPE = "OpenDocuments";

	private VelocityContainer mainVC;
	private final Link collaboraLink;
	private final Link onlyOfficeLink;
	private final Link office365Link;
	private final Link documentsInUseLink;
	private final SegmentViewComponent segmentView;
	
	private Controller collaboraCtrl;
	private Controller onlyOfficeCtrl;
	private Controller office365Ctrl;
	private DocumentsInUseListController documentsInUseCtrl;

	public AdminDocEditorController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		collaboraLink = LinkFactory.createLink("admin.collabora", mainVC, this);
		segmentView.addSegment(collaboraLink, true);
		onlyOfficeLink = LinkFactory.createLink("admin.onlyoffice", mainVC, this);
		segmentView.addSegment(onlyOfficeLink, false);
		office365Link = LinkFactory.createLink("admin.office365", mainVC, this);
		segmentView.addSegment(office365Link, false);
		documentsInUseLink = LinkFactory.createLink("admin.documents.in.use", mainVC, this);
		segmentView.addSegment(documentsInUseLink, false);

		doOpenCollabora(ureq);
		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if(COLLABORA_RES_TYPE.equalsIgnoreCase(type)) {
			doOpenCollabora(ureq);
			segmentView.select(collaboraLink);
		} else if(ONLY_OFFICE_RES_TYPE.equalsIgnoreCase(type)) {
			doOpenOnlyOffice(ureq);
			segmentView.select(onlyOfficeLink);
		} else if(OFFICE365_RES_TYPE.equalsIgnoreCase(type)) {
			doOpenOffice365(ureq);
			segmentView.select(office365Link);
		} else if(DOCUMENTS_IN_USE_RES_TYPE.equalsIgnoreCase(type)) {
			doOpenDocumentsInUse(ureq);
			segmentView.select(documentsInUseLink);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == collaboraLink) {
					doOpenCollabora(ureq);
				} else if (clickedLink == onlyOfficeLink) {
					doOpenOnlyOffice(ureq);
				} else if (clickedLink == office365Link) {
					doOpenOffice365(ureq);
				} else if (clickedLink == documentsInUseLink) {
					doOpenDocumentsInUse(ureq);
				}
			}
		}
	}
	
	private void doOpenCollabora(UserRequest ureq) {
		if(collaboraCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(COLLABORA_RES_TYPE), null);
			collaboraCtrl = new CollaboraAdminController(ureq, swControl);
			listenTo(collaboraCtrl);
		} else {
			addToHistory(ureq, collaboraCtrl);
		}
		mainVC.put("segmentCmp", collaboraCtrl.getInitialComponent());
	}
	
	private void doOpenOnlyOffice(UserRequest ureq) {
		if(onlyOfficeCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ONLY_OFFICE_RES_TYPE), null);
			onlyOfficeCtrl = new OnlyOfficeAdminController(ureq, swControl);
			listenTo(onlyOfficeCtrl);
		} else {
			addToHistory(ureq, onlyOfficeCtrl);
		}
		mainVC.put("segmentCmp", onlyOfficeCtrl.getInitialComponent());
	}
	
	private void doOpenOffice365(UserRequest ureq) {
		if(office365Ctrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(OFFICE365_RES_TYPE), null);
			office365Ctrl = new Office365AdminController(ureq, swControl);
			listenTo(office365Ctrl);
		} else {
			addToHistory(ureq, office365Ctrl);
		}
		mainVC.put("segmentCmp", office365Ctrl.getInitialComponent());
	}
	
	private void doOpenDocumentsInUse(UserRequest ureq) {
		if(documentsInUseCtrl == null) {
			WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(DOCUMENTS_IN_USE_RES_TYPE), null);
			documentsInUseCtrl = new DocumentsInUseListController(ureq, swControl);
			listenTo(documentsInUseCtrl);
		} else {
			documentsInUseCtrl.loadModel();
			addToHistory(ureq, documentsInUseCtrl);
		}
		mainVC.put("segmentCmp", documentsInUseCtrl.getInitialComponent());
	}
	
	@Override
	protected void doDispose() {
		//
	}

}
