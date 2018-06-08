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
package org.olat.modules.qpool.restapi;

import static org.olat.restapi.security.RestSecurityHelper.isQuestionPoolManager;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.i18n.I18nManager;
import org.olat.modules.qpool.QPoolService;
import org.olat.modules.qpool.QuestionItem;
import org.olat.modules.qpool.QuestionItemAuditLog.Action;
import org.olat.modules.qpool.QuestionItemAuditLogBuilder;
import org.olat.modules.qpool.QuestionItemShort;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.MultipartReader;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.stereotype.Component;

/**
 * 
 * Initial date: 8 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Component
@Path("qpool/items")
public class QuestionPoolWebService {
	
	private static final OLog log = Tracing.createLoggerFor(QuestionPoolWebService.class);
	
	@PUT
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response importQuestionItemsPut(@Context HttpServletRequest request) {
		return importQuestionItems(request);
	}
	
	@POST
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response importQuestionItemsPost(@Context HttpServletRequest request) {
		return importQuestionItems(request);
	}
	
	private Response importQuestionItems(HttpServletRequest request) {
		if(!isQuestionPoolManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		MultipartReader partsReader = null;
		try {		
			Identity identity = RestSecurityHelper.getUserRequest(request).getIdentity();
			partsReader = new MultipartReader(request);
			File tmpFile = partsReader.getFile();
			long length = tmpFile.length();
			if(length > 0) {
				String filename = partsReader.getValue("filename");
				String language = partsReader.getValue("language");
				QuestionItemVOes voes = importQuestionItem(identity, filename, tmpFile, language, identity);
				return Response.ok(voes).build();
			}
			return Response.serverError().status(Status.NO_CONTENT).build();
		} catch (Exception e) {
			log.error("Error while importing a file",e);
		} finally {
			MultipartReader.closeQuietly(partsReader);
		}
		return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
	}
	
	private QuestionItemVOes importQuestionItem(Identity owner, String filename, File tmpFile, String language, Identity executor) {
		Locale locale = CoreSpringFactory.getImpl(I18nManager.class).getLocaleOrDefault(language);
		
		QPoolService qpoolService = CoreSpringFactory.getImpl(QPoolService.class);
		List<QuestionItem> items = qpoolService.importItems(owner, locale, filename, tmpFile);
		for (QuestionItem item: items) {
			QuestionItemAuditLogBuilder builder = qpoolService.createAuditLogBuilder(executor,
					Action.CREATE_QUESTION_ITEM_BY_IMPORT);
			builder.withAfter(item);
			qpoolService.persist(builder.create());
		}
		QuestionItemVOes voes = new QuestionItemVOes();
		QuestionItemVO[] voArray = new QuestionItemVO[items.size()];
		for(int i=items.size(); i-->0; ) {
			voArray[i] = new QuestionItemVO(items.get(i));
		}
		voes.setQuestionItems(voArray);
		voes.setTotalCount(items.size());
		return voes;
	}
	
	/**
	 * Delete a question item by id.
	 * 
	 * @response.representation.200.doc Nothing
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The question item not found
	 * @param itemKey The question item identifier
	 * @param request The HTTP request
	 * @return Nothing
	 */
	@DELETE
	@Path("{itemKey}")
	public Response deleteQuestionItem(@PathParam("itemKey") Long itemKey, @Context HttpServletRequest request) {
		if(!isQuestionPoolManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		QPoolService poolService = CoreSpringFactory.getImpl(QPoolService.class);
		QuestionItem item = poolService.loadItemById(itemKey);
		if(item == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		List<QuestionItem> itemToDelete = Collections.singletonList(item);
		poolService.deleteItems(itemToDelete);
		Identity identity = RestSecurityHelper.getUserRequest(request).getIdentity();
		QuestionItemAuditLogBuilder builder = poolService.createAuditLogBuilder(identity,
				Action.DELETE_QUESTION_ITEM);
		builder.withBefore(item);
		poolService.persist(builder.create());
		
		return Response.ok().build();
	}
	
	/**
	 * Get all authors of the question item.
	 * 
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of authors
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The question item not found
	 * @param itemKey The question item identifier
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{itemKey}/authors")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getAuthors(@PathParam("itemKey") Long itemKey,
			@Context HttpServletRequest request) {

		if(!isQuestionPoolManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		QPoolService poolService = CoreSpringFactory.getImpl(QPoolService.class);
		QuestionItem item = poolService.loadItemById(itemKey);
		if(item == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		List<Identity> authorList = poolService.getAuthors(item);
		
		int count = 0;
		UserVO[] authors = new UserVO[authorList.size()];
		for(Identity author:authorList) {
			authors[count++] = UserVOFactory.get(author);
		}
		return Response.ok(authors).build();
	}
	
	/**
	 * Get this specific author of the quesiton item.
	 * 
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The author
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The question item not found or the user is not an author of the course
	 * @param itemKey The question item identifier
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns an <code>UserVO</code>
	 */
	@GET
	@Path("{itemKey}/authors/{identityKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getAuthor(@PathParam("itemKey") Long itemKey, @PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest request) {
		if(!isQuestionPoolManager(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		QPoolService poolService = CoreSpringFactory.getImpl(QPoolService.class);
		QuestionItem item = poolService.loadItemById(itemKey);
		if(item == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		List<Identity> authorList = poolService.getAuthors(item);
		for(Identity author:authorList) {
			if(author.getKey().equals(identityKey)) {
				UserVO authorVo = UserVOFactory.get(author);
				return Response.ok(authorVo).build();
			}
		}
		return Response.serverError().status(Status.NOT_FOUND).build();
	}
	
	/**
	 * Add an author to the question item.
	 * 
	 * @response.representation.200.doc The user is an author of the question item
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The question item or the user not found
	 * @param itemKey The question item identifier
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is added as author of the question item
	 */
	@PUT
	@Path("{itemKey}/authors/{identityKey}")
	public Response addAuthor(@PathParam("itemKey") Long itemKey, @PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if(!isQuestionPoolManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		QPoolService poolService = CoreSpringFactory.getImpl(QPoolService.class);
		QuestionItem item = poolService.loadItemById(itemKey);
		if(item == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity author = securityManager.loadIdentityByKey(identityKey, false);
		if(author == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		List<Identity> authors = Collections.singletonList(author);
		List<QuestionItemShort> items = Collections.singletonList(item);
		poolService.addAuthors(authors, items);
		return Response.ok().build();
	}
	
	/**
	 * Remove an author to the question item.
	 * 
	 * @response.representation.200.doc The user was successfully removed as author of the question item
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The question item or the user not found
	 * @param itemKey The question item identifier
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is removed as author of the question item
	 */
	@DELETE
	@Path("{itemKey}/authors/{identityKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response removeAuthor(@PathParam("itemKey") Long itemKey, @PathParam("identityKey") Long identityKey,
			@Context HttpServletRequest httpRequest) {
		if(!isQuestionPoolManager(httpRequest)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}

		QPoolService poolService = CoreSpringFactory.getImpl(QPoolService.class);
		QuestionItem item = poolService.loadItemById(itemKey);
		if(item == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		BaseSecurity securityManager = CoreSpringFactory.getImpl(BaseSecurity.class);
		Identity author = securityManager.loadIdentityByKey(identityKey, false);
		if(author == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		List<Identity> authors = Collections.singletonList(author);
		List<QuestionItemShort> items = Collections.singletonList(item);
		poolService.removeAuthors(authors, items);
		return Response.ok().build();
	}	
}
