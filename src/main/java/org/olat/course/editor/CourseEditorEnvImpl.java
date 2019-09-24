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

package org.olat.course.editor;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org._3pq.jgrapht.DirectedGraph;
import org._3pq.jgrapht.Edge;
import org._3pq.jgrapht.alg.CycleDetector;
import org._3pq.jgrapht.edge.EdgeFactories;
import org._3pq.jgrapht.edge.EdgeFactories.DirectedEdgeFactory;
import org._3pq.jgrapht.graph.DefaultDirectedGraph;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.interpreter.ConditionErrorMessage;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.bc.BCCourseNodeEditController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.group.area.BGArea;


public class CourseEditorEnvImpl implements CourseEditorEnv {
	/**
	 * the course editor tree model used in this editing session, exist only once
	 * per open course editor
	 */
	private CourseEditorTreeModel cetm;
	String currentCourseNodeId = null;
	/**
	 * the course group manager is used for answering the existXXX questions
	 * concering, groups and areas
	 */
	private CourseGroupManager cgm;
	/**
	 * the editor locale, it is used in the condition interpreter to provide
	 * localized error messages.
	 */
	private Locale editorLocale;
	/**
	 * book keeping of (coursNodeId,
	 * {conditionexpression,conditionexpression,...})
	 */
	private Map<String,List<ConditionExpression>> softRefs = new HashMap<>();
	/**
	 * book keeping of (courseNodeId, StatusDescription)
	 */
	private Map<String,List<StatusDescription>> statusDescs = new HashMap<>();
	/**
	 * current active condition expression, it is activated by a call to
	 * <code>validateConditionExpression(..)</code> the condition interpreter is
	 * then asked for validating the expression. This validation parses the
	 * expression into the atomic functions etc, which in turn access the
	 * <code>CourseEditorEnvImpl</code> to <code>pushError()</code> and
	 * <code>addSoftReference()</code>.
	 */
	private ConditionExpression currentConditionExpression = null;
	
	/**
	 * the condition interpreter for evaluating the condtion expressions.
	 */
	private ConditionInterpreter ci = null;

	public CourseEditorEnvImpl(CourseEditorTreeModel cetm, CourseGroupManager cgm, Locale editorLocale) {
		this.cetm = cetm;
		this.cgm = cgm;
		this.editorLocale = editorLocale;
	}

