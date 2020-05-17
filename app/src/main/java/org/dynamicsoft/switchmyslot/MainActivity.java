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
import android.content.Context;
import android.content.Intent;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class MainActivity extends AppCompatActivity {

    TextView halInfoTV, numberOfSlotsTV, currentSlotTV, CurrentSlotSuffixTV;
    int currentSlot;
    String convertedSlotNumberToAlphabet = null, path;
    Switch switcher;

    @SuppressLint("SetTextI18n")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        halInfoTV = findViewById(R.id.halInfoTV);
        numberOfSlotsTV = findViewById(R.id.numberOfSlotsTV);
        currentSlotTV = findViewById(R.id.currentSlotTV);
        CurrentSlotSuffixTV = findViewById(R.id.CurrentSlotSuffixTV);
        switcher = findViewById(R.id.switcher);

        Context context = getApplicationContext();
        path = "/data/data/org.dynamicsoft.switchmyslot/files/";
        File file = new File(path);
        if (!file.exists()) {
            file.mkdir();
            copyFile(context);
        }

        try {
            Runtime.getRuntime().exec("chmod +x /data/data/org.dynamicsoft.switchmyslot/files/bootctl");
            Process halInfoProcess;
            halInfoProcess = Runtime.getRuntime().exec("su -c /data/data/org.dynamicsoft.switchmyslot/files/bootctl hal-info");
            DataInputStream halinfoOUT = new DataInputStream(halInfoProcess.getInputStream());
            halInfoTV.setText(halinfoOUT.readLine());

            Process numberOfSlotsProcess;
            numberOfSlotsProcess = Runtime.getRuntime().exec("su -c /data/data/org.dynamicsoft.switchmyslot/files/bootctl get-number-slots");
            DataInputStream numberOfSlotsProcessOUT = new DataInputStream(numberOfSlotsProcess.getInputStream());
            numberOfSlotsTV.setText("Number of slots: " + numberOfSlotsProcessOUT.readLine());

            Process currentSlotProcess;
            currentSlotProcess = Runtime.getRuntime().exec("su -c /data/data/org.dynamicsoft.switchmyslot/files/bootctl get-current-slot");
            DataInputStream currentSlotProcessOUT = new DataInputStream(currentSlotProcess.getInputStream());

            currentSlot = Integer.parseInt(currentSlotProcessOUT.readLine());
            if (currentSlot == 0) {
                convertedSlotNumberToAlphabet = "A";
                switcher.setChecked(false);
            }

            if (currentSlot == 1) {
                convertedSlotNumberToAlphabet = "B";
                switcher.setChecked(true);
            }

            currentSlotTV.setText("Current slot: " + convertedSlotNumberToAlphabet);

            Process CurrentSlotSuffixProcess;
            CurrentSlotSuffixProcess = Runtime.getRuntime().exec("su -c /data/data/org.dynamicsoft.switchmyslot/files/bootctl get-suffix " + currentSlot);
            DataInputStream CurrentSlotSuffixProcessOUT = new DataInputStream(CurrentSlotSuffixProcess.getInputStream());
            CurrentSlotSuffixTV.setText("Current slot suffix: " + CurrentSlotSuffixProcessOUT.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }

        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    if (isChecked) {
                        Runtime.getRuntime().exec("su -c /data/data/org.dynamicsoft.switchmyslot/files/bootctl set-active-boot-slot b");
                        Toast.makeText(MainActivity.this, "Switching to Slot B", Toast.LENGTH_SHORT).show();
                    } else {
                        Runtime.getRuntime().exec("su -c /data/data/org.dynamicsoft.switchmyslot/files/bootctl set-active-boot-slot a");
                        Toast.makeText(MainActivity.this, "Switching to Slot A", Toast.LENGTH_SHORT).show();
                    }
                    Runtime.getRuntime().exec("su -c reboot");
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });
    }

    public void openRepo(View view) {
        Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/shoukolate/Switch-My-Slot-Android"));
        startActivity(browserIntent);
    }

    private void copyFile(Context context) {
        AssetManager assetManager = context.getAssets();
        try {
            InputStream in = assetManager.open("bootctl");
            OutputStream out = new FileOutputStream(path + "bootctl");
            byte[] buffer = new byte[1024];
            int read = in.read(buffer);
            while (read != -1) {
                out.write(buffer, 0, read);
                read = in.read(buffer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}