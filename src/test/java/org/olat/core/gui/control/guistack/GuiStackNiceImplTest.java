/**
 * <a href="https://www.openolat.org">
 * OpenOLAT - Online Learning and Training</a><br>
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License"); <br>
 * you may not use this file except in compliance with the License.<br>
 * You may obtain a copy of the License at the
 * <a href="https://www.apache.org/licenses/LICENSE-2.0">Apache homepage</a>
 * <p>
 * Unless required by applicable law or agreed to in writing,<br>
 * software distributed under the License is distributed on an "AS IS" BASIS, <br>
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
 * See the License for the specific language governing permissions and <br>
 * limitations under the License.
 * <p>
 * Initial code contributed and copyrighted by<br>
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.core.gui.control.guistack;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.atLeast;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.olat.core.gui.components.panel.Panel;
import org.olat.core.gui.control.WindowBackOffice;
import org.olat.core.gui.control.winmgr.Command;

/**
 *
 * Initial date: 21 Oct 2026<br>
 * @author uhensler, urs.hensler@frentix.com, https://www.frentix.com
 *
 */
public class GuiStackNiceImplTest {
	
	private final WindowBackOffice wbo = mock(WindowBackOffice.class);
	private final GuiStackNiceImpl guiStack = new GuiStackNiceImpl(wbo, new Panel("main"));
	
	@Test
	public void modalPushesAndPopsFocus() {
		guiStack.pushModalDialog(new Panel("modal"), false);
		guiStack.popContent();
		
		assertThat(sentFunctions()).containsExactly("pushdialogfocus", "popdialogfocus");
	}
	
	@Test
	public void calloutSendsNoFocusCommand() {
		guiStack.pushCallout(new Panel("callout"), "o_c1", null);
		guiStack.popContent();
		
		assertThat(sentFunctions()).isEmpty();
	}
	
	@Test
	public void modalOverTopModalPushesOnce() {
		guiStack.pushTopModalDialog(new Panel("top"));
		guiStack.pushModalDialog(new Panel("modal"), false);
		guiStack.popContent();
		guiStack.popContent();
		
		assertThat(sentFunctions()).containsExactly("pushdialogfocus", "pushdialogfocus", "popdialogfocus", "popdialogfocus");
	}
	
	@Test
	public void removeModalDialogPopsFocus() {
		Panel modal = new Panel("modal");
		guiStack.pushModalDialog(modal, false);
		
		assertThat(guiStack.removeModalDialog(modal)).isTrue();
		assertThat(sentFunctions()).containsExactly("pushdialogfocus", "popdialogfocus");
	}
	
	@Test
	public void removeModalDialogUnderTopModalRemovesItsOwnLayer() {
		Panel modal = new Panel("modal");
		guiStack.pushModalDialog(modal, false);
		guiStack.pushTopModalDialog(new Panel("top"));
		
		assertThat(guiStack.removeModalDialog(modal)).isTrue();
		assertThat(guiStack.getModalPanel().getContent()).isNull();
		assertThat(guiStack.getTopModalPanel().getContent()).isNotNull();
		assertThat(sentFunctions()).containsExactly("pushdialogfocus", "pushdialogfocus", "popdialogfocus");
	}
	
	private List<String> sentFunctions() {
		ArgumentCaptor<Command> commands = ArgumentCaptor.forClass(Command.class);
		verify(wbo, atLeast(0)).sendCommandTo(commands.capture());
		return commands.getAllValues().stream()
				.map(command -> command.getSubJSON().getString("func"))
				.toList();
	}
}
