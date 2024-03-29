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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.DataTransferDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerControllerDomain;

@VisibilityConstraint(domains = { InitializationDomain.class, WeatherViewerControllerDomain.class })
@DomainRole.Join(membership = DataTransferDomain.class)
public class StationLoader
{
	public static StationLoader getInstance()
	{
		return INSTANCE;
	}

	private static final StationLoader INSTANCE = new StationLoader();

	private static final String STATION_LIST_URL = "http://www.weather.gov/xml/current_obs/index.xml";
	private static final String STATION_QUERY = "wx_station_index/station";

	private void dumpUrl(URL url) throws IOException
	{
		InputStream dataInput = url.openStream();
		byte[] stationData = new byte[dataInput.available()];
		dataInput.read(stationData);
		String stationXML = new String(stationData);
		Log.out(Tag.DEBUG, "Station data:");
		Log.out(Tag.DEBUG, stationXML);
	}

	/**
	 * @JTourBusStop 3, WeatherStation loading, StationLoader.loadStations( ) implements the station loading:
	 * 
	 *               This method has no caveats about when it is called, and it holds no references to the loaded data,
	 *               so the caller is able to take full responsibility over the effects of station loading. The
	 *               orthogonality constraints for the DataTransferDomain (described in tour
	 *               "WeatherStation loading isolation") ensure that it will have no role in application workflow or
	 *               data management--both of which typically become tangled up with a loading facility like this.
	 * 
	 * @JTourBusStop 2, WeatherStation loading isolation, StationLoader.loadStations( ) may only be invoked by the
	 *               InitializationDomain:
	 * 
	 *               Without this constraint, other domains would be able to collaborate with the DataTransferDomain in
	 *               the loading of stations. The net effect is to reduce complexity by limiting the scope of
	 *               responsibility for station loading and its effects.
	 */
	@InvocationConstraint(domains = InitializationDomain.class)
	public Map<WeatherStationRegion, List<WeatherStation>> loadStations() throws IOException
	{
		try
		{
			URL url = new URL(STATION_LIST_URL);
			InputStream dataInput = url.openStream();

			Builder builder = new Builder();
			Document document = builder.build(dataInput);

			Nodes stationNodes = document.query(STATION_QUERY);
			Log.out(Tag.DEBUG, "station count: %d", stationNodes.size());

			Map<WeatherStationRegion, List<WeatherStation>> stations = new HashMap<WeatherStationRegion, List<WeatherStation>>();
			for (int i = 0; i < stationNodes.size(); i++)
			{
				Element stationElement = (Element) stationNodes.get(i);
				if (WeatherStation.isValid(stationElement))
				{
					WeatherStationRegion region = WeatherStationRegion.forElement(stationElement);
					if (!stations.containsKey(region))
					{
						stations.put(region, new ArrayList<WeatherStation>());
					}
					WeatherStation station = new WeatherStation(stationElement);
					stations.get(region).add(station);
				}
			}

			return stations;
		}
		catch (ValidityException e)
		{
			throw new IOException("Failed to parse the station list from NOAA", e);
		}
		catch (ParsingException e)
		{
			throw new IOException("Failed to parse the station list from NOAA", e);
		}
	}

	/**
	 * @JTourBusStop 1, StationReport lifecycle, StationLoader.loadReport( ) creates a StationReport from raw report
	 *               data loaded from NOAA:
	 * @JTourBusStop 1, StationReport immutability, StationLoader.loadReport( ) creates a StationReport from raw report
	 *               data loaded from NOAA:
	 * 
	 *               The report arrives as an XML stream and is parsed for populating a StationReport instance.
	 * 
	 * @JTourBusStop 3, StationReport loading, StationLoader.loadReport( ) implements the weather report loading:
	 * 
	 *               This method has no caveats about when it is called, and it holds no references to the loaded data,
	 *               so the caller is able to take full responsibility over the effects of weather report loading. The
	 *               orthogonality constraints for the DataTransferDomain (described in tour
	 *               "StationReport loading isolation") ensure that it will have no role in application workflow or data
	 *               management--both of which typically become tangled up with a loading facility like this.
	 * 
	 * @JTourBusStop 2, StationReport loading isolation, StationLoader.loadReport( ) may only be invoked by the
	 *               WeatherViewerControllerDomain:
	 * 
	 *               Without this constraint, other domains would be able to collaborate with the DataTransferDomain in
	 *               the loading of weather reports. The net effect is to reduce complexity by limiting the scope of
	 *               responsibility for report loading and its effects.
	 */
	@InvocationConstraint(domains = WeatherViewerControllerDomain.class)
	public StationReport loadReport(WeatherStation station) throws IOException
	{
		try
		{
			URL url = new URL(station.url);
			InputStream reportInput = url.openStream();

			Builder builder = new Builder();
			Document document = builder.build(reportInput);

			return new StationReport(document.getRootElement());
		}
		catch (ValidityException e)
		{
			throw new IOException("Failed to parse the station report for '" + station.id + "'", e);
		}
		catch (ParsingException e)
		{
			throw new IOException("Failed to parse the station report for '" + station.id + "'", e);
		}
	}

	@InvocationConstraint(domains = InitializationDomain.class)
	public static void main(String[] args)
	{
		Log.addOutput(System.out);

		try
		{
			StationLoader loader = new StationLoader();
			Map<WeatherStationRegion, List<WeatherStation>> stations = loader.loadStations();
			Log.out(Tag.DEBUG, "Stations: %s", stations);

			Iterator<WeatherStationRegion> regionIterator = stations.keySet().iterator();
			for (int i = 0; i < 5; i++)
			{
				WeatherStationRegion region = regionIterator.next();
				WeatherStation station = stations.get(region).get(0);

				try
				{
					StationReport report = loader.loadReport(station);
					Log.out(Tag.DEBUG, "Report for station %s: %s", station.id, report);
				}
				catch (FileNotFoundException e)
				{
					Log.out(Tag.WARNING, "Station report not available for '%s'", station.name);
				}
			}
		}
		catch (Throwable t)
		{
			Log.out(Tag.CRITICAL, t, "Failed to test the weather data loader!");
		}
	}
}
