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
package org.olat.modules.curriculum.ui;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import org.olat.NewControllerFactory;
import org.olat.core.commons.services.mark.Mark;
import org.olat.core.commons.services.mark.MarkManager;
import org.olat.core.dispatcher.mapper.MapperService;
import org.olat.core.dispatcher.mapper.manager.MapperKey;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponentDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableCssDelegate;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableRendererType;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.stack.BreadcrumbPanel;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.CorruptedCourseException;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementMembership;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.curriculum.ui.CurriculumElementWithViewsDataModel.ElementViewCols;
import org.olat.modules.curriculum.ui.component.CurriculumElementCompositeRenderer;
import org.olat.modules.curriculum.ui.component.CurriculumElementIndentRenderer;
import org.olat.modules.curriculum.ui.component.CurriculumElementViewsRowComparator;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.repository.controllers.EntryChangedEvent;
import org.olat.repository.controllers.EntryChangedEvent.Change;
import org.olat.repository.model.RepositoryEntryRefImpl;
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
 * 
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementListController extends FormBasicController implements FlexiTableCssDelegate, FlexiTableComponentDelegate {
	
	private FlexiTableElement tableEl;
	private CurriculumElementWithViewsDataModel tableModel;
	private final BreadcrumbPanel stackPanel;
	
	private int counter;
	private final boolean guestOnly;
	private final CurriculumRef curriculum;
	private final MapperKey mapperThumbnailKey;
	
	private RepositoryEntryDetailsController detailsCtrl;
	
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
	
	public CurriculumElementListController(UserRequest ureq, WindowControl wControl, BreadcrumbPanel stackPanel, CurriculumRef curriculum) {
		super(ureq, wControl, "curriculum_element_list", Util.createPackageTranslator(RepositoryService.class, ureq.getLocale()));
		this.curriculum = curriculum;
		this.stackPanel = stackPanel;
		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		mapperThumbnailKey = mapperService.register(null, "repositoryentryImage", new RepositoryEntryImageMapper());
		
		initForm(ureq);
		loadModel(ureq);
	}
	
	public CurriculumRef getCurriculum() {
		return curriculum;
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementViewCols.key));
		DefaultFlexiColumnModel elementNameCol = new DefaultFlexiColumnModel(ElementViewCols.elementDisplayName, "select");
		elementNameCol.setCellRenderer(new CurriculumElementCompositeRenderer("select", new CurriculumElementIndentRenderer()));
		columnsModel.addFlexiColumnModel(elementNameCol);
		DefaultFlexiColumnModel elementIdentifierCol = new DefaultFlexiColumnModel(ElementViewCols.elementIdentifier, "select");
		elementIdentifierCol.setCellRenderer(new CurriculumElementCompositeRenderer("select", new TextFlexiCellRenderer()));
		columnsModel.addFlexiColumnModel(elementIdentifierCol);
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.entryDisplayName, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.entryExternalRef, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementViewCols.select));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.mark));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.details));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.start));
		
		tableModel = new CurriculumElementWithViewsDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setAvailableRendererTypes(FlexiTableRendererType.custom, FlexiTableRendererType.classic);
		tableEl.setRendererType(FlexiTableRendererType.custom);
		tableEl.setElementCssClass("o_curriculumtable");
		tableEl.setCustomizeColumns(true);
		tableEl.setEmtpyTableMessageKey("table.curriculum.empty");
		tableEl.setCssDelegate(this);
		
		VelocityContainer row = createVelocityContainer("curriculum_element_row");
		row.setDomReplacementWrapperRequired(false); // sets its own DOM id in velocity container
		tableEl.setRowRenderer(row, this);
		
		tableEl.setAndLoadPersistedPreferences(ureq, "my-curriculum-elements-" + curriculum.getKey());
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
		CurriculumElementWithViewsRow rowWithView = tableModel.getObject(pos);
		if(type == FlexiTableRendererType.custom) {
			sb.append("o_table_row row ");
	
			if(rowWithView.isCurriculumElementOnly()) {
				sb.append("o_curriculum_element");
			} else if(rowWithView.isRepositoryEntryOnly()) {
				sb.append("o_repository_entry");
			} else if(rowWithView.isCurriculumElementWithEntry()) {
				sb.append("o_mixed_element");
			}
		}
		
		int count = 0;
		for(CurriculumElementWithViewsRow parent=rowWithView.getParent(); parent != null; parent=parent.getParent()) {
			count++;
		}
		if(rowWithView.isRepositoryEntryOnly()) {
			count++;
		}
		sb.append(" o_curriculum_element_l").append(count);
		return sb.toString();
	}

	@Override
	public Iterable<Component> getComponents(int row, Object rowObject) {
		return null;
	}

	private void loadModel(UserRequest ureq) {
		Roles roles = ureq.getUserSession().getRoles();
		List<CurriculumElementRepositoryEntryViews> elementsWithViews = curriculumService.getCurriculumElements(getIdentity(), roles, curriculum);
		
		Set<Long> repoKeys = new HashSet<>(elementsWithViews.size() * 3);
		List<OLATResource> resourcesWithAC = new ArrayList<>(elementsWithViews.size() * 3);
		for(CurriculumElementRepositoryEntryViews elementWithViews:elementsWithViews) {
			for(RepositoryEntryMyView entry:elementWithViews.getEntries()) {
				repoKeys.add(entry.getKey());
				if(entry.isValidOfferAvailable()) {
					resourcesWithAC.add(entry.getOlatResource());
				}
			}
		}
		List<OLATResourceAccess> resourcesWithOffer = acService.filterResourceWithAC(resourcesWithAC);
		repositoryService.filterMembership(getIdentity(), repoKeys);

		List<CurriculumElementWithViewsRow> rows = new ArrayList<>(elementsWithViews.size() * 3);
		for(CurriculumElementRepositoryEntryViews elementWithViews:elementsWithViews) {
			CurriculumElement element = elementWithViews.getCurriculumElement();
			CurriculumElementMembership elementMembership = elementWithViews.getCurriculumMembership();
			
			if(elementWithViews.getEntries() == null || elementWithViews.getEntries().isEmpty()) {
				rows.add(new CurriculumElementWithViewsRow(element, elementMembership));
			} else if(elementWithViews.getEntries().size() == 1) {
				CurriculumElementWithViewsRow row = new CurriculumElementWithViewsRow(element, elementMembership, elementWithViews.getEntries().get(0), true);
				forge(row, repoKeys, resourcesWithOffer);
				rows.add(row);
			} else {
				rows.add(new CurriculumElementWithViewsRow(element, elementMembership));
				for(RepositoryEntryMyView entry:elementWithViews.getEntries()) {
					CurriculumElementWithViewsRow row = new CurriculumElementWithViewsRow(element, elementMembership, entry, false);
					forge(row, repoKeys, resourcesWithOffer);
					rows.add(row);
				}
			}
		}

		Map<Long,CurriculumElementWithViewsRow> keyToRow = rows.stream()
				.collect(Collectors.toMap(CurriculumElementWithViewsRow::getKey, row -> row, (row1, row2) -> row1));
		rows.forEach(row -> {
			row.setParent(keyToRow.get(row.getParentKey()));
			if(row.getOlatResource() != null) {
				VFSLeaf image = repositoryManager.getImage(row.getRepositoryEntryResourceable());
				if(image != null) {
					row.setThumbnailRelPath(mapperThumbnailKey.getUrl() + "/" + image.getName());
				}
			}
		});
		
		Collections.sort(rows, new CurriculumElementViewsRowComparator(getLocale()));
		
		removeByPermissions(rows);
		removeDeactivated(rows);

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void removeDeactivated(List<CurriculumElementWithViewsRow> rows) {
		for(Iterator<CurriculumElementWithViewsRow> it=rows.iterator(); it.hasNext(); ) {
			CurriculumElementWithViewsRow row = it.next();
			for(CurriculumElementWithViewsRow parent=row; parent.getParent() != null; parent=parent.getParent()) {
				if(!parent.isActive()) {
					it.remove();
					break;
				}
			}
		}
	}
	
	private void removeByPermissions(List<CurriculumElementWithViewsRow> rows) {
		// propagate the member marker along the parent line
		for(CurriculumElementWithViewsRow row:rows) {
			if(row.isCurriculumMember()) {
				for(CurriculumElementWithViewsRow parentRow=row.getParent(); parentRow != null; parentRow=parentRow.getParent()) {
					parentRow.setCurriculumMember(true);
				}
			}
		}
		
		// trim part of the tree without member flag
		for(Iterator<CurriculumElementWithViewsRow> it=rows.iterator(); it.hasNext(); ) {
			if(!it.next().isCurriculumMember()) {
				it.remove();
			}
		}
	}
	
	private void forge(CurriculumElementWithViewsRow row, Collection<Long> repoKeys, List<OLATResourceAccess> resourcesWithOffer) {
		if(row.getRepositoryEntryKey() == null) return;
			
		List<PriceMethod> types = new ArrayList<>();
		if (!row.isAllUsers() && !row.isGuests()) {
			// members only always show lock icon
			types.add(new PriceMethod("", "o_ac_membersonly_icon", translate("cif.access.membersonly.short")));
		} else {
			// collect access control method icons
			OLATResource resource = row.getOlatResource();
			for(OLATResourceAccess resourceAccess:resourcesWithOffer) {
				if(resource.getKey().equals(resourceAccess.getResource().getKey())) {
					for(PriceMethodBundle bundle:resourceAccess.getMethods()) {
						String type = (bundle.getMethod().getMethodCssClass() + "_icon").intern();
						String price = bundle.getPrice() == null || bundle.getPrice().isEmpty() ? "" : PriceFormat.fullFormat(bundle.getPrice());
						AccessMethodHandler amh = acModule.getAccessMethodHandler(bundle.getMethod().getType());
						String displayName = amh.getMethodName(getLocale());
						types.add(new PriceMethod(price, type, displayName));
					}
				}
			}
		}
		
		row.setMember(repoKeys.contains(row.getRepositoryEntryKey()));
		
		if(!types.isEmpty()) {
			row.setAccessTypes(types);
		}
		
		forgeStartLink(row);
		forgeDetails(row);
		forgeMarkLink(row);
		forgeSelectLink(row);
	}
	
	private void forgeDetails(CurriculumElementWithViewsRow row) {
		FormLink detailsLink = uifactory.addFormLink("details_" + (++counter), "details", "details", null, null, Link.LINK);
		detailsLink.setCustomEnabledLinkCSS("o_details");
		detailsLink.setUserObject(row);
		row.setDetailsLink(detailsLink);
	}
	
	private void forgeStartLink(CurriculumElementWithViewsRow row) {
		String label;
		boolean isStart = true;
		if((row.isAllUsers() || row.isGuests()) && row.getAccessTypes() != null && !row.getAccessTypes().isEmpty() && !row.isMember()) {
			/*if(guestOnly) {
				if(row.getAccess() == RepositoryEntry.ACC_USERS_GUESTS) {
					label = "start";
				} else {
					return;
				}
			} else {*/ //TODO repo access
				label = "book";
				isStart = false;
			//}
		} else {
			label = "start";
		}
		FormLink startLink = uifactory.addFormLink("start_" + (++counter), "start", label, null, null, Link.LINK);
		startLink.setUserObject(row);
		startLink.setCustomEnabledLinkCSS(isStart ? "o_start btn-block" : "o_book btn-block");
		startLink.setIconRightCSS("o_icon o_icon_start");
		row.setStartLink(startLink);
	}
	
	private void forgeMarkLink(CurriculumElementWithViewsRow row) {
		if(!guestOnly) {
			FormLink markLink = uifactory.addFormLink("mark_" + (++counter), "mark", "", null, null, Link.NONTRANSLATED);
			markLink.setIconLeftCSS(row.isMarked() ? Mark.MARK_CSS_LARGE : Mark.MARK_ADD_CSS_LARGE);
			markLink.setTitle(translate(row.isMarked() ? "details.bookmark.remove" : "details.bookmark"));
			markLink.setUserObject(row);
			row.setMarkLink(markLink);
		}
	}
	
	private void forgeSelectLink(CurriculumElementWithViewsRow row) {
		if(row.isCurriculumElementOnly()) return;
		
		String displayName = StringHelper.escapeHtml(row.getRepositoryEntryDisplayName());
		FormLink selectLink = uifactory.addFormLink("select_" + (++counter), "select", displayName, null, null, Link.NONTRANSLATED);
		if(row.isClosed()) {
			selectLink.setIconLeftCSS("o_icon o_CourseModule_icon_closed");
		}
		selectLink.setUserObject(row);
		row.setSelectLink(selectLink);
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void propagateDirtinessToContainer(FormItem fiSrc, FormEvent event) {
		//do not update the 
	}
	
	
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == mainForm.getInitialComponent()) {
			if("ONCLICK".equals(event.getCommand())) {
				String rowKeyStr = ureq.getParameter("select_row");
				if(StringHelper.isLong(rowKeyStr)) {
					try {
						Long rowKey = new Long(rowKeyStr);
						List<CurriculumElementWithViewsRow> rows = tableModel.getObjects();
						for(CurriculumElementWithViewsRow row:rows) {
							if(row != null && row.getRepositoryEntryKey() != null  && row.getRepositoryEntryKey().equals(rowKey)) {
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
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("start".equals(link.getCmd())) {
				CurriculumElementWithViewsRow row = (CurriculumElementWithViewsRow)link.getUserObject();
				doOpen(ureq, row, null);
			} else if("details".equals(link.getCmd())) {
				CurriculumElementWithViewsRow row = (CurriculumElementWithViewsRow)link.getUserObject();
				doOpenDetails(ureq, row);
			} else if ("select".equals(link.getCmd())) {
				CurriculumElementWithViewsRow row = (CurriculumElementWithViewsRow)link.getUserObject();
				if (row.isMember()) {
					doOpen(ureq, row, null);					
				} else {
					doOpenDetails(ureq, row);
				}
			} else if("mark".equals(link.getCmd())) {
				CurriculumElementWithViewsRow row = (CurriculumElementWithViewsRow)link.getUserObject();
				boolean marked = doMark(ureq, row);
				link.setIconLeftCSS(marked ? "o_icon o_icon_bookmark o_icon-lg" : "o_icon o_icon_bookmark_add o_icon-lg");
				link.setTitle(translate(marked ? "details.bookmark.remove" : "details.bookmark"));
				link.getComponent().setDirty(true);
				row.setMarked(marked);
			}
		} else if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				CurriculumElementWithViewsRow row = tableModel.getObject(se.getIndex());
				if (row.isMember()) {
					doOpen(ureq, row, null);					
				} else {
					doOpenDetails(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpen(UserRequest ureq, CurriculumElementWithViewsRow row, String subPath) {
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
	
	private void doOpenDetails(UserRequest ureq, CurriculumElementWithViewsRow row) {
		// to be more consistent: course members see info page within the course, non-course members see it outside the course
		if (row.isMember()) {
			doOpen(ureq, row, "[Infos:0]");
		} else {
			removeAsListenerAndDispose(detailsCtrl);
			
			OLATResourceable ores = OresHelper.createOLATResourceableInstance("Infos", 0l);
			WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(ores, null, getWindowControl());

			RepositoryEntry entry = repositoryService.loadByKey(row.getRepositoryEntryKey());
			if(entry == null) {
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
	
	private boolean doMark(UserRequest ureq, CurriculumElementWithViewsRow row) {
		OLATResourceable item = OresHelper.createOLATResourceableInstance("RepositoryEntry", row.getRepositoryEntryKey());
		RepositoryEntryRef ref = new RepositoryEntryRefImpl(row.getRepositoryEntryKey());
		if(markManager.isMarked(item, getIdentity(), null)) {
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
