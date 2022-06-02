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
package org.olat.course;


import org.olat.core.gui.control.generic.iframe.DeliveryOptions;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.condition.Condition;
import org.olat.course.condition.ExtendedCondition;
import org.olat.course.condition.additionalconditions.PasswordCondition;
import org.olat.course.condition.additionalconditions.PasswordStore;
import org.olat.course.condition.operators.AbstractOperator;
import org.olat.course.condition.operators.AttributeEndswithOperator;
import org.olat.course.condition.operators.AttributeStartswithOperator;
import org.olat.course.condition.operators.EqualsOperator;
import org.olat.course.condition.operators.GreaterThanEqualsOperator;
import org.olat.course.condition.operators.GreaterThanOperator;
import org.olat.course.condition.operators.HasAttributeOperator;
import org.olat.course.condition.operators.HasNotAttributeOperator;
import org.olat.course.condition.operators.IsInAttributeOperator;
import org.olat.course.condition.operators.IsNotInAttributeOperator;
import org.olat.course.condition.operators.LowerThanEqualsOperator;
import org.olat.course.condition.operators.LowerThanOperator;
import org.olat.course.condition.operators.Operator;
import org.olat.course.config.CourseConfig;
import org.olat.course.learningpath.obligation.BusinessGroupExceptionalObligation;
import org.olat.course.learningpath.obligation.CurriculumElementExceptionalObligation;
import org.olat.course.learningpath.obligation.IdentityExceptionalObligation;
import org.olat.course.learningpath.obligation.OrganisationExceptionalObligation;
import org.olat.course.learningpath.obligation.PassedExceptionalObligation;
import org.olat.course.learningpath.obligation.UserPropertyExceptionalObligation;
import org.olat.course.noderight.NodeRight;
import org.olat.course.noderight.NodeRightGrant;
import org.olat.course.noderight.NodeRightGrant.NodeRightRole;
import org.olat.course.noderight.model.NodeRightGrantImpl;
import org.olat.course.noderight.model.NodeRightImpl;
import org.olat.course.nodes.AdobeConnectCourseNode;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.BasicLTICourseNode;
import org.olat.course.nodes.BlogCourseNode;
import org.olat.course.nodes.COCourseNode;
import org.olat.course.nodes.CPCourseNode;
import org.olat.course.nodes.CalCourseNode;
import org.olat.course.nodes.Card2BrainCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.FOCourseNode;
import org.olat.course.nodes.IQSELFCourseNode;
import org.olat.course.nodes.IQSURVCourseNode;
import org.olat.course.nodes.IQTESTCourseNode;
import org.olat.course.nodes.InfoCourseNode;
import org.olat.course.nodes.MSCourseNode;
import org.olat.course.nodes.MediaSiteCourseNode;
import org.olat.course.nodes.PodcastCourseNode;
import org.olat.course.nodes.PortfolioCourseNode;
import org.olat.course.nodes.ProjectBrokerCourseNode;
import org.olat.course.nodes.SPCourseNode;
import org.olat.course.nodes.STCourseNode;
import org.olat.course.nodes.ScormCourseNode;
import org.olat.course.nodes.TACourseNode;
import org.olat.course.nodes.TUCourseNode;
import org.olat.course.nodes.ViteroCourseNode;
import org.olat.course.nodes.WikiCourseNode;
import org.olat.course.nodes.adobeconnect.compatibility.AdobeConnectCompatibilityConfiguration;
import org.olat.course.nodes.adobeconnect.compatibility.MeetingCompatibilityDate;
import org.olat.course.nodes.adobeconnect.compatibility.WimbaClassroomCompatibilityConfiguration;
import org.olat.course.nodes.cl.model.Checkbox;
import org.olat.course.nodes.cl.model.CheckboxList;
import org.olat.course.nodes.gta.model.Solution;
import org.olat.course.nodes.gta.model.SolutionList;
import org.olat.course.nodes.gta.model.TaskDefinition;
import org.olat.course.nodes.gta.model.TaskDefinitionList;
import org.olat.course.nodes.practice.PracticeFilterRule;
import org.olat.course.style.model.ImageSourceImpl;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.modules.edubase.BookSection;
import org.olat.modules.edubase.model.BookSectionImpl;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.security.ExplicitTypePermission;

