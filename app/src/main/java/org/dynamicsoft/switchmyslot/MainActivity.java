/********************************************************************************************
 org/dynamicsoft/switchmyslot/MainActivity.java: MainActivity for Switch My Slot Android App

 Copyright (C) 2020 Shouko

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

package org.dynamicsoft.switchmyslot;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    TextView halInfoTV, numberOfSlotsTV, currentSlotTV, CurrentSlotSuffixTV;
    int currentSlot;
    String convertedSlotNumberToAlphabet = null, path;
    Button button;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        halInfoTV = findViewById(R.id.halInfoTV);
        numberOfSlotsTV = findViewById(R.id.numberOfSlotsTV);
        currentSlotTV = findViewById(R.id.currentSlotTV);
        CurrentSlotSuffixTV = findViewById(R.id.CurrentSlotSuffixTV);
        button = findViewById(R.id.button);

        try {
            Process phoneSupportedOrNotProcess;
            phoneSupportedOrNotProcess = Runtime.getRuntime().exec("getprop | grep suffix");
            DataInputStream phoneSupportedOrNotProcessOUT = new DataInputStream(phoneSupportedOrNotProcess.getInputStream());
            if (phoneSupportedOrNotProcessOUT.equals("")){
                Toast.makeText(this, getString(R.string.error_ab_device), Toast.LENGTH_LONG).show(); //This is not an A/B device!
            } else {
                Process halInfoProcess;
                halInfoProcess = Runtime.getRuntime().exec("su -c bootctl hal-info");
                DataInputStream halinfoOUT = new DataInputStream(halInfoProcess.getInputStream());
                halInfoTV.setText(halinfoOUT.readLine());

                Process numberOfSlotsProcess;
                numberOfSlotsProcess = Runtime.getRuntime().exec("su -c bootctl get-number-slots");
                DataInputStream numberOfSlotsProcessOUT = new DataInputStream(numberOfSlotsProcess.getInputStream());
                numberOfSlotsTV.setText(getString(R.string.number_of_slots) + " " + numberOfSlotsProcessOUT.readLine()); //Number of slots:

                Process currentSlotProcess;
                currentSlotProcess = Runtime.getRuntime().exec("su -c bootctl get-current-slot");
                DataInputStream currentSlotProcessOUT = new DataInputStream(currentSlotProcess.getInputStream());

                currentSlot = Integer.parseInt(currentSlotProcessOUT.readLine());
                if (currentSlot == 0) {
                    convertedSlotNumberToAlphabet = "A";
                    button.setText(getString(R.string.switch_slot_to) + " B"); //"Switch Slot to B"
                }

                if (currentSlot == 1) {
                    convertedSlotNumberToAlphabet = "B";
                    button.setText(getString(R.string.switch_slot_to) + " A"); //"Switch Slot to A"
                }

                currentSlotTV.setText(getString(R.string.current_slot)+ " " + convertedSlotNumberToAlphabet); //"Current slot: "

                Process CurrentSlotSuffixProcess;
                CurrentSlotSuffixProcess = Runtime.getRuntime().exec("su -c bootctl get-suffix " + currentSlot);
                DataInputStream CurrentSlotSuffixProcessOUT = new DataInputStream(CurrentSlotSuffixProcess.getInputStream());
                CurrentSlotSuffixTV.setText(getString(R.string.current_slot_suffix) + " " + CurrentSlotSuffixProcessOUT.readLine()); //"Current slot suffix: "
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void openRepo(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/shoukolate/Switch-My-Slot-Android"));
        startActivity(browserIntent);
    }

    public void switchSlot(View view) {
        //Toast.makeText(this, getString(R.string.error_ab_device), Toast.LENGTH_LONG).show(); //This is not an A/B device!

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(android.R.string.dialog_alert_title);
        builder.setMessage(getString(R.string.dialog_confirmation));

        builder.setPositiveButton(getString(android.R.string.ok), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    if (currentSlot == 0) {
                        Runtime.getRuntime().exec("su -c bootctl set-active-boot-slot 1");
                    } if (currentSlot == 1) {
                        Runtime.getRuntime().exec("su -c bootctl set-active-boot-slot 0");
                    }
                    Runtime.getRuntime().exec("su -c svc power reboot || reboot");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
        builder.setNegativeButton(getString(android.R.string.no), null);
        AlertDialog dialog = builder.create();
        dialog.show();
    }
}
