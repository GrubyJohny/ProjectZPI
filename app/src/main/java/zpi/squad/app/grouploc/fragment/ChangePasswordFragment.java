package zpi.squad.app.grouploc.fragment;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.os.Bundle;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.InflateException;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

import zpi.squad.app.grouploc.R;

public class ChangePasswordFragment extends Fragment {
    private static View view;
    private Button confirm;
    final static String mapTAG = "MAP";
    final static String passwordTAG = "PASSWORD";
    private EditText currentPass, newPass, newPassConfirmed;

    String oldPass, pass, pass2;
    private TextInputLayout inputLayoutOldPassword, inputLayoutNewPassword, inputLayoutNewPasswordAgain;
    private boolean positiveValidate;
    private ProgressDialog progress;

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

        inputLayoutOldPassword = (TextInputLayout) getActivity().findViewById(R.id.input_layout_passwords_old);
        inputLayoutNewPassword = (TextInputLayout) getActivity().findViewById(R.id.input_layout_passwords_new1);
        inputLayoutNewPasswordAgain = (TextInputLayout) getActivity().findViewById(R.id.input_layout_passwords_new2);

        currentPass.addTextChangedListener(new MyTextWatcher(currentPass));
        newPass.addTextChangedListener(new MyTextWatcher(newPass));
        newPassConfirmed.addTextChangedListener(new MyTextWatcher(newPassConfirmed));

        confirm = (Button) getActivity().findViewById(R.id.confirmSettingsButton);

        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                oldPass = currentPass.getText().toString();
                pass = newPass.getText().toString();
                pass2 = newPassConfirmed.getText().toString();

                submitForm();
                if (positiveValidate) {
                    progress = ProgressDialog.show(getActivity(), getString(R.string.pleaseWait),
                            "Changing password request", true);

                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            // do the thing that takes a long time

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
                                                    Toast.makeText(getActivity().getApplicationContext(), "Password changed succesfully!", Toast.LENGTH_SHORT).show();

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
                                                    Toast.makeText(getActivity().getApplicationContext(), "Network error", Toast.LENGTH_SHORT).show();
                                            }
                                        });

                                    } else {
                                        InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
                                        imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
                                        Toast.makeText(getActivity().getApplicationContext(), "Old password incorrect", Toast.LENGTH_SHORT).show();
                                    }
                                    getActivity().runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progress.dismiss();
                                        }
                                    });
                                }
                            });


                        }
                    }).start();
                }
            }
        });
    }

    private void submitForm() {
        positiveValidate = false;

        if (!validateCurrentPassword()) {
            return;
        } else if (!validateNewPassword()) {
            return;
        } else if (!validateNewConfirmedPassword()) {
            return;
        } else if (!validateSamePasswords()) {
            return;
        } else
            positiveValidate = true;
    }

    private boolean validateSamePasswords() {
        if (!newPass.getText().toString().equals(newPassConfirmed.getText().toString())) {
            inputLayoutNewPasswordAgain.setError(getString(R.string.passwordValidSame));
            requestFocus(inputLayoutNewPasswordAgain);
            return false;
        } else {
            inputLayoutNewPasswordAgain.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateCurrentPassword() {

        if (currentPass.getText().toString().trim().isEmpty()) {
            inputLayoutOldPassword.setError(getString(R.string.passwordValidEmpty));
            requestFocus(inputLayoutOldPassword);
            return false;
        } else {
            inputLayoutOldPassword.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateNewPassword() {
        int minimumLength = 6;

        if (newPass.getText().toString().trim().isEmpty()) {
            inputLayoutNewPassword.setError(getString(R.string.passwordValidEmpty));
            requestFocus(newPass);
            return false;
        } else if (newPass.getText().toString().trim().length() < minimumLength) {
            inputLayoutNewPassword.setError(getString(R.string.passwordValidMoreThan));
            requestFocus(newPass);
            return false;
        } else {
            inputLayoutNewPassword.setErrorEnabled(false);
        }

        return true;
    }

    private boolean validateNewConfirmedPassword() {
        int minimumLength = 6;

        if (newPassConfirmed.getText().toString().trim().isEmpty()) {
            inputLayoutNewPasswordAgain.setError(getString(R.string.passwordValidEmpty));
            requestFocus(newPassConfirmed);
            return false;
        } else if (newPassConfirmed.getText().toString().trim().length() < minimumLength) {
            inputLayoutNewPasswordAgain.setError(getString(R.string.passwordValidMoreThan));
            requestFocus(newPassConfirmed);
            return false;
        } else {
            inputLayoutNewPasswordAgain.setErrorEnabled(false);
        }

        return true;
    }

    private void requestFocus(View view) {
        if (view.requestFocus()) {
            getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
        }
    }

    private class MyTextWatcher implements TextWatcher {

        private View view;

        private MyTextWatcher(View view) {
            this.view = view;
        }

        public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        }

        public void afterTextChanged(Editable editable) {
            switch (view.getId()) {
                case R.id.passwordChangeCurrentPassword:
                    validateCurrentPassword();
                    break;
                case R.id.passwordChangeEdit:
                    validateNewPassword();
                    break;
                case R.id.passwordChangeEdit2:
                    validateNewConfirmedPassword();
                    break;
            }
        }
    }
}
