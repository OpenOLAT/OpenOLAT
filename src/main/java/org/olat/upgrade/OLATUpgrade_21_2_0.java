/**
 * <a href="https://www.openolat.org">
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
 * frentix GmbH, https://www.frentix.com
 * <p>
 */
package org.olat.upgrade;

import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import org.apache.logging.log4j.Logger;
import org.olat.core.commons.persistence.DB;
import org.olat.core.commons.services.color.ColorService;
import org.olat.core.commons.services.color.ColorValidator;
import org.olat.core.commons.services.color.Swatch;
import org.olat.core.logging.Tracing;
import org.olat.core.util.StringHelper;
import org.olat.course.assessment.manager.UserCourseInformationsManager;
import org.olat.course.assessment.model.UserCourseInfosImpl;
import org.olat.course.style.ColorCategory;
import org.olat.user.UserImpl;
import org.springframework.beans.factory.annotation.Autowired;

/**
 * 
 * Initial date: 7 août 2026<br>
 * @author srosse, stephane.rosse@frentix.com, https://www.frentix.com
 *
 */
public class OLATUpgrade_21_2_0 extends OLATUpgrade {
	
	private static final Logger log = Tracing.createLoggerFor(OLATUpgrade_21_2_0.class);

	private static final int BATCH_SIZE = 5000;
	
	private static final String VERSION = "OLAT_21.2.0";
	
	private static final String INITIALIZE_USER_COURSE_ACTIVITY = "INITIALIZE USER COURSE ACTIVITY";
	private static final String MIGRATE_COLOR_VALUES = "MIGRATE COLOR VALUES";
	private static final String MIGRATE_COLOR_CATEGORIES = "MIGRATE COLOR CATEGORIES";
	
	/*
	 * The colour values of the 4 columns that stored a CSS class of an old
	 * colour chooser: old class (lower case) -> key of the colour set M, the
	 * solid swatch of the hue of the old key (OO-9993). New code stores the
	 * bare key in these columns, e.g. "blue".
	 */
	
	/** Calendar colours, column o_cal_use_config.c_cssclass */
	static final Map<String,String> CALENDAR_COLOR_UPGRADES = upgrades("o_cal_",
			"grey", "gray",
			"yellow", "yellow",
			"orange", "orange",
			"red", "red",
			"maroon", "red",
			"blue", "blue",
			"navy", "blue",
			"fuchsia", "magenta",
			"rebeccapurple", "violet",
			"lime", "lime",
			"green", "green",
			"olive", "lime");
	
	/** Colours of the project avatars and of the user initials, without prefix */
	private static final String[] AVATAR_COLORS = {
			"dark_blue", "blue",
			"light_blue", "cyan",
			"purple", "magenta",
			"red", "red",
			"orange", "orange",
			"yellow", "yellow",
			"light_green", "lime",
			"dark_green", "green" };
	
	/** Project avatars, column o_proj_project.p_avatar_css_class */
	static final Map<String,String> PROJECT_AVATAR_COLOR_UPGRADES = upgrades("o_proj_avatar_", AVATAR_COLORS);
	
	/** User initials, column o_user.u_initials_css_class */
	static final Map<String,String> USER_INITIALS_COLOR_UPGRADES = upgrades("o_user_initials_", AVATAR_COLORS);
	
	/** Buildings, column o_rm_building.r_color_css: the keys of the old color.list */
	static final Map<String,String> BUILDING_COLOR_UPGRADES = upgrades("o_color_",
			"lightgray", "gray",
			"yellow", "yellow",
			"orange", "orange",
			"brown", "brown",
			"red", "red",
			"orchid", "pink",
			"purple", "violet",
			"lightblue", "cyan",
			"cobaltblue", "blue",
			"darkblue", "blue",
			"lightgreen", "lime",
			"seagreen", "green");
	
	/** The columns of the colour upgrade, values without prefix, e.g. "blue" */
	static final List<ColorColumn> COLOR_COLUMNS = List.of(
			new ColorColumn("o_cal_use_config.c_cssclass", "caluserconfig", "cssClass", "", CALENDAR_COLOR_UPGRADES, Set.of()),
			new ColorColumn("o_proj_project.p_avatar_css_class", "projproject", "avatarCssClass", "", PROJECT_AVATAR_COLOR_UPGRADES, Set.of()),
			// the grey initials are no stored colour, but the class exists: no warning
			new ColorColumn("o_user.u_initials_css_class", UserImpl.class.getName(), "initialsCssClass", "", USER_INITIALS_COLOR_UPGRADES,
					Set.of("o_user_initials_grey")),
			new ColorColumn("o_rm_building.r_color_css", "rmbuilding", "colorCss", "", BUILDING_COLOR_UPGRADES, Set.of()));
	
