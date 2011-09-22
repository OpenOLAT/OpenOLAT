// ** I18N
Calendar._DN = new Array
("Dimanche",
 "Lundi",
 "Mardi",
 "Mercredi",
 "Jeudi",
 "Vendredi",
 "Samedi",
 "Dimanche");
Calendar._MN = new Array
("Janvier",
 "F\u00e9vrier",
 "Mars",
 "Avril",
 "Mai",
 "Juin",
 "Juillet",
 "Ao\u00fbt",
 "Septembre",
 "Octobre",
 "Novembre",
 "D\u00e9cembre");
Calendar._SDN = new Array
("Lun",
 "Mar",
 "Mer",
 "Jeu",
 "Thu",
 "Ven",
 "Sam",
 "Dim");
Calendar._SMN = new Array
("Jan",
 "Fev",
 "Mar",
 "Avr",
 "Mai",
 "Juin",
 "Juil",
 "Aout",
 "Sep",
 "Oct",
 "Nov",
 "Dec");

// tooltips
Calendar._TT = {};

Calendar._TT["INFO"] = "A propos du calendrier";

Calendar._TT["ABOUT"] =
"DHTML Date/Heure Selecteur\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"Pour la derniere version visitez: http://dynarch.com/mishoo/calendar.epl\n" +
"Distribu� par GNU LGPL.  Voir http://gnu.org/licenses/lgpl.html pour les details." +
"\n\n" +
"Selection de la date :\n" +
"- Utiliser les bouttons \xab, \xbb  pour selectionner l\'annee\n" +
"- Utiliser les bouttons " + String.fromCharCode(0x2039) + ", " + String.fromCharCode(0x203a) + " pour selectionner les mois\n" +
"- Garder la souris sur n'importe quels boutons pour un selection plus rapide";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"Selection de l\'heure:\n" +
"- Cliquer sur heures ou minutes pour incrementer\n" +
"- ou Maj-clic pour decrementer\n" +
"- ou clic et glisser deplacer pour un selection plus rapide";

Calendar._TT["TOGGLE"] = "Changer le premier jour de la semaine";
Calendar._TT["PREV_YEAR"] = "Ann\u00e9 pr\u00e9c. (maintenir pour menu)";
Calendar._TT["PREV_MONTH"] = "Mois pr\u00e9c. (maintenir pour menu)";
Calendar._TT["GO_TODAY"] = "Atteindre date du jour";
Calendar._TT["NEXT_MONTH"] = "Mois suiv. (maintenir pour menu)";
Calendar._TT["NEXT_YEAR"] = "Ann\u00e9e suiv. (maintenir pour menu)";
Calendar._TT["SEL_DATE"] = "Choisir une date";
Calendar._TT["DRAG_TO_MOVE"] = "D\u00e9placer";
Calendar._TT["PART_TODAY"] = " (Aujourd'hui)";
Calendar._TT["MON_FIRST"] = "Commencer par lundi";
Calendar._TT["SUN_FIRST"] = "Commencer par dimanche";
Calendar._TT["CLOSE"] = "Fermer";
Calendar._TT["TODAY"] = "Aujourd'hui";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "%s comme d\u00e9but de la semaine";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["TIME_PART"] = "click (commutation)ou tirer";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%d-%m-%y";
Calendar._TT["TT_DATE_FORMAT"] = " %A %e %B %Y";

Calendar._TT["WK"] = "sem";
