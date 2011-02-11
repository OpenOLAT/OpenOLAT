/**
* OLAT - Online Learning and Training<br>
* http://www.olat.org
* <p>
* Licensed under the Apache License, Version 2.0 (the "License"); <br>
* you may not use this file except in compliance with the License.<br>
* You may obtain a copy of the License at
* <p>
* http://www.apache.org/licenses/LICENSE-2.0
* <p>
* Unless required by applicable law or agreed to in writing,<br>
* software distributed under the License is distributed on an "AS IS" BASIS, <br>
* WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. <br>
* See the License for the specific language governing permissions and <br>
* limitations under the License.
* <p>
* Copyright (c) frentix GmbH<br>
* http://www.frentix.com<br>
* <p>
*/ 

package org.olat.core.commons.contextHelp;

import java.io.File;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import org.olat.core.gui.UserRequest;
import org.olat.core.gui.control.Event;
import org.olat.core.id.OLATResourceable;
import org.olat.core.manager.BasicManager;
import org.olat.core.util.WebappHelper;
import org.olat.core.util.coordinate.CoordinatorManager;
import org.olat.core.util.coordinate.SyncerExecutor;
import org.olat.core.util.event.GenericEventListener;
import org.olat.core.util.prefs.Preferences;
import org.olat.core.util.resource.OresHelper;
import org.olat.core.util.xml.XStreamHelper;

/**
 * <h3>Description:</h3> This manager offers methods to deal with the context
 * help. This version of the manager stores file in the filesystem because
 * brasato has not yet database stuff in it.
 * 
 * <h3>Events thrown by this manager:</h3>
 * <ul>
 * <li>ContextHelpRatingEvent: communicate new user ratings between cluster
 * nodes via CoordinatorManager</li>
 * </ul>
 * 
 * <p>
 * Initial Date: 31.10.2008 <br>
 * 
 * @author Florian Gnaegi, frentix GmbH, http://www.frentix.com
 */

public class ContextHelpManager extends BasicManager implements GenericEventListener {
	private static ContextHelpManager INSTANCE;
	private static final String CONTEXT_HELP_RATING_XML = "context_help_rating.xml";
	private static final String SYSTEM_DIR = "system";
	private static final String GUI_PREFS_PREFIX = "page_rating_";
	private static final OLATResourceable contextHelpRatingEventBus = OresHelper.createOLATResourceableType("contextHelpRatingEventBus");
	private static File contextHelpRatingFile;	
	private static Map<String, Object[]> contextHelpRatings;
	private CoordinatorManager coordinatorManager;

	/**
	 * Get an instance of the manager
	 * @return
	 * @deprecated
	 */
	public static ContextHelpManager getInstance() {
		return INSTANCE;
	}


	/**
	 * [spring]
	 */
	private ContextHelpManager(CoordinatorManager coordinatorManager) {
		// Do in sync with other VM's
		this.coordinatorManager = coordinatorManager;
		coordinatorManager.getCoordinator().getEventBus().registerFor(INSTANCE, null, contextHelpRatingEventBus);
		coordinatorManager.getCoordinator().getSyncer().doInSync(contextHelpRatingEventBus,new SyncerExecutor(){
			public void execute() {
			// Load statistics from disk (no database access in core)		
			File systemDir = new File(WebappHelper.getUserDataRoot(), SYSTEM_DIR);
			contextHelpRatingFile = new File(systemDir, CONTEXT_HELP_RATING_XML);
			
			if (contextHelpRatingFile.exists()) {
				contextHelpRatings = (Map<String, Object[]>) XStreamHelper.readObject(contextHelpRatingFile);
			} else {
				contextHelpRatings = new HashMap<String, Object[]>();
			}
		}});
		INSTANCE = this;
	}
	

