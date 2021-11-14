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
package org.olat.modules.coach.ui;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.olat.NewControllerFactory;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DateFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.PopEvent;
import org.olat.core.gui.components.stack.TooledController;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.dtabs.Activateable2;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.id.context.BusinessControl;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.id.context.StateEntry;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.ui.DownloadCertificateCellRenderer;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.modules.assessment.ui.ScoreCellRenderer;
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.IdentityResourceKey;
import org.olat.modules.coach.ui.EfficiencyStatementEntryTableDataModel.Columns;
import org.olat.modules.coach.ui.UserDetailsController.Segment;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Description:<br>
 * Overview of all students under the scrutiny of a coach.
 * 
 * <P>
 * Initial Date:  8 févr. 2012 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class GroupController extends FormBasicController implements Activateable2, GenericEventListener, TooledController {
	
	private final Link openGroup;
	private final TooledStackedPanel stackPanel;
	private Link nextGroup, detailsGroupCmp, previousGroup;
	
	private FlexiTableElement tableEl;
	private EfficiencyStatementEntryTableDataModel model;
	private UserDetailsController statementCtrl;

	private boolean hasChanged = false;
	
	private final int index;
	private final int numOfGroups;
	private final BusinessGroup group;
	private final GroupStatEntry entry;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private BusinessGroupService groupManager;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public GroupController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			GroupStatEntry groupStatistic, int index, int numOfGroups) {
		super(ureq, wControl, "group_view");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		boolean isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);
		
		this.index = index;
		this.entry = groupStatistic;
		this.stackPanel = stackPanel;
		this.numOfGroups = numOfGroups;
		group = groupManager.loadBusinessGroup(groupStatistic.getGroupKey());
		
		initForm(ureq);
		loadModel();

		openGroup = LinkFactory.createButton("open.group", flc.getFormItemComponent(), this);
		openGroup.setIconLeftCSS("o_icon o_icon_group");
		flc.getFormItemComponent().put("open", openGroup);

		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	public void initTools() {
		//next, previous group
		previousGroup = LinkFactory.createToolLink("previous.group", translate("previous.group"), this);
		previousGroup.setIconLeftCSS("o_icon o_icon_previous");
		previousGroup.setEnabled(numOfGroups > 1);
		stackPanel.addTool(previousGroup);
		
		String details = translate("students.details", new String[]{
				StringHelper.escapeHtml(group.getName()), Integer.toString(index + 1), Integer.toString(numOfGroups)
		});		
		detailsGroupCmp = LinkFactory.createToolLink("details.group", details, this);
		detailsGroupCmp.setIconLeftCSS("o_icon o_icon_group");
		stackPanel.addTool(detailsGroupCmp);
				
		nextGroup = LinkFactory.createToolLink("next.group", translate("next.group"), this);
		nextGroup.setIconLeftCSS("o_icon o_icon_next");
		nextGroup.setEnabled(numOfGroups > 1);
		stackPanel.addTool(nextGroup);
		stackPanel.addListener(this);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("groupName", StringHelper.escapeHtml(group.getName()));
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		int colIndex = UserListController.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(UserListController.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select",
					true, userPropertyHandler.i18nColumnDescriptorLabelKey()));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.repoName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.passed, new PassedCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.score, new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.certificate, new DownloadCertificateCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.recertification, new DateFlexiCellRenderer(getLocale())));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lastModification));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lastUserModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(false, Columns.lastCoachModified));
		
		model = new EfficiencyStatementEntryTableDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmptyTableSettings("default.tableEmptyMessage", null, "o_icon_group");
		tableEl.setAndLoadPersistedPreferences(ureq, "fGroupController-v2");
	}

	@Override
	protected void doDispose() {
		stackPanel.removeListener(this);
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.deregisterFor(this, CertificatesManager.ORES_CERTIFICATE_EVENT);
        super.doDispose();
	}

	@Override
	public void event(Event event) {
		if(event instanceof CertificateEvent) {
			CertificateEvent ce = (CertificateEvent)event;
			IdentityResourceKey key = new IdentityResourceKey(ce.getOwnerKey(), ce.getResourceKey());
			if(model.contains(key)) {
				updateCertificate(ce.getCertificateKey());
			}
		}
	}
	
	private void updateCertificate(Long certificateKey) {
		CertificateLight certificate = certificatesManager.getCertificateLightById(certificateKey);
		model.putCertificate(certificate);
	}
	
	public GroupStatEntry getEntry() {
		return entry;
	}
	
	private List<EfficiencyStatementEntry> loadModel() {
		List<EfficiencyStatementEntry> allGroup = coachingService.getGroup(group, userPropertyHandlers, getLocale());
		
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(group);
		ConcurrentMap<IdentityResourceKey, CertificateLight> certificateMap = new ConcurrentHashMap<>();
		for(CertificateLight certificate:certificates) {
			IdentityResourceKey key = new IdentityResourceKey(certificate.getIdentityKey(), certificate.getOlatResourceKey());
			certificateMap.put(key, certificate);
		}
		model.setObjects(allGroup, certificateMap);
		tableEl.reloadData();
		tableEl.reset();
		return allGroup;
	}
	
	private void reloadModel() {
		if(hasChanged) {
			loadModel();
			hasChanged = false;
		}
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				EfficiencyStatementEntry selectedRow = model.getObject(se.getIndex());
				if("select".equals(cmd)) {
					selectDetails(ureq, selectedRow);
				}
			}
		} 
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(source == openGroup) {
			openGroup(ureq);
		} else if(nextGroup == source) {
			fireEvent(ureq, event);
		} else if(previousGroup == source) {
			fireEvent(ureq, event);
		} else if(stackPanel == source) {
			if(event instanceof PopEvent) {
				PopEvent pe = (PopEvent)event;
				if(pe.getController() == statementCtrl && hasChanged) {
					reloadModel();
				}
			}
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (statementCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
				fireEvent(ureq, event);
			} else if ("previous".equals(event.getCommand())) {
				previousEntry(ureq);
			} else if ("next".equals(event.getCommand())) {
				nextEntry(ureq);
			} 
		}
		super.event(ureq, source, event);
	}
	
	@Override
	public void activate(UserRequest ureq, List<ContextEntry> entries, StateEntry state) {
		if(entries == null || entries.isEmpty()) return;
		
		ContextEntry ce = entries.get(0);
		OLATResourceable ores = ce.getOLATResourceable();
		if("Identity".equals(ores.getResourceableTypeName())) {
			Long identityKey = ores.getResourceableId();
			for(EfficiencyStatementEntry row:model.getObjects()) {
				if(identityKey.equals(row.getIdentityKey())) {
					selectDetails(ureq, row);
					statementCtrl.activate(ureq, entries.subList(1, entries.size()), ce.getTransientState());
					break;
				}
			}
		}
	}
	
	private void previousEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int previousIndex = model.getObjects().indexOf(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= model.getRowCount()) {
			previousIndex = model.getRowCount() - 1;
		}
		EfficiencyStatementEntry previousEntry = model.getObject(previousIndex);
		selectDetails(ureq, previousEntry);
	}
	
	private void nextEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int nextIndex = model.getObjects().indexOf(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= model.getRowCount()) {
			nextIndex = 0;
		}
		EfficiencyStatementEntry nextEntry = model.getObject(nextIndex);
		selectDetails(ureq, nextEntry);
	}
	
	private void selectDetails(UserRequest ureq, EfficiencyStatementEntry statementEntry) {
		Segment selectedTool = null;
		if(statementCtrl != null) {
			selectedTool = statementCtrl.getSelectedSegment();
		}

		int entryIndex = model.getObjects().indexOf(statementEntry) + 1;
		Identity assessedIdentity = securityManager.loadIdentityByKey(statementEntry.getIdentityKey());
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, statementEntry.getIdentityKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);

		String fullname = userManager.getUserDisplayName(assessedIdentity);
		String displayName = statementEntry.getCourseDisplayName();
		String display =fullname + " (" + displayName + ")";
		String details = translate("students.details", new String[] {
				display, String.valueOf(entryIndex), String.valueOf(model.getRowCount())
		});

		statementCtrl = new UserDetailsController(ureq, bwControl, stackPanel,
				statementEntry, assessedIdentity, details, entryIndex, model.getRowCount(), selectedTool, true, false);
		listenTo(statementCtrl);
		stackPanel.popUpToController(this);
		stackPanel.pushController(display, statementCtrl);
	}
	
	private void openGroup(UserRequest ureq) {
		List<ContextEntry> ces = new ArrayList<>(4);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("BusinessGroup", entry.getGroupKey());
		ces.add(BusinessControlFactory.getInstance().createContextEntry(ores));

		BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
}
