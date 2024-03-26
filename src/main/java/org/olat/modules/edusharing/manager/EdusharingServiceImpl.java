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
package org.olat.modules.edusharing.manager;

import java.security.KeyPair;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Properties;

import jakarta.annotation.PostConstruct;

import org.apache.logging.log4j.Logger;
import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.core.util.cache.CacheWrapper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.course.style.CourseStyleService;
import org.olat.modules.edusharing.CreateUsageParameter;
import org.olat.modules.edusharing.EdusharingClient;
import org.olat.modules.edusharing.EdusharingConversionService;
import org.olat.modules.edusharing.EdusharingException;
import org.olat.modules.edusharing.EdusharingHtmlElement;
import org.olat.modules.edusharing.EdusharingModule;
import org.olat.modules.edusharing.EdusharingProperties;
import org.olat.modules.edusharing.EdusharingProvider;
import org.olat.modules.edusharing.EdusharingResponse;
import org.olat.modules.edusharing.EdusharingSecurityService;
import org.olat.modules.edusharing.EdusharingService;
import org.olat.modules.edusharing.EdusharingSignature;
import org.olat.modules.edusharing.EdusharingUsage;
import org.olat.modules.edusharing.GetPreviewParameter;
import org.olat.modules.edusharing.GetRenderedParameter;
import org.olat.modules.edusharing.NodeIdentifier;
import org.olat.modules.edusharing.Ticket;
import org.olat.modules.edusharing.model.EdusharingErrorResponse;
import org.olat.modules.edusharing.model.TicketImpl;
import org.olat.modules.edusharing.model.Usage;
import org.olat.modules.edusharing.model.Usages;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryDataDeletable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EdusharingServiceImpl implements EdusharingService, RepositoryEntryDataDeletable {
	
	private static final Logger log = Tracing.createLoggerFor(EdusharingServiceImpl.class);
	
	@Autowired
	private EdusharingModule edusharingModule;
	@Autowired
	private EdusharingUsageDAO usageDao;
	@Autowired
	private EdusharingClient client;
	@Autowired
	private EdusharingConversionService conversionService;
	@Autowired
	private EdusharingSecurityService securityService;
	@Autowired
	private EdusharingUserFactory userFactory;
	@Autowired
	private CoordinatorManager coordinatorManager;
	
	private CacheWrapper<Long, Ticket> ticketCache;
	
	@PostConstruct
	public void initCache() {
		ticketCache = coordinatorManager.getCoordinator().getCacher().getCache(CourseStyleService.class.getSimpleName(), "edusharingTicket");
	}

	@Override
	public Properties getConfigForRegistration() {
		Properties props = new Properties();
		String appId = edusharingModule.getAppId();
		if (StringHelper.containsNonWhitespace(appId)) {
			props.put("appid", appId);
		}
		props.put("trustedclient", "true");
		props.put("type", "LMS");
		props.put("subtype", "OpenOLAT");
		props.put("domain", Settings.getServerDomainName());
		String host = edusharingModule.getHost();
		if (StringHelper.containsNonWhitespace(host)) {
			props.put("host", host);
		}
		KeyPair soapKeys = edusharingModule.getSoapKeys();
		if (soapKeys != null) {
			String publicKey = securityService.getPublicKey(soapKeys);
			props.put("public_key", publicKey);
		}
		// unused
		// props.put("hasTeachingPermission", "");
		// props.put("appcaption", "");
		return props;
	}

	@Override
	public EdusharingProperties getEdusharingRepoConfig() throws EdusharingException {
		return client.getRepoConfig();
	}
	
	@Override
	public Ticket getTicket(Identity identity) {
		Ticket ticket = null;
		try {
			ticket = ticketCache.get(identity.getKey());
			if (ticket != null) {
				ticket = validateTicket(ticket).orElse(createTicket(identity));
			} else {
				ticket = createTicket(identity);
			}
		} catch (Exception e) {
			log.error("Error while getting edu-sharing ticket", e);
		}
		return ticket;
	}

	private Ticket createTicket(Identity identity) throws EdusharingException {
		String tooken = client.createTicket(identity);
		TicketImpl ticket = new TicketImpl(tooken);
		ticket.setLastValidation(LocalDateTime.now());
		ticketCache.put(identity.getKey(), ticket);
		return ticket;
	}

	private Optional<Ticket> validateTicket(Ticket ticket) {
		try {
			// Ticket is younger than the valid time, we must not check.
			if (ticket.getLastValidation().plusSeconds(edusharingModule.getTicketValidSeconds()).isAfter(LocalDateTime.now())) {
				return Optional.of(ticket);
			}
			
			boolean valid = client.validateTicket(ticket);
			if (valid) {
				if (ticket instanceof TicketImpl) {
					TicketImpl ticketImpl = (TicketImpl) ticket;
					ticketImpl.setLastValidation(LocalDateTime.now());
				}
				return Optional.of(ticket);
			}
		} catch (Exception e) {
			log.warn("Validation of edu-sharingg ticket failed.", e);
		}
		
		return Optional.empty();
	}

	@Override
	public EdusharingResponse getPreview(Ticket ticket, String objectUrl) throws EdusharingException {
		NodeIdentifier nodeIdentifier = conversionService.toNodeIdentifier(objectUrl);
		
		GetPreviewParameter parameter = new GetPreviewParameter(
				ticket.getTooken(),
				nodeIdentifier.getRepositoryId(),
				nodeIdentifier.getNodeId());
		return client.getPreview(parameter);
	}
	
	@Override
	public EdusharingResponse getRendered(Identity viewer, String identifier, String version, String width,
			String height, String language) throws EdusharingException {
		EdusharingUsage usage = usageDao.loadByIdentifier(identifier);
		if (usage == null) {
			return new EdusharingErrorResponse(404);
		}

		String widthChecked = width != null? width: usage.getWidth();
		String heightChecked = height != null? height: usage.getHeight();
		NodeIdentifier nodeIdentifier = conversionService.toNodeIdentifier(usage.getObjectUrl());
		String courseId = conversionService.toEdusharingCourseId(usage.getOlatResourceable());
		EdusharingSignature signature = securityService.createSignature(null);
		String userIdentifier = userFactory.getUserIdentifier(viewer);
		String encryptedUserIdentifier = securityService.encrypt(edusharingModule.getRepoPublicKey(), userIdentifier);
		
		GetRenderedParameter parameter = new GetRenderedParameter(
				signature.getAppId(), 
				nodeIdentifier.getRepositoryId(),
				nodeIdentifier.getNodeId(),
				identifier,
				courseId,
				version,
				language,
				language,
				signature.getSigned(),
				signature.getSignature(),
				signature.getTimeStamp(),
				encryptedUserIdentifier,
				"inline");
		parameter.setWidth(widthChecked);
		parameter.setHeight(heightChecked);
		return client.getRendered(parameter);
	}

	@Override
	public String getRenderAsWindowUrl(Ticket ticket, Identity viewer, String identifier, String language) {
		EdusharingUsage usage = usageDao.loadByIdentifier(identifier);
		if (usage == null) {
			return null;
		}

		NodeIdentifier nodeIdentifier = conversionService.toNodeIdentifier(usage.getObjectUrl());
		String courseId = conversionService.toEdusharingCourseId(usage.getOlatResourceable());
		EdusharingSignature signature = securityService.createSignature(null);
		String userIdentifier = userFactory.getUserIdentifier(viewer);
		String encryptedUserIdentifier = securityService.encrypt(edusharingModule.getRepoPublicKey(), userIdentifier);
		String encryptedTicket = securityService.encrypt(edusharingModule.getRepoPublicKey(), ticket.getTooken());
		
		GetRenderedParameter parameter = new GetRenderedParameter(
				signature.getAppId(), 
				nodeIdentifier.getRepositoryId(),
				nodeIdentifier.getNodeId(),
				identifier,
				courseId,
				usage.getVersion(),
				language,
				language,
				signature.getSigned(),
				signature.getSignature(),
				signature.getTimeStamp(),
				encryptedUserIdentifier,
				"window");
		parameter.setEncryptedTicket(encryptedTicket);
		return client.getRenderUrl(parameter);
	}

	@Override
	public EdusharingUsage createUsage(Identity identity, EdusharingHtmlElement element, EdusharingProvider provider)
			throws EdusharingException {
		OLATResourceable ores = provider.getOlatResourceable();
		CreateUsageParameter parameter = new CreateUsageParameter(
				element.getIdentifier(),
				element.getObjectUrl(),
				userFactory.getUserIdentifier(identity),
				conversionService.toEdusharingCourseId(ores),
				conversionService.toNodeIdentifier(element.getObjectUrl())
				);
		client.createUsage(getTicket(identity), parameter);
		EdusharingUsage usage = usageDao.create(identity, element, ores, provider.getSubPath());
		
		log.debug("edu-sharing usage created for identifier: {}, resType={}, resId={}",
				element.getIdentifier(), ores.getResourceableTypeName(), ores.getResourceableId());
		return usage;
	}

	@Override
	public EdusharingUsage loadUsageByIdentifier(String identifier) {
		return usageDao.loadByIdentifier(identifier);
	}

	@Override
	public List<EdusharingUsage> loadUsages(OLATResourceable ores, String subPath) {
		return usageDao.loadByResoureable(ores, subPath);
	}

	@Override
	public void deleteUsage(Identity identity, String identifier) throws EdusharingException {
		EdusharingUsage usage = usageDao.loadByIdentifier(identifier);
		if (usage == null) return;
		
		deleteUsage(usage);
	}
	
	@Override
	public void deleteUsage(EdusharingUsage usage) {
		NodeIdentifier nodeIdentifier = conversionService.toNodeIdentifier(usage.getObjectUrl());
		Usages edusharingUsages = client.getUsages(getTicket(usage.getIdentity()), nodeIdentifier);
		if (edusharingUsages != null && edusharingUsages.getUsages() != null) {
			for (Usage edusharingUsage : edusharingUsages.getUsages()) {
				// usages matches the edusharingUsage
				// https://github.com/edu-sharing/php-auth-plugin/blob/22a0b79578e2f866cbdec30c45a07e017ed463d7/src/EduSharing/EduSharingNodeHelper.php#L98C17-L98C165
				if (Objects.equals(edusharingUsage.getResourceId(), usage.getIdentifier())
						&& Objects.equals(edusharingUsage.getAppId(), edusharingModule.getAppId()) 
						&& Objects.equals(edusharingUsage.getCourseId(), conversionService.toEdusharingCourseId(usage.getOlatResourceable()))) {
					try {
						// The nodeId of the usage is the usageId!
						client.deleteUsage(getTicket(usage.getIdentity()), nodeIdentifier, edusharingUsage.getNodeId());
					} catch (Exception e) {
						log.warn("edu-sharing usage deletion failed for identifier: {}, usageId: {}",
								usage.getIdentifier(), edusharingUsage.getNodeId());
					}
				}
			}
		}
		
		log.debug("edu-sharing usage deleted for identifier: {}", usage.getIdentifier());
		usageDao.delete(usage);
	}
	
	@Override
	public void deleteUsages(EdusharingProvider edusharingProvider) {
		deleteUsages(edusharingProvider.getOlatResourceable(), edusharingProvider.getSubPath());
	}
	
	@Override
	public void deleteUsages(OLATResourceable ores, String subPath) throws EdusharingException {
		List<EdusharingUsage> usages = usageDao.loadByResoureable(ores, subPath);
		for (EdusharingUsage usage : usages) {
			deleteUsage(usage);
		}
	}

	@Override
	public boolean deleteRepositoryEntryData(RepositoryEntry re) {
		deleteUsages(re, null);
		return true;
	}

}
