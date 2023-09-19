package com.kongqw.wechathelper

import android.content.Context
import android.graphics.Bitmap
import android.os.Build
import androidx.annotation.Nullable
import com.kongqw.wechathelper.enums.Scene
import com.kongqw.wechathelper.enums.SceneMiniProgramType
import com.kongqw.wechathelper.listener.IPaymentParams
import com.kongqw.wechathelper.listener.OnWeChatAuthLoginListener
import com.kongqw.wechathelper.listener.OnWeChatPaymentListener
import com.kongqw.wechathelper.listener.OnWeChatShareListener
import com.kongqw.wechathelper.utils.BitmapUtil
import com.kongqw.wechathelper.utils.FileUtils
import com.kongqw.wechathelper.utils.Logger
import com.kongqw.wechathelper.utils.MetaUtil
import com.tencent.mm.opensdk.modelmsg.*
import com.tencent.mm.opensdk.modelpay.PayReq
import com.tencent.mm.opensdk.openapi.IWXAPI
import com.tencent.mm.opensdk.openapi.WXAPIFactory
import com.tencent.mm.opensdk.modelmsg.SendMessageToWX

internal class WeChatBaseHelper(val context: Context) {

    // 微信 APP ID
    private val mWeChatAppId = MetaUtil.getWeChatAppId(context)

    // 通过WXAPIFactory工厂，获取IWXAPI的实例
    private val api: IWXAPI = WXAPIFactory.createWXAPI(context, mWeChatAppId, true)

    companion object {

        private val TAG = WeChatBaseHelper::class.java.simpleName

        private const val SCOPE = "snsapi_userinfo"
        private const val STATE = "lls_engzo_wechat_login"
        const val THUMB_SIZE = 150

        var mOnWeChatShareListener: OnWeChatShareListener? = null
        var mOnWeChatAuthLoginListener: OnWeChatAuthLoginListener? = null
        var mOnWeChatPaymentListener: OnWeChatPaymentListener? = null
    }

    private fun registerApp(listener: (() -> Unit)) {
        if (WeChatClient.isRegistered) {
            listener()
            return
        }
        // 将应用的appId注册到微信
        WeChatClient.isRegistered = api.registerApp(mWeChatAppId)
        listener()
    }

    /**
     * 分享文字内容
     */
    fun shareText(content: String, scene: Scene, listener: OnWeChatShareListener): Boolean {

        val isInitWeChat = api.registerApp(mWeChatAppId)
        Logger.i(TAG, "isInitWeChat = $isInitWeChat  mWeChatAppId = $mWeChatAppId")

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
        thumbWidth: Int = THUMB_SIZE,
        thumbHeight: Int = THUMB_SIZE
    ): Boolean {
        val isInitWeChat = api.registerApp(mWeChatAppId)
        Logger.i(TAG, "isInitWeChat = $isInitWeChat  mWeChatAppId = $mWeChatAppId")

        return if (isSupportFileProvider()) {
            shareImageByFileProvider(bmp, scene, listener, thumbWidth, thumbHeight)
        } else {
            shareImageNormal(bmp, scene, listener, thumbWidth, thumbHeight)
        }
    }

