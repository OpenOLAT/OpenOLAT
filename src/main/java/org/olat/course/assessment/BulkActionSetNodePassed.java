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

package org.olat.course.assessment;

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
import org.olat.course.nodes.AssessableCourseNode;
import org.olat.course.run.scoring.ScoreEvaluation;
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
/**
 * 
 * Description:<br>
 * TODO: schneider Class Description for BulkActionSetNodePassed
 * 
 * <P>
 * Initial Date:  23.01.2006 <br>
 *
 * @author Alexander Schneider
 */
public class BulkActionSetNodePassed extends BulkAction {
	private Translator translator;
	private AssessableCourseNode courseNode;
	private List allowedIdKeys;
	private Identity coachIdentity;
	private boolean hasPassed;
	Float cut;
	private OLATResourceable ores;
	
	public BulkActionSetNodePassed(OLATResourceable ores, List allowedIdKeys,Identity coachIdentity, Translator translator){
		this.translator = translator;
		this.allowedIdKeys = allowedIdKeys;
		this.coachIdentity = coachIdentity;
		this.ores = ores;
	}
	
	public List doAction(List identitiesAndTheirsNodePassed) {
		if (this.ores == null || this.courseNode == null || this.coachIdentity == null){
			throw new AssertException("use constructor with course, assessable coursnode and coachidentity");
		}
		List feedbacks = new ArrayList(identitiesAndTheirsNodePassed.size());
		ICourse course = CourseFactory.loadCourse(ores);
		for (Iterator iter = identitiesAndTheirsNodePassed.iterator(); iter.hasNext();) {
			Object[] identityAndItsNodePassed = (Object[]) iter.next();
			if (identityAndItsNodePassed[0] != null){
				
				if (allowedIdKeys.contains(((Identity)identityAndItsNodePassed[0]).getKey())){
					IdentityEnvironment ienv = new IdentityEnvironment();
					ienv.setIdentity((Identity)identityAndItsNodePassed[0]);
					UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
					String nodePassed = (String) identityAndItsNodePassed[1];
					if((nodePassed != null && nodePassed.equals("y")) || (nodePassed != null && nodePassed.equals("n"))){
						if (hasPassed && cut == null){ // Configuration of manual assessment --> Display passed/not passed: yes, Type of display: Manual by tutor
								ScoreEvaluation seOld = courseNode.getUserScoreEvaluation(uce);
								Float score = seOld.getScore();
								Boolean passed = Boolean.TRUE; 
								if(nodePassed.equals("n"))passed = Boolean.FALSE;
								ScoreEvaluation seNew = new ScoreEvaluation(score, passed);
								
								// Update score,passed properties in db, and the user's efficiency statement
								boolean incrementAttempts = false;
								courseNode.updateUserScoreEvaluation(seNew, uce, coachIdentity, incrementAttempts);
								
								// Refresh score view
								uce.getScoreAccounting().scoreInfoChanged(this.courseNode, seNew);
								
								Object[] feedback = new Object[]{Boolean.TRUE, identityAndItsNodePassed[0],translator.translate("bulk.action.ok")};
								feedbacks.add(feedback);
						}else{ // Configuration of manual assessment --> Display passed/not passed: no
							Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsNodePassed[0],translator.translate("bulk.action.wrong.config.toSetPassed")};
							feedbacks.add(feedback);	
						}
					}else{ // nodePassed == null
						Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsNodePassed[0],translator.translate("bulk.action.no.value")};
						feedbacks.add(feedback);
					}
				}else{ // identity exists, but current user has no rights to assess identityAndItsScore[0]
					Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsNodePassed[0],translator.translate("bulk.action.not.allowed")};
					feedbacks.add(feedback);
				}
			}else{ // identity == null
				Object[] feedback = new Object[]{Boolean.FALSE,identityAndItsNodePassed[0],translator.translate("bulk.action.no.such.user")};
				feedbacks.add(feedback);
			}
		}
		return feedbacks;
	}

	public void setCourseNode(AssessableCourseNode courseNode) {
		this.courseNode = courseNode;
		this.hasPassed = courseNode.hasPassedConfigured();
		if (hasPassed) {
			this.cut = courseNode.getCutValueConfiguration();
		}
	}
}
