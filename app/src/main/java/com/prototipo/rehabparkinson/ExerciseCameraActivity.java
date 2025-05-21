package com.prototipo.rehabparkinson;

import org.opencv.android.CameraActivity;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewFrame;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.RotatedRect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.CameraBridgeViewBase.CvCameraViewListener2;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.WindowManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Collections;
import java.util.List;

public class ExerciseCameraActivity extends CameraActivity implements CvCameraViewListener2 {
    private static final String TAG = "OpenCV::ExerciseCameraActivity";

    private CameraBridgeViewBase mOpenCvCameraView;
    private static final Size DET_POS = new Size(0.5, 0.5);
    private static final double DET_Y_OFFSET = -0.1;

    private CascadeClassifier faceDetector;
    public ExerciseCameraActivity() {
        Log.i(TAG, "Instantiated new " + this.getClass());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");
        super.onCreate(savedInstanceState);

        // Los archivos en res/raw no existen en el móvil, por lo que
        // hay que hacer un hack para acceder a los cascades:
        // Leer los bytes con getResources() y escribirlos en un archivo temporal,
        // para entonces pasarle el path absoluto de este archivo a CascadeClassifier.
        // https://laxmantidake.medium.com/real-time-face-detection-with-android-studio-and-opencv-e0b2e86a04eb
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
            if (faceDetector.empty()) {
                faceDetector = null;
            } else if (!cascadeDir.delete()) {
                Log.e(TAG, "Couldn't delete " + cascadeDir);
            }

            if (!caseFile.delete()) {
                Log.e(TAG, "Couldn't delete " + caseFile);
            }

        } catch (Exception e) {
            Log.e(TAG, "Error reading cascade file. Is it in res/raw/?");
        }

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_terapia_ejercicio);

        mOpenCvCameraView = findViewById(R.id.tutorial1_activity_java_surface_view);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onResume()
    {
        super.onResume();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.enableView();
    }

    @Override
    protected List<? extends CameraBridgeViewBase> getCameraViewList() {
        return Collections.singletonList(mOpenCvCameraView);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mOpenCvCameraView != null)
            mOpenCvCameraView.disableView();
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CvCameraViewFrame inputFrame) {
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

        return frame;
    }
}
