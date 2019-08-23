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
package org.olat.modules.curriculum.restapi;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.HEAD;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.basesecurity.BaseSecurity;
import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.core.id.Identity;
import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementStatus;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumLectures;
import org.olat.modules.curriculum.CurriculumRoles;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.manager.CurriculumElementToTaxonomyLevelDAO;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.olat.modules.curriculum.model.CurriculumMember;
import org.olat.modules.curriculum.model.SearchMemberParameters;
import org.olat.modules.taxonomy.TaxonomyLevel;
import org.olat.modules.taxonomy.TaxonomyService;
import org.olat.modules.taxonomy.model.TaxonomyLevelRefImpl;
import org.olat.modules.taxonomy.restapi.TaxonomyLevelVO;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryService;
import org.olat.restapi.support.vo.RepositoryEntryVO;
import org.olat.user.restapi.UserVO;
import org.olat.user.restapi.UserVOFactory;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * The security checks are done by the CurriculumsWebService.
 * 
 * Initial date: 15 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementsWebService {
	
	@Autowired
	private DB dbInstance;
	@Autowired
	private BaseSecurity securityManager;
	@Autowired
	private TaxonomyService taxonomyService;
	@Autowired
	private CurriculumService curriculumService;
	@Autowired
	private RepositoryService repositoryService;
	@Autowired
	private CurriculumElementToTaxonomyLevelDAO curriculumElementToTaxonomyLevelDao;
	
	private Curriculum curriculum;
	
	public CurriculumElementsWebService(Curriculum curriculum) {
		this.curriculum = curriculum;
		CoreSpringFactory.autowireObject(this);
	}
	
	/**
	 * Return the curriculum elements of a curriculum.
	 * 
	 * @response.representation.200.qname {http://www.example.com}curriculumElementVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc A taxonomy
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param taxonomyKey If true, the status of the block is done or the status of the roll call is closed or auto closed
	 * @param httpRequest  The HTTP request
	 * @return The taxonomy
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCurriculumElements() {
		List<CurriculumElement> elements = curriculumService.getCurriculumElements(curriculum, CurriculumElementStatus.notDeleted());
		List<CurriculumElementVO> voes = new ArrayList<>(elements.size());
		for(CurriculumElement element:elements) {
			voes.add(CurriculumElementVO.valueOf(element));
		}
		return Response.ok(voes.toArray(new CurriculumElementVO[voes.size()])).build();
	}
	
	/**
	 * Get a specific curriculum element.
	 * 
	 * @response.representation.200.qname {http://www.example.com}curriculumElementVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The curriculum element
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param curriculumElementKey The curriculum element primary key
	 * @param httpRequest The HTTP request
	 * @return The curriculum element
	 */
	@GET
	@Path("{curriculumElementKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCurriculumElement(@PathParam("curriculumElementKey") Long curriculumElementKey, @Context HttpServletRequest httpRequest) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		CurriculumElementVO curriculumElementVo = CurriculumElementVO.valueOf(curriculumElement);
		return Response.ok(curriculumElementVo).build();
	}
	
	/**
	 * Creates and persists a new curriculum element entity.
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumElementVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element to persist
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The persisted curriculum element
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param curriculumElement The curriculum element to persist
	 * @return The new persisted <code>curriculum element</code>
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putCurriculumElement(CurriculumElementVO curriculumElement)
	throws WebApplicationException {
		CurriculumElement savedElement = saveCurriculumElement(curriculumElement);
		return Response.ok(CurriculumElementVO.valueOf(savedElement)).build();
	}
	
	/**
	 * Updates a curriculum element entity.
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumElementVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element to update
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged curriculum element
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param curriculumElement The curriculum element to merge
	 * @return The merged <code>curriculum element</code>
	 */
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCurriculumElement(CurriculumElementVO curriculumElement)
	throws WebApplicationException {
		CurriculumElement savedElement = saveCurriculumElement(curriculumElement);
		return Response.ok(CurriculumElementVO.valueOf(savedElement)).build();
	}
	
	/**
	 * Updates a curriculum element entity. The primary key is taken from
	 * the URL. The curriculum element object can be "primary key free".
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumElementVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element to update
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged curriculum element
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param curriculumElementKey The curriculum element primary key
	 * @param curriculumElement The curriculum element to merge
	 * @return The merged <code>curriculum element</code>
	 */
	@POST
	@Path("{curriculumElementKey}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCurriculumElement(@PathParam("curriculumElementKey") Long curriculumElementKey, CurriculumElementVO curriculumElement)
	throws WebApplicationException {
		if(curriculumElement.getKey() == null) {
			curriculumElement.setKey(curriculumElementKey);
		} else if(!curriculumElementKey.equals(curriculumElement.getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		CurriculumElement savedElement = saveCurriculumElement(curriculumElement);
		return Response.ok(CurriculumElementVO.valueOf(savedElement)).build();
	}
	
	
	private CurriculumElement saveCurriculumElement(CurriculumElementVO curriculumElement)
	throws WebApplicationException {
		CurriculumElement elementToSave = null;
		CurriculumElementType type = null;
		if(curriculumElement.getCurriculumElementTypeKey() != null) {
			type = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(curriculumElement.getCurriculumElementTypeKey()));
		}
		CurriculumElement parentElement = null;
		if(curriculumElement.getParentElementKey() != null) {
			parentElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElement.getParentElementKey()));
			checkCurriculum(parentElement);
			if(curriculumElement.getParentElementKey().equals(curriculumElement.getKey())) {
				throw new WebApplicationException(Status.CONFLICT);
			}
		}
		
		boolean move = false;
		boolean moveAsCurriculumRoot = false;
		if(curriculumElement.getKey() == null) {
			elementToSave = curriculumService.createCurriculumElement(curriculumElement.getIdentifier(), curriculumElement.getDisplayName(),
					null, curriculumElement.getBeginDate(), curriculumElement.getEndDate(), parentElement, type,
					CurriculumCalendars.disabled, CurriculumLectures.disabled, curriculum);
		} else {
			elementToSave = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElement.getKey()));
			elementToSave.setDisplayName(curriculumElement.getDisplayName());
			elementToSave.setIdentifier(curriculumElement.getIdentifier());
			elementToSave.setBeginDate(curriculumElement.getBeginDate());
			elementToSave.setEndDate(curriculumElement.getEndDate());
			elementToSave.setType(type);
			if((parentElement != null && elementToSave.getParent() != null && !elementToSave.getParent().getKey().equals(parentElement.getKey()))) {
				move = true;
			} else if(parentElement == null && elementToSave.getParent() == null
					&& (elementToSave.getCurriculum() != null && !elementToSave.getCurriculum().getKey().equals(curriculum.getKey()))) {
				// this is a root curriculum element and it get a new curriculum as home
				moveAsCurriculumRoot = true;
			} else if(!elementToSave.getCurriculum().getKey().equals(curriculumElement.getCurriculumKey())) {
				move = true;
			}
		}
		
		elementToSave.setDescription(curriculumElement.getDescription());
		elementToSave.setExternalId(curriculumElement.getExternalId());
		elementToSave.setManagedFlags(CurriculumElementManagedFlag.toEnum(curriculumElement.getManagedFlagsString()));
		if(StringHelper.containsNonWhitespace(curriculumElement.getStatus())) {
			elementToSave.setElementStatus(CurriculumElementStatus.valueOf(curriculumElement.getStatus()));
		}
		if(StringHelper.containsNonWhitespace(curriculumElement.getCalendars())) {
			elementToSave.setCalendars(CurriculumCalendars.valueOf(curriculumElement.getCalendars()));
		} else {
			elementToSave.setCalendars(null);
		}
		CurriculumElement savedElement = curriculumService.updateCurriculumElement(elementToSave);
		if(move) {
			curriculumService.moveCurriculumElement(savedElement, parentElement, null, curriculum);
			dbInstance.commit();
			savedElement = curriculumService.getCurriculumElement(savedElement);
		} else if(moveAsCurriculumRoot) {
			dbInstance.commit();// make sure all is flushed on the database before such a move
			curriculum = curriculumService.getCurriculum(curriculum);
			savedElement = curriculumService.moveCurriculumElement(savedElement, curriculum);
		}
		return savedElement;
	}
	
	private void checkCurriculum(CurriculumElement element) {
		if(element.getCurriculum() != null && !element.getCurriculum().getKey().equals(curriculum.getKey())) {
			throw new WebApplicationException(Response.serverError().status(Status.CONFLICT).build());
		}
	}
	
	/**
	 * Get the curriculum elements laying under the specified curriculum element.
	 * 
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc Get the curriculum elements under the specified element
	 * @response.representation.200.doc The curriculum elements under the specified element
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element was not found
	 * @param curriculumElementKey The curriculum element
	 * @return An array of curriculum elements
	 */
	@GET
	@Path("{curriculumElementKey}/elements")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCurriculumElementChildren(@PathParam("curriculumElementKey") Long curriculumElementKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		List<CurriculumElement> entries = curriculumService.getCurriculumElements(curriculumElement);
		CurriculumElementVO[] entriesVoes = new CurriculumElementVO[entries.size()];
		for(int i=entries.size(); i-->0; ) {
			entriesVoes[i] = CurriculumElementVO.valueOf(entries.get(i));
		}
		return Response.ok(entriesVoes).build();
	}

	/**
	 * Get the repository entries laying under the specified curriculum element.
	 * 
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc Get the repository entries
	 * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The repository entries
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_REPOENTRYVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the repository entry was not found
	 * @param curriculumElementKey The curriculum element
	 * @return An array of repository entries
	 */
	@GET
	@Path("{curriculumElementKey}/entries")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRepositoryEntriesInElement(@PathParam("curriculumElementKey") Long curriculumElementKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(curriculumElement);
		RepositoryEntryVO[] entriesVoes = new RepositoryEntryVO[entries.size()];
		for(int i=entries.size(); i-->0; ) {
			entriesVoes[i] = RepositoryEntryVO.valueOf(entries.get(i));
		}
		return Response.ok(entriesVoes).build();
	}
	
	/**
	 * To see if a repository entry is under the specified curriculum element.
	 * 
	 * @response.representation.200.doc The repository entry is there
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the repository entry was not found
	 * @param curriculumElementKey The curriculum element
	 * @param repositoryEntryKey The repository entry
	 * @return Nothing
	 */
	@HEAD
	@Path("{curriculumElementKey}/entries/{repositoryEntryKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response headRepositoryEntryInElement(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("repositoryEntryKey") Long repositoryEntryKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(curriculumElement);
		for(RepositoryEntry entry:entries) {
			if(entry.getKey().equals(repositoryEntryKey)) {
				return Response.ok().build();
			}
		}
		return Response.serverError().status(Status.NOT_FOUND).build();
	}
	
	/**
	 * Load the repository entry laying under the specified curriculum element.
	 * 
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc Get the repository entries
	 * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The repository entries
	 * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_REPOENTRYVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the repository entry was not found
	 * @param curriculumElementKey The curriculum element
	 * @return An array of repository entries
	 */
	@GET
	@Path("{curriculumElementKey}/entries/{repositoryEntryKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getRepositoryEntryInElement(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("repositoryEntryKey") Long repositoryEntryKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		List<RepositoryEntry> entries = curriculumService.getRepositoryEntries(curriculumElement);
		for(RepositoryEntry entry:entries) {
			if(entry.getKey().equals(repositoryEntryKey)) {
				return Response.ok(RepositoryEntryVO.valueOf(entry)).build();
			}
		}
		return Response.serverError().status(Status.NOT_FOUND).build();
	}
	
	/**
	 * Add a relation between a repository entry and a curriculum element.
	 * 
	 * @response.representation.200.doc The relation was added
	 * @response.representation.304.doc There is already a relation, nothing changed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the repository entry was not found
	 * @param curriculumElementKey The curriculum element
	 * @param repositoryEntryKey The repository entry
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/entries/{repositoryEntryKey}")
	public Response addRepositoryEntryToElement(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("repositoryEntryKey") Long repositoryEntryKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		RepositoryEntry entry = repositoryService.loadByKey(repositoryEntryKey);
		if(entry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(!curriculumService.hasRepositoryEntry(curriculumElement, entry)) {
			curriculumService.addRepositoryEntry(curriculumElement, entry, false);
			return Response.ok().build();
		}
		return Response.ok().status(Status.NOT_MODIFIED).build();
	}
	
	
	/**
	 * Remove a relation between a curriculum element and a repository entry.
	 * 
	 * @response.representation.200.doc The relation was successfully removed. 
	 * @response.representation.304.doc There is no relation to remove
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the repository entry was not found
	 * @param curriculumElementKey The curriculum element
	 * @param repositoryEntryKey The repository entry
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumElementKey}/entries/{repositoryEntryKey}")
	public Response removeRepositoryEntryToElement(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("repositoryEntryKey") Long repositoryEntryKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		RepositoryEntry entry = repositoryService.loadByKey(repositoryEntryKey);
		if(entry == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		if(curriculumService.hasRepositoryEntry(curriculumElement, entry)) {
			curriculumService.removeRepositoryEntry(curriculumElement, entry);
			return Response.ok().build();
		}
		return Response.ok().status(Status.NOT_MODIFIED).build();
	}
	
	/**
	 * Get the memberships informations of the specified curriculum element.
	 * 
	 * @response.representation.200.qname {http://www.example.com}curriculumElementMemberVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The curriculum element membership
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTMEMBERVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param curriculumElementKey The curriculum element primary key
	 * @param httpRequest The HTTP request
	 * @return The curriculum element
	 */
	@GET
	@Path("{curriculumElementKey}/members")
	public Response getMembers(@PathParam("curriculumElementKey") Long curriculumElementKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		SearchMemberParameters params = new SearchMemberParameters();
		List<CurriculumMember> members = curriculumService.getMembers(curriculumElement, params);
		List<CurriculumElementMemberVO> voList = new ArrayList<>(members.size());
		for(CurriculumMember member:members) {
			voList.add(CurriculumElementMemberVO.valueOf(member));
		}
		return Response.ok(voList.toArray(new CurriculumElementMemberVO[voList.size()])).build();
	}
	
	/**
	 * Get all members of the specified curriculum element. A query parameter can
	 * specify the role of them.
	 * 
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of authors
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The course not found
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{curriculumElementKey}/users")
	public Response getUsers(@PathParam("curriculumElementKey") Long curriculumElementKey, @QueryParam("role") String role) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		if(role != null && !CurriculumRoles.isValueOf(role)) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		
		List<Identity> members = curriculumService.getMembersIdentity(curriculumElement, CurriculumRoles.valueOf(role));
		List<UserVO> voList = new ArrayList<>(members.size());
		for(Identity member:members) {
			voList.add(UserVOFactory.get(member));
		}
		return Response.ok(voList.toArray(new UserVO[voList.size()])).build();
	}
	
	/**
	 * Add a membership to the specified curriculum element.
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumElementMemberVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element membership to persist
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTMEMBERVO}
	 * @response.representation.200.doc The membership was persisted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @response.representation.409.doc The role is not allowed
	 * @param curriculumElementKey The curriculum element primary key
	 * @param membership The membership informations
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/members")
	public Response putMembers(@PathParam("curriculumElementKey") Long curriculumElementKey,
			CurriculumElementMemberVO membership) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Identity identity = securityManager.loadIdentityByKey(membership.getIdentityKey());
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		String role = membership.getRole();
		if(!CurriculumRoles.isValueOf(role)) {
			return Response.serverError().status(Status.CONFLICT).build();
		}
		curriculumService.addMember(curriculumElement, identity, CurriculumRoles.valueOf(role));
		return Response.ok().build();
	}
	
	/**
	 * Remove all memberships of the identity from the specified curriculum element.
	 * 
	 * @response.representation.200.doc The membership was removed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumElementKey}/members/{identityKey}")
	public Response deleteMembers(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("identityKey") Long identityKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		curriculumService.removeMember(curriculumElement, identity);
		return Response.ok().build();
	}
	
	/**
	 * Get all participants of the specified curriculum element.
	 * 
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of participants
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element not found
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{curriculumElementKey}/participants")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getParticipants(@PathParam("curriculumElementKey") Long curriculumElementKey) {
		return getMembers(curriculumElementKey, CurriculumRoles.participant);
	}
	
	/**
	 * Get all coaches of the specified curriculum element.
	 * 
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of coaches
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element not found
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{curriculumElementKey}/coaches")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCoaches(@PathParam("curriculumElementKey") Long curriculumElementKey) {
		return getMembers(curriculumElementKey, CurriculumRoles.coach);
	}
	
	/**
	 * Get all owners of the specified curriculum element.
	 * 
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of coaches
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element not found
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{curriculumElementKey}/owners")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getOwners(@PathParam("curriculumElementKey") Long curriculumElementKey) {
		return getMembers(curriculumElementKey, CurriculumRoles.owner);
	}
	
	/**
	 * Get all master coaches of the specified curriculum element.
	 * 
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of coaches
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element not found
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{curriculumElementKey}/mastercoaches")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getMasterCoaches(@PathParam("curriculumElementKey") Long curriculumElementKey) {
		return getMembers(curriculumElementKey, CurriculumRoles.mastercoach);
	}
	
	/**
	 * Get all curriculum managers of the specified curriculum element.
	 * 
	 * @response.representation.200.qname {http://www.example.com}userVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The array of curriculum managers
	 * @response.representation.200.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element not found
	 * @param httpRequest The HTTP request
	 * @return It returns an array of <code>UserVO</code>
	 */
	@GET
	@Path("{curriculumElementKey}/curriculumelementowners")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCurriculumManagers(@PathParam("curriculumElementKey") Long curriculumElementKey) {
		return getMembers(curriculumElementKey, CurriculumRoles.curriculumelementowner);
	}
	
	private Response getMembers(Long curriculumElementKey, CurriculumRoles role) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		List<Identity> members = curriculumService.getMembersIdentity(curriculumElement, role);
		List<UserVO> voList = new ArrayList<>(members.size());
		for(Identity member:members) {
			voList.add(UserVOFactory.get(member));
		}
		return Response.ok(voList.toArray(new UserVO[voList.size()])).build();
	}
	
	/**
	 * Make the specified user a participant of the curriculum element.
	 * 
	 * @response.representation.200.doc The membership was added
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to make a participant of
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/participants/{identityKey}")
	public Response putParticipant(@PathParam("curriculumElementKey") Long curriculumElementKey, @PathParam("identityKey") Long identityKey) {
		return putMember(curriculumElementKey, identityKey, CurriculumRoles.participant);
	}
	
	/**
	 * Make the specified user a coach of the curriculum element.
	 * 
	 * @response.representation.200.doc The membership was added
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to make a coach of
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/coaches/{identityKey}")
	public Response putCoach(@PathParam("curriculumElementKey") Long curriculumElementKey, @PathParam("identityKey") Long identityKey) {
		return putMember(curriculumElementKey, identityKey, CurriculumRoles.coach);
	}
	
	/**
	 * Make the specified user a course owner of the curriculum element.
	 * 
	 * @response.representation.200.doc The membership was added
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to make a course owner of
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/owners/{identityKey}")
	public Response putOwner(@PathParam("curriculumElementKey") Long curriculumElementKey, @PathParam("identityKey") Long identityKey) {
		return putMember(curriculumElementKey, identityKey, CurriculumRoles.owner);
	}
	
	/**
	 * Make the specified user a curriculum manager of the curriculum element.
	 * 
	 * @response.representation.200.doc The membership was added
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to make a curriculum manager of
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/curriculumelementowners/{identityKey}")
	public Response putCurriculumElementOwner(@PathParam("curriculumElementKey") Long curriculumElementKey, @PathParam("identityKey") Long identityKey) {
		return putMember(curriculumElementKey, identityKey, CurriculumRoles.curriculumelementowner);
	}
	
	/**
	 * Make the specified user a master coach of the curriculum element.
	 * 
	 * @response.representation.200.doc The membership was added
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to make a curriculum manager of
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/mastercoaches/{identityKey}")
	public Response putMasterCoach(@PathParam("curriculumElementKey") Long curriculumElementKey, @PathParam("identityKey") Long identityKey) {
		return putMember(curriculumElementKey, identityKey, CurriculumRoles.mastercoach);
	}
	
	private Response putMember(Long curriculumElementKey, Long identityKey, CurriculumRoles role) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		curriculumService.addMember(curriculumElement, identity, role);
		return Response.ok().build();
	}
	
	/**
	 * Make the array of users participant of the specified curriculum element.
	 * 
	 * @response.representation.qname {http://www.example.com}userVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element membership to persist
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.200.doc The memberships was persisted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @response.representation.409.doc The role is not allowed
	 * @param curriculumElementKey The curriculum element primary key
	 * @param participants The future participants
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/participants")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putParticipants(@PathParam("curriculumElementKey") Long curriculumElementKey, UserVO[] participants) {
		return putMembers(curriculumElementKey, participants, CurriculumRoles.participant);
	}
	
	/**
	 * Make the array of users coach of the specified curriculum element.
	 * 
	 * @response.representation.qname {http://www.example.com}userVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element membership to persist
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.200.doc The memberships was persisted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @response.representation.409.doc The role is not allowed
	 * @param curriculumElementKey The curriculum element primary key
	 * @param participants The future coaches
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/coaches")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putCoaches(@PathParam("curriculumElementKey") Long curriculumElementKey, UserVO[] coaches) {
		return putMembers(curriculumElementKey, coaches, CurriculumRoles.coach);
	}
	
	/**
	 * Make the array of users course owner of the specified curriculum element.
	 * 
	 * @response.representation.qname {http://www.example.com}userVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element membership to persist
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.200.doc The memberships was persisted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @response.representation.409.doc The role is not allowed
	 * @param curriculumElementKey The curriculum element primary key
	 * @param owners The future course owners
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/owners")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putOwners(@PathParam("curriculumElementKey") Long curriculumElementKey, UserVO[] owners) {
		return putMembers(curriculumElementKey, owners, CurriculumRoles.owner);
	}
	
	/**
	 * Make the array of users course master coaches of the specified curriculum element.
	 * 
	 * @response.representation.qname {http://www.example.com}userVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element membership to persist
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.200.doc The memberships was persisted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @response.representation.409.doc The role is not allowed
	 * @param curriculumElementKey The curriculum element primary key
	 * @param masterCoaches The future master coaches
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/mastercoaches")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putMasterCoaches(@PathParam("curriculumElementKey") Long curriculumElementKey, UserVO[] masterCoaches) {
		return putMembers(curriculumElementKey, masterCoaches, CurriculumRoles.mastercoach);
	}
	
	/**
	 * Make the array of users curriculum managers of the specified curriculum element.
	 * 
	 * @response.representation.qname {http://www.example.com}userVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element membership to persist
	 * @response.representation.example {@link org.olat.user.restapi.Examples#SAMPLE_USERVOes}
	 * @response.representation.200.doc The memberships was persisted
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @response.representation.409.doc The role is not allowed
	 * @param curriculumElementKey The curriculum element primary key
	 * @param participants The future curriculum manages
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementKey}/curriculumelementowners")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putCurriculumManagers(@PathParam("curriculumElementKey") Long curriculumElementKey, UserVO[] coaches) {
		return putMembers(curriculumElementKey, coaches, CurriculumRoles.curriculumelementowner);
	}
	
	private Response putMembers(Long curriculumElementKey, UserVO[] members, CurriculumRoles role) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		for(UserVO member:members) {
			Identity identity = securityManager.loadIdentityByKey(member.getKey());
			if(identity != null) {
				curriculumService.addMember(curriculumElement, identity, role);
			}
		}
		return Response.ok().build();
	}
	
	/**
	 * Remove the participant membership of the identity from the specified curriculum element.
	 * 
	 * @response.representation.200.doc The membership was removed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumElementKey}/participants/{identityKey}")
	public Response deleteParticipant(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("identityKey") Long identityKey) {
		return deleteMember(curriculumElementKey, identityKey, CurriculumRoles.participant);
	}
	
	/**
	 * Remove the coach membership of the identity from the specified curriculum element.
	 * 
	 * @response.representation.200.doc The membership was removed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumElementKey}/coaches/{identityKey}")
	public Response deleteCoach(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("identityKey") Long identityKey) {
		return deleteMember(curriculumElementKey, identityKey, CurriculumRoles.coach);
	}
	
	/**
	 * Remove the owner membership of the identity from the specified curriculum element.
	 * 
	 * @response.representation.200.doc The membership was removed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumElementKey}/owners/{identityKey}")
	public Response deleteOwner(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("identityKey") Long identityKey) {
		return deleteMember(curriculumElementKey, identityKey, CurriculumRoles.owner);
	}
	
	/**
	 * Remove the master coach membership of the identity from the specified curriculum element.
	 * 
	 * @response.representation.200.doc The membership was removed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumElementKey}/mastercoaches/{identityKey}")
	public Response deleteMasterCoach(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("identityKey") Long identityKey) {
		return deleteMember(curriculumElementKey, identityKey, CurriculumRoles.mastercoach);
	}
	
	/**
	 * Remove the curriculum manager membership of the identity from the specified curriculum element.
	 * 
	 * @response.representation.200.doc The membership was removed
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element or the identity was not found
	 * @param curriculumElementKey The curriculum element primary key
	 * @param identityKey The member to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumElementKey}/curriculumelementowners/{identityKey}")
	public Response deleteCurriculumManager(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("identityKey") Long identityKey) {
		return deleteMember(curriculumElementKey, identityKey, CurriculumRoles.curriculumelementowner);
	}
	
	private Response deleteMember(Long curriculumElementKey, Long identityKey, CurriculumRoles role) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		Identity identity = securityManager.loadIdentityByKey(identityKey);
		if(identity == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		
		curriculumService.removeMember(curriculumElement, identity, role);
		return Response.ok().build();
	}
	
	@GET
	@Path("{curriculumElementKey}/taxonomy/levels")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getTaxonomyLevels(@PathParam("curriculumElementKey") Long curriculumElementKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		List<TaxonomyLevel> levels = curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(curriculumElement);
		TaxonomyLevelVO[] voes = new TaxonomyLevelVO[levels.size()];
		for(int i=levels.size(); i-->0; ) {
			voes[i] = TaxonomyLevelVO.valueOf(levels.get(i));
		}
		return Response.ok(voes).build();
	}
	
	@PUT
	@Path("{curriculumElementKey}/taxonomy/levels/{taxonomyLevelKey}")
	public Response getTaxonomyLevels(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("taxonomyLevelKey") Long taxonomyLevelKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		List<TaxonomyLevel> levels = curriculumElementToTaxonomyLevelDao.getTaxonomyLevels(curriculumElement);
		for(TaxonomyLevel level:levels) {
			if(level.getKey().equals(taxonomyLevelKey)) {
				return Response.ok().status(Status.NOT_MODIFIED).build();
			}
			
		}
		
		TaxonomyLevel level = taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(taxonomyLevelKey));
		if(level == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		curriculumElementToTaxonomyLevelDao.createRelation(curriculumElement, level);
		return Response.ok().build();
	}
	
	@DELETE
	@Path("{curriculumElementKey}/taxonomy/levels/{taxonomyLevelKey}")
	public Response deleteTaxonomyLevel(@PathParam("curriculumElementKey") Long curriculumElementKey,
			@PathParam("taxonomyLevelKey") Long taxonomyLevelKey) {
		CurriculumElement curriculumElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElementKey));
		if(curriculumElement == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		if(!curriculumElement.getCurriculum().getKey().equals(curriculum.getKey())) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		TaxonomyLevel level = taxonomyService.getTaxonomyLevel(new TaxonomyLevelRefImpl(taxonomyLevelKey));
		if(level == null) {
			return Response.ok(Status.NOT_FOUND).build();
		}
		curriculumElementToTaxonomyLevelDao.deleteRelation(curriculumElement, level);
		return Response.ok().build();
	}
}
