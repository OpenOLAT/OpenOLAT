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
package org.olat.modules.video.ui.question;

import java.io.File;
import java.math.BigDecimal;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.components.Component;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.control.Controller;
import org.olat.core.gui.control.Event;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.control.controller.BasicController;
import org.olat.course.nodes.VideoCourseNode;
import org.olat.ims.qti21.AssessmentItemSession;
import org.olat.ims.qti21.AssessmentSessionAuditLogger;
import org.olat.ims.qti21.AssessmentTestSession;
import org.olat.ims.qti21.OutcomesAssessmentItemListener;
import org.olat.ims.qti21.QTI21DeliveryOptions;
import org.olat.ims.qti21.QTI21Service;
import org.olat.ims.qti21.manager.audit.DefaultAssessmentSessionAuditLogger;
import org.olat.ims.qti21.ui.AssessmentItemDisplayController;
import org.olat.ims.qti21.ui.QTIWorksAssessmentItemEvent;
import org.olat.ims.qti21.ui.ResponseInput;
import org.olat.modules.assessment.AssessmentEntry;
import org.olat.modules.assessment.AssessmentService;
import org.olat.modules.video.VideoManager;
import org.olat.modules.video.VideoQuestion;
import org.olat.repository.RepositoryEntry;
import org.springframework.beans.factory.annotation.Autowired;

import uk.ac.ed.ph.jqtiplus.node.result.AssessmentResult;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentItem;
import uk.ac.ed.ph.jqtiplus.running.ItemSessionController;
import uk.ac.ed.ph.jqtiplus.state.ItemSessionState;
import uk.ac.ed.ph.jqtiplus.types.Identifier;

