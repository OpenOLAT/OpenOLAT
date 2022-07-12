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
package org.olat.course.certificate;

import java.io.File;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.ICourse;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.certificate.model.PreviewCertificate;
import org.olat.group.BusinessGroup;
import org.olat.repository.RepositoryEntry;
import org.olat.resource.OLATResource;

/**
 * 
 * Initial date: 20.10.2014<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface CertificatesManager {

	public static final String ORES_CERTIFICATE =  OresHelper.calculateTypeName(CertificatesManager.class);
	public static final OLATResourceable ORES_CERTIFICATE_EVENT =  OresHelper.createOLATResourceableInstance("Certificate", 0l);
	
	public boolean isHTMLTemplateAllowed();
	
	//notifications
	public SubscriptionContext getSubscriptionContext(ICourse course);
	
	public PublisherData getPublisherData(ICourse course, String businessPath);
	
	public void markPublisherNews(Identity ident, ICourse course);
	
	//repository maintenance
	public int deleteRepositoryEntry(RepositoryEntry re);
	
	public List<OLATResource> getResourceWithCertificates();
	
	//templates management
	public List<CertificateTemplate> getTemplates();
	
	/**
	 * Add a new template
	 * @param name The filename of the template
	 * @param file The file which is / or contains the template
	 * @param publicTemplate True if the tempalte is accessible system-wide
	 * @param addedBy the file uploader
	 * @return
	 */
	public CertificateTemplate addTemplate(String name, File file, String format, String orientation, boolean publicTemplate, Identity addedBy);
	
	/**
	 * Update the template files
	 * @param template
	 * @param name
	 * @param file
	 * @param identity 
	 * @return
	 */
	public CertificateTemplate updateTemplate(CertificateTemplate template, String name, File file, String format, String orientation, Identity updatedBy);
	
	/**
	 * Delete the template in the file system and in the database
	 * @param template
	 */
	public void deleteTemplate(CertificateTemplate template);
	
	public CertificateTemplate getTemplateById(Long key);

	public File getTemplateFile(CertificateTemplate template);
	
	public VFSLeaf getTemplateLeaf(CertificateTemplate template);
	
	public InputStream getDefaultTemplate();
	
	//certificate
	public Certificate getCertificateById(Long key);
	
	public Certificate getCertificateByUuid(String uuid);
	
	public CertificateLight getCertificateLightById(Long key);
	
	public VFSLeaf getCertificateLeaf(Certificate certificate);
	
	/**
	 * Return the last certificates of the user.
	 * @param identity
	 * @return A list of certificates
	 */
	public List<CertificateLight> getLastCertificates(IdentityRef identity);
	
	/**
	 * List the certificates of a user or a learn resource.
	 * 
	 * @param identity The certificates owner
	 * @param resource The learn resource
	 * @param externalId The external identifier
	 * @param managedOnly if true returns only the managed ones, if false all
	 * @param lastOnly if true returns only the last one, if false all
	 * @return A list of light certificates
	 */
	public List<CertificateLight> getCertificates(IdentityRef identity, OLATResource resource,
			String externalId, Boolean managedOnly, Boolean lastOnly);
	
	/**
	 * Return the last certificates of all users f the specified course.
	 * @param resourceKey The resource primary key of the course.
	 * @return A list of certificates
	 */
	public List<CertificateLight> getLastCertificates(OLATResource resourceKey);
	
	/**
	 * Return the last certificates of all users and all courses linked
	 * to this group.
	 * @param businessGroup
	 * @return A list of certificates
	 */
	public List<CertificateLight> getLastCertificates(BusinessGroup businessGroup);
	
	public List<Certificate> getCertificatesForNotifications(Identity identity, RepositoryEntry entry, Date lastNews);

	public List<Certificate> getCertificates(OLATResource resource);
	
	public boolean hasCertificate(IdentityRef identity, Long resourceKey);
	
	public Certificate getLastCertificate(IdentityRef identity, Long resourceKey);
	
	public List<Certificate> getCertificates(IdentityRef identity, OLATResource resource);
	
	
	
	/**
	 * Check if recertification is allowed and if it is the case, check the
	 * recertification period. If not allowed, check if a certificate was
	 * already emitted.
	 * 
	 * @param identity
	 * @param entry
	 * @return
	 */
	
	public boolean isCertificationAllowed(Identity identity, RepositoryEntry entry);
	
	/**
	 * Get the next re-certification date or NULL if no recertification possible
	 * @param certificate An exiting certificate
	 * @param entry The repository entry of the course
	 * @return Date representing the next possible recertification date or NULL if no recertification possible at this time
	 */
	public Date getDateNextRecertification(Certificate certificate, RepositoryEntry entry);
	
	public PreviewCertificate previewCertificate(CertificateTemplate template, RepositoryEntry entry, Locale locale, String custom1, String custom2, String custom3);

	public Certificate uploadCertificate(Identity identity, Date creationDate,
			String externalId, CertificateManagedFlag[] managedFlags, OLATResource resource, File certificateFile);
	
	public Certificate uploadStandaloneCertificate(Identity identity, Date creationDate,
			String externalId, CertificateManagedFlag[] managedFlags, String courseTitle, Long resourceKey, File certificateFile);
	
	public void generateCertificates(List<CertificateInfos> infos, RepositoryEntry entry, CertificateTemplate template, CertificateConfig config);

	public Certificate generateCertificate(CertificateInfos infos, RepositoryEntry entry, CertificateTemplate template, CertificateConfig config);
	
	public void deleteCertificate(Certificate certificate);
	
	public void deleteStandalonCertificate(Certificate certificate);

}
