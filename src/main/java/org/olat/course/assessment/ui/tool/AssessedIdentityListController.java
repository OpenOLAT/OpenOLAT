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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.commons.persistence.SortKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableDataModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnDef;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableDataModel;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentMainController;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.IdentityAssessmentEditController;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.07.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessedIdentityListController extends FormBasicController implements GenericEventListener {

	private FlexiTableElement tableEl;
	private AssessedUserTableModel usersTableModel;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final TooledStackedPanel stackPanel;

	private RepositoryEntry courseEntry;
	private final boolean isAdministrativeUser;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private AssessmentToolManager assessmentToolManager;

	
	public AssessedIdentityListController(UserRequest ureq, WindowControl wControl,
			TooledStackedPanel stackPanel, RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "identities");
		setTranslator(Util.createPackageTranslator(AssessmentMainController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));

		this.stackPanel = stackPanel;
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		updateModel();
		
		// Register for assessment changed events
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
				.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.username, "select"));
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select", false, null));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(UserCols.certificate, new DownloadCertificateCellRenderer()));

		usersTableModel = new AssessedUserTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", usersTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
	}
	
	private void updateModel() {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, null, null, assessmentCallback);

		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
		List<AssessedIdentityRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			rows.add(new AssessedIdentityRow(assessedIdentity, userPropertyHandlers, getLocale()));
		}
		usersTableModel.setObjects(rows);
		
		ConcurrentMap<Long, CertificateLight> certificates =  new ConcurrentHashMap<>();
		List<CertificateLight> certificateList = certificatesManager.getLastCertificates(courseEntry.getOlatResource());
		for(CertificateLight certificate:certificateList) {
			CertificateLight currentCertificate = certificates.get(certificate.getIdentityKey());
			if(currentCertificate == null || currentCertificate.getCreationDate().before(certificate.getCreationDate())) {
				certificates.put(certificate.getIdentityKey(), certificate);
			}
		}
		usersTableModel.setCertificates(certificates);
		tableEl.reloadData();
	}
	
	private void updateCertificate(Long certificateKey) {
		CertificateLight certificate = certificatesManager.getCertificateLightById(certificateKey);
		usersTableModel.putCertificate(certificate);
		tableEl.getComponent().setDirty(true);
	}
	
	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof CertificateEvent) {
			CertificateEvent ce = (CertificateEvent)event;
			if(courseEntry.getOlatResource().getKey().equals(ce.getResourceKey())) {
				updateCertificate(ce.getCertificateKey());
			}
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessedIdentityRow selectedRow = usersTableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					doSelectUser(ureq, selectedRow);
				}
			}
		} else if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("download-cert".equals(link.getCmd())) {
				
			}
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doSelectUser(UserRequest ureq, AssessedIdentityRow row) {
		ICourse course = CourseFactory.loadCourse(courseEntry.getOlatResource());
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		IdentityAssessmentEditController userController = new IdentityAssessmentEditController(getWindowControl(), ureq,
				stackPanel, assessedIdentity, course, true, false, true);
		listenTo(userController);
		String fullname = userManager.getUserDisplayName(assessedIdentity);
		stackPanel.pushController(fullname, userController);
	}

	public static class AssessedUserTableModel extends DefaultFlexiTableDataModel<AssessedIdentityRow> implements SortableFlexiTableDataModel<AssessedIdentityRow> {

		private ConcurrentMap<Long, CertificateLight> certificates;

		public AssessedUserTableModel(FlexiTableColumnModel columnModel) {
			super(columnModel);
		}
		
		public void setCertificates(ConcurrentMap<Long, CertificateLight> certificates) {
			this.certificates = certificates;
		}
		
		public void putCertificate(CertificateLight certificate) {
			if(certificates != null) {
				certificates.put(certificate.getIdentityKey(), certificate);
			}
		}
		
		@Override
		public void sort(SortKey sortKey) {
			//
		}
		
		@Override
		public Object getValueAt(int row, int col) {
			AssessedIdentityRow identityRow = getObject(row);
			return getValueAt(identityRow, col);
		}

		@Override
		public Object getValueAt(AssessedIdentityRow row, int col) {
			if(col >= 0 && col < UserCols.values().length) {
				switch(UserCols.values()[col]) {
					case username: return row.getIdentityName();
					case certificate: return certificates.get(row.getIdentityKey());
				}
			}
			int propPos = col - AssessmentToolConstants.USER_PROPS_OFFSET;
			return row.getIdentityProp(propPos);
		}

		@Override
		public DefaultFlexiTableDataModel<AssessedIdentityRow> createCopyWithEmptyList() {
			return new AssessedUserTableModel(getTableColumnModel());
		}
	}

	public enum UserCols implements FlexiColumnDef {
		
		username("table.header.name"),
		certificate("table.header.certificate");
		
		private final String i18nKey;
		
		private UserCols(String i18nKey) {
			this.i18nKey = i18nKey;
		}
		
		@Override
		public String i18nHeaderKey() {
			return i18nKey;
		}
	}
}
