package com.expensemanager.app.profile;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.expensemanager.app.R;
import com.expensemanager.app.main.BaseActivity;
import com.expensemanager.app.models.User;
import com.expensemanager.app.service.SyncUser;

import bolts.Continuation;
import bolts.Task;
import butterknife.BindView;
import butterknife.ButterKnife;

/**
 * Created by Zhaolong Zhong on 8/22/16.
 */

public class ProfileActivity extends BaseActivity {
    private static final String TAG = ProfileActivity.class.getSimpleName();

    private static final String USER_ID = "userId";

    private User currentUser;

    @BindView(R.id.toolbar_id) Toolbar toolbar;
    @BindView(R.id.toolbar_back_image_view_id) ImageView backImageView;
    @BindView(R.id.toolbar_title_text_view_id) TextView titleTextView;
    @BindView(R.id.profile_activity_profile_photo_image_view_id) ImageView profilePhotoImageView;
    @BindView(R.id.profile_activity_fullname_text_view_id) TextView fullnameTextView;

    public static void newInstance(Context context, String userId) {
        Intent intent = new Intent(context, ProfileActivity.class);
        intent.putExtra(USER_ID, userId);
        context.startActivity(intent);
        ((Activity)context).overridePendingTransition(R.anim.right_in, R.anim.left_out);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.profile_activity);
        ButterKnife.bind(this);

        SharedPreferences sharedPreferences = getSharedPreferences(getString(R.string.shared_preferences_session_key), 0);
        String loginUserId = sharedPreferences.getString(User.USER_ID, null);

        String userId = getIntent().getStringExtra(USER_ID);

        if (userId == null || userId.isEmpty()) {
            userId = loginUserId;
        }

        currentUser = User.getUserById(userId);

        if (currentUser != null) {
            Log.d(TAG, "current user name: " + currentUser.getFullname());
        }

        setupToolbar();

        invalidateViews();

        SyncUser.getLoginUser().continueWith(onResponseReturned, Task.UI_THREAD_EXECUTOR);
    }

    private void invalidateViews() {
        if (currentUser == null) {
            return;
        }

        Glide.with(this)
                .load(currentUser.getPhotoUrl())
                .fitCenter()
                .into(profilePhotoImageView);
        fullnameTextView.setText(currentUser.getFullname());
    }

    private void setupToolbar() {
        toolbar.setContentInsetsAbsolute(0,0);
        setSupportActionBar(toolbar);
        ActionBar actionBar = getSupportActionBar();

        if (actionBar != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(false);
        }

        titleTextView.setText(getString(R.string.profile));
        titleTextView.setOnClickListener(v -> close());
        backImageView.setOnClickListener(v -> close());
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.profile_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_item_update_photo:
                // todo: update user photo
                return true;
        }

        return super.onOptionsItemSelected(item);
    }

    private Continuation<Void, Void> onResponseReturned = new Continuation<Void, Void>() {
        @Override
        public Void then(Task<Void> task) throws Exception {
            if (task.isFaulted()) {
                Log.e(TAG, task.getError().toString());
                return null;
            }

            invalidateViews();
            return null;
        }
    };
}