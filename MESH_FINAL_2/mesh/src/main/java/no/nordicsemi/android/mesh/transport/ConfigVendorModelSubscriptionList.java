/*
 * Copyright (c) 2018, Nordic Semiconductor
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this list of conditions and the following disclaimer.
 *
 * 2. Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following disclaimer in the
 * documentation and/or other materials provided with the distribution.
 *
 * 3. Neither the name of the copyright holder nor the names of its contributors may be used to endorse or promote products derived from this
 * software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
 * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT
 * HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
 * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE
 * USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package no.nordicsemi.android.mesh.transport;

import android.os.Parcel;
import android.os.Parcelable;
import no.nordicsemi.android.mesh.logger.MeshLogger;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.opcodes.ConfigMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * Creates the ConfigModelSubscriptionStatus Message.
 * <p> This message lists all subscription addresses for a SIG Models </p>
 */
@SuppressWarnings({"unused", "WeakerAccess"})
public class ConfigVendorModelSubscriptionList extends ConfigStatusMessage implements Parcelable {

    private static final String TAG = ConfigVendorModelSubscriptionList.class.getSimpleName();
    private static final int OP_CODE = ConfigMessageOpCodes.CONFIG_VENDOR_MODEL_SUBSCRIPTION_LIST;
    private int mElementAddress;
    private int mModelIdentifier;
    private final List<Integer> mSubscriptionAddresses;

    private static final Creator<ConfigVendorModelSubscriptionList> CREATOR = new Creator<ConfigVendorModelSubscriptionList>() {
        @Override
        public ConfigVendorModelSubscriptionList createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            //noinspection ConstantConditions
            return new ConfigVendorModelSubscriptionList(message);
        }

        @Override
        public ConfigVendorModelSubscriptionList[] newArray(int size) {
            return new ConfigVendorModelSubscriptionList[size];
        }
    };

    /**
     * Constructs the ConfigModelSubscriptionStatus mMessage.
     *
     * @param message Access Message
     */
    public ConfigVendorModelSubscriptionList(@NonNull final AccessMessage message) {
        super(message);
        mSubscriptionAddresses = new ArrayList<>();
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    final void parseStatusParameters() {
        final AccessMessage message = (AccessMessage) mMessage;
        mStatusCode = mParameters[0];
        mStatusCodeName = getStatusCodeName(mStatusCode);
        mElementAddress = MeshParserUtils.unsignedBytesToInt(mParameters[1], mParameters[2]);
        mModelIdentifier = MeshParserUtils.bytesToInt(new byte[]{mParameters[4], mParameters[3], mParameters[6], mParameters[5]});

        MeshLogger.verbose(TAG, "Status code: " + mStatusCode);
        MeshLogger.verbose(TAG, "Status message: " + mStatusCodeName);
        MeshLogger.verbose(TAG, "Element Address: " + MeshAddress.formatAddress(mElementAddress, true));
        MeshLogger.verbose(TAG, "Model Identifier: " + Integer.toHexString(mModelIdentifier));

        for (int i = 7; i < mParameters.length; i += 2) {
            final int address = MeshParserUtils.unsignedBytesToInt(mParameters[i], mParameters[i + 1]);
            mSubscriptionAddresses.add(address);
            MeshLogger.verbose(TAG, "Subscription Address: " + MeshAddress.formatAddress(address, false));
        }
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the element address that the key was bound to
     *
     * @return element address
     */
    public int getElementAddress() {
        return mElementAddress;
    }

    /**
     * Returns the list of subscription addresses.
     *
     * @return subscription address
     */
    public List<Integer> getSubscriptionAddresses() {
        return mSubscriptionAddresses;
    }

    /**
     * Returns the model identifier
     *
     * @return 16-bit sig model identifier or 32-bit vendor model identifier
     */
    public final int getModelIdentifier() {
        return mModelIdentifier;
    }

    /**
     * Returns if the message was successful
     *
     * @return true if the message was successful or false otherwise
     */
    public final boolean isSuccessful() {
        return mStatusCode == 0x00;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(final Parcel dest, final int flags) {
        final AccessMessage message = (AccessMessage) mMessage;
        dest.writeParcelable(message, flags);
    }
}
