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

import com.baidu.duer.dcs.util.message.Payload;

import java.io.Serializable;
import java.util.List;

/**
 * Created by chenxiaojian on 17/11/1.
 */

public class RenderPlayerInfoPayload extends Payload implements Serializable {


    private ContentBean content;
    private String token;
    private List<ControlsBean> controls;

    public ContentBean getContent() {
        return content;
    }

    public void setContent(ContentBean content) {
        this.content = content;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public List<ControlsBean> getControls() {
        return controls;
    }

    public void setControls(List<ControlsBean> controls) {
        this.controls = controls;
    }

    public static class ContentBean {

        private ArtBean art;
        private String audioItemType;
        private LyricBean lyric;
        private int mediaLengthInMilliseconds;
        private ProviderBean provider;
        private String title;
        private String titleSubtext1;
        private String titleSubtext2;

        public ArtBean getArt() {
            return art;
        }

        public void setArt(ArtBean art) {
            this.art = art;
        }

        public String getAudioItemType() {
            return audioItemType;
        }

        public void setAudioItemType(String audioItemType) {
            this.audioItemType = audioItemType;
        }

        public LyricBean getLyric() {
            return lyric;
        }

        public void setLyric(LyricBean lyric) {
            this.lyric = lyric;
        }

        public int getMediaLengthInMilliseconds() {
            return mediaLengthInMilliseconds;
        }

        public void setMediaLengthInMilliseconds(int mediaLengthInMilliseconds) {
            this.mediaLengthInMilliseconds = mediaLengthInMilliseconds;
        }

        public ProviderBean getProvider() {
            return provider;
        }

        public void setProvider(ProviderBean provider) {
            this.provider = provider;
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

        public String getTitleSubtext2() {
            return titleSubtext2;
        }

        public void setTitleSubtext2(String titleSubtext2) {
            this.titleSubtext2 = titleSubtext2;
        }

        public static class ArtBean {
            private String src;

            public String getSrc() {
                return src;
            }

            public void setSrc(String src) {
                this.src = src;
            }

            @Override
            public String toString() {
                return "ArtBean{" +
                        "src='" + src + '\'' +
                        '}';
            }
        }

        public static class LyricBean {

            private String format;
            private String url;

            public String getFormat() {
                return format;
            }

            public void setFormat(String format) {
                this.format = format;
            }

            public String getUrl() {
                return url;
            }

            public void setUrl(String url) {
                this.url = url;
            }

            @Override
            public String toString() {
                return "LyricBean{" +
                        "format='" + format + '\'' +
                        ", url='" + url + '\'' +
                        '}';
            }
        }

        public static class ProviderBean {

            private LogoBean logo;
            private String name;

            public LogoBean getLogo() {
                return logo;
            }

            public void setLogo(LogoBean logo) {
                this.logo = logo;
            }

            public String getName() {
                return name;
            }

            public void setName(String name) {
                this.name = name;
            }

            public static class LogoBean {

                private String src;

                public String getSrc() {
                    return src;
                }

                public void setSrc(String src) {
                    this.src = src;
                }
            }

            @Override
            public String toString() {
                return "ProviderBean{" +
                        "logo=" + logo +
                        ", name='" + name + '\'' +
                        '}';
            }
        }

        @Override
        public String toString() {
            return "ContentBean{" +
                    "art=" + art +
                    ", audioItemType='" + audioItemType + '\'' +
                    ", lyric=" + lyric +
                    ", mediaLengthInMilliseconds=" + mediaLengthInMilliseconds +
                    ", provider=" + provider +
                    ", title='" + title + '\'' +
                    ", titleSubtext1='" + titleSubtext1 + '\'' +
                    ", titleSubtext2='" + titleSubtext2 + '\'' +
                    '}';
        }
    }

    public static class ControlsBean {

        private boolean enabled;
        private String name;
        private boolean selected;
        private String type;
        private String selectedValue;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getSelectedValue() {
            return selectedValue;
        }

        public void setSelectedValue(String selectedValue) {
            this.selectedValue = selectedValue;
        }

        @Override
        public String toString() {
            return "ControlsBean{" +
                    "enabled=" + enabled +
                    ", name='" + name + '\'' +
                    ", selected=" + selected +
                    ", type='" + type + '\'' +
                    ", selectedValue='" + selectedValue + '\'' +
                    '}';
        }
    }
}
