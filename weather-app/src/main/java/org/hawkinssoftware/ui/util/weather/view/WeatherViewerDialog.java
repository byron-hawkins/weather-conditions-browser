package org.hawkinssoftware.ui.util.weather.view;

import java.awt.Dimension;

import org.hawkinssoftware.azia.core.action.GenericTransaction;
import org.hawkinssoftware.azia.core.action.TransactionRegistry;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask;
import org.hawkinssoftware.azia.core.action.UserInterfaceTask.ConcurrentAccessException;
import org.hawkinssoftware.azia.core.log.AziaLogging.Tag;
import org.hawkinssoftware.azia.core.role.UserInterfaceDomains.AssemblyDomain;
import org.hawkinssoftware.azia.ui.component.DesktopWindow;
import org.hawkinssoftware.azia.ui.component.transaction.window.SetVisibleAction;
import org.hawkinssoftware.azia.ui.tile.TopTile;
import org.hawkinssoftware.rns.core.log.Log;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.ui.util.weather.view.WeatherViewerComponents.LayoutKey;

public class WeatherViewerDialog
{
	private final DesktopWindow<WeatherViewerComponents.LayoutKey> window;

	public WeatherViewerDialog(DesktopWindow<LayoutKey> window)
	{
		this.window = window;
	}
	
	@InvocationConstraint(domains = AssemblyDomain.class)
	public void assemble()
	{
		window.pack(new Dimension(400, 300), new Dimension(800, 600));
		window.center(0);
	}	

	public void display(final boolean b)
	{
		try
		{
			TransactionRegistry.executeTask(new UserInterfaceTask() {
				@Override
				protected boolean execute()
				{
					GenericTransaction transaction = getTransaction(GenericTransaction.class);
					transaction.addAction(new SetVisibleAction(window, b));
					return true;
				}
			});
		}
		catch (ConcurrentAccessException e)
		{
			Log.out(Tag.CRITICAL, e, "Failed to open the console.");
		}
//		window.setVisible(b);
	}

	public TopTile<WeatherViewerComponents.LayoutKey> getTopTile()
	{
		return window.getTopTile();
	}
}
