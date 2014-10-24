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
import java.util.Date;
import java.util.List;

import org.olat.basesecurity.IdentityRef;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.id.Identity;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.course.ICourse;
import org.olat.course.certificate.model.CertificateInfos;
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
	
	//notifications
	public SubscriptionContext getSubscriptionContext(ICourse course);
	
	public PublisherData getPublisherData(ICourse course, String businessPath);
	
	public void markPublisherNews(Identity ident, ICourse course);
	
	//templates management
	public List<CertificateTemplate> getTemplates();
	
	public CertificateTemplate addTemplate(String name, File file, boolean publicTemplate);
	
	public CertificateTemplate getTemplateById(Long key);

	public File getTemplateFile(CertificateTemplate template);
	
	public VFSLeaf getTemplateLeaf(CertificateTemplate template);
	
	//certificate
	public Certificate getCertificateById(Long key);
	
	public Certificate getCertificateByUuid(String uuid);
	
	public VFSLeaf getCertificateLeaf(Certificate certificate);
	

	public List<CertificateLight> getLastCertificates(IdentityRef identity);
	
	public List<Certificate> getCertificatesForNotifications(Identity identity, RepositoryEntry entry, Date lastNews);

	public List<CertificateLight> getCertificates(OLATResource resourceKey);
	
	public Certificate getLastCertificate(IdentityRef identity, Long resourceKey);
	
	public List<Certificate> getCertificates(IdentityRef identity, OLATResource resource);
	
	public void generateCertificates(List<CertificateInfos> identities, RepositoryEntry entry, CertificateTemplate template);

	public Certificate generateCertificate(CertificateInfos identity, RepositoryEntry entry, CertificateTemplate template);

}
