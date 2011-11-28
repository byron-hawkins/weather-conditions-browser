package org.hawkinssoftware.ui.util.weather.data;

import java.util.List;
import java.util.Map;

public class WeatherDataModel
{
	public static WeatherDataModel getInstance()
	{
		return INSTANCE;
	}

	private static final WeatherDataModel INSTANCE = new WeatherDataModel();

	private List<WeatherStationState> states;
	private Map<WeatherStationState, List<WeatherStation>> stations;

	public void installData(List<WeatherStationState> states, Map<WeatherStationState, List<WeatherStation>> stations)
	{
		this.states = states;
		this.stations = stations;
	}
	
	public List<WeatherStationState> getStates()
	{
		return states;
	}
	
	public List<WeatherStation> getStations(WeatherStationState state)
	{
		return stations.get(state);
	}
}
