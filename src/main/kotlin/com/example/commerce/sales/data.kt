package com.example.commerce.sales

data class OrderMasterForSales(
        var id: Long,
        val name: String,
        val address: String
)

data class OrderSales(
        var id: Long,
        val name: String,
        val address: String,
        val orderSalesItems: List<OrderSalesItem>
)

data class OrderSalesItem(
        var id: Long,
        val productId: Long,
        val productName: String,
        val quantity: Int,
        val unitPrice: Long
)