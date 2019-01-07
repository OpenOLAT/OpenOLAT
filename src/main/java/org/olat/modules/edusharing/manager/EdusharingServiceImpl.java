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
import java.util.Optional;
import java.util.Properties;

import org.olat.core.helpers.Settings;
import org.olat.core.id.Identity;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.modules.edusharing.CreateUsageParameter;
import org.olat.modules.edusharing.DeleteUsageParameter;
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
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * 
 * Initial date: 28 Nov 2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
@Service
public class EdusharingServiceImpl implements EdusharingService {
	
	private static final OLog log = Tracing.createLoggerFor(EdusharingServiceImpl.class);
	
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
	public Ticket createTicket(Identity identity) throws EdusharingException {
		String tooken = client.createTicket(identity);
		TicketImpl ticket = new TicketImpl(tooken);
		ticket.setLastValidation(LocalDateTime.now());
		return ticket;
	}

	@Override
	public Optional<Ticket> validateTicket(Ticket ticket) {
		try {
			// Ticket is younger than the valid time, we must not check.
			if (ticket.getLastValidation().plusSeconds(edusharingModule.getTicketValidSeconds()).isBefore(LocalDateTime.now())) {
				return Optional.of(ticket);
			}
			
			boolean valid = client.validateTicket(ticket.getTooken());
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
	public EdusharingResponse getRendered(Identity viewer, String identifier, String width, String height,
			String language) throws EdusharingException {
		EdusharingUsage usage = usageDao.loadByIdentifier(identifier);
		if (usage == null) {
			return new EdusharingErrorResponse(404);
		}

		String widthChecked = width != null? width: usage.getWidth();
		String heightChecked = height != null? height: usage.getHeight();
		NodeIdentifier nodeIdentifier = conversionService.toNodeIdentifier(usage.getObjectUrl());
		String courseId = conversionService.toEdusharingCourseId(usage.getOlatResourceable());
		EdusharingSignature signature = securityService.createSignature();
		String userIdentifier = userFactory.getUserIdentifier(viewer);
		String encryptedUserIdentifier = securityService.encrypt(edusharingModule.getRepoPublicKey(), userIdentifier);
		
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
		EdusharingSignature signature = securityService.createSignature();
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
				conversionService.toEdusharingCourseId(ores)
				);
		client.createUsage(parameter);
		EdusharingUsage usage = usageDao.create(identity, element, ores);
		
		log.debug("edu-sharing filter usage created for identifier: "+ element.getIdentifier() + ", resType="
				+ ores.getResourceableTypeName() + ", resId=" + ores.getResourceableId());
		return usage;
	}

	@Override
	public EdusharingUsage loadUsageByIdentifier(String identifier) {
		return usageDao.loadByIdentifier(identifier);
	}

	@Override
	public List<EdusharingUsage> loadUsages(OLATResourceable ores) {
		return usageDao.loadByResoureable(ores);
	}

	@Override
	public void deleteUsage(Identity identity, String identifier) throws EdusharingException {
		EdusharingUsage usage = usageDao.loadByIdentifier(identifier);
		DeleteUsageParameter parameter = new DeleteUsageParameter(
				usage.getIdentifier(),
				usage.getObjectUrl(),
				userFactory.getUserIdentifier(identity),
				conversionService.toEdusharingCourseId(usage.getOlatResourceable())
				);
		client.deleteUsage(parameter);
		log.debug("edu-sharing filter usage deleted for identifier: " + identifier);
		
		usageDao.delete(identifier);
	}
}
