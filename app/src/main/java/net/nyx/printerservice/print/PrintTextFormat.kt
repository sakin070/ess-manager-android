package net.nyx.printerservice.print

import android.os.Parcel
import android.os.Parcelable

class PrintTextFormat : Parcelable {
    var textSize = 28 // 字符串大小,px
    var isUnderline = false // 下划线
    var textScaleX = 1.0f // 字体的横向缩放 参数值0-1表示字体缩小 1表示正常 大于1表示放大
    var textScaleY = 1.0f
    var letterSpacing = 0f // 列间距
    var lineSpacing = 0f //行间距
    var topPadding = 0
    var leftPadding = 0
    var ali = 0 // 对齐方式, 默认0. 0--居左, 1--居中, 2--居右
    var path // 自定义字库文件路径
            : String? = null

    constructor() {}
    protected constructor(`in`: Parcel) {
        textSize = `in`.readInt()
        isUnderline = `in`.readByte().toInt() != 0
        textScaleX = `in`.readFloat()
        textScaleY = `in`.readFloat()
        letterSpacing = `in`.readFloat()
        lineSpacing = `in`.readFloat()
        topPadding = `in`.readInt()
        leftPadding = `in`.readInt()
        ali = `in`.readInt()
        path = `in`.readString()
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeInt(textSize)
        dest.writeByte((if (isUnderline) 1 else 0).toByte())
        dest.writeFloat(textScaleX)
        dest.writeFloat(textScaleY)
        dest.writeFloat(letterSpacing)
        dest.writeFloat(lineSpacing)
        dest.writeInt(topPadding)
        dest.writeInt(leftPadding)
        dest.writeInt(ali)
        dest.writeString(path)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object {
        @JvmField
        val CREATOR: Parcelable.Creator<PrintTextFormat?> =
            object : Parcelable.Creator<PrintTextFormat?> {
                override fun createFromParcel(`in`: Parcel): PrintTextFormat {
                    return PrintTextFormat(`in`)
                }

                override fun newArray(size: Int): Array<PrintTextFormat?> {
                    return arrayOfNulls(size)
                }
            }
    }
}

