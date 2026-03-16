/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.logging.Tracing;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.olat.modules.selectus.FeedbackService;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.ApplicationFeedback;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.Reference;
import org.olat.modules.selectus.model.ReferenceStatus;
import org.olat.modules.selectus.model.ReferenceType;
import org.olat.modules.selectus.model.references.ReferenceSearchParameters;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * Set the status of references and feedbacks to late if needed.
 * 
 * Initial date: 12 mai 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@DisallowConcurrentExecution
public class CloseReferencesStatusJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(CloseReferencesStatusJob.class);
	
	public CloseReferencesStatusJob() {
		//make Spring happy
	}

	@Override
	public void executeWithDB(JobExecutionContext execContext) throws JobExecutionException {
		closeReferenceStatus();
		closeFeedbackStatus();
	}
	
	private void closeReferenceStatus() {
		try {
			Date now = new Date();
			
			RecruitingService recruitingService = CoreSpringFactory.getImpl(RecruitingService.class);
			List<Reference> references = recruitingService.getReferences(new ReferenceSearchParameters(ReferenceStatus.sentAwaiting));
			for(Reference reference:references) {
				Position position = reference.getApplication().getPosition();
				Date deadline = reference.getSubmissionDeadline();
				if(deadline == null) {
					if(reference.getReferenceType() == ReferenceType.expert) {
						deadline = position.getExpertRecommandationDeadline();
					} else if(reference.getReferenceType() == ReferenceType.recommendation) {
						deadline = position.getRefereeRecommandationDeadline();
					}
				}
				
				if(deadline != null) {
					deadline = RecruitingHelper.endOfDay(deadline);
					if(now.after(deadline)) {
						reference.setReferenceStatus(ReferenceStatus.late);
						reference = recruitingService.updateReference(reference);
						log.info(Tracing.M_AUDIT, "Set the status of the reference: {} to late.", reference);
					}
				}
			}
		} catch (Exception e) {
			log.error("Job change status of reference has encounter an error: ", e);
			DBFactory.getInstance().closeSession();
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
	
	private void closeFeedbackStatus() {
		try {
			Date startOfDay = RecruitingHelper.startOfDay(new Date());
			FeedbackService feedbackService = CoreSpringFactory.getImpl(FeedbackService.class);
			List<ApplicationFeedback> feedbacks = feedbackService.searchApplicationsFeedbacks(ReferenceStatus.sentAwaiting, startOfDay);
			for(ApplicationFeedback feedback:feedbacks) {
				feedback.setReferenceStatus(ReferenceStatus.late);
				feedback = feedbackService.updateApplicationFeedback(feedback);
				log.info(Tracing.M_AUDIT, "Set the status of the reference: {} to late.", feedback);
			}
		} catch (Exception e) {
			log.error("Job change status of reference has encounter an error: ", e);
			DBFactory.getInstance().closeSession();
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
}
