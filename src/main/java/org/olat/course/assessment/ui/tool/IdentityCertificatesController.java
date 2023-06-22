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
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.export.ui.ExportsListDataModel.ExportsCols;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.Roles;
import org.olat.core.util.DateUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.ui.tool.component.IdentityCertificateRowComparator;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateManagedFlag;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.certificate.ui.CertificateMediaResource;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a copy, perhaps only maintains the orginal: AssessedIdentityCertificatesController
 * with different arguments for the constructor.
 * 
 * Initial date: 21.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentityCertificatesController extends FormBasicController implements GenericEventListener, FlexiTableComponentDelegate {
	
	private FormLink generateLink;
	private FlexiTableElement tableEl;
	private IdentityCertificatesTableModel tableModel;

	private DialogBoxController confirmDeleteCtrl;
	private DialogBoxController confirmCertificateCtrl;
	
	private final Identity assessedIdentity;
	private final RepositoryEntry courseEntry;
	private final RepositoryEntryCertificateConfiguration certificateConfig;
	
	private final boolean canDelete;
	private final boolean canGenerate;
	private final Formatter formatter;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public IdentityCertificatesController(UserRequest ureq, WindowControl wControl,
			UserCourseEnvironment coachCourseEnv, RepositoryEntry courseEntry,
			RepositoryEntryCertificateConfiguration certificateConfig, Identity assessedIdentity) {
		super(ureq, wControl, "certificate_overview");
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));

		this.courseEntry = courseEntry;
		this.assessedIdentity = assessedIdentity;
		this.certificateConfig = certificateConfig;
		
		canDelete = canGenerate = certificateConfig.isManualCertificationEnabled() && !coachCourseEnv.isCourseReadOnly();
		formatter = Formatter.getInstance(getLocale());

		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
		initForm(ureq);
		loadModel();
	}
	
	public int getNumOfCertificates() {
		return tableModel.getRowCount();
	}
	
	public Certificate getLastCertificate() {
		if(tableModel.getRowCount() > 0) {
			return tableModel.getObject(0).getCertificate();
		}
		return null;
	}
	
	@Override
	protected void doDispose() {
		coordinatorManager.getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
        super.doDispose();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer layoutCont) {
			if(certificateConfig.isRecertificationEnabled() && certificateConfig.isRecertificationLeadTimeEnabled()) {
				int recertificationLeadTime = certificateConfig.getRecertificationLeadTimeInDays();
				layoutCont.contextPut("leadTimeMsg", translate("info.leadtime.days",
						Integer.toString(recertificationLeadTime)));		
			}
		}
		
		if(canGenerate) {
			generateLink = uifactory.addFormLink("generate.certificate", formLayout, Link.BUTTON);
			generateLink.setElementCssClass("o_sel_certificate_generate");
			generateLink.setIconLeftCSS("o_icon o_icon_add");
		}
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.title));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ExportsCols.creationDate));
		
		tableModel = new IdentityCertificatesTableModel(columnsModel, getLocale());
		tableEl = uifactory.addTableElement(getWindowControl(), "certificates", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setCustomizeColumns(false);
		
		VelocityContainer row = createVelocityContainer("certificate_row_1");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		IdentityCertificateRow certificateRow = tableModel.getObject(row);
		List<Component> components = new ArrayList<>(3);
		if(certificateRow.getDownloadLink() != null) {
			components.add(certificateRow.getDownloadLink().getComponent());
		}
		if(certificateRow.getDeleteLink() != null) {
			components.add(certificateRow.getDeleteLink().getComponent());
		}
		return components;
	}

	protected void loadModel() {
		List<Certificate> certificates = certificatesManager.getCertificates(assessedIdentity, courseEntry.getOlatResource());
		List<IdentityCertificateRow> rows = new ArrayList<>(certificates.size());
		int count = 0;
		final Date now = new Date();
		for(Certificate certificate:certificates) {
			String url = DownloadCertificateCellRenderer.getUrl(certificate);
			String filename = DownloadCertificateCellRenderer.getName(certificate);
			
			Long expiredInDays = null;
			Date validity = certificate.getNextRecertificationDate();
			if(validity != null && validity.before(now)) {
				expiredInDays = DateUtils.countDays(validity, now);
			}
			
			IdentityCertificateRow row = new IdentityCertificateRow(certificate, filename, url, expiredInDays);
			rows.add(row);
			
			if(canDelete && !CertificateManagedFlag.isManaged(certificate, CertificateManagedFlag.delete)) {
				FormLink deleteLink = uifactory.addFormLink("delete." + (++count), "delete", "", null, flc, Link.LINK | Link.NONTRANSLATED);
				deleteLink.setElementCssClass("o_sel_certificate_delete");
				deleteLink.setIconLeftCSS("o_icon o_icon-lg o_icon_delete_item");
				deleteLink.setTooltip(translate("delete"));
				deleteLink.setUserObject(row);
				row.setDeleteLink(deleteLink);
			}

			FormLink downloadLink = uifactory.addFormLink("download." + (++count), "download", "", null, flc, Link.LINK | Link.NONTRANSLATED);
			downloadLink.setElementCssClass("o_sel_certificate_download");
			downloadLink.setIconLeftCSS("o_icon o_icon-lg o_icon_download");
			downloadLink.setTooltip(translate("download.certificate"));
			downloadLink.setNewWindow(true, false, false);
			downloadLink.setUserObject(row);
			row.setDownloadLink(downloadLink);
		}
		
		if(rows.size() > 1) {
			Collections.sort(rows, new IdentityCertificateRowComparator());
		}
		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
		flc.contextPut("rowCount", Integer.valueOf(tableModel.getRowCount()));
	}
	
	@Override
	public void event(Event event) {
		if(event instanceof CertificateEvent ce
				&& ce.getOwnerKey().equals(assessedIdentity.getKey())
				&& courseEntry.getOlatResource().getKey().equals(ce.getResourceKey())) {
			loadModel();
		}
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(generateLink == source) {
			doConfirmGenerateCertificate(ureq) ;
		} else if(source instanceof FormLink link && link.getUserObject() instanceof IdentityCertificateRow certificate) {
			String cmd = link.getCmd();
			if("delete".equals(cmd)) {
				doConfirmDelete(ureq, certificate.getCertificate());
			} else if("download".equals(cmd)) {
				doDownload(ureq, certificate.getCertificate());
			}
		}
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(confirmCertificateCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)) {
				doGenerateCertificate(ureq);
			}
		} else if(confirmDeleteCtrl == source) {
			if(DialogBoxUIFactory.isYesEvent(event)
					&& confirmDeleteCtrl.getUserObject() instanceof Certificate certificate) {
				doDelete(ureq, certificate);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void doConfirmDelete(UserRequest ureq, Certificate certificate) {
		String title = translate("confirm.delete.certificate.title");
		String text = translate("confirm.delete.certificate.text");
		confirmDeleteCtrl = activateYesNoDialog(ureq, title, text, confirmDeleteCtrl);
		confirmDeleteCtrl.setUserObject(certificate);
	}
	
	private void doDelete(UserRequest ureq, Certificate certificate) {
		certificatesManager.deleteCertificate(certificate);
		loadModel();
		String displayName = formatter.formatDateAndTime(certificate.getCreationDate());
		showInfo("confirm.certificate.deleted", displayName);
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
	
	private void doDownload(UserRequest ureq, Certificate certificate) {
		VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificate);
		String name = DownloadCertificateCellRenderer.getName(certificate);
		MediaResource certificateResource = new CertificateMediaResource(name, certificateLeaf);
		ureq.getDispatchResult().setResultingMediaResource(certificateResource);
	}

	private void doConfirmGenerateCertificate(UserRequest ureq) {
		if(certificatesManager.isCertificationAllowed(assessedIdentity, courseEntry)) {
			//don't need to confirm
			doGenerateCertificate(ureq);
		} else {
			String title = translate("confirm.certificate.title");
			String fullName = userManager.getUserDisplayName(assessedIdentity);
			String text = translate("confirm.certificate.description", fullName);
			confirmCertificateCtrl = activateYesNoDialog(ureq, title, text, confirmCertificateCtrl);
		}
	}
	
	private void doGenerateCertificate(UserRequest ureq) {
		ICourse course = CourseFactory.loadCourse(courseEntry);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		Roles roles = securityManager.getRoles(assessedIdentity);
		IdentityEnvironment identityEnv = new IdentityEnvironment(assessedIdentity, roles);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
		ScoreAccounting scoreAccounting = assessedUserCourseEnv.getScoreAccounting();
		scoreAccounting.evaluateAll();
		AssessmentEvaluation assessmentEval = scoreAccounting.evalCourseNode(rootNode);

		Float score = assessmentEval == null ? null : assessmentEval.getScore();
		Boolean passed = assessmentEval == null ? null : assessmentEval.getPassed();
		Double completion = assessmentEval == null ? null : assessmentEval.getCompletion();
		Float maxScore = assessmentEval == null ? null : assessmentEval.getMaxScore();
		CertificateInfos certificateInfos = new CertificateInfos(assessedIdentity, score, maxScore, passed, completion);
		CertificateConfig config = CertificateConfig.builder()
				.withCustom1(certificateConfig.getCertificateCustom1())
				.withCustom2(certificateConfig.getCertificateCustom2())
				.withCustom3(certificateConfig.getCertificateCustom3())
				.withSendEmailBcc(true)
				.withSendEmailLinemanager(true)
				.withSendEmailIdentityRelations(true)
				.build();
		
		CertificateTemplate template = certificateConfig.getTemplate();
		certificatesManager.generateCertificate(certificateInfos, courseEntry, template, config);
		loadModel();
		showInfo("msg.certificate.pending");
		fireEvent(ureq, Event.CHANGED_EVENT);
	}
}