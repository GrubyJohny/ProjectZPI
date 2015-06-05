package zpi.squad.app.grouploc;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPClient;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;
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
    private SessionManager session;
    private static List<ListViewItem> mItems;
    private ProgressDialog pDialog;
    private Context context;


    public FriendsFragment() {
        // Required empty public constructor

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
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        resources = getActivity().getResources();
        db = new SQLiteHandler(LoginActivity.context);
        session = new SessionManager(getActivity());
        pDialog = new ProgressDialog(getActivity());

        userFriendsList = db.getAllFriends();
        mItems = new ArrayList<ListViewItem>();


        for(Friend f: userFriendsList){
            Drawable ico = getFriendPhoto(f.getFriendID());
            Bitmap bitmap = ((BitmapDrawable) ico).getBitmap();
// Scale it to 50 x 50
            Drawable d = new BitmapDrawable(getResources(), Bitmap.createScaledBitmap(bitmap, 50, 50, true));
// Set your new, scaled drawable "d"
            mItems.add(new ListViewItem(f.getFriendID(), d==null?resources.getDrawable(R.drawable.image3):d, f.getFriendName(), f.getFriendEmail()));

        }


        friendList = new FriendList(getActivity(), mItems);
        setListAdapter(friendList);



    }

    public static void addFriend(Friend friend) {

        mItems.add(new ListViewItem(friend.getFriendID(), resources.getDrawable(R.drawable.image3), friend.getFriendName(), friend.getFriendEmail()));
        friendList.notifyDataSetChanged();
        //friendList.add(new ListViewItem(friend.getFriendID(), resources.getDrawable(R.drawable.image3), friend.getFriendName(), friend.getFriendEmail()));
    }


    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {

        ListViewItem item = mItems.get(position);
        Log.d("MOJLOG", ""+item.uid);

        deleteFriendship(session.getUserId(), String.valueOf(item.uid), position);
        Log.d("MOJLOG", String.valueOf(item.uid));

    }


    public static void removeItem(String senderEmail){

        Log.e("Liczba znajomych", String.valueOf(mItems.size()));
        for(ListViewItem item: mItems){
            if(item.email.equals(senderEmail)){
                mItems.remove(item);
            }
        }
        Log.e("Liczba znajomych", String.valueOf(mItems.size()));
        friendList.notifyDataSetChanged();
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


    private void deleteFriendship(final String uId, final String friendId, final int position) {

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

                            String message = jObj.getString("msg");
                            Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
                            mItems.remove(position);
                            friendList.notifyDataSetChanged();

                        } else {

                            String errorMsg = jObj.getString("error_msg");
                            Toast.makeText(getActivity(), errorMsg, Toast.LENGTH_LONG).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Didn't erased because of connection problem", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Friendship request Error: " + error.toString());

                    Toast.makeText(getActivity(), error.getMessage(), Toast.LENGTH_LONG).show();
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


    }



    public static FriendsFragment newInstance(String param1, String param2) {
        FriendsFragment fragment = new FriendsFragment();

        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
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


    private Drawable getFriendPhoto(int friendID)
    {

        //download photo from ftp
        Bitmap icon = null;
        FTPClient con = null;

        try
        {
            con = new FTPClient();
            con.connect("ftp.marcinta.webd.pl");

            if (con.login("grouploc@marcinta.webd.pl", "grouploc2015"))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                OutputStream out = new FileOutputStream(new File("/storage/emulated/0/"+friendID+".png"));
                boolean result = con.retrieveFile(friendID+".png", out);
                out.close();
                if (result) Log.v("moj download", "succeeded");
                con.logout();
                con.disconnect();


            }


        }
        catch (Exception e)
        {
            Log.v("download result","failed");
            e.printStackTrace();
        }
/*
            File filePath = new File("/storage/emulated/0/"+f.getFriendID()+".png");
            FileInputStream fi = null;
            try {
                fi = new FileInputStream(filePath);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }

            icon = BitmapFactory.decodeStream(fi);
*/
        Log.d("PRZED DRAWABLE", "OK");
        Drawable ico = Drawable.createFromPath("/storage/emulated/0/"+friendID+".png");


        Log.d("PRAWDA", "" + (ico != null));

        return ico==null?resources.getDrawable(R.drawable.image3):ico;


    }

}
