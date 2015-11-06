package org.droidplanner.android.widgets.actionProviders;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.Spinner;
import android.widget.TextView;

import com.MAVLink.Messages.ApmModes;
import com.google.android.gms.analytics.HitBuilders;

import org.droidplanner.R;
import org.droidplanner.android.utils.analytics.GAUtils;
import org.droidplanner.android.utils.prefs.DroidPlannerPrefs;
import org.droidplanner.android.widgets.spinners.ModeAdapter;
import org.droidplanner.android.widgets.spinners.SpinnerSelfSelect;
import org.droidplanner.core.model.Drone;

import java.util.Collections;
import java.util.List;
import java.util.Locale;

/**
 * Set of actions supported by the info bar
 */
public abstract class InfoBarItemMulti {

	/**
	 * Default value when there's no data to display.
	 */
	protected static final String sDefaultValue = "--";

	/**
	 * Id for the info action.
	 */
	protected final int mItemId;
    private static final String INFOBAR4 = "INFOBAR4";
    private static final String INFOBAR5 = "INFOBAR5";
	/**
	 * Info bar item view.
	 */
	protected View mItemView;

	protected InfoBarItemMulti(Context context, View parentView, Drone drone, int itemId) {
		mItemId = itemId;
		initItemView(context, parentView, drone);
	}

	/**
	 * This initializes the view backing this info bar item.
	 *
	 * @param context
	 *            application context
	 * @param parentView
	 *            parent view for the info bar item
	 * @param drone
	 *            current drone state
	 */
	protected void initItemView(final Context context, View parentView, Drone drone) {
		mItemView = parentView.findViewById(mItemId);
	}

	/**
	 * @return the info bar item view.
	 */
	public View getItemView() {
		return mItemView;
	}

	public void updateItemView(Context context, Drone drone) {
	}

	/**
	 * This is used, during the creation of the
	 * {@link InfoBarActionProvider}
	 * class, to initialize the info bar action popup window.
	 * 
	 * @param context
	 *            application context
	 */
	protected static PopupWindow initPopupWindow(Context context, int popupViewRes) {
		if (popupViewRes == 0)
			return null;

		final LayoutInflater inflater = LayoutInflater.from(context);
		final View popupView = inflater.inflate(popupViewRes, null);

		final PopupWindow popup = new PopupWindow(popupView, ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT, true);
		popup.setBackgroundDrawable(context.getResources().getDrawable(R.drawable.panel_white_bg));

		return popup;
	}

	/**
	 * Home info bar item: displays the distance of the drone from its home
	 * location.
	 */
	public static class HomeInfo extends InfoBarItemMulti {

		public HomeInfo(Context context, View parentView, Drone drone, int num_map) {
			super(context, parentView, drone, R.id.bar_home);
		}

		@Override
		public void updateItemView(final Context context, final Drone drone) {
			if (mItemView != null) {
				String update = drone == null ? "--" : String.format("Home\n%s", drone.getHome()
						.getDroneDistanceToHome().toString());
				((TextView) mItemView).setText(update);
			}
		}
	}

	/**
	 * Gps info bar item: displays the count of satellites, and other gps
	 * information.
	 */

	public static class GpsInfo extends InfoBarItemMulti {
		private DroidPlannerPrefs mAppPrefs;
        private final String INFOBAR3 = "INFOBAR3";


		public GpsInfo(Context context, View parentView, Drone drone, int num_map) {
			super(context, parentView, drone, R.id.bar_gps);


            if(context == null)
                Log.d(INFOBAR3, "context null infobaritem!");
            else
                Log.d(INFOBAR3, "context NOT NULL infobaritem!");

			mAppPrefs = new DroidPlannerPrefs(context.getApplicationContext());
		}

