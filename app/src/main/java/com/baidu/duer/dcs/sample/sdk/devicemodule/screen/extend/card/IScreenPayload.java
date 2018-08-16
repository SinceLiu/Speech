package com.baidu.duer.dcs.sample.sdk.devicemodule.screen.extend.card;

/**
 * Created by oubin on 2018/6/21.
 */

public interface IScreenPayload {

    /**
     * Directive里对应的name字段
     */
    String name();

    /**
     * 要屏显的内容。
     * @return
     */
    String getScreenContent();

}
