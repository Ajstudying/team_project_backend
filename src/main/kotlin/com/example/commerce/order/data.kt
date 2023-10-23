package com.example.commerce.order

data class OrderRequest(
    val orderId: Long,        // 주문 id
)

data class OrderCreateRequest(
    val paymentMethod: String,       // 결제수단
    val paymentPrice: Int,           // 결제금액
    val orderStatus: String,         // 주문상태 (1: 완료, 2:취소)
    val orderItems: MutableList<OrderItemRequest>,  // 주문 item 목록
    val orderAddress: OrderAddressRequest,   // 배송지
    val deliveryMemo: String?,   // 배송요청사항
)

data class OrderResponse(
    val orderId: Long,          // 주문 id
    val paymentMethod: String,  // 결제수단
    val paymentPrice: Int,      // 결제금액
    val orderStatus: String,    // 주문상태 (1: 완료, 2:취소)
    val orderDate: String,      // 주문일자
    val orderItems: OrderItemResponse2, // 주문도서정보
)

data class OrderDeliveryResponse(
    val orderId: Long,          // 주문 id
    val paymentMethod: String,  // 결제수단
    val paymentPrice: Int,      // 결제금액
    val orderStatus: String,    // 주문상태 (1: 완료, 2:취소)
    val orderDate: String,      // 주문일자
    val deliveryName: String,   // 배송자명
    val deliveryPhone: String,  // 배송자 핸드폰번호
    val postcode: String,       // 우편번호
    val address: String,        // 기본주소
    val detailAddress: String,  // 상세주소
    val deliveryMemo: String,   // 배송요청사항
)

fun OrderCreateRequest.validate() =
    !((this.paymentMethod.equals("")) || (this.paymentPrice <= 0) || (this.orderStatus.equals("")))


data class OrderItemRequest(
    val itemId: Int,        // book id
    val quantity: Int,  // 수량
    val orderPrice: Int,// 주문금액
)

fun OrderItemRequest.validate() =
    !((this.itemId == 0) || (quantity == 0))

data class OrderItemResponse(
    val itemId: Int,        // book id
    val title: String,
    val cover: String,
    val author: String,
    val priceStandard: Int,
    val priceSales: Int,
    val categoryName: String,
    val quantity: Int,           // book 수량
)

data class OrderItemResponse2(
    val orderId: Long,        // 주문 id
    val itemId: Int,        // book id
    val title: String,
    val cover: String,
)

data class OrderAddressRequest(
    val deliveryName: String,   // 배송자명
    val deliveryPhone: String,  // 배송자 핸드폰번호
    val postcode: String,       // 우편번호
    val address: String,        // 기본주소
    val detailAddress: String,  // 상세주소
    val deliveryMemo: String,   // 배송요청사항
)