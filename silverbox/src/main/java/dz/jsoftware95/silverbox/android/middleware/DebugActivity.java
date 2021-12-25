package dz.jsoftware95.silverbox.android.middleware;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.os.PersistableBundle;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import org.jetbrains.annotations.NotNull;

public class DebugActivity extends BasicActivity {
    public final String TAG = "DEBUG@" + getClass().getSimpleName();

//    @Nullable
//    @Override
//    public View onCreateView(@Nullable View parent, @NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
//        Log.d(TAG, "onCreateView(View parent, String name, Context context, AttributeSet attrs) {");
//        View view = super.onCreateView(parent, name, context, attrs);
//        Log.d(TAG, "onCreateView(View parent, String name, Context context, AttributeSet attrs) }");
//        return view;
//    }
//
//    @Nullable
//    @Override
//    public View onCreateView(@NonNull String name, @NonNull Context context, @NonNull AttributeSet attrs) {
//        Log.d(TAG, "onCreateView(String name, Context context, AttributeSet attrs) {");
//        View view = super.onCreateView(name, context, attrs);
//        Log.d(TAG, "onCreateView(String name, Context context, AttributeSet attrs) }");
//        return view;
//    }

    @Override
    protected void onCreate(Bundle savedState) {
        Log.d(TAG, "onCreate(Bundle savedState) {");
        super.onCreate(savedState);
        Log.d(TAG, "onCreate(Bundle savedState) }");
    }

    @Override
    protected void onNewIntent(Intent intent) {
        Log.d(TAG, "onNewIntent(Intent intent) {");
        super.onNewIntent(intent);
        Log.d(TAG, "onNewIntent(Intent intent) }");
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart() {");
        super.onStart();
        Log.d(TAG, "onStart() }");
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause() {");
        super.onPause();
        Log.d(TAG, "onPause() }");
    }

    @Override
    protected void onSaveInstanceState(@NotNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState(Bundle outState) {");
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState(Bundle outState) }");
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop() {");
        super.onStop();
        Log.d(TAG, "onStop() }");
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy() {");
        super.onDestroy();
        Log.d(TAG, "onDestroy() }");
    }

    @Override
    public void exportState(@NonNull Intent intent) {
        Log.d(TAG, "exportState(Intent intent) {");
        super.exportState(intent);
        Log.d(TAG, "exportState(Intent intent) }");
    }

    @Override
    public void exportState(@NonNull Intent intent, @NonNull Class<? extends StateOwner> receiver) {
        Log.d(TAG, "exportState(Intent intent, Class receiver) {");
        super.exportState(intent, receiver);
        Log.d(TAG, "exportState(Intent intent, Class receiver) }");
    }

    @Override
    public void importState(@Nullable Intent intent) {
        Log.d(TAG, "importState(Intent intent) {");
        super.importState(intent);
        Log.d(TAG, "importState(Intent intent) }");
    }

    @Override
    public void importState(@Nullable Intent intent, @NonNull Class<? extends StateOwner> receiver) {
        Log.d(TAG, "importState(Intent intent, Class receiver) {");
        super.importState(intent, receiver);
        Log.d(TAG, "importState(Intent intent, Class receiver) }");
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onPostCreate(Bundle savedInstanceState) {");
        super.onPostCreate(savedInstanceState);
        Log.d(TAG, "onPostCreate(Bundle savedInstanceState) }");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged(Configuration newConfig) {");
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged(Configuration newConfig) }");
    }

    @Override
    protected void onPostResume() {
        Log.d(TAG, "onPostResume() {");
        super.onPostResume();
        Log.d(TAG, "onPostResume() }");
    }

    @Override
    public void onContentChanged() {
        Log.d(TAG, "onContentChanged() {");
        super.onContentChanged();
        Log.d(TAG, "onContentChanged() }");
    }

    @Override
    public void onMultiWindowModeChanged(boolean isInMultiWindowMode) {
        Log.d(TAG, "onMultiWindowModeChanged(boolean isInMultiWindowMode) {");
        super.onMultiWindowModeChanged(isInMultiWindowMode);
        Log.d(TAG, "onMultiWindowModeChanged(boolean isInMultiWindowMode) }");
    }

    @Override
    public void onStateNotSaved() {
        Log.d(TAG, "onStateNotSaved() {");
        super.onStateNotSaved();
        Log.d(TAG, "onStateNotSaved() }");
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume() {");
        super.onResume();
        Log.d(TAG, "onResume() }");
    }

    @Override
    protected void onResumeFragments() {
        Log.d(TAG, "onResumeFragments() {");
        super.onResumeFragments();
        Log.d(TAG, "onResumeFragments() }");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        Log.d(TAG, "onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {");
        super.onCreate(savedInstanceState, persistentState);
        Log.d(TAG, "onCreate(Bundle savedInstanceState, PersistableBundle persistentState) }");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        Log.d(TAG, "onRestoreInstanceState(savedInstanceState savedInstanceState) {");
        super.onRestoreInstanceState(savedInstanceState);
        Log.d(TAG, "onRestoreInstanceState(savedInstanceState savedInstanceState) }");
    }

    @Override
    public void onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {
        Log.d(TAG, "onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) {");
        super.onRestoreInstanceState(savedInstanceState, persistentState);
        Log.d(TAG, "onRestoreInstanceState(Bundle savedInstanceState, PersistableBundle persistentState) }");
    }

    @Override
    public void onPostCreate(@Nullable Bundle savedInstanceState, @Nullable PersistableBundle persistentState) {
        Log.d(TAG, "onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) {");
        super.onPostCreate(savedInstanceState, persistentState);
        Log.d(TAG, "onPostCreate(Bundle savedInstanceState, PersistableBundle persistentState) }");
    }

    @Override
    protected void onRestart() {
        Log.d(TAG, "onRestart() {");
        super.onRestart();
        Log.d(TAG, "onRestart() }");
    }

    @Override
    public void finish() {
        Log.d(TAG, "finish() {");
        super.finish();
        Log.d(TAG, "finish() }");
    }

    @Override
    public void finishAffinity() {
        Log.d(TAG, "finishAffinity() {");
        super.finishAffinity();
        Log.d(TAG, "finishAffinity() }");
    }

    @Override
    public void finishAfterTransition() {
        Log.d(TAG, "finishAfterTransition() {");
        super.finishAfterTransition();
        Log.d(TAG, "finishAfterTransition() }");
    }

    @Override
    public void finishActivity(int requestCode) {
        Log.d(TAG, "finishActivity(int requestCode) {");
        super.finishActivity(requestCode);
        Log.d(TAG, "finishActivity(int requestCode) }");
    }

    @Override
    public void finishAndRemoveTask() {
        Log.d(TAG, "finishAndRemoveTask() {");
        super.finishAndRemoveTask();
        Log.d(TAG, "finishAndRemoveTask() }");
    }
}
