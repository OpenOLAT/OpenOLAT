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
package org.olat.course.certificate.manager;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import java.util.Set;
import java.util.UUID;

import javax.annotation.Resource;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import javax.jms.Queue;
import javax.jms.QueueConnection;
import javax.jms.QueueSender;
import javax.jms.QueueSession;
import javax.jms.Session;
import javax.persistence.TypedQuery;

import org.apache.logging.log4j.Logger;
import org.apache.velocity.app.VelocityEngine;
import org.olat.admin.user.imp.TransientIdentity;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityModule;
import org.olat.basesecurity.GroupRoles;
import org.olat.basesecurity.IdentityRef;
import org.olat.basesecurity.IdentityRelationshipService;
import org.olat.basesecurity.IdentityToIdentityRelation;
import org.olat.basesecurity.OrganisationRoles;
import org.olat.basesecurity.RelationRight;
import org.olat.basesecurity.RelationSearchParams;
import org.olat.basesecurity.SearchIdentityParams;
import org.olat.core.commons.modules.bc.FolderModule;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.pdf.PdfModule;
import org.olat.core.commons.services.pdf.PdfService;
import org.olat.core.gui.translator.Translator;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OrganisationRef;
import org.olat.core.id.Roles;
import org.olat.core.id.UserConstants;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.Formatter;
import org.olat.core.util.StringHelper;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.ZipUtil;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.i18n.I18nManager;
import org.olat.core.util.mail.MailBundle;
import org.olat.core.util.mail.MailManager;
import org.olat.core.util.mail.MailerResult;
import org.olat.core.util.vfs.FileStorage;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.VFSManager;
import org.olat.course.CorruptedCourseException;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.certificate.Certificate;
import org.olat.course.certificate.CertificateEmailRightProvider;
import org.olat.course.certificate.CertificateEvent;
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateStatus;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.CertificatesModule;
import org.olat.course.certificate.EmailStatus;
import org.olat.course.certificate.RecertificationTimeUnit;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.certificate.model.CertificateStandalone;
import org.olat.course.certificate.model.CertificateTemplateImpl;
import org.olat.course.certificate.model.JmsCertificateWork;
import org.olat.course.certificate.model.PreviewCertificate;
import org.olat.course.certificate.ui.CertificateController;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.manager.BusinessGroupRelationDAO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntrySecurity;
import org.olat.repository.RepositoryManager;
import org.olat.repository.RepositoryService;
import org.olat.resource.OLATResource;
import org.olat.user.UserDataExportable;
import org.olat.user.UserManager;
import org.olat.user.manager.ManifestBuilder;
import org.olat.user.propertyhandlers.DatePropertyHandler;
import org.olat.user.propertyhandlers.UserPropertyHandler;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 20.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Service("certificatesManager")
public class CertificatesManagerImpl implements CertificatesManager, MessageListener,
		InitializingBean, DisposableBean, UserDataExportable {
	
	private static final Logger log = Tracing.createLoggerFor(CertificatesManagerImpl.class);

	private VelocityEngine velocityEngine;
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private PdfModule pdfModule;
	@Autowired
	private PdfService pdfService;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private BaseSecurityModule baseSecurityModule;
	@Autowired
	private IdentityRelationshipService identityRelationshipService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private BusinessGroupRelationDAO businessGroupRelationDao;
	@Autowired
	private CoordinatorManager coordinatorManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private FolderModule folderModule;
	@Autowired
	private CertificatesModule certificatesModule;
	

	@Resource(name="certificateQueue")
	private Queue jmsQueue;
	private Session certificateSession;
	private MessageConsumer consumer;
	@Resource(name="certificateConnectionFactory")
	private ConnectionFactory connectionFactory;
	private QueueConnection connection;

	private Boolean phantomAvailable;
	private FileStorage usersStorage;
	private FileStorage templatesStorage;
	
	@Override
	public void afterPropertiesSet() {
		//create the folders
		getCertificateTemplatesRoot();
		templatesStorage = new FileStorage(getCertificateTemplatesRootContainer());
		getCertificateRoot();
		usersStorage = new FileStorage(getCertificateRootContainer());
		
		Properties p = new Properties();
		try {
			velocityEngine = new VelocityEngine();
			velocityEngine.init(p);
		} catch (Exception e) {
			throw new RuntimeException("config error " + p);
		}
		
		//deploy script
		try(InputStream inRasteriez = CertificatesManager.class.getResourceAsStream("rasterize.js")) {
			Path rasterizePath = getRasterizePath();
			Files.copy(inRasteriez, rasterizePath, StandardCopyOption.REPLACE_EXISTING);	
		} catch(Exception e) {
			log.error("Can not read rasterize.js library for PhantomJS PDF generation", e);
		}
		try(InputStream inQRCodeLib = CertificatesManager.class.getResourceAsStream("qrcode.min.js")) {
			Path qrCodeLibPath = getQRCodeLibPath();
			Files.copy(inQRCodeLib, qrCodeLibPath, StandardCopyOption.REPLACE_EXISTING);	
		} catch(Exception e) {
			log.error("Can not read qrcode.min.js for QR Code PDF generation", e);
		}
		
		//start the queues
		try {
			startQueue();
		} catch (JMSException e) {
			log.error("", e);
		}
	}
	
	private void startQueue() throws JMSException {
		connection = (QueueConnection)connectionFactory.createConnection();
		connection.start();
		log.info("springInit: JMS connection started with connectionFactory=" + connectionFactory);

		//listen to the queue only if indexing node
		certificateSession = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
		consumer = certificateSession.createConsumer(jmsQueue);
		consumer.setMessageListener(this);
	}

	@Override
	public void destroy() throws Exception {
		closeJms();
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
				certificateSession.close();
				connection.close();
			} catch (JMSException e) {
				log.error("", e);
			}
		}
	}
	
	private Queue getJmsQueue() {
		return jmsQueue;
	}

	@Override
	public boolean isHTMLTemplateAllowed() {
		return pdfModule.isEnabled() || isPhantomAvailable();
	}

	private boolean isPhantomAvailable() {
		if(phantomAvailable == null) {
			phantomAvailable = CertificatePhantomWorker.checkPhantomJSAvailabilty();
		}
		return phantomAvailable.booleanValue();
	}

	@Override
	public SubscriptionContext getSubscriptionContext(ICourse course) {
		CourseNode cn = course.getRunStructure().getRootNode();
		CourseEnvironment ce = course.getCourseEnvironment();
		return new SubscriptionContext(ORES_CERTIFICATE, ce.getCourseResourceableId(), cn.getIdent());
	}

	@Override
	public PublisherData getPublisherData(ICourse course, String businessPath) {
		String data = String.valueOf(course.getCourseEnvironment().getCourseResourceableId());
		return new PublisherData(ORES_CERTIFICATE, data, businessPath);
	}

	@Override
	public void markPublisherNews(Identity ident, ICourse course) {
		SubscriptionContext subsContext = getSubscriptionContext(course);
		if (subsContext != null) {
			notificationsManager.markPublisherNews(subsContext, ident, true);
		}
	}
	
	public void markPublisherNews(Identity ident, OLATResource courseResource) {
		ICourse course = CourseFactory.loadCourse(courseResource);
		SubscriptionContext subsContext = getSubscriptionContext(course);
		if (subsContext != null) {
			notificationsManager.markPublisherNews(subsContext, ident, true);
		}
	}
	
	@Override
	public int deleteRepositoryEntry(RepositoryEntry re) {
		StringBuilder sb = new StringBuilder();
		sb.append("update certificate set olatResource = null where olatResource.key=:resourceKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString())
				.setParameter("resourceKey", re.getOlatResource().getKey())
				.executeUpdate();
	}

	@Override
	public List<OLATResource> getResourceWithCertificates() {
		StringBuilder sb = new StringBuilder();
		sb.append("select distinct resource from certificate cer")
		  .append(" inner join cer.olatResource resource");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), OLATResource.class)
				.getResultList();
	}

	@Override
	public VFSLeaf getCertificateLeaf(Certificate certificate) {
		VFSContainer cerContainer = getCertificateRootContainer();
		VFSItem cerItem = null;
		if(StringHelper.containsNonWhitespace(certificate.getPath())) {
			cerItem = cerContainer.resolve(certificate.getPath());
		}
		return cerItem instanceof VFSLeaf ? (VFSLeaf)cerItem : null;
	}
	
	private File getCertificateFile(Certificate certificate) {
		File file = getCertificateRoot();
		if(StringHelper.containsNonWhitespace(certificate.getPath())) {
			return new File(file, certificate.getPath());
		}
		return null;
	}
	
	@Override
	public CertificateImpl getCertificateById(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificate cer")
		  .append(" inner join fetch cer.identity ident")
		  .append(" inner join fetch ident.user identUser")
		  .append(" where cer.key=:certificateKey");
		List<CertificateImpl> certificates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CertificateImpl.class)
				.setParameter("certificateKey", key)
				.getResultList();
		return certificates.isEmpty() ? null : certificates.get(0);
	}
	
	@Override
	public CertificateLight getCertificateLightById(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificatelight cer")
		  .append(" where cer.key=:certificateKey");
		List<CertificateLight> certificates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CertificateLight.class)
				.setParameter("certificateKey", key)
				.getResultList();
		return certificates.isEmpty() ? null : certificates.get(0);
	}

	@Override
	public Certificate getCertificateByUuid(String uuid) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificate cer")
		  .append(" where cer.uuid=:uuid");
		List<Certificate> certificates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Certificate.class)
				.setParameter("uuid", uuid)
				.getResultList();
		return certificates.isEmpty() ? null : certificates.get(0);
	}
	
	@Override
	public boolean hasCertificate(IdentityRef identity, Long resourceKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer.key from certificate cer")
		  .append(" where (cer.olatResource.key=:resourceKey or cer.archivedResourceKey=:resourceKey)")
		  .append(" and cer.identity.key=:identityKey");
		List<Number> certififcates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Number.class)
				.setParameter("resourceKey", resourceKey)
				.setParameter("identityKey", identity.getKey())
				.setFirstResult(0)
				.setMaxResults(1)
				.getResultList();
		return certififcates != null && !certififcates.isEmpty();
	}

	@Override
	public List<CertificateLight> getLastCertificates(IdentityRef identity) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificatelight cer")
		  .append(" where cer.identityKey=:identityKey and cer.last=true");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CertificateLight.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	@Override
	public Certificate getLastCertificate(IdentityRef identity, Long resourceKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificate cer")
		  .append(" where (cer.olatResource.key=:resourceKey or cer.archivedResourceKey=:resourceKey)")
		  .append(" and cer.identity.key=:identityKey and cer.last=true order by cer.creationDate");
		List<Certificate> certififcates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Certificate.class)
				.setParameter("resourceKey", resourceKey)
				.setParameter("identityKey", identity.getKey())
				.setMaxResults(1)
				.getResultList();
		return certififcates.isEmpty() ? null : certififcates.get(0);
	}

	@Override
	public List<Certificate> getCertificatesForNotifications(Identity identity, RepositoryEntry entry, Date lastNews) {
		Roles roles = securityManager.getRoles(identity);
		RepositoryEntrySecurity security = repositoryManager.isAllowed(identity, roles, entry);
		if(!security.isEntryAdmin() && !security.isCoach() && !security.isParticipant()) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificate cer")
		  .append(" inner join fetch cer.identity ident")
		  .append(" where cer.olatResource.key=:resourceKey and cer.last=true ");
		//must be some kind of restrictions
		boolean securityCheck = false;
		if(!security.isEntryAdmin()) {
			sb.append(" and (");
			boolean or = false;
			if(security.isCoach()) {
				or = or(sb, or);
				sb.append(" exists (select participant.identity.key from repoentrytogroup as rel, bgroupmember as participant, bgroupmember as coach")
				  .append("    where rel.entry.key=:repoEntryKey")
		          .append("      and rel.group.key=coach.group.key and coach.role='").append(GroupRoles.coach.name()).append("' and coach.identity.key=:identityKey")
		          .append("      and rel.group.key=participant.group.key and participant.identity.key=ident.key and participant.role='").append(GroupRoles.participant.name()).append("'")
		          .append(" )");
				securityCheck = true;
			}
			if(security.isParticipant()) {
				or = or(sb, or);
				sb.append(" ident.key=:identityKey");
				securityCheck = true;
			}
			sb.append(")");
		} else {
			securityCheck = true;
		}
		if(!securityCheck) {
			return Collections.emptyList();
		}
		sb.append(" order by cer.creationDate");

		TypedQuery<Certificate> certificates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Certificate.class)
				.setParameter("resourceKey", entry.getOlatResource().getKey());
		if(!security.isEntryAdmin()) {
			if(security.isCoach()) {
				certificates.setParameter("repoEntryKey", entry.getKey());
			}
			if(security.isCoach() || security.isParticipant()) {
				certificates.setParameter("identityKey", identity.getKey());
			}
		}

		return certificates.getResultList();
	}
	
	private final boolean or(StringBuilder sb, boolean or) {
		if(or) sb.append(" or ");
		else sb.append(" ");
		return true;
	}
	
	@Override
	public List<Certificate> getCertificates(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificate cer")
		  .append(" where cer.olatResource.key=:resourceKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Certificate.class)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
	}

	@Override
	public List<Certificate> getCertificates(IdentityRef identity, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificate cer")
		  .append(" where cer.olatResource.key=:resourceKey and cer.identity.key=:identityKey order by cer.creationDate desc");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Certificate.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}
	
	public List<Certificate> getCertificates(IdentityRef identity) {
		String query = "select cer from certificate cer where cer.identity.key=:identityKey";
		return dbInstance.getCurrentEntityManager()
				.createQuery(query, Certificate.class)
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	@Override
	public List<CertificateLight> getLastCertificates(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificatelight cer")
		  .append(" where cer.olatResourceKey=:resourceKey and cer.last=true");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CertificateLight.class)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
	}
	
	@Override
	public List<CertificateLight> getLastCertificates(BusinessGroup businessGroup) {
		List<BusinessGroup> groups = Collections.singletonList(businessGroup);
		List<RepositoryEntry> entries = businessGroupRelationDao.findRepositoryEntries(groups, 0, -1);
		if(entries.isEmpty()) {// no courses, no certificates
			return new ArrayList<>();
		}
		
		List<Long> resourceKeys = new ArrayList<>(entries.size());
		for(RepositoryEntry entry:entries) {
			resourceKeys.add(entry.getOlatResource().getKey());
		}
		
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificatelight cer")
		  .append(" where cer.olatResourceKey in (:resourceKeys) and cer.last=true");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CertificateLight.class)
				.setParameter("resourceKeys", resourceKeys)
				.getResultList();
	}

	@Override
	public boolean isCertificationAllowed(Identity identity, RepositoryEntry entry) {
		boolean allowed = false;
		try {
			ICourse course = CourseFactory.loadCourse(entry);
			CourseConfig config = course.getCourseEnvironment().getCourseConfig();
			if(config.isRecertificationEnabled()) {
				Certificate certificate =  getLastCertificate(identity, entry.getOlatResource().getKey());
				if(certificate == null) {
					allowed = true;
				} else {
					Calendar cal = Calendar.getInstance();
					Date now = cal.getTime();
					Date nextCertificationDate = getDateNextRecertification(certificate, config);
					allowed = (nextCertificationDate != null ? nextCertificationDate.before(now) : false);
				}
			} else {
				allowed = !hasCertificate(identity, entry.getOlatResource().getKey());
			}
		} catch (CorruptedCourseException e) {
			log.error("", e);
		}
		return allowed;
	}
	
	@Override
	public Date getDateNextRecertification(Certificate certificate, RepositoryEntry entry) {
		ICourse course = CourseFactory.loadCourse(entry);
		CourseConfig config = course.getCourseEnvironment().getCourseConfig();
		return getDateNextRecertification(certificate, config);
	}

	private Date getDateNextRecertification(Certificate certificate, CourseConfig config) {
		if(config.isRecertificationEnabled() && certificate != null) {
			int time = config.getRecertificationTimelapse();
			RecertificationTimeUnit timeUnit = config.getRecertificationTimelapseUnit();
			Date date = certificate.getCreationDate();
			Calendar cal = Calendar.getInstance();
			cal.setTime(date);
			switch(timeUnit) {
				case day: cal.add(Calendar.DATE, time); break;
				case week: cal.add(Calendar.DATE, time * 7); break;
				case month: cal.add(Calendar.MONTH, time); break;
				case year: cal.add(Calendar.YEAR, time); break;
			}
			return cal.getTime();
		}		
		return null;
	}

	
	@Override
	public void deleteCertificate(Certificate certificate) {
		File certificateFile = getCertificateFile(certificate);
		if(certificateFile != null && certificateFile.exists()) {
			try {
				FileUtils.deleteDirsAndFiles(certificateFile.getParentFile().toPath());
			} catch (IOException e) {
				log.error("", e);
			}
		}
		CertificateImpl relaodedCertificate = dbInstance.getCurrentEntityManager()
				.getReference(CertificateImpl.class, certificate.getKey());
		dbInstance.getCurrentEntityManager().remove(relaodedCertificate);
		
		//reorder the last flag
		List<Certificate> certificates = getCertificates(relaodedCertificate.getIdentity(), relaodedCertificate.getOlatResource());
		certificates.remove(relaodedCertificate);
		if(!certificates.isEmpty()) {
			boolean hasLast = false;
			for(Certificate cer:certificates) {
				if(((CertificateImpl)cer).isLast()) {
					hasLast = true;
				}
			}
			
			if(!hasLast) {
				CertificateImpl newLastCertificate = (CertificateImpl)certificates.get(0);
				newLastCertificate.setLast(true);
				dbInstance.getCurrentEntityManager().merge(newLastCertificate);
			}
		}
	}

	@Override
	public Certificate uploadCertificate(Identity identity, Date creationDate, OLATResource resource, File certificateFile) {
		CertificateImpl certificate = new CertificateImpl();
		certificate.setOlatResource(resource);
		certificate.setArchivedResourceKey(resource.getKey());
		if(creationDate != null) {
			certificate.setCreationDate(creationDate);
		}
		RepositoryEntry entry = repositoryService.loadByResourceKey(resource.getKey());
		if(entry != null) {
			certificate.setCourseTitle(entry.getDisplayname());
		}
		certificate.setLastModified(certificate.getCreationDate());
		certificate.setIdentity(identity);
		certificate.setUuid(UUID.randomUUID().toString());
		certificate.setLast(true);
		certificate.setStatus(CertificateStatus.ok);

		String dir = usersStorage.generateDir();
		try (InputStream in = Files.newInputStream(certificateFile.toPath())) {
			File dirFile = new File(getCertificateRoot(), dir);
			dirFile.mkdirs();

			File storedCertificateFile = new File(dirFile, "Certificate.pdf");
			Files.copy(in, storedCertificateFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			certificate.setPath(dir + storedCertificateFile.getName());

			Date dateFirstCertification = getDateFirstCertification(identity, resource.getKey());
			if (dateFirstCertification != null) {
				removeLastFlag(identity, resource.getKey());
			}

			dbInstance.getCurrentEntityManager().persist(certificate);
		} catch (Exception e) {
			log.error("", e);
		}

		return certificate;
	}
	
	@Override
	public Certificate uploadStandaloneCertificate(Identity identity, Date creationDate, String courseTitle, Long resourceKey, File certificateFile) {
		CertificateStandalone certificate = new CertificateStandalone();
		certificate.setArchivedResourceKey(resourceKey);
		if(creationDate != null) {
			certificate.setCreationDate(creationDate);
			certificate.setLastModified(new Date());
		} else {
			certificate.setCreationDate(new Date());
			certificate.setLastModified(certificate.getCreationDate());
		}
		certificate.setIdentity(identity);
		certificate.setUuid(UUID.randomUUID().toString());
		certificate.setLast(true);
		certificate.setCourseTitle(courseTitle);
		certificate.setStatus(CertificateStatus.ok);

		String dir = usersStorage.generateDir();
		try (InputStream in = Files.newInputStream(certificateFile.toPath())) {
			File dirFile = new File(getCertificateRoot(), dir);
			dirFile.mkdirs();

			File storedCertificateFile = new File(dirFile, "Certificate.pdf");
			Files.copy(in, storedCertificateFile.toPath(), StandardCopyOption.REPLACE_EXISTING);

			certificate.setPath(dir + storedCertificateFile.getName());
			
			Date dateFirstCertification = getDateFirstCertification(identity, resourceKey);
			if (dateFirstCertification != null) {
				removeLastFlag(identity, resourceKey);
			}

			dbInstance.getCurrentEntityManager().persist(certificate);
		} catch (Exception e) {
			log.error("", e);
		}

		return certificate;
	}

	@Override
	public void generateCertificates(List<CertificateInfos> certificateInfos, RepositoryEntry entry,
			CertificateTemplate template, CertificateConfig config) {
		int count = 0;
		for(CertificateInfos certificateInfo:certificateInfos) {
			generateCertificate(certificateInfo, entry, template, config);
			if(++count % 10 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		markPublisherNews(null, entry.getOlatResource());
	}

	@Override
	public PreviewCertificate previewCertificate(CertificateTemplate template, RepositoryEntry entry, Locale locale, String custom1,
			String custom2, String custom3) {
		Identity identity = getPreviewIdentity();
		
		File certificateFile;
		File dirFile = new File(WebappHelper.getTmpDir(), UUID.randomUUID().toString());
		dirFile.mkdirs();
		
		StringBuilder sb = new StringBuilder();
		sb.append(Settings.getServerContextPathURI()).append("/certificate/")
		  .append(UUID.randomUUID()).append("/preview.pdf");
		String certUrl = sb.toString();
		
		if(template == null) {
			CertificatePDFFormWorker worker = new CertificatePDFFormWorker(identity, entry, 2.0f, 10.0f, true, 0.4,
					new Date(), new Date(), new Date(), custom1, custom2, custom3, certUrl, locale, userManager, this);
			certificateFile = worker.fill(null, dirFile, "Certificate.pdf");
		} else if(template.getPath().toLowerCase().endsWith("pdf")) {
			CertificatePDFFormWorker worker = new CertificatePDFFormWorker(identity, entry, 2.0f, 10.0f, true, 0.4,
					new Date(), new Date(), new Date(), custom1, custom2, custom3, certUrl, locale, userManager, this);
			certificateFile = worker.fill(template, dirFile, "Certificate.pdf");
		} else if (pdfModule.isEnabled()) {
			CertificatePdfServiceWorker worker = new CertificatePdfServiceWorker(identity, entry, 2.0f, 10.0f, true,
					0.4, new Date(), new Date(), new Date(), custom1, custom2, custom3, certUrl, locale, userManager,
					this, pdfService);
			certificateFile = worker.fill(template, dirFile, "Certificate.pdf");
		} else {
			CertificatePhantomWorker worker = new CertificatePhantomWorker(identity, entry, 2.0f, 10.0f, true, 0.4,
					new Date(), new Date(), new Date(), custom1, custom2, custom3, certUrl, locale, userManager, this);
			certificateFile = worker.fill(template, dirFile, "Certificate.pdf");
		}
		return new PreviewCertificate(certificateFile, dirFile);
	}
	
	private Identity getPreviewIdentity() {
		TransientIdentity identity = new TransientIdentity();
		identity.setName("username");
		List<UserPropertyHandler> userPropertyHandlers = userManager.getAllUserPropertyHandlers();
		for(UserPropertyHandler handler:userPropertyHandlers) {
			if(handler instanceof DatePropertyHandler) {
				identity.getUser().setProperty(handler.getName(), Formatter.formatDatetime(new Date()));
			} else {
				identity.getUser().setProperty(handler.getName(), handler.getName());
			}
		}
		return identity;
	}

	@Override
	public Certificate generateCertificate(CertificateInfos certificateInfos, RepositoryEntry entry,
			CertificateTemplate template, CertificateConfig config) {
		Certificate certificate = persistCertificate(certificateInfos, entry, template, config);
		markPublisherNews(null, entry.getOlatResource());
		return certificate;
	}

	private Certificate persistCertificate(CertificateInfos certificateInfos, RepositoryEntry entry,
			CertificateTemplate template, CertificateConfig config) {
		OLATResource resource = entry.getOlatResource();
		Identity identity = certificateInfos.getAssessedIdentity();
		
		CertificateImpl certificate = new CertificateImpl();
		certificate.setOlatResource(resource);
		certificate.setArchivedResourceKey(resource.getKey());
		if(certificateInfos.getCreationDate() != null) {
			certificate.setCreationDate(certificateInfos.getCreationDate());
		} else {
			certificate.setCreationDate(new Date());
		}
		certificate.setLastModified(certificate.getCreationDate());
		certificate.setIdentity(identity);
		certificate.setUuid(UUID.randomUUID().toString());
		certificate.setLast(true);
		certificate.setCourseTitle(entry.getDisplayname());
		certificate.setStatus(CertificateStatus.pending);
		
		Date nextCertification = getDateNextRecertification(certificate, entry);
		certificate.setNextRecertificationDate(nextCertification);
		
		dbInstance.getCurrentEntityManager().persist(certificate);
		dbInstance.commit();
		
		//send message
		sendJmsCertificateFile(certificate, template, certificateInfos.getScore(), certificateInfos.getMaxScore(),
				certificateInfos.getPassed(), certificateInfos.getProgress(), config);

		return certificate;
	}
	
	protected VelocityEngine getVelocityEngine() {
		return velocityEngine;
	}
	
	private void sendJmsCertificateFile(Certificate certificate, CertificateTemplate template, Float score,
			Float maxScore, Boolean passed, Double completion, CertificateConfig config) {
		QueueSender sender;
		QueueSession session = null;
		try  {
			JmsCertificateWork workUnit = new JmsCertificateWork();
			workUnit.setCertificateKey(certificate.getKey());
			if(template != null) {
				workUnit.setTemplateKey(template.getKey());
			}
			workUnit.setScore(score);
			workUnit.setMaxScore(maxScore);
			workUnit.setPassed(passed);
			workUnit.setCompletion(completion);
			workUnit.setConfig(config);
			
			session = connection.createQueueSession(false, Session.AUTO_ACKNOWLEDGE);
			ObjectMessage message = session.createObjectMessage();
			message.setObject(workUnit);

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
		if(message instanceof ObjectMessage) {
			try {
				ObjectMessage objMsg = (ObjectMessage)message;
				JmsCertificateWork workUnit = (JmsCertificateWork)objMsg.getObject();
				doCertificate(workUnit);
				message.acknowledge();
			} catch (JMSException e) {
				log.error("", e);
			} finally {
				dbInstance.commitAndCloseSession();
			}
		}
	}
	
	private void doCertificate(JmsCertificateWork workUnit) {
		CertificateImpl certificate = getCertificateById(workUnit.getCertificateKey());
		CertificateTemplate template = null;
		if(workUnit.getTemplateKey() != null) {
			template = getTemplateById(workUnit.getTemplateKey());
		}
		OLATResource resource = certificate.getOlatResource();
		Identity identity = certificate.getIdentity();
		RepositoryEntry entry = repositoryService.loadByResourceKey(resource.getKey());
		
		String dir = usersStorage.generateDir();
		File dirFile = new File(getCertificateRoot(), dir);
		dirFile.mkdirs();
		
		Float score = workUnit.getScore();
		Float maxScore = workUnit.getMaxScore();
		String lang = identity.getUser().getPreferences().getLanguage();
		Locale locale = i18nManager.getLocaleOrDefault(lang);
		Boolean passed = workUnit.getPassed();
		Double completion = workUnit.getCompletion();
		Date dateCertification = certificate.getCreationDate();
		Date dateFirstCertification = getDateFirstCertification(identity, resource.getKey());
		Date dateNextRecertification = certificate.getNextRecertificationDate();
		String custom1 = workUnit.getConfig().getCustom1();
		String custom2 = workUnit.getConfig().getCustom2();
		String custom3 = workUnit.getConfig().getCustom3();
		
		File certificateFile;
		// File name with user name
		StringBuilder sb = new StringBuilder();
		sb.append(identity.getUser().getProperty(UserConstants.LASTNAME, locale)).append("_")
		  .append(identity.getUser().getProperty(UserConstants.FIRSTNAME, locale)).append("_")
		  .append(entry.getDisplayname()).append("_")
		  .append(Formatter.formatShortDateFilesystem(dateCertification));
		String filename = FileUtils.normalizeFilename(sb.toString()) + ".pdf";
		// External URL to certificate as short as possible for QR-Code
		sb = new StringBuilder();
		sb.append(Settings.getServerContextPathURI()).append("/certificate/")
		  .append(certificate.getUuid()).append("/certificate.pdf");
		String certUrl = sb.toString();
		
		if(template == null || template.getPath().toLowerCase().endsWith("pdf")) {
			CertificatePDFFormWorker worker = new CertificatePDFFormWorker(identity, entry, score, maxScore, passed,
					completion, dateCertification, dateFirstCertification, dateNextRecertification, custom1, custom2,
					custom3, certUrl, locale, userManager, this);
			certificateFile = worker.fill(template, dirFile, filename);
			if(certificateFile == null) {
				certificate.setStatus(CertificateStatus.error);
			} else {
				certificate.setStatus(CertificateStatus.ok);
			}
		} else {
			if(pdfModule.isEnabled()) {
				CertificatePdfServiceWorker worker = new CertificatePdfServiceWorker(identity, entry, score, maxScore,
						passed, completion, dateCertification, dateFirstCertification, dateNextRecertification, custom1,
						custom2, custom3, certUrl, locale, userManager, this, pdfService);
				certificateFile = worker.fill(template, dirFile, filename);
			} else {
				CertificatePhantomWorker worker = new CertificatePhantomWorker(identity, entry, score, maxScore, passed,
						completion, dateCertification, dateFirstCertification, dateNextRecertification, custom1,
						custom2, custom3, certUrl, locale, userManager, this);
				certificateFile = worker.fill(template, dirFile, filename);
			}
			if(certificateFile == null) {
				certificate.setStatus(CertificateStatus.error);
			} else {
				certificate.setStatus(CertificateStatus.ok);
			}
		}

		certificate.setPath(dir + certificateFile.getName());
		if(dateFirstCertification != null) {
			//not the first certification, reset the last of the others certificates
			removeLastFlag(identity, resource.getKey());
		}
		MailerResult result = sendCertificate(identity, entry, certificateFile, workUnit.getConfig());
		if(result.isSuccessful()) {
			certificate.setEmailStatus(EmailStatus.ok);
		} else {
			certificate.setEmailStatus(EmailStatus.error);
		}
		dbInstance.getCurrentEntityManager().merge(certificate);
		dbInstance.commit();
		
		CertificateEvent event = new CertificateEvent(identity.getKey(), certificate.getKey(), resource.getKey());
		coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(event, ORES_CERTIFICATE_EVENT);
	}
	
	private MailerResult sendCertificate(Identity to, RepositoryEntry entry, File certificateFile, CertificateConfig config) {
		MailerResult mailerResult = sendCertificate(to, entry, certificateFile);
		sendCertificateCopies(to, entry, certificateFile, config);
		return mailerResult;
	}

	private MailerResult sendCertificate(Identity to, RepositoryEntry entry, File certificateFile) {
		MailBundle bundle = new MailBundle();
		bundle.setToId(to);
		bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
		
		String[] args = new String[] {
			entry.getDisplayname(),
			userManager.getUserDisplayName(to)
		};
		
		String userLanguage = to.getUser().getPreferences().getLanguage();
		Locale locale = i18nManager.getLocaleOrDefault(userLanguage);
		Translator translator = Util.createPackageTranslator(CertificateController.class, locale);
		String subject = translator.translate("certification.email.subject", args);
		String body = translator.translate("certification.email.body", args);
		bundle.setContent(subject, body, certificateFile);
		return mailManager.sendMessage(bundle);
	}
	
	private void sendCertificateCopies(Identity to, RepositoryEntry entry, File certificateFile, CertificateConfig config) {
		String entryDisplayName = entry.getDisplayname();
		String toDisplayName = userManager.getUserDisplayName(to);
		String toUserLanguage = to.getUser().getPreferences().getLanguage();

		List<MailBundle> mailBundles = new ArrayList<>();
		List<String> bccs = certificatesModule.getCertificatesBccEmails();
		if(config.isSendEmailBcc()) {
			for (String bcc : bccs) {
				MailBundle bundle = createCopyMailBundle(certificateFile, toUserLanguage, entryDisplayName, toDisplayName);
				bundle.setTo(bcc);
				mailBundles.add(bundle);
			}
		}
		
		Set<Identity> copiesTo = new HashSet<>();
		if (config.isSendEmailLinemanager() && certificatesModule.isCertificateLinemanager()) {
			List<Identity> linemanagers = getLinemanagers(to);
			copiesTo.addAll(linemanagers);
		}
		if (config.isSendEmailIdentityRelations() && baseSecurityModule.isRelationRoleEnabled()) {
			RelationSearchParams searchParams = new RelationSearchParams();
			RelationRight right = identityRelationshipService.getRelationRightByRight(CertificateEmailRightProvider.RELATION_RIGHT);
			searchParams.setRight(right);
			List<IdentityToIdentityRelation> relationTargets = identityRelationshipService.getRelationsAsTarget(to, searchParams);
			relationTargets.forEach(target -> { copiesTo.add( target.getSource()); });
		}
		
		for (Identity copyTo : copiesTo) {
			String language = copyTo.getUser().getPreferences().getLanguage();
			MailBundle bundle = createCopyMailBundle(certificateFile, language, entryDisplayName, toDisplayName);
			bundle.setToId(copyTo);
			mailBundles.add(bundle);
		}
		
		for (MailBundle mailBundle : mailBundles) {
			mailManager.sendMessage(mailBundle);
		}
	}

	private MailBundle createCopyMailBundle(File certificateFile, String language, String entryDisplayName, String toDisplayName) {
		String[] args = new String[] {
				entryDisplayName,
				toDisplayName
		};
		MailBundle bundle = new MailBundle();
		bundle.setFrom(WebappHelper.getMailConfig("mailReplyTo"));
		Locale locale = i18nManager.getLocaleOrDefault(language);
		Translator translator = Util.createPackageTranslator(CertificateController.class, locale);
		String subject = translator.translate("certification.email.copy.subject", args);
		String body = translator.translate("certification.email.copy.body", args);
		bundle.setContent(subject, body, certificateFile);
		return bundle;
	}
	
	private List<Identity> getLinemanagers(Identity identity) {
		Roles roles = securityManager.getRoles(identity);
		List<OrganisationRef> identityOrgs = roles.getOrganisationsWithRole(OrganisationRoles.user);
		SearchIdentityParams identityParams = new SearchIdentityParams();
		identityParams.setOrganisations(identityOrgs);
		identityParams.setRoles(new OrganisationRoles[]{ OrganisationRoles.linemanager });
		identityParams.setStatus(Identity.STATUS_VISIBLE_LIMIT);
		return securityManager.getIdentitiesByPowerSearch(identityParams, 0, -1);
	}

	private Date getDateFirstCertification(Identity identity, Long resourceKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer.creationDate from certificate cer")
		  .append(" where cer.olatResource.key=:resourceKey and cer.identity.key=:identityKey")
		  .append(" order by cer.creationDate asc");
		
		List<Date> dates = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Date.class)
				.setParameter("resourceKey", resourceKey)
				.setParameter("identityKey", identity.getKey())
				.setMaxResults(1)
				.getResultList();
		return dates.isEmpty() ? null : dates.get(0);
	}
	
	private void removeLastFlag(Identity identity, Long resourceKey) {
		StringBuilder sb = new StringBuilder();
		sb.append("update certificate cer set cer.last=false")
		  .append(" where cer.olatResource.key=:resourceKey and cer.identity.key=:identityKey");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
				.setParameter("resourceKey", resourceKey)
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}
	

	@Override
	public List<CertificateTemplate> getTemplates() {
		String sb = "select template from certificatetemplate template where template.publicTemplate=true order by template.creationDate desc";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, CertificateTemplate.class)
				.getResultList();
	}
	
	@Override
	public CertificateTemplate getTemplateById(Long key) {
		String sb = "select template from certificatetemplate template where template.key=:templateKey";
		List<CertificateTemplate> templates = dbInstance.getCurrentEntityManager()
				.createQuery(sb, CertificateTemplate.class)
				.setParameter("templateKey", key)
				.getResultList();
		return templates.isEmpty() ? null : templates.get(0);
	}

	@Override
	public void deleteTemplate(CertificateTemplate template) {
		File templateFile = getTemplateFile(template);
		if(templateFile != null && templateFile.getParent() != null && templateFile.getParentFile().exists()) {
			try {
				FileUtils.deleteDirsAndFiles(templateFile.getParentFile().toPath());
			} catch (IOException e) {
				log.error("", e);
			}
		}
		//delete in db
		CertificateTemplate reloadedTemplate = dbInstance.getCurrentEntityManager()
				.getReference(CertificateTemplateImpl.class, template.getKey());
		dbInstance.getCurrentEntityManager().remove(reloadedTemplate);
	}

	@Override
	public CertificateTemplate addTemplate(String name, File file, String format, String orientation, boolean publicTemplate, Identity addedBy) {
		CertificateTemplateImpl template = new CertificateTemplateImpl();

		template.setCreationDate(new Date());
		template.setLastModified(template.getCreationDate());
		template.setPublicTemplate(publicTemplate);
		template.setFormat(format);
		template.setOrientation(orientation);

		String filename = name.toLowerCase();
		if(filename.endsWith(".pdf")) {
			if(addPdfTemplate(name, file, template, addedBy)) {
				dbInstance.getCurrentEntityManager().persist(template);
			} else {
				template = null;
			}
		} else if(filename.endsWith(".zip")) {
			if(addHtmlTemplate(name, file, template)) {
				dbInstance.getCurrentEntityManager().persist(template);
			} else {
				template = null;
			}
		} else {
			template = null;
		}
		return template;
	}
	
	@Override
	public CertificateTemplate updateTemplate(CertificateTemplate template, String name, File file, String format, String orientation, Identity updatedBy) {
		CertificateTemplateImpl templateToUpdate = (CertificateTemplateImpl)template;
		templateToUpdate.setLastModified(new Date());
		templateToUpdate.setFormat(format);
		templateToUpdate.setOrientation(orientation);

		String filename = name.toLowerCase();
		File templateFile = getTemplateFile(templateToUpdate);
		if(filename.endsWith(".pdf")) {
			if(addPdfTemplate(name, file, templateToUpdate, updatedBy)) {
				templateToUpdate = dbInstance.getCurrentEntityManager().merge(templateToUpdate);
			} else {
				templateToUpdate = null;
			}
		} else if(filename.endsWith(".zip")) {
			if(addHtmlTemplate(name, file, templateToUpdate)) {
				templateToUpdate = dbInstance.getCurrentEntityManager().merge(templateToUpdate);
			} else {
				templateToUpdate = null;
			}
		}
		if(templateToUpdate != null && templateFile != null && templateFile.exists()) {
			//if the new template is successfully saved, delete the old one
			FileUtils.deleteDirsAndFiles(templateFile.getParentFile(), true, true);
		}
		return templateToUpdate;
	}
	
	private boolean addHtmlTemplate(String name, File file, CertificateTemplateImpl template) {
		String dir = templatesStorage.generateDir();
		VFSContainer templateDir = templatesStorage.getContainer(dir);
		try {
			ZipUtil.unzipStrict(file, templateDir);
			template.setName(name);
			template.setPath(dir + "index.html");
			return true;
		} catch (Exception e) {
			log.error("", e);
			return false;
		}
	}
	
	private boolean addPdfTemplate(String name, File file, CertificateTemplateImpl template, Identity savedBy) {
		String dir = templatesStorage.generateDir();
		VFSContainer templateDir = templatesStorage.getContainer(dir);
		
		VFSLeaf templateLeaf;
		String renamedName = VFSManager.rename(templateDir, name);
		if(renamedName != null) {
			templateLeaf = templateDir.createChildLeaf(renamedName);
		} else {
			templateLeaf = templateDir.createChildLeaf(name);
		}
		
		try(InputStream inStream = Files.newInputStream(file.toPath())) {
			if(VFSManager.copyContent(inStream, templateLeaf, savedBy)) {
				template.setName(name);
				template.setPath(dir + templateLeaf.getName());
				return true;
			}
		} catch(IOException ex) {
			log.error("", ex);
		}
		return false;
	}
	
	@Override
	public File getTemplateFile(CertificateTemplate template) {
		String templatePath = template.getPath();
		File root = getCertificateTemplatesRoot();
		return new File(root, templatePath);
	}
	
	@Override
	public VFSLeaf getTemplateLeaf(CertificateTemplate template) {
		String templatePath = template.getPath();
		VFSContainer root = this.getCertificateTemplatesRootContainer();
		VFSItem templateItem = root.resolve(templatePath);
		return templateItem instanceof VFSLeaf ? (VFSLeaf)templateItem : null;
	}
	
	@Override
	public InputStream getDefaultTemplate() {
		return CertificatesManager.class.getResourceAsStream("template.pdf");
	}
	
	public File getCertificateTemplatesRoot() {
		Path path = Paths.get(folderModule.getCanonicalRoot(), "certificates", "templates");
		File root = path.toFile();
		if(!root.exists()) {
			root.mkdirs();
		}
		return root;
	}
	
	public VFSContainer getCertificateTemplatesRootContainer() {
		return VFSManager.olatRootContainer(File.separator + "certificates" + File.separator + "templates", null);
	}
	
	public File getCertificateRoot() {
		Path path = Paths.get(folderModule.getCanonicalRoot(), "certificates", "users");
		File root = path.toFile();
		if(!root.exists()) {
			root.mkdirs();
		}
		return root;
	}
	
	public Path getRasterizePath() {
		return Paths.get(folderModule.getCanonicalRoot(), "certificates", "rasterize.js");
	}
	
	public Path getQRCodeLibPath() {
		return Paths.get(folderModule.getCanonicalRoot(), "certificates", "qrcode.min.js");
	}
	
	public VFSContainer getCertificateRootContainer() {
		return VFSManager.olatRootContainer(File.separator + "certificates" + File.separator + "users", null);
	}

	@Override
	public String getExporterID() {
		return "certificates";
	}

	@Override
	public void export(Identity identity, ManifestBuilder manifest, File archiveDirectory, Locale locale) {
		List<Certificate> certificates = getCertificates(identity);
		if(!certificates.isEmpty()) {
			File certificaleArchiveDir = new File(archiveDirectory, "certificates");
			for(Certificate certificate:certificates) {
				File certificateFile = getCertificateFile(certificate);
				if(certificateFile != null && certificateFile.exists()) {
					FileUtils.copyFileToDir(certificateFile, certificaleArchiveDir, true, "Archive certificate");
				}
			}
		}
	}
}
