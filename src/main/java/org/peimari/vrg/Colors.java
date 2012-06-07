package org.peimari.vrg;


public class Colors {

	static String[] colors = new String[] { "#0000ff", "#8a2be2", "#a52a2a",
			"#5F9EA0", "#7FFF00", "#D2691E", "#FF7F50", "#6495ED", "#DC143C",
			"#00FFFF", "#00008B", "#008B8B", "#B8860B", "#006400", "#8B008B",
			"#556B2F", "#FF8C00", "#9932CC", "#8B0000", "#E9967A", "#8FBC8F",
			"#483D8B", "#2F4F4F", "#00CED1", "#9400D3", "#FF1493", "#00BFFF",
			"#696969", "#696969", "#1E90FF", "#B22222", "#228B22",
			"#FF00FF", "#FFD700", "#DAA520", "#808080", "#008000",
			"#ADFF2F", "#FF69B4", "#CD5C5C", "#FF00FF", "#800000", "#66CDAA",
			"#0000CD", "#BA55D3", "#9370D8", "#3CB371", "#7B68EE", "#48D1CC",
			"#C71585", "#191970", "#000080", "#808000", "#FFA500", "#FF4500",
			"#DA70D6", "#D87093", "#CD853F", "#800080", "#FF0000", "#BC8F8F",
			"#4169E1", "#8B4513", "#FA8072", "#F4A460", "#2E8B57", "#A0522D",
			"#87CEEB", "#6A5ACD", "#00FF7F", "#4682B4", "#D2B48C", "#008080",
			"#D8BFD8", "#FF6347", "#40E0D0", "#EE82EE", "#F5DEB3", "#FFFF00",
			"#9ACD32" };

	static {
		// final Random r = new Random(0);
		// Arrays.sort(colors, new Comparator<String>() {
		// public int compare(String o1, String o2) {
		// return r.nextInt(2)-1;
		// }
		// });
	}

	public static String getByIndex(int i) {
		return colors[i % colors.length];
	}
}
