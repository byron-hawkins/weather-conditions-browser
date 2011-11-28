package org.hawkinssoftware.ui.util.weather.view;

import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.hawkinssoftware.azia.core.action.TransactionRegistry;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask.ConcurrentAccessException;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.model.list.ListDataModelTransaction;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintInstanceDirective;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintRequestManager;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.ui.util.weather.data.StationLoader;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;

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

	private final StationLoader stationLoader = new StationLoader();
	private final ScrollPaneComposite<?> list;

	public WeatherViewerController()
	{
		list = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.STATION_LIST_ASSEMBLY);
	}

	public void loadStations() throws ConcurrentAccessException
	{
		TransactionRegistry.executeTask(new LoadStationsTask());
	}

	private class StationSorter implements Comparator<WeatherStation>
	{
		public int compare(WeatherStation first, WeatherStation second)
		{
			int comparison = first.state.compareTo(second.state);
			if (comparison != 0)
			{
				return comparison;
			}
			return first.name.compareTo(second.name);
		}
	}

	private class LoadStationsTask extends UserInterfaceTask
	{
		private List<WeatherStation> stations;

		public List<WeatherStation> getStations()
		{
			return stations;
		}

		@Override
		protected boolean execute()
		{
			try
			{
				stations = stationLoader.loadStations();
				Collections.sort(stations, new StationSorter());

				ListDataModel.Session session = list.getService(ListDataModel.class).createSession(getTransaction(ListDataModelTransaction.class));

				for (WeatherStation station : stations)
				{
					session.add(station);
				}

				RepaintRequestManager.requestRepaint(new RepaintInstanceDirective(list.getComponent()));

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