	/**
	 * The course colour categories, column o_course_color_category.c_css_class:
	 * the classes of the 8 predefined categories -> the swatch class of the
	 * solid swatch of their hue, the class the colour chooser of the
	 * administration stores, e.g. "o_swatch_blue". Classes an administrator
	 * entered by hand stay unchanged. The courses store the identifier of the
	 * category, not the class: no course file changes.
	 */
	static final Map<String,String> COLOR_CATEGORY_UPGRADES = upgrades("o_colcat_",
			"dark_blue", ColorService.SWATCH_CSS_CLASS_PREFIX + "blue",
			"light_blue", ColorService.SWATCH_CSS_CLASS_PREFIX + "cyan",
			"purple", ColorService.SWATCH_CSS_CLASS_PREFIX + "magenta",
			"red", ColorService.SWATCH_CSS_CLASS_PREFIX + "red",
			"orange", ColorService.SWATCH_CSS_CLASS_PREFIX + "orange",
			"yellow", ColorService.SWATCH_CSS_CLASS_PREFIX + "yellow",
			"light_green", ColorService.SWATCH_CSS_CLASS_PREFIX + "lime",
			"dark_green", ColorService.SWATCH_CSS_CLASS_PREFIX + "green");
	
	/** The column of the colour categories, values with the swatch prefix, e.g. "o_swatch_blue" */
	static final List<ColorColumn> COLOR_CATEGORY_COLUMNS = List.of(
			// the classes of the technical categories are no colour: no warning
			new ColorColumn("o_course_color_category.c_css_class", "coursecolorcategory", "cssClass",
					ColorService.SWATCH_CSS_CLASS_PREFIX, COLOR_CATEGORY_UPGRADES,
					Set.of(ColorCategory.CSS_NO_COLOR, "o_colcat_inherited", "o_colcat_course")));


	@Autowired
	private DB dbInstance;
	@Autowired
	private UserCourseInformationsManager userCourseInformationsManager;

	@Override
	public String getVersion() {
		return VERSION;
	}

	@Override
	public boolean doPostSystemInitUpgrade(UpgradeManager upgradeManager) {
		UpgradeHistoryData uhd = upgradeManager.getUpgradesHistory(VERSION);
		if (uhd == null) {
			uhd = new UpgradeHistoryData();
		} else if (uhd.isInstallationComplete()) {
			return false;
		}

		boolean allOk = true;
		allOk &= initializeUserCourseActivity(upgradeManager, uhd);
		allOk &= migrateColorValues(upgradeManager, uhd, MIGRATE_COLOR_VALUES, COLOR_COLUMNS);
		allOk &= migrateColorValues(upgradeManager, uhd, MIGRATE_COLOR_CATEGORIES, COLOR_CATEGORY_COLUMNS);

		//uhd.setInstallationComplete(allOk);
		upgradeManager.setUpgradesHistory(uhd, VERSION);

		if (allOk) {
			log.info(Tracing.M_AUDIT, "Finished OLATUpgrade_21_2_0 successfully!");
		} else {
			log.info(Tracing.M_AUDIT, "OLATUpgrade_21_2_0 not finished, try to restart OpenOlat!");
		}
		return allOk;
	}
	