    /**
     * 分享图片
     */
    private fun shareImageNormal(
        bmp: Bitmap,
        scene: Scene,
        listener: OnWeChatShareListener,
        thumbWidth: Int = THUMB_SIZE,
        thumbHeight: Int = THUMB_SIZE
    ): Boolean {
        val isInitWeChat = api.registerApp(mWeChatAppId)
        Logger.i(TAG, "isInitWeChat = $isInitWeChat  mWeChatAppId = $mWeChatAppId")

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
     * 分享图片
     */
    private fun shareImageByFileProvider(
        bmp: Bitmap,
        scene: Scene,
        listener: OnWeChatShareListener,
        thumbWidth: Int = THUMB_SIZE,
        thumbHeight: Int = THUMB_SIZE
    ): Boolean {
        val isInitWeChat = api.registerApp(mWeChatAppId)
        Logger.i(TAG, "isInitWeChat = $isInitWeChat  mWeChatAppId = $mWeChatAppId")

        mOnWeChatShareListener = listener

        FileUtils.saveBitmap(context, bmp) { isSuccess, file ->
            if (isSuccess) {
                val contentPath = FileUtils.getFileUri(context, file)
                val imageObject = WXImageObject()
                imageObject.setImagePath(contentPath)
                val msg = WXMediaMessage(imageObject)

                // 设置缩略图
                val thumbBmp = Bitmap.createScaledBitmap(bmp, thumbWidth, thumbHeight, true)
                bmp.recycle()
                msg.thumbData = BitmapUtil.bitmapToByteArray(thumbBmp, true)

                //构建一个Req
                val req = SendMessageToWX.Req()
                req.transaction = String.format("img%s", System.currentTimeMillis())

                req.message = msg
                req.scene = scene.type
                api.sendReq(req)

                mOnWeChatShareListener?.onWeChatShareStart()
            } else {
                mOnWeChatShareListener?.onWeChatShareSentFailed(null)
            }
        }

        return true
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
        thumbWidth: Int = THUMB_SIZE,
        thumbHeight: Int = THUMB_SIZE
    ): Boolean {
        val isInitWeChat = api.registerApp(mWeChatAppId)
        Logger.i(TAG, "isInitWeChat = $isInitWeChat  mWeChatAppId = $mWeChatAppId")
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
        thumbWidth: Int = THUMB_SIZE,
        thumbHeight: Int = THUMB_SIZE
    ): Boolean {
        val isInitWeChat = api.registerApp(mWeChatAppId)
        Logger.i(TAG, "isInitWeChat = $isInitWeChat  mWeChatAppId = $mWeChatAppId")
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
        thumbWidth: Int = THUMB_SIZE,
        thumbHeight: Int = THUMB_SIZE
    ): Boolean {
        val isInitWeChat = api.registerApp(mWeChatAppId)
        Logger.i(TAG, "isInitWeChat = $isInitWeChat  mWeChatAppId = $mWeChatAppId")
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
     * 网页分享
     */
    fun shareMiniProgram(
        bitmap: Bitmap,
        scene: Scene,
        miniprogramType:SceneMiniProgramType,
        userName: String,
        path: String,
        webPageUrl: String,
        title: String,
        description: String,
        listener: OnWeChatShareListener
    ): Boolean {
        val isInitWeChat = api.registerApp(mWeChatAppId)
        Logger.i(TAG, "isInitWeChat = $isInitWeChat  mWeChatAppId = $mWeChatAppId")
        mOnWeChatShareListener = listener
        // 初始化一个WXWebpageObject，填写url
        val miniProgramObj = WXMiniProgramObject().apply {
            this.miniprogramType = miniprogramType.type
            this.userName = userName
            this.path = path
            this.webpageUrl = webPageUrl
            this.withShareTicket = true
        }


        // 用 WXMusicObject 对象初始化一个 WXMediaMessage 对象
        val msg = WXMediaMessage(miniProgramObj).apply {
            this.title = title
            this.description = description
            // 设置音乐缩略图
            thumbData = BitmapUtil.bitmapToByteArray2(bitmap, 128)
        }
        // 构造一个Req
        val req = SendMessageToWX.Req().apply {
            transaction = String.format("miniprogram%s", System.currentTimeMillis())
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
        val isInitWeChat = api.registerApp(mWeChatAppId)
        Logger.i(TAG, "isInitWeChat = $isInitWeChat  mWeChatAppId = $mWeChatAppId")
        mOnWeChatAuthLoginListener = listener
        val req = SendAuth.Req()
        req.scope = SCOPE
        req.state = STATE
        mOnWeChatAuthLoginListener?.onWeChatAuthLoginStart()
        val sendReq = api.sendReq(req)
        Logger.i(TAG, "authLogin sendReq = $sendReq")
    }

    /**
     * 微信支付
     */
    fun payment(params: IPaymentParams, listener: OnWeChatPaymentListener) {
        val isInitWeChat = api.registerApp(mWeChatAppId)
        Logger.i(TAG, "isInitWeChat = $isInitWeChat  mWeChatAppId = $mWeChatAppId")
        mOnWeChatPaymentListener = listener
        val req = PayReq().apply {
            appId = params.onAppId()
            partnerId = params.onPartnerId()
            prepayId = params.onPrepayId()
            packageValue = params.onPackageValue()
            nonceStr = params.onNonceStr()
            timeStamp = params.onTimeStamp()
            sign = params.onSign()
        }
        mOnWeChatPaymentListener?.onWeChatPaymentStart()
        val sendReq = api.sendReq(req)
        Logger.i(TAG, "payment sendReq = $sendReq")
    }


    // 判断微信版本是否为7.0.13及以上
    // 判断Android版本是否11 及以上
    private fun isSupportFileProvider(): Boolean {
        return api.wxAppSupportAPI >= 0x27000D00 && Build.VERSION.SDK_INT >= 30
    }
}