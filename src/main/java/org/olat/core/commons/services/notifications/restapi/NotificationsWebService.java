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

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

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
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.user.restapi.UserVO;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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
	 * @response.representation.200.qname {http://www.example.com}publisherVo
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The publisher
	 * @response.representation.200.example {@link org.olat.core.commons.services.notifications.restapi.vo.Examples#SAMPLE_PUBLISHERVO}
	 * @response.representation.204.doc The publisher doesn't exist
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @return It returns the <code>CourseVO</code> object representing the course.
	 */
	@GET
	@Path("publisher/{ressourceName}/{ressourceId}/{subIdentifier}")
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getPublisher(@PathParam("ressourceName") String ressourceName, @PathParam("ressourceId") Long ressourceId,
			@PathParam("subIdentifier") String subIdentifier, @Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
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
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getSubscriber(@PathParam("ressourceName") String ressourceName, @PathParam("ressourceId") Long ressourceId,
			@PathParam("subIdentifier") String subIdentifier, @Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		SubscriptionContext subsContext
			= new SubscriptionContext(ressourceName, ressourceId, subIdentifier);

		Publisher publisher = notificationsMgr.getPublisher(subsContext);
		if(publisher == null) {
			return Response.ok().status(Status.NO_CONTENT).build();
		}
		
		List<Subscriber> subscribers = notificationsMgr.getSubscribers(publisher);
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
	@Consumes({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response subscribe(PublisherVO publisherVO, @Context HttpServletRequest request) {
		if(!isAdmin(request)) {
			return Response.serverError().status(Status.NOT_FOUND).build();
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
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The notifications
	 * @response.representation.200.example {@link org.olat.core.commons.services.notifications.restapi.vo.Examples#SAMPLE_INFOVOes}
	 * @response.representation.404.doc The identity not found
	 * @param date The date (optional)
	 * @param type The type of notifications (User, Forum...) (optional)
	 * @param httpRequest The HTTP request
	 * @return an xml or json representation of a the user being search. The xml
	 *         correspond to a <code>SubscriptionInfoVO</code>. <code>SubscriptionInfoVO</code>
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML ,MediaType.APPLICATION_JSON})
	public Response getNotifications(@QueryParam("date") String date,
			@QueryParam("type") String type, @Context HttpServletRequest httpRequest) {
		Identity identity = RestSecurityHelper.getIdentity(httpRequest);
		Locale locale = RestSecurityHelper.getLocale(httpRequest);
		
		Date compareDate;
		if(StringHelper.containsNonWhitespace(date)) {
			compareDate = parseDate(date, locale);
		} else {
			NotificationsManager man = NotificationsManager.getInstance();
			compareDate = man.getCompareDateFromInterval(man.getUserIntervalOrDefault(identity));
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
		try {//TODO roles, make it resource dependant
			Roles roles = RestSecurityHelper.getRoles(request);
			return roles.isAdministrator();
		} catch (Exception e) {
			return false;
		}
	}
}
