// ** I18N


// full day names
Calendar._DN = new Array
("S�ndag",
 "Mandag",
 "Tirsdag",
 "Onsdag",
 "Torsdag",
 "Fredag",
 "L�rdag",
 "S�ndag");

// short day names
Calendar._SDN = new Array
("Sun",
 "Mon",
 "Tue",
 "Wed",
 "Thu",
 "Fri",
 "Sat",
 "Sun");

// full month names
Calendar._MN = new Array
("January",
 "Februar",
 "Marts",
 "April",
 "Maj",
 "Juni",
 "Juli",
 "August",
 "September",
 "Oktober",
 "November",
 "December");

// short month names
Calendar._SMN = new Array
("Jan",
 "Feb",
 "Mar",
 "Apr",
 "May",
 "Jun",
 "Jul",
 "Aug",
 "Sep",
 "Oct",
 "Nov",
 "Dec");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Om Kalenderen";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"For den seneste version bes�g: http://dynarch.com/mishoo/calendar.epl\n" +
"Distribueret under GNU LGPL.  Se http://gnu.org/licenses/lgpl.html for detajler." +
"\n\n" +
"Valg af dato:\n" +
"- Brug \xab, \xbb knapperne for at v�lge �r\n" +
"- Brug " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " knapperne for at v�lge m�ned\n" +
"- Hold knappen p� musen nede p� knapperne ovenfor for hurtigere valg.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Valg af tid:\n" +
"- Klik p� en vilk�rlig del for st�rre v�rdi\n" +
"- eller Shift-klik for for mindre v�rdi\n" +
"- eller klik og tr�k for hurtigere valg.";

Calendar._TT["TOGGLE"] = "Skift f�rste ugedag";
Calendar._TT["PREV_YEAR"] = "�t �r tilbage (hold for menu)";
Calendar._TT["PREV_MONTH"] = "�n m�ned tilbage (hold for menu)";
Calendar._TT["GO_TODAY"] = "G� til i dag";
Calendar._TT["NEXT_MONTH"] = "�n m�ned frem (hold for menu)";
Calendar._TT["NEXT_YEAR"] = "�t �r frem (hold for menu)";
Calendar._TT["SEL_DATE"] = "V�lg dag";
Calendar._TT["DRAG_TO_MOVE"] = "Tr�k vinduet";
Calendar._TT["PART_TODAY"] = " (i dag)";
Calendar._TT["MON_FIRST"] = "Vis mandag f�rst";
Calendar._TT["SUN_FIRST"] = "Vis s�ndag f�rst";
Calendar._TT["CLOSE"] = "Luk vinduet";
Calendar._TT["TODAY"] = "I dag";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Display %s first";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["TIME_PART"] = "(Umschalt-)Klick oder ziehen";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "dd-mm-yy";
Calendar._TT["TT_DATE_FORMAT"] = "%d. %b, %Y";

Calendar._TT["WK"] = "wk";
Calendar._TT["TIME"] = "Time:";
