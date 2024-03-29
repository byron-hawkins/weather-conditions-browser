/*
 * Copyright (c) 2011 HawkinsSoftware
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Byron Hawkins of HawkinsSoftware
 */
package org.hawkinssoftware.ui.util.weather;

import org.hawkinssoftware.azia.core.action.TransactionRegistry;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.ui.AziaUserInterfaceInitializer;
import org.hawkinssoftware.azia.ui.component.transaction.window.ApplicationFocusHandler;
import org.hawkinssoftware.rns.core.aop.InstrumentationAgentConfiguration;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.rns.core.role.DomainRole;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerAssembler;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerDialog;

/**
 * @JTourBusStop 1, Application assembly, WeatherViewerMain launches the assembly task:
 * 
 *               After initializing the RNS instrumentation agent and Azia library, this launch sequence invokes the
 *               WeatherViewerAssembler.
 */
@DomainRole.Join(membership = { WeatherViewerAssemblyDomain.class, InitializationDomain.class })
public class WeatherViewerMain
{
	private final WeatherViewerAssembler assembler = new WeatherViewerAssembler();

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
			WeatherViewerInitializer.getInstance().startApplication();
		}
		catch (UserInterfaceTask.ConcurrentAccessException e)
		{
			Log.out(Tag.CRITICAL, e, "Failed to assemble the Scrap Menagerie application.");
		}

		WeatherViewerComponents.getInstance().setDialog(new WeatherViewerDialog(assembler.getWindow()));
		WeatherViewerComponents.getInstance().getDialog().assemble();
		WeatherViewerComponents.getInstance().getDialog().display(true);
	}

	@InvocationConstraint
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
