package com.binaryfork.onmap.view.map;

import android.content.Intent;
import android.graphics.Point;
import android.os.Bundle;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.TextView;

import com.binaryfork.onmap.MainPreferenceActivity;
import com.binaryfork.onmap.R;
import com.binaryfork.onmap.clustering.MediaClusterItem;
import com.binaryfork.onmap.model.Media;
import com.binaryfork.onmap.presenter.MediaMapPresenter;
import com.binaryfork.onmap.util.Intents;
import com.binaryfork.onmap.util.Prefs;
import com.binaryfork.onmap.util.Theme;
import com.binaryfork.onmap.view.map.ui.CalendarDialog;
import com.binaryfork.onmap.view.map.ui.DrawerList;
import com.binaryfork.onmap.view.map.ui.RangeSeekBar;
import com.binaryfork.onmap.view.mediaview.ClusterGridView;
import com.binaryfork.onmap.view.mediaview.MediaView;
import com.binaryfork.onmap.view.mediaview.MediaViewFragment;
import com.binaryfork.onmap.view.search.GeoSearchView;
import com.binaryfork.onmap.view.search.SearchFragment;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

public class MediaMapActivity extends AbstractLocationActivity implements
        MediaMapView, LocationState {

    @InjectView(R.id.date) TextView dateTxt;
    @InjectView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @InjectView(R.id.left_drawer) DrawerList drawerList;
    @InjectView(R.id.gridView) ClusterGridView gridView;
    @InjectView(R.id.rangeSeekBar) RangeSeekBar rangeSeekBar;

    private GoogleMap map;
    private MediaMapPresenter mediaMapPresenter;
    private MediaView mediaView;
    private GeoSearchView geoSearchView;
    private MediaMapFragment mediaMapFragment;

    @Override protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Theme.updateActivity(this);
        ButterKnife.inject(this);
        drawerList.setCallback(new DrawerList.OnDrawerItemClickListener() {
            @Override public void onClick(DrawerList.DrawerItem drawerItem) {
                if (drawerItem.apiSource != null) {
                    mediaMapPresenter.changeSource(drawerItem.apiSource);
                    loadMarkers();
                } else if (drawerItem.mapType != 0) {
                    map.setMapType(drawerItem.mapType);
                } else {
                    startActivity(new Intent(MediaMapActivity.this, MainPreferenceActivity.class));
                }
                drawerLayout.closeDrawer(drawerList);
            }
        });
        rangeSeekBar.setMediaMapView(this);
        setupRetainedFragment();
        if (savedInstanceState == null) {
            if (getLastLocation() == null)
                // If no previous location was saved get current location of the user.
                setupLocation();
            else
                // Go to last saved location.
                goToLocation(getLastLocation());
        }
    }

    @Override protected void onDestroy() {
        super.onDestroy();
        Theme.clear();
    }

    private void setupRetainedFragment() {
        FragmentManager fm = getSupportFragmentManager();

        mediaView = (MediaViewFragment) fm.findFragmentById(R.id.mediaFragment);
        gridView.mediaView = mediaView;

        mediaMapFragment = (MediaMapFragment) fm.findFragmentById(R.id.map);
        mediaMapFragment.setMediaMapView(this);
        mediaMapPresenter = mediaMapFragment.getMediaMapPresenter();
        if (map == null) {
            map = mediaMapFragment.getMap();
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
        }
        rangeSeekBar.setMapCircle(mediaMapFragment.getMapCircle());
        if (location == null)
            location = mediaMapPresenter.getLocation();
        geoSearchView = (SearchFragment) fm.findFragmentById(R.id.searchFragment);
        geoSearchView.setMediaMapView(this);
    }

    @Override public void openPhoto(Media media) {
        mediaView.openFromMap(media, null, new Point());
    }

    @Override public void openPhotoFromMap(MediaClusterItem clusterTargetItem) {
        Projection projection = map.getProjection();
        LatLng markerLocation = clusterTargetItem.getPosition();
        Point markerPosition = projection.toScreenLocation(markerLocation);
        mediaView.openFromMap(clusterTargetItem.media, clusterTargetItem.thumbBitmap, markerPosition);
    }

    @Override public void clickPhotoCluster(Cluster<MediaClusterItem> cluster) {
        showPhotoGrid(cluster);
    }

    private void showPhotoGrid(Cluster<MediaClusterItem> cluster) {
        gridView.setVisibility(View.VISIBLE);
        gridView.setupData(cluster);
    }

    private void hidePhotoGrid() {
        gridView.setVisibility(View.GONE);
    }

    @Override public void onMenuClick() {
        if (drawerLayout.isDrawerOpen(drawerList))
            drawerLayout.closeDrawer(drawerList);
        else
            drawerLayout.openDrawer(drawerList);
    }

    @Override public void showTime(String time) {
        dateTxt.setText(time);
    }

    @Override public void goToLocation(LatLng latLng) {
        if (latLng == null)
            return;
        location = latLng;
        saveLastLocation(location);
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 14);
        map.animateCamera(cameraUpdate);
        loadMarkers();
    }

    @Override public LatLng getLocation() {
        return location;
    }

    private void loadMarkers() {
        geoSearchView.showProgress(true);
        mediaMapPresenter.loadMedia(location);
    }

    @Override public void allMarkesLoaded() {
        geoSearchView.showProgress(false);
    }

    @Override public void setDistance(int distance) {
        mediaMapPresenter.setDistance(distance);
    }

    @Override public void showCenterMarker(int distance) {
        mediaMapFragment.showMapCircle(distance, location);
        rangeSeekBar.setMapCircle(mediaMapFragment.getMapCircle());
    }

    @Override protected void onLocationReceived(LatLng location) {
        Timber.i("location %s", location);
        goToLocation(location);
    }

    @OnClick(R.id.date) void datePicker() {
        CalendarDialog calendarDialog = new CalendarDialog();
        calendarDialog.onDateChangeListener = new CalendarDialog.OnDateChangeListener() {
            @Override public void onDateChanged(long min, long max) {
                mediaMapPresenter.setTime(min, max);
            }
        };
        calendarDialog.show(getSupportFragmentManager(), "date");
    }

    @OnClick(R.id.username) public void onClickUsername() {
        Intents.openLink(this, mediaView.getMedia().getSiteUrl());
    }

    @Override public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(drawerList)) {
            drawerLayout.closeDrawer(drawerList);
        } else if (mediaView.isShown()) {
            mediaView.hide();
        } else if (gridView.isShown()) {
            hidePhotoGrid();
        } else if (geoSearchView.isShown()) {
            geoSearchView.hide();
        } else {
            super.onBackPressed();
        }
    }

    @Override public LatLng getLastLocation() {
        return Prefs.isSaveLocation() ? Prefs.getLastLocation() : null;
    }

    @Override public void saveLastLocation(LatLng latLng) {
        Prefs.putLastLocation(latLng);
    }
}