/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
 */
package org.olat.modules.selectus.ui.components;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

import org.olat.core.commons.services.commentAndRating.model.UserRating;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiCellRenderer;
import org.olat.core.gui.components.form.flexible.impl.elements.table.FlexiTableComponent;
import org.olat.core.gui.components.table.CustomCellRenderer;
import org.olat.core.gui.components.velocity.VelocityContainer;
import org.olat.core.gui.render.Renderer;
import org.olat.core.gui.render.StringOutput;
import org.olat.core.gui.render.URLBuilder;
import org.olat.core.gui.translator.Translator;

import org.olat.modules.selectus.ui.UserRatingMapper;
import org.olat.modules.selectus.ui.rating.RatingComparator;
import org.olat.modules.selectus.ui.rating.RatingsOverviewComponent;

/**
 * 
 * Description:<br>
 * 
 * <P>
 * Initial Date:  2 aug. 2010 <br>
 *
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 */
public class MultiRatingCellRenderer implements CustomCellRenderer, FlexiCellRenderer {
	
	private final VelocityContainer vc;
	private final Translator translator;
	private final RatingComparator ratingComparator = new RatingComparator();
	
	public MultiRatingCellRenderer(Translator translator, VelocityContainer vc) {
		this.vc = vc;
		this.translator = translator;
	}
	
	@Override
	public void render(Renderer renderer, StringOutput target, Object cellValue, int row,
			FlexiTableComponent source, URLBuilder ubu, Translator translator) {
		
		if(cellValue instanceof UserRatingMapper) {
			UserRatingMapper mapper = (UserRatingMapper)cellValue;
			String uuid = "multi-" + mapper.getUUID();
			List<UserRating> ratings = mapper.getRatings();
			Collections.sort(ratings, ratingComparator);
			RatingsOverviewComponent cmp = new RatingsOverviewComponent(uuid);
			cmp.setRatings(ratings);
			URLBuilder ubuCopy = renderer.getUrlBuilder().createCopyFor(cmp);
			cmp.getHTMLRendererSingleton().render(renderer, target, cmp, ubuCopy, translator, null, null);
		}
	}

	@Override
	public void render(StringOutput sb, Renderer renderer, Object val, Locale locale, int alignment, String action) {
		if(val instanceof UserRatingMapper) {
			UserRatingMapper mapper = (UserRatingMapper)val;
			String uuid = "multi-" + mapper.getUUID();
			List<UserRating> ratings = mapper.getRatings();
			Collections.sort(ratings, ratingComparator);
			RatingsOverviewComponent cmp = (RatingsOverviewComponent)vc.getComponent(uuid);
			if(cmp == null) {
				cmp = new RatingsOverviewComponent(uuid);
			}
			cmp.setRatings(ratings);
			
			URLBuilder ubu = renderer.getUrlBuilder().createCopyFor(cmp);
			cmp.getHTMLRendererSingleton().render(renderer, sb, cmp, ubu, translator, null, null);
		}
	}

	
	
	
}
