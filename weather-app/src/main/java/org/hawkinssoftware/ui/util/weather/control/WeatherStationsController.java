package org.hawkinssoftware.ui.util.weather.control;

import java.util.List;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
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
import org.hawkinssoftware.azia.ui.component.text.Label;
import org.hawkinssoftware.azia.ui.component.text.LabelComposite;
import org.hawkinssoftware.azia.ui.component.transaction.state.ChangeTextDirective;
import org.hawkinssoftware.azia.ui.model.RowAddress;
import org.hawkinssoftware.azia.ui.model.RowAddress.Section;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.model.list.ListDataModelTransaction;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintInstanceDirective;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintRequestManager;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationDomain;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationRegion;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

@DomainRole.Join(membership = { StationDomain.class, ListDataModel.ModelListDomain.class, FlyweightCellDomain.class })
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
		stationList = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.getStationList());
		stationModel = stationList.getViewport().getService(ListDataModel.class);
		stationList.getViewport().installHandler(this);
	}

	void displayStationRegion(WeatherStationRegion stationRegion)
	{
		populateTask.start(stationRegion);
	}

	public void selectionChanging(SetSelectedRowDirective.Notification change, PendingTransaction transaction)
	{
		if (change.row < 0) // || stationModel.getRowCount(Section.SCROLLABLE) == 0)
		{
			return;
		}

		RowAddress address = stationList.getViewport().createAddress(change.row, RowAddress.Section.SCROLLABLE);
		WeatherConditionsController.getInstance().displayStation((WeatherStation) stationModel.getView().get(address));
	}

	@DomainRole.Join(membership = ListDataModel.ModelListDomain.class)
	private class PopulateListTask extends UserInterfaceTask
	{
		private WeatherStationRegion currentRegion;

		void start(WeatherStationRegion currentRegion)
		{
			try
			{
				this.currentRegion = currentRegion;
				super.start();
			}
			catch (ConcurrentAccessException e)
			{
				Log.out(Tag.CRITICAL, e, "Failed to populate the station list for region %s", currentRegion);
			}
		}

		@Override
		protected boolean execute()
		{
			ListDataModel.Session session = stationList.getService(ListDataModel.class).createSession(getTransaction(ListDataModelTransaction.class));

			session.clear(Section.SCROLLABLE);

			List<WeatherStation> stations = WeatherDataModel.getInstance().getStations(currentRegion);
			for (WeatherStation station : stations)
			{
				session.add(station);
			}

			// kind of a hack, the selection directive is sometimes not getting sent from the generic selection handler
			GenericTransaction transaction = getTransaction(GenericTransaction.class);
			transaction.addAction(new SetSelectedRowDirective(stationList.getViewport(), 0));

			LabelComposite<Label, ?> stationLabelComponent = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.getStationLabel());
			ChangeTextDirective setLabelText = new ChangeTextDirective(stationLabelComponent.getComponent(), currentRegion.displayName + " Stations");
			transaction.addAction(setLabelText);

			stationLabelComponent.getComponent().requestRepaint();
			stationList.getComponent().requestRepaint();

			return true;
		}
	}
}