/**
 * 
 * Initial date: 6 déc. 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class VideoAssessmentItemController extends BasicController implements OutcomesAssessmentItemListener {
	
	private VideoAssessmentItemsDisplayController assessmentItemCtrl;
	private final AssessmentSessionAuditLogger candidateAuditLogger = new DefaultAssessmentSessionAuditLogger();
	
	private final VelocityContainer mainVC;
	private final List<VideoQuestion> answerededQuestions = new ArrayList<>();
	
	private final RepositoryEntry entry;
	private final RepositoryEntry videoEntry;
	private final VideoCourseNode courseNode;
	private final boolean authorMode;
	private VideoQuestion currentQuestion;
	private List<VideoQuestion> allQuestions;
	
	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private VideoManager videoManager;
	@Autowired
	private AssessmentService assessmentService;
	
	public VideoAssessmentItemController(UserRequest ureq, WindowControl wControl, RepositoryEntry videoEntry,
			RepositoryEntry entry, VideoCourseNode courseNode, String videoElementId, boolean authorMode) {
		super(ureq, wControl);
		this.videoEntry = videoEntry;
		this.courseNode = courseNode;
		this.authorMode = authorMode;
		this.entry = entry == null ? videoEntry : entry;
		
		mainVC = createVelocityContainer("item_wrapper");
		mainVC.contextPut("videoElementId", videoElementId);
		mainVC.contextPut("authorMode", Boolean.valueOf(authorMode));
		putInitialPanel(mainVC);
	}
	
	public VideoQuestion getCurrentQuestion() {
		return currentQuestion;
	}
	
	public boolean present(UserRequest ureq, VideoQuestion question, List<VideoQuestion> allAvailableQuestions) {
		removeAsListenerAndDispose(assessmentItemCtrl);

		currentQuestion = question;
		allQuestions = new ArrayList<>(allAvailableQuestions);

		File resourceDirectory = videoManager.getQuestionDirectory(videoEntry.getOlatResource(), question);
		File resourceFile = new File(resourceDirectory, question.getQuestionFilename());
		URI assessmentItemUri = resourceFile.toURI();
		ResolvedAssessmentItem resolvedAssessmentItem = qtiService
				.loadAndResolveAssessmentItem(assessmentItemUri, resourceDirectory);

		String subIdent = courseNode == null ? null : courseNode.getIdent();
		Boolean entryRoot = courseNode == null? Boolean.TRUE: Boolean.FALSE;
		AssessmentEntry assessmentEntry = assessmentService.getOrCreateAssessmentEntry(getIdentity(), null, entry, subIdent, entryRoot, videoEntry);
		
		QTI21DeliveryOptions options = QTI21DeliveryOptions.defaultSettings();
		options.setEnableAssessmentItemBack(true);
		options.setEnableAssessmentItemResetHard(question.isAllowNewAttempt());
		options.setEnableAssessmentItemSkip(question.isAllowSkipping());
	
		assessmentItemCtrl = new VideoAssessmentItemsDisplayController(ureq, getWindowControl(),
				entry, subIdent, videoEntry, assessmentEntry, authorMode,
				resolvedAssessmentItem, resourceDirectory, resourceFile, question.getId(),
				options, this, candidateAuditLogger);
		if(!authorMode) {
			assessmentItemCtrl.setTimeLimit(question.getTimeLimit());
		}
		listenTo(assessmentItemCtrl);
		
		double completion = (assessmentEntry.getCompletion() == null ? 0.0d : assessmentEntry.getCompletion().doubleValue());
		assessmentItemCtrl.updateCompletion(completion);

		mainVC.put("question", assessmentItemCtrl.getInitialComponent());
		return true;
	}
	
	public AssessmentTestSession getCandidateSession() {
		return assessmentItemCtrl == null ? null : assessmentItemCtrl.getCandidateSession();
	}

	@Override
	protected void event(UserRequest ureq, Component source, Event event) {
		//
	}

	@Override
	protected void event(UserRequest ureq, Controller source, Event event) {
		if(assessmentItemCtrl == source) {
			if(event instanceof QTIWorksAssessmentItemEvent) {
				QTIWorksAssessmentItemEvent qe = (QTIWorksAssessmentItemEvent)event;
				if(QTIWorksAssessmentItemEvent.Event.back == qe.getEvent()) {
					fireEvent(ureq, Event.BACK_EVENT);
				} else if(QTIWorksAssessmentItemEvent.Event.skip == qe.getEvent()
						|| QTIWorksAssessmentItemEvent.Event.next == qe.getEvent()) {
					fireEvent(ureq, Event.DONE_EVENT);
					answerededQuestions.add(getCurrentQuestion());
				}
			}
		}
	}

	@Override
	public void outcomes(AssessmentTestSession candidateSession, Float score, Boolean pass) {
		List<AssessmentItemSession> itemSessions = qtiService.getAssessmentItemSessions(candidateSession);
		Map<String,AssessmentItemSession> itemSessionMap = new HashMap<>();
		for(AssessmentItemSession itemSession:itemSessions) {
			itemSessionMap.put(itemSession.getAssessmentItemIdentifier(), itemSession);
		}

		String subIdent = courseNode == null ? null : courseNode.getIdent();
		Boolean entryRoot = courseNode == null? Boolean.TRUE: Boolean.FALSE;
		AssessmentEntry assessmentEntry = assessmentService
				.getOrCreateAssessmentEntry(getIdentity(), null, entry, subIdent, entryRoot, videoEntry);

		int itemsCompleted = 0;
		BigDecimal totalScore = BigDecimal.ZERO;
		
		for(VideoQuestion videoQuestion: allQuestions) {
			String assessmentItemIdentifier = videoQuestion.getAssessmentItemIdentifier();
			AssessmentItemSession itemSession = itemSessionMap.get(assessmentItemIdentifier);
			if(itemSession == null) {
				continue;
			}
			
			Double questionScore = videoQuestion.getMaxScore();
			if(itemSession.getScore() != null && questionScore != null
					&& Math.abs(itemSession.getScore().doubleValue() - questionScore.doubleValue()) < 0.00001) {
				itemsCompleted++;
				totalScore = totalScore.add(itemSession.getScore());
			} else if(itemSession.getScore() == null && (questionScore == null || questionScore.doubleValue() == 0.0d)) {
				ItemSessionState sessionState = qtiService.loadItemSessionState(candidateSession, itemSession);
				if(sessionState != null && sessionState.isResponded()) {
					itemsCompleted++;
				}
			}	
		}
		
		assessmentEntry.setScore(totalScore);
		
		double completion;
		if(itemsCompleted == 0) {
			completion = 0.0d;
		} else {
			completion = (double)itemsCompleted / allQuestions.size();
			if(completion > 100.0d) {
				completion = 100.0d;
			}
		}

		assessmentEntry.setCompletion(completion);
		if(assessmentItemCtrl != null) {
			assessmentItemCtrl.updateCompletion(completion);
		}
		assessmentService.updateAssessmentEntry(assessmentEntry);
	}
	
	private class VideoAssessmentItemsDisplayController extends AssessmentItemDisplayController {

		private AssessmentResult assessmentResult;
		
		public VideoAssessmentItemsDisplayController(UserRequest ureq, WindowControl wControl,
				RepositoryEntry entry, String subIdent, RepositoryEntry testEntry, AssessmentEntry assessmentEntry, boolean authorMode,
				ResolvedAssessmentItem resolvedAssessmentItem, File fUnzippedDirRoot, File itemFile, String videoQuestionId,
				QTI21DeliveryOptions deliveryOptions, OutcomesAssessmentItemListener outcomesListener,
				AssessmentSessionAuditLogger candidateAuditLogger) {
			super(ureq, wControl, entry, subIdent, testEntry, assessmentEntry, authorMode,
					resolvedAssessmentItem, fUnzippedDirRoot, itemFile, videoQuestionId,
					deliveryOptions, outcomesListener, candidateAuditLogger);
		}
		
		public void setTimeLimit(long seconds) {
			qtiWorksCtrl.setTimeLimit(seconds);
		}
		
		public void updateCompletion(double completion) {
			double completionInPercent = completion * 100.0d;
			if(completionInPercent < 0.0d) {
				completionInPercent = 0.0d;
			} else if(completionInPercent > 100.0d) {
				completionInPercent = 100.0d;
			}
			mainVC.contextPut("enableCompletion", Boolean.valueOf(!authorMode));
			mainVC.contextPut("completion", Double.toString(completionInPercent));
		}
		
		@Override
		protected ItemSessionState loadItemSessionState() {
			ItemSessionState itemSessionState = qtiService.loadItemSessionState(candidateSession, itemSession);
			if(itemSessionState == null) {
				itemSessionState = new ItemSessionState();
			}
			return itemSessionState;
		}

		@Override
		protected AssessmentTestSession initOrResumeAssessmentTestSession(RepositoryEntry courseEntry, String subIdent, RepositoryEntry testEntry,
				AssessmentEntry assessmentEntry, boolean author) {
			AssessmentTestSession lastSession = qtiService.getResumableAssessmentItemsSession(getIdentity(), null, courseEntry, subIdent, testEntry, author);
			if(lastSession == null) {
				candidateSession = qtiService.createAssessmentTestSession(getIdentity(), null, assessmentEntry, courseEntry, subIdent, testEntry, null, author);
				return candidateSession;
			}
			return lastSession;
		}

		@Override
		protected ItemSessionController enterSession(UserRequest ureq) {
			File assessmentResultFile = qtiService.getAssessmentResultFile(candidateSession);
			if(assessmentResultFile.exists()) {
				assessmentResult = qtiService.getAssessmentResult(candidateSession);
			}
			return super.enterSession(ureq);
		}

		@Override
		public void handleResponses(UserRequest ureq, Map<Identifier, ResponseInput> stringResponseMap,
				Map<Identifier,ResponseInput> fileResponseMap, String candidateComment) {
			super.handleResponses(ureq, stringResponseMap, fileResponseMap, candidateComment);
		}
		
		@Override
	    protected AssessmentResult updateSessionFinishedStatus(UserRequest ureq) {
	        // we don't close the candidate session
	        return computeAndRecordItemAssessmentResult(ureq);
	    }

		@Override
		public AssessmentResult computeItemAssessmentResult(UserRequest ureq) {
	    	String baseUrl = "http://localhost:8080/olat";
	        final URI sessionIdentifierSourceId = URI.create(baseUrl);
	        final String sessionIdentifier = "itemsession/" + (candidateSession == null ? "sdfj" : candidateSession.getKey());
	        if(assessmentResult == null) {
	        	assessmentResult = itemSessionController.computeAssessmentResult(ureq.getRequestTimestamp(), sessionIdentifier, sessionIdentifierSourceId);
	        	return assessmentResult;
	        }
	        return itemSessionController.computeAssessmentResult(assessmentResult, ureq.getRequestTimestamp());
	    }
	}
}
