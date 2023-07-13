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

import android.animation.ObjectAnimator;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothGattCharacteristic;
import android.bluetooth.BluetoothGattService;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.infineon.airocbluetoothconnect.BLEConnectionServices.BluetoothLeService;
import com.infineon.airocbluetoothconnect.CommonFragments.CarouselFragment;
import com.infineon.airocbluetoothconnect.CommonUtils.Constants;
import com.infineon.airocbluetoothconnect.CommonUtils.GattAttributes;
import com.infineon.airocbluetoothconnect.CommonUtils.Logger;
import com.infineon.airocbluetoothconnect.CommonUtils.Utils;
import com.infineon.airocbluetoothconnect.R;

import java.util.List;

/**
 * Fragment to show the CO2 information service
 */
public class CO2InformationService extends Fragment {

    // Service and characteristics
    private static BluetoothGattService mService;
    private static BluetoothGattCharacteristic mReadCharacteristic;
    private static BluetoothGattCharacteristic mNotifyCharacteristic;
    private Boolean mNotifyCharacteristicEnabled = false;

    //ProgressDialog
    private ProgressDialog mProgressDialog;
    /**
     * BroadcastReceiver for receiving the GATT server status
     */
    private BroadcastReceiver mGattUpdateReceiver = new BroadcastReceiver() {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Bundle extras = intent.getExtras();
            // GATT Data available
            if (BluetoothLeService.ACTION_DATA_AVAILABLE.equals(action)) {
                Logger.i("Data Available");
                // Check for battery information
                if (extras.containsKey(Constants.EXTRA_CO2_VALUE)) {
                    String received_btl_data = intent
                            .getStringExtra(Constants.EXTRA_CO2_VALUE);
                    Logger.i("received_btl_data " + received_btl_data);
                    if (!received_btl_data.equalsIgnoreCase(" ")) {

                        if(CarouselFragment.debug)
                        {
                            displayCO2value(received_btl_data+"00");
                        }
                        else
                        {
                            displayCO2value(received_btl_data);
                        }

                        int BTL_VALUE = Integer.parseInt(received_btl_data);

                        AnimationDrawable Red = (AnimationDrawable) mRedLed.getBackground();
                        AnimationDrawable orange = (AnimationDrawable) mOrangeLed.getBackground();

                        //turn all LED off, then trigger right LED on. If no LED on, it will something wrong.
                        mGreenLed.setImageResource(R.drawable.round_bg);
                        orange.stop();
                        Red.stop();

                        if(CarouselFragment.debug)
                        {
                            if( BTL_VALUE < 10)
                            {
                                mGreenLed.setImageResource(R.drawable.round_bg_green);
                                orange.selectDrawable(0);
                                Red.selectDrawable(0);

                            }else if( 10 <= BTL_VALUE && BTL_VALUE < 50)
                            {
                                orange.start();
                                Red.selectDrawable(0);
                            }else if( 50 <=BTL_VALUE)
                            {
                                Red.start();
                                orange.selectDrawable(0);
                            }
                        }else
                        {
                            if( BTL_VALUE < 1000)
                            {
                                mGreenLed.setImageResource(R.drawable.round_bg_green);

                            }else if( 1000 <= BTL_VALUE && BTL_VALUE < 5000)
                            {
                                orange.start();
                            }else if( 5000 <=BTL_VALUE)
                            {
                                Red.start();
                            }
                        }
                    }
                }
            }

            if (action.equals(BluetoothDevice.ACTION_BOND_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);

                if (state == BluetoothDevice.BOND_BONDING) {
                    // Bonding...
                    Logger.i("Bonding is in process....");
                    Utils.showBondingProgressDialog(getActivity(), mProgressDialog);
                } else if (state == BluetoothDevice.BOND_BONDED) {
                    String dataLog = getResources().getString(R.string.dl_commaseparator)
                            + "[" + BluetoothLeService.getBluetoothDeviceName() + "|"
                            + BluetoothLeService.getBluetoothDeviceAddress() + "]" +
                            getResources().getString(R.string.dl_commaseparator) +
                            getResources().getString(R.string.dl_connection_paired);
                    Logger.dataLog(dataLog);
                    Utils.hideBondingProgressDialog(mProgressDialog);
                    getGattData();

                } else if (state == BluetoothDevice.BOND_NONE) {
                    String dataLog = getResources().getString(R.string.dl_commaseparator)
                            + "[" + BluetoothLeService.getBluetoothDeviceName() + "|"
                            + BluetoothLeService.getBluetoothDeviceAddress() + "]" +
                            getResources().getString(R.string.dl_commaseparator) +
                            getResources().getString(R.string.dl_connection_unpaired);
                    Logger.dataLog(dataLog);
                    Utils.hideBondingProgressDialog(mProgressDialog);
                }
            }

        }

    };
    // Data fields

    private View rootView;
    private ImageView mGreenLed;
    private ImageView mOrangeLed;
    private ImageView mRedLed;

    private TextView mCO2_valueText;
    private Button mNotifyButton;
    private Button mReadButton;

    /**
     * Method to display the CO2 value
     *
     * @param received_btl_data
     */
    private void displayCO2value(String received_btl_data) {
        mCO2_valueText.setText(received_btl_data);
    }

    public static CO2InformationService create(BluetoothGattService service) {
        mService = service;
        return new CO2InformationService();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.co2_info_fragment,
                container, false);

        mCO2_valueText = (TextView) rootView
                .findViewById(R.id.CO2_value);

        LED_init();

        //prepareBroadcastDataNotify(mNotifyCharacteristic);

        mProgressDialog = new ProgressDialog(getActivity());

        setHasOptionsMenu(true);
        return rootView;
    }

    @Override
    public void onResume() {
        getGattData();
        BluetoothLeService.registerBroadcastReceiver(getActivity(), mGattUpdateReceiver, Utils.makeGattUpdateIntentFilter());
        Utils.setUpActionBar((AppCompatActivity)getActivity(), R.string.co2_info_fragment);
        super.onResume();
    }

    @Override
    public void onDestroy() {
        if (mNotifyCharacteristic != null) {
            stopBroadcastDataNotify(mNotifyCharacteristic);
        }
        BluetoothLeService.unregisterBroadcastReceiver(getActivity(), mGattUpdateReceiver);
        super.onDestroy();
    }

    /**
     * Method to get required characteristics from service
     */
    void getGattData() {
        List<BluetoothGattCharacteristic> gattCharacteristics = mService
                .getCharacteristics();
        for (BluetoothGattCharacteristic gattCharacteristic : gattCharacteristics) {
            String uuidchara = gattCharacteristic.getUuid().toString();
            if (uuidchara.equalsIgnoreCase(GattAttributes.CO2_LEVEL)||uuidchara.equalsIgnoreCase(GattAttributes.BATTERY_LEVEL)) {

                mReadCharacteristic = gattCharacteristic;
                mNotifyCharacteristic = gattCharacteristic;

                prepareBroadcastDataNotify(mNotifyCharacteristic);
                break;
            }
        }
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

    void LED_init()
    {
        mGreenLed = (ImageView)  rootView.findViewById(R.id.green_led);
        mOrangeLed = (ImageView)  rootView.findViewById(R.id.orange_led);
        mRedLed = (ImageView)  rootView.findViewById(R.id.red_led);

        mGreenLed.setImageResource(R.drawable.round_bg_green);
        AnimationDrawable orange = (AnimationDrawable) mOrangeLed.getBackground();
        orange.start();
        AnimationDrawable Red = (AnimationDrawable) mRedLed.getBackground();
        Red.start();

        mGreenLed.setImageResource(R.drawable.round_bg);
        orange.stop();
        Red.stop();
    }
}
