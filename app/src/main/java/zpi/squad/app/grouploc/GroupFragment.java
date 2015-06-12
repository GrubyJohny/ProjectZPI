package zpi.squad.app.grouploc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTabHost;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
    private static GroupAdapter groupAdapter;

    private static List<GroupList> groups;

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
                groups = new ArrayList<GroupList>();
                groupAdapter = new GroupAdapter(getActivity(), groups);
                getGroupsByName(getActivity(), groupAdapter, searchGroupText.getText().toString());
                showGroupSelectorDialog();

            }
        });
    }

    public static void getGroupsByName(final Activity activity, final GroupAdapter adapter, final String text) {
        final String TAG = "Requesting for list of groups";

        StringRequest request = new StringRequest(Request.Method.POST, AppConfig.URL_LOGIN, new Response.Listener<String>() {
            @Override
            public void onResponse(String response) {

                Log.d(TAG, response);

                try {
                    JSONObject jObj = new JSONObject(response);
                    if (!jObj.getBoolean("error")) {
                        JSONArray jsonGroups = jObj.getJSONArray("groups");
                        JSONObject gObj;
                        int gid;
                        String gname;
                        int adminId;
                        String adminName;
                        String created_at;

                        for(int i=0; i<jsonGroups.length(); i++){
                            gObj = jsonGroups.getJSONObject(i);
                            gid = gObj.getInt("gid");
                            gname = gObj.getString("gname");
                            adminId = gObj.getInt("adminid");
                            adminName = "Admin: " + gObj.getString("adminName");
                            created_at = "Utworzona: " + gObj.getString("created_at");
                            Log.e("group added. Id:", ""+(gid));
                            GroupList group = new GroupList(gid, gname, adminId, adminName, created_at);
                            groupAdapter.add(group);

                        }

                    } else {
                        Log.d(TAG, "Getting list of groups problem");
                    }
                } catch (JSONException e) {
                    e.printStackTrace();
                }


            }
        }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError volleyError) {

            }
        }) {
            @Override
            protected Map<String, String> getParams() throws AuthFailureError {
                HashMap<String, String> map = new HashMap<String, String>();
                map.put("tag", "listOfGroupsRequest");
                map.put("namePart", text);

                return map;
            }
        };
        AppController.getInstance().addToRequestQueue(request, "listOfGroupsRequest");
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

    public void showGroupSelectorDialog() {
        // Prepare the dialog by setting up a Builder.
        final String fDialogTitle = "Choose group";
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        TextView title = new TextView(getActivity().getApplicationContext());
// You Can Customise your Title here
        title.setText("Choose group");
        title.setBackgroundColor(Color.DKGRAY);
        title.setPadding(15, 15, 15, 15);
        title.setGravity(Gravity.CENTER);
        title.setTextColor(Color.WHITE);
        title.setTextSize(22);
        builder.setCustomTitle(title);
        //builder.setTitle(fDialogTitle);

        // Find the current map type to pre-check the item representing the current state.
        int checkItem = 0;

        // Add an OnClickListener to the dialog, so that the selection will be handled.
        builder.setSingleChoiceItems(
                groupAdapter,
                checkItem,
                new DialogInterface.OnClickListener() {

                    public void onClick(DialogInterface dialog, int position) {

                        GroupList item = groups.get(position);


                        Toast.makeText(getActivity().getApplicationContext(), "You chose group: " + item.getGroupName(), Toast.LENGTH_LONG).show();

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
