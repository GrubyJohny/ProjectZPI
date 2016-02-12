package zpi.squad.app.grouploc;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseInstallation;
import com.parse.ParseUser;

public class ChangePasswordFragment extends Fragment {
    private static View view;
    private Button confirm;
    final static String mapTAG = "MAP";
    final static String passwordTAG = "PASSWORD";
    private EditText currentPass, newPass, newPassConfirmed;

    String oldPass, pass, pass2;

    public ChangePasswordFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        if (view != null) {
            ViewGroup parent = (ViewGroup) view.getParent();
            if (parent != null)
                parent.removeView(view);
        }
        try {
            view = inflater.inflate(R.layout.fragment_passwords, container, false);
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
        currentPass = (EditText) getActivity().findViewById(R.id.passwordChangeCurrentPassword);
        newPass = (EditText) getActivity().findViewById(R.id.passwordChangeEdit);
        newPassConfirmed = (EditText) getActivity().findViewById(R.id.passwordChangeEdit2);


        confirm = (Button) getActivity().findViewById(R.id.confirmSettingsButton);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                oldPass = currentPass.getText().toString();
                pass = newPass.getText().toString();
                pass2 = newPassConfirmed.getText().toString();


                if(oldPass.length()==0)
                {
                    Toast.makeText(getActivity(), "Please enter current password", Toast.LENGTH_LONG).show();
                    currentPass.setText(""); newPass.setText(""); newPassConfirmed.setText("");

                }
                else if (pass.length() == 0 || pass2.length() == 0)
                {
                    Toast.makeText(getActivity(), "Please enter new password twice", Toast.LENGTH_LONG).show();
                    currentPass.setText(""); newPass.setText(""); newPassConfirmed.setText("");
                }
                else if( ! pass.equals(pass2))
                {
                    Toast.makeText(getActivity(), "New passwords are not identical", Toast.LENGTH_LONG).show();
                    currentPass.setText(""); newPass.setText(""); newPassConfirmed.setText("");
                }
                else if(pass.length()>0 & pass2.length()>0 & oldPass.length()>0 & pass.equals(pass2) ) {

                    /*odtąd fajnie byłoby dac jakieś kółko oznaczające oczekiwanie - zmiana hasła nie trwa długo
                    * ale jak ją przerwiesz, to trzeba resetować hasło, a normalny user może na to nie wpaść,
                    * więc lepiej zasugerowac mu, żeby grzecznie poczekał te 2 sekundy :) */
                    final ParseUser currentUser = ParseUser.getCurrentUser();
                    final String userName = ParseUser.getCurrentUser().getUsername();

                    ParseUser.logInInBackground(userName, oldPass, new LogInCallback() {
                        @Override
                        public void done(ParseUser user, ParseException e) {
                            if (user != null) {
                                currentUser.setPassword(pass);
                                try {
                                    currentUser.save();
                                } catch (ParseException e1) {
                                    e1.printStackTrace();
                                    e1.getLocalizedMessage();
                                }
                                ParseUser.logOut();
                                ParseUser.logInInBackground(userName, pass, new LogInCallback() {
                                    @Override
                                    public void done(ParseUser parseUser, ParseException e) {
                                        if (e == null) {
                                            currentPass.setText("");
                                            newPass.setText("");
                                            newPassConfirmed.setText("");
                                            Toast.makeText(getActivity(), "Password changed succesfully!", Toast.LENGTH_SHORT).show();

                                            if (getActivity().getSupportFragmentManager().getBackStackEntryCount() > 1) {
                                                getActivity().getSupportFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
                                                getActivity().getSupportFragmentManager().beginTransaction().replace(R.id.main_container, getActivity().getSupportFragmentManager().findFragmentByTag(mapTAG), mapTAG).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN).commit();
                                            } else {
                                                getActivity().getSupportFragmentManager().popBackStack();
                                            }

                                            InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);

                                            /*prawdopodobnie w tym miejscu trzeba zmienić zaznaczoną opcję
                                            * w drawerze po lewej (na mapę)*/

                                        } else
                                            Toast.makeText(getActivity(), "Network error", Toast.LENGTH_SHORT).show();
                                    }
                                });

                            } else {
                                currentPass.setText(""); newPass.setText(""); newPassConfirmed.setText("");
                                Toast.makeText(getActivity(), "Old password incorrect", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                    /*dotąd*/
                }

            }
        });
    }
}
