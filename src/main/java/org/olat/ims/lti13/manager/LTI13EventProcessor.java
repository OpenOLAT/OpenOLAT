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
package org.olat.ims.lti13.manager;

import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.Authentication;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.manager.AuthenticationDAO;
import org.olat.basesecurity.model.IdentityRefImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.gui.control.Event;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.event.GenericEventListener;
import org.olat.course.CourseFactory;
import org.olat.course.CourseModule;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentChangedEvent;
import org.olat.course.assessment.CourseAssessmentService;
import org.olat.course.assessment.handler.AssessmentConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.AssessmentEvaluation;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
import org.olat.ims.lti13.LTI13Constants;
import org.olat.ims.lti13.LTI13Constants.ActivityProgress;
import org.olat.ims.lti13.LTI13Constants.GradingProgress;
import org.olat.ims.lti13.LTI13JsonUtil;
import org.olat.ims.lti13.LTI13Platform;
import org.olat.ims.lti13.LTI13Service;
import org.olat.ims.lti13.LTI13SharedToolDeployment;
import org.olat.ims.lti13.LTI13SharedToolService;
import org.olat.ims.lti13.LTI13SharedToolService.ServiceType;
import org.olat.ims.lti13.model.json.LineItemScore;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.repository.RepositoryService;
import org.olat.repository.model.RepositoryEntryRefImpl;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.github.scribejava.core.model.OAuth2AccessToken;

