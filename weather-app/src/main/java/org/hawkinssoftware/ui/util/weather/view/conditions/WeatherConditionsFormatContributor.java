package org.hawkinssoftware.ui.util.weather.view.conditions;

import org.hawkinssoftware.azia.core.action.UserInterfaceTransaction.ActorBasedContributor.PendingTransaction;
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.AssemblyDomain;
import org.hawkinssoftware.azia.ui.component.UserInterfaceHandler;
import org.hawkinssoftware.azia.ui.component.text.TextViewportComposite;
import org.hawkinssoftware.azia.ui.component.transaction.state.ChangeTextDirective;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintInstanceDirective;
import org.hawkinssoftware.azia.ui.paint.transaction.repaint.RepaintRequestManager;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.publication.VisibilityConstraint;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.StationConditionsDomain;

/**
 * @JTourBusStop 2, StationReport formatting isolation, WeatherConditionsFormatContributor visible only within the
 *               StationConditionsDomain:
 * 
 *               The @VisibilityConstraint limits usage of the formatter to classes inside this package, and all public
 *               classes inside this package have visibility constrained to the StationConditionsDomain.
 */
@VisibilityConstraint(packages = VisibilityConstraint.MY_PACKAGE)
@DomainRole.Join(membership = StationConditionsDomain.class)
public class WeatherConditionsFormatContributor implements UserInterfaceHandler
{
	/**
	 * @JTourBusStop 5, StationReport display, WeatherConditionsFormatContributor installs into the text area viewport
	 *               on application startup:
	 * 
	 *               This method is called during application startup to make this contributor aware of activity in the
	 *               text area.
	 */
	@InvocationConstraint(domains = AssemblyDomain.class)
	static void install(TextViewportComposite viewport)
	{
		WeatherConditionsFormatContributor contributor = new WeatherConditionsFormatContributor(viewport);
		viewport.installHandler(contributor);
	}

	private final TextViewportComposite viewport;

	private WeatherConditionsFormatContributor(TextViewportComposite viewport)
	{
		this.viewport = viewport;
	}

	/**
	 * @JTourBusStop 6, StationReport display, WeatherConditionsFormatContributor.displayReport( ) formats the station
	 *               report and sends it to the text area:
	 * 
	 *               The UserInterfaceTransaction engine invokes this method whenever a DisplayStationReportNotification
	 *               occurs. This contributor responds by formatting the station report into plain text and sending it
	 *               in a generically recognized ChangeTextDirective to the text area viewport.
	 * 
	 * @JTourBusStop 1, StationReport formatting, WeatherConditionsFormatContributor.displayReport( ) delegates
	 *               formatting of the station report to the StationReportFormatter:
	 * 
	 * @JTourBusStop 2, StationReport lifecycle, WeatherConditionsFormatContributor.displayReport( ) transforms the
	 *               StationReport into plain text for display in the text area:
	 * 
	 * @JTourBusStop 2, StationReport immutability, WeatherConditionsFormatContributor.displayReport( ) transforms the
	 *               StationReport into plain text for display in the text area and discards the report:
	 * 
	 *               The StationReport only travels the stack, never being assigned to an object field, so it has no
	 *               role in application state.
	 */
	public void displayReport(DisplayStationReportNotification reportNotification, PendingTransaction transaction)
	{
		String text;
		if (reportNotification.report == null)
		{
			text = "Station data is not available.";
		}
		else
		{
			text = StationReportFormatter.formatReport(reportNotification.station, reportNotification.report);
		}
		transaction.contribute(new ChangeTextDirective(viewport, text));
		RepaintRequestManager.requestRepaint(new RepaintInstanceDirective(viewport.getComponent()));
	}
}