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
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

import javax.persistence.TypedQuery;

import org.apache.commons.io.IOUtils;
import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.commons.modules.bc.vfs.OlatRootFolderImpl;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.WebappHelper;
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
import org.olat.course.certificate.CertificateLight;
import org.olat.course.certificate.CertificateTemplate;
import org.olat.course.certificate.CertificatesManager;
import org.olat.course.certificate.RecertificationTimeUnit;
import org.olat.course.certificate.model.CertificateImpl;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.certificate.model.CertificateTemplateImpl;
import org.olat.course.certificate.ui.CertificateController;
import org.olat.course.config.CourseConfig;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.environment.CourseEnvironment;
import org.olat.group.BusinessGroup;
import org.olat.group.BusinessGroupService;
import org.olat.group.model.SearchBusinessGroupParams;
import org.olat.modules.vitero.model.GroupRole;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.model.RepositoryEntrySecurity;
import org.olat.resource.OLATResource;
import org.olat.user.UserManager;
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
public class CertificatesManagerImpl implements CertificatesManager, InitializingBean {
	
	private static final OLog log = Tracing.createLoggerFor(CertificatesManagerImpl.class);
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private I18nManager i18nManager;
	@Autowired
	private MailManager mailManager;
	@Autowired
	private UserManager userManager;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private RepositoryManager repositoryManager;
	@Autowired
	private NotificationsManager notificationsManager;
	@Autowired
	private BusinessGroupService businessGroupService;
	

	private FileStorage usersStorage;
	private FileStorage templatesStorage;
	
	@Override
	public void afterPropertiesSet() {
		//create the folders
		getCertificateTemplatesRoot();
		templatesStorage = new FileStorage(getCertificateTemplatesRootContainer());
		getCertificateRoot();
		usersStorage = new FileStorage(getCertificateRootContainer());
	}

	@Override
	public SubscriptionContext getSubscriptionContext(ICourse course) {
		CourseNode cn = course.getRunStructure().getRootNode();
		CourseEnvironment ce = course.getCourseEnvironment();
		SubscriptionContext ctxt = new SubscriptionContext(ORES_CERTIFICATE, ce.getCourseResourceableId(), cn.getIdent());
		return ctxt;
	}

	@Override
	public PublisherData getPublisherData(ICourse course, String businessPath) {
		String data = String.valueOf(course.getCourseEnvironment().getCourseResourceableId());
		PublisherData pData = new PublisherData(ORES_CERTIFICATE, data, businessPath);
		return pData;
	}

	@Override
	public void markPublisherNews(Identity ident, ICourse course) {
		SubscriptionContext subsContext = getSubscriptionContext(course);
		if (subsContext != null) {
			NotificationsManager.getInstance().markPublisherNews(subsContext, ident, true);
		}
	}
	
	public void markPublisherNews(Identity ident, OLATResource courseResource) {
		ICourse course = CourseFactory.loadCourse(courseResource);
		SubscriptionContext subsContext = getSubscriptionContext(course);
		if (subsContext != null) {
			NotificationsManager.getInstance().markPublisherNews(subsContext, ident, true);
		}
	}
	
	@Override
	public VFSLeaf getCertificateLeaf(Certificate certificate) {
		VFSContainer cerContainer = this.getCertificateRootContainer();
		VFSItem cerItem = cerContainer.resolve(certificate.getPath());
		return cerItem instanceof VFSLeaf ? (VFSLeaf)cerItem : null;
	}
	
