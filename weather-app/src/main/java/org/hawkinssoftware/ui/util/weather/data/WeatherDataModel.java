package org.hawkinssoftware.ui.util.weather.data;

import java.util.List;
import java.util.Map;

import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerControllerDomain;

@VisibilityConstraint(domains = WeatherViewerControllerDomain.class)
public class WeatherDataModel
{
	public static WeatherDataModel getInstance()
	{
		return INSTANCE;
	}

	private static final WeatherDataModel INSTANCE = new WeatherDataModel();

	private List<WeatherStationRegion> regions;
	private Map<WeatherStationRegion, List<WeatherStation>> stations;

	@InvocationConstraint(domains = InitializationDomain.class)
	public void installData(List<WeatherStationRegion> regions, Map<WeatherStationRegion, List<WeatherStation>> stations)
	{
		this.regions = regions;
		this.stations = stations;
	}

	public List<WeatherStationRegion> getRegions()
	{
		return regions;
	}

	public List<WeatherStation> getStations(WeatherStationRegion region)
	{
		return stations.get(region);
	}
}
