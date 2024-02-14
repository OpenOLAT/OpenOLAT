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
package org.olat.modules.project;

import org.olat.basesecurity.IdentityRef;
import org.olat.modules.todo.ToDoTaskSecurityCallback;

/**
 * 
 * Initial date: 1 Dec 2022<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ProjProjectSecurityCallback extends ToDoTaskSecurityCallback {
	
	boolean canViewProjectMetadata();
	
	boolean canEditProjectMetadata();
	
	boolean canEditProjectStatus();
	
	boolean canDeleteProject();

	boolean canExportReport();
	
	boolean canCopyProject();
	
	boolean canCreateTemplate();
	
	boolean canSubscribe();
	
	boolean canViewMembers();
	
	boolean canEditMembers();
	
	boolean canViewTimeline();
	
	boolean canViewWhiteboard();
	
	boolean canEditWhiteboard();
	
	boolean canCreateFiles();

	boolean canViewFiles();

	boolean canEditFiles();

	boolean canEditFile(ProjFile file);

	boolean canRestoreFile(ProjFile file);
	
	boolean canDeleteFile(ProjFile file, IdentityRef identity);
	
	boolean canDeleteFilePermanently(ProjFile file);
	
	boolean canViewToDos();

	boolean canCreateToDos();

	boolean canEditToDos();

	boolean canEditToDo(ProjToDo toDo, boolean participant);

	boolean canDeleteToDo(ProjToDo toDo, IdentityRef identity);

	boolean canViewDecisions();

	boolean canCreateDecisions();
	
	boolean canEditDecisions();

	boolean canEditDecision(ProjDecision decision);

	boolean canDeleteDecision(ProjDecision decision, IdentityRef identity);
	
	boolean canViewNotes();

	boolean canCreateNotes();
	
	boolean canEditNotes();

	boolean canEditNote(ProjNote note);

	boolean canDeleteNote(ProjNote note, IdentityRef identity);

	boolean canViewAppointments();

	boolean canCreateAppointments();
	
	boolean canEditAppointments();

	boolean canEditAppointment(ProjAppointment appointment);

	boolean canDeleteAppointment(ProjAppointment appointment, IdentityRef identity);

	boolean canViewMilestones();

	boolean canCreateMilestones();
	
	boolean canEditMilestones();

	boolean canEditMilestone(ProjMilestone milestone);

	boolean canDeleteMilestone(ProjMilestone milestone, IdentityRef identity);

}
