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
package org.olat.core.gui.components.form.flexible.impl.elements;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.junit.Before;
import org.junit.Test;
import org.olat.core.gui.components.form.flexible.elements.SingleSelection;
import org.olat.core.gui.translator.Translator;

/**
 * 
 * Initial date: 04.01.2018<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class SelectboxSelectionImplTest {
	
	private static final int SELECTION_INDEX = 1;
	private static final String SELECTION_KEY = "key2";
	private static final String SELECTION_VALUE = "value2";
	private static final String[] KEYS = {"key1", SELECTION_KEY, "key3"};
	private static final String[] VALUES = {"value1", SELECTION_VALUE, "value3"};
	
	private SelectboxSelectionImpl sut;
	
	@Before
	public void setUp() {
		Translator translatorMock = mock(Translator.class);
		sut = new SelectboxSelectionImpl("", "", translatorMock);
	}

	@Test
	public void shouldGetSelectedIndex() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.select(SELECTION_KEY, true);
		
		int index = sut.getSelected();
		
		assertThat(index).isEqualTo(SELECTION_INDEX);
	}
	
	@Test
	public void shouldGetSelectedIndexIfNoSelectionIsAllowed() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.select(SELECTION_KEY, true);
		sut.setAllowNoSelection(true);
		
		int index = sut.getSelected();
		
		assertThat(index).isEqualTo(SELECTION_INDEX);
	}
	
	@Test
	public void shouldGetSelectedKey() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.select(SELECTION_KEY, true);
		
		String selectedKey = sut.getSelectedKey();
		
		assertThat(selectedKey).isEqualTo(SELECTION_KEY);
	}
	
	@Test
	public void shouldGetSelectedKeyIfNoSelectionIsAllowed() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.setAllowNoSelection(true);
		sut.select(SELECTION_KEY, true);

		String selectedKey = sut.getSelectedKey();
		
		assertThat(selectedKey).isEqualTo(SELECTION_KEY);
	}
	
	@Test
	public void shouldGetSelectedKeyIfNoSelectionNotIsAllowedAnymore() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.setAllowNoSelection(true);
		sut.select(SELECTION_KEY, true);
		sut.setAllowNoSelection(false);

		String selectedKey = sut.getSelectedKey();
		
		assertThat(selectedKey).isEqualTo(SELECTION_KEY);
	}
	
	@Test
	public void shouldGetSelectedValue() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.select(SELECTION_KEY, true);
		
		String selectedValue = sut.getSelectedValue();
		
		assertThat(selectedValue).isEqualTo(SELECTION_VALUE);
	}

	@Test
	public void shouldGetSelectedValueIfNoSelectionIsAllowed() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.select(SELECTION_KEY, true);
		sut.setAllowNoSelection(true);
		
		String selectedValue = sut.getSelectedValue();
		
		assertThat(selectedValue).isEqualTo(SELECTION_VALUE);
	}

	@Test
	public void shouldGetKey() {
		sut.setKeysAndValues(KEYS, VALUES, null);

		String key = sut.getKey(SELECTION_INDEX);
		
		assertThat(key).isEqualTo(SELECTION_KEY);
	}
	
	@Test
	public void shouldGetKeyIfNoSelectionIsAllowed() {
		sut.setAllowNoSelection(true);
		sut.setKeysAndValues(KEYS, VALUES, null);

		String key = sut.getKey(SELECTION_INDEX);
		
		assertThat(key).isEqualTo(SELECTION_KEY);
	}

	@Test
	public void shouldGetValue() {
		sut.setKeysAndValues(KEYS, VALUES, null);

		String value = sut.getValue(SELECTION_INDEX);
		
		assertThat(value).isEqualTo(SELECTION_VALUE);
	}
	
	@Test
	public void shouldGetValueIfNoSelectionIsAllowed() {
		sut.setAllowNoSelection(true);
		sut.setKeysAndValues(KEYS, VALUES, null);

		String value = sut.getValue(SELECTION_INDEX);
		
		assertThat(value).isEqualTo(SELECTION_VALUE);
	}
	
	@Test
	public void shouldCheckWhetherIsOneSelected() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.select(SELECTION_KEY, true);
		
		boolean isOneSelected = sut.isOneSelected();

		assertThat(isOneSelected).isTrue();
	}

	@Test
	public void shouldCheckWhetherIsOneSelectedIfNoSelectionIsAllowed() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.setAllowNoSelection(true);
		sut.select(SELECTION_KEY, true);
		
		boolean isOneSelected = sut.isOneSelected();

		assertThat(isOneSelected).isTrue();
	}

	@Test
	public void shouldCheckWheterIsNotOneSelected() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		
		boolean isOneSelected = sut.isOneSelected();

		assertThat(isOneSelected).isFalse();
	}
	
	@Test
	public void shouldCheckWheterIsNotOneSelectedIfNoSelectionIsAllowed() {
		sut.isAllowNoSelection();
		sut.setKeysAndValues(KEYS, VALUES, null);
		
		boolean isOneSelected = sut.isOneSelected();

		assertThat(isOneSelected).isFalse();
	}
	
	@Test
	public void shouldCheckWhetherIsSelected() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.select(SELECTION_KEY, true);
		
		boolean isSelected = sut.isSelected(SELECTION_INDEX);

		assertThat(isSelected).isTrue();
	}
	
	@Test
	public void shouldCheckWhetherIsSelectedIfNoSelectionIsAllowed() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.select(SELECTION_KEY, true);
		sut.setAllowNoSelection(true);
		
		boolean isSelected = sut.isSelected(SELECTION_INDEX);

		assertThat(isSelected).isTrue();
	}
	
	@Test
	public void shouldCheckWhetherIsNotSelected() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.select(SELECTION_KEY, true);
		
		boolean isSelected = sut.isSelected(9);

		assertThat(isSelected).isFalse();
	}
	
	@Test
	public void shouldGetSize() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		
		int size = sut.getSize();
		
		assertThat(size).isEqualTo(KEYS.length);
	}

	@Test
	public void shouldSouldGetSizeIfNoSelectionAllowed() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.setAllowNoSelection(true);
		
		int size = sut.getSize();
		
		assertThat(size).isEqualTo(KEYS.length);
	}

	@Test
	public void shouldSetKeysAndValues() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		
		assertThat(sut.getKeys()).contains(KEYS);
		assertThat(sut.getValues()).contains(VALUES);
	}

	@Test
	public void shouldAddNoSelectionEntryIfNoSelectionAllowed() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		
		sut.setAllowNoSelection(true);
		
		assertThat(sut.getKeys())
				.hasSize(KEYS.length + 1)
				.contains(KEYS)
				.contains(SingleSelection.NO_SELECTION_KEY);
		assertThat(sut.getValues())
				.hasSize(VALUES.length + 1)
				.contains(VALUES);
	}
	
	@Test
	public void shouldNotBeSelectedIfEmptyEntryIsSelected() {
		sut.setKeysAndValues(KEYS, VALUES, null);
		sut.setAllowNoSelection(true);
		sut.select(SingleSelection.NO_SELECTION_KEY, true);
		
		boolean isOneSelected = sut.isOneSelected();
		
		assertThat(isOneSelected).isFalse();
	}
}
