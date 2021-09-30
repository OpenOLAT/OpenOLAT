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
package org.olat.course.learningpath.obligation;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.WindowControl;
import org.olat.core.gui.translator.Translator;
import org.olat.core.id.Identity;
import org.olat.course.Structure;
import org.olat.course.assessment.ScoreAccountingTriggerData;
import org.olat.course.nodes.CourseNode;
import org.olat.course.run.scoring.ObligationContext;
import org.olat.course.run.scoring.ScoreAccounting;
import org.olat.repository.RepositoryEntry;

/**
 * 
 * Initial date: 17 Sep 2021<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public interface ExceptionalObligationHandler {
	
	public String getType();
	
	/**
	 * The lowest value is the top most entry in the selection list.
	 * 
	 * @return
	 */
	public int getSortValue();
	
	public boolean isEnabled();
	
	public String getAddI18nKey();
	
	public boolean isShowAdd(RepositoryEntry courseEntry);
	
	public String getDisplayType(Translator translator, ExceptionalObligation exceptionalObligation);
	
	public String getDisplayName(Translator translator, ExceptionalObligation exceptionalObligation, RepositoryEntry courseEntry);

	public String getDisplayText(Translator translator, ExceptionalObligation exceptionalObligation, RepositoryEntry courseEntry);
	
	public boolean hasScoreAccountingTrigger();
	
	public ScoreAccountingTriggerData getScoreAccountingTriggerData(ExceptionalObligation exceptionalObligation);
	
	boolean matchesIdentity(ExceptionalObligation exceptionalObligation, Identity identity,
			ObligationContext obligationContext, Structure runStructure, ScoreAccounting scoreAccounting);
	
	public ExceptionalObligationController createCreationController(UserRequest ureq, WindowControl wControl,
			RepositoryEntry courseEntry, CourseNode courseNode);

}
