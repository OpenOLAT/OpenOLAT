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

package org.olat.core.commons.services.notifications.restapi;

import static org.olat.restapi.security.RestSecurityHelper.parseDate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.commons.services.notifications.NotificationHelper;
import org.olat.core.commons.services.notifications.NotificationsManager;
import org.olat.core.commons.services.notifications.Publisher;
import org.olat.core.commons.services.notifications.PublisherData;
import org.olat.core.commons.services.notifications.Subscriber;
import org.olat.core.commons.services.notifications.SubscriptionContext;
import org.olat.core.commons.services.notifications.SubscriptionInfo;
import org.olat.core.commons.services.notifications.model.SubscriptionListItem;
import org.olat.core.commons.services.notifications.restapi.vo.PublisherVO;
import org.olat.core.commons.services.notifications.restapi.vo.SubscriberVO;
import org.olat.core.commons.services.notifications.restapi.vo.SubscriptionInfoVO;
import org.olat.core.commons.services.notifications.restapi.vo.SubscriptionListItemVO;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
import org.olat.core.id.context.BusinessControlFactory;
import org.olat.core.id.context.ContextEntry;
import org.olat.core.util.StringHelper;
import org.olat.modules.fo.restapi.MessageVO;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.user.restapi.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;

/**
 * 
 * <h3>Description:</h3>
 * REST API for notifications
 * <p>
 * Initial Date:  25 aug 2010 <br>
 * @author srosse, srosse@frentix.com, http://www.frentix.com
 */
