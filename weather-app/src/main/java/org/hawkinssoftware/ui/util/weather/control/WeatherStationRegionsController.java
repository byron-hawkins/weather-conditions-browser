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
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.model.list.ListDataModelTransaction;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationRegionDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerControllerDomain;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationRegion;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

@InvocationConstraint(domains = WeatherViewerControllerDomain.class)
@VisibilityConstraint(domains = { InitializationDomain.class, WeatherViewerControllerDomain.class })
@DomainRole.Join(membership = { WeatherViewerControllerDomain.class, StationRegionDomain.class, ListDataModel.ModelListDomain.class, FlyweightCellDomain.class })
public class WeatherStationRegionsController implements UserInterfaceHandler
{
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public static void initialize()
	{
		INSTANCE = new WeatherStationRegionsController();
	}

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public static WeatherStationRegionsController getInstance()
	{
		return INSTANCE;
	}

	private static WeatherStationRegionsController INSTANCE;

	private final ScrollPaneComposite<CellViewportComposite<?>> regionList;
	private final ListDataModel regionModel;
	private final PopulateListTask populateTask = new PopulateListTask();

	@InvocationConstraint(domains = AssemblyDomain.class)
	private WeatherStationRegionsController()
	{
		regionList = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.getStationRegionList());
		regionModel = regionList.getViewport().getService(ListDataModel.class);
		regionList.getViewport().installHandler(this);
	}

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public void initializeView() throws ConcurrentAccessException
	{
		populateTask.start();
	}

	public void selectionChanging(SetSelectedRowDirective.Notification change, PendingTransaction transaction)
	{
		if (change.row < 0)
		{
			return;
		}

		RowAddress address = regionList.getViewport().createAddress(change.row, RowAddress.Section.SCROLLABLE);
		WeatherStationsController.getInstance().displayStationRegion((WeatherStationRegion) regionModel.get(address));
	}

	@DomainRole.Join(membership = ListDataModel.ModelListDomain.class)
	private class PopulateListTask extends UserInterfaceTask
	{
		@Override
		protected boolean execute()
		{
			ListDataModel.Session session = regionModel.createSession(getTransaction(ListDataModelTransaction.class));
			List<WeatherStationRegion> regions = WeatherDataModel.getInstance().getRegions();
			for (WeatherStationRegion region : regions)
			{
				session.add(region);
			}

			regionList.getComponent().requestRepaint();

			return true;
		}
	}
}
