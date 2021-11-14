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

import org.olat.core.commons.services.doceditor.DocEditorAdminSegment;
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
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 8 Apr 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class AdminDocEditorController extends BasicController implements Activateable2 {

	private static final String DOCUMENTS_IN_USE_RES_TYPE = "OpenDocuments";

	private VelocityContainer mainVC;
	private final Link documentsInUseLink;
	private final SegmentViewComponent segmentView;
	
	private Controller editorCtrl;
	private DocumentsInUseListController documentsInUseCtrl;
	
	private int counter = 0;

	@Autowired
	private List<DocEditorAdminSegment> adminSegments;
	
	public AdminDocEditorController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);
		
		mainVC = createVelocityContainer("admin");
		
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);
		
		adminSegments.sort((s1, s2) -> s1.getLinkName(getLocale()).compareToIgnoreCase(s2.getLinkName(getLocale())));
		for (DocEditorAdminSegment adminSegment : adminSegments) {
			String name = "ed-" + (++counter);
			Link link = LinkFactory.createLink(name, name, getTranslator(), mainVC, this, Link.NONTRANSLATED);
			link.setCustomDisplayText(adminSegment.getLinkName(getLocale()));
			link.setUserObject(adminSegment);
			segmentView.addSegment(link, false);
		}
		
		documentsInUseLink = LinkFactory.createLink("admin.documents.in.use", mainVC, this);
		segmentView.addSegment(documentsInUseLink, false);
		
		Component firstLink = segmentView.getSegments().get(0);
		segmentView.select(firstLink);
		doOpenAdminSegment(ureq, adminSegments.get(0));
		
		putInitialPanel(mainVC);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (DOCUMENTS_IN_USE_RES_TYPE.equalsIgnoreCase(type)) {
			doOpenDocumentsInUse(ureq);
			segmentView.select(documentsInUseLink);
		} else {
			DocEditorAdminSegment adminSegment = getAdminSegment(type);
			if (adminSegment != null) {
				doOpenAdminSegment(ureq, adminSegment);
			}
		}
	}

	private DocEditorAdminSegment getAdminSegment(String type) {
		for (Component component : segmentView.getSegments()) {
			if (component instanceof Link) {
				Link link = (Link)component;
				Object userObject = link.getUserObject();
				if (userObject instanceof DocEditorAdminSegment) {
					DocEditorAdminSegment adminSegment = (DocEditorAdminSegment)userObject;
					if (adminSegment.getBusinessPathType().equalsIgnoreCase(type)) {
						segmentView.select(component);
						return adminSegment;
					}
				}
			}
		}
		return null;
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if(source == segmentView) {
			if(event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == documentsInUseLink) {
					doOpenDocumentsInUse(ureq);
				} else if (clickedLink instanceof Link) {
					Link link = (Link)clickedLink;
					Object userObject = link.getUserObject();
					if (userObject instanceof DocEditorAdminSegment) {
						DocEditorAdminSegment adminSegment = (DocEditorAdminSegment)userObject;
						doOpenAdminSegment(ureq, adminSegment);
					}
				}
			}
		}
	}
	
	private void doOpenAdminSegment(UserRequest ureq, DocEditorAdminSegment adminSegment) {
		removeAsListenerAndDispose(editorCtrl);
		editorCtrl = null;
		
		WindowControl swControl = addToHistory(ureq, OresHelper.createOLATResourceableType(adminSegment.getBusinessPathType()), null);
		editorCtrl = adminSegment.createController(ureq, swControl);
		listenTo(editorCtrl);
		mainVC.put("segmentCmp", editorCtrl.getInitialComponent());
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
}
