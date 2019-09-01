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
package org.olat.course.nodes.st.learningpath;

import java.util.List;

import org.olat.course.learningpath.evaluation.DurationEvaluator;
import org.olat.course.learningpath.ui.LearningPathTreeNode;
import org.olat.course.nodes.CourseNode;

/**
 * 
 * Initial date: 1 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class STDurationEvaluator implements DurationEvaluator {

	@Override
	public boolean isDependingOnCurrentNode() {
		return false;
	}

	@Override
	public Integer getDuration(CourseNode courseNode) {
		return null;
	}

	@Override
	public boolean isdependingOnChildNodes() {
		return true;
	}

	@Override
	public Integer getDuration(List<LearningPathTreeNode> children) {
		boolean hasDurations = false;
		int sum = 0;
		for (LearningPathTreeNode child : children) {
			if (child.getDuration() != null) {
				sum += child.getDuration().intValue();
				hasDurations = true;
			}
		}
		return hasDurations? Integer.valueOf(sum): null;
	}

}
