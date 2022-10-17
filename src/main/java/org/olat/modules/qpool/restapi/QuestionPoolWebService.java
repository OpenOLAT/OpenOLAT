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

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.io.File;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.id.Identity;
import org.olat.core.id.Roles;
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

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * 
 * Initial date: 8 sept. 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Qpool")
@Component
@Path("qpool/items")
public class QuestionPoolWebService {
	
	private static final Logger log = Tracing.createLoggerFor(QuestionPoolWebService.class);
	
	@PUT
	@Operation(summary = "Put QuestionItem", description = "Put QuestionItem")
	@ApiResponse(responseCode = "200", description = "The QuestionItem", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = QuestionItemVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = QuestionItemVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Consumes({MediaType.MULTIPART_FORM_DATA})
	public Response importQuestionItemsPut(@Context HttpServletRequest request) {
		return importQuestionItems(request);
	}
	
	@POST
	@Operation(summary = "Post QuestionItem", description = "Post QuestionItem")
	@ApiResponse(responseCode = "200", description = "The QuestionItem", content = {
			@Content(mediaType = "application/json", schema = @Schema(implementation = QuestionItemVO.class)),
			@Content(mediaType = "application/xml", schema = @Schema(implementation = QuestionItemVO.class)) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
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
	 * @param itemKey The question item identifier
	 * @param request The HTTP request
	 * @return Nothing
	 */
	@DELETE
	@Path("{itemKey}")
	@Operation(summary = "Delete a question item by id", description = "Delete a question item by id")
	@ApiResponse(responseCode = "200", description = "Nothing")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The question item not found")
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
	 * @param itemKey The question item identifier
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{itemKey}/authors")
	@Operation(summary = "Get all authors of the question item", description = "Get all authors of the question item")
	@ApiResponse(responseCode = "200", description = "The array of authors", content = {
			@Content(mediaType = "application/json", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))),
			@Content(mediaType = "application/xml", array = @ArraySchema(schema = @Schema(implementation = UserVO.class))) })
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The question item not found")
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
	 * @param itemKey The question item identifier
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns an <code>UserVO</code>
	 */
	@GET
	@Path("{itemKey}/authors/{identityKey}")
	@Operation(summary = "Get this specific author of the quesiton item", description = "Get this specific author of the quesiton item")
	@ApiResponse(responseCode = "200", description = "The author",
			content = {
					@Content(mediaType = "application/json", schema = @Schema(implementation = UserVO.class)),
					@Content(mediaType = "application/xml", schema = @Schema(implementation = UserVO.class))
				})
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The question item not found")
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
	 * @param itemKey The question item identifier
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is added as author of the question item
	 */
	@PUT
	@Path("{itemKey}/authors/{identityKey}")
	@Operation(summary = "Add an author to the question item", description = "Add an author to the question item")
	@ApiResponse(responseCode = "200", description = "The user is an author of the question item")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The question item or the user not found")
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
	 * @param itemKey The question item identifier
	 * @param identityKey The user identifier
	 * @param httpRequest The HTTP request
	 * @return It returns 200  if the user is removed as author of the question item
	 */
	@DELETE
	@Path("{itemKey}/authors/{identityKey}")
	@Operation(summary = "Remove an author to the question item", description = "Remove an author to the question item")
	@ApiResponse(responseCode = "200", description = "The user was successfully removed as author of the question item")
	@ApiResponse(responseCode = "401", description = "The roles of the authenticated user are not sufficient")
	@ApiResponse(responseCode = "404", description = "The question item or the user not found")
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
	
	private boolean isQuestionPoolManager(HttpServletRequest request) {
		try {
			Roles roles = getRoles(request);
			return (roles.isPoolManager() || roles.isAdministrator());
		} catch (Exception e) {
			return false;
		}
	}
}
