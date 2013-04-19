package org.peimari.vrg;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.peimari.rgdomain.Competition;
import org.peimari.rgdomain.CompetitionClass;
import org.peimari.rgdomain.Competitor;
import org.peimari.vrg.service.VRGService;
import org.peimari.vrg.service.VRGService.ImageDetails;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.Control;
import org.vaadin.vol.ImageLayer;
import org.vaadin.vol.OpenLayersMap;
import org.vaadin.vol.Point;
import org.vaadin.vol.PolyLine;
import org.vaadin.vol.Style;
import org.vaadin.vol.StyleMap;
import org.vaadin.vol.Vector;
import org.vaadin.vol.VectorLayer;
import org.vaadin.vol.VectorLayer.DrawingMode;
import org.vaadin.vol.VectorLayer.SelectionMode;
import org.vaadin.vol.VectorLayer.VectorSelectedEvent;
import org.vaadin.vol.VectorLayer.VectorSelectedListener;

import com.vaadin.addon.touchkit.ui.NavigationBar;
import com.vaadin.addon.touchkit.ui.NavigationView;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Window.Notification;

public class DrawRouteView extends NavigationView implements
		VectorSelectedListener, ClickListener {

	private OpenLayersMap map;
	private VectorLayer parts;
	private CourseLayer course;
	private Competition competition;
	private VRGService service;
	private CompetitionClass cClass;
	private VectorLayer active;
	private NavigationBar legbar;
	private Button next;
	private Button prev;
	private Button saveButton;

	public DrawRouteView(CompetitionClass cClass, Competition competition,
			VRGService service, Competitor c) {
		setCaption("Routes");
		this.competition = competition;
		this.service = service;
		this.cClass = cClass;
		this.competitorBeingDrawn = c;

		saveButton = new Button("Save route");
		saveButton.addListener(this);
		setRightComponent(saveButton);

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
		// ImageLayer imageLayer = new ImageLayer("http://rmb.local:8080/MAPS/"
		// + competition.getMap().getId(), imageDetails.getWidth() / 4,
		// imageDetails.getHeight() / 4);
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

		parts = new VectorLayer();
		Style style = new Style();
		style.setStrokeColor("#00ff00");
		style.setStrokeWidth(3);
		StyleMap stylemap = new StyleMap(style);
		parts.setStyleMap(stylemap);
		course = new CourseLayer();
		map.addLayer(course);
		map.addLayer(parts);
		setContent(map);

		legbar = new NavigationBar();
		next = new Button(">", this);
		next.setStyleName("green");
		legbar.setRightComponent(next);
		prev = new Button("<", this);
		legbar.setLeftComponent(prev);
		setToolbar(legbar);

		drawRoute();
	}

	private int colorIndex = 0;
	private Competitor competitorBeingDrawn;
	private HashMap<org.peimari.rgdomain.Point, Vector> pointToVector;
	private org.peimari.rgdomain.Point currentPoint;
	private PolyLine polyLine;

	private String getColor() {
		return Colors.getByIndex(colorIndex++);
	}

	@Override
	public void vectorSelected(VectorSelectedEvent event) {
		// Vector selectedVector = tracs.getSelectedVector();
		// Competitor data = (Competitor) selectedVector.getData();
		// showDetails(data, selectedVector);
		// tracs.setSelectedVector(null);
	}

	@Override
	public void buttonClick(ClickEvent event) {
		if (event.getButton() == next) {
			drawNext();
		} else if (event.getButton() == prev) {
			drawPrevious();
		} else {
			saveLastPoint();
			//
			org.peimari.rgdomain.Point[] controlPoints = cClass.getCourse()
					.getControlPoints();
			ArrayList<org.peimari.rgdomain.Point> rp = new ArrayList<org.peimari.rgdomain.Point>();
			for (int i = 1; i < controlPoints.length; i++) {
				org.peimari.rgdomain.Point point = controlPoints[i];
				Vector vector = pointToVector.get(point);
				Point[] points = vector.getPoints();
				for (int j = 0; j < points.length; j++) {
					Point point2 = points[j];
					org.peimari.rgdomain.Point o = new org.peimari.rgdomain.Point();
					o.setLat(Math.round(point2.getLat()));
					o.setLon(Math.round(point2.getLon()));
					if(j == 0 && !rp.isEmpty()) {
						org.peimari.rgdomain.Point last = rp.get(rp.size()-1);
						if(!last.equals(o)) {
							// only add control point if not in last vector
							rp.add(o);
						}
					} else {
						rp.add(o);
					}
				}
			}

			competitorBeingDrawn.setRoutePoints(rp
					.toArray(new org.peimari.rgdomain.Point[0]));

			try {
				service.persistRoute(competitorBeingDrawn, cClass, competition);
				getNavigationManager().navigateBack();
			} catch (Exception e) {
				getWindow().showNotification("Saving failed!", Notification.TYPE_ERROR_MESSAGE);
				e.printStackTrace();
			}
		}
	}

	private void drawPrevious() {
		int curIndex = getCurrentPoint();
		modify(getPointList().get(curIndex - 1));
	}

	private void drawNext() {
		int curIndex = getCurrentPoint();
		modify(getPointList().get(curIndex + 1));
	}

	private int getCurrentPoint() {
		return getPointList().indexOf(currentPoint);
	}

	private List<org.peimari.rgdomain.Point> getPointList() {
		List<org.peimari.rgdomain.Point> pointlist = Arrays.asList(cClass
				.getCourse().getControlPoints());
		return pointlist;
	}

	/**
	 * Go to drawing mode for given competitor
	 * 
	 * @param c
	 */
	private void drawRoute() {

		Bounds bounds = new Bounds();
		course.drawCourse(bounds, cClass);

		org.peimari.rgdomain.Point[] controlPoints = cClass.getCourse()
				.getControlPoints();

		org.peimari.rgdomain.Point prev = null;
		pointToVector = new HashMap<org.peimari.rgdomain.Point, Vector>();
		for (org.peimari.rgdomain.Point point : controlPoints) {
			if (prev != null) {
				PolyLine polyLine = new PolyLine();
				polyLine.setPoints(new Point(prev.getLon(), prev.getLat()),
						new Point(point.getLon(), point.getLat()));
				parts.addVector(polyLine);
				pointToVector.put(point, polyLine);
			}
			prev = point;
		}
		modify(controlPoints[1]);
	}

	private void modify(org.peimari.rgdomain.Point point) {
		saveLastPoint();

		Vector vector = pointToVector.get(point);

		polyLine = new PolyLine();
		polyLine.setPoints(vector.getPoints());

		parts.removeComponent(vector);
		if (active != null) {
			map.removeComponent(active);
		}
		active = new VectorLayer();
		Style style = new Style();
		style.setStrokeColor("#00ffff");
		style.setStrokeWidth(4);
		style.setPointRadius(18);
		StyleMap stylemap = new StyleMap(style);
		stylemap.setStyle("select", style);
		active = new VectorLayer();
		active.setStyleMap(stylemap);

		map.addComponent(active);
		active.addComponent(polyLine);
		active.setDrawingMode(DrawingMode.MODIFY);
		active.setSelectionMode(SelectionMode.SIMPLE);
		active.setSelectedVector(polyLine);
		map.zoomToExtent(new Bounds(vector.getPoints()));
		currentPoint = point;
		prev.setEnabled(getCurrentPoint() != 1);
		next.setEnabled(getCurrentPoint() != getPointList().size() - 1);
		if(!next.isEnabled()) {
			saveButton.addStyleName("green");
		}
		legbar.setCaption("-> " + getCurrentPoint());

	}

	private void saveLastPoint() {
		try {
			if (currentPoint != null) {
				PolyLine vector = new PolyLine();
				List<Point> points = new ArrayList<Point>(
						Arrays.asList(polyLine.getPoints()));
				Point lastPoint = points.get(points.size() - 1);
				// Verify the control is last point
				if (lastPoint.getLat() != currentPoint.getLat()
						|| lastPoint.getLon() != currentPoint.getLon()) {
					points.add(new Point(currentPoint.getLon(), currentPoint
							.getLat()));
				}
				vector.setPoints(points.toArray(new Point[points.size()]));
				parts.addComponent(vector);
				pointToVector.put(currentPoint, vector);
			}
		} catch (Exception e) {
		}
	}

}
