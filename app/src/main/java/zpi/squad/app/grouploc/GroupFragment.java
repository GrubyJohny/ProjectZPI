package zpi.squad.app.grouploc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;

import com.google.android.gms.maps.GoogleMap;

import java.util.ArrayList;
import java.util.List;

public class GroupFragment extends Fragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private OnFragmentInteractionListener mListener;

    private Button BackToMap;
    private Button searchGroupButton;
    private Button searchButton;
    private EditText searchGroupText;
    private View layoutSettings;
    private Button BackToMapButton;
    private FragmentTabHost tabhost;
    GroupAdapter groupAdapter;

    private List<GroupList> groups;

    //private static final String[] Groups =
      //      {"Road Map", "Hybrid", "Satellite", "Terrain"};

    public static GroupFragment newInstance(String param1, String param2) {
        GroupFragment fragment = new GroupFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public GroupFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }


    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_group_nogroup, container, false);
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        searchGroupButton = (Button) view.findViewById(R.id.searchGroupButton);
        searchGroupText = (EditText) view.findViewById(R.id.searchingGroupText);
        searchButton = (Button) view.findViewById(R.id.searchButton);



        searchGroupButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                searchGroupText.setVisibility(View.VISIBLE);
                searchButton.setVisibility(View.VISIBLE);
            }
        });



        searchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMapTypeSelectorDialog();
            }
        });
    }

    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        layoutSettings = (View) getActivity().findViewById(R.id.settingsLayout);
        tabhost = (FragmentTabHost) getActivity().findViewById(android.R.id.tabhost);

        BackToMapButton = (Button) getView().findViewById(R.id.BacktoMapButton);

        BackToMapButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tabhost.setCurrentTab(0);
                layoutSettings.setVisibility(View.INVISIBLE);
            }
        });

        groups = new ArrayList<GroupList>();
        groups.add(new GroupList(1, "Jakaś", 1, "Jakiś", "25.05.2015"));
        groups.add(new GroupList(2, "GroupLoc", 2, "Inny", "26.05.2015"));
        groups.add(new GroupList(3, "Grill", 3, "Jeszcze inny", "27.05.2015"));
        groups.add(new GroupList(1, "Jakaś", 1, "Jakiś", "25.05.2015"));
        groups.add(new GroupList(2, "GroupLoc", 2, "Inny", "26.05.2015"));
        groups.add(new GroupList(3, "Grill", 3, "Jeszcze inny", "27.05.2015"));
        groups.add(new GroupList(1, "Jakaś", 1, "Jakiś", "25.05.2015"));
        groups.add(new GroupList(2, "GroupLoc", 2, "Inny", "26.05.2015"));
        groups.add(new GroupList(3, "Grill", 3, "Jeszcze inny", "27.05.2015"));
        groups.add(new GroupList(1, "Jakaś", 1, "Jakiś", "25.05.2015"));
        groups.add(new GroupList(2, "GroupLoc", 2, "Inny", "26.05.2015"));
        groups.add(new GroupList(3, "Grill", 3, "Jeszcze inny", "27.05.2015"));
        groups.add(new GroupList(1, "Jakaś", 1, "Jakiś", "25.05.2015"));
        groups.add(new GroupList(2, "GroupLoc", 2, "Inny", "26.05.2015"));
        groups.add(new GroupList(3, "Grill", 3, "Jeszcze inny", "27.05.2015"));
        groupAdapter = new GroupAdapter(getActivity().getApplicationContext(), groups);

    }

    // TODO: Rename method, update argument and hook method into UI event
    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        /*try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
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

    private void showMapTypeSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Choose group";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = 0;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                groupAdapter,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int item) {
                        // Locally create a finalised object.

                        // Perform an action depending on which item was selected.
                        switch (item) {
                            case 1:
                                break;
                            case 2:
                                break;
                            case 3:
                                break;
                            default:
                        }
                        dialog.dismiss();
                    }
                }
        );

        // Build the dialog and show it.
        AlertDialog fMapTypeDialog = builder.create();
        fMapTypeDialog.setCanceledOnTouchOutside(true);
        fMapTypeDialog.show();
    }

}
