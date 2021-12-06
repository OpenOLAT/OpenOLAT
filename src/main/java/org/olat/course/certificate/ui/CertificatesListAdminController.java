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
package org.olat.course.certificate.ui;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 20.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesListAdminController extends FormBasicController {
	
	private FormLink uploadLink;
	private FlexiTableElement tableEl;
	private TemplatesDataModel tableModel;
	
	private CloseableModalController cmc;
	private UploadCertificateTemplateController uploadCtrl;
	private DialogBoxController confirmDeleteCtrl;

	@Autowired
	private CertificatesManager certificatesManager;
	
	public CertificatesListAdminController(UserRequest ureq, WindowControl wControl) {
		super(ureq, wControl, "certificate_list");
		
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.name.i18n(), Cols.name.ordinal()));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("replace", translate("replace"), "replace"));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("delete", translate("delete"), "delete"));
		
		tableModel = new TemplatesDataModel(tableColumnModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "templates", tableModel, getTranslator(), formLayout);

		uploadLink = uifactory.addFormLink("upload", formLayout, Link.BUTTON);
		updateDataModel();
	}
	
	private void updateDataModel() {
		List<CertificateTemplate> templates = certificatesManager.getTemplates();
		tableModel.setObjects(templates);
		tableEl.reset();
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == uploadLink) {
			doUpload(ureq);
		} else if(source == tableEl) {
			SelectionEvent se = (SelectionEvent)event;
			String cmd = se.getCommand();
			CertificateTemplate selectedTemplate = tableModel.getObject(se.getIndex());
			if("replace".equals(cmd)) {
				doReplace(ureq, selectedTemplate);
			} else if("delete".equals(cmd)) {
				doConfirmDelete(ureq, selectedTemplate);
			}
		}
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(uploadCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateDataModel();
			}
			cmc.deactivate();
			cleanUp();
		} else if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isOkEvent(event)) {
				CertificateTemplate template = (CertificateTemplate)confirmDeleteCtrl.getUserObject();
				doDelete(template);
			}
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(uploadCtrl);
		removeAsListenerAndDispose(cmc);
		uploadCtrl = null;
		cmc = null;
	}
	private void doConfirmDelete(UserRequest ureq, CertificateTemplate selectedTemplate) {
		String title = translate("confirm.delete.title");
		String text = translate("confirm.delete.text");
		confirmDeleteCtrl = activateOkCancelDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(selectedTemplate);
	}
	
	private void doDelete(CertificateTemplate template) {
		certificatesManager.deleteTemplate(template);
		updateDataModel();
		showInfo("confirm.certificate.template.deleted", template.getName());
	}

	private void doUpload(UserRequest ureq) {
		removeAsListenerAndDispose(uploadCtrl);
		removeAsListenerAndDispose(cmc);
		
		uploadCtrl = new UploadCertificateTemplateController(ureq, getWindowControl());
		listenTo(uploadCtrl);
		
		String title = translate("upload.title");
		cmc = new CloseableModalController(getWindowControl(), "close", uploadCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doReplace(UserRequest ureq, CertificateTemplate template) {
		removeAsListenerAndDispose(uploadCtrl);
		removeAsListenerAndDispose(cmc);
		
		uploadCtrl = new UploadCertificateTemplateController(ureq, getWindowControl(), template);
		listenTo(uploadCtrl);
		
		String title = translate("upload.title");
		cmc = new CloseableModalController(getWindowControl(), "close", uploadCtrl.getInitialComponent(), true, title);
		listenTo(cmc);
		cmc.activate();
	}
	
	public static enum Cols {

		name("template.name");
		
		private final String i18n;
		
		private Cols(String i18n) {
			this.i18n = i18n;
		}
		
		public String i18n() {
			return i18n;
		}
	}
	
	private static class TemplatesDataModel extends DefaultFlexiTableDataModel<CertificateTemplate> {
		
		public TemplatesDataModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}

		@Override
		public Object getValueAt(int row, int col) {
			CertificateTemplate template = getObject(row);
			switch(Cols.values()[col]) {
				case name: return template.getName();
			}
			return null;
		}
	}
}