import de.bps.course.nodes.ll.LLModel;
import de.bps.olat.modules.cl.Checklist;
import de.bps.olat.modules.cl.Checkpoint;

/**
 * Helper class for course related aliases. The XStream object is
 * thread safe. There is 2 XStream objects, one to read, one to write
 * to accomodate the refactoring in 7.3 (Uni.).<br/>
 * We set as standard key the classname with the packages for backwards
 * compatibility. Export a course in OpenOLAT 8 and import it in OLAt 7.x
 * 
 * <P>
 * Initial Date:  11.04.2011 <br>
 * @author lavinia
 */
public class CourseXStreamAliases {
	
	private static final XStream readXstream = XStreamHelper.createXStreamInstance();
	private static final XStream writeXstream = XStreamHelper.createXStreamInstance();
	
	public static void courseSecurity(XStream xstream) {
		Class<?>[] types = new Class[] {
			// course structure
			Structure.class, CourseEditorTreeModel.class, CourseEditorTreeNode.class, ImageSourceImpl.class,
			// course node permissions
			NodeRight.class, NodeRightImpl.class, NodeRightGrant.class, NodeRightGrantImpl.class, NodeRightRole.class,
			// course node permissions
			ExtendedCondition.class, Operator.class,
			// learning path
			BusinessGroupExceptionalObligation.class, CurriculumElementExceptionalObligation.class,
			IdentityExceptionalObligation.class, OrganisationExceptionalObligation.class, PassedExceptionalObligation.class,
			UserPropertyExceptionalObligation.class,
			// course node password
			PasswordCondition.class, PasswordStore.class,
			// group task element
			TaskDefinitionList.class, TaskDefinition.class, SolutionList.class, Solution.class,
			// check lists element
			Checklist.class, Checkpoint.class, CheckboxList.class, Checkbox.class,
			// link list element
			LLModel.class,
			// single page element
			DeliveryOptions.class,
			// edubook elements
			BookSection.class, BookSectionImpl.class,
			// adobe connect element
			AdobeConnectCompatibilityConfiguration.class, WimbaClassroomCompatibilityConfiguration.class, MeetingCompatibilityDate.class,
			// practice
			PracticeFilterRule.class, PracticeFilterRule.Type.class, PracticeFilterRule.Operator.class
		};
		
		xstream.addPermission(new ExplicitTypePermission(types));
		xstream.allowTypeHierarchy(CourseNode.class);
		xstream.allowTypeHierarchy(AbstractOperator.class);
	}
	
