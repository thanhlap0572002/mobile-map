package com.pethoalpar.osmdroidexmple;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.media.MediaPlayer;
import android.os.Handler;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.osmdroid.views.MapView;
import org.osmdroid.views.overlay.Overlay;

public class AlertOverlay extends Overlay {
    private Context context;
    private Location location;
    private Dialog dialog;
    private Handler handler;
    private DatabaseReference mDatabase;
    private boolean isFlashing;

    private Button alertButton;
    private int backgroundColor = Color.RED;
    private boolean isInitialAlertShown = false;
    private DatabaseReference databaseReference;


    public AlertOverlay(Context context, Location location) {
        super(context);
        this.context = context;
        this.location = location;
        handler = new Handler();

    }


    @Override
    public void draw(Canvas canvas, MapView mapView, boolean shadow) {
        super.draw(canvas, mapView, shadow);

        // Kiểm tra và hiển thị cửa sổ popup khi có cảnh báo
        if (isAlertNeeded() && isInitialAlertShown) {
            showDialog();
            startFlashingScreen();
        } else {
            dismissDialog();
            stopFlashingScreen();
        }

        // Đặt giá trị isInitialAlertShown thành true sau lần đầu tiên vẽ overlay
        isInitialAlertShown = true;
    }

    private boolean isAlertNeeded() {
        // Kiểm tra điều kiện cảnh báo ở đây
        // Ví dụ: nếu vị trí hiện tại gần với vị trí cảnh báo
        // Nếu cửa sổ popup đang hiển thị, trả về false để không hiển thị cảnh báo mới
        if (dialog != null && dialog.isShowing()) {
            return false;
        }

        // Truy vấn giá trị từ Firebase và kiểm tra điều kiện cảnh báo
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("alert").child("Khoảng cách đến điểm an toàn");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {

                if (dataSnapshot.exists()) {
                    Float distanceValue = dataSnapshot.getValue(Float.class);
                    if (distanceValue != null && distanceValue < 20) {

                        showDialog();
                    }

                    else if (distanceValue != null && distanceValue < 5) {
                            showDialog();
                        }
                    }
                }
            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                // Xử lý khi có lỗi xảy ra trong quá trình truy cập Firebase
            }
        });

        // Trả về true nếu cần hiển thị cảnh báo, ngược lại trả về false
//        return dialog == null;
        return true;

    }

    public void updatePopupText(String text) {
        if (dialog != null && dialog.isShowing()) {
            TextView textView = dialog.findViewById(R.id.sau);
            textView.setText(text);
        }
    }

    public void showDialog() {

        // Khởi tạo Dialog
        dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setContentView(R.layout.popup_layout);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.getWindow().setLayout(1000, 1500);
        dialog.getWindow().setGravity(Gravity.CENTER);



        mDatabase = FirebaseDatabase.getInstance().getReference();


        mDatabase.child("alert").child("Khoảng cách đến điểm an toàn").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Float doubleValue = dataSnapshot.getValue(Float.class);
                        String khoangCach = doubleValue.toString();
//                String khoangCach = dataSnapshot.getValue(String.class);
                        TextView distanceTextView = dialog.findViewById(R.id.distance);
                        distanceTextView.setText(khoangCach);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        });

        mDatabase.child("alert").child("Vị trí hiện tại").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Double doubleValue = dataSnapshot.getValue(Double.class);
//                String viTriHienTai = doubleValue.toString();
                String viTriHienTai = dataSnapshot.getValue(String.class);
                TextView vtriTextView = dialog.findViewById(R.id.inside);
                vtriTextView.setText(viTriHienTai);

            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        });
        mDatabase.child("alert").child("Điểm an toàn").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Double doubleValue = dataSnapshot.getValue(Double.class);
//                String viTriHienTai = doubleValue.toString();
                String diemAnToan = dataSnapshot.getValue(String.class);
                TextView diemAnToanTextView = dialog.findViewById(R.id.pointSave);
                diemAnToanTextView.setText(diemAnToan);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        });

        mDatabase.child("Weather Current").child("Nhiệt độ").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer doubleValue = dataSnapshot.getValue(Integer.class);
                String nhietDo = doubleValue.toString();
//                String nhietDo = dataSnapshot.getValue(String.class);
                TextView nhietDoTextView = dialog.findViewById(R.id.temp);
                nhietDoTextView.setText(nhietDo);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        });

        mDatabase.child("Weather Current").child("Thời tiết").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
//                Double doubleValue = dataSnapshot.getValue(Double.class);
//                String thoiTiet = doubleValue.toString();
                String thoiTiet = dataSnapshot.getValue(String.class);
                TextView thoiTietTextView = dialog.findViewById(R.id.weather);
                thoiTietTextView.setText(thoiTiet);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        });

        mDatabase.child("Weather Current").child("Tầm nhìn").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer doubleValue = dataSnapshot.getValue(Integer.class);
                String tamNhin = doubleValue.toString();
//                String tamNhin = dataSnapshot.getValue(String.class);
                TextView tamNhinTextView = dialog.findViewById(R.id.view);
                tamNhinTextView.setText(tamNhin);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        });

        mDatabase.child("Weather Current").child("Tốc độ gió").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                Integer doubleValue = dataSnapshot.getValue(Integer.class);
                String tocDoGio = doubleValue.toString();
//                String tocDoGio = dataSnapshot.getValue(String.class);
                TextView tocDoGioTextView = dialog.findViewById(R.id.speedWind);
                tocDoGioTextView.setText(tocDoGio);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                // Xử lý khi có lỗi xảy ra
            }
        });


        // Phát âm thanh
        MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.music);
        mediaPlayer.start();

        TextView is_inside = dialog.findViewById(R.id.inside);
        TextView nearest_distance = dialog.findViewById(R.id.distance);
        TextView nearest_point = dialog.findViewById(R.id.pointSave);

        Button closeButton = dialog.findViewById(R.id.button);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismissDialog();
            }
        });


        dialog.show();
    }

    private void dismissDialog() {
        if (dialog != null && dialog.isShowing()) {
            // Dừng phát âm thanh
            MediaPlayer mediaPlayer = MediaPlayer.create(context, R.raw.music);
            mediaPlayer.stop();
            mediaPlayer.release();

            dialog.dismiss();
            dialog = null;
        }
    }

    public void startFlashingScreen() {
        if (!isFlashing) {
            isFlashing = true;
            handler.post(flashRunnable);
        }
    }

    private void stopFlashingScreen() {
        if (isFlashing) {
            isFlashing = false;
            handler.removeCallbacks(flashRunnable);
            resetBackgroundColor();
        }
    }

    private void resetBackgroundColor() {
        Activity activity = (Activity) context;
        activity.runOnUiThread(new Runnable() {
            @Override
            public void run() {
                View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
                rootView.setBackgroundColor(Color.WHITE);
            }
        });
    }

    private Runnable flashRunnable = new Runnable() {
        private int backgroundColor = Color.RED;

        @Override
        public void run() {
            Activity activity = (Activity) context;
            View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            Drawable background = rootView.getBackground();
            if (background instanceof ColorDrawable) {
                int currentColor = ((ColorDrawable) background).getColor();
                if (currentColor == backgroundColor) {
                    resetBackgroundColor();
                } else {
                    setBackgroundColor(backgroundColor);
                }
            }
            handler.postDelayed(this, 500);
        }

        private void setBackgroundColor(int color) {
            Activity activity = (Activity) context;
            View rootView = activity.getWindow().getDecorView().findViewById(android.R.id.content);
            rootView.setBackgroundColor(color);
        }
    };
}
