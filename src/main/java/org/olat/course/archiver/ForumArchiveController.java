/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
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
package org.olat.course.archiver;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.DateUtils;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.model.AssessmentNodeData;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.fo.archiver.ForumReportExportResource;

/**
 * Extended GenericArchiveController for Forum specific actions and buttons like generating statistical reports
 * <p>
 * Initial date: Apr 17, 2023
 *
 * @author Sumit Kapoor, sumit.kapoor@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class ForumArchiveController extends GenericArchiveController {

	private static final String FORUM_REPORT_GENERATOR = "fo.report.generator";

	private FormLink reportButton;

	private ForumArchiveReportExportController forumArchiveReportExportCtrl;
	private CloseableModalController cmc;
	private List<CourseNode> courseNodes;

	/**
	 * Constructor for the assessment tool controller.
	 *
	 * @param ureq        The user request
	 * @param wControl    The window control
	 * @param ores        The resourceable of the course
	 * @param withOptions Allow to configure the archive options
	 * @param nodeTypes   The node types to export
	 */
	public ForumArchiveController(UserRequest ureq, WindowControl wControl, OLATResourceable ores, boolean withOptions, CourseNode nodeType) {
		super(ureq, wControl, ores, withOptions, nodeType);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		super.initForm(formLayout, listener, ureq);
		reportButton = uifactory.addFormLink("fo.report", formLayout, Link.BUTTON);
	}

	/**
	 * start exporting forum report by opening cmc for further export options
	 *
	 * @param ureq
	 */
	private void doStartExportReport(UserRequest ureq) {
		courseNodes = getCourseNodes();

		// show warning if no item was selected in the table
		if (courseNodes.isEmpty()) {
			showWarning("warning.atleast.node");
		} else {
			// activate cmc for export options
			forumArchiveReportExportCtrl = new ForumArchiveReportExportController(ureq, getWindowControl());
			listenTo(forumArchiveReportExportCtrl);

			cmc = new CloseableModalController(getWindowControl(), "cancel", forumArchiveReportExportCtrl.getInitialComponent(),
					true, translate(FORUM_REPORT_GENERATOR));
			listenTo(cmc);
			cmc.activate();
		}
	}

	/**
	 * @return list of selected courseNodes
	 */
	private List<CourseNode> getCourseNodes() {
		ICourse course = CourseFactory.loadCourse(ores);
		Set<Integer> selectedIndex = tableEl.getMultiSelectedIndex();
		List<CourseNode> nodes = new ArrayList<>(selectedIndex.size());

		// add selected course elements to nodes List
		for (Integer index : selectedIndex) {
			AssessmentNodeData nodeData = nodeTableModel.getObject(index);
			CourseNode node = course.getRunStructure().getNode(nodeData.getIdent());
			if (matchTypes(node)) {
				nodes.add(node);
			}
		}

		return nodes;
	}

	/**
	 * Create forum report export and dispatch it
	 *
	 * @param ureq
	 * @param beginDate        from date
	 * @param endDate          inclusive date
	 * @param selectedOrgaKeys
	 */
	private void doExportReport(UserRequest ureq, Date beginDate, Date endDate, List<String> selectedOrgaKeys) {
		ICourse course = CourseFactory.loadCourse(ores);
		// trigger export and dispatch its result as .xlsx
		ForumReportExportResource foReportExport = new ForumReportExportResource(courseNodes, course, getTranslator(), beginDate, endDate, selectedOrgaKeys);
		ureq.getDispatchResult().setResultingMediaResource(foReportExport);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source == reportButton) {
			doStartExportReport(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if (source == forumArchiveReportExportCtrl) {
			if (event == Event.DONE_EVENT) {
				if (forumArchiveReportExportCtrl.getReportDataEl().isKeySelected("all")) {
					// if no filter is selected and all data should be exported
					doExportReport(ureq, null, null, null);
				} else {
					Date beginDate = forumArchiveReportExportCtrl.getDateRangeEl().getDate();
					// increasing by 1, because end date is inclusive
					Date endDate = forumArchiveReportExportCtrl.getDateRangeEl().getSecondDate() != null
							? DateUtils.addDays(forumArchiveReportExportCtrl.getDateRangeEl().getSecondDate(), 1) : null;
					List<String> selectedOrgaKeys = forumArchiveReportExportCtrl.getOrgaSelectionEl().getSelectedKeys().stream().toList();

					doExportReport(ureq, beginDate, endDate, selectedOrgaKeys);
				}
			}
			// after done or canceled, deactivate cmc and clean up
			deactivateCmc();
			cleanUp();
		}
	}

	private void deactivateCmc() {
		if (cmc != null) {
			cmc.deactivate();
		}
	}

	private void cleanUp() {
		removeAsListenerAndDispose(forumArchiveReportExportCtrl);
		removeAsListenerAndDispose(cmc);
		forumArchiveReportExportCtrl = null;
		cmc = null;
	}
}
