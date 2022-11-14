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

import org.junit.Before;
import org.junit.Test;
import org.olat.core.gui.components.form.flexible.impl.Form;
import org.olat.core.gui.render.StringOutput;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.olat.core.gui.translator.Translator;
import org.olat.test.KeyTranslator;

import java.util.Locale;

/**
 * 
 * Initial date: 23.09.2018<br>
 * @author tomgross, itconsense@gmail.com
 *
 */
public class FileElementRendererTest {
	

	private FileElementComponent fileElementComponentMock;
	private FileElementImpl fileElementImplMock;

	private Form formMock;
	private StringOutput stringOutput;
	private FileElementRenderer fer;

	@Before
	public void setup() {
		formMock = mock(Form.class);
		fileElementComponentMock = mock(FileElementComponent.class);
		fileElementImplMock = mock(FileElementImpl.class);
		stringOutput = new StringOutput();
		fer = spy(new FileElementRenderer());
	}

	@Test
	public void renderButtonsShouldContainFileInput() {
		when(fileElementImplMock.getRootForm()).thenReturn(formMock);
		when(fileElementComponentMock.getFormItem()).thenReturn(fileElementImplMock);
		when(fileElementComponentMock.isEnabled()).thenReturn(Boolean.TRUE);
		when(fileElementImplMock.isButtonsEnabled()).thenReturn(Boolean.TRUE);

		String resultHtml = new StringBuilder()
				.append("<input type='file'").toString();

		Translator translator = new KeyTranslator(Locale.ENGLISH);
		doReturn(translator).when(fer).getTranslator(any());
		fer.render(null, stringOutput, fileElementComponentMock, null, translator, null, null);

		assertThat(stringOutput.toString()).containsIgnoringCase(resultHtml);
	}
}
