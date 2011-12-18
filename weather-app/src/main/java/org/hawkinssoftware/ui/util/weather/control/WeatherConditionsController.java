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

import java.io.FileNotFoundException;
import java.io.IOException;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTransactionDomains.TransactionParticipant;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.DesktopWindow;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.component.text.TextViewportComposite;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.tile.transaction.modify.ModifyLayoutTransaction;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationConditionsDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerControllerDomain;
import org.hawkinssoftware.ui.util.weather.data.StationLoader;
import org.hawkinssoftware.ui.util.weather.data.StationReport;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;
import org.hawkinssoftware.ui.util.weather.view.conditions.DisplayStationReportNotification;
import org.hawkinssoftware.ui.util.weather.view.conditions.WeatherConditionsView;

/**
 * @JTourBusStop 2, StationDomain-StationConditionsDomain connection, WeatherConditionsController invocation is
 *               restricted to the WeatherViewerControllerDomain:
 * 
 *               Only members of the WeatherViewerControllerDomain may invoke methods on this controller.
 */
@InvocationConstraint(domains = WeatherViewerControllerDomain.class)
@VisibilityConstraint(domains = { InitializationDomain.class, WeatherViewerControllerDomain.class })
@DomainRole.Join(membership = { WeatherViewerControllerDomain.class, StationConditionsDomain.class, ListDataModel.ModelListDomain.class,
		TransactionParticipant.class })
public class WeatherConditionsController
{
	/**
	 * @JTourBusStop 3, Application assembly, WeatherViewerAssembly invokes WeatherConditionsController.initialize( ):
	 * 
	 *               As a matter of domain responsibility, which was designated in the application design, this
	 *               constructor invocation should be owned by the assembly domain, not by a controller. It is allowed
	 *               here because the @InvocationConstraint makes the initialize() method effectively a tangent of the
	 *               assembly domain. The same goes for initialize() methods on other controllers.
	 */
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public static void initialize()
	{
		INSTANCE = new WeatherConditionsController();
	}

	@InvocationConstraint(domains = { WeatherViewerControllerDomain.class, WeatherViewerAssemblyDomain.class })
	public static WeatherConditionsController getInstance()
	{
		return INSTANCE;
	}

	private static WeatherConditionsController INSTANCE;

	private final WeatherConditionsView view = new WeatherConditionsView();
	private ScrollPaneComposite<TextViewportComposite> conditionsPanel;

	private final PopulatePanelTask populateTask = new PopulatePanelTask();

	private WeatherConditionsController()
	{
	}

	/**
	 * @JTourBusStop 4, Application assembly, WeatherViewerAssembly invokes WeatherConditionsController.assembleView( ):
	 * 
	 *               This assembly method is also a tangent of the assembly domain, as are the parallel methods on the
	 *               other controllers.
	 */
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public ModifyLayoutTransaction<LayoutKey>.PairHandle assembleView(ModifyLayoutTransaction<LayoutKey> transaction, GenericTransaction adHocTransaction,
			DesktopWindow<LayoutKey> window)
	{
		return view.assembleView(transaction, adHocTransaction, window);
	}

	/**
	 * @JTourBusStop 2, StationReport display, WeatherConditionsController.initializeView( ) acquires the text area:
	 * 
	 *               This initialization acquires the conditionsPanel from the ComponentRegistry.
	 * 
	 * @JTourBusStop 4, StationReport display isolation, WeatherConditionsController acquires the conditionsPanel and
	 *               keeps it private:
	 * 
	 *               This controller is obliged to keep references to the view's members within the
	 *               StationConditionsDomain, so that the view remains wholly isolated within the domain.
	 */
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public void initializeView()
	{
		conditionsPanel = ComponentRegistry.getInstance().getComposite(view.getTextAreaAssembly());
	}

	/**
	 * @JTourBusStop 3, StationReport display, WeatherConditionsController.displayStation( ) starts a transaction to
	 *               update the station report text area:
	 * 
	 *               This method is called by the WeatherStationsController when a station is selected.
	 * 
	 * @JTourBusStop 4, StationDomain-StationConditionsDomain connection, WeatherConditionsController.displayStation( ):
	 * 
	 *               The basis of collaboration between this controller and its peers is the WeatherStation itself. This
	 *               controller does not know if the requested station was selected in a list, or is displayed by
	 *               default, or for any other reason. All it knows is that other controllers may wish to have weather
	 *               conditions for a station displayed, and this method serves the purpose.
	 * 
	 * @JTourBusStop 2, StationDomain-StationConditionsDomain isolation, @VisibilityConstraint on
	 *               WeatherConditionsController:
	 * 
	 *               This controller can only be seen by members of the WeatherViewerControllerDomain, so of all the
	 *               classes in the StationDomain, only the WeatherStationsController may see this controller.
	 * 
	 * @JTourBusStop 3, StationDomain-StationConditionsDomain isolation, WeatherConditionsController.displayStation( )
	 *               is the entry point for displaying a station weather report:
	 * 
	 *               Many classes in the StationConditionsDomain participate in the display of weather reports, but this
	 *               is the only entry point for all of that functionality.
	 * 
	 * 
	 * @JTourBusStop 1, StationReport loading, WeatherConditionsController.displayStation( ) invokes weather report
	 *               loading:
	 * 
	 *               The WeatherConditionsController chooses to invoke the loading of weather reports upon request.
	 */
	void displayStation(WeatherStation station)
	{
		populateTask.start(station);
	}

	/**
	 * @JTourBusStop 4, StationReport display, WeatherConditionsController.PopulateListTask - obtains the selected
	 *               station's current weather report for display:
	 * 
	 *               In Azia, all mutable data access must occur in a transaction. A GenericTransaction is obtained in
	 *               this implementation of UserInterfaceTask, and the report display process is initiated by sending a
	 *               DisplayStationReportNotification to the text area viewport.
	 * 
	 * @JTourBusStop 2, StationReport loading, WeatherConditionsController.PopulatePanelTask directly contacts the
	 *               StationLoader:
	 * 
	 *               This task is also a member of the WeatherViewerControllerDomain, and is responsible for making
	 *               direct contact with the StationLoader.
	 */
	@DomainRole.Join(membership = { StationConditionsDomain.class, WeatherViewerControllerDomain.class, ListDataModel.ModelListDomain.class })
	private class PopulatePanelTask extends UserInterfaceTask
	{
		private WeatherStation currentStation;

		void start(WeatherStation currentStation)
		{
			try
			{
				this.currentStation = currentStation;
				super.start();
			}
			catch (ConcurrentAccessException e)
			{
				Log.out(Tag.CRITICAL, e, "Failed to populate the weather conditions for station %s", currentStation);
			}
		}

		@Override
		protected boolean execute()
		{
			try
			{
				StationReport report = null;

				try
				{
					report = StationLoader.getInstance().loadReport(currentStation);
				}
				catch (FileNotFoundException e)
				{
					Log.out(Tag.WARNING, "Warning: no weather conditions found for station %s", currentStation.name);
				}

				GenericTransaction transaction = getTransaction(GenericTransaction.class);
				transaction.addNotification(new DisplayStationReportNotification(conditionsPanel.getViewport(), currentStation, report));
			}
			catch (IOException e)
			{
				Log.out(Tag.DEBUG, e, "Failed to load the weather conditions for station %s", currentStation.name);
			}

			return true;
		}
	}
}
