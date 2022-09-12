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
package org.olat.ims.qti21.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.segmentedview.SegmentViewComponent;
import org.olat.core.gui.components.segmentedview.SegmentViewEvent;
import org.olat.core.gui.components.segmentedview.SegmentViewFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.resource.OresHelper;
import org.olat.ims.qti21.ui.editor.testsexport.TestsExportAdminController;
import org.olat.modules.grading.ui.GradingAdminController;

/**
 * 
 * Initial date: 12 Sep 2022<br>
 * 
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class TestAdminController extends BasicController implements Activateable2 {

	private static final String ORES_TYPE_QTI = "QTI";
	private static final String ORES_TYPE_GRADING = "Correction";
	private static final String ORES_TYPE_GRADING_WORKFLOW = "CorrectionWorkflow";
	private static final String ORES_TYPE_EXPORT = "Export";

	private final VelocityContainer mainVC;
	private final SegmentViewComponent segmentView;
	private final Link qtiLink;
	private final Link qtiCorrectionLink;
	private final Link gradingWorkflowLink;
	private final Link exportLink;

	private final QTI21AdminController qtiCtrl;
	private final QTI21CorrectionAdminController qtiCorrectionCtrl;
	private final GradingAdminController gradingWorkflowCtrl;
	private final TestsExportAdminController exportCtrl;

	public TestAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl);

		WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_QTI), null);
		qtiCtrl = new QTI21AdminController(ureq, bwControl);
		listenTo(qtiCtrl);

		bwControl = addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_GRADING), null);
		qtiCorrectionCtrl = new QTI21CorrectionAdminController(ureq, bwControl);
		listenTo(qtiCorrectionCtrl);

		addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_GRADING_WORKFLOW), null);
		gradingWorkflowCtrl = new GradingAdminController(ureq, bwControl);
		listenTo(gradingWorkflowCtrl);

		addToHistory(ureq, OresHelper.createOLATResourceableType(ORES_TYPE_EXPORT), null);
		exportCtrl = new TestsExportAdminController(ureq, bwControl);
		listenTo(exportCtrl);

		mainVC = createVelocityContainer("segments");
		segmentView = SegmentViewFactory.createSegmentView("segments", mainVC, this);

		qtiLink = LinkFactory.createLink("admin.segment.qti", mainVC, this);
		segmentView.addSegment(qtiLink, true);

		qtiCorrectionLink = LinkFactory.createLink("admin.segment.grading.config", mainVC, this);
		segmentView.addSegment(qtiCorrectionLink, false);

		gradingWorkflowLink = LinkFactory.createLink("admin.segment.grading.workflow", mainVC, this);
		segmentView.addSegment(gradingWorkflowLink, false);

		exportLink = LinkFactory.createLink("admin.segment.export", mainVC, this);
		segmentView.addSegment(exportLink, false);

		doOpenQti(ureq);
		putInitialPanel(mainVC);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if (entries == null || entries.isEmpty())
			return;

		String type = entries.get(0).getOLATResourceable().getResourceableTypeName();
		if (ORES_TYPE_QTI.equalsIgnoreCase(type)) {
			doOpenQti(ureq);
		} else if (ORES_TYPE_GRADING.equalsIgnoreCase(type)) {
			doOpenQtiCorrection(ureq);
		} else if (ORES_TYPE_GRADING_WORKFLOW.equalsIgnoreCase(type)) {
			doOpenGradingWorkflow(ureq);
		} else if (ORES_TYPE_EXPORT.equalsIgnoreCase(type)) {
			doOpenExport(ureq);
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		if (source == segmentView) {
			if (event instanceof SegmentViewEvent) {
				SegmentViewEvent sve = (SegmentViewEvent)event;
				String segmentCName = sve.getComponentName();
				Component clickedLink = mainVC.getComponent(segmentCName);
				if (clickedLink == qtiLink) {
					doOpenQti(ureq);
				} else if (clickedLink == qtiCorrectionLink) {
					doOpenQtiCorrection(ureq);
				} else if (clickedLink == gradingWorkflowLink) {
					doOpenGradingWorkflow(ureq);
				} else if (clickedLink == exportLink) {
					doOpenExport(ureq);
				}
			}
		}
	}

	private void doOpenQti(UserRequest ureq) {
		addToHistory(ureq, qtiCtrl);
		mainVC.put("segmentCmp", qtiCtrl.getInitialComponent());
		segmentView.select(qtiLink);
	}

	private void doOpenQtiCorrection(UserRequest ureq) {
		addToHistory(ureq, qtiCorrectionCtrl);
		mainVC.put("segmentCmp", qtiCorrectionCtrl.getInitialComponent());
		segmentView.select(qtiCorrectionLink);
	}

	private void doOpenGradingWorkflow(UserRequest ureq) {
		addToHistory(ureq, gradingWorkflowCtrl);
		mainVC.put("segmentCmp", gradingWorkflowCtrl.getInitialComponent());
		segmentView.select(gradingWorkflowLink);
	}

	private void doOpenExport(UserRequest ureq) {
		addToHistory(ureq, exportCtrl);
		mainVC.put("segmentCmp", exportCtrl.getInitialComponent());
		segmentView.select(exportLink);
	}

}