	private boolean initializeUserCourseActivity(UpgradeManager upgradeManager, UpgradeHistoryData uhd) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(INITIALIZE_USER_COURSE_ACTIVITY)) {
			try {
				log.info("Start initializing users courses activity");

				int count = 0;
				Long lastKey = Long.valueOf(0l);
				List<UserCourseInfosImpl> userCourseInfosList = getUsersCoursesInformations(lastKey, BATCH_SIZE);
				while (!userCourseInfosList.isEmpty()) {
					for (UserCourseInfosImpl userCourseInfos : userCourseInfosList) {
						lastKey = userCourseInfos.getKey();
						int visit = Math.min(20, userCourseInfos.getVisit());
						Date date = userCourseInfos.getRecentLaunch() == null
								? userCourseInfos.getCreationDate()
								: userCourseInfos.getRecentLaunch();
						double activity = userCourseInformationsManager.calculateActivity(visit, date);
						if(activity != userCourseInfos.getActivity()) {
							userCourseInfos.setActivity(activity);
							dbInstance.getCurrentEntityManager().merge(userCourseInfos);
						}
						if (count++ % 25 == 0) {
							dbInstance.commitAndCloseSession();
						}
					}
					dbInstance.commitAndCloseSession();
					userCourseInfosList = getUsersCoursesInformations(lastKey, BATCH_SIZE);
				}

				log.info("End initializing users courses activity: {}", count);
			} catch (Exception e) {
				log.error("", e);
				allOk = false;
			}
			uhd.setBooleanDataValue(INITIALIZE_USER_COURSE_ACTIVITY, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}

	/**
	 * Replaces the old colour classes of the columns with the key of the colour
	 * set M, e.g. "o_proj_avatar_light_blue" with "cyan", or with its swatch
	 * class, e.g. "o_colcat_light_blue" with "o_swatch_cyan". One update per
	 * old value, case insensitive. NULL, empty values, keys of the chooser sets
	 * and unknown values stay unchanged, an unknown value is logged once with
	 * its number of rows. The updates are idempotent: a failed column runs
	 * again at the next start.
	 * 
	 * @param taskKey The key of the task in the upgrade history
	 * @param columns The columns of the task
	 */
	private boolean migrateColorValues(UpgradeManager upgradeManager, UpgradeHistoryData uhd,
			String taskKey, List<ColorColumn> columns) {
		boolean allOk = true;
		if (!uhd.getBooleanDataValue(taskKey)) {
			log.info("Start migration of the colour values: {}", taskKey);
			for (ColorColumn column : columns) {
				try {
					migrateColorValues(column);
					dbInstance.commitAndCloseSession();
				} catch (Exception e) {
					log.error("Migration of the colour values of {} failed", column.name(), e);
					dbInstance.rollbackAndCloseSession();
					allOk = false;
				}
			}
			log.info("End migration of the colour values: {}", taskKey);
			uhd.setBooleanDataValue(taskKey, allOk);
			upgradeManager.setUpgradesHistory(uhd, VERSION);
		}
		return allOk;
	}
	
	private void migrateColorValues(ColorColumn column) {
		String update = "update " + column.entity() + " as c set c." + column.field() + "=:newValue"
				+ " where lower(c." + column.field() + ")=:oldValue";
		int total = 0;
		for (Map.Entry<String,String> upgrade : column.upgrades().entrySet()) {
			int rows = dbInstance.getCurrentEntityManager()
					.createQuery(update)
					.setParameter("newValue", upgrade.getValue())
					.setParameter("oldValue", upgrade.getKey())
					.executeUpdate();
			if (rows > 0) {
				log.info("Colour values of {}: {} -> {} in {} rows", column.name(), upgrade.getKey(), upgrade.getValue(), rows);
			}
			total += rows;
		}
		log.info("Colour values of {}: {} rows updated", column.name(), total);
		
		String values = "select c." + column.field() + ", count(c.key) from " + column.entity() + " as c"
				+ " group by c." + column.field();
		List<Object[]> valueCounts = dbInstance.getCurrentEntityManager()
				.createQuery(values, Object[].class)
				.getResultList();
		for (Object[] valueCount : valueCounts) {
			String value = (String)valueCount[0];
			if (isUnknownColorValue(value, column)) {
				log.warn("Colour values of {}: unknown value \"{}\" in {} rows, unchanged",
						column.name(), ColorValidator.toLogValue(value), valueCount[1]);
			}
		}
	}
	
	/**
	 * @param value A value of the column
	 * @param column The column
	 * @return false for NULL, an empty value, a key of the chooser sets with
	 * 		the swatch prefix of the column (e.g. "blue", "theme2", "blue-light",
	 * 		or "o_swatch_blue", "o_swatch_theme1"), an old value of the upgrade
	 * 		and a value the column keeps; else true
	 */
	static boolean isUnknownColorValue(String value, ColorColumn column) {
		if (!StringHelper.containsNonWhitespace(value)) {
			return false;
		}
		return !isSwatchValue(value, column.swatchPrefix())
				&& !column.upgrades().containsKey(value.toLowerCase(Locale.ROOT))
				&& !column.keep().contains(value);
	}
	
	private static boolean isSwatchValue(String value, String swatchPrefix) {
		return value.startsWith(swatchPrefix)
				&& Swatch.fromKey(value.substring(swatchPrefix.length())) != null;
	}
	
	private static Map<String,String> upgrades(String prefix, String... oldKeyNewKey) {
		Map<String,String> upgrades = new LinkedHashMap<>();
		for (int i = 0; i < oldKeyNewKey.length; i += 2) {
			upgrades.put(prefix + oldKeyNewKey[i], oldKeyNewKey[i + 1]);
		}
		return Collections.unmodifiableMap(upgrades);
	}
	
	/**
	 * @param name The table and the column, for the log
	 * @param entity The JPA entity
	 * @param field The field of the column
	 * @param swatchPrefix The prefix of a key of the chooser sets in the column,
	 * 		"" for the bare key, "o_swatch_" for the swatch class
	 * @param upgrades Old value (lower case) -> new value
	 * @param keep Values that are no colour of the upgrade, but valid
	 */
	record ColorColumn(String name, String entity, String field, String swatchPrefix,
			Map<String,String> upgrades, Set<String> keep) {
		//
	}

	private List<UserCourseInfosImpl> getUsersCoursesInformations(Long lastKey, int maxResults) {
		String sb = """
			select infos from usercourseinfos as infos
			where infos.key>:lastKey
			order by infos.key""";

		return dbInstance.getCurrentEntityManager()
				.createQuery(sb, UserCourseInfosImpl.class)
				.setParameter("lastKey", lastKey)
				.setFirstResult(0)
				.setMaxResults(maxResults)
				.getResultList();
	}
}
