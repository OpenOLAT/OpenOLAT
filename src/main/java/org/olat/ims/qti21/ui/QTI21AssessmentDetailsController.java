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
package org.olat.ims.qti21.ui;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;

import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.EscapeMode;
import org.olat.core.gui.components.form.flexible.FormItem;
import org.olat.core.gui.components.form.flexible.FormItemContainer;
import org.olat.core.gui.components.form.flexible.elements.FlexiTableElement;
import org.olat.core.gui.components.form.flexible.impl.FormBasicController;
import org.olat.core.gui.components.form.flexible.impl.FormEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.BooleanCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.DefaultFlexiColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableColumnModel;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableDataModelFactory;
import org.olat.core.gui.components.form.flexible.impl.elements.table.SelectionEvent;
import org.olat.core.gui.components.form.flexible.impl.elements.table.StaticFlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.TextFlexiCellRenderer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.generic.closablewrapper.CloseableModalController;
import org.olat.core.gui.control.generic.modal.DialogBoxController;
import org.olat.core.gui.control.generic.modal.DialogBoxUIFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.resource.OresHelper;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.QTI21DeliveryOptions.ShowResultsOnFinish;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.ui.QTI21TestSessionTableModel.TSCols;
import org.olat.ims.qti21.ui.assessment.IdentityAssessmentTestCorrectionController;
import org.olat.ims.qti21.ui.event.RetrieveAssessmentTestSessionEvent;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.assessment.AssessmentToolOptions;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.user.UserManager;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 28.06.2016<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class QTI21AssessmentDetailsController extends FormBasicController {

	private Component resetToolCmp;
	private FlexiTableElement tableEl;
	private QTI21TestSessionTableModel tableModel;
	
	private RepositoryEntry entry;
	private final String subIdent;
	private final boolean manualCorrections;
	private final Identity assessedIdentity;
	
	private IQTESTCourseNode courseNode;
	private final RepositoryEntrySecurity reSecurity;
	private UserCourseEnvironment coachCourseEnv;
	private UserCourseEnvironment assessedUserCourseEnv;
	
	private CloseableModalController cmc;
	private AssessmentResultController resultCtrl;
	private QTI21ResetToolController resetToolCtrl;
	private DialogBoxController retrieveConfirmationCtr;
	private IdentityAssessmentTestCorrectionController correctionCtrl;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private UserManager userManager;
	@Autowired
	protected QTI21Service qtiService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private AssessmentService assessmentService;
	
	public QTI21AssessmentDetailsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry assessableEntry, IQTESTCourseNode courseNode,
			UserCourseEnvironment coachCourseEnv, UserCourseEnvironment assessedUserCourseEnv) {
		super(ureq, wControl, "assessment_details");
		entry = assessableEntry;
		this.courseNode = courseNode;
		subIdent = courseNode.getIdent();
		this.coachCourseEnv = coachCourseEnv;
		this.assessedUserCourseEnv = assessedUserCourseEnv;
		RepositoryEntry testEntry = courseNode.getReferencedRepositoryEntry();
		assessedIdentity = assessedUserCourseEnv.getIdentityEnvironment().getIdentity();
		manualCorrections = qtiService.needManualCorrection(testEntry);
		
		RepositoryEntry courseEntry = assessedUserCourseEnv.getCourseEnvironment().getCourseGroupManager().getCourseEntry();
		reSecurity = repositoryManager.isAllowed(ureq, courseEntry);

		initForm(ureq);
		updateModel();
	}
	
	public QTI21AssessmentDetailsController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry assessableEntry, Identity assessedIdentity) {
		super(ureq, wControl, "assessment_details");
		entry = assessableEntry;
		subIdent = null;
		this.assessedIdentity = assessedIdentity;
		manualCorrections = qtiService.needManualCorrection(assessableEntry);
		reSecurity = repositoryManager.isAllowed(ureq, assessableEntry);

		initForm(ureq);
		updateModel();
	}

	@Override
	protected void initForm(FormItemContainer formLayout, Controller listener, UserRequest ureq) {
		FlexiTableColumnModel columnsModel = FlexiTableDataModelFactory.createFlexiTableColumnModel();
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.lastModified));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.duration, new TextFlexiCellRenderer(EscapeMode.none)));
		columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.results, new TextFlexiCellRenderer(EscapeMode.none)));
		
		if(coachCourseEnv.isCourseReadOnly()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel("select", translate("select"), "open"));
		} else {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.open.i18nHeaderKey(), TSCols.open.ordinal(), "open",
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("select"), "open"),
							new StaticFlexiCellRenderer(translate("pull"), "open"))));
		}
		if(manualCorrections && !coachCourseEnv.isCourseReadOnly()) {
			columnsModel.addFlexiColumnModel(new DefaultFlexiColumnModel(TSCols.correction.i18nHeaderKey(), TSCols.correction.ordinal(), "correction",
					new BooleanCellRenderer(new StaticFlexiCellRenderer(translate("correction"), "correction"), null)));
		}
	

		tableModel = new QTI21TestSessionTableModel(columnsModel, getTranslator());
		tableEl = uifactory.addTableElement(getWindowControl(), "sessions", tableModel, 20, false, getTranslator(), formLayout);
		tableEl.setEmtpyTableMessageKey("results.empty");
		
		
		if(reSecurity.isEntryAdmin() && !coachCourseEnv.isCourseReadOnly()) {
			AssessmentToolOptions asOptions = new AssessmentToolOptions();
			asOptions.setAdmin(reSecurity.isEntryAdmin());
			asOptions.setIdentities(Collections.singletonList(assessedIdentity));
			if(courseNode != null) {
				resetToolCtrl = new QTI21ResetToolController(ureq, getWindowControl(),
						assessedUserCourseEnv.getCourseEnvironment(), asOptions, courseNode);
			} else {
				resetToolCtrl = new QTI21ResetToolController(ureq, getWindowControl(), entry, asOptions);
			}
			listenTo(resetToolCtrl);
			resetToolCmp = resetToolCtrl.getInitialComponent();	
		}
	} 

	@Override
	protected void doDispose() {
		//
	}
	
	protected void updateModel() {
		List<AssessmentTestSession> sessions = qtiService.getAssessmentTestSessions(entry, subIdent, assessedIdentity);
		Collections.sort(sessions, new AssessmentTestSessionComparator());
		tableModel.setObjects(sessions);
		tableEl.reloadData();
		tableEl.reset();
			
		if(resetToolCmp != null) {
			if(sessions.size() > 0) {
				flc.getFormItemComponent().put("reset.tool", resetToolCmp);
			} else {
				flc.getFormItemComponent().remove(resetToolCmp);
			}
		}
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(cmc == source) {
			cleanUp();
		} else if(correctionCtrl == source) {
			if(event == Event.DONE_EVENT || event == Event.CHANGED_EVENT) {
				if(courseNode != null) {
					doUpdateCourseNode(correctionCtrl.getAssessmentTestSession());
				} else {
					doUpdateEntry(correctionCtrl.getAssessmentTestSession());
				}
				updateModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
			cmc.deactivate();
			cleanUp();
		} else if(retrieveConfirmationCtr == source) {
			if(DialogBoxUIFactory.isYesEvent(event) || DialogBoxUIFactory.isOkEvent(event)) {
				doPullSession((AssessmentTestSession)retrieveConfirmationCtr.getUserObject());
				updateModel();
			}
		} else if(resetToolCtrl == source) {
			if(event == Event.DONE_EVENT) {
				updateModel();
				fireEvent(ureq, Event.CHANGED_EVENT);
			}
		}
		super.event(ureq, source, event);
	}
	
	private void cleanUp() {
		removeAsListenerAndDispose(correctionCtrl);
		removeAsListenerAndDispose(resultCtrl);
		removeAsListenerAndDispose(cmc);
		correctionCtrl = null;
		resultCtrl = null;
		cmc = null;
	}

	@Override
	protected void formInnerEvent(UserRequest ureq, FormItem source, FormEvent event) {
		if(tableEl == source) {
			if(event instanceof SelectionEvent) {
				SelectionEvent se = (SelectionEvent)event;
				String cmd = se.getCommand();
				AssessmentTestSession row = tableModel.getObject(se.getIndex());
				row = qtiService.getAssessmentTestSession(row.getKey());
				if("open".equals(cmd)) {
					if(row.getTerminationTime() == null) {
						doConfirmPullSession(ureq, row);
					} else {
						doOpenResult(ureq, row);
					}
				} else if("correction".equals(cmd)) {
					doCorrection(ureq, row);
				}
			}
		}
		super.formInnerEvent(ureq, source, event);
	}

	@Override
	protected void formOK(UserRequest ureq) {
		//
	}

	private void doCorrection(UserRequest ureq, AssessmentTestSession session) {
		correctionCtrl = new IdentityAssessmentTestCorrectionController(ureq, getWindowControl(), session);
		listenTo(correctionCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", correctionCtrl.getInitialComponent(),
				true, translate("correction"));
		cmc.activate();
		listenTo(cmc);
	}
	
	private void doUpdateCourseNode(AssessmentTestSession session) {
		ScoreEvaluation scoreEval = courseNode.getUserScoreEvaluation(assessedUserCourseEnv);
		BigDecimal finalScore = calculateFinalScore(session);
		Float score = finalScore == null ? null : finalScore.floatValue();
		ScoreEvaluation manualScoreEval = new ScoreEvaluation(score, scoreEval.getPassed(),
				scoreEval.getAssessmentStatus(), scoreEval.getFullyAssessed(), session.getKey());
		courseNode.updateUserScoreEvaluation(manualScoreEval, assessedUserCourseEnv, getIdentity(), false);
	}
	
	private void doUpdateEntry(AssessmentTestSession session) {
		AssessmentEntry assessmentEntry = assessmentService.loadAssessmentEntry(assessedIdentity, entry, null, entry);
		BigDecimal finalScore = calculateFinalScore(session);
		assessmentEntry.setScore(finalScore);
		assessmentEntry.setAssessmentId(session.getKey());
		assessmentService.updateAssessmentEntry(assessmentEntry);
	}
	
	private BigDecimal calculateFinalScore(AssessmentTestSession session) {
		BigDecimal finalScore = session.getScore();
		if(finalScore == null) {
			finalScore = session.getManualScore();
		} else if(session.getManualScore() != null) {
			finalScore = finalScore.add(session.getManualScore());
		}
		return finalScore;
	}

	private void doConfirmPullSession(UserRequest ureq, AssessmentTestSession session) {
		String title = translate("pull");
		String fullname = userManager.getUserDisplayName(session.getIdentity());
		String text = translate("retrievetest.confirm.text", new String[]{ fullname });
		retrieveConfirmationCtr = activateOkCancelDialog(ureq, title, text, retrieveConfirmationCtr);
		retrieveConfirmationCtr.setUserObject(session);
	}
	
	private void doPullSession(AssessmentTestSession session) {
		if(session.getFinishTime() == null) {
			session.setFinishTime(new Date());
		}
		session.setTerminationTime(new Date());
		session = qtiService.updateAssessmentTestSession(session);
		dbInstance.commit();//make sure that the changes committed before sending the event
		
		AssessmentSessionAuditLogger candidateAuditLogger = qtiService.getAssessmentSessionAuditLogger(session, false);
		candidateAuditLogger.logTestRetrieved(session, getIdentity());
		
		OLATResourceable sessionOres = OresHelper.createOLATResourceableInstance(AssessmentTestSession.class, session.getKey());
		CoordinatorManager.getInstance().getCoordinator().getEventBus()
			.fireEventToListenersOf(new RetrieveAssessmentTestSessionEvent(session.getKey()), sessionOres);
	}

	private void doOpenResult(UserRequest ureq, AssessmentTestSession session) {
		if(resultCtrl != null) return;

		FileResourceManager frm = FileResourceManager.getInstance();
		File fUnzippedDirRoot = frm.unzipFileResource(session.getTestEntry().getOlatResource());
		URI assessmentObjectUri = qtiService.createAssessmentObjectUri(fUnzippedDirRoot);
		File submissionDir = qtiService.getAssessmentResultFile(session);
		String mapperUri = registerCacheableMapper(null, "QTI21Resources::" + session.getTestEntry().getKey(),
				new ResourcesMapper(assessmentObjectUri, submissionDir));
		
		resultCtrl = new AssessmentResultController(ureq, getWindowControl(), assessedIdentity, false, session,
				ShowResultsOnFinish.details, fUnzippedDirRoot, mapperUri, true, true);
		listenTo(resultCtrl);
		cmc = new CloseableModalController(getWindowControl(), "close", resultCtrl.getInitialComponent(),
				true, translate("table.header.results"));
		cmc.activate();
		listenTo(cmc);
	}
	
	public static class AssessmentTestSessionComparator implements Comparator<AssessmentTestSession> {

		@Override
		public int compare(AssessmentTestSession a1, AssessmentTestSession a2) {
			Date t1 = a1.getTerminationTime();
			Date t2 = a2.getTerminationTime();
			
			int c;
			if(t1 == null && t2 == null) {
				c = 0;
			} else if(t2 == null) {
				return -1;
			} else if(t1 == null) {
				return 1;
			} else {
				c = t1.compareTo(t2);
			}
			
			if(c == 0) {
				Date c1 = a1.getCreationDate();
				Date c2 = a2.getCreationDate();
				if(c1 == null && c2 == null) {
					c = 0;
				} else if(c2 == null) {
					return -1;
				} else if(c1 == null) {
					return 1;
				} else {
					c = c1.compareTo(c2);
				}
			}
			return -c;
		}
	}
}