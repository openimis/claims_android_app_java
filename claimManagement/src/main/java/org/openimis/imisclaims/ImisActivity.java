package org.openimis.imisclaims;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;

public abstract class ImisActivity extends AppCompatActivity {
    private BroadcastReceiver broadcastReceiver;
    private final ArrayList<String> emptyBroadcastList = new ArrayList<>();

    protected ProgressDialog progressDialog;
    protected ActionBar actionBar;
    protected Global global;
    protected SQLHandler sqlHandler;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        global = (Global) getApplicationContext();
        global.setLanguage(this, global.getSavedLanguage());

        actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        broadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                onBroadcastReceived(context, intent);
            }
        };

        sqlHandler = new SQLHandler(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        sqlHandler.closeDatabases();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();

        IntentFilter intentFilter = new IntentFilter();

        ArrayList<String> intentList = getBroadcastList();
        for (String intent : intentList) {
            intentFilter.addAction(intent);
        }

        registerReceiver(broadcastReceiver, intentFilter);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }

    /**
     * Override to handle registered broadcasts
     * works together with getBroadcastList
     */
    protected void onBroadcastReceived(Context context, Intent intent) {

    }

    /**
     * Override to register for broadcast intents
     * works together with onBroadcastReceived
     *
     * @return List of actions to listen
     */
    protected ArrayList<String> getBroadcastList() {
        return emptyBroadcastList;
    }

    protected void refresh() {
        finish();
        startActivity(getIntent());
    }

    protected Context getContext() {
        return this;
    }

    protected AlertDialog showSelectDialog(String title, CharSequence[] itemList, DialogInterface.OnClickListener itemSelectedCallback, DialogInterface.OnClickListener cancelCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setTitle(title)
                .setCancelable(false)
                .setItems(itemList, itemSelectedCallback);

        if (cancelCallback != null) {
            builder.setNegativeButton(R.string.Cancel, cancelCallback);
        } else {
            builder.setNegativeButton(R.string.Cancel, ((dialog, which) -> dialog.cancel()));
        }

        return builder.show();
    }

    protected AlertDialog showDialog(String msg, DialogInterface.OnClickListener okCallback, DialogInterface.OnClickListener cancelCallback) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this)
                .setMessage(msg)
                .setCancelable(false);

        if (okCallback != null) {
            builder.setPositiveButton(R.string.Ok, okCallback);
        } else {
            builder.setPositiveButton(R.string.Ok, ((dialog, which) -> dialog.cancel()));
        }

        if (cancelCallback != null) {
            builder.setNegativeButton(R.string.Cancel, cancelCallback);
        }

        return builder.show();
    }

    protected AlertDialog showDialog(String msg, DialogInterface.OnClickListener okCallback) {
        return showDialog(msg, okCallback, null);
    }

    protected AlertDialog showDialog(String msg) {
        return showDialog(msg, null, null);
    }

    private AlertDialog showLoginDialogBox(Runnable onLoggedIn, Runnable onCancel) {
        LayoutInflater li = LayoutInflater.from(this);
        View promptsView = li.inflate(R.layout.login_dialog, null);

        AlertDialog.Builder alertDialogBuilder = new AlertDialog.Builder(this);
        alertDialogBuilder.setView(promptsView);

        final TextView username = promptsView.findViewById(R.id.UserName);
        final TextView password = promptsView.findViewById(R.id.Password);


        String officer_code = ((Global) getApplicationContext()).getOfficerCode();
        username.setText(officer_code != null ? officer_code : "");

        // set dialog message
        alertDialogBuilder
                .setCancelable(false)
                .setPositiveButton(R.string.Ok,
                        (dialog, id) -> {
                            if (!(username.getText().length() == 0) && !(password.getText().length() == 0)) {
                                progressDialog = ProgressDialog.show(this, getResources().getString(R.string.Login), getResources().getString(R.string.InProgress));

                                runOnNewThread(
                                        () -> new Login().LoginToken(username.getText().toString(), password.getText().toString()),
                                        () -> {
                                            progressDialog.dismiss();
                                            if (global.isLoggedIn()) {
                                                runOnUiThread(() -> {
                                                    showToast(R.string.Login_Successful);
                                                    onLoggedIn.run();
                                                });
                                            } else {
                                                runOnUiThread(() -> {
                                                    showToast(R.string.LoginFail);
                                                    showLoginDialogBox(onLoggedIn, onCancel);
                                                });
                                            }
                                        },
                                        500);
                            } else {
                                showToast(R.string.Enter_Credentials);
                                dialog.dismiss();
                                showLoginDialogBox(onLoggedIn, onCancel);
                            }
                        })
                .setNegativeButton(R.string.Cancel,
                        (dialog, id) -> {
                            dialog.dismiss();
                            onCancel.run();
                        });

        return alertDialogBuilder.show();
    }


    /**
     * Execute the task if internet is available and the user is logged in.
     * If there is no network or the the user cancels login the task is canceled.
     *
     * @param task     Task to do when the user is logged in.
     * @param onCancel Task to do when the initial task is canceled by the user or there is no internet.
     */
    protected void doLoggedIn(Runnable task, Runnable onCancel) {
        doInOnlineMode(
                () -> {
                    if (global.isLoggedIn()) {
                        task.run();
                    } else {
                        showLoginDialogBox(task, onCancel);
                    }
                },
                () -> {
                    showToast(R.string.InternetRequired);
                    onCancel.run();
                }
        );
    }

    /**
     * Execute the task if internet is available and the user is logged in.
     * If there is no network or the the user cancels login the task is canceled.
     *
     * @param task Task to do when the user is logged in.
     */
    protected void doLoggedIn(Runnable task) {
        doLoggedIn(task, () -> {
        });
    }

    /**
     * Execute the task if internet is available
     *
     * @param task         Task to do in online mode
     * @param onNoInternet Task to do, if there is no internet
     */
    protected void doInOnlineMode(Runnable task, Runnable onNoInternet) {
        if (global.isNetworkAvailable()) {
            task.run();
        } else {
            onNoInternet.run();
        }
    }

    /**
     * Execute the task if internet is available
     *
     * @param task Task to do in online mode
     */
    protected void doInOnlineMode(Runnable task) {
        doInOnlineMode(task, () -> {
        });
    }

    protected void showToast(String message) {
        runOnUiThread(() -> Toast.makeText(this, message, Toast.LENGTH_LONG).show());
    }

    protected void showToast(int resourceId) {
        showToast(getResources().getString(resourceId));
    }

    /**
     * @param task           Task to run on a new thread
     * @param onTaskFinished Task to run after the initial task was finished
     * @param taskMinLength  Minimum amount of time between start of a task and start of onTaskFinished.
     *                       This can be used to prevent fast flashing of ui elements modified by the task.
     */
    protected void runOnNewThread(Runnable task, Runnable onTaskFinished, long taskMinLength) {
        new Thread(() -> {
            long start = System.currentTimeMillis();
            task.run();
            long length = System.currentTimeMillis() - start;
            if (taskMinLength > 0) {
                try { //This prevents fast flashing
                    Thread.sleep(length >= taskMinLength ? 0 : taskMinLength - length);
                } catch (Exception e) {
                    //Nothing to do
                }
            }
            onTaskFinished.run();
        }).start();
    }

    /**
     * @param task           Task to run on a new thread
     * @param onTaskFinished Task to run after the initial task was finished
     */
    protected void runOnNewThread(Runnable task, Runnable onTaskFinished) {
        runOnNewThread(task, onTaskFinished, 0);
    }


    /**
     * @param task Task to run on a new thread
     */
    protected void runOnNewThread(Runnable task) {
        runOnNewThread(task, () -> {
        }, 0);
    }
}
