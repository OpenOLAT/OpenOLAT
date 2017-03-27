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
package org.olat.modules.lecture;

import java.util.List;

import org.olat.core.id.Identity;
import org.olat.repository.RepositoryEntry;
import org.olat.repository.RepositoryEntryRef;

/**
 * 
 * Initial date: 17 mars 2017<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public interface LectureService {
	
	/**
	 * Create but not persist a new lecture block.
	 * 
	 * @param entry The repository entry which own the block
	 * @return A new lecture block
	 */
	public LectureBlock createLectureBlock(RepositoryEntry entry);
	
	/**
	 * Merge or persist the specified lecture block and return
	 * the fresh block.
	 * 
	 * @param lectureBlock The block to merge or persist
	 * @return The merged block
	 */
	public LectureBlock save(LectureBlock lectureBlock);
	
	public LectureBlock getLectureBlock(LectureBlockRef block);
	
	/**
	 * 
	 * @param entry
	 * @return
	 */
	public List<LectureBlock> getLectureBlocks(RepositoryEntryRef entry);

	/**
	 * 
	 * @param entry
	 * @param identity
	 * @return
	 */
	public boolean hasLecturesAsTeacher(RepositoryEntryRef entry, Identity identity);
	
	public List<Identity> getTeachers(LectureBlock block);
	
	public void addTeacher(LectureBlock block, Identity teacher);
	
	public void removeTeacher(LectureBlock block, Identity teacher);

}
