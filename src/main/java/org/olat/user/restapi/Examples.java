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

package org.olat.user.restapi;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  26 oct. 2011 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class Examples {
	
	public static final UserVO SAMPLE_USERVO = new UserVO();
	public static final UserVOes SAMPLE_USERVOes = new UserVOes();
  
  static {
  	SAMPLE_USERVO.setKey(345l);
  	SAMPLE_USERVO.setFirstName("John");
  	SAMPLE_USERVO.setLastName("Smith");
  	SAMPLE_USERVO.setLogin("john");
  	SAMPLE_USERVO.setEmail("john.smith@frentix.com");
  	SAMPLE_USERVO.setPassword("");
  	SAMPLE_USERVO.putProperty("telPrivate", "238456782");
  	SAMPLE_USERVO.putProperty("telMobile", "238456782");
  	SAMPLE_USERVOes.setUsers(new UserVO[]{SAMPLE_USERVO});
  }
}