	/**
	 * Used for reading editortreemodel.xml and runstructure.xml.
	 * Creates a new XStream with the aliases used in the mentioned xml files.
	 * @return
	 */
	static {
		courseSecurity(readXstream);
		courseSecurity(writeXstream);
		
		//write XStream
		writeXstream.alias("com.frentix.olat.course.nodes.ViteroCourseNode", ViteroCourseNode.class);
		writeXstream.alias("BookSection", BookSectionImpl.class);
		writeXstream.alias("ImageSource", ImageSourceImpl.class);
		writeXstream.alias("NodeRight", NodeRightImpl.class);
		writeXstream.alias("NodeRightGrant", NodeRightGrantImpl.class);
		//end write XStream
		
		////////////////////////////////////////////////////////////////////////////////////////////////
		//start read configuration (mostly for compatibility with OLAT 7.3 and greater)
		readXstream.alias("CourseConfig", CourseConfig.class);
		readXstream.alias("com.frentix.olat.course.nodes.ViteroCourseNode", ViteroCourseNode.class);
		readXstream.alias("org.olat.course.nodes.QTI21AssessmentCourseNode", IQTESTCourseNode.class);
		readXstream.alias("CourseEditorTreeModel", CourseEditorTreeModel.class);
		readXstream.alias("CourseEditorTreeNode", CourseEditorTreeNode.class);
		readXstream.alias("Structure", Structure.class);
		readXstream.alias("AssessableCourseNode", CourseNode.class);
		readXstream.alias("BasicLTICourseNode", BasicLTICourseNode.class);
		readXstream.alias("BCCourseNode", BCCourseNode.class);
		readXstream.alias("BlogCourseNode", BlogCourseNode.class);
		readXstream.alias("CalCourseNode", CalCourseNode.class);
		readXstream.alias("Card2BrainCourseNode", Card2BrainCourseNode.class);
		readXstream.alias("MediaSiteCourseNode", MediaSiteCourseNode.class);
		readXstream.alias("COCourseNode", COCourseNode.class);
		readXstream.alias("CourseNode", CourseNode.class);
		readXstream.alias("CPCourseNode", CPCourseNode.class);
		readXstream.alias("DialogCourseNode", DialogCourseNode.class);
		readXstream.alias("BookSection", BookSectionImpl.class);
		readXstream.alias("ENCourseNode", ENCourseNode.class);
		readXstream.alias("FOCourseNode", FOCourseNode.class);
		readXstream.alias("InfoCourseNode", InfoCourseNode.class);
		readXstream.alias("IQSELFCourseNode", IQSELFCourseNode.class);
		readXstream.alias("IQSURVCourseNode", IQSURVCourseNode.class);
		readXstream.alias("IQTESTCourseNode", IQTESTCourseNode.class);
		readXstream.alias("MSCourseNode", MSCourseNode.class);
		readXstream.alias("PodcastCourseNode", PodcastCourseNode.class);
		readXstream.alias("PortfolioCourseNode", PortfolioCourseNode.class);
		readXstream.alias("ProjectBrokerCourseNode", ProjectBrokerCourseNode.class);
		readXstream.alias("ScormCourseNode", ScormCourseNode.class);
		readXstream.alias("SPCourseNode", SPCourseNode.class);
		readXstream.alias("STCourseNode", STCourseNode.class);
		readXstream.alias("TACourseNode", TACourseNode.class);
		readXstream.alias("TUCourseNode", TUCourseNode.class);
		readXstream.alias("WikiCourseNode", WikiCourseNode.class);
		readXstream.alias("ExtendedCondition", ExtendedCondition.class);
		readXstream.alias("Condition", Condition.class);
		readXstream.alias("ImageSource", ImageSourceImpl.class);
		readXstream.alias("NodeRight", NodeRightImpl.class);
		readXstream.alias("NodeRightGrant", NodeRightGrantImpl.class);
		
		// vc node to new adobe connect cours element
		readXstream.alias("de.bps.course.nodes.VCCourseNode", AdobeConnectCourseNode.class);
		readXstream.alias("de.bps.course.nodes.vc.MeetingDate", MeetingCompatibilityDate.class);
		readXstream.alias("de.bps.course.nodes.vc.provider.adobe.AdobeConnectConfiguration", AdobeConnectCompatibilityConfiguration.class);
		readXstream.alias("de.bps.course.nodes.vc.provider.wimba.WimbaClassroomConfiguration", WimbaClassroomCompatibilityConfiguration.class);

		// conditions can hold operators and they get serialized as well. So we need all of the as aliases
		readXstream.alias("IsInAttributeOperator", IsInAttributeOperator.class);
		readXstream.alias("EqualsOperator", EqualsOperator.class);
		readXstream.alias("GreaterThanEqualsOperator", GreaterThanEqualsOperator.class);
		readXstream.alias("GreaterThanOperator", GreaterThanOperator.class);
		readXstream.alias("LowerThanEqualsOperator", LowerThanEqualsOperator.class);
		readXstream.alias("LowerThanOperator", LowerThanOperator.class);
		readXstream.alias("IsNotInAttributeOperator", IsNotInAttributeOperator.class);
		readXstream.alias("HasAttributeOperator", HasAttributeOperator.class);
		readXstream.alias("HasNotAttributeOperator", HasNotAttributeOperator.class);
		readXstream.alias("AttributeStartswithOperator", AttributeStartswithOperator.class);
		readXstream.alias("AttributeEndswithOperator", AttributeEndswithOperator.class);
		
		//deleted attributes
		readXstream.omitField(DeliveryOptions.class, "noJavascript");
		readXstream.omitField(org.hibernate.collection.internal.PersistentBag.class, "specjLazyLoad");
		readXstream.omitField(org.olat.course.nodes.BlogCourseNode.class, "config");
		readXstream.omitField(org.olat.course.nodes.PodcastCourseNode.class, "config");
	}
	
	/**
	 * Used for reading CourseConfig.xml.
	 * Creates a new XStream with the aliases used in the mentioned xml file.
	 * 
	 * @return
	 */
	public static XStream getReadCourseXStream() {
		return readXstream;
	}
	
	public static XStream getWriteCourseXStream() {
		return writeXstream;
	}
}
