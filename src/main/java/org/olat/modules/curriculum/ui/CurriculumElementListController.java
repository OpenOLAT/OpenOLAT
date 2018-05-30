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
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.olat.NewControllerFactory;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Roles;
import org.olat.course.CorruptedCourseException;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumRef;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRepositoryEntryViews;
import org.olat.modules.curriculum.ui.CurriculumElementDataModel.ElementCols;
import org.olat.modules.curriculum.ui.CurriculumElementWithViewsDataModel.ElementViewCols;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryMyView;
import org.olat.repository.RepositoryService;
import org.olat.repository.ui.PriceMethod;
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
 * folllow the same rules as {@link org.olat.repository.ui.list.RepositoryEntryListController}<br>
 * 
 * 
 * Initial date: 11 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementListController extends FormBasicController {
	
	private FlexiTableElement tableEl;
	private CurriculumElementWithViewsDataModel tableModel;
	
	private int counter;
	private final boolean guestOnly;
	private final CurriculumRef curriculum;
	
	@Autowired
	private ACService acService;
	@Autowired
	private AccessControlModule acModule;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumService curriculumService;
	
	public CurriculumElementListController(UserRequest ureq, WindowControl wControl, CurriculumRef curriculum) {
		super(ureq, wControl, "curriculum_element_list");
		this.curriculum = curriculum;
		guestOnly = ureq.getUserSession().getRoles().isGuestOnly();
		
		initForm(ureq);
		loadModel(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, ElementCols.key));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.elementDisplayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.elementIdentifier));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.entryDisplayName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ElementViewCols.start));
		
		tableModel = new CurriculumElementWithViewsDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setCustomizeColumns(true);
		tableEl.setEmtpyTableMessageKey("table.curriculum.empty");
		tableEl.setAndLoadPersistedPreferences(ureq, "cur-curriculum-manage");
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
			
			if(elementWithViews.getEntries() == null || elementWithViews.getEntries().isEmpty()) {
				rows.add(new CurriculumElementWithViewsRow(element));
			} else if(elementWithViews.getEntries().size() == 1) {
				CurriculumElementWithViewsRow row = new CurriculumElementWithViewsRow(element, elementWithViews.getEntries().get(0));
				forge(row, repoKeys, resourcesWithOffer);
				rows.add(row);
			} else {
				rows.add(new CurriculumElementWithViewsRow(element));
				for(RepositoryEntryMyView entry:elementWithViews.getEntries()) {
					CurriculumElementWithViewsRow row = new CurriculumElementWithViewsRow(element, entry);
					forge(row, repoKeys, resourcesWithOffer);
					rows.add(row);
				}
			}
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private void forge(CurriculumElementWithViewsRow row, Collection<Long> repoKeys, List<OLATResourceAccess> resourcesWithOffer) {
		if(row.getRepositoryEntryKey() == null) return;
			
		List<PriceMethod> types = new ArrayList<>();
		if (row.isMembersOnly()) {
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
	}
	
	private void forgeStartLink(CurriculumElementWithViewsRow row) {
		String label;
		boolean isStart = true;
		if(!row.isMembersOnly() && row.getAccessTypes() != null && !row.getAccessTypes().isEmpty() && !row.isMember()) {
			if(guestOnly) {
				if(row.getAccess() == RepositoryEntry.ACC_USERS_GUESTS) {
					label = "start";
				} else {
					return;
				}
			} else {
				label = "book";
				isStart = false;
			}
		} else {
			label = "start";
		}
		FormLink startLink = uifactory.addFormLink("start_" + (++counter), "start", label, null, null, Link.LINK);
		startLink.setUserObject(row);
		startLink.setCustomEnabledLinkCSS(isStart ? "o_start btn-block" : "o_book btn-block");
		startLink.setIconRightCSS("o_icon o_icon_start");
		row.setStartLink(startLink);
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
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if("start".equals(link.getCmd())) {
				CurriculumElementWithViewsRow row = (CurriculumElementWithViewsRow)link.getUserObject();
				doOpen(ureq, row, null);
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
}
