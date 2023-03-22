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
package org.olat.course.nodes.st;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.creator.ControllerCreator;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.winmgr.Command;
import org.olat.core.gui.control.winmgr.CommandFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.util.FileUtils;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.ui.reset.ConfirmResetDataController;
import org.olat.course.assessment.ui.reset.ResetDataContext;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetCourse;
import org.olat.course.assessment.ui.reset.ResetDataContext.ResetParticipants;
import org.olat.course.assessment.ui.tool.AssessmentIdentiesPrintController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeController;
import org.olat.course.assessment.ui.tool.IdentityListCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ResetCourseDataHelper;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.ims.qti21.resultexport.IdentitiesList;
import org.olat.modules.assessment.Role;
import org.olat.modules.assessment.ui.AssessedIdentityElementRow;
import org.olat.modules.assessment.ui.AssessmentToolContainer;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * This specialized list of assessed identities show the certificates
 * and recertification date if configured.
 * 
 * Initial date: 18 déc. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class STIdentityListCourseNodeController extends IdentityListCourseNodeController {
	
	private FormLink pdfButton;
	private FormLink resetDataAllButton;
	private FormLink resetDataBulkButton;
	
	private ConfirmResetDataController resetDataCtrl;

	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private UserCourseInformationsManager userInfosMgr;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	
	public STIdentityListCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, CourseNode courseNode, UserCourseEnvironment coachCourseEnv,
			AssessmentToolContainer toolContainer, AssessmentToolSecurityCallback assessmentCallback, boolean showTitle) {
		super(ureq, wControl, stackPanel, courseEntry, courseNode, coachCourseEnv, toolContainer, assessmentCallback, showTitle);
	}

	@Override
	protected boolean isSelectable() {
		return courseNode.getParent() == null;
	}

	@Override
	protected void initModificationDatesColumns(FlexiTableColumnModel columnsModel) {
		if(courseNode.getParent() == null) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.initialLaunchDate, getSelectAction()));
		}
		super.initModificationDatesColumns(columnsModel);
	}

	@Override
	protected void initCalloutColumns(FlexiTableColumnModel columnsModel) {
		ICourse course = CourseFactory.loadCourse(getCourseRepositoryEntry());
		if(course.getCourseConfig().isCertificateEnabled()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.certificate, new DownloadCertificateCellRenderer(getLocale())));
			if(course.getCourseConfig().isRecertificationEnabled()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.recertification, new DateFlexiCellRenderer(getLocale())));
			}
		}
		//no callout
	}
	
	@Override
	protected String getTableId() {
		return "st-assessment-tool-identity-list-v2";
	}

	@Override
	protected void initMultiSelectionTools(UserRequest ureq, FormLayoutContainer formLayout) {
		if(courseNode.getParent() == null && pdfModule.isEnabled()) {
			pdfButton = uifactory.addFormLink("bulk.pdf", formLayout, Link.BUTTON); 
			pdfButton.setIconLeftCSS("o_icon o_icon_tool_pdf");
			tableEl.addBatchButton(pdfButton);
		} else {
			tableEl.setMultiSelect(false);
			tableEl.setSelectAllEnable(false);
		}
		
		if(courseNode.getParent() == null && getAssessmentCallback().canResetData()) {
			resetDataAllButton = uifactory.addFormLink("reset.data.button", formLayout, Link.BUTTON);
			resetDataAllButton.setIconLeftCSS("o_icon o_icon-fw o_icon_reset_data");
			
			resetDataBulkButton = uifactory.addFormLink("reset.data.bulk", "reset.data.button", null, formLayout, Link.BUTTON);
			resetDataBulkButton.setIconLeftCSS("o_icon o_icon-fw o_icon_reset_data");
			tableEl.addBatchButton(resetDataBulkButton);
		}
	}

	@Override
	public void reload(UserRequest ureq) {
		Map<Long, Date> initialLaunchDates = userInfosMgr
				.getInitialLaunchDates(getCourseRepositoryEntry().getOlatResource());
		super.reload(ureq);
		
		List<AssessedIdentityElementRow> rows = usersTableModel.getObjects();
		for(AssessedIdentityElementRow row:rows) {
			Date initialLaunchDate = initialLaunchDates.get(row.getIdentityKey());
			row.setInitialCourseLaunchDate(initialLaunchDate);
		}

		AssessmentToolContainer toolContainer = getToolContainer();
		if(toolContainer.getCertificateMap() == null) {
			List<CertificateLight> certificates = certificatesManager.getLastCertificates(getCourseRepositoryEntry().getOlatResource());
			ConcurrentMap<Long, CertificateLight> certificateMap = new ConcurrentHashMap<>();
			for(CertificateLight certificate:certificates) {
				certificateMap.put(certificate.getIdentityKey(), certificate);
			}
			toolContainer.setCertificateMap(certificateMap);
		}
		usersTableModel.setCertificateMap(toolContainer.getCertificateMap());
	}
	
	@Override
	public void event(UserRequest ureq, Controller source, Event event) {
		if(resetDataCtrl == source) {
			if(event == Event.DONE_EVENT) {
				doResetData(ureq, resetDataCtrl.getDataContext()); 
			}
			cmc.deactivate();
			cleanUp();
		}
		super.event(ureq, source, event);
	}
	
	@Override
	protected void cleanUp() {
		removeAsListenerAndDispose(resetDataCtrl);
		resetDataCtrl = null;
		super.cleanUp();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(pdfButton == source) {
			doExportPdf(ureq);
		} else if(resetDataAllButton == source) {
			doConfirmResetDataAll(ureq);
		} else if(resetDataBulkButton == source) {
			doConfirmResetDataSelectedIdentities(ureq);
		} else {
			super.formInnerEvent(ureq, source, event);
		}
	}
	
	private void doConfirmResetDataSelectedIdentities(UserRequest ureq) {
		List<Identity> selectedIdentities = getSelectedIdentities(row -> true);
		if(selectedIdentities.isEmpty()) {
			showWarning("");
		} else {
			doConfirmResetData(ureq, selectedIdentities, ResetParticipants.selected);
		}
	}
	
	private void doConfirmResetDataAll(UserRequest ureq) {
		IdentitiesList identities = getIdentities(true);
		doConfirmResetData(ureq, identities.getIdentities(), ResetParticipants.all);
	}
	
	private void doConfirmResetData(UserRequest ureq, List<Identity> identities, ResetParticipants resetParticipants) {
		ResetDataContext dataContext = new ResetDataContext(getCourseRepositoryEntry());
		if(courseNode.getParent() == null) {
			dataContext.setResetCourse(ResetCourse.all);
		} else {
			dataContext.setResetCourse(ResetCourse.elements);
			dataContext.setCourseNodes(List.of(courseNode));
		}
		dataContext.setResetParticipants(resetParticipants);
		dataContext.setSelectedParticipants(identities);
		resetDataCtrl = new ConfirmResetDataController(ureq, getWindowControl(), dataContext, null);
		listenTo(resetDataCtrl);
		
		String title = translate("reset.data.title");
		cmc = new CloseableModalController(getWindowControl(), null, resetDataCtrl.getInitialComponent(), true, title, true);
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doResetData(UserRequest ureq, ResetDataContext dataContext) {
		List<Identity> identities;
		if(dataContext.getResetParticipants() == ResetParticipants.all) {
			identities = getIdentities(true).getIdentities();
		} else {
			identities = dataContext.getSelectedParticipants();
		}
		
		ResetCourseDataHelper resetCourseNodeHelper = new ResetCourseDataHelper(getCourseEnvironment());
		MediaResource archiveResource = null;
		if(dataContext.getResetCourse() == ResetCourse.all) {
			archiveResource = resetCourseNodeHelper.resetCourse(identities, getIdentity(), Role.coach);
		} else if(!dataContext.getCourseNodes().isEmpty()) {
			archiveResource = resetCourseNodeHelper.resetCourseNodes(identities, dataContext.getCourseNodes(), false, getIdentity(), Role.coach);
		}
		if(archiveResource != null) {
			Command downloadCmd = CommandFactory.createDownloadMediaResource(ureq, archiveResource);
			getWindowControl().getWindowBackOffice().sendCommandTo(downloadCmd);
		}
	}

	private void doExportPdf(UserRequest ureq) {
		List<Long> assessesIdentityKeys = getMultiSelectionIdentityKeys();
		if (assessesIdentityKeys.isEmpty()) {
			showWarning("bulk.no.selection");
			return;
		}
		ControllerCreator printControllerCreator = (lureq, lwControl) -> new AssessmentIdentiesPrintController(lureq, lwControl, courseEntry, coachCourseEnv,
				assessesIdentityKeys, getAssessmentCallback());
		String title = getPdfTitle();
		MediaResource resource = pdfService.convert(title, getIdentity(), printControllerCreator, getWindowControl());
		ureq.getDispatchResult().setResultingMediaResource(resource);
	}

	private List<Long> getMultiSelectionIdentityKeys() {
		Set<Integer> selections = tableEl.getMultiSelectedIndex();
		List<Long> identityKeys = new ArrayList<>(selections.size());
		for(Integer i:selections) {
			AssessedIdentityElementRow row = usersTableModel.getObject(i.intValue());
			if (row != null) {
				identityKeys.add(row.getIdentityKey());
			}
		}
		return identityKeys;
	}
	
	private String getPdfTitle() {
		StringBuilder sb = new StringBuilder();
		sb.append(translate("bulk.pdf.prefix"));
		sb.append("_");
		sb.append(courseEntry.getDisplayname());
		return FileUtils.normalizeFilename(sb.toString());
	}
}
