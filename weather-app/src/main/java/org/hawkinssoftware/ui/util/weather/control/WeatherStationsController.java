package org.hawkinssoftware.ui.util.weather.control;

import java.util.List;

import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.model.list.ListDataModelTransaction;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintInstanceDirective;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintRequestManager;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationState;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

public class WeatherStationsController
{
	public static WeatherStationsController getInstance()
	{
		return INSTANCE;
	}

	private static final WeatherStationsController INSTANCE = new WeatherStationsController();

	private final ScrollPaneComposite<?> stationList;

	private final PopulateListTask populateTask = new PopulateListTask();

	public WeatherStationsController()
	{
		stationList = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.STATION_LIST_ASSEMBLY);
	}

	public void initializeView()
	{

	}

	private class PopulateListTask extends UserInterfaceTask
	{
		private WeatherStationState currentState;

		void start(WeatherStationState currentState) throws ConcurrentAccessException
		{
			this.currentState = currentState;
			super.start();
		}

		@Override
		protected boolean execute()
		{
			ListDataModel.Session session = stationList.getService(ListDataModel.class).createSession(getTransaction(ListDataModelTransaction.class));
			List<WeatherStation> stations = WeatherDataModel.getInstance().getStations(currentState);
			for (WeatherStation station : stations)
			{
				session.add(station);
			}

			RepaintRequestManager.requestRepaint(new RepaintInstanceDirective(stationList.getComponent()));

			return true;
		}
	}
}
