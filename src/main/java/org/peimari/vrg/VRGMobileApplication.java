package org.peimari.vrg;

import com.vaadin.addon.touchkit.ui.TouchKitApplication;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class VRGMobileApplication extends TouchKitApplication
{
	
	@Override
	public void init() {
		super.init();
		setMainWindow(new VRGMobileWindow());
	}

	@Override
	public void onBrowserDetailsReady() {
		getMainWindow().addURIHandler(MapHandler.instance);
	}
    
}