/**
 * 
 * Initial date: 8 mars 2021<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service
public class LTI13EventProcessor implements GenericEventListener, MessageListener,
		InitializingBean, DisposableBean {
	
	private static final Logger log = Tracing.createLoggerFor(LTI13EventProcessor.class);

	private final CoordinatorManager coordinatorManager;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private LTI13Service lti13Service;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private AuthenticationDAO authenticationDao;
	@Autowired
	private LTI13SharedToolServiceDAO sharedToolServiceDao;
	@Autowired
	private LTI13SharedToolDeploymentDAO sharedToolDeploymentDao;
	@Autowired
	private CourseAssessmentService courseAssessmentService;
	
	@Resource(name="ltiQueue")
	private Queue jmsQueue;
	private Session ltiSession;
	private MessageConsumer consumer;
	@Resource(name="ltiConnectionFactory")
	private ConnectionFactory connectionFactory;
	private QueueConnection connection;
	
	@Autowired
	public LTI13EventProcessor(CoordinatorManager coordinatorManager) {
		this.coordinatorManager = coordinatorManager;
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		coordinatorManager.getCoordinator().getEventBus()
			.registerFor(this, null, CourseModule.ORESOURCEABLE_TYPE_COURSE);
		
		//start the queue
		try {
			startQueue();
		} catch (JMSException e) {
			log.error("", e);
		}
	}
	
	@Override
	public void destroy() throws Exception {
		closeJms();
	}

	private void startQueue() throws JMSException {
		connection = (QueueConnection)connectionFactory.createConnection();
		connection.start();
		log.info("springInit: JMS connection started with connectionFactory={}", connectionFactory);

		//listen to the queue only if indexing node
		ltiSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = ltiSession.createConsumer(jmsQueue);
		consumer.setMessageListener(this);
	}
	
	private void closeJms() {
		if(consumer != null) {
			try {
				consumer.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
		if(connection != null) {
			try {
				ltiSession.close();
				connection.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
	}
	
	private Queue getJmsQueue() {
		return jmsQueue;
	}
	
	private void sendMessage(IdentityRef identity, LTI13SharedToolService service, String operation) {
		QueueSender sender;
		QueueSession session = null;
		try  {
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			MapMessage message = session.createMapMessage();
			message.setLong("identity", identity.getKey().longValue());
			message.setLong("service", service.getKey().longValue());
			message.setString("operation", operation);
			sender = session.createSender(getJmsQueue());
			sender.send( message );
		} catch (JMSException e) {
			log.error("", e);
		} finally {
			if(session != null) {
				try {
					session.close();
				} catch (JMSException e) {
					//last hope
				}
			}
		}
	}
	
	@Override
	public void onMessage(Message message) {
		try {
			if(message instanceof MapMessage) {
				MapMessage mm = (MapMessage)message;
				Long identityKey = mm.getLong("identity");
				Long serviceKey = mm.getLong("service");
				String operation = mm.getString("operation");
				LTI13SharedToolService service = sharedToolServiceDao.loadByKey(serviceKey);
				if("push-score".equals(operation) && service != null && identityKey != null) {
					pushScore(new IdentityRefImpl(identityKey), service);
				}
			}
		} catch (Exception e) {
			log.error("", e);
		} finally {
			dbInstance.commitAndCloseSession();
		}
	}

	@Override
	public void event(Event event) {
		if(AssessmentChangedEvent.TYPE_SCORE_EVAL_CHANGED.equals(event.getCommand())
				&& event instanceof AssessmentChangedEvent) {
			processCourseNodeAssessmentChangedEvent((AssessmentChangedEvent)event);
		}
	}
	
	private void processCourseNodeAssessmentChangedEvent(AssessmentChangedEvent event) {
		Long entryKey = event.getCourseEntryKey();
		Long identityKey = event.getIdentityKey();
		if(sharedToolDeploymentDao.hasDeployment(entryKey)) {
			IdentityRef identityRef = new IdentityRefImpl(identityKey);
			RepositoryEntryRef entryRef = new RepositoryEntryRefImpl(entryKey);
			List<LTI13SharedToolService> services = getServicesDeploiedFor(identityRef, entryRef);
			if(!services.isEmpty()) {
				for(LTI13SharedToolService service:services) {
					sendMessage(identityRef, service, "push-score");
				}
			}
		}
	}
	
	private void pushScore(IdentityRef identityRef, LTI13SharedToolService service) {
		String url = service.getEndpointUrl();
		LTI13SharedToolDeployment deployment = service.getDeployment();
		LTI13Platform platform = deployment.getPlatform();
		String deploymentId = deployment.getDeploymentId();
		log.info("Push to service lineitems {} {}", deploymentId, url);
		
		Identity identity = securityManager.loadIdentityByKey(identityRef.getKey());
		Authentication ltiUser = authenticationDao.getAuthentication(identity, LTI13Service.LTI_PROVIDER, platform.getIssuer());
		
		RepositoryEntry entry = repositoryService.loadByKey(deployment.getEntry().getKey());
		ICourse course = CourseFactory.loadCourse(entry);

		IdentityEnvironment identityEnv = new IdentityEnvironment(identity, null);
		UserCourseEnvironment assessedUserCourseEnv = new UserCourseEnvironmentImpl(identityEnv, course.getCourseEnvironment());
		ScoreAccounting scoreAccounting = assessedUserCourseEnv.getScoreAccounting();
		
		CourseNode rootNode = course.getRunStructure().getRootNode();
		AssessmentEvaluation assessmentEval = scoreAccounting.evalCourseNode(rootNode);
		if(assessmentEval == null) {
			return;
		}
	
		AssessmentConfig rootConfig = courseAssessmentService.getAssessmentConfig(entry, rootNode);
		Double maxScore = LTI13JsonUtil.convert(rootConfig.getMaxScore());
		if(maxScore == null) {
			maxScore = Double.valueOf(100.0d);// value is mandatory
		} else if(maxScore.doubleValue() < 0.0d) {
			maxScore = Double.valueOf(0.0d);
		}
		
		try {
			// Prepare score
			LineItemScore score = new LineItemScore();
			score.setTimestamp(new Date());
			score.setUserId(ltiUser.getAuthusername());
			score.setScoreGiven(LTI13JsonUtil.convert(assessmentEval.getScore()));
			score.setScoreMaximum(maxScore);
			score.setActivityProgress(ActivityProgress.valueOf(assessmentEval).name());
			score.setGradingProgress(GradingProgress.valueOf(assessmentEval).name());
			score.setComment(assessmentEval.getComment());
			
			List<String> scopes = List.of(LTI13Constants.Scopes.AGS_LINE_ITEM.url(), LTI13Constants.Scopes.AGS_SCORE.url());
			OAuth2AccessToken accessToken = lti13Service.getAccessToken(platform, scopes);		

			String urlScore = LTI13RequestUtil.scoreUrl(url);
			LTI13RequestUtil.post(accessToken.getAccessToken(), LTI13Constants.ContentTypes.SCORE_CONTENT_TYPE, urlScore, score);
		} catch (Exception e) {
			log.error("", e);
		}
	}
	
	private List<LTI13SharedToolService> getServicesDeploiedFor(IdentityRef identityRef, RepositoryEntryRef entryRef) {
		List<Authentication> authentications = authenticationDao.getAuthentications(identityRef);
		Set<String> validIssuers = new HashSet<>();
		for(Authentication authentication:authentications) {
			if(LTI13Service.LTI_PROVIDER.equals(authentication.getProvider())
					&& StringHelper.containsNonWhitespace(authentication.getIssuer())) {
				validIssuers.add(authentication.getIssuer());
			}
		}
		
		if(!validIssuers.isEmpty()) {
			return sharedToolServiceDao.getSharedToolServices(entryRef, ServiceType.lineitem, List.copyOf(validIssuers));	
		}
		return List.of();
	}
}
