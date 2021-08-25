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
package org.olat.course.learningpath;

import org.assertj.core.api.SoftAssertions;
import org.junit.Test;
import org.olat.course.learningpath.manager.LearningPathNodeAccessProvider;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.CourseNodeFactory;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.test.OlatTestCase;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 6 Feb 2020<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningPathServiceTest extends OlatTestCase {
	
	private static final NodeAccessType NODE_ACCESS_TYPE = NodeAccessType.of(LearningPathNodeAccessProvider.TYPE);
	
	@Autowired
	private LearningPathService sut;
	
	@Test
	public void shouldGetSequenceConfig() {
		CourseNode root = createCourseNode(STCourseNode.TYPE, null);
		root.getModuleConfiguration().setStringValue(STCourseNode.CONFIG_LP_SEQUENCE_KEY, STCourseNode.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		
		CourseNode sp_0_1 = createCourseNode(SPCourseNode.TYPE, root);
		root.addChild(sp_0_1);
		
		CourseNode st1 = createCourseNode(STCourseNode.TYPE, root);
		st1.getModuleConfiguration().setStringValue(STCourseNode.CONFIG_LP_SEQUENCE_KEY, STCourseNode.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL);
		root.addChild(st1);
		CourseNode sp_1_1 = createCourseNode(SPCourseNode.TYPE, st1);
		st1.addChild(sp_1_1);
		CourseNode sp_1_2 = createCourseNode(SPCourseNode.TYPE, st1);
		st1.addChild(sp_1_2);
		CourseNode sp_1_2_1 = createCourseNode(SPCourseNode.TYPE, sp_1_2);
		sp_1_2.addChild(sp_1_2_1);
		
		CourseNode st2 = createCourseNode(STCourseNode.TYPE, root);
		st2.getModuleConfiguration().setStringValue(STCourseNode.CONFIG_LP_SEQUENCE_KEY, STCourseNode.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		root.addChild(st2);
		CourseNode st2_1 = createCourseNode(STCourseNode.TYPE, st2);
		st2_1.getModuleConfiguration().setStringValue(STCourseNode.CONFIG_LP_SEQUENCE_KEY, STCourseNode.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		st2.addChild(st2_1);
		CourseNode st2_1_1 = createCourseNode(STCourseNode.TYPE, st2_1);
		st2_1_1.getModuleConfiguration().setStringValue(STCourseNode.CONFIG_LP_SEQUENCE_KEY, STCourseNode.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL);
		st2_1.addChild(st2_1_1);
		CourseNode sp2_1_1_1 = createCourseNode(SPCourseNode.TYPE, st2_1_1);
		st2_1_1.addChild(sp2_1_1_1);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getSequenceConfig(root).isInSequence()).as("root in").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(root).hasSequentialChildren()).as("root children").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(sp_0_1).isInSequence()).as("sp1 in").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(sp_0_1).hasSequentialChildren()).as("sp1 children").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(st1).isInSequence()).as("st1 in").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(st1).hasSequentialChildren()).as("st1 children").isEqualTo(true);
		softly.assertThat(sut.getSequenceConfig(sp_1_1).isInSequence()).as("sp_1_1 in").isEqualTo(true);
		softly.assertThat(sut.getSequenceConfig(sp_1_1).hasSequentialChildren()).as("sp_1_1 children").isEqualTo(true);
		softly.assertThat(sut.getSequenceConfig(sp_1_2).isInSequence()).as("sp_1_2 in").isEqualTo(true);
		softly.assertThat(sut.getSequenceConfig(sp_1_2).hasSequentialChildren()).as("sp_1_2 children").isEqualTo(true);
		softly.assertThat(sut.getSequenceConfig(sp_1_2_1).isInSequence()).as("sp_1_2_1 in").isEqualTo(true);
		softly.assertThat(sut.getSequenceConfig(sp_1_2_1).hasSequentialChildren()).as("sp_1_2_1 children").isEqualTo(true);
		softly.assertThat(sut.getSequenceConfig(st2).isInSequence()).as("st2 in").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(st2).hasSequentialChildren()).as("st2 children").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(st2_1).isInSequence()).as("st2_1 in").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(st2_1).hasSequentialChildren()).as("st2_1 children").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(st2_1_1).isInSequence()).as("st2_1_1 in").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(st2_1_1).hasSequentialChildren()).as("st2_1_1 children").isEqualTo(true);
		softly.assertThat(sut.getSequenceConfig(sp2_1_1_1).isInSequence()).as("sp2_1_1_1 in").isEqualTo(true);
		softly.assertThat(sut.getSequenceConfig(sp2_1_1_1).hasSequentialChildren()).as("sp2_1_1_1 children").isEqualTo(true);
		softly.assertAll();
	}
	
	@Test
	public void shouldGetRooSequenceConfig() {
		CourseNode rootWithout = createCourseNode(STCourseNode.TYPE, null);
		rootWithout.getModuleConfiguration().setStringValue(STCourseNode.CONFIG_LP_SEQUENCE_KEY, STCourseNode.CONFIG_LP_SEQUENCE_VALUE_WITHOUT);
		CourseNode rootSequential = createCourseNode(STCourseNode.TYPE, null);
		rootSequential.getModuleConfiguration().setStringValue(STCourseNode.CONFIG_LP_SEQUENCE_KEY, STCourseNode.CONFIG_LP_SEQUENCE_VALUE_SEQUENTIAL);
		
		SoftAssertions softly = new SoftAssertions();
		softly.assertThat(sut.getSequenceConfig(rootWithout).isInSequence()).as("root without in").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(rootWithout).hasSequentialChildren()).as("root without children").isEqualTo(false);
		softly.assertThat(sut.getSequenceConfig(rootSequential).isInSequence()).as("root sequential in").isEqualTo(true);
		softly.assertThat(sut.getSequenceConfig(rootSequential).hasSequentialChildren()).as("root sequential children").isEqualTo(true);
		softly.assertAll();
	}

	private CourseNode createCourseNode(String type, CourseNode parent) {
		CourseNode courseNode = CourseNodeFactory.getInstance().getCourseNodeConfiguration(type).getInstance();
		courseNode.updateModuleConfigDefaults(true, parent, NODE_ACCESS_TYPE);
		return courseNode;
	}

}
