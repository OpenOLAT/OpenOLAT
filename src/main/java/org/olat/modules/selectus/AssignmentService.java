/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus;

import java.util.List;

import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;

import org.olat.modules.selectus.model.ApplicationAssignmentLight;
import org.olat.modules.selectus.model.ApplicationLight;
import org.olat.modules.selectus.model.ApplicationRef;
import org.olat.modules.selectus.model.PositionRef;
import org.olat.modules.selectus.ui.committee.assignment.AssignmentsData.Spreading;

/**
 * 
 * Initial date: 25 oct. 2019<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface AssignmentService {
	
	/**
	 * Assign all applications to all assignee
	 * @param application
	 * @param assigneeList
	 */
	public void assignments(PositionRef position, List<ApplicationLight> applications, List<Identity> assigneeList,
			Identity doer, Translator translator);
	
	/**
	 * 
	 * @param position
	 * @param applications
	 * @param assigneeList
	 * @param maximumAssignments
	 * @param additionalAssignments
	 * @param spreading
	 * @param doer
	 * @param translator
	 */
	public void assignments(PositionRef position, List<ApplicationLight> applications, List<Identity> assigneeList,
			Integer maximumAssignments, Integer additionalAssignments, Spreading spreading,
			Identity doer, Translator translator);
	
	/**
	 * Remove the following committee member's assignments from the pseicified list of applications.
	 * 
	 * @param position The position
	 * @param applications The applications to manage
	 * @param assigneeList A list of assignees to remove
	 * @param doer 
	 * @param translator
	 */
	public void removeAssignments(PositionRef position, List<ApplicationLight> applications, List<Identity> assigneeList,
			Identity doer, Translator translator);
	
	public List<ApplicationAssignmentLight> getAssignments(PositionRef position);
	
	public List<Identity> getAssignees(ApplicationRef application);

}
