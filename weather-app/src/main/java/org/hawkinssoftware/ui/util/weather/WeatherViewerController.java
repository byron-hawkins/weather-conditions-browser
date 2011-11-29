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
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.AssemblyDomain;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.ui.util.weather.control.WeatherConditionsController;
import org.hawkinssoftware.ui.util.weather.control.WeatherStationStatesController;
import org.hawkinssoftware.ui.util.weather.control.WeatherStationsController;
import org.hawkinssoftware.ui.util.weather.data.StationLoader;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationState;

public class WeatherViewerController
{
	public static void install()
	{

	}

	public static WeatherViewerController getInstance()
	{
		return INSTANCE;
	}

	private static final WeatherViewerController INSTANCE = new WeatherViewerController();

	@InvocationConstraint(domains = AssemblyDomain.class)
	public void startApplication() throws ConcurrentAccessException
	{
		TransactionRegistry.executeTask(new LoadStationsTask());

		WeatherConditionsController.initialize();
		WeatherStationsController.initialize();
		WeatherStationStatesController.initialize();
		WeatherStationStatesController.getInstance().initializeView();
	}

	private class StateSorter implements Comparator<WeatherStationState>
	{
		public int compare(WeatherStationState first, WeatherStationState second)
		{
			return first.token.compareTo(second.token);
		}
	}

	private class StationSorter implements Comparator<WeatherStation>
	{
		public int compare(WeatherStation first, WeatherStation second)
		{
			return first.name.compareTo(second.name);
		}
	}

	private class LoadStationsTask extends UserInterfaceTask
	{
		@Override
		protected boolean execute()
		{
			try
			{
				Map<WeatherStationState, List<WeatherStation>> stations = StationLoader.getInstance().loadStations();
				for (List<WeatherStation> stateStations : stations.values())
				{
					Collections.sort(stateStations, new StationSorter());
				}

				List<WeatherStationState> states = new ArrayList<WeatherStationState>(stations.keySet());
				Collections.sort(states, new StateSorter());

				WeatherDataModel.getInstance().installData(states, stations);

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
