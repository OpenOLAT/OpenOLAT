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
package org.olat.modules.assessment.ui.component;

import java.util.Comparator;
import java.util.Locale;

import org.olat.core.gui.components.form.flexible.impl.elements.table.SortableFlexiTableModelDelegate;
import org.olat.core.gui.components.progressbar.ProgressBar;
import org.olat.core.gui.components.progressbar.ProgressBar.BarColor;
import org.olat.core.gui.components.progressbar.ProgressBar.LabelFormat;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderSize;
import org.olat.core.gui.components.progressbar.ProgressBar.RenderStyle;
import org.olat.core.gui.components.progressbar.ProgressBarCellRenderer;
import org.olat.core.gui.components.progressbar.ProgressBarFactory;

/**
 * 
 * Initial date: 24 Nov 2019<br>
 * @author uhensler, urs.hensler@frentix.com, http://www.frentix.com
 *
 */
public class LearningProgressCompletionCellRenderer extends ProgressBarCellRenderer {

	public LearningProgressCompletionCellRenderer() {
		super(RenderStyle.radial, RenderSize.inline, LabelFormat.none, BarColor.success);
	}
	
	@Override
	protected ProgressBar createProgressBar(Object cellValue, Locale locale) {
		if (cellValue instanceof CompletionPassed completionPassed && completionPassed.getCompletion() != null) {
			BarColor barColor = completionPassed.getPassed() == null || completionPassed.getPassed().booleanValue()
					? BarColor.success
					: BarColor.danger;
			ProgressBar progressBar = createProgressBar(locale);
			progressBar.setMax(1);
			ProgressBarFactory.setValue(progressBar, completionPassed.getCompletion().floatValue(), barColor);
			return progressBar;
		}
		return null;
	}
	
	public interface CompletionPassed {
		
		public Double getCompletion();
		
		public Boolean getPassed();
		
	}
	
	public static Comparator<CompletionPassed> createComparator() {
		return new CompletionPassedComparator();
	}
	
	private static final class CompletionPassedComparator implements Comparator<CompletionPassed> {
		
		@Override
		public int compare(CompletionPassed o1, CompletionPassed o2) {
			if(o1 == null || o2 == null) {
				return SortableFlexiTableModelDelegate.compareNullObjects(o1, o2);
			}
			
			Double completion1 = o1.getCompletion();
			Double completion2 = o2.getCompletion();
			
			if (completion1 == null || completion2 == null) {
				return SortableFlexiTableModelDelegate.compareNullObjects(completion1, completion2);
			}
			int c = Double.compare(completion1.doubleValue(), completion2.doubleValue());
			if (c != 0) {
				return c;
			}
			
			Boolean passed1 = o1.getPassed();
			Boolean passed2 = o2.getPassed();
			if (passed1 == null || passed2 == null) {
				return SortableFlexiTableModelDelegate.compareNullObjects(passed1, passed2);
			}
			
			return Boolean.compare(passed1, passed2);
		}
	}

}
