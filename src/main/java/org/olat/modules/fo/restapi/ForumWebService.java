/**
 * OLAT - Online Learning and Training<br>
 * http://www.olat.org
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 * <p>
 */

package org.olat.modules.fo.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.isAdmin;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;
import javax.ws.rs.core.StreamingOutput;
import javax.ws.rs.core.UriInfo;

import org.olat.basesecurity.BaseSecurity;
import org.olat.basesecurity.BaseSecurityManager;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.restapi.SystemItemFilter;
import org.olat.core.util.vfs.restapi.VFSStreamingOutput;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;
import org.olat.restapi.support.MediaTypeVariants;
import org.olat.restapi.support.vo.FileVO;

/**
 * 
 * Description:<br>
 * Web service to manage a forum.
 * 
 * <P>
 * Initial Date:  20 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ForumWebService {
	
	private static final OLog log = Tracing.createLoggerFor(ForumWebService.class);
	
	public static CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	private final Forum forum;
	private final ForumManager fom = ForumManager.getInstance();
	
	public ForumWebService(Forum forum) {
		this.forum = forum;
	}
	
	/**
	 * Retrieves the forum.
	 * @response.representation.200.qname {http://www.example.com}forumVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The root message of the thread
	 * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_FORUMVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The forum not found
	 * @return The forum
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getForum() {
		if(forum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		ForumVO forumVo = new ForumVO(forum);
		return Response.ok(forumVo).build();
	}
	
	/**
	 * Retrieves the threads in the forum
	 * @response.representation.200.qname {http://www.example.com}messageVOes
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The root message of the thread
   * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_MESSAGEVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The author, forum or message not found
	 * @param start
	 * @param limit
	 * @param orderBy (value name,creationDate)
	 * @param asc (value true/false)
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The list of threads
	 */
	@GET
	@Path("threads")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getThreads(@QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit,  @QueryParam("orderBy") @DefaultValue("creationDate") String orderBy,
			@QueryParam("asc") @DefaultValue("true") Boolean asc, @Context HttpServletRequest httpRequest, @Context Request request) {
		if(forum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			int totalCount = fom.countThreadsByForumID(forum.getKey());
			Message.OrderBy order = toEnum(orderBy);
			List<Message> threads = fom.getThreadsByForumID(forum.getKey(), start, limit, order, asc);
			MessageVO[] vos = toArrayOfVO(threads);
			MessageVOes voes = new MessageVOes();
			voes.setMessages(vos);
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			List<Message> threads = fom.getThreadsByForumID(forum.getKey(), 0, -1, null, true);
			MessageVO[] voes = toArrayOfVO(threads);
			return Response.ok(voes).build();
		}
	}
	
	/**
	 * Creates a new thread in the forum of the course node
   * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.200.qname {http://www.example.com}messageVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The root message of the thread
   * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_MESSAGEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The author, forum or message not found
	 * @param forumKey The id of the forum
	 * @param title The title for the first post in the thread
	 * @param body The body for the first post in the thread
	 * @param authorKey The author key (optional)
	 * @param httpRequest The HTTP request
	 * @return The new thread
	 */
	@POST
	@Path("threads")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response newThreadToForumPost(@FormParam("title") String title,
			@FormParam("body") String body, @FormParam("authorKey") Long authorKey,
			@Context HttpServletRequest httpRequest) {
		return newThreadToForum(title, body, authorKey, httpRequest);
	}
	
	/**
	 * Creates a new thread in the forum of the course node
	 * @response.representation.200.qname {http://www.example.com}messageVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The root message of the thread
   * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_MESSAGEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The author, forum or message not found
	 * @param title The title for the first post in the thread
	 * @param body The body for the first post in the thread
	 * @param authorKey The author user key (optional)
	 * @param httpRequest The HTTP request
	 * @return The new thread
	 */
	@PUT
	@Path("threads")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response newThreadToForum(@QueryParam("title") String title,
			@QueryParam("body") String body, @QueryParam("authorKey") Long authorKey,
			@Context HttpServletRequest httpRequest) {

		Identity author = getMessageAuthor(authorKey, httpRequest);
		// creating the thread (a message without a parent message)
		Message newThread = fom.createMessage();
		newThread.setTitle(title);
		newThread.setBody(body);
		// open a new thread
		fom.addTopMessage(author, forum, newThread);
		
		MessageVO vo = new MessageVO(newThread);
		return Response.ok(vo).build();
	}

	/**
	 * Retrieves the messages in the thread
	 * @response.representation.200.qname {http://www.example.com}messageVOes
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The root message of the thread
   * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_MESSAGEVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The author, forum or message not found
	 * @param threadKey The key of the thread
	 * @param start
	 * @param limit
	 * @param orderBy (value name, creationDate)
	 * @param asc (value true/false)
	 * @param httpRequest The HTTP request
	 * @param request The REST request
	 * @return The messages of the thread
	 */
	@GET
	@Path("posts/{threadKey}")
	public Response getMessages( @PathParam("threadKey") Long threadKey, @QueryParam("start") @DefaultValue("0") Integer start,
			@QueryParam("limit") @DefaultValue("25") Integer limit, @QueryParam("orderBy") @DefaultValue("creationDate") String orderBy,
			@QueryParam("asc") @DefaultValue("true") Boolean asc, @Context HttpServletRequest httpRequest, @Context Request request) {
		
		if(forum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		ForumManager fom = ForumManager.getInstance();
		if(MediaTypeVariants.isPaged(httpRequest, request)) {
			int totalCount = fom.countThread(threadKey);
			Message.OrderBy order = toEnum(orderBy);
			List<Message> threads = fom.getThread(threadKey, start, limit, order, asc);
			MessageVO[] vos = toArrayOfVO(threads);
			MessageVOes voes = new MessageVOes();
			voes.setMessages(vos);
			voes.setTotalCount(totalCount);
			return Response.ok(voes).build();
		} else {
			List<Message> messages = fom.getThread(threadKey);
			MessageVO[] messageArr = toArrayOfVO(messages);
			return Response.ok(messageArr).build();
		}
	}
	
	/**
	 * Creates a new reply in the forum of the course node
   * @response.representation.mediaType application/x-www-form-urlencoded
	 * @response.representation.200.qname {http://www.example.com}messageVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The root message of the thread
   * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_MESSAGEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The author or message not found
	 * @param messageKey The id of the reply message
	 * @param title The title for the first post in the thread
	 * @param body The body for the first post in the thread
	 * @param authorKey The author key
	 * @param httpRequest The HTTP request
	 * @return The new message
	 */
	@POST
	@Path("posts/{messageKey}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response replyToPostPost(@PathParam("messageKey") Long messageKey, @FormParam("title") String title,
			@FormParam("body") String body, @FormParam("authorKey") Long authorKey,
			@Context HttpServletRequest httpRequest) {
		return replyToPost(messageKey, title, body, authorKey, httpRequest);
	}
	
	/**
	 * Creates a new reply in the forum of the course node
	 * @response.representation.200.qname {http://www.example.com}messageVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc The root message of the thread
   * @response.representation.200.example {@link org.olat.modules.fo.restapi.Examples#SAMPLE_MESSAGEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The author or message not found
	 * @param messageKey The id of the reply message
	 * @param title The title for the first post in the thread
	 * @param body The body for the first post in the thread
	 * @param authorKey The author user key (optional)
	 * @param httpRequest The HTTP request
	 * @return The new Message
	 */
	@PUT
	@Path("posts/{messageKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response replyToPost(@PathParam("messageKey") Long messageKey, @QueryParam("title") String title,
			@QueryParam("body") String body, @QueryParam("authorKey") Long authorKey,
			@Context HttpServletRequest httpRequest) {

		Identity author = this.getMessageAuthor(authorKey, httpRequest);
		// load message
		Message mess = fom.loadMessage(messageKey);
		if(mess == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!forum.equalsByPersistableKey(mess.getForum())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		// creating the thread (a message without a parent message)
		Message newMessage = fom.createMessage();
		newMessage.setTitle(title);
		newMessage.setBody(body);
		fom.replyToMessage(newMessage, author, mess);
		MessageVO vo = new MessageVO(newMessage);
		return Response.ok(vo).build();
	}
	

	/**
	 * Retrieves the attachments of the message
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The links to the attachments
	 * @response.representation.404.doc The message not found
	 * @param messageKey The key of the message
	 * @param uriInfo The URI information
	 * @return The attachments
	 */
	@GET
	@Path("posts/{messageKey}/attachments")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getAttachments(@PathParam("messageKey") Long messageKey, @Context UriInfo uriInfo) {
		//load message
		Message mess = fom.loadMessage(messageKey);
		if(mess == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!forum.equalsByPersistableKey(mess.getForum())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		VFSContainer container = fom.getMessageContainer(mess.getForum().getKey(), mess.getKey());

		List<FileVO> attachments = new ArrayList<FileVO>();
		for(VFSItem item: container.getItems(new SystemItemFilter())) {
			String uri = uriInfo.getAbsolutePathBuilder().path(format(item.getName())).build().toString();
			if(item instanceof VFSLeaf) {
				attachments.add(new FileVO("self", uri, item.getName(), ((VFSLeaf)item).getSize()));
			} else {
				attachments.add(new FileVO("self", uri, item.getName()));
			}
		}
		
		FileVO[] attachmentArr = new FileVO[attachments.size()];
		attachmentArr = attachments.toArray(attachmentArr);
		return Response.ok(attachmentArr).build();
	}
	
	/**
	 * Retrieves the attachment of the message
	 * @response.representation.200.mediaType application/octet-stream
	 * @response.representation.200.doc The portrait as image
   * @response.representation.404.doc The identity or the portrait not found
	 * @param messageKey The identity key of the user being searched
	 * @param filename The name of the attachment
	 * @param request The REST request
	 * @return The attachment
	 */
	@GET
	@Path("posts/{messageKey}/attachments/{filename}")
	@Produces({"*/*", MediaType.APPLICATION_OCTET_STREAM})
	public Response getAttachment(@PathParam("messageKey") Long messageKey, @PathParam("filename") String filename, 
			@Context Request request) {
		//load message
		Message mess = fom.loadMessage(messageKey);
		if(mess == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!forum.equalsByPersistableKey(mess.getForum())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		VFSContainer container = fom.getMessageContainer(mess.getForum().getKey(), mess.getKey());
		VFSItem item = container.resolve(filename);
		if(item instanceof LocalFileImpl) {
			//local file -> the length is given to the client which is good
			Date lastModified = new Date(item.getLastModified());
			Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
			if(response == null) {
				File attachment = ((LocalFileImpl)item).getBasefile();
				response = Response.ok(attachment).lastModified(lastModified).cacheControl(cc);
			}
			return response.build();
		} else if (item instanceof VFSLeaf) {
			//stream -> the length is not given to the client which is not nice
			Date lastModified = new Date(item.getLastModified());
			Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
			if(response == null) {
				StreamingOutput attachment = new VFSStreamingOutput((VFSLeaf)item);
				response = Response.ok(attachment).lastModified(lastModified).cacheControl(cc);
			}
			return response.build();
		}
		return Response.serverError().status(Status.NOT_FOUND).build();
	}
	
	/**
	 * Upload the attachment of a message
	 * @response.representation.200.mediaType application/json, application/xml
	 * @response.representation.200.doc Ok
	 * @response.representation.404.doc The identity or the portrait not found
	 * @param messageKey The key of the message
	 * @param filename The name of the attachment
	 * @file file The attachment
	 * @param request The HTTP request
	 * @return Ok
	 */
	@POST
	@Path("posts/{messageKey}/attachments")
	@Consumes(MediaType.MULTIPART_FORM_DATA)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response replyToPostAttachment(@PathParam("messageKey") Long messageKey, @FormParam("filename") String filename,
			@FormParam("file") InputStream file, @Context HttpServletRequest request) {
		return attachToPost(messageKey, filename, file, request);
	}
	
	protected Response attachToPost(Long messageKey, String filename, InputStream file,  HttpServletRequest request) {
		//load message
		Message mess = fom.loadMessage(messageKey);
		if(mess == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!forum.equalsByPersistableKey(mess.getForum())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		return attachToPost(mess, filename, file, request);
	}

	protected Response attachToPost(Message mess, String filename, InputStream file,  HttpServletRequest request) {
		Identity identity = getIdentity(request);
		if(identity == null) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		} else if (!identity.equalsByPersistableKey(mess.getCreator())) {
			if(mess.getModifier() == null || !identity.equalsByPersistableKey(mess.getModifier())) {
				return Response.serverError().status(Status.UNAUTHORIZED).build();
			}
		}

		VFSContainer container = fom.getMessageContainer(mess.getForum().getKey(), mess.getKey());
		VFSItem item = container.resolve(filename);
		VFSLeaf attachment = null;
		if(item == null) {
			attachment = container.createChildLeaf(filename);
		} else if (item instanceof VFSLeaf) {
			attachment = (VFSLeaf)item;
		} else {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		OutputStream out = attachment.getOutputStream(false);
		FileUtils.copy(file, out);
		FileUtils.closeSafely(out);
		FileUtils.closeSafely(file);
		return Response.ok().build();
	}
	
	public String format(String segment) {
		segment = segment.replace(" ", "_");
		return segment;
	}
	
	private MessageVO[] toArrayOfVO(List<Message> threads) {
		MessageVO[] threadArr = new MessageVO[threads.size()];
		int i=0;
		for(Message thread:threads) {
			threadArr[i++] = new MessageVO(thread);
		}
		return threadArr;
	}
	
	private Message.OrderBy toEnum(String str) {
		if(StringHelper.containsNonWhitespace(str)) {
			try {
				return Message.OrderBy.valueOf(str);
			} catch (Exception e) {
				log.warn("", e);
			}
		}
		return null;
	}
	
	private Identity getMessageAuthor(Long authorKey, HttpServletRequest httpRequest) {
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		Identity author;
		if(authorKey == null) {
			author = getIdentity(httpRequest);
		} else if(isAdmin(httpRequest)) {
			author = securityManager.loadIdentityByKey(authorKey, false);
		} else {
			author = getIdentity(httpRequest);
			if(!authorKey.equals(author.getKey())) {
				throw new WebApplicationException(Status.CONFLICT);
			}
		}
		
		if(author == null) {
			throw new WebApplicationException(Status.NOT_FOUND);
		}
		
		return author;
	}
}
