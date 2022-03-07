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
import java.util.function.Supplier;
import java.util.stream.Collectors;

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
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.tree.TreeModel;
import org.olat.core.gui.components.tree.TreeNode;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CalloutSettings;
import org.olat.core.gui.control.generic.closablewrapper.CloseableCalloutWindowController;
import org.olat.core.id.Identity;
import org.olat.core.util.Util;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.assessment.AssessmentModule;
import org.olat.course.assessment.IndentedNodeRenderer;
import org.olat.course.assessment.ui.tool.CourseNodeToReviewTableModel.ToReviewCols;
import org.olat.course.assessment.ui.tool.event.CourseNodeIdentityEvent;
import org.olat.course.nodes.CourseNode;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.ui.AssessedIdentityListState;
import org.olat.modules.assessment.ui.AssessmentToolSecurityCallback;
import org.olat.repository.RepositoryEntry;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 26 Nov 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public abstract class CourseNodeToReviewAbstractSmallController extends FormBasicController {
	
	private static final String CMD_IDENTITIES = "identities";
	private static final String CMD_IDENTITY = "identity";

	private FlexiTableElement tableEl;
	private CourseNodeToReviewTableModel usersTableModel;
	
	private CloseableCalloutWindowController ccwc;
	private Controller identitySelectionCtrl;

	protected final RepositoryEntry courseEntry;
	protected final AssessmentToolSecurityCallback assessmentCallback;
	protected final Map<String, CourseNode> nodeIdentToCourseNode;
	private int counter = 0;
	
	@Autowired
	private UserManager userManager;
	
	protected abstract String getIconCssClass();
	protected abstract String getTitleI18nKey();
	protected abstract String getTitleNumberI18nKey();
	protected abstract String getTableEmptyI18nKey();
	protected abstract Map<String, List<AssessmentEntry>> loadNodeIdentToEntries();
	protected abstract Supplier<AssessedIdentityListState> getIdentityFilter();
	
	protected CourseNodeToReviewAbstractSmallController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, AssessmentToolSecurityCallback assessmentCallback) {
		super(ureq, wControl, "overview_to_review");
		setTranslator(Util.createPackageTranslator(AssessmentModule.class, getLocale(), getTranslator()));
		setTranslator(userManager.getPropertyHandlerTranslator(getTranslator()));
		
		this.courseEntry = courseEntry;
		this.assessmentCallback = assessmentCallback;
		
		nodeIdentToCourseNode = new HashMap<>();
		ICourse course = CourseFactory.loadCourse(courseEntry);
		TreeModel tm = AssessmentHelper.assessmentTreeModel(course, getLocale());
		new TreeVisitor(node -> {
			if(node instanceof TreeNode) {
				Object uobject = ((TreeNode)node).getUserObject();
				if(uobject instanceof CourseNode) {
					CourseNode tNode = (CourseNode)uobject;
					nodeIdentToCourseNode.put(tNode.getIdent(), tNode);
				}
			}
		}, tm.getRootNode(), false).visitAll();
		
		initForm(ureq);
		loadModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		flc.contextPut("iconCssClass", getIconCssClass());
		
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		
		IndentedNodeRenderer intendedNodeRenderer = new IndentedNodeRenderer();
		intendedNodeRenderer.setIndentationEnabled(false);
		DefaultFlexiColumnModel nodeModel = new DefaultFlexiColumnModel(ToReviewCols.courseNode, CMD_IDENTITIES, intendedNodeRenderer);
		columnsModel.addFlexiColumnModel(nodeModel);
		
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(ToReviewCols.participant));
		
		usersTableModel = new CourseNodeToReviewTableModel(columnsModel); 
		tableEl = uifactory.addTableElement(getWindowControl(), "table", usersTableModel, 20, false, getTranslator(), formLayout);
		tableEl.setNumOfRowsEnabled(false);
		tableEl.setExportEnabled(false);
		tableEl.setCustomizeColumns(false);
		tableEl.setEmptyTableSettings(getTableEmptyI18nKey(), null, getIconCssClass());
	}
	
	private void loadModel() {
		Map<String, List<AssessmentEntry>> nodeIdentToEntries = loadNodeIdentToEntries();
		
		List<CourseNodeToReviewRow> rows = new ArrayList<>(nodeIdentToEntries.size());
		for (Map.Entry<String, List<AssessmentEntry>> entry : nodeIdentToEntries.entrySet()) {
			String nodeIdent = entry.getKey();
			if (nodeIdentToCourseNode.containsKey(nodeIdent)) {
				List<Identity> identities = entry.getValue().stream()
						.map(AssessmentEntry::getIdentity)
						.distinct()
						.collect(Collectors.toList());
				
				if (!identities.isEmpty()) {
					CourseNodeToReviewRow row = new CourseNodeToReviewRow(nodeIdentToCourseNode.get(nodeIdent), identities);
					
					String identityLabel = identities.size() == 1
							? userManager.getUserDisplayName(identities.get(0))
							: translate("participants.to.review", Integer.toString(identities.size())) + " <i class='o_icon o_icon_info'> </i>";
					FormLink identityLink = uifactory.addFormLink("o_user_" + counter++, CMD_IDENTITY, identityLabel, null, null, Link.NONTRANSLATED);
					identityLink.setUserObject(row);
					row.setIdentityLink(identityLink);
					
					rows.add(row);
				}
			}
		}
		
		int numReviews = rows.stream().mapToInt(row -> row.getIdentities().size()).sum();
		String title = numReviews > 0
				? translate(getTitleNumberI18nKey(), Integer.toString(numReviews))
				: translate(getTitleI18nKey());
		flc.contextPut("title", title);
		
		usersTableModel.setObjects(rows);
		tableEl.reset();
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(source == tableEl) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				if(CMD_IDENTITIES.equals(se.getCommand())) {
					int index = se.getIndex();
					CourseNodeToReviewRow row = usersTableModel.getObject(index);
					doSelectIdentity(ureq, row.getCourseNodeIdent(), null);
				}
			}
		} else if (source instanceof FormLink) {
			FormLink link = (FormLink)source;
			if (CMD_IDENTITY.equals(link.getCmd())) {
				CourseNodeToReviewRow row = (CourseNodeToReviewRow)link.getUserObject();
				doSelectIdentity(ureq, row, link);
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if (source == identitySelectionCtrl) {
			ccwc.deactivate();
			cleanUp();
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	private void cleanUp() {
		removeAsListenerAndDispose(identitySelectionCtrl);
		removeAsListenerAndDispose(ccwc);
		identitySelectionCtrl = null;
		ccwc = null;
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}
	
	private void doSelectIdentity(UserRequest ureq, CourseNodeToReviewRow row, FormLink link) {
		removeAsListenerAndDispose(ccwc);
		removeAsListenerAndDispose(identitySelectionCtrl);
		
		if (row.getIdentities().size() <=1) {
			Identity identity = row.getIdentities().size() == 1? row.getIdentities().get(0): null;
			doSelectIdentity(ureq, row.getCourseNodeIdent(), identity);
		} else {
			identitySelectionCtrl = new IdentitySelectionController(ureq, getWindowControl(), row.getCourseNodeIdent(),
					row.getIdentities(), getIdentityFilter());
			listenTo(identitySelectionCtrl);
			
			CalloutSettings settings = new CalloutSettings();
			ccwc = new CloseableCalloutWindowController(ureq, getWindowControl(), identitySelectionCtrl.getInitialComponent(),
					link.getFormDispatchId(), "", true, "", settings);
			listenTo(ccwc);
			ccwc.activate();
		}
	}

	private void doSelectIdentity(UserRequest ureq, String courseNodeIdent, Identity identity) {
		fireEvent(ureq, new CourseNodeIdentityEvent(courseNodeIdent, identity, getIdentityFilter()));
	}

}