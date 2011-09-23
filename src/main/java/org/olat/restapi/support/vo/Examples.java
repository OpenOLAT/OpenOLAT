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

package org.olat.restapi.support.vo;

public class Examples {
 
	public static final GroupVO SAMPLE_GROUPVO = new GroupVO();
	public static final GroupVOes SAMPLE_GROUPVOes = new GroupVOes();
	public static final GroupInfoVO SAMPLE_GROUPINFOVO = new GroupInfoVO();
	
	public static final ErrorVO SAMPLE_ERRORVO = new ErrorVO();
	public static final ErrorVOes SAMPLE_ERRORVOes = new ErrorVOes();
	
	public static final RepositoryEntryVO SAMPLE_REPOENTRYVO = new RepositoryEntryVO();
	public static final RepositoryEntryVOes SAMPLE_REPOENTRYVOes = new RepositoryEntryVOes();
	
	public static final AuthenticationVO SAMPLE_AUTHVO = new AuthenticationVO();
	public static final AuthenticationVOes SAMPLE_AUTHVOes = new AuthenticationVOes();
	
	public static final AssessableResultsVO SAMPLE_ASSESSABLERESULTSVO = new AssessableResultsVO();
	public static final AssessableResultsVOes SAMPLE_ASSESSABLERESULTSVOes = new AssessableResultsVOes();
	
	public static final KeyValuePair SAMPLE_KEYVALUEVO = new KeyValuePair();
	public static final KeyValuePairVOes SAMPLE_KEYVALUEVOes = new KeyValuePairVOes();
	
	public static final CourseVO SAMPLE_COURSEVO = new CourseVO();
	public static final CourseVOes SAMPLE_COURSEVOes = new CourseVOes();
	
	public static final CourseNodeVO SAMPLE_COURSENODEVO = new CourseNodeVO();
	public static final CourseNodeVOes SAMPLE_COURSENODEVOes = new CourseNodeVOes();

	public static final CourseConfigVO SAMPLE_COURSECONFIGVO = new CourseConfigVO();
  
  static {
  	SAMPLE_GROUPVO.setKey(123467l);
  	SAMPLE_GROUPVO.setName("My group");
  	SAMPLE_GROUPVO.setDescription("My group description");
  	SAMPLE_GROUPVO.setMinParticipants(0);
  	SAMPLE_GROUPVO.setMaxParticipants(0);
  	SAMPLE_GROUPVOes.getGroups().add(SAMPLE_GROUPVO);
  	
  	SAMPLE_GROUPINFOVO.setNews("<p>Hello world</p>");
  	SAMPLE_GROUPINFOVO.setForumKey(374589l);
  	
  	SAMPLE_ERRORVO.setCode("org.olat.restapi:error");
  	SAMPLE_ERRORVO.setTranslation("Hello world, there is an error");
  	SAMPLE_ERRORVOes.getErrors().add(SAMPLE_ERRORVO);

  	SAMPLE_REPOENTRYVO.setKey(479286l);
  	SAMPLE_REPOENTRYVO.setSoftkey("internal_cp");
  	SAMPLE_REPOENTRYVO.setDisplayname("CP-demo");
  	SAMPLE_REPOENTRYVO.setResourcename("fdhasl");
  	SAMPLE_REPOENTRYVO.setResourceableId(4368567l);
  	SAMPLE_REPOENTRYVO.setResourceableTypeName("CourseModule");
  	SAMPLE_REPOENTRYVOes.getEntries().add(SAMPLE_REPOENTRYVO);
  	
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
  	SAMPLE_COURSEVO.setTitle("Demo course");
  	SAMPLE_COURSEVOes.getCourses().add(SAMPLE_COURSEVO);
  	
  	SAMPLE_COURSENODEVO.setId("id");
  	SAMPLE_COURSENODEVOes.getNodes().add(SAMPLE_COURSENODEVO);
  	
  	SAMPLE_COURSECONFIGVO.setSharedFolderSoftKey("head_1_olat_43985684395");
  }
}
