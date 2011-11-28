package org.hawkinssoftware.ui.util.weather.data;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import nu.xom.Element;

public class WeatherStation
{
	private enum StationTag
	{
		ID("station_id"),
		STATE("state"),
		NAME("station_name"),
		LATITUDE("latitude"),
		LONGITUDE("longitude"),
		XML_URL("xml_url");

		final String element;

		private StationTag(String element)
		{
			this.element = element;
		}
	}
	
	private static final Pattern NAME_SCRUB = Pattern.compile("[0-9]+ - (.*)");

	public static boolean isValid(Element stationElement)
	{
		return stationElement.getFirstChildElement(StationTag.ID.element).getValue().length() > 0;
	}

	private static String scrubName(String name)
	{
		Matcher matcher = NAME_SCRUB.matcher(name);
		if (matcher.find())
		{
			return matcher.group(1);
		}
		
		return name;
	}
	
	public final String id;
	public final String state;
	public final String name;
	public final float latitude;
	public final float longitude;
	public final String url;

	WeatherStation(Element stationElement)
	{
		id = stationElement.getFirstChildElement(StationTag.ID.element).getValue();
		state = stationElement.getFirstChildElement(StationTag.STATE.element).getValue();
		name = scrubName(stationElement.getFirstChildElement(StationTag.NAME.element).getValue());
		latitude = Float.parseFloat(stationElement.getFirstChildElement(StationTag.LATITUDE.element).getValue());
		longitude = Float.parseFloat(stationElement.getFirstChildElement(StationTag.LONGITUDE.element).getValue());
		url = stationElement.getFirstChildElement(StationTag.XML_URL.element).getValue();
	}
	
	@Override
	public String toString()
	{
		return name + " (" + latitude + ", " + longitude + ")";
	}
}
