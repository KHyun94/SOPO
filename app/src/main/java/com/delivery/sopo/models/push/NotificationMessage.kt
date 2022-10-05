package com.delivery.sopo.models.push

import com.delivery.sopo.R
import com.delivery.sopo.consts.EmojiConst
import com.delivery.sopo.enums.DeliveryStatus
import com.delivery.sopo.extensions.asEmoji
import com.delivery.sopo.models.parcel.Parcel

data class NotificationMessage(val title: String, val content: String, val summaryText: String? = null, val bigPicture: Int? = null)
{
    companion object
    {
        fun getUpdatePusMessage(parcel: Parcel.Common): NotificationMessage
        {
            val from = parcel.trackingInfo?.from?.name ?: "노네임"

            when(parcel.deliveryStatus)
            {
                DeliveryStatus.INFORMATION_RECEIVED.CODE ->
                {
                    val title = "${parcel.carrier} / ${parcel.waybillNum}"
                    val content = "${EmojiConst.EMOJI_MUSICAL_NOTE.asEmoji()}상품을 픽업했어요"
                    val summaryText = "${EmojiConst.EMOJI_MUSICAL_NOTE.asEmoji()} ${from}님이 보낸 택배가 준비되고 있어요"
                    val bigPicture = R.drawable.ic_noti_big_at_pickup
                    return NotificationMessage(title = title, content = content, summaryText = summaryText, bigPicture)
                }
                DeliveryStatus.AT_PICKUP.CODE ->
                {
                    val title = "${parcel.carrier} / ${parcel.waybillNum}"
                    val content = "${EmojiConst.EMOJI_MUSICAL_NOTE.asEmoji()}상품을 픽업했어요"
                    val summaryText = "${EmojiConst.EMOJI_MUSICAL_NOTE.asEmoji()} ${from}님이 보낸 택배가 준비되고 있어요"
                    val bigPicture = R.drawable.ic_noti_big_at_pickup
                    return NotificationMessage(title = title, content = content, summaryText = summaryText, bigPicture)
                }
                DeliveryStatus.IN_TRANSIT.CODE ->
                {
                    val currentLocation = parcel.trackingInfo?.progresses?.get(parcel.trackingInfo?.progresses?.lastIndex?:0)?.location?.name ?: "불명"

                    val title = "${parcel.carrier} / ${parcel.waybillNum}"
                    val content = "${EmojiConst.EMOJI_SPEECH_BALLOON.asEmoji()} ${from}님이 보낸 택배가 이동 중이에요."
                    val summaryText = "현재 위치:$currentLocation"
                    val bigPicture = R.drawable.ic_noti_big_intransit
                    return NotificationMessage(title = title, content = content, summaryText = summaryText, bigPicture)
                }
                DeliveryStatus.OUT_FOR_DELIVERY.CODE ->
                {
                    val title = "${parcel.carrier} / ${parcel.waybillNum}"
                    val content = "${EmojiConst.EMOJI_BLUE_HEART.asEmoji()} 택배가 동네에 도착했어요!"
                    val summaryText = "${EmojiConst.EMOJI_BLUE_HEART} ${from}님이 보내신 택배예요!"
                    val bigPicture = R.drawable.ic_noti_big_out_for_delivery
                    return NotificationMessage(title = title, content = content, summaryText = summaryText, bigPicture)
                }
                DeliveryStatus.DELIVERED.CODE ->
                {
                    val title = "${parcel.carrier} / ${parcel.waybillNum}"
//                    val content = "${EmojiConst.EMOJI_CHEERING_MEGAPHONE.asEmoji()} ${NotificationImpl.userRepo.getNickname()}님! 택배가 도착했대요."
                    val content = "${EmojiConst.EMOJI_CHEERING_MEGAPHONE.asEmoji()} 공백님! 택배가 도착했대요."
                    val summaryText = "${EmojiConst.EMOJI_CHEERING_MEGAPHONE} ${from}님이 보내신 택배예요!"
                    val bigPicture = R.drawable.ic_noti_big_delivered
                    return NotificationMessage(title = title, content = content, summaryText = summaryText, bigPicture)
                }
                DeliveryStatus.ORPHANED.CODE ->
                {
                    val title = "2주간 소식이 없는 택배가 있어요."
                    val content = "운송장이 맞는지 확인해주세요."
                    return NotificationMessage(title = title, content = content)
                }
                else -> throw IllegalArgumentException("불량 코드")
            }
        }
    }
}
