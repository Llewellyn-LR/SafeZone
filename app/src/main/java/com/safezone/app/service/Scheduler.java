package com.safezone.app.service;

import android.os.Handler;
import android.os.Looper;

public interface Scheduler {
    interface Cancelable { void cancel(); }

    Cancelable postDelayed(Runnable r, long delayMs);

    Scheduler MAIN = new Scheduler() {
        private Handler handler;
        @Override public Cancelable postDelayed(Runnable r, long delayMs) {
            if (handler == null) handler = new Handler(Looper.getMainLooper());
            final Handler h = handler;
            h.postDelayed(r, delayMs);
            return () -> h.removeCallbacks(r);
        }
    };

    Scheduler IMMEDIATE = (r, d) -> { r.run(); return () -> { }; };
}
