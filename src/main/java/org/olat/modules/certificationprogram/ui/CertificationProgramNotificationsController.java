/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.certificationprogram.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DetailsToggleEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController.InputType;
import org.olat.core.util.i18n.ui.SingleKeyTranslatorController.SingleKey;
import org.olat.core.util.mail.MailHelper;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailConfigurationStatus;
import org.olat.modules.certificationprogram.CertificationProgramMailType;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.manager.CertificationProgramMailing;
import org.olat.modules.certificationprogram.manager.CertificationProgramMailing.CPMailTemplate;
import org.olat.modules.certificationprogram.manager.CertificationProgramMailing.I18nKeys;
import org.olat.modules.certificationprogram.ui.CertificationProgramNotificationsTableModel.NotificationsCols;
import org.olat.modules.certificationprogram.ui.component.CertificationProgramMailConfigurationComparator;
import org.olat.modules.certificationprogram.ui.component.CustomizedCellRenderer;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 10 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramNotificationsController extends AbstractNotificationsController {

	private static final String TOGGLE_DETAILS_CMD = "toggle-details";
	
	private int count = 0;

	private FlexiTableElement tableEl;
	private CertificationProgramNotificationsTableModel tableModel;
	
	private ToolsController toolsCtrl;
	private SingleKeyTranslatorController translatorCtrl;
	private CloseableCalloutWindowController calloutCtrl;

	@Autowired
	private DB dbInstance;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificationProgramNotificationsController(UserRequest ureq, WindowControl wControl,
			CertificationProgram certificationProgram) {
		super(ureq, wControl, "notifications", certificationProgram);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationsCols.notification));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationsCols.content,
				new CustomizedCellRenderer(getTranslator())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(NotificationsCols.status));
		columnsModel.addFlexiColumnModel(new ActionsColumnModel(NotificationsCols.tools));
		
		tableModel = new CertificationProgramNotificationsTableModel(columnsModel);
		
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 25, false, getTranslator(), formLayout);
		
		tableEl.setDetailsRenderer(detailsVC, this);
		tableEl.setMultiDetails(true);
	}
	
	@Override
	protected void loadModel() {
		List<CertificationProgramMailConfiguration> configurations = certificationProgramService.getMailConfigurations(certificationProgram);
		Set<CertificationProgramMailType> types = CertificationProgramMailType.notificationsSet(certificationProgram);

		List<CertificationProgramNotificationRow> rows = new ArrayList<>(5);
		for(CertificationProgramMailConfiguration configuration:configurations) {
			CertificationProgramMailType type = configuration.getType();
			if(!types.contains(type) || configuration.getStatus() == CertificationProgramMailConfigurationStatus.deleted) {
				continue;
			}
			
			String label = translate("notifications.".concat(type.name()));
			CertificationProgramNotificationRow certificateIssuedRow = forgeRow(label, configuration);
			rows.add(certificateIssuedRow);
		}
		
		Collections.sort(rows, new CertificationProgramMailConfigurationComparator());
		
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CertificationProgramNotificationRow forgeRow(String label, CertificationProgramMailConfiguration configuration) {
		FormToggle statusEl = uifactory.addToggleButton("status_" + (++count), null, translate("on"), translate("off"), flc);
		statusEl.setLabel("Test", null, true);
		statusEl.toggle(configuration.getStatus() == CertificationProgramMailConfigurationStatus.active);
		
		FormLink toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		CertificationProgramNotificationRow row = new CertificationProgramNotificationRow(label, configuration, statusEl, toolsLink);
		toolsLink.setUserObject(row);
		statusEl.setUserObject(row);
		return row;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(toolsCtrl == source) {
			if(event == Event.CLOSE_EVENT) {
				calloutCtrl.deactivate();
				cleanUp();
			}
		} else if(translatorCtrl == source && translatorCtrl.getUserObject() instanceof CertificationProgramNotificationRow row) {
			if(event == Event.CHANGED_EVENT || event == Event.DONE_EVENT) {
				doSaveCustomisedTemplate(row); 
			}
			cmc.deactivate();
			cleanUp();
		} else if(calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		super.cleanUp();
		removeAsListenerAndDispose(translatorCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		translatorCtrl = null;
		calloutCtrl = null;
		toolsCtrl = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		 if(tableEl == source) {
			 if(event instanceof SelectionEvent se) {
				String cmd = se.getCommand();
				if(TOGGLE_DETAILS_CMD.equals(cmd)) {
					CertificationProgramNotificationRow row = tableModel.getObject(se.getIndex());
					if(row.getDetailsController() != null) {
						doCloseNotificationDetails(row);
						tableEl.collapseDetails(se.getIndex());
					} else {
						doOpenNotificationsDetails(ureq, row);
						tableEl.expandDetails(se.getIndex());
					}
				}
			} else if(event instanceof DetailsToggleEvent toggleEvent) {
				CertificationProgramNotificationRow row = tableModel.getObject(toggleEvent.getRowIndex());
				if(toggleEvent.isVisible()) {
					doOpenNotificationsDetails(ureq, row);
				} else {
					doCloseNotificationDetails(row);
				}
			}
		} else if(source instanceof FormLink link) {
			String cmd = link.getCmd();
			if("tools".equals(cmd) && link.getUserObject() instanceof CertificationProgramNotificationRow row) {
				doOpenTools(ureq, row, link.getFormDispatchId());
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenTools(UserRequest ureq, CertificationProgramNotificationRow elementRow, String targetId) {
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), elementRow);
		listenTo(toolsCtrl);
	
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), targetId, "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private void doTranslate(UserRequest ureq, CertificationProgramNotificationRow notificationRow) {
		if(guardModalController(translatorCtrl)) return;
		
		String description = MailHelper.getVariableNamesHelp(CPMailTemplate.variableNames(), getLocale());
		I18nKeys customTemplate = CertificationProgramMailing.getCustomI18nKeys(notificationRow);
		I18nKeys template = CertificationProgramMailing.getDefaultI18nKeys(notificationRow.getType(),
				certificationProgram.hasCreditPoints());
		SingleKey subjectKey = new SingleKey(customTemplate.subject(), InputType.TEXT_ELEMENT, template.subject());
		SingleKey bodyKey = new SingleKey(customTemplate.body(), InputType.RICH_TEXT_ELEMENT, template.body());
		List<SingleKey> keys = List.of(subjectKey, bodyKey);
		translatorCtrl = new SingleKeyTranslatorController(ureq, getWindowControl(), keys,
				CertificationProgramNotificationsController.class, description);
		translatorCtrl.setUserObject(notificationRow);
		listenTo(translatorCtrl);

		String title = translate("translate.title", notificationRow.getNotificationLabel());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), translatorCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSaveCustomisedTemplate(CertificationProgramNotificationRow notificationRow) {
		CertificationProgramMailConfiguration config = certificationProgramService.getMailConfiguration(notificationRow.getKey());
		config.setCustomized(true);
		certificationProgramService.updateMailConfiguration(config);
		dbInstance.commit();
		loadModel();
	}
	
	private class ToolsController extends BasicController {
		
		private Link editLink;
		private Link resetLink;
		
		private final CertificationProgramNotificationRow notificationRow;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CertificationProgramNotificationRow notificationRow) {
			super(ureq, wControl);
			this.notificationRow = notificationRow;
			
			VelocityContainer mainVC = createVelocityContainer("tool_notifications");
			
			if(notificationRow.isCustomized()) {
				editLink = LinkFactory.createLink("notification.edit", "edit", getTranslator(), mainVC, this, Link.LINK);
				editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_edit");
				resetLink = LinkFactory.createLink("notification.reset", "reset", getTranslator(), mainVC, this, Link.LINK);
				resetLink.setIconLeftCSS("o_icon o_icon-fw o_icon_recycle");
			} else {
				editLink = LinkFactory.createLink("notification.customize", "edit", getTranslator(), mainVC, this, Link.LINK);
				editLink.setIconLeftCSS("o_icon o_icon-fw o_icon_mail");
			}

			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(editLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doTranslate(ureq, notificationRow);
			} else if(resetLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doConfirmReset(ureq, notificationRow);
			}
		}
	}
}
