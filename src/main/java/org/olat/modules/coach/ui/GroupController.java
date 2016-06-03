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
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.text.TextComponent;
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
import org.olat.modules.coach.CoachingService;
import org.olat.modules.coach.model.EfficiencyStatementEntry;
import org.olat.modules.coach.model.GroupStatEntry;
import org.olat.modules.coach.model.IdentityResourceKey;
import org.olat.modules.coach.ui.EfficiencyStatementEntryTableDataModel.Columns;
import org.olat.modules.coach.ui.ToolbarController.Position;
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
public class GroupController extends FormBasicController implements Activateable2, GenericEventListener {
	
	private final Link backLink, next, previous;
	private final Link nextGroup, previousGroup;
	private final Link openGroup;
	private final TextComponent detailsCmp, detailsGroupCmp;
	
	private FlexiTableElement tableEl;
	private EfficiencyStatementEntryTableDataModel model;

	private final ToolbarController toolbar;
	private EfficiencyStatementDetailsController statementCtrl;

	private boolean hasChanged = false;
	
	private final BusinessGroup group;
	private final GroupStatEntry entry;

	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;

	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private BusinessGroupService groupManager;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public GroupController(UserRequest ureq, WindowControl wControl, GroupStatEntry groupStatistic, int index, int numOfGroups) {
		super(ureq, wControl, "group_view");
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(UserListController.usageIdentifyer, isAdministrativeUser);
		
		this.entry = groupStatistic;
		group = groupManager.loadBusinessGroup(groupStatistic.getGroupKey());
		
		initForm(ureq);

		List<EfficiencyStatementEntry> allGroup = loadModel();

		toolbar = new ToolbarController(ureq, wControl, getTranslator());
		listenTo(toolbar);
		flc.getFormItemComponent().put("toolbar", toolbar.getInitialComponent());
		
		backLink = toolbar.addToolbarLink("back", this, Position.left);
		backLink.setIconLeftCSS("o_icon o_icon_back");

		//next/previous student
		previous = toolbar.addToolbarLink("previous", this, Position.center);
		previous.setIconLeftCSS("o_icon o_icon_move_left");
		previous.setCustomDisabledLinkCSS("navbar-text");
		previous.setEnabled(allGroup.size() > 1);
		
		detailsCmp = toolbar.addToolbarText("", this, Position.center);
	
		next = toolbar.addToolbarLink("next", this, Position.center);
		next.setIconRightCSS("o_icon o_icon_move_right");
		next.setCustomDisabledLinkCSS("navbar-text");
		next.setEnabled(allGroup.size() > 1);
		//next/previous group
		//students next,previous
		previousGroup = toolbar.addToolbarLink("previous.group", this, Position.center);
		previousGroup.setIconLeftCSS("o_icon o_icon_move_left");
		previousGroup.setCustomDisabledLinkCSS("navbar-text");
		previousGroup.setEnabled(numOfGroups > 1);
		
		detailsGroupCmp = toolbar.addToolbarText("details.group", "", this, Position.center);
		detailsGroupCmp.setCssClass("navbar-text");
		detailsGroupCmp.setText(translate("students.details", new String[]{
				StringHelper.escapeHtml(group.getName()), Integer.toString(index + 1), Integer.toString(numOfGroups)
		}));
		nextGroup = toolbar.addToolbarLink("next.group", this, Position.center);
		nextGroup.setIconRightCSS("o_icon o_icon_move_right");
		nextGroup.setCustomDisabledLinkCSS("navbar-text");
		nextGroup.setEnabled(numOfGroups > 1);

		openGroup = LinkFactory.createButton("open.group", flc.getFormItemComponent(), this);
		openGroup.setIconLeftCSS("o_icon o_icon_group");
		flc.getFormItemComponent().put("open", openGroup);

		setDetailsToolbarVisible(false);

		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {

		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("groupName", StringHelper.escapeHtml(group.getName()));
		}
		
		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.name, "select"));
		}
		
		int colIndex = UserListController.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = userManager.isMandatoryUserProperty(UserListController.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select", true, null));
		}
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.repoName));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.passed, new PassedCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.certificate, new DownloadCertificateCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.score, new ScoreCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(Columns.lastModification));
		
		model = new EfficiencyStatementEntryTableDataModel(columnsModel);
		tableEl = uifactory.addTableElement(getWindowControl(), "table", model, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
		tableEl.setEmtpyTableMessageKey("error.no.found");
		tableEl.setAndLoadPersistedPreferences(ureq, "fGroupController");
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
		if (source == next) {
			nextEntry(ureq);
		} else if (source == previous) {
			previousEntry(ureq);
		} else if(source == backLink) {
			reloadModel();
			back(ureq);
		} else if(source == openGroup) {
			openGroup(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == toolbar) {
			if("back".equals(event.getCommand())) {
				reloadModel();
				back(ureq);
			} else if ("previous".equals(event.getCommand())) {
				previousEntry(ureq);
			} else if ("next".equals(event.getCommand())) {
				nextEntry(ureq);
			} else if ("next.group".equals(event.getCommand())) {
				fireEvent(ureq, event);
			} else if ("previous.group".equals(event.getCommand())) {
				fireEvent(ureq, event);
			}
		} else if (statementCtrl == source) {
			if(event == Event.CHANGED_EVENT) {
				hasChanged = true;
				fireEvent(ureq, event);
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
	
	private void setDetailsToolbarVisible(boolean visible) {
		next.setVisible(visible);
		previous.setVisible(visible);
		detailsCmp.setVisible(visible);
		
		nextGroup.setVisible(!visible);
		previousGroup.setVisible(!visible);
		detailsGroupCmp.setVisible(!visible);
	}
	
	private void back(UserRequest ureq) {
		if(statementCtrl == null) {
			fireEvent(ureq, Event.BACK_EVENT);
		} else {
			removeDetails(ureq);
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
	
	private void removeDetails(UserRequest ureq) {
		flc.getFormItemComponent().remove(statementCtrl.getInitialComponent());	
		removeAsListenerAndDispose(statementCtrl);
		statementCtrl = null;
		setDetailsToolbarVisible(false);
		addToHistory(ureq);
	}
	
	private void selectDetails(UserRequest ureq, EfficiencyStatementEntry statementEntry) {
		boolean selectAssessmentTool = false;
		if(statementCtrl != null) {
			selectAssessmentTool = statementCtrl.isAssessmentToolSelected();
			removeAsListenerAndDispose(statementCtrl);
		}
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, statementEntry.getIdentityKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		statementCtrl = new EfficiencyStatementDetailsController(ureq, bwControl, statementEntry, selectAssessmentTool);
		listenTo(statementCtrl);
		flc.getFormItemComponent().put("efficiencyDetails", statementCtrl.getInitialComponent());
		
		int index = model.getObjects().indexOf(statementEntry) + 1;
		String details = translate("students.details", new String[]{
				StringHelper.escapeHtml(statementEntry.getIdentityKey().toString()),//TODO user props
				String.valueOf(index), String.valueOf(model.getRowCount())
		});
		detailsCmp.setText(details);
		setDetailsToolbarVisible(true);
	}
	
	private void openGroup(UserRequest ureq) {
		List<ContextEntry> ces = new ArrayList<ContextEntry>(4);
		OLATResourceable ores = OresHelper.createOLATResourceableInstance("BusinessGroup", entry.getGroupKey());
		ces.add(BusinessControlFactory.getInstance().createContextEntry(ores));

		BusinessControl bc = BusinessControlFactory.getInstance().createFromContextEntries(ces);
		WindowControl bwControl = BusinessControlFactory.getInstance().createBusinessWindowControl(bc, getWindowControl());
		NewControllerFactory.getInstance().launch(ureq, bwControl);
	}
}
