package com.example.marcin.lokalizator;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;


/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link MapActivity.OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link MapActivity#newInstance} factory method to
 * create an instance of this fragment.
 */
public class MapActivity extends SupportMapFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER


    // TODO: Rename and change types of parameters


    private GoogleMap googleMap;
    double wroclawLatitude = 51.11;
    double wroclawLongitude = 17.03 ;
    MarkerOptions marker = new MarkerOptions().position(new LatLng(wroclawLatitude, wroclawLongitude)).title("Dżomborno!");
    CameraPosition wroclawCameraPosition = new CameraPosition.Builder().target(
            new LatLng(wroclawLatitude, wroclawLongitude)).zoom(12).build();

    private OnFragmentInteractionListener mListener;


    public static MapActivity newInstance(String param1, String param2) {
        MapActivity fragment = new MapActivity();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    public MapActivity() {

    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        try {

            initializeMap();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_map, container, false);
    }


    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
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
        public void onFragmentInteraction(Uri uri);
    }

    private void initializeMap() {
        if (googleMap == null) {
            MapsInitializer.initialize(getActivity());
            googleMap = ((MapActivity) getFragmentManager().findFragmentById(
                    R.id.MapActivity)).getMap();
            //googleMap.addMarker(marker);
            //googleMap.animateCamera(CameraUpdateFactory.newCameraPosition(wroclawCameraPosition));
            //googleMap.setMyLocationEnabled(true);
            //googleMap.getUiSettings().setMyLocationButtonEnabled(true);

            if (googleMap == null) {
                Toast.makeText(getActivity(),
                        "Sorry! unable to create maps", Toast.LENGTH_SHORT)
                        .show();
            }
        }
    }

}
