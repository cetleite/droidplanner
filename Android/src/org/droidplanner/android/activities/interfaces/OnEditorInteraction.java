package org.droidplanner.android.activities.interfaces;

import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.core.helpers.coordinates.Coord2D;

public interface OnEditorInteraction {
	public boolean onItemLongClick(MissionItemProxy item);

	public void onItemClick(MissionItemProxy item, boolean zoomToFit, int num_map);

	public void onMapClick(Coord2D coord);
    public void onMapClick2(Coord2D coord);
    public void onMapClick3(Coord2D coord);
    public void onMapClick4(Coord2D coord);

	public void onListVisibilityChanged();
}
