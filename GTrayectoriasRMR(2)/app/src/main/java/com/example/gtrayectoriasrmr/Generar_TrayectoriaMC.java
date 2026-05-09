package com.example.gtrayectoriasrmr;

import android.content.Context;
import android.content.Intent;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Bundle;
import android.util.AttributeSet;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.example.gtrayectoriasrmr.MobileAnarchy.JoystickMovedListener;
import com.example.gtrayectoriasrmr.MobileAnarchy.JoystickView;

import java.io.File;
import java.io.FileOutputStream;
import java.util.ArrayList;

public class Generar_TrayectoriaMC extends AppCompatActivity {

    private static final long DRAW_RATE_MS = 16; // Approx 60 FPS
    private static final float JOYSTICK_SENSITIVITY = 6.0f;

    private SignatureView mSignatureView;
    private Button mBtnSave;
    private EditText mTrajectoryNameEditText;

    private float mJoystickX, mJoystickY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_generar_trayectoria_mc);

        // --- UI Setup ---
        LinearLayout drawingContent = findViewById(R.id.linearLayout);
        Button btnClear = findViewById(R.id.limp);
        mBtnSave = findViewById(R.id.save);
        Button btnCancel = findViewById(R.id.cancel);
        mTrajectoryNameEditText = findViewById(R.id.nomtrayect);
        JoystickView joystick = findViewById(R.id.joystickView); // The only joystick we need

        // Hide the second joystick as it's not used
        findViewById(R.id.joystickView2).setVisibility(View.GONE);
        findViewById(R.id.TextViewX).setVisibility(View.GONE);
        findViewById(R.id.TextViewY).setVisibility(View.GONE);

        // --- Drawing Pad Setup ---
        mSignatureView = new SignatureView(this, null);
        mSignatureView.setBackgroundColor(Color.WHITE);
        drawingContent.addView(mSignatureView, ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        mBtnSave.setEnabled(false);

        // --- Listeners ---
        joystick.setOnJostickMovedListener(mJoystickListener);

        btnClear.setOnClickListener(v -> {
            mSignatureView.clear();
            mBtnSave.setEnabled(false);
        });
        mBtnSave.setOnClickListener(v -> saveSignature());
        btnCancel.setOnClickListener(v -> finish());
    }

    private final JoystickMovedListener mJoystickListener = new JoystickMovedListener() {
        @Override
        public void OnMoved(int pan, int tilt) {
            mSignatureView.setSaveEnabled();
            mJoystickX = pan; // Controls X-axis
            mJoystickY = tilt; // Controls Y-axis (inverted for natural feel)
        }

        @Override
        public void OnReleased() {
            mJoystickX = 0;
            mJoystickY = 0;
        }

        @Override
        public void OnReturnedToCenter() {
            OnReleased();
        }
    };

    private void saveSignature() {
        String trajectoryName = mTrajectoryNameEditText.getText().toString().trim();
        if (trajectoryName.isEmpty()) {
            Toast.makeText(this, "Por favor, ingresa un nombre para la trayectoria.", Toast.LENGTH_SHORT).show();
            return;
        }

        File file = new File(getFilesDir(), trajectoryName + ".txt");
        if (file.exists()) {
            Toast.makeText(this, "Ya existe una trayectoria con ese nombre. Elige otro.", Toast.LENGTH_LONG).show();
            return;
        }

        mSignatureView.saveAndShareTrajectory(trajectoryName);
        finish();
    }

    // --- Inner Drawing Class ---
    public class SignatureView extends View {
        private final Paint mPaint = new Paint();
        private final Path mPath = new Path();
        private final ArrayList<Float> mPointsX = new ArrayList<>();
        private final ArrayList<Float> mPointsY = new ArrayList<>();
        private float mCurrentX, mCurrentY;
        private boolean mIsDrawingByTouch = false;

        public SignatureView(Context context, AttributeSet attrs) {
            super(context, attrs);
            mPaint.setAntiAlias(true);
            mPaint.setColor(Color.BLACK);
            mPaint.setStyle(Paint.Style.STROKE);
            mPaint.setStrokeJoin(Paint.Join.ROUND);
            mPaint.setStrokeWidth(10f);
        }

        public void setSaveEnabled(){
            if(!mBtnSave.isEnabled()){
                mBtnSave.setEnabled(true);
            }
        }

        public void saveAndShareTrajectory(String trajectoryName) {
            String filename = trajectoryName + ".txt";
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mPointsX.size(); i++) {
                sb.append(mPointsX.get(i)).append(",").append(mPointsY.get(i)).append("\n");
            }
            String trajectoryData = sb.toString();

            try (FileOutputStream fos = openFileOutput(filename, Context.MODE_PRIVATE)) {
                fos.write(trajectoryData.getBytes());
                Toast.makeText(getContext(), "Trayectoria Guardada como: " + filename, Toast.LENGTH_LONG).show();

                // --- Share Intent ---
                Intent shareIntent = new Intent(Intent.ACTION_SEND);
                shareIntent.setType("text/plain");
                shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Trayectoria: " + trajectoryName);
                shareIntent.putExtra(Intent.EXTRA_TEXT, trajectoryData);
                getContext().startActivity(Intent.createChooser(shareIntent, "Compartir Trayectoria"));

            } catch (Exception e) {
                Log.e("SaveError", "Error saving or sharing trajectory file", e);
                Toast.makeText(getContext(), "Error al guardar o compartir", Toast.LENGTH_LONG).show();
            }
        }

        public void clear() {
            mPath.reset();
            mPointsX.clear();
            mPointsY.clear();
            mCurrentX = 0;
            mCurrentY = 0;
            invalidate();
        }

        private void updateDrawingWithJoystick() {
            if (mJoystickX != 0 || mJoystickY != 0) {
                if (mPath.isEmpty()) {
                    mCurrentX = getWidth() / 2.0f;
                    mCurrentY = getHeight() / 2.0f;
                    mPath.moveTo(mCurrentX, mCurrentY);
                }

                float dx = (mJoystickX / 100.0f) * JOYSTICK_SENSITIVITY;
                float dy = (mJoystickY / 100.0f) * JOYSTICK_SENSITIVITY;

                mCurrentX += dx;
                mCurrentY += dy;

                mCurrentX = Math.max(0, Math.min(getWidth(), mCurrentX));
                mCurrentY = Math.max(0, Math.min(getHeight(), mCurrentY));

                mPath.lineTo(mCurrentX, mCurrentY);
                mPointsX.add(mCurrentX);
                mPointsY.add(mCurrentY);
            }
        }

        @Override
        protected void onDraw(Canvas canvas) {
            super.onDraw(canvas);
            if(!mIsDrawingByTouch){
                updateDrawingWithJoystick();
            }
            canvas.drawPath(mPath, mPaint);
            postInvalidateDelayed(DRAW_RATE_MS);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            setSaveEnabled();
            float eventX = event.getX();
            float eventY = event.getY();

            mCurrentX = eventX;
            mCurrentY = eventY;

            switch (event.getAction()) {
                case MotionEvent.ACTION_DOWN:
                    mIsDrawingByTouch = true;
                    mPath.moveTo(eventX, eventY);
                    mPointsX.add(eventX);
                    mPointsY.add(eventY);
                    return true;
                case MotionEvent.ACTION_MOVE:
                    mPath.lineTo(eventX, eventY);
                    mPointsX.add(eventX);
                    mPointsY.add(eventY);
                    break;
                case MotionEvent.ACTION_UP:
                    mIsDrawingByTouch = false;
                    break;
                default:
                    return false;
            }
            invalidate();
            return true;
        }
    }
}