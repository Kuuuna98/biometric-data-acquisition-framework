/**************************************************************************************************
 * Filename:       BleDeviceInfo.java
 * <p>
 * Copyright (c) 2013 - 2014 Texas Instruments Incorporated
 * <p>
 * All rights reserved not granted herein.
 * Limited License.
 * <p>
 * Texas Instruments Incorporated grants a world-wide, royalty-free,
 * non-exclusive license under copyrights and patents it now or hereafter
 * owns or controls to make, have made, use, import, offer to sell and sell ("Utilize")
 * this software subject to the terms herein.  With respect to the foregoing patent
 * license, such license is granted  solely to the extent that any such patent is necessary
 * to Utilize the software alone.  The patent license shall not apply to any combinations which
 * include this software, other than combinations with devices manufactured by or for TI ('TI Devices').
 * No hardware patent is licensed hereunder.
 * <p>
 * Redistributions must preserve existing copyright notices and reproduce this license (including the
 * above copyright notice and the disclaimer and (if applicable) source code license limitations below)
 * in the documentation and/or other materials provided with the distribution
 * <p>
 * Redistribution and use in binary form, without modification, are permitted provided that the following
 * conditions are met:
 * <p>
 * No reverse engineering, decompilation, or disassembly of this software is permitted with respect to any
 * software provided in binary form.
 * any redistribution and use are licensed by TI for use only with TI Devices.
 * Nothing shall obligate TI to provide you with source code for the software licensed and provided to you in object code.
 * <p>
 * If software source code is provided to you, modification and redistribution of the source code are permitted
 * provided that the following conditions are met:
 * <p>
 * any redistribution and use of the source code, including any resulting derivative works, are licensed by
 * TI for use only with TI Devices.
 * any redistribution and use of any object code compiled from the source code and any resulting derivative
 * works, are licensed by TI for use only with TI Devices.
 * <p>
 * Neither the name of Texas Instruments Incorporated nor the names of its suppliers may be used to endorse or
 * promote products derived from this software without specific prior written permission.
 * <p>
 * DISCLAIMER.
 * <p>
 * THIS SOFTWARE IS PROVIDED BY TI AND TI'S LICENSORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING,
 * BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL TI AND TI'S LICENSORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 **************************************************************************************************/
package com.kaist.iclab.devices;

import android.bluetooth.BluetoothDevice;
import android.bluetooth.le.ScanResult;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

public class BleDeviceInfo {
    // Data
    private BluetoothDevice mBtDevice;
    private int mRssi;

    public BleDeviceInfo(ScanResult result) {
        mBtDevice = result.getDevice();
        mRssi = result.getRssi();
    }

    public BleDeviceInfo(BluetoothDevice device, int rssi) {
        mBtDevice = device;
        mRssi = rssi;
    }

    public BluetoothDevice getBluetoothDevice() {
        return mBtDevice;
    }

    public int getRssi() {
        return mRssi;
    }

    public void updateRssi(int rssiValue) {
        mRssi = rssiValue;
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder().append(getBluetoothDevice()).hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(o == null || o.getClass() != getClass())
            return false;
        if(o == this)
            return true;
        BleDeviceInfo other = (BleDeviceInfo) o;
        return new EqualsBuilder().append(other.getBluetoothDevice(),getBluetoothDevice()).isEquals();
    }
}
