package zpi.squad.app.grouploc;

import android.app.Activity;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.parse.ParseException;
import com.parse.ParseFacebookUtils;
import com.parse.ParseUser;
import com.parse.SaveCallback;

import java.io.ByteArrayOutputStream;

public class ChangePhotoFragment extends Fragment {
    private static View view;
    private Button changeImgFromGallery;
    private Button changeImgFromCamera;
    private Button changeImgFromFacebook;
    private Button changeImgFromAdjust;
    private static final int PICK_FROM_CAMERA = 1;
    private static final int PICK_FROM_GALLERY = 2;
    private static final int CROP_IMAGE = 3;
    Bitmap profilePictureRaw;
    private SessionManager session;

    public ChangePhotoFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        session = SessionManager.getInstance(getActivity().getApplicationContext());
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
                    startActivityForResult(Intent.createChooser(intent,
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
                    startActivityForResult(intent, PICK_FROM_CAMERA);

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

                                                         Bitmap toCrop = null;
                                                         String previouslyEncodedImage = "";
                                                         /*if (shre.getString("facebook_image_data", "") != "") {
                                                             previouslyEncodedImage = shre.getString("facebook_image_data", "");
                                                             toCrop = decodeBase64ToBitmap(previouslyEncodedImage);
                                                         }*/

                                                         Uri uri = getImageUri(getActivity().getApplicationContext(), toCrop);
                                                         Intent cropIntent = new Intent("com.android.camera.action.CROP");

                                                         cropIntent.setDataAndType(uri, "image/*");
                                                         cropIntent.putExtra("crop", "true");
                                                         cropIntent.putExtra("aspectX", 1);
                                                         cropIntent.putExtra("aspectY", 1);
                                                         cropIntent.putExtra("outputX", 500);
                                                         cropIntent.putExtra("outputY", 500);
                                                         cropIntent.putExtra("return-data", true);

                                                         startActivityForResult(cropIntent, CROP_IMAGE);
                                                     }
                                                 }
        );

        changeImgFromAdjust = (Button) getActivity().findViewById(R.id.buttonImageFromAdjust);
        changeImgFromAdjust.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Bitmap toCrop = profilePictureRaw;

                Uri uri = getImageUri(getActivity().getApplicationContext(), toCrop);
                Intent cropIntent = new Intent("com.android.camera.action.CROP");

                cropIntent.setDataAndType(uri, "image/*");
                cropIntent.putExtra("crop", "true");
                cropIntent.putExtra("aspectX", 1);
                cropIntent.putExtra("aspectY", 1);
                cropIntent.putExtra("outputX", 500);
                cropIntent.putExtra("outputY", 500);
                cropIntent.putExtra("return-data", true);

                startActivityForResult(cropIntent, CROP_IMAGE);
            }
        });
    }

    public Uri getImageUri(Context inContext, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.PNG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }
}
