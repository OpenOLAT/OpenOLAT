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

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.wizard.Step;
import org.olat.core.gui.control.generic.wizard.StepRunnerCallback;
import org.olat.core.gui.control.generic.wizard.StepsMainRunController;
import org.olat.core.gui.control.generic.wizard.StepsRunContext;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessedIdentityWrapper;
import org.olat.course.assessment.AssessmentMainController;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.tool.AssessmentIdentitiesCourseTableModel.IdentityCourseCols;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.certificate.ui.Certificates_1_SelectionStep;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.course.config.CourseConfig;
import org.olat.group.BusinessGroup;
import org.olat.modules.assessment.model.AssessmentEntryStatus;
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
	
	private final RepositoryEntry courseEntry;
	private final boolean isAdministrativeUser;
	private List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentToolSecurityCallback assessmentCallback;

	private Link nextLink, previousLink;
	private FormLink generateCertificateButton;
	private FlexiTableElement tableEl;
	private TooledStackedPanel stackPanel;
	private AssessmentIdentitiesCourseTableModel usersTableModel;
	
	private StepsMainRunController wizardCtrl;
	private AssessmentIdentityCourseController currentIdentityCtrl;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public AssessmentIdentitiesCourseController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "identity_course");
		setTranslator(Util.createPackageTranslator(AssessmentMainController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.stackPanel = stackPanel;
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);

		initForm(ureq);
		loadModel(null, null, null);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseCols.username, "select"));
		}
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex, "select",
					true, "userProp-" + colIndex));
			colIndex++;
		}
	
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseCols.passed, new PassedCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseCols.score));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseCols.lastScoreUpdate));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseCols.certificate, new DownloadCertificateCellRenderer()));
		
		usersTableModel = new AssessmentIdentitiesCourseTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", usersTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSearchEnabled(new AssessedIdentityListProvider(getIdentity(), courseEntry, null, null, assessmentCallback), ureq.getUserSession());
		
		List<FlexiTableFilter> filters = new ArrayList<>();
		filters.add(new FlexiTableFilter(translate("filter.passed"), "passed"));
		filters.add(new FlexiTableFilter(translate("filter.failed"), "failed"));
		filters.add(new FlexiTableFilter(translate("filter.inProgress"), "inProgress"));
		filters.add(new FlexiTableFilter(translate("filter.inReview"), "inReview"));
		filters.add(new FlexiTableFilter(translate("filter.done"), "done"));
		tableEl.setFilters("", filters);

		ICourse course = CourseFactory.loadCourse(courseEntry);
		if(assessmentCallback.canAssessBusinessGoupMembers()) {
			List<BusinessGroup> coachedGroups = null;
			if(assessmentCallback.isAdmin()) {
				coachedGroups = course.getCourseEnvironment().getCourseGroupManager().getAllBusinessGroups();
			} else {
				coachedGroups = assessmentCallback.getCoachedGroups(); 
			}

			if(coachedGroups.size() > 0) {
				List<FlexiTableFilter> groupFilters = new ArrayList<>();
				for(BusinessGroup coachedGroup:coachedGroups) {
					groupFilters.add(new FlexiTableFilter(coachedGroup.getName(), coachedGroup.getKey().toString(), "o_icon o_icon_group"));
				}
				
				tableEl.setExtendedFilterButton(translate("filter.groups"), groupFilters);
			}
		}
		
		CourseConfig courseConfig = course.getCourseConfig();
		if(courseConfig.isManualCertificationEnabled()) {
			generateCertificateButton = uifactory.addFormLink("generate.certificate", formLayout, Link.BUTTON);
		}
	}
	
	public List<EfficiencyStatementEntry> loadModel(String searchStr, List<FlexiTableFilter> filters, List<FlexiTableFilter> extendedFilters) {

		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, null, null, assessmentCallback);
		
		List<AssessmentEntryStatus> assessmentStatus = null;
		if(filters != null && filters.size() > 0) {
			assessmentStatus = new ArrayList<>(filters.size());
			for(FlexiTableFilter filter:filters) {
				if("passed".equals(filter.getFilter())) {
					params.setPassed(true);
				} else if("failed".equals(filter.getFilter())) {
					params.setFailed(true);
				} else if(AssessmentEntryStatus.isValueOf(filter.getFilter())){
					assessmentStatus.add(AssessmentEntryStatus.valueOf(filter.getFilter()));
				}
			}
		}
		params.setAssessmentStatus(assessmentStatus);
		
		List<Long> businessGroupKeys = null;
		if(extendedFilters != null && extendedFilters.size() > 0) {
			businessGroupKeys = new ArrayList<>(extendedFilters.size());
			for(FlexiTableFilter extendedFilter:extendedFilters) {
				if(StringHelper.isLong(extendedFilter.getFilter())) {
					businessGroupKeys.add(Long.parseLong(extendedFilter.getFilter()));
				}
			}
		}
		params.setBusinessGroupKeys(businessGroupKeys);
		params.setSearchString(searchStr);
		
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
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(previousLink == source) {
			doPrevious(ureq);
		} else if(nextLink == source) {
			doNext(ureq);
		}
		super.event(ureq, source, event);
	}
	
	

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(wizardCtrl == source) {
			if(event == Event.CANCELLED_EVENT || event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				getWindowControl().pop();
				removeAsListenerAndDispose(wizardCtrl);
				wizardCtrl = null;
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessedIdentityCourseRow row = usersTableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					doSelect(ureq, row);
				}
			} else if(event instanceof FlexiTableSearchEvent) {
				FlexiTableSearchEvent ftse = (FlexiTableSearchEvent)event;
				loadModel(ftse.getSearch(), ftse.getFilters(), ftse.getExtendedFilters());
			}
		} else if(generateCertificateButton == source) {
			doGenerateCertificates(ureq);
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doGenerateCertificates(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(courseEntry);

		List<AssessedIdentityWrapper> datas = new ArrayList<>();
		Certificates_1_SelectionStep start = new Certificates_1_SelectionStep(ureq, courseEntry, datas, course.hasAssessableNodes());
		StepRunnerCallback finish = new StepRunnerCallback() {
			@Override
			public Step execute(UserRequest uureq, WindowControl wControl, StepsRunContext runContext) {
				@SuppressWarnings("unchecked")
				List<CertificateInfos> assessedIdentitiesInfos = (List<CertificateInfos>)runContext.get("infos");
				if(assessedIdentitiesInfos != null && assessedIdentitiesInfos.size() > 0) {
					doGenerateCertificates(assessedIdentitiesInfos);
					return StepsMainRunController.DONE_MODIFIED;
				}
				return StepsMainRunController.DONE_UNCHANGED;
			}
		};
		
		wizardCtrl = new StepsMainRunController(ureq, getWindowControl(), start, finish, null,
				translate("certificates.wizard.title"), "o_sel_certificates_wizard");
		listenTo(wizardCtrl);
		getWindowControl().pushAsModalDialog(wizardCtrl.getInitialComponent());
	}
	
	private void doGenerateCertificates(List<CertificateInfos> assessedIdentitiesInfos) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		Long templateKey = course.getCourseConfig().getCertificateTemplate();
		CertificateTemplate template = null;
		if(templateKey != null) {
			template = certificatesManager.getTemplateById(templateKey);
		}
		
		certificatesManager.generateCertificates(assessedIdentitiesInfos, courseEntry, template, true);
	}
	
	private void doNext(UserRequest ureq) {
		stackPanel.popController(currentIdentityCtrl);
		
		Identity currentIdentity = currentIdentityCtrl.getAssessedIdentity();
		int index = getIndexOf(currentIdentity);
		if(index >= 0) {
			int nextIndex = index + 1;//next
			if(nextIndex >= 0 && nextIndex < usersTableModel.getRowCount()) {
				doSelect(ureq, usersTableModel.getObject(nextIndex));
			} else if(usersTableModel.getRowCount() > 0) {
				doSelect(ureq, usersTableModel.getObject(0));
			}
		}
	}
	
	private void doPrevious(UserRequest ureq) {
		stackPanel.popController(currentIdentityCtrl);
		
		Identity currentIdentity = currentIdentityCtrl.getAssessedIdentity();
		int index = getIndexOf(currentIdentity);
		if(index >= 0) {
			int previousIndex = index - 1;//next
			if(previousIndex >= 0 && previousIndex < usersTableModel.getRowCount()) {
				doSelect(ureq, usersTableModel.getObject(previousIndex));
			} else if(usersTableModel.getRowCount() > 0) {
				doSelect(ureq, usersTableModel.getObject(usersTableModel.getRowCount() - 1));
			}
		}
	}
	
	private int getIndexOf(Identity identity) {
		int index = -1;
		for(int i=usersTableModel.getRowCount(); i-->0; ) {
			Long rowIdentityKey = usersTableModel.getObject(i).getIdentityKey();
			if(rowIdentityKey.equals(identity.getKey())) {
				return i;
			}
		}
		return index;
	}

	private void doSelect(UserRequest ureq, AssessedIdentityCourseRow row) {
		removeAsListenerAndDispose(currentIdentityCtrl);
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		String fullName = userManager.getUserDisplayName(assessedIdentity);
		
		currentIdentityCtrl = new AssessmentIdentityCourseController(ureq, getWindowControl(), stackPanel, courseEntry, assessedIdentity);
		listenTo(currentIdentityCtrl);
		stackPanel.pushController(fullName, currentIdentityCtrl);
		
		previousLink = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
		previousLink.setTitle(translate("command.previous"));
		stackPanel.addTool(previousLink, Align.rightEdge, false, "o_tool_previous");
		nextLink = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
		nextLink.setTitle(translate("command.next"));
		stackPanel.addTool(nextLink, Align.rightEdge, false, "o_tool_next");
	}
}
