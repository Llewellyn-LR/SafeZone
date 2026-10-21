package com.safezone.app.service;

import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;

final class TaskErrors {
    private TaskErrors() {}

    static <T> Task<T> mapFailures(Task<T> task) {
        return task.continueWithTask(t -> t.isSuccessful()
                ? t : Tasks.<T>forException(DataError.from(t.getException())));
    }
}
