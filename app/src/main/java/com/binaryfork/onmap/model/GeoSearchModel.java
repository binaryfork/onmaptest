package com.binaryfork.onmap.model;

import android.widget.TextView;

import com.binaryfork.onmap.model.google.GoogleGeo;
import com.binaryfork.onmap.model.google.model.GeocodeResults;
import com.google.android.gms.maps.model.LatLng;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.android.widget.OnTextChangeEvent;
import rx.android.widget.WidgetObservable;
import rx.functions.Action1;
import rx.functions.Func1;
import timber.log.Timber;

public class GeoSearchModel {

    public static void subscribe(TextView input, Action1<GeocodeResults> onComplete) {
        textChangedObservable(input)
               // .debounce(100, TimeUnit.MILLISECONDS)
                .switchMap(geocodeResults())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(onComplete);
    }

    private static Observable<String> textChangedObservable(TextView input) {
        return WidgetObservable
                .text(input)
                .map(new Func1<OnTextChangeEvent, String>() {
                    @Override
                    public String call(OnTextChangeEvent event) {
                        return event.text().toString().trim();
                    }
                });
    }

    private static Func1<String, Observable<GeocodeResults>> geocodeResults() {
        return new Func1<String, Observable<GeocodeResults>>() {
            @Override
            public Observable<GeocodeResults> call(String query) {
                if (query == null || query.length() < 3) {
                    return Observable.empty();
                }
                return GoogleGeo.getInstance()
                        .geo()
                        .locationByAddress(query);
            }
        };
    }

    public static Observable<String> addressByLocation(LatLng latLng) {
        String ll = latLng.latitude + "," + latLng.longitude;
        Timber.i("geo " + ll);
        return GoogleGeo.getInstance()
                .geo()
                .addressByLocation(ll)
                .flatMap(new Func1<GeocodeResults, Observable<String>>() {
                    @Override public Observable<String> call(final GeocodeResults geocodeResults) {
                        Timber.i("geo " + geocodeResults.results.get(0).formatted_address);
                        return Observable.create(new Observable.OnSubscribe<String>() {
                            @Override public void call(Subscriber<? super String> subscriber) {
                                String address = geocodeResults.results.get(0).formatted_address.replace("Unnamed Road, ", "");
                                subscriber.onNext(address);
                                subscriber.onCompleted();
                            }
                        });
                    }
                });
    }
}
