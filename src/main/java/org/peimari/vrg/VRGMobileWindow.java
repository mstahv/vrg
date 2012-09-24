package org.peimari.vrg;

import java.net.URL;

import com.vaadin.addon.touchkit.ui.NavigationManager;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationEvent;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationListener;
import com.vaadin.addon.touchkit.ui.TouchKitWindow;

public class VRGMobileWindow extends TouchKitWindow {
	
	private NavigationManager navManager = new NavigationManager();
	
	public void openGadgetViewer(URL baseurl) {
		navManager.navigateTo(new GadgetView(baseurl));
	}

	public VRGMobileWindow() {
		setCaption("Köyhänmiehen reittihärveli");
		setTheme("vrgmobile");
		setContent(navManager);
		
		setOfflineTimeout(999*1000); // to help debugging
		
		final InitialView newcurrentComponent = new InitialView(this);
		navManager.setCurrentComponent(newcurrentComponent);
		navManager.addListener(new NavigationListener() {
			
			@Override
			public void navigate(NavigationEvent event) {
				if(navManager.getCurrentComponent() == newcurrentComponent) {
					newcurrentComponent.reloadList();
				}
			}
		});
	}
	
}
