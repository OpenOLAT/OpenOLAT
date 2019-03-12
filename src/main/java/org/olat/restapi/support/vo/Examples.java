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

package org.olat.restapi.support.vo;

import java.util.Calendar;

import org.olat.repository.CatalogEntry;
import org.olat.repository.RepositoryEntryStatusEnum;
import org.olat.restapi.support.ObjectFactory;


public class Examples {
 
	public static final GroupVO SAMPLE_GROUPVO = new GroupVO();
	public static final GroupVOes SAMPLE_GROUPVOes = new GroupVOes();
	public static final GroupInfoVO SAMPLE_GROUPINFOVO = new GroupInfoVO();
	public static final GroupInfoVOes SAMPLE_GROUPINFOVOes = new GroupInfoVOes();
	
	public static final RepositoryEntryLifecycleVO SAMPLE_LIFECYCLE = new RepositoryEntryLifecycleVO();
	
	public static final ErrorVO SAMPLE_ERRORVO = new ErrorVO();
	public static final ErrorVOes SAMPLE_ERRORVOes = new ErrorVOes();
	
	public static final OlatResourceVO SAMPLE_OLATRESOURCEVO = new OlatResourceVO();
	
	public static final RepositoryEntryVO SAMPLE_REPOENTRYVO = new RepositoryEntryVO();
	public static final RepositoryEntryVOes SAMPLE_REPOENTRYVOes = new RepositoryEntryVOes();
	public static final RepositoryEntryAccessVO SAMPLE_REPOACCESS = new RepositoryEntryAccessVO();
	
	public static final AuthenticationVO SAMPLE_AUTHVO = new AuthenticationVO();
	public static final AuthenticationVOes SAMPLE_AUTHVOes = new AuthenticationVOes();
	
	public static final AssessableResultsVO SAMPLE_ASSESSABLERESULTSVO = new AssessableResultsVO();
	public static final AssessableResultsVOes SAMPLE_ASSESSABLERESULTSVOes = new AssessableResultsVOes();
	
	public static final KeyValuePair SAMPLE_KEYVALUEVO = new KeyValuePair();
	public static final KeyValuePairVOes SAMPLE_KEYVALUEVOes = new KeyValuePairVOes();
	
	public static final CourseVO SAMPLE_COURSEVO = new CourseVO();
	public static final CourseVOes SAMPLE_COURSEVOes = new CourseVOes();
	
	public static final CourseInfoVO SAMPLE_COURSEINFOVO = new CourseInfoVO();
	public static final CourseInfoVOes SAMPLE_COURSEINFOVOes = new CourseInfoVOes();
	
	public static final CourseNodeVO SAMPLE_COURSENODEVO = new CourseNodeVO();
	public static final CourseNodeVOes SAMPLE_COURSENODEVOes = new CourseNodeVOes();

	public static final CourseConfigVO SAMPLE_COURSECONFIGVO = new CourseConfigVO();
	
	public static final FileVO SAMPLE_FILE = new FileVO();
	
	public static final FileMetadataVO SAMPLE_FILE_METADATA = new FileMetadataVO();
	
	public static final FolderVO SAMPLE_FOLDERVO = new FolderVO();
	public static final FolderVOes SAMPLE_FOLDERVOes = new FolderVOes();
	
	public static final CatalogEntryVO SAMPLE_CATALOGENTRYVO = new CatalogEntryVO();
	public static final CatalogEntryVOes SAMPLE_CATALOGENTRYVOes = new CatalogEntryVOes();

