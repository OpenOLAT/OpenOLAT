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
package org.olat.modules.coach.ui.em;

import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ExportableFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateIdentityConfig;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * Initial date: 2024-12-23<br>
 *
 * @author cpfranger, christoph.pfranger@frentix.com, <a href="https://www.frentix.com">https://www.frentix.com</a>
 */
public class CertificatesController extends FormBasicController implements Activateable2, ExportableFlexiTableDataModel {

	public static final int USER_PROPS_OFFSET = 100;

	private final List<UserPropertyHandler> userPropertyHandlers;
	private final String propsIdentifier;

	private FlexiTableElement tableEl;
	private CertificatesTableModel tableModel;

	@Autowired
	private UserManager userManager;
	@Autowired
	private CertificatesManager certificatesManager;

	public CertificatesController(UserRequest ureq, WindowControl wControl, 
								  List<UserPropertyHandler> userPropertyHandlers, String propsIdentifier) {
		super(ureq, wControl, LAYOUT_BAREBONE);
		this.userPropertyHandlers = userPropertyHandlers;
		this.propsIdentifier = propsIdentifier;

		initForm(ureq);
		loadData();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, CertificatesTableModel.CertificateCols.id));
		
		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler = userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(propsIdentifier, userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, 
					userPropertyHandler.i18nColumnDescriptorLabelKey(),	colIndex++, "select", true,
					userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificatesTableModel.CertificateCols.path));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificatesTableModel.CertificateCols.course));
		
		tableModel = new CertificatesTableModel(columnsModel, this);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, 
				getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
	}

	private void loadData() {
		List<CertificateRow> certificates = certificatesManager
				.getCertificatesForOrganizations(getIdentity(), userPropertyHandlers, null, null)
				.stream()
				.map(CertificateRow::new).toList();
		tableModel.setObjects(certificates);

	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		reload();
	}

	public void reload() {
		loadData();
		tableEl.reloadData();
	}


	@Override
	public MediaResource export(FlexiTableComponent ftC) {
		List<CertificateIdentityConfig> certificates = certificatesManager
				.getCertificatesForOrganizations(getIdentity(), userPropertyHandlers, null, null);
		return new CertificatesExport(certificates, userPropertyHandlers, getTranslator());
	}
}
