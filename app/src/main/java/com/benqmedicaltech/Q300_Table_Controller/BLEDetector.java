/*
 *
 *
 *     BLEDetector.java
 *
 *     Copyright (c) 2016, Atmel Corporation. All rights reserved.
 *     Released under NDA
 *     Licensed under Atmel's Limited License Agreement.
 *
 *     THIS SOFTWARE IS PROVIDED BY ATMEL "AS IS" AND ANY EXPRESS OR IMPLIED
 *     WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF
 *     MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NON-INFRINGEMENT ARE
 *     EXPRESSLY AND SPECIFICALLY DISCLAIMED. IN NO EVENT SHALL ATMEL BE LIABLE FOR
 *     ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 *     DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS
 *     OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 *     HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
 *     STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN
 *     ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 *     POSSIBILITY OF SUCH DAMAGE.
 *
 *     Atmel Corporation: http://www.atmel.com
 *
 *
 */

package com.benqmedicaltech.Q300_Table_Controller;

import android.bluetooth.BluetoothDevice;
import android.os.ParcelUuid;

import java.util.List;


public interface BLEDetector {

    void leDeviceDetected(BluetoothDevice bluetoothDevice, int rssi, List<ParcelUuid> uuidList);

    void leScanStopped();
}
