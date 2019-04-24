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

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.StepFormBasicController;
import org.olat.core.gui.control.generic.wizard.StepsEvent;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessedIdentitiesTableDataModel;
import org.olat.course.assessment.AssessedIdentityWrapper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificatesSelectionController extends StepFormBasicController {
	
	private FlexiTableElement tableEl;
	private CertificatesSelectionDataModel tableModel;
	
	private final boolean hasAssessableNodes;
	private final RepositoryEntry courseEntry;
	private final boolean isAdministrativeUser;
	private final List<AssessedIdentityWrapper> datas;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	
	public CertificatesSelectionController(UserRequest ureq, WindowControl wControl,
			Form rootForm, StepsRunContext runContext, RepositoryEntry courseEntry,
			List<AssessedIdentityWrapper> datas, boolean hasAssessableNodes) {
		super(ureq, wControl, rootForm, runContext, LAYOUT_BAREBONE, null);
		setTranslator(Util.createPackageTranslator(UserPropertyHandler.class, getLocale(), getTranslator()));
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		
		this.datas = datas;
		this.courseEntry = courseEntry;
		this.hasAssessableNodes = hasAssessableNodes;
		Roles roles = ureq.getUserSession().getRoles();
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(roles);
		
		initForm(ureq);
	}
	
	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();;
		int colPos = 0;
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.name", CertificatesSelectionDataModel.USERNAME_COL));
		}
		
		List<UserPropertyHandler> userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessedIdentitiesTableDataModel.usageIdentifyer, isAdministrativeUser);
		List<UserPropertyHandler> resultingPropertyHandlers = new ArrayList<>();
		// followed by the users fields
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(AssessedIdentitiesTableDataModel.usageIdentifyer , userPropertyHandler);
			resultingPropertyHandlers.add(userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colPos++, false, null));
		}
		
		if(hasAssessableNodes) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.passed", CertificatesSelectionDataModel.PASSED_COL, new PassedCellRenderer()));
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.score", CertificatesSelectionDataModel.SCORE_COL));
		}
		
		tableModel = new CertificatesSelectionDataModel(columnsModel, resultingPropertyHandlers);

		Set<Integer> preselectedRows = new HashSet<>();
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		List<CertificateInfos> infos = new ArrayList<>(datas.size());
		
		int count = 0;
		for(AssessedIdentityWrapper data:datas) {
			ScoreEvaluation scoreEval = data.getUserCourseEnvironment().getScoreAccounting().getScoreEvaluation(rootNode);
			Float score = scoreEval == null ? null : scoreEval.getScore();
			Boolean passed = scoreEval == null ? null : scoreEval.getPassed();
			Identity assessedIdentity = data.getIdentity();
			infos.add(new CertificateInfos(assessedIdentity, score, passed));
			if(passed != null && passed.booleanValue()) {
				preselectedRows.add(new Integer(count));
			}
			count++;
		}
		tableModel.setObjects(infos);

		tableEl = uifactory.addTableElement(getWindowControl(), "selection", tableModel, getTranslator(), formLayout);
		tableEl.setMultiSelect(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelectedIndex(preselectedRows);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void formOK(UserRequest ureq) {
		if(tableEl.getMultiSelectedIndex().size() > 0) {
			Set<Integer> selectedRows = tableEl.getMultiSelectedIndex();
			List<CertificateInfos> selectedInfos = new ArrayList<>(selectedRows.size());
			for(Integer selectedRow:selectedRows) {
				selectedInfos.add(tableModel.getObject(selectedRow.intValue()));
			}
			addToRunContext("infos", selectedInfos);
			fireEvent(ureq, StepsEvent.ACTIVATE_NEXT);
		}
	}
}
