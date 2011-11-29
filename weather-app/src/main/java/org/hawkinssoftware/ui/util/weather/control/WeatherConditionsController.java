package org.hawkinssoftware.ui.util.weather.control;

import java.io.FileNotFoundException;
import java.io.IOException;

import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.AssemblyDomain;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.component.text.TextViewportComposite;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintInstanceDirective;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintRequestManager;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.data.StationLoader;
import org.hawkinssoftware.ui.util.weather.data.StationReport;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

public class WeatherConditionsController
{
	@InvocationConstraint(domains = AssemblyDomain.class)
	public static void initialize()
	{
		INSTANCE = new WeatherConditionsController();
	}

	public static WeatherConditionsController getInstance()
	{
		return INSTANCE;
	}

	private static WeatherConditionsController INSTANCE;

	private final ScrollPaneComposite<TextViewportComposite> conditionsPanel;

	private final PopulatePanelTask populateTask = new PopulatePanelTask();

	public WeatherConditionsController()
	{
		conditionsPanel = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.STATION_DATA_ASSEMBLY);
	}

	public void displayStation(WeatherStation station)
	{
		populateTask.start(station);
	}

	@DomainRole.Join(membership = ListDataModel.ModelListDomain.class)
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
				StationReport report = StationLoader.getInstance().loadReport(currentStation);
				Log.out(Tag.DEBUG, "Display weather report %s", report);

				RepaintRequestManager.requestRepaint(new RepaintInstanceDirective(conditionsPanel.getComponent()));
			}
			catch (FileNotFoundException e)
			{
				Log.out(Tag.WARNING, "Warning: no weather conditions found for station %s", currentStation.name);
			}
			catch (IOException e)
			{
				Log.out(Tag.DEBUG, e, "Failed to load the weather conditions for station %s", currentStation.name);
			}
			
			return true;
		}
	}
}
