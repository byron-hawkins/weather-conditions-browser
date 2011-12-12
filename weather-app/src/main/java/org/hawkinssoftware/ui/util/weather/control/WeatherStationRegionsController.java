package org.hawkinssoftware.ui.util.weather.control;

import java.util.List;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask.ConcurrentAccessException;
import org.hawkinssoftware.azia.core.action.UserInterfaceTransaction.ActorBasedContributor.PendingTransaction;
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.FlyweightCellDomain;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.DesktopWindow;
import org.hawkinssoftware.azia.ui.component.UserInterfaceHandler;
import org.hawkinssoftware.azia.ui.component.cell.CellViewportComposite;
import org.hawkinssoftware.azia.ui.component.cell.transaction.SetSelectedRowDirective;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.model.RowAddress;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel.ModelListWriteDomain;
import org.hawkinssoftware.azia.ui.model.list.ListDataModelTransaction;
import org.hawkinssoftware.azia.ui.tile.transaction.modify.ModifyLayoutTransaction;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationRegionDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerControllerDomain;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationRegion;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;
import org.hawkinssoftware.ui.util.weather.view.regions.WeatherStationRegionView;

/**
 * @JTourBusStop 3, StationRegionDomain-StationDomain connection, WeatherStationRegionsController is a member of the
 *               WeatherViewerControllerDomain:
 * 
 *               Other classes in the StationRegionDomain are not allowed to invoke methods on the
 *               WeatherStationsController, but membership in the WeatherViewerControllerDomain gives this controller
 *               permission to contact it.
 */
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

	@InvocationConstraint(domains = { WeatherViewerControllerDomain.class, WeatherViewerAssemblyDomain.class })
	public static WeatherStationRegionsController getInstance()
	{
		return INSTANCE;
	}

	private static WeatherStationRegionsController INSTANCE;

	private final WeatherStationRegionView view = new WeatherStationRegionView();
	private ScrollPaneComposite<CellViewportComposite<?>> regionList;
	private ListDataModel regionModel;

	private PopulateListTask populateTask = new PopulateListTask();

	private WeatherStationRegionsController()
	{
	}

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public ModifyLayoutTransaction<LayoutKey>.PairHandle assembleView(ModifyLayoutTransaction<LayoutKey> transaction, GenericTransaction adHocTransaction,
			DesktopWindow<LayoutKey> window)
	{
		return view.assembleView(transaction, adHocTransaction, window);
	}

	/**
	 * @JTourBusStop 2, WeatherStationRegion list model usage, WeatherStationRegionsController.initializeView( )
	 *               acquires the ListDataModel:
	 * 
	 *               This initialization acquires the stationList from the ComponentRegistry, then obtains a reference
	 *               to its ListDataModel.
	 * 
	 * @JTourBusStop 2, WeatherStationRegion list selection response, WeatherStationRegionsController.initializeView( )
	 *               installs the selection handler:
	 * 
	 *               After acquiring the stationList from the view, this controller installs itself as a handler, making
	 *               it aware of list actions and eligible to contribute actions of its own.
	 * 
	 * @JTourBusStop 5, WeatherStationRegion list model isolation, WeatherStationRegionsController acquires the
	 *               stationList and keeps it private:
	 * 
	 *               This controller is obliged to keep references to the view's members within the StationRegionDomain,
	 *               so that the view remains wholly isolated within the domain.
	 * 
	 * @JTourBusStop 5, WeatherStationRegion list selection isolation, WeatherStationRegionsController acquires the
	 *               stationList and keeps it private:
	 * 
	 *               This controller is obliged to keep references to the view's members within the StationRegionDomain,
	 *               so that the view remains wholly isolated within the domain. By installing itself as a handler, this
	 *               controller becomes aware of stationList actions.
	 */
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public void initializeView()
	{
		regionList = ComponentRegistry.getInstance().getComposite(view.getListAssembly());
		regionModel = regionList.getViewport().getService(ListDataModel.class);
		regionList.getViewport().installHandler(this);
	}

	/**
	 * @JTourBusStop 3, WeatherStationRegion list model usage, WeatherStationRegionsController.populateView( ) starts a
	 *               transaction to populate the scrollable list of stations:
	 * 
	 *               This method is called by the WeatherViewerInitializer during application startup, to populate the
	 *               list of station regions. The list never changes during the course of application usage.
	 * 
	 * @JTourBusStop 9, Application assembly, WeatherViewerAssemblyDomain invokes
	 *               WeatherStationRegionsController.populateView( ):
	 * 
	 *               The scrollable list of station regions is only populated once, at application startup, and for this
	 *               reason it is owned by the WeatherViewerAssemblyDomain.
	 */
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public void populateView() throws ConcurrentAccessException
	{
		populateTask.start();
	}

	/**
	 * @JTourBusStop 3, WeatherStationRegion list selection response, WeatherStationRegionsController.selectionChanging(
	 *               ) receives notification of changes in the region list's selection:
	 * 
	 *               The UserInterfaceTransaction engine invokes this method whenever a SetSelectedRowDirective occurs
	 *               for any instance handled by this controller; in this case, the stationList. No actions are
	 *               contributed to the PendingTransaction here, but the WeatherStationsController is prompted to
	 *               display the stations for the selected region.
	 * 
	 * @JTourBusStop 6, WeatherStationRegion list selection isolation,
	 *               WeatherStationRegionsController.selectionChanging( ) receives notification of changes in the region
	 *               list's selection:
	 * 
	 *               This controller contributes no actions in response to the selection, but prompts the
	 *               WeatherStationsController to display the stations for the selected region. This delegation could be
	 *               considered a violation of the isolation requirement, because the WeatherStationsController is
	 *               outside the StationRegionDomain and is participating in the execution path initiated at region list
	 *               selection. In this design it is not considered a violation because the WeatherRegionController is
	 *               invoked on the basis of a general contract about displaying stations for a region, and no artifact
	 *               of region list selection is included in the invocation.
	 * 
	 * @JTourBusStop 1, StationRegionDomain-StationDomain connection, WeatherStationRegionsController contacts the
	 *               WeatherStationsController on region selection change:
	 * 
	 *               When the selected region changes in the scrollable list of regions, this method is invoked by the
	 *               UserInterfaceTransaction engine. In response, this method prompts the WeatherStationsController to
	 *               display the stations for the newly selected region.
	 */
	public void selectionChanging(SetSelectedRowDirective.Notification change, PendingTransaction transaction)
	{
		if (change.row < 0)
		{
			return;
		}

		RowAddress address = regionList.getViewport().createAddress(change.row, RowAddress.Section.SCROLLABLE);
		WeatherStationsController.getInstance().displayStationRegion((WeatherStationRegion) regionModel.get(address));
	}

	/**
	 * @JTourBusStop 4, WeatherStationRegion list model usage, WeatherStationRegionsController.PopulateListTask -
	 *               displays the complete set of station regions in the scrollable list:
	 * 
	 *               All mutable data access must occur in a transaction. A ListDataModelTransaction initiated here in
	 *               this implementation of UserInterfaceTask for changing the list content.
	 */
	@DomainRole.Join(membership = { StationRegionDomain.class, WeatherViewerControllerDomain.class, ModelListWriteDomain.class })
	private class PopulateListTask extends UserInterfaceTask
	{
		/**
		 * @JTourBusStop 5, WeatherStationRegion list model usage, All regions are added to the scrollable list:
		 * 
		 *               A ListDataModelTransaction is acquired, and a session is created from the ListDataModel and
		 *               bound to that transaction. Changes to the session are visible to all participants in the
		 *               transaction, and are applied to the ListDataModel on commit.
		 */
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
