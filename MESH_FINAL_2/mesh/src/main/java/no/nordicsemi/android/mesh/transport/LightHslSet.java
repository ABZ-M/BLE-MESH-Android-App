package no.nordicsemi.android.mesh.transport;


import no.nordicsemi.android.mesh.logger.MeshLogger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

/**
 * To be used as a wrapper class when creating a GenericLevelSet message.
 */
@SuppressWarnings("unused")
public class LightHslSet extends ApplicationMessage {

    private static final String TAG = LightCtlSet.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.LIGHT_HSL_SET;
    private static final int LIGHT_LIGHTNESS_SET_TRANSITION_PARAMS_LENGTH = 9;
    private static final int LIGHT_LIGHTNESS_SET_PARAMS_LENGTH = 7;

    private final Integer mTransitionSteps;
    private final Integer mTransitionResolution;
    private final Integer mDelay;
    private final int mLightness;
    private final int mHue;
    private final int mSaturation;
    private final int tId;

    /**
     * Constructs GenericLevelSet message.
     *
     * @param appKey          {@link ApplicationKey} key for this message
     * @param lightLightness  Lightness of the LightHslModel
     * @param lightHue        Hue of the LightHslModel
     * @param lightSaturation Saturation of the LightHslModel
     * @param tId             Transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public LightHslSet(@NonNull final ApplicationKey appKey,
                       final int lightLightness,
                       final int lightHue,
                       final int lightSaturation,
                       final int tId) throws IllegalArgumentException {
        this(appKey, null, null, null, lightLightness, lightHue, lightSaturation, tId);
    }

    /**
     * Constructs GenericLevelSet message.
     *
     * @param appKey               {@link ApplicationKey} key for this message
     * @param transitionSteps      Transition steps for the lightLightness
     * @param transitionResolution Transition resolution for the lightLightness
     * @param delay                Delay for this message to be executed 0 - 1275 milliseconds
     * @param lightLightness       Lightness of the LightHslModel
     * @param lightHue             Hue of the LightHslModel
     * @param lightSaturation      Saturation of the LightHslModel
     * @param tId                  Transaction id
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    @SuppressWarnings("WeakerAccess")
    public LightHslSet(@NonNull final ApplicationKey appKey,
                       @Nullable final Integer transitionSteps,
                       @Nullable final Integer transitionResolution,
                       @Nullable final Integer delay,
                       final int lightLightness,
                       final int lightHue,
                       final int lightSaturation,
                       final int tId) throws IllegalArgumentException {
        super(appKey);
        this.mTransitionSteps = transitionSteps;
        this.mTransitionResolution = transitionResolution;
        this.mDelay = delay;
        if (lightLightness < 0 || lightLightness > 0xFFFF)
            throw new IllegalArgumentException("Light lightness value must be between 0 to 0xFFFF");
        if (lightHue < 0 || lightHue > 0xFFFF)
            throw new IllegalArgumentException("Light hue value must be between 0 to 0xFFFF");
        if (lightSaturation < 0 || lightSaturation > 0xFFFF)
            throw new IllegalArgumentException("Light hue value must be between 0 to 0xFFFF");
        this.mLightness = lightLightness;
        this.mHue = lightHue;
        this.mSaturation = lightSaturation;
        this.tId = tId;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey() );
        final ByteBuffer paramsBuffer;
        MeshLogger.verbose(TAG, "Lightness: " + mLightness);
        MeshLogger.verbose(TAG, "Hue: " + mHue);
        MeshLogger.verbose(TAG, "Saturation: " + mSaturation);
        MeshLogger.verbose(TAG, "TID: " + tId);
        if (mTransitionSteps == null || mTransitionResolution == null || mDelay == null) {
            paramsBuffer = ByteBuffer.allocate(LIGHT_LIGHTNESS_SET_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) mLightness);
            paramsBuffer.putShort((short) mHue);
            paramsBuffer.putShort((short) mSaturation);
            paramsBuffer.put((byte) tId);
        } else {
            MeshLogger.verbose(TAG, "Transition steps: " + mTransitionSteps);
            MeshLogger.verbose(TAG, "Transition step resolution: " + mTransitionResolution);
            paramsBuffer = ByteBuffer.allocate(LIGHT_LIGHTNESS_SET_TRANSITION_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
            paramsBuffer.putShort((short) mLightness);
            paramsBuffer.putShort((short) mHue);
            paramsBuffer.putShort((short) mSaturation);
            paramsBuffer.put((byte) tId);
            paramsBuffer.put((byte) (mTransitionResolution << 6 | mTransitionSteps));
            final int delay = mDelay;
            paramsBuffer.put((byte) delay);
        }
        mParameters = paramsBuffer.array();
    }


}
