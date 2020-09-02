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
package org.olat.ims.qti21.restapi;

import static org.olat.restapi.security.RestSecurityHelper.getRoles;

import java.io.File;
import java.net.URI;
import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.Response.Status;

import org.apache.logging.log4j.Logger;
import org.olat.core.id.Roles;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.fileresource.FileResourceManager;
import org.olat.ims.qti21.QTI21Service;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import uk.ac.ed.ph.jqtiplus.node.test.AssessmentTest;
import uk.ac.ed.ph.jqtiplus.node.test.ItemSessionControl;
import uk.ac.ed.ph.jqtiplus.node.test.TestPart;
import uk.ac.ed.ph.jqtiplus.resolution.ResolvedAssessmentTest;

/**
 * 
 * Initial date: 2 sept. 2020<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
@Component
@Path("repo/tests")
public class AssessmentTestWebService {
	
	private static final Logger log = Tracing.createLoggerFor(AssessmentTestWebService.class);

	@Autowired
	private QTI21Service qtiService;
	@Autowired
	private RepositoryManager repositoryManager;
	
	@PUT
	@Path("{repoEntryKey}/parts/maxattempts")
	public Response getRepositoryEntryResource(@PathParam("repoEntryKey")String repoEntryKey, @Context HttpServletRequest httpRequest)
	throws WebApplicationException {
		Roles roles = getRoles(httpRequest);
		if(roles == null || !roles.isAdministrator()) {
	    	throw new WebApplicationException(Status.FORBIDDEN);
		}
		RepositoryEntry re = lookupRepositoryEntry(repoEntryKey);
	    if(re == null) {
	    	throw new WebApplicationException(Status.NOT_FOUND);
	    }
	    
	    FileResourceManager frm = FileResourceManager.getInstance();
		File unzippedDirRoot = frm.unzipFileResource(re.getOlatResource());
		ResolvedAssessmentTest resolvedObject = qtiService.loadAndResolveAssessmentTest(unzippedDirRoot, false, true);
		AssessmentTest assessmentTest = resolvedObject.getRootNodeLookup().extractIfSuccessful();
		
		List<TestPart> parts = assessmentTest.getChildAbstractParts();
		for(TestPart part:parts) {
			ItemSessionControl itemSessionControl = part.getItemSessionControl();
			itemSessionControl.setMaxAttempts(Integer.valueOf(0));
		}
		
		URI testURI = resolvedObject.getTestLookup().getSystemId();
		File testFile = new File(testURI);
		qtiService.updateAssesmentObject(testFile, resolvedObject);
		return Response.ok().build();
	}
	
	private RepositoryEntry lookupRepositoryEntry(String key) {
		RepositoryEntry re = null;
		if (StringHelper.isLong(key)) {// looks like a primary key
			try {
				re = repositoryManager.lookupRepositoryEntry(Long.valueOf(key));
			} catch (NumberFormatException e) {
				log.warn("", e);
			}
		}
		return re;
	}

}
