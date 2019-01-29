/*
 * Copyright 2018 New Vector Ltd
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.matrix.androidsdk.crypto.keysbackup

import org.matrix.androidsdk.crypto.MXCrypto
import org.matrix.androidsdk.util.Log
import java.util.*

class KeysBackupStateManager(val crypto: MXCrypto) {

    private val mListeners = ArrayList<KeysBackupStateListener>()

    // Backup state
    var state = KeysBackupState.Unknown
        set(newState) {
            Log.d("KeysBackup", "setState: $field -> $newState")

            field = newState

            // Notify listeners about the state change, on the ui thread
            synchronized(mListeners) {
                crypto.uiHandler.post {
                    mListeners.forEach {
                        // Use newState because state may have already changed again
                        it.onStateChange(newState)
                    }
                }
            }
        }

    val isEnabled: Boolean
        get() = state == KeysBackupStateManager.KeysBackupState.ReadyToBackUp
                || state == KeysBackupStateManager.KeysBackupState.WillBackUp
                || state == KeysBackupStateManager.KeysBackupState.BackingUp

    /**
     * E2e keys backup states.
     *
     * <pre>
     *                               |
     *                               V        deleteKeyBackupVersion (on current backup)
     *  +---------------------->  UNKNOWN  <-------------
     *  |                            |
     *  |                            | checkAndStartKeyBackup (at startup or on new verified device or a new detected backup)
     *  |                            V
     *  |                     CHECKING BACKUP
     *  |                            |
     *  |    Network error           |
     *  +<----------+----------------+-------> DISABLED <----------------------+
     *  |           |                |            |                            |
     *  |           |                |            | createKeyBackupVersion     |
     *  |           V                |            V                            |
     *  +<---  WRONG VERSION         |         ENABLING                        |
     *              ^                |            |                            |
     *              |                V       ok   |     error                  |
     *              |     +------> READY <--------+----------------------------+
     *              |     |          |
     *              |     |          | on new key
     *              |     |          V
     *              |     |     WILL BACK UP (waiting a random duration)
     *              |     |          |
     *              |     |          |
     *              |     | ok       V
     *              |     +----- BACKING UP
     *              |                |
     *              |      Error     |
     *              +<---------------+
     * </pre>
     */
    // TODO Missing state: KeyBackup exist and waiting for recovery key
    enum class KeysBackupState {
        // Need to check the current backup version on the homeserver
        Unknown,
        // Checking if backup is enabled on home server
        CheckingBackUpOnHomeserver,
        // Backup has been stopped because a new backup version has been detected on the homeserver
        WrongBackUpVersion,
        // Backup from this device is not enabled
        Disabled,
        // Backup is being enabled: the backup version is being created on the homeserver
        Enabling,
        // Backup is enabled and ready to send backup to the homeserver
        ReadyToBackUp,
        // e2e keys are going to be sent to the homeserver
        WillBackUp,
        // e2e keys are being sent to the homeserver
        BackingUp
    }

    interface KeysBackupStateListener {
        fun onStateChange(newState: KeysBackupState)
    }

    fun addListener(listener: KeysBackupStateListener) {
        synchronized(mListeners) {
            mListeners.add(listener)
        }
    }

    fun removeListener(listener: KeysBackupStateListener) {
        synchronized(mListeners) {
            mListeners.remove(listener)
        }
    }
}
