package org.hawkinssoftware.ui.util.weather.control;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.ui.component.ComponentRegistry;
import org.hawkinssoftware.azia.ui.component.scalar.ScrollPaneComposite;
import org.hawkinssoftware.azia.ui.component.text.TextViewportComposite;
import org.hawkinssoftware.azia.ui.component.transaction.state.ChangeTextDirective;
import org.hawkinssoftware.azia.ui.model.list.ListDataModel;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintInstanceDirective;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintRequestManager;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationConditionsDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerControllerDomain;
import org.hawkinssoftware.ui.util.weather.data.StationLoader;
import org.hawkinssoftware.ui.util.weather.data.StationReport;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;

@InvocationConstraint(domains = WeatherViewerControllerDomain.class)
@VisibilityConstraint(domains = { InitializationDomain.class, WeatherViewerControllerDomain.class })
@DomainRole.Join(membership = { WeatherViewerControllerDomain.class, StationConditionsDomain.class })
public class WeatherConditionsController
{
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
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
		conditionsPanel = ComponentRegistry.getInstance().getComposite(WeatherViewerComponents.getConditionsPanel());
	}

	public void displayStation(WeatherStation station)
	{
		populateTask.start(station);
	}

	@DomainRole.Join(membership = { WeatherViewerControllerDomain.class, ListDataModel.ModelListDomain.class })
	private class PopulatePanelTask extends UserInterfaceTask
	{
		private WeatherStation currentStation;
		private StationReport currentReport;

		private final StringBuilder buffer = new StringBuilder();
		private final ReportLine[] reportLines = new ReportLine[] { new Location(), new LatLong(), new DateTime(), new General(), new Wind(), new Atmosphere(),
				new Visibility(), new Coast() };

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
				buffer.setLength(0);

				try
				{
					currentReport = StationLoader.getInstance().loadReport(currentStation);

					for (ReportLine line : reportLines)
					{
						if (line.hasText())
						{
							buffer.append(line.getText());
							buffer.append("\n");
						}
					}
				}
				catch (FileNotFoundException e)
				{
					Log.out(Tag.WARNING, "Warning: no weather conditions found for station %s", currentStation.name);

					currentReport = null;
					buffer.append("Station data is not available.");
				}

				GenericTransaction transaction = getTransaction(GenericTransaction.class);
				transaction.addAction(new ChangeTextDirective(conditionsPanel.getViewport(), buffer.toString()));

				RepaintRequestManager.requestRepaint(new RepaintInstanceDirective(conditionsPanel.getComponent()));
			}
			catch (IOException e)
			{
				Log.out(Tag.DEBUG, e, "Failed to load the weather conditions for station %s", currentStation.name);
			}

			return true;
		}

		private class Location extends ReportLine
		{
			@Override
			String getText()
			{
				return currentStation.name;
			}
		}

		private class LatLong extends ReportLine
		{
			private final String textFrame = "Location: (%s, %s)";
			private final DecimalFormat locationFormat = new DecimalFormat("0.000");

			@Override
			String getText()
			{
				return String.format(textFrame, locationFormat.format(currentReport.latitude), locationFormat.format(currentReport.longitude));
			}
		}

		private class DateTime extends ReportLine
		{
			private final String textFrame = "At %s on %s";
			private final Pattern reportDateTimePattern = Pattern.compile("Last Updated on ([A-Za-z0-9 ]+), ([A-Za-z0-9: ]+)$");
			private Matcher reportDateTime;

			@Override
			boolean hasText()
			{
				reportDateTime = reportDateTimePattern.matcher(currentReport.observationTime);
				return reportDateTime.find();
			}

			@Override
			String getText()
			{
				return String.format(textFrame, reportDateTime.group(2), reportDateTime.group(1));
			}
		}

		private class General extends ReportLine
		{
			private final String temperatureFrame = "%s° F";
			private final String humidityFrame = "Humidity: %s%%";
			private final DecimalFormat temperatureFormat = new DecimalFormat("0");
			private final StringBuilder buffer = new StringBuilder();

			@Override
			boolean hasText()
			{
				return currentReport.temperature >= 0f;
			}

			@Override
			String getText()
			{
				buffer.setLength(0);
				buffer.append(String.format(temperatureFrame, temperatureFormat.format(currentReport.temperature)));

				if (currentReport.weatherSummary != null)
				{
					buffer.append(", ");
					buffer.append(currentReport.weatherSummary);
				}
				if (currentReport.humidity >= 0f)
				{
					buffer.append(", ");
					buffer.append(String.format(humidityFrame, currentReport.humidity));
				}

				return buffer.toString();
			}
		}

		private class Wind extends ReportLine
		{
			private final String directionFrame = "Wind: %s";
			private final String speedFrame = " at %s mph";
			private final String gustFrame = " (Gusts up to %s mph)";
			private final DecimalFormat speedFormat = new DecimalFormat("0");
			private final StringBuilder buffer = new StringBuilder();

			@Override
			boolean hasText()
			{
				return currentReport.windDirection != null;
			}

			@Override
			String getText()
			{
				buffer.setLength(0);
				buffer.append(String.format(directionFrame, currentReport.windDirection.sourceValue));

				if (currentReport.windSpeed >= 0f)
				{
					buffer.append(String.format(speedFrame, speedFormat.format(currentReport.windSpeed)));
				}
				if (currentReport.windGust > 0f)
				{
					buffer.append(String.format(gustFrame, speedFormat.format(currentReport.windGust)));
				}

				return buffer.toString();
			}
		}

		private class Atmosphere extends ReportLine
		{
			private final String atmosphereFrame = "Atmospheric Pressure: %smb";
			private final String dewpointFrame = "Dewpoint: %s° F";
			private final DecimalFormat wholeNumberFormat = new DecimalFormat("0");
			private final StringBuilder buffer = new StringBuilder();

			@Override
			boolean hasText()
			{
				return (currentReport.pressure >= 0f) || (currentReport.dewpoint >= 0f);
			}

			@Override
			String getText()
			{
				buffer.setLength(0);

				if (currentReport.pressure >= 0f)
				{
					buffer.append(String.format(atmosphereFrame, wholeNumberFormat.format(currentReport.pressure)));

					if (currentReport.dewpoint >= 0f)
					{
						buffer.append(", ");
					}
				}

				if (currentReport.dewpoint >= 0f)
				{
					buffer.append(String.format(dewpointFrame, wholeNumberFormat.format(currentReport.dewpoint)));
				}

				return buffer.toString();
			}
		}

		private class Visibility extends ReportLine
		{
			private final String textFrame = "Visibility: %s miles";
			private final DecimalFormat distanceFormat = new DecimalFormat("0.0");

			@Override
			boolean hasText()
			{
				return currentReport.visibility >= 0f;
			}

			String getText()
			{
				return String.format(textFrame, distanceFormat.format(currentReport.visibility));
			}
		}

		private class Coast extends ReportLine
		{
			private final String waveFrame = "Mean Wave Direction: %s";
			private final String tideFrame = "Tide: %sft";
			private final DecimalFormat tideFormat = new DecimalFormat("0.00");
			private final StringBuilder buffer = new StringBuilder();

			@Override
			boolean hasText()
			{
				return (currentReport.waveDirection != null) || (currentReport.tide >= 0f);
			}

			@Override
			String getText()
			{
				buffer.setLength(0);

				if (currentReport.waveDirection != null)
				{
					buffer.append(String.format(waveFrame, currentReport.waveDirection.sourceValue));

					if (currentReport.tide >= 0f)
					{
						buffer.append(", ");
					}
				}

				if (currentReport.tide >= 0f)
				{
					buffer.append(String.format(tideFrame, tideFormat.format(currentReport.tide)));
				}

				return buffer.toString();
			}
		}
	}

	private abstract static class ReportLine
	{
		abstract String getText();

		boolean hasText()
		{
			return true;
		}
	}
}
