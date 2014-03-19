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

package org.olat.modules.dialog;

/**
 * Description:<br>
 * TODO: guido Class Description for DialogElement
 * <P>
 * Initial Date: 14.11.2005 <br>
 * 
 * @author guido
 */
import java.util.Iterator;
import java.util.List;

import org.olat.core.id.OLATResourceable;
import org.olat.core.logging.AssertException;
import org.olat.core.logging.OLATRuntimeException;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.xml.XStreamHelper;
import org.olat.course.nodes.CourseNode;
import org.olat.course.nodes.DialogCourseNode;
import org.olat.course.properties.CoursePropertyManager;
import org.olat.properties.Property;
import org.olat.properties.PropertyManager;

public class DialogElementsPropertyManager extends BasicManager {
	public static final String PROPERTY_NAME = "fileDialog";
	// create with spring
	private static DialogElementsPropertyManager dialogElementsManager = new DialogElementsPropertyManager();

	private DialogElementsPropertyManager() {
	// private constr. for singleton
	}

	/**
	 * @return single instance
	 */
	public static DialogElementsPropertyManager getInstance() {
		return dialogElementsManager;
	}

	/**
	 * @param userCourseEnv
	 * @param courseNode
	 * @return an empty DialogPropertyElements if noting found or the populated object
	 */
	public DialogPropertyElements findDialogElements(CoursePropertyManager coursePropMgr, CourseNode courseNode) {
		Property property = coursePropMgr.findCourseNodeProperty(courseNode, null, null, PROPERTY_NAME);
		if (property == null) return new DialogPropertyElements(PROPERTY_NAME);
		else return (DialogPropertyElements) XStreamHelper.fromXML(property.getTextValue());
	}

	/**
	 * Find all DialogElements for a certain coursenode
	 * 
	 * @param courseId
	 * @param courseNodeId
	 * @return a Object containing a collection of DialogElements
	 */
	public DialogPropertyElements findDialogElements(Long courseId, String courseNodeId) {
		Property prop = findProperty(courseId, courseNodeId);
		if (prop == null) return new DialogPropertyElements(PROPERTY_NAME);
		return (DialogPropertyElements) XStreamHelper.fromXML(prop.getTextValue());
	}

	/**
	 * @param userCourseEnv
	 * @param courseNode
	 * @param fileDialogId
	 * @return
	 */
	protected DialogElement findDialogElement(CoursePropertyManager coursePropMgr, CourseNode courseNode, Long forumKey) {
		DialogPropertyElements elements = findDialogElements(coursePropMgr, courseNode);
		if (elements != null) {
			List<DialogElement> list = elements.getDialogPropertyElements();
			for (DialogElement element : list) {
				if (element.getForumKey().equals(forumKey)) return element;
			}
		} else {
			throw new OLATRuntimeException(this.getClass(), "trying to find property element, but no properties yet exists: coursenode "
					+courseNode.getShortTitle()+"("+courseNode.getIdent()+") and forum with key: "+forumKey, null);
		}
		// no match
		return null;
	}

	/**
	 * Deletes a single dialog element which are all in one property stored. The
	 * property will still exist even if it contains no elements
	 * 
	 * @param userCourseEnv
	 * @param courseNode
	 * @param fileDialogId
	 */
	protected void deleteDialogElement(final CoursePropertyManager coursePropMgr, final CourseNode courseNode, Long forumKey) {
		final DialogPropertyElements elements = findDialogElements(coursePropMgr, courseNode);
		if (elements != null) {
			final List<DialogElement> list = elements.getDialogPropertyElements();
			for (Iterator<DialogElement> iter = list.iterator(); iter.hasNext();) {
				final DialogElement element = iter.next();
				if (element.getForumKey().equals(forumKey)) {					
          //o_clusterOK by:ld 
					OLATResourceable courseNodeResourceable = OresHelper.createOLATResourceableInstance(DialogCourseNode.class, new Long(courseNode.getIdent()));
					CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(courseNodeResourceable, new SyncerExecutor(){
					  public void execute() {
						list.remove(element);
						String dialogElementsAsXML = XStreamHelper.toXML(elements);
						Property property = coursePropMgr.findCourseNodeProperty(courseNode, null, null, PROPERTY_NAME);

						property.setTextValue(dialogElementsAsXML);
						coursePropMgr.updateProperty(property);						
					  }});	
					break;
				}					
			}			
		} else {
			throw new OLATRuntimeException(this.getClass(), "trying to delete property element, but no properties yet exist for course node: "+courseNode.getIdent(), null);
		}
	}

	/**
	 * persits a new added dialog element in the course node property
	 * 
	 * @param userCourseEnv
	 * @param courseNode
	 * @param forumKey
	 * @param fileName
	 * @param authorUsername
	 * @param fileDialogId
	 */
	public void addDialogElement(final CoursePropertyManager coursePropMgr, final CourseNode courseNode, final DialogElement element) {
		//o_clusterOK by:ld (it was assumed that the courseNodeId - used for constructing  the olatResourceable - is unique over all courses)
		OLATResourceable courseNodeResourceable = OresHelper.createOLATResourceableInstance(DialogCourseNode.class, new Long(courseNode.getIdent()));
		CoordinatorManager.getInstance().getCoordinator().getSyncer().doInSync(courseNodeResourceable, new SyncerExecutor(){
		  public void execute() {
		  	DialogPropertyElements dialogProps = findDialogElements(coursePropMgr, courseNode);

				dialogProps.addElement(element);
				String dialogElementsAsXML = XStreamHelper.toXML(dialogProps);
				Property property = coursePropMgr.findCourseNodeProperty(courseNode, null, null, PROPERTY_NAME);
				if (property == null) {
					property = coursePropMgr.createCourseNodePropertyInstance(courseNode, null, null, PROPERTY_NAME, null, null, null,
							dialogElementsAsXML);
					coursePropMgr.saveProperty(property);
				} else {
					property.setTextValue(dialogElementsAsXML);
					coursePropMgr.updateProperty(property);
				}		 
	  }});				
	}

	private Property findProperty(Long courseId, String courseNodeId) {
		PropertyManager propMrg = PropertyManager.getInstance();
		String category = "NID:dial::" + courseNodeId;
		List<Property> elements = propMrg.findProperties(null, null, "CourseModule", courseId, category, PROPERTY_NAME);
		if (elements.size() == 0) return null; //no match
		if (elements.size() != 1) throw new AssertException(
				"Found more then one property for a course node 'dialog element' which should never happen!");
		return elements.get(0);
	}

	/**
	 * Delete the file dialog course node poperty
	 * 
	 * @param courseId
	 * @param courseNodeId
	 */
	public void deleteProperty(Long courseId, String courseNodeId) {
		Property prop = findProperty(courseId, courseNodeId);
		PropertyManager propMrg = PropertyManager.getInstance();
		if (prop != null) propMrg.deleteProperty(prop);
	}
	
}
