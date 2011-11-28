package org.hawkinssoftware.ui.util.weather.data;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import nu.xom.Builder;
import nu.xom.Document;
import nu.xom.Element;
import nu.xom.Nodes;
import nu.xom.ParsingException;
import nu.xom.ValidityException;

import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.rns.core.log.Log;

public class StationLoader
{
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

	public List<WeatherStation> loadStations() throws IOException
	{
		try
		{
			URL url = new URL(STATION_LIST_URL);
			InputStream dataInput = url.openStream();

			Builder builder = new Builder();
			Document document = builder.build(dataInput);

			Nodes stationNodes = document.query(STATION_QUERY);
			Log.out(Tag.DEBUG, "station count: %d", stationNodes.size());

			List<WeatherStation> stations = new ArrayList<WeatherStation>();
			for (int i = 0; i < stationNodes.size(); i++)
			{
				Element stationElement = (Element) stationNodes.get(i);
				if (WeatherStation.isValid(stationElement))
				{
					WeatherStation station = new WeatherStation(stationElement);
					stations.add(station);
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

	public static void main(String[] args)
	{
		Log.addOutput(System.out);

		try
		{
			StationLoader loader = new StationLoader();
			List<WeatherStation> stations = loader.loadStations();
			Log.out(Tag.DEBUG, "Stations: %s", stations);

			for (int i = 0; i < 5; i++)
			{
				WeatherStation station = stations.get(i);

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
