package org.hawkinssoftware.ui.util.weather.control;

import java.util.List;

import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask.ConcurrentAccessException;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.model.list.ListDataModelTransaction;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintInstanceDirective;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintRequestManager;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationState;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

public class WeatherStationStatesController
{
	public static WeatherStationStatesController getInstance()
	{
		return INSTANCE;
	}

	private static final WeatherStationStatesController INSTANCE = new WeatherStationStatesController();

	private final ScrollPaneComposite<?> stateList;
	private final PopulateListTask populateTask = new PopulateListTask();

	public WeatherStationStatesController()
	{
		stateList = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.STATION_STATE_LIST_ASSEMBLY);
	}

	public void initializeView() throws ConcurrentAccessException
	{
		populateTask.start();
	}

	private class PopulateListTask extends UserInterfaceTask
	{
		@Override
		protected boolean execute()
		{
			ListDataModel.Session session = stateList.getService(ListDataModel.class).createSession(getTransaction(ListDataModelTransaction.class));
			List<WeatherStationState> states = WeatherDataModel.getInstance().getStates();
			for (WeatherStationState state : states)
			{
				session.add(state);
			}

			RepaintRequestManager.requestRepaint(new RepaintInstanceDirective(stateList.getComponent()));

			return true;
		}
	}
}
