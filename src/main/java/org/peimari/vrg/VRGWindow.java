package org.peimari.vrg;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;

import org.peimari.vrg.service.VRGService;
import org.vaadin.vol.OpenLayersMap;

import com.vaadin.data.Item;
import com.vaadin.event.ItemClickEvent;
import com.vaadin.event.ItemClickEvent.ItemClickListener;
import com.vaadin.event.ShortcutAction.KeyCode;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.HorizontalSplitPanel;
import com.vaadin.ui.Label;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.TextField;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.Window;

public class VRGWindow extends Window {
	
	private static Date initTime = new Date();
	private static int instancecounter = 0;
	
	private static final String DESCRIPTION = "<h1>Köyhänmiehen reittihärveli</h1>This is a prototype of an application that replicates RouteGadget features without need for Java support for browser. Currently just browsing routes is possible. 'Why?' 'iPad.' And well, all other devices that don't support Java applets. At least at this point the application reads data from the original <a href='http://routegadget.net'>RouteGadget</a> and is thus kind of backwards compatible. Built using <a href='http://vaadin.com'>Vaadin</a> and <a href='http://code.google.com/p/vopenlayers/'>OpenLayers Wrapper</a>. Currently built for desktops, but there is a plan to build a version with <a href='https://vaadin.com/add-ons/touchkit'>TouchKit</a>. As this is a hobby project on very early phase, so don't expect everything to work, but feel free to <a href='https://github.com/mstahv/vrg'>join the effort</a>.";
	private OpenLayersMap map = new OpenLayersMap();
	private MapController2 mapController = new MapController2(map);

	public VRGWindow(URL baseurl) {
		setCaption("Köyhänmiehen reittihärveli");
		setTheme("vrg");
		openGadgetViewer(baseurl);
	}

	private void openGadgetViewer(URL baseurl) {
		mapController.setRoot(baseurl);
		HorizontalSplitPanel horizontalSplitPanel = new HorizontalSplitPanel();
		horizontalSplitPanel.setSizeFull();
		horizontalSplitPanel.setSplitPosition(300, UNITS_PIXELS, true);
		map.setSizeFull();
		horizontalSplitPanel.setFirstComponent(map);
		horizontalSplitPanel.setSecondComponent(mapController);
		setContent(horizontalSplitPanel);
	}

	public VRGWindow() {
		instancecounter++;
		setCaption("Köyhänmiehen reittihärveli");
		setTheme("vrg");
		
		((VerticalLayout)getContent()).setSpacing(true);

		addComponent(new Label(DESCRIPTION, Label.CONTENT_XHTML));
		addComponent(new Label("<div style='font-size: x-small; text-align:right;'>" + instancecounter + " sessions since " + initTime + "</div>", Label.CONTENT_XHTML));

		HorizontalLayout newGadgetLayout = new HorizontalLayout();
		newGadgetLayout.setWidth("100%");
		newGadgetLayout
				.setCaption("Open new RG with url ('kartat' directory, guessed if script given):");
		final TextField url = new TextField();
		url.setWidth("100%");
		url.setValue("http://av.nettirasia.com/reitti/cgi-bin/reitti.cgi");
		// url.setValue("file:///Users/Shared/routegadget/reitti/kartat/");
		newGadgetLayout.addComponent(url);
		newGadgetLayout.setExpandRatio(url, 1);
		
		Button button = new Button("Open", new Button.ClickListener() {

			public void buttonClick(ClickEvent event) {
				String root = url.getValue().toString();
				if (root.contains("cgi-bin")) {
					if (root.contains("id=")) {
						// TODO deep linking
					}

					root = root.substring(0, root.indexOf("cgi-bin"));
					root = root + "kartat/";
				}
				try {
					openGadgetViewer(new URL(root));
				} catch (MalformedURLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		button.setClickShortcut(KeyCode.ENTER, null);
		newGadgetLayout.addComponent(button);
		addComponent(newGadgetLayout);

		Table table = new Table();
		table.addContainerProperty("host", String.class, "");
		URL[] latestUrls = VRGService.latestUrls();
		for (int i = latestUrls.length -1 ; i > -1; i--) {
			URL u = latestUrls[i];
			if(u != null) {
				Item addItem = table.addItem(u);
				addItem.getItemProperty("host").setValue(u.getHost());
			}
		}
		table.addListener(new ItemClickListener() {
			@Override
			public void itemClick(ItemClickEvent event) {
				URL u = (URL) event.getItemId();
				openGadgetViewer(u);
			}
		});
		table.addGeneratedColumn("clearCache", new ColumnGenerator() {
			@Override
			public Object generateCell(Table source, final Object itemId,
					Object columnId) {
				Button button = new Button("Clear cache");
				button.addListener(new Button.ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						new VRGService((URL) itemId).clearCache();
					}
				});
				return button;
			}
		});
		table.setCaption("... or choose one of recently accessed gadgets:");
		table.setColumnHeaderMode(Table.COLUMN_HEADER_MODE_HIDDEN);
		addComponent(table);
	}
	
}