@Component
@Path("notifications")
public class NotificationsWebService {
	
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private NotificationsManager notificationsMgr;

	
	/**
	 * Get the publisher by resource name and id + sub identifier.
	 * 
	 * @return It returns the <code>CourseVO</code> object representing the course.
	 */
	@GET
	@Path("publisher/{ressourceName}/{ressourceId}/{subIdentifier}")
	@Operation(summary = "Get publisher",
		description = "Get the publisher by resource name and id + sub identifier")
	@ApiResponse(responseCode = "200", description = "The publisher",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = PublisherVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = PublisherVO.class))
				})
	@ApiResponse(responseCode = "204", description = "The publisher doesn't exist")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getPublisher(@PathParam("ressourceName") String ressourceName, @PathParam("ressourceId") Long ressourceId,
			@PathParam("subIdentifier") String subIdentifier, @Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		SubscriptionContext subsContext
			= new SubscriptionContext(ressourceName, ressourceId, subIdentifier);

		Publisher publisher = notificationsMgr.getPublisher(subsContext);
		if(publisher == null) {
			return Response.ok().status(Status.NO_CONTENT).build();
		}
		PublisherVO publisherVo = new PublisherVO(publisher);
		return Response.ok(publisherVo).build();
	}
	
	@GET
	@Path("subscribers/{ressourceName}/{ressourceId}/{subIdentifier}")
	@Operation(summary = "Get subscribers",
		description = "Get the subscribers by resource name and id + sub identifier")
	@ApiResponse(responseCode = "200", description = "The subscribers",
			content = {
					@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = SubscriberVO.class))),
					@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = SubscriberVO.class)))
				})
	@ApiResponse(responseCode = "204", description = "The subscribers don't exist")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getSubscriber(@PathParam("ressourceName") String ressourceName, @PathParam("ressourceId") Long ressourceId,
			@PathParam("subIdentifier") String subIdentifier, @Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}
		
		SubscriptionContext subsContext
			= new SubscriptionContext(ressourceName, ressourceId, subIdentifier);

		Publisher publisher = notificationsMgr.getPublisher(subsContext);
		if(publisher == null) {
			return Response.ok().status(Status.NO_CONTENT).build();
		}
		
		List<Subscriber> subscribers = notificationsMgr.getSubscribers(publisher, false);
		SubscriberVO[] subscriberVoes = new SubscriberVO[subscribers.size()];
		int count = 0;
		for(Subscriber subscriber:subscribers) {
			SubscriberVO subscriberVO = new SubscriberVO();
			subscriberVO.setPublisherKey(publisher.getKey());
			subscriberVO.setSubscriberKey(subscriber.getKey());
			subscriberVO.setIdentityKey(subscriber.getIdentity().getKey());
			subscriberVoes[count++] = subscriberVO;
		}
		return Response.ok(subscriberVoes).build();
	}

	@PUT
	@Path("subscribers")
	@Operation(summary = "Put subscribers", description = "Put the subscribers")
	@ApiResponse(responseCode = "200", description = "Ok")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The author or message not found")
	@Consumes({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response subscribe(PublisherVO publisherVO, @Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.FORBIDDEN).build();
		}

		SubscriptionContext subscriptionContext
			= new SubscriptionContext(publisherVO.getResName(), publisherVO.getResId(), publisherVO.getSubidentifier());
		PublisherData publisherData
			= new PublisherData(publisherVO.getType(), publisherVO.getData(), publisherVO.getBusinessPath());
		
		List<UserVO> userVoes = publisherVO.getUsers();
		List<Long> identityKeys = new ArrayList<>();
		for(UserVO userVo:userVoes) {
			identityKeys.add(userVo.getKey());
		}
		List<Identity> identities = securityManager.loadIdentityByKeys(identityKeys);
		notificationsMgr.subscribe(identities, subscriptionContext, publisherData);
		return Response.ok().build();
	}
	
	@DELETE
	@Path("subscribers/{subscriberKey}")
	@Operation(summary = "Delete subscribers", description = "Delete the subscribers by id")
	@ApiResponse(responseCode = "200", description = "Ok")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The author or message not found")
	@Consumes({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response unsubscribe(@PathParam("subscriberKey") Long subscriberKey, @Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(notificationsMgr.deleteSubscriber(subscriberKey)) {
			return Response.ok().build();
		}
		return Response.ok().status(Status.NOT_MODIFIED).build();
	}
	
	/**
	 * Retrieves the notification of the logged in user.
	 * 
	 * @param date The date (optional)
	 * @param type The type of notifications (User, Forum...) (optional)
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the user being search. The xml
	 *         correspond to a <code>SubscriptionInfoVO</code>. <code>SubscriptionInfoVO</code>
	 */
	@GET
	@Operation(summary = "Retrieves the notification of the logged in user", description = "Retrieves the notification of the logged in user")
	@ApiResponse(responseCode = "200", description = "Ok.",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = MessageVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = MessageVO.class))
				})
	@ApiResponse(responseCode = "404", description = "The identity not found")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getNotifications(@QueryParam("date") @Parameter(description = "The date (optional)") String date,
			@QueryParam("type") @Parameter(description = "The type of notifications (User, Forum...) (optional)") String type, @Context HttpServletRequest httpRequest) {
		Identity identity = RestSecurityHelper.getIdentity(httpRequest);
		Locale locale = RestSecurityHelper.getLocale(httpRequest);
		
		Date compareDate;
		if(StringHelper.containsNonWhitespace(date)) {
			compareDate = parseDate(date, locale);
		} else {
			compareDate = notificationsMgr.getCompareDateFromInterval(notificationsMgr.getUserIntervalOrDefault(identity));
		}
		
		List<String> types = new ArrayList<>(1);
		if(StringHelper.containsNonWhitespace(type)) {
			types.add(type);
		}
		
		Map<Subscriber,SubscriptionInfo> subsInfoMap = NotificationHelper.getSubscriptionMap(identity, locale, true, compareDate, types);
		List<SubscriptionInfoVO> voes = new ArrayList<>();
		for(Map.Entry<Subscriber, SubscriptionInfo> entry: subsInfoMap.entrySet()) {
			SubscriptionInfo info = entry.getValue();
			if(info.hasNews()) {
				Subscriber subscriber = entry.getKey();
				voes.add(createSubscriptionInfoVO(subscriber.getPublisher(), info));
			}
		}
		SubscriptionInfoVO[] voesArr = new SubscriptionInfoVO[voes.size()];
		voes.toArray(voesArr);
		return Response.ok(voesArr).build();
	}
	
	private SubscriptionInfoVO createSubscriptionInfoVO(Publisher publisher, SubscriptionInfo info) {
		SubscriptionInfoVO infoVO  = new SubscriptionInfoVO(info);
		if(info.getSubscriptionListItems() != null && !info.getSubscriptionListItems().isEmpty()) {
			List<SubscriptionListItemVO> itemVOes = new ArrayList<>(info.getSubscriptionListItems().size());
			
			String publisherType = publisher.getType();
			String resourceType = publisher.getResName();
			for(SubscriptionListItem item:info.getSubscriptionListItems()) {
				SubscriptionListItemVO itemVO = new SubscriptionListItemVO(item); 
				//resource specific
				if("BusinessGroup".equals(resourceType)) {
					itemVO.setGroupKey(publisher.getResId());
				} else if("CourseModule".equals(resourceType)) {
					itemVO.setCourseKey(publisher.getResId());
					itemVO.setCourseNodeId(publisher.getSubidentifier());
				}
				
				//publisher specififc
				if("Forum".equals(publisherType)) {
					//extract the message id
					List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(item.getBusinessPath());
					if(!ces.isEmpty()) {
						ContextEntry lastCe = ces.get(ces.size() - 1);
						if("Message".equals(lastCe.getOLATResourceable().getResourceableTypeName())) {
							itemVO.setMessageKey(lastCe.getOLATResourceable().getResourceableId());
						}
					}	
				} else if("FolderModule".equals(publisherType)) {
					List<ContextEntry> ces = BusinessControlFactory.getInstance().createCEListFromString(item.getBusinessPath());
					if(!ces.isEmpty()) {
						ContextEntry lastCe = ces.get(ces.size() - 1);
						if(lastCe.getOLATResourceable().getResourceableTypeName().startsWith("path=")) {
							String path = BusinessControlFactory.getInstance().getPath(lastCe);
							itemVO.setPath(path);
						}
					}	
				}
				itemVOes.add(itemVO);
			}
			infoVO.setItems(itemVOes);
		}
		return infoVO;
	}
	
	private boolean isAdmin(HttpServletRequest request) {
		try {
			Roles roles = RestSecurityHelper.getRoles(request);
			return roles.isAdministrator();
		} catch (Exception e) {
			return false;
		}
	}
}
