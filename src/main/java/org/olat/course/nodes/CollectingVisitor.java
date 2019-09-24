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
package org.olat.course.nodes;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.Visitor;

/**
 * 
 * Initial date: 23 Sep 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class CollectingVisitor implements Visitor {

	private final List<CourseNode> courseNodes = new ArrayList<>();
	private final Predicate<CourseNode> predicate;
	private final Function<INode, CourseNode> function;
	
	public static final CollectingVisitor testing(Predicate<CourseNode> predicate) {
		return new CollectingVisitor(predicate, null);
	}
	
	public static final CollectingVisitor applying(Function<INode, CourseNode> function) {
		return new CollectingVisitor(null, function);
	}
	
	private CollectingVisitor(Predicate<CourseNode> predicate, Function<INode, CourseNode> function) {
		this.predicate = predicate;
		this.function = function;
	}

	@Override
	public void visit(INode node) {
		if (predicate != null) {
			test(node);
		} else if (function != null) {
			apply(node);
		}
	}

	private void test(INode node) {
		if (node instanceof CourseNode) {
			CourseNode courseNode = (CourseNode) node;
			if (predicate.test(courseNode)) {
				courseNodes.add(courseNode);
			}
		}
	}

	private void apply(INode node) {
		CourseNode courseNode = function.apply(node);
		if (courseNode != null) {
			courseNodes.add(courseNode);
		}
	}

	public List<CourseNode> getCourseNodes() {
		return courseNodes;
	}

}
