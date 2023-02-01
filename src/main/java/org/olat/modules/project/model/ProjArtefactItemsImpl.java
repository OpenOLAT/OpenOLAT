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

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.olat.modules.project.ProjArtefactItems;
import org.olat.modules.project.ProjArtefactRef;
import org.olat.modules.project.ProjFile;
import org.olat.modules.project.ProjNote;

/**
 * 
 * Initial date: 6 Jan 2023<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class ProjArtefactItemsImpl implements ProjArtefactItems {
	
	private List<ProjFile> files;
	private List<ProjNote> notes;
	private Map<Long, ProjFile> artefactKeyToFile;
	private Map<Long, ProjNote> artefactKeyToNote;
	
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

}
