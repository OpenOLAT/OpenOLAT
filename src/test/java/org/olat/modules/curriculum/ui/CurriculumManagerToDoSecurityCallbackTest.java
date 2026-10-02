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
package org.olat.modules.curriculum.ui;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.olat.modules.curriculum.CurriculumElement;
import org.olat.modules.curriculum.CurriculumElementRef;
import org.olat.modules.curriculum.CurriculumSecurityCallback;
import org.olat.modules.curriculum.CurriculumService;
import org.olat.modules.todo.ToDoStatus;
import org.olat.modules.todo.ToDoTask;

/**
 * Initial date: 2 Oct 2026<br>
 * @author uhensler, https://www.frentix.com
 */
public class CurriculumManagerToDoSecurityCallbackTest {

	private final CurriculumSecurityCallback secCallback = mock(CurriculumSecurityCallback.class);
	private final CurriculumService curriculumService = mock(CurriculumService.class);
	private final CurriculumManagerToDoSecurityCallback sut =
			new CurriculumManagerToDoSecurityCallback(secCallback, curriculumService);

	@Test
	public void shouldLoadElementOnDemand() {
		CurriculumElement element = mockElement(42l, true);

		boolean canEdit = sut.canEdit(createTask("42"), false, false, false);

		assertThat(canEdit).isTrue();
		verify(secCallback).canEditCurriculumElement(element);
	}

	@Test
	public void shouldLoadElementOnlyOncePerSubPath() {
		mockElement(42l, true);
		ToDoTask task = createTask("42");

		sut.canEdit(task, false, false, false);
		sut.canDelete(task, false, false, false);
		sut.getAssigneeRightsOverride(task);

		verify(curriculumService, times(1)).getCurriculumElement(argThat((CurriculumElementRef ref) -> isKey(ref, 42l)));
	}

	@Test
	public void shouldDenyEdit_whenElementDoesNotExist() {
		boolean canEdit = sut.canEdit(createTask("43"), false, false, false);

		assertThat(canEdit).isFalse();
	}

	@Test
	public void shouldDenyEdit_whenSecurityCallbackDenies() {
		mockElement(42l, false);

		boolean canEdit = sut.canEdit(createTask("42"), false, false, false);

		assertThat(canEdit).isFalse();
	}

	@Test
	public void shouldDenyEdit_whenToDoTaskHasNoSubPath() {
		boolean canEdit = sut.canEdit(createTask(null), false, false, false);

		assertThat(canEdit).isFalse();
	}

	private CurriculumElement mockElement(Long key, boolean canEdit) {
		CurriculumElement element = mock(CurriculumElement.class);
		when(curriculumService.getCurriculumElement(argThat((CurriculumElementRef ref) -> isKey(ref, key)))).thenReturn(element);
		when(secCallback.canEditCurriculumElement(element)).thenReturn(canEdit);
		return element;
	}

	private boolean isKey(CurriculumElementRef ref, Long key) {
		return ref != null && key.equals(ref.getKey());
	}

	private ToDoTask createTask(String originSubPath) {
		ToDoTask task = mock(ToDoTask.class);
		when(task.getStatus()).thenReturn(ToDoStatus.open);
		when(task.getOriginSubPath()).thenReturn(originSubPath);
		return task;
	}

}
