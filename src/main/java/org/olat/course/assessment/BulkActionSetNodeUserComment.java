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
import org.olat.course.run.userview.UserCourseEnvironment;
import org.olat.course.run.userview.UserCourseEnvironmentImpl;
/**
 * 
 * Description:<br>
 * TODO: schneider Class Description for BulkActionSetNodeUserComment
 * 
 * <P>
 * Initial Date:  23.01.2006 <br>
 *
 * @author Alexander Schneider
 */
public class BulkActionSetNodeUserComment extends BulkAction {
	private Translator translator;
	private AssessableCourseNode courseNode;
	private List allowedIdKeys;
	private Identity coachIdentity;
	
	private boolean hasUserComment;
	Float min, max, cut;
	private OLATResourceable ores;
	
	public BulkActionSetNodeUserComment(OLATResourceable ores, List allowedIdKeys,Identity coachIdentity, Translator translator){
		this.ores = ores;
		this.translator = translator;
		this.allowedIdKeys = allowedIdKeys;
		this.coachIdentity = coachIdentity;
	}
	
	public List doAction(List identitiesAndTheirsUserComments) {
		if (this.ores == null || this.courseNode == null || this.coachIdentity == null){
			throw new AssertException("use constructor with course, assessable coursnode and coachidentity");
		}
		List feedbacks = new ArrayList(identitiesAndTheirsUserComments.size());
		ICourse course = CourseFactory.loadCourse(ores);
		for (Iterator iter = identitiesAndTheirsUserComments.iterator(); iter.hasNext();) {
			Object[] identityAndItsUserComment = (Object[]) iter.next();
			if (identityAndItsUserComment[0] != null){
				
				if (allowedIdKeys.contains(((Identity)identityAndItsUserComment[0]).getKey())){
					IdentityEnvironment ienv = new IdentityEnvironment();
					ienv.setIdentity((Identity)identityAndItsUserComment[0]);
					UserCourseEnvironment uce = new UserCourseEnvironmentImpl(ienv, course.getCourseEnvironment());
					String userComment = (String) identityAndItsUserComment[1];
					if(userComment != null && !userComment.equals("")){
						if (hasUserComment){
								// Update userComment in db
								courseNode.updateUserUserComment(userComment, uce, coachIdentity);
								//LD: why do we have to update the efficiency statement?
								//EfficiencyStatementManager esm =	EfficiencyStatementManager.getInstance();
								//esm.updateUserEfficiencyStatement(uce);
								
								Object[] feedback = new Object[]{Boolean.TRUE, identityAndItsUserComment[0],translator.translate("bulk.action.ok")};
								feedbacks.add(feedback);
						}else{ // Configuration of manual assessment --> Score granted: No
							Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsUserComment[0],translator.translate("bulk.action.wrong.config.toSetUserComment")};
							feedbacks.add(feedback);	
						}
					}else{ // userComment == null
						Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsUserComment[0],translator.translate("bulk.action.no.value")};
						feedbacks.add(feedback);
					}
				}else{ // identity exists, but current user has no rights to assess identityAndItsUserComment[0]
					Object[] feedback = new Object[]{Boolean.FALSE, identityAndItsUserComment[0],translator.translate("bulk.action.not.allowed")};
					feedbacks.add(feedback);
				}
			}else{ // identity == null
				Object[] feedback = new Object[]{Boolean.FALSE,identityAndItsUserComment[0],translator.translate("bulk.action.no.such.user")};
				feedbacks.add(feedback);
			}
		}
		return feedbacks;
	}

	public void setCourseNode(AssessableCourseNode courseNode) {
		this.hasUserComment = courseNode.hasCommentConfigured();
		this.courseNode = courseNode;
	}

	
}
