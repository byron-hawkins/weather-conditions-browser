package org.hawkinssoftware.ui.util.weather.control;

import java.util.List;

import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask.ConcurrentAccessException;
import org.hawkinssoftware.azia.core.action.UserInterfaceTransaction.ActorBasedContributor.PendingTransaction;
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
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationState;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

@DomainRole.Join(membership = { ListDataModel.ModelListDomain.class, FlyweightCellDomain.class })
public class WeatherStationStatesController implements UserInterfaceHandler
{
	@InvocationConstraint(domains = AssemblyDomain.class)
	public static void initialize()
	{
		INSTANCE = new WeatherStationStatesController();
	}
	
	public static WeatherStationStatesController getInstance()
	{
		return INSTANCE;
	}

	private static WeatherStationStatesController INSTANCE;

	private final ScrollPaneComposite<CellViewportComposite<?>> stateList;
	private final ListDataModel stateModel;
	private final PopulateListTask populateTask = new PopulateListTask();

	@InvocationConstraint(domains = AssemblyDomain.class)
	private WeatherStationStatesController()
	{
		stateList = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.STATION_STATE_LIST_ASSEMBLY);
		stateModel = stateList.getViewport().getService(ListDataModel.class);
		stateList.getViewport().installHandler(this);
	}

	public void initializeView() throws ConcurrentAccessException
	{
		populateTask.start();
	}

	public void selectionChanging(SetSelectedRowDirective.Notification change, PendingTransaction transaction)
	{
		if ((change.row < 0) || stateModel.getRowCount(Section.SCROLLABLE) == 0)
		{
			return;
		}
		
		RowAddress address = stateList.getViewport().createAddress(change.row, RowAddress.Section.SCROLLABLE);
		WeatherStationsController.getInstance().displayStationState((WeatherStationState) stateModel.get(address));
	}

	@DomainRole.Join(membership = ListDataModel.ModelListDomain.class)
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
