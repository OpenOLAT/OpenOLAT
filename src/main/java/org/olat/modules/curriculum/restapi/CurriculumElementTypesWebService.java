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
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.util.StringHelper;
import org.olat.modules.curriculum.CurriculumCalendars;
import org.olat.modules.curriculum.CurriculumElementType;
import org.olat.modules.curriculum.CurriculumElementTypeManagedFlag;
import org.olat.modules.curriculum.CurriculumElementTypeToType;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.curriculum.model.CurriculumElementTypeRefImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.swagger.v3.oas.annotations.tags.Tag;

/**
 * The security check is done by the curriculums web service.
 * 
 * Initial date: 16 mai 2018<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Tag(name = "Curriculum")
@Component
@Path("curriculum/types")
public class CurriculumElementTypesWebService {
	
	@Autowired
	private CurriculumService curriculumService;
	
	/**
	 * Return the curriculum element types used in the whole OpenOLAT instance.
	 * 
	 * @response.representation.200.qname {http://www.example.com}curriculumElementTypeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc An array of curriculum element typess
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param httpRequest  The HTTP request
	 * @return An array of curriculum element types
	 */
	@GET
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getElementTypes() {
		List<CurriculumElementType> elementTypes = curriculumService.getCurriculumElementTypes();
		List<CurriculumElementTypeVO> voes = new ArrayList<>(elementTypes.size());
		for(CurriculumElementType elementType:elementTypes) {
			voes.add(CurriculumElementTypeVO.valueOf(elementType));
		}
		return Response.ok(voes.toArray(new CurriculumElementTypeVO[voes.size()])).build();
	}
	

	/**
	 * Creates and persists a new curriculum element type entity.
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumElementTypeVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element type to persist
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTTYPEVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The persisted curriculum element type
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param curriculumelementType The curriculum element type to persist
	 * @return The new persisted <code>curriculum element type</code>
	 */
	@PUT
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response putCurriculumElementType(CurriculumElementTypeVO curriculumelementType) {
		CurriculumElementType savedElementType = saveCurriculumElementType(curriculumelementType);
		return Response.ok(CurriculumElementTypeVO.valueOf(savedElementType)).build();
	}
	
	/**
	 * Updates a new curriculum element type entity.
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumElementTypeVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element type to update
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTTYPEVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged curriculum element type
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param curriculumElementType The curriciulum element type to merge
	 * @return The merged <code>curriculum element type</code>
	 */
	@POST
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCurriculumElementType(CurriculumElementTypeVO curriculumElementType) {
		CurriculumElementType savedElementType = saveCurriculumElementType(curriculumElementType);
		return Response.ok(CurriculumElementTypeVO.valueOf(savedElementType)).build();
	}
	
	/**
	 * Updates a new curriculum element type entity. The primary key is taken from
	 * the URL. The curriculum element type object can be "primary key free".
	 * 
	 * @response.representation.qname {http://www.example.com}curriculumElementTypeVO
	 * @response.representation.mediaType application/xml, application/json
	 * @response.representation.doc The curriculum element type to update
	 * @response.representation.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTTYPEVO}
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The merged type curriculum element
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.406.mediaType application/xml, application/json
	 * @param curriculumElementTypeKey The curriculum element type primary key
	 * @param curriculumElementType The curriculum element type to merge
	 * @return The merged <code>curriculum element type</code>
	 */
	@POST
	@Path("{curriculumElementTypeKey}")
	@Consumes({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response postCurriculumElementType(@PathParam("curriculumElementTypeKey") Long curriculumElementTypeKey,
			CurriculumElementTypeVO curriculumElementType) {
		if(curriculumElementType.getKey() == null) {
			curriculumElementType.setKey(curriculumElementTypeKey);
		} else if(!curriculumElementTypeKey.equals(curriculumElementType.getKey())) {
			return Response.serverError().status(Status.CONFLICT).build();
		}

		CurriculumElementType savedElementType = saveCurriculumElementType(curriculumElementType);
		return Response.ok(CurriculumElementTypeVO.valueOf(savedElementType)).build();
	}
	
	private CurriculumElementType saveCurriculumElementType(CurriculumElementTypeVO elementTypeVo) {
		CurriculumElementType elementType;
		if(elementTypeVo.getKey() == null) {
			elementType = curriculumService.createCurriculumElementType(elementTypeVo.getIdentifier(),
					elementTypeVo.getDisplayName(), elementTypeVo.getDescription(), elementTypeVo.getExternalId());
		} else {
			elementType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(elementTypeVo.getKey()));
			elementType.setDisplayName(elementTypeVo.getDisplayName());
			elementType.setIdentifier(elementTypeVo.getIdentifier());
			elementType.setDescription(elementTypeVo.getDescription());
			elementType.setExternalId(elementTypeVo.getExternalId());
		}
		elementType.setCssClass(elementTypeVo.getCssClass());
		if(StringHelper.containsNonWhitespace(elementTypeVo.getCalendars())) {
			elementType.setCalendars(CurriculumCalendars.valueOf(elementTypeVo.getCalendars()));
		} else {
			elementType.setCalendars(CurriculumCalendars.disabled);
		}
		elementType.setManagedFlags(CurriculumElementTypeManagedFlag.toEnum(elementTypeVo.getManagedFlagsString()));
		return curriculumService.updateCurriculumElementType(elementType);
	}
	
	/**
	 * Get a specific curriculum element type.
	 * 
	 * @response.representation.200.qname {http://www.example.com}curriculumElementTypeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The curriculum element type
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @param curriculumElementTypeKey The curriculum element type primary key
	 * @return The curriculum element type
	 */
	@GET
	@Path("{curriculumElementTypeKey}")
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response getCurriculumElementTypes(@PathParam("curriculumElementTypeKey") Long curriculumElementTypeKey) {
		CurriculumElementType elementType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(curriculumElementTypeKey));
		if(elementType == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		return Response.ok(CurriculumElementTypeVO.valueOf(elementType)).build();
	}	

	/**
	 * Get the allowed sub-types of a specified curriculum element type.
	 * 
	 * @response.representation.200.qname {http://www.example.com}curriculumElementTypeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc An array of curriculum element types
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element type was not found
	 * @param curriculumElementTypeKey The curriculum element type primary key
	 * @return An array of curriculum element types
	 */
	@GET
	@Path("{curriculumElementTypeKey}/allowedSubTypes")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response getAllowedSubTypes(@PathParam("curriculumElementTypeKey") Long curriculumElementTypeKey) {
		CurriculumElementType type = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(curriculumElementTypeKey));
		if(type == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		Set<CurriculumElementTypeToType> typeToTypes = type.getAllowedSubTypes();
		List<CurriculumElementTypeVO> subTypeVOes = new ArrayList<>(typeToTypes.size());
		for(CurriculumElementTypeToType typeToType:typeToTypes) {
			CurriculumElementType subType = typeToType.getAllowedSubType();
			subTypeVOes.add(CurriculumElementTypeVO.valueOf(subType));
		}
		return Response.ok(subTypeVOes.toArray(new CurriculumElementTypeVO[subTypeVOes.size()])).build();
	}
	
	/**
	 * Add a sub-type to a specified curriculum element type.
	 * 
	 * @response.representation.200.qname {http://www.example.com}curriculumElementTypeVO
	 * @response.representation.200.mediaType application/xml, application/json
	 * @response.representation.200.doc The sub type was added to the allowed sub types
	 * @response.representation.200.example {@link org.olat.modules.curriculum.restapi.Examples#SAMPLE_CURRICULUMELEMENTTYPEVO}
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element type was not found
	 * @param curriculumElementTypeKey The type
	 * @param subTypeKey The sub type
	 * @return Nothing
	 */
	@PUT
	@Path("{curriculumElementTypeKey}/allowedSubTypes/{subTypeKey}")
	@Produces({MediaType.APPLICATION_JSON, MediaType.APPLICATION_XML})
	public Response allowSubTaxonomyLevelType(@PathParam("curriculumElementTypeKey") Long curriculumElementTypeKey, @PathParam("subTypeKey") Long subTypeKey) {
		CurriculumElementType type = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(curriculumElementTypeKey));
		CurriculumElementType subType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(subTypeKey));
		if(type == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}

		curriculumService.allowCurriculumElementSubType(type, subType);
		return Response.ok().build();
	}
	
	/**
	 * Remove a sub-type to a specified curriculum element type.
	 * 
	 * @response.representation.200.doc The sub type was removed successfully
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
	 * @response.representation.404.doc The curriculum element type was not found
	 * @param curriculumElementTypeKey The type
	 * @param subTypeKey The sub type to remove
	 * @return Nothing
	 */
	@DELETE
	@Path("{curriculumElementTypeKey}/allowedSubTypes/{subTypeKey}")
	public Response disalloweSubType(@PathParam("curriculumElementTypeKey") Long curriculumElementTypeKey, @PathParam("subTypeKey") Long subTypeKey) {
		CurriculumElementType type = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(curriculumElementTypeKey));
		CurriculumElementType subType = curriculumService.getCurriculumElementType(new CurriculumElementTypeRefImpl(subTypeKey));
		if(type == null || subType == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		}
		curriculumService.disallowCurriculumElementSubType(type, subType);
		return Response.ok().build();
	}

}
