package com.sven.sjcalendar;

import android.content.Context;

import com.github.moduth.blockcanary.BlockCanaryContext;
import com.github.moduth.blockcanary.internal.BlockInfo;

import timber.log.Timber;

/**
 * Created by Sven.J on 18-5-4.
 */
public class AppBlockCanaryContext extends BlockCanaryContext {

    @Override
    public void onBlock(Context context, BlockInfo blockInfo) {
        Timber.i("block info : \r\n%s" + blockInfo.toString());
    }

}
