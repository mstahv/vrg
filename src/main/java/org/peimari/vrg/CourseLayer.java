package org.peimari.vrg;

import java.util.ArrayList;

import org.peimari.rgdomain.CompetitionClass;
import org.peimari.rgdomain.Course;
import org.peimari.rgdomain.Graphic;
import org.peimari.rgdomain.Graphic.Type;
import org.vaadin.vol.Attributes;
import org.vaadin.vol.Bounds;
import org.vaadin.vol.Point;
import org.vaadin.vol.PointVector;
import org.vaadin.vol.PolyLine;
import org.vaadin.vol.Style;
import org.vaadin.vol.StyleMap;
import org.vaadin.vol.Vector;
import org.vaadin.vol.VectorLayer;

public class CourseLayer extends VectorLayer {
	private static final String STYLE_CONTEXT_JS = "{ getRadius: function(feature) {\n"
			+ "return 17 / feature.layer.map.getResolution();\n"
			+ "}\n, getSmallRadius: function(feature) {\n"
			+ "return 12 / feature.layer.map.getResolution();\n"
			+ "}\n,  getStrokeWidth: function(feature) {\n"
			+ "return 3 / feature.layer.map.getResolution();\n" + "}\n}\n";

	public CourseLayer() {
		Style style = new Style();
		String courseColor = "magenta";
		style.setStrokeColor(courseColor);
		style.setStrokeOpacity(0.7);
		style.setStrokeWidthByAttribute("getStrokeWidth");
		style.setContextJs(STYLE_CONTEXT_JS);
		StyleMap stylemap = new StyleMap(style);

		Style controlStyle = new Style();
		controlStyle.extendCoreStyle("default");
		controlStyle.setStrokeColor(courseColor);
		controlStyle.setStrokeOpacity(0.7);
		controlStyle.setFill(false);
		controlStyle.setPointRadiusByAttribute("getRadius");
		controlStyle.setStrokeWidthByAttribute("getStrokeWidth");
		controlStyle.setContextJs(STYLE_CONTEXT_JS);
		stylemap.setStyle("control", controlStyle);

		Style controlNrStyle = new Style();
		controlNrStyle.setFill(false);
		controlNrStyle.setStroke(false);
		controlNrStyle.extendCoreStyle("default");
		controlNrStyle.setStrokeColor(courseColor);
		controlNrStyle.setProperty("label", "${text}");
		controlNrStyle.setProperty("fontColor", "magenta");
		controlNrStyle.setProperty("fontWeight", "bold");
		controlNrStyle.setProperty("labelAlign", "cr");
		controlNrStyle.setProperty("labelYOffset", 12);
		stylemap.setStyle("controlnr", controlNrStyle);

		Style finishExtraCircle = new Style();
		finishExtraCircle.extendCoreStyle("default");
		finishExtraCircle.setStrokeColor(courseColor);
		finishExtraCircle.setFill(false);
		finishExtraCircle.setPointRadiusByAttribute("getSmallRadius");
		finishExtraCircle.setStrokeWidthByAttribute("getStrokeWidth");
		finishExtraCircle.setContextJs(STYLE_CONTEXT_JS);
		stylemap.setStyle("finishcircle", finishExtraCircle);
		stylemap.setExtendDefault(true);
		setStyleMap(stylemap);
	}

	protected Bounds drawCourse(Bounds bounds, CompetitionClass value) {
		removeAllComponents();
		if(value == null) {
			return bounds;
		}
		Course c = value.getCourse();
		Graphic[] graphics = c.getGraphics();

		ArrayList<Vector> vectors = new ArrayList<Vector>();
		for (Graphic g : graphics) {
			Type type = g.getType();
			Point point = new Point(g.getLon(), g.getLat());
			switch (type) {

			case LINE:
				PolyLine polyLine = new PolyLine();
				polyLine.setPoints(point, new Point(g.getLon2(), g.getLat2()));
				addComponent(polyLine);
				vectors.add(polyLine);
				break;
			case SMALLCIRCLE:
				PointVector innerCircleAndIDontMeanTheBandHehHeh = new PointVector();
				innerCircleAndIDontMeanTheBandHehHeh.setPoints(point);
				innerCircleAndIDontMeanTheBandHehHeh
						.setRenderIntent("finishcircle");
				addComponent(innerCircleAndIDontMeanTheBandHehHeh);
				vectors.add(innerCircleAndIDontMeanTheBandHehHeh);
			case CONTROLCIRCLE:
				PointVector control = new PointVector();
				control.setRenderIntent("control");
				control.setPoints(point);
				addComponent(control);
				vectors.add(control);
				break;

			case CONTROLNUMBER:
				PointVector controlNr = new PointVector();
				controlNr.setRenderIntent("controlnr");
				int ctrlnr = (int) g.getLon2();
				Attributes attributes = new Attributes();
				attributes.setProperty("text", ctrlnr + ".");
				controlNr.setAttributes(attributes);
				controlNr.setPoints(point);
				addComponent(controlNr);
				vectors.add(controlNr);
				break;
			default:
				break;
			}
			bounds.extend(point);
		}
		return bounds;
	}

}
