package org.peimari.vrg;

import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

public class AboutView extends Popover {
	
	private static final String DESCRIPTION = "<h1>Köyhänmiehen reittihärveli</h1>This is a prototype of an application that replicates RouteGadget features without need for Java support for browser. Currently just browsing routes is possible. 'Why?' 'iPad.' And well, all other devices that don't support Java applets. At least at this point the application reads data from the original <a href='http://routegadget.net'>RouteGadget</a> and is thus kind of backwards compatible. Built using <a href='http://vaadin.com'>Vaadin</a> and <a href='http://code.google.com/p/vopenlayers/'>OpenLayers Wrapper</a>. Currently built for desktops, but there is a plan to build a version with <a href='https://vaadin.com/add-ons/touchkit'>TouchKit</a>. As this is a hobby project on very early phase, so don't expect everything to work, but feel free to <a href='https://github.com/mstahv/vrg'>join the effort</a>.";

	public AboutView(final VRGMobileWindow vrgMobileWindow) {
		NavigationView navigationView = new NavigationView("Köyhänmiehen reittihärveli");
		setWidth("80%");
		setHeight("40%");
		setContent(navigationView);
		CssLayout l = new CssLayout();
		l.setMargin(true);
		Label c = new Label(DESCRIPTION, Label.CONTENT_XHTML);
		c.setWidth("100%");
		l.addComponent(c);
		navigationView.setContent(l);
		setClosable(true);
	}

}