		@Override
		public void updateItemView(final Context context, final Drone drone) {

            //Log.d(INFOBAR5, "ENTROU PARA ATUALIZAR!!! GEPEESSE");
			if (mItemView != null) {
                Log.d(INFOBAR5, " GEPEESSE != NULL");

				final String update;
				if (drone == null) {
                    Log.d(INFOBAR5, " Droneeee ===== NULL ===>>>(((");
					update = "--";
				} else if (mAppPrefs.shouldGpsHdopBeDisplayed()) {
					update = String.format(Locale.ENGLISH, "Satellite\n%d, %.1f", drone.getGps()
							.getSatCount(), drone.getGps().getGpsEPH());
				} else {
					update = String.format(Locale.ENGLISH, "Satellite\n%d, %s", drone.getGps()
							.getSatCount(), drone.getGps().getFixType());
				}

				((TextView) mItemView).setText(update);
			}
            else
                Log.d(INFOBAR5, " GEPEESSE ======= NULL");
		}
	}

	/**
	 * Flight time info bar item: displays the amount of time the drone is
	 * armed.
	 */
	public static class FlightTimeInfo extends InfoBarItemMulti {

		/**
		 * This is the period for the flight time update.
		 */
		protected final static long FLIGHT_TIMER_PERIOD = 1000l; // 1 second

		/**
		 * This is the layout resource id for the popup window.
		 */
		protected static final int sPopupWindowLayoutId = R.layout.popup_info_flight_time;

		/**
		 * This popup is used to offer the user the option to reset the flight
		 * time.
		 */
		protected PopupWindow mPopup;

		/**
		 * This handler is used to update the flight time value.
		 */
		protected Handler mHandler;

		/**
		 * Handle to the current drone state.
		 */
		protected Drone mDrone;

		/**
		 * Runnable used to update the drone flight time.
		 */
		protected Runnable mFlightTimeUpdater;

		public FlightTimeInfo(Context context, View parentView, Drone drone, int num_map) {
			super(context, parentView, drone, R.id.bar_propeller);
		}

