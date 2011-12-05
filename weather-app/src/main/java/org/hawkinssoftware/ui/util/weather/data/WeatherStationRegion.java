package org.hawkinssoftware.ui.util.weather.data;

import nu.xom.Element;

public enum WeatherStationRegion
{
	AL("Alabama"),
	AK("Alaska"),
	AR("Arkansas"),
	AZ("Arizona"),
	CA("California"),
	CO("Colorado"),
	CT("Connecticut"),
	DE("Delaware"),
	FL("Florida"),
	GA("Georgia"),
	HI("Hawaii"),
	ID("Idaho"),
	IL("Illinois"),
	IN("Indiana"),
	IA("Iowa"),
	KS("Kansas"),
	KY("Kentucky"),
	LA("Louisiana"),
	ME("Maine"),
	MD("Maryland"),
	MA("Massachusetts"),
	MI("Michigan"),
	MN("Minnesota"),
	MS("Mississippi"),
	MO("Missouri"),
	MT("Montana"),
	NE("Nebraska"),
	NV("Nevada"),
	NH("New Hampshire"),
	NJ("New Jersey"),
	NM("New Mexico"),
	NY("New York"),
	NC("North Carolina"),
	ND("North Dakota"),
	OH("Ohio"),
	OK("Oklahoma"),
	OR("Oregon"),
	PA("Pennsylvania"),
	RI("Rhode Island"),
	SC("South Carolina"),
	SD("South Dakota"),
	TN("Tennessee"),
	TX("Texas"),
	UT("Utah"),
	VT("Vermont"),
	VA("Virginia"),
	WA("Washington"),
	WV("West Virginia"),
	WI("Wisconsin"),
	WY("Wyoming"),
	AG("Antigua"),
	AS("American Samoa"),
	AW("Aruba"),
	BB("Barbados"),
	BH("Bahamas"),
	DC("Washington, D.C."),
	DM("Dominica"),
	FJ("Fiji"),
	FM("Micronesia"),
	GD("Grenada"),
	GU("Guam"),
	KB("Kiribati"),
	KU("Cook Islands"),
	LC("St. Lucia"),
	MH("Marshall Islands"),
	MQ("Martinique"),
	MX("Mexico"),
	NL("New Caledonia"),
	PF("French Polynesia"),
	PR("Puerto Rico"),
	PW("Palau"),
	TO("Tonga"),
	TT("Trinidad & Tobago"),
	TV("Tuvalu"),
	UK("United Kingdom"),
	VC("St. Vincent and the Grenadines"),
	VG("British Virgin Islands"),
	VI("U.S. Virgin Islands"),
	WS("Samoa");

	public final String displayName;

	private WeatherStationRegion(String displayName)
	{
		this.displayName = displayName;
	}

	static WeatherStationRegion forElement(Element stationElement)
	{
		String token = stationElement.getFirstChildElement(WeatherStation.StationTag.REGION.element).getValue();

		// data errors
		if (token.equals("LS"))
		{
			token = "LA";
		}
		else if (token.equals("Ve"))
		{
			token = "VT";
		}
		
		return WeatherStationRegion.valueOf(token);
	}
}
