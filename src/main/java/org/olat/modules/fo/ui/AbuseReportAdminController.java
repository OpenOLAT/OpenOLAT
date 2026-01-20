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
package org.olat.modules.fo.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.modules.fo.AbuseReport;
import org.olat.modules.fo.AbuseReport.AbuseReportStatus;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.Message;
import org.olat.modules.fo.manager.ForumManager;
import org.olat.modules.fo.ui.AbuseReportDataModel.AbuseReportCols;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Admin controller for moderators to manage abuse reports.
 * 
 * Initial date: January 2026<br>
 * @author OpenOLAT Community
 */
public class AbuseReportAdminController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private AbuseReportDataModel dataModel;
	
	private DialogBoxController confirmDismissCtrl;
	private DialogBoxController confirmActionCtrl;
	
	private final Forum forum;
	private final Formatter formatter;
	
	@Autowired
	private ForumManager forumManager;
	@Autowired
	private UserManager userManager;
	
	public AbuseReportAdminController(UserRequest ureq, WindowControl wControl, Forum forum) {
		super(ureq, wControl);
		setTranslator(Util.createPackageTranslator(Forum.class, getLocale(), getTranslator()));
		this.forum = forum;
		this.formatter = Formatter.getInstance(getLocale());
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		setFormTitle("abuse.reports.admin.title");
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbuseReportCols.message));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbuseReportCols.reporter));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbuseReportCols.date));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbuseReportCols.reason));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(AbuseReportCols.status));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("abuse.reports.actions", AbuseReportCols.actions.ordinal(), "actions", 
				new AbuseReportActionsCellRenderer()));
		
		dataModel = new AbuseReportDataModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "reports", dataModel, getTranslator(), formLayout);
		tableEl.setEmptyTableMessageKey("abuse.reports.none");
	}
	
	private void loadModel() {
		List<AbuseReport> reports = forumManager.getPendingAbuseReports(forum.getKey());
		List<AbuseReportRow> rows = new ArrayList<>(reports.size());
		
		for (AbuseReport report : reports) {
			Message message = report.getMessage();
			Identity reporter = report.getReporter();
			
			AbuseReportRow row = new AbuseReportRow();
			row.setReport(report);
			row.setMessageTitle(message.getTitle());
			row.setReporterName(userManager.getUserDisplayName(reporter));
			row.setReportDate(formatter.formatDateAndTime(report.getCreationDate()));
			row.setReason(report.getReason());
			row.setStatus(translate("abuse.reports.status." + report.getStatus().name().toLowerCase()));
			
			// Create action buttons
			FormLink dismissLink = uifactory.addFormLink("dismiss_" + report.getKey(), "dismiss", "abuse.reports.dismiss", 
					null, flc, Link.LINK);
			dismissLink.setUserObject(row);
			row.setDismissLink(dismissLink);
			
			FormLink actionLink = uifactory.addFormLink("action_" + report.getKey(), "action", "abuse.reports.take.action", 
					null, flc, Link.LINK);
			actionLink.setUserObject(row);
			row.setActionLink(actionLink);
			
			rows.add(row);
		}
		
		dataModel.setObjects(rows);
		tableEl.reset();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if (source instanceof FormLink) {
			FormLink link = (FormLink) source;
			String cmd = link.getCmd();
			AbuseReportRow row = (AbuseReportRow) link.getUserObject();
			
			if ("dismiss".equals(cmd)) {
				doConfirmDismiss(ureq, row);
			} else if ("action".equals(cmd)) {
				doConfirmAction(ureq, row);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == confirmDismissCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				AbuseReportRow row = (AbuseReportRow) confirmDismissCtrl.getUserObject();
				doDismissReport(ureq, row);
			}
		} else if (source == confirmActionCtrl) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				AbuseReportRow row = (AbuseReportRow) confirmActionCtrl.getUserObject();
				doTakeAction(ureq, row);
			}
		}
	}

	private void doConfirmDismiss(UserRequest ureq, AbuseReportRow row) {
		String text = translate("abuse.reports.confirm.dismiss");
		confirmDismissCtrl = activateYesNoDialog(ureq, null, text, confirmDismissCtrl);
		confirmDismissCtrl.setUserObject(row);
	}

	private void doDismissReport(UserRequest ureq, AbuseReportRow row) {
		try {
			forumManager.updateAbuseReportStatus(row.getReport(), AbuseReportStatus.DISMISSED, getIdentity());
			showInfo("abuse.reports.update.success");
			loadModel();
		} catch (Exception e) {
			logError("Error dismissing abuse report", e);
			showError("abuse.reports.update.error");
		}
	}

	private void doConfirmAction(UserRequest ureq, AbuseReportRow row) {
		String text = translate("abuse.reports.confirm.action");
		confirmActionCtrl = activateYesNoDialog(ureq, null, text, confirmActionCtrl);
		confirmActionCtrl.setUserObject(row);
	}

	private void doTakeAction(UserRequest ureq, AbuseReportRow row) {
		try {
			// Delete the reported message
			Message message = row.getReport().getMessage();
			forumManager.deleteMessageTree(forum.getKey(), message);
			
			// Update the abuse report status
			forumManager.updateAbuseReportStatus(row.getReport(), AbuseReportStatus.ACTION_TAKEN, getIdentity());
			showInfo("abuse.reports.update.success");
			loadModel();
			fireEvent(ureq, Event.CHANGED_EVENT);
		} catch (Exception e) {
			logError("Error taking action on abuse report", e);
			showError("abuse.reports.update.error");
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		// Not used
	}
	
	/**
	 * Row object for abuse report table
	 */
	public static class AbuseReportRow {
		private AbuseReport report;
		private String messageTitle;
		private String reporterName;
		private String reportDate;
		private String reason;
		private String status;
		private FormLink dismissLink;
		private FormLink actionLink;
		
		public AbuseReport getReport() {
			return report;
		}
		
		public void setReport(AbuseReport report) {
			this.report = report;
		}
		
		public String getMessageTitle() {
			return messageTitle;
		}
		
		public void setMessageTitle(String messageTitle) {
			this.messageTitle = messageTitle;
		}
		
		public String getReporterName() {
			return reporterName;
		}
		
		public void setReporterName(String reporterName) {
			this.reporterName = reporterName;
		}
		
		public String getReportDate() {
			return reportDate;
		}
		
		public void setReportDate(String reportDate) {
			this.reportDate = reportDate;
		}
		
		public String getReason() {
			return reason;
		}
		
		public void setReason(String reason) {
			this.reason = reason;
		}
		
		public String getStatus() {
			return status;
		}
		
		public void setStatus(String status) {
			this.status = status;
		}
		
		public FormLink getDismissLink() {
			return dismissLink;
		}
		
		public void setDismissLink(FormLink dismissLink) {
			this.dismissLink = dismissLink;
		}
		
		public FormLink getActionLink() {
			return actionLink;
		}
		
		public void setActionLink(FormLink actionLink) {
			this.actionLink = actionLink;
		}
	}
}