		@Override
		protected void initItemView(final Context context, View parentView, final Drone drone) {
			super.initItemView(context, parentView, drone);
			if (mItemView == null)
				return;

			mHandler = new Handler();

			mFlightTimeUpdater = new Runnable() {
				@Override
				public void run() {
					mHandler.removeCallbacks(this);
					if (mDrone == null)
						return;

					if (mItemView != null) {
						long timeInSeconds = mDrone.getState().getFlightTime();
						long minutes = timeInSeconds / 60;
						long seconds = timeInSeconds % 60;

						((TextView) mItemView).setText(String.format("Air Time\n%02d:%02d",
								minutes, seconds));
					}

					mHandler.postDelayed(this, FLIGHT_TIMER_PERIOD);
				}
			};

			mPopup = initPopupWindow(context, sPopupWindowLayoutId);
			final View popupView = mPopup.getContentView();
			popupView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mDrone != null) {
						mDrone.getState().resetFlightTimer();
					}
					mPopup.dismiss();
				}
			});

			mItemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPopup == null)
						return;

					mPopup.showAsDropDown(mItemView);
				}
			});

			updateItemView(context, drone);
		}

		@Override
		public void updateItemView(final Context context, final Drone drone) {
            //Log.d(INFOBAR4, "ENTROU PARA ATUALIZAR!!!");
			mDrone = drone;
			if (mItemView == null)
				return;

			mHandler.removeCallbacks(mFlightTimeUpdater);
			if (drone != null) {
				mFlightTimeUpdater.run();
			} else {
				((TextView) mItemView).setText("--:--");
			}
		}
	}

	/**
	 * BatteryInfo info bar item: displays the drone remaining voltage, and
	 * ratio of remaining to full voltage.
	 */
	public static class BatteryInfo extends InfoBarItemMulti {

		/**
		 * This is the layout resource id for the popup window.
		 */
		protected static final int sPopupWindowLayoutId = R.layout.popup_info_power;
		
		
		/**
		 * This popup is used to show additional signal info.
		 */
		private PopupWindow mPopup;


		private TextView currentView;


		private TextView mAhView;
		
		public BatteryInfo(Context context, View parentView, Drone drone, int num_map) {
			super(context, parentView, drone, R.id.bar_battery);
		}

		@Override
		protected void initItemView(Context context, View parentView, Drone drone) {
			super.initItemView(context, parentView, drone);
			if (mItemView == null)
				return;

			mPopup = initPopupWindow(context, sPopupWindowLayoutId);

			final View popupView = mPopup.getContentView();
			currentView = (TextView) popupView.findViewById(R.id.bar_power_current);
			mAhView = (TextView) popupView.findViewById(R.id.bar_power_mAh);

			mItemView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (mPopup == null)
                        return;

                    mPopup.showAsDropDown(mItemView);
                }
            });

			updateItemView(context, drone);
		}

		@Override
		public void updateItemView(Context context, Drone drone) {

           // Log.d(INFOBAR4, "ENTROU PARA ATUALIZAR!!!");

			if (mItemView == null)
				return;

			String infoUpdate;
			if (drone == null) {
				infoUpdate = sDefaultValue;
				currentView.setText(sDefaultValue);
				mAhView.setText(sDefaultValue);
			} else {

				Double discharge = drone.getBattery().getBattDischarge();
				String dischargeText;
				if (discharge == null) {
					dischargeText = sDefaultValue;
				}else{
					dischargeText = String.format(Locale.ENGLISH, "%2.0f mAh", discharge);					
				}
				
				mAhView.setText(String.format(Locale.ENGLISH,"Remaining %2.0f%%", drone.getBattery().getBattRemain()));
				currentView.setText(String.format("Current %2.1f A", drone.getBattery().getBattCurrent()));
				
				infoUpdate = String.format(Locale.ENGLISH,"%2.1fv\n", drone.getBattery().getBattVolt());
				infoUpdate = infoUpdate.concat(dischargeText);
			}

			mPopup.update();
			((TextView) mItemView).setText(infoUpdate);
		}
	}

	/**
	 * Radio signal info bar item: displays the drone radio signal strength.
	 */
	public static class SignalInfo extends InfoBarItemMulti {

		/**
		 * This is the layout resource id for the popup window.
		 */
		protected static final int sPopupWindowLayoutId = R.layout.popup_info_signal;

		/**
		 * This popup is used to show additional signal info.
		 */
		protected PopupWindow mPopup;

		/*
		 * Radio signal sub views.
		 */
		private TextView mRssiView;
		private TextView mRemRssiView;
		private TextView mNoiseView;
		private TextView mRemNoiseView;
		private TextView mFadeView;
		private TextView mRemFadeView;

		public SignalInfo(Context context, View parentView, Drone drone, int num_map) {
			super(context, parentView, drone, R.id.bar_signal);
		}

		@Override
		protected void initItemView(Context context, View parentView, Drone drone) {
			super.initItemView(context, parentView, drone);
			if (mItemView == null)
				return;

			mPopup = initPopupWindow(context, sPopupWindowLayoutId);

			final View popupView = mPopup.getContentView();
			mRssiView = (TextView) popupView.findViewById(R.id.bar_signal_rssi);
			mRemRssiView = (TextView) popupView.findViewById(R.id.bar_signal_remrssi);
			mNoiseView = (TextView) popupView.findViewById(R.id.bar_signal_noise);
			mRemNoiseView = (TextView) popupView.findViewById(R.id.bar_signal_remnoise);
			mFadeView = (TextView) popupView.findViewById(R.id.bar_signal_fade);
			mRemFadeView = (TextView) popupView.findViewById(R.id.bar_signal_remfade);

			mItemView.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (mPopup == null)
						return;

					mPopup.showAsDropDown(mItemView);
				}
			});

			updateItemView(context, drone);
		}

		@Override
		public void updateItemView(Context context, final Drone drone) {
			if (mItemView == null)
				return;

			if (drone == null) {
				setDefaultValues();
			}else if (!drone.getRadio().isValid()){
				setDefaultValues();
			}else{
				setValuesFromRadio(drone);
			}

			mPopup.update();
		}

		private void setValuesFromRadio(final Drone drone) {
			((TextView) mItemView).setText(String.format(Locale.ENGLISH, "%d%%", drone.getRadio().getSignalStrength()));

			mRssiView.setText(String.format("RSSI %2.0f dB", drone.getRadio().getRssi()));
			mRemRssiView.setText(String.format("RemRSSI %2.0f dB", drone.getRadio()
					.getRemRssi()));
			mNoiseView.setText(String.format("Noise %2.0f dB", drone.getRadio().getNoise()));
			mRemNoiseView.setText(String.format("RemNoise %2.0f dB", drone.getRadio()
                    .getRemNoise()));
			mFadeView.setText(String.format("Fade %2.0f dB", drone.getRadio().getFadeMargin()));
			mRemFadeView.setText(String.format("RemFade %2.0f dB", drone.getRadio()
                    .getRemFadeMargin()));
		}

		private void setDefaultValues() {
			((TextView) mItemView).setText(sDefaultValue);
			mRssiView.setText(sDefaultValue);
			mRemRssiView.setText(sDefaultValue);
			mNoiseView.setText(sDefaultValue);
			mRemNoiseView.setText(sDefaultValue);
			mFadeView.setText(sDefaultValue);
			mRemFadeView.setText(sDefaultValue);
		}
	}

	/**
	 * Flight/APM modes info bar item: allows the user to select/view the drone
	 * flight mode.
	 */
	public static class FlightModesInfo extends InfoBarItemMulti {

		/**
		 * Stores the type of the current drone state.
		 */
		private int mLastDroneType = -1;

		/**
		 * This is the spinner modes adapter.
		 */
		private ModeAdapter mModeAdapter;

		/**
		 * Handle to the current drone state.
		 */
		private Drone mDrone;

		public FlightModesInfo(Context context, View parentView, Drone drone, int num_map) {
			super(context, parentView, drone, R.id.bar_flight_mode);
		}

		@Override
		protected void initItemView(final Context context, View parentView, final Drone drone) {
			super.initItemView(context, parentView, drone);
			if (mItemView == null)
				return;

			final SpinnerSelfSelect modesSpinner = (SpinnerSelfSelect) mItemView;

			mModeAdapter = new ModeAdapter(context, R.layout.spinner_drop_down);
			modesSpinner.setAdapter(mModeAdapter);

			modesSpinner.setOnSpinnerItemSelectedListener(new SpinnerSelfSelect.OnSpinnerItemSelectedListener() {
                @Override
                public void onSpinnerItemSelected(Spinner parent, int position) {
                    if (mDrone != null) {
                        final ApmModes newMode = (ApmModes) parent.getItemAtPosition(position);
                        mDrone.getState().changeFlightMode(newMode);

                        //Record the attempt to change flight modes
                        final HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder()
                                .setCategory(GAUtils.Category.FLIGHT)
                                .setAction("Flight mode changed")
                                .setLabel(newMode.getName());
                        GAUtils.sendEvent(eventBuilder);
                    }
                }
            });

			updateItemView(context, drone);
		}

		@Override
		public void updateItemView(final Context context, final Drone drone) {
			mDrone = drone;



			if (mItemView == null)
				return;

			final SpinnerSelfSelect modesSpinner = (SpinnerSelfSelect) mItemView;
			final int droneType = drone == null ? -1 : drone.getType();
			if (droneType != mLastDroneType) {
				final List<ApmModes> flightModes = droneType == -1 ? Collections
						.<ApmModes> emptyList() : ApmModes.getModeList(droneType);

				mModeAdapter.clear();
				mModeAdapter.addAll(flightModes);
				mModeAdapter.notifyDataSetChanged();

				mLastDroneType = droneType;
			}

			if (mDrone != null) {
                modesSpinner.forcedSetSelection(mModeAdapter.getPosition(mDrone.getState()
                        .getMode()));
            }
		}
	}
}