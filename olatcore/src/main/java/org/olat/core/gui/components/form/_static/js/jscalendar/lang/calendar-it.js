// ** I18N
Calendar._DN = new Array
("Domenica",
 "Luned�",
 "Marted�",
 "Mercoled�",
 "Gioved�",
 "Venerd�",
 "Sabato",
 "Domenica");
Calendar._MN = new Array
("Gennaio",
 "Febbraio",
 "Marzo",
 "Aprile",
 "Maggio",
 "Giugno",
 "Luglio",
 "Agosto",
 "Settembre",
 "Ottobre",
 "Novembre",
 "Dicembre");

// short month names
Calendar._SMN = new Array
("Gen",
 "Feb",
 "Mar",
 "Apr",
 "Mag",
 "Giu",
 "Lug",
 "Ago",
 "Set",
 "Ott",
 "Nov",
 "Dic");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "a proposito del calendario";

Calendar._TT["ABOUT"] =
"DHTML Date/Time Selector\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"Per le ultime versioni vai a: http://dynarch.com/mishoo/calendar.epl\n" +
"Distribuito su licenza GNU LGPL.  Vedi http://gnu.org/licenses/lgpl.html per i dettagli." +
"\n\n" +
"selezione della data:\n" +
"- Usa i bottoni \xab, \xbb per selezionare l'anno\n" +
"- Usa i bottoni " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " per selezionare il mese\n" +
"- Utilizza il mouse per una selezione rapida.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"selezione dell'ora:\n" +
"- Clicca sull'ora visualizzata per aumentarla\n" +
"- o Shift-click per diminuirla\n" +
"- o click a trascina per la selezione rapida.";


Calendar._TT["TOGGLE"] = "Modifica il primo giorno della settimana";
Calendar._TT["PREV_YEAR"] = "Anno prec. (tieni premuto per menu)";
Calendar._TT["PREV_MONTH"] = "Mese prec. (tieni premuto per menu)";
Calendar._TT["GO_TODAY"] = "Vai a oggi";
Calendar._TT["NEXT_MONTH"] = "Mese succ. (tieni premuto per menu)";
Calendar._TT["NEXT_YEAR"] = "Anno succ. (tieni premuto per menu)";
Calendar._TT["SEL_DATE"] = "Seleziona data";
Calendar._TT["DRAG_TO_MOVE"] = "Trascina per spostare";
Calendar._TT["PART_TODAY"] = " (oggi)";
Calendar._TT["MON_FIRST"] = "Parti da luned�";
Calendar._TT["SUN_FIRST"] = "Parti da domenica";
Calendar._TT["CLOSE"] = "Chiudi";
Calendar._TT["TODAY"] = "Oggi";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "%s als Wochenbeginn";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["TIME_PART"] = "(Umschalt-)Klick oder ziehen";


// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%d-%m-%Y";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %e %b ";

Calendar._TT["WK"] = "Setti";
