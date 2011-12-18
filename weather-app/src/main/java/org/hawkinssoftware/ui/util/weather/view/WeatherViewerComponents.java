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
package org.hawkinssoftware.ui.util.weather.view;

import java.awt.Color;

import org.hawkinssoftware.azia.ui.component.text.Label;
import org.hawkinssoftware.azia.ui.component.text.LabelComposite;
import org.hawkinssoftware.azia.ui.tile.LayoutEntity;
import org.hawkinssoftware.rns.core.publication.InvocationConstraint;
import org.hawkinssoftware.rns.core.role.CoreDomains.InitializationDomain;
import org.hawkinssoftware.ui.util.weather.WeatherViewerDomains.WeatherViewerAssemblyDomain;

public class WeatherViewerComponents
{
	public enum LayoutKey implements LayoutEntity.Key<LayoutKey>
	{
		WINDOW,
		MAIN_PANEL, // pair:V
		TITLE_PANEL, // unit
		DATA_PANEL, // pair:H
		STATION_NAVIGATION_PANEL, // pair:V
		STATION_REGION_LIST_FRAME, // pair:V
		STATION_REGION_LABEL_PANEL, // unit
		STATION_REGION_LIST_PANEL, // unit
		STATION_LIST_FRAME, // pair:V
		STATION_LIST_LABEL_PANEL, // unit
		STATION_LIST_PANEL, // unit
		STATION_DATA_FRAME, // pair:V
		STATION_DATA_LABEL_PANEL, // unit
		STATION_DATA_PANEL, // unit
		TITLE,
		STATION_REGION_LABEL,
		STATION_REGION_LIST,
		STATION_LABEL,
		STATION_LIST,
		DATA_LABEL,
		STATION_DATA;

		@Override
		public String getName()
		{
			return name();
		}
	}

	public static class LabelAssembly extends Label.Assembly
	{
		private final LayoutKey key;

		public LabelAssembly(LayoutKey key)
		{
			this.key = key;
		}

		@Override
		public void assemble(LabelComposite<Label, ?> label)
		{
			super.assemble(label);

			label.installHandler(new LabelComposite.UpdateHandler<LayoutKey>(key));
		}
	}

	private static final LabelAssembly TITLE_LABEL_ASSEMBLY = new LabelAssembly(LayoutKey.TITLE_PANEL);

	/**
	 * @JTourBusStop 6, Application assembly, WeatherViewerAssemblyDomain has exclusive permission to modify the title
	 *               label:
	 * 
	 *               This assembly descriptor can be used to obtain a reference to the label from the ComponentRegistry,
	 *               so this @InvocationConstraint on its accessor ensures that only assembly code will ever modify it.
	 */
	@InvocationConstraint(domains = WeatherViewerAssemblyDomain.class)
	public static LabelAssembly getTitleLabel()
	{
		return TITLE_LABEL_ASSEMBLY;
	}

	public static final Color SELECTION_BACKGROUND = new Color(0xEEFFDD);

	private static final WeatherViewerComponents INSTANCE = new WeatherViewerComponents();

	public static WeatherViewerComponents getInstance()
	{
		return INSTANCE;
	}

	private WeatherViewerDialog dialog;

	@InvocationConstraint(domains = InitializationDomain.class)
	public WeatherViewerDialog getDialog()
	{
		return dialog;
	}

	@InvocationConstraint(domains = InitializationDomain.class)
	public void setDialog(WeatherViewerDialog dialog)
	{
		this.dialog = dialog;
	}
}
