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
 * <P>
 * Initial Date:  19.12.2005 <br>
 *
 * @author Alexander Schneider
 */
public class BulkActionSetNodeScore extends BulkAction {
	private Translator translator;
	private AssessableCourseNode courseNode;
	private List allowedIdKeys;
	private Identity coachIdentity;
	
	private boolean hasScore, hasPassed;
	Float min, max, cut;
	private OLATResourceable ores;
	
	public BulkActionSetNodeScore(OLATResourceable ores, List allowedIdKeys,Identity coachIdentity, Translator translator){
		this.ores = ores;
		this.translator = translator;
		this.allowedIdKeys = allowedIdKeys;
		this.coachIdentity = coachIdentity;
	}
	
	public List doAction(List identitiesAndTheirsScores) {
		if (this.ores == null || this.courseNode == null || this.coachIdentity == null){
			throw new AssertException("use constructor with course, assessable coursnode and coachidentity");
		}
		List feedbacks = new ArrayList(identitiesAndTheirsScores.size());
		ICourse course = CourseFactory.loadCourse(ores);
		for (Iterator iter = identitiesAndTheirsScores.iterator(); iter.hasNext();) {
			Object[] identityAndItsScore = (Object[]) iter.next();
			if (identityAndItsScore[0] != null){
				
				if (allowedIdKeys.contains(((Identity)identityAndItsScore[0]).getKey())){
					IdentityEnvironment ienv = new IdentityEnvironment();
					ienv.setIdentity((Identity)identityAndItsScore[0]);
					UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
					String scoreAsString = (String) identityAndItsScore[1];
					if(scoreAsString != null){
						try {
							Float score = Float.valueOf(scoreAsString);
							//*** Score granted: Yes --> it must have a minimum and a maximum score value
							if (hasScore){
								// score < minimum score
								if ((min != null && score.floatValue() < min.floatValue()) 
										|| (score.floatValue() < AssessmentHelper.MIN_SCORE_SUPPORTED)) {
									Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsScore[0],
											translator.translate("bulk.action.lessThanMin",new String[]{String.valueOf(min)})};
									feedbacks.add(feedback);
								}
								// score > maximum score
								else if ((max != null && score.floatValue() > max.floatValue())
										|| (score.floatValue() > AssessmentHelper.MAX_SCORE_SUPPORTED)) {
									Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsScore[0],
											translator.translate("bulk.action.greaterThanMax",new String[]{String.valueOf(max)})};
									feedbacks.add(feedback);
								}
								// score between minimum and maximum score
								else {
									ScoreEvaluation se;
									// *** Display passed/not passed: yes
									// *** Type of display: automatic using cut value --> it must have a cut value
									if (hasPassed && cut != null){
										Boolean passed = (score.floatValue() >= cut.floatValue()) ? Boolean.TRUE	: Boolean.FALSE;
										se = new ScoreEvaluation(score, passed);
										
									// *** Display passed/not passed: yes
									// *** Type of display: Manual by tutor --> there is no cut value
									// or
									// *** Display passed/not passed: no --> there is no cut value
									}else{
										se = new ScoreEvaluation(score, null);
									}
									
									// Update score,passed properties in db, and the user's efficiency statement
									boolean incrementAttempts = false;
									courseNode.updateUserScoreEvaluation(se, uce, coachIdentity, incrementAttempts);
									
									// Refresh score view
									uce.getScoreAccounting().scoreInfoChanged(this.courseNode, se);
									
									Object[] feedback = new Object[]{Boolean.TRUE, identityAndItsScore[0],translator.translate("bulk.action.ok")};
									feedbacks.add(feedback);
								}
							}else{ // *** Score granted: No
								Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsScore[0],translator.translate("bulk.action.wrong.config.toSetScore")};
								feedbacks.add(feedback);	
							}
						} catch(NumberFormatException nfEx){
							Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsScore[0],translator.translate("bulk.action.wrong.float")};
							feedbacks.add(feedback);
						}
					}else{ // score == null
						Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsScore[0],translator.translate("bulk.action.no.value")};
						feedbacks.add(feedback);
					}
				}else{ // identity exists, but current user has no rights to assess identityAndItsScore[0]
					Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsScore[0],translator.translate("bulk.action.not.allowed")};
					feedbacks.add(feedback);
				}
			}else{ // identity == null
				Object[] feedback = new Object[]{Boolean.FALSE,identityAndItsScore[0],translator.translate("bulk.action.no.such.user")};
				feedbacks.add(feedback);
			}
		}
		return feedbacks;
	}

	public void setCourseNode(AssessableCourseNode courseNode) {
		this.courseNode = courseNode;
		this.hasScore = courseNode.hasScoreConfigured();
		this.hasPassed = courseNode.hasPassedConfigured();
		
		if (hasScore) {
			this.min = courseNode.getMinScoreConfiguration();
			this.max = courseNode.getMaxScoreConfiguration();
			if (hasPassed) {
				this.cut = courseNode.getCutValueConfiguration();
			}
		}
	}

	
}
