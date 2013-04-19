package org.peimari.vrg;

import java.net.URL;

import com.vaadin.addon.touchkit.ui.NavigationManager;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationEvent;
import com.vaadin.addon.touchkit.ui.NavigationManager.NavigationListener;
import com.vaadin.addon.touchkit.ui.TouchKitWindow;

public class VRGMobileWindow extends TouchKitWindow {

	private NavigationManager navManager;

	public void openGadgetViewer(URL baseurl) {
		navManager.navigateTo(new GadgetView(baseurl));
	}

	public VRGMobileWindow() {
		setCaption("Köyhänmiehen reittihärveli");
		setTheme("vrgmobile");

		setOfflineTimeout(999 * 1000); // to help debugging

		init();
	}

	private void init() {
		navManager = new NavigationManager();
		setContent(navManager);
		final InitialView newcurrentComponent = new InitialView(this);
		navManager.setCurrentComponent(newcurrentComponent);
		navManager.addListener(new NavigationListener() {

			@Override
			public void navigate(NavigationEvent event) {
				if (navManager.getCurrentComponent() == newcurrentComponent) {
					newcurrentComponent.reloadList();
				}
			}
		});
	}

	public void init(String lastFragment) {
		if (lastFragment != null) {
			String[] split = lastFragment.split(";");
			try {
				String url = split[0];
				int competitionId = Integer.parseInt(split[1]);
				int classId = Integer.parseInt(split[2]);
				int routeId = Integer.parseInt(split[3]);
				init();
				openGadgetViewer(new URL(url));
				((GadgetView) navManager.getCurrentComponent()).show(
						competitionId, classId, routeId);
			} catch (Exception e) {

			}
		}
	}

}
