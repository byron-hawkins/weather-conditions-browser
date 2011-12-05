package org.hawkinssoftware.ui.util.weather;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.hawkinssoftware.azia.core.action.TransactionRegistry;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask.ConcurrentAccessException;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.control.WeatherConditionsController;
import org.hawkinssoftware.ui.util.weather.control.WeatherStationRegionsController;
import org.hawkinssoftware.ui.util.weather.control.WeatherStationsController;
import org.hawkinssoftware.ui.util.weather.data.StationLoader;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationRegion;

class WeatherViewerInitializer
{
	public static WeatherViewerInitializer getInstance()
	{
		return INSTANCE;
	}

	private static final WeatherViewerInitializer INSTANCE = new WeatherViewerInitializer();

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public void startApplication() throws ConcurrentAccessException
	{
		TransactionRegistry.executeTask(new LoadStationsTask());

		WeatherConditionsController.initialize();
		WeatherStationsController.initialize();
		WeatherStationRegionsController.initialize();
		WeatherStationRegionsController.getInstance().initializeView();
	}

	private class RegionSorter implements Comparator<WeatherStationRegion>
	{
		public int compare(WeatherStationRegion first, WeatherStationRegion second)
		{
			return first.displayName.compareTo(second.displayName);
		}
	}

	private class StationSorter implements Comparator<WeatherStation>
	{
		public int compare(WeatherStation first, WeatherStation second)
		{
			return first.name.compareTo(second.name);
		}
	}

	@DomainRole.Join(membership = InitializationDomain.class)
	private class LoadStationsTask extends UserInterfaceTask
	{
		@Override
		protected boolean execute()
		{
			try
			{
				Map<WeatherStationRegion, List<WeatherStation>> stations = StationLoader.getInstance().loadStations();
				for (List<WeatherStation> regionStations : stations.values())
				{
					Collections.sort(regionStations, new StationSorter());
				}

				List<WeatherStationRegion> regions = new ArrayList<WeatherStationRegion>(stations.keySet());
				Collections.sort(regions, new RegionSorter());

				WeatherDataModel.getInstance().installData(regions, stations);

				return true;
			}
			catch (IOException e)
			{
				Log.out(Tag.CRITICAL, e, "Failed to load the weather stations!");
				return false;
			}
		}
	}

	private class LoadReportTask extends UserInterfaceTask
	{
		@Override
		protected boolean execute()
		{
			return false;
		}
	}
}
