package com.safezone.app.service;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.FirebaseFirestoreException.Code;

import org.junit.Test;

import java.io.IOException;

public class DataErrorTest {
    private static DataError fs(Code code) {
        return DataError.from(new FirebaseFirestoreException("raw " + code.name(), code));
    }

    @Test
    public void networkFailuresMapToNetwork() {
        assertEquals(DataError.Kind.NETWORK, DataError.from(new FirebaseNetworkException("x")).kind);
        assertEquals(DataError.Kind.NETWORK, DataError.from(new IOException("x")).kind);
        assertEquals(DataError.Kind.NETWORK, fs(Code.UNAVAILABLE).kind);
        assertEquals(DataError.Kind.NETWORK, fs(Code.DEADLINE_EXCEEDED).kind);
    }

    @Test
    public void firestoreCodesMapToTheirKinds() {
        assertEquals(DataError.Kind.PERMISSION_DENIED, fs(Code.PERMISSION_DENIED).kind);
        assertEquals(DataError.Kind.UNAUTHENTICATED, fs(Code.UNAUTHENTICATED).kind);
        assertEquals(DataError.Kind.NOT_FOUND, fs(Code.NOT_FOUND).kind);
        assertEquals(DataError.Kind.SERVER, fs(Code.INTERNAL).kind);
    }

    @Test
    public void rawFirebaseTextNeverReachesTheUser() {
        String message = fs(Code.PERMISSION_DENIED).getMessage();
        assertFalse(message.contains("PERMISSION_DENIED"));
        assertFalse(message.contains("raw"));
        assertTrue(message.contains("permission"));
    }

    @Test
    public void ourOwnValidationMessagesPassThroughAsInvalid() {
        DataError e = DataError.from(new IllegalArgumentException("This report has no location."));
        assertEquals(DataError.Kind.INVALID, e.kind);
        assertEquals("This report has no location.", e.getMessage());
    }

    @Test
    public void unknownExceptionsGetAGenericMessage() {
        DataError e = DataError.from(new RuntimeException("internal detail"));
        assertEquals(DataError.Kind.UNKNOWN, e.kind);
        assertFalse(e.getMessage().contains("internal detail"));
    }

    @Test
    public void aDataErrorIsNotWrappedTwice() {
        DataError original = DataError.tooLarge();
        assertSame(original, DataError.from(original));
        assertEquals(DataError.Kind.TOO_LARGE, original.kind);
    }
}
