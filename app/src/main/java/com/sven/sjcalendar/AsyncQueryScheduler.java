package com.sven.sjcalendar;

import android.content.ContentProviderOperation;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.net.Uri;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicInteger;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * Created by zhimin
 * On date 17-5-19.
 */
public class AsyncQueryScheduler {
    private static final String TAG = "AsyncQueryScheduler";

    // Used for generating unique tokens for calls to this service
    private static AtomicInteger mUniqueToken = new AtomicInteger(0);

    private final WeakReference<ContentResolver> mResolver;

    public AsyncQueryScheduler(ContentResolver cr) {
        mResolver = new WeakReference<ContentResolver>(cr);
    }

    public int getNextToken() {
        return mUniqueToken.getAndIncrement();
    }

    public Disposable startQuery(int token, Uri uri, String[] projection, String selection, String[] selectionArgs,
                                 String orderBy, OperationCallback callback) {
        OperationInfo info = new OperationInfo();
        info.token = token;
        info.op = Operation.EVENT_ARG_QUERY;
        info.resolver = mResolver.get();

        info.callback = callback;
        info.uri = uri;
        info.projection = projection;
        info.selection = selection;
        info.selectionArgs = selectionArgs;
        info.orderBy = orderBy;

        return startOperation(info);
    }

    public Disposable startInsert(int token, Uri uri, ContentValues initialValues, OperationCallback callback) {
        OperationInfo info = new OperationInfo();
        info.token = token;
        info.op = Operation.EVENT_ARG_INSERT;
        info.resolver = mResolver.get();
        info.callback = callback;

        info.uri = uri;
        info.values = initialValues;

        return startOperation(info);
    }

    public Disposable startDelete(int token, Uri uri, String selection, String[] selectionArgs, OperationCallback callback) {
        OperationInfo info = new OperationInfo();
        info.token = token;
        info.op = Operation.EVENT_ARG_DELETE;
        info.resolver = mResolver.get();
        info.callback = callback;

        info.uri = uri;
        info.selection = selection;
        info.selectionArgs = selectionArgs;

        return startOperation(info);
    }

    public Disposable startUpdate(int token, Uri uri, ContentValues values, String selection, String[] selectionArgs,
                                  OperationCallback callback) {
        OperationInfo info = new OperationInfo();
        info.token = token;
        info.op = Operation.EVENT_ARG_UPDATE;
        info.resolver = mResolver.get();
        info.callback = callback;

        info.uri = uri;
        info.values = values;
        info.selection = selection;
        info.selectionArgs = selectionArgs;

        return startOperation(info);
    }

    public void startBatch(int token, String authority, ArrayList<ContentProviderOperation> cpo, OperationCallback callback) {
        OperationInfo info = new OperationInfo();
        info.token = token;
        info.op = Operation.EVENT_ARG_BATCH;
        info.resolver = mResolver.get();
        info.callback = callback;

        info.authority = authority;
        info.cpo = cpo;

        startOperation(info);
    }

    private Disposable startOperation(final OperationInfo info) {
        return Observable.create(
                new ObservableOnSubscribe<Object>() {
                    @Override
                    public void subscribe(ObservableEmitter<Object> emitter) throws Exception {
                        try {
                            emitter.onNext(executeOperation(info));
                        } catch (Exception e) {
                            emitter.onError(e);
                        } finally {
                            emitter.onComplete();
                        }
                    }
                })
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Object>() {
                    @Override
                    public void accept(Object o) throws Exception {
                        if (info.callback != null) {
                            info.callback.onSuccess(info.token, o);
                        }
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        if (info.callback != null) {
                            info.callback.onError(throwable);
                        }
                    }
                });
    }

    private Object executeOperation(OperationInfo info) {
        ContentResolver resolver = info.resolver;
        if (resolver != null) {
            switch (info.op) {
                case Operation.EVENT_ARG_QUERY:
                    Cursor cursor;
                    try {
                        cursor = resolver.query(info.uri, info.projection, info.selection,
                                info.selectionArgs, info.orderBy);
                        /*
                         * Calling getCount() causes the cursor window to be
                         * filled, which will make the first access on the main
                         * thread a lot faster
                         */
                        if (cursor != null) {
                            cursor.getCount();
                        }
                    } catch (Exception e) {
                        Log.e(TAG, "execute query operation failed, e -> " + e.getMessage());
                        cursor = null;
                    }
                    return cursor;
                case Operation.EVENT_ARG_INSERT:
                    return resolver.insert(info.uri, info.values);
                case Operation.EVENT_ARG_UPDATE:
                    return resolver.update(info.uri, info.values, info.selection,
                            info.selectionArgs);
                case Operation.EVENT_ARG_DELETE:
                    return resolver.delete(info.uri, info.selection, info.selectionArgs);
                case Operation.EVENT_ARG_BATCH:
                    try {
                        return resolver.applyBatch(info.authority, info.cpo);
                    } catch (Exception e) {
                        Log.e(TAG, "apply batch failed, e -> " + e.getMessage());
                    }
                    break;
            }
        }

        return null;
    }

    public static class OperationInfo {
        public int token;
        public int op;
        public ContentResolver resolver;
        public Uri uri;
        public String authority;
        public String[] projection;
        public String selection;
        public String[] selectionArgs;
        public String orderBy;
        public Object cookie;
        public ContentValues values;
        public OperationCallback callback;
        public ArrayList<ContentProviderOperation> cpo;
    }

    private static class Operation {
        static final int EVENT_ARG_QUERY = 1;
        static final int EVENT_ARG_INSERT = 2;
        static final int EVENT_ARG_UPDATE = 3;
        static final int EVENT_ARG_DELETE = 4;
        static final int EVENT_ARG_BATCH = 5;
    }

    public interface OperationCallback {
        void onSuccess(int token, Object result);
        void onError(Throwable e);
    }

    public static class SimpleCallback implements OperationCallback {
        @Override
        public void onSuccess(int token, Object result) {

        }

        @Override
        public void onError(Throwable e) {
            Log.e(TAG, "Operation failed, error -> " + e.getMessage());
        }
    }
}
