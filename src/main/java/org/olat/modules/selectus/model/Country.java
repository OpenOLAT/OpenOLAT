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
package org.olat.modules.selectus.model;

import org.olat.core.util.StringHelper;

/**
 * 
 * Initial date: 17.06.2015<br>
 * @author srosse, stephane.rosse@frentix.com, http://www.frentix.com
 *
 */
public enum Country {
	
	ad("Andorra","AD","AND","ad"),
	ae("United Arab Emirates","AE","ARE","ae"),
	af("Afghanistan","AF","AFG","af"),
	ag("Antigua and Barbuda","AG","ATG","ag"),
	ai("Anguilla","AI","AIA","ai"),
	al("Albania","AL","ALB","al"),
	am("Armenia","AM","ARM","am"),
	ao("Angola","AO","AGO","ao"),
	aq("Antarctica","AQ","ATA","aq"),
	ar("Argentina","AR","ARG","ar"),
	as("American Samoa","AS","ASM","as"),
	at("Austria","AT","AUT","at"),
	au("Australia","AU","AUS","au"),
	aw("Aruba","AW","ABW","aw"),
	ax("\u00c5land Islands","AX","ALA","ax"),
	az("Azerbaijan","AZ","AZE","az"),
	ba("Bosnia-Herzegovina","BA","BIH","ba"),
	bb("Barbados","BB","BRB","bb"),
	bd("Bangladesh","BD","BGD","bd"),
	be("Belgium","BE","BEL","be"),
	bf("Burkina Faso","BF","BFA","bf"),
	bg("Bulgaria","BG","BGR","bg"),
	bh("Bahrain","BH","BHR","bh"),
	bi("Burundi","BI","BDI","bi"),
	bj("Benin","BJ","BEN","bj"),
	bl("St. Barthélemy","BL","BLM","bl"),
	bm("Bermuda","BM","BMU","bm"),
	bn("Brunei","BN","BRN","bn"),
	bo("Bolivia","BO","BOL","bo"),
	bq("Bonaire, Saint Eustatius and Saba","BQ","BES","bq"),
	br("Brazil","BR","BRA","br"),
	bs("Bahamas","BS","BHS","bs"),
	bt("Bhutan","BT","BTN","bt"),
	bv("Bouvet Island","BV","BVT","bv"),
	bw("Botswana","BW","BWA","bw"),
	by("Belarus","BY","BLR","by"),
	bz("Belize","BZ","BLZ","bz"),
	ca("Canada","CA","CAN","ca"),
	cc("Cocos (Keeling) Island","CC","CCK","cc"),
	cd("Congo, Democratic Republic","CD","COD","cd"),
	cf("Central African Republic","CF","CAF","cf"),
	cg("Congo (Republic)","CG","COG","cg"),
	ch("Switzerland","CH","CHE","ch"),
	ci("Ivory Coast","CI","CIV","ci"),
	ck("Cook Islands","CK","COK","ck"),
	cl("Chile","CL","CHL","cl"),
	cm("Cameroon","CM","CMR","cm"),
	cn("China (People's Republic OF)","CN","CHN","cn"),
	co("Colombia","CO","COL","co"),
	cr("Costa Rica","CR","CRI","cr"),
	cu("Cuba","CU","CUB","cu"),
	cv("Cape Verde","CV","CPV","cv"),
	cw("Curaçao","CW","CUW","cw"),
	cx("Christmas Island (Indian Ocean)","CX","CXR","cx"),
	cy("Cyprus","CY","CYP","cy"),
	cz("Czech Republic","CZ","CZE","cz"),
	de("Germany","DE","DEU","de"),
	dj("Djibouti","DJ","DJI","dj"),
	dk("Denmark","DK","DNK","dk"),
	dm("Dominica","DM","DMA","dm"),
	dom("Dominican Republic","DO","DOM","do"),
	dz("Algeria","DZ","DZA","dz"),
	ec("Ecuador","EC","ECU","ec"),
	ee("Estonia","EE","EST","ee"),
	eg("Egypt","EG","EGY","eg"),
	eh("Western Sahara","EH","ESH","eh"),
	er("Eritrea","ER","ERI","er"),
	es("Spain","ES","ESP","es"),
	et("Ethiopia","ET","ETH","et"),
	fi("Finland","FI","FIN","fi"),
	fj("Fiji","FJ","FJI","fj"),
	fk("Falkland Islands","FK","FLK","fk"),
	fm("Micronesia (Federated States OF)","FM","FSM","fm"),
	fo("Faroe Islands","FO","FRO","fo"),
	fr("France","FR","FRA","fr"),
	ga("Gabon","GA","GAB","ga"),
	gb("Great Britain and Northern Ireland","GB","GBR","gb"),
	gd("Grenada","GD","GRD","gd"),
	ge("Georgia","GE","GEO","ge"),
	gf("French Guiana","GF","GUF","gf"),
	gg("Guernsey","GG","GGY","gg"),
	gh("Ghana","GH","GHA","gh"),
	gi("Gibraltar","GI","GIB","gi"),
	gl("Greenland","GL","GRL","gl"),
	gm("Gambia","GM","GMB","gm"),
	gn("Guinea (Republic)","GN","GIN","gn"),
	gp("Guadeloupe","GP","GLP","gp"),
	gq("Equatorial Guinea","GQ","GNQ","gq"),
	gr("Greece","GR","GRC","gr"),
	gs("South Georgia AND the south Sandwich Islands","GS","SGS","gs"),
	gt("Guatemala","GT","GTM","gt"),
	gu("Guam","GU","GUM","gu"),
	gw("Guinea-Bissau","GW","GNB","gw"),
	gy("Guyana","GY","GUY","gy"),
	hk("Hong Kong","HK","HKG","hk"),
	hm("Heard Island and McDonald Islands","HM","HMD","hm"),
	hn("Honduras","HN","HND","hn"),
	hr("Croatia","HR","HRV","hr"),
	ht("Haiti","HT","HTI","ht"),
	hu("Hungary","HU","HUN","hu"),
	id("Indonesia","ID","IDN","id"),
	ie("Ireland","IE","IRL","ie"),
	il("Israel","IL","ISR","il"),
	im("Island OF Man","IM","IMN","im"),
	in("India","IN","IND","in"),
	io("British Indian Ocean Territory","IO","IOT","io"),
	iq("Iraq","IQ","IRQ","iq"),
	ir("Iran","IR","IRN","ir"),
	is("Iceland","IS","ISL","is"),
	it("Italy","IT","ITA","it"),
	je("Jersey","JE","JEY","je"),
	jm("Jamaica","JM","JAM","jm"),
	jo("Jordan","JO","JOR","jo"),
	jp("Japan","JP","JPN","jp"),
	ke("Kenya","KE","KEN","ke"),
	kg("Kyrgyzstan","KG","KGZ","kg"),
	kh("Cambodia","KH","KHM","kh"),
	ki("Kiribati","KI","KIR","ki"),
	km("Comoros","KM","COM","km"),
	kn("St. Christopher (St. Kitts) and Nevis","KN","KNA","kn"),
	kp("Korea, Democratic People's Republic of (North Korea)","KP","PRK","kp"),
	kr("Korea, Republic of (South Korea)","KR","KOR","kr"),
	kw("Kuwait","KW","KWT","kw"),
	ky("Cayman Islands","KY","CYM","ky"),
	kz("Kazakhstan","KZ","KAZ","kz"),
	la("Laos","LA","LAO","la"),
	lb("Lebanon","LB","LBN","lb"),
	lc("St. Lucia","LC","LCA","lc"),
	li("Liechtenstein","LI","LIE","li"),
	lk("Sri Lanka","LK","LKA","lk"),
	lr("Liberia","LR","LBR","lr"),
	ls("Lesotho","LS","LSO","ls"),
	lt("Lithuania","LT","LTU","lt"),
	lu("Luxembourg","LU","LUX","lu"),
	lv("Latvia","LV","LVA","lv"),
	ly("Libya","LY","LBY","ly"),
	ma("Morocco","MA","MAR","ma"),
	mc("Monaco","MC","MCO","mc"),
	md("Moldova","MD","MDA","md"),
	me("Montenegro, Republic","ME","MNE","me"),
	mf("St. Martin","MF","MAF","mf"),
	mg("Madagascar","MG","MDG","mg"),
	mh("Marshall Islands","MH","MHL","mh"),
	mk("Macedonia, the Former Yugoslav Republic of","MK","MKD","mk"),
	ml("Mali","ML","MLI","ml"),
	mm("Myanmar (Union of)","MM","MMR","mm"),
	mn("Mongolia","MN","MNG","mn"),
	mo("Macao","MO","MAC","mo"),
	mp("Mariana Islands","MP","MNP","mp"),
	mq("Martinique","MQ","MTQ","mq"),
	mr("Mauritania","MR","MRT","mr"),
	ms("Montserrat","MS","MSR","ms"),
	mt("Malta","MT","MLT","mt"),
	mu("Mauritius Island","MU","MUS","mu"),
	mv("Maldives","MV","MDV","mv"),
	mw("Malawi","MW","MWI","mw"),
	mx("Mexico","MX","MEX","mx"),
	my("Malaysia","MY","MYS","my"),
	mz("Mozambique","MZ","MOZ","mz"),
	na("Namibia","NA","NAM","na"),
	nc("New Caledonia","NC","NCL","nc"),
	ne("Niger","NE","NER","ne"),
	nf("Norfolk Island","NF","NFK","nf"),
	ng("Nigeria","NG","NGA","ng"),
	ni("Nicaragua","NI","NIC","ni"),
	nl("Netherlands","NL","NLD","nl"),
	no("Norway","NO","NOR","no"),
	np("Nepal","NP","NPL","np"),
	nr("Nauru","NR","NRU","nr"),
	nu("Niue","NU","NIU","nu"),
	nz("New Zealand","NZ","NZL","nz"),
	om("Oman","OM","OMN","om"),
	pa("Panama","PA","PAN","pa"),
	pe("Peru","PE","PER","pe"),
	pf("French Polynesia","PF","PYF","pf"),
	pg("Papua New Guinea","PG","PNG","pg"),
	ph("Philippines","PH","PHL","ph"),
	pk("Pakistan","PK","PAK","pk"),
	pl("Poland","PL","POL","pl"),
	pm("St. Pierre and Miquelon","PM","SPM","pm"),
	pn("Pitcairn","PN","PCN","pn"),
	pr("Puerto Rico","PR","PRI","pr"),
	ps("Palestine","PS","PSE","ps"),
	pt("Portugal","PT","PRT","pt"),
	pw("Palau","PW","PLW","pw"),
	py("Paraguay","PY","PRY","py"),
	qa("Qatar","QA","QAT","qa"),
	re("Réunion","RE","REU","re"),
	ro("Romania","RO","ROU","ro"),
	rs("Serbia, Republic","RS","SRB","rs"),
	ru("Russian Federation","RU","RUS","ru"),
	rw("Rwanda","RW","RWA","rw"),
	sa("Saudi Arabia","SA","SAU","sa"),
	sb("Salomon Islands","SB","SLB","sb"),
	sc("Seychelles","SC","SYC","sc"),
	sd("Sudan","SD","SDN","sd"),
	se("Sweden","SE","SWE","se"),
	sg("Singapore","SG","SGP","sg"),
	sh("St. Helena, Ascension and Tristan da Cunha","SH","SHN","sh"),
	si("Slovenia","SI","SVN","si"),
	sj("Svalbard and Jan Mayen","SJ","SJM","sj"),
	sk("Slovak Republic","SK","SVK","sk"),
	sl("Sierra Leone","SL","SLE","sl"),
	sm("San Marino","SM","SMR","sm"),
	sn("Senegal","SN","SEN","sn"),
	so("Somalia","SO","SOM","so"),
	sr("Suriname","SR","SUR","sr"),
	ss("South Sudan","SS","SS","ss"),
	st("St. Tome and Principe","ST","STP","st"),
	sv("El Salvador","SV","SLV","sv"),
	sx("St. Maarten","SX","SXM","sx"),
	sy("Syria","SY","SYR","sy"),
	sz("Swaziland","SZ","SWZ","sz"),
	tc("Turks and Caicos","TC","TCA","tc"),
	td("Chad","TD","TCD","td"),
	tf("French Southern Territories","TF","ATF","tf"),
	tg("Togo","TG","TGO","tg"),
	th("Thailand","TH","THA","th"),
	tj("Tajikistan","TJ","TJK","tj"),
	tk("Tokelau","TK","TKL","tk"),
	tl("Timor-Leste","TL","TLS","tl"),
	tm("Turkmenistan","TM","TKM","tm"),
	tn("Tunisia","TN","TUN","tn"),
	to("Tonga","TO","TON","to"),
	tr("Turkey","TR","TUR","tr"),
	tt("Trinidad and Tobago","TT","TTO","tt"),
	tv("Tuvalu","TV","TUV","tv"),
	tw("Taiwan (Chinese Taipei)","TW","TWN","tw"),
	tz("Tanzania","TZ","TZA","tz"),
	ua("Ukraine","UA","UKR","ua"),
	ug("Uganda","UG","UGA","ug"),
	um("United States Minor Outlying Islands","UM","UMI","um"),
	us("United States of America","US","USA","us"),
	uy("Uruguay","UY","URY","uy"),
	uz("Uzbekistan","UZ","UZB","uz"),
	va("Vatican City State","VA","VAT","va"),
	vc("St. Vincent and the Grenadines","VC","VCT","vc"),
	ve("Venezuela","VE","VEN","ve"),
	vg("Virgin Islands, British (Tortola)","VG","VGB","vg"),
	vi("Virgin Islands (USA)","VI","VIR","vi"),
	vn("Vietnam","VN","VNM","vn"),
	vu("Vanuatu","VU","VUT","vu"),
	wf("Wallis and Futuna Islands","WF","WLF","wf"),
	ws("Western Samoa","WS","WSM","ws"),
	xk("Kosovo / Unmik","XK","XK","xk"),
	ye("Yemen","YE","YEM","ye"),
	yt("Mayotte","YT","MYT","yt"),
	za("South Africa","ZA","ZAF","za"),
	zm("Zambia","ZM","ZMB","zm"),
	zw("Zimbabwe","ZW","ZWE","zw");

	
	private final String key;
	private final String country2Code;
	private final String country3Code;
	private final String i18nKey;
	
	private Country(String key, String country2Code, String country3Code, String i18nKey) {
		this.key = key;
		this.country2Code = country2Code;
		this.country3Code = country3Code;
		this.i18nKey = "country.code." + i18nKey.toUpperCase();
	}
	
	public static boolean isInTheList(String value) {
		boolean found = false;
		if(StringHelper.containsNonWhitespace(value)) {
			for(Country country:values()) {
				if(value.equals(country.key) || value.equals(country.country2Code)) {
					found = true;
					break;
				}
			}
		}
		return found;
	}
	
	public static Country country(String value) {
		Country found = null;
		if(StringHelper.containsNonWhitespace(value)) {
			for(Country country:values()) {
				if(value.equals(country.key) || value.equals(country.country2Code) || value.equals(country.country3Code)) {
					found = country;
					break;
				}
			}
		}
		return found;
	}
	
	public String key() {
		return key;
	}
	
	public String country2Code() {
		return country2Code;
	}
	
	public String country3Code() {
		return country3Code;
	}
	
	public String i18nKey() {
		return i18nKey;
	}

}