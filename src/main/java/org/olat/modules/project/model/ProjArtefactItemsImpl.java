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
package org.olat.modules.project.model;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.modules.project.ProjAppointment;
import org.olat.modules.project.ProjArtefact;
import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactRef;
import org.olat.modules.project.ProjDecision;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjMilestone;
import org.olat.modules.project.ProjNote;
import org.olat.modules.project.ProjToDo;

/**
 * 
 * Initial date: 6 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjArtefactItemsImpl implements ProjArtefactItems {
	
	private List<ProjFile> files;
	private Map<Long, ProjFile> artefactKeyToFile;
	private List<ProjToDo> toDos;
	private Map<Long, ProjToDo> artefactKeyToToDo;
	private List<ProjDecision> decisions;
	private Map<Long, ProjDecision> artefactKeyToDecision;
	private List<ProjNote> notes;
	private Map<Long, ProjNote> artefactKeyToNote;
	private List<ProjAppointment> appointments;
	private Map<Long, ProjAppointment> artefactKeyToAppointment;
	private List<ProjMilestone> milestones;
	private Map<Long, ProjMilestone> artefactKeyToMilestone;
	
	@Override
	public Set<ProjArtefact> getArtefacts() {
		Set<ProjArtefact> artefacts = new HashSet<>();
		if (files != null) {
			artefacts.addAll(files.stream().map(ProjFile::getArtefact).toList());
		}
		if (toDos != null) {
			artefacts.addAll(toDos.stream().map(ProjToDo::getArtefact).toList());
		}
		if (decisions != null) {
			artefacts.addAll(decisions.stream().map(ProjDecision::getArtefact).toList());
		}
		if (notes != null) {
			artefacts.addAll(notes.stream().map(ProjNote::getArtefact).toList());
		}
		if (appointments != null) {
			artefacts.addAll(appointments.stream().map(ProjAppointment::getArtefact).toList());
		}
		if (milestones != null) {
			artefacts.addAll(milestones.stream().map(ProjMilestone::getArtefact).toList());
		}
		return artefacts;
	}
	
	@Override
	public List<ProjFile> getFiles() {
		return files;
	}
	
	public void setFiles(List<ProjFile> files) {
		this.files = files;
	}
	
	@Override
	public ProjFile getFile(ProjArtefactRef artefact) {
		if (artefactKeyToFile == null) {
			if (files != null) {
				artefactKeyToFile = files.stream().collect(Collectors.toMap(file -> file.getArtefact().getKey(), Function.identity()));
			}
		}
		return artefactKeyToFile != null? artefactKeyToFile.get(artefact.getKey()): null;
	}
	
	@Override
	public List<ProjToDo> getToDos() {
		return toDos;
	}
	
	public void setToDos(List<ProjToDo> toDos) {
		this.toDos = toDos;
	}
	
	@Override
	public ProjToDo getToDo(ProjArtefactRef artefact) {
		if (artefactKeyToToDo == null) {
			if (toDos != null) {
				artefactKeyToToDo = toDos.stream().collect(Collectors.toMap(toDo -> toDo.getArtefact().getKey(), Function.identity()));
			}
		}
		return artefactKeyToToDo != null? artefactKeyToToDo.get(artefact.getKey()): null;
	}
	
	@Override
	public List<ProjDecision> getDecisions() {
		return decisions;
	}
	
	public void setDecisions(List<ProjDecision> decisions) {
		this.decisions = decisions;
	}
	
	@Override
	public ProjDecision getDecision(ProjArtefactRef artefact) {
		if (artefactKeyToDecision == null) {
			if (decisions != null) {
				artefactKeyToDecision = decisions.stream().collect(Collectors.toMap(decision -> decision.getArtefact().getKey(), Function.identity()));
			}
		}
		return artefactKeyToDecision != null? artefactKeyToDecision.get(artefact.getKey()): null;
	}
	
	@Override
	public List<ProjNote> getNotes() {
		return notes;
	}
	
	public void setNotes(List<ProjNote> notes) {
		this.notes = notes;
	}
	
	@Override
	public ProjNote getNote(ProjArtefactRef artefact) {
		if (artefactKeyToNote == null) {
			if (notes != null) {
				artefactKeyToNote = notes.stream().collect(Collectors.toMap(note -> note.getArtefact().getKey(), Function.identity()));
			}
		}
		return artefactKeyToNote != null? artefactKeyToNote.get(artefact.getKey()): null;
	}
	
	@Override
	public List<ProjAppointment> getAppointments() {
		return appointments;
	}
	
	public void setAppointments(List<ProjAppointment> appointments) {
		this.appointments = appointments;
	}
	
	@Override
	public ProjAppointment getAppointment(ProjArtefactRef artefact) {
		if (artefactKeyToAppointment == null) {
			if (appointments != null) {
				artefactKeyToAppointment = appointments.stream().collect(Collectors.toMap(appointment -> appointment.getArtefact().getKey(), Function.identity()));
			}
		}
		return artefactKeyToAppointment != null? artefactKeyToAppointment.get(artefact.getKey()): null;
	}
	
	@Override
	public List<ProjMilestone> getMilestones() {
		return milestones;
	}
	
	public void setMilestones(List<ProjMilestone> milestones) {
		this.milestones = milestones;
	}
	
	@Override
	public ProjMilestone getMilestone(ProjArtefactRef artefact) {
		if (artefactKeyToMilestone == null) {
			if (milestones != null) {
				artefactKeyToMilestone = milestones.stream().collect(Collectors.toMap(milestone -> milestone.getArtefact().getKey(), Function.identity()));
			}
		}
		return artefactKeyToMilestone != null? artefactKeyToMilestone.get(artefact.getKey()): null;
	}

}
