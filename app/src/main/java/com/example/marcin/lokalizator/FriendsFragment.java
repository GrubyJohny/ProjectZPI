package com.example.marcin.lokalizator;

import android.app.Activity;

import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.*;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;


public class FriendsFragment extends ListFragment {
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    /*String[] web = {
            "Ferrari",
            "Vento",
            "Inny"
    };
    Integer[] imageId = {
            R.drawable.image1,
            R.drawable.image2,
            R.drawable.image3
    };*/
    private List<ListViewItem> mItems;

    private ArrayList<String> listaElementow;
    //private ArrayAdapter<String> adapter;

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment FriendsFragment.
     */
    // TODO: Rename and change types and number of parameters
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

        mItems = new ArrayList<ListViewItem>();
        Resources resources = getResources();

        mItems.add(new ListViewItem(resources.getDrawable(R.drawable.image1), "Adam Grzech"));
        mItems.add(new ListViewItem(resources.getDrawable(R.drawable.image2), "Zygmunt Bazur"));
        mItems.add(new ListViewItem(resources.getDrawable(R.drawable.image3), "Zdzisek Spławski"));

        setListAdapter(new FriendList(getActivity(), mItems));
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
    public void onListItemClick(ListView l, View v, int position, long id) {

        ListViewItem item = mItems.get(position);
        Toast.makeText(getActivity(), item.name, Toast.LENGTH_SHORT).show();
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


}
