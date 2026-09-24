package com.good.anticheat;

import ai.onnxruntime.*;

import java.nio.FloatBuffer;
import java.nio.file.Path;
import java.util.Collections;

public class AIDetector {

    private OrtEnvironment env;
    private OrtSession session;
    private boolean ready = false;

    public AIDetector(Path modelPath) {
        try {
            env = OrtEnvironment.getEnvironment();
            session = env.createSession(modelPath.toString(), new OrtSession.SessionOptions());
            ready = true;
        } catch (Exception e) {
            ready = false;
        }
    }

    public boolean isReady() { return ready; }

    public float predict(float[] features) {
        if (!ready) return 0f;
        try {
            long[] shape = {1, features.length};
            OnnxTensor tensor = OnnxTensor.createTensor(env, FloatBuffer.wrap(features), shape);
            OrtSession.Result res = session.run(Collections.singletonMap("input", tensor));
            float[][] out = (float[][]) res.get(0).getValue();
            return out[0][0];
        } catch (Exception e) {
            return 0f;
        }
    }
}