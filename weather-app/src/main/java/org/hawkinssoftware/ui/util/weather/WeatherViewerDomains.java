package org.hawkinssoftware.ui.util.weather;

import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.AssemblyDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;

public interface WeatherViewerDomains
{
	public static class DataDomain extends DomainRole
	{
		@DomainRole.Instance
		public static DataDomain INSTANCE = new DataDomain();
	}

	public static class StationRegionDomain extends DataDomain
	{
		@DomainRole.Instance
		public static StationRegionDomain INSTANCE = new StationRegionDomain();
	}

	public static class StationDomain extends DataDomain
	{
		@DomainRole.Instance
		public static StationDomain INSTANCE = new StationDomain();
	}

	public static class StationConditionsDomain extends DataDomain
	{
		@DomainRole.Instance
		public static StationConditionsDomain INSTANCE = new StationConditionsDomain();
	}

	public static class WeatherViewerControllerDomain extends DomainRole
	{
		@DomainRole.Instance
		public static WeatherViewerControllerDomain INSTANCE = new WeatherViewerControllerDomain();
	}

	public static class DataTransferDomain extends DomainRole
	{
		@DomainRole.Instance
		public static DataTransferDomain INSTANCE = new DataTransferDomain();
	}

	public static class WeatherViewerAssemblyDomain extends AssemblyDomain
	{
		@DomainRole.Instance
		public static WeatherViewerAssemblyDomain INSTANCE = new WeatherViewerAssemblyDomain();
	}
}
