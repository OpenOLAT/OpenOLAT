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
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessedIdentitiesTableDataModel;
import org.olat.course.assessment.AssessmentMainController;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.tool.AssessmentIdentitiesCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.assessment.ui.tool.AssessmentIdentitiesCourseTableModel.IdentityCourseCols;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentIdentitiesCourseController extends FormBasicController {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = AssessedIdentitiesTableDataModel.usageIdentifyer;
	
	private final RepositoryEntry courseEntry;
	private final boolean isAdministrativeUser;
	private List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentToolSecurityCallback assessmentCallback;
	
	private FlexiTableElement tableEl;
	private AssessmentIdentitiesCourseTableModel usersTableModel;

	@Autowired
	private UserManager userManager;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public AssessmentIdentitiesCourseController(UserRequest ureq, WindowControl wControl, RepositoryEntry courseEntry,
			AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "identity_course");
		setTranslator(Util.createPackageTranslator(AssessmentMainController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);

		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.username, "select"));
		}
		
		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select", false, null));
		}
	
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseCols.passed, new PassedCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseCols.certificate, new DownloadCertificateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseCols.score));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseCols.lastScoreUpdate));
		
		usersTableModel = new AssessmentIdentitiesCourseTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", usersTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
	}
	
	public List<EfficiencyStatementEntry> loadModel() {
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, null, null, assessmentCallback);
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
		List<EfficiencyStatementEntry> entries = coachingService.getCourse(getIdentity(), courseEntry);
		Map<Long,EfficiencyStatementEntry> identityKeyToStatementMap = entries.stream()
				.collect(Collectors.toMap(EfficiencyStatementEntry::getStudentKey, Function.identity()));

		List<AssessedIdentityCourseRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity: assessedIdentities) {
			EfficiencyStatementEntry statement = identityKeyToStatementMap.get(assessedIdentity.getKey());
			rows.add(new AssessedIdentityCourseRow(assessedIdentity, statement, userPropertyHandlers, getLocale()));
		}

		List<CertificateLight> certificates = certificatesManager.getLastCertificates(courseEntry.getOlatResource());
		ConcurrentMap<Long, CertificateLight> certificateMap = new ConcurrentHashMap<>();
		for(CertificateLight certificate:certificates) {
			certificateMap.put(certificate.getIdentityKey(), certificate);
		}
		
		usersTableModel.setCertificateMap(certificateMap);
		usersTableModel.setObjects(rows);
		tableEl.reloadData();
		return entries;
	}
	
	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}


	
	

}
