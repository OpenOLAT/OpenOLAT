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
package org.olat.course.assessment.ui.tool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.stack.TooledStackedPanel;
import org.olat.core.gui.components.stack.TooledStackedPanel.Align;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.course.assessment.AssessmentMainController;
import org.olat.course.assessment.AssessmentToolManager;
import org.olat.course.assessment.bulk.PassedCellRenderer;
import org.olat.course.assessment.model.SearchAssessedIdentityParams;
import org.olat.course.assessment.ui.tool.AssessmentIdentitiesCourseNodeTableModel.IdentityCourseElementCols;
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 06.10.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class AssessmentIdentitiesCourseNodeController extends FormBasicController {
	
	
	
	private final CourseNode courseNode;
	private final RepositoryEntry courseEntry;
	private final boolean isAdministrativeUser;
	private final List<UserPropertyHandler> userPropertyHandlers;
	private final AssessmentToolSecurityCallback assessmentCallback;
	

	private Link nextLink, previousLink;
	private FlexiTableElement tableEl;
	private final TooledStackedPanel stackPanel;
	private AssessmentIdentitiesCourseNodeTableModel usersTableModel;
	
	private AssessmentIdentityCourseNodeController currentIdentityCtrl;
	
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule securityModule;
	@Autowired
	private AssessmentToolManager assessmentToolManager;
	
	public AssessmentIdentitiesCourseNodeController(UserRequest ureq, WindowControl wControl, TooledStackedPanel stackPanel,
			RepositoryEntry courseEntry, CourseNode courseNode, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "identity_courseelement");
		setTranslator(Util.createPackageTranslator(AssessmentMainController.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.courseNode = courseNode;
		this.stackPanel = stackPanel;
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		
		isAdministrativeUser = securityModule.isUserAllowedAdminProps(ureq.getUserSession().getRoles());
		userPropertyHandlers = userManager.getUserPropertyHandlersFor(AssessmentToolConstants.usageIdentifyer, isAdministrativeUser);
		
		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		if(formLayout instanceof FormLayoutContainer) {
			FormLayoutContainer layoutCont = (FormLayoutContainer)formLayout;
			layoutCont.contextPut("courseNodeTitle", courseNode.getShortTitle());
			layoutCont.contextPut("courseNodeCssClass", CourseNodeFactory.getInstance().getCourseNodeConfiguration(courseNode.getType()).getIconCSSClass());
		}

		//add the table
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		if(isAdministrativeUser) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.username, "select"));
		}
		
		int colIndex = AssessmentToolConstants.USER_PROPS_OFFSET;
		for (int i = 0; i < userPropertyHandlers.size(); i++) {
			UserPropertyHandler userPropertyHandler	= userPropertyHandlers.get(i);
			boolean visible = UserManager.getInstance().isMandatoryUserProperty(AssessmentToolConstants.usageIdentifyer , userPropertyHandler);
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(visible, userPropertyHandler.i18nColumnDescriptorLabelKey(), colIndex++, "select", false, null));
		}
		AssessableCourseNode assessableNode = null;
		if(courseNode instanceof AssessableCourseNode) {
			assessableNode = (AssessableCourseNode)courseNode;
			
			if(assessableNode.hasAttemptsConfigured()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.attempts, "select"));
			}
			if(assessableNode.hasScoreConfigured()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.min, "select"));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.max, "select"));
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.score, "select"));
			}
			if(assessableNode.hasPassedConfigured()) {
				columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.passed, new PassedCellRenderer()));
			}
		}

		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.assessmentStatus, new AssessmentStatusCellRenderer()));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.initialLaunchDate, "select"));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(IdentityCourseElementCols.lastScoreUpdate, "select"));

		usersTableModel = new AssessmentIdentitiesCourseNodeTableModel(columnsModel, assessableNode); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", usersTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setExportEnabled(true);
	}
	
	private void updateModel() {
		RepositoryEntry referenceEntry = null;
		if(courseNode.needsReferenceToARepositoryEntry()) {
			referenceEntry = courseNode.getReferencedRepositoryEntry();
		}
		SearchAssessedIdentityParams params = new SearchAssessedIdentityParams(courseEntry, referenceEntry, courseNode.getIdent(), assessmentCallback);
		List<Identity> assessedIdentities = assessmentToolManager.getAssessedIdentities(getIdentity(), params);
		List<AssessmentEntry> assessmentEntries = assessmentToolManager.getAssessmentEntries(getIdentity(), params, null);
		Map<Long,AssessmentEntry> entryMap = new HashMap<>();
		assessmentEntries.forEach((entry) -> entryMap.put(entry.getIdentity().getKey(), entry));

		List<AssessedIdentityCourseElementRow> rows = new ArrayList<>(assessedIdentities.size());
		for(Identity assessedIdentity:assessedIdentities) {
			AssessmentEntry entry = entryMap.get(assessedIdentity.getKey());
			rows.add(new AssessedIdentityCourseElementRow(assessedIdentity, entry, userPropertyHandlers, getLocale()));
		}
		usersTableModel.setObjects(rows);
		tableEl.reloadData();
	}

	@Override
	protected void doDispose() {
		//
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	@Override
	public void event(UserRequest ureq, Component source, Event event) {
		if(previousLink == source) {
			doPrevious(ureq);
		} else if(nextLink == source) {
			doNext(ureq);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessedIdentityCourseElementRow row = usersTableModel.getObject(se.getIndex());
				if("select".equals(cmd)) {
					doSelect(ureq, row);
				}
			}
		}
		
		super.formInnerEvent(ureq, source, event);
	}
	
	private void doNext(UserRequest ureq) {
		stackPanel.popController(currentIdentityCtrl);
		
		Identity currentIdentity = currentIdentityCtrl.getAssessedIdentity();
		int index = getIndexOf(currentIdentity);
		if(index >= 0) {
			int nextIndex = index + 1;//next
			if(nextIndex >= 0 && nextIndex < usersTableModel.getRowCount()) {
				doSelect(ureq, usersTableModel.getObject(nextIndex));
			} else if(usersTableModel.getRowCount() > 0) {
				doSelect(ureq, usersTableModel.getObject(0));
			}
		}
	}
	
	private void doPrevious(UserRequest ureq) {
		stackPanel.popController(currentIdentityCtrl);
		
		Identity currentIdentity = currentIdentityCtrl.getAssessedIdentity();
		int index = getIndexOf(currentIdentity);
		if(index >= 0) {
			int previousIndex = index - 1;//next
			if(previousIndex >= 0 && previousIndex < usersTableModel.getRowCount()) {
				doSelect(ureq, usersTableModel.getObject(previousIndex));
			} else if(usersTableModel.getRowCount() > 0) {
				doSelect(ureq, usersTableModel.getObject(usersTableModel.getRowCount() - 1));
			}
		}
	}
	
	private int getIndexOf(Identity identity) {
		int index = -1;
		for(int i=usersTableModel.getRowCount(); i-->0; ) {
			Long rowIdentityKey = usersTableModel.getObject(i).getIdentityKey();
			if(rowIdentityKey.equals(identity.getKey())) {
				return i;
			}
		}
		return index;
	}
	
	private void doSelect(UserRequest ureq, AssessedIdentityCourseElementRow row) {
		removeAsListenerAndDispose(currentIdentityCtrl);
		
		Identity assessedIdentity = securityManager.loadIdentityByKey(row.getIdentityKey());
		String fullName = userManager.getUserDisplayName(assessedIdentity);
		
		currentIdentityCtrl = new AssessmentIdentityCourseNodeController(ureq, getWindowControl(), assessedIdentity);
		listenTo(currentIdentityCtrl);
		stackPanel.pushController(fullName, currentIdentityCtrl);
		
		previousLink = LinkFactory.createToolLink("previouselement","", this, "o_icon_previous_toolbar");
		previousLink.setTitle(translate("command.previous"));
		stackPanel.addTool(previousLink, Align.rightEdge, false, "o_tool_previous");
		nextLink = LinkFactory.createToolLink("nextelement","", this, "o_icon_next_toolbar");
		nextLink.setTitle(translate("command.next"));
		stackPanel.addTool(nextLink, Align.rightEdge, false, "o_tool_next");
	}
}