	/**
	 * Store the new page rating somewhere
	 * @param ureq
	 * @param locale
	 * @param bundleName
	 * @param page
	 * @param rating
	 * 
	 * @deprecated - use the CommentAndRatingService instead. Still here in case we want to migrate the ratings
	 */
	@Deprecated 
	public void storePageRating(final UserRequest ureq, final Locale locale, final String bundleName, final String page, final float rating) {
		final String key = calculateCombinedKey(locale, bundleName, page);
		final Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		// 1) Update community rating
		coordinatorManager.getCoordinator().getSyncer().doInSync(contextHelpRatingEventBus,new SyncerExecutor(){
			public void execute() {
				Object[] statsValues = contextHelpRatings.get(key);
				if (statsValues == null) {
					// create new data object for this page
					statsValues = new Object[2];
					statsValues[0] = new Double(rating);
					statsValues[1] = new Integer(1);
				} else {
					Float lastRating = (Float) guiPrefs.get(ContextHelpModule.class, GUI_PREFS_PREFIX + key);
					// update data object for this page
					Double cummulatedRatings = (Double) statsValues[0];
					double newValue = cummulatedRatings.doubleValue() + rating - (lastRating == null ? 0 : lastRating.doubleValue());
					statsValues[0] = new Double(newValue > 0 ? newValue : 0);
					Integer numberOfRatings = (Integer) statsValues[1];			
					int newRatingCount = numberOfRatings.intValue() + (lastRating == null ? 1 : 0);
					statsValues[1] = new Integer(newRatingCount);
				}
				// notify everybody about this change (including ourselfs) to update  local context help ratings
				ContextHelpRatingEvent ratingEvent = new ContextHelpRatingEvent(key, statsValues);
				coordinatorManager.getCoordinator().getEventBus().fireEventToListenersOf(ratingEvent, contextHelpRatingEventBus);
				// now save in filesystem
				XStreamHelper.writeObject(contextHelpRatingFile, contextHelpRatings);
			}
		});		
		// 2) Update user rating
		guiPrefs.putAndSave(ContextHelpModule.class, GUI_PREFS_PREFIX + key, new Float(rating));
	}

	/**
	 * Get the ranking of the given page for the average community rating
	 * @param locale
	 * @param bundleName
	 * @param page
	 * @return the average ranking (simple average)
	 * 
	 * @deprecated - use the CommentAndRatingService instead. Still here in case we want to migrate the ratings
	 */
	@Deprecated 
	public float getCommunityPageRanking(Locale locale, String bundleName, String page) {
		String key = calculateCombinedKey(locale, bundleName, page);
		Object[] statsValues = contextHelpRatings.get(key);
		if (statsValues == null) {
			return 0f;
		} else {
			Double cummulatedRatings = (Double) statsValues[0];
			Integer numberOfRatings = (Integer) statsValues[1];				
			int ratings = numberOfRatings.intValue();
			if (ratings == 0) return 0;
			return (cummulatedRatings.floatValue() / ratings);
		}
	}
	
	/**
	 * Get the ranking of the given page for a single user
	 * @param ureq
	 * @param locale
	 * @param bundleName
	 * @param page
	 * @return the average ranking (simple average)
	 * 
	 * @deprecated - use the CommentAndRatingService instead. Still here in case we want to migrate the ratings
	 */
	@Deprecated 
	public float getPersonalPageRanking(UserRequest ureq, Locale locale, String bundleName, String page) {
		String key = calculateCombinedKey(locale, bundleName, page);
		Preferences guiPrefs = ureq.getUserSession().getGuiPreferences();
		Float lastRating = (Float) guiPrefs.get(ContextHelpModule.class, GUI_PREFS_PREFIX + key);
		if (lastRating == null) return 0;
		return lastRating.floatValue();
	}

	/**
	 * Get the number of ratings that have been made so far
	 * @param locale
	 * @param bundleName
	 * @param page
	 * @return
	 * 
	 * @deprecated - use the CommentAndRatingService instead. Still here in case we want to migrate the ratings
	 */
	@Deprecated 
	public int countCommunityRatings(Locale locale, String bundleName, String page) {
		String key = calculateCombinedKey(locale, bundleName, page);
		Object[] statsValues = contextHelpRatings.get(key);
		if (statsValues == null) {
			return 0;
		} else {
			Integer numberOfRatings = (Integer) statsValues[1];				
			return numberOfRatings.intValue();
		}
	}
	
	/**
	 * Method to calculate a key that identifies a help page uniquely
	 * @param locale
	 * @param bundleName
	 * @param page
	 * @return
	 */
	public String calculateCombinedKey(Locale locale, String bundleName, String page) {
		return locale.toString() + ":" + bundleName + ":" + page;
	}

	/**
	 * @see org.olat.core.util.event.GenericEventListener#event(org.olat.core.gui.control.Event)
	 */
	public void event(Event event) {
		if (event instanceof ContextHelpRatingEvent) {
			ContextHelpRatingEvent ratingEvent = (ContextHelpRatingEvent) event;
			// Just set new total values, no need to synchronize
			contextHelpRatings.put(ratingEvent.getKey(), ratingEvent.getRatingValues());
		}
	}

}
