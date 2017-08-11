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
package org.olat.ims.qti21.ui.assessment;


import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.olat.basesecurity.GroupRoles;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.link.Link;
import org.olat.core.gui.components.link.LinkFactory;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.course.archiver.ScoreAccountingHelper;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.group.BusinessGroupService;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentTestHelper;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21Service;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentItemRef;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.state.TestPlan;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNode.TestNodeType;
import uk.ac.ed.ph.jqtiplus.state.TestPlanNodeKey;
import uk.ac.ed.ph.jqtiplus.state.TestSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 16.08.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class IdentitiesAssessmentTestCorrectionController extends BasicController {
	
	private final VelocityContainer mainVC;
	private NavigationController navigationCtrl;
	
	private IdentitiesAssessmentTestOverviewController overviewCtrl;
	private IdentitiesAssessmentItemCorrectionController itemCorrectionCtrl;
	
	private final AssessmentToolOptions asOptions;

	private String subIdent;
	private RepositoryEntry testEntry;
	private RepositoryEntry courseEntry;
	private CourseEnvironment courseEnv;
	
	private AssessmentItemRef currentItemRef;
	private final List<AssessmentItemRef> itemRefs;
	private final ResolvedAssessmentTest resolvedAssessmentTest;
	private final AssessmentTestCorrection testCorrections;
	
	private final Map<Identity,AssessmentEntry> assessmentEntries;
	private final Map<Identity,AssessmentTestSession> lastSessions;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private AssessmentService assessmentService;
	@Autowired
	private BusinessGroupService businessGroupService;
	
	public IdentitiesAssessmentTestCorrectionController(UserRequest ureq, WindowControl wControl,
			CourseEnvironment courseEnv, AssessmentToolOptions asOptions, IQTESTCourseNode courseNode) {
		super(ureq, wControl);
		
		this.asOptions = asOptions;
		this.courseEnv = courseEnv;
		
		subIdent = courseNode.getIdent();
		testEntry = courseNode.getReferencedRepositoryEntry();
		courseEntry = courseEnv.getCourseGroupManager().getCourseEntry();
		
		File fUnzippedDirRoot = FileResourceManager.getInstance()
				.unzipFileResource(testEntry.getOlatResource());
		resolvedAssessmentTest = qtiService.loadAndResolveAssessmentTest(fUnzippedDirRoot, false, false);

		lastSessions = getLastSessions();
		assessmentEntries = getAssessmentEntries(lastSessions.keySet());

		mainVC = createVelocityContainer("corrections");

		itemRefs = calculateAssessmentItemToCorrect();
		testCorrections = collectAssessedIdentityForItem(itemRefs);

		navigationCtrl = new NavigationController(ureq, getWindowControl());
		listenTo(navigationCtrl);
		mainVC.put("navigation", navigationCtrl.getInitialComponent());
		
		if(itemRefs.size() > 0) {
			currentItemRef = itemRefs.get(0);
			navigationCtrl.updatePreviousNext();
			updateIdentitiesAssessmentItem(ureq);
		}
		putInitialPanel(mainVC);
		
		overviewCtrl = new IdentitiesAssessmentTestOverviewController(ureq, getWindowControl(), testCorrections);
		listenTo(overviewCtrl);
	}
	
	public int getNumberOfAssessedIdentities() {
		return lastSessions == null ? 0 : lastSessions.size();
	}
	
	public AssessmentTestCorrection getTestCorrections() {
		return testCorrections;
	}
	
	private List<AssessmentItemRef> calculateAssessmentItemToCorrect() {
		return resolvedAssessmentTest.getAssessmentItemRefs();
	}
	
	private void updateIdentitiesAssessmentItem(UserRequest ureq) {
		removeAsListenerAndDispose(itemCorrectionCtrl);
		
		if(currentItemRef != null) {
			itemCorrectionCtrl = new IdentitiesAssessmentItemCorrectionController(ureq, getWindowControl(),
					testCorrections, currentItemRef, testEntry, resolvedAssessmentTest);
			int index = itemRefs.indexOf(currentItemRef);
			if(index + 1 == itemRefs.size()) {
				itemCorrectionCtrl.disableNext();
			}
			listenTo(itemCorrectionCtrl);
			mainVC.put("itemCorrection", itemCorrectionCtrl.getInitialComponent());
		} else if(itemCorrectionCtrl != null) {
			mainVC.remove(itemCorrectionCtrl.getInitialComponent());
			itemCorrectionCtrl = null;
			
			mainVC.put("itemCorrection", overviewCtrl.getInitialComponent());
		}
	}
	
	private AssessmentTestCorrection collectAssessedIdentityForItem(List<AssessmentItemRef> itemRefList) {
		AssessmentTestCorrection corrections = new AssessmentTestCorrection(assessmentEntries);
		for(AssessmentItemRef itemRef:itemRefList) {
			String itemRefIdentifier = itemRef.getIdentifier().toString();
			List<AssessmentItemSession> itemSessions = qtiService.getAssessmentItemSessions(courseEntry, subIdent, testEntry, itemRefIdentifier);
			Map<AssessmentTestSession,AssessmentItemSession> testToItemSessions = new HashMap<>();
			for(AssessmentItemSession itemSession:itemSessions) {
				testToItemSessions.put(itemSession.getAssessmentTestSession(), itemSession);
			}
			
			for(Map.Entry<Identity,AssessmentTestSession> entry:lastSessions.entrySet()) {
				AssessmentTestSession testSession = entry.getValue();
				Identity assessedIdentity = testSession.getIdentity();
				
				TestSessionState testSessionState = qtiService.loadTestSessionState(testSession);
				TestPlan testPlan = testSessionState.getTestPlan();
				List<TestPlanNode> nodes = testPlan.getTestPlanNodeList();
				for(TestPlanNode node:nodes) {
					TestNodeType testNodeType = node.getTestNodeType();
					AssessmentItemSession itemSession = testToItemSessions.get(testSession);
					ItemSessionState itemSessionState = testSessionState.getItemSessionStates().get(node.getKey());
	
					TestPlanNodeKey testPlanNodeKey = node.getKey();
					if(testPlanNodeKey != null && testPlanNodeKey.getIdentifier() != null) {
						Identifier identifier = testPlanNodeKey.getIdentifier();
						if(testNodeType == TestNodeType.ASSESSMENT_ITEM_REF && itemRef.getIdentifier().equals(identifier)) {
							AssessmentItemCorrection correction = new AssessmentItemCorrection(assessedIdentity,
									 testSession, testSessionState, itemSession, itemSessionState, itemRef, node);
							corrections.add(correction);
							break;
						}
					}
				}
			}
		}
		return corrections;
	}
	
	private Map<Identity,AssessmentEntry> getAssessmentEntries(Set<Identity> identities) {
		List<AssessmentEntry> entries = assessmentService.loadAssessmentEntriesBySubIdent(courseEntry, subIdent);
		Map<Identity,AssessmentEntry> identityToAssessmentEntryMap = new HashMap<>();
		for(AssessmentEntry assessmentEntry:entries) {
			if(identities.contains(assessmentEntry.getIdentity())) {
				identityToAssessmentEntryMap.put(assessmentEntry.getIdentity(), assessmentEntry);
			}
		}
		return identityToAssessmentEntryMap;
	}
	
	private Map<Identity,AssessmentTestSession> getLastSessions() {
		Set<Identity> identitiesSet;
		if(asOptions.getGroup() != null) {
			List<Identity> identities = businessGroupService.getMembers(asOptions.getGroup(), GroupRoles.participant.name());
			identitiesSet = new HashSet<>(identities);
		} else if(asOptions.getIdentities() != null) {
			identitiesSet = new HashSet<>(asOptions.getIdentities());
		} else {
			identitiesSet = new HashSet<>(ScoreAccountingHelper.loadUsers(courseEnv));
		}

		List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(courseEntry, subIdent, testEntry);
		Map<Identity,AssessmentTestSession> identityToSessions = new HashMap<>();
		for(AssessmentTestSession session:sessions) {
			//filter last session / user
			Identity assessedIdentity = session.getIdentity();
			if(identitiesSet != null && !identitiesSet.contains(assessedIdentity)) {
				continue;
			}
			
			Date fDate = session.getFinishTime();
			if(fDate == null) {
				//not terminated
			} else {
				if(identityToSessions.containsKey(assessedIdentity)) {
					AssessmentTestSession currentSession = identityToSessions.get(assessedIdentity);

					Date currentFDate = currentSession.getFinishTime();
					if(fDate.after(currentFDate)) {
						identityToSessions.put(assessedIdentity, session);
					}
				} else {
					identityToSessions.put(assessedIdentity, session);
				}
			}	
		}
		return identityToSessions;
	}

	@Override
	protected void doDispose() {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(itemCorrectionCtrl == source) {
			if(event == Event.CANCELLED_EVENT) {
				fireEvent(ureq, event);
			} else if(event == Event.DONE_EVENT) {
				doNextAssessmentItem(ureq);
			}
		} else if(overviewCtrl == source) {
			fireEvent(ureq, event);
		}
		super.event(ureq, source, event);
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}
	
	private void doPreviousAssessmentItem(UserRequest ureq) {
		int previousIndex = -1;
		if(currentItemRef == null) {
			previousIndex = itemRefs.size() - 1;
		} else {
			previousIndex = itemRefs.indexOf(currentItemRef) - 1;
		}

		if(previousIndex >= 0 && itemRefs.size() > previousIndex) {
			currentItemRef = itemRefs.get(previousIndex);
			navigationCtrl.updatePreviousNext();
			updateIdentitiesAssessmentItem(ureq);
		} else if(itemRefs.size() > 0) {
			currentItemRef = itemRefs.get(0);
		}
	}

	private void doNextAssessmentItem(UserRequest ureq) {
		int nextIndex = itemRefs.indexOf(currentItemRef) + 1;
		if(nextIndex >= 0 && itemRefs.size() > nextIndex) {
			currentItemRef = itemRefs.get(nextIndex);
			navigationCtrl.updatePreviousNext();
			updateIdentitiesAssessmentItem(ureq);
		} else if(itemRefs.size() > 0) {
			currentItemRef = itemRefs.get(itemRefs.size() - 1);
		}
	}
	
	private void doSelectByIndex(UserRequest ureq, int index) {
		currentItemRef = itemRefs.get(index);
		navigationCtrl.updatePreviousNext();
		updateIdentitiesAssessmentItem(ureq);
		mainVC.setDirty(true);
	}
	
	private void doOverview() {
		currentItemRef = null;
		mainVC.put("itemCorrection", overviewCtrl.getInitialComponent());
		navigationCtrl.overview();
	}
	
	private class NavigationController extends BasicController {

		private final VelocityContainer navigationVC;
		private Link previousItemLink, nextItemLink, overviewLink;
		
		public NavigationController(UserRequest ureq, WindowControl wControl) {
			super(ureq, wControl);
			
			navigationVC = createVelocityContainer("corrections_navigation");

			List<SelectPair> itemRefKeys = new ArrayList<>(itemRefs.size());
			for(int i=0; i<itemRefs.size(); i++) {
				String itemTitle = AssessmentTestHelper.getAssessmentItemTitle(itemRefs.get(i), resolvedAssessmentTest);
				itemRefKeys.add(new SelectPair(Integer.toString(i), itemTitle));
			}
			itemRefKeys.add(new SelectPair("overview", translate("overview.tests")));
			navigationVC.contextPut("itemRefKeys", itemRefKeys);

			previousItemLink = LinkFactory.createButton("previous.item", navigationVC, this);
			previousItemLink.setIconLeftCSS("o_icon o_icon_previous");
			nextItemLink = LinkFactory.createButton("next.item", navigationVC, this);
			nextItemLink.setIconRightCSS("o_icon o_icon_next");
			overviewLink = LinkFactory.createButton("overview.tests", navigationVC, this);

			putInitialPanel(navigationVC);
		}

		@Override
		protected void doDispose() {
			//
		}

		@Override
		protected void event(UserRequest ureq, Component source, Event event) {
			if(previousItemLink == source) {
				doPreviousAssessmentItem(ureq);
			} else if(nextItemLink == source) {
				doNextAssessmentItem(ureq);
			} else if(overviewLink == source) {
				doOverview();
			} else if(navigationVC == source) {
				String cmd = ureq.getParameter("cid");
				if("sel".equals(cmd)) {
					String selectedKey = ureq.getParameter("item");
					if("overview".equals(selectedKey)) {
						doOverview();
					} else if(StringHelper.isLong(selectedKey)) {
						try {
							doSelectByIndex(ureq, Integer.parseInt(selectedKey));
						} catch (NumberFormatException e) {
							logError("Cannot parse select index: " + selectedKey, e);
						}
					}
				}
			}
		}
		
		protected void overview() {
			navigationVC.contextPut("selectedValue", "overview");
			
			if(itemRefs.size() > 0) {
				AssessmentItemRef itemRef = itemRefs.get(itemRefs.size() - 1);
				String itemTitle = AssessmentTestHelper.getAssessmentItemTitle(itemRef, resolvedAssessmentTest);
				previousItemLink.setCustomDisplayText(itemTitle);
				previousItemLink.setEnabled(true);
			}
			
			nextItemLink.setCustomDisplayText(translate("next.item"));
			nextItemLink.setEnabled(false);
		}
		
		protected void updatePreviousNext() {
			int index = itemRefs.indexOf(currentItemRef);
			
			if(index > 0) {
				AssessmentItemRef itemRef = itemRefs.get(index - 1);
				String itemTitle = AssessmentTestHelper.getAssessmentItemTitle(itemRef, resolvedAssessmentTest);
				previousItemLink.setCustomDisplayText(itemTitle);
				previousItemLink.setEnabled(true);
			} else {
				previousItemLink.setCustomDisplayText(translate("previous.item"));
				previousItemLink.setEnabled(false);
			}
			
			navigationVC.contextPut("selectedValue", index);
			navigationVC.setDirty(true);

			if(index + 1 < itemRefs.size()) {
				AssessmentItemRef itemRef = itemRefs.get(index + 1);
				String itemTitle = AssessmentTestHelper.getAssessmentItemTitle(itemRef, resolvedAssessmentTest);
				nextItemLink.setCustomDisplayText(itemTitle);
				nextItemLink.setEnabled(true);
			} else {
				nextItemLink.setCustomDisplayText(translate("next.item"));
				nextItemLink.setEnabled(false);
			}
		}
	}
	
	public class SelectPair {
		
		private final String text;
		private final String value;
		
		public SelectPair(String value, String text) {
			this.text = text;
			this.value = value;
		}

		public String getText() {
			return text;
		}

		public String getValue() {
			return value;
		}
	}
}