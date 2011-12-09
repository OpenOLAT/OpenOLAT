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

package org.olat.course.nodes.iq;

import org.olat.core.id.Identity;
import org.olat.course.assessment.AssessmentManager;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.ims.qti.process.AssessmentInstance;
import org.olat.modules.ModuleConfiguration;
import org.olat.modules.iq.IQSecurityCallback;

/**
 * Initial Date:  Mar 4, 2004
 * @author Mike Stock
 */
public class CourseIQSecurityCallback implements IQSecurityCallback {

	int attemptsConfig;
    AssessmentManager assessmentManager; 
    Identity identity;
    CourseNode courseNode;

    /**
     * Constructor for the QTI security callback used by the course iqtest building blocks (test)
     * @param iqtestCourseNode The current course node
     * @param assessmentManager The course property manager to get user results
     * @param identity The current identity
     */
	public CourseIQSecurityCallback(IQTESTCourseNode iqtestCourseNode, AssessmentManager assessmentManager, Identity identity) {
		super();
		ModuleConfiguration config = iqtestCourseNode.getModuleConfiguration();
		Integer attConf =  (Integer) config.get(IQEditController.CONFIG_KEY_ATTEMPTS);
		if (attConf == null) {
		    // number of attempts configuration is set to unlimited, use internal value of 10000
		    this.attemptsConfig = 10000;
		} else {
		    this.attemptsConfig = attConf.intValue(); 
		}
		this.assessmentManager = assessmentManager;
		this.identity = identity;
		this.courseNode = iqtestCourseNode;
	}

    /**
     * Constructor for the QTI security callback used by the course iqself building blocks (self-test)
     * @param iqselfCourseNode The current course node
     * @param assessmentManager The course property manager to get user results
     * @param identity The current identity
     */
	public CourseIQSecurityCallback(IQSELFCourseNode iqselfCourseNode, AssessmentManager assessmentManager, Identity identity) {
		super();
	    // se internal value of 10000  to symbolize unlimited number of attempts
	    this.attemptsConfig = 10000;		
		this.assessmentManager = assessmentManager;
		this.identity = identity;
		this.courseNode = iqselfCourseNode;
	}

    /**
     * Constructor for the QTI security callback used by the course iqsurv building blocks (questionnaire)
     * @param iqsurvCourseNode The current course node
     * @param assessmentManager The course property manager to get user results
     * @param identity The current identity
     */
	public CourseIQSecurityCallback(IQSURVCourseNode iqsurvCourseNode, AssessmentManager assessmentManager, Identity identity) {
		super();
	    // questionnaires can only be launched once
	    this.attemptsConfig = 1;		
		this.assessmentManager = assessmentManager;
		this.identity = identity;
		this.courseNode = iqsurvCourseNode;
	}

	/**
	 * @see org.olat.modules.iq.IQSecurityCallback#isAllowed(org.olat.ims.qti.process.AssessmentInstance)
	 */
	public boolean isAllowed(AssessmentInstance ai) {
		return true;
	}

	/**
	 * @see org.olat.modules.iq.IQSecurityCallback#attemptsLeft(org.olat.ims.qti.process.AssessmentInstance)
	 */
	public int attemptsLeft(AssessmentInstance ai) {
	    Integer userAttempts = assessmentManager.getNodeAttempts(courseNode, identity);
		return (attemptsConfig - userAttempts.intValue());
	}

	/**
	 * @see org.olat.modules.iq.IQSecurityCallback#isPreview()
	 */
	public boolean isPreview() {
		return false;
	}

}
