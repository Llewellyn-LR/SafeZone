package com.safezone.app.service;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.StorageException;

import java.io.IOException;

public final class DataError extends Exception {
    public enum Kind { NETWORK, PERMISSION_DENIED, UNAUTHENTICATED, NOT_FOUND, TOO_LARGE, INVALID, SERVER, UNKNOWN }

    public final Kind kind;

    private DataError(Kind kind, String message, Throwable cause) {
        super(message, cause);
        this.kind = kind;
    }

    public static DataError tooLarge() {
        return new DataError(Kind.TOO_LARGE, "Photo is too large — please choose one under 5 MB.", null);
    }

    public static DataError storageNotConfigured() {
        return new DataError(Kind.SERVER,
                "Photo upload isn't set up for this project yet. Ask an administrator to enable Firebase Storage.", null);
    }

    public static DataError unauthenticated() {
        return unauthenticated(null);
    }

    public static DataError from(Throwable t) {
        if (t instanceof DataError) return (DataError) t;
        if (t instanceof FirebaseNetworkException || t instanceof IOException) return network(t);
        if (t instanceof FirebaseFirestoreException) {
            switch (((FirebaseFirestoreException) t).getCode()) {
                case UNAVAILABLE:
                case DEADLINE_EXCEEDED: return network(t);
                case PERMISSION_DENIED:
                    return new DataError(Kind.PERMISSION_DENIED,
                            "You don't have permission to do that. If you're a moderator, "
                                    + "check that your account is on the moderators list.", t);
                case UNAUTHENTICATED: return unauthenticated(t);
                case NOT_FOUND: return new DataError(Kind.NOT_FOUND, "That item no longer exists.", t);
                default: return server(t);
            }
        }
        if (t instanceof StorageException) {
            switch (((StorageException) t).getErrorCode()) {
                case StorageException.ERROR_NOT_AUTHENTICATED: return unauthenticated(t);
                case StorageException.ERROR_NOT_AUTHORIZED:
                    return new DataError(Kind.PERMISSION_DENIED,
                            "You don't have permission to upload that photo.", t);
                case StorageException.ERROR_RETRY_LIMIT_EXCEEDED: return network(t);
                case StorageException.ERROR_OBJECT_NOT_FOUND:
                    return new DataError(Kind.NOT_FOUND, "That photo no longer exists.", t);
                default: return server(t);
            }
        }
        if (t instanceof IllegalArgumentException && t.getMessage() != null) {

            return new DataError(Kind.INVALID, t.getMessage(), t);
        }
        return new DataError(Kind.UNKNOWN, "Something went wrong. Please try again.", t);
    }

    private static DataError network(Throwable t) {
        return new DataError(Kind.NETWORK, "No connection. Check your internet and try again.", t);
    }

    private static DataError unauthenticated(Throwable t) {
        return new DataError(Kind.UNAUTHENTICATED, "Your session expired — please sign in again.", t);
    }

    private static DataError server(Throwable t) {
        return new DataError(Kind.SERVER, "Something went wrong on our side. Please try again.", t);
    }
}
