package org.hawkinssoftware.ui.util.weather.view;

import java.awt.Dimension;

import org.hawkinssoftware.azia.ui.component.DesktopWindow;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;

@InvocationConstraint(domains = InitializationDomain.class)
public class WeatherViewerDialog
{
	private final DesktopWindow<WeatherViewerComponents.LayoutKey> window;

	public WeatherViewerDialog(DesktopWindow<LayoutKey> window)
	{
		this.window = window;
	}

	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public void assemble()
	{
		window.pack(new Dimension(400, 300), new Dimension(800, 600));
		window.center(0);
	}

	public void display(final boolean b)
	{
		window.display(b);
	}
}
