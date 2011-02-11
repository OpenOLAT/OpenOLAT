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
* <p>
*/

package org.olat.course.archiver;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.core.id.IdentityEnvironment;
import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.util.bulk.BulkAction;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
/**
 * 
 * Description:<br>
 * TODO: schneider Class Description for BulkActionGetNodeScore
 * 
 * <P>
 * Initial Date:  19.12.2005 <br>
 *
 * @author Alexander Schneider
 */
public class BulkActionGetNodeScore extends BulkAction {
	private Translator translator;
	private CourseNode courseNode;
	private OLATResourceable ores;
	
	public BulkActionGetNodeScore(OLATResourceable ores, CourseNode courseNode, Translator translator){
		this.courseNode = courseNode;
		this.translator = translator;
		this.ores = ores;
		
	}
	
	@SuppressWarnings("unchecked")
	public List doAction(List identities) {
		if (this.ores == null || this.courseNode == null){
			throw new AssertException("use constructor with course and coursnode");
		}
		List nodeScores = new ArrayList(identities.size());
		
		for (Iterator iter = identities.iterator(); iter.hasNext();) {
			Identity identity = (Identity) iter.next();
			if (identity != null){
				IdentityEnvironment ienv = new IdentityEnvironment();
				ienv.setIdentity(identity);
				ICourse course = CourseFactory.loadCourse(ores);
				UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
				ScoreEvaluation scoreEvaluation = uce.getScoreAccounting().getScoreEvaluation(courseNode);
				
				if (scoreEvaluation == null){
						nodeScores.add(translator.translate("bulk.action.no.value"));
				}else{
					Float nodeScore = scoreEvaluation.getScore();
					if(nodeScore != null){
						nodeScores.add(nodeScore.toString());
					}else{
						nodeScores.add(translator.translate("bulk.action.no.value"));
					}
				}
			}else{
				nodeScores.add(translator.translate("bulk.action.no.such.user"));
			}
		}
		return nodeScores;
	}

}
