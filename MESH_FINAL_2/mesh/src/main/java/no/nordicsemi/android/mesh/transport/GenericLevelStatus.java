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

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.MeshAddress;
import no.nordicsemi.android.mesh.utils.MeshParserUtils;

/**
 * To be used as a wrapper class to create generic level status message.
 */
@SuppressWarnings({"WeakerAccess"})
public final class GenericLevelStatus extends ApplicationStatusMessage implements Parcelable {

    private static final String TAG = GenericLevelStatus.class.getSimpleName();
    private static final int GENERIC_LEVEL_STATUS_MANDATORY_LENGTH = 2;
    private static final int OP_CODE = ApplicationMessageOpCodes.GENERIC_LEVEL_STATUS;
    private int mPresentLevel;
    private Integer mTargetLevel;
    private int mTransitionSteps;
    private int mTransitionResolution;

    private static final Creator<GenericLevelStatus> CREATOR = new Creator<GenericLevelStatus>() {
        @Override
        public GenericLevelStatus createFromParcel(Parcel in) {
            final AccessMessage message = in.readParcelable(AccessMessage.class.getClassLoader());
            return new GenericLevelStatus(message);
        }

        @Override
        public GenericLevelStatus[] newArray(int size) {
            return new GenericLevelStatus[size];
        }
    };

    /**
     * Constructs GenericLevelStatus message
     * @param message access message
     */
    public GenericLevelStatus(@NonNull final AccessMessage message) {
        super(message);
        this.mMessage = message;
        this.mParameters = message.getParameters();
        parseStatusParameters();
    }

    @Override
    void parseStatusParameters() {
        MeshLogger.verbose(TAG, "Received generic level status from: " + MeshAddress.formatAddress(mMessage.getSrc(), true));
        final ByteBuffer buffer = ByteBuffer.wrap(mParameters).order(ByteOrder.LITTLE_ENDIAN);
        mPresentLevel = (int) (buffer.getShort());
        MeshLogger.verbose(TAG, "Present level: " + mPresentLevel);
        if (buffer.limit() > GENERIC_LEVEL_STATUS_MANDATORY_LENGTH) {
            mTargetLevel = (int) (buffer.getShort());
            final int remainingTime = buffer.get() & 0xFF;
            mTransitionSteps = (remainingTime & 0x3F);
            mTransitionResolution = (remainingTime >> 6);
            MeshLogger.verbose(TAG, "Target level: " + mTargetLevel);
            MeshLogger.verbose(TAG, "Remaining time, transition number of steps: " + mTransitionSteps);
            MeshLogger.verbose(TAG, "Remaining time, transition number of step resolution: " + mTransitionResolution);
            MeshLogger.verbose(TAG, "Remaining time: " + MeshParserUtils.getRemainingTime(remainingTime));
        }
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    /**
     * Returns the present level of the GenericOnOffModel
     *
     * @return present level
     */
    public final int getPresentLevel() {
        return mPresentLevel;
    }

    /**
     * Returns the target level of the GenericOnOffModel
     *
     * @return target level
     */
    public final Integer getTargetLevel() {
        return mTargetLevel;
    }

    /**
     * Returns the transition steps.
     *
     * @return transition steps
     */
    public int getTransitionSteps() {
        return mTransitionSteps;
    }

    /**
     * Returns the transition resolution.
     *
     * @return transition resolution
     */
    public int getTransitionResolution() {
        return mTransitionResolution;
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
