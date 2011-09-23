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
 * software distributed under the License is distributed on an "AS IS" BASIS,
 * <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Copyright (c) 2005-2006 by JGS goodsolutions GmbH, Switzerland<br>
 * http://www.goodsolutions.ch All rights reserved.
 * <p>
 */
package org.olat.modules.scorm;

import java.util.Properties;


/**
 * Description:<br>
 * is called on after a LMSCommit of an sco, to e.g. calculate sum of scores
 * <P>
 * Initial Date:  16.08.2006 <br>
 *
 * @author Felix Jost
 */
public interface ScormAPICallback {

	/**
	 * @param olatSahsId
	 * @param scoScores properties with key: scoId, value = rawscore 
	 */
	void lmsCommit(String olatSahsId, Properties scoScores);

}
