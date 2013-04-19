package org.peimari.vrg;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.vaadin.addon.touchkit.ui.TouchKitApplication;

/**
 * The Application's "main" class
 */
@SuppressWarnings("serial")
public class VRGMobileApplication extends TouchKitApplication {
	
	private String lastFragment;
	VRGMobileWindow mainWindow = new VRGMobileWindow();

	@Override
	public void init() {
		super.init();
		setMainWindow(mainWindow);
	}

	@Override
	public void onBrowserDetailsReady() {
		getMainWindow().addURIHandler(MapHandler.instance);
	}
	
	@Override
	public void onRequestStart(HttpServletRequest request,
			HttpServletResponse response) {
		if(request.getParameter("fr") != null) {
			lastFragment = request.getParameter("fr");
			mainWindow.init(getLastFragment());
		}
		super.onRequestStart(request, response);
	}
	
	public String getLastFragment() {
		return lastFragment;
	}
    
}
