package org.hawkinssoftware.ui.util.weather.data;

import nu.xom.Element;

import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationConditionsDomain;

/**
 * @JTourBusStop 3, StationReport immutability, StationReport has only final fields:
 * 
 *               Every field of the StationReport is itself immutable or a primitive.
 */
@DomainRole.Join(membership = StationConditionsDomain.class)
public class StationReport
{
	private enum ReportTag
	{
		ID("station_id"),
		LATITUDE("latitude"),
		LONGITUDE("longitude"),
		OBSERVATION_TIME("observation_time"),
		WEATHER_SUMMARY("weather"),
		TEMPERATURE_FAHRENHEIT("temp_f"),
		HUMIDITY("relative_humidity"),
		WIND_SUMMARY("wind_string"),
		WIND_DIRECTION("wind_dir"),
		WIND_SPEED("wind_mph"),
		WIND_GUST("wind_gust_mph"),
		WAVE_DIRECTION("mean_wave_dir"),
		TIDE("tide_ft"),
		PRESSURE("pressure_mb"),
		DEWPOINT_FAHRENHEIT("dewpoint_f"),
		VISIBILITY("visibility_mi");

		final String element;

		private ReportTag(String element)
		{
			this.element = element;
		}
	}

	public enum Direction
	{
		NORTH("North"),
		NORTHEAST("Northeast"),
		EAST("East"),
		SOUTHEAST("Southeast"),
		SOUTH("South"),
		SOUTHWEST("Southwest"),
		WEST("West"),
		NORTHWEST("Northwest"),
		VARIABLE("Variable");

		public final String sourceValue;

		private Direction(String sourceValue)
		{
			this.sourceValue = sourceValue;
		}

		static Direction forSourceValue(String sourceValue)
		{
			for (Direction direction : Direction.values())
			{
				if (direction.sourceValue.equals(sourceValue))
				{
					return direction;
				}
			}

			Log.out(Tag.WARNING, "Unknown direction source value '%s'", sourceValue);
			return null;
		}
	}

	public final String stationId;
	public final float latitude;
	public final float longitude;
	public final String observationTime;
	public final String weatherSummary;
	public final float temperature;
	public final int humidity;
	public final String windSummary;
	public final Direction windDirection;
	public final float windSpeed;
	public final float windGust;
	public final Direction waveDirection;
	public final float tide;
	public final float pressure;
	public final float dewpoint;
	public final float visibility;

	StationReport(Element reportElement)
	{
		stationId = loadString(reportElement, ReportTag.ID);
		latitude = loadFloat(reportElement, ReportTag.LATITUDE);
		longitude = loadFloat(reportElement, ReportTag.LONGITUDE);
		observationTime = loadString(reportElement, ReportTag.OBSERVATION_TIME);
		weatherSummary = loadString(reportElement, ReportTag.WEATHER_SUMMARY);
		temperature = loadFloat(reportElement, ReportTag.TEMPERATURE_FAHRENHEIT);
		humidity = loadInt(reportElement, ReportTag.HUMIDITY);
		windSummary = loadString(reportElement, ReportTag.WIND_SUMMARY);
		windDirection = loadDirection(reportElement, ReportTag.WIND_DIRECTION);
		windSpeed = loadFloat(reportElement, ReportTag.WIND_SPEED);
		windGust = loadFloat(reportElement, ReportTag.WIND_GUST);
		waveDirection = loadDirection(reportElement, ReportTag.WAVE_DIRECTION);
		tide = loadFloat(reportElement, ReportTag.TIDE);
		pressure = loadFloat(reportElement, ReportTag.PRESSURE);
		dewpoint = loadFloat(reportElement, ReportTag.DEWPOINT_FAHRENHEIT);
		visibility = loadFloat(reportElement, ReportTag.VISIBILITY);
	}

	private String loadString(Element reportElement, ReportTag tag)
	{
		Element element = reportElement.getFirstChildElement(tag.element);
		if (element == null)
		{
			return null;
		}
		else
		{
			return element.getValue();
		}
	}

	private int loadInt(Element reportElement, ReportTag tag)
	{
		Element element = reportElement.getFirstChildElement(tag.element);
		if (element == null)
		{
			return -1;
		}
		else
		{
			return Integer.parseInt(element.getValue());
		}
	}

	private float loadFloat(Element reportElement, ReportTag tag)
	{
		Element element = reportElement.getFirstChildElement(tag.element);
		if (element == null)
		{
			return -1f;
		}
		else
		{
			return Float.parseFloat(element.getValue());
		}
	}

	private Direction loadDirection(Element reportElement, ReportTag tag)
	{
		Element element = reportElement.getFirstChildElement(tag.element);
		if (element == null)
		{
			return null;
		}
		else
		{
			return Direction.forSourceValue(element.getValue());
		}
	}

	@Override
	public String toString()
	{
		return "Weather report for " + stationId + ": " + temperature + "F";
	}
}
