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
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.table.BooleanColumnDescriptor;
import org.olat.core.gui.components.table.ColumnDescriptor;
import org.olat.core.gui.components.table.CustomRenderColumnDescriptor;
import org.olat.core.gui.components.table.DefaultColumnDescriptor;
import org.olat.core.gui.components.table.TableController;
import org.olat.core.gui.components.table.TableEvent;
import org.olat.core.gui.components.table.TableGuiConfiguration;
import org.olat.core.gui.components.text.TextComponent;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
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
public class GroupController extends BasicController implements Activateable2, GenericEventListener {
	
	private final Link backLink, next, previous;
	private final Link nextGroup, previousGroup;
	private final Link openGroup;
	private final TextComponent detailsCmp, detailsGroupCmp;
	private final TableController tableCtr;
	private final VelocityContainer mainVC;
	private final VelocityContainer groupDetailsVC;
	private EfficiencyStatementEntryTableDataModel model;

	private final ToolbarController toolbar;
	private EfficiencyStatementDetailsController statementCtrl;

	private boolean hasChanged = false;
	
	private final BusinessGroup group;
	private final GroupStatEntry entry;
	@Autowired
	private CoachingService coachingService;
	@Autowired
	private BusinessGroupService groupManager;
	@Autowired
	private CertificatesManager certificatesManager;
	
	public GroupController(UserRequest ureq, WindowControl wControl, GroupStatEntry groupStatistic, int index, int numOfGroups) {
		super(ureq, wControl);
		
		this.entry = groupStatistic;

		TableGuiConfiguration tableConfig = new TableGuiConfiguration();
		tableConfig.setTableEmptyMessage(translate("error.no.found"));
		tableConfig.setDownloadOffered(true);
		tableConfig.setPreferencesOffered(true, "groupController");
		
		tableCtr = new TableController(tableConfig, ureq, getWindowControl(),  null, null, null, null, true, getTranslator());
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("student.name", Columns.studentName.ordinal(), "select", getLocale()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.course.name", Columns.repoName.ordinal(), "select", getLocale()));
		tableCtr.addColumnDescriptor(new BooleanColumnDescriptor("table.header.passed", Columns.passed.ordinal(), translate("passed.true"), translate("passed.false")));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.score", Columns.score.ordinal(),"select", getLocale(),
				ColumnDescriptor.ALIGNMENT_RIGHT, new ScoreCellRenderer()));
		tableCtr.addColumnDescriptor(new CustomRenderColumnDescriptor("table.header.certificate", Columns.certificate.ordinal(), null, getLocale(),
				ColumnDescriptor.ALIGNMENT_LEFT, new DownloadCertificateCellRenderer()));
		tableCtr.addColumnDescriptor(new DefaultColumnDescriptor("table.header.lastScoreDate", Columns.lastModification.ordinal(), "select", getLocale()));
		listenTo(tableCtr);

		group = groupManager.loadBusinessGroup(groupStatistic.getGroupKey());
		List<EfficiencyStatementEntry> allGroup = loadModel();
		
		mainVC = createVelocityContainer("group_view");
		
		toolbar = new ToolbarController(ureq, wControl, getTranslator());
		listenTo(toolbar);
		
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


		groupDetailsVC = createVelocityContainer("group_details");
		groupDetailsVC.contextPut("groupName", StringHelper.escapeHtml(group.getName()));
		
		openGroup = LinkFactory.createButton("open.group", groupDetailsVC, this);
		openGroup.setIconLeftCSS("o_icon o_icon_group");
		groupDetailsVC.put("open", openGroup);

		mainVC.put("toolbar", toolbar.getInitialComponent());
		mainVC.put("groupDetails", groupDetailsVC);
		mainVC.put("groupsTable", tableCtr.getInitialComponent());

		setDetailsToolbarVisible(false);
		putInitialPanel(mainVC);

		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.registerFor(this, getIdentity(), CertificatesManager.ORES_CERTIFICATE_EVENT);
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
		List<EfficiencyStatementEntry> allGroup = coachingService.getGroup(group);
		
		List<CertificateLight> certificates = certificatesManager.getLastCertificates(group);
		ConcurrentMap<IdentityResourceKey, CertificateLight> certificateMap = new ConcurrentHashMap<>();
		for(CertificateLight certificate:certificates) {
			IdentityResourceKey key = new IdentityResourceKey(certificate.getIdentityKey(), certificate.getOlatResourceKey());
			certificateMap.put(key, certificate);
		}
		model = new EfficiencyStatementEntryTableDataModel(allGroup, certificateMap);
		tableCtr.setTableDataModel(model);
		return allGroup;
	}
	
	private void reloadModel() {
		if(hasChanged) {
			loadModel();
			hasChanged = false;
		}
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
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
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(source == tableCtr) {
			if(event instanceof TableEvent) {
				TableEvent e = (TableEvent) event;
				if("select".equals(e.getActionId())) {
					EfficiencyStatementEntry row = (EfficiencyStatementEntry)tableCtr.getTableDataModel().getObject(e.getRowId());
					selectDetails(ureq, row);
				}
			}
		} else if (source == toolbar) {
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
			for(int i=tableCtr.getRowCount(); i-->0; ) {
				EfficiencyStatementEntry row = (EfficiencyStatementEntry)tableCtr.getTableDataModel().getObject(i);
				if(identityKey.equals(row.getStudentKey())) {
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
		int previousIndex = tableCtr.getIndexOfSortedObject(currentEntry) - 1;
		if(previousIndex < 0 || previousIndex >= tableCtr.getRowCount()) {
			previousIndex = tableCtr.getRowCount() - 1;
		}
		EfficiencyStatementEntry previousEntry = (EfficiencyStatementEntry)tableCtr.getSortedObjectAt(previousIndex);
		selectDetails(ureq, previousEntry);
	}
	
	private void nextEntry(UserRequest ureq) {
		EfficiencyStatementEntry currentEntry = statementCtrl.getEntry();
		int nextIndex = tableCtr.getIndexOfSortedObject(currentEntry) + 1;
		if(nextIndex < 0 || nextIndex >= tableCtr.getRowCount()) {
			nextIndex = 0;
		}
		EfficiencyStatementEntry nextEntry = (EfficiencyStatementEntry)tableCtr.getSortedObjectAt(nextIndex);
		selectDetails(ureq, nextEntry);
	}
	
	private void removeDetails(UserRequest ureq) {
		mainVC.remove(statementCtrl.getInitialComponent());	
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
		
		OLATResourceable ores = OresHelper.createOLATResourceableInstance(Identity.class, statementEntry.getStudentKey());
		WindowControl bwControl = addToHistory(ureq, ores, null);
		statementCtrl = new EfficiencyStatementDetailsController(ureq, bwControl, statementEntry, selectAssessmentTool);
		listenTo(statementCtrl);
		mainVC.put("efficiencyDetails", statementCtrl.getInitialComponent());
		
		int index = tableCtr.getIndexOfSortedObject(statementEntry) + 1;
		String details = translate("students.details", new String[]{
				StringHelper.escapeHtml(statementEntry.getStudentFullName()), String.valueOf(index), String.valueOf(tableCtr.getRowCount())
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
