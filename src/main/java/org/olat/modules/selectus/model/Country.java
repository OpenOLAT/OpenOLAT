/**
 * <p>
 * Copyright (c) frentix GmbH<br>
 * http://www.frentix.com<br>
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
	
	ad("Andorra","AD","AND","country.ad"),
	ae("United Arab Emirates","AE","ARE","country.ae"),
	af("Afghanistan","AF","AFG","country.af"),
	ag("Antigua and Barbuda","AG","ATG","country.ag"),
	ai("Anguilla","AI","AIA","country.ai"),
	al("Albania","AL","ALB","country.al"),
	am("Armenia","AM","ARM","country.am"),
	ao("Angola","AO","AGO","country.ao"),
	aq("Antarctica","AQ","ATA","country.aq"),
	ar("Argentina","AR","ARG","country.ar"),
	as("American Samoa","AS","ASM","country.as"),
	at("Austria","AT","AUT","country.at"),
	au("Australia","AU","AUS","country.au"),
	aw("Aruba","AW","ABW","country.aw"),
	ax("\u00c5land Islands","AX","ALA","country.ax"),
	az("Azerbaijan","AZ","AZE","country.az"),
	ba("Bosnia-Herzegovina","BA","BIH","country.ba"),
	bb("Barbados","BB","BRB","country.bb"),
	bd("Bangladesh","BD","BGD","country.bd"),
	be("Belgium","BE","BEL","country.be"),
	bf("Burkina Faso","BF","BFA","country.bf"),
	bg("Bulgaria","BG","BGR","country.bg"),
	bh("Bahrain","BH","BHR","country.bh"),
	bi("Burundi","BI","BDI","country.bi"),
	bj("Benin","BJ","BEN","country.bj"),
	bl("St. Barthélemy","BL","BLM","country.bl"),
	bm("Bermuda","BM","BMU","country.bm"),
	bn("Brunei","BN","BRN","country.bn"),
	bo("Bolivia","BO","BOL","country.bo"),
	bq("Bonaire, Saint Eustatius and Saba","BQ","BES","country.bq"),
	br("Brazil","BR","BRA","country.br"),
	bs("Bahamas","BS","BHS","country.bs"),
	bt("Bhutan","BT","BTN","country.bt"),
	bv("Bouvet Island","BV","BVT","country.bv"),
	bw("Botswana","BW","BWA","country.bw"),
	by("Belarus","BY","BLR","country.by"),
	bz("Belize","BZ","BLZ","country.bz"),
	ca("Canada","CA","CAN","country.ca"),
	cc("Cocos (Keeling) Island","CC","CCK","country.cc"),
	cd("Congo, Democratic Republic","CD","COD","country.cd"),
	cf("Central African Republic","CF","CAF","country.cf"),
	cg("Congo (Republic)","CG","COG","country.cg"),
	ch("Switzerland","CH","CHE","country.ch"),
	ci("Ivory Coast","CI","CIV","country.ci"),
	ck("Cook Islands","CK","COK","country.ck"),
	cl("Chile","CL","CHL","country.cl"),
	cm("Cameroon","CM","CMR","country.cm"),
	cn("China (People's Republic OF)","CN","CHN","country.cn"),
	co("Colombia","CO","COL","country.co"),
	cr("Costa Rica","CR","CRI","country.cr"),
	cu("Cuba","CU","CUB","country.cu"),
	cv("Cape Verde","CV","CPV","country.cv"),
	cw("Curaçao","CW","CUW","country.cw"),
	cx("Christmas Island (Indian Ocean)","CX","CXR","country.cx"),
	cy("Cyprus","CY","CYP","country.cy"),
	cz("Czech Republic","CZ","CZE","country.cz"),
	de("Germany","DE","DEU","country.de"),
	dj("Djibouti","DJ","DJI","country.dj"),
	dk("Denmark","DK","DNK","country.dk"),
	dm("Dominica","DM","DMA","country.dm"),
	dom("Dominican Republic","DO","DOM","country.do"),
	dz("Algeria","DZ","DZA","country.dz"),
	ec("Ecuador","EC","ECU","country.ec"),
	ee("Estonia","EE","EST","country.ee"),
	eg("Egypt","EG","EGY","country.eg"),
	eh("Western Sahara","EH","ESH","country.eh"),
	er("Eritrea","ER","ERI","country.er"),
	es("Spain","ES","ESP","country.es"),
	et("Ethiopia","ET","ETH","country.et"),
	fi("Finland","FI","FIN","country.fi"),
	fj("Fiji","FJ","FJI","country.fj"),
	fk("Falkland Islands","FK","FLK","country.fk"),
	fm("Micronesia (Federated States OF)","FM","FSM","country.fm"),
	fo("Faroe Islands","FO","FRO","country.fo"),
	fr("France","FR","FRA","country.fr"),
	ga("Gabon","GA","GAB","country.ga"),
	gb("Great Britain and Northern Ireland","GB","GBR","country.gb"),
	gd("Grenada","GD","GRD","country.gd"),
	ge("Georgia","GE","GEO","country.ge"),
	gf("French Guiana","GF","GUF","country.gf"),
	gg("Guernsey","GG","GGY","country.gg"),
	gh("Ghana","GH","GHA","country.gh"),
	gi("Gibraltar","GI","GIB","country.gi"),
	gl("Greenland","GL","GRL","country.gl"),
	gm("Gambia","GM","GMB","country.gm"),
	gn("Guinea (Republic)","GN","GIN","country.gn"),
	gp("Guadeloupe","GP","GLP","country.gp"),
	gq("Equatorial Guinea","GQ","GNQ","country.gq"),
	gr("Greece","GR","GRC","country.gr"),
	gs("South Georgia AND the south Sandwich Islands","GS","SGS","country.gs"),
	gt("Guatemala","GT","GTM","country.gt"),
	gu("Guam","GU","GUM","country.gu"),
	gw("Guinea-Bissau","GW","GNB","country.gw"),
	gy("Guyana","GY","GUY","country.gy"),
	hk("Hong Kong","HK","HKG","country.hk"),
	hm("Heard Island and McDonald Islands","HM","HMD","country.hm"),
	hn("Honduras","HN","HND","country.hn"),
	hr("Croatia","HR","HRV","country.hr"),
	ht("Haiti","HT","HTI","country.ht"),
	hu("Hungary","HU","HUN","country.hu"),
	id("Indonesia","ID","IDN","country.id"),
	ie("Ireland","IE","IRL","country.ie"),
	il("Israel","IL","ISR","country.il"),
	im("Island OF Man","IM","IMN","country.im"),
	in("India","IN","IND","country.in"),
	io("British Indian Ocean Territory","IO","IOT","country.io"),
	iq("Iraq","IQ","IRQ","country.iq"),
	ir("Iran","IR","IRN","country.ir"),
	is("Iceland","IS","ISL","country.is"),
	it("Italy","IT","ITA","country.it"),
	je("Jersey","JE","JEY","country.je"),
	jm("Jamaica","JM","JAM","country.jm"),
	jo("Jordan","JO","JOR","country.jo"),
	jp("Japan","JP","JPN","country.jp"),
	ke("Kenya","KE","KEN","country.ke"),
	kg("Kyrgyzstan","KG","KGZ","country.kg"),
	kh("Cambodia","KH","KHM","country.kh"),
	ki("Kiribati","KI","KIR","country.ki"),
	km("Comoros","KM","COM","country.km"),
	kn("St. Christopher (St. Kitts) and Nevis","KN","KNA","country.kn"),
	kp("Korea, Democratic People's Republic of (North Korea)","KP","PRK","country.kp"),
	kr("Korea, Republic of (South Korea)","KR","KOR","country.kr"),
	kw("Kuwait","KW","KWT","country.kw"),
	ky("Cayman Islands","KY","CYM","country.ky"),
	kz("Kazakhstan","KZ","KAZ","country.kz"),
	la("Laos","LA","LAO","country.la"),
	lb("Lebanon","LB","LBN","country.lb"),
	lc("St. Lucia","LC","LCA","country.lc"),
	li("Liechtenstein","LI","LIE","country.li"),
	lk("Sri Lanka","LK","LKA","country.lk"),
	lr("Liberia","LR","LBR","country.lr"),
	ls("Lesotho","LS","LSO","country.ls"),
	lt("Lithuania","LT","LTU","country.lt"),
	lu("Luxembourg","LU","LUX","country.lu"),
	lv("Latvia","LV","LVA","country.lv"),
	ly("Libya","LY","LBY","country.ly"),
	ma("Morocco","MA","MAR","country.ma"),
	mc("Monaco","MC","MCO","country.mc"),
	md("Moldova","MD","MDA","country.md"),
	me("Montenegro, Republic","ME","MNE","country.me"),
	mf("St. Martin","MF","MAF","country.mf"),
	mg("Madagascar","MG","MDG","country.mg"),
	mh("Marshall Islands","MH","MHL","country.mh"),
	mk("Macedonia, the Former Yugoslav Republic of","MK","MKD","country.mk"),
	ml("Mali","ML","MLI","country.ml"),
	mm("Myanmar (Union of)","MM","MMR","country.mm"),
	mn("Mongolia","MN","MNG","country.mn"),
	mo("Macao","MO","MAC","country.mo"),
	mp("Mariana Islands","MP","MNP","country.mp"),
	mq("Martinique","MQ","MTQ","country.mq"),
	mr("Mauritania","MR","MRT","country.mr"),
	ms("Montserrat","MS","MSR","country.ms"),
	mt("Malta","MT","MLT","country.mt"),
	mu("Mauritius Island","MU","MUS","country.mu"),
	mv("Maldives","MV","MDV","country.mv"),
	mw("Malawi","MW","MWI","country.mw"),
	mx("Mexico","MX","MEX","country.mx"),
	my("Malaysia","MY","MYS","country.my"),
	mz("Mozambique","MZ","MOZ","country.mz"),
	na("Namibia","NA","NAM","country.na"),
	nc("New Caledonia","NC","NCL","country.nc"),
	ne("Niger","NE","NER","country.ne"),
	nf("Norfolk Island","NF","NFK","country.nf"),
	ng("Nigeria","NG","NGA","country.ng"),
	ni("Nicaragua","NI","NIC","country.ni"),
	nl("Netherlands","NL","NLD","country.nl"),
	no("Norway","NO","NOR","country.no"),
	np("Nepal","NP","NPL","country.np"),
	nr("Nauru","NR","NRU","country.nr"),
	nu("Niue","NU","NIU","country.nu"),
	nz("New Zealand","NZ","NZL","country.nz"),
	om("Oman","OM","OMN","country.om"),
	pa("Panama","PA","PAN","country.pa"),
	pe("Peru","PE","PER","country.pe"),
	pf("French Polynesia","PF","PYF","country.pf"),
	pg("Papua New Guinea","PG","PNG","country.pg"),
	ph("Philippines","PH","PHL","country.ph"),
	pk("Pakistan","PK","PAK","country.pk"),
	pl("Poland","PL","POL","country.pl"),
	pm("St. Pierre and Miquelon","PM","SPM","country.pm"),
	pn("Pitcairn","PN","PCN","country.pn"),
	pr("Puerto Rico","PR","PRI","country.pr"),
	ps("Palestine","PS","PSE","country.ps"),
	pt("Portugal","PT","PRT","country.pt"),
	pw("Palau","PW","PLW","country.pw"),
	py("Paraguay","PY","PRY","country.py"),
	qa("Qatar","QA","QAT","country.qa"),
	re("Réunion","RE","REU","country.re"),
	ro("Romania","RO","ROU","country.ro"),
	rs("Serbia, Republic","RS","SRB","country.rs"),
	ru("Russian Federation","RU","RUS","country.ru"),
	rw("Rwanda","RW","RWA","country.rw"),
	sa("Saudi Arabia","SA","SAU","country.sa"),
	sb("Salomon Islands","SB","SLB","country.sb"),
	sc("Seychelles","SC","SYC","country.sc"),
	sd("Sudan","SD","SDN","country.sd"),
	se("Sweden","SE","SWE","country.se"),
	sg("Singapore","SG","SGP","country.sg"),
	sh("St. Helena, Ascension and Tristan da Cunha","SH","SHN","country.sh"),
	si("Slovenia","SI","SVN","country.si"),
	sj("Svalbard and Jan Mayen","SJ","SJM","country.sj"),
	sk("Slovak Republic","SK","SVK","country.sk"),
	sl("Sierra Leone","SL","SLE","country.sl"),
	sm("San Marino","SM","SMR","country.sm"),
	sn("Senegal","SN","SEN","country.sn"),
	so("Somalia","SO","SOM","country.so"),
	sr("Suriname","SR","SUR","country.sr"),
	ss("South Sudan","SS","SS","country.ss"),
	st("St. Tome and Principe","ST","STP","country.st"),
	sv("El Salvador","SV","SLV","country.sv"),
	sx("St. Maarten","SX","SXM","country.sx"),
	sy("Syria","SY","SYR","country.sy"),
	sz("Swaziland","SZ","SWZ","country.sz"),
	tc("Turks and Caicos","TC","TCA","country.tc"),
	td("Chad","TD","TCD","country.td"),
	tf("French Southern Territories","TF","ATF","country.tf"),
	tg("Togo","TG","TGO","country.tg"),
	th("Thailand","TH","THA","country.th"),
	tj("Tajikistan","TJ","TJK","country.tj"),
	tk("Tokelau","TK","TKL","country.tk"),
	tl("Timor-Leste","TL","TLS","country.tl"),
	tm("Turkmenistan","TM","TKM","country.tm"),
	tn("Tunisia","TN","TUN","country.tn"),
	to("Tonga","TO","TON","country.to"),
	tr("Turkey","TR","TUR","country.tr"),
	tt("Trinidad and Tobago","TT","TTO","country.tt"),
	tv("Tuvalu","TV","TUV","country.tv"),
	tw("Taiwan (Chinese Taipei)","TW","TWN","country.tw"),
	tz("Tanzania","TZ","TZA","country.tz"),
	ua("Ukraine","UA","UKR","country.ua"),
	ug("Uganda","UG","UGA","country.ug"),
	um("United States Minor Outlying Islands","UM","UMI","country.um"),
	us("United States of America","US","USA","country.us"),
	uy("Uruguay","UY","URY","country.uy"),
	uz("Uzbekistan","UZ","UZB","country.uz"),
	va("Vatican City State","VA","VAT","country.va"),
	vc("St. Vincent and the Grenadines","VC","VCT","country.vc"),
	ve("Venezuela","VE","VEN","country.ve"),
	vg("Virgin Islands, British (Tortola)","VG","VGB","country.vg"),
	vi("Virgin Islands (USA)","VI","VIR","country.vi"),
	vn("Vietnam","VN","VNM","country.vn"),
	vu("Vanuatu","VU","VUT","country.vu"),
	wf("Wallis and Futuna Islands","WF","WLF","country.wf"),
	ws("Western Samoa","WS","WSM","country.ws"),
	xk("Kosovo / Unmik","XK","XK","country.xk"),
	ye("Yemen","YE","YEM","country.ye"),
	yt("Mayotte","YT","MYT","country.yt"),
	za("South Africa","ZA","ZAF","country.za"),
	zm("Zambia","ZM","ZMB","country.zm"),
	zw("Zimbabwe","ZW","ZWE","country.zw");

	
	private final String key;
	private final String country2Code;
	private final String country3Code;
	private final String i18nKey;
	
	private Country(String key, String country2Code, String country3Code, String i18nKey) {
		this.key = key;
		this.country2Code = country2Code;
		this.country3Code = country3Code;
		this.i18nKey = i18nKey;
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