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
* Copyright (c) since 2004 at Multimedia- & E-Learning Services (MELS),<br>
* University of Zurich, Switzerland.
* <hr>
* <a href="http://www.openolat.org">
* OpenOLAT - Online Learning and Training</a><br>
* This file has been modified by the OpenOLAT community. Changes are licensed
* under the Apache 2.0 license as the original file.
*/
package org.olat.restapi.repository;

import static org.olat.restapi.security.RestSecurityHelper.getIdentity;
import static org.olat.restapi.security.RestSecurityHelper.getUserRequest;
import static org.olat.restapi.security.RestSecurityHelper.isAuthor;
import static org.olat.restapi.security.RestSecurityHelper.isAuthorEditor;

import java.io.File;
import java.io.InputStream;
import java.util.Date;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.FormParam;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.CacheControl;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.EntityTag;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.olat.core.commons.modules.bc.FolderConfig;
import org.olat.core.gui.UserRequest;
import org.olat.core.gui.media.MediaResource;
import org.olat.core.id.Identity;
import org.olat.core.logging.OLog;
import org.olat.core.logging.Tracing;
import org.olat.core.util.CodeHelper;
import org.olat.core.util.FileUtils;
import org.olat.core.util.StringHelper;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.LockResult;
import org.olat.fileresource.FileResourceManager;
import org.olat.fileresource.types.ImsCPFileResource;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.olat.repository.handlers.RepositoryHandler;
import org.olat.repository.handlers.RepositoryHandlerFactory;
import org.olat.resource.OLATResource;
import org.olat.resource.OLATResourceManager;
import org.olat.restapi.security.RestSecurityHelper;
import org.olat.restapi.support.ErrorWindowControl;
import org.olat.restapi.support.ObjectFactory;
import org.olat.restapi.support.vo.RepositoryEntryVO;

/**
 * Description:<br>
 * Repository entry resource
 *
 * <P>
 * Initial Date:  19.05.2009 <br>
 * @author patrickb, srosse, stephane.rosse@frentix.com
 */
public class RepositoryEntryResource {

  private static final OLog log = Tracing.createLoggerFor(RepositoryEntryResource.class);

  public static CacheControl cc = new CacheControl();

  static {
    cc.setMaxAge(-1);
  }

  /**
   * get a resource in the repository
   * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc Get the repository resource
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_REPOENTRYVO}
   * @response.representation.404.doc The repository entry not found
   * @param repoEntryKey The key or soft key of the repository entry
   * @param request The REST request
   * @return
   */
  @GET
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  public Response getById(@PathParam("repoEntryKey")String repoEntryKey, @Context Request request){
    RepositoryEntry re = lookupRepositoryEntry(repoEntryKey);
    if(re == null) {
      return Response.serverError().status(Status.NOT_FOUND).build();
    }

    Date lastModified = re.getLastModified();

    Response.ResponseBuilder response;
    if(lastModified == null) {
      EntityTag eTag = ObjectFactory.computeEtag(re);
      response = request.evaluatePreconditions(eTag);
      if(response == null) {
        RepositoryEntryVO vo = ObjectFactory.get(re);
        response = Response.ok(vo).tag(eTag).lastModified(lastModified);
      }
    } else {
      EntityTag eTag = ObjectFactory.computeEtag(re);
      response = request.evaluatePreconditions(lastModified, eTag);
      if(response == null) {
        RepositoryEntryVO vo = ObjectFactory.get(re);
        response = Response.ok(vo).tag(eTag).lastModified(lastModified);
      }
    }
    return response.build();
  }

