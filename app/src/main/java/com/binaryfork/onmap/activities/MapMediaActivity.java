package com.binaryfork.onmap.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.speech.RecognizerIntent;
import android.support.v4.widget.DrawerLayout;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.DatePicker;
import android.widget.ListView;
import android.widget.TextView;

import com.binaryfork.onmap.R;
import com.binaryfork.onmap.clustering.MediaClusterItem;
import com.binaryfork.onmap.mvp.MapMediaView;
import com.binaryfork.onmap.mvp.MarkersViewImplementation;
import com.binaryfork.onmap.mvp.ModelImplementation;
import com.binaryfork.onmap.mvp.PresenterImplementation;
import com.binaryfork.onmap.network.ApiSource;
import com.binaryfork.onmap.ui.ClusterGridView;
import com.binaryfork.onmap.ui.LocationSearchBox;
import com.binaryfork.onmap.ui.MediaContainerView;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.maps.android.clustering.Cluster;

import java.util.ArrayList;
import java.util.Calendar;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import timber.log.Timber;

public class MapMediaActivity extends AbstractLocationActivity implements
        GoogleMap.OnMapClickListener,
        MapMediaView {

    @InjectView(R.id.date) TextView dateTxt;
    @InjectView(R.id.searchbox) LocationSearchBox searchBox;
    @InjectView(R.id.drawer_layout) DrawerLayout drawerLayout;
    @InjectView(R.id.left_drawer) ListView drawerList;
    @InjectView(R.id.info_layout) View mediaContainerLayout;
    @InjectView(R.id.gridView) ClusterGridView gridView;

    private GoogleMap map;
    private PresenterImplementation presenter;
    private MarkersViewImplementation view;
    private MediaContainerView mediaContainerView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setUpMapIfNeeded();
        ButterKnife.inject(this);
        drawerList.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_list_item_1,
                getResources().getStringArray(R.array.api_sources)));
        drawerList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 0:
                        presenter.apiSource = ApiSource.INSTAGRAM;
                        break;
                    case 1:
                        presenter.apiSource = ApiSource.FLICKR;
                        break;
                }
            }
        });

        ModelImplementation model = new ModelImplementation();
        view = new MarkersViewImplementation(map, this, this);
        presenter = new PresenterImplementation(model, view, this, getApplicationContext());
        presenter.toCurrentTime();
        searchBox.setup(this);

        mediaContainerView = new MediaContainerView(mediaContainerLayout, map, this);

        dateTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                presenter.backInTime();
            }
        });
        if (location != null) {
            goToLocation(location);
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        presenter.onDestroy();
    }

    @Override
    public void openPhoto(MediaClusterItem clusterTargetItem) {
        mediaContainerView.onPhotoOpen(clusterTargetItem);
    }

    @Override
    public void clickPhotoCluster(Cluster<MediaClusterItem> cluster) {
        showPhotoGrid(cluster);
    }

    private void showPhotoGrid(Cluster<MediaClusterItem> cluster) {
        gridView.setVisibility(View.VISIBLE);
        gridView.setupData(cluster);
    }

    private void hidePhotoGrid() {
        gridView.setVisibility(View.GONE);
    }

    @Override
    public void onMenuClick() {
        if (drawerLayout.isDrawerOpen(drawerList))
            drawerLayout.closeDrawer(drawerList);
        else
            drawerLayout.openDrawer(drawerList);
    }

    @Override
    public void showTime(String time) {
        dateTxt.setText(time);
    }

    @Override
    public void goToLocation(LatLng latLng) {
        if (latLng == null)
            return;
        presenter.toCurrentTime();
        location = latLng;
        CameraUpdate cameraUpdate = CameraUpdateFactory.newLatLngZoom(location, 14);
        map.animateCamera(cameraUpdate);
        presenter.getMedia(location);
    }

    private void setUpMapIfNeeded() {
        if (map == null) {
            map = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            map.setOnMapClickListener(this);
            map.setMyLocationEnabled(true);
            map.getUiSettings().setZoomControlsEnabled(true);
        }
    }

    @Override
    protected void onLocationReceived(LatLng location) {
        Timber.i("location %s", location);
        goToLocation(location);
    }

    @Override
    public void onMapClick(LatLng latLng) {
        goToLocation(latLng);
    }

    @OnClick(R.id.calendar)
    void datePicker() {
        final Calendar calendar = Calendar.getInstance();
        new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int monthOfYear, int dayOfMonth) {
                calendar.set(Calendar.YEAR, year);
                calendar.set(Calendar.MONTH, monthOfYear);
                calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
                calendar.set(Calendar.HOUR_OF_DAY, 23);
                calendar.set(Calendar.MINUTE, 59);
                calendar.set(Calendar.SECOND, 59);
                presenter.setTime(calendar.getTimeInMillis() / 1000);
            }
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show();
    }

    // Required by SearchBox.
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == 1234 && resultCode == RESULT_OK) {
            ArrayList<String> matches = data
                    .getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS);
            searchBox.populateEditText(matches);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    // Required by SearchBox.
    public void mic(View v) {
        searchBox.micClick(this);
    }

    @Override
    public void onBackPressed() {
        if (mediaContainerLayout.isShown()) {
            mediaContainerView.hideMediaInfo();
        } else if (gridView.isShown()) {
            hidePhotoGrid();
        } else {
            super.onBackPressed();
        }
    }
}