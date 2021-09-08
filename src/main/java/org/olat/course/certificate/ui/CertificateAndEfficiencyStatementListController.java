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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.stack.BreadcrumbPanelAware;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.CorruptedCourseException;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.EfficiencyStatement;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.course.assessment.portfolio.EfficiencyStatementMediaHandler;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListModel.Cols;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.component.LearningProgressCompletionCellRenderer;
import org.olat.modules.portfolio.PortfolioV2Module;
import org.olat.modules.portfolio.ui.wizard.CollectArtefactController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 21.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CertificateAndEfficiencyStatementListController extends FormBasicController implements BreadcrumbPanelAware, GenericEventListener, Activateable2 {
	
	private static final String CMD_SHOW = "cmd.show";
	private static final String CMD_LAUNCH_COURSE = "cmd.launch.course";
	private static final String CMD_DELETE = "cmd.delete";
	private static final String CMD_MEDIA = "cmd.MEDIA";

	private FlexiTableElement tableEl;
	private BreadcrumbPanel stackPanel;
	private FormLink coachingToolButton;
	private CertificateAndEfficiencyStatementListModel tableModel;

	private CloseableModalController cmc;
	private CollectArtefactController collectorCtrl;
	private DialogBoxController confirmDeleteCtr;
	
	private final boolean canModify;
	private final boolean linkToCoachingTool;
	private final boolean canLaunchCourse;
	private final Identity assessedIdentity;
	
	@Autowired
	private EfficiencyStatementManager esm;
	@Autowired
	private PortfolioV2Module portfolioV2Module;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CertificatesManager certificatesManager;
	@Autowired
	private EfficiencyStatementMediaHandler mediaHandler;
	@Autowired
	private AssessmentService assessmentService;
	
	public CertificateAndEfficiencyStatementListController(UserRequest ureq, WindowControl wControl) {
		this(ureq, wControl, ureq.getUserSession().getIdentity(), false, true, true);
	}
	
	public CertificateAndEfficiencyStatementListController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, boolean linkToCoachingTool, boolean canModify, boolean canLaunchCourse) {
		super(ureq, wControl, "cert_statement_list");
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		this.canModify = canModify;
		this.assessedIdentity = assessedIdentity;
		this.linkToCoachingTool = linkToCoachingTool;
		this.canLaunchCourse = canLaunchCourse;

		// Show heading
		flc.contextPut("showHeading", true);
		
		initForm(ureq);
		
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	public CertificateAndEfficiencyStatementListController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, boolean linkToCoachingTool, boolean canModify, boolean canLaunchCourse, boolean showHeading) {
		this(ureq, wControl, assessedIdentity, linkToCoachingTool, canModify, canLaunchCourse);

		// Set visibility of heading
		flc.contextPut("showHeading", showHeading);
	}
	
	public CertificateAndEfficiencyStatementListController(UserRequest ureq, WindowControl wControl, Identity assessedIdentity, boolean withFieldSet) {
		this(ureq, wControl, assessedIdentity, false, false, true);
		
		// Show different header in user management
		flc.contextPut("withFieldSet", withFieldSet);
	}
	
	@Override
	protected void doDispose() {
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	public void event(Event event) {
		if(event instanceof CertificateEvent) {
			CertificateEvent ce = (CertificateEvent)event;
			if(getIdentity().getKey().equals(ce.getOwnerKey())) {
				updateStatement(ce.getResourceKey(), ce.getCertificateKey());
			}
		}
	}
	
	private void updateStatement(Long resourceKey, Long certificateKey) {
		List<CertificateAndEfficiencyStatement> statements = tableModel.getObjects();
		for(int i=statements.size(); i-->0; ) {
			CertificateAndEfficiencyStatement statement = statements.get(i);
			if(resourceKey.equals(statement.getResourceKey())) {
				CertificateLight certificate = certificatesManager.getCertificateLightById(certificateKey);
				statement.setCertificate(certificate);
				break;
			}
		}
	}

	@Override
	public void setBreadcrumbPanel(BreadcrumbPanel stackPanel) {
		this.stackPanel = stackPanel;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(linkToCoachingTool) {
			flc.contextPut("withFieldSet", true);
			coachingToolButton = uifactory.addFormLink("coaching.tool", formLayout, Link.BUTTON);
		}
		
		FlexiTableColumnModel tableColumnModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.displayName));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.score));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.passed, new PassedCellRenderer()));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.completion, new LearningProgressCompletionCellRenderer()));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.show",
				translate("table.header.show"), CMD_SHOW));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lastModified));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.lastUserUpdate));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.certificate, new DownloadCertificateCellRenderer(assessedIdentity, getLocale())));
		tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Cols.recertification, new DateFlexiCellRenderer(getLocale())));
		if (canLaunchCourse) {
			tableColumnModel.addFlexiColumnModel(new DefaultFlexiColumnModel("table.header.launchcourse",
					translate("table.header.launchcourse"), CMD_LAUNCH_COURSE));
		}
	
		if(canModify) {
			DefaultFlexiColumnModel deleteColumn = new DefaultFlexiColumnModel(Cols.deleteEfficiencyStatement.i18nHeaderKey(), Cols.deleteEfficiencyStatement.ordinal(), CMD_DELETE,
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("table.action.delete"), CMD_DELETE), null));
			tableColumnModel.addFlexiColumnModel(deleteColumn);
		}
		
		//artefact
		if(assessedIdentity.equals(getIdentity())) {
			if(portfolioV2Module.isEnabled()) {
				DefaultFlexiColumnModel portfolioColumn = new DefaultFlexiColumnModel( Cols.artefact, CMD_MEDIA,
						new BooleanCellRenderer(new StaticFlexiCellRenderer(CMD_MEDIA, new AsArtefactCellRenderer()), null));
				tableColumnModel.addFlexiColumnModel(portfolioColumn);
			}
		}
		
		tableModel = new CertificateAndEfficiencyStatementListModel(tableColumnModel, getLocale());
		loadModel();
		tableEl = uifactory.addTableElement(getWindowControl(), "certificates", tableModel, getTranslator(), formLayout);
		tableEl.setElementCssClass("o_sel_certificates_table");
		tableEl.setEmptyTableSettings("table.statements.empty", null, "o_icon_certificate");
	}
	
	private void loadModel() {
		Map<Long, CertificateAndEfficiencyStatement> resourceKeyToStatments = new HashMap<>();
		List<CertificateAndEfficiencyStatement> statments = new ArrayList<>();
		List<UserEfficiencyStatementLight> efficiencyStatementsList = esm.findEfficiencyStatementsLight(assessedIdentity);
		
		List<Long> courseEntryKeys = efficiencyStatementsList.stream()
				.map(UserEfficiencyStatementLight::getCourseRepoKey)
				.filter(key -> key != null)
				.collect(Collectors.toList());
		Map<Long, Double> courseEntryKeysToCompletion = assessmentService
				.loadRootAssessmentEntriesByAssessedIdentity(assessedIdentity, courseEntryKeys).stream()
				.filter(ae -> ae.getCompletion() != null)
				.collect(Collectors.toMap(
						ae -> ae.getRepositoryEntryKey(),
						ae -> ae.getCompletion()
					));
		
		for(UserEfficiencyStatementLight efficiencyStatement:efficiencyStatementsList) {
			CertificateAndEfficiencyStatement wrapper = new CertificateAndEfficiencyStatement();
			wrapper.setDisplayName(efficiencyStatement.getTitle());
			wrapper.setPassed(efficiencyStatement.getPassed());
			wrapper.setScore(efficiencyStatement.getScore());
			wrapper.setEfficiencyStatementKey(efficiencyStatement.getKey());
			wrapper.setResourceKey(efficiencyStatement.getArchivedResourceKey());
			wrapper.setLastModified(efficiencyStatement.getLastModified());
			wrapper.setLastUserModified(efficiencyStatement.getLastUserModified());
			Double completion = courseEntryKeysToCompletion.get(efficiencyStatement.getCourseRepoKey());
			wrapper.setCompletion(completion);
			statments.add(wrapper);
			resourceKeyToStatments.put(efficiencyStatement.getArchivedResourceKey(), wrapper);
		}
		
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(assessedIdentity);
		for(CertificateLight certificate:certificates) {
			Long resourceKey = certificate.getOlatResourceKey();
			CertificateAndEfficiencyStatement wrapper = resourceKeyToStatments.get(resourceKey);
			if(wrapper == null) {
				wrapper = new CertificateAndEfficiencyStatement();
				wrapper.setDisplayName(certificate.getCourseTitle());
				resourceKeyToStatments.put(resourceKey, wrapper);
				statments.add(wrapper);
			} else {
				if(!StringHelper.containsNonWhitespace(wrapper.getDisplayName())) {
					wrapper.setDisplayName(certificate.getCourseTitle());
				}
				wrapper.setResourceKey(resourceKey);
			}
			if(resourceKey != null && wrapper.getResourceKey() == null) {
				wrapper.setResourceKey(resourceKey);
			}
			wrapper.setCertificate(certificate);
		}
		
		for(CertificateAndEfficiencyStatement statment:statments) {
			if(!StringHelper.containsNonWhitespace(statment.getDisplayName()) && statment.getResourceKey() != null) {
				String displayName = repositoryManager.lookupDisplayNameByResourceKey(statment.getResourceKey());
				statment.setDisplayName(displayName);
			}
		}
		
		tableModel.setObjects(statments);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent te = (SelectionEvent) event;
				String cmd = te.getCommand();
				CertificateAndEfficiencyStatement statement = tableModel.getObject(te.getIndex());
				if(CMD_LAUNCH_COURSE.equals(cmd)) {
					doLaunchCourse(ureq, statement.getResourceKey());
				} else if(CMD_DELETE.equals(cmd)) {
					doConfirmDelete(ureq, statement);
				} else if(CMD_SHOW.equals(cmd)) {
					doShowStatement(ureq, statement);
				} else if(CMD_MEDIA.equals(cmd)) {
					doCollectMedia(ureq, statement.getDisplayName(), statement.getEfficiencyStatementKey());
				}
			}
		} else if(coachingToolButton == source) {
			doLaunchCoachingTool(ureq);
		}
		super.formInnerEvent(ureq, source, event);
	}
	
	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		 if (source == confirmDeleteCtr) {
			if (DialogBoxUIFactory.isYesEvent(event)) {
				CertificateAndEfficiencyStatement statement = (CertificateAndEfficiencyStatement)confirmDeleteCtr.getUserObject();
				doDelete(statement.getEfficiencyStatementKey());
			}
		} else if(collectorCtrl == source) {
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(collectorCtrl);
		removeAsListenerAndDispose(cmc);
		collectorCtrl = null;
		cmc = null;
	}
	
	private void doShowStatement(UserRequest ureq, CertificateAndEfficiencyStatement statement) {
		RepositoryEntry entry = repositoryService.loadByResourceKey(statement.getResourceKey());
		EfficiencyStatement efficiencyStatment = esm.getUserEfficiencyStatementByKey(statement.getEfficiencyStatementKey());
		CertificateAndEfficiencyStatementController efficiencyCtrl = new CertificateAndEfficiencyStatementController(getWindowControl(), ureq,
				assessedIdentity, null, statement.getResourceKey(), entry, efficiencyStatment, false);
		listenTo(efficiencyCtrl);
		stackPanel.pushController(statement.getDisplayName(), efficiencyCtrl);
	}

	private void doConfirmDelete(UserRequest ureq, CertificateAndEfficiencyStatement statement) {
		RepositoryEntry re = repositoryService.loadByResourceKey(statement.getResourceKey());
		if(re == null) {
			String text = translate("efficiencyStatements.delete.confirm", statement.getDisplayName());
			confirmDeleteCtr = activateYesNoDialog(ureq, null, text, confirmDeleteCtr);
			confirmDeleteCtr.setUserObject(statement);
		} else {
			showWarning("efficiencyStatements.cannot.delete");
		}
	}
	
	private void doDelete(Long efficiencyStatementKey) {
		UserEfficiencyStatementLight efficiencyStatement = esm.getUserEfficiencyStatementLightByKey(efficiencyStatementKey);
		if(efficiencyStatement != null) {
			esm.deleteEfficiencyStatement(efficiencyStatement);
		}
		
		loadModel();
		tableEl.reset();
		showInfo("info.efficiencyStatement.deleted");
	}
	
	private void doLaunchCoachingTool(UserRequest ureq) {
		String businessPath = "[CoachSite:0][Search:0][Identity:" + assessedIdentity.getKey() + "]";
		NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
	}

	private void doLaunchCourse(UserRequest ureq, Long resourceKey) {
		RepositoryEntry entry = repositoryService.loadByResourceKey(resourceKey);
		if(entry == null) {
			showWarning("efficiencyStatements.course.noexists");
		} else if (!repositoryManager.isAllowedToLaunch(getIdentity(), ureq.getUserSession().getRoles(), entry)) {
			showWarning("efficiencyStatements.course.noaccess");
		} else {
			try {
				String businessPath = "[RepositoryEntry:" + entry.getKey() + "]";
				NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
			} catch (CorruptedCourseException e) {
				logError("Course corrupted: " + entry.getKey() + " (" + entry.getResourceableId() + ")", e);
				showError("cif.error.corrupted");
			}
		}
	}

	private void doCollectMedia(UserRequest ureq, String title, Long efficiencyStatementKey) {
		if(guardModalController(collectorCtrl)) return;
		
		EfficiencyStatement fullStatement = esm.getUserEfficiencyStatementByKey(efficiencyStatementKey);
		collectorCtrl = new CollectArtefactController(ureq, getWindowControl(), fullStatement, mediaHandler, null);
		listenTo(collectorCtrl);
		
		cmc = new CloseableModalController(getWindowControl(), null, collectorCtrl.getInitialComponent(), true, title, true);
		cmc.addControllerListener(this);
		cmc.activate();
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {

	}

	public class AsArtefactCellRenderer implements FlexiCellRenderer {
		
		@Override
		public void render(Renderer renderer, StringOutput sb, Object cellValue, int row,
				FlexiTableComponent source, URLBuilder ubu, Translator translator) {
			sb.append("<i class='o_icon o_icon-lg o_icon_eportfolio_add'> </i> <span title=\"")
				.append(translate("table.add.as.artefact"))
				.append("\"> </span>");
		}
	}
}
