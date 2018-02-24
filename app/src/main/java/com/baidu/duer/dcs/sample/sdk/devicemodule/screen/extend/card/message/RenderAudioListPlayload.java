/*
 * *
 * Copyright (c) 2017 Baidu, Inc. All Rights Reserved.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */
package com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card.message;

import com.baidu.duer.dcs.framework.message.Payload;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chenxiaojian on 17/11/1.
 */

public class RenderAudioListPlayload extends Payload implements Serializable {

    private int nowPlayingIndex;
    private String title;
    private String token;
    private List<AudioItemsBean> audioItems;

    public int getNowPlayingIndex() {
        return nowPlayingIndex;
    }

    public void setNowPlayingIndex(int nowPlayingIndex) {
        this.nowPlayingIndex = nowPlayingIndex;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<AudioItemsBean> getAudioItems() {
        return audioItems;
    }

    public void setAudioItems(List<AudioItemsBean> audioItems) {
        this.audioItems = audioItems;
    }

    public static class AudioItemsBean {

        private ImageBean image;
        private boolean isFavorited;
        private String title;
        private String titleSubtext1;
        private String url;

        public ImageBean getImage() {
            return image;
        }

        public void setImage(ImageBean image) {
            this.image = image;
        }

        public boolean isIsFavorited() {
            return isFavorited;
        }

        public void setIsFavorited(boolean isFavorited) {
            this.isFavorited = isFavorited;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getTitleSubtext1() {
            return titleSubtext1;
        }

        public void setTitleSubtext1(String titleSubtext1) {
            this.titleSubtext1 = titleSubtext1;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public static class ImageBean {

            private String src;

            public String getSrc() {
                return src;
            }

            public void setSrc(String src) {
                this.src = src;
            }
        }
    }
}
