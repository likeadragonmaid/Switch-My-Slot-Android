/********************************************************************************************
 org/shou/switchmyslot/MainActivityViewModel.java: MainActivityViewModel for Switch My Slot Android App

 Copyright (C) 2010 - 2021 Shou

 MIT License

 Permission is hereby granted, free of charge, to any person obtaining a copy
 of this software and associated documentation files (the "Software"), to deal
 in the Software without restriction, including without limitation the rights
 to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 copies of the Software, and to permit persons to whom the Software is
 furnished to do so, subject to the following conditions:

 The above copyright notice and this permission notice shall be included in all
 copies or substantial portions of the Software.

 THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 SOFTWARE.
 */

package org.shou.switchmyslot;

import android.util.Log;

import androidx.lifecycle.ViewModel;

import com.stericson.RootShell.execution.Shell;

import java.io.IOException;

public class MainActivityViewModel extends ViewModel {

    private Shell shell;

    public void setShell(Shell shell) {
        this.shell = shell;
    }

    public Shell getShell() {
        return shell;
    }

    public void closeShell() {
        try {
            if (shell != null && !shell.isClosed) {
                shell.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        closeShell();
    }
}
