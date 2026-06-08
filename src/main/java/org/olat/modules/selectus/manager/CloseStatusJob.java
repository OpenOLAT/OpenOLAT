/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.modules.selectus.manager;

import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DBFactory;
import org.olat.core.commons.services.scheduler.JobWithDB;
import org.olat.core.gui.translator.Translator;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.i18n.I18nModule;
import org.quartz.DisallowConcurrentExecution;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

import org.olat.modules.selectus.AuditService;
import org.olat.modules.selectus.RecruitingModule;
import org.olat.modules.selectus.RecruitingService;
import org.olat.modules.selectus.model.Position;
import org.olat.modules.selectus.model.PositionStatus;
import org.olat.modules.selectus.model.RecruitingAuditLog.Action;
import org.olat.modules.selectus.model.RecruitingAuditLog.ActionTarget;
import org.olat.modules.selectus.ui.PositionController;
import org.olat.modules.selectus.ui.RecruitingHelper;

/**
 * 
 * Description:<br>
 * Change the status of position from published or published and in screening
 * to closed and in screening after the deadline.
 * 
 * <P>
 * Initial Date:  17 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
@DisallowConcurrentExecution
public class CloseStatusJob extends JobWithDB {
	
	private static final Logger log = Tracing.createLoggerFor(CloseStatusJob.class);
	
	public CloseStatusJob() {
		//make Spring happy
	}

	@Override
	public void executeWithDB(JobExecutionContext execContext) throws JobExecutionException {
		closePositionStatus();
		CoreSpringFactory.getImpl(CommitteeReminderSender.class).sendCommitteeReminder();
	}
	
	private void closePositionStatus() {
		try {
			Date now = new Date();
			
			AuditService auditService = CoreSpringFactory.getImpl(AuditService.class);
			RecruitingModule recruitingModule = CoreSpringFactory.getImpl(RecruitingModule.class);
			RecruitingService recruitingService = CoreSpringFactory.getImpl(RecruitingService.class);
			Translator translator = Util.createPackageTranslator(PositionController.class, I18nModule.getDefaultLocale());

			Locale defaultPositionLocale = recruitingModule.getPositionDefaultLocale();
			List<Position> positions = recruitingService.getPositions(PositionStatus.published, PositionStatus.publishedAndInScreening);
			for(Position position:positions) {
				Date deadline = position.getApplicationDeadline();
				if(deadline != null) {
					deadline = RecruitingHelper.endOfDay(deadline);
					if(now.after(deadline)) {
						String before = auditService.toAuditXml(position);
						String beforeStatus = position.getStatus();
						position.setStatus(PositionStatus.closedAndInScreening.name());
						position = recruitingService.savePosition(position);
						log.info(Tracing.M_AUDIT, "Set the status of the position: {} to closed and in screening.", position);
						
						// log
						String after = auditService.toAuditXml(position);
						String messageI18n = "audit.log.position.change.status";
						String[] messageArgs = new String[] { position.getMLTitle(defaultPositionLocale),
								translator.translate("status." + beforeStatus), translator.translate("status." + PositionStatus.closedAndInScreening) };
						auditService.auditPositionLog(Action.changeStatus, ActionTarget.position, before, after,
								messageI18n, messageArgs, translator, position, null);
					}
				}
			}

			List<Position> positionsToClose = recruitingService.getPositions(PositionStatus.published, PositionStatus.publishedAndInScreening, PositionStatus.closedAndInScreening);
			for(Position position:positionsToClose) {
				Date ratingDeadline = position.getRatingDeadline();
				if(ratingDeadline != null && now.after(ratingDeadline)) {
					String before = auditService.toAuditXml(position);
					String beforeStatus = position.getStatus();
					
					position.setStatus(PositionStatus.closedAndNoRating.name());
					recruitingService.savePosition(position);
					log.info(Tracing.M_AUDIT, "Set the status of the position: {} to closed and in screening.", position);
					
					// log
					String after = auditService.toAuditXml(position);
					String messageI18n = "audit.log.position.change.status";
					String[] messageArgs = new String[] { position.getPositionTitle(),
							translator.translate("status." + beforeStatus), translator.translate("status." + PositionStatus.closedAndNoRating.name()) };
					auditService.auditPositionLog(Action.changeStatus, ActionTarget.position, before, after,
							messageI18n, messageArgs, translator, position, null);
				}
			}
		} catch (Exception e) {
			log.error("Job change status of position has encounter an error: ", e);
			DBFactory.getInstance().closeSession();
		} finally {
			DBFactory.getInstance().commitAndCloseSession();
		}
	}
}
