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
package org.olat.modules.coach.ui.curriculum.certificate;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilter;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CorruptedCourseException;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.manager.EfficiencyStatementManager;
import org.olat.course.assessment.model.UserEfficiencyStatementLight;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.CertificatesModule;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListController;
import org.olat.course.certificate.ui.CertificateAndEfficiencyStatementListModel;
import org.olat.course.certificate.ui.UploadExternalCertificateController;
import org.olat.modules.assessment.AssessmentEntryCompletion;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.ui.curriculum.certificate.CurriculumElementWithViewsDataModel.ElementViewCols;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.curriculum.ui.CurriculumElementCalendarController;
import org.olat.modules.curriculum.ui.CurriculumListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.olat.repository.model.SearchMyRepositoryEntryViewParams;
import org.olat.repository.ui.PriceMethod;
import org.olat.repository.ui.RepositoryEntryImageMapper;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
import org.olat.resource.OLATResource;
import org.olat.resource.accesscontrol.ACService;
import org.olat.resource.accesscontrol.AccessControlModule;
import org.olat.resource.accesscontrol.method.AccessMethodHandler;
import org.olat.resource.accesscontrol.model.OLATResourceAccess;
import org.olat.resource.accesscontrol.model.PriceMethodBundle;
import org.olat.resource.accesscontrol.ui.PriceFormat;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * This is a list of curriculum elements and repository entries
 * aimed to participants. The repository entries permissions
 * follow the same rules as {@link org.olat.repository.ui.list.RepositoryEntryListController}<br>
 * <p>
 * <p>
 * Initial date: 11 mai 2018<br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class CertificateAndEfficiencyStatementCurriculumListController extends FormBasicController implements FlexiTableCssDelegate, FlexiTableComponentDelegate, Activateable2 {

    private FlexiTableElement tableEl;
    private CurriculumElementWithViewsDataModel tableModel;
    private final BreadcrumbPanel stackPanel;
    
    private FormLink uploadCertificateButton;
    private boolean canUploadExternalCertificate;

    private int counter;
    private final boolean guestOnly;
    private final List<Curriculum> curriculumList;

    private final MapperKey mapperThumbnailKey;
    private final Identity assessedIdentity;
    private final CurriculumSecurityCallback curriculumSecurityCallback;
    private final RoleSecurityCallback roleSecurityCallback;

    private CloseableModalController cmc;
    private RepositoryEntryDetailsController detailsCtrl;
    private CurriculumElementCalendarController calendarsCtrl;
    private UploadExternalCertificateController uploadCertificateController;

    @Autowired
    private ACService acService;
    @Autowired
    private MarkManager markManager;
    @Autowired
    private MapperService mapperService;
    @Autowired
    private AccessControlModule acModule;
    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private CurriculumService curriculumService;
    @Autowired
    private RepositoryManager repositoryManager;
    @Autowired
    private AssessmentService assessmentService;
    @Autowired
    private BaseSecurity securityManager;
    @Autowired
    private EfficiencyStatementManager esm;
    @Autowired
    private CertificatesManager certificatesManager;
    @Autowired
    private CertificatesModule certificatesModule;

    public CertificateAndEfficiencyStatementCurriculumListController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
                                                                     Identity assessedIdentity, CurriculumSecurityCallback curriculumSecurityCallback, RoleSecurityCallback roleSecurityCallback) {
        super(ureq, wControl, "curriculum_element_list");
        setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
        setTranslator(Util.createPackageTranslator(CertificateAndEfficiencyStatementListController.class, getLocale(), getTranslator()));
        setTranslator(Util.createPackageTranslator(CurriculumListController.class, getLocale(), getTranslator()));
        setTranslator(Util.createPackageTranslator(RepositoryService.class, getLocale(), getTranslator()));
        setTranslator(Util.createPackageTranslator(CertificateAndEfficiencyStatementCurriculumListController.class, getLocale(), getTranslator()));

        this.stackPanel = stackPanel;
        this.curriculumSecurityCallback = curriculumSecurityCallback;
        this.roleSecurityCallback = roleSecurityCallback;
        this.assessedIdentity = assessedIdentity;
        this.curriculumList = curriculumService.getMyCurriculums(assessedIdentity);
        guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
        mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper());
        
        // Upload certificates
		Roles userRoles = ureq.getUserSession().getRoles();
		if (getIdentity().equals(assessedIdentity)) {
			canUploadExternalCertificate = certificatesModule.canUserUploadExternalCertificates();
		} else if (userRoles.isUserManager()) {
			canUploadExternalCertificate = certificatesModule.canUserManagerUploadExternalCertificates();
		} else if (userRoles.isLineManager()) {
			canUploadExternalCertificate = securityManager.getRoles(assessedIdentity).hasRole(userRoles.getOrganisations(), OrganisationRoles.user);			
		} else if (userRoles.isAdministrator() || userRoles.isSystemAdmin()) {
			canUploadExternalCertificate = true;
		}

        initForm(ureq);
        loadModel(ureq);
    }

    @Override
    public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
    	//
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
    	if (canUploadExternalCertificate) {
			flc.contextPut("uploadCertificate", true);
			uploadCertificateButton = uifactory.addFormLink("upload.certificate", formLayout, Link.BUTTON);
			uploadCertificateButton.setIconLeftCSS("o_icon o_icon_import");
		}
    	
        FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementViewCols.key));

        TreeNodeFlexiCellRenderer treeNodeRenderer = new TreeNodeFlexiCellRenderer("select");
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.displayName, treeNodeRenderer));

        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.details));

        DefaultFlexiColumnModel elementIdentifierCol = new DefaultFlexiColumnModel(ElementViewCols.identifier, "select");
        elementIdentifierCol.setCellRenderer(new CurriculumElementCompositeRenderer("select", new TextFlexiCellRenderer()));
        columnsModel.addFlexiColumnModel(elementIdentifierCol);

        if (roleSecurityCallback.canViewCourseProgressAndStatus()) {
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.passed));
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.completion));
        }
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.lastModification));
        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.lastUserModified));
        if (roleSecurityCallback.canViewEfficiencyStatements()) {
            BooleanCellRenderer efficiencyStatementRenderer = new BooleanCellRenderer(new StaticFlexiCellRenderer("openStatement", translate("table.header.show")), null);
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.efficiencyStatement, efficiencyStatementRenderer));
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.certificate));
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.recertification));
        }
        if (roleSecurityCallback.canViewCalendar()) {
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.calendars));
        }

        tableModel = new CurriculumElementWithViewsDataModel(columnsModel);
        tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), formLayout);
        tableEl.setElementCssClass("o_curriculumtable");
        tableEl.setCustomizeColumns(true);
        tableEl.setEmptyTableMessageKey("table.curriculum.empty");
        tableEl.setCssDelegate(this);
        tableEl.setFilters("activity", getFilters(), false);
        tableEl.setSelectedFilterKey(CurriculumElementWithViewsDataModel.FilterKeys.withStatementOnly.name());

        VelocityContainer row = createVelocityContainer("curriculum_element_row");
        row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
        tableEl.setRowRenderer(row, this);

        tableEl.setAndLoadPersistedPreferences(ureq, "c123oach-mentor-curriculum-"
                + (assessedIdentity.equals(getIdentity()) ? "" : "look-"));
    }

    private List<FlexiTableFilter> getFilters() {
        List<FlexiTableFilter> filters = new ArrayList<>(5);
        filters.add(new FlexiTableFilter(translate(CurriculumElementWithViewsDataModel.FilterKeys.activ.i18nHeaderKey()), CurriculumElementWithViewsDataModel.FilterKeys.activ.name()));
        filters.add(new FlexiTableFilter(translate(CurriculumElementWithViewsDataModel.FilterKeys.withStatementOnly.i18nHeaderKey()), CurriculumElementWithViewsDataModel.FilterKeys.withStatementOnly.name()));
        filters.add(FlexiTableFilter.SPACER);
        filters.add(new FlexiTableFilter(translate(CurriculumElementWithViewsDataModel.FilterKeys.showAll.i18nHeaderKey()), CurriculumElementWithViewsDataModel.FilterKeys.showAll.name(), true));
        return filters;
    }

    @Override
    public String getWrapperCssClass(FlexiTableRendererType type) {
        return null;
    }

    @Override
    public String getTableCssClass(FlexiTableRendererType type) {
        return null;
    }

    @Override
    public String getRowCssClass(FlexiTableRendererType type, int pos) {
        StringBuilder sb = new StringBuilder(64);
        CurriculumTreeWithViewsRow rowWithView = tableModel.getObject(pos);
        if (type == FlexiTableRendererType.custom) {
            sb.append("o_table_row ");

            if (rowWithView.isCurriculumElementOnly()) {
                sb.append("o_curriculum_element");
                if (rowWithView.getCurriculumElementRepositoryEntryCount() > 1) {
                    sb.append(" o_with_multi_repository_entries");
                }
            } else if (rowWithView.isRepositoryEntryOnly()) {
                sb.append("o_repository_entry");
            } else if (rowWithView.isCurriculumElementWithEntry()) {
                sb.append("o_mixed_element");
            }
        }

        if (rowWithView.isCurriculum() || rowWithView.getLevel() == -1) {
            sb.append(" o_curriculum");
        } else {
            int count = 0;
            for (CurriculumTreeWithViewsRow parent = rowWithView.getParent(); parent != null; parent = parent.getParent()) {
                count++;
            }

            // Substract one level for the curriculum parent
            if (count > 0) {
                count -= 1;
            }

            sb.append(" o_curriculum_element_l").append(count);
            if (!rowWithView.isRepositoryEntryOnly() && rowWithView.getCurriculumElementTypeCssClass() != null) {
                sb.append(" ").append(rowWithView.getCurriculumElementTypeCssClass());
            }
            if (rowWithView.getEntryStatus() != null) {
                sb.append(" repo_status_").append(rowWithView.getEntryStatus());
            }
        }
        return sb.toString();
    }

    @Override
    public Iterable<Component> getComponents(int row, Object rowObject) {
        return null;
    }

    private void loadModel(UserRequest ureq) {
        // Load efficiency statements
        Map<Long, CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement> resourceKeyToStatments = new HashMap<>();
        List<CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement> statements = new ArrayList<>();
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
            CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement wrapper = new CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement();
            wrapper.setDisplayName(efficiencyStatement.getShortTitle());
            wrapper.setPassed(efficiencyStatement.getPassed());
            wrapper.setScore(efficiencyStatement.getScore());
            wrapper.setEfficiencyStatementKey(efficiencyStatement.getKey());
            wrapper.setResourceKey(efficiencyStatement.getArchivedResourceKey());
            wrapper.setLastModified(efficiencyStatement.getLastModified());
            wrapper.setLastUserModified(efficiencyStatement.getLastUserModified());
            Double completion = courseEntryKeysToCompletion.get(efficiencyStatement.getCourseRepoKey());
            wrapper.setCompletion(completion);
            statements.add(wrapper);
            resourceKeyToStatments.put(efficiencyStatement.getArchivedResourceKey(), wrapper);
        }

        List<CertificateLight> certificates = certificatesManager.getLastCertificates(assessedIdentity);
        for(CertificateLight certificate:certificates) {
            Long resourceKey = certificate.getOlatResourceKey();
            CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement wrapper = resourceKeyToStatments.get(resourceKey);
            if(wrapper == null) {
                wrapper = new CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement();
                wrapper.setDisplayName(certificate.getCourseTitle());
                resourceKeyToStatments.put(resourceKey, wrapper);
                statements.add(wrapper);
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

        for(CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement statment:statements) {
            if(!StringHelper.containsNonWhitespace(statment.getDisplayName()) && statment.getResourceKey() != null) {
                String displayName = repositoryManager.lookupDisplayNameByResourceKey(statment.getResourceKey());
                statment.setDisplayName(displayName);
            }
        }

        // Set of Olat resources with statements
        Set<Long> statementEntries = statements.stream().map(CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement::getResourceKey).collect(Collectors.toSet());
        // Set of entries, which will be added in the next step
        Set<Long> alreadyAdded = new HashSet<>();

        // Load Curricula
        Roles roles = ureq.getUserSession().getRoles();
        List<CurriculumTreeWithViewsRow> allRows = new ArrayList<>();
        List<CurriculumElementRepositoryEntryViews> elementsWithViewsForAll = curriculumService.getCurriculumElements(assessedIdentity, roles, curriculumList);
        Map<Curriculum, List<CurriculumElementRepositoryEntryViews>> elementsMap = elementsWithViewsForAll.stream().collect(Collectors.groupingBy(row -> row.getCurriculumElement().getCurriculum(), Collectors.toList()));

        for (Curriculum curriculum : curriculumList) {
            CurriculumTreeWithViewsRow curriculumRow = new CurriculumTreeWithViewsRow(curriculum);
            List<CurriculumElementRepositoryEntryViews> elementsWithViews = elementsMap.get(curriculum);

            if (elementsWithViews != null && !elementsWithViews.isEmpty()) {
                List<CurriculumTreeWithViewsRow> rows = new ArrayList<>();

                Set<Long> repoKeys = new HashSet<>();
                List<OLATResource> resourcesWithAC = new ArrayList<>();
                for (CurriculumElementRepositoryEntryViews elementWithViews : elementsWithViews) {
                    for (RepositoryEntryMyView entry : elementWithViews.getEntries()) {
                        repoKeys.add(entry.getKey());
                        if (entry.isValidOfferAvailable()) {
                            resourcesWithAC.add(entry.getOlatResource());
                        }
                    }
                }
                List<OLATResourceAccess> resourcesWithOffer = acService.filterResourceWithAC(resourcesWithAC);
                repositoryService.filterMembership(assessedIdentity, repoKeys);

                for (CurriculumElementRepositoryEntryViews elementWithViews : elementsWithViews) {
                    CurriculumElement element = elementWithViews.getCurriculumElement();
                    CurriculumElementMembership elementMembership = elementWithViews.getCurriculumMembership();

                    List<RepositoryEntryMyView> repositoryEntryMyViews = new ArrayList<>();
                    if (elementWithViews.getEntries() != null && !elementWithViews.getEntries().isEmpty()) {
                        for (RepositoryEntryMyView entry : elementWithViews.getEntries()) {
                           // if (statementEntries.contains(entry.getOlatResource().getKey())) {
                                repositoryEntryMyViews.add(entry);
                           // }
                        }
                    }


                    if (repositoryEntryMyViews == null || repositoryEntryMyViews.isEmpty()) {
                        CurriculumTreeWithViewsRow row = new CurriculumTreeWithViewsRow(curriculum, element, elementMembership, 0);
                        forgeCalendarsLink(row);
                        rows.add(row);
                    } else if (repositoryEntryMyViews.size() == 1) {
                        CurriculumTreeWithViewsRow row = new CurriculumTreeWithViewsRow(curriculum, element, elementMembership, elementWithViews.getEntries().get(0), true);
                        forge(row, repoKeys, resourcesWithOffer);
                        forgeCalendarsLink(row);
                        rows.add(row);
                    } else {
                        CurriculumTreeWithViewsRow elementRow = new CurriculumTreeWithViewsRow(curriculum, element, elementMembership, elementWithViews.getEntries().size());
                        forgeCalendarsLink(elementRow);
                        rows.add(elementRow);
                        for (RepositoryEntryMyView entry : repositoryEntryMyViews) {
                            CurriculumTreeWithViewsRow row = new CurriculumTreeWithViewsRow(curriculum, element, elementMembership, entry, false);
                            forge(row, repoKeys, resourcesWithOffer);
                            rows.add(row);
                        }
                    }
                }


                Map<CurriculumKey, CurriculumTreeWithViewsRow> keyToRow = rows.stream()
                        .collect(Collectors.toMap(CurriculumTreeWithViewsRow::getKey, row -> row, (row1, row2) -> row1));
                rows.forEach(row -> {
                    row.setParent(keyToRow.get(row.getParentKey()));
                    if (row.getOlatResource() != null) {
                        alreadyAdded.add(row.getOlatResource().getKey());
                        if (statementEntries.contains(row.getOlatResource().getKey())){
                            row.setHasStatement(true);
                        }

                        VFSLeaf image = repositoryManager.getImage(row.getRepositoryEntryResourceable().getResourceableId(), row.getOlatResource());
                        if (image != null) {
                            row.setThumbnailRelPath(mapperThumbnailKey.getUrl() + "/" + image.getName());
                        }
                    }
                });

                removeByPermissions(rows);
                forgeCurriculumCompletions(rows);
                addRoot(rows, curriculumRow);

                allRows.add(curriculumRow);
                allRows.addAll(rows);
            }
        }



        // Filter for entries which are already in a curriculum
        Roles assessedRoles = securityManager.getRoles(assessedIdentity);
        SearchMyRepositoryEntryViewParams params = new SearchMyRepositoryEntryViewParams(assessedIdentity, assessedRoles);
        params.setMembershipMandatory(true);
        List<RepositoryEntryMyView> courses = repositoryService.searchMyView(params, 0, 0);

        courses.removeIf(course -> alreadyAdded.contains(course.getOlatResource().getKey()));

        // Filter for entries which are without curriculum
        CurriculumTreeWithViewsRow foreignEntryParent = new CurriculumTreeWithViewsRow(translate("curriculum.foreign.entries"));

        if (!courses.isEmpty()) {
            allRows.add(foreignEntryParent);

            courses.forEach(course -> {
                CurriculumTreeWithViewsRow row = new CurriculumTreeWithViewsRow(course);
                forgeSelectLink(row);
                forgeCompletion(row,row.getRepositoryEntryCompletion());
                forgeDetails(row);
                row.setParent(foreignEntryParent);
                row.setHasStatement(true);
                allRows.add(row);
                alreadyAdded.add(course.getOlatResource().getKey());
            });
        }

        // Add Statements which don't belong to any course
        Set<Long> orphanCertificates = statementEntries.stream().filter(certificateResourceKey -> !alreadyAdded.contains(certificateResourceKey)).collect(Collectors.toSet());

        if (!orphanCertificates.isEmpty()) {
            if (!allRows.contains(foreignEntryParent)) {
                allRows.add(foreignEntryParent);
            }

            orphanCertificates.forEach(orphan -> {
                CertificateAndEfficiencyStatementListModel.CertificateAndEfficiencyStatement statement = statements.stream().filter(certStatement -> certStatement.getResourceKey().equals(orphan)).findFirst().get();
                CurriculumTreeWithViewsRow row = new CurriculumTreeWithViewsRow(statement);
                row.setParent(foreignEntryParent);
                row.setHasStatement(true);
                allRows.add(row);
            });
        }


        Collections.sort(allRows, new CurriculumElementViewsRowComparator(getLocale()));


        tableModel.setObjects(allRows);
        tableEl.reset(true, true, true);
    }

    private void removeByPermissions(List<CurriculumTreeWithViewsRow> rows) {
        // propagate the member marker along the parent line
        for (CurriculumTreeWithViewsRow row : rows) {
            if (row.isCurriculumMember()) {
                for (CurriculumTreeWithViewsRow parentRow = row.getParent(); parentRow != null; parentRow = parentRow.getParent()) {
                    parentRow.setCurriculumMember(true);
                }
            }
        }

        // trim part of the tree without member flag
        rows.removeIf(curriculumTreeWithViewsRow -> !curriculumTreeWithViewsRow.isCurriculumMember());
    }

    private void forge(CurriculumTreeWithViewsRow row, Collection<Long> repoKeys, List<OLATResourceAccess> resourcesWithOffer) {
        if (row.getRepositoryEntryKey() == null || guestOnly) return;// nothing for guests

        boolean isMember = repoKeys.contains(row.getRepositoryEntryKey());
        row.setMember(isMember);

        FormLink startLink = null;
        List<PriceMethod> types = new ArrayList<>();
        if (row.isAllUsers() || isMember) {
            startLink = uifactory.addFormLink("start_" + (++counter), "start", "start", null, null, Link.LINK);
            startLink.setElementCssClass("o_start btn-block");
            startLink.setCustomEnabledLinkCSS("o_start btn-block");
            startLink.setIconRightCSS("o_icon o_icon_start");
        } else if (row.isBookable()) {
            // collect access control method icons
            OLATResource resource = row.getOlatResource();
            for (OLATResourceAccess resourceAccess : resourcesWithOffer) {
                if (resource.getKey().equals(resourceAccess.getResource().getKey())) {
                    for (PriceMethodBundle bundle : resourceAccess.getMethods()) {
                        String type = (bundle.getMethod().getMethodCssClass() + "_icon").intern();
                        String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
                        AccessMethodHandler amh = acModule.getAccessMethodHandler(bundle.getMethod().getType());
                        String displayName = amh.getMethodName(getLocale());
                        types.add(new PriceMethod(price, type, displayName));
                    }
                }
            }

            startLink = uifactory.addFormLink("start_" + (++counter), "start", "book", null, null, Link.LINK);
            startLink.setElementCssClass("o_start btn-block");
            startLink.setCustomEnabledLinkCSS("o_book btn-block");
            startLink.setIconRightCSS("o_icon o_icon_start");
        }

        if(startLink != null) {
            startLink.setUserObject(row);
            String businessPath = "[RepositoryEntry:" + row.getRepositoryEntryKey() + "]";
            String startUrl = BusinessControlFactory.getInstance().getAuthenticatedURLFromBusinessPathString(businessPath);
            startLink.setUrl(startUrl);
            row.setStartLink(startLink, startUrl);
        }


        if (!row.isAllUsers() && !row.isGuests()) {
            // members only always show lock icon
            types.add(new PriceMethod("", "o_ac_membersonly_icon", translate("cif.access.membersonly.short")));
        }
        if (!types.isEmpty()) {
            row.setAccessTypes(types);
        }

        forgeDetails(row);
        forgeMarkLink(row);
        forgeSelectLink(row);
        forgeCompletion(row, row.getRepositoryEntryCompletion());
    }

    private void forgeDetails(CurriculumTreeWithViewsRow row) {
        FormLink detailsLink = uifactory.addFormLink("details_" + (++counter), "details", "details", null, null, Link.LINK);
        detailsLink.setCustomEnabledLinkCSS("o_details");
        detailsLink.setUserObject(row);
        Long repoEntryKey = row.getRepositoryEntryKey();
        String detailsUrl = null;
        if(repoEntryKey != null) {
            String businessPath = "[RepositoryEntry:" + repoEntryKey + "][Infos:0]";
            detailsUrl = BusinessControlFactory.getInstance()
                    .getAuthenticatedURLFromBusinessPathString(businessPath);
            detailsLink.setUrl(detailsUrl);
        }
        row.setDetailsLink(detailsLink, detailsUrl);
    }

    private void forgeMarkLink(CurriculumTreeWithViewsRow row) {
        if (!guestOnly) {
            FormLink markLink = uifactory.addFormLink("mark_" + (++counter), "mark", "", null, null, Link.NONTRANSLATED);
            markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
            markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
            markLink.setUserObject(row);
            row.setMarkLink(markLink);
        }
    }

    private void forgeSelectLink(CurriculumTreeWithViewsRow row) {
        if (row.isCurriculumElementOnly()) return;

        String displayName = StringHelper.escapeHtml(row.getRepositoryEntryDisplayName());
        FormLink selectLink = uifactory.addFormLink("select_" + (++counter), "select", displayName, null, null, Link.NONTRANSLATED);
        if (row.isClosed()) {
            selectLink.setIconLeftCSS("o_icon o_CourseModule_icon_closed");
        }
        Long repoEntryKey = row.getRepositoryEntryKey();
        if (repoEntryKey != null) {
            String businessPath = "[RepositoryEntry:" + repoEntryKey + "]";
            selectLink.setUrl(BusinessControlFactory.getInstance()
                    .getAuthenticatedURLFromBusinessPathString(businessPath));
        }
        selectLink.setUserObject(row);
        row.setSelectLink(selectLink);
    }

    private void forgeCalendarsLink(CurriculumTreeWithViewsRow row) {
        if (row.isCalendarsEnabled()) {
            FormLink calendarLink = uifactory.addFormLink("cals_" + (++counter), "calendars", "calendars", null, null, Link.LINK);
            calendarLink.setIconLeftCSS("o_icon o_icon-fw o_icon_timetable");
            calendarLink.setUserObject(row);
            row.setCalendarsLink(calendarLink);
        }
    }

    private void forgeCurriculumCompletions(List<CurriculumTreeWithViewsRow> rows) {
    	Map<Long, Double> completions = loadCurriculumElementCompletions(rows);
    	if(!completions.isEmpty()) {
    		for (CurriculumTreeWithViewsRow row : rows) {
    			if (row.getCompletionItem() == null && row.getCurriculumElementKey() != null) { // does not show completion of the child entry
    				forgeCompletion(row, completions.get(row.getCurriculumElementKey()));
    			}
    		}
    	}
    }

    private void forgeCompletion(CurriculumTreeWithViewsRow row, Double completion) {
        if (completion != null) {
            ProgressBarItem completionItem = new ProgressBarItem("completion_" + row.getKey(), 100,
                    completion.floatValue(), Float.valueOf(1), null);
            completionItem.setWidthInPercent(true);
            completionItem.setLabelAlignment(LabelAlignment.none);
            completionItem.setRenderStyle(RenderStyle.radial);
            completionItem.setRenderSize(RenderSize.inline);
            completionItem.setBarColor(BarColor.success);
            row.setCompletionItem(completionItem);
        }
    }

    private void addRoot(List<CurriculumTreeWithViewsRow> rows, CurriculumTreeWithViewsRow parent) {
        rows.stream().filter(row -> row.getParent() == null).forEach(row -> {
            row.setParent(parent);
            if (row.hasStatement()) {
                parent.setHasStatement(true);
            }
        });
    }

    private Map<Long, Double> loadCurriculumElementCompletions(List<CurriculumTreeWithViewsRow> rows) {
        List<Long> curEleLearningProgressKeys = rows.stream()
                .filter(CurriculumTreeWithViewsRow::isLearningProgressEnabled)
                .map(CurriculumTreeWithViewsRow::getKey)
                .map(CurriculumKey::getCurriculumElement)
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
        List<AssessmentEntryCompletion> loadAvgCompletionsByCurriculumElements = assessmentService
                .loadAvgCompletionsByCurriculumElements(assessedIdentity, curEleLearningProgressKeys);
        Map<Long, Double> completions = new HashMap<>();
        for (AssessmentEntryCompletion completion : loadAvgCompletionsByCurriculumElements) {
            if (completion.getCompletion() != null) {
                completions.put(completion.getKey(), completion.getCompletion());
            }
        }
        return completions;
    }

    @Override
    protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
        //do not update the
    }
    
    @Override
    protected void event(UserRequest ureq, Controller source, Event event) {
    	if(uploadCertificateController == source) {
			if (event == Event.DONE_EVENT) {
				loadModel(ureq);
				tableEl.reset();
			}
			
			cmc.deactivate();
			cleanUp();
		}
    	
    	super.event(ureq, source, event);
    }
    
    private void cleanUp() {
		removeAsListenerAndDispose(uploadCertificateController);
		removeAsListenerAndDispose(cmc);
		uploadCertificateController = null;
		cmc = null;
	}

    @Override
    public void event(UserRequest ureq, Component source, Event event) {
        if (source == mainForm.getInitialComponent()) {
            if ("ONCLICK".equals(event.getCommand())) {
                String rowKeyStr = ureq.getParameter("select_row");
                if (StringHelper.isLong(rowKeyStr)) {
                    try {
                        Long rowKey = Long.valueOf(rowKeyStr);
                        List<CurriculumTreeWithViewsRow> rows = tableModel.getObjects();
                        for (CurriculumTreeWithViewsRow row : rows) {
                            if (row != null && row.getRepositoryEntryKey() != null && row.getRepositoryEntryKey().equals(rowKey)) {
                                if (row.isMember()) {
                                    doOpen(ureq, row, null);
                                } else {
                                    doOpenDetails(ureq, row);
                                }
                            }
                        }
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
			showUploadCertificateController(ureq);
		} else if (source instanceof FormLink) {
            FormLink link = (FormLink) source;
            if ("start".equals(link.getCmd())) {
                CurriculumTreeWithViewsRow row = (CurriculumTreeWithViewsRow) link.getUserObject();
                doOpen(ureq, row, null);
            } else if ("details".equals(link.getCmd())) {
                CurriculumTreeWithViewsRow row = (CurriculumTreeWithViewsRow) link.getUserObject();
                doOpenDetails(ureq, row);
            } else if ("select".equals(link.getCmd())) {
                CurriculumTreeWithViewsRow row = (CurriculumTreeWithViewsRow) link.getUserObject();
                if (row.isMember()) {
                    doOpen(ureq, row, null);
                } else {
                    doOpenDetails(ureq, row);
                }
            } else if ("mark".equals(link.getCmd())) {
                CurriculumTreeWithViewsRow row = (CurriculumTreeWithViewsRow) link.getUserObject();
                boolean marked = doMark(ureq, row);
                link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
                link.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
                link.getComponent().setDirty(true);
                row.setMarked(marked);
            } else if ("calendars".equals(link.getCmd())) {
                CurriculumTreeWithViewsRow row = (CurriculumTreeWithViewsRow) link.getUserObject();
                doOpenCalendars(ureq, row);
            }
        } else if (source == tableEl) {
            if (event instanceof SelectionEvent) {
                SelectionEvent se = (SelectionEvent) event;
                CurriculumTreeWithViewsRow row = tableModel.getObject(se.getIndex());
                if (row.isMember()) {
                    doOpen(ureq, row, null);
                } else {
                    doOpenDetails(ureq, row);
                }
            }
        }
        super.formInnerEvent(ureq, source, event);
    }
    
    private void showUploadCertificateController(UserRequest ureq) {
		if(guardModalController(uploadCertificateController)) return;
		
		uploadCertificateController = new UploadExternalCertificateController(ureq, getWindowControl(), assessedIdentity);
		listenTo(uploadCertificateController);
		
		cmc = new CloseableModalController(getWindowControl(), null, uploadCertificateController.getInitialComponent(), true, translate("upload.certificate"), true);
		cmc.addControllerListener(this);
		cmc.activate();
	}

    @Override
    protected void formOK(UserRequest ureq) {
        //
    }

    private void doOpen(UserRequest ureq, CurriculumTreeWithViewsRow row, String subPath) {
        try {
            String businessPath = "[RepositoryEntry:" + row.getRepositoryEntryKey() + "]";
            if (subPath != null) {
                businessPath += subPath;
            }
            NewControllerFactory.getInstance().launch(businessPath, ureq, getWindowControl());
        } catch (CorruptedCourseException e) {
            logError("Course corrupted: " + row.getKey() + " (" + row.getOlatResource().getResourceableId() + ")", e);
            showError("cif.error.corrupted");
        }
    }

    private void doOpenDetails(UserRequest ureq, CurriculumTreeWithViewsRow row) {
        // to be more consistent: course members see info page within the course, non-course members see it outside the course
        if (row.isMember()) {
            doOpen(ureq, row, "[Infos:0]");
        } else {
            removeAsListenerAndDispose(detailsCtrl);

            OLATResourceable ores = OresHelper.createOLATResourceableInstance("Infos", 0l);
            WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

            Long repoEntryKey = row.getRepositoryEntryKey();
            RepositoryEntry entry = repositoryService.loadByKey(repoEntryKey);
            if (repoEntryKey == null) {
                showInfo("curriculum.element.empty");
            } else if (entry == null) {
                showWarning("repositoryentry.not.existing");
            } else {
                detailsCtrl = new RepositoryEntryDetailsController(ureq, bwControl, entry, false);
                listenTo(detailsCtrl);
                addToHistory(ureq, detailsCtrl);

                String displayName = row.getRepositoryEntryDisplayName();
                stackPanel.pushController(displayName, detailsCtrl);
            }
        }
    }

    private void doOpenCalendars(UserRequest ureq, CurriculumTreeWithViewsRow row) {
        removeAsListenerAndDispose(calendarsCtrl);

        OLATResourceable ores = OresHelper.createOLATResourceableInstance("Calendars", row.getCurriculumElementKey());
        WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
        CurriculumElement element = curriculumService
                .getCurriculumElement(new CurriculumElementRefImpl(row.getCurriculumElementKey()));
        List<CurriculumTreeWithViewsRow> rows = tableModel.getObjects();

        Set<Long> entryKeys = new HashSet<>();
        for (CurriculumTreeWithView elementWithView : rows) {
            if (elementWithView.isCurriculumMember()
                    && !elementWithView.getEntries().isEmpty()
                    && elementWithView.isParentOrSelf(row)) {
                for (RepositoryEntryMyView view : elementWithView.getEntries()) {
                    if ("CourseModule".equals(view.getOlatResource().getResourceableTypeName())) {
                        entryKeys.add(view.getKey());
                    }
                }
            }
        }


        List<RepositoryEntry> entries = repositoryService.loadByKeys(entryKeys);
        calendarsCtrl = new CurriculumElementCalendarController(ureq, bwControl, element, entries, curriculumSecurityCallback);
        listenTo(calendarsCtrl);
        stackPanel.pushController(translate("calendars"), calendarsCtrl);
    }

    private boolean doMark(UserRequest ureq, CurriculumTreeWithViewsRow row) {
        OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getRepositoryEntryKey());
        RepositoryEntryRef ref = new RepositoryEntryRefImpl(row.getRepositoryEntryKey());
        if (markManager.isMarked(item, getIdentity(), null)) {
            markManager.removeMark(item, getIdentity(), null);

            EntryChangedEvent e = new EntryChangedEvent(ref, getIdentity(), Change.removeBookmark, "curriculum");
            ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
            return false;
        } else {
            String businessPath = "[RepositoryEntry:" + item.getResourceableId() + "]";
            markManager.setMark(item, getIdentity(), null, businessPath);

            EntryChangedEvent e = new EntryChangedEvent(ref, getIdentity(), Change.addBookmark, "curriculum");
            ureq.getUserSession().getSingleUserEventCenter().fireEventToListenersOf(e, RepositoryService.REPOSITORY_EVENT_ORES);
            return true;
        }
    }
}
