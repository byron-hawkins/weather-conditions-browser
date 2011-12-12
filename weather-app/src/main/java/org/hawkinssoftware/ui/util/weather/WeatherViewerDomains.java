package org.hawkinssoftware.ui.util.weather;

import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.AssemblyDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;

/**
 * @JTourBusStop 1, StationRegionDomain-StationDomain isolation, StationRegionDomain is orthogonal to the StationDomain:
 * 
 *               Refer to the file src/main/resources/rns/weather-app.domains.xml to see that these two domains are not
 *               allowed to share any classes.
 * 
 * @JTourBusStop 1, StationDomain-StationConditionsDomain isolation, StationDomain is orthogonal to the
 *               StationConditionsDomain:
 * 
 *               Refer to the file src/main/resources/rns/weather-app.domains.xml to see that these two domains are not
 *               allowed to share any classes.
 * 
 * @JTourBusStop 1, WeatherStation loading isolation, InitializationDomain is orthogonal to the DataTransferDomain:
 * 
 *               Refer to the file src/main/resources/rns/weather-app.domains.xml to see that these two domains are not
 *               allowed to share any classes. The net effect is that initialization classes may not make network
 *               connections, and data transfer classes may not participate in application initialization. When an
 *               operation includes both categories of functionality, it must be implemented as a collaboration between
 *               the controller domain and the transfer domain.
 * 
 * @JTourBusStop 1, StationReport loading isolation, InitializationDomain is orthogonal to the
 *               WeatherViewerControllerDomain:
 * 
 *               Refer to the file src/main/resources/rns/weather-app.domains.xml to see that these two domains are not
 *               allowed to share any classes. The net effect is that controller classes may not make network
 *               connections, and data transfer classes may not participate in coordination of application data domains.
 *               When an operation includes both categories of functionality, it must be implemented as a collaboration
 *               between the controller domain and the transfer domain.
 */
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
