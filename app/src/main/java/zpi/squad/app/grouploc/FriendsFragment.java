package zpi.squad.app.grouploc;

import android.app.ActionBar;
import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.*;
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
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
    PopupWindow pw;
    Button dismiss;
    Button showFriendOnMap;
    Button deleteFriend;
    ListViewItem item;


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
                InputMethodManager imm = (InputMethodManager)getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(getView().getWindowToken(), 0);
                if(friend.equals("")){
                    Toast.makeText(getActivity().getApplicationContext(), "Please enter email address", Toast.LENGTH_SHORT).show();
                }
                else {
                    sendFriendshipRequest(session.getUserId(), friend);
                    editText.setText("");
                }
            }
        });

/*        LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        pw = new PopupWindow(getActivity());
        // The code below assumes that the root container has an id called 'main'*/



        if(mItems.size() < 3 ) {
            mItems.add(new ListViewItem("150", resources.getDrawable(R.drawable.johny), "Nowy znajomy", "znajomy@o2.pl"));
            mItems.add(new ListViewItem("151", resources.getDrawable(R.drawable.johny), "Stary znajomy", "stary_znajomy@o2.pl"));
            mItems.add(new ListViewItem("152", resources.getDrawable(R.drawable.johny), "Adam Małysz", "małysz@gmail.pl"));
        }
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        context = getActivity().getApplicationContext();
        resources = getActivity().getResources();
        db = new SQLiteHandler(LoginActivity.context);
        session = SessionManager.getInstance(context);
        pDialog = new ProgressDialog(getActivity());


        pDialog.setMessage("Please wait...");
        pDialog.show();
        //PhotoDecodeRunnable pr = new PhotoDecodeRunnable();
        //pr.run();



    }

    public static void addFriend(Friend friend) {

        mItems.add(new ListViewItem(friend.getFriendID(), resources.getDrawable(R.drawable.image3), friend.getFriendName(), friend.getFriendEmail()));
        friendList.notifyDataSetChanged();
        //friendList.add(new ListViewItem(friend.getFriendID(), resources.getDrawable(R.drawable.image3), friend.getFriendName(), friend.getFriendEmail()));
    }

    private PopupWindow pwindo;

    private void initiatePopupWindow() {
        try {
            LayoutInflater inflater = (LayoutInflater) getActivity().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View layout = inflater.inflate(R.layout.popup,
                    (ViewGroup) getActivity().findViewById(R.id.popup_element));
            pwindo = new PopupWindow(layout, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
            pwindo.showAtLocation(layout, Gravity.CENTER, 0, 0);

            dismiss = (Button) layout.findViewById(R.id.dismiss);
            dismiss.setOnClickListener(dismiss_click_listener);

            showFriendOnMap = (Button) layout.findViewById(R.id.showFriendOnMap);
            showFriendOnMap.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    pwindo.dismiss();
                }
            });

            deleteFriend = (Button) layout.findViewById(R.id.deleteFriend);


        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private View.OnClickListener dismiss_click_listener = new View.OnClickListener() {
        public void onClick(View v) {
            pwindo.dismiss();

        }
    };


    @Override
    public void onListItemClick(ListView l, View v, final int position, long id) {

        item = mItems.get(position);
        Log.d("MOJLOG", "" + item.uid);


        //pw.showAtLocation(getActivity().findViewById(R.id.friendsFragment), Gravity.CENTER, 0, 0);

        initiatePopupWindow();

        deleteFriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteFriendship(session.getUserId(), String.valueOf(item.uid), position);
                pwindo.dismiss();
            }
        });

        //deleteFriendship(session.getUserId(), String.valueOf(item.uid), position);
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
                        Toast.makeText(getActivity().getApplicationContext(), "Exception - connection issue", Toast.LENGTH_LONG).show();
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Friendship request Error: " + error.toString());

                    Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_LONG).show();
                    hideDialog();
                    //zmiana do commita

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
                            Toast.makeText(getActivity().getApplicationContext(), errorMsg, Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        Toast.makeText(getActivity(), "Didn't erased because of connection problem", Toast.LENGTH_SHORT).show();
                        e.printStackTrace();
                    }

                }
            }, new Response.ErrorListener() {

                @Override
                public void onErrorResponse(VolleyError error) {
                    Log.e(TAG, "Friendship request Error: " + error.toString());

                    Toast.makeText(getActivity().getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
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


    private Drawable getImageFromFTP(int userID) //może zwracać null - uwaga dla Szczurka
    {

        Bitmap icon = null;
        FTPClient con = null;
        Drawable phot =null;
        try
        {
            con = new FTPClient();

            con.connect("ftp.marcinta.webd.pl");
            if (con.login("grouploc@marcinta.webd.pl", "grouploc2015"))
            {
                con.enterLocalPassiveMode(); // important!
                con.setFileType(FTP.BINARY_FILE_TYPE);

                phot = Drawable.createFromStream(con.retrieveFileStream(userID+".png"), "userID");

                con.logout();
                con.disconnect();
            }


        }
        catch (Exception e)
        {
            Log.e("download result", "failed");
            e.printStackTrace();
        }

        return phot;
    }

    public static Bitmap drawableToBitmap (Drawable drawable) {
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }




}
