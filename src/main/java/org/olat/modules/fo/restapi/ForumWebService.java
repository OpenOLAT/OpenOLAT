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

import java.util.ArrayList;
import java.util.List;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.modules.fo.Forum;
import org.olat.modules.fo.ForumManager;
import org.olat.modules.fo.Message;

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
	
	private final Forum forum;
	
	public ForumWebService(Forum forum) {
		this.forum = forum;
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

		ForumManager fom = ForumManager.getInstance();
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
}
