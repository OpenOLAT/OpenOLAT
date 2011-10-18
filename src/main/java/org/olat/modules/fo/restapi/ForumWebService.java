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

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import org.olat.core.util.FileUtils;
import org.olat.core.util.vfs.LocalFileImpl;
import org.olat.core.util.vfs.VFSContainer;
import org.olat.core.util.vfs.VFSItem;
import org.olat.core.util.vfs.VFSLeaf;
import org.olat.core.util.vfs.restapi.SystemItemFilter;
import org.olat.core.util.vfs.restapi.VFSStreamingOutput;
import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.vo.FileVO;

/**
 * 
 * Description:<br>
 * Web service to manage forum element. This implementation is
 * only for import.
 * 
 * <P>
 * Initial Date:  20 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class ForumWebService {
	
	public static CacheControl cc = new CacheControl();
	static {
		cc.setMaxAge(-1);
	}
	
	private final Forum forum;
	private final ForumManager fom = ForumManager.getInstance();
	
	public ForumWebService(Forum forum) {
		this.forum = forum;
	}
	
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
	 * @param forumKey The id of the forum
	 * @return The list of threads
	 */
	@GET
	@Path("threads")
	public Response getThreads() {
		if(forum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		List<Message> messages = fom.getMessagesByForum(forum);
		List<MessageVO> threads = new ArrayList<MessageVO>();
		for(Message message:messages) {
			if(message.getParent() == null) {
				threads.add(new MessageVO(message));
			}
		}
		
		MessageVO[] threadArr = new MessageVO[threads.size()];
		threads.toArray(threadArr);
		return Response.ok(threads).build();
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
	 * @param authorKey The author key
	 * @param request The HTTP request
	 * @return The new thread
	 */
	@POST
	@Path("threads")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response newThreadToForumPost(@FormParam("title") String title,
			@FormParam("body") String body, @FormParam("authorKey") Long authorKey) {
		return newThreadToForum(title, body, authorKey);
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
	 * @param authorKey The author user key
	 * @return The new thread
	 */
	@PUT
	@Path("threads")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response newThreadToForum(@QueryParam("title") String title,
			@QueryParam("body") String body, @QueryParam("authorKey") Long authorKey) {
		
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		Identity identity = securityManager.loadIdentityByKey(authorKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		// creating the thread (a message without a parent message)
		Message newThread = fom.createMessage();
		newThread.setTitle(title);
		newThread.setBody(body);
		// open a new thread
		fom.addTopMessage(identity, forum, newThread);
		
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
	 * @return The messages of the thread
	 */
	@GET
	@Path("posts/{threadKey}")
	public Response getMessages( @PathParam("threadKey") Long threadKey) {
		if(forum == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		ForumManager fom = ForumManager.getInstance();
		List<Message> messages = fom.getThread(threadKey);
		MessageVO[] messageArr = new MessageVO[messages.size()];
		int i=0;
		for(Message message:messages) {
			messageArr[i++] = new MessageVO(message);
		}
		return Response.ok(messageArr).build();
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
	 * @return The new message
	 */
	@POST
	@Path("posts/{messageKey}")
	@Consumes(MediaType.APPLICATION_FORM_URLENCODED)
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response replyToPostPost(@PathParam("messageKey") Long messageKey, @FormParam("title") String title,
			@FormParam("body") String body, @FormParam("authorKey") Long authorKey) {
		return replyToPost(messageKey, title, body, authorKey);
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
	 * @param authorKey The author user key
	 * @return The new Message
	 */
	@PUT
	@Path("posts/{messageKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response replyToPost(@PathParam("messageKey") Long messageKey, @QueryParam("title") String title,
			@QueryParam("body") String body, @QueryParam("authorKey") Long authorKey) {
		
		BaseSecurity securityManager = BaseSecurityManager.getInstance();
		Identity identity = securityManager.loadIdentityByKey(authorKey, false);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

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
		fom.replyToMessage(newMessage, identity, mess);
		MessageVO vo = new MessageVO(newMessage);
		return Response.ok(vo).build();
	}
	

	/**
	 * Retrieves the attachments of the message
	 * @response.representation.200.mediaType application/xml
	 * @response.representation.200.doc The portrait as image
   * @response.representation.404.doc The identity or the portrait not found
	 * @param messageKey The key of the message
	 * @param request The REST request
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
	 * @param identityKey The identity key of the user being searched
	 * @param request The REST request
	 * @return The image
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
		Identity identity = RestSecurityHelper.getIdentity(request);
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
}
