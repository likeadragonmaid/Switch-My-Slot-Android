/********************************************************************************************
 org/shou/switchmyslot/MainActivity.java: MainActivity for Switch My Slot Android App

 Copyright (C) 2010 - 2021 Shou

 MIT License

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package org.shou.switchmyslot;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.stericson.RootShell.exceptions.RootDeniedException;
import com.stericson.RootShell.execution.Command;
import com.stericson.RootShell.execution.Shell;
import com.stericson.RootTools.RootTools;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

public class MainActivity extends AppCompatActivity {

    // UI
    TextView halInfoTV, numberOfSlotsTV, currentSlotTV, currentSlotSuffixTV;
    AlertDialog errorDialog, confirmationDialog;
    Button button;

    // Shell integration
    Command halInfoCommand, numberOfSlotsCommand, currentSlotCommand, currentSlotSuffixCommand;
    boolean informationGathered;
    Shell shell;

    // Information to preserve during re-creation of activity (except from convertedSlotAlphabet)
    String halInfo, numberOfSlots, currentSlotSuffix, convertedSlotAlphabet, errorDialogString;
    boolean confirmationDialogShown, errorDialogShown;
    int currentSlot;
    MainActivityViewModel model;


    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Getting views in the activity
        halInfoTV = findViewById(R.id.halInfoTV);
        numberOfSlotsTV = findViewById(R.id.numberOfSlotsTV);
        currentSlotTV = findViewById(R.id.currentSlotTV);
        currentSlotSuffixTV = findViewById(R.id.CurrentSlotSuffixTV);
        button = findViewById(R.id.button);

        model = new ViewModelProvider(this).get(MainActivityViewModel.class);  // for preserving the root shell through re-creations of the activity

        // if the activity is being restored (device rotation, multi-window mode, re-opening the app after it got killed in the background by the system)
        if (savedInstanceState != null) {

            // Restoring information from the saved state of the activity

            errorDialogShown = savedInstanceState.getBoolean(Constants.STATE_ERROR_DIALOG_SHOWN);

            if (errorDialogShown) {  // if the error dialog was visible before the activity's re-creation then show it again
                errorDialogString = savedInstanceState.getString(Constants.STATE_ERROR_DIALOG_STRING);
                displayErrorAndExit(errorDialogString);
            } else {  // if not then keep restoring information
                halInfo = savedInstanceState.getString(Constants.STATE_HAL_INFO);
                numberOfSlots = savedInstanceState.getString(Constants.STATE_NUMBER_OF_SLOTS);
                currentSlot = savedInstanceState.getInt(Constants.STATE_CURRENT_SLOT);
                currentSlotSuffix = savedInstanceState.getString(Constants.STATE_CURRENT_SLOT_SUFFIX);
                confirmationDialogShown = savedInstanceState.getBoolean(Constants.STATE_CONFIRMATION_DIALOG_SHOWN);

                shell = model.getShell();  // getting the shell that was preserved by the ViewModel
                if (shell == null) {  // if it is a re-creation of the activity after the app got killed in the background by the system. i.e., the shell was terminated
                    shell = getRootShell();
                    model.setShell(shell);
                }

                if (confirmationDialogShown) {  // if the confirmation dialog was visible before the activity's re-creation then show it again by calling the onClick listener of the button that switches the slot
                    switchSlot(button);
                }
            }
        } else {  // if it is the first creation of the activity

            if (checkDeviceSupport()) {  // if the device is supported

                // Creating commands
                halInfoCommand = new Command(Constants.HAL_INFO_COMMAND_ID, false, "bootctl hal-info") {
                    @Override
                    public void commandOutput(int id, String line) {
                        halInfo = line;
                        super.commandOutput(id, line);  // MUST be in the end of the method - not in the start
                    }
                };

                numberOfSlotsCommand = new Command(Constants.NUMBER_OF_SLOTS_COMMAND_ID, false, "bootctl get-number-slots") {
                    @Override
                    public void commandOutput(int id, String line) {
                        numberOfSlots = line;
                        super.commandOutput(id, line);
                    }
                };

                currentSlotCommand = new Command(Constants.CURRENT_SLOT_COMMAND_ID, false, "bootctl get-current-slot") {
                    @Override
                    public void commandOutput(int id, String line) {
                        currentSlot = Integer.parseInt(line);

                        // Creating get-suffix command with current slot
                        currentSlotSuffixCommand = new Command(Constants.CURRENT_SLOT_SUFFIX_COMMAND_ID, false, "bootctl get-suffix " + currentSlot) {
                            @Override
                            public void commandOutput(int id, String line) {
                                currentSlotSuffix = line;
                                informationGathered = true;  // Releases main thread to assign the information
                                super.commandOutput(id, line);
                            }
                        };

                        super.commandOutput(id, line);
                    }
                };

                try {
                    shell = getRootShell();
                    model.setShell(shell);

                    // Executing commands
                    informationGathered = false;
                    shell.add(halInfoCommand);
                    shell.add(numberOfSlotsCommand);
                    shell.add(currentSlotCommand);
                    while (currentSlotSuffixCommand == null); // waiting for command creation with the current slot
                    shell.add(currentSlotSuffixCommand);
                    while (!informationGathered); // waiting for all the information from the commands, i.e., waiting for the last command to handle its output
                } catch (IOException e) {
                    e.printStackTrace();
                    displayErrorAndExit(e.getMessage());
                }
            }
        }

        if (!errorDialogShown) {  // Assigning information to the Views if there isn't an error dialog
            halInfoTV.setText(halInfo);
            numberOfSlotsTV.setText(getString(R.string.number_of_slots) + " " + numberOfSlots);
            currentSlotSuffixTV.setText(getString(R.string.current_slot_suffix) + " " + currentSlotSuffix);

            if (currentSlot == 0) {
                convertedSlotAlphabet = "A";
                button.setText(getString(R.string.switch_slot_to) + " B"); //"Switch Slot to B"
            } else if (currentSlot == 1) {
                convertedSlotAlphabet = "B";
                button.setText(getString(R.string.switch_slot_to) + " A"); //"Switch Slot to A"
            }
            currentSlotTV.setText(getString(R.string.current_slot) + " " + convertedSlotAlphabet);
        }
    }


    /**
     * Saving information like dialogs and bootctl commands' output for activity re-creation
     *
     * @param outState The bundle for saving the information
     */
    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        // Saving information from bootctl utility (so no need to run the commands once again when the activity is being restored) and dialogs
        outState.putBoolean(Constants.STATE_ERROR_DIALOG_SHOWN, errorDialogShown);  // if the error dialog is visible
        if (errorDialogShown) {
            outState.putString(Constants.STATE_ERROR_DIALOG_STRING, errorDialogString);
        } else {
            outState.putString(Constants.STATE_HAL_INFO, halInfo);
            outState.putString(Constants.STATE_NUMBER_OF_SLOTS, numberOfSlots);
            outState.putInt(Constants.STATE_CURRENT_SLOT, currentSlot);
            outState.putString(Constants.STATE_CURRENT_SLOT_SUFFIX, currentSlotSuffix);
            outState.putBoolean(Constants.STATE_CONFIRMATION_DIALOG_SHOWN, confirmationDialogShown);  // if the confirmation dialog is visible
        }
    }


    /**
     * Finishing activity, closing the shell and exiting JVM (otherwise there's a bug when re-opening the app)
     *
     */
    @Override
    public void onBackPressed() {
        exitApp();
        model.onCleared();
        System.exit(0);  // Completely terminates the process. Fixes the blank screen when opening the app after back button pressed.
    }


    /**
     * Dismissing dialogs when activity is destroyed
     *
     */
    @Override
    protected void onDestroy() {
        super.onDestroy();

        // if a dialog is visible, dismissing it without the listener (to show it again if the activity is going to be re-created)
        if (errorDialogShown) {
            errorDialog.setOnDismissListener(null);
            errorDialog.dismiss();
        } else if (confirmationDialogShown) {
            confirmationDialog.setOnDismissListener(null);
            confirmationDialog.dismiss();
        }
    }


    /**
     * Returns a root shell with proper exceptions handling
     *
     * @return The root shell
     */
    public Shell getRootShell() {
        Shell rootShell = null;

        try {
            rootShell = RootTools.getShell(true);  // trying to get a new root shell
        } catch (RootDeniedException e) {
            e.printStackTrace();
            displayErrorAndExit(getString(R.string.error_root_denied));
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
            displayErrorAndExit(e.getMessage());
        }

        return rootShell;
    }


    /**
     * Checks if the device is supported. The requirements are:
     *  - Android version 7.1 or newer
     *  - A/B partitions (conventional or virtual)
     *  - SU availability
     *  - SU granted
     *  - Availability of bootctl utility
     *
     * If the device isn't supported then the app shows an error dialog and exits.
     *
     * @return True if the device is supported, else false.
     */
    public boolean checkDeviceSupport() {

        boolean supported = false;
        String unsupportedReason = "";

        if (Integer.parseInt(Build.VERSION.SDK) < Build.VERSION_CODES.N_MR1) {  // Seamless A/B updates are only from Android Nougat 7.1
            unsupportedReason = getString(R.string.error_min_api);
        } else if (ABChecker.check() == null) {  // if the device don't support the conventional or virtual A/B partitions
            unsupportedReason = getString(R.string.error_ab_device);
        } else if (!RootTools.isRootAvailable()) {  // if su binary is not available
            unsupportedReason = getString(R.string.error_root_required);
        } else if (!RootTools.isAccessGiven()) {  // if user denied the su request
            unsupportedReason = getString(R.string.error_root_denied);
        } else if (!RootTools.checkUtil("bootctl")) {  // checking bootctl availability
            unsupportedReason = getString(R.string.error_bootctl_missing);
        } else {
            supported = true;
        }

        if (supported) {
            Log.d("Switch My Slot", "Device supported! This is an A/B device with Android version 7.1 or newer and bootctl utility is available.");
        } else {
            Log.e("Switch My Slot", "Error: Device unsupported. " + unsupportedReason);
            displayErrorAndExit(unsupportedReason);
        }

        return supported;
    }


    /**
     * Opens the github repo.
     *
     * @param view The TextView that got clicked
     */
    public void openRepo(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/gibcheesepuffs/Switch-My-Slot-Android"));
        startActivity(browserIntent);
    }


    /**
     * Shows confirmation dialog and switches the active slot using bootctl utility.
     * Then reboots the device using the power manager. If it fails then executes a force reboot.
     *
     * @param view The button that got clicked.
     */
    public void switchSlot(View view) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(android.R.string.dialog_alert_title);
        builder.setMessage(getString(R.string.dialog_confirmation));

        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                String setActiveSlotCommandText = "";
                Command setActiveSlotCommand, rebootCommand;

                if (currentSlot == 0) {
                    setActiveSlotCommandText = "bootctl set-active-boot-slot 1";
                } else if (currentSlot == 1) {
                    setActiveSlotCommandText = "bootctl set-active-boot-slot 0";
                }

                // Creating commands
                setActiveSlotCommand = new Command(4, false, setActiveSlotCommandText);
                rebootCommand = new Command(5, false, "svc power reboot || reboot");

                try {
                    
                    // Executing commands
                    shell.add(setActiveSlotCommand);
                    shell.add(rebootCommand);
                    shell.close();

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(getString(android.R.string.no), null);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                confirmationDialogShown = false;
            }
        });

        confirmationDialog = builder.create();
        confirmationDialog.show();
        confirmationDialogShown = true;
    }


    /**
     * Displays an error dialog that when dismissed, it calls the app to exit.
     *
     * @param error The error message to display.
     */
    public void displayErrorAndExit(String error) {

        AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
        builder.setTitle(getString(R.string.dialog_error_title));
        builder.setMessage(error);
        errorDialogString = error;  // Saving error for activity re-creation (if happens)
        builder.setPositiveButton(getString(android.R.string.ok), null);

        builder.setOnDismissListener(new DialogInterface.OnDismissListener() {  // Closing app on dismiss
            @Override
            public void onDismiss(DialogInterface dialog) {
                errorDialogShown = false;
                exitApp();
            }
        });

        errorDialog = builder.create();
        errorDialog.show();
        errorDialogShown = true;

    }


    /**
     * Exits the app by finishing the activity and then calling the System.exit function.
     * If the root shell is open then closing it.
     *
     */
    public void exitApp() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            finishAffinity();
        } else {
            finish();
        }
    }
}
