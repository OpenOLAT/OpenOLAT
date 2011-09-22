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
("星期日",
 "星期一",
 "星期二",
 "星期三",
 "星期四",
 "星期五",
 "星期六",
 "星期日");

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
("周日",
 "周一",
 "周二",
 "周三",
 "周四",
 "周五",
 "周六",
 "周日");

// full month names
Calendar._MN = new Array
("1月",
 "2月",
 "3月",
 "4月",
 "5月",
 "6月",
 "7月",
 "8月",
 "9月",
 "10月",
 "11月",
 "12月");

// short month names
Calendar._SMN = new Array
("1月",
 "2月",
 "3月",
 "4月",
 "5月",
 "6月",
 "7月",
 "8月",
 "9月",
 "10月",
 "11月",
 "12月");

// tooltips
Calendar._TT = {};
Calendar._TT["INFO"] = "关于日历";

Calendar._TT["ABOUT"] =
"DHTML 日期/时间 选择器\n" +
"(c) dynarch.com 2002-2003\n" + // don't translate this this ;-)
"最新版本请访问: http://dynarch.com/mishoo/calendar.epl\n" +
"在GNU LGPL下发布. 详情参见 http://gnu.org/licenses/lgpl.html." +
"\n\n" +
"日期选择:\n" +
"- 使用 \xab 和 \xbb 按钮选择年份\n" +
"- 使用 " + String.fromCharCode(0x2039) + " 和 " + String.fromCharCode(0x203a) + " 按钮选择月份\n" +
"- 在以上按钮上按住鼠标可实现快速选择.";
Calendar._TT["ABOUT_TIME"] = "\n\n" +
"时间选择:\n" +
"- 点击时间部分可以增加其数值\n" +
"- 或者按住Shift点击可以减小其数值\n" +
"- 或者点击拖拽以实现快速选择.";

Calendar._TT["PREV_YEAR"] = "前一年 (按住可打开菜单)";
Calendar._TT["PREV_MONTH"] = "前一年 (按住可打开菜单)";
Calendar._TT["GO_TODAY"] = "转到今天";
Calendar._TT["NEXT_MONTH"] = "后一年 (按住可打开菜单)";
Calendar._TT["NEXT_YEAR"] = "后一年 (按住可打开菜单)";
Calendar._TT["SEL_DATE"] = "选择日期";
Calendar._TT["DRAG_TO_MOVE"] = "拖拽";
Calendar._TT["PART_TODAY"] = " (今天)";

// the following is to inform that "%s" is to be the first day of week
// %s will be replaced with the day name.
Calendar._TT["DAY_FIRST"] = "首先显示 %";

// This may be locale-dependent.  It specifies the week-end days, as an array
// of comma-separated numbers.  The numbers are from 0 to 6: 0 means Sunday, 1
// means Monday, etc.
Calendar._TT["WEEKEND"] = "0,6";

Calendar._TT["CLOSE"] = "关闭";
Calendar._TT["TODAY"] = "今天";
Calendar._TT["TIME_PART"] = "(按住Shift)点击或拖拽可以改变数值";

// date formats
Calendar._TT["DEF_DATE_FORMAT"] = "%Y-%m-%d";
Calendar._TT["TT_DATE_FORMAT"] = "%a, %b %e";

Calendar._TT["WK"] = "周";
Calendar._TT["TIME"] = "时间:";
