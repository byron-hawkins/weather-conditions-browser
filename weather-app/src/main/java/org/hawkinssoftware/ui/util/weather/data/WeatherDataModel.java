/*
 * Copyright (c) 2011 HawkinsSoftware
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Byron Hawkins of HawkinsSoftware
 */
package org.hawkinssoftware.ui.util.weather.data;

import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.validation.ValidateRead;
import org.hawkinssoftware.rns.core.validation.ValidateWrite;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerControllerDomain;

@ValidateRead
@ValidateWrite
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

	/**
	 * @JTourBusStop 3, WeatherStation lifecycle, WeatherDataModel holds an unmodifiable map of WeatherStations:
	 * @JTourBusStop 3, WeatherStationRegion lifecycle, WeatherDataModel holds an unmodifiable list of
	 *               WeatherStationRegions:
	 * 
	 * @JTourBusStop 2, WeatherStation immutability, WeatherDataModel holds an unmodifiable map of WeatherStations:
	 * 
	 *               The set of WeatherStations cannot be modified after being installed in this model.
	 * 
	 * @JTourBusStop 2, WeatherStationRegion immutability, WeatherDataModel holds an unmodifiable list of
	 *               WeatherStationRegions:
	 * 
	 *               The set of WeatherStationRegions cannot be modified after being installed in this model.
	 */
	@InvocationConstraint(domains = InitializationDomain.class)
	public void installData(List<WeatherStationRegion> regions, Map<WeatherStationRegion, List<WeatherStation>> stations)
	{
		this.regions = Collections.unmodifiableList(regions);

		for (WeatherStationRegion region : stations.keySet())
		{
			stations.put(region, Collections.unmodifiableList(stations.get(region)));
		}
		this.stations = Collections.unmodifiableMap(stations);
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