	@Override
	public Certificate getCertificateById(Long key) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificate cer")
		  .append(" where cer.key=:certificateKey");
		List<Certificate> certificates = dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Certificate.class)
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
		  .append(" where cer.olatResource.key=:resourceKey and cer.identity.key=:identityKey and cer.last=true order by cer.creationDate");
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
		if(!security.isEntryAdmin() && !security.isCourseCoach() && !security.isGroupCoach() && !security.isCourseParticipant() && !security.isGroupParticipant()) {
			return Collections.emptyList();
		}

		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificate cer")
		  .append(" inner join fetch cer.identity ident")
		  .append(" where cer.olatResource.key=:resourceKey and cer.last=true ");
		//must be some kind of restrictions
		boolean securityCheck = false;
		List<Long> baseGroupKeys = null;
		if(!security.isEntryAdmin()) {
			sb.append(" and (");
			boolean or = false;
			if(security.isCourseCoach()) {
				or = or(sb, or);
				sb.append(" exists (select membership.identity.key from repoentrytogroup as rel, bgroup as reBaseGroup, bgroupmember membership ")
				  .append("   where ident.key=membership.identity.key and rel.entry.key=:repoKey and rel.group=reBaseGroup and membership.group=reBaseGroup and membership.role='").append(GroupRole.participant).append("'")
				  .append(" )");
				securityCheck = true;
			}
			
			if(security.isGroupCoach()) {
				SearchBusinessGroupParams params = new SearchBusinessGroupParams(identity, true, false);
				List<BusinessGroup> groups = businessGroupService.findBusinessGroups(params, entry, 0, -1);
				if(groups.size() > 0) {
					or = or(sb, or);
					sb.append(" exists (select membership.identity.key from bgroupmember membership ")
					  .append("   where ident.key=membership.identity.key and membership.group.key in (:groups) and membership.role='").append(GroupRole.participant).append("'")
					  .append(" )");
					
					baseGroupKeys = new ArrayList<>(groups.size());
					for(BusinessGroup group:groups) {
						baseGroupKeys.add(group.getBaseGroup().getKey());
					}
					securityCheck = true;
				}
			}
			
			if(security.isCourseParticipant() || security.isGroupParticipant()) {
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
			if(security.isCourseCoach()) {
				certificates.setParameter("repoKey", entry.getKey());
			}
			
			if(security.isCourseParticipant() || security.isGroupParticipant()) {
				certificates.setParameter("identityKey", identity.getKey());
			}
		}
		
		if(baseGroupKeys != null && !baseGroupKeys.isEmpty()) {
			certificates.setParameter("groups", baseGroupKeys);
		}
		return certificates.getResultList();
	}
	
	private final boolean or(StringBuilder sb, boolean or) {
		if(or) sb.append(" or ");
		else sb.append(" ");
		return true;
	}

	@Override
	public List<Certificate> getCertificates(IdentityRef identity, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificate cer")
		  .append(" where cer.olatResource.key=:resourceKey and cer.identity.key=:identityKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), Certificate.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("identityKey", identity.getKey())
				.getResultList();
	}

	@Override
	public List<CertificateLight> getCertificates(OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer from certificatelight cer")
		  .append(" where cer.olatResourceKey=:resourceKey");
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb.toString(), CertificateLight.class)
				.setParameter("resourceKey", resource.getKey())
				.getResultList();
	}

	@Override
	public boolean isRecertificationAllowed(Identity identity, RepositoryEntry entry) {
		boolean allowed = false;
		try {
			ICourse course = CourseFactory.loadCourse(entry.getOlatResource());
			CourseConfig config = course.getCourseEnvironment().getCourseConfig();
			if(config.isRecertificationEnabled()) {
				int time = config.getRecertificationTimelapse();
				RecertificationTimeUnit timeUnit = config.getRecertificationTimelapseUnit();
				Certificate certificate =  getLastCertificate(identity, entry.getOlatResource().getKey());
				if(certificate == null) {
					allowed = true;
				} else {
					Date date = certificate.getCreationDate();
					Calendar cal = Calendar.getInstance();
					Date now = cal.getTime();
					cal.setTime(date);
					switch(timeUnit) {
						case day: cal.add(Calendar.DATE, time); break;
						case week: cal.add(Calendar.DATE, time * 7); break;
						case month: cal.add(Calendar.MONTH, time); break;
						case year: cal.add(Calendar.YEAR, time); break;
					}
					Date nextCertification = cal.getTime();
					allowed = nextCertification.before(now);
				}
			} else {
				allowed = true;
			}
		} catch (CorruptedCourseException e) {
			log.error("", e);
		}
		return allowed;
	}

	@Override
	public void generateCertificates(List<CertificateInfos> certificateInfos, RepositoryEntry entry,
			CertificateTemplate template, MailerResult result) {
		int count = 0;
		for(CertificateInfos certificateInfo:certificateInfos) {
			generateCertificate(certificateInfo, entry, template, result);
			if(++count % 10 == 0) {
				dbInstance.commitAndCloseSession();
			}
		}
		markPublisherNews(null, entry.getOlatResource());
	}

	@Override
	public Certificate generateCertificate(CertificateInfos certificateInfos, RepositoryEntry entry,
			CertificateTemplate template, MailerResult result) {
		Certificate certificate = peristCertificate(certificateInfos, entry, template, result);
		markPublisherNews(null, entry.getOlatResource());
		return certificate;
	}

	private Certificate peristCertificate(CertificateInfos certificateInfos, RepositoryEntry entry,
			CertificateTemplate template, MailerResult result) {
		OLATResource resource = entry.getOlatResource();
		Identity identity = certificateInfos.getAssessedIdentity();
		
		CertificateImpl certificate = new CertificateImpl();
		certificate.setOlatResource(resource);
		certificate.setArchivedResourceKey(resource.getKey());
		certificate.setCreationDate(new Date());
		certificate.setLastModified(certificate.getCreationDate());
		certificate.setIdentity(identity);
		certificate.setUuid(UUID.randomUUID().toString());
		certificate.setLast(true);
		
		String templateName;
		InputStream templateStream = null;
		String dir = usersStorage.generateDir();
		try {
			if(template == null) {
				//default
				templateName = "Certificate.pdf";
				templateStream = CertificatesManager.class.getResourceAsStream("template.pdf");
			} else {
				templateName = template.getName();
				File templateFile = getTemplateFile(template);
				if(templateFile != null && templateFile.exists()) {
					templateStream = new FileInputStream(templateFile);
				} else {
					templateStream = CertificatesManager.class.getResourceAsStream("template.pdf");
				}
			}
			
			File dirFile = new File(getCertificateRoot(), dir);
			dirFile.mkdirs();
			File certificateFile = new File(dirFile, templateName);
			
			Float score = certificateInfos.getScore();
			Locale locale = I18nManager.getInstance().getLocaleOrDefault(null);
			Boolean passed = certificateInfos.getPassed();
			Date dateCertification = certificate.getCreationDate();
			Date dateFirstCertification = getDateFirstCertification(identity, resource);

			CertificateTemplateWorker worker = new CertificateTemplateWorker(identity, entry, score, passed, dateCertification, dateFirstCertification, locale, userManager);
			worker.fill(templateStream, certificateFile);
			certificate.setName(templateName);
			certificate.setPath(dir + templateName);
			
			if(dateFirstCertification != null) {
				//not the first certification, reset the last of the others certificates
				removeLastFlag(identity, resource);
			}
			
			dbInstance.getCurrentEntityManager().persist(certificate);
			
			
			MailerResult newResult = sendCertificate(identity, entry, certificateFile);
			if(result != null) {
				result.append(newResult);
			}
			
		} catch (Exception e) {
			log.error("", e);
		} finally {
			IOUtils.closeQuietly(templateStream);
		}
		return certificate;
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
	
	private Date getDateFirstCertification(Identity identity, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("select cer.creationDate from certificate cer")
		  .append(" where cer.olatResource.key=:resourceKey and cer.identity.key=:identityKey")
		  .append(" order by cer.creationDate asc");
		
		List<Date> dates = dbInstance.getCurrentEntityManager().createQuery(sb.toString(), Date.class)
				.setParameter("resourceKey", resource.getKey())
				.setParameter("identityKey", identity.getKey())
				.setMaxResults(1)
				.getResultList();
		return dates.isEmpty() ? null : dates.get(0);
	}
	
	private void removeLastFlag(Identity identity, OLATResource resource) {
		StringBuilder sb = new StringBuilder();
		sb.append("update certificate  cer set cer.last=false")
		  .append(" where cer.olatResource.key=:resourceKey and cer.identity.key=:identityKey");
		
		dbInstance.getCurrentEntityManager().createQuery(sb.toString())
				.setParameter("resourceKey", resource.getKey())
				.setParameter("identityKey", identity.getKey())
				.executeUpdate();
	}

	@Override
	public List<CertificateTemplate> getTemplates() {
		String sb = "select template from certificatetemplate template where template.publicTemplate=true";
		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, CertificateTemplate.class)
				.getResultList();
	}
	
	public CertificateTemplate getTemplateById(Long key) {
		String sb = "select template from certificatetemplate template where template.key=:templateKey";
		List<CertificateTemplate> templates = dbInstance.getCurrentEntityManager()
				.createQuery(sb, CertificateTemplate.class)
				.setParameter("templateKey", key)
				.getResultList();
		return templates.isEmpty() ? null : templates.get(0);
	}

	@Override
	public CertificateTemplate addTemplate(String name, File file, boolean publicTemplate) {
		CertificateTemplateImpl template = new CertificateTemplateImpl();

		template.setCreationDate(new Date());
		template.setLastModified(template.getCreationDate());
		template.setPublicTemplate(publicTemplate);

		VFSLeaf templateLeaf;

		String dir = templatesStorage.generateDir();
		VFSContainer templateDir = templatesStorage.getContainer(dir);
		
		String renamedName = VFSManager.rename(templateDir, name);
		if(renamedName != null) {
			templateLeaf = templateDir.createChildLeaf(renamedName);
		} else {
			templateLeaf = templateDir.createChildLeaf(name);
		}
		
		
		try(InputStream inStream = Files.newInputStream(file.toPath())) {
			if(VFSManager.copyContent(inStream, templateLeaf)) {
				template.setName(name);
				template.setPath(dir + "/" + templateLeaf.getName());
				dbInstance.getCurrentEntityManager().persist(template);
				return template;
			}
		} catch(IOException ex) {
			log.error("", ex);
		}
		return null;
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
	
	public File getCertificateTemplatesRoot() {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), "certificates", "templates");
		File root = path.toFile();
		if(!root.exists()) {
			root.mkdirs();
		}
		return root;
	}
	
	public VFSContainer getCertificateTemplatesRootContainer() {
		return new OlatRootFolderImpl(File.separator + "certificates" + File.separator + "templates", null);
	}
	
	public File getCertificateRoot() {
		Path path = Paths.get(FolderConfig.getCanonicalRoot(), "certificates", "users");
		File root = path.toFile();
		if(!root.exists()) {
			root.mkdirs();
		}
		return root;
	}
	
	public VFSContainer getCertificateRootContainer() {
		return new OlatRootFolderImpl(File.separator + "certificates" + File.separator + "users", null);
	}
}
