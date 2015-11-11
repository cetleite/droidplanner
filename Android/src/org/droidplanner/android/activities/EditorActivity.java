package org.droidplanner.android.activities;

import java.util.List;

import org.droidplanner.R;
import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.activities.interfaces.OnEditorInteraction;
import org.droidplanner.android.dialogs.EditInputDialog;
import org.droidplanner.android.dialogs.YesNoDialog;
import org.droidplanner.android.dialogs.openfile.OpenFileDialog;
import org.droidplanner.android.dialogs.openfile.OpenMissionDialog;
import org.droidplanner.android.fragments.EditorListFragment;
import org.droidplanner.android.fragments.EditorListFragment2;
import org.droidplanner.android.fragments.EditorListFragment3;
import org.droidplanner.android.fragments.EditorListFragment4;
import org.droidplanner.android.fragments.EditorMapFragment;
import org.droidplanner.android.fragments.EditorToolsFragment;
import org.droidplanner.android.fragments.EditorToolsFragment2;
import org.droidplanner.android.fragments.EditorToolsFragment3;
import org.droidplanner.android.fragments.EditorToolsFragment4;
import org.droidplanner.android.fragments.helpers.EditorTools;
import org.droidplanner.android.fragments.helpers.GestureMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment2;
import org.droidplanner.android.fragments.helpers.GestureMapFragment3;
import org.droidplanner.android.fragments.helpers.GestureMapFragment4;
import org.droidplanner.android.proxy.mission.MissionProxy;
import org.droidplanner.android.proxy.mission.MissionSelection;
import org.droidplanner.android.proxy.mission.item.MissionItemProxy;
import org.droidplanner.android.proxy.mission.item.fragments.MissionDetailFragment;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.file.FileStream;
import org.droidplanner.android.utils.file.IO.MissionReader;
import org.droidplanner.android.utils.file.IO.MissionWriter;
import org.droidplanner.android.utils.prefs.AutoPanMode;
import org.droidplanner.core.drone.DroneInterfaces.DroneEventsType;
import org.droidplanner.core.helpers.coordinates.Coord2D;
import org.droidplanner.core.helpers.units.Length;
import org.droidplanner.core.helpers.units.Speed;
import org.droidplanner.core.mission.MissionItemType;
import org.droidplanner.core.model.Drone;
import org.droidplanner.core.util.Pair;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.ActionMode;
import android.view.ActionMode.Callback;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnLongClickListener;
import android.widget.AbsListView;
import android.widget.ImageButton;
import android.widget.RadioButton;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.common.msg_mission_item;
import com.google.android.gms.analytics.HitBuilders;

