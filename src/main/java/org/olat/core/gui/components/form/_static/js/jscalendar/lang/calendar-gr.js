// ** I18N

// Calendar GR language
// Author: Mihai Bazon, <mishoo@infoiasi.ro>
// Encoding: any
// Distributed under the same terms as the calendar itself.

// For translators: please use UTF-8 if possible.  We strongly believe that
// Unicode is the answer to a real internationalized world.  Also please
// include your contact information in the header, as can be seen above.

// full day names
Calendar._DN = new Array
("Κυριακή",
 "Δευτέρα",
 "Τρίτη",
 "Τετάρτη",
 "Πέμπτη",
 "Παρασκευή",
 "Σάββατο",
 "Κυριακή");

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
("ΚΥ",
 "ΔΕ",
 "ΤΡ",
 "ΤΕ",
 "ΠΕ",
 "ΠΑ",
 "ΣΑ",
 "ΚΥ");

// full month names
Calendar._MN = new Array
("Ιανουάριος",
 "Φεβρουάριος",
 "Μάρτιος",
 "Απρίλιος",
 "Μάιος",
 "Ιούνιος",
 "Ιούλιος",
 "Αύγουστος",
 "Σεπτέμβριος",
 "Οκτώβριος",
 "Νοέμβριος",
 "Δεκέμβριος");

// short month names
Calendar._SMN = new Array
("Ιαν",
 "Φεβ",
 "Μαρ",
 "Απρ",
 "Μαϊ",
 "Ιουν",
 "Ιουλ",
 "Αυγ",
 "Σεπ",
 "Οκτ",
 "Νοε",
 "Δεκ");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "Σχετικά με το ημερολόγιο";

Calendar._TT["ABOUT"] =
"DHTML Επιλογέας Ημερομηνίας/Ώρας\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"Για την τελευταία έκδοση επισκευθείτε: http://dynarch.com/mishoo/calendar.epl\n" +
"Διανέμεται με άδεια GNU LGPL.  Βλέπε http://gnu.org/licenses/lgpl.html for details." +
"\n\n" +
"Επιλογή ημερομηνίας:\n" +
"- Χρησιμοποιήστε τα κουμπιά \xab, \xbb για να επιλέξετε έτος\n" +
"- Χρησιμοποιήστε τα κουμπιά " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " για να επιλέξετε μήνα\n" +
"- Κρατήστε το κουμπί του ποντικιού σε κάποιο από τα παραπάνω κουμπιά για γρήγορη επιλογή.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Επιλογή ώρας:\n" +
"- Κάντε κλικ σε οποιοδήποτε από τα μέρη της ώρας για να το αυξήσετε\n" +
"- ή Shift-κλικ για να το αυξήσετε\n" +
"- ή κάντε κλικ και σύρετε για γρηγορότερη επιλογή.";

Calendar._TT["PREV_YEAR"] = "Προηγ. έτος (κρατήστε για μενού)";
Calendar._TT["PREV_MONTH"] = "Προηγ. μήνας (κρατήστε για μενού)";
Calendar._TT["GO_TODAY"] = "Σήμερα";
Calendar._TT["NEXT_MONTH"] = "Επόμ. μήνας (κρατήστε για μενού)";
Calendar._TT["NEXT_YEAR"] = "Επόμ. έτος (κρατήστε για μενού)";
Calendar._TT["SEL_DATE"] = "Επιλογή ημερομηνίας";
Calendar._TT["DRAG_TO_MOVE"] = "Σύρετε για να μετακινήσετε";
Calendar._TT["PART_TODAY"] = " (σήμερα)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "Πρώτη μέρα: %s";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "Κλείσιμο";
Calendar._TT["TODAY"] = "Σήμερα";
Calendar._TT["TIME_PART"] = "Κάντε (Shift-)Κλικ ή σύρετε για να αλλάξετε την τιμή";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "εβδ";
Calendar._TT["TIME"] = "Ώρα:";
