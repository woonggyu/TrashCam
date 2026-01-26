package com.example.camera

import android.content.Context
import com.example.UIDesign.R

/**
 * YOLO 모델의 className에 따라 다국어 분리수거 지침을 제공하는 클래스
 */
object RecyclingGuide {

    data class Guide(
        val category: String,
        val instruction: String,
        val disposalArea: String,
        val videoId: String
    )

    /**
     * context를 이용해 strings.xml에서 언어별 문자열을 불러옴
     */
    fun getGuide(context: Context, className: String): Guide {
        return when (className) {
            "Plastic bottle" -> Guide(
                context.getString(R.string.category_plastic_bottle),
                context.getString(R.string.instruction_plastic_bottle),
                context.getString(R.string.disposal_plastic_bottle),
                "K8Zi1xx1vSs"
            )
            "Poly bag" -> Guide(
                context.getString(R.string.category_poly_bag),
                context.getString(R.string.instruction_poly_bag),
                context.getString(R.string.disposal_poly_bag),
                "9AJJ5qwpfX8"
            )
            "Paper" -> Guide(
                context.getString(R.string.category_paper),
                context.getString(R.string.instruction_paper),
                context.getString(R.string.disposal_paper),
                "aAXUEDrtj0U&t=209s"
            )
            else -> Guide(
                context.getString(R.string.category_unknown),
                context.getString(R.string.instruction_unknown),
                context.getString(R.string.disposal_unknown),
                "BwzxJwjaznE"
            )
        }
    }
}
