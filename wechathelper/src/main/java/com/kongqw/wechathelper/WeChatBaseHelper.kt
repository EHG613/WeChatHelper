package com.kongqw.wechathelper

import android.content.Context
import android.graphics.Bitmap
import androidx.annotation.Nullable
import com.kongqw.wechathelper.enums.Scene
import com.kongqw.wechathelper.listener.OnWeChatAuthLoginListener
import com.kongqw.wechathelper.listener.OnWeChatShareListener
import com.kongqw.wechathelper.utils.BitmapUtil
import com.kongqw.wechathelper.utils.MetaUtil
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory

open class WeChatBaseHelper(context: Context) {

    // 微信 APP ID
    protected val mWeChatAppId = MetaUtil.getWeChatAppId(context)
    // 通过WXAPIFactory工厂，获取IWXAPI的实例
    protected val api: IWXAPI = WXAPIFactory.createWXAPI(context, mWeChatAppId, true)

    companion object {

        private const val SCOPE = "snsapi_userinfo"
        private const val STATE = "lls_engzo_wechat_login"
        private const val THUMB_SIZE = 150

        var mOnWeChatShareListener: OnWeChatShareListener? = null
        var mOnWeChatAuthLoginListener: OnWeChatAuthLoginListener? = null
    }

    /**
     * 分享文字内容
     */
    fun shareText(content: String, scene: Scene, listener: OnWeChatShareListener): Boolean {
        mOnWeChatShareListener = listener
        // 设置分享文字
        val textObj = WXTextObject().apply {
            text = content
        }
        // 用 WXTextObject 对象初始化一个 WXMediaMessage 对象//用 WXTextObject 对象初始化一个 WXMediaMessage 对象
        val msg = WXMediaMessage().apply {
            mediaObject = textObj
            description = content
        }
        val req = SendMessageToWX.Req().apply {
            // transaction 字段用于唯一标示一个请求
            transaction = System.currentTimeMillis().toString()
            message = msg
            this.scene = scene.type
        }
        mOnWeChatShareListener?.onWeChatShareStart()
        return api.sendReq(req)
    }


    /**
     * 分享图片
     */
    fun shareImage(
        bmp: Bitmap,
        scene: Scene,
        listener: OnWeChatShareListener,
        @Nullable thumbWidth: Int = THUMB_SIZE,
        @Nullable thumbHeight: Int = THUMB_SIZE
    ): Boolean {
        mOnWeChatShareListener = listener
        //初始化 WXImageObject 和 WXMediaMessage 对象
        val imgObj = WXImageObject(bmp)
        //设置缩略图
        val thumbBmp = Bitmap.createScaledBitmap(bmp, thumbWidth, thumbHeight, true)
        // 释放原图
        bmp.recycle()

        val msg = WXMediaMessage().apply {
            mediaObject = imgObj
            thumbData = BitmapUtil.bitmapToByteArray(thumbBmp, true)
        }

        //构造一个Req
        val req = SendMessageToWX.Req().apply {
            transaction = String.format("img%s", System.currentTimeMillis())
            message = msg
            this.scene = scene.type
            // req.userOpenId = getOpenId()
        }
        mOnWeChatShareListener?.onWeChatShareStart()
        return api.sendReq(req)
    }


    /**
     * 分享音乐
     */
    fun shareMusic(
        bitmap: Bitmap,
        scene: Scene,
        musicUrl: String,
        title: String,
        description: String,
        listener: OnWeChatShareListener,
        @Nullable thumbWidth: Int = THUMB_SIZE,
        @Nullable thumbHeight: Int = THUMB_SIZE
    ): Boolean {
        mOnWeChatShareListener = listener
        // 初始化一个WXMusicObject，填写url
        val music = WXMusicObject().apply {
            this.musicUrl = musicUrl
        }

        //设置缩略图
        val thumbBmp = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, true)

        // 用 WXMusicObject 对象初始化一个 WXMediaMessage 对象
        val msg = WXMediaMessage(music).apply {
            this.title = title
            this.description = description
            // 设置音乐缩略图
            thumbData = BitmapUtil.bitmapToByteArray(thumbBmp, true)
        }


        // 构造一个Req
        val req = SendMessageToWX.Req().apply {
            transaction = String.format("music%s", System.currentTimeMillis())
            message = msg
            this.scene = scene.type
            // userOpenId = getOpenId();
        }
        mOnWeChatShareListener?.onWeChatShareStart()
        // 调用api接口，发送数据到微信
        return api.sendReq(req)
    }


    /**
     * 分享视频
     */
    fun shareVideo(
        bitmap: Bitmap,
        scene: Scene,
        videoUrl: String,
        title: String,
        description: String,
        listener: OnWeChatShareListener,
        @Nullable thumbWidth: Int = THUMB_SIZE,
        @Nullable thumbHeight: Int = THUMB_SIZE
    ): Boolean {
        mOnWeChatShareListener = listener
        //初始化一个WXVideoObject，填写url
        val video = WXVideoObject().apply {
            this.videoUrl = videoUrl
        }

        //设置缩略图
        val thumbBmp = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, true)

        // 用 WXMusicObject 对象初始化一个 WXMediaMessage 对象
        val msg = WXMediaMessage(video).apply {
            this.title = title
            this.description = description
            // 设置音乐缩略图
            thumbData = BitmapUtil.bitmapToByteArray(thumbBmp, true)
        }


        // 构造一个Req
        val req = SendMessageToWX.Req().apply {
            transaction = String.format("video%s", System.currentTimeMillis())
            message = msg
            this.scene = scene.type
            // userOpenId = getOpenId();
        }
        mOnWeChatShareListener?.onWeChatShareStart()
        // 调用api接口，发送数据到微信
        return api.sendReq(req)
    }


    /**
     * 网页分享
     */
    fun shareWebPage(
        bitmap: Bitmap,
        scene: Scene,
        webPageUrl: String,
        title: String,
        description: String,
        listener: OnWeChatShareListener,
        @Nullable thumbWidth: Int = THUMB_SIZE,
        @Nullable thumbHeight: Int = THUMB_SIZE
    ): Boolean {
        mOnWeChatShareListener = listener
        // 初始化一个WXWebpageObject，填写url
        val webpage = WXWebpageObject().apply {
            this.webpageUrl = webPageUrl
        }

        //设置缩略图
        val thumbBmp = Bitmap.createScaledBitmap(bitmap, thumbWidth, thumbHeight, true)

        // 用 WXMusicObject 对象初始化一个 WXMediaMessage 对象
        val msg = WXMediaMessage(webpage).apply {
            this.title = title
            this.description = description
            // 设置音乐缩略图
            thumbData = BitmapUtil.bitmapToByteArray(thumbBmp, true)
        }
        // 构造一个Req
        val req = SendMessageToWX.Req().apply {
            transaction = String.format("webpage%s", System.currentTimeMillis())
            message = msg
            this.scene = scene.type
            // userOpenId = getOpenId();
        }
        mOnWeChatShareListener?.onWeChatShareStart()
        // 调用api接口，发送数据到微信
        return api.sendReq(req)
    }

    /**
     * 授权登录
     */
    fun authLogin(listener: OnWeChatAuthLoginListener) {
        mOnWeChatAuthLoginListener = listener
        val req = SendAuth.Req()
        req.scope = SCOPE
        req.state = STATE
        mOnWeChatAuthLoginListener?.onWeChatAuthLoginStart()
        api.sendReq(req)
    }
}