  static {
  	SAMPLE_GROUPVO.setKey(123467l);
  	SAMPLE_GROUPVO.setName("My group");
  	SAMPLE_GROUPVO.setDescription("My group description");
  	SAMPLE_GROUPVO.setExternalId("External Identifier");
  	SAMPLE_GROUPVO.setManagedFlags("title,description");
  	SAMPLE_GROUPVO.setMinParticipants(0);
  	SAMPLE_GROUPVO.setMaxParticipants(0);
  	SAMPLE_GROUPVOes.setGroups(new GroupVO[]{SAMPLE_GROUPVO});
  	
  	SAMPLE_GROUPINFOVO.setKey(123467l);
  	SAMPLE_GROUPINFOVO.setName("My group");
  	SAMPLE_GROUPINFOVO.setDescription("My group description");
  	SAMPLE_GROUPINFOVO.setExternalId("External identifier");
  	SAMPLE_GROUPINFOVO.setMinParticipants(0);
  	SAMPLE_GROUPINFOVO.setMaxParticipants(0);
  	SAMPLE_GROUPINFOVO.setNews("<p>Hello world</p>");
  	SAMPLE_GROUPINFOVO.setForumKey(374589l);
  	SAMPLE_GROUPINFOVOes.setGroups(new GroupInfoVO[]{SAMPLE_GROUPINFOVO});

  	SAMPLE_ERRORVO.setCode("org.olat.restapi:error");
  	SAMPLE_ERRORVO.setTranslation("Hello world, there is an error");
  	SAMPLE_ERRORVOes.getErrors().add(SAMPLE_ERRORVO);
  	
  	SAMPLE_OLATRESOURCEVO.setKey(264278l);
  	SAMPLE_OLATRESOURCEVO.setResourceableId(66365742l);
  	SAMPLE_OLATRESOURCEVO.setResourceableTypeName("CourseModule");

  	SAMPLE_REPOENTRYVO.setKey(479286l);
  	SAMPLE_REPOENTRYVO.setSoftkey("internal_cp");
  	SAMPLE_REPOENTRYVO.setDisplayname("CP-demo");
  	SAMPLE_REPOENTRYVO.setResourcename("fdhasl");
  	SAMPLE_REPOENTRYVO.setResourceableId(4368567l);
  	SAMPLE_REPOENTRYVO.setResourceableTypeName("CourseModule");
  	SAMPLE_REPOENTRYVO.setExternalId("External identifier");
  	SAMPLE_REPOENTRYVO.setExternalRef("External reference");
  	SAMPLE_REPOENTRYVO.setManagedFlags("title.description");
  	SAMPLE_REPOENTRYVOes.setRepositoryEntries(new RepositoryEntryVO[]{SAMPLE_REPOENTRYVO});
  	SAMPLE_REPOENTRYVOes.setTotalCount(1);
  	
  	SAMPLE_REPOACCESS.setRepoEntryKey(479286l);
  	SAMPLE_REPOACCESS.setStatus(RepositoryEntryStatusEnum.published.name());
  	SAMPLE_REPOACCESS.setAllUsers(true);
  	SAMPLE_REPOACCESS.setGuests(false);
  	
  	Calendar cal = Calendar.getInstance();
  	SAMPLE_LIFECYCLE.setKey(2873423876l);
  	SAMPLE_LIFECYCLE.setLabel("Semester 13");
  	SAMPLE_LIFECYCLE.setSoftkey("st_13");
  	SAMPLE_LIFECYCLE.setValidFrom(ObjectFactory.formatDate(cal.getTime()));
  	cal.add(Calendar.DATE, 5);
  	SAMPLE_LIFECYCLE.setValidTo(ObjectFactory.formatDate(cal.getTime()));
  	
  	SAMPLE_AUTHVO.setKey(38759l);
  	SAMPLE_AUTHVO.setAuthUsername("john");
  	SAMPLE_AUTHVO.setProvider("OLAT");
  	SAMPLE_AUTHVO.setIdentityKey(345l);
  	SAMPLE_AUTHVOes.getAuthentications().add(SAMPLE_AUTHVO);
  	
  	SAMPLE_ASSESSABLERESULTSVO.setPassed(Boolean.TRUE);
  	SAMPLE_ASSESSABLERESULTSVO.setScore(34.0f);
  	SAMPLE_ASSESSABLERESULTSVO.setIdentityKey(345l);
  	SAMPLE_ASSESSABLERESULTSVOes.getResults().add(SAMPLE_ASSESSABLERESULTSVO);
  	
  	SAMPLE_KEYVALUEVO.setKey("Prefered color");
  	SAMPLE_KEYVALUEVO.setValue("Green");
  	SAMPLE_KEYVALUEVOes.getPairs().add(SAMPLE_KEYVALUEVO);
  	
  	SAMPLE_COURSEVO.setKey(777l);
  	SAMPLE_COURSEVO.setRepoEntryKey(27684l);
  	SAMPLE_COURSEVO.setSoftKey("internal_fx_cp");
  	SAMPLE_COURSEVO.setDisplayName("Demo course");
  	SAMPLE_COURSEVO.setTitle("Demo course");
  	SAMPLE_COURSEVO.setExternalId("External identifier");
  	SAMPLE_COURSEVO.setExternalRef("External reference");
  	SAMPLE_COURSEVO.setManagedFlags("title,description");
  	SAMPLE_COURSEVOes.setTotalCount(0);
  	SAMPLE_COURSEVOes.setCourses(new CourseVO[]{SAMPLE_COURSEVO});
  	
  	SAMPLE_COURSENODEVO.setId("id");
  	SAMPLE_COURSENODEVOes.getNodes().add(SAMPLE_COURSENODEVO);

  	SAMPLE_COURSEINFOVO.setKey(777l);
  	SAMPLE_COURSEINFOVO.setDisplayName("Demo course");
  	SAMPLE_COURSEINFOVO.setTitle("Demo course");
  	SAMPLE_COURSEINFOVO.setRepoEntryKey(456l);
  	SAMPLE_COURSEINFOVO.setSoftKey("oo_98237498");
  	SAMPLE_COURSEINFOVOes.setTotalCount(1);
  	SAMPLE_COURSEINFOVOes.setInfos(new CourseInfoVO[]{SAMPLE_COURSEINFOVO});

  	SAMPLE_COURSECONFIGVO.setSharedFolderSoftKey("head_1_olat_43985684395");
  	
  	SAMPLE_FILE.setHref("href");
  	SAMPLE_FILE.setRel("rel");
  	SAMPLE_FILE.setSize(8200l);
  	SAMPLE_FILE.setTitle("portrait.jpg");
  	
  	SAMPLE_FILE_METADATA.setFileName("portrait.jpg");
  	SAMPLE_FILE_METADATA.setHref("http://www.openolat.org/");
  	SAMPLE_FILE_METADATA.setLastModified(8945783984l);
  	SAMPLE_FILE_METADATA.setMimeType("image/jpg");
  	SAMPLE_FILE_METADATA.setSize(37638l);

  	SAMPLE_FOLDERVO.setCourseKey(375397l);
  	SAMPLE_FOLDERVO.setCourseNodeId("438950850389");
  	SAMPLE_FOLDERVO.setName("Course folder");
  	SAMPLE_FOLDERVO.setSubscribed(true);
  	
  	SAMPLE_FOLDERVOes.setFolders(new FolderVO[]{SAMPLE_FOLDERVO});
  	SAMPLE_FOLDERVOes.setTotalCount(1);
  	
  	SAMPLE_CATALOGENTRYVO.setKey(new Long(478l));
	SAMPLE_CATALOGENTRYVO.setName("Category");
	SAMPLE_CATALOGENTRYVO.setDescription("Description of the category");
	SAMPLE_CATALOGENTRYVO.setType(CatalogEntry.TYPE_NODE);
	
	SAMPLE_CATALOGENTRYVOes.setCatalogEntries(new CatalogEntryVO[]{SAMPLE_CATALOGENTRYVO});
	SAMPLE_CATALOGENTRYVOes.setTotalCount(0);
  }
}
