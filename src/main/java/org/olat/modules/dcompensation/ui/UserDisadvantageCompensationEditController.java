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
package org.olat.modules.dcompensation.ui;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.DateChooser;
import org.olat.core.gui.components.form.flexible.elements.FormLink;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.components.form.flexible.elements.TextElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.FormLayoutContainer;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.util.KeyValues;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.iq.QTI21IdentityListCourseNodeToolsController.AssessmentTestSessionDetailsComparator;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.model.jpa.AssessmentTestSessionStatistics;
import org.olat.modules.dcompensation.DisadvantageCompensation;
import org.olat.modules.dcompensation.DisadvantageCompensationAuditLog.Action;
import org.olat.modules.dcompensation.DisadvantageCompensationService;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.controllers.ReferencableEntriesSearchController;
import org.olat.repository.controllers.RepositorySearchController.Can;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 22 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class UserDisadvantageCompensationEditController extends FormBasicController {

	private DateChooser approvalEl;
	private TextElement extraTimeEl;
	private TextElement approvedByEl;
	private FormLink selectEntryButton;
	private SingleSelection elementEl;
	private FormLayoutContainer selectRepositoryEntryLayout;

	private RepositoryEntry entry;
	private Identity disadvantagedIdentity;
	private DisadvantageCompensation compensation;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private DisadvantageCompensationService disadvantageCompensationService;
	
	private CloseableModalController cmc;
	private ReferencableEntriesSearchController searchFormCtrl;

	public UserDisadvantageCompensationEditController(UserRequest ureq, WindowControl wControl, DisadvantageCompensation compensation) {
		super(ureq, wControl);
		this.compensation = compensation;
		this.entry = compensation.getEntry();
		this.disadvantagedIdentity = compensation.getIdentity();
		initForm(ureq);
	}
	
	public UserDisadvantageCompensationEditController(UserRequest ureq, WindowControl wControl, Identity disadvantagedIdentity) {
		super(ureq, wControl);
		this.disadvantagedIdentity = disadvantagedIdentity;
		initForm(ureq);
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		
		String approvedBy = compensation == null ? null : compensation.getApprovedBy();
		approvedByEl = uifactory.addTextElement("edit.approved.by", 255, approvedBy, formLayout);
		approvedByEl.setMandatory(true);
		
		Date approval = compensation == null ? null : compensation.getApproval();
		approvalEl = uifactory.addDateChooser("edit.approval.date", approval, formLayout);
		approvalEl.setMandatory(true);
		
		String extraTime = (compensation == null || compensation.getExtraTime() == null)
				? null : Integer.toString(compensation.getExtraTime().intValue() / 60);
		extraTimeEl = uifactory.addTextElement("edit.extra.time", 5, extraTime, formLayout);
		extraTimeEl.setDisplaySize(5);
		extraTimeEl.setDomReplacementWrapperRequired(false);
		extraTimeEl.setMandatory(true);

		String editPage = Util.getPackageVelocityRoot(getClass()) + "/select_repository_entry.html";
		selectRepositoryEntryLayout = FormLayoutContainer.createCustomFormLayout("selectFormLayout", getTranslator(), editPage);
		selectRepositoryEntryLayout.setLabel("edit.entry", null);
		selectRepositoryEntryLayout.setMandatory(true);
		formLayout.add(selectRepositoryEntryLayout);
		if(compensation != null && compensation.getEntry() != null) {
			updateEntryName(compensation.getEntry());
		}
		selectEntryButton = uifactory.addFormLink("select.entry", selectRepositoryEntryLayout, Link.BUTTON);
		selectEntryButton.getComponent().setSuppressDirtyFormWarning(true);
		selectEntryButton.setVisible(compensation == null || compensation.getEntry() == null);
		
		elementEl = uifactory.addDropdownSingleselect("select.entry.element", formLayout, new String[0], new String[0]);
		elementEl.addActionListener(FormEvent.ONCHANGE);
		elementEl.setHelpTextKey("select.entry.element.hint", null);
		if(compensation != null && compensation.getEntry() != null) {
			updateElementSelection(compensation.getEntry(), compensation.getSubIdent());
		}
		
		FormLayoutContainer buttonsCont = FormLayoutContainer.createButtonLayout("buttons", getTranslator());
		formLayout.add(buttonsCont);
		uifactory.addFormCancelButton("cancel", buttonsCont, ureq, getWindowControl());
		uifactory.addFormSubmitButton("save", buttonsCont);
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected boolean validateFormLogic(UserRequest ureq) {
		boolean allOk = super.validateFormLogic(ureq);
		
		elementEl.clearError();
		if(!elementEl.isOneSelected()) {
			elementEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		approvedByEl.clearError();
		if(!StringHelper.containsNonWhitespace(approvedByEl.getValue())) {
			approvedByEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		approvalEl.clearError();
		if(approvalEl.getDate() == null) {
			approvalEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		extraTimeEl.clearError();
		if(StringHelper.containsNonWhitespace(extraTimeEl.getValue())) {
			if(StringHelper.isLong(extraTimeEl.getValue())) {
				try {
					Integer.parseInt(extraTimeEl.getValue());
				} catch (NumberFormatException e) {
					extraTimeEl.setErrorKey("form.error.nointeger", null);
					allOk &= false;
				}
			} else {
				extraTimeEl.setErrorKey("form.error.nointeger", null);
				allOk &= false;
			}
		} else {
			extraTimeEl.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		selectRepositoryEntryLayout.clearError();
		if(entry == null) {
			selectRepositoryEntryLayout.setErrorKey("form.legende.mandatory", null);
			allOk &= false;
		}
		
		return allOk;
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(searchFormCtrl == source) {
			if(event == ReferencableEntriesSearchController.EVENT_REPOSITORY_ENTRY_SELECTED) {
				doSelectForm(searchFormCtrl.getSelectedEntry());
			}
			cmc.deactivate();
			cleanUp();
		} else if(cmc == source) {
			cleanUp();
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(searchFormCtrl);
		removeAsListenerAndDispose(cmc);
		searchFormCtrl = null;
		cmc = null;
	}
	
	@Override
	protected void formOK(UserRequest ureq) {
		Date approval = approvalEl.getDate();
		String approvedBy = approvedByEl.getValue();
		int extraTime = Integer.parseInt(extraTimeEl.getValue()) * 60;
		String subIdent = elementEl.getSelectedKey();
		String subIdentName = getCourseNodeName(entry, subIdent,elementEl.getSelectedValue());
		
		List<AssessmentTestSessionStatistics> sessionsStatistics = qtiService
				.getAssessmentTestSessionsStatistics(entry, subIdent, disadvantagedIdentity, true);
		if(!sessionsStatistics.isEmpty()) {
			Collections.sort(sessionsStatistics, new AssessmentTestSessionDetailsComparator());
			AssessmentTestSession oneLastSession = sessionsStatistics.get(0).getTestSession();
			qtiService.compensationExtraTimeAssessmentTestSession(oneLastSession, extraTime, getIdentity());
		}
		
		if(compensation == null) {
			compensation = disadvantageCompensationService.createDisadvantageCompensation(disadvantagedIdentity,
					extraTime, approvedBy, approval, getIdentity(), entry, subIdent, subIdentName);
			String afterXml = disadvantageCompensationService.toXml(compensation);
			disadvantageCompensationService.auditLog(Action.create, null, afterXml, compensation, getIdentity());
		} else {
			String beforeXml = disadvantageCompensationService.toXml(compensation);
			
			compensation.setApprovedBy(approvedBy);
			compensation.setApproval(approval);
			compensation.setExtraTime(extraTime);
			compensation.setSubIdent(subIdent);
			compensation.setSubIdentName(subIdentName);
			compensation = disadvantageCompensationService.updateDisadvantageCompensation(compensation);
			
			String afterXml = disadvantageCompensationService.toXml(compensation);
			disadvantageCompensationService.auditLog(Action.update, beforeXml, afterXml, compensation, getIdentity());
		}
		fireEvent(ureq, Event.DONE_EVENT);
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(selectEntryButton == source) {
			doSelectEntry(ureq);
		} else if(elementEl == source) {
			updateElementPath(CourseFactory.loadCourse(entry));	
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formCancelled(UserRequest ureq) {
		fireEvent(ureq, Event.CANCELLED_EVENT);
	}
	
	private void doSelectEntry(UserRequest ureq) {
		if(guardModalController(searchFormCtrl)) return;

		searchFormCtrl = new ReferencableEntriesSearchController(getWindowControl(), ureq, 
					new String[] { CourseModule.ORES_TYPE_COURSE}, null, disadvantagedIdentity,
					translate("select.entry"), false, false, false, false, true, false, Can.all);
		listenTo(searchFormCtrl);
		cmc = new CloseableModalController(getWindowControl(), translate("close"), searchFormCtrl.getInitialComponent(),
				true, translate("select.entry"));
		cmc.suppressDirtyFormWarningOnClose();
		listenTo(cmc);
		cmc.activate();
	}
	
	private void doSelectForm(RepositoryEntry selectedEntry) {
		this.entry = selectedEntry;
		updateEntryName(selectedEntry);
		String currentSelected = null;
		if(elementEl != null && elementEl.isOneSelected()) {
			currentSelected = elementEl.getSelectedKey();
		}
		updateElementSelection(selectedEntry, currentSelected);	
	}
	
	private void updateEntryName(RepositoryEntry selectedEntry) {
		if(selectedEntry == null) {
			selectRepositoryEntryLayout.contextRemove("entryName");
		} else {
			StringBuilder sb = new StringBuilder(64);
			sb.append(StringHelper.escapeHtml(selectedEntry.getDisplayname()));
			if(StringHelper.containsNonWhitespace(entry.getExternalRef())) {
				sb.append(" (").append(StringHelper.escapeHtml(selectedEntry.getExternalRef())).append(")");
			}
			selectRepositoryEntryLayout.contextPut("entryName", sb.toString());
		}
	}
	
	private void updateElementSelection(RepositoryEntry selectedEntry, String currentSelectedIdent) {
		final KeyValues values = new KeyValues();
		Visitor visitor = node -> {
			if(node instanceof IQTESTCourseNode) {
				IQTESTCourseNode testNode = (IQTESTCourseNode)node;
				RepositoryEntry testEntry = testNode.getCachedReferencedRepositoryEntry();
				if(testNode.hasQTI21TimeLimit(testEntry)) {
					StringBuilder sb = new StringBuilder(32);
					sb.append(testNode.getShortTitle())
					  .append(" (").append(testNode.getIdent()).append(")");
					values.add(KeyValues.entry(testNode.getIdent(), sb.toString()));
				}
			}
		};
		
		ICourse course = CourseFactory.loadCourse(selectedEntry);
		CourseNode rootNode = course.getRunStructure().getRootNode();
		new TreeVisitor(visitor, rootNode, false)
			.visitAll();
		elementEl.setKeysAndValues(values.keys(), values.values(), null);
		if(currentSelectedIdent != null && values.containsKey(currentSelectedIdent)) {
			elementEl.select(currentSelectedIdent, true);
		}
		updateElementPath(course);
	}
	
	private void updateElementPath(ICourse course) {
		String path = "";
		if(elementEl.isOneSelected()) {
			String nodeIdent = elementEl.getSelectedKey();
			CourseNode node = course.getRunStructure().getNode(nodeIdent);
			String parentLine = getCourseNodePath(node);
			if(parentLine != null) {
				path = parentLine;
			}
		}
		elementEl.setExampleKey("noTransOnlyParam", new String[] { path });
	}
	
	private String getCourseNodePath(CourseNode node) {
		if(node == null) return null;
		
		List<CourseNode> parentLine = new ArrayList<>();
		for(CourseNode parent = (CourseNode)node.getParent(); parent != null; parent=(CourseNode)parent.getParent()) {
			parentLine.add(parent);
		}
		if(parentLine.size() <= 1) {
			return null;
		}
		
		Collections.reverse(parentLine);
		
		StringBuilder sb = new StringBuilder(128);
		for(CourseNode n:parentLine) {
			sb.append("/ ").append(n.getShortTitle()).append(" ");
		}
		return sb.toString();
	}
	
	private String getCourseNodeName(RepositoryEntry selectedEntry, String ident, String value) {
		ICourse course = CourseFactory.loadCourse(selectedEntry);
		CourseNode rootNode = course.getRunStructure().getNode(ident);
		if(rootNode != null) {
			return rootNode.getShortTitle();
		}
		return value;
	}
}
