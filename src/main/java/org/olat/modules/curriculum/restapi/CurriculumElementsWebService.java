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
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.CoreSpringFactory;
import org.olat.core.commons.persistence.DB;
import org.olat.modules.curriculum.Curriculum;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementManagedFlag;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementRefImpl;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;

/**
 * The security checks are done by the CurriculumsWebService.
 * 
 * Initial date: 15 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public class CurriculumElementsWebService {
	
	private final Curriculum curriculum;
	
	public CurriculumElementsWebService(Curriculum curriculum) {
		this.curriculum = curriculum;
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
		CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		List<CurriculumElement> elements = curriculumService.getCurriculumElements(curriculum);
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
		CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
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
	public Response putCurriculumElement(CurriculumElementVO curriculumElement) {
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
	public Response postCurriculumElement(CurriculumElementVO curriculumElement) {
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
	public Response postCurriculumElement(@PathParam("curriculumElementKey") Long curriculumElementKey, CurriculumElementVO curriculumElement) {
		if(curriculumElement.getKey() == null) {
			curriculumElement.setKey(curriculumElementKey);
		} else if(!curriculumElementKey.equals(curriculumElement.getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		CurriculumElement savedElement = saveCurriculumElement(curriculumElement);
		return Response.ok(CurriculumElementVO.valueOf(savedElement)).build();
	}
	
	
	private CurriculumElement saveCurriculumElement(CurriculumElementVO curriculumElement) {
		CurriculumService curriculumService = CoreSpringFactory.getImpl(CurriculumService.class);
		
		CurriculumElement elementToSave = null;
		CurriculumElementType type = null;
		if(curriculumElement.getCurriculumElementTypeKey() != null) {
			type = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(curriculumElement.getCurriculumElementTypeKey()));
		}
		CurriculumElement parentElement = null;
		if(curriculumElement.getParentElementKey() != null) {
			parentElement = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElement.getParentElementKey()));
			checkCurriculum(parentElement);
		}
		
		boolean move = false;
		if(curriculumElement.getKey() == null) {
			elementToSave = curriculumService.createCurriculumElement(curriculumElement.getIdentifier(), curriculumElement.getDisplayName(),
					curriculumElement.getBeginDate(), curriculumElement.getEndDate(), parentElement, type, curriculum);
		} else {
			elementToSave = curriculumService.getCurriculumElement(new CurriculumElementRefImpl(curriculumElement.getKey()));
			checkCurriculum(elementToSave);
			elementToSave.setDisplayName(curriculumElement.getDisplayName());
			elementToSave.setIdentifier(curriculumElement.getIdentifier());
			elementToSave.setBeginDate(curriculumElement.getBeginDate());
			elementToSave.setEndDate(curriculumElement.getEndDate());
			elementToSave.setType(type);
			if(parentElement != null && elementToSave.getParent() != null
					&& !elementToSave.getParent().getKey().equals(parentElement.getKey())) {
				move = true;
			}
		}
		
		elementToSave.setDescription(curriculumElement.getDescription());
		elementToSave.setExternalId(curriculumElement.getExternalId());
		elementToSave.setManagedFlags(CurriculumElementManagedFlag.toEnum(curriculumElement.getManagedFlagsString()));
		elementToSave.setStatus(curriculumElement.getStatus());
		
		CurriculumElement savedElement = curriculumService.updateCurriculumElement(elementToSave);
		if(move) {
			curriculumService.moveCurriculumElement(savedElement, parentElement);
			CoreSpringFactory.getImpl(DB.class).commit();
			savedElement = curriculumService.getCurriculumElement(savedElement);
		}
		return savedElement;
	}
	
	public void checkCurriculum(CurriculumElement element) {
		if(element.getCurriculum() != null && !element.getCurriculum().getKey().equals(curriculum.getKey())) {
			throw new WebApplicationException(Response.serverError().status(Status.CONFLICT).build());
		}
	}
}
