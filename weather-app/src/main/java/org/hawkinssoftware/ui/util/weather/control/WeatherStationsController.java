/*
 * Copyright (c) 2011 HawkinsSoftware
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Byron Hawkins of HawkinsSoftware
 */
package org.hawkinssoftware.ui.util.weather.control;

import java.util.List;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTransaction.ActorBasedContributor.PendingTransaction;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.FlyweightCellDomain;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.DesktopWindow;
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
import org.hawkinssoftware.azia.ui.model.list.ListDataModel.ModelListWriteDomain;
import org.hawkinssoftware.azia.ui.model.list.ListDataModelTransaction;
import org.hawkinssoftware.azia.ui.tile.transaction.modify.ModifyLayoutTransaction;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerControllerDomain;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationRegion;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;
import org.hawkinssoftware.ui.util.weather.view.stations.WeatherStationView;

/**
 * @JTourBusStop 2, StationRegionDomain-StationDomain isolation, @VisibilityConstraint on WeatherStationsController:
 * 
 *               This controller can only be seen by members of the WeatherViewerControllerDomain, so of all the classes
 *               in the StationRegionDomain, only the WeatherStationRegionsController may see this controller.
 * 
 * @JTourBusStop 2, StationRegionDomain-StationDomain connection, WeatherStationsController invocation is restricted to
 *               the WeatherViewerControllerDomain:
 * 
 *               Only members of the WeatherViewerControllerDomain may invoke methods on this controller.
 * 
 * @JTourBusStop 3, StationDomain-StationConditionsDomain connection, WeatherStationsController is a member of the
 *               WeatherViewerControllerDomain:
 * 
 *               Other classes in the StationDomain are not allowed to invoke methods on the
 *               WeatherConditionsController, but membership in the WeatherViewerControllerDomain gives this controller
 *               permission to contact it.
 */
@InvocationConstraint(domains = WeatherViewerControllerDomain.class)
@VisibilityConstraint(domains = { InitializationDomain.class, WeatherViewerControllerDomain.class })
@DomainRole.Join(membership = { WeatherViewerControllerDomain.class, StationDomain.class, ListDataModel.ModelListDomain.class, FlyweightCellDomain.class })
public class WeatherStationsController implements UserInterfaceHandler
{
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public static void initialize()
	{
		INSTANCE = new WeatherStationsController();
	}

	@InvocationConstraint(domains = { WeatherViewerControllerDomain.class, WeatherViewerAssemblyDomain.class })
	public static WeatherStationsController getInstance()
	{
		return INSTANCE;
	}

	private static WeatherStationsController INSTANCE;

	private final WeatherStationView view = new WeatherStationView();
	private ScrollPaneComposite<CellViewportComposite<?>> stationList;
	private ListDataModel stationModel;

	private final ThreadLocal<PopulateListTask> populateTasks = new ThreadLocal<PopulateListTask>() {
		@Override
		protected PopulateListTask initialValue()
		{
			return new PopulateListTask();
		}
	};

	private WeatherStationsController()
	{
	}

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public ModifyLayoutTransaction<LayoutKey>.PairHandle assembleView(ModifyLayoutTransaction<LayoutKey> transaction, GenericTransaction adHocTransaction,
			DesktopWindow<LayoutKey> window)
	{
		return view.assembleView(transaction, adHocTransaction, window);
	}

	/**
	 * @JTourBusStop 2, WeatherStation list model usage, WeatherStationsController.initializeView( ) acquires the
	 *               ListDataModel:
	 * 
	 *               This initialization acquires the stationList from the ComponentRegistry, then obtains a reference
	 *               to its ListDataModel.
	 * 
	 * @JTourBusStop 2, WeatherStation list selection response, WeatherStationsController.initializeView( ) installs the
	 *               selection handler:
	 * 
	 *               After acquiring the stationList from the view, this controller installs itself as a handler, making
	 *               it aware of list actions and eligible to contribute actions of its own.
	 * 
	 * @JTourBusStop 5, WeatherStation list model isolation, WeatherStationsController acquires the stationList and
	 *               keeps it private:
	 * 
	 *               This controller is obliged to keep references to the view's members within the StationDomain, so
	 *               that the view remains wholly isolated within the domain.
	 * 
	 * @JTourBusStop 5, WeatherStation list selection isolation, WeatherStationsController acquires the stationList and
	 *               keeps it private:
	 * 
	 *               This controller is obliged to keep references to the view's members within the StationDomain, so
	 *               that the view remains wholly isolated within the domain. By installing itself as a handler, this
	 *               controller becomes aware of stationList actions.
	 */
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public void initializeView()
	{
		stationList = ComponentRegistry.getInstance().getComposite(view.getListAssembly());
		stationModel = stationList.getViewport().getService(ListDataModel.class);
		stationList.getViewport().installHandler(this);
	}

