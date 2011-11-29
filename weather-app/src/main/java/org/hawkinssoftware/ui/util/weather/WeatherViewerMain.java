package org.hawkinssoftware.ui.util.weather;

import org.hawkinssoftware.azia.core.action.TransactionRegistry;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.AssemblyDomain;
import org.hawkinssoftware.azia.ui.AziaUserInterfaceInitializer;
import org.hawkinssoftware.azia.ui.component.transaction.window.ApplicationFocusHandler;
import org.hawkinssoftware.rns.core.aop.InstrumentationAgentConfiguration;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerAssembler;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerDialog;

@DomainRole.Join(membership = { AssemblyDomain.class, InitializationDomain.class })
public class WeatherViewerMain
{
	WeatherViewerAssembler assembler = new WeatherViewerAssembler();

	private void start()
	{
		InstrumentationAgentConfiguration.addOmittedPackagePath("nu/xom");
		InstrumentationAgentConfiguration.addOmittedPackagePath("org/apache/xerces");

		AziaUserInterfaceInitializer.initialize();
		ApplicationFocusHandler.install();
		Log.addTagFilter(Tag.NO_SUBSYSTEMS_UP_TO_DEBUG);
		Log.addTagFilter(org.hawkinssoftware.rns.core.util.RNSLogging.Tag.EVERYTHING);

		try
		{
			TransactionRegistry.executeTask(assembler);
			WeatherViewerController.getInstance().startApplication();
		}
		catch (UserInterfaceTask.ConcurrentAccessException e)
		{
			Log.out(Tag.CRITICAL, e, "Failed to assemble the Scrap Menagerie application.");
		}

		WeatherViewerComponents.getInstance().setDialog(new WeatherViewerDialog(assembler.window));
		WeatherViewerComponents.getInstance().getDialog().assemble();
		WeatherViewerComponents.getInstance().getDialog().display(true);
	}

	public static void main(String[] args)
	{
		try
		{
			WeatherViewerMain app = new WeatherViewerMain();
			app.start();
		}
		catch (Throwable t)
		{
			Log.out(Tag.CRITICAL, t, "Failed to start the weather viewer application.");
		}
	}
}
