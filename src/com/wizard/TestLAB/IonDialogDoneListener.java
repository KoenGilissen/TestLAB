package com.wizard.TestLAB;

import java.io.File;

/**
 * Interface for Dialog Fragment callbacks
 * @author K. Gilissen
 * @version 1.0
 */
public interface IonDialogDoneListener
{
    void onDialogDone(String tag, boolean cancelled, String message);

}