	/**
	 * @JTourBusStop 3, StationRegionDomain-StationDomain isolation, WeatherStationsController.displayStationRegion( )
	 *               is the entry point for displaying sets of stations:
	 * 
	 *               Many classes in the StationDomain participate in the display of stations, but this is the only
	 *               entry point for all of that functionality.
	 * 
	 * @JTourBusStop 3, WeatherStation list model usage, WeatherStationsController.displayStationRegion( ) starts a
	 *               transaction to update the scrollable list of stations:
	 * 
	 *               This method is called by the WeatherStationRegionsController when a region is selected.
	 * 
	 * @JTourBusStop 4, StationRegionDomain-StationDomain connection, WeatherStationController.displayStationRegion( )
	 *               populates the list with stations for a region:
	 * 
	 *               The basis of collaboration between this controller and its peers is the WeatherStationRegion
	 *               itself. This controller does not know if the requested region was selected in a list, or is
	 *               displayed by default, or for any other reason. All it knows is that other controllers may wish to
	 *               have stations for a region displayed, and this method serves the purpose.
	 */
	void displayStationRegion(WeatherStationRegion stationRegion)
	{
		populateTasks.get().start(stationRegion);
	}

	/**
	 * @JTourBusStop 3, WeatherStation list selection response, WeatherStationsController.selectionChanging( ) receives
	 *               notification of changes in the station list's selection:
	 * 
	 *               The UserInterfaceTransaction engine invokes this method whenever a SetSelectedRowDirective occurs
	 *               for any instance handled by this controller; in this case, the stationList. No actions are
	 *               contributed to the PendingTransaction here, but the WeatherConditionsController is prompted to
	 *               display the selected station's current weather report.
	 * 
	 * @JTourBusStop 6, WeatherStation list selection isolation, WeatherStationsController.selectionChanging( ) receives
	 *               notification of changes in the station list's selection:
	 * 
	 *               This controller contributes no actions in response to the selection, but prompts the
	 *               WeatherConditionsController to display the current weather report for the selected station. This
	 *               delegation could be considered a violation of the isolation requirement, because the
	 *               WeatherConditionsController is outside the StationDomain and is participating in the execution path
	 *               initiated at station list selection. In this design it is not considered a violation because the
	 *               WeatherConditionsController is invoked on the basis of a general contract about displaying station
	 *               reports, and no artifact of station list selection is included in the invocation.
	 * 
	 * 
	 * @JTourBusStop 1, StationDomain-StationConditionsDomain connection, WeatherStationsController contacts the
	 *               WeatherConditionsController on station selection change:
	 * 
	 *               When the selected station changes in the scrollable list of stations, this method is invoked by the
	 *               UserInterfaceTransaction engine. In response, this method prompts the WeatherConditionsController
	 *               to display the weather report for the newly selected station.
	 */
	public void selectionChanging(SetSelectedRowDirective.Notification change, PendingTransaction transaction)
	{
		if (change.row < 0)
		{
			return;
		}

		RowAddress address = stationList.getViewport().createAddress(change.row, RowAddress.Section.SCROLLABLE);
		WeatherConditionsController.getInstance().displayStation((WeatherStation) stationModel.get(address));
	}

	/**
	 * @JTourBusStop 4, WeatherStation list model usage, WeatherStationsController.PopulateListTask - obtains stations
	 *               for the selected region and displays them in the station list:
	 * 
	 *               In Azia, all mutable data access must occur in a transaction. A ListDataModelTransaction is
	 *               initiated in this implementation of UserInterfaceTask for changing the list content.
	 */
	@DomainRole.Join(membership = { StationDomain.class, WeatherViewerControllerDomain.class, ModelListWriteDomain.class })
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

		/**
		 * @JTourBusStop 5, WeatherStation list model usage, Stations for the selected region are added to the
		 *               scrollable list:
		 * 
		 *               A ListDataModelTransaction is acquired, and a session is created from the ListDataModel and
		 *               bound to that transaction. Changes to the session are visible to all participants in the
		 *               transaction, and are applied to the ListDataModel on commit.
		 */
		@Override
		protected boolean execute()
		{
			ListDataModel.Session session = stationModel.createSession(getTransaction(ListDataModelTransaction.class));
			session.clear(Section.SCROLLABLE);
			List<WeatherStation> stations = WeatherDataModel.getInstance().getStations(currentRegion);
			for (WeatherStation station : stations)
			{
				session.add(station);
			}

			GenericTransaction transaction = getTransaction(GenericTransaction.class);
			LabelComposite<Label, ?> stationLabelComponent = ComponentRegistry.getInstance().getComposite(view.getLabelAssembly());
			ChangeTextDirective setLabelText = new ChangeTextDirective(stationLabelComponent.getComponent(), currentRegion.displayName + " Stations");
			transaction.addAction(setLabelText);

			stationLabelComponent.getComponent().requestRepaint();
			stationList.getComponent().requestRepaint();

			return true;
		}
	}
}
