/*
 * Copyright 2014-2022, Cypress Semiconductor Corporation (an Infineon company) or
 * an affiliate of Cypress Semiconductor Corporation.  All rights reserved.
 *
 * This software, including source code, documentation and related
 * materials ("Software") is owned by Cypress Semiconductor Corporation
 * or one of its affiliates ("Cypress") and is protected by and subject to
 * worldwide patent protection (United States and foreign),
 * United States copyright laws and international treaty provisions.
 * Therefore, you may use this Software only as provided in the license
 * agreement accompanying the software package from which you
 * obtained this Software ("EULA").
 * If no EULA applies, Cypress hereby grants you a personal, non-exclusive,
 * non-transferable license to copy, modify, and compile the Software
 * source code solely for use in connection with Cypress's
 * integrated circuit products.  Any reproduction, modification, translation,
 * compilation, or representation of this Software except as specified
 * above is prohibited without the express written permission of Cypress.
 *
 * Disclaimer: THIS SOFTWARE IS PROVIDED AS-IS, WITH NO WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING, BUT NOT LIMITED TO, NONINFRINGEMENT, IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE. Cypress
 * reserves the right to make changes to the Software without notice. Cypress
 * does not assume any liability arising out of the application or use of the
 * Software or any product or circuit described in the Software. Cypress does
 * not authorize its products for use in any products where a malfunction or
 * failure of the Cypress product may reasonably be expected to result in
 * significant property damage, injury or death ("High Risk Product"). By
 * including Cypress's product in a High Risk Product, the manufacturer
 * of such system or application assumes all risk of such use and in doing
 * so agrees to indemnify Cypress against all liability.
 */

package com.infineon.airocbluetoothconnect.BLEServiceFragments;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.infineon.airocbluetoothconnect.AIROCBluetoothConnectApp;
import com.infineon.airocbluetoothconnect.BLEConnectionServices.BluetoothLeService;
import com.infineon.airocbluetoothconnect.CommonFragments.CarouselFragment;
import com.infineon.airocbluetoothconnect.CommonUtils.Constants;
import com.infineon.airocbluetoothconnect.CommonUtils.GattAttributes;
import com.infineon.airocbluetoothconnect.CommonUtils.HexKeyBoard;
import com.infineon.airocbluetoothconnect.CommonUtils.Logger;
import com.infineon.airocbluetoothconnect.CommonUtils.Utils;
import com.infineon.airocbluetoothconnect.GATTDBFragments.GattDescriptorFragment;
import com.infineon.airocbluetoothconnect.R;

import java.util.List;

/**
 * Fragment to show the CEILING FAN service
 */
public class CeilingFanService extends Fragment {

    // Service and characteristics
    private static BluetoothGattService mService;

    // Application
    private AIROCBluetoothConnectApp mApplication;

    //characteristics
    private BluetoothGattCharacteristic mCharacteristic;

    private List<BluetoothGattCharacteristic> mGattCharacteristics;

    // Data fields

    private View rootView;


    private TextView mSpeedLevelText;

    private TextView mDirectionText;
    private Button mSpeedUpButton;
    private Button mSpeedDownButton;

    private Button mStopButton;

    private Button mInvertButton;

    private int mSpeedLevel;

    private String mDirection;
    public static CeilingFanService create(BluetoothGattService service) {
        mService = service;
        return new CeilingFanService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.ceiling_fan_control_fragment,
                container, false);
        mApplication = (AIROCBluetoothConnectApp) getActivity().getApplication();

        //GattCharacteristics need set - Start
        mGattCharacteristics = mService.getCharacteristics();
        //TODO : check safe function
        mApplication.setBluetoothGattCharacteristic(mGattCharacteristics.get(0));
        //GattCharacteristics need set - End

        mCharacteristic = mApplication.getBluetoothGattCharacteristic();

        mSpeedLevelText = (TextView) rootView
                .findViewById(R.id.SpeedLevel);
        mDirectionText  = (TextView) rootView
                .findViewById(R.id.Direction);
        mDirectionText.setText("F");

        mSpeedUpButton = (Button) rootView.findViewById(R.id.Speed_up);
        mSpeedDownButton = (Button) rootView.findViewById(R.id.Speed_down);
        mStopButton = (Button) rootView.findViewById(R.id.Stop);
        mInvertButton = (Button) rootView.findViewById(R.id.Invert);



        mSpeedUpButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeedLevel = Integer.parseInt(mSpeedLevelText.getText().toString());

                if(mSpeedLevel<7 && 0<=mSpeedLevel)
                {
                    mSpeedLevel++;
                }
                mSpeedLevelText.setText(String.valueOf(mSpeedLevel));
                byte[] convertedBytes = Utils.convertingToByteArray("0x0"+ mSpeedLevel);
                writeCharacteristicValue(convertedBytes);
            }
        });
        mSpeedDownButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeedLevel = Integer.parseInt(mSpeedLevelText.getText().toString());

                if(mSpeedLevel<=7 && 1<mSpeedLevel)
                {
                    mSpeedLevel--;
                }

                //Start up when stop status
                if(mSpeedLevel==0)
                {
                    mSpeedLevel = 1;
                }
                mSpeedLevelText.setText(String.valueOf(mSpeedLevel));
                byte[] convertedBytes = Utils.convertingToByteArray("0x0"+ mSpeedLevel);
                writeCharacteristicValue(convertedBytes);
            }
        });
        mStopButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mSpeedLevelText.setText("0");
                byte[] convertedBytes = Utils.convertingToByteArray("0x00");
                writeCharacteristicValue(convertedBytes);
            }
        });
        mInvertButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mDirection = mDirectionText.getText().toString();
                if(mDirection=="F")
                {
                    mDirectionText.setText("R");
                    byte[] convertedBytes = Utils.convertingToByteArray("0xFF");
                    writeCharacteristicValue(convertedBytes);
                }
                else {
                    mDirectionText.setText("F");
                    byte[] convertedBytes = Utils.convertingToByteArray("0xFE");
                    writeCharacteristicValue(convertedBytes);
                }
            }
        });

        setHasOptionsMenu(true);
        return rootView;
    }


    @Override
    public void onResume() {
        getGattData();
        Utils.setUpActionBar((AppCompatActivity)getActivity(), R.string.ceiling_fan_fragment);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    /**
     * Method to get required characteristics from service
     */
    void getGattData() {
        List<BluetoothGattCharacteristic> gattCharacteristics = mService
                .getCharacteristics();

    }

    /**
     * Preparing Broadcast receiver to broadcast read characteristics
     *
     * @param gattCharacteristic
     */
    void prepareBroadcastDataRead(
            BluetoothGattCharacteristic gattCharacteristic) {
        if (checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(),
                BluetoothGattCharacteristic.PROPERTY_READ)) {
            BluetoothLeService.readCharacteristic(gattCharacteristic);
        }
    }

    /**
     * Preparing Broadcast receiver to broadcast notify characteristics
     *
     * @param gattCharacteristic
     */
    void prepareBroadcastDataNotify(
            BluetoothGattCharacteristic gattCharacteristic) {
        Logger.i("Notify called");
        if (checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
            BluetoothLeService.setCharacteristicNotification(gattCharacteristic,
                    true);
        }
    }

    /**
     * Stopping Broadcast receiver to broadcast notify characteristics
     *
     * @param gattCharacteristic
     */
    void stopBroadcastDataNotify(
            BluetoothGattCharacteristic gattCharacteristic) {
        if (checkCharacteristicsPropertyPresence(gattCharacteristic.getProperties(),
                BluetoothGattCharacteristic.PROPERTY_NOTIFY)) {
            if (gattCharacteristic != null) {
                BluetoothLeService.setCharacteristicNotification(
                        gattCharacteristic, false);
            }

        }

    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        menu.clear();
        inflater.inflate(R.menu.global, menu);
        MenuItem graph = menu.findItem(R.id.graph);
        MenuItem log = menu.findItem(R.id.log);
        MenuItem search = menu.findItem(R.id.search);
        search.setVisible(false);
        graph.setVisible(false);
        log.setVisible(true);
        super.onCreateOptionsMenu(menu, inflater);
    }

    // Return the properties of mGattCharacteristics
    boolean checkCharacteristicsPropertyPresence(int characteristics,
                                                 int characteristicsSearch) {
        return (characteristics & characteristicsSearch) == characteristicsSearch;
    }


    /**
     * Method to write the byte value to the characteristic
     *
     * @param value
     */
    private void writeCharacteristicValue(byte[] value) {
        try {
            BluetoothLeService.writeCharacteristicGattDb(mCharacteristic, value);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

}
