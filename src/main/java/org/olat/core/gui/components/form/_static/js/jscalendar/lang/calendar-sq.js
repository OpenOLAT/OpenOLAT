// ** I18N

// Calendar EN language
// Author: Mihai Bazon, <mishoo@infoiasi.ro>
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("E diel",
 "E hënë",
 "E martë",
 "E mërkurë",
 "E enjte",
 "E premte",
 "E shtunë",
 "E diel");

// Please note that the following array of short day names (and the same goes
// for short month names, _SMN) isn't absolutely necessary.  We give it here
// for exemplification on how one can customize the short day names, but if
// they are simply the first N letters of the full name you can simply say:
//
//   Calendar._SDN_len = N; // short day name length
//   Calendar._SMN_len = N; // short month name length
//
// If N = 3 then this is not needed either since we assume a value of 3 if not
// present, to be compatible with translation files that were written before
// this feature.

// short day names
Calendar._SDN = new Array
("Die",
 "Hën",
 "Mar",
 "Mër",
 "Enj",
 "Pre",
 "Sht",
 "Die");

// full month names
Calendar._MN = new Array
("Janar",
 "Shkurt",
 "Mars",
 "Prill",
 "Maj",
 "Qershor",
 "Korrik",
 "Gusht",
 "Shtator",
 "Tetor",
 "Nëntor",
 "Dhjetor");

// short month names
Calendar._SMN = new Array
("Jan",
 "Shk",
 "Mar",
 "Pri",
 "Maj",
 "Qer",
 "Kor",
 "Gst",
 "Sht",
 "Tet",
 "Nën",
 "Dhj");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Për kalendarin";

Calendar._TT["ABOUT"] =
"DHTML Përzgjedhësi Data/Ora\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"Për versionet e fundit vizito: http://dynarch.com/mishoo/calendar.epl\n" +
"Shpërndrarë nën GNU LGPL.  Shih http://gnu.org/licenses/lgpl.html për detajet." +
"\n\n" +
"Përzgjedhja e datës:\n" +
"- Përdor pullat \xab, \xbb për të përzgjedhur vitin\n" +
"- Përdor pullat " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " për të përzgjedhur muajt\n" +
"- Mbaje të shtypur pullën e miut mbi cilëndo pullë për një përzgjedhje më të shpejtë.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Përzgjedhja e orës:\n" +
"- Kliko në cilëdo pjesë të pjesëve të orës për të shtuar atë\n" +
"- ose Shift-klik për të zbritur atë \n" +
"- ose klik dhe tërheq për përzgjedhje më të shpejtë.";

Calendar._TT["PREV_YEAR"] = "Viti paraprak (mbaj për menynë)";
Calendar._TT["PREV_MONTH"] = "Muaji paraprak (mbaj për menynë)";
Calendar._TT["GO_TODAY"] = "Dita e sotme";
Calendar._TT["NEXT_MONTH"] = "Muaju pasues (mbaj për menynë)";
Calendar._TT["NEXT_YEAR"] = "Viti pasues (mbaj për menynë)";
Calendar._TT["SEL_DATE"] = "Përzgjedh datë";
Calendar._TT["DRAG_TO_MOVE"] = "Tërheq për të zhvendosur";
Calendar._TT["PART_TODAY"] = " (sot)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Shfaq %s të parën";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "Mbyll";
Calendar._TT["TODAY"] = "Sot";
Calendar._TT["TIME_PART"] = "(Shift-)Klik ose tërheq për të ndërruar vlerën";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "wk";
Calendar._TT["TIME"] = "kohë:";
