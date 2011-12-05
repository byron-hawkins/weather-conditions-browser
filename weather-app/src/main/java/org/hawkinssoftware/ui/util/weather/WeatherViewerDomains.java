package org.hawkinssoftware.ui.util.weather;

import org.hawkinssoftware.rns.core.role.DomainRole;

public interface WeatherViewerDomains
{
	public static class StationRegionDomain extends DomainRole
	{
		@DomainRole.Instance
		public static StationDomain INSTANCE = new StationDomain();
	}

	public static class StationDomain extends DomainRole
	{
		@DomainRole.Instance
		public static StationDomain INSTANCE = new StationDomain();
	}

	public static class StationConditionsDomain extends DomainRole
	{
		@DomainRole.Instance
		public static StationDomain INSTANCE = new StationDomain();
	}
}
