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
import org.droidplanner.android.fragments.EditorMapFragment;
import org.droidplanner.android.fragments.EditorToolsFragment;
import org.droidplanner.android.fragments.EditorToolsFragment.EditorTools;
import org.droidplanner.android.fragments.EditorToolsFragment.OnEditorToolSelected;
import org.droidplanner.android.fragments.FlightMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment;
import org.droidplanner.android.fragments.helpers.GestureMapFragment.OnPathFinishedListener;
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

import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.MAVLink.common.msg_mission_item;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;

/**
 * This implements the map editor activity. The map editor activity allows the
 * user to create and/or modify autonomous missions for the drone.
 */
public class EditorActivity extends DrawerNavigationUI implements OnPathFinishedListener,
        OnEditorToolSelected, MissionDetailFragment.OnMissionDetailListener, OnEditorInteraction,
        Callback, MissionSelection.OnSelectionUpdateListener, OnClickListener, OnLongClickListener {

    private static final int GOOGLE_PLAY_SERVICES_REQUEST_CODE = 101;

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
    private MissionProxy missionProxy;

    /*
     * View widgets.
     */
    private EditorMapFragment expandedMapFragment;
    private EditorMapFragment planningMapFragment1, planningMapFragment2, planningMapFragment3,planningMapFragment4;
    private GestureMapFragment gestureMapFragment;
    private EditorToolsFragment editorToolsFragment;
    private MissionDetailFragment itemDetailFragment;
    private FragmentManager fragmentManager;
    private EditorListFragment missionListFragment;

    private View mSplineToggleContainer;
    private boolean mIsSplineEnabled;

    private TextView infoView;

    private boolean mMultiEditEnabled;
    private boolean mapExpanded = false;

    private int NUM_MAPS = 3;

    private RelativeLayout expandedViewLayout;

    /**
     * This view hosts the mission item detail fragment. On phone, or device
     * with limited screen estate, it's removed from the layout, and the item
     * detail ends up displayed as a dialog.
     */
    private View mContainerItemDetail;

    private ActionMode contextualActionBar;
    private RadioButton normalToggle;
    private RadioButton splineToggle;

    private boolean mAllPOIsOpen = false, mAllPOIsOpen2 = false, mAllPOIsOpen3 = false, mAllPOIsOpen4 = false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editor);


        fragmentManager = getSupportFragmentManager();

        //NUM_MAPS = ((DroidPlannerApp) getApplication()).getDroneList().size();
        loadMiniatureLayout();


        expandedViewLayout = (RelativeLayout) findViewById(R.id.expanded_edit_view);
         /*DO NOT SHOW EDIT OPTIONS IF MORE THAN ONE DRONE CONNECTED*/
        //       if(numDronesOsCreate > 1)
        expandedViewLayout.setVisibility(RelativeLayout.GONE);

        expandedMapFragment = ((EditorMapFragment) fragmentManager
                .findFragmentById(R.id.mapFragment));
        gestureMapFragment = ((GestureMapFragment) fragmentManager
                .findFragmentById(R.id.gestureMapFragment));
        editorToolsFragment = (EditorToolsFragment) fragmentManager
                .findFragmentById(R.id.flightActionsFragment);
        missionListFragment = (EditorListFragment) fragmentManager
                .findFragmentById(R.id.missionFragment1);

        mSplineToggleContainer = findViewById(R.id.editorSplineToggleContainer);
        mSplineToggleContainer.setVisibility(View.VISIBLE);

        infoView = (TextView) findViewById(R.id.editorInfoWindow);

        final ImageButton resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button);
        resetMapBearing.setOnClickListener(this);
        final ImageButton zoomToFit = (ImageButton) findViewById(R.id.zoom_to_fit_button);
        zoomToFit.setVisibility(View.VISIBLE);
        zoomToFit.setOnClickListener(this);
        final ImageButton mGoToMyLocation = (ImageButton) findViewById(R.id.my_location_button);
        mGoToMyLocation.setOnClickListener(this);
        mGoToMyLocation.setOnLongClickListener(this);
        final ImageButton mGoToDroneLocation = (ImageButton) findViewById(R.id.drone_location_button);
        mGoToDroneLocation.setOnClickListener(this);
        mGoToDroneLocation.setOnLongClickListener(this);
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

    private static final String CLICKEDIT = "CLICKEDIT";
    @Override
    public void onClick(View v) {

        Log.d(CLICKEDIT, "CLICKEDIT: " + v.getTag());

        switch (v.getId()) {
            case R.id.map_orientation_button:
                if(expandedMapFragment != null) {
                    expandedMapFragment.updateMapBearing(0);
                }
                break;
            case R.id.zoom_to_fit_button:
                if(expandedMapFragment != null){
                    expandedMapFragment.zoomToFit();
                }
                break;
            case R.id.splineWpToggle:
                mIsSplineEnabled = splineToggle.isChecked();
                break;
            case R.id.normalWpToggle:
                mIsSplineEnabled = !normalToggle.isChecked();
                break;
            case R.id.drone_location_button:
                expandedMapFragment.goToDroneLocation();
                break;
            case R.id.my_location_button:
                expandedMapFragment.goToMyLocation();
                break;
            default:
                break;
        }
    }

    @Override
    public boolean onLongClick(View view) {
        switch (view.getId()) {
            case R.id.drone_location_button:
                expandedMapFragment.setAutoPanMode(AutoPanMode.DRONE);
                return true;
            case R.id.my_location_button:
                expandedMapFragment.setAutoPanMode(AutoPanMode.USER);
                return true;
            default:
                return false;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        final DroidPlannerApp dpApp = ((DroidPlannerApp) getApplication());
        missionProxy = dpApp.getMissionProxy();
        editorToolsFragment.setToolAndUpdateView(getTool());
        setupTool(getTool());
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

        final DroidPlannerApp dpApp = ((DroidPlannerApp) getApplication());
        missionProxy = dpApp.getMissionProxy();
        missionProxy.selection.addSelectionUpdateListener(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        missionProxy.selection.removeSelectionUpdateListener(this);
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
                expandedMapFragment.zoomToFit();
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
        expandedMapFragment.saveCameraPosition();
    }

    @Override
    public void onDroneEvent(DroneEventsType event, Drone drone) {
        super.onDroneEvent(event, drone);

        switch (event) {
            case MISSION_UPDATE:
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
                break;

            case MISSION_RECEIVED:
                if (expandedMapFragment != null) {
                    expandedMapFragment.zoomToFit();
                }
                break;

            default:
                break;
        }
    }

    @Override
    public void onMapClick(Coord2D point) {
        enableMultiEdit(false);

        // If an mission item is selected, unselect it.
        missionProxy.selection.clearSelection();

        switch (getTool()) {
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

    public EditorTools getTool() {
        return editorToolsFragment.getTool();
    }

    @Override
    public void editorToolChanged(EditorTools tools) {
        missionProxy.selection.clearSelection();
        setupTool(tools);
    }

    private void setupTool(EditorTools tool) {
        expandedMapFragment.skipMarkerClickEvents(false);
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
                expandedMapFragment.skipMarkerClickEvents(true);
                break;

            case TRASH:
            case NONE:
                enableSplineToggle(false);
                gestureMapFragment.disableGestureDetection();
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

    @Override
    public void onPathFinished(List<Coord2D> path) {
        List<Coord2D> points = expandedMapFragment.projectPathIntoMap(path);
        switch (getTool()) {
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
                expandedMapFragment.zoomToFit();
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
        switch (getTool()) {
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
                expandedMapFragment.zoomToFit();
            }
            else{
                expandedMapFragment.zoomToFit(MissionProxy.getVisibleCoords(selected));
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

        expandedMapFragment.postUpdate();
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


    public void loadMiniatureLayout()
    {
        setupMapFragment();
        setupLocationButtons();
        updateMultiLayout();

    }

    private void setupMapFragment() {
        ImageButton resetMapBearing;

        if (planningMapFragment1 == null && isGooglePlayServicesValid(true)) {
            planningMapFragment1 = (EditorMapFragment) fragmentManager.findFragmentById(R.id.mapFragment1);
            if (planningMapFragment1 == null) {
                planningMapFragment1 = new EditorMapFragment();
                fragmentManager.beginTransaction().add(R.id.mapFragment1, planningMapFragment1).commit();
            }
        }
        resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button1);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, planningMapFragment1);
            }
        });


        if (planningMapFragment2 == null && isGooglePlayServicesValid(true)) {
            planningMapFragment2 = (EditorMapFragment) fragmentManager.findFragmentById(R.id.mapFragment2);
            if (planningMapFragment2 == null) {
                planningMapFragment2 = new EditorMapFragment();
                fragmentManager.beginTransaction().add(R.id.mapFragment2, planningMapFragment2).commit();
            }
        }
        resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button2);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, planningMapFragment2);
            }
        });


        if (planningMapFragment3 == null && isGooglePlayServicesValid(true)) {
            planningMapFragment3 = (EditorMapFragment) fragmentManager.findFragmentById(R.id.mapFragment3);
            if (planningMapFragment3 == null) {
                planningMapFragment3 = new EditorMapFragment();
                fragmentManager.beginTransaction().add(R.id.mapFragment3, planningMapFragment3).commit();
            }
        }
        resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button3);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, planningMapFragment3);
            }
        });

        if (planningMapFragment4 == null && isGooglePlayServicesValid(true)) {
            planningMapFragment4 = (EditorMapFragment) fragmentManager.findFragmentById(R.id.mapFragment4);
            if (planningMapFragment4 == null) {
                planningMapFragment4 = new EditorMapFragment();
                fragmentManager.beginTransaction().add(R.id.mapFragment4, planningMapFragment4).commit();
            }
        }
        resetMapBearing = (ImageButton) findViewById(R.id.map_orientation_button4);
        resetMapBearing.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                updateMapBearing(0, planningMapFragment4);
            }
        });
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

                setEditToolsInvisible(1);
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

                setEditToolsInvisible(2);
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

                setEditToolsInvisible(3);
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

                setEditToolsInvisible(4);
                break;
        }

    }

    private void setupLocationButtons()
    {
        final ImageButton mGoToMyLocation1, mGoToDroneLocation1, mExpandMap1;
        final ImageButton mGoToMyLocation2, mGoToDroneLocation2, mExpandMap2;
        final ImageButton mGoToMyLocation3, mGoToDroneLocation3, mExpandMap3;
        final ImageButton mGoToMyLocation4, mGoToDroneLocation4, mExpandMap4;
        final ImageButton mAllPOIs1, mAllPOIs2, mAllPOIs3, mAllPOIs4;


        mGoToMyLocation1 = (ImageButton) findViewById(R.id.my_location_button1);
        mGoToDroneLocation1 = (ImageButton) findViewById(R.id.drone_location_button1);
        mExpandMap1 = (ImageButton) findViewById(R.id.expand_map_button1);
        mAllPOIs1 = (ImageButton) findViewById(R.id.all_waypoints_button1);

        mAllPOIs1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (planningMapFragment1 != null) {

                    enableAlgorithmMenu(1);

                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment1);
                }
            }
        });

        mExpandMap1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (planningMapFragment1 != null) {

                    expandMap(1);

                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment1);
                }
            }
        });


        mGoToMyLocation1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (planningMapFragment1 != null) {
                    planningMapFragment1.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment1);
                }
            }
        });
        mGoToMyLocation1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (planningMapFragment1 != null) {
                    planningMapFragment1.goToMyLocation();
                    updateMapLocationButtons(AutoPanMode.USER, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment1);
                    return true;
                }
                return false;
            }
        });

        mGoToDroneLocation1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (planningMapFragment1 != null) {
                    planningMapFragment1.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DISABLED, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment1);
                }
            }
        });
        mGoToDroneLocation1.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                if (planningMapFragment1 != null) {
                    planningMapFragment1.goToDroneLocation();
                    updateMapLocationButtons(AutoPanMode.DRONE, mGoToMyLocation1, mGoToDroneLocation1, planningMapFragment1);
                    return true;
                }
                return false;
            }
        });


        /***************************************************************************************************************************/

        mGoToMyLocation2 = (ImageButton) findViewById(R.id.my_location_button2);
        mGoToDroneLocation2 = (ImageButton) findViewById(R.id.drone_location_button2);
        mExpandMap2 = (ImageButton) findViewById(R.id.expand_map_button2);
        mAllPOIs2 = (ImageButton) findViewById(R.id.all_waypoints_button2);

        mAllPOIs2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (planningMapFragment2 != null) {

                    enableAlgorithmMenu(2);

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

        /***************************************************************************************************************************/

        mGoToMyLocation3 = (ImageButton) findViewById(R.id.my_location_button3);
        mGoToDroneLocation3 = (ImageButton) findViewById(R.id.drone_location_button3);
        mExpandMap3 = (ImageButton) findViewById(R.id.expand_map_button3);
        mAllPOIs3 = (ImageButton) findViewById(R.id.all_waypoints_button3);

        mAllPOIs3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (planningMapFragment3 != null) {

                    enableAlgorithmMenu(3);

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

        /***************************************************************************************************************************/

        mGoToMyLocation4 = (ImageButton) findViewById(R.id.my_location_button4);
        mGoToDroneLocation4 = (ImageButton) findViewById(R.id.drone_location_button4);
        mExpandMap4 = (ImageButton) findViewById(R.id.expand_map_button4);
        mAllPOIs4 = (ImageButton) findViewById(R.id.all_waypoints_button4);

        mAllPOIs4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (planningMapFragment4 != null) {

                    enableAlgorithmMenu(4);

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

                        setEditToolsVisible(1);
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

                        setEditToolsVisible(1);


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

                        setEditToolsVisible(1);

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

    public void setEditToolsVisible(int num_map)
    {
        RelativeLayout rLayout;
        switch(num_map)
        {
            case 1:
                rLayout = (RelativeLayout) findViewById(R.id.expanded_edit_view);
                rLayout.setVisibility(RelativeLayout.VISIBLE);
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
        }
    }

    public void setEditToolsInvisible(int num_map)
    {
        RelativeLayout rLayout;
        switch(num_map)
        {
            case 1:
                rLayout = (RelativeLayout) findViewById(R.id.expanded_edit_view);
                rLayout.setVisibility(RelativeLayout.GONE);
                break;
            case 2:
                break;
            case 3:
                break;
            case 4:
                break;
        }
    }


    public void enableAlgorithmMenu(int num_map)
    {
        switch(num_map)
        {
            case 1:
                if(mAllPOIsOpen)
                {
                    mAllPOIsOpen = false;
                    View menu_view = findViewById(R.id.alg_menu1);
                    menu_view.setVisibility(View.GONE);
                }
                else {
                    mAllPOIsOpen = true;
                    View menu_view = findViewById(R.id.alg_menu1);
                    menu_view.setVisibility(View.VISIBLE);
                }
                break;
            case 2:
                if(mAllPOIsOpen2)
                {
                    mAllPOIsOpen2 = false;
                    View menu_view = findViewById(R.id.alg_menu2);
                    menu_view.setVisibility(View.GONE);
                }
                else {
                    mAllPOIsOpen2 = true;
                    View menu_view = findViewById(R.id.alg_menu2);
                    menu_view.setVisibility(View.VISIBLE);
                }
                break;
            case 3:
                if(mAllPOIsOpen3)
                {
                    mAllPOIsOpen3 = false;
                    View menu_view = findViewById(R.id.alg_menu3);
                    menu_view.setVisibility(View.GONE);
                }
                else {
                    mAllPOIsOpen3 = true;
                    View menu_view = findViewById(R.id.alg_menu3);
                    menu_view.setVisibility(View.VISIBLE);
                }
                break;
            case 4:
                if(mAllPOIsOpen4)
                {
                    mAllPOIsOpen4 = false;
                    View menu_view = findViewById(R.id.alg_menu4);
                    menu_view.setVisibility(View.GONE);
                }
                else {
                    mAllPOIsOpen4 = true;
                    View menu_view = findViewById(R.id.alg_menu4);
                    menu_view.setVisibility(View.VISIBLE);
                }
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


    private boolean isGooglePlayServicesValid(boolean showErrorDialog) {
        // Check for the google play services is available
        final int playStatus = GooglePlayServicesUtil
                .isGooglePlayServicesAvailable(getApplicationContext());
        final boolean isValid = playStatus == ConnectionResult.SUCCESS;

        if (!isValid && showErrorDialog) {
            final Dialog errorDialog = GooglePlayServicesUtil.getErrorDialog(playStatus, this,
                    GOOGLE_PLAY_SERVICES_REQUEST_CODE, new DialogInterface.OnCancelListener() {
                        @Override
                        public void onCancel(DialogInterface dialog) {
                            finish();
                        }
                    });

            if (errorDialog != null)
                errorDialog.show();
        }

        return isValid;
    }

    public void updateMapBearing(float bearing, EditorMapFragment mapFragment){
        if(mapFragment != null)
            mapFragment.updateMapBearing(bearing);
    }

}
