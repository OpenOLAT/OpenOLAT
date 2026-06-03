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
package org.olat.course.certificate.ui;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import jakarta.servlet.http.HttpServletRequest;

import org.olat.basesecurity.BaseSecurityManager;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.image.Size;
import org.olat.core.commons.services.vfs.VFSRepositoryService;
import org.olat.core.dispatcher.mapper.Mapper;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.emptystate.EmptyStateConfig;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.ActionsColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableOneClickSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.lightbox.LightboxController;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.gui.media.NotFoundMediaResource;
import org.olat.core.gui.media.ServletUtil;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSMediaResource;
import org.olat.course.assessment.ui.tool.IdentityCertificatesController;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.CertificatesModule;
import org.olat.course.certificate.RepositoryEntryCertificateConfiguration;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.course.certificate.model.CertificateWithInfos;
import org.olat.course.certificate.ui.CertificatesListDataModel.CertificateCols;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.ui.CertificationHelper;
import org.olat.modules.certificationprogram.ui.CertificationProgramCertifiedMembersController;
import org.olat.modules.certificationprogram.ui.CertificationStatus;
import org.olat.modules.certificationprogram.ui.component.CertificationStatusCellRenderer;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * @see IdentityCertificatesController
 * 
 * Initial date: 23 oct. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificatesListOverviewController extends FormBasicController implements Activateable2, FlexiTableComponentDelegate {

	protected static final Size THUMBNAIL_SIZE = new Size(249, 172, false);
	protected static final String THUMBNAIL_MAPPER_ID = "media-thumbnail-249-172";

	private static final String ALL_TAB_ID = "All";
	private static final String VALID_TAB_ID = "Valid";
	private static final String EXPIRED_TAB_ID = "Expired";
	
	protected static final String FILTER_WITH_RECERTIFICATION = "WithRecertification";
	protected static final String FILTER_STATUS = "Status";
	protected static final String FILTER_ORIGIN = "Origin";
	protected static final String FILTER_ORIGIN_COURSE_KEY = "course";
	protected static final String FILTER_ORIGIN_PROGRAM_KEY = "program";
	protected static final String FILTER_ORIGIN_UPLOAD_KEY = "upload";

	private FlexiFiltersTab allTab;
	private FlexiFiltersTab activeTab;
	private FlexiFiltersTab expiredTab;
	
	private FormLink uploadCertificateButton;
	private FlexiTableElement tableEl;
	private CertificatesListDataModel tableModel;

	private final Identity assessedIdentity;
	private final boolean canUploadExternalCertificate;

	private ToolsController toolsCtrl;
	private CloseableModalController cmc;
	private LightboxController lightboxCtrl;
	private CertificateDetailsController detailsCtrl;
	private CloseableCalloutWindowController calloutCtrl;
	private ConfirmDeleteCertificateController deleteCertificateCtrl;
	private UploadExternalCertificateController uploadCertificateController;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private CertificatesModule certificatesModule;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private BaseSecurityManager baseSecurityManager;
	@Autowired
	private VFSRepositoryService vfsRepositoryService;
	
	public CertificatesListOverviewController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, ureq.getIdentity(), null);
	}
	
	public CertificatesListOverviewController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, Boolean canUploadCertificate) {
		super(ureq, wControl, "cert_list_overview",
				Util.createPackageTranslator(CertificationProgramCertifiedMembersController.class, ureq.getLocale()));
		this.assessedIdentity = assessedIdentity;
		
		// Upload certificates
		if (canUploadCertificate == null) {
			Roles userRoles = ureq.getUserSession().getRoles();
			if (getIdentity().equals(assessedIdentity)) {
				canUploadExternalCertificate = certificatesModule.canUserUploadExternalCertificates();
			} else if (userRoles.isUserManager()) {
				canUploadExternalCertificate = certificatesModule.canUserManagerUploadExternalCertificates();
			} else if (userRoles.isLineManager()) {
				canUploadExternalCertificate = baseSecurityManager.getRoles(assessedIdentity).hasRole(userRoles.getOrganisations(), OrganisationRoles.user);			
			} else if (userRoles.isAdministrator() || userRoles.isSystemAdmin()) {
				canUploadExternalCertificate = true;
			} else {
				canUploadExternalCertificate = false;
			}
		} else {
			canUploadExternalCertificate = canUploadCertificate.booleanValue();
		}
		
		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if (canUploadExternalCertificate) {
			uploadCertificateButton = uifactory.addFormLink("upload.certificate", formLayout, Link.BUTTON);
			uploadCertificateButton.setIconLeftCSS("o_icon o_icon_import");
		}

		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificateCols.awardedBy));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificateCols.origin));
		DateFlexiCellRenderer dateCellRenderer = new DateFlexiCellRenderer(getLocale());
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificateCols.issuedOn,
				dateCellRenderer));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificateCols.validUntil,
				dateCellRenderer));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificateCols.dateRecertification,
				new RecertificationInDaysFlexiCellRenderer(getTranslator())));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificateCols.dateRevocation,
				dateCellRenderer));
		
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificateCols.recertificationCount));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificateCols.status,
				new CertificationStatusCellRenderer(getTranslator())));
		
		// Workaround to prevent dirty marking after uploading a certificate
		StaticFlexiCellRenderer reportRenderer = new StaticFlexiCellRenderer(translate("open"), "open");
		reportRenderer.setPush(true);
		reportRenderer.setDirtyCheck(false);
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(true, true, "open", null, -1, "open", false, null, FlexiColumnModel.ALIGNMENT_LEFT, reportRenderer));

		tableColumnModel.addFlexiColumnModel(new ActionsColumnModel(CertificateCols.tools));
		
		tableModel = new CertificatesListDataModel(tableColumnModel, getLocale());	
		tableEl = uifactory.addTableElement(getWindowControl(), "certificates", tableModel, 25, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setElementCssClass("o_sel_certificates");
		tableEl.setEmptyStateConfig(EmptyStateConfig.builder()
				.withMessageI18nKey("table.statements.empty")
				.withIconCss("o_icon_certificate")
				.build());
		VelocityContainer rowVC = createVelocityContainer("certificate_row");
		rowVC.setDomReplacementWrapperRequired(false);
		String mapperThumbnailUrl = registerCacheableMapper(ureq, THUMBNAIL_MAPPER_ID,
				new ThumbnailMapper(tableModel, certificatesManager, vfsRepositoryService));
		rowVC.contextPut("mapperThumbnailUrl", mapperThumbnailUrl);
		tableEl.setRowRenderer(rowVC, this);
		tableEl.setCssDelegate(CertificateListCssDelegate.DELEGATE);
		
		initFilters();
		initFiltersPresets(ureq);
		
		String prefsId = !getIdentity().equals(assessedIdentity)
				? "certificates-list-user-overview-v1"
				: "certificates-list-admin-overview-v1";
		tableEl.setAndLoadPersistedPreferences(ureq, prefsId);
	}
	
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();

		SelectionValues recertificationValues = new SelectionValues();
		recertificationValues.add(SelectionValues.entry(FILTER_WITH_RECERTIFICATION, translate("filter.with.recertification")));
		filters.add(new FlexiTableOneClickSelectionFilter(translate("filter.with.recertification"),
				FILTER_WITH_RECERTIFICATION, recertificationValues, true));
		
		SelectionValues originValues = new SelectionValues();
		originValues.add(SelectionValues.entry(FILTER_ORIGIN_PROGRAM_KEY, translate("origin.certification.program")));
		originValues.add(SelectionValues.entry(FILTER_ORIGIN_COURSE_KEY, translate("origin.course")));
		originValues.add(SelectionValues.entry(FILTER_ORIGIN_UPLOAD_KEY, translate("origin.upload.manual")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.certificate.origin"),
				FILTER_ORIGIN, originValues, true));
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(CertificationStatus.VALID.name(), translate("certification.status.valid")));
		statusValues.add(SelectionValues.entry(CertificationStatus.EXPIRED.name(), translate("certification.status.expired")));
		statusValues.add(SelectionValues.entry(CertificationStatus.REVOKED.name(), translate("certification.status.revoked")));
		statusValues.add(SelectionValues.entry(CertificationStatus.ARCHIVED.name(), translate("certification.status.archived")));
		filters.add(new FlexiTableMultiSelectionFilter(translate("filter.certificate.status"),
				FILTER_STATUS, statusValues, true));
		
		tableEl.setFilters(true, filters, false, false);
	}
	
	private void initFiltersPresets(UserRequest ureq) {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB_ID, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		activeTab = FlexiFiltersTabFactory.tabWithImplicitFilters(VALID_TAB_ID, translate("certification.status.valid"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, CertificationStatus.VALID.name())));
		tabs.add(activeTab);
		
		expiredTab = FlexiFiltersTabFactory.tabWithImplicitFilters(EXPIRED_TAB_ID, translate("certification.status.expired"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS, CertificationStatus.EXPIRED.name())));
		tabs.add(expiredTab);
		
		tableEl.setFilterTabs(true, tabs);
		tableEl.setSelectedFilterTab(ureq, allTab);
	}
	
	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return List.of();
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}

	private void loadModel(UserRequest ureq) {
		List<CertificateWithInfos> certificates = certificatesManager.getCertificatesWithInfos(assessedIdentity);
		List<CertificateRow> rows = certificates.stream()
				.map(infos -> forgeRow(infos, ureq))
				.toList();
		tableModel.setObjects(rows);
	}
	
	private CertificateRow forgeRow(CertificateWithInfos infos, UserRequest ureq) {
		Certificate certificate = infos.certificate();
		Date referenceDate = ureq.getRequestTimestamp();
		RepositoryEntry entry = infos.repositoryEntry();
		RepositoryEntryCertificateConfiguration certificateConfig = infos.certificateConfiguration();
		CertificationProgram program = certificate instanceof CertificateImpl impl
				? impl.getCertificationProgram()
				: null;
		String points = program != null && program.getCreditPointSystem() != null
				? CertificationHelper.compactCreditPointsToString(program)
				: null;
		
		Identity uploadedBy = certificate.getUploadedBy();
		String uploadedByName = certificate.getUploadedBy() == null
				? null
				: userManager.getUserDisplayName(uploadedBy);
		String origin;
		if(program != null) {
			origin = translate("origin.certification.program");
		} else if(uploadedBy != null) {
			origin = translate("origin.upload.manual");
		} else {
			origin = translate("origin.course");
		}
		
		String filename = DownloadCertificateCellRenderer.getName(certificate);
		
		RecertificationInDays recertificationInDays = null;
		if(program != null && program.isRecertificationEnabled()) {
			recertificationInDays = RecertificationInDays.valueOf(certificate, program, referenceDate);
		} else if(entry != null && certificateConfig != null && certificateConfig.isRecertificationEnabled()) {
			Date nextRecertificationDate = certificatesManager.nextRecertificationWindow(certificate, infos.certificateConfiguration());
			if(nextRecertificationDate != null) {
				recertificationInDays = RecertificationInDays.valueOf(certificate.getNextRecertificationDate(), nextRecertificationDate, entry, referenceDate);
			}
		}

		CertificationStatus status = CertificationStatus.evaluate(certificate, referenceDate);
		String statusString = status.asLabelExplained(certificate, referenceDate, getTranslator());
		
		// Uploaded are not issued
		Long issued = null;
		FormLink toolsLink = null;
		if(certificate.getUploadedBy() == null) {
			issued = infos.issued() == 0 ? 1l : infos.issued();
		} else if(canUploadExternalCertificate) {// If you can upload it, you can delete it
			toolsLink = ActionsColumnModel.createLink(uifactory, getTranslator());
		}

		CertificateRow certificateRow = new CertificateRow(certificate, infos.repositoryEntry(), certificateConfig, program, uploadedByName,
				status, statusString, recertificationInDays, issued, filename, origin, points, toolsLink);
		if(toolsLink != null) {
			toolsLink.setUserObject(certificateRow);
		}
		return certificateRow;
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(uploadCertificateController == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq);
				tableEl.reset(true, true, true);
			}
			cmc.deactivate();
			cleanUp();
		} else if(detailsCtrl == source) {
			if(event instanceof DeleteCertificateEvent) {
				loadModel(ureq);
				filterModel();
				lightboxCtrl.deactivate();
				cleanUp();
			} else if(event == Event.CHANGED_EVENT) {
				loadModel(ureq);
				filterModel();
			} else {
				lightboxCtrl.deactivate();
				cleanUp();
			}
		} else if(deleteCertificateCtrl == source) {
			if(event == Event.DONE_EVENT) {
				loadModel(ureq);
				filterModel();
			}
			cmc.deactivate();
			cleanUp();
			
		} else if(toolsCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
		} else if(cmc == source || lightboxCtrl == source || calloutCtrl == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(uploadCertificateController);
		removeAsListenerAndDispose(deleteCertificateCtrl);
		removeAsListenerAndDispose(lightboxCtrl);
		removeAsListenerAndDispose(calloutCtrl);
		removeAsListenerAndDispose(detailsCtrl);
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(cmc);
		uploadCertificateController = null;
		deleteCertificateCtrl = null;
		lightboxCtrl = null;
		calloutCtrl = null;
		detailsCtrl = null;
		toolsCtrl = null;
		cmc = null;
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == mainForm.getInitialComponent()) {
			if("ONCLICK".equals(event.getCommand())) {
				String rowKeyStr = ureq.getParameter("select_row");
				if(StringHelper.isLong(rowKeyStr)) {
					try {
						Long rowKey = Long.valueOf(rowKeyStr);
						CertificateRow row = tableModel.getObjectByCertificateKey(rowKey);
						doOpenCertificateDetails(ureq, row);
					} catch (NumberFormatException e) {
						logWarn("Not a valid long: " + rowKeyStr, e);
					}
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(uploadCertificateButton == source) {
			doUploadCertificateController(ureq);
		} else if(tableEl == source) {
			if(event instanceof SelectionEvent se) {
				if("open".equals(se.getCommand())) {
					CertificateRow row = tableModel.getObject(se.getIndex());
					doOpenCertificateDetails(ureq, row);
				} 
			} else if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		} else if(source instanceof FormLink link
				&& link.getUserObject() instanceof CertificateRow selectedRow) {
			doOpenTools(ureq, selectedRow, link);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	
	private void doUploadCertificateController(UserRequest ureq) {
		if(guardModalController(uploadCertificateController)) return;
		
		uploadCertificateController = new UploadExternalCertificateController(ureq, getWindowControl(), assessedIdentity);
		listenTo(uploadCertificateController);
		
		cmc = new CloseableModalController(getWindowControl(), null, uploadCertificateController.getInitialComponent(), true, translate("upload.certificate"), true);
		cmc.addControllerListener(this);
		cmc.activate();
	}
	
	private void doOpenCertificateDetails(UserRequest ureq, CertificateRow row) {
		detailsCtrl = new CertificateDetailsController(ureq, getWindowControl(), assessedIdentity,
				row, canUploadExternalCertificate);
		listenTo(detailsCtrl);
		
		lightboxCtrl = new LightboxController(ureq, getWindowControl(), detailsCtrl);
		listenTo(lightboxCtrl);
		lightboxCtrl.activate();
	}
	
	private void doOpenTools(UserRequest ureq, CertificateRow row, FormLink link) {
		if(toolsCtrl != null) return;
		
		removeAsListenerAndDispose(toolsCtrl);
		removeAsListenerAndDispose(calloutCtrl);

		toolsCtrl = new ToolsController(ureq, getWindowControl(), row);
		listenTo(toolsCtrl);

		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				toolsCtrl.getInitialComponent(), link.getFormDispatchId(), "", true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
	
	private static final class CertificateListCssDelegate extends DefaultFlexiTableCssDelegate {
		
		private static final CertificateListCssDelegate DELEGATE = new CertificateListCssDelegate();
		
		@Override
		public String getWrapperCssClass(FlexiTableRendererType type) {
			return "o_table_wrapper o_table_flexi o_certificates_list";
		}
		
		@Override
		public String getTableCssClass(FlexiTableRendererType type) {
			return FlexiTableRendererType.custom == type
					? "o_certificates_rows o_block_top o_certificates_cards"
					: "o_certificates_rows o_block_top";
		}
		
		@Override
		public String getRowCssClass(FlexiTableRendererType type, int pos) {
			return "o_certificate_row";
		}
	}
	
	private static class ThumbnailMapper implements Mapper {
		
		private final CertificatesListDataModel model;
		private final CertificatesManager certificatesManager;
		private final VFSRepositoryService vfsRepositoryService;
		
		public ThumbnailMapper(CertificatesListDataModel model, CertificatesManager certificatesManager, VFSRepositoryService vfsRepositoryService) {
			this.model = model;
			this.certificatesManager = certificatesManager;
			this.vfsRepositoryService = vfsRepositoryService;
		}

		@Override
		public MediaResource handle(String relPath, HttpServletRequest request) {
			MediaResource mr = null;
			
			String row = relPath;
			if(row.startsWith("/")) {
				row = row.substring(1, row.length());
			}
			int index = row.indexOf("/");
			if(index > 0) {
				row = row.substring(0, index);
				CertificateRow certificateRow = model.getObjectByCertificateKey(Long.valueOf(row));
				VFSLeaf certificateLeaf = certificatesManager.getCertificateLeaf(certificateRow.getCertificate());
				if(certificateLeaf != null) {
					VFSLeaf thumbnail = vfsRepositoryService.getThumbnail(certificateLeaf, THUMBNAIL_SIZE.getWidth(), THUMBNAIL_SIZE.getHeight(), true);
					if(thumbnail != null) {
						mr = new VFSMediaResource(thumbnail, ServletUtil.CACHE_ONE_MONTH);
					}
				}
			}
			
			return mr == null ? new NotFoundMediaResource() : mr;
		}
	}
	
	private void doConfirmDelete(UserRequest ureq, CertificateRow row) {
		removeAsListenerAndDispose(deleteCertificateCtrl);
		removeAsListenerAndDispose(cmc);
		
		Certificate certificate = certificatesManager.getCertificateById(row.getKey());
		if(certificate == null) {
			loadModel(ureq);
			tableEl.reset(true, true, true);
		} else {
			deleteCertificateCtrl = new ConfirmDeleteCertificateController(ureq, getWindowControl(),
					translate("confirm.delete.certificate.def.text"), null,
					translate("delete"), certificate);
			listenTo(deleteCertificateCtrl);
	
			String title = translate("confirm.delete.certificate.title");
			cmc = new CloseableModalController(getWindowControl(), translate("close"), deleteCertificateCtrl.getInitialComponent(), true, title);
			listenTo(cmc);
			cmc.activate();
		}
	}
	
	private class ToolsController extends BasicController {

		private final Link deleteLink;
		
		private final CertificateRow row;
		
		public ToolsController(UserRequest ureq, WindowControl wControl, CertificateRow row) {
			super(ureq, wControl);
			this.row = row;
			
			VelocityContainer mainVC = createVelocityContainer("tools_certificate");
			
			deleteLink = LinkFactory.createLink("delete", "delete", getTranslator(), mainVC, this, Link.LINK);
			deleteLink.setIconLeftCSS("o_icon o_icon-fw o_icon_delete_item");
			
			putInitialPanel(mainVC);
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(deleteLink == source) {
				fireEvent(ureq, Event.CLOSE_EVENT);
				doConfirmDelete(ureq, row);
			}
		}
	}
}
