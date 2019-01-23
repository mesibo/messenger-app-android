/** Copyright (c) 2019 Mesibo
 * https://mesibo.com
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification,
 * are permitted provided that the terms and condition mentioned on https://mesibo.com
 * as well as following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list
 * of conditions, the following disclaimer and links to documentation and source code
 * repository.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this
 * list of conditions and the following disclaimer in the documentation and/or other
 * materials provided with the distribution.
 *
 * Neither the name of Mesibo nor the names of its contributors may be used to endorse
 * or promote products derived from this software without specific prior written
 * permission.
 *
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT,
 * INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
 * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
 * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 *
 * Documentation
 * https://mesibo.com/documentation/
 *
 * Source Code Repository
 * https://github.com/mesibo/messenger-app-android
 *
 */

package org.mesibo.messenger;

import android.os.Bundle;

import com.google.gson.Gson;
import com.mesibo.api.Mesibo;

public class MesiboFileTransferHelper implements Mesibo.FileTransferHandler {

    private static Gson mGson = new Gson();
    private static Mesibo.HttpQueue mQueue = new Mesibo.HttpQueue(4, 0);
    public static class MesiboUrl {
        public String op;
        public String file;
        public String result;
        public String xxx;

        MesiboUrl() {
            result = null;
            op = null;
            xxx = null;
            file = null;
        }
    }

    MesiboFileTransferHelper() {
        	Mesibo.addListener(this);
    }
    
    public boolean Mesibo_onStartUpload(Mesibo.MessageParams params, final Mesibo.FileInfo file) {

        // we don't need to check origin the way we do in download
        if(Mesibo.getNetworkConnectivity() != Mesibo.CONNECTIVITY_WIFI && !file.userInteraction)
            return false;

        // limit simultaneous upload or download
        if(mUploadCounter >= 3 && !file.userInteraction)
            return false;

        final long mid = file.mid;

        Bundle b = new Bundle();
        b.putString("op", "upload");
        b.putString("token", SampleAPI.getToken());
        b.putLong("mid", mid);
        b.putInt("profile", 0);

        updateUploadCounter(1);
        Mesibo.Http http = new Mesibo.Http();

        http.url = SampleAPI.getUploadUrl();
        http.postBundle = b;
        http.uploadFile = file.getPath();
        http.uploadFileField = "photo";
        http.other = file;
        file.setFileTransferContext(http);

        http.listener = new Mesibo.HttpListener() {
            @Override
            public boolean Mesibo_onHttpProgress(Mesibo.Http config, int state, int percent) {
                Mesibo.FileInfo f = (Mesibo.FileInfo)config.other;

                if(100 == percent && Mesibo.Http.STATE_DOWNLOAD == state) {
                    String response = config.getDataString();
                    MesiboUrl mesibourl = null;
                    try {
                        mesibourl = mGson.fromJson(response, MesiboUrl.class);
                    } catch (Exception e) {}

                    if(null == mesibourl || null == mesibourl.file) {
                        Mesibo.updateFileTransferProgress(f, -1, Mesibo.FileInfo.STATUS_FAILED);
                        return false;
                    }

                    //TBD, f.setPath if video is re-compressed
                    f.setUrl(mesibourl.file);
                }

                int status = f.getStatus();
                if(100 == percent || status != Mesibo.FileInfo.STATUS_RETRYLATER) {
                    status = Mesibo.FileInfo.STATUS_INPROGRESS;
                    if(percent < 0)
                        status = Mesibo.FileInfo.STATUS_RETRYLATER;
                }

                if(percent < 100 || (100 == percent && Mesibo.Http.STATE_DOWNLOAD == state))
                    Mesibo.updateFileTransferProgress(f, percent, status);

                if((100 == percent && Mesibo.Http.STATE_DOWNLOAD == state) || status != Mesibo.FileInfo.STATUS_INPROGRESS)
                    updateUploadCounter(-1);

                return ((100 == percent && Mesibo.Http.STATE_DOWNLOAD == state) || status != Mesibo.FileInfo.STATUS_RETRYLATER);
            }
        };

        if(null != mQueue)
            mQueue.queue(http);
        else if(http.execute()) {

        }

        return true;

    }

