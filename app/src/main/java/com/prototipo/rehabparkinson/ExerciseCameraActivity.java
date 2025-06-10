package com.prototipo.rehabparkinson;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.Toast;

import androidx.core.app.ActivityCompat;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class ExerciseCameraActivity extends CameraActivity implements CameraBridgeViewBase.CvCameraViewListener2 {
    private static final String TAG = "OpenCV::ExerciseCamera";

    private CameraBridgeViewBase mOpenCvCameraView;
    private CascadeClassifier faceDetector;
    private int cameraIndex = CameraBridgeViewBase.CAMERA_ID_BACK;
    private boolean isRecording = false;
    private MediaRecorder mediaRecorder;
    private File videoFile;

    private Button btnSwitchCamera, btnRecord, btnCancel;

    private static final Size DET_POS = new Size(0.5, 0.5);
    private static final double DET_Y_OFFSET = -0.1;

    public ExerciseCameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_terapia_ejercicio);

        // Permisos requeridos
        ActivityCompat.requestPermissions(this, new String[]{
                Manifest.permission.CAMERA,
                Manifest.permission.RECORD_AUDIO,
                Manifest.permission.WRITE_EXTERNAL_STORAGE
        }, 1);

        // Cargar clasificador en cascada
        try {
            InputStream is = getResources().openRawResource(R.raw.haarcascade_frontalface_alt2);
            File cascadeDir = getDir("cascade", Context.MODE_PRIVATE);
            File caseFile = new File(cascadeDir, "haarcascade_frontalface_alt2.xml");

            FileOutputStream fos = new FileOutputStream(caseFile);
            byte[] buffer = new byte[4096];
            int bytesRead;

            while ((bytesRead = is.read(buffer)) != -1) {
                fos.write(buffer, 0, bytesRead);
            }
            is.close();
            fos.close();

            faceDetector = new CascadeClassifier(caseFile.getAbsolutePath());
            if (faceDetector.empty()) faceDetector = null;

            caseFile.delete();
            cascadeDir.delete();

        } catch (Exception e) {
            Log.e(TAG, "Error reading cascade file", e);
        }

        mOpenCvCameraView = findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCameraIndex(cameraIndex);
        mOpenCvCameraView.setCvCameraViewListener(this);
        mOpenCvCameraView.enableView();

        // Inicializar botones
        btnSwitchCamera = findViewById(R.id.btnSwitchCamera);
        btnRecord = findViewById(R.id.btnRecord);
        btnCancel = findViewById(R.id.btnCancel);

        btnSwitchCamera.setOnClickListener(v -> switchCamera());
        btnRecord.setOnClickListener(v -> toggleRecording());
        btnCancel.setOnClickListener(v -> {
            if (isRecording) stopRecording();
            finish();
        });
    }

    private void switchCamera() {
        cameraIndex = (cameraIndex == CameraBridgeViewBase.CAMERA_ID_BACK)
                ? CameraBridgeViewBase.CAMERA_ID_FRONT
                : CameraBridgeViewBase.CAMERA_ID_BACK;

        mOpenCvCameraView.disableView();
        mOpenCvCameraView.setCameraIndex(cameraIndex);
        mOpenCvCameraView.enableView();
    }

    private void toggleRecording() {
        if (isRecording) {
            stopRecording();
            btnRecord.setText("Grabar");
        } else {
            if (startRecording()) {
                btnRecord.setText("Detener");
            } else {
                Toast.makeText(this, "No se pudo iniciar la grabación", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private boolean startRecording() {
        try {
            videoFile = new File(getExternalFilesDir(null), "grabacion.mp4");

            mediaRecorder = new MediaRecorder();
            mediaRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
            mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
            mediaRecorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
            mediaRecorder.setOutputFile(videoFile.getAbsolutePath());
            mediaRecorder.setVideoEncoder(MediaRecorder.VideoEncoder.H264);
            mediaRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
            mediaRecorder.setVideoFrameRate(30);
            mediaRecorder.setVideoEncodingBitRate(1_000_000);
            mediaRecorder.setVideoSize(640, 480);
            mediaRecorder.prepare();
            mediaRecorder.start();

            isRecording = true;
            return true;
        } catch (Exception e) {
            Log.e(TAG, "startRecording failed", e);
            return false;
        }
    }

    private void stopRecording() {
        try {
            mediaRecorder.stop();
            mediaRecorder.reset();
            mediaRecorder.release();
            isRecording = false;
        } catch (Exception e) {
            Log.e(TAG, "stopRecording failed", e);
        }
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mOpenCvCameraView != null) mOpenCvCameraView.enableView();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null) mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) { }

    @Override
    public void onCameraViewStopped() { }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        Mat frame = inputFrame.rgba();
        Mat gray = inputFrame.gray();
        Size s = frame.size();

        Point p = new Point(
                s.width * DET_POS.width,
                s.height * (DET_POS.height + DET_Y_OFFSET)
        );

        Imgproc.ellipse(
                frame,
                new RotatedRect(p, new Size(50, 50), 0),
                new Scalar(255, 0, 0),
                2
        );

        if (faceDetector != null) {
            MatOfRect detections = new MatOfRect();
            faceDetector.detectMultiScale(gray, detections);
            for (Rect rect : detections.toArray()) {
                Imgproc.rectangle(
                        frame,
                        new Point(rect.x, rect.y),
                        new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0),
                        2
                );
            }
        }

        return frame;
    }
}
