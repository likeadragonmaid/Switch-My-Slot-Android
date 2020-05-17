package org.dynamicsoft.switchmyslot;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import java.io.DataInputStream;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

    TextView halInfoTV, numberOfSlotsTV, currentSlotTV, CurrentSlotSuffixTV;
    int currentSlot;
    String convertedSlotNumberToAlphabet = null;
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


        try {
            Process halInfoProcess;
            halInfoProcess = Runtime.getRuntime().exec("su -c bootctl hal-info");
            DataInputStream halinfoOUT = new DataInputStream(halInfoProcess.getInputStream());
            halInfoTV.setText(halinfoOUT.readLine());

            Process numberOfSlotsProcess;
            numberOfSlotsProcess = Runtime.getRuntime().exec("su -c bootctl get-number-slots");
            DataInputStream numberOfSlotsProcessOUT = new DataInputStream(numberOfSlotsProcess.getInputStream());
            numberOfSlotsTV.setText("Number of slots: " + numberOfSlotsProcessOUT.readLine());

            Process currentSlotProcess;
            currentSlotProcess = Runtime.getRuntime().exec("su -c bootctl get-current-slot");
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
            CurrentSlotSuffixProcess = Runtime.getRuntime().exec("su -c bootctl get-suffix " + currentSlot);
            DataInputStream CurrentSlotSuffixProcessOUT = new DataInputStream(CurrentSlotSuffixProcess.getInputStream());
            CurrentSlotSuffixTV.setText("Current slot suffix: " + CurrentSlotSuffixProcessOUT.readLine());

        } catch (IOException e) {
            e.printStackTrace();
        }

        switcher.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                try {
                    if (isChecked) {
                        Runtime.getRuntime().exec("su -c bootctl set-active-boot-slot b");
                        Toast.makeText(MainActivity.this, "Switching to Slot B", Toast.LENGTH_SHORT).show();
                    } else {
                        Runtime.getRuntime().exec("su -c bootctl set-active-boot-slot a");
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
}