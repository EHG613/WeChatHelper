package com.kongqw.wechathelper.enums

enum class Scene(val type: Int) {
    /**分享到对话:支持卡片和小程序*/
    Session(0),

    /**分享到朋友圈*/
    Timeline(1),

    /**
     * 分享到收藏
     */
    Favorite(2),

    /**
     * 分享指定场景联系人
     */
    SpecifiedContact(3)
}