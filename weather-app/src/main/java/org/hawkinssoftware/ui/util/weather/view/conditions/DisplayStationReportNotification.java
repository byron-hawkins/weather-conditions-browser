package org.hawkinssoftware.ui.util.weather.view.conditions;

import org.hawkinssoftware.azia.core.action.UserInterfaceNotification;
import org.hawkinssoftware.azia.ui.component.text.TextViewportComposite;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationConditionsDomain;
import org.hawkinssoftware.ui.util.weather.data.StationReport;
import org.hawkinssoftware.ui.util.weather.data.WeatherStation;

/**
 * @JTourBusStop 1, StationReport formatting isolation, DisplayStationReportNotification visible only within the
 *               StationConditionsDomain:
 * 
 *               Sending a DisplayStationReportNotification to the text area viewport invokes the report formatting and
 *               display sequence. The @VisibilityConstraint limits visibility of this notification to the
 *               StationConditionsDomain.
 */
@VisibilityConstraint(domains = StationConditionsDomain.class)
@DomainRole.Join(membership = StationConditionsDomain.class)
public class DisplayStationReportNotification extends UserInterfaceNotification.Directed
{
	public final WeatherStation station;
	public final StationReport report;

	public DisplayStationReportNotification(TextViewportComposite textArea, WeatherStation station, StationReport report)
	{
		super(textArea);

		this.station = station;
		this.report = report;
	}
}
