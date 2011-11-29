package org.hawkinssoftware.ui.util.weather.control;

import java.util.List;

import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTransaction.ActorBasedContributor.PendingTransaction;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.AssemblyDomain;
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.FlyweightCellDomain;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.UserInterfaceHandler;
import org.hawkinssoftware.azia.ui.component.cell.CellViewportComposite;
import org.hawkinssoftware.azia.ui.component.cell.transaction.SetSelectedRowDirective;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.model.RowAddress;
import org.hawkinssoftware.azia.ui.model.RowAddress.Section;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.model.list.ListDataModelTransaction;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintInstanceDirective;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintRequestManager;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationState;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

@DomainRole.Join(membership = { ListDataModel.ModelListDomain.class, FlyweightCellDomain.class })
public class WeatherStationsController implements UserInterfaceHandler
{
	@InvocationConstraint(domains = AssemblyDomain.class)
	public static void initialize()
	{
		INSTANCE = new WeatherStationsController();
	}

	public static WeatherStationsController getInstance()
	{
		return INSTANCE;
	}

	private static WeatherStationsController INSTANCE;

	private final ScrollPaneComposite<CellViewportComposite<?>> stationList;
	private final ListDataModel stationModel;

	private final PopulateListTask populateTask = new PopulateListTask();

	@InvocationConstraint(domains = AssemblyDomain.class)
	private WeatherStationsController()
	{
		stationList = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.STATION_LIST_ASSEMBLY);
		stationModel = stationList.getViewport().getService(ListDataModel.class);
		stationList.getViewport().installHandler(this);
	}

	void displayStationState(WeatherStationState stationState)
	{
		populateTask.start(stationState);
	}
	
	public void selectionChanging(SetSelectedRowDirective.Notification change, PendingTransaction transaction)
	{
		if ((change.row < 0) || stationModel.getRowCount(Section.SCROLLABLE) == 0)
		{
			return;
		}
		
		RowAddress address = stationList.getViewport().createAddress(change.row, RowAddress.Section.SCROLLABLE);
		WeatherConditionsController.getInstance().displayStation((WeatherStation) stationModel.get(address));
	}

	@DomainRole.Join(membership = ListDataModel.ModelListDomain.class)
	private class PopulateListTask extends UserInterfaceTask
	{
		private WeatherStationState currentState;

		void start(WeatherStationState currentState)
		{
			try
			{
				this.currentState = currentState;
				super.start();
			}
			catch (ConcurrentAccessException e)
			{
				Log.out(Tag.CRITICAL, e, "Failed to populate the station list for state %s", currentState);
			}
		}

		@Override
		protected boolean execute()
		{
			ListDataModel.Session session = stationList.getService(ListDataModel.class).createSession(getTransaction(ListDataModelTransaction.class));
			
			session.clear();
			
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
