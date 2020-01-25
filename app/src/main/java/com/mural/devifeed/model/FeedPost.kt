package com.mural.devifeed.model

import android.os.Parcel
import android.os.Parcelable
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName

@Entity(
    tableName = "posts"
)
data class FeedPost(
    @PrimaryKey
    @SerializedName("id")
    val id: String,
    val name: String,
    @SerializedName("title")
    val title: String,
    @SerializedName("author")
    val author: String,
    @SerializedName("created_utc")
    val createdDate: Long,
    @SerializedName("thumbnail")
    val thumbnailUrl: String?,
    @SerializedName("num_comments")
    val num_comments: Int,
    //unread status
    val unread: Boolean
) : Parcelable {
    var topIndex: Int = -1

    constructor(parcel: Parcel) : this(
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readString().toString(),
        parcel.readLong(),
        parcel.readString(),
        parcel.readInt(),
        parcel.readByte() != 0.toByte()
    ) {
        topIndex = parcel.readInt()
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeString(name)
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeLong(createdDate)
        parcel.writeString(thumbnailUrl)
        parcel.writeInt(num_comments)
        parcel.writeByte(if (unread) 1 else 0)
        parcel.writeInt(topIndex)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<FeedPost> {
        override fun createFromParcel(parcel: Parcel): FeedPost {
            return FeedPost(parcel)
        }

        override fun newArray(size: Int): Array<FeedPost?> {
            return arrayOfNulls(size)
        }
    }
}