	/**
	 * @param ci
	 */
	public void setConditionInterpreter(ConditionInterpreter ci) {
		this.ci = ci;
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#isEnrollmentNode(java.lang.String)
	 */
	public boolean isEnrollmentNode(String nodeId) {
		CourseEditorTreeNode cen = cetm.getCourseEditorNodeById(nodeId);
		if (cen == null) return false;
		if (cen.isDeleted()) return false;
		// node exists and is not marked as deleted, check the associated course
		// node correct type
		return (cen.getCourseNode() instanceof ENCourseNode);
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#isAssessable(java.lang.String)
	 */
	public boolean isAssessable(String nodeId) {
		CourseEditorTreeNode cen = cetm.getCourseEditorNodeById(nodeId);
		if (cen == null) return false;
		if (cen.isDeleted()) return false;
		// node exists and is not marked as deleted, check the associated course
		// node for assessability.
		return AssessmentHelper.checkIfNodeIsAssessable(cen.getCourseNode());
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#existsNode(java.lang.String)
	 */
	public boolean existsNode(String nodeId) {
		CourseEditorTreeNode cen = cetm.getCourseEditorNodeById(nodeId);
		boolean retVal = cen != null && !cen.isDeleted();
		return retVal;
	}
	
	// <OLATCE-91>
	/**
	 * @see org.olat.course.editor.CourseEditorEnv#getNode(java.lang.String)
	 */
	@Override
	public CourseNode getNode(String nodeId) {
		CourseNode cen = cetm.getCourseNode(nodeId);
		return cen;
	}
	// </OLATCE-91>

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#existsGroup(java.lang.String)
	 */
	public boolean existsGroup(String groupNameOrKey) {
		return cgm.existGroup(groupNameOrKey);
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#existsArea(java.lang.String)
	 */
	public boolean existsArea(String areaNameOrKey) {
		return cgm.existArea(areaNameOrKey);
	}

	@Override
	public List<String> validateAreas(List<String> areanames) {
		List<BGArea> cnt = cgm.getAllAreas();
		List<String> invalidNames = new ArrayList<>();
		
		a_a:
		for(String areaname:areanames) {
			for (BGArea element : cnt) {
				if (element.getName().equals(areaname)) { 
					continue a_a;
				}
			}
			invalidNames.add(areaname);
			
		}
		return invalidNames;
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#getCurrentCourseNodeId()
	 */
	public String getCurrentCourseNodeId() {
		return currentCourseNodeId;
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#setCurrentCourseNodeId(java.lang.String)
	 */
	public void setCurrentCourseNodeId(String courseNodeId) {
		this.currentCourseNodeId = courseNodeId;
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#getEditorEnvLocale()
	 */
	public Locale getEditorEnvLocale() {
		return editorLocale;
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#validateConditionExpression(org.olat.course.condition.interpreter.ConditionExpression)
	 */
	public ConditionErrorMessage[] validateConditionExpression(ConditionExpression condExpr) {
		// first set the active condition expression, which will be accessed from
		// the conditions functions inserting soft references
		currentConditionExpression = condExpr;
		if(condExpr.getExptressionString()==null) {
			return null;
		}
		// evaluate expression
		ConditionErrorMessage[] cems = ci.syntaxTestExpression(condExpr);
		if (softRefs.containsKey(this.currentCourseNodeId)) {
			List<ConditionExpression> condExprs = softRefs.get(this.currentCourseNodeId);
			for (Iterator<ConditionExpression> iter = condExprs.iterator(); iter.hasNext();) {
				ConditionExpression element = iter.next();
				if (element.getId().equals(currentConditionExpression.getId())) {
					condExprs.remove(element);
					break;
				}
			}
			condExprs.add(currentConditionExpression);
		} else {
			List<ConditionExpression> condExprs = new ArrayList<>();
			condExprs.add(currentConditionExpression);
			softRefs.put(currentCourseNodeId, condExprs);
		}
		return cems;
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#addSoftReference(java.lang.String,
	 *      java.lang.String)
	 */
	@Override
	public void addSoftReference(String category, String softReference, boolean cycleDetector) {
		currentConditionExpression.addSoftReference(category, softReference, cycleDetector);

	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#pushError(java.lang.Exception)
	 */
	@Override
	public void pushError(Exception e) {
		currentConditionExpression.pushError(e);
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#validateCourse()
	 */
	@Override
	public void validateCourse() {
		/*
		 * collect all condition error messages and soft references collect all
		 * configuration errors.
		 */
		String currentNodeWas = currentCourseNodeId;
		// reset all
		softRefs = new HashMap<>();
		Visitor v = new CollectConditionExpressionsVisitor();
		(new TreeVisitor(v, cetm.getRootNode(), true)).visitAll();

		// refresh,create status descriptions of the course
		statusDescs = new HashMap<>();
		v = new CollectStatusDescriptionVisitor(this);
		(new TreeVisitor(v, cetm.getRootNode(), true)).visitAll();
		//
		currentCourseNodeId = currentNodeWas;
	}

	/**
	 * @see org.olat.course.editor.CourseEditorEnv#getCourseStatus()
	 */
	public StatusDescription[] getCourseStatus() {
		String[] a = statusDescs.keySet().toArray(new String[statusDescs.keySet().size()]);
		Arrays.sort(a);
		List<StatusDescription> all2gether = new ArrayList<>();
		for (int i = a.length - 1; i >= 0; i--) {
			all2gether.addAll(statusDescs.get(a[i]));
		}


		ICourse course = CourseFactory.loadCourse(cgm.getCourseEntry());
		if(course!= null){
			if(course.getCourseConfig().getSharedFolderSoftkey().equals("sf.notconfigured")){
				INode rootNode = course.getEditorTreeModel().getRootNode();
				List<StatusDescription> descriptions = new ArrayList<>();
				descriptions = checkFolderNodes(rootNode, course, descriptions);
				if(!descriptions.isEmpty()){
					all2gether.addAll(descriptions);
				}
			}
		}



		StatusDescription[] retVal = new StatusDescription[all2gether.size()];
		retVal = all2gether.toArray(retVal);
		return retVal;
	}

	private List<StatusDescription> checkFolderNodes(INode rootNode, ICourse course, List<StatusDescription> descriptions){
		List<StatusDescription> descriptionsI = descriptions;
		Visitor visitor = new Visitor() {
			@Override
			public void visit(INode node) {
				CourseEditorTreeNode courseNode = (CourseEditorTreeNode) course.getEditorTreeModel().getNodeById(node.getIdent());
				if(!courseNode.isDeleted() && courseNode.getCourseNode() instanceof BCCourseNode){
					BCCourseNode bcNode = (BCCourseNode) courseNode.getCourseNode();
					if (bcNode.isSharedFolder()) {
						String translPackage = Util.getPackageName(BCCourseNodeEditController.class);
						StatusDescription status = new StatusDescription(StatusDescription.ERROR, "warning.no.sharedfolder", "warning.no.sharedfolder", null, translPackage);
						status.setDescriptionForUnit(bcNode.getIdent());
						// set which pane is affected by error
						status.setActivateableViewIdentifier(BCCourseNodeEditController.PANE_TAB_FOLDER);
						descriptionsI.add(status);
					}
				}
			}
		};

		TreeVisitor v = new TreeVisitor(visitor, rootNode, false);
		v.visitAll();

		return descriptionsI;
	}

	@Override
	public String toString() {
		String retVal = "";
		Set<String> keys = softRefs.keySet();
		for (Iterator<String> iter = keys.iterator(); iter.hasNext();) {
			String nodId = iter.next();
			retVal += "nodeId:" + nodId + "\n";
			List<ConditionExpression> conditionExprs = softRefs.get(nodId);
			for (Iterator<ConditionExpression> iterator = conditionExprs.iterator(); iterator.hasNext();) {
				ConditionExpression ce = iterator.next();
				retVal += "\t" + ce.toString() + "\n";
			}
			retVal += "\n";
		}
		return retVal;
	}

	class CollectStatusDescriptionVisitor implements Visitor {
		private CourseEditorEnv cev;

		public CollectStatusDescriptionVisitor(CourseEditorEnv cev) {
			this.cev = cev;
		}

		/**
		 * @see org.olat.core.util.tree.Visitor#visit(org.olat.core.util.nodes.INode)
		 */
		public void visit(INode node) {
			/**
			 * collect only status descriptions of not deleted nodes
			 */
			CourseEditorTreeNode tmp = (CourseEditorTreeNode) node;
			if (!tmp.isDeleted()) {
				CourseNode cn = tmp.getCourseNode();
				String key = cn.getIdent();
				StatusDescription[] allSds = cn.isConfigValid(cev);
				if (allSds.length > 0) {
					for (int i = 0; i < allSds.length; i++) {
						StatusDescription sd = allSds[i];
						if (sd != StatusDescription.NOERROR) {
							if (!statusDescs.containsKey(key)) {
								statusDescs.put(key, new ArrayList<StatusDescription>());
							}
							List<StatusDescription> sds = statusDescs.get(key);
							sds.add(sd);
						}
					}
				}
			}
		}

	}

	class CollectConditionExpressionsVisitor implements Visitor {
		/**
		 * @see org.olat.core.util.tree.Visitor#visit(org.olat.core.util.nodes.INode)
		 */
		public void visit(INode node) {
			/**
			 * collect condition expressions only for not deleted nodes
			 */
			CourseEditorTreeNode tmp = (CourseEditorTreeNode) node;
			CourseNode cn = tmp.getCourseNode();
			String key = cn.getIdent();
			List<ConditionExpression> condExprs = cn.getConditionExpressions();
			if (condExprs.size() > 0 && !tmp.isDeleted()) {
				// evaluate each expression
				for (Iterator<ConditionExpression> iter = condExprs.iterator(); iter.hasNext();) {
					ConditionExpression ce = iter.next();
					currentCourseNodeId = key;
					currentConditionExpression = ce;
					ci.syntaxTestExpression(ce);
				}
				// add it to the cache.
				softRefs.put(key, condExprs);
			}
		}

	}

	private static class Convert2DGVisitor implements Visitor{
		private DirectedEdgeFactory def;
		private DirectedGraph dg;
		public Convert2DGVisitor(DirectedGraph dg) {
			this.dg = dg;
			def = new EdgeFactories.DirectedEdgeFactory();
		}
		
		@Override
		public void visit(INode node) {
			CourseEditorTreeNode tmp = (CourseEditorTreeNode) node;
			CourseNode cn = tmp.getCourseNode();
			String key = cn.getIdent();
			dg.addVertex(key);
			/*
			 * add edge from parent to child. This directed edge represents the visibility accessability inheritance direction.
			 */
			INode parent = tmp.getParent();
			if(parent!=null) {
				dg.addVertex(parent.getIdent());
				Edge toParent = def.createEdge( parent.getIdent(),key);
				dg.addEdge(toParent);
			}
		}
		
	}
	
	/**
	 * 
	 * @see org.olat.course.editor.CourseEditorEnv#listCycles()
	 */
	@Override
	public Set<String> listCycles() {
		/*
		 * convert nodeRefs datastructure to a directed graph 
		 */
		DirectedGraph dg = new DefaultDirectedGraph();
		DirectedEdgeFactory def = new EdgeFactories.DirectedEdgeFactory();
		/*
		 * add the course structure as directed graph, where 
		 */
		Visitor v = new Convert2DGVisitor(dg);
		(new TreeVisitor(v, cetm.getRootNode(), true)).visitAll();
		/*
		 * iterate over nodeRefs, add each not existing node id as vertex, for each
		 * key - child relation add an edge to the directed graph.
		 */
		
		Map<String,Set<String>> nodeSoftRefs = new HashMap<>();
		for (Iterator<String> iter = softRefs.keySet().iterator(); iter.hasNext();) {
			String nodeId = iter.next();
			List<ConditionExpression> conditionExprs = softRefs.get(nodeId);
			for (int i = 0; i < conditionExprs.size(); i++) {
				ConditionExpression ce = conditionExprs.get(i);
				Set<String> refs = ce.getSoftReferencesForCycleDetectorOf("courseNodeId");
				if (refs != null && refs.size() > 0) {
					if(nodeSoftRefs.containsKey(nodeId)) {
						nodeSoftRefs.get(nodeId).addAll(refs);
					} else {
						nodeSoftRefs.put(nodeId, refs);
					}
				}
			}
		}
	
		for(Iterator<String> keys = nodeSoftRefs.keySet().iterator(); keys.hasNext(); ) {
			//a node
			String key = keys.next();
			if(!dg.containsVertex(key)) {
				dg.addVertex(key);
			}
			//and its children
			Set<String> children = nodeSoftRefs.get(key);
			
			for(Iterator<String> childrenIt = children.iterator(); childrenIt.hasNext(); ){
				String child = childrenIt.next();
				if(!dg.containsVertex(child)) {
					dg.addVertex(child);
				}
				//add edge, precondition: vertex key - child are already added to the graph
				Edge de = def.createEdge(key, child);
				dg.addEdge(de);
			}
		}
		/*
		 * find the id's participating in the cycle, and return the intersection
		 * with set of id's which actually produce references.
		 */
		CycleDetector cd = new CycleDetector(dg);
		Set<String> cycleIds = cd.findCycles();
		cycleIds.retainAll(nodeSoftRefs.keySet());
		return cycleIds;
	}

	/**
	 * 
	 * @return CourseGroupManager for this course environment
	 */
	public CourseGroupManager getCourseGroupManager() {
		return cgm;
	}
	
	public String getRootNodeId() {
		return cetm.getRootNode().getIdent();
	}


}
