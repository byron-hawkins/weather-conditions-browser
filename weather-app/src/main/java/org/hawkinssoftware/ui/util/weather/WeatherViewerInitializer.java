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
package org.hawkinssoftware.ui.util.weather;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import org.hawkinssoftware.azia.core.action.TransactionRegistry;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask.ConcurrentAccessException;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.control.WeatherConditionsController;
import org.hawkinssoftware.ui.util.weather.control.WeatherStationRegionsController;
import org.hawkinssoftware.ui.util.weather.control.WeatherStationsController;
import org.hawkinssoftware.ui.util.weather.data.StationLoader;
import org.hawkinssoftware.ui.util.weather.data.WeatherDataModel;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.data.WeatherStationRegion;

class WeatherViewerInitializer
{
	public static WeatherViewerInitializer getInstance()
	{
		return INSTANCE;
	}

	private static final WeatherViewerInitializer INSTANCE = new WeatherViewerInitializer();

	/**
	 * @JTourBusStop 1, WeatherStation loading, WeatherViewerInitializer.startApplication( ) invokes station loading:
	 * 
	 *               The WeatherViewerInitializer chooses to invoke the loading of stations after the views are
	 *               initialized. Of course it must also be done before the scrollable stations list can be populated.
	 * 
	 * @JTourBusStop 1, WeatherStation lifecycle, WeatherStation loading is requested at application startup:
	 * @JTourBusStop 1, WeatherStationRegion lifecycle, WeatherStationRegion loading is requested at application
	 *               startup:
	 */
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public void startApplication() throws ConcurrentAccessException
	{
		WeatherStationsController.getInstance().initializeView();
		WeatherStationRegionsController.getInstance().initializeView();
		WeatherConditionsController.getInstance().initializeView();

		TransactionRegistry.executeTask(new LoadStationsTask());
		WeatherStationRegionsController.getInstance().populateView();
	}

	private class RegionSorter implements Comparator<WeatherStationRegion>
	{
		public int compare(WeatherStationRegion first, WeatherStationRegion second)
		{
			return first.displayName.compareTo(second.displayName);
		}
	}

	private class StationSorter implements Comparator<WeatherStation>
	{
		public int compare(WeatherStation first, WeatherStation second)
		{
			return first.name.compareTo(second.name);
		}
	}

	/**
	 * @JTourBusStop 2, WeatherStation loading, WeatherViewerInitializer.LoadStationsTask directly contacts the
	 *               StationLoader:
	 * 
	 *               This task is also a member of the InitializationDomain, and is responsible for making direct
	 *               contact with the StationLoader.
	 */
	@DomainRole.Join(membership = InitializationDomain.class)
	private class LoadStationsTask extends UserInterfaceTask
	{
		/**
		 * @JTourBusStop 2, WeatherStation lifecycle, WeatherStations are loaded and installed in the WeatherDataModel:
		 * @JTourBusStop 2, WeatherStationRegion lifecycle, WeatherStationRegions are loaded and installed in the
		 *               WeatherDataModel:
		 */
		@Override
		protected boolean execute()
		{
			try
			{
				Map<WeatherStationRegion, List<WeatherStation>> stations = StationLoader.getInstance().loadStations();
				for (List<WeatherStation> regionStations : stations.values())
				{
					Collections.sort(regionStations, new StationSorter());
				}

				List<WeatherStationRegion> regions = new ArrayList<WeatherStationRegion>(stations.keySet());
				Collections.sort(regions, new RegionSorter());

				WeatherDataModel.getInstance().installData(regions, stations);

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
