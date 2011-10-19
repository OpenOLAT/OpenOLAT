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
  
  static {
  	SAMPLE_MESSAGEVO.setKey(380l);
  	SAMPLE_MESSAGEVO.setAuthorKey(345l);
  	SAMPLE_MESSAGEVO.setTitle("A message");
  	SAMPLE_MESSAGEVO.setBody("The content of the message");
  	SAMPLE_MESSAGEVOes.setMessages(new MessageVO[]{SAMPLE_MESSAGEVO});
  	SAMPLE_MESSAGEVOes.setTotalCount(1);
  }
}
