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

package org.olat.commons.info.restapi;


public class Examples {
	
	public static final InfoMessageVO SAMPLE_INFOMESSAGEVO = new InfoMessageVO();
	public static final InfoMessageVOes SAMPLE_INFOMESSAGEVOes = new InfoMessageVOes();
  
  static {
  	SAMPLE_INFOMESSAGEVO.setKey(345l);
  	SAMPLE_INFOMESSAGEVO.setTitle("John");
  	SAMPLE_INFOMESSAGEVO.setMessage("Smith");
  	SAMPLE_INFOMESSAGEVO.setResName("CourseModule");
  	SAMPLE_INFOMESSAGEVO.setResId(81929756230689l);
  	SAMPLE_INFOMESSAGEVO.setResSubPath("81939515470663");
  	SAMPLE_INFOMESSAGEVO.setBusinessPath("[RepositoryEntry:109051904][CourseNode:81939515470663]");
  	SAMPLE_INFOMESSAGEVO.setAuthorKey(35789l);
  	SAMPLE_INFOMESSAGEVOes.getInfoMessages().add(SAMPLE_INFOMESSAGEVO);
  }
}
