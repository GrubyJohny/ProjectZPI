package zpi.squad.app.grouploc.fragments;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;

import zpi.squad.app.grouploc.R;
import zpi.squad.app.grouploc.SessionManager;
import zpi.squad.app.grouploc.helpers.CommonMethods;

public class ChangePhotoFragment extends Fragment {
    private static View view;
    private Button changeImgFromGallery;
    private Button changeImgFromCamera;
    private Button changeImgFromFacebook;
    private Button changeImgFromAdjust;
    public static final int PICK_FROM_CAMERA = 1;
    public static final int PICK_FROM_GALLERY = 2;
    public static final int CROP_IMAGE = 3;
    private SessionManager session;
    private Bitmap profileImageFromFacebook;
    private CommonMethods commonMethods = new CommonMethods();

    public ChangePhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        session = SessionManager.getInstance(getActivity());
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_photo_change, container, false);
        } catch (InflateException e) {

        }
        return view;
    }

    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        settingButtons();
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    public void settingButtons() {
        changeImgFromGallery = (Button) getActivity().findViewById(R.id.changeImgFromGalleryButton);
        changeImgFromGallery.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent();

                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 500);
                intent.putExtra("outputY", 500);

                try {
                    intent.putExtra("return-data", true);
                    getActivity().startActivityForResult(Intent.createChooser(intent,
                            "Complete action using"), PICK_FROM_GALLERY);

                } catch (ActivityNotFoundException e) {

                }
            }
        });

        changeImgFromCamera = (Button) getActivity().findViewById(R.id.changeImgFromCameraButton);
        changeImgFromCamera.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);

                intent.putExtra(MediaStore.EXTRA_OUTPUT,
                        MediaStore.Images.Media.EXTERNAL_CONTENT_URI.toString());
                intent.putExtra("crop", "true");
                intent.putExtra("aspectX", 1);
                intent.putExtra("aspectY", 1);
                intent.putExtra("outputX", 500);
                intent.putExtra("outputY", 500);

                try {

                    intent.putExtra("return-data", true);
                    getActivity().startActivityForResult(intent, PICK_FROM_CAMERA);

                } catch (ActivityNotFoundException e) {

                }
            }

        });

        changeImgFromFacebook = (Button) getActivity().findViewById(R.id.buttonImageFromFacebook);
        if(!session.isLoggedByFacebook()){
            changeImgFromFacebook.setVisibility(View.GONE);
        }
        changeImgFromFacebook.setOnClickListener(new View.OnClickListener() {
                                                     @Override
                                                     public void onClick(View v) {

                                                         if (session.isLoggedByFacebook())
                                                         {
                                                             Bitmap photo = null;
                                                             try {
                                                                 photo = getFacebookProfilePicture(AccessToken.getCurrentAccessToken());

                                                                 Uri uri = getImageUri(getActivity().getApplicationContext(), photo);

                                                                 Intent cropIntent = new Intent("com.android.camera.action.CROP");
                                                                 cropIntent.setDataAndType(uri, "image/*");
                                                                 cropIntent.putExtra("crop", "true");
                                                                 cropIntent.putExtra("aspectX", 1);
                                                                 cropIntent.putExtra("aspectY", 1);
                                                                 cropIntent.putExtra("outputX", 500);
                                                                 cropIntent.putExtra("outputY", 500);
                                                                 cropIntent.putExtra("return-data", true);

                                                                 getActivity().startActivityForResult(cropIntent, CROP_IMAGE);

                                                             } catch (IOException e) {
                                                                 e.printStackTrace();
                                                             }


                                                         }

                                                     }
                                                 }
        );

        changeImgFromAdjust = (Button) getActivity().findViewById(R.id.buttonImageFromAdjust);
        changeImgFromAdjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap photo = commonMethods.decodeBase64ToBitmap(session.getUserPhoto());
                Uri uri = getImageUri(getActivity().getApplicationContext(), photo);

                Intent cropIntent = new Intent("com.android.camera.action.CROP");
                cropIntent.setDataAndType(uri, "image/*");
                cropIntent.putExtra("crop", "true");
                cropIntent.putExtra("aspectX", 1);
                cropIntent.putExtra("aspectY", 1);
                cropIntent.putExtra("outputX", 500);
                cropIntent.putExtra("outputY", 500);
                cropIntent.putExtra("return-data", true);

                getActivity().startActivityForResult(cropIntent, CROP_IMAGE);
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    private Bitmap getFacebookProfilePicture(AccessToken accessToken) throws IOException {


        Bundle params = new Bundle();
        params.putBoolean("redirect", false);
        params.putInt("height", 100);
        params.putInt("width", 100);
        GraphResponse srequest = new GraphRequest(
                AccessToken.getCurrentAccessToken(),
                "/" + accessToken.getUserId() + "/picture",
                params,
                HttpMethod.GET,
                new GraphRequest.Callback() {
                    public void onCompleted(GraphResponse response) {
                        //String ara = response.getJSONObject().toString();
                        //Log.e("PROFILOWE: ", ara);
                        try {
                            JSONObject araa = response.getJSONObject();
                            JSONObject aray = araa.getJSONObject("data");

                            URL facebookProfilePictureUrl = new URL(aray.getString("url"));
                            profileImageFromFacebook = BitmapFactory.decodeStream(facebookProfilePictureUrl.openConnection().getInputStream());
                        } catch (JSONException e) {
                            e.printStackTrace();
                        } catch (MalformedURLException e) {
                            e.printStackTrace();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }

                    }
                }
        ).executeAndWait();

        return profileImageFromFacebook;
    }
}
