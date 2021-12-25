package dz.jsoftware95.silverbox.android.middleware;

import android.content.Context;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class DebugFragment extends BasicFragment {
    public final String TAG = "DEBUG@" + getClass().getSimpleName();

    @Override
    public void onAttach(@NonNull Context context) {
        Log.d(TAG, "onAttach(Context context) {");
        super.onAttach(context);
        Log.d(TAG, "onAttach(Context context) }");
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedState) {
        Log.d(TAG, "onActivityCreated(Bundle savedState) {");
        super.onActivityCreated(savedState);
        Log.d(TAG, "onActivityCreated(Bundle savedState) }");
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause() {");
        super.onPause();
        Log.d(TAG, "onPause() }");
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        Log.d(TAG, "onSaveInstanceState(Bundle outState) {");
        super.onSaveInstanceState(outState);
        Log.d(TAG, "onSaveInstanceState(Bundle outState) }");
    }

    @Override
    public void onDestroyView() {
        Log.d(TAG, "onDestroyView() {");
        super.onDestroyView();
        Log.d(TAG, "onDestroyView() }");
    }

    @Override
    public void onDetach() {
        Log.d(TAG, "onDetach() {");
        super.onDetach();
        Log.d(TAG, "onDetach() }");
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
    public void onAttachFragment(@NonNull Fragment childFragment) {
        Log.d(TAG, "onAttachFragment(Fragment childFragment) {");
        super.onAttachFragment(childFragment);
        Log.d(TAG, "onAttachFragment(Fragment childFragment) }");
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate(Bundle savedInstanceState) {");
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate(Bundle savedInstanceState) }");
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {");
        View view = super.onCreateView(inflater, container, savedInstanceState);
        Log.d(TAG, "onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) }");
        return view;
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewCreated(View view, Bundle savedInstanceState) {");
        super.onViewCreated(view, savedInstanceState);
        Log.d(TAG, "onViewCreated(View view, Bundle savedInstanceState) }");
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onViewStateRestored(Bundle savedInstanceState) {");
        super.onViewStateRestored(savedInstanceState);
        Log.d(TAG, "onViewStateRestored(Bundle savedInstanceState) }");
    }

    @Override
    public void onStart() {
        Log.d(TAG, "onStart() {");
        super.onStart();
        Log.d(TAG, "onStart() }");
    }

    @Override
    public void onResume() {
        Log.d(TAG, "onResume() {");
        super.onResume();
        Log.d(TAG, "onResume() }");
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        Log.d(TAG, "onConfigurationChanged(Configuration newConfig) {");
        super.onConfigurationChanged(newConfig);
        Log.d(TAG, "onConfigurationChanged(Configuration newConfig) }");
    }

    @Override
    public void onStop() {
        Log.d(TAG, "onStop() {");
        super.onStop();
        Log.d(TAG, "onStop() }");
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "onDestroy() {");
        super.onDestroy();
        Log.d(TAG, "onDestroy() }");
    }

    @Override
    public void onCreateOptionsMenu(@NonNull Menu menu, @NonNull MenuInflater inflater) {
        Log.d(TAG, "onCreateOptionsMenu(Menu menu, MenuInflater inflater) {");
        super.onCreateOptionsMenu(menu, inflater);
        Log.d(TAG, "onCreateOptionsMenu(Menu menu, MenuInflater inflater) }");
    }

    @Override
    public void onPrepareOptionsMenu(@NonNull Menu menu) {
        Log.d(TAG, "onPrepareOptionsMenu(Menu menu) {");
        super.onPrepareOptionsMenu(menu);
        Log.d(TAG, "onPrepareOptionsMenu(Menu menu) }");
    }

    @Override
    public void onDestroyOptionsMenu() {
        Log.d(TAG, "onDestroyOptionsMenu() {");
        super.onDestroyOptionsMenu();
        Log.d(TAG, "onDestroyOptionsMenu() }");
    }

    @Override
    public void onCreateContextMenu(@NonNull ContextMenu menu, @NonNull View v, @Nullable ContextMenu.ContextMenuInfo menuInfo) {
        Log.d(TAG, "onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {");
        super.onCreateContextMenu(menu, v, menuInfo);
        Log.d(TAG, "onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) }");
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        Log.d(TAG, "onHiddenChanged(boolean hidden) {");
        super.onHiddenChanged(hidden);
        Log.d(TAG, "onHiddenChanged(boolean hidden) }");
    }

    @NonNull
    @Override
    public LayoutInflater onGetLayoutInflater(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onGetLayoutInflater(Bundle savedInstanceState) {");
        LayoutInflater layoutInflater = super.onGetLayoutInflater(savedInstanceState);
        Log.d(TAG, "onGetLayoutInflater(Bundle savedInstanceState) }");
        return layoutInflater;
    }

    @Override
    public void onLowMemory() {
        Log.d(TAG, "onLowMemory() {");
        super.onLowMemory();
        Log.d(TAG, "onLowMemory() }");
    }
}
