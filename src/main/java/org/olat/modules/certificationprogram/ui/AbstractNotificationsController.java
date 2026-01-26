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
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormToggle;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController;
import org.olat.core.gui.control.generic.confirmation.ConfirmationController.ButtonType;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramMailConfiguration;
import org.olat.modules.certificationprogram.CertificationProgramMailConfigurationStatus;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 19 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public abstract class AbstractNotificationsController extends FormBasicController implements FlexiTableComponentDelegate {

	protected final VelocityContainer detailsVC;
	protected FlexiTableElement tableEl;
	protected CertificationProgramNotificationsTableModel tableModel;
	
	protected final CertificationProgram certificationProgram;
	protected final CertificationProgramSecurityCallback secCallback;
	
	protected CloseableModalController cmc;
	private ConfirmationController confirmResetCtrl;
	
	@Autowired
	protected DB dbInstance;
	@Autowired
	protected CertificationProgramService certificationProgramService;
	
	public AbstractNotificationsController(UserRequest ureq, WindowControl wControl, String page,
			CertificationProgram certificationProgram, CertificationProgramSecurityCallback secCallback) {
		super(ureq, wControl, page);
		this.secCallback = secCallback;
		this.certificationProgram = certificationProgram;

		detailsVC = createVelocityContainer("program_notification_details");
	}
	
	protected abstract void loadModel();
	
	/**
	 * Reopen the details controller after loading the model.
	 * 
	 * @param ureq The user request
	 * @param detailsCtrl The details controller which produces a change event
	 */
	private void reloadModel(UserRequest ureq, CertificationProgramNotificationDetailsController detailsCtrl) {
		CertificationProgramNotificationRow notificationRow = null;
		if(detailsCtrl != null && detailsCtrl.getNotificationRow().getDetailsController() != null) {
			notificationRow = detailsCtrl.getNotificationRow();
			doCloseNotificationDetails(notificationRow);
		}
		
		loadModel();
		
		if(notificationRow != null) {
			int index = tableModel.getIndexByKey(notificationRow.getKey());
			if(index >= 0) {
				CertificationProgramNotificationRow detailsRow = tableModel.getObject(index);
				doOpenNotificationsDetails(ureq, detailsRow);
				tableEl.expandDetails(index);
			}
		}
		tableEl.reset(false, false, false);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		List<Component> components = new ArrayList<>(1);
		if(rowObject instanceof CertificationProgramNotificationRow elementRow
				&& elementRow.getDetailsController() != null) {
			components.add(elementRow.getDetailsController().getInitialFormItem().getComponent());
		}
		return components;
	}

	@Override
	public boolean isDetailsRow(int row, Object rowObject) {
		return true;
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmResetCtrl == source && confirmResetCtrl.getUserObject() instanceof CertificationProgramNotificationRow row) {
			if (event == Event.DONE_EVENT) {
				doReset(row);
			}
			cmc.deactivate();
			cleanUp();
		} else if(source instanceof CertificationProgramNotificationDetailsController detailsCtrl && event == Event.CHANGED_EVENT) {
			reloadModel(ureq, detailsCtrl);
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	protected void cleanUp() {
		removeAsListenerAndDispose(confirmResetCtrl);
		removeAsListenerAndDispose(cmc);
		confirmResetCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormToggle toggle && toggle.getUserObject() instanceof CertificationProgramNotificationRow row) {
			doToggleStatus(row, toggle);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	protected final void doConfirmReset(UserRequest ureq, CertificationProgramNotificationRow notificationRow) {
		confirmResetCtrl = new ConfirmationController(ureq, getWindowControl(),
				translate("confirm.reset.template.message"),
				null, translate("reset.template"), ButtonType.regular);
		listenTo(confirmResetCtrl);
		confirmResetCtrl.setUserObject(notificationRow);
		
		String title = translate("confirm.reset.template.title", notificationRow.getNotificationLabel());
		cmc = new CloseableModalController(getWindowControl(), translate("close"), confirmResetCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private final void doReset(CertificationProgramNotificationRow notificationRow) {
		CertificationProgramMailConfiguration config = certificationProgramService.getMailConfiguration(notificationRow.getKey());
		config.setCustomized(false);
		certificationProgramService.updateMailConfiguration(config);
		dbInstance.commit();
		loadModel();
	}
	
	protected final void doOpenNotificationsDetails(UserRequest ureq, CertificationProgramNotificationRow row) {
		if(row == null) return;
		
		if(row.getDetailsController() != null) {
			removeAsListenerAndDispose(row.getDetailsController());
			flc.remove(row.getDetailsController().getInitialFormItem());
		}
		
		CertificationProgramNotificationDetailsController detailsCtrl = new CertificationProgramNotificationDetailsController(ureq, getWindowControl(), mainForm,
				certificationProgram, row, secCallback);
		listenTo(detailsCtrl);
		row.setDetailsController(detailsCtrl);
		flc.add(detailsCtrl.getInitialFormItem());
	}
	
	protected final void doCloseNotificationDetails(CertificationProgramNotificationRow row) {
		if(row.getDetailsController() == null) return;
		removeAsListenerAndDispose(row.getDetailsController());
		flc.remove(row.getDetailsController().getInitialFormItem());
		row.setDetailsController(null);
	}
	
	private final void doToggleStatus(CertificationProgramNotificationRow row, FormToggle toggle) {
		CertificationProgramMailConfiguration configuration = certificationProgramService.getMailConfiguration(row.getKey());
		CertificationProgramMailConfigurationStatus status = toggle.isOn()
				? CertificationProgramMailConfigurationStatus.active
				: CertificationProgramMailConfigurationStatus.inactive;
		configuration.setStatus(status);
		certificationProgramService.updateMailConfiguration(configuration);
		dbInstance.commit();
		loadModel();
	}

}
