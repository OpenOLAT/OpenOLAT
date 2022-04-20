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
import org.apache.logging.log4j.Logger;
import org.olat.core.CoreSpringFactory;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.Tracing;
import org.olat.core.util.Util;
import org.olat.core.util.nodes.INode;
import org.olat.core.util.tree.TreeVisitor;
import org.olat.core.util.tree.Visitor;
import org.olat.course.CourseEntryRef;
import org.olat.course.CourseFactory;
import org.olat.course.ICourse;
import org.olat.course.assessment.AssessmentHelper;
import org.olat.course.condition.interpreter.ConditionErrorMessage;
import org.olat.course.condition.interpreter.ConditionExpression;
import org.olat.course.condition.interpreter.ConditionInterpreter;
import org.olat.course.groupsandrights.CourseGroupManager;
import org.olat.course.nodeaccess.NodeAccessService;
import org.olat.course.nodeaccess.NodeAccessType;
import org.olat.course.nodes.BCCourseNode;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.ENCourseNode;
import org.olat.course.nodes.bc.BCCourseNodeEditController;
import org.olat.course.tree.CourseEditorTreeModel;
import org.olat.course.tree.CourseEditorTreeNode;
import org.olat.group.area.BGArea;


public class CourseEditorEnvImpl implements CourseEditorEnv {
	
	private static final Logger log = Tracing.createLoggerFor(CourseEditorEnvImpl.class);
	
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
	
	private final boolean conditionExporessionSupported;

	public CourseEditorEnvImpl(CourseEditorTreeModel cetm, CourseGroupManager cgm, Locale editorLocale,
			NodeAccessType nodeAccessType) {
		this.cetm = cetm;
		this.cgm = cgm;
		this.editorLocale = editorLocale;
		this.conditionExporessionSupported = CoreSpringFactory.getImpl(NodeAccessService.class)
				.isConditionExpressionSupported(nodeAccessType);
	}

	@Override
	public void setConditionInterpreter(ConditionInterpreter ci) {
		this.ci = ci;
	}

	@Override
	public boolean isEnrollmentNode(String nodeId) {
		CourseEditorTreeNode cen = cetm.getCourseEditorNodeById(nodeId);
		if (cen == null) return false;
		if (cen.isDeleted()) return false;
		// node exists and is not marked as deleted, check the associated course
		// node correct type
		return (cen.getCourseNode() instanceof ENCourseNode);
	}

	@Override
	public boolean isAssessable(String nodeId) {
		CourseEditorTreeNode cen = cetm.getCourseEditorNodeById(nodeId);
		if (cen == null) return false;
		if (cen.isDeleted()) return false;
		// node exists and is not marked as deleted, check the associated course
		// node for assessability.
		return AssessmentHelper.checkIfNodeIsAssessable(new CourseEntryRef(cgm), cen.getCourseNode());
	}

	@Override
	public boolean existsNode(String nodeId) {
		CourseEditorTreeNode cen = cetm.getCourseEditorNodeById(nodeId);
		boolean retVal = cen != null && !cen.isDeleted();
		return retVal;
	}
	
	@Override
	public CourseNode getNode(String nodeId) {
		CourseNode cen = cetm.getCourseNode(nodeId);
		return cen;
	}
	// </OLATCE-91>

	@Override
	public boolean existsGroup(String groupNameOrKey) {
		return cgm.existGroup(groupNameOrKey);
	}

	@Override
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

	@Override
	public String getCurrentCourseNodeId() {
		return currentCourseNodeId;
	}

	@Override
	public void setCurrentCourseNodeId(String courseNodeId) {
		this.currentCourseNodeId = courseNodeId;
	}

	@Override
	public Locale getEditorEnvLocale() {
		return editorLocale;
	}

	@Override
	public ConditionErrorMessage[] validateConditionExpression(ConditionExpression condExpr) {
		if (!conditionExporessionSupported) return null;
		
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

	@Override
	public void addSoftReference(String category, String softReference, boolean cycleDetector) {
		currentConditionExpression.addSoftReference(category, softReference, cycleDetector);

	}

	@Override
	public void pushError(Exception e) {
		currentConditionExpression.pushError(e);
	}

	@Override
	public void validateCourse() {
		/*
		 * collect all condition error messages and soft references collect all
		 * configuration errors.
		 */
		String currentNodeWas = currentCourseNodeId;
		if (conditionExporessionSupported) {
			// reset all
			softRefs = new HashMap<>();
			Visitor conditionVisitor = new CollectConditionExpressionsVisitor();
			(new TreeVisitor(conditionVisitor, cetm.getRootNode(), true)).visitAll();
		}

		// refresh,create status descriptions of the course
		statusDescs = new HashMap<>();
		Visitor statusVisitor = new CollectStatusDescriptionVisitor(this);
		(new TreeVisitor(statusVisitor, cetm.getRootNode(), true)).visitAll();
		//
		currentCourseNodeId = currentNodeWas;
	}

	@Override
	public StatusDescription[] getCourseStatus() {
		String[] a = statusDescs.keySet().toArray(new String[statusDescs.keySet().size()]);
		Arrays.sort(a);
		List<StatusDescription> all2gether = new ArrayList<>();
		for (int i = a.length - 1; i >= 0; i--) {
			all2gether.addAll(statusDescs.get(a[i]));
		}

		ICourse course = null;
		try {
			course = CourseFactory.getCourseEditSession(cgm.getCourseEntry().getOlatResource().getResourceableId());
		} catch (AssertException e) {
			log.error("", e);
			course = CourseFactory.loadCourse(cgm.getCourseEntry());
		}

		if(course != null && course.getCourseConfig().getSharedFolderSoftkey().equals("sf.notconfigured")){
			INode rootNode = course.getEditorTreeModel().getRootNode();
			List<StatusDescription> descriptions = new ArrayList<>();
			checkFolderNodes(rootNode, course, descriptions);
			if(!descriptions.isEmpty()){
				all2gether.addAll(descriptions);
			}
		}

		StatusDescription[] retVal = new StatusDescription[all2gether.size()];
		retVal = all2gether.toArray(retVal);
		return retVal;
	}

	private void checkFolderNodes(INode rootNode, ICourse course, final List<StatusDescription> descriptions){
		Visitor visitor = node -> {
			CourseEditorTreeNode courseNode = (CourseEditorTreeNode) course.getEditorTreeModel().getNodeById(node.getIdent());
			if(!courseNode.isDeleted() && courseNode.getCourseNode() instanceof BCCourseNode){
				BCCourseNode bcNode = (BCCourseNode) courseNode.getCourseNode();
				if (bcNode.isSharedFolder()) {
					String translPackage = Util.getPackageName(BCCourseNodeEditController.class);
					StatusDescription status = new StatusDescription(StatusDescription.ERROR, "warning.no.sharedfolder", "warning.no.sharedfolder", null, translPackage);
					status.setDescriptionForUnit(bcNode.getIdent());
					// set which pane is affected by error
					status.setActivateableViewIdentifier(BCCourseNodeEditController.PANE_TAB_FOLDER);
					descriptions.add(status);
				}
			}
		};

		TreeVisitor v = new TreeVisitor(visitor, rootNode, false);
		v.visitAll();
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

		@Override
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
		
		@Override
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

	@Override
	public CourseGroupManager getCourseGroupManager() {
		return cgm;
	}
	
	@Override
	public String getRootNodeId() {
		return cetm.getRootNode().getIdent();
	}

}