/**
 * This implements the map editor activity. The map editor activity allows the
 * user to create and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends DrawerNavigationUI implements GestureMapFragment.OnPathFinishedListener,
        GestureMapFragment2.OnPathFinishedListener,GestureMapFragment3.OnPathFinishedListener,GestureMapFragment4.OnPathFinishedListener,
        EditorToolsFragment.OnEditorToolSelected, EditorToolsFragment2.OnEditorToolSelected,EditorToolsFragment3.OnEditorToolSelected,
        EditorToolsFragment4.OnEditorToolSelected, MissionDetailFragment.OnMissionDetailListener, OnEditorInteraction,
        Callback, MissionSelection.OnSelectionUpdateListener, OnClickListener, OnLongClickListener {

    /**
     * Used to retrieve the item detail window when the activity is destroyed,
     * and recreated.
     */
    private static final String ITEM_DETAIL_TAG = "Item Detail Window";
    private static final String EXTRA_IS_SPLINE_ENABLED = "extra_is_spline_enabled";

    /**
     * Used to provide access and interact with the
     * {@link org.droidplanner.core.mission.Mission} object on the Android
     * layer.
     */
    private MissionProxy missionProxy, missionProxy2, missionProxy3, missionProxy4;

    /*
     * View widgets.
     */
    private EditorMapFragment planningMapFragment, planningMapFragment2,planningMapFragment3,planningMapFragment4;
    private GestureMapFragment gestureMapFragment;
    private GestureMapFragment2 gestureMapFragment2;
    private GestureMapFragment3 gestureMapFragment3;
    private GestureMapFragment4 gestureMapFragment4;
    private EditorToolsFragment editorToolsFragment;
    private EditorToolsFragment2 editorToolsFragment2;
    private EditorToolsFragment3 editorToolsFragment3;
    private EditorToolsFragment4 editorToolsFragment4;
    private MissionDetailFragment itemDetailFragment, itemDetailFragment2, itemDetailFragment3, itemDetailFragment4;
    private FragmentManager fragmentManager;
    private EditorListFragment missionListFragment;
    private EditorListFragment2 missionListFragment2;
    private EditorListFragment3 missionListFragment3;
    private EditorListFragment4 missionListFragment4;

    private View mSplineToggleContainer, mSplineToggleContainer2, mSplineToggleContainer3, mSplineToggleContainer4;
    private boolean mIsSplineEnabled, mIsSplineEnabled2, mIsSplineEnabled3, mIsSplineEnabled4;

    private TextView infoView, infoView2, infoView3, infoView4;

    private boolean mMultiEditEnabled, mMultiEditEnabled2, mMultiEditEnabled3, mMultiEditEnabled4;
    private boolean mapExpanded = false;

    private int NUM_MAPS = 4;

    private final  String EDITORFLUX = "EDITORFLUX";
    /**
     * This view hosts the mission item detail fragment. On phone, or device
     * with limited screen estate, it's removed from the layout, and the item
     * detail ends up displayed as a dialog.
     */
    private View mContainerItemDetail, mContainerItemDetail2,mContainerItemDetail3, mContainerItemDetail4;

    private ActionMode contextualActionBar, contextualActionBar2, contextualActionBar3, contextualActionBar4;
    private RadioButton normalToggle, normalToggle3, normalToggle2, normalToggle4;
    private RadioButton splineToggle, splineToggle2, splineToggle3, splineToggle4;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);

        fragmentManager = getSupportFragmentManager();

        initializeMaps(savedInstanceState);

        updateLayout();

    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.map_orientation_button:
                if(planningMapFragment != null) {
                    planningMapFragment.updateMapBearing(0);
                }
                break;
            case R.id.zoom_to_fit_button:
                if(planningMapFragment != null){
                    planningMapFragment.zoomToFit();
                }
                break;
            case R.id.splineWpToggle:
                mIsSplineEnabled = splineToggle.isChecked();
                break;
            case R.id.normalWpToggle:
                mIsSplineEnabled = !normalToggle.isChecked();
                break;
            case R.id.drone_location_button:
                planningMapFragment.goToDroneLocation();
                break;
            case R.id.my_location_button:
                planningMapFragment.goToMyLocation();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.drone_location_button:
                planningMapFragment.setAutoPanMode(AutoPanMode.DRONE);
                return true;
            case R.id.my_location_button:
                planningMapFragment.setAutoPanMode(AutoPanMode.USER);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        switch(NUM_MAPS)
        {
            case 1:
                editorToolsFragment.setToolAndUpdateView(getTool(1));
                setupTool(getTool(1));
                break;
            case 2:
                editorToolsFragment.setToolAndUpdateView(getTool(1));
                setupTool(getTool(1));
                editorToolsFragment2.setToolAndUpdateView(getTool(2));
                setupTool(getTool(2));
                break;
            case 3:
                editorToolsFragment.setToolAndUpdateView(getTool(1));
                setupTool(getTool(1));
                editorToolsFragment2.setToolAndUpdateView(getTool(2));
                setupTool(getTool(2));
                editorToolsFragment3.setToolAndUpdateView(getTool(3));
                setupTool(getTool(3));
                break;
            case 4:
                editorToolsFragment.setToolAndUpdateView(getTool(1));
                setupTool(getTool(1));
                editorToolsFragment2.setToolAndUpdateView(getTool(2));
                setupTool(getTool(2));
                editorToolsFragment3.setToolAndUpdateView(getTool(3));
                setupTool(getTool(3));
                editorToolsFragment4.setToolAndUpdateView(getTool(4));
                setupTool(getTool(4));
                break;
        }



    }

    @Override
    public void onSaveInstanceState(Bundle outState){
        super.onSaveInstanceState(outState);
        outState.putBoolean(EXTRA_IS_SPLINE_ENABLED, mIsSplineEnabled);
    }

    @Override
    protected int getNavigationDrawerEntryId() {
        return R.id.navigation_editor;
    }

    @Override
    public void onStart() {
        super.onStart();

        switch(NUM_MAPS)
        {
            case 1:
                missionProxy.selection.addSelectionUpdateListener(this);
                break;
            case 2:
                missionProxy.selection.addSelectionUpdateListener(this);
                missionProxy2.selection.addSelectionUpdateListener(this);
                break;
            case 3:
                missionProxy.selection.addSelectionUpdateListener(this);
                missionProxy2.selection.addSelectionUpdateListener(this);
                missionProxy3.selection.addSelectionUpdateListener(this);
                break;
            case 4:
                missionProxy.selection.addSelectionUpdateListener(this);
                missionProxy2.selection.addSelectionUpdateListener(this);
                missionProxy3.selection.addSelectionUpdateListener(this);
                missionProxy4.selection.addSelectionUpdateListener(this);
                break;
        }

    }

    @Override
    public void onStop() {
        super.onStop();

        switch(NUM_MAPS)
        {
            case 1:
                missionProxy.selection.removeSelectionUpdateListener(this);
                break;
            case 2:
                missionProxy.selection.removeSelectionUpdateListener(this);
                missionProxy2.selection.removeSelectionUpdateListener(this);
                break;
            case 3:
                missionProxy.selection.removeSelectionUpdateListener(this);
                missionProxy2.selection.removeSelectionUpdateListener(this);
                missionProxy3.selection.removeSelectionUpdateListener(this);
                break;
            case 4:
                missionProxy.selection.removeSelectionUpdateListener(this);
                missionProxy2.selection.removeSelectionUpdateListener(this);
                missionProxy3.selection.removeSelectionUpdateListener(this);
                missionProxy4.selection.removeSelectionUpdateListener(this);
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        getMenuInflater().inflate(R.menu.menu_mission, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_open_mission:
                openMissionFile();
                return true;

            case R.id.menu_save_mission:
                saveMissionFile();
                return true;

            default:
                return super.onMenuItemSelected(featureId, item);
        }
    }

    private void openMissionFile() {
        OpenFileDialog missionDialog = new OpenMissionDialog(drone) {
            @Override
            public void waypointFileLoaded(MissionReader reader) {
                drone.getMission().onMissionLoaded(reader.getMsgMissionItems());
                planningMapFragment.zoomToFit();
            }
        };
        missionDialog.openDialog(this);
    }

    private void saveMissionFile() {
        final Context context = getApplicationContext();
        final EditInputDialog dialog = EditInputDialog.newInstance(context, getString(R.string.label_enter_filename),
                FileStream.getWaypointFilename("waypoints"), new EditInputDialog.Listener() {
                    @Override
                    public void onOk(CharSequence input) {
                        final List<msg_mission_item> missionItems = drone.getMission()
                                .getMsgMissionItems();
                        if (MissionWriter.write( missionItems, input.toString())) {
                            Toast.makeText(context, R.string.file_saved_success, Toast.LENGTH_SHORT).show();

                            final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                                    .setCategory(GAUtils.Category.MISSION_PLANNING)
                                    .setAction("Mission saved to file")
                                    .setLabel("Mission items count")
                                    .setValue(missionItems.size());
                            GAUtils.sendEvent(eventBuilder);
                        } else {
                            Toast.makeText(context, R.string.file_saved_error, Toast.LENGTH_SHORT).show();
                        }
                    }

                    @Override
                    public void onCancel() {}
                });

        dialog.show(getSupportFragmentManager(), "Mission filename");
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        planningMapFragment.saveCameraPosition();
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        super.onDroneEvent(event, drone);





        switch (event) {
            //Aparentemente só atualiza a barra com informações de texto e não os pontos na tela
            case MISSION_UPDATE:
                /*
                Length missionLength = missionProxy.getMissionLength();
                Speed speedParameter = drone.getSpeed().getSpeedParameter();
                String infoString = "Distance " + missionLength;
                if (speedParameter != null) {
                    int time = (int) (missionLength.valueInMeters() / speedParameter
                            .valueInMetersPerSecond());
                    infoString = infoString
                            + String.format(", Flight time: %02d:%02d", time / 60, time % 60);
                }
                infoView.setText(infoString);

                // Remove detail window if item is removed
                if (missionProxy.selection.getSelected().isEmpty() && itemDetailFragment != null) {
                    removeItemDetail();
                }
                */
                break;

            case MISSION_RECEIVED:
                if (planningMapFragment != null) {
                    planningMapFragment.zoomToFit();
                }
                if (planningMapFragment2 != null) {
                    planningMapFragment2.zoomToFit();
                }
                if (planningMapFragment3 != null) {
                    planningMapFragment3.zoomToFit();
                }
                if (planningMapFragment4 != null) {
                    planningMapFragment4.zoomToFit();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onMapClick(Coord2D point) {onMapClicked(point, 1);}
    @Override
    public void onMapClick2(Coord2D point) {onMapClicked(point, 2);}

    @Override
    public void onMapClick3(Coord2D point) {onMapClicked(point, 3);}

    @Override
    public void onMapClick4(Coord2D point) {onMapClicked(point, 4);}



    public void onMapClicked(Coord2D point, int num_map)
    {
        enableMultiEdit(false);
        MissionProxy missionProxy;

        switch(num_map)
        {
            case 1: missionProxy = this.missionProxy; break;
            case 2: missionProxy = this.missionProxy2; break;
            case 3: missionProxy = this.missionProxy3; break;
            case 4: missionProxy = this.missionProxy4; break;
            default: missionProxy = this.missionProxy; break;
        }


        // If an mission item is selected, unselect it.
        missionProxy.selection.clearSelection();

        switch (getTool(num_map)) {
            case MARKER:
                if (mIsSplineEnabled) {
                    missionProxy.addSplineWaypoint(point);
                } else {
                    missionProxy.addWaypoint(point);
                }
                break;
            case DRAW:
                break;
            case POLY:
                break;
            case TRASH:
                break;
            case NONE:
                break;
        }
    }

    public EditorTools getTool(int num_map) {
        switch(num_map)
        {
            case 1: return editorToolsFragment.getTool();
            case 2: return editorToolsFragment2.getTool();
            case 3: return editorToolsFragment3.getTool();
            case 4: return editorToolsFragment2.getTool();
            default: return editorToolsFragment.getTool();
        }

    }

    @Override
    public void editorToolChanged(EditorTools tools) {
        missionProxy.selection.clearSelection();
        setupTool(tools);
    }

    private void setupTool(EditorTools tool) {

        switch(NUM_MAPS)
        {
            case 1: setupTools1(tool); break;
            case 2: setupTools2(tool);setupTools1(tool); break;
            case 3: setupTools2(tool);setupTools1(tool);setupTools3(tool); break;
            case 4: setupTools2(tool);setupTools1(tool);setupTools3(tool);setupTools4(tool); break;
        }

    }

    public void setupTools1(EditorTools tool)
    {
        planningMapFragment.skipMarkerClickEvents(false);
        switch (tool) {
            case DRAW:
                enableSplineToggle(true);
                gestureMapFragment.enableGestureDetection();
                break;

            case POLY:
                enableSplineToggle(false);
                Toast.makeText(this, R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
                gestureMapFragment.enableGestureDetection();
                break;

            case MARKER:
                // Enable the spline selection toggle
                enableSplineToggle(true);
                gestureMapFragment.disableGestureDetection();
                planningMapFragment.skipMarkerClickEvents(true);
                break;

            case TRASH:
            case NONE:
                enableSplineToggle(false);
                gestureMapFragment.disableGestureDetection();
                break;
        }
    }
    public void setupTools2(EditorTools tool)
    {
        planningMapFragment2.skipMarkerClickEvents(false);
        switch (tool) {
            case DRAW:
                enableSplineToggle(true);
                gestureMapFragment2.enableGestureDetection();
                break;

            case POLY:
                enableSplineToggle(false);
                Toast.makeText(this, R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
                gestureMapFragment2.enableGestureDetection();
                break;

            case MARKER:
                // Enable the spline selection toggle
                enableSplineToggle(true);
                gestureMapFragment2.disableGestureDetection();
                planningMapFragment2.skipMarkerClickEvents(true);
                break;

            case TRASH:
            case NONE:
                enableSplineToggle(false);
                gestureMapFragment2.disableGestureDetection();
                break;
        }
    }
    public void setupTools3(EditorTools tool)
    {
        planningMapFragment3.skipMarkerClickEvents(false);
        switch (tool) {
            case DRAW:
                enableSplineToggle(true);
                gestureMapFragment3.enableGestureDetection();
                break;

            case POLY:
                enableSplineToggle(false);
                Toast.makeText(this, R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
                gestureMapFragment3.enableGestureDetection();
                break;

            case MARKER:
                // Enable the spline selection toggle
                enableSplineToggle(true);
                gestureMapFragment3.disableGestureDetection();
                planningMapFragment3.skipMarkerClickEvents(true);
                break;

            case TRASH:
            case NONE:
                enableSplineToggle(false);
                gestureMapFragment3.disableGestureDetection();
                break;
        }
    }
    public void setupTools4(EditorTools tool)
    {
        planningMapFragment4.skipMarkerClickEvents(false);
        switch (tool) {
            case DRAW:
                enableSplineToggle(true);
                gestureMapFragment4.enableGestureDetection();
                break;

            case POLY:
                enableSplineToggle(false);
                Toast.makeText(this, R.string.draw_the_survey_region, Toast.LENGTH_SHORT).show();
                gestureMapFragment4.enableGestureDetection();
                break;

            case MARKER:
                // Enable the spline selection toggle
                enableSplineToggle(true);
                gestureMapFragment4.disableGestureDetection();
                planningMapFragment4.skipMarkerClickEvents(true);
                break;

            case TRASH:
            case NONE:
                enableSplineToggle(false);
                gestureMapFragment4.disableGestureDetection();
                break;
        }
    }

    @Override
    public void editorToolLongClicked(EditorTools tools) {
        switch (tools) {
            case TRASH: {
                // Clear the mission?
                doClearMissionConfirmation();
                break;
            }

            default: {
                break;
            }
        }
    }

    private void enableSplineToggle(boolean isEnabled) {
        if (mSplineToggleContainer != null) {
            mSplineToggleContainer.setVisibility(isEnabled ? View.VISIBLE : View.INVISIBLE);
        }
    }

    private void showItemDetail(MissionDetailFragment itemDetail) {
        if (itemDetailFragment == null) {
            addItemDetail(itemDetail);
        } else {
            switchItemDetail(itemDetail);
        }
    }

    private void addItemDetail(MissionDetailFragment itemDetail) {
        itemDetailFragment = itemDetail;
        if (itemDetailFragment == null)
            return;

        if (mContainerItemDetail == null) {
            itemDetailFragment.show(fragmentManager, ITEM_DETAIL_TAG);
        } else {
            fragmentManager.beginTransaction()
                    .replace(R.id.containerItemDetail, itemDetailFragment, ITEM_DETAIL_TAG)
                    .commit();
        }
    }

    public void switchItemDetail(MissionDetailFragment itemDetail) {
        removeItemDetail();
        addItemDetail(itemDetail);
    }

    private void removeItemDetail() {
        if (itemDetailFragment != null) {
            if (mContainerItemDetail == null) {
                itemDetailFragment.dismiss();
            } else {
                fragmentManager.beginTransaction().remove(itemDetailFragment).commit();
            }
            itemDetailFragment = null;
        }
    }
    /********************************/
    /********************************/

    @Override
    public void onPathFinished(List<Coord2D> path) {


        Log.d(EDITORFLUX, "EditorActivity  -  onPathFinished()");
        List<Coord2D> points = planningMapFragment.projectPathIntoMap(path);
        switch (getTool(1)) {
            case DRAW:
                if (mIsSplineEnabled) {
                    missionProxy.addSplineWaypoints(points);
                } else {
                    missionProxy.addWaypoints(points);
                }
                break;

            case POLY:
                if (path.size() > 2) {
                    missionProxy.addSurveyPolygon(points);
                } else {
                    editorToolsFragment.setTool(EditorTools.POLY);
                    return;
                }
                break;

            default:
                break;
        }
        editorToolsFragment.setTool(EditorTools.NONE);

    }

    /********************************/
    /********************************/
    @Override
    public void onPathFinished2(List<Coord2D> path) {
        Log.d(EDITORFLUX, "EditorActivity  -  onPathFinished2()");

        List<Coord2D> points = planningMapFragment2.projectPathIntoMap(path);
        switch (getTool(2)) {
            case DRAW:
                if (mIsSplineEnabled2) {
                    missionProxy2.addSplineWaypoints(points);
                } else {
                    missionProxy2.addWaypoints(points);
                }
                break;

            case POLY:
                if (path.size() > 2) {
                    missionProxy2.addSurveyPolygon(points);
                } else {
                    editorToolsFragment2.setTool(EditorTools.POLY);
                    return;
                }
                break;

            default:
                break;
        }
        editorToolsFragment2.setTool(EditorTools.NONE);

    }


    /********************************/
    /********************************/

    @Override
    public void onPathFinished3(List<Coord2D> path) {
        Log.d(EDITORFLUX, "EditorActivity  -  onPathFinished3()");
        List<Coord2D> points = planningMapFragment3.projectPathIntoMap(path);
        switch (getTool(3)) {
            case DRAW:
                if (mIsSplineEnabled3) {
                    missionProxy3.addSplineWaypoints(points);
                } else {
                    missionProxy3.addWaypoints(points);
                }
                break;

            case POLY:
                if (path.size() > 2) {
                    missionProxy3.addSurveyPolygon(points);
                } else {
                    editorToolsFragment3.setTool(EditorTools.POLY);
                    return;
                }
                break;

            default:
                break;
        }
        editorToolsFragment3.setTool(EditorTools.NONE);
    }

    /********************************/
    /********************************/

    @Override
    public void onPathFinished4(List<Coord2D> path) {
        Log.d(EDITORFLUX, "EditorActivity  -  onPathFinished4()");
        List<Coord2D> points = planningMapFragment4.projectPathIntoMap(path);
        switch (getTool(4)) {
            case DRAW:
                if (mIsSplineEnabled4) {
                    missionProxy4.addSplineWaypoints(points);
                } else {
                    missionProxy4.addWaypoints(points);
                }
                break;

            case POLY:
                if (path.size() > 2) {
                    missionProxy4.addSurveyPolygon(points);
                } else {
                    editorToolsFragment4.setTool(EditorTools.POLY);
                    return;
                }
                break;

            default:
                break;
        }
        editorToolsFragment4.setTool(EditorTools.NONE);
    }

    /********************************/
    /********************************/

    @Override
    public void onDetailDialogDismissed(List<MissionItemProxy> itemList) {
        missionProxy.selection.removeItemsFromSelection(itemList);
    }

    @Override
    public void onWaypointTypeChanged(List<Pair<MissionItemProxy, MissionItemProxy>> oldNewItemsList) {
        missionProxy.replaceAll(oldNewItemsList);
    }

    @Override
    public boolean onActionItemClicked(ActionMode mode, MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_action_multi_edit:
                if(mMultiEditEnabled){
                    removeItemDetail();
                    enableMultiEdit(false);
                    return true;
                }

                final List<MissionItemProxy> selectedProxies = missionProxy.selection.getSelected();
                if(selectedProxies.size() >= 1){
                    showItemDetail(selectMissionDetailType(selectedProxies));
                    enableMultiEdit(true);
                }
                else {
                    Toast.makeText(getApplicationContext(), "No Waypoint(s) selected.", Toast.LENGTH_LONG)
                            .show();
                }

                HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                        .setCategory(GAUtils.Category.EDITOR)
                        .setAction("Action mode button")
                        .setLabel("Multi edit");
                GAUtils.sendEvent(eventBuilder);

                return true;

            case R.id.menu_action_delete:
                missionProxy.removeSelection(missionProxy.selection);
                mode.finish();
                planningMapFragment.zoomToFit();
                return true;

            case R.id.menu_action_reverse:
                missionProxy.reverse();
                return true;

            default:
                return false;
        }
    }

    private MissionDetailFragment selectMissionDetailType(List<MissionItemProxy> proxies){
        if(proxies == null || proxies.isEmpty())
            return null;

        MissionItemType referenceType = null;
        for(MissionItemProxy proxy: proxies){
            final MissionItemType proxyType = proxy.getMissionItem().getType();
            if(referenceType == null){
                referenceType = proxyType;
            }
            else if(referenceType != proxyType){
                //Return a generic mission detail.
                return new MissionDetailFragment();
            }
        }

        return MissionDetailFragment.newInstance(referenceType);
    }

    @Override
    public boolean onCreateActionMode(ActionMode mode, Menu menu) {
        mode.getMenuInflater().inflate(R.menu.action_mode_editor, menu);
        editorToolsFragment.getView().setVisibility(View.INVISIBLE);
        return true;
    }

    @Override
    public void onDestroyActionMode(ActionMode arg0) {
        missionListFragment.updateChoiceMode(AbsListView.CHOICE_MODE_SINGLE);
        missionProxy.selection.clearSelection();

        contextualActionBar = null;
        enableMultiEdit(false);

        editorToolsFragment.getView().setVisibility(View.VISIBLE);
    }

    private void enableMultiEdit(boolean enable){
        mMultiEditEnabled = enable;

        if(contextualActionBar != null){
            final Menu menu = contextualActionBar.getMenu();
            final MenuItem multiEdit = menu.findItem(R.id.menu_action_multi_edit);
            multiEdit.setIcon(mMultiEditEnabled
                    ? R.drawable.ic_action_copy_blue
                    : R.drawable.ic_action_copy);
        }
    }

    @Override
    public boolean onPrepareActionMode(ActionMode mode, Menu menu) {
        return false;
    }

    @Override
    public boolean onItemLongClick(MissionItemProxy item) {
        enableMultiEdit(false);
        if (contextualActionBar != null) {
            if (missionProxy.selection.selectionContains(item)) {
                missionProxy.selection.clearSelection();
            } else {
                missionProxy.selection.setSelectionTo(missionProxy.getItems());
            }
        } else {
            editorToolsFragment.setTool(EditorTools.NONE);
            missionListFragment.updateChoiceMode(AbsListView.CHOICE_MODE_MULTIPLE);
            contextualActionBar = startActionMode(this);
            missionProxy.selection.setSelectionTo(item);
        }
        return true;
    }

    @Override
    public void onItemClick(MissionItemProxy item, boolean zoomToFit) {
        enableMultiEdit(false);
        switch (getTool(1)) {
            default:
                if (contextualActionBar != null) {
                    if (missionProxy.selection.selectionContains(item)) {
                        missionProxy.selection.removeItemFromSelection(item);
                    } else {
                        missionProxy.selection.addToSelection(item);
                    }
                } else {
                    if (missionProxy.selection.selectionContains(item)) {
                        missionProxy.selection.clearSelection();
                    } else {
                        editorToolsFragment.setTool(EditorTools.NONE);
                        missionProxy.selection.setSelectionTo(item);
                    }
                }

                break;

            case TRASH:
                missionProxy.removeItem(item);
                missionProxy.selection.clearSelection();

                if (missionProxy.getItems().size() <= 0) {
                    editorToolsFragment.setTool(EditorTools.NONE);
                }
                break;
        }

        if(zoomToFit) {
            List<MissionItemProxy> selected = missionProxy.selection.getSelected();
            if (selected.isEmpty()) {
                planningMapFragment.zoomToFit();
            }
            else{
                planningMapFragment.zoomToFit(MissionProxy.getVisibleCoords(selected));
            }
        }
    }

    @Override
    public void onListVisibilityChanged() {}

    @Override
    protected boolean enableMissionMenus(){
        return true;
    }

    @Override
    public void onSelectionUpdate(List<MissionItemProxy> selected) {

        final boolean isEmpty = selected.isEmpty();

        Log.d(EDITORFLUX, "EditorActivity  -  onSelectionUpdate()");

        missionListFragment.setArrowsVisibility(!isEmpty);

        if (isEmpty) {
            removeItemDetail();
        } else {
            if (contextualActionBar != null && !mMultiEditEnabled)
                removeItemDetail();
            else {
                showItemDetail(selectMissionDetailType(selected));
            }
        }

        planningMapFragment.postUpdate();
    }

    private void doClearMissionConfirmation() {
        YesNoDialog ynd = YesNoDialog.newInstance(getApplicationContext(), getString(R.string
                        .dlg_clear_mission_title),
                getString(R.string.dlg_clear_mission_confirm), new YesNoDialog.Listener() {
                    @Override
                    public void onYes() {
                        missionProxy.clear();
                        missionProxy.addTakeoff();
                    }

                    @Override
                    public void onNo() {
                    }
                });

        if(ynd != null) {
            ynd.show(getSupportFragmentManager(), "clearMission");
        }
    }

    public void initializeMaps(Bundle savedInstanceState)
    {
        initMap1(savedInstanceState);
        initMap2(savedInstanceState);
        initMap3(savedInstanceState);
        initMap4(savedInstanceState);

    }


    public void updateLayout()
    {

    }

    public void initMap1(Bundle savedInstanceState)
    {
        planningMapFragment = ((EditorMapFragment) fragmentManager
                .findFragmentById(R.id.mapFragment));
        planningMapFragment = EditorMapFragment.newInstance(1);
        fragmentManager.beginTransaction().add(R.id.mapFragment, planningMapFragment).commit();


        gestureMapFragment = ((GestureMapFragment) fragmentManager
                .findFragmentById(R.id.gestureMapFragment));
        editorToolsFragment = (EditorToolsFragment) fragmentManager
                .findFragmentById(R.id.flightActionsFragment);
        missionListFragment = (EditorListFragment) fragmentManager
                .findFragmentById(R.id.missionFragment1);

        mSplineToggleContainer = findViewById(R.id.editorSplineToggleContainer);
        mSplineToggleContainer.setVisibility(View.VISIBLE);

        infoView = (TextView) findViewById(R.id.editorInfoWindow);

        setupButtons(1);

        normalToggle = (RadioButton) findViewById(R.id.normalWpToggle);
        normalToggle.setOnClickListener(this);
        splineToggle = (RadioButton) findViewById(R.id.splineWpToggle);
        splineToggle.setOnClickListener(this);

        if(savedInstanceState != null){
            mIsSplineEnabled = savedInstanceState.getBoolean(EXTRA_IS_SPLINE_ENABLED);
        }

        // Retrieve the item detail fragment using its tag
        itemDetailFragment = (MissionDetailFragment) fragmentManager
                .findFragmentByTag(ITEM_DETAIL_TAG);

		/*
		 * On phone, this view will be null causing the item detail to be shown
		 * as a dialog.
		 */
        mContainerItemDetail = findViewById(R.id.containerItemDetail);

        final DroidPlannerApp dpApp = ((DroidPlannerApp) getApplication());
        missionProxy = dpApp.getMissionProxy();
        gestureMapFragment.setOnPathFinishedListener(this);

    }

    public void initMap2(Bundle savedInstanceState)
    {
        planningMapFragment2 = ((EditorMapFragment) fragmentManager
                .findFragmentById(R.id.mapFragment2));
        planningMapFragment2 = EditorMapFragment.newInstance(2);
        fragmentManager.beginTransaction().add(R.id.mapFragment2, planningMapFragment2).commit();

        gestureMapFragment2 = ((GestureMapFragment2) fragmentManager
                .findFragmentById(R.id.gestureMapFragment2));
        editorToolsFragment2 = (EditorToolsFragment2) fragmentManager
                .findFragmentById(R.id.flightActionsFragment2);
        missionListFragment2 = (EditorListFragment2) fragmentManager
                .findFragmentById(R.id.missionFragment2);

        mSplineToggleContainer2 = findViewById(R.id.editorSplineToggleContainer2);
        mSplineToggleContainer2.setVisibility(View.VISIBLE);

        infoView2 = (TextView) findViewById(R.id.editorInfoWindow2);

        setupButtons(2);

        normalToggle2 = (RadioButton) findViewById(R.id.normalWpToggle2);
        normalToggle2.setOnClickListener(this);
        splineToggle2 = (RadioButton) findViewById(R.id.splineWpToggle2);
        splineToggle2.setOnClickListener(this);

        if(savedInstanceState != null){
            mIsSplineEnabled2 = savedInstanceState.getBoolean(EXTRA_IS_SPLINE_ENABLED);
        }

        // Retrieve the item detail fragment using its tag
        itemDetailFragment2 = (MissionDetailFragment) fragmentManager
                .findFragmentByTag(ITEM_DETAIL_TAG);

		/*
		 * On phone, this view will be null causing the item detail to be shown
		 * as a dialog.
		 */
        mContainerItemDetail2 = findViewById(R.id.containerItemDetail2);

        final DroidPlannerApp dpApp = ((DroidPlannerApp) getApplication());
        missionProxy2 = dpApp.getMissionProxy(2);
        planningMapFragment2.setMissionProxy(missionProxy2);

        gestureMapFragment2.setOnPathFinishedListener(this);
    }

    public void initMap3(Bundle savedInstanceState)
    {
        planningMapFragment3 = ((EditorMapFragment) fragmentManager
                .findFragmentById(R.id.mapFragment3));
        planningMapFragment3 = EditorMapFragment.newInstance(3);
        fragmentManager.beginTransaction().add(R.id.mapFragment3, planningMapFragment3).commit();


        gestureMapFragment3 = ((GestureMapFragment3) fragmentManager
                .findFragmentById(R.id.gestureMapFragment3));
        editorToolsFragment3 = (EditorToolsFragment3) fragmentManager
                .findFragmentById(R.id.flightActionsFragment3);
        missionListFragment3 = (EditorListFragment3) fragmentManager
                .findFragmentById(R.id.missionFragment3);

        mSplineToggleContainer3 = findViewById(R.id.editorSplineToggleContainer3);
        mSplineToggleContainer3.setVisibility(View.VISIBLE);

        infoView3 = (TextView) findViewById(R.id.editorInfoWindow3);

        setupButtons(3);

        normalToggle3 = (RadioButton) findViewById(R.id.normalWpToggle3);
        normalToggle3.setOnClickListener(this);
        splineToggle3 = (RadioButton) findViewById(R.id.splineWpToggle3);
        splineToggle3.setOnClickListener(this);

        if(savedInstanceState != null){
            mIsSplineEnabled3 = savedInstanceState.getBoolean(EXTRA_IS_SPLINE_ENABLED);
        }

        // Retrieve the item detail fragment using its tag
        itemDetailFragment3 = (MissionDetailFragment) fragmentManager
                .findFragmentByTag(ITEM_DETAIL_TAG);

		/*
		 * On phone, this view will be null causing the item detail to be shown
		 * as a dialog.
		 */
        mContainerItemDetail3 = findViewById(R.id.containerItemDetail3);

        final DroidPlannerApp dpApp = ((DroidPlannerApp) getApplication());
        missionProxy3 = dpApp.getMissionProxy(3);
        planningMapFragment3.setMissionProxy(missionProxy3);

        gestureMapFragment3.setOnPathFinishedListener(this);
    }

    public void initMap4(Bundle savedInstanceState)
    {
        planningMapFragment4 = ((EditorMapFragment) fragmentManager
                .findFragmentById(R.id.mapFragment4));
        planningMapFragment4 = EditorMapFragment.newInstance(4);
        fragmentManager.beginTransaction().add(R.id.mapFragment4, planningMapFragment4).commit();

        gestureMapFragment4 = ((GestureMapFragment4) fragmentManager
                .findFragmentById(R.id.gestureMapFragment4));
        editorToolsFragment4 = (EditorToolsFragment4) fragmentManager
                .findFragmentById(R.id.flightActionsFragment4);
        missionListFragment4 = (EditorListFragment4) fragmentManager
                .findFragmentById(R.id.missionFragment4);

        mSplineToggleContainer4 = findViewById(R.id.editorSplineToggleContainer4);
        mSplineToggleContainer4.setVisibility(View.VISIBLE);

        infoView4 = (TextView) findViewById(R.id.editorInfoWindow4);

        setupButtons(4);

        normalToggle4 = (RadioButton) findViewById(R.id.normalWpToggle4);
        normalToggle4.setOnClickListener(this);
        splineToggle4 = (RadioButton) findViewById(R.id.splineWpToggle4);
        splineToggle4.setOnClickListener(this);

        if(savedInstanceState != null){
            mIsSplineEnabled4 = savedInstanceState.getBoolean(EXTRA_IS_SPLINE_ENABLED);
        }

        // Retrieve the item detail fragment using its tag
        itemDetailFragment4 = (MissionDetailFragment) fragmentManager
                .findFragmentByTag(ITEM_DETAIL_TAG);

		/*
		 * On phone, this view will be null causing the item detail to be shown
		 * as a dialog.
		 */
        mContainerItemDetail4 = findViewById(R.id.containerItemDetail4);

        final DroidPlannerApp dpApp = ((DroidPlannerApp) getApplication());
        missionProxy4 = dpApp.getMissionProxy(4);
        planningMapFragment4.setMissionProxy(missionProxy4);

        gestureMapFragment4.setOnPathFinishedListener(this);
    }


    public void setupButtons(int num_map)
    {
        switch(num_map)
        {
            case 1:
                /**************************************************************************/
                final ImageButton mGoToMyLocation1, mGoToDroneLocation1, mExpandMap1;
                final ImageButton mGoToMyLocation2, mGoToDroneLocation2, mExpandMap2;
                final ImageButton mGoToMyLocation3, mGoToDroneLocation3, mExpandMap3;
                final ImageButton mGoToMyLocation4, mGoToDroneLocation4, mExpandMap4;
                final ImageButton mAllPOIs1, mAllPOIs2, mAllPOIs3, mAllPOIs4;


                mGoToMyLocation1 = (ImageButton) findViewById(R.id.my_location_button);
                mGoToDroneLocation1 = (ImageButton) findViewById(R.id.drone_location_button);
                mExpandMap1 = (ImageButton) findViewById(R.id.expand_map_button);
                mAllPOIs1 = (ImageButton) findViewById(R.id.all_waypoints_button);

                final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button);
                resetMapBearing.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        updateMapBearing(0, planningMapFragment);
                    }
                });

                mAllPOIs1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment != null) {

                            //enableAlgorithmMenu(1);

                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment);
                        }
                    }
                });

                mExpandMap1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment != null) {

                            expandMap(1);

                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment);
                        }
                    }
                });


                mGoToMyLocation1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment != null) {
                            planningMapFragment.goToMyLocation();
                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment);
                        }
                    }
                });
                mGoToMyLocation1.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (planningMapFragment != null) {
                            planningMapFragment.goToMyLocation();
                            updateMapLocationButtons(AutoPanMode.USER, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment);
                            return true;
                        }
                        return false;
                    }
                });

                mGoToDroneLocation1.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment != null) {
                            planningMapFragment.goToDroneLocation();
                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment);
                        }
                    }
                });
                mGoToDroneLocation1.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (planningMapFragment != null) {
                            planningMapFragment.goToDroneLocation();
                            updateMapLocationButtons(AutoPanMode.DRONE, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment);
                            return true;
                        }
                        return false;
                    }
                });
                /**************************************************************************/
                break;
            case 2:
                /**************************************************************************/
                mGoToMyLocation2 = (ImageButton) findViewById(R.id.my_location_button2);
                mGoToDroneLocation2 = (ImageButton) findViewById(R.id.drone_location_button2);
                mExpandMap2 = (ImageButton) findViewById(R.id.expand_map_button2);
                mAllPOIs2 = (ImageButton) findViewById(R.id.all_waypoints_button2);

                mAllPOIs2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment2 != null) {

                            //enableAlgorithmMenu(2);

                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation2, mGoToDroneLocation2, planningMapFragment2);
                        }
                    }
                });


                mExpandMap2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment2 != null) {

                            expandMap(2);

                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation2, mGoToDroneLocation2, planningMapFragment2);
                        }
                    }
                });
                mGoToMyLocation2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment2 != null) {
                            planningMapFragment2.goToMyLocation();
                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation2, mGoToDroneLocation2, planningMapFragment2);
                        }
                    }
                });
                mGoToMyLocation2.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (planningMapFragment2 != null) {
                            planningMapFragment2.goToMyLocation();
                            updateMapLocationButtons(AutoPanMode.USER, mGoToMyLocation2, mGoToDroneLocation2, planningMapFragment2);
                            return true;
                        }
                        return false;
                    }
                });

                mGoToDroneLocation2.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment2 != null) {
                            planningMapFragment2.goToDroneLocation();
                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation2, mGoToDroneLocation2, planningMapFragment2);
                        }
                    }
                });
                mGoToDroneLocation2.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (planningMapFragment2 != null) {
                            planningMapFragment2.goToDroneLocation();
                            updateMapLocationButtons(AutoPanMode.DRONE, mGoToMyLocation2, mGoToDroneLocation2, planningMapFragment2);
                            return true;
                        }
                        return false;
                    }
                });
                /**************************************************************************/
                break;
            case 3:
                /**************************************************************************/
                mGoToMyLocation3 = (ImageButton) findViewById(R.id.my_location_button3);
                mGoToDroneLocation3 = (ImageButton) findViewById(R.id.drone_location_button3);
                mExpandMap3 = (ImageButton) findViewById(R.id.expand_map_button3);
                mAllPOIs3 = (ImageButton) findViewById(R.id.all_waypoints_button3);

                mAllPOIs3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment3 != null) {

                            //enableAlgorithmMenu(3);

                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation3, mGoToDroneLocation3, planningMapFragment3);
                        }
                    }
                });


                mExpandMap3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment3 != null) {

                            expandMap(3);

                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation3, mGoToDroneLocation3, planningMapFragment3);
                        }
                    }
                });
                mGoToMyLocation3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment3 != null) {
                            planningMapFragment3.goToMyLocation();
                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation3, mGoToDroneLocation3, planningMapFragment3);
                        }
                    }
                });
                mGoToMyLocation3.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (planningMapFragment3 != null) {
                            planningMapFragment3.goToMyLocation();
                            updateMapLocationButtons(AutoPanMode.USER, mGoToMyLocation3, mGoToDroneLocation3, planningMapFragment3);
                            return true;
                        }
                        return false;
                    }
                });

                mGoToDroneLocation3.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment3 != null) {
                            planningMapFragment3.goToDroneLocation();
                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation3, mGoToDroneLocation3, planningMapFragment3);
                        }
                    }
                });
                mGoToDroneLocation3.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (planningMapFragment3 != null) {
                            planningMapFragment3.goToDroneLocation();
                            updateMapLocationButtons(AutoPanMode.DRONE, mGoToMyLocation3, mGoToDroneLocation3, planningMapFragment3);
                            return true;
                        }
                        return false;
                    }
                });
                /**************************************************************************/
                break;
            case 4:
                /**************************************************************************/
                mGoToMyLocation4 = (ImageButton) findViewById(R.id.my_location_button4);
                mGoToDroneLocation4 = (ImageButton) findViewById(R.id.drone_location_button4);
                mExpandMap4 = (ImageButton) findViewById(R.id.expand_map_button4);
                mAllPOIs4 = (ImageButton) findViewById(R.id.all_waypoints_button4);

                mAllPOIs4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment4 != null) {

                            //enableAlgorithmMenu(4);

                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation4, mGoToDroneLocation4, planningMapFragment4);
                        }
                    }
                });


                mExpandMap4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment4 != null) {

                            expandMap(4);

                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation4, mGoToDroneLocation4, planningMapFragment4);
                        }
                    }
                });
                mGoToMyLocation4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment4 != null) {
                            planningMapFragment4.goToMyLocation();
                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation4, mGoToDroneLocation4, planningMapFragment4);
                        }
                    }
                });
                mGoToMyLocation4.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (planningMapFragment4 != null) {
                            planningMapFragment4.goToMyLocation();
                            updateMapLocationButtons(AutoPanMode.USER, mGoToMyLocation4, mGoToDroneLocation4, planningMapFragment4);
                            return true;
                        }
                        return false;
                    }
                });

                mGoToDroneLocation4.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (planningMapFragment4 != null) {
                            planningMapFragment4.goToDroneLocation();
                            updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation4, mGoToDroneLocation4, planningMapFragment4);
                        }
                    }
                });
                mGoToDroneLocation4.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View v) {
                        if (planningMapFragment4 != null) {
                            planningMapFragment4.goToDroneLocation();
                            updateMapLocationButtons(AutoPanMode.DRONE, mGoToMyLocation4, mGoToDroneLocation4, planningMapFragment4);
                            return true;
                        }
                        return false;
                    }
                });
                /**************************************************************************/
                break;
        }
    }

    private void updateMapLocationButtons(AutoPanMode mode, ImageButton mGoToMyLocation, ImageButton mGoToDroneLocation, EditorMapFragment mapFragment) {
        mGoToMyLocation.setActivated(false);
        mGoToDroneLocation.setActivated(false);

        if (mapFragment != null) {
            mapFragment.setAutoPanMode(mode);
        }

        switch (mode) {
            case DRONE:
                mGoToDroneLocation.setActivated(true);
                break;

            case USER:
                mGoToMyLocation.setActivated(true);
                break;
            default:
                break;
        }
    }

    public void updateMapBearing(float bearing, EditorMapFragment mapFragment){
        if(mapFragment != null)
            mapFragment.updateMapBearing(bearing);
    }

    private void expandMap(int selected_map)
    {
        LinearLayout lLayout;
        FrameLayout fLayout;

        //Se mapa expandiu, retornar as miniaturas.
        if(mapExpanded && NUM_MAPS > 1)
        {
            mapExpanded = false;
            updateMultiLayout();

        }
        //Se miniatura, expandir mapa selecionado
        else
        {
            mapExpanded = true;
            switch(NUM_MAPS)
            {
                case 2:
                    if(selected_map == 1){
                        lLayout = (LinearLayout) findViewById(R.id.edit_layout_right);
                        lLayout.setVisibility(LinearLayout.GONE);

                    }
                    else {
                        lLayout = (LinearLayout) findViewById(R.id.edit_layout_left);
                        lLayout.setVisibility(LinearLayout.GONE);
                    }
                    break;
                case 3:
                    if(selected_map == 1){
                        lLayout = (LinearLayout) findViewById(R.id.edit_layout_right);
                        lLayout.setVisibility(LinearLayout.GONE);

                        fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment3);
                        fLayout.setVisibility(FrameLayout.GONE);


                    }
                    else if(selected_map ==2){
                        lLayout = (LinearLayout) findViewById(R.id.edit_layout_left);
                        lLayout.setVisibility(LinearLayout.GONE);
                    }
                    else{
                        lLayout = (LinearLayout) findViewById(R.id.edit_layout_right);
                        lLayout.setVisibility(LinearLayout.GONE);

                        fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment);
                        fLayout.setVisibility(FrameLayout.GONE);
                    }
                    break;
                case 4:
                    if(selected_map == 1){
                        lLayout = (LinearLayout) findViewById(R.id.edit_layout_right);
                        lLayout.setVisibility(LinearLayout.GONE);


                        fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment3);
                        fLayout.setVisibility(FrameLayout.GONE);
                    }
                    else if(selected_map == 2){
                        lLayout = (LinearLayout) findViewById(R.id.edit_layout_left);
                        lLayout.setVisibility(LinearLayout.GONE);

                        fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment4);
                        fLayout.setVisibility(FrameLayout.GONE);
                    }
                    else if(selected_map == 3){
                        lLayout = (LinearLayout) findViewById(R.id.edit_layout_right);
                        lLayout.setVisibility(LinearLayout.GONE);

                        fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment);
                        fLayout.setVisibility(FrameLayout.GONE);
                    }
                    else {
                        lLayout = (LinearLayout) findViewById(R.id.edit_layout_left);
                        lLayout.setVisibility(LinearLayout.GONE);

                        fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment2);
                        fLayout.setVisibility(FrameLayout.GONE);
                    }
                    break;
                default:break;
            }
        }
    }

    private void updateMultiLayout()
    {
        /*
        * edit_multiple_layout (Linear)
        *       edit_layout_left (Linear)
        *           edit_multi_fragment (Fragment)
        *           edit_multi_fragment3
        *       edit_layout_right
        *           edit_multi_fragment2
        *           edit_multi_fragment4
        * */

        FrameLayout fLayout;
        LinearLayout lLayout;
        switch(NUM_MAPS)
        {
            case 1:
                lLayout = (LinearLayout) findViewById(R.id.edit_layout_left);
                lLayout.setVisibility(LinearLayout.VISIBLE);

                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment);
                fLayout.setVisibility(FrameLayout.VISIBLE);

                lLayout = (LinearLayout) findViewById(R.id.edit_layout_right);
                lLayout.setVisibility(LinearLayout.GONE);

                break;
            case 2:
                lLayout = (LinearLayout) findViewById(R.id.edit_layout_left);
                lLayout.setVisibility(LinearLayout.VISIBLE);
                lLayout = (LinearLayout) findViewById(R.id.edit_layout_right);
                lLayout.setVisibility(LinearLayout.VISIBLE);

                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment);
                fLayout.setVisibility(FrameLayout.VISIBLE);
                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment2);
                fLayout.setVisibility(FrameLayout.VISIBLE);


                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment3);
                fLayout.setVisibility(FrameLayout.GONE);
                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment4);
                fLayout.setVisibility(FrameLayout.GONE);

                break;
            case 3:
                lLayout = (LinearLayout) findViewById(R.id.edit_layout_left);
                lLayout.setVisibility(LinearLayout.VISIBLE);
                lLayout = (LinearLayout) findViewById(R.id.edit_layout_right);
                lLayout.setVisibility(LinearLayout.VISIBLE);

                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment);
                fLayout.setVisibility(FrameLayout.VISIBLE);
                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment2);
                fLayout.setVisibility(FrameLayout.VISIBLE);
                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment3);
                fLayout.setVisibility(FrameLayout.VISIBLE);

                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment4);
                fLayout.setVisibility(FrameLayout.GONE);

                break;
            case 4:
                lLayout = (LinearLayout) findViewById(R.id.edit_layout_left);
                lLayout.setVisibility(LinearLayout.VISIBLE);
                lLayout = (LinearLayout) findViewById(R.id.edit_layout_right);
                lLayout.setVisibility(LinearLayout.VISIBLE);

                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment);
                fLayout.setVisibility(FrameLayout.VISIBLE);
                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment2);
                fLayout.setVisibility(FrameLayout.VISIBLE);
                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment3);
                fLayout.setVisibility(FrameLayout.VISIBLE);
                fLayout = (FrameLayout) findViewById(R.id.edit_multi_fragment4);
                fLayout.setVisibility(FrameLayout.VISIBLE);


                break;
        }

    }

    @Override
    public void editorToolChanged2(EditorTools tools)
    {
        missionProxy2.selection.clearSelection();
        setupTool(tools);
    }

    @Override
    public void editorToolLongClicked2(EditorTools tools)
    {

    }

    @Override
    public void editorToolChanged3(EditorTools tools)
    {
        missionProxy3.selection.clearSelection();
        setupTool(tools);
    }

    @Override
    public void editorToolLongClicked3(EditorTools tools)
    {

    }

    @Override
    public void editorToolChanged4(EditorTools tools)
    {
        missionProxy4.selection.clearSelection();
        setupTool(tools);
    }

    @Override
    public void editorToolLongClicked4(EditorTools tools)
    {

    }

}