  /**
   * Download the export zip file of a repository entry.
   * @response.representation.mediaType multipart/form-data
   * @response.representation.doc Download the resource file
   * @response.representation.200.mediaType application/zip
   * @response.representation.200.doc Download the repository entry as export zip file
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_REPOENTRYVO}
   * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @response.representation.404.doc The resource could not found
   * @response.representation.406.doc Download of this resource is not possible
   * @response.representation.409.doc The resource is locked
   * @param repoEntryKey
   * @param request The HTTP request
   * @return
   */
  @GET
  @Path("file")
  @Produces({"application/zip", MediaType.APPLICATION_OCTET_STREAM})
  public Response getRepoFileById(@PathParam("repoEntryKey")String repoEntryKey, @Context HttpServletRequest request) {
    RepositoryEntry re = lookupRepositoryEntry(repoEntryKey);
    if(re == null) return Response.serverError().status(Status.NOT_FOUND).build();

    RepositoryHandler typeToDownload = RepositoryHandlerFactory.getInstance().getRepositoryHandler(re);
    if(typeToDownload == null) return Response.serverError().status(Status.NOT_FOUND).build();
    
    OLATResource ores = OLATResourceManager.getInstance().findResourceable(re.getOlatResource());
    if(ores == null) return Response.serverError().status(Status.NOT_FOUND).build();

    Identity identity = getIdentity(request);
    boolean isAuthor = RestSecurityHelper.isAuthor(request);
    boolean isOwner = RepositoryManager.getInstance().isOwnerOfRepositoryEntry(identity, re);
    if(!(isAuthor | isOwner)) return Response.serverError().status(Status.UNAUTHORIZED).build();
    boolean canDownload = re.getCanDownload() && typeToDownload.supportsDownload(re);
    if(!canDownload) return Response.serverError().status(Status.NOT_ACCEPTABLE).build();

    boolean isAlreadyLocked = typeToDownload.isLocked(ores);
    LockResult lockResult = null;
    try {
      lockResult = typeToDownload.acquireLock(ores, identity);
      if(lockResult == null || (lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) {
        MediaResource mr = typeToDownload.getAsMediaResource(ores, false);
        if(mr != null) {
          RepositoryManager.getInstance().incrementDownloadCounter(re);
          return Response.ok(mr.getInputStream()).cacheControl(cc).build(); // success
        } else return Response.serverError().status(Status.NO_CONTENT).build();
      } else return Response.serverError().status(Status.CONFLICT).build();
    }
    finally {
      if((lockResult != null && lockResult.isSuccess() && !isAlreadyLocked)) typeToDownload.releaseLock(lockResult);
    }
  }

  /**
   * Replace a resource in the repository and update its display name. The implementation is
   * limited to CP.
   * @response.representation.mediaType multipart/form-data
   * @response.representation.doc Import the resource file
   * @response.representation.200.qname {http://www.example.com}repositoryEntryVO
   * @response.representation.200.mediaType application/xml, application/json
   * @response.representation.200.doc Replace the resource and return the updated repository entry
   * @response.representation.200.example {@link org.olat.restapi.support.vo.Examples#SAMPLE_REPOENTRYVO}
   * @response.representation.401.doc The roles of the authenticated user are not sufficient
   * @param repoEntryKey The key or soft key of the repository entry
   * @param filename The name of the file
   * @param file The file input stream
   * @param displayname The display name
   * @param request The HTTP request
   * @return
   */
  @POST
  @Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
  @Consumes({MediaType.MULTIPART_FORM_DATA})
  public Response replaceResource(@PathParam("repoEntryKey") String repoEntryKey,
      @FormParam("filename") String filename, @FormParam("file") InputStream file,
      @FormParam("displayname") String displayname, @FormParam("description") String description,
      @Context HttpServletRequest request) {
    if(!RestSecurityHelper.isAuthor(request)) {
      return Response.serverError().status(Status.UNAUTHORIZED).build();
    }

    File tmpFile = null;
    long length = 0;
    try {
      final RepositoryEntry re = lookupRepositoryEntry(repoEntryKey);
      if(re == null) {
        return Response.serverError().status(Status.NOT_FOUND).build();
      }

      Identity identity = RestSecurityHelper.getUserRequest(request).getIdentity();
      RepositoryEntry replacedRe;
      if(file == null) {
      	replacedRe = re;
      	if(StringHelper.containsNonWhitespace(displayname)) {
      		replacedRe.setDisplayname(displayname);
      	}
      	if(StringHelper.containsNonWhitespace(description)) {
      		replacedRe.setDescription(description);
      	}
      	RepositoryManager.getInstance().updateRepositoryEntry(replacedRe);
      } else {
	      String tmpName = StringHelper.containsNonWhitespace(filename) ? filename : "import.zip";
	      tmpFile = getTmpFile(tmpName);
	      FileUtils.save(file, tmpFile);
	      FileUtils.closeSafely(file);
	      length = tmpFile.length();
	
	      if(length == 0) {
	        return Response.serverError().status(Status.NO_CONTENT).build();
	      }
	      replacedRe = replaceFileResource(identity, re, tmpFile);
	      if(replacedRe == null) {
	        return Response.serverError().status(Status.NOT_FOUND).build();
	      } else if (StringHelper.containsNonWhitespace(displayname)) {
	        replacedRe.setDisplayname(displayname);
	        RepositoryManager.getInstance().updateRepositoryEntry(replacedRe);
	      }
      }
      RepositoryEntryVO vo = ObjectFactory.get(replacedRe);
      return Response.ok(vo).build();
    } catch (Exception e) {
      log.error("Error while importing a file",e);
    } finally {
      if(tmpFile != null && tmpFile.exists()) {
        tmpFile.delete();
      }
    }
    return Response.serverError().status(Status.INTERNAL_SERVER_ERROR).build();
  }

  private RepositoryEntry replaceFileResource(Identity identity, RepositoryEntry re, File fResource) {
    if(re == null) throw new NullPointerException("RepositoryEntry cannot be null");

    FileResourceManager frm = FileResourceManager.getInstance();
    File currentResource = frm.getFileResource(re.getOlatResource());
    if(currentResource == null || !currentResource.exists()) {
      log.debug("Current resource file doesn't exist");
      return null;
    }

    String typeName = re.getOlatResource().getResourceableTypeName();
    if(typeName.equals(ImsCPFileResource.TYPE_NAME)) {
      if(currentResource.delete()) {
        FileUtils.copyFileToFile(fResource, currentResource, false);

        String repositoryHome = FolderConfig.getCanonicalRepositoryHome();
        String relUnzipDir = frm.getUnzippedDirRel(re.getOlatResource());
        File unzipDir = new File(repositoryHome, relUnzipDir);
        if(unzipDir != null && unzipDir.exists()) {
          FileUtils.deleteDirsAndFiles(unzipDir, true, true);
        }
        frm.unzipFileResource(re.getOlatResource());
      }
      log.audit("Resource: " + re.getOlatResource() + " replaced by " + identity.getName());
      return re;
    }

    log.debug("Cannot replace a resource of the type: " + typeName);
    return null;
  }

  private File getTmpFile(String suffix) {
    suffix = (suffix == null ? "" : suffix);
    File tmpFile = new File(WebappHelper.getUserDataRoot()	+ "/tmp/", CodeHelper.getGlobalForeverUniqueID() + "_" + suffix);
    FileUtils.createEmptyFile(tmpFile);
    return tmpFile;
  }
  
  /**
	 * Delete a course by id
 * @response.representation.200.doc The metadatas of the created course
	 * @response.representation.401.doc The roles of the authenticated user are not sufficient
 * @response.representation.404.doc The course not found
	 * @param courseId The course resourceable's id
	 * @param request The HTTP request
	 * @return It returns the XML representation of the <code>Structure</code>
	 *         object representing the course.
	 */
	@DELETE
	@Produces({MediaType.APPLICATION_XML, MediaType.APPLICATION_JSON})
	public Response deleteCourse(@PathParam("repoEntryKey") String repoEntryKey, @Context HttpServletRequest request) {
		if(!isAuthor(request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		
		RepositoryEntry re = lookupRepositoryEntry(repoEntryKey);
		if(re == null) {
			return Response.serverError().status(Status.NOT_FOUND).build();
		} else if (!isAuthorEditor(re, request)) {
			return Response.serverError().status(Status.UNAUTHORIZED).build();
		}
		UserRequest ureq = getUserRequest(request);
		
		//fxdiff
		ErrorWindowControl error = new ErrorWindowControl();
		RepositoryManager rm = RepositoryManager.getInstance();
		rm.deleteRepositoryEntryWithAllData(ureq, error, re);
		
		return Response.ok().build();
	}

  private RepositoryEntry lookupRepositoryEntry(String key) {
    Long repoEntryKey = longId(key);
    RepositoryEntry re = null;
    if(repoEntryKey != null) {//looks like a primary key
      re = RepositoryManager.getInstance().lookupRepositoryEntry(repoEntryKey);
    }
    if(re == null) {// perhaps a soft key
      re = RepositoryManager.getInstance().lookupRepositoryEntryBySoftkey(key, false);
    }
    return re;
  }

  private Long longId(String key) {
    try {
      for(int i=key.length(); i-->0; ) {
        if(!Character.isDigit(key.charAt(i))) {
          return null;
        }
      }
      return new Long(key);
    } catch(NumberFormatException ex) {
      return null;
    }
  }
}
