// get olat translator for calendar package
var trans = jQuery(document).ooTranslator().getTranslator(o_info.locale, 'org.olat.core.gui.components.form._static.js.jscalendar');

// short day names
Calendar._SDN = new Array(
trans.translate('day.short.so'),
trans.translate('day.short.mo'),
trans.translate('day.short.di'),
trans.translate('day.short.mi'),
trans.translate('day.short.do'),
trans.translate('day.short.fr'),
trans.translate('day.short.sa')
);

// full day names
Calendar._DN = new Array(
trans.translate('day.long.so'),
trans.translate('day.long.mo'),
trans.translate('day.long.di'),
trans.translate('day.long.mi'),
trans.translate('day.long.do'),
trans.translate('day.long.fr'),
trans.translate('day.long.sa')
);

// short day names only use 2 letters instead of 3
Calendar._SDN_len = 2;

// full month names
Calendar._MN = new Array(
trans.translate('month.long.jan'),
trans.translate('month.long.feb'),
trans.translate('month.long.mar'),
trans.translate('month.long.apr'),
trans.translate('month.long.mai'),
trans.translate('month.long.jun'),
trans.translate('month.long.jul'),
trans.translate('month.long.aug'),
trans.translate('month.long.sep'),
trans.translate('month.long.oct'),
trans.translate('month.long.nov'),
trans.translate('month.long.dec')
);

// short month names
Calendar._SMN = new Array(
trans.translate('month.short.jan'),
trans.translate('month.short.feb'),
trans.translate('month.short.mar'),
trans.translate('month.short.apr'),
trans.translate('month.short.mai'),
trans.translate('month.short.jun'),
trans.translate('month.short.jul'),
trans.translate('month.short.aug'),
trans.translate('month.short.sept'),
trans.translate('month.short.oct'),
trans.translate('month.short.nov'),
trans.translate('month.short.dec')
);

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = trans.translate('info');

Calendar._TT["ABOUT"] = 
trans.translate('about.title') +
"\n(c) dynarch.com 2002-2003\n" +
trans.translate('about.content');

Calendar._TT["ABOUT_TIME"] = trans.translate('about.time');

Calendar._TT["TOGGLE"] = trans.translate('toggle');
Calendar._TT["PREV_YEAR"] = trans.translate('previous.year');
Calendar._TT["PREV_MONTH"] = trans.translate('previous.month');
Calendar._TT["GO_TODAY"] = trans.translate('today');
Calendar._TT["NEXT_MONTH"] = trans.translate('next.month');
Calendar._TT["NEXT_YEAR"] = trans.translate('next.year');
Calendar._TT["SEL_DATE"] = trans.translate('select.date');
Calendar._TT["DRAG_TO_MOVE"] = trans.translate('drag.to.move');
Calendar._TT["PART_TODAY"] = " (" + trans.translate('today') + ")";
Calendar._TT["MON_FIRST"] = trans.translate('monday.first');
Calendar._TT["SUN_FIRST"] = trans.translate('sunday.first');
Calendar._TT["CLOSE"] = trans.translate('close');
Calendar._TT["TODAY"] = trans.translate('today');

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "%s " + trans.translate('day.first');

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = trans.translate('weekend.days');

Calendar._TT["TIME_PART"] = trans.translate('time.part');

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = trans.translate('formate.date');
Calendar._TT["TT_DATE_FORMAT"] = trans.translate('choose.date');

Calendar._TT["WK"] = trans.translate('calendar.week');
