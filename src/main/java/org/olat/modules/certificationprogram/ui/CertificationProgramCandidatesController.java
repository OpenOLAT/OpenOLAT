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
package org.olat.modules.certificationprogram.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.admin.user.UserTableDataModel;
import org.olat.basesecurity.BaseSecurityModule;
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
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableSearchEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.tab.FlexiTableFilterTabEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.modules.certificationprogram.CertificationProgramService;
import org.olat.modules.certificationprogram.model.CertificationProgramCandidate;
import org.olat.modules.certificationprogram.model.CertificationProgramMemberSearchParameters;
import org.olat.modules.certificationprogram.ui.CertificationProgramCandidatesTableModel.CertificationProgramCandidatesCols;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 25 nov. 2025<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class CertificationProgramCandidatesController extends FormBasicController implements Activateable2 {
	
	public static final int USER_PROPS_OFFSET = 500;
	public static final String usageIdentifyer = UserTableDataModel.class.getCanonicalName();
	
	private int count = 0;
	private FlexiTableElement tableEl;
	private CertificationProgramCandidatesTableModel tableModel;
	
	private final CertificationProgram certificationProgram;
	private final List<UserPropertyHandler> userPropertyHandlers;
	
	private CloseableCalloutWindowController calloutCtrl;
	private CurriculumElementsCalloutController elementsCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CertificationProgramService certificationProgramService;
	
	public CertificationProgramCandidatesController(UserRequest ureq, WindowControl wControl, CertificationProgram certificationProgram) {
		super(ureq, wControl, "members_list");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		this.certificationProgram = certificationProgram;
		
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();

		int colIndex = USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(),
					colIndex, null, true, "userProp-" + colIndex));
			colIndex++;
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(CertificationProgramCandidatesCols.elements));
		
		tableModel = new CertificationProgramCandidatesTableModel(columnsModel, getLocale()); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setSelectAllEnable(true);
		tableEl.setMultiSelect(true);
		tableEl.setSearchEnabled(true);
	}
	
	private void loadModel() {
		CertificationProgramMemberSearchParameters searchParams = new CertificationProgramMemberSearchParameters(certificationProgram);
		List<CertificationProgramCandidate> candidates = certificationProgramService.getCandidates(searchParams);
		
		Map<Identity,CertificationProgramCandidateRow> rowsMap = new HashMap<>();
		List<CertificationProgramCandidateRow> rows = new ArrayList<>();
		for(CertificationProgramCandidate candidate:candidates) {
			final Identity participant = candidate.identity();
			
			CertificationProgramCandidateRow row;
			if(rowsMap.containsKey(participant)) {
				row = rowsMap.get(participant);
			} else {
				row = forgeRow(participant);
				rows.add(row);
			}
			CurriculumElement element = candidate.element();
			row.getCurriculumElements().add(element);
		}
		
		for(CertificationProgramCandidateRow row:rows) {
			if(row.getCurriculumElements().size() > 1) {
				row.getElementsLink().setLinkTitle(Integer.toString(row.getCurriculumElements().size()));
			}
		}

		tableModel.setObjects(rows);
		tableEl.reset(true, true, true);
	}
	
	private CertificationProgramCandidateRow forgeRow(Identity participant) {
		FormLink elementsLink = uifactory.addFormLink("elements_" + (++count), "details", "1", null, flc, Link.LINK | Link.NONTRANSLATED);
		CertificationProgramCandidateRow row = new CertificationProgramCandidateRow(participant, elementsLink, userPropertyHandlers, getLocale());
		elementsLink.setUserObject(row);
		return row;
	}
	
	private void filterModel() {
		tableModel.filter(tableEl.getQuickSearchString(), tableEl.getFilters());
		tableEl.reset(true, true, true);
	}

	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(elementsCtrl == source) {
			calloutCtrl.deactivate();
			cleanUp();
		} else if(calloutCtrl == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(elementsCtrl);
		removeControllerListener(calloutCtrl);
		elementsCtrl = null;
		calloutCtrl = null;
	}
	
	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof FlexiTableSearchEvent || event instanceof FlexiTableFilterTabEvent) {
				filterModel();
			}
		} else if(source instanceof FormLink link && link.getUserObject() instanceof CertificationProgramCandidateRow row) {
			doOpenElements(ureq, link, row);
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doOpenElements(UserRequest ureq, FormLink link, CertificationProgramCandidateRow row) {
		elementsCtrl = new CurriculumElementsCalloutController(ureq, getWindowControl(), row.getCurriculumElements());
		listenTo(elementsCtrl);
		
		String title = translate("certification.program.memberships");
		calloutCtrl = new CloseableCalloutWindowController(ureq, getWindowControl(),
				elementsCtrl.getInitialComponent(), link.getFormDispatchId(), title, true, "");
		listenTo(calloutCtrl);
		calloutCtrl.activate();
	}
}
