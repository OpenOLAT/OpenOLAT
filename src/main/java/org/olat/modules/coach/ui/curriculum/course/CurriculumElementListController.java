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
package org.olat.modules.coach.ui.curriculum.course;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableExtendedFilter;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableFilterValue;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TreeNodeFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.filter.FlexiTableMultiSelectionFilter;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTab;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiFiltersTabFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.TabSelectionBehavior;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelAlignment;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBarItem;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.util.SelectionValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.CorruptedCourseException;
import org.olat.modules.assessment.AssessmentEntryCompletion;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.ui.AssessedIdentityListController;
import org.olat.modules.coach.RoleSecurityCallback;
import org.olat.modules.coach.ui.curriculum.course.CurriculumElementWithViewsDataModel.ElementViewCols;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementWithView;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.curriculum.ui.CurriculumElementCalendarController;
import org.olat.modules.curriculum.ui.CurriculumListController;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.list.RepositoryEntryDetailsController;
import org.olat.repository.ui.list.RepositoryEntryInfosController;
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
public class CurriculumElementListController extends FormBasicController implements FlexiTableCssDelegate, Activateable2 {
	
	public static CurriculumElementStatus[] VISIBLE_STATUS = {
		CurriculumElementStatus.preparation, CurriculumElementStatus.provisional,
		CurriculumElementStatus.confirmed, CurriculumElementStatus.active,
		CurriculumElementStatus.cancelled, CurriculumElementStatus.finished 
	};

	private static final String ALL_TAB = "All";
	private static final String RELEVANT_TAB = "Relevant";
	private static final String FINISHED_TAB = "Finished";
	
	static final String FILTER_STATUS = "Status";
	
	private FlexiFiltersTab allTab;
	private FlexiFiltersTab relevantTab;
	private FlexiFiltersTab finishedTab;
	
    private FlexiTableElement tableEl;
    private CurriculumElementWithViewsDataModel tableModel;
    private final BreadcrumbPanel stackPanel;

    private int counter;
    private final List<CurriculumRef> curriculumRefList;

    private final Identity assessedIdentity;
    private final boolean implementationsOnly;
    private final CurriculumElement implementation;
    private final CurriculumSecurityCallback curriculumSecurityCallback;
    private final RoleSecurityCallback roleSecurityCallback;

    private RepositoryEntryDetailsController detailsCtrl;
    private CurriculumElementCalendarController calendarsCtrl;
    private CurriculumElementListController curriculumElementListCtrl;

    @Autowired
    private RepositoryService repositoryService;
    @Autowired
    private CurriculumService curriculumService;
    @Autowired
    private AssessmentService assessmentService;
    @Autowired
    private BaseSecurity securityManager;

