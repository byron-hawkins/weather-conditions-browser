package org.hawkinssoftware.ui.util.weather.data;

import nu.xom.Element;

public class WeatherStationState
{
	static String getToken(Element stationElement)
	{
		return stationElement.getFirstChildElement(WeatherStation.StationTag.STATE.element).getValue();
	}

	public final String token;

	WeatherStationState(Element stationElement)
	{
		token = getToken(stationElement);
	}
}
