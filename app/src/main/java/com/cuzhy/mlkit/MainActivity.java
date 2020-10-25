package com.cuzhy.mlkit;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.app.AlertDialog;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

import com.cuzhy.mlkit.helper.GraphicOverlay;
import com.cuzhy.mlkit.helper.ReactOverlay;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.mlkit.vision.common.InputImage;
import com.google.mlkit.vision.face.Face;
import com.google.mlkit.vision.face.FaceContour;
import com.google.mlkit.vision.face.FaceDetection;
import com.google.mlkit.vision.face.FaceDetector;
import com.google.mlkit.vision.face.FaceDetectorOptions;
import com.google.mlkit.vision.face.FaceLandmark;
import com.wonderkiln.camerakit.CameraKitError;
import com.wonderkiln.camerakit.CameraKitEvent;
import com.wonderkiln.camerakit.CameraKitEventListener;
import com.wonderkiln.camerakit.CameraKitImage;
import com.wonderkiln.camerakit.CameraKitVideo;
import com.wonderkiln.camerakit.CameraView;

import java.util.List;

import dmax.dialog.SpotsDialog;

public class MainActivity extends AppCompatActivity {

    private final String TAG = "MainActivity";

    private Button faceDetectButton;
    private GraphicOverlay graphicOverlay;
    private CameraView cameraView;

    private AlertDialog alertDialog;

    private ImageView cameraDetectImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        faceDetectButton = findViewById(R.id.detect_face_btn);
        graphicOverlay = findViewById(R.id.graphic_overlay);
        cameraView = findViewById(R.id.camera_views);
        cameraDetectImageView = findViewById(R.id.camera_detect_image_view);

        alertDialog = new SpotsDialog.Builder()
            .setContext(this)
            .setMessage("Please Wait, Processing...")
            .setCancelable(false)
            .build();

        faceDetectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                cameraView.start();
                cameraView.captureImage();
                graphicOverlay.clear();
                // clear draw canvas
                cameraDetectImageView.setImageBitmap(null);
            }
        });

        cameraView.addCameraKitListener(new CameraKitEventListener() {
            @Override
            public void onEvent(CameraKitEvent cameraKitEvent) {

            }

            @Override
            public void onError(CameraKitError cameraKitError) {

            }

            @Override
            public void onImage(CameraKitImage cameraKitImage) {
                alertDialog.show();
                Bitmap bitmap = cameraKitImage.getBitmap();
                bitmap = bitmap.createScaledBitmap(
                        bitmap,
                        cameraView.getWidth(),
                        cameraView.getHeight(),
                        false);
                cameraView.stop();

                processFaceDetection(bitmap);
            }

            @Override
            public void onVideo(CameraKitVideo cameraKitVideo) {

            }
        });
    }

    private void processFaceDetection(Bitmap bitmap) {
        InputImage image = InputImage.fromBitmap(bitmap, 0);

        FaceDetectorOptions options =
                new FaceDetectorOptions.Builder()
                        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_ALL)
                        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_ACCURATE)
                        .setLandmarkMode(FaceDetectorOptions.LANDMARK_MODE_ALL)
                        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_ALL)
                        .build();
        FaceDetector detector = FaceDetection.getClient(options);

        Task<List<Face>> result = detector.process(image)
            .addOnSuccessListener(
                new OnSuccessListener<List<Face>>() {
                    @Override
                    public void onSuccess(List<Face> faces) {
                        getFaceResult(faces);
                    }
                })
            .addOnFailureListener(
                new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(
                                MainActivity.this,
                                "Error:" + e.getMessage(),
                                Toast.LENGTH_SHORT
                        ).show();
                    }
                });
    }

    private void getFaceResult(List<Face> faces) {
        cameraDetectImageView.setImageBitmap(null);
        int counter = 0;
        for (Face face : faces) {
            Rect rect = face.getBoundingBox();
            ReactOverlay reactOverlay = new ReactOverlay(graphicOverlay, rect);

            graphicOverlay.add(reactOverlay);

            counter = counter + 1;

            int width = cameraDetectImageView.getWidth();
            int height = cameraDetectImageView.getHeight();
            Bitmap bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888);
            Canvas canvas = new Canvas(bitmap);

            Paint paint = new Paint();
            paint.setColor(Color.RED);
            paint.setStyle(Paint.Style.STROKE);
            paint.setStrokeWidth(4.0f);

            Paint paintG = new Paint();
            paintG.setColor(Color.GREEN);
            paintG.setStyle(Paint.Style.STROKE);
            paintG.setStrokeWidth(4.0f);

            // Landmarks
            FaceLandmark leftEar = face.getLandmark(FaceLandmark.LEFT_EAR);
            if (leftEar != null) {
                PointF leftEarPos = leftEar.getPosition();
                canvas.drawCircle(leftEarPos.x, leftEarPos.y, 4.0f,paint);
            }
            FaceLandmark rightEar = face.getLandmark(FaceLandmark.RIGHT_EAR);
            if (rightEar != null) {
                PointF rightEarPos = rightEar.getPosition();
                canvas.drawCircle(rightEarPos.x, rightEarPos.y, 4.0f,paint);
            }

            FaceLandmark leftEye = face.getLandmark(FaceLandmark.LEFT_EYE);
            if (leftEye != null) {
                PointF leftEyePos = leftEye.getPosition();
                canvas.drawCircle(leftEyePos.x, leftEyePos.y, 4.0f,paint);
            }

            FaceLandmark rightEye = face.getLandmark(FaceLandmark.RIGHT_EYE);
            if (leftEye != null) {
                PointF rightEyePos = rightEye.getPosition();
                canvas.drawCircle(rightEyePos.x, rightEyePos.y, 4.0f,paint);
            }

            FaceLandmark nose = face.getLandmark(FaceLandmark.NOSE_BASE);
            if (nose != null) {
                PointF nosePos = nose.getPosition();
                canvas.drawCircle(nosePos.x, nosePos.y, 4.0f,paint);
            }

            FaceLandmark leftMouth = face.getLandmark(FaceLandmark.MOUTH_LEFT);
            if (leftMouth != null) {
                PointF leftMouthPos = leftMouth.getPosition();
                canvas.drawCircle(leftMouthPos.x, leftMouthPos.y, 4.0f,paint);
            }

            FaceLandmark rightMouth = face.getLandmark(FaceLandmark.MOUTH_RIGHT);
            if (rightMouth != null) {
                PointF rightMouthPos = rightMouth.getPosition();
                canvas.drawCircle(rightMouthPos.x, rightMouthPos.y, 4.0f,paint);
            }

            FaceLandmark bottomMouth = face.getLandmark(FaceLandmark.MOUTH_BOTTOM);
            if (bottomMouth != null) {
                PointF bottomMouthPos = bottomMouth.getPosition();
                canvas.drawCircle(bottomMouthPos.x, bottomMouthPos.y, 4.0f,paint);
            }

            // Contours
            List<PointF> faceContours = face.getContour(FaceContour.FACE).getPoints();
            if (faceContours.size() > 0) {
                for (PointF facePos : faceContours) {
                    canvas.drawCircle(facePos.x, facePos.y, 4.0f,paintG);
                }
            }

            List<PointF> noseBridgeContours = face.getContour(FaceContour.NOSE_BRIDGE).getPoints();
            if (noseBridgeContours.size() > 0) {
                int index = 0;
                for (PointF nosePos : noseBridgeContours) {
                    if (index + 1 != noseBridgeContours.size()) {
                        canvas.drawLine(
                                nosePos.x,
                                nosePos.y,
                                noseBridgeContours.get(index + 1).x,
                                noseBridgeContours.get(index + 1).y,
                                paintG);
                    } else {
                        canvas.drawCircle(nosePos.x, nosePos.y, 4.0f,paintG);
                    }
                    index += 1;
                }
            }

            List<PointF> noseBottomContours = face.getContour(FaceContour.NOSE_BOTTOM).getPoints();
            if (noseBottomContours.size() > 0) {
                int index = 0;
                for (PointF noseBottomPos : noseBottomContours) {
                    if (index + 1 != noseBottomContours.size()) {
                        canvas.drawLine(
                                noseBottomPos.x,
                                noseBottomPos.y,
                                noseBottomContours.get(index + 1).x,
                                noseBottomContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }

            List<PointF> rightEyeBrowTopContours = face.getContour(FaceContour.RIGHT_EYEBROW_TOP).getPoints();
            if (rightEyeBrowTopContours.size() > 0) {
                int index = 0;
                for (PointF rightEyePos : rightEyeBrowTopContours) {
                    if (index + 1 != rightEyeBrowTopContours.size()) {
                        canvas.drawLine(
                                rightEyePos.x,
                                rightEyePos.y,
                                rightEyeBrowTopContours.get(index + 1).x,
                                rightEyeBrowTopContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }

            List<PointF> rightEyeBrowBottomContours = face.getContour(FaceContour.RIGHT_EYEBROW_BOTTOM).getPoints();
            if (rightEyeBrowBottomContours.size() > 0) {
                int index = 0;
                for (PointF rightEyePos : rightEyeBrowBottomContours) {
                    if (index + 1 != rightEyeBrowBottomContours.size()) {
                        canvas.drawLine(
                                rightEyePos.x,
                                rightEyePos.y,
                                rightEyeBrowBottomContours.get(index + 1).x,
                                rightEyeBrowBottomContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }

            List<PointF> leftEyeBrowTopContours = face.getContour(FaceContour.LEFT_EYEBROW_TOP).getPoints();
            if (leftEyeBrowTopContours.size() > 0) {
                int index = 0;
                for (PointF leftEyePos : leftEyeBrowTopContours) {
                    if (index + 1 != leftEyeBrowTopContours.size()) {
                        canvas.drawLine(
                                leftEyePos.x,
                                leftEyePos.y,
                                leftEyeBrowTopContours.get(index + 1).x,
                                leftEyeBrowTopContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }

            List<PointF> leftEyeBrowBottomContours = face.getContour(FaceContour.LEFT_EYEBROW_BOTTOM).getPoints();
            if (leftEyeBrowBottomContours.size() > 0) {
                int index = 0;
                for (PointF leftEyePos : leftEyeBrowBottomContours) {
                    if (index + 1 != leftEyeBrowBottomContours.size()) {
                        canvas.drawLine(
                                leftEyePos.x,
                                leftEyePos.y,
                                leftEyeBrowBottomContours.get(index + 1).x,
                                leftEyeBrowBottomContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }

            List<PointF> upperLipTopContours = face.getContour(FaceContour.UPPER_LIP_TOP).getPoints();
            if (upperLipTopContours.size() > 0) {
                int index = 0;
                for (PointF lipPos : upperLipTopContours) {
                    if (index + 1 != upperLipTopContours.size()) {
                        canvas.drawLine(
                                lipPos.x,
                                lipPos.y,
                                upperLipTopContours.get(index + 1).x,
                                upperLipTopContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }

            List<PointF> upperLipBottomContours = face.getContour(FaceContour.UPPER_LIP_BOTTOM).getPoints();
            if (upperLipBottomContours.size() > 0) {
                int index = 0;
                for (PointF lipPos : upperLipBottomContours) {
                    if (index + 1 != upperLipBottomContours.size()) {
                        canvas.drawLine(
                                lipPos.x,
                                lipPos.y,
                                upperLipBottomContours.get(index + 1).x,
                                upperLipBottomContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }

            List<PointF> lowerLipTopContours = face.getContour(FaceContour.LOWER_LIP_TOP).getPoints();
            if (lowerLipTopContours.size() > 0) {
                int index = 0;
                for (PointF lipPos : lowerLipTopContours) {
                    if (index + 1 != lowerLipTopContours.size()) {
                        canvas.drawLine(
                                lipPos.x,
                                lipPos.y,
                                lowerLipTopContours.get(index + 1).x,
                                lowerLipTopContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }

            List<PointF> lowerLipBottomContours = face.getContour(FaceContour.LOWER_LIP_BOTTOM).getPoints();
            if (lowerLipBottomContours.size() > 0) {
                int index = 0;
                for (PointF lipPos : lowerLipBottomContours) {
                    if (index + 1 != lowerLipBottomContours.size()) {
                        canvas.drawLine(
                                lipPos.x,
                                lipPos.y,
                                lowerLipBottomContours.get(index + 1).x,
                                lowerLipBottomContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }

            List<PointF> leftEyeContours = face.getContour(FaceContour.LEFT_EYE).getPoints();
            if (leftEyeContours.size() > 0) {
                int index = 0;
                for (PointF eyePos : leftEyeContours) {
                    if (index + 1 != leftEyeContours.size()) {
                        canvas.drawLine(
                                eyePos.x,
                                eyePos.y,
                                leftEyeContours.get(index + 1).x,
                                leftEyeContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }
            List<PointF> rightEyeContours = face.getContour(FaceContour.RIGHT_EYE).getPoints();
            if (rightEyeContours.size() > 0) {
                int index = 0;
                for (PointF eyePos : rightEyeContours) {
                    if (index + 1 != rightEyeContours.size()) {
                        canvas.drawLine(
                                eyePos.x,
                                eyePos.y,
                                rightEyeContours.get(index + 1).x,
                                rightEyeContours.get(index + 1).y,
                                paintG);
                    }
                    index += 1;
                }
            }
            cameraDetectImageView.setImageBitmap(bitmap);
        }

        Log.e(TAG, counter + "");
        alertDialog.dismiss();
    }

    @Override
    protected void onPause() {
        super.onPause();

        cameraView.stop();
    }

    @Override
    protected void onResume() {
        super.onResume();

        cameraView.start();
    }
}