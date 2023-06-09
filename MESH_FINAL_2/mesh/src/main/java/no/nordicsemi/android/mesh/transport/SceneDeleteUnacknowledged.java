package no.nordicsemi.android.mesh.transport;

import no.nordicsemi.android.mesh.logger.MeshLogger;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

import androidx.annotation.NonNull;
import no.nordicsemi.android.mesh.ApplicationKey;
import no.nordicsemi.android.mesh.opcodes.ApplicationMessageOpCodes;
import no.nordicsemi.android.mesh.utils.SecureUtils;

import static no.nordicsemi.android.mesh.Scene.isValidSceneNumber;

/**
 * To be used as a wrapper class when creating a SceneDeleteUnacknowledged message.
 */
public class SceneDeleteUnacknowledged extends ApplicationMessage {

    private static final String TAG = SceneDeleteUnacknowledged.class.getSimpleName();
    private static final int OP_CODE = ApplicationMessageOpCodes.SCENE_DELETE_UNACKNOWLEDGED;
    private static final int SCENE_DELETE_PARAMS_LENGTH = 2;

    private int sceneNumber;

    /**
     * Constructs SceneDeleteUnacknowledged message.
     *
     * @param appKey      {@link ApplicationKey} key for this message
     * @param sceneNumber Scene number of SceneDeleteUnacknowledged message
     * @throws IllegalArgumentException if any illegal arguments are passed
     */
    public SceneDeleteUnacknowledged(@NonNull final ApplicationKey appKey,
                                     final int sceneNumber) {
        super(appKey);
        if (isValidSceneNumber(sceneNumber))
            this.sceneNumber = sceneNumber;
        assembleMessageParameters();
    }

    @Override
    public int getOpCode() {
        return OP_CODE;
    }

    @Override
    void assembleMessageParameters() {
        mAid = SecureUtils.calculateK4(mAppKey.getKey());
        final ByteBuffer paramsBuffer;
        MeshLogger.verbose(TAG, "Scene Number: " + sceneNumber);
        paramsBuffer = ByteBuffer.allocate(SCENE_DELETE_PARAMS_LENGTH).order(ByteOrder.LITTLE_ENDIAN);
        paramsBuffer.putShort((short) sceneNumber);
        mParameters = paramsBuffer.array();
    }

    public int getSceneNumber() {
        return sceneNumber;
    }
}
