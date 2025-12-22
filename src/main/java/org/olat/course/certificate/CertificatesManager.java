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
import java.util.Set;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.ICourse;
import org.olat.course.certificate.model.CertificateConfig;
import org.olat.course.certificate.model.CertificateIdentityConfig;
import org.olat.course.certificate.model.CertificateInfos;
import org.olat.course.certificate.model.CertificateWithInfos;
import org.olat.course.certificate.model.PreviewCertificate;
import org.olat.group.BusinessGroup;
import org.olat.modules.certificationprogram.CertificationProgram;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;
import org.olat.resource.OLATResource;
import org.olat.user.propertyhandlers.UserPropertyHandler;

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
	
	public RepositoryEntryCertificateConfiguration createConfiguration(RepositoryEntry entry);
	
	public RepositoryEntryCertificateConfiguration getConfiguration(RepositoryEntry entry);
	
	public RepositoryEntryCertificateConfiguration updateConfiguration(RepositoryEntryCertificateConfiguration configuration);
	
	
	/**
	 * Clone the configuration of the source if it's available.
	 * 
	 * @param sourceEntry The source of the clonage
	 * @param targetEntry The target of the clonage
	 * @return A cloned configuration if the source has one, or null
	 */
	public RepositoryEntryCertificateConfiguration copyRepositoryEntryCertificateConfiguration(RepositoryEntry sourceEntry, RepositoryEntry targetEntry);
	
	public boolean isCertificateEnabled(RepositoryEntryRef entry);
	
	public boolean isAutomaticCertificationEnabled(RepositoryEntryRef entry);
	
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
	
	public boolean isTemplateInUse(CertificateTemplate template);

	public File getTemplateFile(CertificateTemplate template);
	
	public VFSLeaf getTemplateLeaf(CertificateTemplate template);
	
	public InputStream getDefaultTemplate();
	
	//certificate
	public Certificate getCertificateById(Long key);
	
	public Certificate getCertificateByUuid(String uuid);
	
	public CertificateLight getCertificateLightById(Long key);
	
	public VFSLeaf getCertificateLeaf(Certificate certificate);
	
	public VFSLeaf getCertificateLeaf(CertificateLight certificate);
	
	/**
	 * Return the last certificates of the user.
	 * @param identity
	 * @return A list of certificates
	 */
	public List<CertificateLight> getLastCertificates(IdentityRef identity);
	
	public List<CertificateWithInfos> getCertificatesWithInfos(IdentityRef identity);
	
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
	
	public List<Certificate> getCertificatesForNotifications(Identity identity, RepositoryEntry entry, Date creationDateAfter);

	public List<Certificate> getCertificates(OLATResource resource);
	
	public boolean hasCertificate(IdentityRef identity, Long resourceKey);
	
	public Certificate getLastCertificate(IdentityRef identity, Long resourceKey);
	
	public List<Certificate> getCertificates(IdentityRef identity, OLATResource resource);

	/**
	 * Returns certificates of courses that 'identity' is a coach for.
	 * 
	 * @param identity The logged-in identity.
	 * @param userPropertyHandlers User properties that should be set for each result entry.
	 * @param from The start date of the date range to consider for the returned result.
	 * @param to The end date of the date range to consider for the returned result.
	 * @return A list of certificates enhanced with data about the certificate recipient and the related course.
	 */
	public List<CertificateIdentityConfig> getCertificatesForGroups(Identity identity, 
																	List<UserPropertyHandler> userPropertyHandlers, 
																	Date from, Date to);

	/**
	 * Returns certificates of users that 'identity' somehow is a manager for through organizational mapping.
	 * For instance, it includes certificates of users for whom 'identity' acts as an education manager.
	 * 
	 * @param identity The logged-in identity.
	 * @param userPropertyHandlers User properties that should be set for each result entry.
	 * @param from The start date of the date range to consider for the returned result.
	 * @param to The end date of the date range to consider for the returned result.
	 * @return A list of certificates enhanced with data about the certificate recipient and the related course.
	 */
	public List<CertificateIdentityConfig> getCertificatesForOrganizations(Identity identity, 
																		   List<UserPropertyHandler> userPropertyHandlers, 
																		   Date from, Date to);

	/**
	 * Enhances the provided set of certificates with additional information 
	 * about whether the associated participant has passed the associated course.
	 *
	 * @param certificates the set of certificates to be enhanced with 'passed' information.
	 */
	public void enhanceCertificatesWithPassedInformation(Set<CertificateIdentityConfig> certificates);

	/**
	 * Check if certification is allowed and check if a certificate was
	 * already emitted and a new one can be generated.
	 * 
	 * @param identity The identity
	 * @param entry The repository entry / course
	 * @return
	 */
	public boolean isCertificationAllowed(Identity identity, RepositoryEntry entry);

	/**
	 * Check if the user can starts the certification process again.
	 * 
	 * @param identity The identity
	 * @param entry The repository entry / course
	 * @return
	 */
	public boolean isRecertificationAllowed(Identity identity, RepositoryEntry entry);
	
	/**
	 * Set the status to archive.
	 * 
	 * @param certificate The certificate to archive
	 * @return A merged certificate
	 */
	public Certificate archiveCertificate(Certificate certificate);
	

	public Date nextRecertificationWindow(Certificate certificate, RepositoryEntryCertificateConfiguration certificateConfig);
	
	public Date nextRecertificationWindow(Date nextCertificationDate, RepositoryEntryCertificateConfiguration certificateConfig);
	
	public Date getDateWindowRecertification(Date nextCertificationDate, CertificationProgram certificateProgram);
	
	public PreviewCertificate previewCertificate(CertificateTemplate template, RepositoryEntry entry, Locale locale, String custom1, String custom2, String custom3);

	public Certificate uploadCertificate(Identity identity, Date creationDate,
			String externalId, CertificateManagedFlag[] managedFlags, OLATResource resource,
			Date nextRecertificationDate, File certificateFile);
	
	public Certificate uploadStandaloneCertificate(Identity identity, Date creationDate,
			String externalId, CertificateManagedFlag[] managedFlags, String courseTitle, Long resourceKey,
			Date nextRecertificationDate, File certificateFile, Identity doer);
	
	public void generateCertificates(List<CertificateInfos> infos, RepositoryEntry entry, CertificateTemplate template, CertificateConfig config);

	public Certificate generateCertificate(CertificateInfos infos, RepositoryEntry entry, CertificateTemplate template, CertificateConfig config);
	
	public Certificate generateCertificate(CertificateInfos infos, CertificationProgram certificationProgram, RepositoryEntry entry, CertificateConfig config);
	
	public void revokeCertificate(Certificate certificate);
	
	public void deleteCertificate(Certificate certificate);
	
	public void deleteStandalonCertificate(Certificate certificate);

}