    public boolean Mesibo_onStartDownload(final Mesibo.MessageParams params, final Mesibo.FileInfo file) {

        //TBD, check file type and size to decide automatic download
        if(!SampleAPI.getMediaAutoDownload() && Mesibo.getNetworkConnectivity() != Mesibo.CONNECTIVITY_WIFI && !file.userInteraction)
            return false;

        // only realtime messages to be downloaded in automatic mode.
        if(Mesibo.ORIGIN_REALTIME != params.origin && !file.userInteraction)
            return false;

        // limit simultaneous upload or download, 1st condition is redundant but for reference only
        if(Mesibo.getNetworkConnectivity() != Mesibo.CONNECTIVITY_WIFI && mDownloadCounter >= 3 && !file.userInteraction)
            return false;

        updateDownloadCounter(1);

        final long mid = file.mid;

        String url = file.getUrl();
        if(!url.toLowerCase().startsWith("http://") && !url.toLowerCase().startsWith("https://")) {
            url = SampleAPI.getFileUrl() + url;
        }

        Mesibo.Http http = new Mesibo.Http();

        http.url = url;
        http.downloadFile = file.getPath();
        http.resume = true;
        http.maxRetries = 10;
        http.other = file;
        file.setFileTransferContext(http);

        http.listener = new Mesibo.HttpListener() {
            @Override
            public boolean Mesibo_onHttpProgress(Mesibo.Http http, int state, int percent) {
                Mesibo.FileInfo f = (Mesibo.FileInfo)http.other;

                int status = Mesibo.FileInfo.STATUS_INPROGRESS;

                //TBD, we can simplify this now, don't need separate handling
                if(Mesibo.FileInfo.SOURCE_PROFILE == f.source) {
                    if(100 == percent) {
                        Mesibo.updateFileTransferProgress(f, percent, Mesibo.FileInfo.STATUS_INPROGRESS);
                    }
                } else {

                    status = f.getStatus();
                    if(100 == percent || status != Mesibo.FileInfo.STATUS_RETRYLATER) {
                        status = Mesibo.FileInfo.STATUS_INPROGRESS;
                        if(percent < 0)
                            status = Mesibo.FileInfo.STATUS_RETRYLATER;
                    }

                    Mesibo.updateFileTransferProgress(f, percent, status);

                }

                if(100 == percent || status != Mesibo.FileInfo.STATUS_INPROGRESS)
                    updateDownloadCounter(-1);

                return (100 == percent  || status != Mesibo.FileInfo.STATUS_RETRYLATER);
            }
        };

        if(null != mQueue)
            mQueue.queue(http);
        else if(http.execute()) {

        }

        return true;
    }

    @Override
    public boolean Mesibo_onStartFileTransfer(Mesibo.FileInfo file) {
        if(Mesibo.FileInfo.MODE_DOWNLOAD == file.mode)
            return Mesibo_onStartDownload(file.getParams(), file);

        return Mesibo_onStartUpload(file.getParams(), file);
    }

    @Override
    public boolean Mesibo_onStopFileTransfer(Mesibo.FileInfo file) {
        Mesibo.Http http = (Mesibo.Http) file.getFileTransferContext();
        if(null != http)
            http.cancel();

        return true;
    }

    private int mDownloadCounter = 0, mUploadCounter = 0;
    public synchronized int updateDownloadCounter(int increment) {
        mDownloadCounter += increment;
        return mDownloadCounter;
    }

    public synchronized int updateUploadCounter(int increment) {
        mUploadCounter += increment;
        return mUploadCounter;
    }
}


