package org.droidplanner.android.fragments;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.view.MenuInflater;
import android.view.Menu;

import android.support.v4.app.ListFragment;

import android.widget.ListView;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;

import org.droidplanner.R;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link AlgorithmMenuFragment.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link AlgorithmMenuFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class AlgorithmMenuFragment extends android.support.v4.app.Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    private Integer num_map;

    private OnFragmentInteractionListener mListener;

    String[] algorithms = { "Algorithm 1", "Algorithm 2" };


    // TODO: Rename and change types and number of parameters
    public static AlgorithmMenuFragment newInstance(int num_map) {
        AlgorithmMenuFragment fragment = new AlgorithmMenuFragment();

        Bundle args = new Bundle();
        args.putInt("num_map", num_map);
        fragment.setArguments(args);

        return fragment;
    }

    public AlgorithmMenuFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);


        if (getArguments() != null) {
            num_map = getArguments().getInt("num_map");
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment

        View view = inflater.inflate(R.layout.fragment_algorithm_menu, container, false);
        registerForContextMenu(view);

        this.setHasOptionsMenu(true);
        return view;
    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction_AlgorithmMenu(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p/>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        public void onFragmentInteraction_AlgorithmMenu(Uri uri);
    }

    public void createMenu()
    {
        registerForContextMenu(getView());
    }


}
