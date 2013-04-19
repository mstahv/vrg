package org.peimari.vrg;

import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;

public class AboutView extends Popover {
	
	private static final String DESCRIPTION = "<h1>Köyhänmiehen reittihärveli</h1>This is a prototype of an application that replicates RouteGadget features without need for Java support for browser. Currently features are much more limited than in real route gadget. E.g. drawing routes is only possible with ipad, modern android and Chrome/Safari on desktop. 'Why did you do this?' 'iPad.' And well, all other devices that don't support Java applets. At least at this point the application reads data from the original <a href='http://routegadget.net'>RouteGadget</a> and is thus kind of backwards compatible. Built using <a href='http://vaadin.com'>Vaadin</a>, <a href='http://code.google.com/p/vopenlayers/'>OpenLayers Wrapper</a> and <a href='https://vaadin.com/add-ons/touchkit'>TouchKit</a>. As this is a hobby project on very early phase, so don't expect everything to work, but feel free to <a href='https://github.com/mstahv/vrg'>join the effort or fill in bugs and feature requests</a>.";

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
