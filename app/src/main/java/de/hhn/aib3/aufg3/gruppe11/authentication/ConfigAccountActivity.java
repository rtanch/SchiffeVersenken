package de.hhn.aib3.aufg3.gruppe11.authentication;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AppCompatActivity;
import android.text.InputFilter;
import android.text.InputType;

import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import org.greenrobot.eventbus.Subscribe;

import java.net.HttpURLConnection;

import de.hhn.aib3.aufg3.gruppe11.R;
import de.hhn.aib3.aufg3.gruppe11.rest.Event;
import de.hhn.aib3.aufg3.gruppe11.rest.RestEvent;
import de.hhn.aib3.aufg3.gruppe11.rest.RestService;
import de.hhn.aib3.aufg3.gruppe11.utility.Utility;

/**
 * Configure client name, password and description
 * Changes are saved once client confirms entry
 */
public class ConfigAccountActivity extends AppCompatActivity {

    private static final String DEBUGLOG_TAG = "DEBUGLOG-CAA";
    private TextView configName;
    private static String PASSWORD_HASH;

    private EditText passwordView;
    private EditText passwordConfirmationView;
    private View focusView;

    private RestService restService;
    private Client client;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_config_account);

        Intent intent = getIntent();
        restService = (RestService) intent.getSerializableExtra(getString(R.string.extra_restService));
        client = (Client) intent.getSerializableExtra(getString(R.string.extra_client));

        configName();
        configPassword();
        saveChanges();

        Utility.refreshEventBus(this);
    }

    @Subscribe
    public void onEvent(RestEvent restEvent) {
        Event event = restEvent.getEvent();
        int responseCode = restEvent.getResponseCode();

        switch (event) {

            case DELETE:
                if (restEvent.isWsConnected()) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, R.string.account_delete, Toast.LENGTH_SHORT).show();
                        client = null;
                        Utility.backToLogin(this);
                        finish();
                    } else if (restEvent.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        Toast.makeText(this, R.string.jwt_token_expired, Toast.LENGTH_LONG).show();
                        Utility.backToLogin(this);
                    } else {
                        Toast.makeText(this, R.string.failed_delete_account, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                    Utility.backToLogin(this);
                }
                break;

            case UPDATE:
                if (restEvent.isWsConnected()) {
                    if (responseCode == HttpURLConnection.HTTP_OK) {
                        Toast.makeText(this, R.string.account_information_updated, Toast.LENGTH_SHORT).show();
                        finish();
                    } else if (restEvent.getResponseCode() == HttpURLConnection.HTTP_FORBIDDEN) {
                        Toast.makeText(this, R.string.jwt_token_expired, Toast.LENGTH_LONG).show();
                        Utility.backToLogin(this);
                    } else {
                        Toast.makeText(this, R.string.failed_update_account, Toast.LENGTH_LONG).show();
                    }
                } else {
                    Toast.makeText(this, R.string.connection_failed, Toast.LENGTH_LONG).show();
                    Utility.backToLogin(this);
                }
                break;
        }
        Utility.refreshEventBus(this);
    }

    /**
     * Client name config.
     * Opens Dialog for new user name entry
     */
    private void configName() {
        configName = (TextView) findViewById(R.id.textView_config_name);
        configName.setText(client.getUsername());
        configName.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                AlertDialog.Builder builder = new AlertDialog.Builder(ConfigAccountActivity.this);
                builder.setTitle(R.string.dialog_choose_name);

                final EditText input = new EditText(ConfigAccountActivity.this);
                input.setInputType(InputType.TYPE_CLASS_TEXT);
                input.setFilters(new InputFilter[]{new InputFilter.LengthFilter(15)});

                builder.setView(input);

                builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                        if (input.getText().toString().isEmpty()) {
                            if (client != null) {
                                configName.setText(client.getUsername());
                            }

                        } else {
                            configName.setText(input.getText().toString());
                        }
                    }
                });

                builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();


            }
        });


    }


    /**
     * Client password config.
     * Dialog for current password confirmation
     * Dialog for new password entry with confirmation
     */
    private void configPassword() {
        TextView configPassword = (TextView) findViewById(R.id.textView_config_password);

        configPassword.setText(R.string.mask_password);
        configPassword.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {


                final AlertDialog.Builder builder = new AlertDialog.Builder(ConfigAccountActivity.this);
                builder.setTitle(R.string.current_password);


                final EditText passwordConfirmationInput = new EditText(ConfigAccountActivity.this);
                passwordConfirmationInput.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);


                builder.setView(passwordConfirmationInput);
                builder.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        try {
                            if (!Utility.hash(passwordConfirmationInput.getText().toString()).equals(client.getPassword())) {
                                Toast.makeText(ConfigAccountActivity.this, R.string.false_password, Toast.LENGTH_SHORT).show();
                                Log.d(DEBUGLOG_TAG, "password hash false");
                            } else {
                                Toast.makeText(ConfigAccountActivity.this, R.string.right_password, Toast.LENGTH_SHORT).show();
                                Log.d(DEBUGLOG_TAG, "password hash true");

                                TextInputLayout.LayoutParams inputParams = new TextInputLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);


                                TextInputLayout layoutTeIn1 = new TextInputLayout(ConfigAccountActivity.this);
                                passwordView = new EditText(ConfigAccountActivity.this);
                                passwordView.setHint(R.string.new_password);
                                passwordView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                layoutTeIn1.addView(passwordView, inputParams);

                                TextInputLayout layoutTeIn2 = new TextInputLayout(ConfigAccountActivity.this);
                                passwordConfirmationView = new EditText(ConfigAccountActivity.this);
                                passwordConfirmationView.setHint(R.string.new_password_confirm);
                                passwordConfirmationView.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_PASSWORD);
                                layoutTeIn2.addView(passwordConfirmationView, inputParams);

                                LinearLayout linearLayout = new LinearLayout(ConfigAccountActivity.this);
                                linearLayout.setOrientation(LinearLayout.VERTICAL);
                                linearLayout.setPadding(0, 100, 0, 0);
                                linearLayout.addView(layoutTeIn1);
                                linearLayout.addView(layoutTeIn2);

                                AlertDialog.Builder builder2 = new AlertDialog.Builder(ConfigAccountActivity.this);
                                builder2.setTitle(R.string.new_password);
                                builder2.setView(linearLayout);


                                builder2.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                    }
                                });

                                builder2.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        dialog.cancel();
                                    }
                                });

                                final AlertDialog newPwDialog = builder2.create();

                                newPwDialog.setOnShowListener(new DialogInterface.OnShowListener() {
                                    @Override
                                    public void onShow(DialogInterface dialog) {
                                        Button button = newPwDialog.getButton(DialogInterface.BUTTON_POSITIVE);
                                        button.setOnClickListener(new View.OnClickListener() {
                                            @Override
                                            public void onClick(View v) {
                                                if (isPasswordValid(passwordView.getText().toString(), passwordConfirmationView.getText().toString())) {
                                                    PASSWORD_HASH = Utility.hash(passwordView.getText().toString());
                                                    newPwDialog.cancel();
                                                } else {
                                                    focusView.requestFocus();
                                                }
                                            }
                                        });
                                    }
                                });

                                newPwDialog.show();
                            }
                        } catch (NullPointerException e) {
                            Log.d(DEBUGLOG_TAG, "Utility.hash() may return null " + e.getMessage());
                        }
                    }
                });

                builder.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });

                builder.show();
            }
        });

    }

    /**
     * Entry confirmation
     * Saves new password or user-name values
     */
    private void saveChanges() {
        FloatingActionButton saveConfig = (FloatingActionButton) findViewById(R.id.fab_save_config);
        saveConfig.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (client != null) {
                    if (!configName.getText().toString().isEmpty()) {
                        client.setUsername(configName.getText().toString());
                    }
                    if (PASSWORD_HASH != null) {
                        client.setPassword(PASSWORD_HASH);
                    }
                } else {
                    Log.d(DEBUGLOG_TAG, "Client null");
                }

                if (restService != null && client != null) {
                    restService.updateClientData(client);
                }
            }
        });

        FloatingActionButton deleteAccountButton = (FloatingActionButton) findViewById(R.id.fab_delete_account);
        deleteAccountButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(ConfigAccountActivity.this);
                alertDialog.setTitle(R.string.delete_account);
                alertDialog.setPositiveButton(R.string.ok_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
//                        restService.deleteClientAccount(client);
                        restService.deleteClientAccount(client.getId());
                    }
                });
                alertDialog.setNegativeButton(R.string.cancel_button, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                alertDialog.show();
            }
        });
    }


    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final String REGEXP_NUMBERS = ".*\\d+.*";
    private static final String REGEXP_ALPHANUMERICS = ".*\\W+.*";

    /**
     * Validates password strength LOCALLY - sets Error messages accordingly
     * Criteria: (1) length >= 8 (2) contains number/s (3) contains symbol/s (4) pws match
     *
     * @param password plain text password
     * @return true: Criteria met
     * false: Criteria not met
     */
    private boolean isPasswordValid(String password, String passwordConfirmation) {

        if (password.length() >= MIN_PASSWORD_LENGTH) {
            if (password.matches(REGEXP_NUMBERS)) {
                if (password.matches(REGEXP_ALPHANUMERICS)) {
                    if (password.equals(passwordConfirmation)) {
                        Log.d(DEBUGLOG_TAG, "PASSWORD VALID");
                        return true;
                    } else {
                        Log.d(DEBUGLOG_TAG, "PASSWORDS DIFFER");
                        passwordConfirmationView.setError(getString(R.string.error_invalid_password_match));
                        focusView = passwordConfirmationView;
                        return false;
                    }
                } else {
                    Log.d(DEBUGLOG_TAG, "PASSWORD DOES NOT CONTAIN SYMBOLS");
                    passwordView.setError(getString(R.string.error_invalid_password_symbol));
                    focusView = passwordView;
                    return false;
                }
            } else {
                Log.d(DEBUGLOG_TAG, "PASSWORD DOES NOT CONTAIN NUMBERS");
                passwordView.setError(getString(R.string.error_invalid_password_number));
                focusView = passwordView;
                return false;
            }
        } else {
            Log.d(DEBUGLOG_TAG, "PASSWORD LENGTH TOO SHORT");
            passwordView.setError(getString(R.string.error_invalid_password_length));
            focusView = passwordView;
            return false;
        }
    }
}