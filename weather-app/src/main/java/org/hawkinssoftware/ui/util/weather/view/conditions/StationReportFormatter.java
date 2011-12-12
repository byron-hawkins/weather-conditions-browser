package org.hawkinssoftware.ui.util.weather.view.conditions;

import java.text.DecimalFormat;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationConditionsDomain;
import org.hawkinssoftware.ui.util.weather.data.StationReport;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;

/**
 * @JTourBusStop 3, StationReport formatting isolation, StationReportFormatter visible only within the
 *               StationConditionsDomain:
 * 
 *               The package-protected access modifier limits usage of this formatter to classes inside this package,
 *               and all public classes inside this package have visibility constrained to the StationConditionsDomain.
 * 
 * @JTourBusStop 5, StationDomain-StationConditionsDomain isolation, StationReportFormatter isolated within the
 *               StationConditionsDomain:
 * 
 *               This formatter could be used to display weather reports in some other component, but the
 *               package-private declaration makes it only visible to the WeatherConditionsView, which we just saw is
 *               itself restricted to the StationConditionsDomain. Having isolated all station list rendering within the
 *               StationConditionsDomain, the only way for outside classes to collaborate with this functionality is to
 *               go through the WeatherConditionsController.
 */
@DomainRole.Join(membership = StationConditionsDomain.class)
class StationReportFormatter
{
	private static final ThreadLocal<StationReportFormatter> INSTANCES = new ThreadLocal<StationReportFormatter>() {
		@Override
		protected StationReportFormatter initialValue()
		{
			return new StationReportFormatter();
		}
	};

	/**
	 * @JTourBusStop 2, StationReport formatting, StationReportFormatter.formatReport( ) transforms the station report
	 *               into a displayable text string:
	 */
	static String formatReport(WeatherStation station, StationReport report)
	{
		return INSTANCES.get().serializeReport(station, report);
	}

	private final ReportLine[] reportLines = new ReportLine[] { new Location(), new LatLong(), new DateTime(), new General(), new Wind(), new Atmosphere(),
			new Visibility(), new Coast() };

	private WeatherStation station;
	private StationReport report;

	private final StringBuilder buffer = new StringBuilder();

	private StationReportFormatter()
	{
	}

	private String serializeReport(WeatherStation station, StationReport report)
	{
		this.station = station;
		this.report = report;

		buffer.setLength(0);
		for (ReportLine line : reportLines)
		{
			if (line.hasText())
			{
				buffer.append(line.getText());
				buffer.append("\n");
			}
		}
		return buffer.toString();
	}

	private class Location extends ReportLine
	{
		@Override
		String getText()
		{
			return station.name;
		}
	}

	private class LatLong extends ReportLine
	{
		private final String textFrame = "Location: (%s, %s)";
		private final DecimalFormat locationFormat = new DecimalFormat("0.000");

		@Override
		String getText()
		{
			return String.format(textFrame, locationFormat.format(report.latitude), locationFormat.format(report.longitude));
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
			reportDateTime = reportDateTimePattern.matcher(report.observationTime);
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
			return report.temperature >= 0f;
		}

		@Override
		String getText()
		{
			buffer.setLength(0);
			buffer.append(String.format(temperatureFrame, temperatureFormat.format(report.temperature)));

			if (report.weatherSummary != null)
			{
				buffer.append(", ");
				buffer.append(report.weatherSummary);
			}
			if (report.humidity >= 0f)
			{
				buffer.append(", ");
				buffer.append(String.format(humidityFrame, report.humidity));
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
			return report.windDirection != null;
		}

		@Override
		String getText()
		{
			buffer.setLength(0);
			buffer.append(String.format(directionFrame, report.windDirection.sourceValue));

			if (report.windSpeed >= 0f)
			{
				buffer.append(String.format(speedFrame, speedFormat.format(report.windSpeed)));
			}
			if (report.windGust > 0f)
			{
				buffer.append(String.format(gustFrame, speedFormat.format(report.windGust)));
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
			return (report.pressure >= 0f) || (report.dewpoint >= 0f);
		}

		@Override
		String getText()
		{
			buffer.setLength(0);

			if (report.pressure >= 0f)
			{
				buffer.append(String.format(atmosphereFrame, wholeNumberFormat.format(report.pressure)));

				if (report.dewpoint >= 0f)
				{
					buffer.append(", ");
				}
			}

			if (report.dewpoint >= 0f)
			{
				buffer.append(String.format(dewpointFrame, wholeNumberFormat.format(report.dewpoint)));
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
			return report.visibility >= 0f;
		}

		String getText()
		{
			return String.format(textFrame, distanceFormat.format(report.visibility));
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
			return (report.waveDirection != null) || (report.tide >= 0f);
		}

		@Override
		String getText()
		{
			buffer.setLength(0);

			if (report.waveDirection != null)
			{
				buffer.append(String.format(waveFrame, report.waveDirection.sourceValue));

				if (report.tide >= 0f)
				{
					buffer.append(", ");
				}
			}

			if (report.tide >= 0f)
			{
				buffer.append(String.format(tideFrame, tideFormat.format(report.tide)));
			}

			return buffer.toString();
		}
	}

	private abstract class ReportLine
	{
		abstract String getText();

		boolean hasText()
		{
			return true;
		}
	}

}
