package zpi.squad.app.grouploc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.res.Resources;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class FriendsFragment extends ListFragment {

    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";
    private SQLiteHandler db;
    private static FriendList friendList;
    private static ArrayList<Friend> userFriendsList;
    private static Resources resources;
    public static FragmentActivity fragmentActivity;
    private static ListAdapter adapter;
    private String mParam1;
    private String mParam2;
    private MainActivity mainActivity = new MainActivity();
    private SessionManager session;
    private static List<ListViewItem> mItems;
    private ProgressDialog pDialog;


    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    public FriendsFragment() {
        // Required empty public constructor

    }

    public static void addFriend(Friend friend) {

        friendList.add(new ListViewItem(friend.getFriendID(), resources.getDrawable(R.drawable.image3), friend.getFriendName(), friend.getFriendEmail()));
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        db = new SQLiteHandler(LoginActivity.context);
        userFriendsList = db.getAllFriends();

        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mItems = new ArrayList<ListViewItem>();
        resources = getResources();
        fragmentActivity = getActivity();
        for(Friend f: userFriendsList){
            mItems.add(new ListViewItem(f.getFriendID(), resources.getDrawable(R.drawable.image3), f.getFriendName(), f.getFriendEmail()));
        }

        friendList = new FriendList(fragmentActivity, mItems);
        setListAdapter(friendList);

        session = new SessionManager(getActivity());
        pDialog = new ProgressDialog(getActivity());

    }


    public void sendFriendshipRequest(final String id, final String email) {

        String tag_string_req = "req_friendshipRequest";
        pDialog.setMessage("Sending Request for Friendship");
        showDialog();
        final String TAG = "Friendship request";
        if(!email.equals(session.getUserEmail())) {
            StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Friendship request Response: " + response.toString());
                    hideDialog();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

                        if (!error) {

                            Toast.makeText(getActivity().getApplicationContext(), "Successfully sent invitation", Toast.LENGTH_LONG).show();

                        } else {

                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getActivity().getApplicationContext(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getActivity().getApplicationContext(), "Exception - problem z połączeniem", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Friendship request Error: " + error.toString());

                    Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();

                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("tag", "friendshipRequest");
                    params.put("sender", id);
                    params.put("receiverEmail", email);

                    return params;
                }

            };

            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);
        }
        else{
            hideDialog();
            Toast.makeText(getActivity().getApplicationContext(), "You cannot send request to yourself", Toast.LENGTH_LONG).show();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
     /*   if(container == null) {
            return null;
        }*/
        View rootView = inflater.inflate(R.layout.fragment_friends, container, false);

        //W tej meodzie tworzymy tylko widok, uzupelnienie widoku w onViewCreated
        return rootView;
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        // remove the dividers from the ListView of the ListFragment
        getListView().setDividerHeight(2);


        /*listaElementow = new ArrayList<String>();
        listaElementow.add("Pajac");
        listaElementow.add("Pajac");
        listaElementow.add("Pajac");
        listaElementow.add("Pajac");
        listaElementow.add("idiota");
        listaElementow.add("idiota");


        //adapter = new ArrayAdapter<String>(getActivity().getApplicationContext(), R.layout.one_row, R.id.txt, listaElementow);
        getListView().setAdapter(adapter);
        //lista = (ListView) view.findViewById(R.id.listView);
        ((BaseAdapter) getListView().getAdapter()).notifyDataSetChanged();

        getListView().setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View viewClicked,int position, long id) {
                if(getActivity()!=null)
                    Toast.makeText(getActivity(), "Click on element list position = "+position, Toast.LENGTH_SHORT).show();
            }
        });*/
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        Button addFriendButton = (Button) getActivity().findViewById(R.id.addFriendButton);
        final EditText editText = (EditText) getActivity().findViewById(R.id.friendEmail);
        addFriendButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String friend = editText.getText().toString();
                sendFriendshipRequest(session.getUserId(), friend);
                editText.setText("");
            }
        });
    }





    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        ListViewItem item = mItems.get(position);
        Log.d("MOJLOG", session.getUserId());

        deleteFriendship(session.getUserId(), String.valueOf(item.uid));
        mItems.remove(position);
        friendList.notifyDataSetChanged();

    }




    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
       /* try {
            mListener = (OnFragmentInteractionListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnFragmentInteractionListener");
        }*/
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void setFriends(){
        Resources resources = getResources();
        for(Friend f: userFriendsList){
            mItems.add(new ListViewItem(f.getFriendID(), resources.getDrawable(R.drawable.image3), f.getFriendName(), f.getFriendEmail()));
        }

        setListAdapter(new FriendList(getActivity(), mItems));
        Log.d("FriendsFragment", "Wykonano");
    }


    private void deleteFriendship(final String uId, final String friendId) {

        String tag_string_req = "delete_friendshipRequest";
        pDialog.setMessage("Sending Request for deleting Friendship");
        showDialog();
        final String TAG = "Friendship delete request";

        StringRequest strReq = new StringRequest(Request.Method.POST, AppConfig.URL_REGISTER, new Response.Listener<String>() {

                @Override
                public void onResponse(String response) {
                    Log.d(TAG, "Friendship request Response: " + response.toString());
                    hideDialog();
                    try {
                        JSONObject jObj = new JSONObject(response);
                        boolean error = jObj.getBoolean("error");

                        if (!error) {

                            String message = jObj.getString("error_msg");
                            Toast.makeText(fragmentActivity, message, Toast.LENGTH_LONG).show();

                        } else {

                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(fragmentActivity, errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(fragmentActivity, "Exception - Connection problem", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Friendship request Error: " + error.toString());

                    Toast.makeText(fragmentActivity, error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();

                }
            }) {

                @Override
                protected Map<String, String> getParams() {
                    Map<String, String> params = new HashMap<String, String>();
                    params.put("tag", "deleteFriend");
                    params.put("senderId", uId);
                    params.put("receiverId", friendId);

                    return params;
                }

            };

            AppController.getInstance().addToRequestQueue(strReq, tag_string_req);

    }
    private void showDialog() {
        if (!pDialog.isShowing())
            pDialog.show();
    }

    private void hideDialog() {
        if (pDialog.isShowing())
            pDialog.dismiss();
    }




}
