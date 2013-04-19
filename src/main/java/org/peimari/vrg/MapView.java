package org.peimari.vrg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.Set;

import org.peimari.rgdomain.Competition;
import org.peimari.rgdomain.CompetitionClass;
import org.peimari.rgdomain.Competitor;
import org.peimari.util.Util;
import org.peimari.vrg.service.VRGService;
import org.peimari.vrg.service.VRGService.ImageDetails;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.Control;
import org.vaadin.vol.ImageLayer;
import org.vaadin.vol.OpenLayersMap;
import org.vaadin.vol.Point;
import org.vaadin.vol.PolyLine;
import org.vaadin.vol.Style;
import org.vaadin.vol.Vector;
import org.vaadin.vol.VectorLayer;
import org.vaadin.vol.VectorLayer.SelectionMode;
import org.vaadin.vol.VectorLayer.VectorSelectedEvent;
import org.vaadin.vol.VectorLayer.VectorSelectedListener;

import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.addon.touchkit.ui.Popover;
import com.vaadin.addon.touchkit.ui.VerticalComponentGroup;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.CssLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.Window.CloseEvent;
import com.vaadin.ui.Window.CloseListener;

public class MapView<E> extends NavigationView implements
		VectorSelectedListener, ClickListener {

	private OpenLayersMap map;
	private VectorLayer tracs;
	private CourseLayer course;
	private Competition competition;
	private VRGService service;
	private CompetitionClass cClass;

	public MapView(CompetitionClass cClass, Competition competition,
			VRGService service) {
		setCaption("Routes");
		this.competition = competition;
		this.service = service;
		this.cClass = cClass;

		Button button = new Button("Routes");
		button.addListener(this);
		setRightComponent(button);
	}

	@Override
	public void attach() {
		super.attach();
		if (competition.getMap() == null) {
			throw new RuntimeException("No map found");
		}
		long currentTimeMillis = System.currentTimeMillis();
		ImageDetails imageDetails = service.getMapDetails(competition);
		System.err.println((System.currentTimeMillis() - currentTimeMillis)
				+ " MS");

		MapHandler.addFile(service.getMapFile(competition.getMap()),
				competition.getMap().getId(), imageDetails.getFormatName());

		map = new OpenLayersMap();
		map.setSizeFull();

		ImageLayer imageLayer = new ImageLayer(getApplication().getURL()
				.toExternalForm() + "MAPS/" + competition.getMap().getId(),
				imageDetails.getWidth() / 4, imageDetails.getHeight() / 4);
		imageLayer.setBounds(0d, (double) -imageDetails.getHeight(),
				(double) imageDetails.getWidth(), 0d);
		map.setApiProjection("EPSG:900913");
		map.setJsMapOptions("{numZoomLevels: 5, projection: new OpenLayers.Projection(\"EPSG:900913\"), sphericalMercator: true, maxExtent: new OpenLayers.Bounds(0,-"
				+ 5000 + "," + 5000 + ",0)}");
		map.addLayer(imageLayer);
		map.getControls().clear();
		map.addControl(Control.Navigation);
		map.addControl(Control.MousePosition);
		map.addControl(Control.PanZoomBar);
		map.zoomToExtent(new Bounds(new Point(0, 0), new Point(imageDetails
				.getWidth(), -imageDetails.getHeight())));

		tracs = new VectorLayer();
		course = new CourseLayer();
		map.addLayer(course);
		map.addLayer(tracs);
		setContent(map);

		tracs.setSelectionMode(SelectionMode.SIMPLE);
		tracs.addListener(this);

	}

	public void drawRoutes(Set<Competitor> selected) {

		ArrayList<Competitor> arrayList = new ArrayList<Competitor>(selected);
		Collections.sort(arrayList, new Comparator<Competitor>() {
			@Override
			public int compare(Competitor o1, Competitor o2) {
				return o1.getTime() - o2.getTime();
			}
		});

		tracs.removeAllComponents();

		Bounds bounds = new Bounds();
		course.drawCourse(bounds, cClass);
		for (Competitor competitor : arrayList) {
			drawCompetitor(bounds, competitor);
		}
		map.zoomToExtent(bounds);

	}

	private void drawCompetitor(Bounds bounds, Competitor competitor) {
		String color = getColor();
		org.peimari.rgdomain.Point[] routePoints = competitor.getRoutePoints();
		PolyLine polyLine = new PolyLine();
		Point[] points = new Point[routePoints.length];
		for (int i = 0; i < points.length; i++) {
			org.peimari.rgdomain.Point p = routePoints[i];
			Point point = points[i] = new Point(p.getLon(), p.getLat());
			bounds.extend(point);
		}
		Style s = new Style();
		s.setStrokeColor(color);
		s.setStrokeWidth(3);
		s.setStrokeOpacity(0.75);
		polyLine.setCustomStyle(s);
		polyLine.setPoints(points);
		polyLine.setData(competitor);
		tracs.addComponent(polyLine);
	}

	private int colorIndex = 0;
	private Competitor competitorBeingDrawn;

	private String getColor() {
		return Colors.getByIndex(colorIndex++);
	}

	@Override
	public void vectorSelected(VectorSelectedEvent event) {
		Vector selectedVector = tracs.getSelectedVector();
		Competitor data = (Competitor) selectedVector.getData();
		showDetails(data, selectedVector);
		tracs.setSelectedVector(null);
	}

	private void showDetails(Competitor data, final Vector selectedVector) {
		final Style customStyle = selectedVector.getCustomStyle();

		customStyle.setStrokeWidth(6);
		selectedVector.setCustomStyle(customStyle);

		Popover popover = new Popover();

		popover.setWidth("300px");
		popover.setHeight("200px");
		NavigationView navigationView = new NavigationView();
		navigationView.setCaption(data.getName());
		popover.setContent(navigationView);

		CssLayout cssLayout = new CssLayout();
		cssLayout.setSizeUndefined();
		cssLayout.setMargin(true);

		VerticalComponentGroup verticalComponentGroup = new VerticalComponentGroup();
		Label time = new Label("" + Util.formatTime(data.getTime() * 1000));
		time.setSizeUndefined();
		time.setCaption("Result ");
		verticalComponentGroup.addComponent(time);

		cssLayout.addComponent(verticalComponentGroup);

		navigationView.setContent(cssLayout);

		popover.addListener(new CloseListener() {

			@Override
			public void windowClose(CloseEvent e) {
				customStyle.setStrokeWidth(3);
				selectedVector.setCustomStyle(customStyle);
			}
		});

		popover.showRelativeTo(getNavigationBar());

	}

	@Override
	public void buttonClick(ClickEvent event) {

		VerticalComponentGroup cssLayout = new VerticalComponentGroup();

		Iterator<Component> componentIterator = tracs.getComponentIterator();
		while (componentIterator.hasNext()) {
			Vector next = (Vector) componentIterator.next();
			String color = next.getCustomStyle().getStrokeColor();
			Competitor data = (Competitor) next.getData();

			String content = "";
			content += "<span style='font-weight:bold;color:" + color + ";'>";
			content += Util.formatTime(data.getTime() * 1000);
			content += "</span>";
			content += "";
			Label c = new Label(content, Label.CONTENT_XHTML);
			c.setCaption(data.getName());
			c.setSizeUndefined();
			cssLayout.addComponent(c);

		}

		NavigationView navigationView = new NavigationView(cssLayout);
		navigationView.setCaption("Routes");
		Popover popover = new Popover(navigationView);
		popover.setWidth("360px");
		popover.setHeight("80%");
		popover.showRelativeTo(getRightComponent());

	}

}