    public CurriculumElementListController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel,
    			Identity assessedIdentity, List<CurriculumRef> curriculumRefList, CurriculumElementRef implementation,
    			CurriculumSecurityCallback curriculumSecurityCallback, RoleSecurityCallback roleSecurityCallback,
    			boolean implementationsOnly) {
        super(ureq, wControl, "curriculum_element_list");
        setTranslator(Util.createPackageTranslator(CurriculumListController.class, ureq.getLocale(), getTranslator()));
        setTranslator(Util.createPackageTranslator(RepositoryService.class, ureq.getLocale(), getTranslator()));
        setTranslator(Util.createPackageTranslator(AssessedIdentityListController.class, ureq.getLocale(), getTranslator()));
        
        this.implementationsOnly = implementationsOnly;
        this.implementation = implementation == null
        		? null
        		: curriculumService.getCurriculumElement(implementation);
        this.curriculumRefList = implementation == null
        		? curriculumRefList
        		: List.of(this.implementation.getCurriculum());

        this.stackPanel = stackPanel;
        this.curriculumSecurityCallback = curriculumSecurityCallback;
        this.roleSecurityCallback = roleSecurityCallback;
        this.assessedIdentity = assessedIdentity;

        initForm(ureq);
		tableEl.setSelectedFilterTab(ureq, allTab);
        loadModel();
    }

    @Override
    protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
        FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

        columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementViewCols.key));

        if(implementationsOnly) {
        	columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.displayName, "select"));
        } else {
        	TreeNodeFlexiCellRenderer displayNameRenderer = new TreeNodeFlexiCellRenderer("select");
        	displayNameRenderer.setFlatBySort(true);
        	columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.displayName, displayNameRenderer));
        }

        DefaultFlexiColumnModel elementIdentifierCol = new DefaultFlexiColumnModel(ElementViewCols.identifier, "select");
        elementIdentifierCol.setCellRenderer(new CurriculumElementCompositeRenderer("select", new TextFlexiCellRenderer()));
        columnsModel.addFlexiColumnModel(elementIdentifierCol);

        if (roleSecurityCallback.canViewCourseProgressAndStatus()) {
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.completion));
        }
        if (roleSecurityCallback.canViewCalendar()) {
            columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.calendars));
        }

        tableModel = new CurriculumElementWithViewsDataModel(columnsModel, getLocale());
        tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 50, false, getTranslator(), formLayout);
        tableEl.setElementCssClass("o_curriculumtable");
        tableEl.setCustomizeColumns(true);
        tableEl.setEmptyTableMessageKey("table.curriculum.empty");
        tableEl.setCssDelegate(this);
        // Don't persist preferences, persisted sort kill the tree representation
        
        initFilters();
        initFilterPresets();
    }
    
	private void initFilters() {
		List<FlexiTableExtendedFilter> filters = new ArrayList<>();
		
		SelectionValues statusValues = new SelectionValues();
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.preparation.name(), translate("filter.preparation")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.provisional.name(), translate("filter.provisional")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.confirmed.name(), translate("filter.confirmed")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.active.name(), translate("filter.active")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.finished.name(), translate("filter.finished")));
		statusValues.add(SelectionValues.entry(CurriculumElementStatus.cancelled.name(), translate("filter.cancelled")));
		FlexiTableMultiSelectionFilter statusFilter = new FlexiTableMultiSelectionFilter(translate("filter.status"),
				FILTER_STATUS, statusValues, true);
		filters.add(statusFilter);
		
		tableEl.setFilters(true, filters, false, false);
	}
    
	private void initFilterPresets() {
		List<FlexiFiltersTab> tabs = new ArrayList<>();
		
		allTab = FlexiFiltersTabFactory.tabWithImplicitFilters(ALL_TAB, translate("filter.all"),
				TabSelectionBehavior.nothing, List.of());
		tabs.add(allTab);
		
		relevantTab = FlexiFiltersTabFactory.tabWithImplicitFilters(RELEVANT_TAB, translate("filter.relevant"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						List.of(CurriculumElementStatus.preparation.name(), CurriculumElementStatus.provisional.name(),
								CurriculumElementStatus.confirmed.name(), CurriculumElementStatus.active.name()))));
		tabs.add(relevantTab);
		
		finishedTab = FlexiFiltersTabFactory.tabWithImplicitFilters(FINISHED_TAB, translate("filter.finished"),
				TabSelectionBehavior.nothing, List.of(FlexiTableFilterValue.valueOf(FILTER_STATUS,
						List.of(CurriculumElementStatus.finished.name()))));
		tabs.add(finishedTab);

		tableEl.setFilterTabs(true, tabs);
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
        CourseCurriculumTreeWithViewsRow rowWithView = tableModel.getObject(pos);
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

        int count = 0;
        for (CourseCurriculumTreeWithViewsRow parent = rowWithView.getParent(); parent != null; parent = parent.getParent()) {
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

        return sb.toString();
    }

    private void loadModel() {
    	Roles roles = securityManager.getRoles(assessedIdentity);
        List<CurriculumElementRepositoryEntryViews> elementsWithViewsForAll = curriculumService
        		.getCurriculumElements(assessedIdentity, roles, curriculumRefList, VISIBLE_STATUS);
        Map<Long, List<CurriculumElementRepositoryEntryViews>> elementsMap = elementsWithViewsForAll.stream()
        		.collect(Collectors.groupingBy(row -> row.getCurriculumElement().getCurriculum().getKey(), Collectors.toList()));

        List<CourseCurriculumTreeWithViewsRow> allRows = new ArrayList<>();
        for (CurriculumRef curriculumRef : curriculumRefList) {
            List<CurriculumElementRepositoryEntryViews> elementsWithViews = elementsMap.get(curriculumRef.getKey());
            if (elementsWithViews == null || elementsWithViews.isEmpty()) continue;
            
            List<CourseCurriculumTreeWithViewsRow> rows = new ArrayList<>();

            Set<Long> repoKeys = new HashSet<>();
            for (CurriculumElementRepositoryEntryViews elementWithViews : elementsWithViews) {
                for (RepositoryEntryMyView entry : elementWithViews.getEntries()) {
                    repoKeys.add(entry.getKey());
                }
            }
            repositoryService.filterMembership(assessedIdentity, repoKeys);
            
            Map<Long,CourseCurriculumTreeWithViewsRow> potentialParentRows = new HashMap<>();

            for (CurriculumElementRepositoryEntryViews elementWithViews : elementsWithViews) {
                CurriculumElement element = elementWithViews.getCurriculumElement();
                CurriculumElementMembership elementMembership = elementWithViews.getCurriculumMembership();

                if (elementWithViews.getEntries() == null || elementWithViews.getEntries().isEmpty()) {
                    CourseCurriculumTreeWithViewsRow row = new CourseCurriculumTreeWithViewsRow(element, elementMembership, 0);
                    forgeCalendarsLink(row);
                    rows.add(row);
                    potentialParentRows.put(elementWithViews.getKey(), row);
                } else if (elementWithViews.getEntries().size() == 1) {
                    CourseCurriculumTreeWithViewsRow row = new CourseCurriculumTreeWithViewsRow(element, elementMembership, elementWithViews.getEntries().get(0), true);
                    forge(row, repoKeys);
                    forgeCalendarsLink(row);
                    rows.add(row);
                    potentialParentRows.put(elementWithViews.getKey(), row);
                } else {
                    CourseCurriculumTreeWithViewsRow elementRow = new CourseCurriculumTreeWithViewsRow(element, elementMembership, elementWithViews.getEntries().size());
                    forgeCalendarsLink(elementRow);
                    rows.add(elementRow);
                    potentialParentRows.put(elementWithViews.getKey(), elementRow);
                    for (RepositoryEntryMyView entry : elementWithViews.getEntries()) {
                        CourseCurriculumTreeWithViewsRow row = new CourseCurriculumTreeWithViewsRow(element, elementMembership, entry, false);
                        row.setParent(elementRow);
                        forge(row, repoKeys);
                        rows.add(row);
                    }
                }
            }

            rows.forEach(row -> {
            	if(row.getParent() == null) {
            		if(row.getCurriculumElementParentKey() != null) {
            			row.setParent(potentialParentRows.get(row.getCurriculumElementParentKey()));
            		} 
            	}
            });

            removeByPermissions(rows);
            forgeCurriculumCompletions(rows);
            if(implementationsOnly) {
            	removeNotImplementations(rows);
            } else if(implementation != null) {
            	removeNotInImplementationParentLine(rows, implementation);
            }
            allRows.addAll(rows);
        }
        
        Collections.sort(allRows, new CurriculumElementViewsRowComparator(getLocale()));
        
        tableModel.setObjects(allRows);
        tableEl.reset(true, true, true);
    }
    
	private void removeNotInImplementationParentLine(List<CourseCurriculumTreeWithViewsRow> rows, CurriculumElement element) {
		for (Iterator<CourseCurriculumTreeWithViewsRow> it = rows.iterator(); it.hasNext(); ) {
			if (!hasParentLine(it.next(), element)) {
				it.remove();
			}
		}
	}
	
	private boolean hasParentLine(CourseCurriculumTreeWithViewsRow row, CurriculumElement element) {
		for(CourseCurriculumTreeWithViewsRow el=row; el != null; el=el.getParent()) {
			if(el.getCurriculumElementKey() != null && element.getKey().equals(el.getCurriculumElementKey())) {
				return true;
			}
		}
		return false;
	}
    
	/**
	 * Filter to keep only implementations. Remove sub-elements, courses only and
	 * single course implementation.
	 * 
	 * @param rows
	 */
	private void removeNotImplementations(List<CourseCurriculumTreeWithViewsRow> rows) {
		for (Iterator<CourseCurriculumTreeWithViewsRow> it = rows.iterator(); it.hasNext(); ) {
			CourseCurriculumTreeWithViewsRow row = it.next();
			if (row.getCurriculumElementParentKey() != null
					|| row.isRepositoryEntryOnly()
					|| row.isSingleCourseImplementation()) {
				it.remove();
			} 
		}
	}

    private void removeByPermissions(List<CourseCurriculumTreeWithViewsRow> rows) {
        // propagate the member marker along the parent line
        for (CourseCurriculumTreeWithViewsRow row : rows) {
            if (row.isCurriculumMember()) {
                for (CourseCurriculumTreeWithViewsRow parentRow = row.getParent(); parentRow != null; parentRow = parentRow.getParent()) {
                    parentRow.setCurriculumMember(true);
                }
            }
        }

        // trim part of the tree without member flag
        for (Iterator<CourseCurriculumTreeWithViewsRow> it = rows.iterator(); it.hasNext(); ) {
            if (!it.next().isCurriculumMember()) {
                it.remove();
            }
        }
    }

    private void forge(CourseCurriculumTreeWithViewsRow row, Collection<Long> repoKeys) {
        if (row.getRepositoryEntryKey() != null)  {
        	row.setMember(repoKeys.contains(row.getRepositoryEntryKey()));
        	forgeCompletion(row, row.getRepositoryEntryCompletion());
        }
    }

    private void forgeCalendarsLink(CourseCurriculumTreeWithViewsRow row) {
        if (row.isCalendarsEnabled()) {
            FormLink calendarLink = uifactory.addFormLink("cals_" + (++counter), "calendars", "calendars", null, null, Link.LINK);
            calendarLink.setIconLeftCSS("o_icon o_icon-fw o_icon_calendar");
            calendarLink.setUserObject(row);
            row.setCalendarsLink(calendarLink);
        }
    }

    private void forgeCurriculumCompletions(List<CourseCurriculumTreeWithViewsRow> rows) {
        Map<Long, Double> completions = loadCurriculumElementCompletions(rows);

        for (CourseCurriculumTreeWithViewsRow row : rows) {
            if (row.getCompletionItem() == null) { // does not show completion of the child entry
                forgeCompletion(row, completions.get(row.getKey()));
            }
        }
    }

	private void forgeCompletion(CourseCurriculumTreeWithViewsRow row, Double completion) {
		if (completion != null) {
			ProgressBarItem completionItem = new ProgressBarItem("completion_" + row.getKey(), 100,
					completion.floatValue(), Float.valueOf(1), null);
			completionItem.setWidthInPercent(true);
			completionItem.setLabelAlignment(LabelAlignment.none);
			completionItem.setRenderStyle(RenderStyle.radial);
			completionItem.setRenderSize(RenderSize.inline);
			completionItem.setBarColor(BarColor.neutral);
			row.setCompletionItem(completionItem);
			row.setCompletion(completion);
		}
	}

    private Map<Long, Double> loadCurriculumElementCompletions(List<CourseCurriculumTreeWithViewsRow> rows) {
        List<Long> curEleLearningProgressKeys = rows.stream()
                .filter(CourseCurriculumTreeWithViewsRow::isLearningProgressEnabled)
                .map(CourseCurriculumTreeWithViewsRow::getKey)
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
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
    	if(entries == null || entries.isEmpty()) return;
    	
		ContextEntry entry = entries.get(0);
		String type = entry.getOLATResourceable().getResourceableTypeName();
		if("CurriculumElement".equalsIgnoreCase(type)) {
			CourseCurriculumTreeWithViewsRow row = tableModel.getObjectByKey(entry.getOLATResourceable().getResourceableId());
			if(row != null) {
            	doSelect(ureq, row);
			}
		}
	}

	@Override
    protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
        //do not update the
    }

    @Override
    protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
        if (source instanceof FormLink) {
            FormLink link = (FormLink) source;
            if ("select".equals(link.getCmd())
            		&& link.getUserObject() instanceof CourseCurriculumTreeWithViewsRow row) {
            	doSelect(ureq, row);
            } else if ("calendars".equals(link.getCmd())
            		&& link.getUserObject() instanceof CourseCurriculumTreeWithViewsRow row) {
                doOpenCalendars(ureq, row);
            }
        } else if (source == tableEl) {
            if (event instanceof SelectionEvent se) {
                CourseCurriculumTreeWithViewsRow row = tableModel.getObject(se.getIndex());
                doSelect(ureq, row);
            } else if(event instanceof FlexiTableFilterTabEvent) {
            	tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
            	tableEl.reset(true, true, true);
            }
        }
        super.formInnerEvent(ureq, source, event);
    }

	@Override
	protected void formOK(UserRequest ureq) {
    	//
	}

	private void doSelect(UserRequest ureq, CourseCurriculumTreeWithViewsRow row) {
		if(implementationsOnly && row.getCurriculumElementParentKey() == null) {
			doOpenCurriculumElement(ureq, row);
		} else if (row.isMember()) {
			doOpen(ureq, row, null);
		} else {
			doOpenDetails(ureq, row);
		}
	}

    private void doOpen(UserRequest ureq, CourseCurriculumTreeWithViewsRow row, String subPath) {
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

    private void doOpenDetails(UserRequest ureq, CourseCurriculumTreeWithViewsRow row) {
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
                detailsCtrl = new RepositoryEntryInfosController(ureq, bwControl, entry, false);
                listenTo(detailsCtrl);
                addToHistory(ureq, detailsCtrl);

                String displayName = row.getRepositoryEntryDisplayName();
                stackPanel.pushController(displayName, detailsCtrl);
            }
        }
    }
    
    private void doOpenCurriculumElement(UserRequest ureq, CourseCurriculumTreeWithViewsRow row) {
    	CurriculumElement curriculumElement = curriculumService.getCurriculumElement(row);
    	List<CurriculumRef> curriculumRef = List.of(curriculumElement.getCurriculum());

    	WindowControl bwControl = addToHistory(ureq, OresHelper.createOLATResourceableInstance(CourseListWrapperController.CMD_IMPLEMENTATION, row.getKey()), null);
		curriculumElementListCtrl = new CurriculumElementListController(ureq, bwControl, stackPanel, assessedIdentity, curriculumRef, curriculumElement,
				curriculumSecurityCallback, roleSecurityCallback, false);
		listenTo(curriculumElementListCtrl);
		
		stackPanel.pushController(curriculumElement.getDisplayName(), curriculumElementListCtrl);
    }

    private void doOpenCalendars(UserRequest ureq, CourseCurriculumTreeWithViewsRow row) {
        removeAsListenerAndDispose(calendarsCtrl);

        OLATResourceable ores = OresHelper.createOLATResourceableInstance("Calendars", row.getCurriculumElementKey());
        WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());
        CurriculumElement element = curriculumService
                .getCurriculumElement(new CurriculumElementRefImpl(row.getCurriculumElementKey()));
        List<CourseCurriculumTreeWithViewsRow> rows = tableModel.getObjects();

        Set<Long> entryKeys = new HashSet<>();
        for (CurriculumElementWithView elementWithView : rows) {
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
}
