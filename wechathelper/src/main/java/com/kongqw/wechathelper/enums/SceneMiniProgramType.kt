package com.kongqw.wechathelper.enums

enum class SceneMiniProgramType(val type: Int) {
    /**正式版*/
    MINIPTOGRAM_TYPE_RELEASE(0),

    /**测试版*/
    MINIPROGRAM_TYPE_TEST(1),

    /**
     * 预览版
     */
    MINIPROGRAM_TYPE_PREVIEW(2),


}