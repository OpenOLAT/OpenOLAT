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

package org.olat.modules.fo.restapi;

/**
 * 
 * Description:<br>
 * Examples of the VO Objects of forum for the WADL documentation
 * 
 * <P>
 * Initial Date:  22 apr. 2010 <br>
 * @author srosse, stephane.rosse@frentix.com
 */
public class Examples {

	public static final MessageVO SAMPLE_MESSAGEVO = new MessageVO();
	public static final MessageVOes SAMPLE_MESSAGEVOes = new MessageVOes();
	
	public static final ForumVO SAMPLE_FORUMVO = new ForumVO();
	
	public static final ForumVOes SAMPLE_FORUMVOes = new ForumVOes();
  
  static {
  	SAMPLE_MESSAGEVO.setKey(380l);
  	SAMPLE_MESSAGEVO.setAuthorKey(345l);
  	SAMPLE_MESSAGEVO.setTitle("A message");
  	SAMPLE_MESSAGEVO.setBody("The content of the message");
  	SAMPLE_MESSAGEVOes.setMessages(new MessageVO[]{SAMPLE_MESSAGEVO});
  	SAMPLE_MESSAGEVOes.setTotalCount(1);
  	
  	SAMPLE_FORUMVO.setForumKey(28294l);
  	SAMPLE_FORUMVO.setCourseKey(286l);
  	SAMPLE_FORUMVO.setCourseNodeId("2784628");
  	SAMPLE_FORUMVO.setDetailsName("It is a forum");
  	SAMPLE_FORUMVO.setForumKey(3865487l);
  	SAMPLE_FORUMVO.setName("My forum");
  	
  	SAMPLE_FORUMVOes.setTotalCount(1);
  	SAMPLE_FORUMVOes.setForums(new ForumVO[]{SAMPLE_FORUMVO});
  }
}
